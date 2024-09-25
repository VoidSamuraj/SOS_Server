import React from 'react';
import menu from '../icons/menu.svg';

function TopBar({ onDropdownToggle, guards}) {
  return (
    <div id="topBar">
      <div>
           {guards ? (
          <div id="infoBox">
            <div>Patrole</div>
            <div>
              Dostępne:{Array.from(guards.values()).filter(guard => guard.status === 0).length}&nbsp;
              Interwencja:{Array.from(guards.values()).filter(guard => guard.status === 2).length}&nbsp;
              Niedostępne:{Array.from(guards.values()).filter(guard => guard.status === 1).length}
            </div>
          </div>
           ) : (
              ""
            )}
      <img onClick={ onDropdownToggle } src={menu} alt="menu" />
      </div>
    </div>
  );
}

export default TopBar;