import React, { useState } from "react";
import "../style/patrolsMenu.css"; // Załaduj style dla tego komponentu
import leftarrow from "../icons/left-arrow.svg";
import { usePatrols } from "./map/MapFunctions";

function PatrolsMenu({ isVisible, onPatrolsToggle, patrols }) {
  const [sortByStatus, setSortByStatus] = useState(true);

  const { statusToCode } = usePatrols();
  const sortedPatrols = Array.from(patrols.entries()).sort(
    ([idA, { status: statusA }], [idB, { status: statusB }]) => {
      if (sortByStatus) {
        return statusA-statusB;
      } else {
        return idA - idB;
      }
      return 0;
    }
  );


  return (
    <div
      id="patrolsMenu"
      className={`${isVisible ? "patrolsMenuVisible" : "patrolsMenuHidden"}`}
    >
      <img
        onClick={onPatrolsToggle}
        id="patrolsClose"
        src={leftarrow}
        alt="close"
      />
      <div>
        <span>Sortuj</span>
        <button
          onClick={() => setSortByStatus(false)}
          className={`${!sortByStatus ? "checked" : ""}`}
        >
          ID
        </button>
        <button
          onClick={() => setSortByStatus(true)}
          className={`${sortByStatus ? "checked" : ""}`}
        >
          Status
        </button>
      </div>
      <div id="patrolsList">
        {sortedPatrols.map(([id, { position, status, name, surname, phone }]) => (
          <div style={{ backgroundColor: statusToCode(status) }} onClick={(event) => event.currentTarget.classList.toggle("expandedMenu")}>
            <div
              key={id}
              className="patrol-item"
            >
              {id}
            </div>
            <p>{name+" "+surname}</p>
            <p>{phone}</p>
          </div>
        ))}
      </div>
    </div>
  );
}

export default PatrolsMenu;
