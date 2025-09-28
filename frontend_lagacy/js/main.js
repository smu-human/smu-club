// FRONTEND/js/main.js
document.addEventListener("DOMContentLoaded", () => {
  /* ====== (추가) 내가 넣을 이미지 매핑 ====== */
  // key는 club.id, value는 이미지 경로(상대/절대/URL 모두 가능)
  const image_map = {
    1: "images/justdoit.jpg",
    2: "images/sori.jpg",
    3: "images/tornado.jpg",
    4: "images/trip.jpg",
  };

  /* ====== 더미 데이터: 실제 API 연결 전까지 UI 확인용 ====== */
  const clubs = [
    {
      id: 1,
      name: "해커톤 연구회",
      members: 42,
      status: "recruiting", // recruiting | upcoming | closed
      desc: "웹/앱 해커톤을 준비하는 스터디. 매주 수요일 알고리즘/프로젝트 세션 진행.",
      image: "https://picsum.photos/seed/club1/256/256",
      deadline: "2025-09-15",
    },
    {
      id: 2,
      name: "사진 동아리",
      members: 18,
      status: "upcoming",
      desc: "필름부터 디지털까지, 출사·현상·전시까지 한 번에 배우는 사진 커뮤니티.",
      image: "https://picsum.photos/seed/club2/256/256",
      deadline: "2025-09-20",
    },
    {
      id: 3,
      name: "보드게임 모임",
      members: 30,
      status: "recruiting",
      desc: "카탄/테라포 등 전략게임 정모. 룰티칭·리그전 운영, 신작 체험회 진행.",
      image: "https://picsum.photos/seed/club3/256/256",
      deadline: "2025-09-10",
    },
    {
      id: 4,
      name: "러닝 클럽",
      members: 25,
      status: "recruiting",
      desc: "주 3회 함께 달려요! 초보/중급 그룹 나눠서 러닝폼 교정과 기록 공유.",
      image: "https://picsum.photos/seed/club4/256/256",
      deadline: "2025-09-08",
    },
  ];

  const notices = [
    {
      id: 101,
      title: "정기 모임 안내 (9월 2주차)",
      date: "2025-09-02",
      content:
        "이번 주 정기 모임은 수요일 19시, 학생회관 402호에서 진행합니다.",
      important: true,
      isNew: true,
    },
    {
      id: 102,
      title: "신입 환영회 신청",
      date: "2025-09-01",
      content: "9/10(수) 18시 신입 환영회! 링크 통해 신청해주세요.",
      important: false,
      isNew: true,
    },
  ];

  /* ====== 엘리먼트 ====== */
  const $grid = document.getElementById("clubGrid");
  const $noResults = document.getElementById("noResults");
  const $search = document.getElementById("searchInput");
  const $sort = document.getElementById("sortSelect");
  const $count = document.getElementById("clubCount");

  const $noticeBtn = document.getElementById("noticeBtn");
  const $noticeModal = document.getElementById("noticeModal");
  const $noticeList = document.getElementById("noticeList");
  const $modalClose = document.getElementById("modalClose");
  const $noticeBadge = document.getElementById("noticeBadge");

  /* ====== 상태 ====== */
  let isLoggedIn = false; // ✅ 더미 로그인 상태
  let filtered = [...clubs];

  /* ====== 표시 제어 ====== */
  if (!isLoggedIn) {
    $noticeBtn.style.display = "none";
  } else {
    $noticeBtn.style.display = "inline-flex";
  }

  /* ====== 유틸 ====== */
  // 띄어쓰기 무시 검색
  const normalize = (s) => s.toLowerCase().replace(/\s+/g, "");

  // D-day 라벨/클래스 계산
  function getDDay(deadlineStr) {
    if (!deadlineStr) return { label: "-", cls: "dday-neutral" };
    const today = new Date();
    const dline = new Date(deadlineStr + "T00:00:00");
    const diffMs = dline.setHours(0, 0, 0, 0) - today.setHours(0, 0, 0, 0);
    const diffDays = Math.round(diffMs / (1000 * 60 * 60 * 24));

    if (diffDays > 0) return { label: `D-${diffDays}`, cls: "dday-positive" };
    if (diffDays === 0) return { label: "D-DAY", cls: "dday-today" };
    return { label: `D+${Math.abs(diffDays)}`, cls: "dday-passed" };
  }

  function statusLabel(status) {
    if (status === "recruiting") return "모집중";
    if (status === "upcoming") return "예정";
    return "마감";
  }

  /* ====== 렌더링 (설명 중심 카드) ====== */
  function renderClubs(list) {
    $grid.innerHTML = "";
    list.forEach((c) => {
      const { label, cls } = getDDay(c.deadline);

      // 매핑된 이미지 우선
      const imgSrc = image_map[c.id] ?? c.image;

      // ✅ div → a 로 변경 + href 설정
      const card = document.createElement("a");
      card.className = "club-card desc-first";
      card.href = `pages/club.html?id=${c.id}`; // 필요시 경로를 club.html 로 바꿔

      card.innerHTML = `
      <div class="club-head">
        <img class="club-logo" src="${imgSrc}" alt="${c.name} 로고" />
        <div class="club-title">
          <div class="club-name">${c.name}</div>
          <div class="club-meta">
            <span class="club-members">
              <svg class="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                <circle cx="9" cy="7" r="4"></circle>
              </svg>
              ${c.members}명
            </span>
            <span class="club-status ${c.status}">${statusLabel(
        c.status
      )}</span>
          </div>
        </div>
        <span class="club-dday ${cls}">${label}</span>
      </div>

      <p class="club-description">${c.desc}</p>

      <div class="club-foot">
        ${
          c.deadline ? `<span class="deadline">마감일 ${c.deadline}</span>` : ""
        }
        <span class="foot-spacer"></span>
        <span class="foot-hint">${
          c.status === "recruiting"
            ? "지금 신청 가능"
            : c.status === "upcoming"
            ? "일정 공개 예정"
            : "모집 마감"
        }</span>
      </div>
    `;
      $grid.appendChild(card);
    });

    $count.textContent = `총 ${list.length}개의 동아리`;
    $noResults.style.display = list.length ? "none" : "block";
  }

  function renderNotices(items) {
    $noticeList.innerHTML = items
      .map(
        (n) => `
        <div class="notice-card ${n.important ? "important" : ""}">
          <div class="notice-header">
            <div class="notice-title">
              <span>${n.title}</span>
              ${
                n.important
                  ? '<span class="notice-badge important">중요</span>'
                  : ""
              }
              ${n.isNew ? '<span class="notice-badge new">NEW</span>' : ""}
            </div>
            <div class="notice-date">${n.date}</div>
          </div>
          <div class="notice-content">${n.content}</div>
        </div>
      `
      )
      .join("");
    $noticeBadge.textContent = String(items.filter((i) => i.isNew).length);
  }

  /* ====== 핸들러 ====== */
  $search.addEventListener("input", (e) => {
    const q = normalize(e.target.value);
    filtered = clubs.filter((c) => {
      const n = normalize(c.name);
      const d = normalize(c.desc);
      return n.includes(q) || d.includes(q);
    });
    applySort();
  });

  $sort.addEventListener("change", applySort);

  function applySort() {
    const type = $sort.value;
    const list = [...filtered];
    if (type === "name")
      list.sort((a, b) => a.name.localeCompare(b.name, "ko"));
    if (type === "members") list.sort((a, b) => b.members - a.members);
    if (type === "status")
      list.sort((a, b) => a.status.localeCompare(b.status));
    renderClubs(list);
  }
  function updateHeaderCut() {
    const h = document.querySelector(".header")?.offsetHeight || 220;
    document.documentElement.style.setProperty("--header-h", `${h}px`);
  }
  window.addEventListener("load", updateHeaderCut);
  window.addEventListener("resize", updateHeaderCut);
  $noticeBtn?.addEventListener("click", () => {
    $noticeModal.classList.add("show");
  });

  $modalClose?.addEventListener("click", () => {
    $noticeModal.classList.remove("show");
  });

  $noticeModal?.addEventListener("click", (e) => {
    if (e.target === $noticeModal) $noticeModal.classList.remove("show");
  });

  /* ====== 초기 렌더 ====== */
  renderClubs(clubs);
  renderNotices(notices);
  applySort();
});
