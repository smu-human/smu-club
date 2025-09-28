import { Outlet } from "react-router-dom";
import TopBar from "./top_bar";
import BottomNav from "./bottom_nav";

export default function AppFrame() {
  return (
    <div className="app_frame">
      <TopBar />
      <main className="content">
        <Outlet />
      </main>
      <BottomNav />
    </div>
  );
}
