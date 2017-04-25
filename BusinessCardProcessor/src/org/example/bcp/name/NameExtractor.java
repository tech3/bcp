package org.example.bcp.name;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.example.bcp.FieldExtractor;
import org.example.bcp.RecordProcessingException;

/**
 * A field extractor that finds things that look like names.
 * @author astein
 *
 */
public class NameExtractor implements FieldExtractor {

	NameDatabase nameDatabase;
	
	// will help to resolve some kinds of ambiguity (i.e. where person names appear in
	// company names, such as "Arthur Anderson Corp"
	// TODO externalize this listing so we can add cases
	String [] companyIndicatorsArr = { "LLC", "LLC.", "INC", "INC.", "CORP", "CORP.",
			"CORPORATION", "CO", "CO.", "ASSOCIATES", "CONSULTING" };
	Set<String> companyIndicators;
	
	public NameExtractor() throws FileNotFoundException, IOException {
		nameDatabase = new NameDatabase();
		
		if (nameDatabase.containsName("all other names")) {
			System.out.println("found all other names");
		} else {
			System.out.println("did not find all other names");
		}
		
		companyIndicators = new HashSet<String>(Arrays.asList(companyIndicatorsArr));
	}

	@Override
	public String getField(List<String> fieldSet) throws RecordProcessingException {
		List<Integer> nameFieldIndexes = new ArrayList<Integer>();
		int i = 0;
		for (String s : fieldSet) {
			if (isNameProbably(s) && !isCorpProbably(s) && !isAddressProbably(s)) {
				nameFieldIndexes.add(i);
			}
			i++;
		}
		
		if (nameFieldIndexes.size() == 1) {
			return fieldSet.get(nameFieldIndexes.get(0));
		} else if (nameFieldIndexes.size() < 1) {
			throw new RecordProcessingException("name extractor could not locate a likely name in the provided data set");
		} else { // more than one hit
			throw new RecordProcessingException("name extractor located multiple likely names in the provided data set");
		}
	}

	/**
	 * Decides is a string is likely to be a street address. For now, our 
	 * heuristic is that if it starts with one or more digits then it's
	 * more likely to be a street address than a person's name.
	 * 
	 * This could be strengthened by checking the end of the string for things
	 * like "Court", "Ct", "Street" and so on.
	 * @param s
	 * @return
	 */
	private boolean isAddressProbably(String s) {
		if (s.trim().matches("^\\d+")) {
			return true;
		}
		return false;
	}

	/**
	 * Decides if a string is likely to be a corporation or other business
	 * entity name. Runs through a listing of typical substrings that indicate a
	 * corp is being referred to and treats those that end with such substrings as
	 * probably corp names.
	 * @param s
	 * @return
	 */
	private boolean isCorpProbably(String s) {
		String sUpper = s.trim().toUpperCase();
		for (String corp : companyIndicators) {
			if (sUpper.endsWith(corp)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Decides if the given field looks like a person name.
	 * Split the field into space and hyphen, delimited terms and looks to
	 * see if each term is in our names database.
	 * Since our names database is actually surnames, we search the list of
	 * terms in reverse order to save a little time.
	 * @param s
	 * @return true if this is likely a person name, false otherwise
	 */
	private boolean isNameProbably(String s) {
		String [] terms = s.split("");
		for (int i=terms.length-1; i==0; i--) {
			if (nameDatabase.containsName(terms[i])) {
				return true;
			}
		}
		return false;
	}
}
