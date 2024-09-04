import React from "react";
import "../style/dropdownMenu.css"; // Załaduj style dla tego komponentu
import barchart from "../icons/bar-chart.svg";
import cog from "../icons/cog.svg";
import exit from "../icons/exit.svg";

function DropdownMenu({ isVisible, onSettingsToggle, onStatsToggle }) {
  return (
    <div
      id="dropdownMenu"
      className={`${isVisible ? "dropdownMenuVisible" : "dropdownMenuHidden"}`}
    >
      <button onClick={onStatsToggle} className="icon-button">
        <img src={barchart} alt="stats" />
        Statystyki
      </button>
      <button onClick={onSettingsToggle} className="icon-button">
        <img src={cog} alt="settings" />
        Opcje
      </button>
      <button className="icon-button">
        <img src={exit} alt="logout" />
        Wyloguj się
      </button>
    </div>
  );
}

export default DropdownMenu;
