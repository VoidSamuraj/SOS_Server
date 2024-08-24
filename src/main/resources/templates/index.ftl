<!DOCTYPE html>
<html lang="en">
   <head>
      <meta charset="UTF-8">
      <meta name="viewport" content="width=device-width, initial-scale=1.0">
      <title>System SOS</title>
      <link rel="stylesheet" href="static/style.css">
   </head>
   <body>
     <div id="topBar">
       <div id="infoBox">
         <div>Patrole</div>
         <div>
           Dostępne:2
           Interwencja:2
           Niedostępne:2
         </div>
       </div>
       <img id=barDropdown src="static/icons/menu.svg" alt="menu">
     </div>
     <div id="dropdownMenu">
        <button id="statsButton" class="icon-button">
           <img src="static/icons/bar-chart.svg" alt="stats">
           Statystyki
        </button>
        <button id="settingsButton" class="icon-button">
             <img src="static/icons/cog.svg" alt="settings">
            Opcje
        </button>
        <button class="icon-button">
             <img src="static/icons/exit.svg" alt="logout">
            Wyloguj się
        </button>
     </div>
     <div id="settingsMenu">
        <img id="settingsClose" src="static/icons/right-arrow.svg" alt="close">
        <div class="switch">
          <input id="toggle" class="toggle" type="checkbox" role="switch" name="toggle">
          <span class="slider">Ciemny tryb</span>
        </div>
        <div class="slider-container">
            <label for="mySlider">Kontrast</label><br/>
            <input type="range" id="mySlider" min="0" max="100" value="0">
        </div>
     </div>
     <div id="patrolsButton">
       <img src="static/icons/car.svg" alt="patrols">
     </div>
     <div id="patrolsMenu">
       <img id="patrolsClose" src="static/icons/left-arrow.svg" alt="close">
       <div>
         <span>Sortuj</span>
         <button>Dystans</button>
         <button>Status</button>
       </div>
       <div id="patrolsList">

       </div>
     </div>
     <div id="statsOverlay">
       <div>
         <img id="statsClose" src="static/icons/x.svg" alt="close">
         <div id="statsLegend">
           Legenda
           <p>
            Czerwone okręgi przedstawiają częstotliwości interwencji w danym obszarze.
            Im większy punkt tym więcej wezwań zanotowano z tego rejonu.
           </p>
         </div>
       </div>
     </div>
       <script type="module" src="static/navigation.js"></script>
   </body>
</html>
