// API Configuration
//export const API_BASE_URL = 'http://localhost:8080';
export const API_BASE_URL = 'http://192.168.0.250:8080';

// API Endpoints
export const API_ENDPOINTS = {
  BASE: API_BASE_URL,
  DIVYASETU: `${API_BASE_URL}/divyasetu`,
  CONTACT_CHATBOT_BASE: 'http://localhost:8085/angeticai',
  
  // Auth endpoints
  LOGIN: `${API_BASE_URL}/divyasetu/login/validateuser`,
  
  // Data endpoints
  ZIP_AND_CITY: `${API_BASE_URL}/divyasetu/api/zipandcity`,
  COMMUNITIES: `${API_BASE_URL}/divyasetu/api/communities`,
  ALL_COMMUNITIES: `${API_BASE_URL}/divyasetu/api/communities`,
  CONTACTS: `${API_BASE_URL}/divyasetu/api/contacts`,
  ADD_CONTACT: `${API_BASE_URL}/divyasetu/api/addcontact`,
  UPDATE_CONTACTS: `${API_BASE_URL}/divyasetu/api/updatecontacts`,
  DOWNLOAD: `${API_BASE_URL}/divyasetu/api/download`,
  CONTACT_CHATBOT_SEARCH: 'http://192.168.0.250:8085/angeticai/api/pdf/search'
};

// Helper function to build dynamic URLs
export const buildApiUrl = (endpoint, params = {}) => {
  let url = endpoint;
  Object.keys(params).forEach(key => {
    url = url.replace(`{${key}}`, params[key]);
  });
  return url;
};
