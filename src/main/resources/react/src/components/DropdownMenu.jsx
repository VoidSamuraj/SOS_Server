import React from "react";
import "../style/dropdownMenu.css"; // Załaduj style dla tego komponentu
import barchart from "../icons/bar-chart.svg";
import cog from "../icons/cog.svg";
import exit from "../icons/exit.svg";
import group from "../icons/group.svg";
import mapIcon from "../icons/map.svg";
import { logout } from "../script/ApiService.js";
import { useNavigate } from "react-router-dom";

function DropdownMenu({
  isVisible,
  onSettingsToggle,
  onStatsToggle,
  onLogout,
  onAdminClick,
  onMapClick,
}) {
  const navigate = useNavigate();

  return (
    <div
      id="dropdownMenu"
      className={`${isVisible ? "dropdownMenuVisible" : "dropdownMenuHidden"}`}
    >
      {onAdminClick ? (
        <button onClick={onAdminClick} className="icon-button">
          <img src={group} alt="administration" />
          Administracja
        </button>
      ) : (
        ""
      )}
      {onMapClick ? (
        <button onClick={onMapClick} className="icon-button">
          <img src={mapIcon} alt="mapa" />
          Dyspozytornia
        </button>
      ) : (
        ""
      )}
      <button onClick={onSettingsToggle} className="icon-button">
        <img src={cog} alt="settings" />
        Opcje
      </button>
      {onStatsToggle ? (
        <button onClick={onStatsToggle} className="icon-button">
          <img src={barchart} alt="stats" />
          Statystyki
        </button>
      ) : (
        ""
      )}
      <button
        onClick={() =>
          logout(() => {
            navigate("/login");
            onLogout();
          })
        }
        className="icon-button"
      >
        <img src={exit} alt="logout" />
        Wyloguj się
      </button>
    </div>
  );
}

export default DropdownMenu;
