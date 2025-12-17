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
  const norm = (t) => {
    if (!t) return null;
    const s = String(t).trim();
    return s.toLowerCase().startsWith("bearer ") ? s.slice(7).trim() : s;
  };

  const access = norm(access_token);
  const refresh = norm(refresh_token);

  if (access) localStorage.setItem(ACCESS_TOKEN_KEY, access);
  if (refresh) localStorage.setItem(REFRESH_TOKEN_KEY, refresh);
}

export function clear_tokens() {
  localStorage.removeItem(ACCESS_TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
}

export function is_logged_in() {
  return !!get_access_token();
}

// ===== 내부 공통 =====
function resolve_url(path) {
  if (typeof path === "string" && /^https?:\/\//.test(path)) return path;
  if (typeof path === "string") return `${API_BASE}${path}`;
  return path;
}

function make_init(init = {}, access_token) {
  const merged_headers = {
    ...(init.headers || {}),
    ...(access_token ? { Authorization: `Bearer ${access_token}` } : {}),
  };

  const has_body = init.body !== undefined && init.body !== null;

  return {
    method: "GET",
    ...init,
    credentials: "include",
    headers: has_body
      ? { "Content-Type": "application/json", ...merged_headers }
      : merged_headers,
  };
}

// ===== fetch 래퍼 (만료는 body의 EXPIRED_TOKEN일 때만 reissue) =====
export async function apiFetch(path, init = {}) {
  let access_token = get_access_token();
  const doFetch = () => fetch(resolve_url(path), make_init(init, access_token));

  let res = await doFetch();

  let data = null;
  try {
    data = await res.clone().json();
  } catch (_) {}

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

    const re_res = await fetch(resolve_url("/public/auth/reissue"), {
      method: "POST",
      credentials: "include",
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
    err.code = code || (res.status === 401 ? "UNAUTHORIZED" : "ERROR");
    err.raw = data;
    err.status = res.status;
    throw err;
  }
  return data;
}

// ===== owner 전용 fetch (항상 Authorization 주입, credentials 제거) =====
async function owner_fetch_json(path, init = {}) {
  const access_token = get_access_token();

  const res = await fetch(resolve_url(path), {
    method: "GET",
    ...init,
    headers: {
      ...(init.headers || {}),
      ...(access_token ? { Authorization: `Bearer ${access_token}` } : {}),
    },
  });

  const data = await res.json().catch(() => null);

  if (!res.ok || data?.status === "FAIL") {
    const err = new Error(data?.message || "요청에 실패했습니다.");
    err.code =
      data?.errorCode || (res.status === 401 ? "UNAUTHORIZED" : "ERROR");
    err.status = res.status;
    err.raw = data;
    throw err;
  }

  return data;
}

// ===== 인증 =====
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

export async function apiSignup({ studentId, password, phoneNumber }) {
  return apiJson("/public/auth/signup", {
    method: "POST",
    body: JSON.stringify({ studentId, password, phoneNumber }),
  });
}

export async function apiLogout() {
  const res = await apiJson("/auth/logout", { method: "POST" });
  clear_tokens();
  return res;
}

// ===== 공개 클럽 =====
export async function fetch_public_clubs() {
  const res = await apiJson("/public/clubs", { method: "GET" });
  return res.data || [];
}

export async function fetch_public_club(club_id) {
  const res = await apiJson(`/public/clubs/${club_id}`, { method: "GET" });
  return res.data;
}

// ===== 멤버 =====
export async function fetch_member_club_apply(club_id) {
  const res = await apiJson(`/member/clubs/${club_id}/apply`, {
    method: "GET",
  });
  return res.data;
}

export async function fetch_mypage_name() {
  const res = await apiJson("/member/mypage/name", { method: "GET" });
  return res.data;
}

export async function fetch_mypage_profile() {
  const res = await apiJson("/member/mypage/update", { method: "GET" });
  return res.data;
}

export async function update_mypage_phone(newPhoneNumber) {
  const res = await apiJson("/member/mypage/update/phone", {
    method: "PUT",
    body: JSON.stringify({ newPhoneNumber }),
  });
  return res.data;
}

export async function update_mypage_email(newEmail) {
  const res = await apiJson("/member/mypage/update/email", {
    method: "PUT",
    body: JSON.stringify({ newEmail }),
  });
  return res.data;
}

export async function fetch_my_applications() {
  const res = await apiJson("/member/mypage/applications", { method: "GET" });
  return res.data || [];
}

export async function fetch_application_result(club_id) {
  const res = await apiJson(`/member/mypage/applications/${club_id}/result`, {
    method: "GET",
  });
  return res.data;
}

export async function delete_application(club_id) {
  const res = await apiJson(`/member/mypage/applications/${club_id}/delete`, {
    method: "POST",
  });
  return res;
}

export async function fetch_application_for_update(club_id) {
  const res = await apiJson(`/member/mypage/applications/${club_id}/update`, {
    method: "GET",
  });
  return res.data;
}

export async function update_application(club_id, payload) {
  const res = await apiJson(`/member/mypage/applications/${club_id}/update`, {
    method: "PUT",
    body: JSON.stringify(payload),
  });
  return res;
}

export async function api_member_withdraw() {
  const res = await apiJson("/member/mypage/delete", { method: "POST" });
  clear_tokens();
  return res;
}

// ✅ 오너: 내가 관리하는 동아리 목록 조회
export async function fetch_owner_managed_clubs() {
  const res = await apiJson("/owner/club/managed-clubs", { method: "GET" });
  return res.data || [];
}

// ===== OWNER: 이미지 업로드 (Presigned URL) =====
export async function owner_issue_upload_url({
  originalFileName,
  contentType,
}) {
  const res = await apiJson("/owner/club/upload-url", {
    method: "POST",
    body: JSON.stringify({ originalFileName, contentType }),
  });
  return res.data; // { fileName, preSignedUrl }
}

export async function owner_put_presigned_url(preSignedUrl, file) {
  const putRes = await fetch(preSignedUrl, {
    method: "PUT",
    headers: {
      "Content-Type": file.type || "application/octet-stream",
    },
    body: file,
  });

  if (!putRes.ok) {
    const err = new Error("이미지 업로드에 실패했습니다.");
    err.status = putRes.status;
    throw err;
  }
  return true;
}

export async function owner_upload_images(files = []) {
  const uploaded_names = [];

  for (const file of files) {
    const { fileName, preSignedUrl } = await owner_issue_upload_url({
      originalFileName: file.name,
      contentType: file.type || "application/octet-stream",
    });

    await owner_put_presigned_url(preSignedUrl, file);
    uploaded_names.push(fileName);
  }

  return uploaded_names;
}

// ===== OWNER: 동아리 등록 (multipart) =====
export async function owner_register_club(payload) {
  const access_token = get_access_token();

  const form = new FormData();

  const uploaded = payload?.uploadedImageFileNames || [];
  (uploaded || []).forEach((v) => form.append("uploadedImageFileNames", v));

  if (payload?.name !== undefined) form.append("name", payload.name);
  if (payload?.title !== undefined) form.append("title", payload.title);
  if (payload?.president !== undefined)
    form.append("president", payload.president);
  if (payload?.contact !== undefined) form.append("contact", payload.contact);
  if (payload?.recruitingEnd !== undefined)
    form.append("recruitingEnd", payload.recruitingEnd);
  if (payload?.clubRoom !== undefined)
    form.append("clubRoom", payload.clubRoom);
  if (payload?.description !== undefined)
    form.append("description", payload.description);

  const res = await fetch(resolve_url("/owner/club/register/club"), {
    method: "POST",
    headers: {
      ...(access_token ? { Authorization: `Bearer ${access_token}` } : {}),
    },
    body: form,
  });

  const data = await res.json().catch(() => null);

  if (!res.ok || data?.status === "FAIL") {
    const err = new Error(data?.message || "요청에 실패했습니다.");
    err.code =
      data?.errorCode || (res.status === 401 ? "UNAUTHORIZED" : "ERROR");
    err.status = res.status;
    err.raw = data;
    throw err;
  }

  return data;
}

// ===== OWNER: 동아리 상세 조회 (GET) =====
export async function fetch_owner_club_detail(club_id) {
  const res = await owner_fetch_json(`/owner/club/club/${club_id}`, {
    method: "GET",
  });
  return res.data || null;
}

// ===== OWNER: 동아리 수정 (PUT multipart) =====
export async function owner_update_club(club_id, payload) {
  const form = new FormData();

  const uploaded = payload?.uploadedImageFileNames || [];
  (uploaded || []).forEach((v) => form.append("uploadedImageFileNames", v));

  if (payload?.name !== undefined) form.append("name", payload.name);
  if (payload?.title !== undefined) form.append("title", payload.title);
  if (payload?.president !== undefined)
    form.append("president", payload.president);
  if (payload?.contact !== undefined) form.append("contact", payload.contact);
  if (payload?.recruitingEnd !== undefined)
    form.append("recruitingEnd", payload.recruitingEnd);
  if (payload?.clubRoom !== undefined)
    form.append("clubRoom", payload.clubRoom);
  if (payload?.description !== undefined)
    form.append("description", payload.description);

  return owner_fetch_json(`/owner/club/club/${club_id}`, {
    method: "PUT",
    body: form,
  });
}

// ===== OWNER: 모집 시작/중지 =====
export async function owner_start_recruitment(club_id) {
  return apiJson(`/owner/club/${club_id}/start-recruitment`, {
    method: "POST",
  });
}

export async function owner_stop_recruitment(club_id) {
  return apiJson(`/owner/club/${club_id}/stop-recruitment`, {
    method: "POST",
  });
}

// ===== OWNER: 지원자 목록 =====
export async function fetch_owner_applicants(club_id) {
  const res = await owner_fetch_json(`/owner/club/${club_id}/applicants`, {
    method: "GET",
  });
  return res.data || [];
}
