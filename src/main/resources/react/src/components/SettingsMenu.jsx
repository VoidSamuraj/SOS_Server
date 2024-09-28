import React, { useState } from "react";
import { LoadScript, Autocomplete } from "@react-google-maps/api";
import rightarrow from "../icons/right-arrow.svg";
import EditAccountData from "./EditAccountData";
import SystemAlert from "./SystemAlert";

/**
 * SettingsMenu component displays a menu for adjusting settings,
 * including theme toggles, contrast adjustments, and location settings.
 *
 * @param {boolean} props.isVisible - Determines if the settings menu is visible.
 * @param {Function} props.onSettingsToggle - Function to be called when the settings toggle is activated.
 * @param {Function} props.onEditedToggle - Function to be called when the account editing window is toggled.
 * @param {string} props.locationJson - JSON string representing the current location data.
 * @param {Function} props.setLocationJson - Function to update the location data in JSON format.
 * @param {boolean} props.canSetMapLoc - Indicates whether the user can set a default map location.
 *
 * @returns {JSX.Element} The rendered settings menu component.
 */
function SettingsMenu({
  isVisible,
  onSettingsToggle,
  onEditedToggle,
  locationJson,
  setLocationJson,
  canSetMapLoc,
}) {
  const [location, setLocation] = useState(
    locationJson ? JSON.parse(locationJson)?.name ?? "" : ""
  );
  const [autocomplete, setAutocomplete] = useState(null);
  const [editAccountWindowOpen, setEditAccountWindowOpen] = useState(false);

  const [alertMessage, setAlertMessage] = useState("");
  const [alertType, setAlertType] = useState("info");

  const handlePlaceChanged = () => {
    if (autocomplete) {
      const place = autocomplete.getPlace();
      if (place.geometry) {
        const locationData = {
          name: place.formatted_address || place.name,
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
    <>
      <div
        id="settingsMenu"
        className={`${
          isVisible ? "settingsMenuVisible" : "settingsMenuHidden"
        }`}
      >
        <img onClick={onSettingsToggle} src={rightarrow} alt="close" />
        <div className="switch">
          <input
            className="toggle"
            type="checkbox"
            role="switch"
            name="toggle"
          />
          <span className="slider">Ciemny tryb</span>
        </div>
        <div className="slider-container">
          <label htmlFor="mySlider">Kontrast</label>
          <br />
          <input type="range" id="mySlider" min="0" max="100" value="0" />
        </div>
        {canSetMapLoc && (
          <div id="locationBox">
            <label htmlFor="loc">Lokalizacja domyślna</label>
            <Autocomplete
              onLoad={(autocomplete) => {
                setAutocomplete(autocomplete);
              }}
              onPlaceChanged={handlePlaceChanged}
            >
              <input
                type="text"
                placeholder="Wpisz adres..."
                title="Lokalizacja domyślna służąca do szybkiego nawigowania na mapie"
                value={location}
                onChange={(event) => setLocation(event.target.value)}
              />
            </Autocomplete>
            <input
              type="button"
              id="saveLocation"
              value="Zapisz Lokalizację"
              placeholder="Zapisz Lokalizację"
              title="Zapisz lokalizację domyślna służąca do szybkiego nawigowania na mapie"
              onClick={() => {
                localStorage.setItem("HomeLocation", locationJson);
                setAlertType("success");
                setAlertMessage("Zaktualizowano domyślną lokalizację.");
              }}
            />
          </div>
        )}
        <input
          type="button"
          id="editAccount"
          value="Edytuj swoje dane"
          placeholder="Edytuj swoje dane"
          title
          onClick={() => setEditAccountWindowOpen(true)}
        />
        {editAccountWindowOpen && (
          <EditAccountData
            open={editAccountWindowOpen}
            setAlertMessage={setAlertMessage}
            setAlertType={setAlertType}
            onClose={() => {
              if (onEditedToggle) onEditedToggle();
              setEditAccountWindowOpen(false);
            }}
          />
        )}
      </div>
      {alertMessage && (
        <SystemAlert
          severity={alertType}
          message={alertMessage}
          onClose={() => {
            setAlertMessage("");
            if (alertType == "success") {
              if (onEditedToggle) onEditedToggle();
              setEditAccountWindowOpen(false);
            }
          }}
        />
      )}
    </>
  );
}

export default SettingsMenu;
