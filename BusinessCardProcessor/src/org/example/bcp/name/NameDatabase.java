package org.example.bcp.name;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.example.bcp.util.CsvProcessor;

/**
 * A very simple in-mem database of all names.
 * Currently, 'all' is defined by US 2010 Census data for surnames which appear in census at least 100 times.
 * Once upon a time, it was possible to get all census names, first and last, regardless of frequency. Sadly, this data
 * seems to be no longer available from .gov.
 * @author astein
 *
 */
public class NameDatabase {
	
	// a very simple database should allow us to do very fast look-ups
	Map<String, DatabaseRecord> nameDatabase = new HashMap<String, DatabaseRecord>();
	
	/**
	 * Allows us to associate a name with its ranking.
	 * @author astein
	 *
	 */
	private class DatabaseRecord {
		public DatabaseRecord(String name, int rank) {
			this.name = name;
			this.rank = rank;
		}
		
		@SuppressWarnings("unused")
		String name;
		int rank;
	}
	
	/**
	 * Includes processing to init the database from the file
	 * @param databaseFile
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public NameDatabase(String databaseFile) throws FileNotFoundException, IOException {
		try (BufferedReader bufReader =	new BufferedReader(
				new InputStreamReader(this.getClass().getResourceAsStream("/" + databaseFile))
			)) {
			CsvProcessor csvProc = new CsvProcessor(bufReader);
			boolean first = true;
			int nameIndex = -1;
			int rankIndex = -1;
			for (List<String> rec : csvProc) {
				if (first) {
					// first row should tell us which field contains our name data
					nameIndex = rec.indexOf("name");
					rankIndex = rec.indexOf("rank");
					first = false;
				} else {
					nameDatabase.put(
						rec.get(nameIndex).toUpperCase(),
						new DatabaseRecord(rec.get(nameIndex).toUpperCase(), Integer.parseInt(rec.get(rankIndex))));
				}
			}
		}
	}
	
	public boolean containsName(String name) {
		return nameDatabase.get(name.toUpperCase()) != null ? true : false;
	}
	
	public int size() {
		return nameDatabase.size();
	}
	
	public int rankFor(String name) {
		if (containsName(name)) {
			return nameDatabase.get(name.toUpperCase()).rank;
		} else {
			return -1;
		}
	}
}
