// FRONTEND/js/student_auth.js

// DOM
const formEl = document.getElementById("studentAuthForm");
const authFormWrap = document.getElementById("authForm");
const successScreen = document.getElementById("successScreen");

const studentIdEl = document.getElementById("authStudentId");
const passwordEl = document.getElementById("authPassword");
const agreeEl = document.getElementById("agreeAuth");

const authBtn = document.getElementById("authBtn");
const authText = document.getElementById("authText");
const authSpinner = document.getElementById("authSpinner");

// init
document.addEventListener("DOMContentLoaded", () => {
  formEl.addEventListener("submit", on_submit_auth);
});

async function on_submit_auth(e) {
  e.preventDefault();

  const sid = studentIdEl.value.trim();
  const pw = passwordEl.value.trim();

  if (!sid || !pw) {
    alert("학번과 비밀번호를 입력해주세요.");
    return;
  }
  if (!agreeEl.checked) {
    alert("학생 정보 조회 동의가 필요합니다.");
    return;
  }

  set_loading(true);

  try {
    // TODO: 실제 API로 교체
    await simulate_auth_api(sid, pw);

    // 성공: 화면 전환
    authFormWrap.style.display = "none";
    successScreen.style.display = "flex";

    // 필요 시 토큰/유저정보 저장 로직 추가
    // localStorage.setItem("access_token", token);
  } catch (err) {
    alert("인증에 실패했습니다. 학번/비밀번호를 확인해주세요.");
  } finally {
    set_loading(false);
  }
}

function set_loading(isLoading) {
  authBtn.disabled = isLoading;
  authText.textContent = isLoading ? "인증 중..." : "인증하기";
  authSpinner.style.display = isLoading ? "inline-block" : "none";
}

function simulate_auth_api(sid, pw) {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      // 예시 검증: 자리수 기준 (실제로는 서버 검증)
      if (sid.length >= 8 && pw.length >= 4) resolve();
      else reject(new Error("invalid"));
    }, 1200);
  });
}
