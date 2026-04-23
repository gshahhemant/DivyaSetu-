package com.swadhyaydata.app.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.swadhyaydata.app.entity.ScheduleMappingResponseDTO;
import com.swadhyaydata.app.entity.VratinFamily;
import com.swadhyaydata.app.entity.VratinHost;
import com.swadhyaydata.app.entity.VratinSchedule;
import com.swadhyaydata.app.entity.VratinScheduleMapping;


@RestController
@RequestMapping(value = "/api/vratin")
public class VratinSchedulerController {

	@Autowired
	private VratinSchedulerService vratinSchedulerService;

	/**
	 * Get all vrati hosts ordered by name
	 * @return List of all VratinHost
	 */
	@GetMapping(value = "/hosts")
	public ResponseEntity<List<VratinHost>> getAllVratinHosts() {
		try {
			List<VratinHost> hosts = vratinSchedulerService.getAllVratinHosts();
			return ResponseEntity.ok().body(hosts);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	/**
	 * Get vrati host by sr_no
	 * @param srNo
	 * @return VratinHost
	 */
	@GetMapping(value = "/hosts/{srNo}")
	public ResponseEntity<VratinHost> getVratinHostBySrNo(@PathVariable Long srNo) {
		try {
			VratinHost host = vratinSchedulerService.getVratinHostBySrNo(srNo);
			if (host != null) {
				return ResponseEntity.ok().body(host);
			} else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	/**
	 * Create a new vrati host
	 * @param vratinHost
	 * @return Created VratinHost
	 */
	@PostMapping(value = "/hosts")
	public ResponseEntity<VratinHost> createVratinHost(@RequestBody VratinHost vratinHost) {
		try {
			VratinHost savedHost = vratinSchedulerService.saveVratinHost(vratinHost);
			return ResponseEntity.status(HttpStatus.CREATED).body(savedHost);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	/**
	 * Update an existing vrati host
	 * @param srNo
	 * @param vratinHost
	 * @return Updated VratinHost
	 */
	@PutMapping(value = "/hosts/{srNo}")
	public ResponseEntity<VratinHost> updateVratinHost(@PathVariable Long srNo, @RequestBody VratinHost vratinHost) {
		try {
			VratinHost existingHost = vratinSchedulerService.getVratinHostBySrNo(srNo);
			if (existingHost != null) {
				vratinHost.setSrNo(srNo);
				VratinHost updatedHost = vratinSchedulerService.saveVratinHost(vratinHost);
				return ResponseEntity.ok().body(updatedHost);
			} else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	/**
	 * Delete vrati host by sr_no
	 * @param srNo
	 * @return Success message
	 */
	@PostMapping(value = "/hosts/{srNo}")
	public ResponseEntity<String> deleteVratinHost(@PathVariable Long srNo) {
		try {
			VratinHost existingHost = vratinSchedulerService.getVratinHostBySrNo(srNo);
			if (existingHost != null) {
				vratinSchedulerService.deleteVratinHost(srNo);
				return ResponseEntity.ok().body("Vrati host deleted successfully");
			} else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Vrati host not found");
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting vrati host");
		}
	}

	// ========== VratinSchedule REST APIs ==========

	/**
	 * Get all vrati schedules ordered by sr_no
	 * @return List of all VratinSchedule
	 */
	@GetMapping(value = "/schedules")
	public ResponseEntity<List<VratinSchedule>> getAllVratinSchedules() {
		try {
			List<VratinSchedule> schedules = vratinSchedulerService.getAllVratinSchedules();
			return ResponseEntity.ok().body(schedules);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	/**
	 * Get all vrati schedules ordered by schedule_date
	 * @return List of all VratinSchedule ordered by date
	 */
	@GetMapping(value = "/schedules/bydate")
	public ResponseEntity<List<VratinSchedule>> getAllVratinSchedulesByDate() {
		try {
			List<VratinSchedule> schedules = vratinSchedulerService.getAllVratinSchedulesByDate();
			return ResponseEntity.ok().body(schedules);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	/**
	 * Get vrati schedule by sr_no
	 * @param srNo
	 * @return VratinSchedule
	 */
	@GetMapping(value = "/schedules/{srNo}")
	public ResponseEntity<VratinSchedule> getVratinScheduleBySrNo(@PathVariable Long srNo) {
		try {
			VratinSchedule schedule = vratinSchedulerService.getVratinScheduleBySrNo(srNo);
			if (schedule != null) {
				return ResponseEntity.ok().body(schedule);
			} else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	/**
	 * Create a new vrati schedule
	 * @param vratinSchedule
	 * @return Created VratinSchedule
	 */
	@PostMapping(value = "/schedules")
	public ResponseEntity<VratinSchedule> createVratinSchedule(@RequestBody VratinSchedule vratinSchedule) {
		try {
			VratinSchedule savedSchedule = vratinSchedulerService.saveVratinSchedule(vratinSchedule);
			return ResponseEntity.status(HttpStatus.CREATED).body(savedSchedule);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	/**
	 * Update an existing vrati schedule
	 * @param srNo
	 * @param vratinSchedule
	 * @return Updated VratinSchedule
	 */
	@PutMapping(value = "/schedules/{srNo}")
	public ResponseEntity<VratinSchedule> updateVratinSchedule(@PathVariable Long srNo, @RequestBody VratinSchedule vratinSchedule) {
		try {
			VratinSchedule existingSchedule = vratinSchedulerService.getVratinScheduleBySrNo(srNo);
			if (existingSchedule != null) {
				vratinSchedule.setSrNo(srNo);
				VratinSchedule updatedSchedule = vratinSchedulerService.saveVratinSchedule(vratinSchedule);
				return ResponseEntity.ok().body(updatedSchedule);
			} else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	/**
	 * Delete vrati schedule by sr_no
	 * @param srNo
	 * @return Success message
	 */
	@PostMapping(value = "/schedules/{srNo}")
	public ResponseEntity<String> deleteVratinSchedule(@PathVariable Long srNo) {
		try {
			VratinSchedule existingSchedule = vratinSchedulerService.getVratinScheduleBySrNo(srNo);
			if (existingSchedule != null) {
				vratinSchedulerService.deleteVratinSchedule(srNo);
				return ResponseEntity.ok().body("Vrati schedule deleted successfully");
			} else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Vrati schedule not found");
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting vrati schedule");
		}
	}

	// ========== VratinFamily REST APIs ==========

	/**
	 * Get all vrati families ordered by name
	 * @return List of all VratinFamily
	 */
	@GetMapping(value = "/families")
	public ResponseEntity<List<VratinFamily>> getAllVratinFamilies() {
		try {
			List<VratinFamily> families = vratinSchedulerService.getAllVratinFamilies();
			return ResponseEntity.ok().body(families);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	/**
	 * Get vrati family by sr_no
	 * @param srNo
	 * @return VratinFamily
	 */
	@GetMapping(value = "/families/{srNo}")
	public ResponseEntity<VratinFamily> getVratinFamilyBySrNo(@PathVariable Long srNo) {
		try {
			VratinFamily family = vratinSchedulerService.getVratinFamilyBySrNo(srNo);
			if (family != null) {
				return ResponseEntity.ok().body(family);
			} else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	/**
	 * Create a new vrati family
	 * @param vratinFamily
	 * @return Created VratinFamily
	 */
	@PostMapping(value = "/families")
	public ResponseEntity<VratinFamily> createVratinFamily(@RequestBody VratinFamily vratinFamily) {
		try {
			VratinFamily savedFamily = vratinSchedulerService.saveVratinFamily(vratinFamily);
			return ResponseEntity.status(HttpStatus.CREATED).body(savedFamily);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	/**
	 * Update an existing vrati family
	 * @param srNo
	 * @param vratinFamily
	 * @return Updated VratinFamily
	 */
	@PutMapping(value = "/families/{srNo}")
	public ResponseEntity<VratinFamily> updateVratinFamily(@PathVariable Long srNo, @RequestBody VratinFamily vratinFamily) {
		try {
			VratinFamily existingFamily = vratinSchedulerService.getVratinFamilyBySrNo(srNo);
			if (existingFamily != null) {
				vratinFamily.setSrNo(srNo);
				VratinFamily updatedFamily = vratinSchedulerService.saveVratinFamily(vratinFamily);
				return ResponseEntity.ok().body(updatedFamily);
			} else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	/**
	 * Delete vrati family by sr_no
	 * @param srNo
	 * @return Success message
	 */
	@PostMapping(value = "/families/{srNo}")
	public ResponseEntity<String> deleteVratinFamily(@PathVariable Long srNo) {
		try {
			VratinFamily existingFamily = vratinSchedulerService.getVratinFamilyBySrNo(srNo);
			if (existingFamily != null) {
				vratinSchedulerService.deleteVratinFamily(srNo);
				return ResponseEntity.ok().body("Vrati family deleted successfully");
			} else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Vrati family not found");
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting vrati family");
		}
	}

	// ========== VratinScheduleMapping REST APIs ==========

	/**
	 * Get all schedule mappings
	 * @return List of all VratinScheduleMapping
	 */
	@GetMapping(value = "/mappings")
	public ResponseEntity<List<VratinScheduleMapping>> getAllScheduleMappings() {
		try {
			List<VratinScheduleMapping> mappings = vratinSchedulerService.getAllScheduleMappings();
			return ResponseEntity.ok().body(mappings);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	/**
	 * Get schedule mappings by schedule sr_no
	 * @param scheduleSrno
	 * @return List of VratinScheduleMapping
	 */
	@GetMapping(value = "/mappings/schedule/{scheduleSrno}")
	public ResponseEntity<List<VratinScheduleMapping>> getMappingsBySchedule(@PathVariable Long scheduleSrno) {
		try {
			List<VratinScheduleMapping> mappings = vratinSchedulerService.getMappingsBySchedule(scheduleSrno);
			return ResponseEntity.ok().body(mappings);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	/**
	 * Get schedule mappings by host sr_no
	 * @param hostSrno
	 * @return List of VratinScheduleMapping
	 */
	@GetMapping(value = "/mappings/host/{hostSrno}")
	public ResponseEntity<List<VratinScheduleMapping>> getMappingsByHost(@PathVariable Long hostSrno) {
		try {
			List<VratinScheduleMapping> mappings = vratinSchedulerService.getMappingsByHost(hostSrno);
			return ResponseEntity.ok().body(mappings);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	/**
	 * Get schedule mappings by family sr_no
	 * @param familySrno
	 * @return List of VratinScheduleMapping
	 */
	@GetMapping(value = "/mappings/family/{familySrno}")
	public ResponseEntity<List<VratinScheduleMapping>> getMappingsByFamily(@PathVariable Long familySrno) {
		try {
			List<VratinScheduleMapping> mappings = vratinSchedulerService.getMappingsByFamily(familySrno);
			return ResponseEntity.ok().body(mappings);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	/**
	 * Create a new schedule mapping
	 * @param scheduleMapping
	 * @return Created VratinScheduleMapping
	 */
	@PostMapping(value = "/mappings")
	public ResponseEntity<VratinScheduleMapping> createScheduleMapping(@RequestBody VratinScheduleMapping scheduleMapping) {
		try {
			VratinScheduleMapping savedMapping = vratinSchedulerService.saveScheduleMapping(scheduleMapping);
			return ResponseEntity.status(HttpStatus.CREATED).body(savedMapping);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	/**
	 * Delete schedule mapping by composite key
	 * @param scheduleSrno
	 * @param hostSrno
	 * @param familySrno
	 * @return Success message
	 */
	@PutMapping(value = "/mappings/{scheduleSrno}/{hostSrno}/{familySrno}")
	public ResponseEntity<String> deleteScheduleMapping(
			@PathVariable Long scheduleSrno,
			@PathVariable Long hostSrno,
			@PathVariable Long familySrno) {
		try {
			vratinSchedulerService.deleteScheduleMapping(scheduleSrno, hostSrno, familySrno);
			return ResponseEntity.ok().body("Schedule mapping deleted successfully");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting schedule mapping");
		}
	}

	/**
	 * Get schedule mapping details with hosts and families grouped by host
	 * @param scheduleSrno - Schedule sr_no from vratin_schedule_mapping
	 * @return List of hosts with their families for the given schedule
	 */
	@GetMapping(value = "/mappings/details/{scheduleSrno}")
	public ResponseEntity<List<ScheduleMappingResponseDTO>> getScheduleMappingDetails(@PathVariable Long scheduleSrno) {
		try {
			List<ScheduleMappingResponseDTO> mappingDetails = vratinSchedulerService.getScheduleMappingDetails(scheduleSrno);
			return ResponseEntity.ok().body(mappingDetails);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}
	
	/**
	 * Get all family names with their schedule dates for a given year
	 * Returns a map with family name as key and list of scheduled dates as value
	 * Families with no schedules will have an empty list
	 * @param year - The year to filter schedules (e.g., 2026)
	 * @return Map with family name as key and list of LocalDate as value
	 */
	@GetMapping(value = "/families/schedules/{year}")
	public ResponseEntity<Map<String, List<LocalDate>>> getFamilySchedulesByYear(@PathVariable int year) {
		try {
			Map<String, List<LocalDate>> familySchedules = vratinSchedulerService.getFamilySchedulesByYear(year);
			return ResponseEntity.ok().body(familySchedules);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}
	
	
	/**
	 * Get all host family names with their schedule dates for a given year
	 * Returns a map with family name as key and list of scheduled dates as value
	 * Families with no schedules will have an empty list
	 * @param year - The year to filter schedules (e.g., 2026)
	 * @return Map with family name as key and list of LocalDate as value
	 */
	@GetMapping(value = "/hostfamilies/schedules/{year}")
	public ResponseEntity<Map<String, List<LocalDate>>> getHostFamilySchedulesByYear(@PathVariable int year) {
		try {
			Map<String, List<LocalDate>> familySchedules = vratinSchedulerService.getHostFamilySchedulesByYear(year);
			return ResponseEntity.ok().body(familySchedules);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}
}
