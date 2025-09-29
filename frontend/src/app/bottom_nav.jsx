import { NavLink } from "react-router-dom";

export default function BottomNav() {
  const cls = ({ isActive }) => "bottom_nav_item" + (isActive ? " active" : "");
  return (
    <nav className="bottom_nav">
      <NavLink to="/home" className={cls}>
        홈
      </NavLink>
      <NavLink to="/login" className={cls}>
        로그인
      </NavLink>
    </nav>
  );
}
