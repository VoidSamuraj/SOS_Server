import React, { useState} from 'react';
import '../style/patrolsMenu.css'; // Załaduj style dla tego komponentu
import leftarrow from '../icons/left-arrow.svg';

function PatrolsMenu({isVisible, onPatrolsToggle, patrols}) {

 const [sortByStatus, setSortByStatus] = useState(true);

const sortedPatrols = Array.from(patrols.entries()).sort(([idA, { color: colorA }], [idB, { color: colorB }]) => {
    if (sortByStatus) {
      return colorA.localeCompare(colorB);
    } else {
      return  idA - idB
    }
    return 0;
  });

  return (
    <div id="patrolsMenu" className={`${isVisible ? 'patrolsMenuVisible' : 'patrolsMenuHidden'}`}>
      <img onClick={onPatrolsToggle} id="patrolsClose" src={leftarrow} alt="close" />
      <div>
        <span>Sortuj</span>
        <button onClick={()=>setSortByStatus(false)} className={`${!sortByStatus?'checked':''}`}>ID</button>
        <button onClick={()=>setSortByStatus(true)} className={`${sortByStatus?'checked':''}`}>Status</button>
      </div>
      <div id="patrolsList">
      {sortedPatrols.map(([id, { position, color }]) => (
              <div key={id} className="patrol-item" style={{ backgroundColor: color }}>
                {id}
              </div>
            ))}
      </div>
    </div>
  );
}

export default PatrolsMenu;
