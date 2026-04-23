import React, { useState } from 'react';
import { API_ENDPOINTS } from '../config/api';
import '../styles/VratinSchedulerReport.css';

export function VratinSchedulerReport() {
  const [year, setYear] = useState('2026');
  const [reportData, setReportData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [reportType, setReportType] = useState('family'); // 'family' | 'host'
  const [hostReportData, setHostReportData] = useState(null);
  const [hostLoading, setHostLoading] = useState(false);

  const months = ['JAN', 'FEB', 'MAR', 'APR', 'MAY', 'JUN', 'JUL', 'AUG', 'SEP', 'OCT', 'NOV', 'DEC'];

  const fetchReport = async () => {
    if (!year) {
      alert('Please enter a year');
      return;
    }

    setLoading(true);
    try {
      const response = await fetch(`${API_ENDPOINTS.DIVYASETU}/api/vratin/families/schedules/${year}`);
      if (response.ok) {
        const data = await response.json();
        setReportData(data);
      } else {
        alert('Failed to fetch report data');
      }
    } catch (error) {
      console.error('Error fetching report:', error);
      alert('Error fetching report data');
    } finally {
      setLoading(false);
    }
  };

  const fetchHostReport = async () => {
    if (!year) {
      alert('Please enter a year');
      return;
    }

    setHostLoading(true);
    try {
      const response = await fetch(`${API_ENDPOINTS.DIVYASETU}/api/vratin/hostfamilies/schedules/${year}`);
      if (response.ok) {
        const data = await response.json();
        setHostReportData(data);
      } else {
        alert('Failed to fetch host report data');
      }
    } catch (error) {
      console.error('Error fetching host report:', error);
      alert('Error fetching host report data');
    } finally {
      setHostLoading(false);
    }
  };

  const hasScheduleInMonth = (dates, monthIndex) => {
    if (!dates || dates.length === 0) return false;
    return dates.some(dateStr => {
      const date = new Date(dateStr);
      return date.getMonth() === monthIndex;
    });
  };

  const getRowTotal = (dates) => {
    return dates ? dates.length : 0;
  };

  const getMonthTotal = (monthIndex) => {
    if (!reportData) return 0;
    let total = 0;
    Object.values(reportData).forEach(dates => {
      if (hasScheduleInMonth(dates, monthIndex)) {
        total++;
      }
    });
    return total;
  };

  const getGrandTotal = () => {
    if (!reportData) return 0;
    return Object.values(reportData).reduce((sum, dates) => sum + (dates ? dates.length : 0), 0);
  };

  const getHostMonthTotal = (monthIndex) => {
    if (!hostReportData) return 0;
    let total = 0;
    Object.values(hostReportData).forEach(dates => {
      if (hasScheduleInMonth(dates, monthIndex)) {
        total++;
      }
    });
    return total;
  };

  const getHostGrandTotal = () => {
    if (!hostReportData) return 0;
    return Object.values(hostReportData).reduce((sum, dates) => sum + (dates ? dates.length : 0), 0);
  };

  const copyToClipboard = () => {
    if (!reportData) return;

    // Create tab-separated text for clipboard
    let text = `Family Name\t${months.join('\t')}\tTotal\n`;
    
    Object.entries(reportData).forEach(([familyName, dates]) => {
      text += `${familyName}\t`;
      months.forEach((month, monthIndex) => {
        text += `${hasScheduleInMonth(dates, monthIndex) ? 'Y' : ''}\t`;
      });
      text += `${getRowTotal(dates)}\n`;
    });

    // Add totals row
    text += `Total\t`;
    months.forEach((month, monthIndex) => {
      text += `${getMonthTotal(monthIndex)}\t`;
    });
    text += `${getGrandTotal()}\n`;

    // Copy to clipboard
    navigator.clipboard.writeText(text)
      .then(() => {
        alert('Report copied to clipboard! You can paste it into Excel or Google Sheets.');
      })
      .catch(err => {
        console.error('Failed to copy:', err);
        // Fallback method
        const textArea = document.createElement('textarea');
        textArea.value = text;
        document.body.appendChild(textArea);
        textArea.select();
        try {
          document.execCommand('copy');
          alert('Report copied to clipboard! You can paste it into Excel or Google Sheets.');
        } catch (err) {
          console.error('Fallback copy failed:', err);
          alert('Failed to copy. Please try again.');
        }
        document.body.removeChild(textArea);
      });
  };

  const exportToCSV = () => {
    if (!reportData) return;

    // Create CSV content
    let csv = `Family Name,${months.join(',')},Total\n`;
    
    Object.entries(reportData).forEach(([familyName, dates]) => {
      csv += `"${familyName}",`;
      months.forEach((month, monthIndex) => {
        csv += `${hasScheduleInMonth(dates, monthIndex) ? 'Y' : ''},`;
      });
      csv += `${getRowTotal(dates)}\n`;
    });

    // Add totals row
    csv += `Total,`;
    months.forEach((month, monthIndex) => {
      csv += `${getMonthTotal(monthIndex)},`;
    });
    csv += `${getGrandTotal()}\n`;

    // Create download link
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.setAttribute('href', url);
    link.setAttribute('download', `Vratin_Report_${year}.csv`);
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  const copyHostToClipboard = () => {
    if (!hostReportData) return;
    let text = `Host Family Name\t${months.join('\t')}\tTotal\n`;
    Object.entries(hostReportData).forEach(([hostName, dates]) => {
      text += `${hostName}\t`;
      months.forEach((month, monthIndex) => {
        text += `${hasScheduleInMonth(dates, monthIndex) ? 'Y' : ''}\t`;
      });
      text += `${getRowTotal(dates)}\n`;
    });
    text += `Total\t`;
    months.forEach((month, monthIndex) => {
      text += `${getHostMonthTotal(monthIndex)}\t`;
    });
    text += `${getHostGrandTotal()}\n`;
    navigator.clipboard.writeText(text)
      .then(() => {
        alert('Host Report copied to clipboard! You can paste it into Excel or Google Sheets.');
      })
      .catch(() => {
        const textArea = document.createElement('textarea');
        textArea.value = text;
        document.body.appendChild(textArea);
        textArea.select();
        try {
          document.execCommand('copy');
          alert('Host Report copied to clipboard!');
        } catch (err) {
          alert('Failed to copy. Please try again.');
        }
        document.body.removeChild(textArea);
      });
  };

  const exportHostToCSV = () => {
    if (!hostReportData) return;
    let csv = `Host Family Name,${months.join(',')},Total\n`;
    Object.entries(hostReportData).forEach(([hostName, dates]) => {
      csv += `"${hostName}",`;
      months.forEach((month, monthIndex) => {
        csv += `${hasScheduleInMonth(dates, monthIndex) ? 'Y' : ''},`;
      });
      csv += `${getRowTotal(dates)}\n`;
    });
    csv += `Total,`;
    months.forEach((month, monthIndex) => {
      csv += `${getHostMonthTotal(monthIndex)},`;
    });
    csv += `${getHostGrandTotal()}\n`;
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.setAttribute('href', url);
    link.setAttribute('download', `Host_Report_${year}.csv`);
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  return (
    <div className="vratin-report-container">
      <div className="section-title">Vratin Scheduler Report</div>

      <div className="report-type-tabs">
        <button
          className={`report-tab-btn${reportType === 'family' ? ' active' : ''}`}
          onClick={() => setReportType('family')}
        >
          Family Report
        </button>
        <button
          className={`report-tab-btn${reportType === 'host' ? ' active' : ''}`}
          onClick={() => setReportType('host')}
        >
          Host Report
        </button>
      </div>

      {reportType === 'family' && (
        <>
          <div className="report-controls">
            <label>
              Year:
              <input
                type="text"
                value={year}
                onChange={(e) => setYear(e.target.value)}
                placeholder="YYYY"
                className="year-input"
              />
            </label>
            <button onClick={fetchReport} disabled={loading} className="load-button">
              {loading ? 'Loading...' : 'Load Report'}
            </button>
            {reportData && (
              <>
                <button onClick={copyToClipboard} className="copy-button">
                  📋 Copy to Clipboard
                </button>
                <button onClick={exportToCSV} className="export-button">
                  📥 Export to CSV
                </button>
              </>
            )}
          </div>

          {reportData && (
            <div className="report-table-wrapper">
              <table className="vratin-report-table">
                <thead>
                  <tr>
                    <th className="family-column">Family Name</th>
                    {months.map(month => (
                      <th key={month} className="month-column">{month}</th>
                    ))}
                    <th className="total-column">Total</th>
                  </tr>
                </thead>
                <tbody>
                  {Object.entries(reportData).map(([familyName, dates], idx) => (
                    <tr key={idx}>
                      <td className="family-name-cell">{familyName}</td>
                      {months.map((month, monthIndex) => (
                        <td key={month} className="schedule-cell">
                          {hasScheduleInMonth(dates, monthIndex) ? 'Y' : ''}
                        </td>
                      ))}
                      <td className="total-cell">{getRowTotal(dates)}</td>
                    </tr>
                  ))}
                  <tr className="totals-row">
                    <td className="total-label"><strong>Total</strong></td>
                    {months.map((month, monthIndex) => (
                      <td key={month} className="month-total-cell">
                        <strong>{getMonthTotal(monthIndex)}</strong>
                      </td>
                    ))}
                    <td className="grand-total-cell">
                      <strong>{getGrandTotal()}</strong>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          )}
        </>
      )}

      {reportType === 'host' && (
        <>
          <div className="report-controls">
            <label>
              Year:
              <input
                type="text"
                value={year}
                onChange={(e) => setYear(e.target.value)}
                placeholder="YYYY"
                className="year-input"
              />
            </label>
            <button onClick={fetchHostReport} disabled={hostLoading} className="load-button">
              {hostLoading ? 'Loading...' : 'Load Host Report'}
            </button>
            {hostReportData && (
              <>
                <button onClick={copyHostToClipboard} className="copy-button">
                  📋 Copy to Clipboard
                </button>
                <button onClick={exportHostToCSV} className="export-button">
                  📥 Export to CSV
                </button>
              </>
            )}
          </div>

          {hostReportData && (
            <div className="report-table-wrapper">
              <table className="vratin-report-table">
                <thead>
                  <tr>
                    <th className="family-column">Host Family Name</th>
                    {months.map(month => (
                      <th key={month} className="month-column">{month}</th>
                    ))}
                    <th className="total-column">Total</th>
                  </tr>
                </thead>
                <tbody>
                  {Object.entries(hostReportData).map(([hostName, dates], idx) => (
                    <tr key={idx}>
                      <td className="family-name-cell">{hostName}</td>
                      {months.map((month, monthIndex) => (
                        <td key={month} className="schedule-cell">
                          {hasScheduleInMonth(dates, monthIndex) ? 'Y' : ''}
                        </td>
                      ))}
                      <td className="total-cell">{getRowTotal(dates)}</td>
                    </tr>
                  ))}
                  <tr className="totals-row">
                    <td className="total-label"><strong>Total</strong></td>
                    {months.map((month, monthIndex) => (
                      <td key={month} className="month-total-cell">
                        <strong>{getHostMonthTotal(monthIndex)}</strong>
                      </td>
                    ))}
                    <td className="grand-total-cell">
                      <strong>{getHostGrandTotal()}</strong>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          )}
        </>
      )}
    </div>
  );
}
