import React from "react";
import barchart from "../icons/bar-chart.svg";
import cog from "../icons/cog.svg";
import exit from "../icons/exit.svg";
import group from "../icons/group.svg";
import mapIcon from "../icons/map.svg";
import { logout } from "../script/ApiService.js";


/**
 * DropdownMenu component displays a menu for user actions such as settings,
 * statistics, administration, and logout.
 *
 * This component conditionally renders buttons based on the visibility
 * and the presence of callback functions for specific actions.
 * Each button is associated with an action like navigating to settings,
 * displaying statistics, or logging out.
 *
 * @param {boolean} props.isVisible - Indicates if the dropdown menu is visible.
 * @param {function} props.onSettingsToggle - Function to call when settings button is clicked.
 * @param {function} props.onStatsToggle - Function to call when statistics button is clicked.
 * @param {function} [props.onAdminClick] - Optional function to call when admin button is clicked.
 * @param {function} [props.onMapClick] - Optional function to call when map button is clicked.
 *
 * @returns {JSX.Element} The rendered dropdown menu component.
 */
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
          logout()
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
