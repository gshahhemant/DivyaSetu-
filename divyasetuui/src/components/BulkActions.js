import React from 'react';

export function BulkActions({ onBulkUpdate, onDownload, userRole }) {
  return (
    <div style={{ marginBottom: '10px' }}>
      {(userRole === 'S' || userRole === 'W') && (
        <button onClick={onBulkUpdate} style={{ marginRight: '10px' }}>🔁 Bulk Update All</button>
      )}
      <button onClick={onDownload}>📥 Download Excel</button>
    </div>
  );
}
