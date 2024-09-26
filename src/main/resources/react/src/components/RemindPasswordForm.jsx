import React, { useState, useEffect } from 'react';
import {resetPassword} from "../script/ApiService.js";

/**
 * RemindPasswordForm component handles the password reset functionality.
 *
 * This component allows the user to enter a new password and confirm it.
 * It retrieves the token from the URL to send it along with the password for validation.
 *
 * @returns {JSX.Element} The rendered password reset form.
 */
const RemindPasswordForm = () => {
  const [password, setPassword] = useState('');
  const [passwordRepeat, setPasswordRepeat] = useState('');
  const [token, setToken] = useState('');


  useEffect(() => {
    const urlParams = new URLSearchParams(window.location.search);
    const tokenFromUrl = urlParams.get('token');
    if (tokenFromUrl) {
        console.log(tokenFromUrl)
      setToken(tokenFromUrl);
    }
  }, []);

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (password !== passwordRepeat) {
      alert("Hasła muszą być takie same!");
      return;
    }

    const passwordRequirements = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;
    if (!passwordRequirements.test(password)) {
      alert("Hasło musi zawierać co najmniej 8 znaków, jedną wielką literę, jedną cyfrę i jeden znak specjalny.");
      return;
    }

    resetPassword(token, password);

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
        />
        <label htmlFor="passwordRepeat">Powtórz Hasło</label>
        <input
          type="password"
          id="passwordRepeat"
          name="passwordRepeat"
          placeholder="Powtórz Hasło"
          required
          value={passwordRepeat}
          onChange={(e) => setPasswordRepeat(e.target.value)}
        />
        <input id="recoverPasswordButton" type="submit" value="Zapisz hasło" />
      </form>
    </div>
  );
};

export default RemindPasswordForm;
