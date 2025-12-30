// src/pages/mypage/mypage.jsx

import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import "../../styles/globals.css";
import "./mypage.css";

import {
  fetch_mypage_name,
  fetch_my_applications,
  is_logged_in,
  apiLogout,
  api_member_withdraw,
} from "../../lib/api";

export default function MyPage() {
  const navigate = useNavigate();

  const [name, setName] = useState("");
  const [applications, setApplications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error_msg, set_error_msg] = useState("");

  useEffect(() => {
    if (!is_logged_in()) {
      navigate("/login");
      return;
    }

    const load = async () => {
      try {
        const nameData = await fetch_mypage_name();
        const appsData = await fetch_my_applications();

        setName(nameData?.name || "");
        setApplications(appsData || []);
      } catch (err) {
        set_error_msg(err.message || "마이페이지 정보를 불러오지 못했습니다.");
      } finally {
        setLoading(false);
      }
    };

    load();
  }, [navigate]);

  const handleLogout = async () => {
    try {
      await apiLogout();
      navigate("/");
    } catch (err) {
      set_error_msg(err.message || "로그아웃에 실패했습니다.");
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
      set_error_msg(err.message || "탈퇴에 실패했습니다.");
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
            <h1>마이페이지</h1>
            <span className="header-name">{name}님</span>
          </div>
        </div>
      </div>

      {/* Main */}
      <main className="page-main mypage_main">
        {error_msg && <p className="mypage_error">{error_msg}</p>}

        {/* 지원 목록 */}
        <section className="mypage_section">
          <h2 className="mypage_title">지원 목록</h2>

          {applications.length === 0 ? (
            <div className="mypage_card">
              <p className="empty">아직 지원한 동아리가 없습니다.</p>
            </div>
          ) : (
            <div className="mypage_card">
              {applications.map((app) => (
                <div className="club_box" key={app.clubId}>
                  <p className="club_title">{app.clubName}</p>
                  <div className="club_buttons">
                    <button onClick={() => navigate(`/club/${app.clubId}`)}>
                      동아리 페이지
                    </button>
                    <button
                      onClick={() => navigate(`/apply_form_edit/${app.clubId}`)}
                    >
                      지원서 편집
                    </button>
                    <button
                      onClick={() =>
                        navigate(`/apply_form_result/${app.clubId}`)
                      }
                    >
                      결과 확인
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </section>

        {/* 동아리 운영/관리 (추후 오너 기능 연동용) */}
        <section className="mypage_section">
          <h2 className="mypage_title">동아리 운영/관리</h2>
          <div className="mypage_card">
            <p className="empty">운영 중인 동아리가 없습니다.</p>
            {/* 오너 전용 데이터 생기면 여기서 map 렌더링 */}
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

      {/* Footer */}
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
