// FRONTEND/js/login.js

// DOM 요소
const loginForm = document.getElementById("loginForm");
const studentIdInput = document.getElementById("studentId");
const passwordInput = document.getElementById("password");
const passwordToggle = document.getElementById("passwordToggle");
const eyeIcon = document.getElementById("eyeIcon");
const rememberMeCheckbox = document.getElementById("rememberMe");
const loginBtn = document.getElementById("loginBtn");
const loginText = document.getElementById("loginText");
const loginSpinner = document.getElementById("loginSpinner");

// 초기화
document.addEventListener("DOMContentLoaded", () => {
  setupEventListeners();
  loadSavedCredentials();
});

// 이벤트 리스너 설정
function setupEventListeners() {
  loginForm.addEventListener("submit", handleLogin);
  passwordToggle.addEventListener("click", togglePassword);
}

// 비밀번호 표시/숨김 토글
function togglePassword() {
  const isPassword = passwordInput.type === "password";
  passwordInput.type = isPassword ? "text" : "password";

  // 아이콘 변경
  eyeIcon.innerHTML = isPassword
    ? '<path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/>'
    : '<path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>';
}

// 로그인 처리
async function handleLogin(e) {
  e.preventDefault();

  const studentId = studentIdInput.value.trim();
  const password = passwordInput.value.trim();
  const rememberMe = rememberMeCheckbox.checked;

  // 유효성 검사
  if (!studentId || !password) {
    alert("학번과 비밀번호를 모두 입력해주세요.");
    return;
  }

  // 로딩 상태 시작
  setLoadingState(true);

  try {
    // TODO: 실제 로그인 API로 교체
    await simulateLogin(studentId, password);

    // 로그인 상태 유지 처리
    if (rememberMe) {
      saveCredentials(studentId, rememberMe);
    } else {
      clearSavedCredentials();
    }

    // 성공 처리
    alert("로그인이 완료되었습니다!");
    // pages/login.html 기준 홈으로 이동
    window.location.href = "../index.html";
  } catch (error) {
    alert("로그인에 실패했습니다. 학번과 비밀번호를 확인해주세요.");
  } finally {
    setLoadingState(false);
  }
}

// 로그인 시뮬레이션
function simulateLogin(studentId, password) {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      // 간단한 유효성 검사 (실제에선 서버 검증)
      if (studentId.length >= 8 && password.length >= 4) {
        resolve();
      } else {
        reject(new Error("Invalid credentials"));
      }
    }, 1500);
  });
}

// 로딩 상태 설정
function setLoadingState(isLoading) {
  loginBtn.disabled = isLoading;

  if (isLoading) {
    loginText.textContent = "인증 중...";
    loginSpinner.style.display = "inline-block";
  } else {
    loginText.textContent = "로그인";
    loginSpinner.style.display = "none";
  }
}

// 자격 증명 저장
function saveCredentials(studentId, rememberMe) {
  if (rememberMe) {
    localStorage.setItem("rememberedStudentId", studentId);
    localStorage.setItem("rememberLogin", "true");
  }
}

// 저장된 자격 증명 불러오기
function loadSavedCredentials() {
  const rememberedStudentId = localStorage.getItem("rememberedStudentId");
  const rememberLogin = localStorage.getItem("rememberLogin") === "true";

  if (rememberedStudentId && rememberLogin) {
    studentIdInput.value = rememberedStudentId;
    rememberMeCheckbox.checked = true;
  }
}

// 저장된 자격 증명 삭제
function clearSavedCredentials() {
  localStorage.removeItem("rememberedStudentId");
  localStorage.removeItem("rememberLogin");
}
