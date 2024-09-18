import { useState, useEffect, useRef } from "react";
import "../../style/map.css";
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

function MapController({ locationJson, refreshFlag }) {
  const map = useMap();

  const navigateHome = () => {
    if (map) {
      const savedLocation = localStorage.getItem("HomeLocation");
      let locationData;
      try {
        locationData = locationJson
          ? JSON.parse(locationJson)
          : savedLocation
          ? JSON.parse(savedLocation)
          : null;
      } catch (error) {
        console.error("Error parsing location data from localStorage:", error);
        locationData = null;
      }

      const defaultCenter = {
        lat: locationData?.latitude ?? 51.9189046,
        lng: locationData?.longitude ?? 19.1343786,
      };

      map.panTo(defaultCenter);
    }
  };

  useEffect(() => {
    navigateHome();
  }, [locationJson, map, refreshFlag]);

  return null;
}

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
        <img id="homeButton" onClick={toggleButton} src={homeImg} alt="home" />
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
