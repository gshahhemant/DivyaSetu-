package com.swadhyaydata.app.entity;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "vratin_schedule")
public class VratinSchedule {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "sr_no")
	private Long srNo;

	@Column(name = "schedule_date", nullable = false)
	@JsonFormat(pattern = "yyyy-MMM-dd")
	private LocalDate scheduleDate;

	@Column(name = "day", nullable = false, length = 20)
	private String day;

	@Column(name = "location", length = 200)
	private String location;

	// Constructors
	public VratinSchedule() {
	}

	public VratinSchedule(LocalDate scheduleDate, String day) {
		this.scheduleDate = scheduleDate;
		this.day = day;
	}

	public VratinSchedule(LocalDate scheduleDate, String day, String location) {
		this.scheduleDate = scheduleDate;
		this.day = day;
		this.location = location;
	}

	// Getters and Setters
	public Long getSrNo() {
		return srNo;
	}

	public void setSrNo(Long srNo) {
		this.srNo = srNo;
	}

	public LocalDate getScheduleDate() {
		return scheduleDate;
	}

	public void setScheduleDate(LocalDate scheduleDate) {
		this.scheduleDate = scheduleDate;
	}

	public String getDay() {
		return day;
	}

	public void setDay(String day) {
		this.day = day;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	@Override
	public String toString() {
		return "VratinSchedule [srNo=" + srNo + ", scheduleDate=" + scheduleDate + ", day=" + day 
				+ ", location=" + location + "]";
	}
}
