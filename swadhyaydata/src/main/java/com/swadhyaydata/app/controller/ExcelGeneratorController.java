package com.swadhyaydata.app.controller;

import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.swadhyaydata.app.entity.CityZipDTO;
import com.swadhyaydata.app.entity.Contact;
import com.swadhyaydata.app.sanantonio.services.CsvProcessor;

@RestController
@RequestMapping(value = "/api")
public class ExcelGeneratorController {

	@Autowired
	ExcelGeneratorService excelGeneratorService;
	@Autowired
	CsvProcessor csvProcessor;

	@GetMapping(value = "/test")
	public ResponseEntity<String> test() {

		String str = "<html> <body><center><b>SWADHYAY DATA APP  WORKING FINE   </b></center></body></html>";

		csvProcessor.processCsv("src/main/resources/test.csv", "src/main/resources/filtered_output.csv");

		return new ResponseEntity<String>(str, HttpStatus.OK);

	}

	@GetMapping("/zipandcity")
	public ResponseEntity<LinkedList<CityZipDTO>> findAllZipAndCity() {
		return ResponseEntity.ok().body(excelGeneratorService.findAllZipAndCity());
	}

	@GetMapping("/zips")
	public LinkedList<String> getAllZip() {
		return excelGeneratorService.getAllZip();
	}

	@GetMapping("/communities/zip/{zip}")
	public LinkedList<String> getCommunitiesByZip(@PathVariable String zip) {
		return excelGeneratorService.getCommunitiesByZip(zip);
	}
	
	@GetMapping("/communities")
	public LinkedList<String> getCommunities() {
		return excelGeneratorService.getCommunities();
	}

	@GetMapping(value = "/contacts/zip/{zip}/community/{communityName}")
	public ResponseEntity<LinkedList<Contact>> getContactsByCommunityAndZip(@PathVariable String communityName,
			@PathVariable String zip, @RequestParam String commentYear) throws Exception {

		System.out.println(communityName);
		return ResponseEntity.ok()
				.body(excelGeneratorService.getContactsByCommunityAndZip(communityName, zip, commentYear));

	}

	@GetMapping(value = "/contacts/zip/{zip}")
	public ResponseEntity<LinkedList<Contact>> getContactsByZip(@PathVariable String zip,
			@RequestParam String commentYear) throws Exception {

		return ResponseEntity.ok().body(excelGeneratorService.getContactsByZip(zip, commentYear));

	}

	@PostMapping(value = "/updatecontacts")
	public ResponseEntity<LinkedList<Contact>> updateConatacts(@RequestBody List<Contact> contacts,
			@RequestParam String commentYear) throws Exception {

		return ResponseEntity.ok().body(excelGeneratorService.updateContact(contacts, commentYear));

	}

	@PostMapping(value = "/addcontact")
	public ResponseEntity<String> addConatact(@RequestBody Contact contacts) throws Exception {

		return ResponseEntity.ok().body(excelGeneratorService.addContact(contacts));

	}

	@GetMapping(value = "/folder/zip/{zip}/community/{communityName}")
	public ResponseEntity<byte[]> createFolder(@PathVariable String communityName, @PathVariable String zip,
			@RequestParam String commentYear) throws Exception {

		System.out.println(communityName);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + communityName + ".xlsx")
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(excelGeneratorService.createFolderByZip(communityName, zip, commentYear));

	}

	@GetMapping(value = "/folder/zip/{zip}")
	public ResponseEntity<byte[]> createFolderByZipOnly(@PathVariable String zip, @RequestParam String commentYear)
			throws Exception {

		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + zip + ".xlsx")
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(excelGeneratorService.createFolderByZipOnly(zip, commentYear));

	}

	@GetMapping(value = "/sanantonio/folder/zip/{zip}")
	public ResponseEntity<byte[]> createFolderByZipSanantoniaOnly(@PathVariable String zip,
			@RequestParam String commentYear) throws Exception {

		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + zip + ".xlsx")
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(excelGeneratorService.createFolderByZipSanantoniaOnly(zip, commentYear));

	}

	/*
	 * @GetMapping(value = "/updatecontact") public
	 * ResponseEntity<LinkedList<Contact>> updateContact(@RequestBody Contact
	 * contact) throws Exception {
	 * 
	 * System.out.println(communityName); return
	 * ResponseEntity.ok().body(excelGeneratorService.getContactsByCommunityAndZip(
	 * communityName, zip));
	 * 
	 * }
	 */

	/*
	 * @GetMapping(value = "/folder/{communityName}") public ResponseEntity<byte[]>
	 * createFolder(@PathVariable String communityName) throws Exception {
	 * 
	 * System.out.println(communityName); return ResponseEntity.ok()
	 * .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" +
	 * communityName + ".xlsx") .contentType(MediaType.APPLICATION_OCTET_STREAM)
	 * .body(excelGeneratorService.createFolder(communityName));
	 * 
	 * }
	 * 
	 * @GetMapping("/states") public LinkedList<String> getAllStates() { return
	 * excelGeneratorService.getAllStates(); }
	 * 
	 *//**
		 * GET /api/communities/cities/{state} Returns distinct cities for a given
		 * state.
		 */
	/*
	 * @GetMapping("city/{state}") public LinkedList<String>
	 * getCitiesByState(@PathVariable String state) { return
	 * excelGeneratorService.getCitiesByState(state); }
	 * 
	 *//**
		 * GET /api/communities/{state}/{city} Returns communities for a given city and
		 * state.
		 *//*
			 * @GetMapping("communities/{state}/{city}") public LinkedList<String>
			 * getCommunitiesByCityAndState(@PathVariable String city, @PathVariable String
			 * state) { return excelGeneratorService.getCommunitiesByCityAndState(city,
			 * state); }
			 */

}
