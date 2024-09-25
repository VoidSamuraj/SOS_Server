import React from "react";
import barchart from "../icons/bar-chart.svg";
import cog from "../icons/cog.svg";
import exit from "../icons/exit.svg";
import group from "../icons/group.svg";
import mapIcon from "../icons/map.svg";
import { logout } from "../script/ApiService.js";

function DropdownMenu({
  isVisible,
  onSettingsToggle,
  onStatsToggle,
  onAdminClick,
  onMapClick,
}) {

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
            window.location.href = "/login";
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
