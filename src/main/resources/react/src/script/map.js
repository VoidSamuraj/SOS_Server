
export const carMarkers = {};
export const reportMarkers = [];


async function addCarMarker(position, id, size, color) {
  try{
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

    const marker = new google.maps.marker.AdvancedMarkerElement({
      position: position,
      map: map,
      content: markerElement,
    });

    carMarkers[id] = marker;               } catch (error) {
      console.error("SVG loading error:", error);
    }
  }

  async function removeCarMarker(array,id) {
    if (array[id]) {
      array[id].setMap(null);
      delete array[id];
    }
  }
  async function removeReportMarker(array,id) {
    if (array[id]) {
      array[id].setMap(null);
      delete array[id];
    }
  }
  async function changeMarkerIcon(array, id, newIconUrl) {
    if (array[id]) {
      array[id].setIcon(newIconUrl);
    }
  }
  async function addReportMarker(position, size){
    try {
      const response = await fetch("static/icons/SOSANIM.svg");

      const svgContent = await response.text();

      const parser = new DOMParser();
      const svgElement = parser.parseFromString(svgContent, "image/svg+xml").documentElement;


      const markerElement = document.createElement("div");

      markerElement.appendChild(svgElement);
      markerElement.style.width = size;
      markerElement.style.height = `${parseFloat(size)/2}px`;

      const marker = new google.maps.marker.AdvancedMarkerElement({
        position: position,
        map: map,
        content: markerElement,
      });

      reportMarkers.push(marker);
    } catch (error) {
      console.error("SVG loading error:", error);
    }
  }

  //TEST
function generateRandomReports(){
    if(reportMarkers.length <5){
        var y = Math.floor(Math.random() * (54 - 50)) + 50;
        var x = Math.floor(Math.random() * (24 - 15)) + 15;
         addReportMarker({ lat: y, lng: x }, "200px");
    }
}

const intervalId = setInterval(generateRandomReports, 5000);

window.onload = function() {
    addCarMarker({ lat: 51.5, lng: 19.0 }, "11", "80px","#F00");
    addCarMarker({ lat: 51.6, lng: 21.1 }, "12", "80px","#0F0");
    addCarMarker({ lat: 52.6, lng: 21.5 }, "13", "80px","#0F0");
    addCarMarker({ lat: 50.2, lng: 22.9 }, "4", "80px","#F00");
    addCarMarker({ lat: 53.6, lng: 22.0 }, "5", "80px","#0F0");
    addCarMarker({ lat: 51.6, lng: 22.2 }, "6", "80px","#AAA");

}