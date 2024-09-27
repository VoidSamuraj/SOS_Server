import React, { useState, useEffect } from "react";
import { resetPassword } from "../script/ApiService.js";

/**
 * RemindPasswordForm component handles the password reset functionality.
 *
 * This component allows the user to enter a new password and confirm it.
 * It retrieves the token from the URL to send it along with the password for validation.
 *
 * @returns {JSX.Element} The rendered password reset form.
 */
const ResetPassword = () => {
  const [password, setPassword] = useState("");
  const [passwordRepeat, setPasswordRepeat] = useState("");
  const [token, setToken] = useState("");

  const [passwordError, setPasswordError] = useState("");
  const [passwordError2, setPasswordError2] = useState("");

  useEffect(() => {
    const urlParams = new URLSearchParams(window.location.search);
    const tokenFromUrl = urlParams.get("token");
    if (tokenFromUrl) {
      console.log(tokenFromUrl);
      setToken(tokenFromUrl);
    }
  }, []);

  const handleSubmit = async (event) => {
    event.preventDefault();

    let isValid = true;

    if (password.trim() === "") {
      setPasswordError("Hasło jest wymagane.");
      isValid = false;
    } else {
      setPasswordError("");
    }

    if (passwordRepeat.trim() === "") {
      setPasswordError2("Hasło jest wymagane.");
      isValid = false;
    } else {
      setPasswordError2("");
    }

    if (password !== passwordRepeat) {
      setPasswordError2("Hasła muszą być takie same!");
      isValid = false;
    } else {
      setPasswordError2("");
    }

    const passwordRequirements =
      /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;
    if (!passwordRequirements.test(password)) {
      setPasswordError(
        "Hasło musi zawierać co najmniej 8 znaków, jedną wielką literę, jedną cyfrę i jeden znak specjalny."
      );
      isValid = false;
    } else {
      setPasswordError("");
    }
    if (isValid) resetPassword(token, password);
  };

  const handleKeyDown = (event) => {
    if (event.key === "Enter") {
      handleSubmit(event);
    }
  };

  return (
    <div className="formBox">
      <form onSubmit={handleSubmit}>
        <input type="hidden" name="token" value={token} />
        <label htmlFor="password">Hasło</label>
        <input
          type="password"
          id="password"
          name="password"
          placeholder="Hasło"
          required
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          onKeyDown={handleKeyDown}
        />
        {passwordError && <div className="error">{passwordError}</div>}
        <label htmlFor="passwordRepeat">Powtórz Hasło</label>
        <input
          type="password"
          id="passwordRepeat"
          name="passwordRepeat"
          placeholder="Powtórz Hasło"
          required
          value={passwordRepeat}
          onChange={(e) => setPasswordRepeat(e.target.value)}
          onKeyDown={handleKeyDown}
        />
        {passwordError2 && <div className="error">{passwordError2}</div>}
        <input id="recoverPasswordButton" type="submit" value="Zapisz hasło" />
      </form>
    </div>
  );
};

export default ResetPassword;
