<!DOCTYPE html>
<html lang="en">
   <head>
      <meta charset="UTF-8">
      <meta name="viewport" content="width=device-width, initial-scale=1.0">
      <title>System SOS</title>
      <link rel="stylesheet" href="static/style/style.css">
      <link rel="stylesheet" href="static/style/map.css">
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

    <!--
     <gmp-map id="map" center="51.250614166259766,22.57177734375" zoom="14" disableDefaultUI="true" map-id="818dbbf76a918381">
       <gmp-advanced-marker position="51.250614166259766,22.57177734375" title="My location"></gmp-advanced-marker>
     </gmp-map>
     -->
<div id="map"></div>
<button id="marker" style="z-index: 5; position: absolute;">ADD MARKER</button>


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
       <script type="module" src="static/script/navigation.js"></script>
       <script type="module" src="static/script/initMap.js"></script>
        <script>
            ({key: "${google_api_key}"});
        </script>
       <script type="module" src="static/script/map.js"></script>
       <script>
       let map;

       window.initMap = async function() {
         // The location of Uluru
         const position = { lat: 51.9189046, lng: 19.1343786 };
         // Request needed libraries.
         //@ts-ignore
         const { Map } = await google.maps.importLibrary("maps");
         const { AdvancedMarkerElement,PinElement } = await google.maps.importLibrary("marker");

         // The map, centered at Uluru
         map = new Map(document.getElementById("map"), {
           zoom: 7.5,
           center: position,
           mapId: "${map_id}",
         });
/*
         // The marker, positioned at Poland
         const marker = new AdvancedMarkerElement({
           map: map,
           position: position,
           title: "Polska",
         })*/
         };

       </script>
    <script async src="https://maps.googleapis.com/maps/api/js?key=${google_api_key}&callback=initMap&libraries=maps,marker&v=beta"></script>
    <script></script>
   </body>
</html>
