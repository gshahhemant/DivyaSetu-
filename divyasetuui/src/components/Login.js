import React, { useState } from 'react';
import { API_ENDPOINTS } from '../config/api';

export function Login({ onLogin }) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      const res = await fetch(API_ENDPOINTS.LOGIN, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password, roles: '' })
      });
      const data = await res.json();
      if (data && data.username) {
        onLogin(data);
      } else {
        setError('Invalid username or password.');
      }
    } catch {
      setError('Login failed. Please try again.');
    }
  };

  return (
    <div className="login-fancy-bg">
      <div className="login-card">
        <div className="login-avatar" style={{marginTop: '0', marginBottom: '18px', overflow: 'hidden', height: '70px', display: 'flex', alignItems: 'flex-start', justifyContent: 'center'}}>
          <img src="/images/userpic.jpg" alt="user" style={{width: '70px', height: '70px', borderRadius: '50%', objectFit: 'cover', objectPosition: 'top'}} />
        </div>
        <h2 className="login-title">Welcome to DivyaSetu</h2>
        <form className="login-form" onSubmit={handleSubmit}>
          <div className="login-field">
            <label htmlFor="username">Username</label>
            <input id="username" value={username} onChange={e => setUsername(e.target.value)} required autoFocus />
          </div>
          <div className="login-field">
            <label htmlFor="password">Password</label>
            <input id="password" type="password" value={password} onChange={e => setPassword(e.target.value)} required />
          </div>
          <button className="login-btn" type="submit">Login</button>
          {error && <div className="login-error">{error}</div>}
        </form>
      </div>
    </div>
  );
}
