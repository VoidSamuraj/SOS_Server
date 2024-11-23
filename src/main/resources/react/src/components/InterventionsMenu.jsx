import React, { useState, useEffect } from "react";
import leftarrow from "../icons/left-arrow.svg";
import { usePatrols } from "./map/MapFunctions";
import emergency from "../icons/emergency.svg";

/**
 * InterventionsMenu component displays a list of interventions with sorting options.
 *
 * This component allows users to toggle the visibility of the intervention menu,
 * sort patrols by ID or status, and displays detailed information for each patrol.
 *
 * @param {Map} props.interventions - A map containing interventions data, where each entry is a patrol ID and its associated details.
 * @param {boolean} props.isInterventionListVisible - Boolean representing if menu is visible.
 * @param {function} props.setIsInterventionListVisible - Function to set is menu visibility.
 * @param {function} props.hideOtherMenu - Function to hide other menus.
 * @param {function} props.setNavigateTo - Function to set location to center in map.
 *
 * @returns {JSX.Element} The rendered interventions menu.
 */
function InterventionsMenu({
  interventions,
  isInterventionListVisible,
  setIsInterventionListVisible,
  hideOtherMenu,
  setNavigateTo,
}) {
  const [sortByStatus, setSortByStatus] = useState(true);
  const [sortedInterventions, setSortedInterventions] = useState([]);

  const [expandedItem, setExpandedItem] = useState(null);

  const { statusToColorReportMenu, statusToReportColor } = usePatrols();

  const toggleInterventionList = () => {
    setIsInterventionListVisible(!isInterventionListVisible);
  };

  useEffect(() => {
    if (interventions) {
      const sorted = Array.from(interventions.entries()).sort(
        ([idA, { status: statusA }], [idB, { status: statusB }]) => {
          if (sortByStatus) {
            return statusA - statusB;
          } else {
            return idA - idB;
          }
        }
      );
      setSortedInterventions(sorted);
    }
  }, [sortByStatus, interventions]);

  return (
    <>
      <div
        id="interventionsButton"
        onClick={() => {
          hideOtherMenu();
          toggleInterventionList();
        }}
        title="Lista Zgłoszeń"
      >
        <img src={emergency} alt="zgłoszenia" />
      </div>

      <div
        id="interventionsMenu"
        className={`${
          isInterventionListVisible
            ? "interventionsMenuVisible"
            : "interventionsMenuHidden"
        }`}
      >
        <img
          onClick={() => {
            hideOtherMenu();
            toggleInterventionList();
          }}
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
          {sortedInterventions
            ? sortedInterventions
                .filter(([key, { status }]) => status === 0 || status === 1)
                .map(([key, { position, date, status }]) => (
                  <div
                    className={expandedItem == key ? "expandedMenu" : ""}
                    style={{ backgroundColor: statusToColorReportMenu(status) }}
                    onClick={(event) => setExpandedItem(key)}
                  >
                    <div key={key} className="patrol-item">
                      {key}
                    </div>
                    <p>{formatDateISO(date)}</p>
                    <input
                      type="button"
                      value="Pokaż na mapie"
                      style={{ backgroundColor: statusToReportColor(status) }}
                      onClick={() => setNavigateTo(position)}
                    />
                  </div>
                ))
            : ""}
        </div>
      </div>
    </>
  );
}
function formatDateISO(date) {
  return new Date(date).toLocaleDateString("pl-PL", {
    year: "numeric",
    month: "long",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
  });
}

export default InterventionsMenu;
