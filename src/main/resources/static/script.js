// 동아리 데이터
const clubs = [
  {
    id: 1,
    name: "나누리",
    description: "한국해비타트와 함께하는 봉사활동",
    image: "https://via.placeholder.com/300x200",
    status: "모집 마감",
    members: 25,
    established: "2019",
  },
  {
    id: 2,
    name: "키비탄",
    description: "아동과 함께하는 따뜻한 봉사활동",
    image: "https://via.placeholder.com/300x200",
    status: "모집 중",
    members: 18,
    established: "2020",
  },
  {
    id: 3,
    name: "PTPI",
    description: "전람활동을 통한 사회공헌",
    image: "https://via.placeholder.com/300x200",
    status: "모집 마감",
    members: 32,
    established: "2018",
  },
  {
    id: 4,
    name: "RCY",
    description: "대한적십자 청소년적십자",
    image: "https://via.placeholder.com/300x200",
    status: "모집 중",
    members: 28,
    established: "2017",
  },
  {
    id: 5,
    name: "농어민후생연구회 흙",
    description: "농촌 문제 연구 및 실천 활동",
    image: "https://via.placeholder.com/300x200",
    status: "모집 예정",
    members: 15,
    established: "2021",
  },
  {
    id: 6,
    name: "명지챌린저스",
    description: "도전과 성장을 추구하는 동아리",
    image: "https://via.placeholder.com/300x200",
    status: "모집 중",
    members: 22,
    established: "2019",
  },
  {
    id: 7,
    name: "인액터스",
    description: "사회문제 해결을 위한 창업동아리",
    image: "https://via.placeholder.com/300x200",
    status: "모집 예정",
    members: 20,
    established: "2020",
  },
  {
    id: 8,
    name: "COW",
    description: "IT 기술로 세상을 바꾸는 동아리",
    image: "https://via.placeholder.com/300x200",
    status: "모집 마감",
    members: 35,
    established: "2016",
  },
  {
    id: 9,
    name: "국예슬연구회 알",
    description: "연극을 통한 사회 메시지 전달",
    image: "https://via.placeholder.com/300x200",
    status: "모집 중",
    members: 40,
    established: "2018",
  },
  {
    id: 10,
    name: "주리방",
    description: "밴드 음악으로 하나되는 우리",
    image: "https://via.placeholder.com/300x200",
    status: "모집 마감",
    members: 16,
    established: "2019",
  },
  {
    id: 11,
    name: "통해",
    description: "통기타로 전하는 따뜻한 선율",
    image: "https://via.placeholder.com/300x200",
    status: "모집 중",
    members: 12,
    established: "2020",
  },
  {
    id: 12,
    name: "MGH",
    description: "음악을 사랑하는 사람들의 모임",
    image: "https://via.placeholder.com/300x200",
    status: "모집 마감",
    members: 14,
    established: "2021",
  },
]

// 공지사항 데이터
const notices = [
  {
    id: 1,
    title: "2024년 1학기 동아리 모집 안내",
    content:
      "2024년 1학기 동아리 모집이 3월 4일부터 시작됩니다. 각 동아리별 모집 일정과 지원 방법을 확인하시고 관심있는 동아리에 지원해주세요.",
    date: "2024.03.01",
    isImportant: true,
    isNew: true,
  },
  {
    id: 2,
    title: "동아리 박람회 개최 안내",
    content:
      "3월 11일(월) ~ 3월 13일(수) 학생회관 1층에서 동아리 박람회가 열립니다. 각 동아리의 활동 내용을 직접 확인하고 궁금한 점을 물어보세요!",
    date: "2024.02.28",
    isImportant: false,
    isNew: true,
  },
  {
    id: 3,
    title: "동아리 지원서 작성 가이드",
    content:
      "동아리 지원서 작성 시 주의사항과 팁을 안내드립니다. 자기소개서는 구체적인 경험과 동기를 중심으로 작성해주세요.",
    date: "2024.02.25",
    isImportant: false,
    isNew: false,
  },
  {
    id: 4,
    title: "동아리 활동비 지원 안내",
    content:
      "학교에서 동아리 활동비를 지원합니다. 신청 기간은 3월 20일까지이며, 자세한 내용은 학생처 홈페이지를 확인해주세요.",
    date: "2024.02.20",
    isImportant: true,
    isNew: false,
  },
  {
    id: 5,
    title: "동아리 회장단 워크샵 개최",
    content:
      "동아리 회장단을 대상으로 한 리더십 워크샵이 3월 15일에 개최됩니다. 동아리 운영에 도움이 되는 다양한 정보를 제공할 예정입니다.",
    date: "2024.02.15",
    isImportant: false,
    isNew: false,
  },
]

// 전역 변수
let filteredClubs = [...clubs]
let currentSort = "name"

// DOM 요소
const clubGrid = document.getElementById("clubGrid")
const searchInput = document.getElementById("searchInput")
const sortSelect = document.getElementById("sortSelect")
const clubCount = document.getElementById("clubCount")
const noResults = document.getElementById("noResults")
const noticeBtn = document.getElementById("noticeBtn")
const noticeModal = document.getElementById("noticeModal")
const modalClose = document.getElementById("modalClose")
const noticeList = document.getElementById("noticeList")

// 초기화
document.addEventListener("DOMContentLoaded", () => {
  renderClubs()
  renderNotices()
  setupEventListeners()
  updateBanner()
})

// 이벤트 리스너 설정
function setupEventListeners() {
  searchInput.addEventListener("input", handleSearch)
  sortSelect.addEventListener("change", handleSort)
  noticeBtn.addEventListener("click", openNoticeModal)
  modalClose.addEventListener("click", closeNoticeModal)

  // 모달 외부 클릭 시 닫기
  noticeModal.addEventListener("click", (e) => {
    if (e.target === noticeModal) {
      closeNoticeModal()
    }
  })
}

// 동아리 렌더링
function renderClubs() {
  if (filteredClubs.length === 0) {
    clubGrid.style.display = "none"
    noResults.style.display = "block"
    return
  }

  clubGrid.style.display = "grid"
  noResults.style.display = "none"

  clubGrid.innerHTML = filteredClubs
    .map(
      (club) => `
        <div class="club-card" onclick="handleClubClick(${club.id})">
            <div class="club-image">
                <img src="${club.image}" alt="${club.name} 대표 이미지">
                <div class="club-status ${getStatusClass(club.status)}">
                    ${club.status}
                </div>
            </div>
            <div class="club-content">
                <h3 class="club-name">${club.name}</h3>
                <p class="club-description">${club.description}</p>
                <div class="club-info">
                    <div class="club-members">
                        <svg class="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                            <circle cx="9" cy="7" r="4"/>
                            <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
                            <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
                        </svg>
                        ${club.members}명
                    </div>
                    <span>설립 ${club.established}</span>
                </div>
                <button class="btn ${getButtonClass(club.status)} btn-large" 
                        ${club.status === "모집 마감" ? "disabled" : ""}>
                    ${getButtonText(club.status)}
                </button>
            </div>
        </div>
    `,
    )
    .join("")

  updateClubCount()
}

// 상태별 클래스 반환
function getStatusClass(status) {
  switch (status) {
    case "모집 중":
      return "recruiting"
    case "모집 예정":
      return "upcoming"
    default:
      return ""
  }
}

// 버튼 클래스 반환
function getButtonClass(status) {
  return status === "모집 중" ? "btn-primary" : "btn-outline"
}

// 버튼 텍스트 반환
function getButtonText(status) {
  switch (status) {
    case "모집 중":
      return "지원하기"
    case "모집 예정":
      return "알림 신청"
    default:
      return "모집 마감"
  }
}

// 동아리 개수 업데이트
function updateClubCount() {
  clubCount.textContent = `총 ${filteredClubs.length}개의 동아리`
}

// 검색 처리
function handleSearch() {
  const searchTerm = searchInput.value.toLowerCase()
  filteredClubs = clubs.filter(
    (club) => club.name.toLowerCase().includes(searchTerm) || club.description.toLowerCase().includes(searchTerm),
  )
  sortClubs()
  renderClubs()
}

// 정렬 처리
function handleSort() {
  currentSort = sortSelect.value
  sortClubs()
  renderClubs()
}

// 동아리 정렬
function sortClubs() {
  filteredClubs.sort((a, b) => {
    switch (currentSort) {
      case "name":
        return a.name.localeCompare(b.name)
      case "members":
        return b.members - a.members
      case "status":
        const statusOrder = { "모집 중": 0, "모집 예정": 1, "모집 마감": 2 }
        return statusOrder[a.status] - statusOrder[b.status]
      default:
        return 0
    }
  })
}

// 동아리 클릭 처리
function handleClubClick(clubId) {
  const club = clubs.find((c) => c.id === clubId)
  alert(`${club.name} 동아리 상세 페이지로 이동합니다.`)
}

// 공지사항 렌더링
function renderNotices() {
  noticeList.innerHTML = notices
    .map(
      (notice) => `
        <div class="notice-card ${notice.isImportant ? "important" : ""}">
            <div class="notice-header">
                <div class="notice-title">
                    ${notice.isImportant ? '<span class="notice-badge important">중요</span>' : ""}
                    ${notice.isNew ? '<span class="notice-badge new">NEW</span>' : ""}
                    <span>${notice.title}</span>
                </div>
                <div class="notice-date">${notice.date}</div>
            </div>
            <div class="notice-content">
                ${notice.content}
            </div>
        </div>
    `,
    )
    .join("")
}

// 공지사항 모달 열기
function openNoticeModal() {
  noticeModal.classList.add("show")
  document.body.style.overflow = "hidden"
}

// 공지사항 모달 닫기
function closeNoticeModal() {
  noticeModal.classList.remove("show")
  document.body.style.overflow = "auto"
}

// 배너 업데이트
function updateBanner() {
  const recruitingClubs = clubs.filter((club) => club.status === "모집 중")
  const bannerScroll = document.getElementById("bannerScroll")

  // 배너 아이템 복제 (무한 스크롤 효과)
  const bannerItems = recruitingClubs.map(
    (club) => `
        <div class="banner-item">
            <div class="banner-icon">
                <svg class="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                    <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                    <circle cx="9" cy="7" r="4"/>
                    <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
                    <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
                </svg>
            </div>
            <span>${club.name}</span>
            <span class="banner-badge">모집중</span>
        </div>
    `,
  )

  // 배너 아이템을 두 번 반복해서 무한 스크롤 효과
  bannerScroll.innerHTML = [...bannerItems, ...bannerItems].join("")
}
