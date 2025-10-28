import { createBrowserRouter } from "react-router-dom";

import HomePage from "../pages/home/home.jsx"; // index.jsx면 폴더까지만
import LoginPage from "../pages/login/login.jsx";
import SignupPage from "../pages/student_auth/student_auth.jsx";
import ClubPage from "../pages/club/club.jsx"; // 파일명이 club.jsx라면 이렇게

export const router = createBrowserRouter([
  { path: "/", element: <HomePage /> },
  { path: "/login", element: <LoginPage /> },
  { path: "/signup", element: <SignupPage /> },
  { path: "/club/:id", element: <ClubPage /> },
  // 404 처리 원하면 추가 가능
  // { path: "*", element: <NotFoundPage /> },
]);
