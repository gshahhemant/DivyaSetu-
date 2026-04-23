package com.swadhyaydata.app.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class VratinScheduleMappingId implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = "schedule_srno")
	private Long scheduleSrno;

	@Column(name = "host_srno")
	private Long hostSrno;

	@Column(name = "family_srno")
	private Long familySrno;

	// Constructors
	public VratinScheduleMappingId() {
	}

	public VratinScheduleMappingId(Long scheduleSrno, Long hostSrno, Long familySrno) {
		this.scheduleSrno = scheduleSrno;
		this.hostSrno = hostSrno;
		this.familySrno = familySrno;
	}

	// Getters and Setters
	public Long getScheduleSrno() {
		return scheduleSrno;
	}

	public void setScheduleSrno(Long scheduleSrno) {
		this.scheduleSrno = scheduleSrno;
	}

	public Long getHostSrno() {
		return hostSrno;
	}

	public void setHostSrno(Long hostSrno) {
		this.hostSrno = hostSrno;
	}

	public Long getFamilySrno() {
		return familySrno;
	}

	public void setFamilySrno(Long familySrno) {
		this.familySrno = familySrno;
	}

	// equals and hashCode
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		VratinScheduleMappingId that = (VratinScheduleMappingId) o;
		return Objects.equals(scheduleSrno, that.scheduleSrno) &&
				Objects.equals(hostSrno, that.hostSrno) &&
				Objects.equals(familySrno, that.familySrno);
	}

	@Override
	public int hashCode() {
		return Objects.hash(scheduleSrno, hostSrno, familySrno);
	}
}
