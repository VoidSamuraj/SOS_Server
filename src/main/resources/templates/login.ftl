<!DOCTYPE html>
<html lang="en">
   <head>
      <meta charset="UTF-8">
      <meta name="viewport" content="width=device-width, initial-scale=1.0">
      <title>System SOS</title>
      <link rel="stylesheet" href="static/style/login.css">
   </head>
   <body>
       <div>
         <form id="loginForm">
                <label for="login">Login</label>
                <input type="text" id="login" placeholder="Login">
                <label for="password">Hasło</label>
                <input type="password" id="password" placeholder="Hasło">
                <button id="showPassword" class="link-button" type="button">Pokaż hasło</button>
                <input type="submit" id="loginButton" value="Zaloguj się" placeholder="Login">
                <button id="remindPasswordNavButton" class="link-button" type="button">Przypomnij Hasło</button>
          </form>
         <form id="recoverPasswordForm">
                <label for="email">Email</label>
                <input type="email" id="email" placeholder="Email">
                <input type="submit" id="recoverPasswordButton" value="Odzyskaj hasło" placeholder="Recover password">
          </form>
       </div>
       <script type="module" src="static/script/login.js"></script>
   </body>
</html>
