import { useParams, useNavigate } from "react-router-dom";
import { useState, useRef } from "react";
import "./club.css";

export default function ClubPage() {
  const { id } = useParams();
  const nav = useNavigate();

  // 보여줄 이미지들 (public/images/ 기준)
  const images = [
    "/images/justdoit.jpg",
    "/images/sori.jpg",
    "/images/tornado.jpg",
    "/images/trip.jpg",
  ];

  // 캐러셀 상태
  const [activeIndex, setActiveIndex] = useState(0);
  const carouselRef = useRef(null);

  const goTo = (nextIdx) => {
    if (!carouselRef.current) return;
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
            <h1>클럽 {id} 상세</h1>
          </div>
        </div>
      </header>

      <main className="club_main safe-area-padding">
        <div className="container">
          {/* ===== 갤러리 ===== */}
          <section className="gallery card">
            <div className="carousel" ref={carouselRef} onScroll={onScroll}>
              {images.map((src, i) => (
                <div className="slide" key={i}>
                  <img src={src} alt={`클럽 이미지 ${i + 1}`} />
                </div>
              ))}
            </div>

            {/* 좌우 버튼 */}
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

                {/* 네비게이션 dots */}
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
          </section>

          {/* ===== 메타 예시 ===== */}
          <section className="club_meta card">
            <ul className="info_list">
              <li>
                <span className="label">회장</span>
                <span className="val">홍길동</span>
              </li>
              <li>
                <span className="label">연락처</span>
                <span className="val">010-1234-5678</span>
              </li>
              <li>
                <span className="label">모집 기간</span>
                <span className="val">2025.03.01 ~ 2025.03.15</span>
              </li>
              <li>
                <span className="label">상태</span>
                <span className="val badge">모집중</span>
              </li>
            </ul>
          </section>

          {/* ===== 소개 ===== */}
          <section className="intro card">
            <h2 className="section_title">동아리 소개</h2>
            <p className="desc">
              이곳에 클럽 {id}의 소개글을 넣어주세요. 활동 목적, 주요 활동, 성과
              등을 적을 수 있습니다.
            </p>
          </section>
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
