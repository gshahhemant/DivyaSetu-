package com.swadhyaydata.app.entity;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class ContactId implements Serializable {

    @Column(name = "property_id", nullable = false)
    private String propertyId;

    @Column(name = "zip", nullable = false)
    private String zip;

    public ContactId() {}

    public ContactId(String propertyId, String zip) {
        this.propertyId = propertyId;
        this.zip = zip;
    }

    public String getPropertyId() { return propertyId; }
    public void setPropertyId(String propertyId) { this.propertyId = propertyId; }

    public String getZip() { return zip; }
    public void setZip(String zip) { this.zip = zip; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContactId)) return false;
        ContactId that = (ContactId) o;
        return Objects.equals(propertyId, that.propertyId) &&
               Objects.equals(zip, that.zip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(propertyId, zip);
    }
}