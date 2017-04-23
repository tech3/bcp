package org.example.bcp.phone;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.example.bcp.FieldExtractor;
import org.example.bcp.RecordProcessingException;

public class PhoneNumberExtractor implements FieldExtractor {

	@Override
	public int getField(List<String> ocrRecord) throws RecordProcessingException {
		for (String s : ocrRecord) {
//			Pattern p = Pattern.compile(".+@");
			Pattern p = Pattern.compile(".*[0-9]{3}.*[0-9]{3}.*[0-9]{4}");
			Matcher m = p.matcher("7035551275");
			boolean b = m.matches();
		}
		
		return -1;
	}

}
