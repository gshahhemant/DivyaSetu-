import { useState, useCallback } from 'react';

export const useNotification = () => {
  const [notification, setNotification] = useState(null);

  const showNotification = useCallback((message, type = 'info') => {
    setNotification({ message, type });
  }, []);

  const hideNotification = useCallback(() => {
    setNotification(null);
  }, []);

  return {
    notification,
    showNotification,
    hideNotification
  };
};

export const Notification = ({ notification, onClose }) => {
  if (!notification) return null;

  return (
    <div className={`notification ${notification.type}`}>
      <div className="notification-message">
        {notification.message}
      </div>
      <button className="notification-ok-btn" onClick={onClose}>
        OK
      </button>
    </div>
  );
};
