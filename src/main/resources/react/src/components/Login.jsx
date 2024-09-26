import React, { useState } from "react";
import {login, remindPassword} from "../script/ApiService.js"


/**
 * Login component allows users to log in or recover their password.
 *
 * This component provides a form for user login and an option to recover the password.
 * It maintains the visibility of the password field and handles the submission of both login
 * and recovery forms.
 *
 * @returns {JSX.Element} The rendered component.
 */
function Login() {
  const [passwordVisible, setPasswordVisible] = useState(false);
  const [showRecoverForm, setShowRecoverForm] = useState(false);
  const [loginValue, setLoginValue] = useState("");
  const [password, setPassword] = useState("");
  const [email, setEmail] = useState("");

  const togglePasswordVisibility = () => {
    setPasswordVisible(!passwordVisible);
  };

  const handleLogin = (event) => {
    event.preventDefault();
    login(loginValue, password, ()=>{
         window.location.href = "/map";
    });
  };
  const handleRecoverPassword = (event) => {
    event.preventDefault();
    remindPassword(email);
  };

  return (
    <>
      <div className="formBox">
        {!showRecoverForm ? (
          <form id="loginForm" onSubmit={handleLogin}>
            <label htmlFor="login">Login</label>
            <input
              type="text"
              id="login"
              placeholder="Login"
              value={loginValue}
              onChange={(event) => setLoginValue(event.target.value)}
            />

            <label htmlFor="password">Hasło</label>
            <input
              type={passwordVisible ? "text" : "password"}
              id="password"
              placeholder="Hasło"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
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
          <form id="recoverPasswordForm" onSubmit={handleRecoverPassword}>
            <label htmlFor="email">Email</label>
            <input
              type="email"
              id="email"
              placeholder="Email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
            />

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
