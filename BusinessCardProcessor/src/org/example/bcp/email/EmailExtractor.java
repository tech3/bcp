package org.example.bcp.email;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.example.bcp.FieldExtractor;
import org.example.bcp.RecordProcessingException;

/**
 * A pretty simple data extractor for email addresses. Uses a regex to find an email address field, if any.
 * @author astein
 *
 */
public class EmailExtractor implements FieldExtractor {
	
	// a very generous email pattern; not trying to validate against
	// the W3C spec. just any text, followed by an '@', followed by text
	// containing at least one '.'
	// should prevent @twitter handles from matching
	Pattern emailPattern = Pattern.compile(".+@.+\\..+");

	@Override
	public String getField(List<String> ocrRecord) throws RecordProcessingException {
		for (String s : ocrRecord) {
			Matcher emailMatcher = emailPattern.matcher(s.trim());
			if (!emailMatcher.matches()) {
				continue;
			}
			return s.trim();
		}
		
		throw new RecordProcessingException("phone number extractor cannot locate email address in OCR record");
	}

}
