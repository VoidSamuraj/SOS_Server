import React, { useState, useEffect, useCallback, useRef } from "react";
import TopBar from "./TopBar";
import DropdownMenu from "./DropdownMenu";
import SettingsMenu from "./SettingsMenu";
import PatrolsMenu from "./PatrolsMenu";
import InterventionsMenu from "./InterventionsMenu";
import MyMap from "./map/MyMap";
import StatsOverlay from "./StatsOverlay";
import { useReports, usePatrols } from "./map/MapFunctions";
import { getAllGuards } from "../script/ApiService.js";
import SystemWebSocket from "../script/SystemWebSocket.js";
import { LoadScript, Autocomplete } from "@react-google-maps/api";
import keys from "../keys";
import config from "../config";

const libraries = ["places"];

/**
 * Home component serves as the main interface for managing patrols and reports.
 *
 * This component allows users to view and interact with patrols and reports.
 * It includes features such as dropdown menus for settings and statistics,
 * as well as the ability to assign tasks to patrols.
 *
 * @returns {JSX.Element} The rendered component.
 */
function Home() {
  const { patrols, editPatrol, syncPatrols, convertArrayToPatrolMap } =
    usePatrols();
  const { reports, addReport, editReport, syncReports, removeReport } =
    useReports();
  const [locationJson, setLocationJson] = useState(
    localStorage.getItem("HomeLocation")
  );

  const [isDropdownVisible, setIsDropdownVisible] = useState(false);
  const [isSettingsVisible, setIsSettingsVisible] = useState(false);
  const [isStatsVisible, setIsStatsVisible] = useState(false);
  const [navigateTo, setNavigateTo] = useState(false);

  const [isLoading, setIsLoading] = useState(false);

  const mapSocketRef = useRef(null);

  useEffect(() => {
    document.documentElement.classList.add("indexStyle");
    document.body.classList.add("indexStyle");

    mapSocketRef.current = new SystemWebSocket(
      "wss://"+config.ADDRESS+":"+config.PORT+"/mapSocket",
      () => {setIsLoading(false);},
      () => {setIsLoading(true);}
    );

    const messageHandler = (data) => {
      if (
        Array.isArray(data?.updatedGuards) &&
        Array.isArray(data?.updatedReports)
      ) {
        syncPatrols(data.updatedGuards);
        syncReports(data.updatedReports);
      }
    };

    mapSocketRef.current.addMessageHandler(messageHandler);

    return () => {
      document.documentElement.classList.remove("indexStyle");
      document.body.classList.remove("indexStyle");
      mapSocketRef.current.removeMessageHandler(messageHandler);
      mapSocketRef.current.close();
    };
  }, []);

  const assignTask = (patrolId, reportId) => {
    editPatrol(patrolId, 2, null);
    editReport(reportId, null, null, 1);
  };

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


  return (
    <LoadScript googleMapsApiKey={keys.GOOGLE_API_KEY} libraries={libraries}>
      <TopBar onDropdownToggle={toggleDropdown} guards={patrols}/>
      <DropdownMenu
        isVisible={isDropdownVisible}
        onSettingsToggle={toggleSettings}
        onStatsToggle={toggleStats}
        onAdminClick={() => (window.location.href = "/administration")}
      />
      <SettingsMenu
        isVisible={isSettingsVisible}
        onSettingsToggle={toggleSettings}
        locationJson={locationJson}
        setLocationJson={setLocationJson}
        canSetMapLoc={true}
      />

      <PatrolsMenu
        patrols={patrols}
        setNavigateTo={setNavigateTo}
      />
      <InterventionsMenu
        patrols={patrols}
      />
      <MyMap
        patrols={patrols}
        reports={reports}
        locationHome={locationJson}
        onAssignTask={assignTask}
        navigateTo={navigateTo}
        setNavigateTo={setNavigateTo}
      />
      <StatsOverlay isVisible={isStatsVisible} onStatsToggle={toggleStats} locationJson={locationJson} />
      <div class={isLoading ? "loader" : "hiddenLoader"}><div></div></div>
    </LoadScript>
  );
}

export default Home;
