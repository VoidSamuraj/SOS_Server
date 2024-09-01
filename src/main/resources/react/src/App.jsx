import React, { useState } from 'react';
import TopBar from './components/TopBar';
import DropdownMenu from './components/DropdownMenu';
import SettingsMenu from './components/SettingsMenu';
import PatrolsMenu from './components/PatrolsMenu';
import MyMap from './components/MyMap';
import StatsOverlay from './components/StatsOverlay';
import AssignTaskBox from './components/AssignTaskBox';
import './style/style.css'; // Załaduj style dla całej aplikacji
import car from './icons/car.svg';



function App() {
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
      <PatrolsMenu isVisible={isPatrolListVisible} onPatrolsToggle={togglePatrolList}/>
      <MyMap />
      <StatsOverlay isVisible={isStatsVisible} onStatsToggle={toggleStats}/>
      <AssignTaskBox />
    </>
  );
}

export default App;
