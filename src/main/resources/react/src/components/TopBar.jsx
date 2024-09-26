import React from 'react';
import menu from '../icons/menu.svg';


/**
 * TopBar component displays the status of guards and includes a dropdown toggle.
 *
 * @param {Function} props.onDropdownToggle - Function to be called when the dropdown is toggled.
 * @param {Map} props.guards - A Map of guards, where each guard has a status.
 *
 * @returns {JSX.Element} The rendered component.
 */
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