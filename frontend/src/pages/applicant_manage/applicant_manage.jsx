// src/pages/applicant_manage/applicant_manage.jsx
import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import "../../styles/globals.css";
import "./applicant_manage.css";
import {
  fetch_owner_applicants,
  fetch_owner_applicant_detail,
  owner_update_applicant_status,
  owner_send_result_email,
} from "../../lib/api";

function normalize_status(s) {
  const v = String(s || "").toUpperCase();
  if (v === "ACCEPTED") return "accepted";
  if (v === "REJECTED") return "rejected";
  if (v === "PENDING") return "pending";
  if (v === "WAITING") return "pending";
  return "pending";
}

function status_label(s) {
  if (s === "accepted") return "합격";
  if (s === "rejected") return "불합격";
  return "미정";
}

function status_chip_class(s) {
  if (s === "accepted") return "accepted";
  if (s === "rejected") return "rejected";
  return "pending";
}

function safe(v) {
  return v == null ? "" : String(v);
}

export default function ApplicantManage() {
  const navigate = useNavigate();
  const { id } = useParams(); // route: /applicant_manage/:id
  const club_id = useMemo(() => (id == null ? null : String(id)), [id]);

  const [applicants, set_applicants] = useState([]);
  const [loading, set_loading] = useState(true);
  const [error_msg, set_error_msg] = useState("");

  const [selected, set_selected] = useState(null);
  const [detail_loading, set_detail_loading] = useState(false);
  const [detail_error, set_detail_error] = useState("");

  const [status_loading_map, set_status_loading_map] = useState({});
  const [email_loading, set_email_loading] = useState(false);

  const load_list = async () => {
    if (!club_id) return;

    set_loading(true);
    set_error_msg("");

    try {
      const res = await fetch_owner_applicants(club_id);
      const list = Array.isArray(res) ? res : [];

      const mapped = list.map((a) => ({
        clubMemberId: a?.clubMemberId ?? a?.club_member_id ?? a?.id ?? null,
        memberId: a?.memberId ?? a?.member_id ?? null,
        name: a?.name ?? "",
        studentId: a?.studentId ?? a?.student_id ?? "",
        appliedAt: a?.appliedAt ?? a?.applied_at ?? null,
      }));

      set_applicants(mapped.filter((x) => x.clubMemberId != null));
    } catch (e) {
      set_error_msg(e?.message || "지원자 목록을 불러오지 못했습니다.");
      set_applicants([]);
    } finally {
      set_loading(false);
    }
  };

  useEffect(() => {
    load_list();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [club_id]);

  const open_detail = async (club_member_id) => {
    if (!club_id || !club_member_id) return;

    set_selected(null);
    set_detail_error("");
    set_detail_loading(true);

    try {
      const data = await fetch_owner_applicant_detail(club_id, club_member_id);

      const applicant =
        data?.applicantInfo ?? data?.applicant ?? data?.applicant_info ?? null;
      const form =
        data?.applicationForm ?? data?.application_form ?? data?.form ?? [];

      const normalized = {
        clubMemberId: applicant?.clubMemberId ?? club_member_id,
        memberId: applicant?.memberId ?? null,
        name: applicant?.name ?? "",
        studentId: applicant?.studentId ?? "",
        department: applicant?.department ?? "",
        phoneNumber: applicant?.phoneNumber ?? "",
        email: applicant?.email ?? "",
        appliedAt: applicant?.appliedAt ?? null,
        status: normalize_status(applicant?.status),
        applicationForm: Array.isArray(form)
          ? form
              .map((q) => ({
                questionId: q?.questionId ?? null,
                orderNum: q?.orderNum ?? 0,
                questionContent: q?.questionContent ?? "",
                answerContent: q?.answerContent ?? "",
              }))
              .sort((a, b) => (a.orderNum ?? 0) - (b.orderNum ?? 0))
          : [],
      };

      set_selected(normalized);
    } catch (e) {
      set_detail_error(e?.message || "지원자 상세를 불러오지 못했습니다.");
    } finally {
      set_detail_loading(false);
    }
  };

  const close_detail = () => {
    set_selected(null);
    set_detail_error("");
    set_detail_loading(false);
  };

  const update_status = async (club_member_id, next_status) => {
    if (!club_id || !club_member_id) return;

    set_status_loading_map((prev) => ({ ...prev, [club_member_id]: true }));

    try {
      await owner_update_applicant_status(club_id, club_member_id, next_status);

      set_selected((prev) =>
        prev && String(prev.clubMemberId) === String(club_member_id)
          ? { ...prev, status: normalize_status(next_status) }
          : prev
      );

      alert("상태가 변경되었습니다.");
    } catch (e) {
      alert(e?.message || "상태 변경에 실패했습니다.");
    } finally {
      set_status_loading_map((prev) => ({ ...prev, [club_member_id]: false }));
    }
  };

  const send_result_email = async () => {
    if (!club_id) return;

    set_email_loading(true);
    try {
      await owner_send_result_email(club_id);
      alert("결과 메일 발송 요청 완료");
    } catch (e) {
      alert(e?.message || "메일 발송에 실패했습니다.");
    } finally {
      set_email_loading(false);
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
            <h1>지원자 관리</h1>
          </div>
        </div>
      </div>

      <main className="page-main applicant_main">
        <section className="applicant_section">
          <h2 className="applicant_title">지원자</h2>

          <div className="applicant_footer">
            <p className="hint_text">지원자를 클릭해서 자세히 보기</p>
            <button
              className="mail_btn"
              type="button"
              onClick={send_result_email}
              disabled={email_loading}
            >
              {email_loading ? "발송중..." : "결과 메일 발송하기"}
            </button>
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
                <button
                  key={`${a.clubMemberId}`}
                  type="button"
                  className="applicant_card applicant_card_btn"
                  onClick={() => open_detail(a.clubMemberId)}
                >
                  <span className="applicant_info">
                    {a.name} {a.studentId}
                  </span>
                  <span className="applicant_date">
                    {safe(a.appliedAt).slice(0, 10)}
                  </span>
                </button>
              ))}
            </div>
          )}
        </section>

        {/* ===== 상세 모달 ===== */}
        {(detail_loading || detail_error || selected) && (
          <div className="result_overlay" role="dialog" aria-modal="true">
            <div className="result_modal">
              <div className="result_head">
                <h3 className="result_title">지원자 상세</h3>
                <button
                  type="button"
                  className="result_close"
                  onClick={close_detail}
                >
                  닫기
                </button>
              </div>

              {detail_loading ? (
                <div className="result_body">불러오는 중...</div>
              ) : detail_error ? (
                <div className="result_body">{detail_error}</div>
              ) : (
                <div className="result_body">
                  <div className="applicant_detail_box">
                    <div className="row">
                      <span className="k">이름</span>
                      <span className="v">{selected?.name}</span>
                    </div>
                    <div className="row">
                      <span className="k">학번</span>
                      <span className="v">{selected?.studentId}</span>
                    </div>
                    <div className="row">
                      <span className="k">학과</span>
                      <span className="v">{selected?.department}</span>
                    </div>
                    <div className="row">
                      <span className="k">전화</span>
                      <span className="v">{selected?.phoneNumber}</span>
                    </div>
                    <div className="row">
                      <span className="k">이메일</span>
                      <span className="v">{selected?.email}</span>
                    </div>
                    <div className="row">
                      <span className="k">지원일</span>
                      <span className="v">{safe(selected?.appliedAt)}</span>
                    </div>

                    <div className="row">
                      <span className="k">상태</span>
                      <span
                        className={`v badge ${status_chip_class(
                          selected?.status
                        )}`}
                      >
                        {status_label(selected?.status)}
                      </span>
                    </div>

                    <div className="status_actions">
                      <button
                        type="button"
                        className="outline_btn sm"
                        disabled={!!status_loading_map[selected.clubMemberId]}
                        onClick={() =>
                          update_status(selected.clubMemberId, "PENDING")
                        }
                      >
                        미정
                      </button>
                      <button
                        type="button"
                        className="outline_btn sm"
                        disabled={!!status_loading_map[selected.clubMemberId]}
                        onClick={() =>
                          update_status(selected.clubMemberId, "ACCEPTED")
                        }
                      >
                        합격
                      </button>
                      <button
                        type="button"
                        className="outline_btn sm"
                        disabled={!!status_loading_map[selected.clubMemberId]}
                        onClick={() =>
                          update_status(selected.clubMemberId, "REJECTED")
                        }
                      >
                        불합격
                      </button>
                    </div>
                  </div>

                  <div className="application_form_box">
                    <h4 className="sub_title">지원서 답변</h4>

                    {selected?.applicationForm?.length ? (
                      <div className="qa_list">
                        {selected.applicationForm.map((q) => (
                          <div
                            key={`${q.questionId}-${q.orderNum}`}
                            className="qa_item"
                          >
                            <div className="q">
                              {q.orderNum + 1}. {q.questionContent}
                            </div>
                            <div className="a">{q.answerContent || "-"}</div>
                          </div>
                        ))}
                      </div>
                    ) : (
                      <p className="empty">지원서 답변이 없습니다.</p>
                    )}
                  </div>
                </div>
              )}
            </div>
          </div>
        )}
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
