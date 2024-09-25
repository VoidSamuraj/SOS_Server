import React, { useState, useEffect } from "react";
import leftarrow from "../icons/left-arrow.svg";
import { usePatrols } from "./map/MapFunctions";

function PatrolsMenu({ isVisible, onPatrolsToggle, patrols }) {
  const [sortByStatus, setSortByStatus] = useState(true);
  const [sortedPatrols, setSortedPatrols] = useState([]);

  const { statusToCode } = usePatrols();

  useEffect(() => {
    if (patrols) {
      const sorted = Array.from(patrols.entries()).sort(
        ([idA, { status: statusA }], [idB, { status: statusB }]) => {
          if (sortByStatus) {
            return statusA - statusB;
          } else {
            return idA - idB;
          }
        }
      );
      setSortedPatrols(sorted);
    }
  }, [sortByStatus, patrols]);

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
        {sortedPatrols
          ? sortedPatrols.map(
              ([id, { position, status, name, surname, phone }]) => (
                <div
                  style={{ backgroundColor: statusToCode(status) }}
                  onClick={(event) =>
                    event.currentTarget.classList.toggle("expandedMenu")
                  }
                >
                  <div key={id} className="patrol-item">
                    {id}
                  </div>
                  <p>{name + " " + surname}</p>
                  <p>{phone}</p>
                </div>
              )
            )
          : ""}
      </div>
    </div>
  );
}

export default PatrolsMenu;
