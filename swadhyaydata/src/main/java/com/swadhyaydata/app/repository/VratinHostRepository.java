package com.swadhyaydata.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swadhyaydata.app.entity.VratinHost;

@Repository
public interface VratinHostRepository extends JpaRepository<VratinHost, Long> {

	// Find all vrati hosts ordered by name
	List<VratinHost> findAllByOrderByNameAsc();
}
