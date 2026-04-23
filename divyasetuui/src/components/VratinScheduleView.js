import React, { useState, useEffect } from 'react';
import { API_ENDPOINTS } from '../config/api';
import '../styles/VratinScheduleView.css';

export function VratinScheduleView({ scheduleSrNo, scheduleDate, scheduleDay, scheduleLocation, onClose }) {
  const [details, setDetails] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (scheduleSrNo) {
      fetchDetails();
    }
  }, [scheduleSrNo]);

  const fetchDetails = async () => {
    setLoading(true);
    try {
      const response = await fetch(`${API_ENDPOINTS.DIVYASETU}/api/vratin/mappings/details/${scheduleSrNo}`);
      if (response.ok) {
        const data = await response.json();
        setDetails(data);
      }
    } catch (error) {
      console.error('Error fetching schedule details:', error);
    } finally {
      setLoading(false);
    }
  };

  const formatScheduleText = () => {
    if (!details || details.length === 0) return '';

    // Helper to remove spaces from phone numbers
    const formatPhone = (phone) => phone ? phone.replace(/\s+/g, '') : '';

    const lines = [];
    lines.push('═══════════════════════════════════');
    lines.push('        Jay Yogeshwar');
    lines.push('═══════════════════════════════════');
    lines.push(`Date: ${scheduleDay}, ${scheduleDate}`);
    lines.push(`${scheduleLocation}`);
    lines.push('═══════════════════════════════════');
    lines.push('');

    details.forEach((host, index) => {
      const hostNumber = index + 1;
      
      lines.push(`HOST ${hostNumber}: ${host.hostName}`);
      lines.push(`${formatPhone(host.hostMobile)}`);
      lines.push(`${host.hostAddress}`);
      
      // List each family with their mobile number
      host.vratiFamilies.forEach(family => {
        let familyLine = `• ${family.vratiFamilyName}`;
        if (family.vratiFamilyMob) {
          familyLine += ` ${formatPhone(family.vratiFamilyMob)}`;
        }
        lines.push(familyLine);
      });
      
      lines.push('───────────────────────────────────');
      lines.push('');
    });

    const totalHosts = details.length;
    const totalFamilies = details.reduce((sum, host) => sum + host.vratiFamilies.length, 0);

    lines.push(`Total: ${totalHosts} Hosts, ${totalFamilies} Families`);
    lines.push('═══════════════════════════════════');

    return lines.join('\n');
  };

  const handleCopy = () => {
    const text = formatScheduleText();
    
    // Try modern clipboard API first
    if (navigator.clipboard && navigator.clipboard.writeText) {
      navigator.clipboard.writeText(text)
        .then(() => {
          console.log('Copied to clipboard successfully!');
          // You could add a visual feedback here
        })
        .catch(err => {
          console.error('Clipboard API failed, trying fallback:', err);
          fallbackCopyTextToClipboard(text);
        });
    } else {
      // Fallback for older browsers
      fallbackCopyTextToClipboard(text);
    }
  };

  const fallbackCopyTextToClipboard = (text) => {
    const textArea = document.createElement('textarea');
    textArea.value = text;
    textArea.style.position = 'fixed';
    textArea.style.top = '0';
    textArea.style.left = '0';
    textArea.style.width = '2em';
    textArea.style.height = '2em';
    textArea.style.padding = '0';
    textArea.style.border = 'none';
    textArea.style.outline = 'none';
    textArea.style.boxShadow = 'none';
    textArea.style.background = 'transparent';
    document.body.appendChild(textArea);
    textArea.focus();
    textArea.select();
    
    try {
      const successful = document.execCommand('copy');
      console.log('Fallback copy ' + (successful ? 'successful' : 'failed'));
    } catch (err) {
      console.error('Fallback copy failed:', err);
    }
    
    document.body.removeChild(textArea);
  };

  if (loading) {
    return (
      <div className="schedule-view-overlay">
        <div className="schedule-view-modal">
          <div className="loading-message">Loading schedule details...</div>
        </div>
      </div>
    );
  }

  return (
    <div className="schedule-view-overlay" onClick={onClose}>
      <div className="schedule-view-modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>Vratin Schedule Details</h2>
          <button className="close-btn" onClick={onClose}>✕</button>
        </div>

        <div className="modal-content">
          {/* Visual Display */}
          <div className="schedule-visual">
            <div className="schedule-header-section">
              <h1 className="schedule-title">Jay Yogeshwar</h1>
              <div className="schedule-info-display">
                <div className="info-item">
                  <span className="info-icon">📅</span>
                  <span className="info-text">{scheduleDay}, {scheduleDate}</span>
                </div>
                <div className="info-item">
                  <span className="info-icon">📍</span>
                  <span className="info-text">{scheduleLocation}</span>
                </div>
              </div>
            </div>

            <div className="hosts-container">
              {details.map((host, index) => (
                <div key={index} className="host-card">
                  <div className="host-header">
                    <span className="host-number">HOST {index + 1}</span>
                    <span className="host-name">{host.hostName}</span>
                  </div>
                  <div className="host-details">
                    <div className="host-info-row">
                      <span className="detail-icon">📞</span>
                      <span className="detail-text">{host.hostMobile}</span>
                    </div>
                    <div className="host-info-row">
                      <span className="detail-icon">🏠</span>
                      <span className="detail-text">{host.hostAddress}</span>
                    </div>
                    <div className="host-info-row families-section">
                      <span className="detail-icon">👥</span>
                      <div className="detail-text">
                        <strong>Families:</strong>
                        <div className="family-list">
                          {host.vratiFamilies.map((family, fIdx) => (
                            <div key={fIdx} className="family-item">
                              <div className="family-name">{family.vratiFamilyName}</div>
                              {family.vratiFamilyMob && (
                                <div className="family-mob">📱 {family.vratiFamilyMob}</div>
                              )}
                            </div>
                          ))}
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>

            <div className="schedule-footer">
              <div className="total-info">
                <span className="total-label">Total:</span>
                <span className="total-value">{details.length} Hosts, {details.reduce((sum, host) => sum + host.vratiFamilies.length, 0)} Families</span>
              </div>
            </div>
          </div>

          {/* Text Format for Copying */}
          <details className="text-format-section">
            <summary>Show Text Format (for copying)</summary>
            <pre className="schedule-text">{formatScheduleText()}</pre>
          </details>
        </div>

        <div className="modal-footer">
          <button className="copy-btn" onClick={handleCopy}>
            📋 Copy to Clipboard
          </button>
          <button className="close-btn-secondary" onClick={onClose}>
            Close
          </button>
        </div>
      </div>
    </div>
  );
}
