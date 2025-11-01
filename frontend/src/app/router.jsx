// src/app/router.jsx
import { createBrowserRouter } from "react-router-dom";

import HomePage from "../pages/home/home.jsx";
import LoginPage from "../pages/login/login.jsx";
import SignupPage from "../pages/student_auth/student_auth.jsx";
import ClubPage from "../pages/club/club.jsx";
import MyPage from "../pages/mypage/mypage.jsx";
import AccountEdit from "../pages/account_edit/account_edit.jsx";
import ClubEdit from "../pages/club_edit/club_edit.jsx";
import ApplicantManage from "../pages/applicant_manage/applicant_manage.jsx";
import ApplyFormEdit from "../pages/apply_form_edit/apply_form_edit.jsx";
import ApplyForm from "../pages/apply_form/apply_form.jsx";
import ApplyFormSubmit from "../pages/apply_form_submit/apply_form_submit.jsx"; // ✅ 추가
import ApplyFormChange from "../pages/apply_form_change/apply_form_change.jsx"; // ✅ 추가

export const router = createBrowserRouter([
  { path: "/", element: <HomePage /> },
  { path: "/login", element: <LoginPage /> },
  { path: "/signup", element: <SignupPage /> },
  { path: "/club/:id", element: <ClubPage /> },
  { path: "/mypage", element: <MyPage /> },
  { path: "/account_edit", element: <AccountEdit /> },
  { path: "/club_edit", element: <ClubEdit /> },
  { path: "/applicant_manage", element: <ApplicantManage /> },
  { path: "/apply_form_edit", element: <ApplyFormEdit /> },
  { path: "/apply_form", element: <ApplyForm /> },
  { path: "/apply_form_submit", element: <ApplyFormSubmit /> }, // ✅ 신규 추가
  { path: "/apply_form_change", element: <ApplyFormChange /> }, // ✅ 신규 추가

  // { path: "*", element: <NotFoundPage /> },
]);
