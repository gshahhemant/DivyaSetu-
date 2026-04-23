package com.swadhyaydata.app.repository;

import java.util.LinkedList;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.swadhyaydata.app.entity.CityZipDTO;
import com.swadhyaydata.app.entity.Community;

@Repository
public interface CommunityRepository extends JpaRepository<Community, Long> {

	@Query(value = "SELECT distinct state  FROM communities  ORDER BY  state ASC", nativeQuery = true)
	LinkedList<String> findAllStates();
	
	@Query(value = "SELECT distinct zipcode  FROM communities  ORDER BY  zipcode ASC", nativeQuery = true)
	LinkedList<String> findAllZip();
	
	@Query(value = "SELECT distinct cityname,zipcode  FROM communities  ORDER BY  cityname ASC", nativeQuery = true)
	LinkedList<CityZipDTO> findAllZipAndCity();
	
	@Query(value = "SELECT distinct communityname FROM communities WHERE zipcode = :zip  ORDER BY communityname ASC", nativeQuery = true)
	LinkedList<String> findCommunitiesByZip(@Param("zip") String zip);

	@Query(value = "SELECT distinct cityname FROM communities WHERE state = :state ORDER BY cityname ASC", nativeQuery = true)
	LinkedList<String> findDistinctCitiesByState(@Param("state") String state);

	@Query(value = "SELECT distinct communityname FROM communities WHERE cityname = :city AND state = :state ORDER BY communityname ASC", nativeQuery = true)
	LinkedList<String> findCommunitiesByCityAndState(@Param("city") String city, @Param("state") String state);
	
	@Query(value = "SELECT distinct communityname FROM communities  ORDER BY communityname ASC", nativeQuery = true)
	LinkedList<String> findCommunities();
	

}
