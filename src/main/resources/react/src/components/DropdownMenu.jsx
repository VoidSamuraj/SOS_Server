import React from "react";
import "../style/dropdownMenu.css"; // Załaduj style dla tego komponentu
import barchart from "../icons/bar-chart.svg";
import cog from "../icons/cog.svg";
import exit from "../icons/exit.svg";
import group from "../icons/group.svg";
import {logout} from "../script/ApiService.js"
import { useNavigate } from "react-router-dom";

function DropdownMenu({ isVisible, onSettingsToggle, onStatsToggle,onLogout, onAdminClick}) {
  const navigate = useNavigate();

  return (
    <div
      id="dropdownMenu"
      className={`${isVisible ? "dropdownMenuVisible" : "dropdownMenuHidden"}`}
    >
      <button onClick={onStatsToggle} className="icon-button">
        <img src={barchart} alt="stats" />
        Statystyki
      </button>
      <button onClick={onAdminClick} className="icon-button">
        <img src={group} alt="accounts" />
        Administracja
      </button>
      <button onClick={onSettingsToggle} className="icon-button">
        <img src={cog} alt="settings" />
        Opcje
      </button>
      <button onClick={()=>logout(()=>{navigate("/login");onLogout();})} className="icon-button">
        <img src={exit} alt="logout" />
        Wyloguj się
      </button>
    </div>
  );
}

export default DropdownMenu;