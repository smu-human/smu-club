// FRONTEND/js/application_form.js

document.addEventListener("DOMContentLoaded", () => {
  const fileSelectButton = document.getElementById("fileSelectBtn");
  const attachmentInput = document.getElementById("attachment");
  const fileNameSpan = document.getElementById("selectedFileName");
  const formEl = document.getElementById("applicationForm");

  function updateFileName(inputEl) {
    if (inputEl.files && inputEl.files.length > 0) {
      fileNameSpan.textContent = inputEl.files[0].name;
    } else {
      fileNameSpan.textContent = "선택된 파일 없음";
    }
  }

  if (fileSelectButton) {
    fileSelectButton.addEventListener("click", () => attachmentInput.click());
  }

  if (attachmentInput) {
    attachmentInput.addEventListener("change", function () {
      updateFileName(this);
    });
  }

  // (선택) 현재는 서버 액션 없는 데모 → 제출 시 기본 동작 막고 안내
  formEl.addEventListener("submit", (e) => {
    // 실제 백엔드 연동 전까지 막아두고 확인용 알림
    // 백엔드 연결되면 이 줄 삭제하고 fetch/폼 제출 로직으로 교체
    e.preventDefault();
    alert(
      "제출되었습니다! (데모)\n백엔드 연동 후 실제로 전송되도록 변경하세요."
    );
  });
});
