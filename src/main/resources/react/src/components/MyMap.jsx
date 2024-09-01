import '../style/map.css'; // Załaduj style dla tego komponentu
import config from '../config';
import {APIProvider, Map, MapCameraChangedEvent} from '@vis.gl/react-google-maps';

function MyMap() {

  return  <APIProvider apiKey={config.GOOGLE_API_KEY} onLoad={() => console.log('Maps API has loaded.')}>
            <Map
               defaultZoom={7.5}
               defaultCenter={ { lat: 51.9189046, lng: 19.1343786} }
               mapId={config.MAP_ID}
               onCameraChanged={ (ev: MapCameraChangedEvent) =>
                 console.log('camera changed:', ev.detail.center, 'zoom:', ev.detail.zoom)
               }>
            </Map>
          </APIProvider>
;
}

export default MyMap;
