import React, { useState, useEffect, useCallback } from "react";
import TopBar from "./TopBar";
import DropdownMenu from "./DropdownMenu";
import SettingsMenu from "./SettingsMenu";
import PatrolsMenu from "./PatrolsMenu";
import MyMap from "./map/MyMap";
import StatsOverlay from "./StatsOverlay";
import car from "../icons/car.svg";
import { useReports, usePatrols } from "./map/MapFunctions";
import {getAllGuards} from "../script/ApiService.js";

import { LoadScript, Autocomplete } from "@react-google-maps/api";
import config from "../config";
const libraries = ["places"];

function Home() {
  const {patrols, setPatrols, updatePatrol,  convertArrayToPatrolMap} =usePatrols();
  const { reports, addReport, editReport, removeReport } = useReports();
  const [locationJson, setLocationJson] = useState(localStorage.getItem("HomeLocation"));


  useEffect(() => {
    document.documentElement.classList.add("indexStyle");
    document.body.classList.add("indexStyle");

    return () => {
      document.documentElement.classList.remove("indexStyle");
      document.body.classList.remove("indexStyle");
    };
  }, []);
  useEffect(() => {
    getAllGuards()
    .then(data => {
            setPatrols(convertArrayToPatrolMap(data));
        })
  }, []);
  const assignTask = (patrolId, reportId) => {
    updatePatrol(patrolId, 2, null);
    editReport(reportId, null, null, 1);
  };
/*
    const [data, setData] = useState([]);
     useEffect(() => {
         const socket = new WebSocket('ws://localhost:8080/updates');

         socket.onopen = () => {
             console.log('WebSocket connection established');
         };

         socket.onmessage = (event) => {
             console.log('Received data:', event.data);
             const newData = event.data.split(', ').map(item => item.trim());
             setData(newData);
         };

         socket.onerror = (error) => {
             console.error('WebSocket error:', error);
         };

socket.onclose = (event) => {
        console.log('Closed websocket');
};

         return () => {
             socket.close();
         };
     }, []);
*/
  //TEST

  useEffect(() => {
    const initialize = () => {
      updatePatrol(0, null, { lat: 51.5, lng: 19.0 });
      updatePatrol(1, null, { lat: 51.2, lng: 18.9 });
      updatePatrol(2, null, { lat: 51.6, lng: 21.1 });
      updatePatrol(3, null, { lat: 52.6, lng: 21.5 });
    };
    initialize();
  }, [patrols.size == 0]);

  const generateRandomReports = useCallback(() => {
    if (reports.size < 5) {
      const y = Math.floor(Math.random() * (54 - 50)) + 50;
      const x = Math.floor(Math.random() * (24 - 15)) + 15;
      addReport(reports.size, { lat: y, lng: x }, Date.now(), 0);
    }
  }, [reports]);

  useEffect(() => {
    const intervalId = setInterval(generateRandomReports, 5000);

    return () => {
      clearInterval(intervalId);
    };
  }, [generateRandomReports]);

  //ENDTEST

  const [isDropdownVisible, setIsDropdownVisible] = useState(false);
  const [isSettingsVisible, setIsSettingsVisible] = useState(false);
  const [isStatsVisible, setIsStatsVisible] = useState(false);
  const [isPatrolListVisible, setIsPatrolListVisible] = useState(false);

  const toggleDropdown = () => {
    setIsDropdownVisible(!isDropdownVisible);
    if (isSettingsVisible) setIsSettingsVisible(false);
  };
  const toggleSettings = () => {
    setIsSettingsVisible(!isSettingsVisible);
  };
  const toggleStats = () => {
    setIsStatsVisible(!isStatsVisible);
  };
  const togglePatrolList = () => {
    setIsPatrolListVisible(!isPatrolListVisible);
  };

  return (
    <LoadScript googleMapsApiKey={config.GOOGLE_API_KEY} libraries={libraries}>
      <TopBar onDropdownToggle={toggleDropdown} guards={patrols} />
      <DropdownMenu
        isVisible={isDropdownVisible}
        onSettingsToggle={toggleSettings}
        onStatsToggle={toggleStats}
        onAdminClick={() => window.location.href = "/administration"}
      />
      <SettingsMenu
        isVisible={isSettingsVisible}
        onSettingsToggle={toggleSettings}
        locationJson={locationJson}
        setLocationJson={setLocationJson}
        canSetMapLoc={true}
      />
      <div id="patrolsButton" onClick={togglePatrolList}>
        <img src={car} alt="patrols" />
      </div>
      <PatrolsMenu
        isVisible={isPatrolListVisible}
        onPatrolsToggle={togglePatrolList}
        patrols={patrols}
      />
      <MyMap patrols={patrols} reports={reports} locationJson={locationJson} onAssignTask={assignTask}/>
      <StatsOverlay isVisible={isStatsVisible} onStatsToggle={toggleStats} />
    </LoadScript>
  );
}

export default Home;
