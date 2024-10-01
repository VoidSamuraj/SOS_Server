import React, {useEffect, useMemo } from "react";
import {useMap, useMapsLibrary} from '@vis.gl/react-google-maps';


const Heatmap = ({interventions, radius, opacity}) => {
  const map = useMap();
  const visualization = useMapsLibrary('visualization');

  const heatmap = useMemo(() => {
    if (!visualization) return null;

    return new google.maps.visualization.HeatmapLayer({
      radius: radius,
      opacity: opacity
    });
  }, [visualization, radius, opacity]);

  useEffect(() => {
    if (!heatmap) return;
    heatmap.setData(
      interventions.map(intervention => {
try{
    let jsonStr = intervention.location.replace(/(\w+):/g, '"$1":');
          let jsonLocation = JSON.parse(jsonStr);
        return {
          location: new google.maps.LatLng(jsonLocation.lat, jsonLocation.lng),
          weight: 1
        };
            } catch (error) {
              console.error("JSON Location parsing error:", error);
              return null;
            }
      })
    );

  }, [heatmap, interventions, radius, opacity]);

  useEffect(() => {
    if (!heatmap) return;

    heatmap.setMap(map);

    return () => {
      heatmap.setMap(null);
    };
  }, [heatmap, map]);

  return null;
    };

export default Heatmap;