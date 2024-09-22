import React, { useState, useEffect } from "react";
import ManageAccounts from "./components/ManageAccounts";
import { useNavigate } from "react-router-dom";
import TopBar from "./components/TopBar";
import DropdownMenu from "./components/DropdownMenu";
import SettingsMenu from "./components/SettingsMenu";
import "./administration.css";

export default function Administration({ onLogout, isLoggedIn, guards }) {
  const navigate = useNavigate();
  const [isDropdownVisible, setIsDropdownVisible] = useState(false);
  const [isSettingsVisible, setIsSettingsVisible] = useState(false);
  const [editedRecord, setEditedRecord] = useState(false);
  useEffect(() => {
    if (!isLoggedIn) {
      navigate("/login");
    }
  }, [isLoggedIn]);
  const toggleDropdown = () => {
    setIsDropdownVisible(!isDropdownVisible);
    if (isSettingsVisible) setIsSettingsVisible(false);
  };
  const toggleSettings = () => {
    setIsSettingsVisible(!isSettingsVisible);
  };
  const toggleEdited = () => {
    setEditedRecord(!editedRecord);
  };
  return (
    <div id="StatsBox">
      <TopBar onDropdownToggle={toggleDropdown} />
      <DropdownMenu
        isVisible={isDropdownVisible}
        onSettingsToggle={toggleSettings}
        onLogout={onLogout}
        onMapClick={() => navigate("/home")}
      />
      <SettingsMenu
        isVisible={isSettingsVisible}
        onSettingsToggle={toggleSettings}
        onEditedToggle={toggleEdited}
        canSetMapLoc={false}
      />
      <ManageAccounts guards={guards} editedRecord={editedRecord} />
    </div>
  );
}
