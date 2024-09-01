import React from 'react';
import '../style/topBar.css'; // Załaduj style dla tego komponentu
import menu from '../icons/menu.svg';

function TopBar({ onDropdownToggle }) {
  return (
    <div id="topBar">
      <div id="infoBox">
        <div>Patrole</div>
        <div>
          Dostępne:2
          Interwencja:2
          Niedostępne:2
        </div>
      </div>
      <img onClick={ onDropdownToggle } src={menu} alt="menu" />
    </div>
  );
}

export default TopBar;