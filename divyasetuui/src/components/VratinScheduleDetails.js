import React, { useState, useEffect } from 'react';
import { API_ENDPOINTS } from '../config/api';
import { VratinScheduleView } from './VratinScheduleView';
import { VratinSchedulerReport } from './VratinSchedulerReport';
import '../styles/VratinScheduleDetails.css';

export function VratinScheduleDetails() {
  const [schedules, setSchedules] = useState([]);
  const [hosts, setHosts] = useState([]);
  const [families, setFamilies] = useState([]);
  const [selectedSchedule, setSelectedSchedule] = useState('');
  const [selectedHost, setSelectedHost] = useState('');
  const [selectedFamilies, setSelectedFamilies] = useState([]);
  const [mappings, setMappings] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showScheduleView, setShowScheduleView] = useState(false);
  const [showReport, setShowReport] = useState(false);
  const [showAddHostForm, setShowAddHostForm] = useState(false);
  const [newHost, setNewHost] = useState({ name: '', address: '', mob: '' });
  const [addHostLoading, setAddHostLoading] = useState(false);
  const [addHostError, setAddHostError] = useState('');

  useEffect(() => {
    fetchSchedules();
    fetchHosts();
    fetchFamilies();
  }, []);

  useEffect(() => {
    if (selectedSchedule) {
      fetchMappings();
    }
  }, [selectedSchedule]);

  const fetchSchedules = async () => {
    try {
      const response = await fetch(`${API_ENDPOINTS.DIVYASETU}/api/vratin/schedules/bydate`);
      if (response.ok) {
        const data = await response.json();
        setSchedules(data);
      }
    } catch (error) {
      console.error('Error fetching schedules:', error);
    }
  };

  const fetchHosts = async () => {
    try {
      const response = await fetch(`${API_ENDPOINTS.DIVYASETU}/api/vratin/hosts`);
      if (response.ok) {
        const data = await response.json();
        setHosts(data);
      }
    } catch (error) {
      console.error('Error fetching hosts:', error);
    }
  };

  const fetchFamilies = async () => {
    try {
      const response = await fetch(`${API_ENDPOINTS.DIVYASETU}/api/vratin/families`);
      if (response.ok) {
        const data = await response.json();
        setFamilies(data);
      }
    } catch (error) {
      console.error('Error fetching families:', error);
    }
  };

  const fetchMappings = async () => {
    if (!selectedSchedule) return;
    
    try {
      const response = await fetch(`${API_ENDPOINTS.DIVYASETU}/api/vratin/mappings/schedule/${selectedSchedule}`);
      if (response.ok) {
        const data = await response.json();
        console.log('Fetched mappings:', data);
        setMappings(data);
      } else {
        console.log('No mappings found or error fetching');
        setMappings([]);
      }
    } catch (error) {
      console.error('Error fetching mappings:', error);
      setMappings([]);
    }
  };

  const handleFamilySelection = (familySrNo) => {
    setSelectedFamilies(prev => {
      if (prev.includes(familySrNo)) {
        return prev.filter(id => id !== familySrNo);
      } else {
        return [...prev, familySrNo];
      }
    });
  };

  const handleSaveMapping = async (familySrNo) => {
    if (!selectedSchedule || !selectedHost) {
      console.log('Please select schedule date and host');
      return;
    }

    setLoading(true);
    try {
      const mapping = {
        scheduleSrno: parseInt(selectedSchedule),
        hostSrno: parseInt(selectedHost),
        familySrno: familySrNo
      };

      const response = await fetch(`${API_ENDPOINTS.DIVYASETU}/api/vratin/mappings`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(mapping)
      });

      if (!response.ok) {
        throw new Error('Failed to save mapping');
      }
      
      console.log('Mapping saved successfully!');
      fetchMappings();
    } catch (error) {
      console.error('Error saving mapping:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteMapping = async (scheduleSrno, hostSrno, familySrno) => {
    console.log('Delete button clicked with:', { scheduleSrno, hostSrno, familySrno });

    try {
      const deleteUrl = `${API_ENDPOINTS.DIVYASETU}/api/vratin/mappings/${scheduleSrno}/${hostSrno}/${familySrno}`;
      console.log('Delete URL:', deleteUrl);
      
      const response = await fetch(deleteUrl, { 
        method: 'PUT',
        headers: {
          'Accept': '*/*',
          'Content-Type': 'application/json'
        }
      });

      console.log('Delete response status:', response.status);
      console.log('Delete response ok:', response.ok);

      if (response.ok || response.status === 204) {
        console.log('Mapping deleted successfully!');
        await fetchMappings();
      } else {
        const errorText = await response.text();
        console.error('Delete failed with status:', response.status, 'Error:', errorText);
      }
    } catch (error) {
      console.error('Error deleting mapping:', error);
    }
  };

  const handleAddHost = async () => {
    if (!newHost.name.trim()) {
      setAddHostError('Host name is required.');
      return;
    }
    if (!newHost.address.trim()) {
      setAddHostError('Address is required.');
      return;
    }
    if (!newHost.mob.trim()) {
      setAddHostError('Mobile is required.');
      return;
    }
    if (!/^\d{3}-\d{3}-\d{4}$/.test(newHost.mob.trim())) {
      setAddHostError('Mobile must be in 999-999-1234 format.');
      return;
    }
    setAddHostLoading(true);
    setAddHostError('');
    try {
      const response = await fetch(`${API_ENDPOINTS.DIVYASETU}/api/vratin/hosts`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name: newHost.name.trim(), address: newHost.address.trim(), mob: newHost.mob.trim() })
      });
      if (!response.ok) throw new Error('Failed to add host');
      setNewHost({ name: '', address: '', mob: '' });
      setShowAddHostForm(false);
      await fetchHosts();
    } catch (error) {
      setAddHostError('Error adding host. Please try again.');
      console.error('Error adding host:', error);
    } finally {
      setAddHostLoading(false);
    }
  };

  const getScheduleDisplay = (schedule) => {
    return `${schedule.scheduleDate} (${schedule.day})`;
  };

  const getHostName = (hostSrNo) => {
    const host = hosts.find(h => h.srNo === hostSrNo);
    return host ? host.name : 'Unknown';
  };

  const getFamilyName = (familySrNo) => {
    const family = families.find(f => f.srNo === familySrNo);
    return family ? family.name : 'Unknown';
  };

  const isFamilyMapped = (familySrNo) => {
    const mapped = mappings.some(m => m.familySrno === familySrNo);
    return mapped;
  };

  // If showing report, render only the report with back button
  if (showReport) {
    return (
      <div className="vratin-schedule-container">
        <div className="back-btn-container">
          <button 
            className="back-btn" 
            onClick={() => setShowReport(false)}
            title="Back to Vratin Scheduler"
          >
            ← Back
          </button>
        </div>
        <VratinSchedulerReport />
      </div>
    );
  }

  return (
    <div className="vratin-schedule-container">
      <div className="scheduler-header">
        <div className="section-title">Vratin Scheduler</div>
        <button 
          className="report-btn" 
          onClick={() => setShowReport(true)}
          title="View Vratin Scheduler Report"
        >
          📊 View Report
        </button>
      </div>

      <div className="schedule-controls">
        <div className="control-group">
          <label>Vratin Schedule Date:</label>
          <div className="schedule-select-wrapper">
            <select 
              value={selectedSchedule} 
              onChange={(e) => setSelectedSchedule(e.target.value)}
              className="schedule-select"
            >
              <option value="">-- Select Schedule Date --</option>
              {schedules.map(schedule => (
                <option key={schedule.srNo} value={schedule.srNo}>
                  {getScheduleDisplay(schedule)}
                </option>
              ))}
            </select>
            {selectedSchedule && (
              <button 
                className="view-schedule-btn" 
                onClick={() => setShowScheduleView(true)}
                title="View Schedule Details"
              >
                👁️ View
              </button>
            )}
          </div>
        </div>

        <div className="control-group">
          <div className="host-label-row">
            <label>Host Name:</label>
            <button
              className="add-host-btn"
              onClick={() => { setShowAddHostForm(true); setAddHostError(''); }}
              title="Add new host"
              type="button"
            >+</button>
          </div>
          <select 
            value={selectedHost} 
            onChange={(e) => setSelectedHost(e.target.value)}
            className="host-select"
          >
            <option value="">-- Select Host --</option>
            {hosts.map(host => (
              <option key={host.srNo} value={host.srNo}>
                {host.name}
              </option>
            ))}
          </select>
        </div>
      </div>

      {selectedSchedule && schedules.find(s => s.srNo === parseInt(selectedSchedule)) && (
        <div className="location-info-bar">
          <span className="location-icon-small">📍</span>
          <span className="location-text-small">
            {schedules.find(s => s.srNo === parseInt(selectedSchedule)).location}
          </span>
        </div>
      )}

      <div className="families-section">
        <h3>Family Members</h3>
        <div className="families-table-wrapper">
          <table className="families-table">
            <thead>
              <tr>
                <th>Family Name</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {families.map(family => (
                <tr key={family.srNo} className={isFamilyMapped(family.srNo) ? 'mapped-row' : ''}>
                  <td className="family-name-col">{family.name}</td>
                  <td className="action-col">
                    {isFamilyMapped(family.srNo) ? (
                      <>
                        <span className="mapped-badge">✓ Mapped</span>
                        <button
                          onClick={() => {
                            const mapping = mappings.find(m => m.familySrno === family.srNo);
                            console.log('Found mapping for delete:', mapping);
                            if (mapping) {
                              handleDeleteMapping(mapping.scheduleSrno, mapping.hostSrno, mapping.familySrno);
                            } else {
                              console.log('Mapping not found for family:', family.srNo);
                            }
                          }}
                          className="delete-btn-inline"
                          disabled={!selectedSchedule}
                          title="Delete this mapping"
                        >
                          🗑️ Delete
                        </button>
                      </>
                    ) : (
                      <button
                        onClick={() => handleSaveMapping(family.srNo)}
                        disabled={loading || !selectedSchedule || !selectedHost}
                        className="save-btn-inline"
                      >
                        💾 Save
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {selectedSchedule && mappings.length > 0 && (
        <div className="mappings-section">
          <h3>Current Mappings for Selected Date</h3>
          <div className="mappings-table-wrapper">
            <table className="mappings-table">
              <thead>
                <tr>
                  <th>Family Name</th>
                  <th>Host Name</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {mappings.map((mapping, idx) => (
                  <tr key={idx}>
                    <td>{getFamilyName(mapping.familySrno)}</td>
                    <td>{getHostName(mapping.hostSrno)}</td>
                    <td>
                      <button
                        onClick={() => handleDeleteMapping(
                          mapping.scheduleSrno,
                          mapping.hostSrno,
                          mapping.familySrno
                        )}
                        className="delete-btn"
                      >
                        🗑️ Delete
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {showAddHostForm && (
        <div className="add-host-modal-overlay" onClick={() => setShowAddHostForm(false)}>
          <div className="add-host-modal" onClick={e => e.stopPropagation()}>
            <div className="add-host-modal-header">
              <span>Add New Host</span>
              <button className="add-host-close-btn" onClick={() => setShowAddHostForm(false)}>✕</button>
            </div>
            <div className="add-host-modal-body">
              <div className="add-host-field">
                <label>Host Name <span className="required">*</span></label>
                <input
                  type="text"
                  value={newHost.name}
                  onChange={e => setNewHost(prev => ({ ...prev, name: e.target.value }))}
                  placeholder="Enter host name"
                  className="add-host-input"
                />
              </div>
              <div className="add-host-field">
                <label>Address <span className="required">*</span></label>
                <input
                  type="text"
                  value={newHost.address}
                  onChange={e => setNewHost(prev => ({ ...prev, address: e.target.value }))}
                  placeholder="Enter address"
                  className="add-host-input"
                />
              </div>
              <div className="add-host-field">
                <label>Mobile <span className="required">*</span></label>
                <input
                  type="text"
                  value={newHost.mob}
                  onChange={e => {
                    const digits = e.target.value.replace(/\D/g, '').slice(0, 10);
                    let formatted = digits;
                    if (digits.length > 6) formatted = digits.slice(0,3) + '-' + digits.slice(3,6) + '-' + digits.slice(6);
                    else if (digits.length > 3) formatted = digits.slice(0,3) + '-' + digits.slice(3);
                    setNewHost(prev => ({ ...prev, mob: formatted }));
                  }}
                  placeholder="999-999-1234"
                  className="add-host-input"
                  maxLength={12}
                />
              </div>
              {addHostError && <div className="add-host-error">{addHostError}</div>}
            </div>
            <div className="add-host-modal-footer">
              <button className="add-host-cancel-btn" onClick={() => setShowAddHostForm(false)}>Cancel</button>
              <button className="add-host-save-btn" onClick={handleAddHost} disabled={addHostLoading}>
                {addHostLoading ? 'Saving...' : 'Save Host'}
              </button>
            </div>
          </div>
        </div>
      )}

      {showScheduleView && selectedSchedule && (
        <VratinScheduleView
          scheduleSrNo={parseInt(selectedSchedule)}
          scheduleDate={schedules.find(s => s.srNo === parseInt(selectedSchedule))?.scheduleDate}
          scheduleDay={schedules.find(s => s.srNo === parseInt(selectedSchedule))?.day}
          scheduleLocation={schedules.find(s => s.srNo === parseInt(selectedSchedule))?.location}
          onClose={() => setShowScheduleView(false)}
        />
      )}
    </div>
  );
}
