



         const markers = {};


        async function addCustomMarker(position, id, size, color) {

            const svgContent = `
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" fill=${color} viewBox="0 0 24 24">
              <path d="m20.772 10.155-1.368-4.104A2.995 2.995 0 0 0 16.559 4H7.441a2.995 2.995 0 0 0-2.845 2.051l-1.368 4.104A2 2 0 0 0 2 12v5c0 .738.404 1.376 1 1.723V21a1 1 0 0 0 1 1h1a1 1 0 0 0 1-1v-2h12v2a1 1 0 0 0 1 1h1a1 1 0 0 0 1-1v-2.277A1.99 1.99 0 0 0 22 17v-5a2 2 0 0 0-1.228-1.845zM7.441 6h9.117c.431 0 .813.274.949.684L18.613 10H5.387l1.105-3.316A1 1 0 0 1 7.441 6zM5.5 16a1.5 1.5 0 1 1 .001-3.001A1.5 1.5 0 0 1 5.5 16zm13 0a1.5 1.5 0 1 1 .001-3.001A1.5 1.5 0 0 1 18.5 16z"/>
              <path fill="#000" d="M7.441 6h9.117c.431 0 .813.274.949.684L18.613 10H5.387l1.105-3.316A1 1 0 0 1 7.441 6z"/>
              <path fill="#fefdde" d="M5.5 16a1.5 1.5 0 1 1 .001-3.001A1.5 1.5 0 0 1 5.5 16z"/>
              <path fill="#fefdde" d="M18.5 16a1.5 1.5 0 1 1 .001-3.001A1.5 1.5 0 0 1 18.5 16z"/>

                <text x="12" y="16" text-anchor="middle" font-size="6" font-weight="bold" fill="#FFFFFF">${id}</text>
            </svg>
                `;


            const markerElement = document.createElement("div");

            markerElement.innerHTML = svgContent;
            markerElement.style.width = size;
            markerElement.style.height = size;
            markerElement.style.position = "relative";

            const marker = new google.maps.marker.AdvancedMarkerElement({
                position: position,
                map: map,
                content: markerElement,
            });

            markers[id] = marker;
        }

        async function removeMarker(id) {
             if (markers[id]) {
                 markers[id].setMap(null);  // Usunięcie markera z mapy
                 delete markers[id];  // Usunięcie markera z obiektu
             }
         }

        async function changeMarkerIcon(id, newIconUrl) {
             if (markers[id]) {
                 markers[id].setIcon(newIconUrl);  // Zmiana ikony markera
             }
         }

document.getElementById('marker').addEventListener('click', function() {
  addCustomMarker({ lat: 51.5, lng: 19.0 }, "11", "80px","#F00");
  addCustomMarker({ lat: 51.6, lng: 19.1 }, "12", "80px","#0F0");
});