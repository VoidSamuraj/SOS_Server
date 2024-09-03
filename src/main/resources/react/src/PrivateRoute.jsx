import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';

//check if user is logged
const PrivateRoute = ({ isLoggedIn }) => {
  return isLoggedIn ? <Outlet /> : <Navigate to="/login" />;
};

export default PrivateRoute;
