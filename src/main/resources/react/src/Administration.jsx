import React, { useEffect } from "react";
import ManageAccounts from "./components/ManageAccounts";
import { useNavigate } from "react-router-dom";
import "./administration.css";

export default function Administration({ isLoggedIn, guards}) {
  const navigate = useNavigate();

  useEffect(() => {
    if (!isLoggedIn) {
      navigate("/login");
    }
  }, [isLoggedIn]);

  return (
    <div id="StatsBox">
      <ManageAccounts guards={guards}/>
    </div>
  );
}
