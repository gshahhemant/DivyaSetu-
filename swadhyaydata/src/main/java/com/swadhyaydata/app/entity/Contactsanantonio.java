package com.swadhyaydata.app.entity;

import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;




@Entity
@Table(name = "contacts_sanantonio", schema = "public")
public class Contactsanantonio {

    @Id
    private String  property_id;

    private String name;

    private String property_address;
    
    private String community_name;
    
    private String zip;
    
    private String details_url;
    
    
    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<Comments> comments;

    // Getters and setters
    
    
    

    public String getCommunity_name() {
		return community_name;
	}

	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	

	public List<Comments> getComments() {
		return comments;
	}

	public void setComments(List<Comments> comments) {
		this.comments = comments;
	}

	public void setCommunity_name(String community_name) {
		this.community_name = community_name;
	}

	public String getProperty_id() {
        return property_id;
    }

    public void setProperty_id(String property_id) {
        this.property_id = property_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProperty_address() {
        return property_address;
    }

    public void setProperty_address(String property_address) {
        this.property_address = property_address;
    }

	public String getDetails_url() {
		return details_url;
	}

	public void setDetails_url(String details_url) {
		this.details_url = details_url;
	}
    
    
}
