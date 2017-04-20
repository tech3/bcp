package org.example.bcp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

/**
 * Main method lives here. This classes manages overall inputs, b-card
 * processing logic and outputs.
 * 
 * Mostly the thing to know is that this program watches a directory to see if
 * new raw data files ever appear there. If they do, then each one is treated as
 * a single business-card's data and data is extracted from it.
 * @author astein
 *
 */
public class BusinessCardProcessor {
	
	BusinessCardParser cardParser;
	
	Path watchDir;
	Path processedDir;
	Path failedDir;

	public static void main(String[] args) throws FileNotFoundException, IOException {
		if (args.length < 1) {
			System.err.println("Please provide a directory where raw business card data files will be stored.");
			return;
		}
		
		if (args.length > 1) {
			System.err.println("Too many arguments.\nPlease provide a directory where raw business card data files will be stored.");
			return;
		}
		
		if (!Files.isDirectory(Paths.get(args[0]))) {
			System.err.println("Provided path argument is not a directory.");
			return;
		}
		
		BusinessCardProcessor bcp = new BusinessCardProcessor();
		
		bcp.doProcessing(args[0]);
	}

	/**
	 * Main processing loop. Watches a directory for files that contain newly-OCR'd, raw
	 * business card text files and processes each one in turn.
	 * @param rawDataPath
	 * @throws IOException
	 */
	private void doProcessing(String rawDataPath) throws IOException {
		// WatchService management code cribbed (but then heavily
		// modified) from an example at
		// http://www.codejava.net/java-se/file-io/file-change-notification-example-with-watch-service-api
		
		WatchService watch = FileSystems.getDefault().newWatchService();
		Path dir = Paths.get(rawDataPath);
		dir.register(watch, StandardWatchEventKinds.ENTRY_CREATE);
		
		while (true) {
			WatchKey watchKey;
			try {
				// wait for directory events
				watchKey = watch.take();
			} catch (InterruptedException ex) {
				System.out.println("got interrupt, stopping work...");
				return;
			}

			for (WatchEvent<?> event : watchKey.pollEvents()) {
				// get event type
				WatchEvent.Kind<?> kind = event.kind();

				// get file name
				@SuppressWarnings("unchecked")
				WatchEvent<Path> ev = (WatchEvent<Path>) event;
				Path fileName = dir.resolve(ev.context());

				if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
					System.out.println("processing file: " + fileName);
					if (!Files.isDirectory(fileName, LinkOption.NOFOLLOW_LINKS) &&
						!Files.isSymbolicLink(fileName)) {
						processFile(fileName);
					} else {
						System.out.println("file '" + fileName + "' is not a processable file");
						continue;
					}

				} else {
					System.err.println("got an unexpected");
				}
			}

			boolean valid = watchKey.reset();
			if (!valid) {
				break;
			}
		}
	}
	
	/**
	 * Processes a single raw data file to produce some structured contact info.
	 * *** NOTE *** This method intentionally swallows exceptions raised due to file I/O problems and data
	 * processing problems! We don't want such exceptions to bring down the application, so they're stopped
	 * at this level. Errors will be reported to STDERR and processing will be allowed to resume.
	 * @param file
	 */
	private void processFile(Path file) {
		String rawData;
		try {
			rawData = new String(Files.readAllBytes(file));
		} catch (IOException e) {
			System.err.println("unable to read input file '" + file + "', due to error: '" + e.getMessage() + "'");
			e.printStackTrace();
			cleanUp(file, true);
			return;
		}

		try {
			// process the data
			ContactInfo info = cardParser.getContactInfo(rawData);
			handleContactInfo(rawData, info);
		} catch (RecordProcessingException e) {
			System.err.println("unable to process input file '" + file + "', due to error: '" + e.getMessage() + "'");
			e.printStackTrace();
			cleanUp(file, true);
			return;
		}
		
		cleanUp(file, false);
	}
	
	/**
	 * Does whatever ought to be done with some new contact info and the originating data.
	 * Currently, this is just printing the data to the console.
	 * @param rawData original text data
	 * @param info structured extracted contact info
	 */
	private void handleContactInfo(String rawData, ContactInfo info) {
		System.out.println(rawData + "\n\n==>\n\n" + info.prettyPrint() + "\n\n");
		
		// TODO combine raw data and structured info into a data record and store in contacts DB
	}
	
	/**
	 * Moves a file out of the input directory and into an archival location
	 * @param file
	 * @param failed indicated whether or not the file was successfully processed, results in a different archival place
	 */
	private void cleanUp(Path file, boolean failed) {
		try {
			if (failed) {
				Files.move(file, failedDir, StandardCopyOption.REPLACE_EXISTING);
			} else {
				Files.move(file, processedDir, StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException e) {
			System.err.println("unable to clean up file '" + file + "'");
			e.printStackTrace();
		}
	}
}
