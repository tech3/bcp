package org.example.bcp;

import java.util.List;

/**
 * Interface for various
 * @author astein
 *
 */
public interface FieldExtractor {
	
	/**
	 * Locates a field within the provided record that fits some expected qualifications
	 * @param ocrRecord a very generic record. each element of this list is considered a field
	 * @return the index of the element that contains the data of the desired type
	 * @throws RecordProcessingException 
	 */
	public String getField(List<String> ocrRecord) throws RecordProcessingException;
}
