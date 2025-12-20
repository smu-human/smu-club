// src/pages/home/home.jsx

import React, { useEffect, useMemo, useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import "../../styles/globals.css";
import "./home.css";
import { fetch_public_clubs, is_logged_in } from "../../lib/api";

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

const DEFAULT_LOGO = "/images/2.png";

// ✅ 썸네일 URL 정규화 (http(s)면 그대로, /로 시작하면 그대로, 그 외는 그대로 두되 확인용)
function normalize_img_url(url) {
  if (!url) return null;
  const s = String(url).trim();
  if (!s) return null;

  if (s.startsWith("http://") || s.startsWith("https://")) return s;
  if (s.startsWith("/")) return s;

  // 백이 S3 key만 내려주는 케이스면 여기서 베이스를 붙여야 함.
  // 지금은 베이스를 모르니 일단 그대로 두고(콘솔로 확인) 필요하면 VITE_S3_BASE_URL로 붙이면 됨.
  return s;
}

// ✅ 썸네일 후보를 최대한 넓게 잡기
function pick_thumbnail(item) {
  const candidates = [
    item?.thumbnailUrl,
    item?.thumbnail_url,
    item?.clubThumbnailUrl,
    item?.clubThumbnail,
    item?.thumbnail,
    item?.thumbUrl,
    item?.thumb_url,
    Array.isArray(item?.clubImageUrls) ? item.clubImageUrls[0] : null,
    Array.isArray(item?.club_image_urls) ? item.club_image_urls[0] : null,
    Array.isArray(item?.imageUrls) ? item.imageUrls[0] : null,
    Array.isArray(item?.images) ? item.images[0] : null,
  ];

  const raw = candidates.find((v) => v != null && String(v).trim() !== "");
  return normalize_img_url(raw);
}

export default function HomePage() {
  const nav = useNavigate();
  const [query, setQuery] = useState("");
  const [sortKey, setSortKey] = useState("name");
  const [onlyOpen, setOnlyOpen] = useState(false);

  const [clubs, setClubs] = useState([]);
  const [is_loading, set_is_loading] = useState(false);
  const [error_msg, set_error_msg] = useState("");
  const [loggedIn, setLoggedIn] = useState(false);

  useEffect(() => {
    setLoggedIn(is_logged_in());
  }, []);

  useEffect(() => {
    const load = async () => {
      set_is_loading(true);
      set_error_msg("");

      try {
        const data = await fetch_public_clubs();

        // ✅ 디버그: 실제로 썸네일 필드가 오는지 확인
        console.log(
          "[public/clubs] raw first item:",
          Array.isArray(data) ? data[0] : data
        );

        const mapped = (Array.isArray(data) ? data : []).map((item) => {
          const thumb = pick_thumbnail(item);

          // ✅ 디버그: 각 아이템별로 최종 썸네일이 무엇인지 확인
          console.log("[club]", item?.id, item?.name, "thumb:", thumb);

          return {
            id: item?.id,
            name: item?.name || "",
            status: (item?.recruitingStatus || "").toLowerCase(),
            members: null,
            dday: null,
            deadline: null,
            desc: item?.title || "",
            logo: thumb || DEFAULT_LOGO,
          };
        });

        setClubs(mapped.filter((c) => c.id != null));
      } catch (err) {
        set_error_msg(err.message || "동아리 목록을 불러오지 못했습니다.");
      } finally {
        set_is_loading(false);
      }
    };

    load();
  }, []);

  const filtered = useMemo(() => {
    let list = [...clubs];

    if (query.trim()) {
      const q = query.trim().toLowerCase();
      list = list.filter((c) => (c.name || "").toLowerCase().includes(q));
    }
    if (onlyOpen) {
      list = list.filter((c) => c.status === "open");
    }

    list.sort((a, b) => {
      if (sortKey === "name") return (a.name || "").localeCompare(b.name || "");
      if (sortKey === "members") return (b.members ?? 0) - (a.members ?? 0);
      if (sortKey === "dday") return (a.dday ?? 9999) - (b.dday ?? 9999);
      return 0;
    });

    return list;
  }, [query, onlyOpen, sortKey, clubs]);

  return (
    <div className="home_page">
      <header className="page-header">
        <div className="brand_row">
          <button
            type="button"
            className="logo_btn"
            onClick={() => window.location.reload()}
            aria-label="SMU club 홈 새로고침"
          >
            <img src={DEFAULT_LOGO} alt="SMU club 로고" className="logo_img" />
          </button>

          <div className="header_actions">
            {loggedIn ? (
              <button
                type="button"
                className="btn text"
                onClick={() => nav("/mypage")}
              >
                마이페이지
              </button>
            ) : (
              <>
                <Link to="/login" className="btn text">
                  로그인
                </Link>
                <Link to="/signup" className="btn primary">
                  회원가입
                </Link>
              </>
            )}
          </div>
        </div>

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

        <div className="toolbar">
          <div className="stat">총 {clubs.length}개의 동아리</div>

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

      <main className="home_main">
        {error_msg && <div className="error_msg">{error_msg}</div>}

        {is_loading ? (
          <div className="list_loading">동아리 목록을 불러오는 중...</div>
        ) : (
          <>
            {filtered.map((c) => (
              <article
                key={c.id}
                className="club_card"
                onClick={() => nav(`/club/${c.id}`)}
              >
                <div className="club_head_row">
                  <img
                    className="club_logo"
                    src={c.logo}
                    alt={`${c.name} 로고`}
                    onError={(e) => {
                      // 이미지 로드 실패하면 기본 이미지로 교체 (404/403/혼합콘텐츠 등)
                      e.currentTarget.onerror = null;
                      e.currentTarget.src = DEFAULT_LOGO;
                    }}
                  />
                  <div className="club_head_left">
                    <h3 className="club_name">{c.name}</h3>
                    <div className="club_meta_row">
                      <span
                        className={`badge ${
                          c.status === "open"
                            ? "open"
                            : c.status === "upcoming"
                            ? "upcoming"
                            : "closed"
                        }`}
                      >
                        {c.status === "open"
                          ? "신청 가능"
                          : c.status === "upcoming"
                          ? "모집 예정"
                          : "신청 불가"}
                      </span>
                      {c.members != null && (
                        <span className="members">· {c.members}명</span>
                      )}
                    </div>
                  </div>
                  <span className={`dday ${ddayClass(c.dday)}`}>
                    {ddayLabel(c.dday)}
                  </span>
                </div>

                <p className="club_desc">{c.desc}</p>

                <div className="club_foot_row">
                  {c.deadline && (
                    <span className="deadline">모집마감일 {c.deadline}</span>
                  )}
                </div>
              </article>
            ))}

            {filtered.length === 0 && !is_loading && (
              <div className="empty">
                <p>검색 결과가 없습니다.</p>
              </div>
            )}
          </>
        )}
      </main>

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
