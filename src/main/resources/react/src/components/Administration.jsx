import React, { useState } from "react";
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

  const [isLoading, setIsLoading] = useState(false);

  let guards = {};
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
    <>
      <div
        id="StatsBox"
        style={{
          pointerEvents: isLoading ? "none" : "auto",
          filter: isLoading ? "grayscale(80%) brightness(80%)" : "none",
        }}
      >
        <TopBar onDropdownToggle={toggleDropdown} />
        <DropdownMenu
          isVisible={isDropdownVisible}
          onSettingsToggle={toggleSettings}
          onMapClick={() => (window.location.href = "/map")}
        />
        <SettingsMenu
          isVisible={isSettingsVisible}
          isTooltipVisible={false}
          onSettingsToggle={toggleSettings}
          onEditedToggle={toggleEdited}
          canSetMapLoc={false}
        />
        <ManageAccounts
          editedRecord={editedRecord}
          setIsLoading={setIsLoading}
        />
      </div>
      <div class={isLoading ? "loader" : "hiddenLoader"}>
        <div></div>
      </div>
    </>
  );
}

export default Administration;
