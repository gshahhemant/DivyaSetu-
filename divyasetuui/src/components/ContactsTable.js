import React from 'react';

const monthMap = {
  "1": "JAN", "2": "FEB", "3": "MAR", "4": "APR", "5": "MAY", "6": "JUN",
  "7": "JUL", "8": "AUG", "9": "SEP", "10": "OCT", "11": "NOV", "12": "DEC"
};

export function ContactsTable({ contacts, nameFilter, addressFilter, onNoteChange, onUpdate, setNameFilter, setAddressFilter, userRole }) {
  const filteredContacts = contacts.filter(contact =>
    contact.name.toLowerCase().includes(nameFilter.toLowerCase()) &&
    contact.property_address.toLowerCase().includes(addressFilter.toLowerCase())
  );

  return (
    <div style={{ marginTop: '30px' }}>
      <h3>Contact Data</h3>
      <div align="right">1 - Not Home. 2 - Good Conversation. 3 - Met At Door. 4 - Not Yet. 5 - Rental. 6 - Visit Again.</div>
      <div className="contacts-table-wrapper">
        <table className="contacts-table" border="1" cellPadding="5" style={{ borderCollapse: 'collapse', width: '100%' }}>
          <thead>
            <tr>
              <th>No</th>
              <th>Name   <input type="text" value={nameFilter} onChange={e => setNameFilter(e.target.value)} placeholder="Type name" style={{ marginRight: '10px' }} /></th>
              <th>Address     <input type="text" value={addressFilter} onChange={e => setAddressFilter(e.target.value)} placeholder="Type address" />  </th>
              {Array.from({ length: 12 }, (_, i) => {
                const monthNum = (i + 1).toString();
                return <th key={monthNum} className="month-col">{monthMap[monthNum]}</th>;
              })}
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            {filteredContacts.map((contact, index) => {
              const contactIndex = contacts.findIndex(c => c.property_id === contact.property_id);
              return (
                <tr key={contact.property_id}>
                  <td>{index + 1}</td>
                  <td>{contact.name}</td>
                  <td>{contact.property_address}</td>
                  {Array.from({ length: 12 }, (_, i) => {
                    const monthNum = (i + 1).toString();
                    return (
                      <td key={monthNum} className="month-col">
                        <input
                          type="text"
                          maxLength={2}
                          value={contact.editableNotes?.[monthNum] || ''}
                          onChange={e => onNoteChange(contactIndex, monthNum, e.target.value)}
                        />
                      </td>
                    );
                  })}
                  <td>
                    {(userRole === 'W' || userRole === 'S') ? (
                      <button onClick={() => onUpdate(contact)}>Update</button>
                    ) : null}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}
