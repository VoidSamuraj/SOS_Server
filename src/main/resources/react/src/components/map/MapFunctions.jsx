import { useState } from "react";

export const usePatrols = () => {
  const [patrols, setPatrols] = useState(new Map());

  const addPatrol = (id, position, color) => {
    setPatrols((prevPatrols) =>
      new Map(prevPatrols).set(id, { position, color })
    );
  };

  const updatePatrol = (id, newColor = null, newPosition = null) => {
    setPatrols((prevPatrols) => {
      const updatedPatrols = new Map(prevPatrols);

      if (!updatedPatrols.has(id)) return prevPatrols;

      const currentPatrol = updatedPatrols.get(id);

      updatedPatrols.set(id, {
        position: newPosition !== null ? newPosition : currentPatrol.position,
        color: newColor !== null ? newColor : currentPatrol.color,
      });

      return updatedPatrols;
    });
  };

  const removePatrol = (id) => {
    const newPatrols = new Map(patrols);
    newPatrols.delete(id);
    setPatrols(newPatrols);
  };

  return { patrols, setPatrols, addPatrol, updatePatrol, removePatrol };
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
