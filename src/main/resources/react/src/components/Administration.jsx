import React, { useState, useEffect } from "react";
import ManageAccounts from "./ManageAccounts";
import TopBar from "./TopBar";
import DropdownMenu from "./DropdownMenu";
import SettingsMenu from "./SettingsMenu";


/**
 * Administration component serves as a control panel for managing settings,
 * dropdown menus, and accounts within the application.
 *
 * This component includes a top bar with a dropdown menu, settings menu,
 * and an account management section. It manages the visibility of the dropdown
 * and settings menus and allows for the editing of account records.
 *
 * @returns {JSX.Element} The rendered administration component.
 */
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
      <ManageAccounts editedRecord={editedRecord} />
    </div>
  );
}

export default Administration;