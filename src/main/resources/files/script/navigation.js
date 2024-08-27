document.getElementById('barDropdown').addEventListener('click', function() {
  var menu = document.getElementById('dropdownMenu');
  var menuSettings = document.getElementById('settingsMenu');

  if (menuSettings.style.right === '150px') {
    menuSettings.style.right = '-100%';
  }

  if (menu.style.top === '62px') {
    menu.style.top = '-100%';
  } else {
    menu.style.top = '62px';
  }
});
document.getElementById('settingsButton').addEventListener('click', function() {
  var menu = document.getElementById('settingsMenu');

  if (menu.style.right === '150px') {
    menu.style.right = '-100%';
  } else {
    menu.style.right = '150px';
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
document.getElementById('statsClose').addEventListener('click', function() {
  var menu = document.getElementById('statsOverlay');
  menu.style.visibility = 'hidden';
});



var assignTaskButton = document.getElementById('assignTaskButton');
var assignTaskMenu = document.getElementById('assignTaskMenu');
var bell = document.getElementById('bell');
var assignBack = document.getElementById('assignBack');
var assignClose = document.getElementById('assignClose');
var assignItems = document.getElementById('assignItems');
var sendPatrolButton = document.getElementById('sendPatrolButton');

var selectedCall=null;
var selectedPatrol=2;

bell.addEventListener('click', clickOnBell);
assignTaskButton.addEventListener('click', chooseCall);
assignClose.addEventListener('click', function() {  assignTaskMenu.style.display = 'none';});

function clickOnBell(){
  bell.style.display = 'none';
  assignTaskMenu.style.display = 'none';
  sendPatrolButton.style.display = 'none';
  assignTaskButton.style.display = 'block';

}
function chooseCall(){
  if(assignTaskMenu.style.display == 'none'){
    assignTaskMenu.style.display = 'flex';
    //add loading proper data in assignItems
    if(selectedCall != null){
      assignTaskButton.innerText="Wybierz patrol"
      assignBack.style.display = 'block';
    }else{
      assignTaskButton.innerText="Wybierz zgłoszenie"
      assignBack.style.display = 'none';
    }
    assignTaskButton.style.display = 'block';
  }else{
    assignTaskMenu.style.display = 'none';

  }
}
