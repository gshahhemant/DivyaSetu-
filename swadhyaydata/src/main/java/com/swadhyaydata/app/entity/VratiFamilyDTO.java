package com.swadhyaydata.app.entity;

/**
 * DTO for Vrati Family details in schedule mapping
 */
public class VratiFamilyDTO {
	private String vratiFamilyName;
	private String vratiFamilyMob;

	public VratiFamilyDTO() {
	}

	public VratiFamilyDTO(String vratiFamilyName, String vratiFamilyMob) {
		this.vratiFamilyName = vratiFamilyName;
		this.vratiFamilyMob = vratiFamilyMob;
	}

	public String getVratiFamilyName() {
		return vratiFamilyName;
	}

	public void setVratiFamilyName(String vratiFamilyName) {
		this.vratiFamilyName = vratiFamilyName;
	}

	public String getVratiFamilyMob() {
		return vratiFamilyMob;
	}

	public void setVratiFamilyMob(String vratiFamilyMob) {
		this.vratiFamilyMob = vratiFamilyMob;
	}
}
