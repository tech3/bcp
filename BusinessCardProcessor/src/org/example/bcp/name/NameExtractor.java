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
	
	// will help to resolve some kinds of ambiguity
	String [] companyIndicatorsArr = { "LLC", "LLC.", "INC", "INC.", "CORP", "CORP.", "CORPORATION", "CO", "CO." };
	String [] addressIndicatorsArr = { "Drive", "LLC.", "INC", "INC.", "CORP", "CORP.", "CORPORATION", "CO", "CO." };
	Set<String> killSet;
	
	public NameExtractor() throws FileNotFoundException, IOException {
		nameDatabase = new NameDatabase();
		
		if (nameDatabase.containsName("all other names")) {
			System.out.println("found all other names");
		} else {
			System.out.println("did not find all other names");
		}
		
		killSet = new HashSet<String>(Arrays.asList(companyIndicatorsArr));
	}

	@Override
	public int getField(List<String> fieldSet) throws RecordProcessingException {
		List<Integer> nameFieldIndexes = new ArrayList<Integer>();
		int i = 0;
		for (String s : fieldSet) {
			if (isNameProbably(s)) {
				nameFieldIndexes.add(i);
			}
			i++;
		}
		
		if (nameFieldIndexes.size() == 1) {
			return nameFieldIndexes.get(0);
		}
		
		if (nameFieldIndexes.size() < 1) {
			throw new RecordProcessingException("name extractor could not locate a likely name in the provided data set");
		}
		
		if (nameFieldIndexes.size() > 1) {
			// try to winnow out company names that looked like person names
			for (Integer idx : nameFieldIndexes) {
				if (isCompanyNameProbably(fieldSet.get(idx))) {
					
				}
			}
			throw new RecordProcessingException("name extractor located multiple likely names in the provided data set");
		}
		
		return 0;
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
	
	/**
	 * Decides if the given field is a the name of a company or business.
	 * @param s
	 * @return true if a company name, false otherwise
	 */
	private boolean isCompanyNameProbably(String s) {
		String [] terms = s.split("");
		for (int i=terms.length-1; i==0; i--) {
			// company/employer names can contains some common person
			// names, so try to identify this and remove it
			if (killSet.contains(terms[i].toUpperCase())) {
				return true;
			}
		}
		return false;
	}
}
