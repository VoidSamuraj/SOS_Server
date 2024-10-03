import React, { useState, useEffect } from "react";
import leftarrow from "../icons/left-arrow.svg";
import { usePatrols } from "./map/MapFunctions";
import car from "../icons/car.svg";

/**
 * PatrolsMenu component displays a list of patrols with sorting options.
 *
 * This component allows users to toggle the visibility of the patrol menu,
 * sort patrols by ID or status, and displays detailed information for each patrol.
 *
 * @param {Map} props.patrols - A map containing patrol data, where each entry is a patrol ID and its associated details.
 * @param {function} props.setNavigateTo - Function to set location to center in map.
 *
 * @returns {JSX.Element} The rendered patrols menu.
 */
function PatrolsMenu({ patrols, setNavigateTo }) {
  const [sortByStatus, setSortByStatus] = useState(true);
  const [sortedPatrols, setSortedPatrols] = useState([]);
  const [isPatrolListVisible, setIsPatrolListVisible] = useState(false);

  const [expandedItem, setExpandedItem] = useState(null);

  const { statusToColor, statusToCarColor } = usePatrols();

  const togglePatrolList = () => {
    setIsPatrolListVisible(!isPatrolListVisible);
  };

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
    <>
      <div id="patrolsButton" onClick={togglePatrolList} title="Lista patroli">
        <img src={car} alt="patrols" />
      </div>
      <div
        id="patrolsMenu"
        className={`${
          isPatrolListVisible ? "patrolsMenuVisible" : "patrolsMenuHidden"
        }`}
      >
        <img
          onClick={togglePatrolList}
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
                    className={expandedItem == id ? "expandedMenu" : ""}
                    style={{ backgroundColor: statusToColor(status) }}
                    onClick={(event) => setExpandedItem(id)}
                  >
                    <div key={id} className="patrol-item">
                      {id}
                    </div>
                    <p>{name + " " + surname}</p>
                    <p>{phone}</p>

                    <input
                      type="button"
                      value="Pokaż na mapie"
                      style={{ backgroundColor: statusToCarColor(status) }}
                      onClick={() => setNavigateTo(position)}
                    />
                  </div>
                )
              )
            : ""}
        </div>
      </div>
    </>
  );
}

export default PatrolsMenu;
