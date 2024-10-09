import React, { useMemo, useState, useEffect } from "react";
import {
  AdvancedMarker,
  InfoWindow,
  useAdvancedMarkerRef,
} from "@vis.gl/react-google-maps";
import { usePatrols } from "./MapFunctions";
import keys from "../../keys";
import { fetchStreetName } from "../../script/ApiService.js";

/**
 * CarMarkers component renders markers for each car in the provided cars data.
 * It filters out invalid positions and uses the CarIcon component to display each marker.
 *
 * @param {Map} props.cars - A Map containing car data, including position, status, and identification information.
 *
 * @returns {JSX.Element} The rendered car markers.
 */
const CarMarkers = ({ cars }) => {
  const { statusToCarColor } = usePatrols();

  // Memoize markers to avoid unnecessary re-renders
  return useMemo(
    () => (
      <>
        {Array.from(cars.entries()).map(
          ([id, { position, status, name, surname, phone }]) => (
            <CarIcon
              id={id}
              position={position}
              color={statusToCarColor(status)}
              name={name}
              surname={surname}
              phone={phone}
            />
          )
        )}
      </>
    ),
    [cars]
  );
};

/**
 * CarIcon component represents an individual car marker on the map.
 * It displays the car's information in an info window when clicked,
 * including the driver's name, surname, phone number, and street name.
 *
 * @param {string} props.id - The unique identifier for the car.
 * @param {Object} props.position - The position of the car, containing latitude and longitude.
 * @param {string} props.color - The color of the car marker.
 * @param {string} props.name - The first name of the driver.
 * @param {string} props.surname - The surname of the driver.
 * @param {string} props.phone - The phone number of the driver.
 *
 * @returns {JSX.Element} The rendered car icon with an info window.
 */
const CarIcon = ({ id, position, color, name, surname, phone }) => {
  const [infowindowOpen, setInfowindowOpen] = useState(false);
  const [markerRef, marker] = useAdvancedMarkerRef();
  const [streetName, setStreetName] = useState(null);
  useEffect(() => {
    fetchStreetName(
      position.lat,
      position.lng,
      keys.GOOGLE_API_KEY,
      setStreetName,
      streetName
    );
  }, [position]);

  return (
    <>
      <AdvancedMarker
        key={id}
        ref={markerRef}
        position={position}
        onClick={() => setInfowindowOpen(true)}
      >
        <div className="mapMarker" style={{ width: "40px", height: "40px" }}>
          <svg
            xmlns="http://www.w3.org/2000/svg"
            width="80"
            height="80"
            viewBox="0 0 24 24"
          >
            <path
              d="m20.772 10.155-1.368-4.104A2.995 2.995 0 0 0 16.559 4H7.441a2.995 2.995 0 0 0-2.845 2.051l-1.368 4.104A2 2 0 0 0 2 12v5c0 .738.404 1.376 1 1.723V21a1 1 0 0 0 1 1h1a1 1 0 0 0 1-1v-2h12v2a1 1 0 0 0 1 1h1a1 1 0 0 0 1-1v-2.277A1.99 1.99 0 0 0 22 17v-5a2 2 0 0 0-1.228-1.845zM7.441 6h9.117c.431 0 .813.274.949.684L18.613 10H5.387l1.105-3.316A1 1 0 0 1 7.441 6zM5.5 16a1.5 1.5 0 1 1 .001-3.001A1.5 1.5 0 0 1 5.5 16zm13 0a1.5 1.5 0 1 1 .001-3.001A1.5 1.5 0 0 1 18.5 16z"
              fill={color}
            />
            <text
              x="12"
              y="16"
              textAnchor="middle"
              fontSize="6"
              fontWeight="bold"
              fill="#FFFFFF"
            >
              {id}
            </text>
          </svg>
        </div>
      </AdvancedMarker>
      {infowindowOpen && (
        <InfoWindow
          anchor={marker}
          maxWidth={200}
          onCloseClick={() => setInfowindowOpen(false)}
        >
          <div className="key-value-table">
            <div className="table-row">
              <div className="table-cell key">Imię:</div>
              <div className="table-cell value">{name}</div>
            </div>
            <div className="table-row">
              <div className="table-cell key">Nazwisko:</div>
              <div className="table-cell value">{surname}</div>
            </div>
            <div className="table-row">
              <div className="table-cell key">Telefon:</div>
              <div className="table-cell value">{phone}</div>
            </div>
            {streetName ? (
              <div className="table-row">
                <div className="table-cell key">Lokalizacja:</div>
                <div className="table-cell value">{streetName}</div>
              </div>
            ) : (
              ""
            )}
          </div>
        </InfoWindow>
      )}
    </>
  );
};

export default CarMarkers;
