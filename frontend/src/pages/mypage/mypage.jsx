// src/pages/mypage/mypage.jsx
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import "../../styles/globals.css";
import "./mypage.css";

export default function MyPage() {
  const navigate = useNavigate();
  const [recruitOpen, setRecruitOpen] = useState(true); // 모집 상태

  return (
    <div className="page-root">
      {/* Header */}
      <div className="page-header sticky-header safe-area-top">
        <div className="container">
          <div className="page-header-content">
            <button
              type="button"
              className="back-btn"
              aria-label="뒤로가기"
              onClick={() => navigate(-1)}
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
            <h1>마이페이지</h1>
            <span className="header-name">이윤표님</span>
          </div>
        </div>
      </div>

      {/* Main */}
      <main className="page-main mypage_main">
        {/* 지원 목록 */}
        <section className="mypage_section">
          <h2 className="mypage_title">지원 목록</h2>
          <div className="mypage_card">
            <div className="club_box">
              <p className="club_title">러닝클럽</p>
              <div className="club_buttons">
                <button>동아리 페이지</button>
                <button>지원서 편집</button>
                <button>결과 확인</button>
              </div>
            </div>
          </div>
        </section>

        {/* 동아리 운영/관리 */}
        <section className="mypage_section">
          <h2 className="mypage_title">동아리 운영/관리</h2>
          <div className="mypage_card">
            <div className="club_box">
              <p className="club_title">러닝클럽</p>
              <div className="club_buttons">
                <button>동아리 관리</button>
                <button>지원양식 편집</button>
                <button>지원자 관리</button>

                {/* ✅ 모집 상태 토글 버튼 */}
                <div className="recruit_actions">
                  <button
                    className={`status_btn ${recruitOpen ? "on" : "off"}`}
                    onClick={() => setRecruitOpen((v) => !v)}
                  >
                    {recruitOpen ? "모집중지" : "모집시작"}
                  </button>
                </div>
              </div>
            </div>

            <button className="add_btn">동아리 등록하기</button>
          </div>
        </section>

        <div className="mypage_footer">
          <p>회원정보수정</p>
          <p>로그아웃</p>
          <p className="logout_red">탈퇴</p>
        </div>
      </main>

      {/* Footer */}
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
