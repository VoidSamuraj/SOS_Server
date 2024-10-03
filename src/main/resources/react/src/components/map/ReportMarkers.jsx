import React, { useMemo, useState, useEffect } from "react";
import {
  AdvancedMarker,
  InfoWindow,
  useAdvancedMarkerRef,
} from "@vis.gl/react-google-maps";
import reportImage from "../../icons/SOSANIM.svg";
import reportImageDot from "../../icons/sosdot.svg";

/**
 * ReportMarkers component renders a list of report markers on a map.
 * Each marker is represented by an AlertIcon, which displays information
 * about the report, including its position, date, and status.
 *
 * @param {Map} props.reports - A Map containing report data, where the key is the report ID
 * and the value is an object with position, date, and status.
 * @param {function} props.selectReport - Function to handle report selection when the
 * "Przydziel zgłoszenie" button is clicked.
 *
 * @returns {JSX.Element} The rendered report markers component.
 */
const ReportMarkers = ({ reports, selectReport }) => {
  return useMemo(
    () => (
      <>
        {Array.from(reports.entries()).map(
          ([id, { position, date, status }]) => (
            <AlertIcon
              id={id}
              position={position}
              date={date}
              status={status}
              selectReport={selectReport}
            />
          )
        )}
      </>
    ),
    [reports]
  );
};

/**
 * AlertIcon component represents a single report marker on the map.
 * It displays the marker based on its status and provides an infowindow
 * with additional information about the report when clicked.
 *
 * @param {string} props.id - The unique identifier of the report.
 * @param {Object} props.position - The geographical position of the report marker.
 * @param {number} props.date - The date when the report was created.
 * @param {number} props.status - The status code.style
 * One of the following:
 * WAITING(0),
 * IN_PROGRESS(1),
 * FINISHED(2);
 * @param {function} props.selectReport - Function to handle report selection when the
 * "Przydziel zgłoszenie" button is clicked.
 *
 * @returns {JSX.Element} The rendered alert icon component.
 */
const AlertIcon = ({ id, position, date, status, selectReport }) => {
  const [infowindowOpen, setInfowindowOpen] = useState(false);
  const [markerRef, marker] = useAdvancedMarkerRef();
  const [passedTime, setPassedTime] = useState(calculatePassedTime(date));

  useEffect(() => {
    const interval = setInterval(() => {
      const newPassedTime = calculatePassedTime(date);
      if (newPassedTime <= 0) {
        clearInterval(interval);
      }
      setPassedTime(newPassedTime);
    }, 1000);

    return () => clearInterval(interval);
  }, [date]);

  const formatTime = (seconds) => {
    const minutes = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${minutes}m ${secs}s`;
  };
  return (
    <>
      <AdvancedMarker
        key={id}
        ref={markerRef}
        position={position}
        onClick={() => setInfowindowOpen(true)}
      >
        <div
          className="mapMarker"
          style={{
            width: status === 0 ? "100px" : "25px",
            height: status === 0 ? "50px" : "25px",
          }}
        >
          <img src={status == 0 ? reportImage : reportImageDot} alt="Report" />
        </div>
      </AdvancedMarker>
      {infowindowOpen && (
        <InfoWindow
          anchor={marker}
          maxWidth={200}
          onCloseClick={() => setInfowindowOpen(false)}
        >

          <div style={{ fontWeight: "bold", padding: "8px 0" }}>
            {status == 0 ? ("Zgłoszenie oczekuje od:"):("Podjęto zgłoszenie")}
          </div>
                  {status == 0 ? (
                      <>
          {formatTime(passedTime)}
          </>
          ):("")
      }
          <input
            type="button"
            value={status == 0 ? "Wyślij patrol": "Anuluj interwencję"}
            className="assignReportButton"
            onClick={() => {
                if(status == 0)
                selectReport(id)
                //TODO
                //else
                }}
          />
        </InfoWindow>
      )}
    </>
  );
};

/**
 * calculatePassedTime calculates the time that has passed since the given date.
 *
 * @param {string} date - The date to compare against (timestamp).
 * @returns {number} The number of seconds that have passed since the date.
 */
const calculatePassedTime = (date) => {
  const now = new Date().getTime();
  const dateTimestamp = new Date(date).getTime();
  const timeDifference = now - dateTimestamp;
  return Math.max(Math.floor(timeDifference / 1000), 0);
};
export default ReportMarkers;
