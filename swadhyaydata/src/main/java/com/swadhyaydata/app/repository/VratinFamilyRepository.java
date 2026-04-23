package com.swadhyaydata.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.swadhyaydata.app.entity.VratinFamily;

@Repository
public interface VratinFamilyRepository extends JpaRepository<VratinFamily, Long> {

	// Find all vrati families ordered by name
	List<VratinFamily> findAllByOrderByNameAsc();
	
	/**
	 * Get all family names with their scheduled dates for a given year
	 * Returns results as Object[] with [name, scheduleDate]
	 * @param year The year to filter schedules
	 * @return List of Object arrays containing [String name, LocalDate scheduleDate]
	 */
	@Query(value = "SELECT vf.name, vs.schedule_date " +
			"FROM vratin_family vf " +
			"LEFT JOIN vratin_schedule_mapping vm ON vm.family_srno = vf.sr_no " +
			"LEFT JOIN vratin_schedule vs ON vm.schedule_srno = vs.sr_no " +
			"    AND EXTRACT(YEAR FROM vs.schedule_date) = :year " +
			"ORDER BY vf.name", nativeQuery = true)
	List<Object[]> findFamilySchedulesByYear(@Param("year") int year);
	
	
	
	/**
	 * Get all host names with their scheduled vrati hosting  for a given year
	 * Returns results as Object[] with [name, scheduleDate]
	 * @param year The year to filter schedules
	 * @return List of Object arrays containing [String name, LocalDate scheduleDate]
	 */
	@Query(value = "SELECT distinct vh.name, vs.schedule_date " +
			"FROM vratin_host vh " +
			"LEFT JOIN vratin_schedule_mapping vm ON vm.host_srno = vh.sr_no " +
			"LEFT JOIN vratin_schedule vs ON vm.schedule_srno = vs.sr_no " +
			"    AND EXTRACT(YEAR FROM vs.schedule_date) = :year " +
			"ORDER BY vh.name", nativeQuery = true)
	List<Object[]> findHostFamilySchedulesByYear(@Param("year") int year);
}
