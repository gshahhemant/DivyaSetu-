package com.swadhyaydata.app.entity;

public class CityZipDTO {

	

	public CityZipDTO() {
		super();
	}

	public CityZipDTO(String cityname, String zipcode) {
		super();
		this.cityname = cityname;
		this.zipcode = zipcode;
	}

	private String cityname;

      
    private String zipcode;

    

	public String getCityname() {
		return cityname;
	}

	public void setCityname(String cityname) {
		this.cityname = cityname;
	}

	

	public String getZipcode() {
		return zipcode;
	}

	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}

    
    
}
