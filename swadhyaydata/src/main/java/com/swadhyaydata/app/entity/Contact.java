package com.swadhyaydata.app.entity;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "contacts", schema = "public")
public class Contact {

    @EmbeddedId
    private ContactId id;

    private String name;

    private String property_address;

    private String community_name;

    private LocalDateTime updated_datetime;

    private String user_id;

    private LocalDateTime created_datetime;

    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<Comments> comments;

    // Convenience getters/setters to keep existing code working
    public String getProperty_id() { return id != null ? id.getPropertyId() : null; }
    public void setProperty_id(String propertyId) {
        if (this.id == null) this.id = new ContactId();
        this.id.setPropertyId(propertyId);
    }

    public String getZip() { return id != null ? id.getZip() : null; }
    public void setZip(String zip) {
        if (this.id == null) this.id = new ContactId();
        this.id.setZip(zip);
    }

    public ContactId getId() { return id; }
    public void setId(ContactId id) { this.id = id; }

    public String getCommunity_name() { return community_name; }
    public void setCommunity_name(String community_name) { this.community_name = community_name; }

    public LocalDateTime getCreated_datetime() { return created_datetime; }
    public void setCreated_datetime(LocalDateTime created_datetime) { this.created_datetime = created_datetime; }

    public String getUser_id() { return user_id; }
    public void setUser_id(String user_id) { this.user_id = user_id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getProperty_address() { return property_address; }
    public void setProperty_address(String property_address) { this.property_address = property_address; }

    public LocalDateTime getUpdated_datetime() { return updated_datetime; }
    public void setUpdated_datetime(LocalDateTime updated_datetime) { this.updated_datetime = updated_datetime; }

    public List<Comments> getComments() { return comments; }
    public void setComments(List<Comments> comments) { this.comments = comments; }

    @Override
    public String toString() {
        return "Contact [property_id=" + getProperty_id() + ", name=" + name + ", property_address=" + property_address
                + ", community_name=" + community_name + ", zip=" + getZip() + ", user_id=" + user_id + "]";
    }
}