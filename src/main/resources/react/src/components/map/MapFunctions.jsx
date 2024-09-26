import React, { useState } from "react";

export const usePatrols = () => {
  const [patrols, setPatrols] = useState(new Map());
  const statusToCode = (status) => {
    switch (status) {
      case 0:
        return "#00ff00";
      case 2:
        return "#ff0000";
      default:
        return "#cccccc";
    }
  };
  const addPatrol = (id, position, status, name, surname, phone) => {
    setPatrols((prevPatrols) =>
      new Map(prevPatrols).set(id, { position, status, name, surname, phone })
    );
  };

  const updatePatrol = (id, newStatus = null, newPosition = null) => {
    setPatrols((prevPatrols) => {
      const updatedPatrols = new Map(prevPatrols);

      if (!updatedPatrols.has(id)) return prevPatrols;

      const currentPatrol = updatedPatrols.get(id);

      updatedPatrols.set(id, {
        position: newPosition !== null ? newPosition : currentPatrol.position,
        status: newStatus !== null ? newStatus : currentPatrol.status,
        name: currentPatrol.name,
        surname: currentPatrol.surname,
        phone: currentPatrol.phone,
        account_deleted: currentPatrol.account_deleted,
      });

      return updatedPatrols;
    });
  };

  const removePatrol = (id) => {
    const newPatrols = new Map(patrols);
    newPatrols.delete(id);
    setPatrols(newPatrols);
  };

  const convertArrayToPatrolMap = (dataArray) => {
    return new Map(
      (dataArray || []).map((data) => [
        data.id,
        {
          position: data.location || "unknown",
          status: data.statusCode,
          name: data.name,
          surname: data.surname,
          phone: data.phone,
          account_deleted: data.account_deleted,
        },
      ])
    );
  };

  return {
    patrols,
    statusToCode,
    setPatrols,
    addPatrol,
    updatePatrol,
    removePatrol,
    convertArrayToPatrolMap,
  };
};

export const useReports = () => {
  const [reports, setReports] = useState(new Map());

  const addReport = (id, position, date, status) => {
    setReports((prevReports) =>
      new Map(prevReports).set(id, { position, date, status })
    );
  };

  const editReport = (id, position = null, date = null, status = null) => {
    setReports((prevReports) => {
      const updatedReports = new Map(prevReports);

      if (!updatedReports.has(id)) {
        return prevReports;
      }

      const currentReport = updatedReports.get(id);

      updatedReports.set(id, {
        position: position !== null ? position : currentReport.position,
        date: date !== null ? date : currentReport.date,
        status: status !== null ? status : currentReport.status,
      });

      return updatedReports;
    });
  };

  const removeReport = (id) => {
    const newReports = new Map(reports);
    newReports.delete(id);
    setReports(newReports);
  };

  return { reports, setReports, addReport, editReport, removeReport };
};
