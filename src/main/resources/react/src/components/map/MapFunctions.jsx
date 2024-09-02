import { useState } from 'react';

export const usePatrols = () => {
  const [patrols, setPatrols] = useState(new Map());

  const addPatrol = (id, position, color) => {
    setPatrols(prevPatrols => new Map(prevPatrols).set(id, { position, color }));
  };

  const removePatrol = (id) => {
    const newPatrols = new Map(patrols);
    newPatrols.delete(id);
    setPatrols(newPatrols);
  };

  const removeFirstPatrol = () => {
    if (patrols.size > 0) {
      const newPatrols = new Map(patrols);
      const firstKey = newPatrols.keys().next().value;
      newPatrols.delete(firstKey);
      setPatrols(newPatrols);
    }
  };

  return { patrols, setPatrols, addPatrol, removePatrol, removeFirstPatrol };
};

export const useReports = () => {
  const [reports, setReports] = useState(new Map());

  const addReport = (id, position) => {
    setReports(prevReports => new Map(prevReports).set(id, position));
  };

  const removeReport = (id) => {
    const newReports = new Map(reports);
    newReports.delete(id);
    setReports(newReports);
  };

  const removeFirstReport = () => {
    if (reports.size > 0) {
      const newReports = new Map(reports);
      const firstKey = newReports.keys().next().value;
      newReports.delete(firstKey);
      setReports(newReports);
    }
  };

  return { reports, setReports, addReport, removeReport, removeFirstReport };
};
