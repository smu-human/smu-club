// src/pages/club_edit/club_edit.jsx
import { useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import "../../styles/globals.css";
import "./club_edit.css";
import { Editor } from "@toast-ui/react-editor";
import "@toast-ui/editor/dist/toastui-editor.css";
import { owner_upload_images, owner_register_club } from "../../lib/api";

export default function ClubEdit() {
  const navigate = useNavigate();
  const editorRef = useRef(null);

  const [clubName, setClubName] = useState("");
  const [clubOneLine, setClubOneLine] = useState("");
  const [leaderName, setLeaderName] = useState("");
  const [phone, setPhone] = useState("");

  // ✅ 모집 시작/마감일
  const [start_date, set_start_date] = useState("");
  const [deadline, setDeadline] = useState("");

  const [images, setImages] = useState([]);
  const [is_saving, set_is_saving] = useState(false);

  const onPickImages = (e) => {
    const files = Array.from(e.target.files || []);
    setImages(files);
  };

  const onSave = async () => {
    if (is_saving) return;
    set_is_saving(true);

    try {
      // (선택) 간단 검증
      if (start_date && deadline && start_date > deadline) {
        alert("모집 시작일은 모집 마감일보다 늦을 수 없습니다.");
        return;
      }

      const introHtml = editorRef.current?.getInstance().getHTML() || "";
      const uploadedImageFileNames = await owner_upload_images(images);

      await owner_register_club({
        uploadedImageFileNames,
        name: clubName,
        title: clubOneLine,
        president: leaderName,
        contact: phone,

        // ✅ 추가: 모집 시작일
        recruitingStart: start_date || null,

        // 기존: 모집 마감일
        recruitingEnd: deadline || null,

        clubRoom: "",
        description: introHtml,
      });

      alert("저장 완료");
      navigate("/mypage");
    } catch (err) {
      alert(err?.message || "저장 실패");
    } finally {
      set_is_saving(false);
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
            <h1>동아리 등록</h1>
          </div>
        </div>
      </div>

      <main className="page-main club_edit_main">
        <section className="club_section">
          <h2 className="club_title">갤러리 이미지</h2>
          <div className="club_card">
            <p className="sub_text">
              동아리 페이지 상단 갤러리 이미지를 등록/삭제하세요. (JPG/PNG, 최대
              5장)
            </p>
            <label className="outline_btn" htmlFor="clubGallery">
              이미지 추가
            </label>
            <input
              id="clubGallery"
              type="file"
              accept="image/png, image/jpeg"
              multiple
              onChange={onPickImages}
              style={{ display: "none" }}
            />
            {images.length > 0 && (
              <p className="hint_text">선택: {images.length}개</p>
            )}
          </div>
        </section>

        <section className="club_section">
          <h2 className="club_title">기본 정보</h2>
          <div className="club_card">
            <label className="field_label" htmlFor="clubName">
              동아리명
            </label>
            <input
              id="clubName"
              className="field_input"
              type="text"
              placeholder="예) 스뮤클럽"
              value={clubName}
              onChange={(e) => setClubName(e.target.value)}
            />

            <label className="field_label" htmlFor="clubOneLine">
              동아리 한줄 소개
            </label>
            <input
              id="clubOneLine"
              className="field_input"
              type="text"
              placeholder="예) 상명대 대표 동아리 플랫폼 동아리"
              value={clubOneLine}
              onChange={(e) => setClubOneLine(e.target.value)}
            />

            <label className="field_label" htmlFor="leaderName">
              회장
            </label>
            <input
              id="leaderName"
              className="field_input"
              type="text"
              placeholder="예) 김스뮤"
              value={leaderName}
              onChange={(e) => setLeaderName(e.target.value)}
            />

            <label className="field_label" htmlFor="phone">
              연락처
            </label>
            <input
              id="phone"
              className="field_input"
              type="tel"
              placeholder="010-1234-5678"
              value={phone}
              onChange={(e) => setPhone(e.target.value)}
            />

            {/* ✅ 추가: 모집 시작일 */}
            <label className="field_label" htmlFor="start_date">
              모집 시작일
            </label>
            <input
              id="start_date"
              className="field_input"
              type="date"
              value={start_date}
              onChange={(e) => set_start_date(e.target.value)}
            />

            <label className="field_label" htmlFor="deadline">
              모집 마감일
            </label>
            <input
              id="deadline"
              className="field_input"
              type="date"
              value={deadline}
              onChange={(e) => setDeadline(e.target.value)}
            />
          </div>
        </section>

        <section className="club_section">
          <h2 className="club_title">동아리 소개</h2>
          <div className="club_card">
            <Editor
              ref={editorRef}
              height="320px"
              initialEditType="wysiwyg"
              previewStyle="vertical"
              usageStatistics={false}
              placeholder="동아리 소개와 활동 내용을 자유롭게 작성하세요."
            />
          </div>
        </section>

        <button
          className="primary_btn club_save_btn"
          onClick={onSave}
          disabled={is_saving}
        >
          {is_saving ? "저장 중..." : "저장하기"}
        </button>
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
