package com.swadhyaydata.app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "vratin_family")
public class VratinFamily {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "sr_no")
	private Long srNo;

	@Column(name = "name", nullable = false, length = 40)
	private String name;

	@Column(name = "mob", length = 20)
	private String mob;

	// Constructors
	public VratinFamily() {
	}

	public VratinFamily(String name, String mob) {
		this.name = name;
		this.mob = mob;
	}

	// Getters and Setters
	public Long getSrNo() {
		return srNo;
	}

	public void setSrNo(Long srNo) {
		this.srNo = srNo;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMob() {
		return mob;
	}

	public void setMob(String mob) {
		this.mob = mob;
	}

	@Override
	public String toString() {
		return "VratinFamily [srNo=" + srNo + ", name=" + name + ", mob=" + mob + "]";
	}
}
