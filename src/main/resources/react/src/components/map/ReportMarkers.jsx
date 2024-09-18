import { useMemo, useState, useEffect } from "react";
import {
  AdvancedMarker,
  InfoWindow,
  useAdvancedMarkerRef,
} from "@vis.gl/react-google-maps";
import reportImage from "../../icons/SOSANIM.svg";
import reportImageDot from "../../icons/sosdot.svg";

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
            width: status === 0 ? "200px" : "50px",
            height: status === 0 ? "100px" : "50px",
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
            Zgłoszenie oczekuje od:
          </div>
          {formatTime(passedTime)}
          <input
            type="button"
            value="Przydziel zgłoszenie"
            className="assignReportButton"
            onClick={() => selectReport(id)}
          />
        </InfoWindow>
      )}
    </>
  );
};
const calculatePassedTime = (date) => {
  const now = new Date().getTime();
  const timeDifference = now - date;
  return Math.max(Math.floor(timeDifference / 1000), 0);
};
export default ReportMarkers;
