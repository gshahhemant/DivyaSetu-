package com.swadhyaydata.app.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for Schedule Mapping Response with Host and Families
 */
public class ScheduleMappingResponseDTO {
	private Long scheduleSrno;
	private String hostName;
	private String hostAddress;
	private String hostMobile;
	private List<VratiFamilyDTO> vratiFamilies;

	public ScheduleMappingResponseDTO() {
		this.vratiFamilies = new ArrayList<>();
	}

	public ScheduleMappingResponseDTO(Long scheduleSrno, String hostName, String hostAddress, String hostMobile) {
		this.scheduleSrno = scheduleSrno;
		this.hostName = hostName;
		this.hostAddress = hostAddress;
		this.hostMobile = hostMobile;
		this.vratiFamilies = new ArrayList<>();
	}

	public Long getScheduleSrno() {
		return scheduleSrno;
	}

	public void setScheduleSrno(Long scheduleSrno) {
		this.scheduleSrno = scheduleSrno;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public String getHostAddress() {
		return hostAddress;
	}

	public void setHostAddress(String hostAddress) {
		this.hostAddress = hostAddress;
	}

	public String getHostMobile() {
		return hostMobile;
	}

	public void setHostMobile(String hostMobile) {
		this.hostMobile = hostMobile;
	}

	public List<VratiFamilyDTO> getVratiFamilies() {
		return vratiFamilies;
	}

	public void setVratiFamilies(List<VratiFamilyDTO> vratiFamilies) {
		this.vratiFamilies = vratiFamilies;
	}

	public void addVratiFamily(VratiFamilyDTO family) {
		this.vratiFamilies.add(family);
	}
}
