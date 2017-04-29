package org.example.bcp.phone;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.example.bcp.FieldExtractor;
import org.example.bcp.RecordProcessingException;

/**
 * Finds a phone number (not a fax number) in a list of strings.
 * @author astein
 *
 */
public class PhoneNumberExtractor implements FieldExtractor {

	// a regex pattern that finds phone numbers
	// at the very least, the text must contain two sequences of three digits
	// followed by a sequence of four digits. the sequences may be separated,
	// preceded and followed by virtually anything.
	// we also capture () pretty much everything so we can inspect it for clues
	// as to whether this is a fax number or not.
	Pattern phonePattern = Pattern.compile("(.*)([\\d]{3}).*([\\d]{3}).*([\\d]{4})(.*)");
	
	String FAX = "FAX";
	String FACSIMILE = "FACSIMILE"; // archaic, yes. but, so is a fax machine.
	
	@Override
	public String getField(List<String> ocrRecord) throws RecordProcessingException {
		for (String s : ocrRecord) {
			Matcher phoneMatcher = phonePattern.matcher(s.trim());
			if (!phoneMatcher.matches()) {
				continue;
			}
			
			// filter out phone numbers that are really fax numbers
			if (phoneMatcher.group(1).toUpperCase().contains(FAX) || phoneMatcher.group(1).toUpperCase().contains(FACSIMILE) ||
				phoneMatcher.group(5).toUpperCase().contains(FAX) || phoneMatcher.group(5).toUpperCase().contains(FACSIMILE)) {
				continue;
			}
			
			return normalize(phoneMatcher);
		}
		
		throw new RecordProcessingException("phone number extractor cannot locate phone number in OCR record");
	}
	
	/**
	 * Generates a phone number formatted as a simple sequence of digits. Uses
	 * the context generated by an already-completed regex match to produce the
	 * required digit sequences.
	 * @param matchedPattern
	 * @return formatted phone number
	 */
	private String normalize(Matcher matchedPattern) {
		if (matchedPattern.group(2) != null && matchedPattern.group(2).compareTo("") != 0) {
			return matchedPattern.group(2) + matchedPattern.group(3) + matchedPattern.group(4);
		} else {
			return matchedPattern.group(3) + matchedPattern.group(4);
		}
	}

}
