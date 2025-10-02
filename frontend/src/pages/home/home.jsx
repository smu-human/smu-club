import React, { useMemo, useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import "../../styles/globals.css";
import "./home.css";

/**
 * 요구사항
 * - 로고 클릭 새로고침
 * - 로그인/회원가입 이동
 * - 검색 + 정렬 + 신청가능 토글
 * - 카드 클릭 → /club/:id
 * - D-day 색상 강약
 * - 모바일 390 레이아웃 / 데스크톱 카드 3열
 */

const mockClubs = [
  {
    id: 1,
    name: "러닝클럽",
    status: "open",
    members: 26,
    dday: 10,
    deadline: "2025-09-08",
    desc: "주 3회 함께 달려요! 초보/중급 그룹 나눠 러닝폼 교정과 기록 공유",
    logo: "/images/trip_road.jpg",
  },
  {
    id: 2,
    name: "TRIP ROAD",
    status: "open",
    members: 18,
    dday: 2,
    deadline: "2025-09-03",
    desc: "트래킹 & 로드 무빙 사진/영상 동아리",
    logo: "/images/trip_road.jpg",
  },
  {
    id: 3,
    name: "알고리즘 학회",
    status: "closed",
    members: 42,
    dday: -3,
    deadline: "2025-08-28",
    desc: "주 1회 코테 스터디와 세미나 진행",
    logo: "/images/trip_road.jpg",
  },
  {
    id: 4,
    name: "알고리즘 학회",
    status: "closed",
    members: 42,
    dday: -3,
    deadline: "2025-08-28",
    desc: "주 1회 코테 스터디와 세미나 진행",
    logo: "/images/trip_road.jpg",
  },
  {
    id: 5,
    name: "알고리즘 학회",
    status: "closed",
    members: 42,
    dday: -3,
    deadline: "2025-08-28",
    desc: "주 1회 코테 스터디와 세미나 진행",
    logo: "/images/trip_road.jpg",
  },
];

function ddayClass(d) {
  if (d == null) return "dday-neutral";
  if (d <= 0) return "dday-passed";
  if (d <= 3) return "dday-hot";
  if (d <= 7) return "dday-soon";
  if (d <= 14) return "dday-warm";
  return "dday-neutral";
}
function ddayLabel(d) {
  if (d == null) return "예정";
  if (d <= 0) return "마감";
  return `D-${d}`;
}

export default function HomePage() {
  const nav = useNavigate();
  const [query, setQuery] = useState("");
  const [sortKey, setSortKey] = useState("name"); // name | members | dday
  const [onlyOpen, setOnlyOpen] = useState(false);

  const filtered = useMemo(() => {
    let list = [...mockClubs];

    if (query.trim()) {
      const q = query.trim().toLowerCase();
      list = list.filter((c) => c.name.toLowerCase().includes(q));
    }
    if (onlyOpen) {
      list = list.filter((c) => c.status === "open");
    }

    list.sort((a, b) => {
      if (sortKey === "name") return a.name.localeCompare(b.name);
      if (sortKey === "members") return b.members - a.members;
      if (sortKey === "dday") return (a.dday ?? 9999) - (b.dday ?? 9999);
      return 0;
    });
    return list;
  }, [query, onlyOpen, sortKey]);

  return (
    <div className="home_page">
      {/* 헤더(390 고정) */}
      <header className="page-header">
        {/* 1) 로고 + 액션 한 줄 */}
        <div className="brand_row">
          <button
            type="button"
            className="logo_btn"
            onClick={() => window.location.reload()}
            aria-label="SMU club 홈 새로고침"
          >
            <img src="/images/2.png" alt="SMU club 로고" className="logo_img" />
          </button>

          <div className="header_actions">
            <Link to="/login" className="btn text">
              로그인
            </Link>
            <Link to="/signup" className="btn primary">
              회원가입
            </Link>
          </div>
        </div>

        {/* 2) 검색 */}
        <div className="search_row">
          <svg
            className="icon"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
          >
            <circle cx="11" cy="11" r="8"></circle>
            <path d="M21 21l-4.35-4.35"></path>
          </svg>
          <input
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            className="search_input"
            placeholder="동아리 이름 검색"
          />
        </div>

        {/* 3) 통계/정렬/토글 */}
        <div className="toolbar">
          <div className="stat">총 {filtered.length}개의 동아리</div>

          <div className="right_controls">
            <label className="toggle">
              <input
                type="checkbox"
                checked={onlyOpen}
                onChange={(e) => setOnlyOpen(e.target.checked)}
              />
              <span>신청가능</span>
            </label>

            <select
              className="sort_select"
              value={sortKey}
              onChange={(e) => setSortKey(e.target.value)}
            >
              <option value="name">이름순</option>
              <option value="members">회원수순</option>
              <option value="dday">마감 임박순</option>
            </select>
          </div>
        </div>
      </header>

      {/* 리스트(모바일: 1열 / 데스크톱: 3열) */}
      <main className="home_main">
        {filtered.map((c) => (
          <article className="club_card">
            <div className="club_head_row">
              <img className="club_logo" src={c.logo} alt={`${c.name} 로고`} />
              <div className="club_head_left">
                <h3 className="club_name">{c.name}</h3>
                <div className="club_meta_row">
                  <span
                    className={`badge ${
                      c.status === "open" ? "open" : "closed"
                    }`}
                  >
                    {c.status === "open" ? "신청 가능" : "신청 불가"}
                  </span>
                  <span className="members">· {c.members}명</span>
                </div>
              </div>
              <span className={`dday ${ddayClass(c.dday)}`}>
                {ddayLabel(c.dday)}
              </span>
            </div>

            <p className="club_desc">{c.desc}</p>

            <div className="club_foot_row">
              <span className="deadline">모집마감일 {c.deadline}</span>
            </div>
          </article>
        ))}

        {filtered.length === 0 && (
          <div className="empty">
            <p>검색 결과가 없습니다.</p>
          </div>
        )}
      </main>
      {/* 🔹 푸터 */}
      <footer className="page-footer">
        <p>© 2025 smu-club. 상명대학교 동아리 통합 플랫폼</p>
        <p>
          <a
            href="https://github.com/smu-human/smu-club"
            target="_blank"
            rel="noopener noreferrer"
          >
            Github
          </a>
        </p>
      </footer>
    </div>
  );
}
