// FRONTEND/js/application_form_editor.js
document.addEventListener("DOMContentLoaded", () => {
  const qList = document.getElementById("qList");
  const addBtn = document.getElementById("addQuestionBtn");
  const saveBtn = document.getElementById("saveBtn");
  const loadBtn = document.getElementById("loadBtn");
  const clearBtn = document.getElementById("clearBtn");

  const TYPE = {
    SHORT: "short",
    LONG: "long",
    RADIO: "radio",
    CHECK: "checkbox",
    FILE: "file",
  };
  const STORAGE_KEY = "smu_app_template";

  /** 상태 (에디터용) */
  let questions = []; // { id, title, type, options?:string[] }

  /** 공통 렌더 */
  function render() {
    qList.innerHTML = "";
    questions.forEach((q) => qList.appendChild(renderItem(q)));
  }

  /** 질문 카드 렌더 */
  function renderItem(q) {
    const wrap = document.createElement("div");
    wrap.className = "q-item";
    wrap.dataset.id = q.id;

    // 헤더 (제목 + 타입 + 삭제)
    const head = document.createElement("div");
    head.className = "q-head";

    const title = document.createElement("input");
    title.className = "q-title";
    title.placeholder = "질문을 입력하세요 (예: 자기소개)";
    title.value = q.title || "";
    title.addEventListener("input", () => (q.title = title.value));

    const type = document.createElement("select");
    type.className = "q-type";
    type.innerHTML = `
      <option value="${TYPE.SHORT}" ${
      q.type === TYPE.SHORT ? "selected" : ""
    }>짧은 글</option>
      <option value="${TYPE.LONG}"  ${
      q.type === TYPE.LONG ? "selected" : ""
    }>긴 글</option>
      <option value="${TYPE.RADIO}" ${
      q.type === TYPE.RADIO ? "selected" : ""
    }>단일 선택</option>
      <option value="${TYPE.CHECK}" ${
      q.type === TYPE.CHECK ? "selected" : ""
    }>복수 선택</option>
      <option value="${TYPE.FILE}"  ${
      q.type === TYPE.FILE ? "selected" : ""
    }>파일 업로드</option>
    `;
    type.addEventListener("change", () => {
      q.type = type.value;
      if (q.type === TYPE.RADIO || q.type === TYPE.CHECK) {
        q.options = q.options?.length ? q.options : ["옵션 1"];
      } else {
        delete q.options;
      }
      render();
    });

    const del = document.createElement("button");
    del.type = "button";
    del.className = "q-remove";
    del.textContent = "삭제";
    del.addEventListener("click", () => {
      questions = questions.filter((x) => x.id !== q.id);
      render();
    });

    head.appendChild(title);
    head.appendChild(type);
    head.appendChild(del);

    wrap.appendChild(head);

    // 옵션 / 미리보기 입력
    if (q.type === TYPE.SHORT) {
      const inp = document.createElement("input");
      inp.className = "q-preview-input";
      inp.placeholder = "답변 입력 (짧은 글)";
      wrap.appendChild(inp);
    } else if (q.type === TYPE.LONG) {
      const ta = document.createElement("textarea");
      ta.className = "q-preview-textarea";
      ta.placeholder = "답변 입력 (긴 글)";
      wrap.appendChild(ta);
    } else if (q.type === TYPE.RADIO || q.type === TYPE.CHECK) {
      const optWrap = document.createElement("div");
      optWrap.className = "q-options";

      (q.options || []).forEach((text, idx) => {
        const row = document.createElement("div");
        row.className = "option-row";

        const txt = document.createElement("input");
        txt.type = "text";
        txt.value = text;
        txt.placeholder = `옵션 ${idx + 1}`;
        txt.addEventListener("input", () => (q.options[idx] = txt.value));

        const rm = document.createElement("button");
        rm.type = "button";
        rm.textContent = "–";
        rm.addEventListener("click", () => {
          q.options.splice(idx, 1);
          render();
        });

        row.appendChild(txt);
        row.appendChild(rm);
        optWrap.appendChild(row);
      });

      const addOpt = document.createElement("button");
      addOpt.type = "button";
      addOpt.className = "add-option";
      addOpt.textContent = "옵션 추가";
      addOpt.addEventListener("click", () => {
        q.options = q.options || [];
        q.options.push(`옵션 ${q.options.length + 1}`);
        render();
      });

      wrap.appendChild(optWrap);
      wrap.appendChild(addOpt);
    } else if (q.type === TYPE.FILE) {
      const inp = document.createElement("input");
      inp.type = "file";
      inp.className = "q-preview-input";
      wrap.appendChild(inp);
    }

    return wrap;
  }

  /** 액션 */
  addBtn.addEventListener("click", () => {
    questions.push({
      id: crypto.randomUUID(),
      title: "자기소개",
      type: TYPE.LONG,
    });
    render();
  });

  saveBtn.addEventListener("click", () => {
    const payload = questions
      .map((q) => ({
        title: (q.title || "").trim(),
        type: q.type,
        options: q.options || [],
      }))
      .filter((q) => q.title);

    localStorage.setItem(STORAGE_KEY, JSON.stringify(payload));
    alert("저장 완료! 지원서 페이지에서 확인해보세요.");
  });

  loadBtn.addEventListener("click", () => {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      questions = raw
        ? JSON.parse(raw).map((o) => ({ id: crypto.randomUUID(), ...o }))
        : [];
    } catch {
      questions = [];
    }
    render();
  });

  clearBtn.addEventListener("click", () => {
    if (!confirm("추가 질문을 모두 지울까요?")) return;
    questions = [];
    render();
  });

  // 최초 로드 시 저장본이 있으면 불러오기
  loadBtn.click();
});
