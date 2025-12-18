// src/pages/apply_form_edit/apply_form_edit.jsx
import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
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

function dedupe_contents(contents) {
  const out = [];
  const seen = new Set();
  for (const c of contents) {
    const normalized = normalize_content(c);
    if (!normalized) continue;
    const key = normalized.toLowerCase();
    if (seen.has(key)) continue;
    seen.add(key);
    out.push(normalized);
  }
  return out;
}

function dedupe_questions_by_content(list) {
  const out = [];
  const seen = new Set();

  for (const q of list) {
    const content = normalize_content(q?.content);
    if (!content) continue;
    const key = content.toLowerCase();
    if (seen.has(key)) continue;
    seen.add(key);

    out.push({
      questionId: q?.questionId ?? q?.id ?? `q-${crypto.randomUUID()}`,
      orderNum: out.length,
      content,
    });
  }

  return out;
}

export default function ApplyFormEdit() {
  const navigate = useNavigate();
  const { id } = useParams(); // /apply_form_edit/:id (clubId)
  const club_id = useMemo(() => (id == null ? null : String(id)), [id]);

  // 고정 기본 항목(UI 유지용)
  const [dept, setDept] = useState("");
  const [studentId, setStudentId] = useState("");
  const [name, setName] = useState("");
  const [phone, setPhone] = useState("");
  const [gender, setGender] = useState("male");

  // 커스텀 질문
  const [questions, setQuestions] = useState([]); // [{ questionId, orderNum, content }]
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

        // ✅ GET 결과도 중복 제거 + orderNum 재정렬
        const deduped = dedupe_questions_by_content(normalized);

        setQuestions(deduped);
      } catch (e) {
        set_error_msg(e?.message || "질문 목록을 불러오지 못했습니다.");
        setQuestions([]);
      } finally {
        setLoading(false);
      }
    };

    load();
  }, [club_id, navigate]);

  const addQuestion = () => {
    const content = normalize_content(newQuestion);
    if (!content) return;

    setQuestions((prev) => {
      const contents = dedupe_contents([
        ...(prev || []).map((q) => q.content),
        content,
      ]);

      return contents.map((c, idx) => ({
        questionId: prev?.[idx]?.questionId ?? `local-${crypto.randomUUID()}`,
        orderNum: idx,
        content: c,
      }));
    });

    setNewQuestion("");
    setAdding(false);
  };

  const removeQuestion = (qid) => {
    setQuestions((prev) => {
      const filtered = (prev || []).filter(
        (q) => String(q.questionId) !== String(qid)
      );

      // ✅ content 기반 중복 제거 + orderNum 재정렬
      const deduped = dedupe_questions_by_content(
        filtered.map((q, idx) => ({ ...q, orderNum: idx }))
      );

      return deduped.map((q, idx) => ({ ...q, orderNum: idx }));
    });
  };

  const updateQuestionContent = (qid, content) => {
    setQuestions((prev) => {
      const updated = (prev || []).map((q) =>
        String(q.questionId) === String(qid) ? { ...q, content } : { ...q }
      );

      // ✅ 편집 중엔 즉시 중복 제거하면 UX가 불편할 수 있어서
      // 저장 시에만 강하게 dedupe. 여기선 orderNum만 유지.
      return updated.map((q, idx) => ({ ...q, orderNum: idx }));
    });
  };

  const onSave = async () => {
    if (!club_id) return;

    setSaving(true);
    set_error_msg("");

    try {
      // 1) content 정리
      const cleaned = (questions || [])
        .map((q) => normalize_content(q.content))
        .filter((c) => c.length > 0);

      // 2) ✅ 중복 제거 (같은 문구가 여러개면 1개만)
      const unique = dedupe_contents(cleaned);

      // 3) swagger 형식 payload 만들기
      const payload = unique.map((content, idx) => ({
        orderNum: idx,
        content,
      }));

      console.log("[save payload]", payload);

      // ✅ 질문이 0개면 PUT을 안 때림 (백이 기본 질문 만들 가능성 방지)
      if (payload.length === 0) {
        alert("저장할 질문이 없습니다. 질문 추가 후 저장하세요.");
        return;
      }

      await owner_update_club_questions(club_id, payload);
      alert("저장되었습니다.");

      // ✅ 저장 성공 후 화면도 payload 기준으로 동기화(중복/누적 방지)
      setQuestions(
        payload.map((p) => ({
          questionId: `saved-${p.orderNum}-${crypto.randomUUID()}`,
          orderNum: p.orderNum,
          content: p.content,
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

      <main className="page-main apply_main">
        <section className="apply_section">
          <h2 className="apply_title">지원서 양식 수정</h2>

          <div className="apply_card">
            {error_msg && <p className="mypage_error">{error_msg}</p>}

            {loading ? (
              <p className="desc">불러오는 중...</p>
            ) : (
              <>
                <p className="desc">
                  지원자가 보게 될 질문들을 추가·수정하세요.
                </p>

                {/* 기본 항목(UI 유지용) */}
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

                {/* 질문 추가 */}
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

                <div className="form_actions">
                  <button
                    className="primary_btn save_btn"
                    onClick={onSave}
                    disabled={saving}
                  >
                    {saving ? "저장중..." : "저장하기"}
                  </button>
                </div>
              </>
            )}
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
