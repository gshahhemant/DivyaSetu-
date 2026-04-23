package com.swadhyaydata.app.sanantonio.services;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.swadhyaydata.app.config.AppListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CsvProcessor {

    private final AppListener appListener;

	@Autowired
	OpenAIService openAIService;

	private final String[] headers = { "Property ID", "Geographic ID", "Type", "Property Address", "Legal Description",
			"Owner Name", "Doing Business As", "Appraised Value" };

    CsvProcessor(AppListener appListener) {
        this.appListener = appListener;
    }

	public void processCsv(String inputFile, String outputFile) {
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

			System.out.print("total Indian names :::" + validRecords.size());

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

		} catch (IOException e) {
			throw new RuntimeException("Failed processing CSV", e);
		}
	}
}
