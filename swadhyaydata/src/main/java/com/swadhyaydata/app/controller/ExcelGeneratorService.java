package com.swadhyaydata.app.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Footer;
import org.apache.poi.ss.usermodel.Header;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.swadhyaydata.app.entity.CityZipDTO;
import com.swadhyaydata.app.entity.Comments;
import com.swadhyaydata.app.entity.Contact;
import com.swadhyaydata.app.entity.ContactId;
import com.swadhyaydata.app.repository.CommunityRepository;
import com.swadhyaydata.app.repository.ContactRepository;
import com.swadhyaydata.app.repository.ContactSantonioRepository;
import com.swadhyaydata.app.utill.Constant;

@Service
public class ExcelGeneratorService {

	@Autowired
	ContactRepository contactRepository;

	@Autowired
	CommunityRepository communityRepository;
	
	@Autowired
	ContactSantonioRepository  contactSantonioRepository;
	
	public byte[] createFolderByZip(String communityName, String zip, String year) throws IOException {

		LinkedList<Contact> contacts = null;
				//contactRepository.findContactByCommunityNameAndZip(communityName, zip);
		

		if (communityName.equals(Constant.ALL_COMUNITIES)) {
			
			communityName=zip;

			contacts = contactRepository.findContactByZipOrderByGeoID(zip);

		} else {

			contacts = contactRepository.findContactByCommunityNameAndZip(communityName, zip);
		}
		
		Workbook workbook = new XSSFWorkbook();

		try {
			
	

			// Set Bold Font
			Font headerFont = workbook.createFont();
			headerFont.setBold(true);
			
			
			int pastYear = Integer.parseInt(year)-1;
			String pastYearStr = String.valueOf(pastYear);
			
			System.out.println(pastYearStr);
			Map<Integer, String> pastYearMap = new TreeMap<>(); 

			CellStyle headerStyle = workbook.createCellStyle();
			headerStyle.setFont(headerFont);

			Sheet sheet = workbook.createSheet(communityName);

			PrintSetup printSetup = sheet.getPrintSetup();
			printSetup.setLandscape(true);
			printSetup.setFitWidth((short) 1); // One-page width
			printSetup.setFitHeight((short) 0); // Auto height
			printSetup.setScale((short) 100);
			// Set First Row Freeze
			sheet.createFreezePane(0, 1);

			// Set Column Width
			sheet.setColumnWidth(0, 256 * Constant.COLUMN_WIDTH_NO);
			sheet.setColumnWidth(1, 256 * Constant.COLUMN_WIDTH_OWNER_NAME);
			sheet.setColumnWidth(2, 256 * Constant.COLUMN_WIDTH_ADDRESS);

			for (int i = 3; i <= 14; i++) {
				sheet.setColumnWidth(i, 256 * Constant.COLUMN_WIDTH_JAN_TO_DEC);
			}

			// Header Labeling
			Row header = sheet.createRow(0);
			for (int i = 0; i < Constant.headers.length; i++) {
				Cell cell = header.createCell(i);
				cell.setCellValue(Constant.headers[i]);
				cell.setCellStyle(headerStyle);
			}

			// Set Footer Note
			Footer footer = sheet.getFooter();
			footer.setCenter(Constant.FOOTER);
			footer.setRight("Page &P of &N");
			// Set Header
			Header pageHeader = sheet.getHeader();
			pageHeader.setRight(communityName+"    "+year);

			sheet.setRepeatingRows(CellRangeAddress.valueOf("1:1"));

			// Populate data from database

			int rowIdx = 1;
			for (Contact contact : contacts) {

				Row row = sheet.createRow(rowIdx++);
				row.createCell(0).setCellValue(rowIdx - 1);
				
				row.createCell(2).setCellValue(contact.getProperty_address());

				// Step 1: Initialize cells for all 12 months with blank values
				for (int i = 0; i < 12; i++) {
					row.createCell(3 + i).setCellValue(" ");
				}
				List<Comments> commentLst = contact.getComments();
				if (commentLst != null) {

					for (Comments comment : commentLst) {

						if (comment.getYear() != null && comment.getYear().equals(year)) {

							String month = comment.getMonth();
							String note = comment.getNote();

							int cellIndex = switch (month) {
							case "01", "1" -> 3;
							case "02", "2" -> 4;
							case "03", "3" -> 5;
							case "04", "4" -> 6;
							case "05", "5" -> 7;
							case "06", "6" -> 8;
							case "07", "7" -> 9;
							case "08", "8" -> 10;
							case "09", "9" -> 11;
							case "10" -> 12;
							case "11" -> 13;
							case "12" -> 14;
							default -> -1;
							};

							if (cellIndex != -1) {
								row.getCell(cellIndex).setCellValue(note);
							}

						}
						
						if (comment.getYear() != null && comment.getYear().equals(pastYearStr)) {
							
							pastYearMap.put(Integer.parseInt(comment.getMonth()), comment.getNote());
							
							
						}

					}
					

				}
				
				System.out.println(pastYearMap);
				if(pastYearMap==null || pastYearMap.isEmpty())
					row.createCell(1).setCellValue(contact.getName());
				else {
									
					Cell cell = row.createCell(1);
					CellStyle wrap = workbook.createCellStyle();
					wrap.setWrapText(true);
					wrap.setVerticalAlignment(VerticalAlignment.TOP);
					
					cell.setCellValue(contact.getName() + "\n" + toFinalString(pastYearMap));
					cell.setCellStyle(wrap);
					

				}
				pastYearMap = new TreeMap<>(); 
				
			}

			// Write to byte array
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			workbook.write(out);
			return out.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			workbook.close();
		}

	}
	
	public  String toFinalString(Map<Integer, String> map) {
	    if (map == null || map.isEmpty()) return "";
	    return map.entrySet().stream()
	        .sorted(Map.Entry.comparingByKey()) // ascending by Integer key
	        .map(e -> "(" + e.getKey() + ": " + String.valueOf(e.getValue()) + ")")
	        .collect(Collectors.joining(", "));
	}
	
	public byte[] createFolderByZipBackUp(String communityName, String zip, String year) throws IOException {

		LinkedList<Contact> contacts = null;
				//contactRepository.findContactByCommunityNameAndZip(communityName, zip);
		

		if (communityName.equals(Constant.ALL_COMUNITIES)) {
			
			communityName=zip;

			contacts = contactRepository.findContactByZipOrderByGeoID(zip);

		} else {

			contacts = contactRepository.findContactByCommunityNameAndZip(communityName, zip);
		}
		
		Workbook workbook = new XSSFWorkbook();

		try {

			// Set Bold Font
			Font headerFont = workbook.createFont();
			headerFont.setBold(true);

			CellStyle headerStyle = workbook.createCellStyle();
			headerStyle.setFont(headerFont);

			Sheet sheet = workbook.createSheet(communityName);

			PrintSetup printSetup = sheet.getPrintSetup();
			printSetup.setLandscape(true);
			printSetup.setFitWidth((short) 1); // One-page width
			printSetup.setFitHeight((short) 0); // Auto height
			printSetup.setScale((short) 100);
			// Set First Row Freeze
			sheet.createFreezePane(0, 1);

			// Set Column Width
			sheet.setColumnWidth(0, 256 * Constant.COLUMN_WIDTH_NO);
			sheet.setColumnWidth(1, 256 * Constant.COLUMN_WIDTH_OWNER_NAME);
			sheet.setColumnWidth(2, 256 * Constant.COLUMN_WIDTH_ADDRESS);

			for (int i = 3; i <= 14; i++) {
				sheet.setColumnWidth(i, 256 * Constant.COLUMN_WIDTH_JAN_TO_DEC);
			}

			// Header Labeling
			Row header = sheet.createRow(0);
			for (int i = 0; i < Constant.headers.length; i++) {
				Cell cell = header.createCell(i);
				cell.setCellValue(Constant.headers[i]);
				cell.setCellStyle(headerStyle);
			}

			// Set Footer Note
			Footer footer = sheet.getFooter();
			footer.setCenter(Constant.FOOTER);
			footer.setRight("Page &P of &N");
			// Set Header
			Header pageHeader = sheet.getHeader();
			pageHeader.setRight(communityName+"    "+year);

			sheet.setRepeatingRows(CellRangeAddress.valueOf("1:1"));

			// Populate data from database

			int rowIdx = 1;
			for (Contact contact : contacts) {

				Row row = sheet.createRow(rowIdx++);
				row.createCell(0).setCellValue(rowIdx - 1);
				row.createCell(1).setCellValue(contact.getName());
				row.createCell(2).setCellValue(contact.getProperty_address());

				// Step 1: Initialize cells for all 12 months with blank values
				for (int i = 0; i < 12; i++) {
					row.createCell(3 + i).setCellValue(" ");
				}
				List<Comments> commentLst = contact.getComments();
				if (commentLst != null) {

					for (Comments comment : commentLst) {

						if (comment.getYear() != null && comment.getYear().equals(year)) {

							String month = comment.getMonth();
							String note = comment.getNote();

							int cellIndex = switch (month) {
							case "01", "1" -> 3;
							case "02", "2" -> 4;
							case "03", "3" -> 5;
							case "04", "4" -> 6;
							case "05", "5" -> 7;
							case "06", "6" -> 8;
							case "07", "7" -> 9;
							case "08", "8" -> 10;
							case "09", "9" -> 11;
							case "10" -> 12;
							case "11" -> 13;
							case "12" -> 14;
							default -> -1;
							};

							if (cellIndex != -1) {
								row.getCell(cellIndex).setCellValue(note);
							}

						}

					}

				}

			}

			// Write to byte array
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			workbook.write(out);
			return out.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			workbook.close();
		}

	}

	public byte[] createFolderByZipSanantoniaOnly(String zip, String year) throws IOException {

		LinkedList<Contact> contacts = contactSantonioRepository.findContactByZipSanantonia(zip);
		Workbook workbook = new XSSFWorkbook();

		try {

			// Set Bold Font
			Font headerFont = workbook.createFont();
			headerFont.setBold(true);

			CellStyle headerStyle = workbook.createCellStyle();
			headerStyle.setFont(headerFont);

			Sheet sheet = workbook.createSheet(zip);

			PrintSetup printSetup = sheet.getPrintSetup();
			printSetup.setLandscape(true);
			printSetup.setFitWidth((short) 1); // One-page width
			printSetup.setFitHeight((short) 0); // Auto height
			printSetup.setScale((short) 100);
			// Set First Row Freeze
			sheet.createFreezePane(0, 1);

			// Set Column Width
			sheet.setColumnWidth(0, 256 * Constant.COLUMN_WIDTH_NO);
			sheet.setColumnWidth(1, 256 * Constant.COLUMN_WIDTH_OWNER_NAME);
			sheet.setColumnWidth(2, 256 * Constant.COLUMN_WIDTH_ADDRESS);

			for (int i = 3; i <= 14; i++) {
				sheet.setColumnWidth(i, 256 * Constant.COLUMN_WIDTH_JAN_TO_DEC);
			}

			// Header Labeling
			Row header = sheet.createRow(0);
			for (int i = 0; i < Constant.headers.length; i++) {
				Cell cell = header.createCell(i);
				cell.setCellValue(Constant.headers[i]);
				cell.setCellStyle(headerStyle);
			}

			// Set Footer Note
			Footer footer = sheet.getFooter();
			footer.setCenter(Constant.FOOTER);
			footer.setRight("Page &P of &N");
			// Set Header
			Header pageHeader = sheet.getHeader();
			pageHeader.setRight(zip+"    "+year);

			sheet.setRepeatingRows(CellRangeAddress.valueOf("1:1"));

			// Populate data from database

			int rowIdx = 1;
			for (Contact contact : contacts) {

				Row row = sheet.createRow(rowIdx++);
				row.createCell(0).setCellValue(rowIdx - 1);
				row.createCell(1).setCellValue(contact.getName());
				row.createCell(2).setCellValue(contact.getProperty_address());

				// Step 1: Initialize cells for all 12 months with blank values
				for (int i = 0; i < 12; i++) {
					row.createCell(3 + i).setCellValue(" ");
				}
				List<Comments> commentLst = contact.getComments();
				if (commentLst != null) {

					for (Comments comment : commentLst) {

						if (comment.getYear() != null && comment.getYear().equals(year)) {

							String month = comment.getMonth();
							String note = comment.getNote();

							int cellIndex = switch (month) {
							case "01", "1" -> 3;
							case "02", "2" -> 4;
							case "03", "3" -> 5;
							case "04", "4" -> 6;
							case "05", "5" -> 7;
							case "06", "6" -> 8;
							case "07", "7" -> 9;
							case "08", "8" -> 10;
							case "09", "9" -> 11;
							case "10" -> 12;
							case "11" -> 13;
							case "12" -> 14;
							default -> -1;
							};

							if (cellIndex != -1) {
								row.getCell(cellIndex).setCellValue(note);
							}

						}

					}

				}

			}

			// Write to byte array
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			workbook.write(out);
			return out.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			workbook.close();
		}

	}
	public byte[] createFolderByZipOnly(String zip, String year) throws IOException {

		LinkedList<Contact> contacts = contactRepository.findContactByZip(zip);
		Workbook workbook = new XSSFWorkbook();

		try {

			// Set Bold Font
			Font headerFont = workbook.createFont();
			headerFont.setBold(true);

			CellStyle headerStyle = workbook.createCellStyle();
			headerStyle.setFont(headerFont);

			Sheet sheet = workbook.createSheet(zip);

			PrintSetup printSetup = sheet.getPrintSetup();
			printSetup.setLandscape(true);
			printSetup.setFitWidth((short) 1); // One-page width
			printSetup.setFitHeight((short) 0); // Auto height
			printSetup.setScale((short) 100);
			// Set First Row Freeze
			sheet.createFreezePane(0, 1);

			// Set Column Width
			sheet.setColumnWidth(0, 256 * Constant.COLUMN_WIDTH_NO);
			sheet.setColumnWidth(1, 256 * Constant.COLUMN_WIDTH_OWNER_NAME);
			sheet.setColumnWidth(2, 256 * Constant.COLUMN_WIDTH_ADDRESS);

			for (int i = 3; i <= 14; i++) {
				sheet.setColumnWidth(i, 256 * Constant.COLUMN_WIDTH_JAN_TO_DEC);
			}

			// Header Labeling
			Row header = sheet.createRow(0);
			for (int i = 0; i < Constant.headers.length; i++) {
				Cell cell = header.createCell(i);
				cell.setCellValue(Constant.headers[i]);
				cell.setCellStyle(headerStyle);
			}

			// Set Footer Note
			Footer footer = sheet.getFooter();
			footer.setCenter(Constant.FOOTER);
			footer.setRight("Page &P of &N");
			// Set Header
			Header pageHeader = sheet.getHeader();
			pageHeader.setRight(zip+"    "+year);

			sheet.setRepeatingRows(CellRangeAddress.valueOf("1:1"));

			// Populate data from database

			int rowIdx = 1;
			for (Contact contact : contacts) {

				Row row = sheet.createRow(rowIdx++);
				row.createCell(0).setCellValue(rowIdx - 1);
				row.createCell(1).setCellValue(contact.getName());
				row.createCell(2).setCellValue(contact.getProperty_address());

				// Step 1: Initialize cells for all 12 months with blank values
				for (int i = 0; i < 12; i++) {
					row.createCell(3 + i).setCellValue(" ");
				}
				List<Comments> commentLst = contact.getComments();
				if (commentLst != null) {

					for (Comments comment : commentLst) {

						if (comment.getYear() != null && comment.getYear().equals(year)) {

							String month = comment.getMonth();
							String note = comment.getNote();

							int cellIndex = switch (month) {
							case "01", "1" -> 3;
							case "02", "2" -> 4;
							case "03", "3" -> 5;
							case "04", "4" -> 6;
							case "05", "5" -> 7;
							case "06", "6" -> 8;
							case "07", "7" -> 9;
							case "08", "8" -> 10;
							case "09", "9" -> 11;
							case "10" -> 12;
							case "11" -> 13;
							case "12" -> 14;
							default -> -1;
							};

							if (cellIndex != -1) {
								row.getCell(cellIndex).setCellValue(note);
							}

						}

					}

				}

			}

			// Write to byte array
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			workbook.write(out);
			return out.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			workbook.close();
		}

	}
	
	public LinkedList<Contact> getContactsByCommunityAndZip(String cummunityName, String zip, String year) {
		LinkedList<Contact> contacts = null;
		
		if (cummunityName.equals(Constant.ALL_COMUNITIES)) {

			contacts = contactRepository.findContactByZipOrderByGeoID(zip);

		} else {

			contacts = contactRepository.findContactByCommunityNameAndZip(cummunityName, zip);
		}
		for (Contact contact : contacts) {

			List<Comments> commentLst = contact.getComments();
			if (commentLst != null) {
				commentLst.removeIf(comment -> !year.equals(comment.getYear()));
			}

		}

		return contacts;

	}


	public LinkedList<Contact> getContactsByZip(String zip, String year) {

		LinkedList<Contact> contacts = contactRepository.findContactByZip(zip);

		for (Contact contact : contacts) {

			List<Comments> commentLst = contact.getComments();
			if (commentLst != null) {
				commentLst.removeIf(comment -> !year.equals(comment.getYear()));
			}

		}

		return contacts;

	}

	public LinkedList<Contact> updateContact(List<Contact> contacts, String year) {
		
		validateContact(contacts);

		LinkedList<Contact> returnContact = new LinkedList<Contact>();

		for (Contact orgContact : contacts) {

				
			
			//Contact contactEntity = contactRepository.findById(new ContactId(orgContact.getProperty_id(), orgContact.getZip()));
			Optional<Contact> opt = contactRepository.findById(
				    new ContactId(orgContact.getProperty_id(), orgContact.getZip())
				);
			
			Contact contactEntity = opt.orElseThrow(() ->
		    new IllegalArgumentException("Contact not found for property_id=" +
		        orgContact.getProperty_id() + ", zip=" + orgContact.getZip()));
		    
			List<Comments> commentEntitylst = contactEntity.getComments();

			if (commentEntitylst != null) {
				commentEntitylst.removeIf(comment -> year.equals(comment.getYear()));
			}

			List<Comments> commentOrinalLst = orgContact.getComments();

			if (commentOrinalLst != null) {
				commentOrinalLst.removeIf(comment -> !year.equals(comment.getYear()));
			}
			
			if(commentEntitylst==null) {
				commentEntitylst = new ArrayList<Comments>();
				contactEntity.setComments(commentEntitylst);
			
			}
				
			commentEntitylst.addAll(commentOrinalLst);

			System.out.println("comments for::" + contactEntity.getProperty_id() + "==>" + commentEntitylst);
			
			contactEntity.setUpdated_datetime(LocalDateTime.now());
			contactEntity.setUser_id(orgContact.getUser_id());

			contactRepository.save(contactEntity);
			
			Optional<Contact> opt1 = contactRepository.findById(
				    new ContactId(orgContact.getProperty_id(), orgContact.getZip())
				);

			Contact updatedEntity = opt1.orElseThrow(() ->
		    new IllegalArgumentException("Contact not found for property_id=" +
		        orgContact.getProperty_id() + ", zip=" + orgContact.getZip()));
		    
		

			returnContact.add(updatedEntity);
		}

		return returnContact;

	}
	
	public String addContact(Contact contacts) {

		String message = null;
		
		if (contacts.getProperty_id() != null && !contacts.getProperty_id().trim().isEmpty()
				&& contacts.getCommunity_name() != null && !contacts.getCommunity_name().trim().isEmpty()
				&& contacts.getProperty_address() != null && !contacts.getProperty_address().trim().isEmpty()
				&& contacts.getName() != null && !contacts.getName().trim().isEmpty() && contacts.getZip() != null
				&& !contacts.getZip().trim().isEmpty() && contacts.getUser_id() != null
				&& !contacts.getUser_id().trim().isEmpty())

		{
			Optional<Contact> opt = contactRepository.findById(
				    new ContactId(contacts.getProperty_id(), contacts.getZip())
				);
						
			Contact contactEntity = opt.orElseThrow(() ->
		    new IllegalArgumentException("Contact not found for property_id=" +
		    		contacts.getProperty_id() + ", zip=" + contacts.getZip()));
		    

			if (contactEntity == null) {
				
				contactEntity = new Contact();
				contactEntity.setProperty_id(contacts.getProperty_id());
				contactEntity.setName(contacts.getName());
				contactEntity.setProperty_address(contacts.getProperty_address());
				contactEntity.setCommunity_name(contacts.getCommunity_name());
				contactEntity.setZip(contacts.getZip());
				contactEntity.setUser_id(contacts.getUser_id());
				contactEntity.setCreated_datetime(LocalDateTime.now());
								
				contactRepository.save(contactEntity);

				return message = "Contact Added successfully !!! " + contacts.toString();
			}else {
				
				return message = "Contact Already exists for this property !!! "+contactEntity.toString();
			}
		
		} else {

			return message = "Error while Adding Contact !!!";
		}

	}
	
	public void validateContact(List<Contact> contacts) {
	    for (Contact contact : contacts) {
	        List<Comments> comments = contact.getComments();
	        if (comments != null) {
	            for (Comments comment : comments) {
	                String noteStr = comment.getNote();
	                String monthStr = comment.getMonth();

	                // Validate note is an integer between 1 and 6
	                try {
	                    int note = Integer.parseInt(noteStr);
	                    if (note < 1 || note > 6) {
	                        throw new IllegalArgumentException("Invalid note: " + note + " for property ID: " + contact.getProperty_id() +
	                                ". Note must be between 1 and 6.");
	                    }
	                } catch (NumberFormatException e) {
	                    throw new IllegalArgumentException("Note is not a number: " + noteStr + " for property ID: " + contact.getProperty_id());
	                }

	                // Validate month is an integer between 1 and 12
	                try {
	                    int month = Integer.parseInt(monthStr);
	                    if (month < 1 || month > 12) {
	                        throw new IllegalArgumentException("Invalid month: " + month + " for property ID: " + contact.getProperty_id() +
	                                ". Month must be between 1 and 12.");
	                    }
	                } catch (NumberFormatException e) {
	                    throw new IllegalArgumentException("Month is not a number: " + monthStr + " for property ID: " + contact.getProperty_id());
	                }
	            }
	        }
	    }
	}

	

	/**
	 * Get all distinct states from communities.
	 */
	public LinkedList<String> getAllStates() {
		return communityRepository.findAllStates();
	}

	/**
	 * Get all distinct states from communities.
	 */
	public LinkedList<String> getAllZip() {
		return communityRepository.findAllZip();
	}
	
	/**
	 * Get all distinct states from communities.
	 */
	public LinkedList<CityZipDTO> findAllZipAndCity() {
		return communityRepository.findAllZipAndCity();
	}

	public LinkedList<String> getCommunitiesByZip(String zip) {
		return communityRepository.findCommunitiesByZip(zip);
	}
	
	public LinkedList<String> getCommunities() {
		return communityRepository.findCommunities();
	}

	/**
	 * Get distinct cities for a given state.
	 * 
	 * @param state the state abbreviation (e.g., "TX")
	 */
	public LinkedList<String> getCitiesByState(String state) {
		return communityRepository.findDistinctCitiesByState(state);
	}

	/**
	 * Get communities for a given city and state.
	 * 
	 * @param city  the city name
	 * @param state the state abbreviation
	 */
	public LinkedList<String> getCommunitiesByCityAndState(String city, String state) {
		return communityRepository.findCommunitiesByCityAndState(city, state);
	}

}
