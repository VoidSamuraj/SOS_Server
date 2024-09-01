import React from 'react';
import '../style/patrolsMenu.css'; // Załaduj style dla tego komponentu
import leftarrow from '../icons/left-arrow.svg';

function PatrolsMenu({isVisible, onPatrolsToggle}) {
  return (
    <div id="patrolsMenu" className={`${isVisible ? 'patrolsMenuVisible' : 'patrolsMenuHidden'}`}>
      <img onClick={onPatrolsToggle} id="patrolsClose" src={leftarrow} alt="close" />
      <div>
        <span>Sortuj</span>
        <button>ID</button>
        <button>Status</button>
      </div>
      <div id="patrolsList"></div>
    </div>
  );
}

export default PatrolsMenu;
