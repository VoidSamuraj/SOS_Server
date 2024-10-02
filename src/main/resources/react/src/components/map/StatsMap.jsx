import React, { useState, useEffect } from "react";
import config from "../../config";
import {
  APIProvider,
  Map as GoogleMap,
} from "@vis.gl/react-google-maps";
import MapController from "./MapController.jsx";
import Heatmap from "./Heatmap.jsx";
import homeImg from "../../icons/home.svg";
import { getAllReports } from "../../script/ApiService.js";

function StatsMap({ locationJson, radius, opacity }) {
  const [buttonState, setButtonState] = useState(true);
  const [interventions, setInterventions] = useState(null);
  const toggleButton = () => {
    setButtonState(!buttonState);
  };

  useEffect(() => {
    const fetchInterventions = async () => {
      const data = await getAllReports();
      if (data) {
        setInterventions(data);
      }
    };
    fetchInterventions();
  }, []);

  return (
    <APIProvider apiKey={config.GOOGLE_API_KEY}>
      <GoogleMap
        defaultZoom={7.5}
        defaultCenter={{ lat: 51.9189046, lng: 19.1343786 }}
        mapId={config.STATS_MAP_ID}
        gestureHandling={"greedy"}
        disableDefaultUI={true}
      >
        {interventions && (
          <Heatmap interventions={interventions} radius={radius} opacity={opacity} />
        )}
        <MapController locationJson={locationJson} refreshFlag={buttonState} />
      </GoogleMap>
      <img
        id="homeButtonStats"
        onClick={toggleButton}
        src={homeImg}
        alt="home"
        title="Pokaż domyślną lokalizację"
      />
    </APIProvider>
  );
}

export default StatsMap;
