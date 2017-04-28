package org.example.bcp.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Processes a CSV file into record objects (String arrays, for our modest purposes).
 * 
 * @author astein
 *
 */
public class CsvProcessor implements Iterable<List<String>>, Iterator<List<String>> {
	
	BufferedReader csvReader = null;
	String currentBuffer;
	
	public CsvProcessor(BufferedReader csvReader) {
		this.csvReader = csvReader;
	}
	
	/**
	 * Breaks a line of text into its comma-delimited elements.
	 * At this time, this parser does not account for escaped (i.e. double-quote-wrapped) CSV fields.
	 * @param csvLine
	 * @return
	 */
	public static List<String> parseCsvLine(String csvLine) {
		return Arrays.asList(csvLine.split(","));
	}

	@Override
	public boolean hasNext() {
		if (csvReader == null) {
			throw new NullPointerException("csvReader was null");
		}
		
		try {
			currentBuffer = csvReader.readLine();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		if (currentBuffer == null) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public List<String> next() {
		return parseCsvLine(currentBuffer);
	}

	@Override
	public Iterator<List<String>> iterator() {
		return this;
	}
}
