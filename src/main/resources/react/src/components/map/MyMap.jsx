import React, { useState, useEffect, useRef } from "react";
import config from "../../config";
import {
  APIProvider,
  Map as GoogleMap,
  useMap,
} from "@vis.gl/react-google-maps";
import CarMarkers from "./CarMarkers.jsx";
import ReportMarkers from "./ReportMarkers.jsx";
import homeImg from "../../icons/home.svg";
import AssignTaskBox from "../AssignTaskBox";
import MapController from "./MapController.jsx"


/**
 * MyMap component renders a Google Map with car markers and report markers.
 * It provides functionalities to assign tasks based on selected reports,
 * and includes a button to reset the map's center location to the default.
 *
 * @param {Map} props.patrols - Aa Map containing patrol used for rendering
 * car markers on the map.
 * @param {Map} props.reports - A Map containing report data used for rendering
 * report markers on the map.
 * @param {string} props.locationJson - A JSON string representing the location
 * coordinates to navigate the map.
 * @param {function} props.onAssignTask - A callback function to handle task assignment
 * for selected reports.
 *
 * @returns {JSX.Element} The rendered map component.
 */
function MyMap({ patrols, reports, locationJson, onAssignTask }) {
  const [hideBell, setHideBell] = useState(false);
  const [selectedReport, setSelectedReport] = useState(null);
  const [nrOfMenu, setNrOfMenu] = useState(1);

  const selectReport = (id) => {
    setSelectedReport(id);
    setNrOfMenu(2);
    setHideBell(true);
  };

  const [buttonState, setButtonState] = useState(true);
  const toggleButton = () => {
    setButtonState(!buttonState);
  };

  useEffect(() => {
    toggleButton();
  }, [locationJson]);

  return (
    <>
      <APIProvider apiKey={config.GOOGLE_API_KEY}>
        <GoogleMap
          defaultZoom={7.5}
          defaultCenter={{ lat: 51.9189046, lng: 19.1343786 }}
          mapId={config.MAP_ID}
        >
          <CarMarkers cars={patrols} />
          <ReportMarkers reports={reports} selectReport={selectReport} />
          <MapController
            locationJson={locationJson}
            refreshFlag={buttonState}
          />
        </GoogleMap>
        <img id="homeButton" onClick={toggleButton} src={homeImg} alt="home" title="Pokaż domyślną lokalizację"/>
      </APIProvider>
      <AssignTaskBox
        patrols={patrols}
        reports={reports}
        onAssignTask={onAssignTask}
        hideBell={hideBell}
        setHideBell={setHideBell}
        selectedReport={selectedReport}
        setSelectedReport={setSelectedReport}
        nrOfMenu={nrOfMenu}
        setNrOfMenu={setNrOfMenu}
      />
    </>
  );
}

export default MyMap;
