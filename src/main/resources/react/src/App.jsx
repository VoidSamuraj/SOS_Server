import { useState, useEffect, useCallback } from 'react';
import TopBar from './components/TopBar';
import DropdownMenu from './components/DropdownMenu';
import SettingsMenu from './components/SettingsMenu';
import PatrolsMenu from './components/PatrolsMenu';
import MyMap from './components/map/MyMap';
import StatsOverlay from './components/StatsOverlay';
import AssignTaskBox from './components/AssignTaskBox';
import './style/style.css';
import car from './icons/car.svg';
import { usePatrols, useReports } from './components/map/MapFunctions';


function App() {
  const { patrols, setPatrols, addPatrol, removePatrol, removeFirstPatrol} = usePatrols();
  const { reports, addReport, removeReport, removeFirstReport } = useReports();

    //TEST

  useEffect(() => {
    const initialize = () => {
      addPatrol(11, { lat: 51.5, lng: 19.0 }, "#F00");
      addPatrol(12, { lat: 51.6, lng: 21.1 }, "#0F0");
      addPatrol(13, { lat: 52.6, lng: 21.5 }, "#0F0");
      addPatrol(14, { lat: 50.2, lng: 22.9 }, "#0F0");
      addPatrol(5, { lat: 53.6, lng: 22.0 }, "#F00");
      addPatrol(6, { lat: 51.6, lng: 22.2 }, "#AAA");
    };

    initialize();
  }, []);

  const generateRandomReports = useCallback(() => {
    if (reports.size < 5) {
      const y = Math.floor(Math.random() * (54 - 50)) + 50;
      const x = Math.floor(Math.random() * (24 - 15)) + 15;
      addReport(reports.size, { lat: y, lng: x });
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
        if(isSettingsVisible)
            setIsSettingsVisible(false);
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
      <TopBar onDropdownToggle={toggleDropdown}/>
      <DropdownMenu isVisible={isDropdownVisible} onSettingsToggle={toggleSettings}  onStatsToggle={toggleStats}/>
      <SettingsMenu isVisible={isSettingsVisible} onSettingsToggle={toggleSettings}/>
      <div id="patrolsButton" onClick={togglePatrolList}>
          <img src={car} alt="patrols"/>
      </div>
      <PatrolsMenu isVisible={isPatrolListVisible} onPatrolsToggle={togglePatrolList} patrols={patrols}/>
      <MyMap patrols={patrols}  reports={reports} />
      <StatsOverlay isVisible={isStatsVisible} onStatsToggle={toggleStats}/>
      <AssignTaskBox patrols={patrols} reports={reports}/>
    </>
  );
}

export default App;
