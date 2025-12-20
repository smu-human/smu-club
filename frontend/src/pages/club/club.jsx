// src/pages/club/club.jsx

import { useParams, useNavigate } from "react-router-dom";
import { useState, useRef, useEffect } from "react";
import "../../styles/globals.css";
import "./club.css";
import {
  fetch_public_club,
  fetch_member_club_apply,
  is_logged_in,
  fetch_owner_club_detail, // ✅ 이거 사용
} from "../../lib/api";

export default function ClubPage() {
  const { id } = useParams();
  const nav = useNavigate();

  const [club, setClub] = useState(null);
  const [images, setImages] = useState([]);
  const [activeIndex, setActiveIndex] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error_msg, set_error_msg] = useState("");
  const carouselRef = useRef(null);

  // ✅ 백엔드에서 동아리 상세 정보 가져오기
  useEffect(() => {
    const load = async () => {
      setLoading(true);
      set_error_msg("");

      try {
        let data;

        // ✅ 로그인 상태면 owner 상세 우선 시도
        if (is_logged_in()) {
          try {
            data = await fetch_owner_club_detail(id);
          } catch {
            // 오너 아니면 public fallback
            data = await fetch_public_club(id);
          }
        } else {
          data = await fetch_public_club(id);
        }

        setClub(data);

        // ✅ owner API 기준 필드명
        const urls = Array.isArray(data?.clubImageUrls)
          ? data.clubImageUrls.filter(Boolean)
          : [];

        if (urls.length > 0) {
          setImages(urls);
        } else if (data?.thumbnailUrl) {
          setImages([data.thumbnailUrl]);
        } else {
          setImages([]);
        }

        setActiveIndex(0);
        carouselRef.current?.scrollTo({ left: 0 });
      } catch (err) {
        set_error_msg(err.message || "동아리 정보를 불러오지 못했습니다.");
      } finally {
        setLoading(false);
      }
    };

    load();
  }, [id]);

  const goTo = (nextIdx) => {
    if (!carouselRef.current || images.length === 0) return;
    const total = images.length;
    const clamped = (nextIdx + total) % total;
    setActiveIndex(clamped);

    const slideWidth = carouselRef.current.clientWidth;
    carouselRef.current.scrollTo({
      left: clamped * slideWidth,
      behavior: "smooth",
    });
  };

  const onPrev = () => goTo(activeIndex - 1);
  const onNext = () => goTo(activeIndex + 1);

  const onScroll = () => {
    if (!carouselRef.current) return;
    const slideWidth = carouselRef.current.clientWidth;
    const idx = Math.round(carouselRef.current.scrollLeft / slideWidth);
    if (idx !== activeIndex) setActiveIndex(idx);
  };

  const render_status = (status) => {
    const s = status?.toUpperCase();
    if (s === "OPEN") return "모집중";
    if (s === "UPCOMING") return "모집 예정";
    if (s === "CLOSED") return "모집 마감";
    return s || "-";
  };

  const handleApply = async () => {
    if (!is_logged_in()) {
      nav("/login");
      return;
    }

    try {
      const data = await fetch_member_club_apply(id);
      nav("/apply_form", {
        state: {
          club,
          applyData: data,
        },
      });
    } catch (err) {
      set_error_msg(err.message || "지원 정보를 불러오지 못했습니다.");
    }
  };

  return (
    <div className="club_page">
      {/* 페이지 헤더 */}
      <header className="page-header sticky-header safe-area-top page-header--club">
        <div className="container">
          <div className="page-header-content">
            <button
              className="back-btn"
              aria-label="뒤로가기"
              onClick={() => nav(-1)}
            >
              <svg
                className="icon"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
              >
                <path d="M19 12H5" />
                <path d="M12 19l-7-7 7-7" />
              </svg>
            </button>
            <h1>{club?.name || `클럽 ${id} 상세`}</h1>
            <button className="apply_btn" onClick={handleApply}>
              지원하기
            </button>
          </div>
        </div>
      </header>

      <main className="club_main safe-area-padding">
        <div className="container">
          {error_msg && (
            <p
              aria-live="polite"
              style={{ minHeight: 20, color: "#a82d2f", marginBottom: "8px" }}
            >
              {error_msg}
            </p>
          )}

          {loading ? (
            <div className="club-loading">동아리 정보를 불러오는 중...</div>
          ) : !club ? (
            <div className="club-empty">동아리 정보를 찾을 수 없습니다.</div>
          ) : (
            <>
              {/* ===== 갤러리 ===== */}
              <section className="gallery card">
                {images.length === 0 ? (
                  <div className="no_image">등록된 이미지가 없습니다.</div>
                ) : (
                  <>
                    <div
                      className="carousel"
                      ref={carouselRef}
                      onScroll={onScroll}
                    >
                      {images.map((src, i) => (
                        <div className="slide" key={i}>
                          <img src={src} alt={`클럽 이미지 ${i + 1}`} />
                        </div>
                      ))}
                    </div>

                    {images.length > 1 && (
                      <>
                        <button
                          className="nav prev"
                          onClick={onPrev}
                          aria-label="이전 이미지"
                        >
                          ‹
                        </button>
                        <button
                          className="nav next"
                          onClick={onNext}
                          aria-label="다음 이미지"
                        >
                          ›
                        </button>

                        <div className="dots">
                          {images.map((_, i) => (
                            <button
                              key={i}
                              className={i === activeIndex ? "is_active" : ""}
                              aria-label={`${i + 1}번째 이미지로 이동`}
                              onClick={() => goTo(i)}
                            />
                          ))}
                        </div>
                      </>
                    )}
                  </>
                )}
              </section>

              {/* ===== 메타 ===== */}
              <section className="club_meta card">
                <ul className="info_list">
                  <li>
                    <span className="label">회장</span>
                    <span className="val">{club.president}</span>
                  </li>
                  <li>
                    <span className="label">연락처</span>
                    <span className="val">{club.contact}</span>
                  </li>
                  <li>
                    <span className="label">모집 기간</span>
                    <span className="val">
                      {club.recruitingStart} ~ {club.recruitingEnd}
                    </span>
                  </li>
                  <li>
                    <span className="label">상태</span>
                    <span className="val badge">
                      {render_status(club.recruitingStatus)}
                    </span>
                  </li>
                  <li>
                    <span className="label">동아리방</span>
                    <span className="val">{club.clubRoom}</span>
                  </li>
                </ul>
              </section>

              {/* ===== 소개 ===== */}
              <section className="intro card">
                <h2 className="section_title">동아리 소개</h2>

                {club?.description ? (
                  <div
                    className="desc rich_desc"
                    dangerouslySetInnerHTML={{ __html: club.description }}
                  />
                ) : (
                  <p className="desc">
                    {`이곳에 클럽 ${id}의 소개글을 넣어주세요. 활동 목적, 주요 활동, 성과 등을 적을 수 있습니다.`}
                  </p>
                )}
              </section>
            </>
          )}
        </div>
      </main>

      <div className="page-footer">
        <p>© 2025 smu-club. 상명대학교 동아리 플랫폼</p>
        <p>
          <a
            href="https://github.com/smu-human/smu-club"
            target="_blank"
            rel="noopener noreferrer"
          >
            Github
          </a>
        </p>
      </div>
    </div>
  );
}
