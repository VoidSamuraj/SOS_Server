import { useState, useEffect } from 'react';
import '../style/map.css'; // Załaduj style dla tego komponentu
import config from '../config';
import {APIProvider, Map as GoogleMap, MapCameraChangedEvent, AdvancedMarker} from '@vis.gl/react-google-maps';
import reportImage from '../icons/SOSANIM.svg';



function MyMap() {
  const [cars, setCars] = useState(new Map());
  const [reports, setReports] = useState(new Map());

  //Test
  useEffect(() => {
    const initialize = () => {
      addCar(11,{ lat: 51.5, lng: 19.0 },"#F00");
      addCar(12,{ lat: 51.6, lng: 21.1 },"#0F0");
      addCar(13,{ lat: 52.6, lng: 21.5 },"#0F0");
      addCar(14,{ lat: 50.2, lng: 22.9 },"#0F0");
      addCar(5,{ lat: 53.6, lng: 22.0 },"#F00");
      addCar(6,{ lat: 51.6, lng: 22.2 },"#AAA");
    };

    initialize();

  }, []);

function generateRandomReports(){
    console.log("report gen")
    if(reports.size <5){
        var y = Math.floor(Math.random() * (54 - 50)) + 50;
        var x = Math.floor(Math.random() * (24 - 15)) + 15;
         addReport(reports.size ,{ lat: y, lng: x });
    }
}

useEffect(() => {
    const intervalId = setInterval(generateRandomReports, 5000);

    return () => {
      clearInterval(intervalId);
    };
  }, [reports]);







  const addCar = (id, position, color) => {
    setCars(prevCars => new Map(prevCars).set(id, { position, color }));
  };

  const removeFirstCar = () => {
    if (cars.size > 0) {
      const newCars = new Map(cars);
      const firstKey = newCars.keys().next().value;
      newCars.delete(firstKey);
      setCars(newCars);
    }
  };

  const removeCar = (id) => {
    const newCars = new Map(cars);
    newCars.delete(id);
    setCars(newCars);
  };


const addReport = (id, position) => {
    setReports(prevReports => new Map(prevReports).set(id, position));
  };

  const removeFirstReport = () => {
    if (reports.size > 0) {
      const newReports = new Map(reports);
      const firstKey = newReports.keys().next().value;
      newReports.delete(firstKey);
      setReports(newReports);
    }
  };

  const removeReport = (id) => {
    const newReports = new Map(reports);
    newReports.delete(id);
    setReports(newReports);
  };

  const CarIcon = ({ id, color }) => (
    <div className="mapMarker" style={{ width: '80px', height: '80px' }}>
    <svg xmlns="http://www.w3.org/2000/svg" width="80" height="80" viewBox="0 0 24 24">
    <path d="m20.772 10.155-1.368-4.104A2.995 2.995 0 0 0 16.559 4H7.441a2.995 2.995 0 0 0-2.845 2.051l-1.368 4.104A2 2 0 0 0 2 12v5c0 .738.404 1.376 1 1.723V21a1 1 0 0 0 1 1h1a1 1 0 0 0 1-1v-2h12v2a1 1 0 0 0 1 1h1a1 1 0 0 0 1-1v-2.277A1.99 1.99 0 0 0 22 17v-5a2 2 0 0 0-1.228-1.845zM7.441 6h9.117c.431 0 .813.274.949.684L18.613 10H5.387l1.105-3.316A1 1 0 0 1 7.441 6zM5.5 16a1.5 1.5 0 1 1 .001-3.001A1.5 1.5 0 0 1 5.5 16zm13 0a1.5 1.5 0 1 1 .001-3.001A1.5 1.5 0 0 1 18.5 16z" fill={color} />
    <path fill="#000" d="M7.441 6h9.117c.431 0 .813.274.949.684L18.613 10H5.387l1.105-3.316A1 1 0 0 1 7.441 6z" />
    <path fill="#fefdde" d="M5.5 16a1.5 1.5 0 1 1 .001-3.001A1.5 1.5 0 0 1 5.5 16z" />
    <path fill="#fefdde" d="M18.5 16a1.5 1.5 0 1 1 .001-3.001A1.5 1.5 0 0 1 18.5 16z" />
    <text x="12" y="16" textAnchor="middle" fontSize="6" fontWeight="bold" fill="#FFFFFF">{id}</text>
    </svg>
    </div>
  );
  const CarMarkers = ({ cars }) => {
    return (
      <>
      {Array.from(cars.entries()).map(([id, { position, color }]) => (
        <AdvancedMarker
        key={id}
        position={position}
        >
        <CarIcon id={id} color={color} />
        </AdvancedMarker>
      ))}
      </>
    );
  };
    const ReportMarkers = ({ reports }) => {
      return (
        <>
        {Array.from(reports.entries()).map(([id, position]) => (
          <AdvancedMarker
          key={id}
          position={position}
          >
          <div className="mapMarker" style={{ width: '200px', height: '100px' }}>
            <img src={reportImage} alt="Report" />
          </div>
          </AdvancedMarker>
        ))}
        </>
      );
    };


  return  <APIProvider apiKey={config.GOOGLE_API_KEY} onLoad={() => console.log('Maps API has loaded.')}>
  <GoogleMap
  defaultZoom={7.5}
  defaultCenter={ { lat: 51.9189046, lng: 19.1343786} }
  mapId={config.MAP_ID}
  onCameraChanged={ (ev: MapCameraChangedEvent) =>
    console.log('camera changed:', ev.detail.center, 'zoom:', ev.detail.zoom)
  }>
  <CarMarkers cars={cars} />
  <ReportMarkers reports={reports}/>
  </GoogleMap>
  </APIProvider>
  ;
}

export default MyMap;
