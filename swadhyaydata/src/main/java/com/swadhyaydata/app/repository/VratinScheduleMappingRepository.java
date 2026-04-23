package com.swadhyaydata.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.swadhyaydata.app.entity.VratinScheduleMapping;
import com.swadhyaydata.app.entity.VratinScheduleMappingId;

@Repository
public interface VratinScheduleMappingRepository extends JpaRepository<VratinScheduleMapping, VratinScheduleMappingId> {

	// Find all mappings by schedule sr_no
	@Query("SELECT vsm FROM VratinScheduleMapping vsm WHERE vsm.id.scheduleSrno = :scheduleSrno")
	List<VratinScheduleMapping> findByScheduleSrno(@Param("scheduleSrno") Long scheduleSrno);

	// Find all mappings by host sr_no
	@Query("SELECT vsm FROM VratinScheduleMapping vsm WHERE vsm.id.hostSrno = :hostSrno")
	List<VratinScheduleMapping> findByHostSrno(@Param("hostSrno") Long hostSrno);

	// Find all mappings by family sr_no
	@Query("SELECT vsm FROM VratinScheduleMapping vsm WHERE vsm.id.familySrno = :familySrno")
	List<VratinScheduleMapping> findByFamilySrno(@Param("familySrno") Long familySrno);

	// Get schedule mapping details with host and families
	@Query("SELECT vm.id.scheduleSrno, vh.name, vh.address, vh.mob, vh.srNo, " +
	       "vf.name, vf.mob " +
	       "FROM VratinScheduleMapping vm " +
	       "JOIN VratinHost vh ON vm.id.hostSrno = vh.srNo " +
	       "JOIN VratinFamily vf ON vm.id.familySrno = vf.srNo " +
	       "WHERE vm.id.scheduleSrno = :scheduleSrno " +
	       "ORDER BY vh.srNo")
	List<Object[]> findScheduleMappingDetails(@Param("scheduleSrno") Long scheduleSrno);
}
