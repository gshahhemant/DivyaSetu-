package com.swadhyaydata.app.repository;


import com.swadhyaydata.app.entity.Contact;
import com.swadhyaydata.app.entity.Contactsanantonio;

import java.util.LinkedList;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactSantonioRepository extends JpaRepository<Contactsanantonio, String> {
   
	
	 @Query(
		        value = "SELECT * FROM contacts_sanantonio where zip= :zip ORDER BY  details_url, substring(property_address from '^\\d+\\s+(.*?)') ASC, CAST(substring(property_address FROM '^(\\d+)') AS INTEGER) ASC",
		        nativeQuery = true
		    )
	 LinkedList<Contact> findContactByZipSanantonia(@Param("zip") String zip);
	
	
}

