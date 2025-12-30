// src/pages/apply_form_submit/apply_form_submit.jsx (전체 교체)
import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams, useLocation } from "react-router-dom";
import "../../styles/globals.css";
import "./apply_form_submit.css";
import {
  fetch_member_club_apply,
  member_issue_application_upload_url,
  member_put_presigned_url,
  member_apply_club,
} from "../../lib/api";

function normalize_content(s) {
  return String(s || "")
    .replace(/\s+/g, " ")
    .trim();
}

function is_file_question_content(content) {
  return normalize_content(content).toLowerCase() === "파일추가";
}

function pick_questions_from_apply_data(applyData) {
  const raw =
    applyData?.questions ??
    applyData?.customQuestions ??
    applyData?.clubQuestions ??
    applyData?.questionList ??
    applyData?.data ??
    [];

  const list = Array.isArray(raw) ? raw : [];

  const mapped = list
    .map((q, idx) => ({
      questionId: q?.questionId ?? q?.id ?? idx,
      orderNum:
        typeof q?.orderNum === "number"
          ? q.orderNum
          : typeof q?.orderNumber === "number"
          ? q.orderNumber
          : idx,
      content: q?.content ?? q?.label ?? q?.question ?? "",
    }))
    .filter((q) => normalize_content(q.content).length > 0)
    .sort((a, b) => (a.orderNum ?? 0) - (b.orderNum ?? 0));

  const file_item = mapped.find((q) => is_file_question_content(q.content));

  // ✅ 파일추가 제외한 텍스트 질문만
  const text_only = mapped.filter((q) => !is_file_question_content(q.content));

  // ✅ 중복 제거(content 기준)
  const seen = new Set();
  const out = [];
  for (const q of text_only) {
    const key = normalize_content(q.content).toLowerCase();
    if (seen.has(key)) continue;
    seen.add(key);
    out.push({
      questionId: Number.isFinite(Number(q.questionId))
        ? Number(q.questionId)
        : q.questionId,
      orderNum: q.orderNum,
      questionContent: normalize_content(q.content),
    });
  }

  return { questions: out, has_file: !!file_item };
}

export default function ApplyFormSubmit() {
  const navigate = useNavigate();
  const { id } = useParams();
  const location = useLocation();

  const club_id = useMemo(() => {
    const from_state =
      location?.state?.clubId ??
      location?.state?.club?.id ??
      location?.state?.club?.clubId;
    const v = from_state ?? id ?? "";
    return v === undefined || v === null ? "" : String(v);
  }, [location, id]);

  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error_msg, set_error_msg] = useState("");

  const [custom_questions, set_custom_questions] = useState([]); // [{questionId, orderNum, questionContent}]
  const [has_file_upload, set_has_file_upload] = useState(false);

  // 기본 항목(지금은 UI만 유지)
  const [dept, setDept] = useState("");
  const [studentId, setStudentId] = useState("");
  const [name, setName] = useState("");
  const [phone, setPhone] = useState("");
  const [gender, setGender] = useState("");

  // 파일
  const [picked_file, set_picked_file] = useState(null);
  const [fileName, setFileName] = useState("선택된 파일 없음");

  // 답변: { [questionId]: string }
  const [answers, setAnswers] = useState({});

  useEffect(() => {
    const load = async () => {
      if (!club_id) {
        set_error_msg("club id가 없습니다.");
        setLoading(false);
        return;
      }

      setLoading(true);
      set_error_msg("");

      try {
        const applyData = await fetch_member_club_apply(club_id);
        const parsed = pick_questions_from_apply_data(applyData);

        set_custom_questions(parsed.questions);
        set_has_file_upload(parsed.has_file);

        setAnswers((prev) => {
          const next = { ...(prev || {}) };
          for (const q of parsed.questions) {
            const key = String(q.questionId);
            if (next[key] == null) next[key] = "";
          }
          for (const k of Object.keys(next)) {
            if (
              !parsed.questions.find((q) => String(q.questionId) === String(k))
            ) {
              delete next[k];
            }
          }
          return next;
        });
      } catch (e) {
        set_error_msg(e?.message || "지원서 정보를 불러오지 못했습니다.");
        set_custom_questions([]);
        set_has_file_upload(false);
      } finally {
        setLoading(false);
      }
    };

    load();
  }, [club_id]);

  const onPickFile = (e) => {
    const f = e.target.files?.[0] || null;
    set_picked_file(f);
    setFileName(f ? f.name : "선택된 파일 없음");
  };

  const onSubmit = async (e) => {
    e.preventDefault();
    if (!club_id || submitting) return;

    setSubmitting(true);
    set_error_msg("");

    try {
      // 1) 파일 업로드(선택)
      let file_key = "";
      if (picked_file) {
        const issued = await member_issue_application_upload_url(picked_file);
        if (!issued?.preSignedUrl || !issued?.fileName) {
          throw new Error("업로드 URL 응답이 올바르지 않습니다.");
        }
        await member_put_presigned_url(issued.preSignedUrl, picked_file);
        file_key = issued.fileName; // ✅ swagger의 fileKey로 사용
      }

      // 2) questionAndAnswer 만들기 (swagger 스키마)
      const questionAndAnswer = (custom_questions || []).map((q) => ({
        questionId: Number(q.questionId),
        orderNum: String(q.orderNum ?? ""),
        questionContent: q.questionContent ?? "",
        answerContent: String(answers[String(q.questionId)] ?? "").trim(),
      }));

      // 3) 제출 payload
      const payload = {
        fileKey: file_key,
        questionAndAnswer,
      };

      await member_apply_club(club_id, payload);

      alert("지원서가 제출되었습니다.");
      navigate(-1);
    } catch (err) {
      set_error_msg(err?.message || "제출에 실패했습니다.");
    } finally {
      setSubmitting(false);
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
            <h1>지원서 작성</h1>
          </div>
        </div>
      </div>

      <main className="page-main apply_submit_main">
        <section className="apply_submit_section">
          <h2 className="apply_submit_title">온라인 지원</h2>

          <form className="apply_submit_card" onSubmit={onSubmit}>
            {error_msg && <p className="mypage_error">{error_msg}</p>}

            {loading ? (
              <p className="desc">불러오는 중...</p>
            ) : (
              <>
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

                  {/* ✅ 파일 업로드(백엔드에 파일추가 질문이 있을 때만 보여줄 수도 있음) */}
                  {has_file_upload && (
                    <>
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
                        이력서, 포트폴리오 등 — 선택 사항
                      </p>
                    </>
                  )}
                </fieldset>

                {/* ✅ 커스텀 질문(텍스트만) */}
                {custom_questions.length > 0 && (
                  <div className="custom_list">
                    {custom_questions.map((q, i) => (
                      <div key={String(q.questionId)} className="q_block">
                        <label className="field_label">
                          {i + 1}. {q.questionContent}
                        </label>
                        <textarea
                          className="answer_area"
                          placeholder="답변을 입력하세요"
                          value={answers[String(q.questionId)] ?? ""}
                          onChange={(e) =>
                            setAnswers((prev) => ({
                              ...(prev || {}),
                              [String(q.questionId)]: e.target.value,
                            }))
                          }
                          required
                        />
                      </div>
                    ))}
                  </div>
                )}

                <button
                  type="submit"
                  className="primary_btn submit_btn"
                  disabled={submitting}
                >
                  {submitting ? "제출중..." : "제출하기"}
                </button>
              </>
            )}
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
