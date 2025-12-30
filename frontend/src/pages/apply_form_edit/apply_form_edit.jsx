// src/pages/apply_form_edit/apply_form_edit.jsx
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import "../../styles/globals.css";
import "./apply_form_edit.css";

export default function ApplyFormEdit() {
  const navigate = useNavigate();

  // 고정 기본 항목
  const [dept, setDept] = useState("");
  const [studentId, setStudentId] = useState("");
  const [name, setName] = useState("");
  const [phone, setPhone] = useState("");
  const [gender, setGender] = useState("");

  // 커스텀 질문
  const [questions, setQuestions] = useState([]); // [{id, label}]
  const [answers, setAnswers] = useState({}); // {id: value}
  const [adding, setAdding] = useState(false);
  const [newQuestion, setNewQuestion] = useState("");

  const addQuestion = () => {
    const label = newQuestion.trim();
    if (!label) return;
    const id = crypto.randomUUID();
    setQuestions((prev) => [...prev, { id, label }]);
    setNewQuestion("");
    setAdding(false);
  };

  const removeQuestion = (id) => {
    setQuestions((prev) => prev.filter((q) => q.id !== id));
    setAnswers((prev) => {
      const cp = { ...prev };
      delete cp[id];
      return cp;
    });
  };

  const onSave = () => {
    const payload = {
      fixed_fields: { dept, studentId, name, phone, gender },
      custom_questions: questions.map((q) => ({
        id: q.id,
        label: q.label,
        answer: answers[q.id] ?? "",
      })),
    };
    console.log(payload);
    alert("저장 로직 연결 예정 (콘솔 확인)");
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
            <h1>지원서 수정</h1>
          </div>
        </div>
      </div>

      {/* Main */}
      <main className="page-main apply_main">
        <section className="apply_section">
          <h2 className="apply_title">지원서 양식 수정</h2>

          <div className="apply_card">
            <p className="desc">지원자가 보게 될 질문들을 추가·수정하세요.</p>

            {/* 기본 항목 */}
            <label className="field_label" htmlFor="dept">
              학과
            </label>
            <input
              id="dept"
              className="field_input"
              placeholder="소속 학과를 입력하세요"
              value={dept}
              onChange={(e) => setDept(e.target.value)}
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
            />

            <label className="field_label" htmlFor="phone">
              전화번호
            </label>
            <input
              id="phone"
              className="field_input"
              placeholder="- 제외 번호만 입력해주세요"
              value={phone}
              onChange={(e) => setPhone(e.target.value)}
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
                />
                기타
              </label>
            </fieldset>

            {/* 커스텀 질문 리스트 */}
            {questions.length > 0 && (
              <div className="custom_list">
                {questions.map((q, idx) => (
                  <div key={q.id} className="q_item">
                    <label className="field_label">
                      {idx + 1}. {q.label}
                    </label>
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
                    />
                    <button
                      type="button"
                      className="q_remove_btn"
                      onClick={() => removeQuestion(q.id)}
                    >
                      삭제
                    </button>
                  </div>
                ))}
              </div>
            )}

            {/* 질문 추가 영역 */}
            {!adding ? (
              <button
                type="button"
                className="outline_btn add_q_btn"
                onClick={() => setAdding(true)}
              >
                + 질문 추가
              </button>
            ) : (
              <div className="add_row">
                <input
                  className="field_input"
                  placeholder="추가할 질문 내용을 입력하세요"
                  value={newQuestion}
                  onChange={(e) => setNewQuestion(e.target.value)}
                />
                <div className="add_actions">
                  <button
                    type="button"
                    className="outline_btn sm"
                    onClick={() => setAdding(false)}
                  >
                    취소
                  </button>
                  <button
                    type="button"
                    className="outline_btn sm"
                    onClick={addQuestion}
                  >
                    추가
                  </button>
                </div>
              </div>
            )}

            {/* 하단 저장만 남김 */}
            <div className="form_actions">
              <button className="primary_btn save_btn" onClick={onSave}>
                저장하기
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
