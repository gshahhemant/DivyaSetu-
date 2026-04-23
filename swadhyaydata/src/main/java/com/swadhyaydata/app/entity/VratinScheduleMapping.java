package com.swadhyaydata.app.entity;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "vratin_schedule_mapping")
public class VratinScheduleMapping {

	@EmbeddedId
	@JsonUnwrapped
	private VratinScheduleMappingId id;

	// Constructors
	public VratinScheduleMapping() {
	}

	public VratinScheduleMapping(VratinScheduleMappingId id) {
		this.id = id;
	}

	public VratinScheduleMapping(Long scheduleSrno, Long hostSrno, Long familySrno) {
		this.id = new VratinScheduleMappingId(scheduleSrno, hostSrno, familySrno);
	}

	// Getters and Setters
	public VratinScheduleMappingId getId() {
		return id;
	}

	public void setId(VratinScheduleMappingId id) {
		this.id = id;
	}

	// Convenience getters
	public Long getScheduleSrno() {
		return id != null ? id.getScheduleSrno() : null;
	}

	public Long getHostSrno() {
		return id != null ? id.getHostSrno() : null;
	}

	public Long getFamilySrno() {
		return id != null ? id.getFamilySrno() : null;
	}

	@Override
	public String toString() {
		return "VratinScheduleMapping [scheduleSrno=" + getScheduleSrno() + 
				", hostSrno=" + getHostSrno() + 
				", familySrno=" + getFamilySrno() + "]";
	}
}
