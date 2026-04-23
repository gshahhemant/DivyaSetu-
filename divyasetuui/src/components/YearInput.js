import React from 'react';

export function YearInput({ year, onChange }) {
  return (
    <label>
      Year:&nbsp;
      <input
        type="text"
        value={year}
        onChange={e => onChange(e.target.value)}
        placeholder="e.g., 2025"
        style={{ width: '80px', marginRight: '10px' }}
      />
    </label>
  );
}
