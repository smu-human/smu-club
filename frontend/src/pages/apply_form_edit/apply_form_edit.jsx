// src/pages/apply_form_edit/apply_form_edit.jsx
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import "../../styles/globals.css";
import "./apply_form_edit.css";
import {
  fetch_owner_club_questions,
  owner_update_club_questions,
  is_logged_in,
} from "../../lib/api";

function normalize_content(s) {
  return String(s || "")
    .replace(/\s+/g, " ")
    .trim();
}

function is_file_question_content(content) {
  return normalize_content(content).toLowerCase() === "파일추가";
}

function dedupe_questions(list) {
  const out = [];
  const seen = new Set();
  let has_file = false;

  for (const q of list || []) {
    const content = normalize_content(q?.content);

    if (!content) continue;

    if (is_file_question_content(content)) {
      if (has_file) continue;
      has_file = true;
      out.push({
        questionId: q?.questionId ?? q?.id ?? `q-${crypto.randomUUID()}`,
        orderNum: out.length,
        content: "파일추가",
        type: "file",
      });
      continue;
    }

    const key = content.toLowerCase();
    if (seen.has(key)) continue;
    seen.add(key);

    out.push({
      questionId: q?.questionId ?? q?.id ?? `q-${crypto.randomUUID()}`,
      orderNum: out.length,
      content,
      type: "text",
    });
  }

  return out.map((q, idx) => ({ ...q, orderNum: idx }));
}

export default function ApplyFormEdit() {
  const navigate = useNavigate();
  const { id } = useParams();
  const club_id = useMemo(() => (id == null ? null : String(id)), [id]);

  const [dept, setDept] = useState("");
  const [studentId, setStudentId] = useState("");
  const [name, setName] = useState("");
  const [phone, setPhone] = useState("");
  const [gender, setGender] = useState("");

  const [questions, setQuestions] = useState([]); // text only in UI
  const [has_file_upload, set_has_file_upload] = useState(false); // file fixed item
  const [adding, setAdding] = useState(false);
  const [newQuestion, setNewQuestion] = useState("");

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error_msg, set_error_msg] = useState("");

  useEffect(() => {
    if (!is_logged_in()) {
      navigate("/login");
      return;
    }

    const load = async () => {
      if (!club_id) {
        set_error_msg("club id가 없습니다.");
        setLoading(false);
        return;
      }

      setLoading(true);
      set_error_msg("");

      try {
        const data = await fetch_owner_club_questions(club_id);
        const list = Array.isArray(data) ? data : [];

        const normalized = list
          .map((q) => ({
            questionId: q?.questionId ?? q?.id ?? null,
            orderNum: typeof q?.orderNum === "number" ? q.orderNum : 0,
            content: q?.content ?? q?.label ?? "",
          }))
          .sort((a, b) => (a.orderNum ?? 0) - (b.orderNum ?? 0));

        const deduped = dedupe_questions(normalized);

        // ✅ 파일추가는 "질문 리스트"에서 빼고, 별도 섹션으로만 보이게
        const file_item = deduped.find((q) => q.type === "file");
        set_has_file_upload(!!file_item);

        const only_text = deduped.filter((q) => q.type !== "file");
        setQuestions(only_text);
      } catch (e) {
        set_error_msg(e?.message || "질문 목록을 불러오지 못했습니다.");
        setQuestions([]);
        set_has_file_upload(false);
      } finally {
        setLoading(false);
      }
    };

    load();
  }, [club_id, navigate]);

  const addQuestion = () => {
    const content = normalize_content(newQuestion);
    if (!content) return;

    if (is_file_question_content(content)) {
      alert("파일 업로드 항목은 기본으로 제공됩니다.");
      return;
    }

    setQuestions((prev) => {
      const merged = [
        ...(prev || []),
        {
          questionId: `local-${crypto.randomUUID()}`,
          orderNum: prev?.length ?? 0,
          content,
          type: "text",
        },
      ];

      // text 중복 제거 + orderNum 재정렬
      const out = [];
      const seen = new Set();
      for (const q of merged) {
        const c = normalize_content(q.content);
        if (!c) continue;
        const key = c.toLowerCase();
        if (seen.has(key)) continue;
        seen.add(key);
        out.push({ ...q, content: c });
      }
      return out.map((q, idx) => ({ ...q, orderNum: idx }));
    });

    setNewQuestion("");
    setAdding(false);
  };

  const removeQuestion = (qid) => {
    setQuestions((prev) => {
      const filtered = (prev || []).filter(
        (q) => String(q.questionId) !== String(qid)
      );
      return filtered.map((q, idx) => ({ ...q, orderNum: idx }));
    });
  };

  const updateQuestionContent = (qid, content) => {
    setQuestions((prev) => {
      const next = (prev || []).map((q) =>
        String(q.questionId) === String(qid) ? { ...q, content } : q
      );
      return next.map((q, idx) => ({ ...q, orderNum: idx }));
    });
  };

  const onSave = async () => {
    if (!club_id) return;

    setSaving(true);
    set_error_msg("");

    try {
      // text 질문 정리 + 중복 제거
      const cleaned = (questions || [])
        .map((q) => normalize_content(q.content))
        .filter((c) => c.length > 0);

      const seen = new Set();
      const unique_texts = [];
      for (const c of cleaned) {
        const key = c.toLowerCase();
        if (seen.has(key)) continue;
        seen.add(key);
        unique_texts.push(c);
      }

      // ✅ 백엔드 요구: 파일추가를 마지막에 항상 포함
      const payload = [
        ...unique_texts.map((content, idx) => ({ orderNum: idx, content })),
        ...(has_file_upload
          ? [{ orderNum: unique_texts.length, content: "파일추가" }]
          : []),
      ];

      // 질문이 0개 + 파일추가만 있는 경우도 저장 허용(원하면 막아도 됨)
      if (payload.length === 0) {
        alert("저장할 항목이 없습니다.");
        return;
      }

      await owner_update_club_questions(club_id, payload);
      alert("저장되었습니다.");

      // 화면 동기화
      setQuestions(
        unique_texts.map((c, idx) => ({
          questionId: `saved-${idx}-${crypto.randomUUID()}`,
          orderNum: idx,
          content: c,
          type: "text",
        }))
      );
    } catch (e) {
      set_error_msg(e?.message || "저장에 실패했습니다.");
    } finally {
      setSaving(false);
    }
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

                {/* ✅ 커스텀 텍스트 질문만 렌더 */}
                {questions.length > 0 && (
                  <div className="custom_list">
                    {questions
                      .slice()
                      .sort((a, b) => (a.orderNum ?? 0) - (b.orderNum ?? 0))
                      .map((q, idx) => (
                        <div key={q.questionId} className="q_item">
                          <label className="field_label">{idx + 1}. 질문</label>

                          <textarea
                            className="answer_area"
                            placeholder="질문 내용을 입력하세요"
                            value={q.content ?? ""}
                            onChange={(e) =>
                              updateQuestionContent(
                                q.questionId,
                                e.target.value
                              )
                            }
                          />

                          <button
                            type="button"
                            className="q_remove_btn"
                            onClick={() => removeQuestion(q.questionId)}
                          >
                            삭제
                          </button>
                        </div>
                      ))}
                  </div>
                )}

                {!adding ? (
                  <button
                    type="button"
                    className="outline_btn sm"
                    onClick={() => setAdding(false)}
                  >
                    취소
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
                        onClick={() => {
                          setAdding(false);
                          setNewQuestion("");
                        }}
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
                {/* ✅ 파일 업로드 섹션(질문 리스트 밖으로 분리) */}
                {has_file_upload && (
                  <div className="file_upload_section">
                    <label className="field_label"></label>
                    <button type="button" className="outline_btn" disabled>
                      파일 업로드 버튼
                    </button>
                  </div>
                )}
                <div className="form_actions">
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
