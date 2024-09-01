import React from 'react';
import '../style/settingsMenu.css'; // Załaduj style dla tego komponentu
import rightarrow from '../icons/right-arrow.svg';


function SettingsMenu({isVisible, onSettingsToggle}) {
  return (
    <div id="settingsMenu" className={`${isVisible ? 'settingsMenuVisible' : 'settingsMenuHidden'}`}>
      <img onClick={onSettingsToggle} src={rightarrow} alt="close" />
      <div className="switch">
        <input className="toggle" type="checkbox" role="switch" name="toggle" />
        <span className="slider">Ciemny tryb</span>
      </div>
      <div className="slider-container">
        <label htmlFor="mySlider">Kontrast</label><br />
        <input type="range" id="mySlider" min="0" max="100" value="0" />
      </div>
    </div>
  );
}

export default SettingsMenu;
