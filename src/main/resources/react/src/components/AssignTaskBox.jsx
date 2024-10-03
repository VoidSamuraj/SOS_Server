import React, { useState, useEffect } from "react";
import bell from "../icons/bell.svg";
import { usePatrols } from "./map/MapFunctions";

/**
 * AssignTaskBox component allows users to assign tasks to patrols based on reports.
 *
 * This component displays a dropdown menu where users can select a report and a patrol.
 * It features a navigation system with multiple steps for selecting the report and patrol,
 * and includes functionality to handle the assignment of tasks.
 *
 * @param {Array} props.patrols - An array of patrols available for task assignment.
 * @param {Map} props.reports - A map of reports that can be assigned to patrols.
 * @param {function} props.onAssignTask - Callback function to execute when a task is assigned.
 * @param {boolean} props.hideBell - Indicates if the notification bell is hidden.
 * @param {function} props.setHideBell - Function to toggle the visibility of the notification bell.
 * @param {string|null} props.selectedReport - Currently selected report for task assignment.
 * @param {function(number, Object):void} props.setSelectedReport - Function that accepts
 * an `id` (number) and a `location` object, and sets the selected report.
 * @param {string|null} props.selectedPatrol - Currently selected patrol for task assignment.
 * @param {function(number, Object):void} props.setSelectedPatrol - Function that accepts
 * an `id` (number) and a `location` object, and sets the selected patrol.
 * @param {number} props.nrOfMenu - Current menu step in the assignment process.
 * @param {function} props.setNrOfMenu - Function to set the current menu step.
 *
 * @returns {JSX.Element} The rendered task assignment box component.
 */
function AssignTaskBox({
  patrols,
  reports,
  onAssignTask,
  hideBell,
  setHideBell,
  selectedReport,
  setSelectedReport,
  selectedPatrol,
  setSelectedPatrol,
  nrOfMenu,
  setNrOfMenu,
}) {
  const [isButtonDisabled, setIsButtonDisabled] = useState(true);
  const [sortedPatrols, setSortedPatrols] = useState([]);
  const [sortedReports, setSortedReports] = useState([]);

  const { statusToColor } = usePatrols();

  function onBack() {
    if (nrOfMenu >= 2) setNrOfMenu(nrOfMenu - 1);
  }
  function proceed() {
    if (!isButtonDisabled && nrOfMenu <= 2) setNrOfMenu(nrOfMenu + 1);
    else if (
      selectedReport != null &&
      selectedPatrol != null &&
      nrOfMenu == 3
    ) {
      setHideBell(false);
      setNrOfMenu(1);
      onAssignTask(selectedPatrol, selectedReport);
      setSelectedPatrol(null, null);
      setSelectedReport(null, null);
    }
  }
  const updateButtonState = () => {
    const newButtonState =
      (nrOfMenu === 1 && selectedReport === null) ||
      (nrOfMenu === 2 && selectedPatrol === null);
    setIsButtonDisabled(newButtonState);
  };

  function haversineDistance(lat1, lon1, lat2, lon2) {
    const R = 6371; // earth radius in km
    const dLat = (lat2 - lat1) * (Math.PI / 180); // lat to rads
    const dLon = (lon2 - lon1) * (Math.PI / 180); // lon to rads

    const a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(lat1 * (Math.PI / 180)) *
        Math.cos(lat2 * (Math.PI / 180)) *
        Math.sin(dLon / 2) *
        Math.sin(dLon / 2);

    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return R * c; // distance in km
  }

  useEffect(() => {
    updateButtonState();
  }, [nrOfMenu, selectedReport, selectedPatrol]);

  useEffect(() => {
    let sorted = Array.from(patrols.entries())
      .filter(([, { status }]) => {
        if (patrols.size === 0 || selectedReport == null) return false;

        return status == 0;
      })
      .sort(([, { position: posA }], [, { position: posB }]) => {
        const distanceA = haversineDistance(
          posA.lat,
          posA.lng,
          reports.get(selectedReport).position.lat,
          reports.get(selectedReport).position.lng
        );
        const distanceB = haversineDistance(
          posB.lat,
          posB.lng,
          reports.get(selectedReport).position.lat,
          reports.get(selectedReport).position.lng
        );
        return distanceA - distanceB;
      });
    setSortedPatrols(sorted);
  }, [selectedReport, patrols]);

  useEffect(() => {
    let sorted = Array.from(reports.entries())
      .filter(([, { status }]) => {
        if (reports.size === 0) return false;

        return status == 0;
      })
      .sort(([, { date: dateA }], [, { date: dateB }]) => {
        const dateObjA = new Date(dateA);
        const dateObjB = new Date(dateB);
        return dateObjA - dateObjB;
      });
    setSortedReports(sorted);
  }, [reports]);

  return (
    <div id="assignTaskBox">
      <div
        id="bell"
        onClick={() => setHideBell(true)}
        className={`${reports.size > 0 && !hideBell ? "visible" : ""}`}
        title="Przydziel zgłoszenie"
      >
        <img src={bell} alt="bell" />
        {reports.size > 1 ? (
          <div id="bellCounter">{sortedReports.length}</div>
        ) : (
          ""
        )}
      </div>
      <div id="assignTaskMenu" className={`${hideBell ? "visible" : ""}`}>
        <div className="navButtons">
          <button
            id="assignBack"
            type="button"
            onClick={onBack}
            className={`${nrOfMenu > 1 ? "visible" : ""}`}
          >
            Cofnij
          </button>
          <button
            id="assignClose"
            onClick={() => setHideBell(false)}
            type="button"
          >
            Zamknij okno
          </button>
        </div>
        <div id="assignItems">
          {nrOfMenu == 1 ? (
            sortedReports.map(([id, { position, date, status }]) => (
              <div
                key={id}
                onClick={() =>setSelectedReport(id, position)}
                className={`${id == selectedReport ? "selected" : ""}`}
              >
                {id}
              </div>
            ))
          ) : nrOfMenu == 2 ? (
            sortedPatrols.map(([id, { position, status }]) => (
              <div
                key={id}
                onClick={() => {
                  setSelectedPatrol(id, position);
                }}
                className={`${id == selectedPatrol ? "selected" : ""}`}
                style={{ backgroundColor: statusToColor(status) }}
              >
                {id}
              </div>
            ))
          ) : (
            <>
              <div>{selectedReport}</div>
              <div style={{ backgroundColor: "#0F0" }}>{selectedPatrol}</div>
            </>
          )}
        </div>
        <div className="navButtons">
          <button
            id="sendPatrolButton"
            type="button"
            onClick={proceed}
            className={`my-button ${isButtonDisabled ? "disabled" : ""}`}
          >
            {nrOfMenu == 1
              ? "Wybierz Zgłoszenie"
              : nrOfMenu == 2
              ? "Wybierz patrol"
              : "Wyślij patrol"}
          </button>
        </div>
      </div>
    </div>
  );
}

export default AssignTaskBox;
