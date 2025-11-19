// src/lib/api.js

const API_BASE = import.meta.env.VITE_API_BASE_URL || "/api/v1";

const ACCESS_TOKEN_KEY = "smu_access_token";
const REFRESH_TOKEN_KEY = "smu_refresh_token";

// ===== 토큰 유틸 =====
export function get_access_token() {
  return localStorage.getItem(ACCESS_TOKEN_KEY) || null;
}

export function get_refresh_token() {
  return localStorage.getItem(REFRESH_TOKEN_KEY) || null;
}

export function set_tokens(access_token, refresh_token) {
  if (access_token) {
    localStorage.setItem(ACCESS_TOKEN_KEY, access_token);
  }
  if (refresh_token) {
    localStorage.setItem(REFRESH_TOKEN_KEY, refresh_token);
  }
}

export function clear_tokens() {
  localStorage.removeItem(ACCESS_TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
}

// ===== 내부 공통 =====
function resolve_url(path) {
  if (typeof path === "string" && /^https?:\/\//.test(path)) return path;
  if (typeof path === "string") return `${API_BASE}${path}`;
  return path;
}

function make_init(init = {}, access_token) {
  return {
    method: "GET",
    ...init,
    headers: {
      "Content-Type": "application/json",
      ...(access_token ? { Authorization: `Bearer ${access_token}` } : {}),
      ...(init.headers || {}),
    },
  };
}

// ===== 자동 토큰 재발급 포함 fetch 래퍼 =====
export async function apiFetch(path, init = {}) {
  let access_token = get_access_token();
  const doFetch = () => fetch(resolve_url(path), make_init(init, access_token));

  // 1차 요청
  let res = await doFetch();
  let data = null;
  try {
    data = await res.clone().json();
  } catch (_) {
    /* json 아님 */
  }

  // 토큰 만료 처리 (백엔드에서 이런 식으로 내려온다고 가정)
  const expired =
    data?.status === "FAIL" && data?.errorCode === "EXPIRED_TOKEN";

  if (expired) {
    const refresh_token = get_refresh_token();

    if (!refresh_token) {
      clear_tokens();
      throw Object.assign(
        new Error("세션이 만료되었습니다. 다시 로그인해 주세요."),
        { code: "EXPIRED_TOKEN" }
      );
    }

    // ✨ 토큰 재발급 요청: POST /api/v1/public/auth/reissue
    const re_res = await fetch(resolve_url("/public/auth/reissue"), {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        accessToken: access_token,
        refreshToken: refresh_token,
      }),
    });

    const re_data = await re_res.json().catch(() => null);

    if (re_res.ok && re_data?.status !== "FAIL") {
      const new_access = re_data?.data?.accessToken;
      const new_refresh = re_data?.data?.refreshToken;

      set_tokens(new_access, new_refresh);
      access_token = new_access;

      // 새 토큰으로 원래 요청 재시도
      res = await fetch(resolve_url(path), make_init(init, access_token));
    } else {
      clear_tokens();
      throw Object.assign(
        new Error("세션이 만료되었습니다. 다시 로그인해 주세요."),
        { code: "EXPIRED_TOKEN" }
      );
    }
  }

  return res;
}

// ===== JSON 헬퍼 =====
export async function apiJson(path, init) {
  const res = await apiFetch(path, init);
  const data = await res.json().catch(() => null);

  if (!res.ok || data?.status === "FAIL") {
    const msg = data?.message || "요청에 실패했습니다.";
    const code = data?.errorCode;
    const err = new Error(msg);
    err.code = code;
    err.raw = data;
    throw err;
  }
  return data;
}

// ===== 인증용 편의 함수 =====

// 로그인: POST /api/v1/public/auth/login
export async function apiLogin({ studentId, password }) {
  const data = await apiJson("/public/auth/login", {
    method: "POST",
    body: JSON.stringify({ studentId, password }),
  });

  const accessToken = data?.data?.accessToken;
  const refreshToken = data?.data?.refreshToken;
  set_tokens(accessToken, refreshToken);

  return data;
}

// 회원가입(학생 인증): POST /api/v1/public/auth/signup
export async function apiSignup({ studentId, password, phoneNumber }) {
  return apiJson("/public/auth/signup", {
    method: "POST",
    body: JSON.stringify({ studentId, password, phoneNumber }),
  });
}

// 로그아웃: POST /api/v1/auth/logout
export async function apiLogout() {
  const res = await apiJson("/auth/logout", {
    method: "POST",
  });
  clear_tokens();
  return res;
}

// 동아리 목록
export async function fetch_public_clubs() {
  const res = await apiJson("/public/clubs", {
    method: "GET",
  });
  // dataType이 Array 라고 했으니까 data 배열만 반환
  return res.data || [];
}

export async function fetch_public_club(club_id) {
  const res = await apiJson(`/public/club/${club_id}`, {
    method: "GET",
  });
  return res.data; // 단일 객체
}
