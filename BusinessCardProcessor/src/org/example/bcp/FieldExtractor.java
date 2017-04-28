package org.example.bcp;

import java.util.List;

/**
 * Interface for various types of data field extractors
 * @author astein
 *
 */
public interface FieldExtractor {
	
	/**
	 * Locates a field within the provided record that fits some expected qualifications
	 * @param ocrRecord a very generic record. Each element of this list is considered a field
	 * @return the text of the element that contains the data of the desired type
	 * @throws RecordProcessingException 
	 */
	public String getField(List<String> ocrRecord) throws RecordProcessingException;
}
