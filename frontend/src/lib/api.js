// 자동 토큰 재발급을 포함한 fetch 래퍼
export async function apiFetch(input, init = {}) {
  const makeInit = () => ({
    credentials: "include", // httpOnly 쿠키 사용 가정
    headers: {
      "Content-Type": "application/json",
      ...(init.headers || {}),
    },
    ...init,
  });

  const doFetch = () => fetch(input, makeInit());

  // 1차 호출
  let res = await doFetch();
  let data = null;
  try {
    data = await res.clone().json();
  } catch (_) {
    /* JSON 아님 */
  }

  // ACCESS 만료 처리
  if (data?.status === "FAIL" && data?.errorCode === "EXPIRED_TOKEN") {
    // 토큰 재발급 시도
    const re = await fetch("/api/v1/public/auth/reissue", {
      method: "POST",
      credentials: "include",
      headers: { "Content-Type": "application/json" },
    });

    const reData = await re.json().catch(() => null);

    if (re.ok && reData?.status !== "FAIL") {
      // 재발급 성공 → 원요청 재시도
      res = await doFetch();
    } else {
      // 재발급 실패 → 세션 만료
      throw Object.assign(
        new Error("세션이 만료되었습니다. 다시 로그인해 주세요."),
        {
          code: "EXPIRED_TOKEN",
        }
      );
    }
  }

  return res;
}

// JSON 헬퍼
export async function apiJson(input, init) {
  const res = await apiFetch(input, init);
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

// 로그아웃
export async function apiLogout() {
  return apiJson("/api/v1/auth/logout", { method: "POST" });
}
