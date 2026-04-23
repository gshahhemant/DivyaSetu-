package com.swadyay.data.ai.sanantonio.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.swadyay.data.ai.sanantonio.services.CsvProcessor;


@RestController
@RequestMapping(value = "/ai")
public class AiDatarController {

	@Autowired
	CsvProcessor csvProcessor;

	@GetMapping(value = "/test")
	public ResponseEntity<String> test() {

		String str = "<html> <body><center><b>SWADHYAY AI s DATA APP  WORKING FINE   </b></center></body></html>";
		return new ResponseEntity<String>(str, HttpStatus.OK);
	}

	@GetMapping(value = "/process/sanantoniadata")
	public ResponseEntity<String> processSanantoniaSata(@RequestParam String fileName) {

		String str = "<html> <body><center><b>SANANTIONIO DATA PROCESSING FINISH </b></center></body></html>";
		
		str=str+":::"+fileName;

		csvProcessor.processCsv(fileName);

		return new ResponseEntity<String>(str, HttpStatus.OK);

	}

}
