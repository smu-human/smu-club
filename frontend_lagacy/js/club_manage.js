// FRONTEND/js/club_manage.js

document.addEventListener("DOMContentLoaded", () => {
  // 갤러리 업로드 & 미리보기
  const input = document.getElementById("galleryInput");
  const addBtn = document.getElementById("addImagesBtn");
  const preview = document.getElementById("galleryPreview");

  const images = []; // { id, file, url }

  addBtn?.addEventListener("click", () => input?.click());
  input?.addEventListener("change", (e) => {
    const files = Array.from(e.target.files || []);
    files.forEach((file) => {
      const url = URL.createObjectURL(file);
      const id = crypto.randomUUID();
      images.push({ id, file, url });
    });
    renderPreview();
    input.value = "";
  });

  function renderPreview() {
    preview.innerHTML = "";
    images.forEach((img) => {
      const item = document.createElement("div");
      item.className = "preview_item";
      item.innerHTML = `
        <img src="${img.url}" alt="preview"/>
        <button type="button" class="remove" data-id="${img.id}" aria-label="삭제">×</button>
      `;
      preview.appendChild(item);
    });
    preview.querySelectorAll(".remove").forEach((btn) => {
      btn.addEventListener("click", () => {
        const id = btn.getAttribute("data-id");
        const idx = images.findIndex((i) => i.id === id);
        if (idx >= 0) {
          URL.revokeObjectURL(images[idx].url);
          images.splice(idx, 1);
          renderPreview();
        }
      });
    });
  }

  // 저장/되돌리기 (데모용)
  const saveBtn = document.getElementById("saveClubBtn");
  const resetBtn = document.getElementById("resetBtn");
  const form = document.getElementById("clubBaseForm");

  saveBtn?.addEventListener("click", () => {
    const data = {
      clubName: form.clubName.value.trim(),
      leaderName: form.leaderName.value.trim(),
      contact: form.contact.value.trim(),
      periodStart: form.periodStart.value,
      periodEnd: form.periodEnd.value,
      days: Array.from(
        document.querySelectorAll("#daysChips input:checked")
      ).map((i) => i.value),
      time: form.time.value.trim(),
      state: form.state.value,
      intro: form.intro.value.trim(),
      imagesCount: images.length,
    };
    // TODO: 실제 API 연동
    console.log("[SAVE CLUB]", data);
    alert("동아리 정보가 저장되었습니다. (데모)");
  });

  resetBtn?.addEventListener("click", () => {
    form.reset();
    images.splice(0, images.length);
    renderPreview();
  });
});
