import React from 'react';

export function ZipSelector({ zips, selectedZip, onChange }) {
  return (
    <label>
      City:&nbsp;
      <select value={selectedZip} onChange={e => onChange(e.target.value)} style={{ marginRight: '10px' }}>
        <option value="">--Select CITY And ZIP--</option>
        {zips.map((zipObj, idx) => (
          <option key={idx} value={zipObj.zipcode}>
            {zipObj.cityname}:: {zipObj.zipcode}
          </option>
        ))}
      </select>
    </label>
  );
}
