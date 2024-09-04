import React, { useState, useEffect } from "react";
import {
  BrowserRouter as Router,
  Route,
  Routes,
  Navigate,
} from "react-router-dom";
import Login from "./Login";
import Home from "./Home";
import PrivateRoute from "./PrivateRoute";
import loadingIcon from "./icons/loader.svg";

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [checkingAuth, setCheckingAuth] = useState(true);

  function checkTokenExpiration() {
    const expString = localStorage.getItem("exp");
    if (expString != null) {
      const expTime = expString ? parseInt(expString, 10) : null;
      const currentTime = Date.now();

      if (expTime === null) {
        setIsLoggedIn(false);
      } else if (currentTime > expTime) {
        setIsLoggedIn(false);
        localStorage.removeItem('exp');
      } else {
        setIsLoggedIn(true);
      }
    }
    setCheckingAuth(false);
  }
  useEffect(() => {
    checkTokenExpiration();
  }, []);
  if (checkingAuth) {
    return <img className={"loadingCircle"} src={loadingIcon} alt="Loading" />;
  }

  return (
    <Router>
      <Routes>
        <Route path="/" element={<Navigate to="/home" />} />
        <Route path="/login" element={<Login isLoggedIn={isLoggedIn} setIsLoggedIn={setIsLoggedIn} />} />
        //nesting Home in PrivateRoute
        <Route element={<PrivateRoute isLoggedIn={isLoggedIn} />}>
          <Route path="/home" element={<Home />} />
        </Route>
        <Route
          path="*"
          element={<Navigate to={isLoggedIn ? "/home" : "/login"} />}
        />
      </Routes>
    </Router>
  );
}

export default App;
