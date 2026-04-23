
import React, { useState, useEffect } from 'react';
import { ZipSelector } from './components/ZipSelector';
import { CommunitySelector } from './components/CommunitySelector';
import { YearInput } from './components/YearInput';
import { BulkActions } from './components/BulkActions';
import { ContactsTable } from './components/ContactsTable';
import { AppMenu } from './components/AppMenu';
import { Login } from './components/Login';
import { AddContactForm } from './components/AddContactForm';
import { ContactChatBot } from './components/ContactChatBot';
import { VratinScheduleDetails } from './components/VratinScheduleDetails';
import { VratinSchedulerReport } from './components/VratinSchedulerReport';
import { useNotification, Notification } from './components/useNotification';
import { API_ENDPOINTS } from './config/api';
import './App.css';

function App() {
  const [user, setUser] = useState(null);
  const [selectedMenu, setSelectedMenu] = useState('contacts');
  const [zips, setZips] = useState([]);
  const [selectedZip, setSelectedZip] = useState('');
  const [communities, setCommunities] = useState([]);
  const [selectedCommunity, setSelectedCommunity] = useState('');
  const [year, setYear] = useState('2025');
  const [contacts, setContacts] = useState([]);
  const [nameFilter, setNameFilter] = useState('');
  const [addressFilter, setAddressFilter] = useState('');
  const [showAddContactForm, setShowAddContactForm] = useState(false);
  const { notification, showNotification, hideNotification } = useNotification();

  useEffect(() => {
    fetch(API_ENDPOINTS.ZIP_AND_CITY)
      .then(res => res.json())
      .then(setZips)
      .catch(err => console.error('ZIP & City Error:', err));
  }, []);

  useEffect(() => {
    if (selectedZip) {
      fetch(`${API_ENDPOINTS.COMMUNITIES}/zip/${selectedZip}`)
        .then(res => res.json())
        .then(data => {
          setCommunities(data);
          setSelectedCommunity('');
        })
        .catch(err => console.error('Community Error:', err));
    }
  }, [selectedZip]);

  const fetchContacts = () => {
    if (!selectedZip || !selectedCommunity || !year) {
      alert('Please select ZIP, Community and enter Year.');
      return;
    }
    const url = `${API_ENDPOINTS.CONTACTS}/zip/${selectedZip}/community/${selectedCommunity}?commentYear=${year}`;
    fetch(url)
      .then(res => res.json())
      .then(data => {
        const editable = data.map(contact => {
          const notes = {};
          contact.comments?.forEach(c => {
            if (c.year === year) {
              notes[c.month] = c.note;
            }
          });
          // Store original notes for change detection
          return { ...contact, editableNotes: notes, originalNotes: { ...notes } };
        });
        setContacts(editable);
      })
      .catch(err => console.error('Contact Fetch Error:', err));
  };

  const handleNoteChange = (contactIndex, month, value) => {
    setContacts(prev => {
      const updated = [...prev];
      updated[contactIndex].editableNotes = {
        ...updated[contactIndex].editableNotes,
        [month]: value
      };
      return updated;
    });
  };

  const handleUpdate = (contact) => {
    // Compare editableNotes and originalNotes
    const hasChanged = Object.keys(contact.editableNotes || {}).some(month => {
      return contact.editableNotes[month] !== contact.originalNotes?.[month];
    });
    if (!hasChanged) {
      alert('No changes detected for this contact.');
      return;
    }
    const comments = Object.entries(contact.editableNotes)
      .filter(([_, note]) => note?.trim())
      .map(([month, note]) => ({ year, month, note }));
    const updatedContact = {
      property_id: contact.property_id,
      name: contact.name,
      property_address: contact.property_address,
      community_name: selectedCommunity,
      zip: selectedZip,
      comments,
      user_id: user?.username || '',
      updated_by: user?.username || ''
    };
    fetch(`${API_ENDPOINTS.UPDATE_CONTACTS}?commentYear=${year}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify([updatedContact])
    })
      .then(res => {
        if (!res.ok) throw new Error('Update failed');
        alert(`Updated: ${contact.property_id}`);
      })
      .catch(err => alert(`Error updating contact: ${err}`));
  };

  const handleBulkUpdate = () => {
    if (!(user?.roles === 'S' || user?.roles === 'W')) {
      alert('You do not have permission to perform bulk update.');
      return;
    }
    if (!year) {
      alert('Please enter a year first.');
      return;
    }
    // Only include contacts whose editableNotes have changed from originalNotes
    const contactsToUpdate = contacts
      .filter(contact => {
        // Check if any month value has changed
        return Object.keys(contact.editableNotes || {}).some(month => {
          return contact.editableNotes[month] !== contact.originalNotes?.[month];
        });
      })
      .map(contact => {
        const comments = Object.entries(contact.editableNotes || {})
          .filter(([_, note]) => note?.trim())
          .map(([month, note]) => ({ year, month, note }));
        return {
          property_id: contact.property_id,
          name: contact.name,
          property_address: contact.property_address,
          community_name: selectedCommunity,
          zip: selectedZip,
          comments,
          user_id: user?.username || '',
          updated_by: user?.username || ''
        };
      });
    if (contactsToUpdate.length === 0) {
      alert('No notes to update.');
      return;
    }
    fetch(`${API_ENDPOINTS.UPDATE_CONTACTS}?commentYear=${year}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(contactsToUpdate)
    })
      .then(res => {
        if (!res.ok) throw new Error('Bulk update failed');
        alert('Bulk update successful!');
      })
      .catch(err => alert(`Bulk update error: ${err}`));
  };

  const handleDownload = () => {
    if (!selectedZip || !selectedCommunity || !year) {
      alert('Please select ZIP, Community and enter Year.');
      return;
    }
    const downloadUrl = `${API_ENDPOINTS.DIVYASETU}/api/folder/zip/${selectedZip}/community/${selectedCommunity}?commentYear=${year}`;
    fetch(downloadUrl)
      .then(response => {
        if (!response.ok) throw new Error('Failed to download file.');
        return response.blob();
      })
      .then(blob => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `${selectedCommunity}.xlsx`;
        document.body.appendChild(a);
        a.click();
        a.remove();
      })
      .catch(err => {
        alert('Download error: ' + err.message);
      });
  };

  if (!user) {
    return <Login onLogin={setUser} />;
  }

  return (
    <>
      <AppMenu selectedMenu={selectedMenu} onSelect={setSelectedMenu} />
      <div style={{textAlign: 'right', padding: '8px 20px', margin: '0'}}>
        <button className="logout-btn" onClick={() => setUser(null)}>Logout</button>
      </div>
      <div className="main-content">
        {selectedMenu === 'contacts' && (
          <>
            <div className="section-title">Manage Contacts</div>
            <div style={{ display: 'flex', gap: '18px', marginBottom: '18px', flexWrap: 'wrap' }}>
              <ZipSelector zips={zips} selectedZip={selectedZip} onChange={setSelectedZip} />
              <CommunitySelector communities={communities} selectedCommunity={selectedCommunity} onChange={setSelectedCommunity} />
              <YearInput year={year} onChange={setYear} />
              <button onClick={fetchContacts}>Get Contacts</button>
              {(user?.roles === 'W' || user?.roles === 'S') && (
                <button onClick={() => setShowAddContactForm(true)}>Add New Contact</button>
              )}
            </div>
            {contacts.length > 0 && (
              <>
                <BulkActions onBulkUpdate={handleBulkUpdate} onDownload={handleDownload} userRole={user?.roles} />
                <ContactsTable
                  contacts={contacts}
                  nameFilter={nameFilter}
                  addressFilter={addressFilter}
                  onNoteChange={handleNoteChange}
                  onUpdate={handleUpdate}
                  setNameFilter={setNameFilter}
                  setAddressFilter={setAddressFilter}
                  userRole={user?.roles}
                />
              </>
            )}
          </>
        )}
        {selectedMenu === 'download' && (
          <>
            <div className="section-title">Download Excel</div>
            <div style={{ display: 'flex', gap: '18px', marginBottom: '18px', flexWrap: 'wrap' }}>
              <ZipSelector zips={zips} selectedZip={selectedZip} onChange={setSelectedZip} />
              <CommunitySelector communities={communities} selectedCommunity={selectedCommunity} onChange={setSelectedCommunity} />
              <YearInput year={year} onChange={setYear} />
              <button onClick={handleDownload}>Download Excel</button>
            </div>
          </>
        )}
        {selectedMenu === 'vratin-scheduler' && (
          <VratinScheduleDetails />
        )}
        {selectedMenu === 'vratin-report' && (
          <VratinSchedulerReport />
        )}
        {selectedMenu === 'contact-chat-bot' && (
          <ContactChatBot />
        )}
      </div>
      {showAddContactForm && (
        <AddContactForm
          selectedZip={selectedZip}
          selectedCommunity={selectedCommunity}
          user={user}
          onClose={() => setShowAddContactForm(false)}
          onSave={() => {
            // Optionally refresh contacts after adding
            if (contacts.length > 0) {
              fetchContacts();
            }
          }}
          showNotification={showNotification}
        />
      )}
      <Notification notification={notification} onClose={hideNotification} />
    </>
  );
}

export default App;