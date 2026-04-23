package com.swadhyaydata.app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "vratin_host")
public class VratinHost {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "sr_no")
	private Long srNo;

	@Column(name = "name", nullable = false, length = 40)
	private String name;

	@Column(name = "address", length = 200)
	private String address;

	@Column(name = "mob", length = 20)
	private String mob;

	// Constructors
	public VratinHost() {
	}

	public VratinHost(String name, String address, String mob) {
		this.name = name;
		this.address = address;
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

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getMob() {
		return mob;
	}

	public void setMob(String mob) {
		this.mob = mob;
	}

	@Override
	public String toString() {
		return "VratinHost [srNo=" + srNo + ", name=" + name + ", address=" + address + ", mob=" + mob + "]";
	}
}
