package com.swadhyaydata.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.swadhyaydata.app.entity.Users;

@RestController
@RequestMapping(value = "/login")
public class LoginController {

	@Autowired
	LoginService loginService;

	@PostMapping(value = "/validateuser")
	public ResponseEntity<Users> isValidUser(@RequestBody Users user) throws Exception {

		return ResponseEntity.ok().body(loginService.isValidUser(user));

	}

}
