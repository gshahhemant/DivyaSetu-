package com.swadyay.data.ai.sanantonio.services;



import java.util.LinkedList;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactRepository extends JpaRepository<Contact, String> {
   
	 @Query(
		        value = "SELECT * FROM contacts where community_name ilike CONCAT('%', :communityName, '%') ORDER BY  substring(property_address from '^\\d+\\s+(.*?)') ASC, CAST(substring(property_address FROM '^(\\d+)') AS INTEGER) ASC",
		        nativeQuery = true
		    )
	 LinkedList<Contact> findContactByCommunityName(@Param("communityName") String communityName);
	 
	 
	 @Query(
		        value = "SELECT * FROM contacts where community_name ilike CONCAT('%', :communityName, '%') and zip= :zip ORDER BY  substring(property_address from '^\\d+\\s+(.*?)') ASC, CAST(substring(property_address FROM '^(\\d+)') AS INTEGER) ASC",
		        nativeQuery = true
		    )
	 LinkedList<Contact> findContactByCommunityNameAndZip(@Param("communityName") String communityName,@Param("zip") String zip);
	 
	 
	 @Query(
		        value = "SELECT * FROM contacts where zip= :zip ORDER BY  substring(property_address from '^\\d+\\s+(.*?)') ASC, CAST(substring(property_address FROM '^(\\d+)') AS INTEGER) ASC",
		        nativeQuery = true
		    )
	 LinkedList<Contact> findContactByZip(@Param("zip") String zip);
	
}

