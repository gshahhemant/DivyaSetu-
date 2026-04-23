package com.swadyay.data.ai.sanantonio.services;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

@Service
public class CsvProcessor {

   

	@Autowired
	OpenAIService openAIService;
	
	@Autowired
	ContactRepository  contactRepository;

	private final String[] headers = { "Property ID", "Geographic ID", "Type", "Property Address", "Legal Description",
			"Owner Name", "Doing Business As", "Appraised Value" };

    
	public void processCsv(String inputFile) {
		 String zip =inputFile;
		String outputFile="src/main/resources/"+inputFile+"_processed.csv";
		
		inputFile="src/main/resources/"+inputFile+".csv";
		
		List<PropertyRecord> validRecords = new ArrayList<>();

		try (BufferedReader rawReader = Files.newBufferedReader(Paths.get(inputFile))) {
			List<String[]> cleanLines = new ArrayList<>();
			String line;
			int lineNumber = 0;
			int expectedFieldCount = 8;

			while ((line = rawReader.readLine()) != null) {
				lineNumber++;

				if (line.trim().isEmpty())
					continue;

				try (CSVReader csvReader = new CSVReader(new StringReader(line))) {
					String[] fields = csvReader.readNext();

					if (fields == null)
						continue;

					cleanLines.add(fields);
				} catch (Exception e) {
					System.err.println("⚠️ Skipped bad line " + lineNumber + ": " + e.getMessage());

					System.err.println(line);

				}
			}

			System.out.print("total clened size:::" + cleanLines.size());
			boolean result = false;
			// ✅ Convert to POJO & filter
			for (String[] fields : cleanLines) {
				PropertyRecord record = new PropertyRecord();
				record.setPropertyId(fields[0]);
				record.setGeographicId(fields[1]);
				record.setType(fields[2]);
				record.setPropertyAddress(fields[3]);
				record.setLegalDescription(fields[4]);
				record.setOwnerName(fields[5]);
				record.setDoingBusinessAs(fields[6]);
				record.setAppraisedValue(fields[7]);

				if (record.getDoingBusinessAs() == null || record.getDoingBusinessAs().trim().isEmpty()) {

					result = openAIService.isIndianName(fields[5]);
					
					System.out.println("************result"+result);

					if (result) {
						validRecords.add(record);
					}
				}
			}

			System.out.print("total Indian names for  :::" + validRecords.size());

			// ✅ Sort
			validRecords = validRecords.stream().sorted(
					Comparator.comparing(PropertyRecord::getGeographicId, Comparator.nullsLast(String::compareTo)))
					.collect(Collectors.toList());

			// ✅ Write output
			try (CSVWriter writer = new CSVWriter(new FileWriter(outputFile))) {
				writer.writeNext(headers);
				for (PropertyRecord record : validRecords) {
					writer.writeNext(new String[] { record.getPropertyId(), record.getGeographicId(), record.getType(),
							record.getPropertyAddress(), record.getLegalDescription(), record.getOwnerName(),
							record.getDoingBusinessAs(), record.getAppraisedValue() });
				}
			}

			
			
			
			System.out.println("✅ Output written to: " + outputFile);
			
			dumpDataToDatabase(validRecords,zip);
			
			

		} catch (IOException e) {
			throw new RuntimeException("Failed processing CSV", e);
		}
	}
	public void dumpDataToDatabase(List<PropertyRecord> validRecords,String zip) {
		int i =0;
		for (PropertyRecord validRecord: validRecords ) {
			Contact contact = new Contact();
			contact.setProperty_id(validRecord.getPropertyId());
			contact.setName(validRecord.getOwnerName());
			contact.setProperty_address(validRecord.getPropertyAddress());
			contact.setCommunity_name(validRecord.getLegalDescription());
			contact.setDetails_url(validRecord.getGeographicId());
			contact.setZip(zip);
			
			contactRepository.save(contact);
			i++;
			
		}
		
		System.out.println("✅ Total records save dto DB  written to::: " + i);
	}
}
