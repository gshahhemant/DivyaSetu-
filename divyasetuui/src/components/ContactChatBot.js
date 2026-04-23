import React, { useEffect, useRef, useState } from 'react';
import { API_ENDPOINTS } from '../config/api';

async function searchContactChatBot(searchQuery, searchLimit) {
  const response = await fetch(API_ENDPOINTS.CONTACT_CHATBOT_SEARCH, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      query: searchQuery.trim() || 'sdss',
      maxResults: Math.min(5, Math.max(1, Number(searchLimit) || 5))
    })
  });

  if (!response.ok) {
    let errMsg = `Request failed with status ${response.status}`;
    try {
      const errBody = await response.json();
      if (errBody?.error?.message) errMsg = errBody.error.message;
      else if (errBody?.message) errMsg = errBody.message;
    } catch (_) {}
    throw new Error(errMsg);
  }

  return response.json();
}

function parseSnippet(snippet) {
  const result = {};
  if (!snippet) return result;
  snippet.split(';').forEach(part => {
    const idx = part.indexOf(':');
    if (idx === -1) return;
    const key = part.slice(0, idx).trim();
    const value = part.slice(idx + 1).trim();
    if (key && value) result[key] = value;
  });
  return result;
}

function MatchCard({ match, index }) {
  const f = parseSnippet(match.textSnippet);
  const name = f.name || '—';
  const address = f.property_address || '—';
  const community = f.community_name || '—';
  const propertyId = f.property_id || null;
  const zip = f.zip || null;
  const detailsUrl = f.details_url && f.details_url.startsWith('http') ? f.details_url : null;
  const relevance = match.score ? Math.round(match.score * 100) : null;

  return (
    <div className="cb-match-card">
      <div className="cb-match-header">
        <span className="cb-match-index">#{index + 1}</span>
        <span className="cb-match-name">{name}</span>
        {relevance !== null && (
          <span className={`cb-match-score ${relevance >= 70 ? 'cb-score-high' : relevance >= 60 ? 'cb-score-mid' : 'cb-score-low'}`}>
            {relevance}% match
          </span>
        )}
      </div>
      <div className="cb-match-fields">
        <div className="cb-match-field">
          <span className="cb-match-label">&#x1F3E0; Address</span>
          <span className="cb-match-value">{address}</span>
        </div>
        <div className="cb-match-field">
          <span className="cb-match-label">&#x1F3D8; Community</span>
          <span className="cb-match-value">{community}</span>
        </div>
        {propertyId && (
          <div className="cb-match-field">
            <span className="cb-match-label">&#x1F4CB; Property ID</span>
            <span className="cb-match-value">{propertyId}</span>
          </div>
        )}
        {zip && (
          <div className="cb-match-field">
            <span className="cb-match-label">&#x1F4CD; ZIP</span>
            <span className="cb-match-value">{zip}</span>
          </div>
        )}
        {detailsUrl && (
          <div className="cb-match-field">
            <span className="cb-match-label">&#x1F517; Details</span>
            <a className="cb-match-link" href={detailsUrl} target="_blank" rel="noreferrer">{detailsUrl}</a>
          </div>
        )}
      </div>
    </div>
  );
}

function MatchResults({ matches }) {
  const [expanded, setExpanded] = React.useState(false);
  return (
    <div className="cb-results-section">
      <button className="cb-toggle-results-btn" onClick={() => setExpanded(e => !e)}>
        {expanded ? `Hide results ▲` : `Show ${matches.length} result(s) ▼`}
      </button>
      {expanded && (
        <div className="cb-results-list">
          {matches.map((match, i) => (
            <MatchCard key={i} match={match} index={i} />
          ))}
        </div>
      )}
    </div>
  );
}

export function ContactChatBot() {
  const [messages, setMessages] = useState([
    { id: 0, role: 'bot', text: 'Hello! I can help you find contact information. Try asking something like "What is the address of Parth Modi?" or "Find contacts in ZIP 78641".' }
  ]);
  const [inputText, setInputText] = useState('');
  const [maxResults, setMaxResults] = useState(5);
  const [loading, setLoading] = useState(false);
  const messagesEndRef = useRef(null);
  const inputRef = useRef(null);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, loading]);

  const sendMessage = async (event) => {
    event.preventDefault();
    const text = inputText.trim();
    if (!text || loading) return;

    setMessages(prev => [...prev, { id: Date.now(), role: 'user', text }]);
    setInputText('');
    setLoading(true);

    try {
      const data = await searchContactChatBot(text, maxResults);
      const answer = data?.answer || null;
      const matches = Array.isArray(data?.matches)
        ? data.matches
        : Array.isArray(data)
          ? data
          : Array.isArray(data?.results)
            ? data.results
            : null;

      const botText = answer
        || (matches && matches.length > 0 ? `Found ${matches.length} result(s) for "${text}":` : `No results found for "${text}".`);

      setMessages(prev => [...prev, {
        id: Date.now() + 1,
        role: 'bot',
        text: botText,
        matches,
        rawData: (!answer && !matches) ? data : null
      }]);
    } catch (err) {
      setMessages(prev => [...prev, {
        id: Date.now() + 1,
        role: 'bot',
        text: `Error: ${err.message || 'Unable to fetch response.'}`,
        isError: true
      }]);
    } finally {
      setLoading(false);
      inputRef.current?.focus();
    }
  };

  return (
    <div className="cb-page">
      <div className="section-title">Contact Chat Bot</div>

      <div className="cb-window">
        <div className="cb-header">
          <div className="cb-avatar">🤖</div>
          <div>
            <div className="cb-header-name">DivyaSetu AI Assistant</div>
            <div className="cb-header-status">Contact Directory Search</div>
          </div>
        </div>

        <div className="cb-messages">
          {messages.map(msg => (
            <div key={msg.id} className={`cb-row cb-row-${msg.role}`}>
              {msg.role === 'bot' && <div className="cb-bot-avatar">🤖</div>}
              <div className={`cb-bubble cb-bubble-${msg.role}${msg.isError ? ' cb-bubble-error' : ''}`}>
                <p className="cb-bubble-text">{msg.text}</p>
                {msg.matches && msg.matches.length > 0 && (
                  <MatchResults matches={msg.matches} />
                )}
                {msg.rawData && (
                  <pre className="cb-result-raw">{JSON.stringify(msg.rawData, null, 2)}</pre>
                )}
              </div>
            </div>
          ))}

          {loading && (
            <div className="cb-row cb-row-bot">
              <div className="cb-bot-avatar">🤖</div>
              <div className="cb-bubble cb-bubble-bot">
                <div className="cb-typing">
                  <span></span><span></span><span></span>
                </div>
              </div>
            </div>
          )}

          <div ref={messagesEndRef} />
        </div>

        <form className="cb-input-bar" onSubmit={sendMessage}>

          <input
            ref={inputRef}
            className="cb-input"
            type="text"
            value={inputText}
            onChange={e => setInputText(e.target.value)}
            placeholder="Type your question…"
            disabled={loading}
            autoFocus
          />
          <div className="cb-input-opts">
            <input
              className="cb-max-input"
              type="number"
              min="1"
              max="5"
              value={maxResults}
              onChange={e => setMaxResults(Math.min(5, Math.max(1, Number(e.target.value) || 1)))}
              title="Max results"
            />
          </div>
          <button className="cb-send-btn" type="submit" disabled={loading || !inputText.trim()}>
            &#9658;
          </button>
        </form>
      </div>
    </div>
  );
}