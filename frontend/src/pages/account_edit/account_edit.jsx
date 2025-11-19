// src/pages/account_edit/account_edit.jsx
import { useNavigate } from "react-router-dom";
import "../../styles/globals.css";
import "./account_edit.css";

export default function AccountEdit() {
  const navigate = useNavigate();

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
            <h1>회원정보수정</h1>
            <span className="header-name">이윤표님</span>
          </div>
        </div>
      </div>

      {/* Main */}
      <main className="page-main account_main">
        {/* 이메일 수정 */}
        <section className="account_section">
          <h2 className="account_title">이메일 수정</h2>
          <div className="account_card">
            <label className="field_label" htmlFor="currentEmail">
              기존 이메일
            </label>
            <input
              className="field_input"
              id="currentEmail"
              type="email"
              placeholder="기존 이메일"
            />

            <label className="field_label" htmlFor="newEmail">
              새 이메일
            </label>
            <input
              className="field_input"
              id="newEmail"
              type="email"
              placeholder="새 이메일"
            />

            <button className="primary_btn">저장하기</button>
          </div>
        </section>

        {/* 전화번호 수정 */}
        <section className="account_section">
          <h2 className="account_title">전화번호 수정</h2>
          <div className="account_card">
            <label className="field_label" htmlFor="currentPhone">
              기존 전화번호
            </label>
            <input
              className="field_input"
              id="currentPhone"
              type="tel"
              placeholder="기존 전화번호"
            />

            <label className="field_label" htmlFor="newPhone">
              새 전화번호
            </label>
            <input
              className="field_input"
              id="newPhone"
              type="tel"
              placeholder="새 전화번호"
            />

            <button className="primary_btn">저장하기</button>
          </div>
        </section>
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
