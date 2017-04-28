package org.example.bcp.name;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
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
	
	Properties props;
	
	public static final String PROPERTIES_KEY = "extractor.props.name";
	public static final String DBFILE_PROPERTY_KEY = "names.db.file";
	
	// will help to resolve some kinds of ambiguity (i.e. where person names appear in
	// company names, such as "Arthur Anderson Corp"
	// TODO externalize this listing so we can add cases
	String [] companyIndicatorsArr = { "LLC", "LLC.", "INC", "INC.", "CORP", "CORP.",
			"CORPORATION", "CO", "CO.", "ASSOCIATES", "CONSULTING", "TECH", "GLOBOCORP" };
	Set<String> companyIndicators;
	
	/**
	 * @param props
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws RecordProcessingException if properties are malformed
	 */
	public NameExtractor(Properties props) throws FileNotFoundException, IOException, RecordProcessingException {
		this.props = getProperties(props);
		nameDatabase = new NameDatabase(this.props.getProperty(DBFILE_PROPERTY_KEY));
		
		companyIndicators = new HashSet<String>(Arrays.asList(companyIndicatorsArr));
	}
	
	/**
	 * Get the nested properties for this extractor. Also check them to see that they're all there.
	 * @param props
	 * @return
	 * @throws RecordProcessingException 
	 */
	private Properties getProperties(Properties props) throws RecordProcessingException {
		Object val = props.get(PROPERTIES_KEY);
		if (val == null) {
			throw new RecordProcessingException(
				"main properties file does not contain an entry for " + PROPERTIES_KEY
			);
		}
		if (!(val instanceof Properties)) {
			throw new RecordProcessingException(
				"value associated with " + PROPERTIES_KEY + " is not a Properties instance"
			);
		}
		Properties myProps = (Properties) val;
		if (!myProps.containsKey(DBFILE_PROPERTY_KEY)) {
			throw new RecordProcessingException(
				"name extractor properties does not include an entry for " + DBFILE_PROPERTY_KEY
			);
		}
		
		return myProps;
	}

	@Override
	public String getField(List<String> fieldSet) throws RecordProcessingException {
		int index = 0;
		int chosenIndex = -1;
		int rank = Integer.MAX_VALUE;
		for (String field : fieldSet) {
			int tmpRank = getNameRank(field);
			if (tmpRank > 0 && !isCorpProbably(field) && !isAddressProbably(field)) {
				if (rank < Integer.MAX_VALUE) {
					System.out.println("WARNING: got more than one potential person name, going to have to choose");
				}
				
				// if new ranking is lower, then use it
				if (tmpRank < rank) {
					chosenIndex = index;
					rank = tmpRank;
				}
			}
			index++;
		}
		
		if (chosenIndex == -1) {
			throw new RecordProcessingException("name extractor could not locate a likely name in the provided data set");
		}
		
		return fieldSet.get(chosenIndex);
	}

	/**
	 * Decides if a string is likely to be a street address. For now, our 
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
	 * probable corp names.
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
	 * Generates a ranking value for a string in an attempt to indicate how strongly
	 * a name-like string resembles a name.
	 * 
	 * For the moment, we use the last-name most->least common ranking values from our
	 * names database to provide the rank value. This is really not the best heuristic,
	 * as the database ranks according to frequency, which is not the same as nameiness.
	 * Could probably improve on this if we had first names too.
	 * 
	 * Split the field into whitespace- and hyphen-delimited terms and looks to
	 * see if each term is in our names database.
	 * Since our names database is actually surnames, we search the list of
	 * terms in reverse order to save a little time.
	 * @param s
	 * @return true if this is likely a person name, false otherwise
	 */
	private int getNameRank(String s) {
		String [] terms = s.split("\\s+|-");
		for (int i=terms.length-1; i>=0; i--) {
			if (nameDatabase.containsName(terms[i])) {
				// the first hit will produce our rank value
				return nameDatabase.rankFor(terms[i]);
			}
		}
		return -1;
	}
	
//	private String highestRanking()
}
