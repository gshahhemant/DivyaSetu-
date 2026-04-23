package com.swadhyaydata.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swadhyaydata.app.entity.VratinSchedule;

@Repository
public interface VratinScheduleRepository extends JpaRepository<VratinSchedule, Long> {

	// Find all vrati schedules ordered by sr_no
	List<VratinSchedule> findAllByOrderBySrNoAsc();
	
	// Find all vrati schedules ordered by schedule_date
	List<VratinSchedule> findAllByOrderByScheduleDateAsc();
}
