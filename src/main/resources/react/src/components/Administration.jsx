import React, { useState, useEffect } from "react";
import ManageAccounts from "./ManageAccounts";
import TopBar from "./TopBar";
import DropdownMenu from "./DropdownMenu";
import SettingsMenu from "./SettingsMenu";
import { refreshToken, getEmployee } from "../script/ApiService.js";

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

  const [loggedInEmployee, setLoggedInEmployee] = useState(null);

  const loadEmployeeInfo = async (id) => {
    let employee = await getEmployee(id);
    setLoggedInEmployee(employee);
  };

  useEffect(() => {
    const checkTokenExpiration = () => {
      const currentTimestamp = Date.now();
      const exp = localStorage.getItem("tokenExp");

      if (exp) {
        const expTimestamp = parseInt(exp, 10);
        const timeDifference = expTimestamp - currentTimestamp;

        if (timeDifference <= 0) {
          window.location.reload();
        } else if (timeDifference < 300000) {
          // Less than 5 minutes to expire
          refreshToken();
        }
      } else {
        console.error("Expiration date not found in localStorage");
      }
    };

    checkTokenExpiration();

    const intervalId = setInterval(() => {
      checkTokenExpiration();
    }, 30000); // 30s

    let id = localStorage.getItem("userData");
    if (id) loadEmployeeInfo(id);

    return () => {
      clearInterval(intervalId);
    };
  }, []);

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
          onMapClick={(loggedInEmployee!=null && loggedInEmployee.roleCode == 2)? () => (window.location.href = "/map"):null}
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
