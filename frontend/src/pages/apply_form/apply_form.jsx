// src/pages/apply_form/apply_form.jsx
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import "../../styles/globals.css";
import "./apply_form.css";

export default function ApplyForm() {
  const navigate = useNavigate();

  // 보기 전용 더미 데이터 (API 연동 시 교체)
  const [form] = useState({
    dept: "휴먼지능정보공학과",
    studentId: "202110869",
    name: "이윤표",
    phone: "01012345678",
    gender: "male",
    intro: "안녕하세요. 프론트엔드에 관심이 많습니다. 열심히 활동하겠습니다.",
    attachment: "portfolio_younpyo.pdf",
  });

  // 담당자가 결정 상태만 변경 가능
  const [decision, setDecision] = useState("pending"); // 'pass' | 'pending' | 'fail'

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
            <h1>이윤표님의 지원서</h1>
          </div>
        </div>
      </div>

      <main className="page-main apply_main">
        <section className="apply_section">
          <h2 className="apply_title">온라인 지원</h2>

          <div className="apply_card">
            <p className="desc">합불 상태를 확인해주세요</p>

            <label className="field_label" htmlFor="dept">
              학과
            </label>
            <input
              id="dept"
              className="field_input"
              value={form.dept}
              disabled
              readOnly
            />

            <label className="field_label" htmlFor="sid">
              학번
            </label>
            <input
              id="sid"
              className="field_input"
              value={form.studentId}
              disabled
              readOnly
            />

            <label className="field_label" htmlFor="uname">
              이름
            </label>
            <input
              id="uname"
              className="field_input"
              value={form.name}
              disabled
              readOnly
            />

            <label className="field_label" htmlFor="phone">
              전화번호
            </label>
            <input
              id="phone"
              className="field_input"
              value={form.phone}
              disabled
              readOnly
            />

            <fieldset className="fieldset">
              <legend className="field_label">성별</legend>
              <label className="radio_item">
                <input
                  type="radio"
                  name="gender"
                  checked={form.gender === "male"}
                  disabled
                  readOnly
                />
                남성
              </label>
              <label className="radio_item">
                <input
                  type="radio"
                  name="gender"
                  checked={form.gender === "female"}
                  disabled
                  readOnly
                />
                여성
              </label>
              <label className="radio_item">
                <input
                  type="radio"
                  name="gender"
                  checked={form.gender === "other"}
                  disabled
                  readOnly
                />
                기타
              </label>
            </fieldset>

            <label className="field_label" htmlFor="intro">
              자기소개
            </label>
            <textarea
              id="intro"
              className="answer_area"
              value={form.intro}
              disabled
              readOnly
            />

            {/* 첨부 파일 - 보기 전용 */}
            <div className="file_row view_only">
              <span className="file_name">
                {form.attachment || "첨부 없음"}
              </span>
              {form.attachment && (
                <a
                  className="outline_btn sm"
                  href="#"
                  onClick={(e) => e.preventDefault()}
                >
                  다운로드
                </a>
              )}
            </div>

            {/* 하단 결정 토글 (담당자만 변경) */}
            <div
              className="decision_toggle"
              role="tablist"
              aria-label="지원 결과 선택"
            >
              <button
                type="button"
                role="tab"
                aria-selected={decision === "fail"}
                className={`seg seg-fail ${
                  decision === "fail" ? "is_active" : ""
                }`}
                onClick={() => setDecision("fail")}
              >
                불합격
              </button>
              <button
                type="button"
                role="tab"
                aria-selected={decision === "pending"}
                className={`seg seg-pending ${
                  decision === "pending" ? "is_active" : ""
                }`}
                onClick={() => setDecision("pending")}
              >
                미정
              </button>
              <button
                type="button"
                role="tab"
                aria-selected={decision === "pass"}
                className={`seg seg-pass ${
                  decision === "pass" ? "is_active" : ""
                }`}
                onClick={() => setDecision("pass")}
              >
                합격
              </button>
            </div>
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
