import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import AppFrame from "./app_frame";
import Home from "../pages/home/home";
import Login from "../pages/login/login";

export function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        {/* 로그인은 프레임 없이 */}
        <Route path="/login" element={<Login />} />

        {/* 공통 프레임 안에 들어가는 페이지들 */}
        <Route element={<AppFrame />}>
          <Route index element={<Navigate to="/home" replace />} />
          <Route path="/home" element={<Home />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}
