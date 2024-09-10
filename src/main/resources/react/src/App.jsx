import React, { useState, useEffect } from "react";
import {
  BrowserRouter as Router,
  Route,
  Routes,
  Navigate,
} from "react-router-dom";
import Login from "./Login";
import Home from "./Home";
import Administration from "./Administration";
import PrivateRoute from "./PrivateRoute";
import loadingIcon from "./icons/loader.svg";
import {getAllGuards} from "./script/ApiService.js";
import {usePatrols} from "./components/map/MapFunctions.jsx";

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [checkingAuth, setCheckingAuth] = useState(true);

  const {patrols, setPatrols, updatePatrol,  convertArrayToPatrolMap} =usePatrols();

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
    getAllGuards()
    .then(data => {
            setPatrols(convertArrayToPatrolMap(data));
        })
  }, []);
/*
    const [data, setData] = useState([]);
     useEffect(() => {
         const socket = new WebSocket('ws://localhost:8080/updates');

         socket.onopen = () => {
             console.log('WebSocket connection established');
         };

         socket.onmessage = (event) => {
             console.log('Received data:', event.data);
             const newData = event.data.split(', ').map(item => item.trim());
             setData(newData);
         };

         socket.onerror = (error) => {
             console.error('WebSocket error:', error);
         };

socket.onclose = (event) => {
        console.log('Closed websocket');
};

         return () => {
             socket.close();
         };
     }, []);
*/

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
          <Route path="/home" element={<Home  onLogout={()=>setIsLoggedIn(false) } patrols={patrols} updatePatrol={updatePatrol}/>} />
        </Route>
        <Route path="/administration" element={<Administration isLoggedIn={isLoggedIn} guards={patrols} />}/>
        <Route
          path="*"
          element={<Navigate to={isLoggedIn ? "/home" : "/login"} />}
        />
      </Routes>
    </Router>
  );
}

export default App;
