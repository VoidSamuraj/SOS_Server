document.getElementById('barDropdown').addEventListener('click', function() {
    var menu = document.getElementById('dropdownMenu');
    var menuSettings = document.getElementById('settingsMenu');

    if (menuSettings.style.right === '150px') {
        menuSettings.style.right = '-100%'; // Ukrywa menu
    }

    if (menu.style.top === '62px') {
        menu.style.top = '-100%'; // Ukrywa menu
    } else {
        menu.style.top = '62px'; // Pokazuje menu
    }
});
document.getElementById('settingsButton').addEventListener('click', function() {
    var menu = document.getElementById('settingsMenu');

    if (menu.style.right === '150px') {
        menu.style.right = '-100%'; // Ukrywa menu
    } else {
        menu.style.right = '150px'; // Pokazuje menu
    }
});

document.getElementById('statsButton').addEventListener('click', function() {
  var menu = document.getElementById('statsOverlay');
  menu.style.visibility = 'visible';
});
document.getElementById('settingsClose').addEventListener('click', function() {
    var menu = document.getElementById('settingsMenu');
    menu.style.right = '-100%';
});

document.getElementById('patrolsButton').addEventListener('click', function() {
  var menu = document.getElementById('patrolsMenu');
  menu.style.left = '0';
});
document.getElementById('patrolsClose').addEventListener('click', function() {
  var menu = document.getElementById('patrolsMenu');
  menu.style.left = '-100%';
});

document.getElementById('statsClose').addEventListener('click', function() {
  var menu = document.getElementById('statsOverlay');
  menu.style.visibility = 'hidden';
});
