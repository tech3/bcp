package org.example.bcp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.example.bcp.email.EmailExtractor;
import org.example.bcp.name.NameExtractor;
import org.example.bcp.phone.PhoneNumberExtractor;

/**
 * Converts the raw text from machine-read business cards into structured data.
 * @author astein
 *
 */
public class BusinessCardParser {
	
	Properties props;
	
	EmailExtractor emailEx = new EmailExtractor();
	PhoneNumberExtractor phoneEx = new PhoneNumberExtractor();
	NameExtractor nameEx;
	
	/**
	 * @param props should contain any sub-properties for whatever extractors may need them
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws RecordProcessingException
	 */
	public BusinessCardParser(Properties props) throws FileNotFoundException, IOException, RecordProcessingException {
		this.props = props;
		nameEx = new NameExtractor(props);
	}

	/**
	 * Main entry point to access this class's functionality.
	 * @param document The raw text of a business card. The only formatting requirement
	 * for this parameter is that each element of information (e.g. name, email...) be
	 * on a separate line (i.e separated by carriage return or CRLF)
	 * @return a ContactInfo instance containing the parsed, normalized data.
	 * @throws RecordProcessingException
	 */
	public ContactInfo getContactInfo(String document) throws RecordProcessingException {
		List<String> lines = splitLines(document);
		
		String name = nameEx.getField(lines);
		String phone = phoneEx.getField(lines);
		String email = emailEx.getField(lines);
		// extractors can be added here
		
		return new ContactInfo(name, phone, email);
	}
	
	/**
	 * Converts a document into a list of lines of text. Removes empty lines.
	 * Trims leading and trailing white-space from the lines.
	 * @param document
	 * @return
	 */
	private List<String> splitLines(String document) {
		document = document.replaceAll("\r\n", "\n");
		String [] lines = document.split("\n");
		
		List<String> cleanLines = new ArrayList<String>();
		
		for (int i=0; i<lines.length; i++) {
			String line = lines[i].trim();
			if (line.length() > 0) {
				cleanLines.add(line);
			}
		}
		
		return cleanLines;
	}
}
