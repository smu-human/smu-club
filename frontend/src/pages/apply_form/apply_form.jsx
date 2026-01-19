// src/pages/apply_form/apply_form.jsx (전체 교체)
import { useEffect, useMemo, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import "../../styles/globals.css";
import "./apply_form.css";
import {
  fetch_owner_applicant_detail,
  owner_update_applicant_status,
  owner_issue_application_download_url,
} from "../../lib/api";

function normalize_status(s) {
  const v = String(s || "").toUpperCase();
  if (v === "ACCEPTED") return "pass";
  if (v === "REJECTED") return "fail";
  if (v === "PENDING") return "pending";
  if (v === "WAITING") return "pending";
  return "pending";
}

function decision_to_api(decision) {
  if (decision === "pass") return "ACCEPTED";
  if (decision === "fail") return "REJECTED";
  return "PENDING";
}

function unwrap_api(res) {
  if (res && typeof res === "object") {
    if (res.data != null && typeof res.data === "object") return res.data;
    if (res.result != null && typeof res.result === "object") return res.result;
  }
  return res;
}

function is_http_url(v) {
  const s = String(v || "").trim();
  return s.startsWith("http://") || s.startsWith("https://");
}

function display_file_name(fileKey) {
  if (!fileKey) return "";
  const s = String(fileKey);
  const parts = s.split("/");
  return parts[parts.length - 1];
}

export default function ApplyForm() {
  const navigate = useNavigate();
  const { clubId, clubMemberId } = useParams();

  const club_id = useMemo(
    () => (clubId == null ? null : String(clubId)),
    [clubId],
  );
  const club_member_id = useMemo(
    () => (clubMemberId == null ? null : String(clubMemberId)),
    [clubMemberId],
  );

  const [loading, set_loading] = useState(true);
  const [error_msg, set_error_msg] = useState("");

  const [applicant_name, set_applicant_name] = useState("지원서");

  const [form, set_form] = useState({
    dept: "",
    studentId: "",
    name: "",
    phone: "",
    gender: "",
    intro: "",
    attachment: "",
  });

  const [decision, setDecision] = useState("pending");
  const [saving, set_saving] = useState(false);

  const [file_key, set_file_key] = useState(""); // key or url
  const [file_url, set_file_url] = useState("");
  const [file_url_loading, set_file_url_loading] = useState(false);

  const request_seq = useRef(0);

  const load_detail = async () => {
    if (!club_id || !club_member_id) {
      set_error_msg(
        "club id / club member id가 없습니다. (라우트 파라미터 확인)",
      );
      set_loading(false);
      return;
    }

    const seq = ++request_seq.current;

    set_loading(true);
    set_error_msg("");

    try {
      const raw = await fetch_owner_applicant_detail(club_id, club_member_id);
      const data = unwrap_api(raw);

      if (seq !== request_seq.current) return;

      const applicant =
        data?.applicantInfo ?? data?.applicant ?? data?.applicant_info ?? null;

      const application_form =
        data?.applicationForm ??
        data?.application_form ??
        data?.application_form_list ??
        data?.form ??
        data?.application ??
        data?.applicationForms ??
        data?.application_forms ??
        [];

      const name = applicant?.name ?? "";
      const student_id = applicant?.studentId ?? applicant?.student_id ?? "";
      const department = applicant?.department ?? "";
      const phone_number =
        applicant?.phoneNumber ?? applicant?.phone_number ?? "";

      const find_answer = (matcher) => {
        const arr = Array.isArray(application_form) ? application_form : [];
        const hit = arr.find((q) => matcher(String(q?.questionContent ?? "")));
        return String(hit?.answerContent ?? "");
      };

      const gender_answer = find_answer((t) => t.includes("성별"));
      const intro_answer = find_answer((t) => t.includes("자기소개"));

      const attachment_answer =
        find_answer((t) => t.includes("첨부")) ||
        find_answer((t) => t.includes("포트폴리오")) ||
        find_answer((t) => t.includes("파일"));

      set_form({
        dept: department,
        studentId: student_id,
        name,
        phone: phone_number,
        gender:
          gender_answer === "남" || gender_answer === "남성"
            ? "male"
            : gender_answer === "여" || gender_answer === "여성"
              ? "female"
              : gender_answer
                ? "other"
                : "",
        intro: intro_answer,
        attachment: attachment_answer,
      });

      set_applicant_name(name ? `${name}님의 지원서` : "지원서");

      const server_status =
        applicant?.status ?? data?.status ?? data?.applicationStatus;
      setDecision(normalize_status(server_status));

      // ✅ 스웨거 예시: data.fileKeyUrl 로 내려오는 케이스 최우선
      const fk =
        data?.fileKeyUrl ??
        data?.file_key_url ??
        applicant?.fileKeyUrl ??
        applicant?.file_key_url ??
        applicant?.fileKey ??
        applicant?.file_key ??
        applicant?.filekey ??
        data?.fileKey ??
        data?.file_key ??
        data?.filekey ??
        attachment_answer ??
        "";

      set_file_key(String(fk || ""));
    } catch (e) {
      if (seq !== request_seq.current) return;
      set_error_msg(e?.message || "지원서 정보를 불러오지 못했습니다.");
    } finally {
      if (seq !== request_seq.current) return;
      set_loading(false);
    }
  };

  useEffect(() => {
    load_detail();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [club_id, club_member_id]);

  useEffect(() => {
    const run = async () => {
      set_file_url("");
      if (!file_key) return;

      // ✅ file_key가 이미 URL이면 그대로 사용
      if (is_http_url(file_key)) {
        set_file_url(String(file_key));
        return;
      }

      set_file_url_loading(true);
      try {
        // ✅ key라면 presigned GET 발급
        const raw = await owner_issue_application_download_url(
          club_id,
          club_member_id,
          file_key,
        );
        const data = unwrap_api(raw);

        const url =
          data?.preSignedUrl ??
          data?.presignedUrl ??
          data?.url ??
          data?.downloadUrl ??
          raw?.preSignedUrl ??
          raw?.presignedUrl ??
          raw?.url ??
          raw?.downloadUrl ??
          "";

        set_file_url(url ? String(url) : "");
      } catch {
        set_file_url("");
      } finally {
        set_file_url_loading(false);
      }
    };

    if (!club_id || !club_member_id) return;
    run();
  }, [club_id, club_member_id, file_key]);

  const on_change_decision = async (next) => {
    if (!club_id || !club_member_id) return;
    if (saving) return;

    const prev = decision;

    setDecision(next);
    set_saving(true);

    try {
      await owner_update_applicant_status(
        club_id,
        club_member_id,
        decision_to_api(next),
      );

      await load_detail();
    } catch (e) {
      setDecision(prev);
      alert(e?.message || "상태 변경에 실패했습니다.");
    } finally {
      set_saving(false);
    }
  };

  const on_download = () => {
    if (!file_url) return;
    window.open(file_url, "_blank", "noopener,noreferrer");
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
              <h1>{applicant_name}</h1>
            </div>
          </div>
        </div>
        <main className="page-main apply_main">
          <section className="apply_section">
            <h2 className="apply_title">온라인 지원</h2>
            <div className="apply_card">불러오는 중...</div>
          </section>
        </main>
      </div>
    );
  }

  if (error_msg) {
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
              <h1>{applicant_name}</h1>
            </div>
          </div>
        </div>
        <main className="page-main apply_main">
          <section className="apply_section">
            <h2 className="apply_title">온라인 지원</h2>
            <div className="apply_card">{error_msg}</div>
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
            <h1>{applicant_name}</h1>
          </div>
        </div>
      </div>

      <main className="page-main apply_main">
        <section className="apply_section">
          <h2 className="apply_title">온라인 지원</h2>

          <div className="apply_card">
            <p className="desc">
              합불 상태를 확인해주세요{" "}
              {saving ? (
                <span style={{ opacity: 0.7 }}>(저장중...)</span>
              ) : null}
            </p>

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

            <div className="file_row view_only">
              <span className="file_name">
                {file_key ? display_file_name(file_key) : "첨부 없음"}
                {file_url_loading ? (
                  <span style={{ marginLeft: 8, opacity: 0.7 }}>
                    (링크 생성중...)
                  </span>
                ) : null}
              </span>

              {file_key ? (
                <button
                  type="button"
                  className="outline_btn sm"
                  onClick={on_download}
                  disabled={!file_url}
                  style={!file_url ? { opacity: 0.5 } : undefined}
                >
                  열기/다운로드
                </button>
              ) : null}
            </div>

            <div
              className="decision_toggle"
              role="tablist"
              aria-label="지원 결과 선택"
            >
              <button
                type="button"
                role="tab"
                aria-selected={decision === "fail"}
                className={`seg seg-fail ${decision === "fail" ? "is_active" : ""}`}
                onClick={() => on_change_decision("fail")}
                disabled={saving}
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
                onClick={() => on_change_decision("pending")}
                disabled={saving}
              >
                미정
              </button>
              <button
                type="button"
                role="tab"
                aria-selected={decision === "pass"}
                className={`seg seg-pass ${decision === "pass" ? "is_active" : ""}`}
                onClick={() => on_change_decision("pass")}
                disabled={saving}
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
