import '../../style/map.css';
import config from '../../config';
import { APIProvider, Map as GoogleMap, MapCameraChangedEvent } from '@vis.gl/react-google-maps';
import CarMarkers from './CarMarkers.jsx'
import ReportMarkers from './ReportMarkers.jsx'

function MyMap({patrols, reports}) {

  return (
    <APIProvider apiKey={config.GOOGLE_API_KEY} onLoad={() => console.log('Maps API has loaded.')}>
      <GoogleMap
        defaultZoom={7.5}
        defaultCenter={{ lat: 51.9189046, lng: 19.1343786 }}
        mapId={config.MAP_ID}
        onCameraChanged={(ev: MapCameraChangedEvent) => console.log('camera changed:', ev.detail.center, 'zoom:', ev.detail.zoom)}
      >
        <CarMarkers cars={patrols}/>
        <ReportMarkers reports={reports}/>
      </GoogleMap>
    </APIProvider>
  );
}

export default MyMap;
