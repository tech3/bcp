package org.example.bcp;

/**
 * Converts the raw text from machine-read business cards into structured data.
 * @author astein
 *
 */
public class BusinessCardParser {
	
	/**
	 * Main entry point to access this class's conversion functionality.
	 * @param document The raw text of a business card. The only formatting requirement
	 * for this parameter is that each element of information (e.g. name, email...) be
	 * on a separate line (i.e separated by carriage return or CR-LF)
	 * @return A ContactInfo instance containing the parsed data.
	 */
	public ContactInfo getContactInfo(String document) throws RecordProcessingException {
		return null;
	}
}
