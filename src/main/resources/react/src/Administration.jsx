import React, { useState, useEffect } from "react";
import ManageAccounts from "./components/ManageAccounts";
import TopBar from "./components/TopBar";
import DropdownMenu from "./components/DropdownMenu";
import SettingsMenu from "./components/SettingsMenu";

function Administration() {
  const [isDropdownVisible, setIsDropdownVisible] = useState(false);
  const [isSettingsVisible, setIsSettingsVisible] = useState(false);
  const [editedRecord, setEditedRecord] = useState(false);


  let guards ={}
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
        onMapClick={() =>  window.location.href = "/map"}
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

export default Administration;