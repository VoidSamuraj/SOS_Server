import React from "react";
import x from "../icons/x.svg";


/**
 * StatsOverlay component displays a statistics overlay with a legend and a close button.
 *
 * @param {boolean} props.isVisible - Determines if the overlay is visible.
 * @param {Function} props.onStatsToggle - Function to be called when the close button is clicked to toggle the overlay.
 *
 * @returns {JSX.Element} The rendered statistics overlay component.
 */
function StatsOverlay({ isVisible, onStatsToggle }) {
  return (
    <div
      id="statsOverlay"
      className={`${isVisible ? "statsOverlayVisible" : "statsOverlayHidden"}`}
    >
      <div>
        <img onClick={onStatsToggle} id="statsClose" src={x} alt="close" />
        <div id="statsLegend">
          Legenda
          <p>
            Czerwone okręgi przedstawiają częstotliwości interwencji w danym
            obszarze. Im większy punkt tym więcej wezwań zanotowano z tego
            rejonu.
          </p>
        </div>
      </div>
    </div>
  );
}

export default StatsOverlay;
