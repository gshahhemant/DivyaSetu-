package com.swadhyaydata.app.entity;


import jakarta.persistence.*;

@Entity
@Table(name = "communities", schema = "public")
public class Community {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-generate the primary key value
    private Long id; // Primary key field
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	private String cityname;

    private String communityname;
    
    private String zipcode;

    private String state;

	public String getCityname() {
		return cityname;
	}

	public void setCityname(String cityname) {
		this.cityname = cityname;
	}

	public String getCommunityname() {
		return communityname;
	}

	public void setCommunityname(String communityname) {
		this.communityname = communityname;
	}

	public String getZipcode() {
		return zipcode;
	}

	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}
    
    
}
