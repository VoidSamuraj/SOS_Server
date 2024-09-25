import React from "react";
import x from "../icons/x.svg";

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
