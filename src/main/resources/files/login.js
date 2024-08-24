
document.getElementById('showPassword').addEventListener('click', function() {
    var showPassword = document.getElementById('showPassword');
    var password = document.getElementById('password');

            if (password.type === 'password') {
                password.type = 'text';
                showPassword.textContent = 'Ukryj hasło';
            } else {
                password.type = 'password';
                showPassword.textContent = 'Pokaż hasło';
            }
});
document.getElementById('remindPasswordNavButton').addEventListener('click', function() {
    var recoverPasswordForm = document.getElementById('recoverPasswordForm');
    var loginForm = document.getElementById('loginForm');
                loginForm.style.display = 'none';
                recoverPasswordForm.style.display = 'block';

});



