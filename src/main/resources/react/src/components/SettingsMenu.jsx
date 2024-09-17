import React, { useState } from "react";
import { LoadScript, Autocomplete } from "@react-google-maps/api";
import "../style/settingsMenu.css";
import rightarrow from "../icons/right-arrow.svg";

function SettingsMenu({
  isVisible,
  onSettingsToggle,
  locationJson,
  setLocationJson,
}) {
  const [location, setLocation] = useState(locationJson ? JSON.parse(locationJson)?.name ?? "":"");
  const [autocomplete, setAutocomplete] = useState(null);

  const handlePlaceChanged = () => {
    if (autocomplete) {
      const place = autocomplete.getPlace();
      if (place.geometry) {
        const locationData = {
          name: (place.formatted_address || place.name),
          latitude: place.geometry.location.lat(),
          longitude: place.geometry.location.lng(),
        };
        const locationString = JSON.stringify(locationData);
        setLocationJson(locationString);

        setLocation(place.formatted_address || place.name);
      }
    }
  };

  return (
    <div
      id="settingsMenu"
      className={`${isVisible ? "settingsMenuVisible" : "settingsMenuHidden"}`}
    >
      <img onClick={onSettingsToggle} src={rightarrow} alt="close" />
      <div className="switch">
        <input className="toggle" type="checkbox" role="switch" name="toggle" />
        <span className="slider">Ciemny tryb</span>
      </div>
      <div className="slider-container">
        <label htmlFor="mySlider">Kontrast</label>
        <br />
        <input type="range" id="mySlider" min="0" max="100" value="0" />
      </div>
      {locationJson ? (
        <div id="locationBox">
          <label htmlFor="loc">Lokalizacja</label>
          <Autocomplete
            onLoad={(autocomplete) => {
              setAutocomplete(autocomplete);
            }}
            onPlaceChanged={handlePlaceChanged}
          >
            <input
              type="text"
              placeholder="Wpisz adres..."
              value={location}
              onChange={(event) => setLocation(event.target.value)}
            />
          </Autocomplete>
          <input
            type="button"
            id="saveLocation"
            value="Zapisz Lokalizację"
            placeholder="Zapisz Lokalizację"
            onClick={() => localStorage.setItem("HomeLocation", locationJson)}
          />
        </div>
      ) : (
        ""
      )}
    </div>
  );
}

export default SettingsMenu;
