import React, { useState, useEffect } from "react";
import "../style/assignTaskBox.css"; // Załaduj style dla tego komponentu
import bell from "../icons/bell.svg";
import { usePatrols } from "./map/MapFunctions";

function AssignTaskBox({ patrols, reports, onAssignTask, colors}) {
  const [hideBell, setHideBell] = useState(false);
  const [selectedReport, setSelectedReport] = useState(null);
  const [selectedPatrol, setSelectedPatrol] = useState(null);
  const [nrOfMenu, setNrOfMenu] = useState(1);
  const [isButtonDisabled, setIsButtonDisabled] = useState(true);
  const [sortedPatrols, setSortedPatrols] = useState([]);
  const [sortedReports, setSortedReports] = useState([]);


  const { statusToCode } = usePatrols();

  function onBack() {
    if (nrOfMenu >= 2) setNrOfMenu(nrOfMenu - 1);
  }
  function proceed() {
    if (!isButtonDisabled && nrOfMenu <= 2)
        setNrOfMenu(nrOfMenu + 1);
    else if(selectedReport != null && selectedPatrol != null && nrOfMenu == 3){
        setHideBell(false);
        setNrOfMenu(1);
        onAssignTask(selectedPatrol,selectedReport);
        setSelectedPatrol(null)
        setSelectedReport(null)
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

  /*
    const sortedPatrols = Array.from(patrols.entries()).sort(([, { color: colorA }], [, { color: colorB }]) => {return colorA.localeCompare(colorB);});
*/

  useEffect(() => {
    let sorted = Array.from(patrols.entries())
      .filter(([, { status }]) => {
        if (patrols.size === 0 || selectedReport == null ) return false;

        return (
          status == 0
        );
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
    console.log(sorted)
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
      >
        <img src={bell} alt="bell" />
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
                onClick={() => setSelectedReport(id)}
                className={`${id == selectedReport ? "selected" : ""}`}
              >
                {id}
              </div>
            ))
          ) : nrOfMenu == 2 ? (
            sortedPatrols.map(([id, { position, status }]) => (
              <div
                key={id}
                onClick={() => setSelectedPatrol(id)}
                className={`${id == selectedPatrol ? "selected" : ""}`}
                style={{ backgroundColor: statusToCode(status)}}
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
