// FRONTEND/js/application_form.js
document.addEventListener("DOMContentLoaded", function () {
  // 파일 선택 이름 표시
  function updateFileName(inputElement) {
    const fileNameSpan = document.getElementById("selectedFileName");
    if (inputElement.files && inputElement.files.length > 0) {
      fileNameSpan.textContent = inputElement.files[0].name;
    } else {
      fileNameSpan.textContent = "선택된 파일 없음";
    }
  }
  const fileSelectButton = document.getElementById("fileSelectBtn");
  const attachmentInput = document.getElementById("attachment");
  fileSelectButton?.addEventListener("click", () => attachmentInput?.click());
  attachmentInput?.addEventListener("change", function () {
    updateFileName(this);
  });

  // ===== 템플릿 로드 & 렌더 (편집 불가) =====
  const listEl = document.getElementById("extraQuestionList");
  const hiddenEl = document.getElementById("extraQuestions");
  const formEl = document.getElementById("applicationForm");

  let template = [];
  try {
    template = JSON.parse(localStorage.getItem("smu_app_template") || "[]");
  } catch {
    template = [];
  }

  template.forEach((q, idx) => {
    const block = document.createElement("div");
    block.className = "q-block";
    const base = `extra_${idx}`;

    const label = document.createElement("label");
    label.className = "q-label";
    label.textContent = q.title + (q.required ? " *" : "");
    block.appendChild(label);

    if (q.type === "short") {
      const input = document.createElement("input");
      input.type = "text";
      input.className = "q-input";
      input.name = base;
      input.required = !!q.required;
      block.appendChild(input);
    } else if (q.type === "long") {
      const ta = document.createElement("textarea");
      ta.className = "q-textarea";
      ta.name = base;
      ta.required = !!q.required;
      block.appendChild(ta);
    } else if (q.type === "radio") {
      const wrap = document.createElement("div");
      wrap.className = "q-options";
      (q.options || []).forEach((opt, oi) => {
        const id = `${base}_r_${oi}`;
        const row = document.createElement("label");
        row.className = "checkbox-label";
        const r = document.createElement("input");
        r.type = "radio";
        r.name = base;
        r.id = id;
        r.value = opt;
        row.appendChild(r);
        row.appendChild(document.createTextNode(" " + opt));
        wrap.appendChild(row);
      });
      if (q.required && wrap.querySelector('input[type="radio"]')) {
        wrap.querySelector('input[type="radio"]').required = true;
      }
      block.appendChild(wrap);
    } else if (q.type === "checkbox") {
      const wrap = document.createElement("div");
      wrap.className = "q-options";
      (q.options || []).forEach((opt, oi) => {
        const id = `${base}_c_${oi}`;
        const row = document.createElement("label");
        row.className = "checkbox-label";
        const c = document.createElement("input");
        c.type = "checkbox";
        c.name = `${base}[]`;
        c.id = id;
        c.value = opt;
        row.appendChild(c);
        row.appendChild(document.createTextNode(" " + opt));
        wrap.appendChild(row);
      });
      block.appendChild(wrap);
    } else if (q.type === "file") {
      const input = document.createElement("input");
      input.type = "file";
      input.name = base;
      input.className = "q-input";
      input.required = !!q.required;
      block.appendChild(input);
    }

    listEl?.appendChild(block);
  });

  // 제출 시, 추가질문 답변을 JSON으로 직렬화해 hidden에 담는다.
  formEl?.addEventListener("submit", (e) => {
    // 체크박스 필수 검증
    for (let i = 0; i < template.length; i++) {
      const q = template[i];
      if (q.type === "checkbox" && q.required) {
        const checked = document.querySelectorAll(
          `input[name="extra_${i}[]"]:checked`
        );
        if (checked.length === 0) {
          e.preventDefault();
          alert(`필수 항목: "${q.title}"를 선택해주세요.`);
          return;
        }
      }
    }

    const answers = template.map((q, i) => {
      const base = `extra_${i}`;
      if (q.type === "short" || q.type === "long") {
        const el = document.querySelector(`[name="${base}"]`);
        return { title: q.title, type: q.type, answer: el?.value || "" };
      }
      if (q.type === "radio") {
        const sel = document.querySelector(`input[name="${base}"]:checked`);
        return { title: q.title, type: q.type, answer: sel ? sel.value : "" };
      }
      if (q.type === "checkbox") {
        const list = Array.from(
          document.querySelectorAll(`input[name="${base}[]"]:checked`)
        ).map((i) => i.value);
        return { title: q.title, type: q.type, answer: list };
      }
      if (q.type === "file") {
        const f = document.querySelector(`input[name="${base}"]`)?.files?.[0];
        return { title: q.title, type: q.type, filename: f?.name || "" };
      }
      return { title: q.title, type: q.type, answer: "" };
    });

    hiddenEl.value = JSON.stringify(answers);
  });
});
