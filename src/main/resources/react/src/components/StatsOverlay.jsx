import React, { useState } from "react";
import x from "../icons/x.svg";
import StatsMap from "./map/StatsMap.jsx";

/**
 * StatsOverlay component displays a statistics overlay with a legend and a close button.
 *
 * @param {boolean} props.isVisible - Determines if the overlay is visible.
 * @param {Function} props.onStatsToggle - Function to be called when the close button is clicked to toggle the overlay.
 *
 * @returns {JSX.Element} The rendered statistics overlay component.
 */
function StatsOverlay({ isVisible, onStatsToggle, locationJson }) {
  const [radius, setRadius] = useState(25);
  const [opacity, setOpacity] = useState(0.8);

  return (
    <div
      id="statsOverlay"
      className={`${isVisible ? "statsOverlayVisible" : "statsOverlayHidden"}`}
    >
      <div>
        <StatsMap
          locationJson={locationJson}
          radius={radius}
          opacity={opacity}
        />
        <img onClick={onStatsToggle} id="statsClose" src={x} alt="close" />
        <div id="statsLegend">
          <h2>Opcje mapy</h2>

          <label htmlFor="radiusPicker">Promień</label>
          <input
            id="radiusPicker"
            type="number"
            value={radius}
            onChange={(event) => setRadius(event.target.value)}
            min="0"
            max="100"
            step="1"
          />
          <label htmlFor="opacityPicker">Nieprzezroczystość</label>
          <input
            id="opacityPicker"
            type="number"
            value={opacity}
            onChange={(event) => setOpacity(event.target.value)}
            min="0.1"
            max="1"
            step="0.1"
          />

          <label>Liczba zgłoszeń</label>
          <div class="scale-container">
            <div class="scale"></div>
            <div class="labels">
              <div class="label">Mało</div>
              <div class="label">Dużo</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default StatsOverlay;
