package com.swadhyaydata.app.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.swadhyaydata.app.entity.ScheduleMappingResponseDTO;
import com.swadhyaydata.app.entity.VratiFamilyDTO;
import com.swadhyaydata.app.entity.VratinFamily;
import com.swadhyaydata.app.entity.VratinHost;
import com.swadhyaydata.app.entity.VratinSchedule;
import com.swadhyaydata.app.entity.VratinScheduleMapping;
import com.swadhyaydata.app.entity.VratinScheduleMappingId;
import com.swadhyaydata.app.repository.VratinFamilyRepository;
import com.swadhyaydata.app.repository.VratinHostRepository;
import com.swadhyaydata.app.repository.VratinScheduleMappingRepository;
import com.swadhyaydata.app.repository.VratinScheduleRepository;

@Service
public class VratinSchedulerService {

	@Autowired
	private VratinHostRepository vratinHostRepository;

	@Autowired
	private VratinScheduleRepository vratinScheduleRepository;

	@Autowired
	private VratinFamilyRepository vratinFamilyRepository;

	@Autowired
	private VratinScheduleMappingRepository vratinScheduleMappingRepository;

	/**
	 * Get all vrati hosts ordered by name
	 * @return List of VratinHost ordered by name
	 */
	public List<VratinHost> getAllVratinHosts() {
		return vratinHostRepository.findAllByOrderByNameAsc();
	}

	/**
	 * Get vrati host by sr_no
	 * @param srNo
	 * @return VratinHost
	 */
	public VratinHost getVratinHostBySrNo(Long srNo) {
		return vratinHostRepository.findById(srNo).orElse(null);
	}

	/**
	 * Save or update vrati host
	 * @param vratinHost
	 * @return VratinHost
	 */
	public VratinHost saveVratinHost(VratinHost vratinHost) {
		return vratinHostRepository.save(vratinHost);
	}

	/**
	 * Delete vrati host by sr_no
	 * @param srNo
	 */
	public void deleteVratinHost(Long srNo) {
		vratinHostRepository.deleteById(srNo);
	}

	// ========== VratinSchedule Methods ==========

	/**
	 * Get all vrati schedules ordered by sr_no
	 * @return List of VratinSchedule ordered by sr_no
	 */
	public List<VratinSchedule> getAllVratinSchedules() {
		return vratinScheduleRepository.findAllByOrderBySrNoAsc();
	}

	/**
	 * Get all vrati schedules ordered by schedule_date
	 * @return List of VratinSchedule ordered by schedule_date
	 */
	public List<VratinSchedule> getAllVratinSchedulesByDate() {
		return vratinScheduleRepository.findAllByOrderByScheduleDateAsc();
	}

	/**
	 * Get vrati schedule by sr_no
	 * @param srNo
	 * @return VratinSchedule
	 */
	public VratinSchedule getVratinScheduleBySrNo(Long srNo) {
		return vratinScheduleRepository.findById(srNo).orElse(null);
	}

	/**
	 * Save or update vrati schedule
	 * @param vratinSchedule
	 * @return VratinSchedule
	 */
	public VratinSchedule saveVratinSchedule(VratinSchedule vratinSchedule) {
		return vratinScheduleRepository.save(vratinSchedule);
	}

	/**
	 * Delete vrati schedule by sr_no
	 * @param srNo
	 */
	public void deleteVratinSchedule(Long srNo) {
		vratinScheduleRepository.deleteById(srNo);
	}

	// ========== VratinFamily Methods ==========

	/**
	 * Get all vrati families ordered by name
	 * @return List of VratinFamily ordered by name
	 */
	public List<VratinFamily> getAllVratinFamilies() {
		return vratinFamilyRepository.findAllByOrderByNameAsc();
	}

	/**
	 * Get vrati family by sr_no
	 * @param srNo
	 * @return VratinFamily
	 */
	public VratinFamily getVratinFamilyBySrNo(Long srNo) {
		return vratinFamilyRepository.findById(srNo).orElse(null);
	}

	/**
	 * Save or update vrati family
	 * @param vratinFamily
	 * @return VratinFamily
	 */
	public VratinFamily saveVratinFamily(VratinFamily vratinFamily) {
		return vratinFamilyRepository.save(vratinFamily);
	}

	/**
	 * Delete vrati family by sr_no
	 * @param srNo
	 */
	public void deleteVratinFamily(Long srNo) {
		vratinFamilyRepository.deleteById(srNo);
	}

	// ========== VratinScheduleMapping Methods ==========

	/**
	 * Get all schedule mappings
	 * @return List of all VratinScheduleMapping
	 */
	public List<VratinScheduleMapping> getAllScheduleMappings() {
		return vratinScheduleMappingRepository.findAll();
	}

	/**
	 * Get schedule mappings by schedule sr_no
	 * @param scheduleSrno
	 * @return List of VratinScheduleMapping
	 */
	public List<VratinScheduleMapping> getMappingsBySchedule(Long scheduleSrno) {
		return vratinScheduleMappingRepository.findByScheduleSrno(scheduleSrno);
	}

	/**
	 * Get schedule mappings by host sr_no
	 * @param hostSrno
	 * @return List of VratinScheduleMapping
	 */
	public List<VratinScheduleMapping> getMappingsByHost(Long hostSrno) {
		return vratinScheduleMappingRepository.findByHostSrno(hostSrno);
	}

	/**
	 * Get schedule mappings by family sr_no
	 * @param familySrno
	 * @return List of VratinScheduleMapping
	 */
	public List<VratinScheduleMapping> getMappingsByFamily(Long familySrno) {
		return vratinScheduleMappingRepository.findByFamilySrno(familySrno);
	}

	/**
	 * Save schedule mapping
	 * @param scheduleMapping
	 * @return VratinScheduleMapping
	 */
	public VratinScheduleMapping saveScheduleMapping(VratinScheduleMapping scheduleMapping) {
		return vratinScheduleMappingRepository.save(scheduleMapping);
	}

	/**
	 * Delete schedule mapping by composite key
	 * @param scheduleSrno
	 * @param hostSrno
	 * @param familySrno
	 */
	public void deleteScheduleMapping(Long scheduleSrno, Long hostSrno, Long familySrno) {
		VratinScheduleMappingId id = new VratinScheduleMappingId(scheduleSrno, hostSrno, familySrno);
		vratinScheduleMappingRepository.deleteById(id);
	}

	/**
	 * Get schedule mapping details with host and families grouped by host
	 * @param scheduleSrno
	 * @return List of ScheduleMappingResponseDTO
	 */
	public List<ScheduleMappingResponseDTO> getScheduleMappingDetails(Long scheduleSrno) {
		List<Object[]> results = vratinScheduleMappingRepository.findScheduleMappingDetails(scheduleSrno);
		
		// Use LinkedHashMap to maintain order by host sr_no
		Map<Long, ScheduleMappingResponseDTO> hostMap = new LinkedHashMap<>();
		
		for (Object[] row : results) {
			Long schedSrno = (Long) row[0];
			String hostName = (String) row[1];
			String hostAddress = (String) row[2];
			String hostMob = (String) row[3];
			Long hostSrno = (Long) row[4];
			String familyName = (String) row[5];
			String familyMob = (String) row[6];
			
			// Get or create host entry
			ScheduleMappingResponseDTO hostDTO = hostMap.get(hostSrno);
			if (hostDTO == null) {
				hostDTO = new ScheduleMappingResponseDTO(schedSrno, hostName, hostAddress, hostMob);
				hostMap.put(hostSrno, hostDTO);
			}
			
			// Add family to host
			VratiFamilyDTO familyDTO = new VratiFamilyDTO(familyName, familyMob);
			hostDTO.addVratiFamily(familyDTO);
		}
		
		return new ArrayList<>(hostMap.values());
	}
	
	/**
	 * Get family schedules grouped by family name for a given year
	 * @param year The year to filter schedules
	 * @return Map with family name as key and list of schedule dates as value
	 */
	public Map<String, List<LocalDate>> getFamilySchedulesByYear(int year) {
		List<Object[]> results = vratinFamilyRepository.findFamilySchedulesByYear(year);
		Map<String, List<LocalDate>> familySchedulesMap = new LinkedHashMap<>();
		
		for (Object[] row : results) {
			String familyName = (String) row[0];
			// Handle both java.sql.Date and java.time.LocalDate
			LocalDate scheduleDate = null;
			if (row[1] != null) {
				if (row[1] instanceof java.sql.Date) {
					scheduleDate = ((java.sql.Date) row[1]).toLocalDate();
				} else if (row[1] instanceof LocalDate) {
					scheduleDate = (LocalDate) row[1];
				}
			}
			
			// Get or create list for this family
			List<LocalDate> dates = familySchedulesMap.get(familyName);
			if (dates == null) {
				dates = new ArrayList<>();
				familySchedulesMap.put(familyName, dates);
			}
			
			// Add date to list (only if not null)
			if (scheduleDate != null) {
				dates.add(scheduleDate);
			}
		}
		
		return familySchedulesMap;
	}
	
	/**
	 * Get family schedules grouped by family name for a given year
	 * @param year The year to filter schedules
	 * @return Map with family name as key and list of schedule dates as value
	 */
	public Map<String, List<LocalDate>> getHostFamilySchedulesByYear(int year) {
		List<Object[]> results = vratinFamilyRepository.findHostFamilySchedulesByYear(year);
		Map<String, List<LocalDate>> familySchedulesMap = new LinkedHashMap<>();
		
		for (Object[] row : results) {
			String familyName = (String) row[0];
			// Handle both java.sql.Date and java.time.LocalDate
			LocalDate scheduleDate = null;
			if (row[1] != null) {
				if (row[1] instanceof java.sql.Date) {
					scheduleDate = ((java.sql.Date) row[1]).toLocalDate();
				} else if (row[1] instanceof LocalDate) {
					scheduleDate = (LocalDate) row[1];
				}
			}
			
			// Get or create list for this family
			List<LocalDate> dates = familySchedulesMap.get(familyName);
			if (dates == null) {
				dates = new ArrayList<>();
				familySchedulesMap.put(familyName, dates);
			}
			
			// Add date to list (only if not null)
			if (scheduleDate != null) {
				dates.add(scheduleDate);
			}
		}
		
		return familySchedulesMap;
	}
	
}

