import React, { useState, useEffect, useRef } from "react";
import TopBar from "./TopBar";
import DropdownMenu from "./DropdownMenu";
import SettingsMenu from "./SettingsMenu";
import PatrolsMenu from "./PatrolsMenu";
import InterventionsMenu from "./InterventionsMenu";
import MyMap from "./map/MyMap";
import StatsOverlay from "./StatsOverlay";
import { useReports, usePatrols } from "./map/MapFunctions";
import SystemWebSocket from "../script/SystemWebSocket.js";
import { refreshToken, getEmployee } from "../script/ApiService.js";
import { LoadScript } from "@react-google-maps/api";
import keys from "../keys";

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
  const { patrols, editPatrol, syncPatrols } = usePatrols();
  const { reports, editReport, syncReports } = useReports();
  const [locationJson, setLocationJson] = useState(
    localStorage.getItem("HomeLocation")
  );

  const [isDropdownVisible, setIsDropdownVisible] = useState(false);
  const [isSettingsVisible, setIsSettingsVisible] = useState(false);
  const [isStatsVisible, setIsStatsVisible] = useState(false);
  const [isPatrolListVisible, setIsPatrolListVisible] = useState(false);
  const [isInterventionListVisible, setIsInterventionListVisible] =
    useState(false);
  const [navigateTo, setNavigateTo] = useState(false);

  const [isTooltipVisible, setIsTooltipVisible] = useState(false);

  const [isLoading, setIsLoading] = useState(false);

  const mapSocketRef = useRef(null);

  const [loggedInEmployee, setLoggedInEmployee] = useState(null);

  const loadEmployeeInfo = async (id) => {
    let employee = await getEmployee(id);
    setLoggedInEmployee(employee);
  };

  useEffect(() => {
    document.documentElement.classList.add("indexStyle");
    document.body.classList.add("indexStyle");
    mapSocketRef.current = new SystemWebSocket(
      "wss://" + window.location.host + "/mapSocket",
      () => {
        setIsLoading(false);
      },
      () => {
        setIsLoading(true);
      }
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

    const checkTokenExpiration = () => {
      const currentTimestamp = Date.now();
      const exp = localStorage.getItem("tokenExp");

      if (exp) {
        const expTimestamp = parseInt(exp, 10);
        const timeDifference = expTimestamp - currentTimestamp;

        if (timeDifference <= 0) {
          window.location.reload();
        } else if (timeDifference < 300000) {
          // Less than 5 minutes to expire
          refreshToken();
        }
      } else {
        console.error("Expiration date not found in localStorage");
      }
    };

    checkTokenExpiration();

    const intervalId = setInterval(() => {
      checkTokenExpiration();
    }, 30000); // 30s


    let id = localStorage.getItem("userData");
    if (id) loadEmployeeInfo(id);

    return () => {
      document.documentElement.classList.remove("indexStyle");
      document.body.classList.remove("indexStyle");
      mapSocketRef.current.removeMessageHandler(messageHandler);
      mapSocketRef.current.close();
      clearInterval(intervalId);
    };
  }, []);

  useEffect(() => {
    if (isTooltipVisible) {
      setIsDropdownVisible(true);
      setIsSettingsVisible(true);
    }
  }, [isTooltipVisible]);

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
    <>
      <LoadScript googleMapsApiKey={keys.GOOGLE_API_KEY} libraries={libraries}>
        <div
          style={{
            height: "100%",
            width: "100%",
            pointerEvents: isLoading ? "none" : "auto",
            filter: isLoading ? "grayscale(80%) brightness(80%)" : "none",
          }}
        >
          <TopBar onDropdownToggle={toggleDropdown} guards={patrols} />
          <DropdownMenu
            isVisible={isDropdownVisible}
            onSettingsToggle={toggleSettings}
            onStatsToggle={toggleStats}
            onAdminClick={(loggedInEmployee!=null && loggedInEmployee.roleCode == 2)? () => (window.location.href = "/administration"):null}
          />
          <SettingsMenu
            isVisible={isSettingsVisible}
            isTooltipVisible={isTooltipVisible}
            onSettingsToggle={toggleSettings}
            locationJson={locationJson}
            setLocationJson={setLocationJson}
            canSetMapLoc={true}
          />

          <PatrolsMenu
            patrols={patrols}
            isPatrolListVisible={isPatrolListVisible}
            setIsPatrolListVisible={setIsPatrolListVisible}
            hideOtherMenu={() => {
              setIsInterventionListVisible(false);
            }}
            setNavigateTo={setNavigateTo}
          />
          <InterventionsMenu
            interventions={reports}
            isInterventionListVisible={isInterventionListVisible}
            setIsInterventionListVisible={setIsInterventionListVisible}
            hideOtherMenu={() => {
              setIsPatrolListVisible(false);
            }}
            setNavigateTo={setNavigateTo}
          />
          <MyMap
            patrols={patrols}
            reports={reports}
            locationHome={locationJson}
            onAssignTask={assignTask}
            navigateTo={navigateTo}
            setNavigateTo={setNavigateTo}
            setIsTooltipVisible={() => {
              setIsTooltipVisible(true);
              setTimeout(() => {
                setIsTooltipVisible(false);
              }, 5000);
            }}
          />
          <StatsOverlay
            isVisible={isStatsVisible}
            onStatsToggle={toggleStats}
            locationJson={locationJson}
          />
        </div>
      </LoadScript>
      <div class={isLoading ? "loader" : "hiddenLoader"}>
        <div></div>
      </div>
    </>
  );
}

export default Home;
