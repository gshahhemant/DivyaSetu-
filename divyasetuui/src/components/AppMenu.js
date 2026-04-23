import React from 'react';

export function AppMenu({ selectedMenu, onSelect }) {
  return (
    <nav className="app-menu">
      <div className="menu-title">DivyaSetu Contacts</div>
      <ul>
        <li className={selectedMenu === 'contacts' ? 'active' : ''} onClick={() => onSelect('contacts')}>Contacts</li>
        <li className={selectedMenu === 'download' ? 'active' : ''} onClick={() => onSelect('download')}>Download</li>
        <li className={selectedMenu === 'vratin-scheduler' ? 'active' : ''} onClick={() => onSelect('vratin-scheduler')}>Vrati</li>
        <li className={selectedMenu === 'contact-chat-bot' ? 'active' : ''} onClick={() => onSelect('contact-chat-bot')}>Contact Chat Bot</li>
        {/* Add more menu items as needed */}
      </ul>
    </nav>
  );
}
