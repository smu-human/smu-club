// src/pages/apply_form_change/apply_form_change.jsx (전체 교체)
import { useEffect, useMemo, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import "../../styles/globals.css";
import "./apply_form_change.css";
import {
  is_logged_in,
  fetch_application_for_update,
  update_application,
  delete_application,
  member_issue_application_upload_url,
  member_put_presigned_url,
} from "../../lib/api";

function unwrap_api(res) {
  // 백이 {status,message,data:{...}} 형태면 data만 꺼내쓰기
  if (res && typeof res === "object") {
    if (res.data != null && typeof res.data === "object") return res.data;
    if (res.result != null && typeof res.result === "object") return res.result;
  }
  return res;
}

function normalize_content(s) {
  return String(s || "")
    .replace(/\s+/g, " ")
    .trim();
}

// ✅ "파일추가" 고정이 아니라, "파일 업로드/첨부" 문구도 파일항목으로 인식
function is_file_question_content(content) {
  const t = normalize_content(content).toLowerCase();

  if (!t) return false;

  if (t === "파일추가") return true;

  // 더 넓게 인식(백에서 문구 고정해도 잡히게)
  if (t.includes("파일") && (t.includes("업로드") || t.includes("첨부")))
    return true;

  if (t.includes("포트폴리오") && (t.includes("업로드") || t.includes("첨부")))
    return true;

  if (t.includes("이력서") && (t.includes("업로드") || t.includes("첨부")))
    return true;

  // 안전빵: 문구가 조금 달라도 "파일"만 들어가면 파일항목으로 취급
  if (t.includes("파일")) return true;

  return false;
}

function infer_input_type(question_content) {
  const t = normalize_content(question_content).toLowerCase();
  if (t.includes("성별")) return "gender";
  return "text";
}

function infer_gender_value(answer_content) {
  const v = normalize_content(answer_content);
  if (v === "남" || v === "남성" || v.toLowerCase() === "male") return "male";
  if (v === "여" || v === "여성" || v.toLowerCase() === "female")
    return "female";
  if (!v) return "";
  return "other";
}

function display_file_name(fileKey) {
  if (!fileKey) return "";
  const s = String(fileKey);
  const parts = s.split("/");
  return parts[parts.length - 1];
}

export default function ApplyFormChange() {
  const navigate = useNavigate();
  const { id } = useParams();
  const club_id = useMemo(() => (id == null ? null : String(id)), [id]);

  const [loading, set_loading] = useState(true);
  const [saving, set_saving] = useState(false);
  const [error_msg, set_error_msg] = useState("");

  const [member_info, set_member_info] = useState({
    memberId: "",
    studentId: "",
    name: "",
    phone: "",
  });

  const [questions, set_questions] = useState([]); // file 제외 질문만
  const [has_file_upload, set_has_file_upload] = useState(false);
  const [file_key, set_file_key] = useState("");

  const file_input_ref = useRef(null);

  const load = async () => {
    if (!club_id) {
      set_error_msg("club id가 없습니다.");
      set_loading(false);
      return;
    }

    set_loading(true);
    set_error_msg("");

    try {
      const raw = await fetch_application_for_update(club_id);
      const data = unwrap_api(raw);

      set_member_info({
        memberId: data?.memberId ?? "",
        studentId: data?.studentId ?? "",
        name: data?.name ?? "",
        phone: data?.phone ?? "",
      });

      // ✅ swagger 응답: data.fileKey 존재
      const server_file_key = data?.fileKey ?? data?.file_key ?? "";
      set_file_key(server_file_key || "");

      const raw_qna =
        data?.questionAndAnswer ??
        data?.question_and_answer ??
        data?.questions ??
        data?.qna ??
        [];

      const list = (Array.isArray(raw_qna) ? raw_qna : [])
        .map((q) => ({
          questionId: q?.questionId ?? q?.id ?? null,
          orderNum:
            typeof q?.orderNum === "number"
              ? q.orderNum
              : typeof q?.orderNumber === "number"
              ? q.orderNumber
              : 0,
          questionContent: q?.questionContent ?? q?.content ?? "",
          answerContent: q?.answerContent ?? q?.answer ?? "",
        }))
        .filter((q) => q.questionId != null)
        .sort((a, b) => (a.orderNum ?? 0) - (b.orderNum ?? 0))
        .map((q) => {
          const qc = normalize_content(q.questionContent);
          const is_file = is_file_question_content(qc);
          const type = is_file ? "file" : infer_input_type(qc);
          return { ...q, questionContent: qc, type };
        });

      const file_item = list.find((q) => q.type === "file");
      set_has_file_upload(!!file_item);

      // ✅ file_key가 서버에서 안 오고, 파일 질문 answerContent로 오는 케이스도 커버
      if (!server_file_key && file_item?.answerContent) {
        set_file_key(String(file_item.answerContent));
      }

      // ✅ 파일 질문은 answers(textarea)에서 제거
      set_questions(list.filter((q) => q.type !== "file"));
    } catch (e) {
      set_error_msg(e?.message || "지원서 정보를 불러오지 못했습니다.");
      set_member_info({ memberId: "", studentId: "", name: "", phone: "" });
      set_questions([]);
      set_has_file_upload(false);
      set_file_key("");
    } finally {
      set_loading(false);
    }
  };

  useEffect(() => {
    if (!is_logged_in()) {
      navigate("/login");
      return;
    }
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [club_id, navigate]);

  const update_answer = (question_id, next_value) => {
    set_questions((prev) =>
      (prev || []).map((q) =>
        String(q.questionId) === String(question_id)
          ? { ...q, answerContent: next_value }
          : q
      )
    );
  };

  const on_pick_file = () => file_input_ref.current?.click();

  const on_file_change = async (e) => {
    const file = e.target.files?.[0];
    e.target.value = "";
    if (!file) return;

    try {
      set_saving(true);

      const issued_raw = await member_issue_application_upload_url(file);
      const issued = unwrap_api(issued_raw);

      // ✅ issued.data 안에 있을 수도 있어서 unwrap_api로 정리
      const preSignedUrl = issued?.preSignedUrl ?? issued?.presignedUrl ?? "";
      const fileName = issued?.fileName ?? issued?.filename ?? "";

      if (!preSignedUrl || !fileName) {
        throw new Error("업로드 URL 발급 응답이 올바르지 않습니다.");
      }

      await member_put_presigned_url(preSignedUrl, file);

      // ✅ update API에 넣을 fileKey = fileName
      set_file_key(fileName);
      alert("파일이 업로드되었습니다.");
    } catch (err) {
      alert(err?.message || "파일 업로드에 실패했습니다.");
    } finally {
      set_saving(false);
    }
  };

  const on_save = async () => {
    if (!club_id) return;
    if (saving) return;

    set_saving(true);
    set_error_msg("");

    try {
      // ✅ swagger update body: { answers: [{questionId, answerContent}], fileKey }
      const answers = (questions || [])
        .filter((q) => q?.questionId != null)
        .map((q) => ({
          questionId: Number(q.questionId) || q.questionId,
          answerContent: String(q.answerContent ?? ""),
        }));

      // ✅ 핵심: fileKey를 ""로 보내지 말고 현재 file_key 상태를 그대로 보내야 함
      // (파일 항목이 있을 때만)
      const payload = {
        answers,
        fileKey: has_file_upload ? String(file_key || "") : "",
      };

      await update_application(club_id, payload);

      alert("수정이 완료되었습니다.");
      await load();
    } catch (e) {
      set_error_msg(e?.message || "저장에 실패했습니다.");
    } finally {
      set_saving(false);
    }
  };

  const on_delete = async () => {
    if (!club_id) return;
    if (saving) return;

    const ok = window.confirm("정말 지원을 취소(삭제)하시겠습니까?");
    if (!ok) return;

    try {
      set_saving(true);
      await delete_application(club_id);
      alert("지원이 취소되었습니다.");
      navigate("/mypage");
    } catch (e) {
      alert(e?.message || "지원 취소에 실패했습니다.");
    } finally {
      set_saving(false);
    }
  };

  if (loading) {
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
            <h2 className="apply_title">지원서 수정</h2>
            <div className="apply_card">불러오는 중...</div>
          </section>
        </main>
      </div>
    );
  }

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
          <h2 className="apply_title">지원서 수정</h2>

          <div className="apply_card">
            {error_msg && <p className="mypage_error">{error_msg}</p>}

            <p className="desc">
              제출한 지원서를 수정하거나, 지원을 취소할 수 있습니다.{" "}
              {saving ? (
                <span style={{ opacity: 0.7 }}>(처리중...)</span>
              ) : null}
            </p>

            <label className="field_label">학번</label>
            <input
              className="field_input"
              value={member_info.studentId || ""}
              disabled
              readOnly
            />

            <label className="field_label">이름</label>
            <input
              className="field_input"
              value={member_info.name || ""}
              disabled
              readOnly
            />

            <label className="field_label">전화번호</label>
            <input
              className="field_input"
              value={member_info.phone || ""}
              disabled
              readOnly
            />

            {(questions || []).length > 0 ? (
              <div className="custom_list">
                {questions
                  .slice()
                  .sort((a, b) => (a.orderNum ?? 0) - (b.orderNum ?? 0))
                  .map((q, idx) => {
                    const is_gender = q.type === "gender";
                    const gender_value = infer_gender_value(q.answerContent);

                    return (
                      <div key={q.questionId} className="q_item">
                        <label className="field_label">
                          {idx + 1}. {q.questionContent || "질문"}
                        </label>

                        {is_gender ? (
                          <fieldset className="fieldset">
                            <legend
                              className="field_label"
                              style={{ display: "none" }}
                            >
                              성별
                            </legend>

                            <label className="radio_item">
                              <input
                                type="radio"
                                name={`gender-${q.questionId}`}
                                value="male"
                                checked={gender_value === "male"}
                                onChange={() =>
                                  update_answer(q.questionId, "남성")
                                }
                                disabled={saving}
                              />
                              남성
                            </label>

                            <label className="radio_item">
                              <input
                                type="radio"
                                name={`gender-${q.questionId}`}
                                value="female"
                                checked={gender_value === "female"}
                                onChange={() =>
                                  update_answer(q.questionId, "여성")
                                }
                                disabled={saving}
                              />
                              여성
                            </label>

                            <label className="radio_item">
                              <input
                                type="radio"
                                name={`gender-${q.questionId}`}
                                value="other"
                                checked={gender_value === "other"}
                                onChange={() =>
                                  update_answer(q.questionId, "기타")
                                }
                                disabled={saving}
                              />
                              기타
                            </label>
                          </fieldset>
                        ) : (
                          <textarea
                            className="answer_area"
                            placeholder="답변을 입력하세요"
                            value={q.answerContent ?? ""}
                            onChange={(e) =>
                              update_answer(q.questionId, e.target.value)
                            }
                            disabled={saving}
                          />
                        )}
                      </div>
                    );
                  })}
              </div>
            ) : (
              <p className="desc" style={{ opacity: 0.8 }}>
                수정 가능한 질문이 없습니다.
              </p>
            )}

            {/* ✅ 파일 질문 textarea는 제거하고, 파일 버튼으로 대체 */}
            {has_file_upload && (
              <div className="file_upload_section">
                <label className="field_label">첨부파일</label>

                <div className="file_row">
                  <span className="file_name">
                    {file_key ? display_file_name(file_key) : "첨부 없음"}
                  </span>

                  <input
                    ref={file_input_ref}
                    type="file"
                    style={{ display: "none" }}
                    onChange={on_file_change}
                  />

                  <button
                    type="button"
                    className="outline_btn sm"
                    onClick={on_pick_file}
                    disabled={saving}
                  >
                    파일 변경
                  </button>
                </div>
              </div>
            )}

            <div className="form_actions">
              <button
                type="button"
                className="delete_btn"
                onClick={on_delete}
                disabled={saving}
                style={{ borderColor: "#a82d2f", color: "#a82d2f" }}
              >
                지원 취소(삭제)
              </button>

              <button
                type="button"
                className="primary_btn save_btn"
                onClick={on_save}
                disabled={saving}
              >
                {saving ? "저장중..." : "수정 저장"}
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
