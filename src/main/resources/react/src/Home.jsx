import { useState, useEffect, useCallback } from "react";
import TopBar from "./components/TopBar";
import DropdownMenu from "./components/DropdownMenu";
import SettingsMenu from "./components/SettingsMenu";
import PatrolsMenu from "./components/PatrolsMenu";
import MyMap from "./components/map/MyMap";
import StatsOverlay from "./components/StatsOverlay";
import AssignTaskBox from "./components/AssignTaskBox";
import "./style/style.css";
import car from "./icons/car.svg";
import { usePatrols, useReports } from "./components/map/MapFunctions";

function Home({onLogout}) {
  const { patrols, setPatrols, addPatrol, updatePatrol, removePatrol } =
    usePatrols();
  const { reports, addReport, editReport, removeReport } = useReports();

  useEffect(() => {
    document.documentElement.classList.add("indexStyle");
    document.body.classList.add("indexStyle");

    return () => {
      document.documentElement.classList.remove("indexStyle");
      document.body.classList.remove("indexStyle");
    };
  }, []);

  const assignTask = (patrolId, reportId) => {
    updatePatrol(patrolId, "#AAA", null);
    editReport(reportId, null, null, 1);
  };

  //TEST

  useEffect(() => {
    const initialize = () => {
      addPatrol(11, { lat: 51.5, lng: 19.0 }, "#F00", "Jan", "Nowak", 987654321);
      addPatrol(12, { lat: 51.6, lng: 21.1 }, "#0F0", "Grzeorz", "Braun", 998);
      addPatrol(13, { lat: 52.6, lng: 21.5 }, "#0F0", "Jacek", "Sasin", 123123123);
      addPatrol(14, { lat: 50.2, lng: 22.9 }, "#0F0", "Andrzej", "Nowak", 456123789);
      addPatrol(5, { lat: 53.6, lng: 22.0 }, "#F00", "Bernard", "Kozak", 741852963);
      addPatrol(6, { lat: 51.6, lng: 22.2 }, "#AAA", "Sylwia", "Małysz", 987456321);
    };

    initialize();
  }, []);

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
    <>
      <script></script>
      <TopBar onDropdownToggle={toggleDropdown} />
      <DropdownMenu
        isVisible={isDropdownVisible}
        onSettingsToggle={toggleSettings}
        onStatsToggle={toggleStats}
        onLogout={onLogout}
      />
      <SettingsMenu
        isVisible={isSettingsVisible}
        onSettingsToggle={toggleSettings}
      />
      <div id="patrolsButton" onClick={togglePatrolList}>
        <img src={car} alt="patrols" />
      </div>
      <PatrolsMenu
        isVisible={isPatrolListVisible}
        onPatrolsToggle={togglePatrolList}
        patrols={patrols}
      />
      <MyMap patrols={patrols} reports={reports} />
      <StatsOverlay isVisible={isStatsVisible} onStatsToggle={toggleStats} />
      <AssignTaskBox
        patrols={patrols}
        reports={reports}
        onAssignTask={assignTask}
      />
    </>
  );
}

export default Home;
