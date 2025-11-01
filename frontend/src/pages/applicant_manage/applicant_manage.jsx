// src/pages/applicant_manage/applicant_manage.jsx
import { useNavigate } from "react-router-dom";
import "../../styles/globals.css";
import "./applicant_manage.css";

export default function ApplicantManage() {
  const navigate = useNavigate();

  const applicants = [
    { id: 1, name: "이윤표", studentId: "202110869", status: "pass" },
    { id: 2, name: "유승준", studentId: "202110869", status: "fail" },
    { id: 3, name: "차준규", studentId: "202110869", status: "pending" },
  ];

  const statusLabel = {
    pass: "합격",
    fail: "불합격",
    pending: "미정",
  };

  return (
    <div className="page-root">
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
            <h1>지원자 관리</h1>
          </div>
        </div>
      </div>

      <main className="page-main applicant_main">
        <section className="applicant_section">
          <h2 className="applicant_title">러닝클럽 지원자</h2>
          <div className="applicant_footer">
            <p className="hint_text">지원자를 클릭해서 자세히 보기</p>
            <button className="mail_btn">결과 메일 발송하기</button>
          </div>
          <div className="applicant_list">
            {applicants.map((a) => (
              <div key={a.id} className="applicant_card">
                <span className="applicant_info">
                  {a.name} {a.studentId}
                </span>
                <span className={`applicant_status ${a.status}`}>
                  {statusLabel[a.status]}
                </span>
              </div>
            ))}
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
