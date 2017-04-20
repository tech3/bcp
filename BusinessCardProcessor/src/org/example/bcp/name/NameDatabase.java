package org.example.bcp.name;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.example.bcp.util.CsvProcessor;

/**
 * A very simple in-mem database of all names.
 * Currently, 'all' defined by US 2010 Census data for surnames which appear in census at least 100 times.
 * Once upon a time, it was possible to get all census names, first and last, regardless of frequency. Sadly, this data
 * seems to be no longer available from .gov.
 * @author astein
 *
 */
public class NameDatabase {
	
	// a very simple database should allow us to do very fast look-ups
	Set<String> nameDatabase = new HashSet<String>();
	
	public NameDatabase() throws FileNotFoundException, IOException {
		try (BufferedReader bufReader =
			new BufferedReader(new FileReader("/Users/astein/Documents/workspace/BusinessCardProcessor/resources/Names_2010Census.csv"))) {
			CsvProcessor csvProc = new CsvProcessor(bufReader);
			boolean first = true;
			int nameIndex = 0;
			for (List<String> rec : csvProc) {
				if (first) {
					// first row should tell us which field contains our name data
					nameIndex = rec.indexOf("name");
					first = false;
				} else {
					nameDatabase.add(rec.get(nameIndex).toUpperCase());
				}
			}
			System.out.println("size: " + nameDatabase.size());
		}
	}
	
	public boolean containsName(String name) {
		return nameDatabase.contains(name.toUpperCase());
	}
	
	public int size() {
		return nameDatabase.size();
	}
}
