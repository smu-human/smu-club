// src/pages/applicant_manage/applicant_manage.jsx
import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import "../../styles/globals.css";
import "./applicant_manage.css";
import { fetch_owner_applicants } from "../../lib/api";

export default function ApplicantManage() {
  const navigate = useNavigate();
  const { clubId } = useParams();

  const [applicants, set_applicants] = useState([]);
  const [loading, set_loading] = useState(true);
  const [error_msg, set_error_msg] = useState("");

  useEffect(() => {
    const load = async () => {
      if (!clubId) return;

      set_loading(true);
      set_error_msg("");

      try {
        const data = await fetch_owner_applicants(clubId);
        set_applicants(Array.isArray(data) ? data : []);
      } catch (e) {
        set_error_msg(e?.message || "지원자 목록을 불러오지 못했습니다.");
        set_applicants([]);
      } finally {
        set_loading(false);
      }
    };

    load();
  }, [clubId]);

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
          <h2 className="applicant_title">지원자</h2>

          <div className="applicant_footer">
            <p className="hint_text">지원자를 클릭해서 자세히 보기</p>
            <button className="mail_btn">결과 메일 발송하기</button>
          </div>

          {loading ? (
            <div className="applicant_list">
              <div className="applicant_card">불러오는 중...</div>
            </div>
          ) : error_msg ? (
            <div className="applicant_list">
              <div className="applicant_card">{error_msg}</div>
            </div>
          ) : applicants.length === 0 ? (
            <div className="applicant_list">
              <div className="applicant_card">지원자가 없습니다.</div>
            </div>
          ) : (
            <div className="applicant_list">
              {applicants.map((a) => (
                <div
                  key={a.id ?? a.applicationId ?? `${a.name}-${a.studentId}`}
                  className="applicant_card"
                >
                  <span className="applicant_info">
                    {a.name} {a.studentId}
                  </span>
                  <span className={`applicant_status ${a.status}`}>
                    {statusLabel[a.status] ?? "미정"}
                  </span>
                </div>
              ))}
            </div>
          )}
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
