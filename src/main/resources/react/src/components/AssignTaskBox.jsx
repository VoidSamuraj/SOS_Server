import React from 'react';
import '../style/assignTaskBox.css'; // Załaduj style dla tego komponentu
import bell from '../icons/bell.svg';

function AssignTaskBox({patrols, reports}) {




  return (
    <div id="assignTaskBox">
      <div id="bell" className={`${reports.size>0?'visible':''}`}>
        <img src={bell} alt="bell" />
      </div>
      <button id="assignTaskButton" type="button">Wybierz zgłoszenie</button>
      <div id="assignTaskMenu">
        <div id="navButtons">
          <button id="assignBack" type="button">Cofnij</button>
          <button id="assignClose" type="button">Zamknij okno</button>
        </div>
        <div id="assignItems"></div>
        <button id="sendPatrolButton" type="button">Wyślij patrol</button>
      </div>
    </div>
  );
}

export default AssignTaskBox;
