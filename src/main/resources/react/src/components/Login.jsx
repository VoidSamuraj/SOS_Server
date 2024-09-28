import React, { useState } from "react";
import { login, remindPassword } from "../script/ApiService.js";
import SystemAlert from "./SystemAlert";

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

  const [loginError, setLoginError] = useState("");
  const [passwordError, setPasswordError] = useState("");
  const [emailError, setEmailError] = useState("");

  const [alertMessage, setAlertMessage] = useState("");
  const [alertType, setAlertType] = useState("info");

  const togglePasswordVisibility = () => {
    setPasswordVisible(!passwordVisible);
  };

  const handleLogin = (event) => {
    event.preventDefault();

    let isValid = true;

    if (loginValue.trim() === "") {
      setLoginError("Login jest wymagany.");
      isValid = false;
    } else {
      setLoginError("");
    }

    if (password.trim() === "") {
      setPasswordError("Hasło jest wymagane.");
      isValid = false;
    } else {
      setPasswordError("");
    }

    if (isValid) {
      login(
        loginValue,
        password,
        () => {
          window.location.href = "/map";
        },
        (response) => {
          if (response.status == 401)
            setLoginError("Podano błędny login lub hasło.");
          else {
            setAlertType("error");
            setAlertMessage(
              "Wystąpił nieznany błąd. Spróbuj ponownie później."
            );
          }
        }
      );
    }
  };
  const handleRecoverPassword = (event) => {
    event.preventDefault();

    if (email.trim() === "") {
      setEmailError("Email jest wymagany.");
    } else if (!/\S+@\S+\.\S+/.test(email)) {
      setEmailError("Proszę podaj poprawny email.");
    } else {
      setEmailError("");
      remindPassword(
        email,
        () => {
          setAlertType("success");
          setAlertMessage(
            "Wiadomość z linkiem do przywrócenia hasła została wysłana na podany adres e-mail."
          );
        },
        () => {
          setEmailError("Nie znaleziono konta z takim adresem email.");
        }
      );
    }
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
              onKeyDown={(event) => {
                if (event.key === "Enter") {
                  handleLogin(event);
                }
              }}
            />
            {loginError && <div className="error">{loginError}</div>}

            <label htmlFor="password">Hasło</label>
            <input
              type={passwordVisible ? "text" : "password"}
              id="password"
              placeholder="Hasło"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              onKeyDown={(event) => {
                if (event.key === "Enter") {
                  handleLogin(event);
                }
              }}
            />
            {passwordError && <div className="error">{passwordError}</div>}

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
              onKeyDown={(event) => {
                if (event.key === "Enter") {
                  handleRecoverPassword(event);
                }
              }}
            />
            {emailError && <div className="error">{emailError}</div>}

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
      {alertMessage && (
        <SystemAlert
          severity={alertType}
          message={alertMessage}
          onClose={() => {
            setAlertMessage("");
            if (alertType == "success") window.location.href = "/login";
          }}
        />
      )}
      <div class="blur-overlay"></div>
    </>
  );
}

export default Login;
