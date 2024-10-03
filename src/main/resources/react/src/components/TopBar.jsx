import React, { useEffect, useState } from "react";
import menu from "../icons/menu.svg";
import { getEmployee } from "../script/ApiService.js";
import userIcon from "../icons/user.svg";

/**
 * TopBar component displays the status of guards and includes a dropdown toggle.
 *
 * @param {Function} props.onDropdownToggle - Function to be called when the dropdown is toggled.
 * @param {Map} props.guards - A Map of guards, where each guard has a status.
 *
 * @returns {JSX.Element} The rendered component.
 */
function TopBar({ onDropdownToggle, guards }) {
  const [loggedInEmployee, setLoggedInEmployee] = useState(null);

  const loadEmployeeInfo = async (id) => {
    let employee = await getEmployee(id);
    setLoggedInEmployee(employee);
  };

  useEffect(() => {
    let id = localStorage.getItem("userData");
    if (id) loadEmployeeInfo(id);
  }, []);

  return (
    <div id="topBar">
      <div>
        {guards ? (
          <div id="infoBox">
            <div>Patrole</div>
            <div>
              Dostępne:
              {
                Array.from(guards.values()).filter(
                  (guard) => guard.status === 0
                ).length
              }
              &nbsp; Interwencja:
              {
                Array.from(guards.values()).filter(
                  (guard) => guard.status === 2
                ).length
              }
              &nbsp; Niedostępne:
              {
                Array.from(guards.values()).filter(
                  (guard) => guard.status === 1
                ).length
              }
            </div>
          </div>
        ) : (
          ""
        )}
        <div id="userBarMenu">
          {loggedInEmployee ? (
            <>
              <img src={userIcon} alt="User" />
              <p>
                {loggedInEmployee.name} {loggedInEmployee.surname}
              </p>
            </>
          ) : (
            ""
          )}
          <img onClick={onDropdownToggle} src={menu} alt="menu" />
        </div>
      </div>
    </div>
  );
}

export default TopBar;
