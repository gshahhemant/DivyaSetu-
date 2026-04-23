import React, { useState, useEffect } from 'react';
import { API_ENDPOINTS } from '../config/api';

export function AddContactForm({ selectedZip, selectedCommunity, user, onClose, onSave, showNotification }) {
  const [formData, setFormData] = useState({
    propertyId: '',
    name: '',
    propertyAddress: '',
    zip: selectedZip || '',
    communityName: selectedCommunity || ''
  });
  
  const [allCommunities, setAllCommunities] = useState([]);
  const [loading, setLoading] = useState(true);
  const [zipAutoFilled, setZipAutoFilled] = useState(false);

  // Fetch all communities when component mounts
  useEffect(() => {
    const fetchAllCommunities = async () => {
      try {
        setLoading(true);
        const response = await fetch(API_ENDPOINTS.ALL_COMMUNITIES);
        if (response.ok) {
          const communities = await response.json();
          setAllCommunities(communities);
        } else {
          console.error('Failed to fetch communities');
          showNotification('Failed to load communities', 'error');
        }
      } catch (error) {
        console.error('Error fetching communities:', error);
        showNotification('Error loading communities', 'error');
      } finally {
        setLoading(false);
      }
    };

    fetchAllCommunities();
  }, [showNotification]);

  const handleChange = (e) => {
    setFormData(prev => ({
      ...prev,
      [e.target.name]: e.target.value
    }));
    
    // Reset auto-fill flag if zip is manually changed
    if (e.target.name === 'zip') {
      setZipAutoFilled(false);
    }
  };

  const handleCommunityChange = (e) => {
    const selectedCommunityName = e.target.value;
    
    // Find the selected community object to get its zipcode
    const selectedCommunityObj = allCommunities.find(community => {
      const communityName = typeof community === 'string' ? community : community.name || community.community_name;
      return communityName === selectedCommunityName;
    });

    // Check if community has zipcode
    const communityZip = selectedCommunityObj && (selectedCommunityObj.zip || selectedCommunityObj.zipcode);
    
    // Update both community name and zipcode
    setFormData(prev => ({
      ...prev,
      communityName: selectedCommunityName,
      // Auto-populate zipcode if available in community data
      zip: communityZip || prev.zip
    }));
    
    // Set flag if zip was auto-filled
    setZipAutoFilled(!!communityZip);
  };

  const handleSave = async () => {
    if (!formData.propertyId || !formData.name || !formData.propertyAddress) {
      showNotification('Please fill in all required fields.', 'error');
      return;
    }

    const contactData = {
      property_id: formData.propertyId,
      name: formData.name,
      property_address: formData.propertyAddress,
      zip: formData.zip,
      community_name: formData.communityName,
      user_id: user?.username || '',
      created_by: user?.username || ''
    };

    try {
      const response = await fetch(API_ENDPOINTS.ADD_CONTACT, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(contactData)
      });

      if (response.ok) {
        // Get the response text to show the API message
        const responseMessage = await response.text();
        showNotification(responseMessage || 'Contact added successfully!', 'success');
        onSave();
        handleClear();
        onClose();
      } else {
        // Get error message from API if available
        const errorMessage = await response.text();
        showNotification(errorMessage || 'Failed to add contact. Please try again.', 'error');
      }
    } catch (error) {
      showNotification('Error adding contact: ' + error.message, 'error');
    }
  };

  const handleClear = () => {
    setFormData({
      propertyId: '',
      name: '',
      propertyAddress: '',
      zip: selectedZip || '',
      communityName: selectedCommunity || ''
    });
    setZipAutoFilled(false);
  };

  return (
    <div className="modal-overlay">
      <div className="modal-content">
        <h3>Add New Contact</h3>
        <div className="form-group">
          <label>Property ID: <span style={{color: 'red'}}>*</span></label>
          <div className="property-id-hint">
            <small style={{color: '#666', fontSize: '12px', fontStyle: 'italic'}}>
              Get Property ID from county website
            </small>
          </div>
          <input
            type="text"
            name="propertyId"
            value={formData.propertyId}
            onChange={handleChange}
            required
          />
        </div>
        <div className="form-group">
          <label>Name: <span style={{color: 'red'}}>*</span></label>
          <input
            type="text"
            name="name"
            value={formData.name}
            onChange={handleChange}
            required
          />
        </div>
        <div className="form-group">
          <label>Property Address: <span style={{color: 'red'}}>*</span></label>
          <input
            type="text"
            name="propertyAddress"
            value={formData.propertyAddress}
            onChange={handleChange}
            required
          />
        </div>
        <div className="form-group">
          <label>Zip:</label>
          <div style={{ position: 'relative' }}>
            <input
              type="text"
              name="zip"
              value={formData.zip}
              onChange={handleChange}
              placeholder="Auto-filled when community selected"
              style={{
                backgroundColor: zipAutoFilled ? '#e8f5e8' : '#fff',
                borderColor: zipAutoFilled ? '#28a745' : '#ddd'
              }}
            />
            {zipAutoFilled && (
              <small style={{
                color: '#28a745',
                fontSize: '11px',
                fontStyle: 'italic',
                marginTop: '2px',
                display: 'block'
              }}>
                ✓ Auto-filled from community
              </small>
            )}
          </div>
        </div>
        <div className="form-group">
          <label>Community Name:</label>
          {loading ? (
            <div style={{ padding: '8px 12px', color: '#666', fontStyle: 'italic' }}>
              Loading communities...
            </div>
          ) : (
            <select
              name="communityName"
              value={formData.communityName}
              onChange={handleCommunityChange}
              style={{
                width: '100%',
                padding: '8px 12px',
                border: '1px solid #ddd',
                borderRadius: '4px',
                fontSize: '14px'
              }}
            >
              <option value="">Select a community</option>
              {allCommunities.map((community, index) => {
                const communityName = typeof community === 'string' ? community : 
                                    community.name || community.community_name || community.communityName;
                const zipCode = typeof community === 'object' ? 
                              (community.zip || community.zipcode || community.zipCode) : '';
                
                return (
                  <option key={index} value={communityName}>
                    {communityName} {zipCode && `(${zipCode})`}
                  </option>
                );
              })}
            </select>
          )}
        </div>
        <div className="form-buttons">
          <button onClick={handleSave} className="save-btn">Save</button>
          <button onClick={handleClear} className="clear-btn">Clear</button>
          <button onClick={onClose} className="cancel-btn">Cancel</button>
        </div>
      </div>
    </div>
  );
}
