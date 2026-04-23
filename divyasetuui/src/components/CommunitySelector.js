import React from 'react';

export function CommunitySelector({ communities, selectedCommunity, onChange }) {
  return (
    <label>
      Community:&nbsp;
      <select value={selectedCommunity} onChange={e => onChange(e.target.value)} style={{ marginRight: '10px' }}>
        <option value="">--Select Community--</option>
        {communities.map((comm, idx) => (
          <option key={idx} value={comm}>{comm}</option>
        ))}
      </select>
    </label>
  );
}
