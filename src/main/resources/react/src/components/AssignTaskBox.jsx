import React, {useState, useEffect } from 'react';
import '../style/assignTaskBox.css'; // Załaduj style dla tego komponentu
import bell from '../icons/bell.svg';

function AssignTaskBox({patrols, reports}) {


    const [hideBell, setHideBell] = useState(false);
    const [selectedReport, setSelectedReport] = useState(null);
    const [selectedPatrol, setSelectedPatrol] = useState(null);
    const [nrOfMenu, setNrOfMenu] = useState(1);
  const [isButtonDisabled, setIsButtonDisabled] = useState(true);

    function onBack(){
        if(nrOfMenu>=2)
            setNrOfMenu(nrOfMenu-1);
    }
    function proceed(){
        if(!isButtonDisabled && nrOfMenu<=2)
            setNrOfMenu(nrOfMenu+1);
    }
      const updateButtonState = () => {
        const newButtonState = (nrOfMenu === 1 && selectedReport === null) ||
                                (nrOfMenu === 2 && selectedPatrol === null);
        setIsButtonDisabled(newButtonState);
      };

  useEffect(() => {
    updateButtonState();
  }, [nrOfMenu, selectedReport, selectedPatrol]);

    const sortedPatrols = Array.from(patrols.entries()).sort(([, { color: colorA }], [, { color: colorB }]) => {return colorA.localeCompare(colorB);});

    const sortedReports = Array.from(reports.entries()).sort(([, { date: dateA }], [, { date: dateB }]) => {
        const dateObjA = new Date(dateA);
        const dateObjB = new Date(dateB);
        return dateObjA - dateObjB;
    });


  return (
    <div id="assignTaskBox">
      <div id="bell" onClick={()=>setHideBell(true)} className={`${(reports.size>0 && !hideBell) ?'visible':''}`}>
        <img src={bell} alt="bell" />
      </div>
      <div id="assignTaskMenu"  className={`${hideBell?'visible':''}`}>
        <div className="navButtons">
          <button id="assignBack" type="button"  onClick={onBack} className={`${(nrOfMenu>1) ?'visible':''}`}>Cofnij</button>
          <button id="assignClose"  onClick={()=>setHideBell(false)} type="button" >Zamknij okno</button>
        </div>
        <div id="assignItems">
            {nrOfMenu == 1 ? (
               sortedReports.map(([id, { position, date, status }]) => (
                 <div key={id} onClick={() => setSelectedReport(id)} className={`${(id == selectedReport) ?'selected':''}`}>
                   {id}
                 </div>
               ))
             ) : nrOfMenu == 2 ? (
               sortedPatrols.map(([id, { position, color }]) => (
                 <div key={id} onClick={() => setSelectedPatrol(id)} className={`${(id == selectedPatrol) ?'selected':''}`} style={{ backgroundColor: color }}>
                   {id}
                 </div>
               ))
             ) : (<>
                    <div>
                        {selectedReport}
                   </div>
                   <div style={{ backgroundColor: "#0F0" }}>
                       {selectedPatrol}
                   </div>
                   </>
             )
            }
            </div>
         <div className="navButtons">
        <button id="sendPatrolButton" type="button" onClick={proceed}  className={`my-button ${isButtonDisabled ? 'disabled' : ''}`}>
            {(nrOfMenu == 1) ?"Wybierz Zgłoszenie":((nrOfMenu == 2) ? "Wybierz patrol" : "Wyślij patrol")}
            </button>
        </div>
      </div>
    </div>
  );
}

export default AssignTaskBox;
