import React, { useState, useEffect } from 'react';

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

  return (
    <div className="formBox">
      <form >
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
