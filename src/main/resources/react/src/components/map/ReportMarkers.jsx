import { useMemo, useState } from "react";
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
              date={new Date(date)}
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
            Data zgłoszenia:
          </div>
          {date.toLocaleDateString()}
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

export default ReportMarkers;
