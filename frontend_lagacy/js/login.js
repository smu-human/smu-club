// FRONTEND/js/login.js

/**********************
 * 기본 설정/상수
 **********************/
const API_BASE_URL = "http://localhost:8080/api/v1";
const LOGIN_PATH = "/public/auth/login";
const REISSUE_PATH = "/public/auth/reissue";

// Storage Keys (snake_case)
const ACCESS_TOKEN_KEY = "access_token";
const REFRESH_TOKEN_KEY = "refresh_token";
const TOKEN_EXPIRES_AT = "token_expires_at"; // (선택) 초 단위 만료 저장

/**********************
 * DOM
 **********************/
const loginForm = document.getElementById("loginForm");
const studentIdInput = document.getElementById("studentId");
const passwordInput = document.getElementById("password");
const passwordToggle = document.getElementById("passwordToggle");
const eyeIcon = document.getElementById("eyeIcon");
const rememberMeCheckbox = document.getElementById("rememberMe");
const loginBtn = document.getElementById("loginBtn");
const loginText = document.getElementById("loginText");
const loginSpinner = document.getElementById("loginSpinner");

/**********************
 * 초기화
 **********************/
document.addEventListener("DOMContentLoaded", () => {
  setupEventListeners();
  loadSavedCredentials();
});

/**********************
 * 이벤트
 **********************/
function setupEventListeners() {
  loginForm.addEventListener("submit", handleLogin);
  passwordToggle.addEventListener("click", togglePassword);
}

/**********************
 * 비밀번호 보기/숨기기
 **********************/
function togglePassword() {
  const isPassword = passwordInput.type === "password";
  passwordInput.type = isPassword ? "text" : "password";

  eyeIcon.innerHTML = isPassword
    ? '<path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/>'
    : '<path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>';
}

/**********************
 * 로그인 처리
 **********************/
async function handleLogin(e) {
  e.preventDefault();

  const studentId = studentIdInput.value.trim();
  const password = passwordInput.value.trim();
  const remember = rememberMeCheckbox.checked;

  if (!studentId || !password) {
    alert("학번과 비밀번호를 모두 입력해주세요.");
    return;
  }

  setLoadingState(true);
  try {
    // 실제 로그인 API 호출
    const res = await fetch(API_BASE_URL + LOGIN_PATH, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ studentId, password }),
    });

    if (!res.ok) {
      throw new Error("로그인 실패");
    }

    // 백엔드 응답 스키마에 맞게 조정:
    // 예) { accessToken, refreshToken, expiresIn } 혹은 { result: { accessToken, refreshToken, expiresIn } }
    const data = await res.json();
    const payload = data.result ?? data;

    const accessToken = payload.accessToken;
    const refreshToken = payload.refreshToken;
    const expiresIn = payload.expiresIn ?? null; // 초 단위(선택)

    if (!accessToken || !refreshToken) {
      throw new Error("토큰 정보가 없습니다.");
    }

    // 토큰 저장
    localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
    if (expiresIn) {
      const expiresAt = Math.floor(Date.now() / 1000) + Number(expiresIn);
      localStorage.setItem(TOKEN_EXPIRES_AT, String(expiresAt));
    }

    // 로그인 상태 유지: 학번 저장
    if (remember) {
      saveCredentials(studentId, true);
    } else {
      clearSavedCredentials();
    }

    alert("로그인이 완료되었습니다!");
    // 로그인 후 홈으로 이동
    window.location.href = "../index.html";
  } catch (err) {
    console.error(err);
    alert("로그인에 실패했습니다. 학번/비밀번호를 확인해주세요.");
  } finally {
    setLoadingState(false);
  }
}

/**********************
 * 글로벌 API 래퍼 (fetch)
 * - Authorization 헤더 자동 첨부
 * - 401 발생 시 refresh → 재시도
 * - 실패 시 logout() 처리
 * - 다른 페이지에서 window.api_fetch 로 재사용
 **********************/
let isRefreshing = false;
let waitQueue = []; // 리프레시 완료 대기중 요청들

async function api_fetch(input, init = {}) {
  const url = input.startsWith("http") ? input : API_BASE_URL + input;
  const headers = new Headers(init.headers || {});
  const accessToken = localStorage.getItem(ACCESS_TOKEN_KEY);

  if (accessToken) headers.set("Authorization", `Bearer ${accessToken}`);

  const doFetch = () => fetch(url, { ...init, headers });

  let res = await doFetch();

  if (res.status !== 401) return res;

  // ==== 401 처리: 토큰 갱신 시도 ====
  // 이미 리프레시 중이면 큐에 넣고 대기
  if (isRefreshing) {
    await new Promise((resolve, reject) => waitQueue.push({ resolve, reject }));
  } else {
    try {
      isRefreshing = true;
      const refreshed = await refresh_token();
      isRefreshing = false;
      // 큐 모두 깨우기
      waitQueue.forEach((p) => p.resolve(refreshed));
      waitQueue = [];
    } catch (e) {
      isRefreshing = false;
      waitQueue.forEach((p) => p.reject(e));
      waitQueue = [];
      // 최종 실패 → 로그아웃
      logout();
      throw e;
    }
  }

  // 토큰 갱신 후 재시도
  const newHeaders = new Headers(init.headers || {});
  const newAccess = localStorage.getItem(ACCESS_TOKEN_KEY);
  if (newAccess) newHeaders.set("Authorization", `Bearer ${newAccess}`);
  res = await fetch(url, { ...init, headers: newHeaders });

  if (res.status === 401) {
    // 재시도 후에도 401 → 로그아웃
    logout();
  }
  return res;
}

async function refresh_token() {
  const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY);
  if (!refreshToken) throw new Error("리프레시 토큰이 없습니다.");

  const res = await fetch(API_BASE_URL + REISSUE_PATH, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ refreshToken }),
  });

  if (!res.ok) throw new Error("토큰 재발급 실패");
  const data = await res.json();
  const payload = data.result ?? data;

  const newAccess = payload.accessToken;
  const newRefresh = payload.refreshToken ?? refreshToken; // 서버가 새 RT를 줄 수도/안줄 수도
  const expiresIn = payload.expiresIn ?? null;

  if (!newAccess) throw new Error("재발급 Access Token 누락");

  localStorage.setItem(ACCESS_TOKEN_KEY, newAccess);
  localStorage.setItem(REFRESH_TOKEN_KEY, newRefresh);
  if (expiresIn) {
    const expiresAt = Math.floor(Date.now() / 1000) + Number(expiresIn);
    localStorage.setItem(TOKEN_EXPIRES_AT, String(expiresAt));
  }
  return true;
}

/**********************
 * 공용 유틸
 **********************/
function logout() {
  // 토큰/유저정보 제거 후 로그인 화면으로
  localStorage.removeItem(ACCESS_TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
  localStorage.removeItem(TOKEN_EXPIRES_AT);
  // (선택) 기억된 학번은 유지
  window.location.href = "./login.html";
}

function setLoadingState(isLoading) {
  loginBtn.disabled = isLoading;
  loginSpinner.style.display = isLoading ? "inline-block" : "none";
  loginText.textContent = isLoading ? "인증 중..." : "로그인";
}

function saveCredentials(studentId, remember) {
  if (remember) {
    localStorage.setItem("rememberedStudentId", studentId);
    localStorage.setItem("rememberLogin", "true");
  }
}
function loadSavedCredentials() {
  const rememberedStudentId = localStorage.getItem("rememberedStudentId");
  const rememberLogin = localStorage.getItem("rememberLogin") === "true";
  if (rememberedStudentId && rememberLogin) {
    studentIdInput.value = rememberedStudentId;
    rememberMeCheckbox.checked = true;
  }
}
function clearSavedCredentials() {
  localStorage.removeItem("rememberedStudentId");
  localStorage.removeItem("rememberLogin");
}

/**********************
 * 전역 노출 (다른 페이지에서 사용)
 **********************/
window.api_fetch = api_fetch;
window.logout = logout;
