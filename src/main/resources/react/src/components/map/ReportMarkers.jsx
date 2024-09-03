import { useMemo } from 'react';
import { AdvancedMarker } from '@vis.gl/react-google-maps';
import reportImage from '../../icons/SOSANIM.svg';
import reportImageDot from '../../icons/sosdot.svg';

// Memoize markers to avoid unnecessary re-renders
const ReportMarkers = ({ reports }) => {
  return useMemo(() => (
    <>
      {Array.from(reports.entries()).map(([id, {position, date, status}]) => (
        <AdvancedMarker key={id} position={position}>
          <div className="mapMarker" style={{ width: status === 0 ? '200px' : '50px', height: status === 0 ? '100px' : '50px' }}>
            <img src={(status == 0)?reportImage:reportImageDot} alt="Report" />
          </div>
        </AdvancedMarker>
      ))}
    </>
  ), [reports]);
};

export default ReportMarkers;
