import React, { useState, useEffect } from "react";
import "./style/login.css";
import { useNavigate } from "react-router-dom";

function Login({ isLoggedIn, setIsLoggedIn }) {
  const [passwordVisible, setPasswordVisible] = useState(false);
  const [showRecoverForm, setShowRecoverForm] = useState(false);
  const navigate = useNavigate();

  const togglePasswordVisibility = () => {
    setPasswordVisible(!passwordVisible);
  };

  const register = () => {
    const formData = new URLSearchParams();
    formData.append("login", "exampleLogin");
    formData.append("password", "examplePassword");

    fetch("/employee/register", {
      method: "POST",
      credentials: "include",
      body: formData,
    })
      .then((response) => {
        if (response.ok) {
          console.log(response);
          return response.json();
        } else {
          return response.json().then((errorData) => {
            const errorMessage = errorData.message || "Unknown error";
            throw new Error(`Server error: ${errorMessage}`);
          });
        }
      })
      .then((data) => {
        console.log("Data received:", data);
        if (data.exp) {
          localStorage.setItem("exp", data.exp);
          setIsLoggedIn(true);
        }
      })
      .catch((error) => console.error("Error:", error));
  };
  const login = () => {
    register();
    navigate("/home");
  };

  useEffect(() => {
    if (isLoggedIn) {
      navigate("/home");
    }
  }, [isLoggedIn]);

  return (
    <>
      <div className="formBox">
        {!showRecoverForm ? (
          <form id="loginForm">
            <label htmlFor="login">Login</label>
            <input type="text" id="login" placeholder="Login" />

            <label htmlFor="password">Hasło</label>
            <input
              type={passwordVisible ? "text" : "password"}
              id="password"
              placeholder="Hasło"
            />

            <button
              className="link-button"
              type="button"
              onClick={togglePasswordVisibility}
            >
              Pokaż hasło
            </button>

            <input
              type="submit"
              id="loginButton"
              value="Zaloguj się"
              placeholder="Login"
              onClick={login}
            />

            <button
              className="link-button"
              type="button"
              onClick={() => setShowRecoverForm(true)}
            >
              Przypomnij Hasło
            </button>
          </form>
        ) : (
          <form id="recoverPasswordForm">
            <label htmlFor="email">Email</label>
            <input type="email" id="email" placeholder="Email" />

            <input
              type="submit"
              id="recoverPasswordButton"
              value="Odzyskaj hasło"
              placeholder="Recover password"
            />
            <button
              className="link-button"
              type="button"
              onClick={() => setShowRecoverForm(false)}
            >
              Wróć do logowania
            </button>
          </form>
        )}
      </div>
      <div class="blur-overlay"></div>
    </>
  );
}

export default Login;
