package org.example.bcp;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Sets up properties object for a program 
 * @author astein
 *
 */
public class PropertiesLoader {
	
	private static String extractorPrefix = "extractor.props.";
	
	/**
	 * Produces a properties object with entries that have string values as well as
	 * values that are themselves Properties instances. The latter are loaded for any
	 * FieldExtractor implementations that require external properties.
	 * 
	 * Any property in propertiesFile that begins with "extractor.props." will be treated
	 * as a file name from which to load an additional Properties instance. The value for
	 * this property will be replaced with the Properties instance.
	 * 
	 * @param propertiesFile
	 * @return
	 * @throws IOException
	 */
	public static Properties loadProperties(String propertiesFile) throws IOException {
		// load base properties
		Properties props = new Properties();
//		InputStream is = new FileInputStream(propertiesFile);
		InputStream is = PropertiesLoader.class.getResourceAsStream(propertiesFile);
		props.load(is);
		is.close();
		
		// if any extractor properties, load those props into a temp map
		Map<String, Properties> tmp = new HashMap<String, Properties>();
		for (Object key : props.keySet()) {
			if (((String)key).startsWith(extractorPrefix)) {
				Properties exProps = new Properties();
//				InputStream exis = new FileInputStream((String)props.get(key));
				InputStream exis = PropertiesLoader.class.getResourceAsStream("/"+(String)props.get(key));
				exProps.load(exis);
				tmp.put((String)key, exProps);
			}
		}
		
		// swap extractor properties instances in for strings that were
		// there originally
		for (String key : tmp.keySet()) {
			props.put(key, tmp.get(key));
		}
		
		return props;
	}
}
