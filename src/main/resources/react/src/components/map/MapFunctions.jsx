import React, { useState } from "react";

export const usePatrols = () => {
  const [patrols, setPatrols] = useState(new Map());
  const statusToColor = (status) => {
    switch (status) {
      case 0:
        return "#00ff00";
      case 2:
        return "#ff0000";
      default:
        return "#cccccc";
    }
  };
  const statusToCarColor = (status) => {
    switch (status) {
      case 0:
        return "#0a5c0a";
      case 2:
        return "#ff0000";
      default:
        return "#949494";
    }
  };
  const addPatrol = (id, position, status, name, surname, phone) => {
    setPatrols((prevPatrols) =>
      new Map(prevPatrols).set(id, { position, status, name, surname, phone })
    );
  };

  // TODO check if work properly with position format, may require process newPosition like in syncReports
  const editPatrol = (id, newStatus = null, newPosition = null) => {
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

  const syncPatrols = (dataArray) => {
    setPatrols((prevPatrols) => {
      const updatedPatrols = new Map(prevPatrols);
      dataArray.forEach((patrol) => {
        let location = "";
        try {
          location =
            JSON.parse(patrol.location.replace(/(\w+):/g, '"$1":')) || "";
        } catch (error) {
          console.error("Sync Reports, JSON Location parsing error:", error);
        }
        updatedPatrols.set(patrol.id, {
          position: location,
          status: patrol.statusCode !== null ? patrol.statusCode : 1,
          name: patrol.name,
          surname: patrol.surname,
          phone: patrol.phone,
          account_deleted: patrol.account_deleted,
        });
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
    statusToColor,
    statusToCarColor,
    setPatrols,
    addPatrol,
    editPatrol,
    syncPatrols,
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

  const syncReports = (dataArray) => {
    setReports((prevReports) => {
      const updatedReports = new Map(prevReports);
      // Iterate over the new reports and update or add them
      dataArray.forEach((report) => {
        let location = "";
        try {
          location =
            JSON.parse(report.location.replace(/(\w+):/g, '"$1":')) || "";
        } catch (error) {
          console.error("Sync Reports, JSON Location parsing error:", error);
        }

        updatedReports.set(report.id, {
          position: location,
          date: report.date || new Date().toISOString(),
          status: report.statusCode || 0,
        });
      });

      return updatedReports;
    });
  };

  const removeReport = (id) => {
    const newReports = new Map(reports);
    newReports.delete(id);
    setReports(newReports);
  };

  return {
    reports,
    setReports,
    addReport,
    editReport,
    syncReports,
    removeReport,
  };
};
