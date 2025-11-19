// src/pages/apply_form_submit/apply_form_submit.jsx
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import "../../styles/globals.css";
import "./apply_form_submit.css";

export default function ApplyFormSubmit() {
  const navigate = useNavigate();

  // 서버에서 내려올 커스텀 질문 예시 (id, label, type 등)
  const [questions] = useState([
    { id: "q1", label: "지원 동기(자유서술)", type: "textarea" },
    { id: "q2", label: "관련 경험을 적어주세요", type: "textarea" },
  ]);

  // 기본 항목
  const [dept, setDept] = useState("");
  const [studentId, setStudentId] = useState("");
  const [name, setName] = useState("");
  const [phone, setPhone] = useState("");
  const [gender, setGender] = useState("");
  const [intro, setIntro] = useState("");

  // 파일
  const [fileName, setFileName] = useState("선택된 파일 없음");
  const onPickFile = (e) => {
    const f = e.target.files?.[0];
    setFileName(f ? f.name : "선택된 파일 없음");
  };

  // 커스텀 질문 답변
  const [answers, setAnswers] = useState({}); // {id: value}

  const onSubmit = (e) => {
    e.preventDefault();
    const payload = {
      dept,
      student_id: studentId,
      name,
      phone,
      gender,
      intro,
      custom_answers: answers,
      attachment_name: fileName === "선택된 파일 없음" ? "" : fileName,
    };
    console.log(payload);
    alert("지원서가 제출되었습니다. (제출 API 연동 예정)");
    navigate(-1);
  };

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
            <h1>지원서 작성</h1>
          </div>
        </div>
      </div>

      {/* Main */}
      <main className="page-main apply_submit_main">
        <section className="apply_submit_section">
          <h2 className="apply_submit_title">온라인 지원</h2>

          <form className="apply_submit_card" onSubmit={onSubmit}>
            <p className="desc">필수 항목을 정확히 입력한 뒤 제출하세요.</p>

            <label className="field_label" htmlFor="dept">
              학과
            </label>
            <input
              id="dept"
              className="field_input"
              placeholder="소속 학과를 입력하세요"
              value={dept}
              onChange={(e) => setDept(e.target.value)}
              required
            />

            <label className="field_label" htmlFor="sid">
              학번
            </label>
            <input
              id="sid"
              className="field_input"
              placeholder="학번을 입력하세요 (예: 202012345)"
              value={studentId}
              onChange={(e) => setStudentId(e.target.value)}
              required
            />

            <label className="field_label" htmlFor="uname">
              이름
            </label>
            <input
              id="uname"
              className="field_input"
              placeholder="이름을 입력하세요"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
            />

            <label className="field_label" htmlFor="phone">
              전화번호
            </label>
            <input
              id="phone"
              className="field_input"
              placeholder="01012345678"
              value={phone}
              onChange={(e) => setPhone(e.target.value)}
              required
            />

            <fieldset className="fieldset">
              <legend className="field_label">성별</legend>
              <label className="radio_item">
                <input
                  type="radio"
                  name="gender"
                  value="male"
                  checked={gender === "male"}
                  onChange={(e) => setGender(e.target.value)}
                  required
                />
                남성
              </label>
              <label className="radio_item">
                <input
                  type="radio"
                  name="gender"
                  value="female"
                  checked={gender === "female"}
                  onChange={(e) => setGender(e.target.value)}
                  required
                />
                여성
              </label>
              <label className="radio_item">
                <input
                  type="radio"
                  name="gender"
                  value="other"
                  checked={gender === "other"}
                  onChange={(e) => setGender(e.target.value)}
                  required
                />
                기타
              </label>
              {/* 첨부파일 (선택) */}
              <div className="file_row">
                <label htmlFor="attach" className="outline_btn">
                  파일 선택
                </label>
                <input
                  id="attach"
                  type="file"
                  onChange={onPickFile}
                  style={{ display: "none" }}
                />
                <span className="file_name">{fileName}</span>
              </div>
              <p className="hint_text">
                이력서, 포트폴리오 등 (PDF, DOCX 등) — 선택 사항
              </p>
            </fieldset>

            {/* 커스텀 질문 렌더 */}
            {questions.length > 0 && (
              <div className="custom_list">
                {questions.map((q, i) => (
                  <div key={q.id} className="q_block">
                    <label className="field_label">
                      {i + 1}. {q.label}
                    </label>
                    {q.type === "textarea" ? (
                      <textarea
                        className="answer_area"
                        placeholder="답변을 입력하세요"
                        value={answers[q.id] ?? ""}
                        onChange={(e) =>
                          setAnswers((prev) => ({
                            ...prev,
                            [q.id]: e.target.value,
                          }))
                        }
                        required
                      />
                    ) : (
                      <input
                        className="field_input"
                        placeholder="답변을 입력하세요"
                        value={answers[q.id] ?? ""}
                        onChange={(e) =>
                          setAnswers((prev) => ({
                            ...prev,
                            [q.id]: e.target.value,
                          }))
                        }
                        required
                      />
                    )}
                  </div>
                ))}
              </div>
            )}

            <button type="submit" className="primary_btn submit_btn">
              제출하기
            </button>
          </form>
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
