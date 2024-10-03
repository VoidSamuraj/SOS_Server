import React, { useState, useEffect } from "react";
import leftarrow from "../icons/left-arrow.svg";
import { usePatrols } from "./map/MapFunctions";
import car from "../icons/car.svg";

/**
 * InterventionsMenu component displays a list of interventions with sorting options.
 *
 * This component allows users to toggle the visibility of the intervention menu,
 * sort patrols by ID or status, and displays detailed information for each patrol.
 *
 * @param {Map} props.interventions - A map containing interventions data, where each entry is a patrol ID and its associated details.
 *
 * @returns {JSX.Element} The rendered interventions menu.
 */
function InterventionsMenu({ interventions }) {
  const [sortByStatus, setSortByStatus] = useState(true);
  const [sortedInterventions, setSortedInterventions] = useState([]);
  const [isInterventionListVisible, setIsInterventionListVisible] = useState(false);

  const [expandedItem, setExpandedItem] = useState(null);

  const { statusToColor } = usePatrols();

  const togglePatrolList = () => {
    setIsInterventionListVisible(!isInterventionListVisible);
  };

  useEffect(() => {
    if (interventions) {
      const sorted = Array.from(interventions.entries())
      setSortedInterventions(sorted);
    }
  }, [sortByStatus, interventions]);

  return (
      <>
            <div id="interventionsButton" onClick={togglePatrolList} title="Lista Interwencji">
              <img src={car} alt="interventions" />
            </div>

    <div
      id="interventionsMenu"
      className={`${isInterventionListVisible ? "interventionsMenuVisible" : "interventionsMenuHidden"}`}
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
      <div id="interventionsList">

      </div>
    </div>
    </>
  );
}

export default InterventionsMenu;