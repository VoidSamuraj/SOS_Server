import React, { useState, useEffect } from "react";
import { Alert, AlertTitle } from "@mui/material";

/**
 * A functional component that displays an alert message with a title and a severity level.
 * The alert hides automatically after a certain amount of time.
 *
 * @param {string} severity - The severity level of the alert ('error', 'warning', 'info', 'success').
 * @param {string} title - The title of the alert.
 * @param {string} message - The message to be displayed in the alert.
 * @param {number} autoHideDuration - The duration (in milliseconds) after which the alert will disappear.
 * @param {function} onClose - Callback to be executed when the alert is closed.
 *
 * @returns {JSX.Element} A dismissible alert with a title and message.
 */
function SystemAlert({
  severity,
  title,
  message,
  autoHideDuration = 3000,
  onClose,
}) {
  const [visible, setVisible] = useState(true);

  const severityStringMap = {
    error: "Wystąpił błąd!",
    warning: "Uwaga!",
    info: "Informacja",
    success: "Sukces!",
  };

  useEffect(() => {
    const timer = setTimeout(() => {
      setVisible(false);
      if (onClose) {
        onClose(); // Call the onClose callback if provided
      }
    }, autoHideDuration);

    return () => clearTimeout(timer); // Cleanup timer on component unmount
  }, [autoHideDuration, onClose]);

  if (!visible) return null;

  return (
    <Alert
      severity={severity}
      onClose={onClose}
      style={{
        position: "fixed",
        zIndex: 1301,
        width: "400px",
        top: "10%",
        left: "50%",
        transform: "translate(-50%, -50%)",
      }}
    >
      <AlertTitle>
        {title || severityStringMap[severity] || "Informacja"}
      </AlertTitle>
      {message}
    </Alert>
  );
}

export default SystemAlert;
