// src/pages/mypage/mypage.jsx
import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import "../../styles/globals.css";
import "./mypage.css";
import result from "../../components/result";
import { fetch_application_result } from "../../lib/api";

import {
  fetch_mypage_name,
  fetch_my_applications,
  fetch_owner_managed_clubs,
  is_logged_in,
  apiLogout,
  api_member_withdraw,
  owner_start_recruitment,
  owner_stop_recruitment,
} from "../../lib/api";

export default function MyPage() {
  const navigate = useNavigate();

  const [name, setName] = useState("");
  const [applications, setApplications] = useState([]);
  const [managed_clubs, set_managed_clubs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error_msg, set_error_msg] = useState("");
  const [result_open, set_result_open] = useState(false);
  const [result_loading, set_result_loading] = useState(false);
  const [result_error, set_result_error] = useState("");
  const [result_data, set_result_data] = useState(null);
  const [recruiting_map, set_recruiting_map] = useState({}); // { [clubId]: true/false }
  const [recruiting_loading_map, set_recruiting_loading_map] = useState({}); // { [clubId]: true/false }

  const toggle_recruiting = async (club) => {
    const id = get_id(club);
    if (!id) return;

    const is_open = !!recruiting_map[id];

    set_recruiting_loading_map((prev) => ({ ...prev, [id]: true }));

    try {
      if (is_open) {
        await owner_stop_recruitment(id);
        set_recruiting_map((prev) => ({ ...prev, [id]: false }));
      } else {
        await owner_start_recruitment(id);
        set_recruiting_map((prev) => ({ ...prev, [id]: true }));
      }
    } catch (e) {
      alert(e?.message || "모집 상태 변경에 실패했습니다.");
    } finally {
      set_recruiting_loading_map((prev) => ({ ...prev, [id]: false }));
    }
  };

  // ✅ id/name 강력 추출 (중첩 대응) + string 통일
  const get_id = (obj) => {
    const v =
      obj?.clubId ??
      obj?.club_id ??
      obj?.id ??
      obj?.club?.clubId ??
      obj?.club?.id ??
      obj?.club?.club_id ??
      obj?.application?.clubId ??
      obj?.application?.club_id ??
      obj?.clubInfo?.clubId ??
      obj?.clubInfo?.id;

    return v === undefined || v === null ? null : String(v);
  };

  const get_name = (obj) =>
    obj?.clubName ??
    obj?.name ??
    obj?.club?.clubName ??
    obj?.club?.name ??
    obj?.clubInfo?.clubName ??
    obj?.clubInfo?.name ??
    "동아리";

  // ✅ “지원서 항목”만 남기기 위한 휴리스틱
  // (applications가 클럽 리스트로 내려오면 여기서 대부분 걸러짐)
  const is_application_item = (a) => {
    // 지원 목록이면 보통 이런 필드가 하나라도 있음(프로젝트마다 다름)
    if (a?.applicationId != null) return true;
    if (a?.applyId != null) return true;
    if (a?.memberId != null) return true;
    if (a?.status != null) return true;
    if (a?.applicationStatus != null) return true;
    if (a?.applyStatus != null) return true;
    if (a?.appliedAt != null) return true;

    // 반대로 “클럽”처럼 생긴 데이터는 걸러냄(운영관리 쪽 형태)
    if (a?.president != null) return false;
    if (a?.contact != null) return false;
    if (a?.recruitingEnd != null) return false;
    if (a?.clubRoom != null) return false;
    if (a?.description != null) return false;

    // 모르겠으면 일단 통과(너무 과하게 비우지 않게)
    return true;
  };

  useEffect(() => {
    if (!is_logged_in()) {
      navigate("/login");
      return;
    }

    const load = async () => {
      try {
        const nameData = await fetch_mypage_name();
        setName(nameData?.name || "");

        const appsData = await fetch_my_applications();
        const apps = Array.isArray(appsData) ? appsData : [];
        setApplications(apps);

        try {
          const ownerData = await fetch_owner_managed_clubs();
          const owners = Array.isArray(ownerData) ? ownerData : [];
          set_managed_clubs(owners);
        } catch (_) {
          set_managed_clubs([]);
        }
      } catch (err) {
        set_error_msg(err?.message || "마이페이지 정보를 불러오지 못했습니다.");
      } finally {
        setLoading(false);
      }
    };

    load();
  }, [navigate]);
  const open_result_modal = async (club_id) => {
    set_result_open(true);
    set_result_loading(true);
    set_result_error("");
    set_result_data(null);

    try {
      const data = await fetch_application_result(club_id);
      set_result_data(data);
    } catch (e) {
      set_result_error(e?.message || "결과를 불러오지 못했습니다.");
    } finally {
      set_result_loading(false);
    }
  };

  const managed_ids = useMemo(() => {
    return new Set((managed_clubs || []).map(get_id).filter((v) => v != null));
  }, [managed_clubs]);

  const pure_applications = useMemo(() => {
    return (applications || [])
      .filter((a) => get_id(a) != null)
      .filter((a) => is_application_item(a))
      .filter((a) => !managed_ids.has(get_id(a))); // ✅ 오너 동아리는 지원목록에서 제거
  }, [applications, managed_ids]);

  const handleLogout = async () => {
    try {
      await apiLogout();
      navigate("/");
    } catch (err) {
      set_error_msg(err?.message || "로그아웃에 실패했습니다.");
    }
  };

  const handleWithdraw = async () => {
    const ok = window.confirm("정말 탈퇴하시겠습니까? 되돌릴 수 없습니다.");
    if (!ok) return;

    try {
      await api_member_withdraw();
      alert("탈퇴가 완료되었습니다.");
      navigate("/");
    } catch (err) {
      set_error_msg(err?.message || "탈퇴에 실패했습니다.");
    }
  };

  if (loading) {
    return (
      <div className="page-root">
        <div className="page-main mypage_main">
          <p>불러오는 중...</p>
        </div>
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
            <h1>마이페이지</h1>
            <span className="header-name">{name}님</span>
          </div>
        </div>
      </div>

      <main className="page-main mypage_main">
        {error_msg && <p className="mypage_error">{error_msg}</p>}

        {/* 지원 목록 */}
        <section className="mypage_section">
          <h2 className="mypage_title">지원 목록</h2>

          {pure_applications.length === 0 ? (
            <div className="mypage_card">
              <p className="empty">아직 지원한 동아리가 없습니다.</p>
            </div>
          ) : (
            <div className="mypage_card">
              {pure_applications.map((app) => {
                const id = get_id(app);
                return (
                  <div className="club_box" key={`app-${id}`}>
                    <p className="club_title">{get_name(app)}</p>
                    <div className="club_buttons">
                      <button onClick={() => navigate(`/club/${id}`)}>
                        동아리 페이지
                      </button>
                      <button
                        onClick={() => navigate(`/apply_form_edit/${id}`)}
                      >
                        지원서 편집
                      </button>
                      <button onClick={() => open_result_modal(get_id(app))}>
                        결과 확인
                      </button>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </section>
        <result
          open={result_open}
          loading={result_loading}
          error={result_error}
          result={result_data}
          onClose={() => set_result_open(false)}
        />

        {/* 동아리 운영/관리 */}
        <section className="mypage_section">
          <h2 className="mypage_title">동아리 운영/관리</h2>

          <div className="mypage_card">
            {managed_clubs.length === 0 ? (
              <p className="empty">운영 중인 동아리가 없습니다.</p>
            ) : (
              managed_clubs
                .filter((c) => get_id(c) != null)
                .map((club) => {
                  const id = get_id(club);
                  return (
                    <div className="club_box" key={`owner-${id}`}>
                      <p className="club_title">{get_name(club)}</p>
                      <div className="club_buttons">
                        <button onClick={() => navigate(`/club/${id}`)}>
                          동아리 페이지
                        </button>
                        <button onClick={() => navigate(`/club_manage/${id}`)}>
                          동아리 관리
                        </button>
                        <button
                          onClick={() => navigate(`/apply_form_edit/${id}`)}
                        >
                          지원양식 편집
                        </button>
                        <button
                          onClick={() => navigate(`/applicant_manage/${id}`)}
                        >
                          지원자 관리
                        </button>
                        <button
                          className={`recruit_btn ${
                            recruiting_map[id] ? "stop" : "start"
                          }`}
                          disabled={!!recruiting_loading_map[id]}
                          onClick={() => toggle_recruiting(club)}
                        >
                          {recruiting_loading_map[id]
                            ? "처리중..."
                            : recruiting_map[id]
                            ? "모집중지"
                            : "모집시작"}
                        </button>
                      </div>
                    </div>
                  );
                })
            )}

            <button className="add_btn" onClick={() => navigate("/club_edit")}>
              동아리 등록하기
            </button>
          </div>
        </section>

        <div className="mypage_footer">
          <button
            type="button"
            className="link_btn"
            onClick={() => navigate("/account_edit")}
          >
            회원정보수정
          </button>
          <button type="button" className="link_btn" onClick={handleLogout}>
            로그아웃
          </button>
          <button
            type="button"
            className="link_btn logout_red"
            onClick={handleWithdraw}
          >
            탈퇴
          </button>
        </div>
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
