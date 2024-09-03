import { useMemo } from 'react';
import { AdvancedMarker } from '@vis.gl/react-google-maps';
import reportImage from '../../icons/SOSANIM.svg';

// Memoize markers to avoid unnecessary re-renders
const ReportMarkers = ({ reports }) => {
  return useMemo(() => (
    <>
      {Array.from(reports.entries()).map(([id, {position, date, status}]) => (
        <AdvancedMarker key={id} position={position}>
          <div className="mapMarker" style={{ width: '200px', height: '100px' }}>
            <img src={reportImage} alt="Report" />
          </div>
        </AdvancedMarker>
      ))}
    </>
  ), [reports]);
};

export default ReportMarkers;
