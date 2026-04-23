package com.swadhyaydata.app.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.swadhyaydata.app.entity.Users;
import com.swadhyaydata.app.repository.UsersRepository;

@Service
public class LoginService {

	@Autowired
	UsersRepository usersRepository;

	public Users isValidUser(Users user) {

		Optional<Users> userEntityOptionl = usersRepository.findById(user.getUsername());

		if (userEntityOptionl.isPresent()) {
			Users userEntity = userEntityOptionl.get();

			if (userEntity != null && user.getPassword() != null && userEntity.getPassword() != null
					&& userEntity.getPassword().equals(user.getPassword()))

				return userEntity;
			else
				return null;

		}
		return null;
	}
}
