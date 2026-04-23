package com.swadhyaydata.app.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swadhyaydata.app.entity.Users;

@Repository
public interface UsersRepository extends JpaRepository<Users, String> {
   
	
	
}

