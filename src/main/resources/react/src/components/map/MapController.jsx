import React, { useEffect } from "react";
import { useMap } from "@vis.gl/react-google-maps";

/**
 * MapController component manages the map's center location based on
 * provided location data or previously saved home location in local storage.
 * It pans the map to the specified coordinates when locationJson or
 * refreshFlag changes.
 *
 * @param {string} props.locationJson - A JSON string representing the location
 * coordinates (latitude and longitude) to navigate to while homing.
 * @param {boolean} props.refreshFlag - A flag that triggers the map to refresh
 * its position when changed.
 * @param {string} props.panTo - A JSON string representing the location
 * coordinates (latitude and longitude) to navigate to now.
 *
 * @returns {null} The component does not render anything.
 */

function MapController({ locationJson, refreshFlag, panTo }) {
  const map = useMap();

  const navigateHome = () => {
    if (map && locationJson) {
      const savedLocation = localStorage.getItem("HomeLocation");
      let locationData;
      try {
        locationData = locationJson
          ? JSON.parse(locationJson)
          : savedLocation
          ? JSON.parse(savedLocation)
          : null;
      } catch (error) {
        console.error("Error parsing location data from localStorage:", error);
        locationData = null;
      }

      const defaultCenter = {
        lat: locationData?.latitude ?? 51.9189046,
        lng: locationData?.longitude ?? 19.1343786,
      };

      map.panTo(defaultCenter);
      if (locationData) map.setZoom(12);
    }
  };

  useEffect(() => {
    navigateHome();
  }, [locationJson, map, refreshFlag]);

  useEffect(() => {
    if (map && panTo) {
      map.panTo(panTo);
      map.setZoom(12);
    }
  }, [panTo]);

  return null;
}
export default MapController;
