import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import "../../styles/globals.css";
import "./student_auth.css";
import { apiJson } from "../../lib/api";

export default function StudentAuth() {
  const navigate = useNavigate();
  const [studentId, setStudentId] = useState("");
  const [password, setPassword] = useState("");
  const [agree, setAgree] = useState(false);
  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState("");

  const onSubmit = async (e) => {
    e.preventDefault();
    if (!agree) {
      return setErrorMsg("개인정보 조회 동의가 필요합니다.");
    }
    setErrorMsg("");
    setLoading(true);

    try {
      const data = await apiJson("/api/v1/public/auth/signup", {
        method: "POST",
        body: JSON.stringify({
          studentId,
          password,
        }),
      });

      // 인증 완료 후 로그인 페이지로 이동
      navigate("/login", { replace: true });
    } catch (err) {
      if (err.code === "UNAUTHORIZED") {
        setErrorMsg("인증이 필요합니다.");
      } else {
        setErrorMsg(err.message || "학생 인증에 실패했습니다.");
      }
    } finally {
      setLoading(false);
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
            <h1>학생 인증</h1>
          </div>
        </div>
      </div>

      {/* Main */}
      <main className="page-main">
        <div className="auth_page">
          {/* Hero Section */}
          <div className="hero-section">
            <div className="hero-content">
              <img
                src="/images/2.png"
                alt="스뮤 클럽 로고"
                className="hero-logo"
              />
              <p style={{ marginTop: "8px" }}>상명대학교 동아리 통합 플랫폼</p>
            </div>
          </div>

          {/* Auth Form */}
          <div className="form-container">
            <div className="card">
              <div className="card-header">
                <h3>학생 인증</h3>
                <p className="auth-subtitle">
                  샘물 통합로그인 학번/비밀번호로 본인 인증
                </p>
                <p
                  aria-live="polite"
                  style={{ minHeight: 20, color: "#a82d2f" }}
                >
                  {errorMsg || ""}
                </p>
              </div>

              <form className="auth-form" onSubmit={onSubmit}>
                <div className="form-group">
                  <label htmlFor="authStudentId">학번</label>
                  <div className="input-container">
                    <svg
                      className="input-icon"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                    >
                      <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                      <circle cx="12" cy="7" r="4" />
                    </svg>
                    <input
                      type="text"
                      id="authStudentId"
                      placeholder="학번을 입력하세요"
                      required
                      value={studentId}
                      onChange={(e) => setStudentId(e.target.value)}
                    />
                  </div>
                </div>

                <div className="form-group">
                  <label htmlFor="authPassword">비밀번호</label>
                  <div className="input-container">
                    <svg
                      className="input-icon"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                    >
                      <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
                      <circle cx="12" cy="16" r="1" />
                      <path d="M7 11V7a5 5 0 0 1 10 0v4" />
                    </svg>
                    <input
                      type="password"
                      id="authPassword"
                      placeholder="비밀번호를 입력하세요"
                      required
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                    />
                  </div>
                </div>

                <div className="form-group">
                  <label className="checkbox-label">
                    <input
                      type="checkbox"
                      checked={agree}
                      onChange={(e) => setAgree(e.target.checked)}
                      required
                    />
                    <span className="checkmark"></span>
                    상명대학교 샘물포털시스템을 통해{" "}
                    <strong>
                      본인 확인 및 학생 정보(학번, 이름, 학과, 재학상태) 조회
                    </strong>
                    에 동의합니다.
                  </label>
                </div>

                <button
                  type="submit"
                  className="btn btn-primary btn-large"
                  id="authBtn"
                  disabled={loading}
                >
                  <span id="authText">
                    {loading ? "인증 중..." : "인증하기"}
                  </span>
                  <div
                    className="spinner"
                    aria-hidden="true"
                    style={{
                      display: loading ? "inline-block" : "none",
                    }}
                  />
                </button>
              </form>

              <div className="form-footer">
                <p>인증 완료 후 자동으로 회원가입이 처리됩니다.</p>
              </div>
            </div>
          </div>

          <p style={{ textAlign: "center", marginTop: "12px" }}>
            <Link to="/login">로그인 화면으로</Link>
          </p>
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
