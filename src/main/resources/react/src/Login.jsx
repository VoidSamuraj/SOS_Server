import React, { useState } from 'react';
import './style/login.css'; // Załaduj style dla tego komponentu

function Login() {
  const [passwordVisible, setPasswordVisible] = useState(false);
  const [showRecoverForm, setShowRecoverForm] = useState(false);

  const togglePasswordVisibility = () => {
    setPasswordVisible(!passwordVisible);
  };
  return (
    <>
    {!showRecoverForm?(
      <form id="loginForm">
        <label htmlFor="login">Login</label>
        <input type="text" id="login" placeholder="Login" />

        <label htmlFor="password">Hasło</label>
        <input
          type={passwordVisible ? 'text' : 'password'}
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
        />

        <button
          className="link-button"
          type="button"
          onClick={() => setShowRecoverForm(true)}
        >
          Przypomnij Hasło
        </button>
      </form>
     ):(
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
    </>
  );
}

export default Login;