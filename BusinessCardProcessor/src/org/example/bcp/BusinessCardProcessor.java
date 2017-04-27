package org.example.bcp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.DirectoryStream;
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
import java.util.Properties;

/**
 * Main method lives here. This class manages overall inputs, b-card
 * processing logic and outputs.
 * 
 * Mostly the thing to know is that this program watches a directory to see if
 * new raw data files ever appear there. If they do, then each one is treated as
 * a single business-card's data and data is extracted from it.
 * 
 * When running, a properties file must be provided on the command line. That file must
 * contain the properties 'bcp.rawdata.dir', 'bcp.processed.dir' and 'bcp.failed.dir'.
 * @author astein
 *
 */
public class BusinessCardProcessor {
	
	BusinessCardParser cardParser;
	
	static final String DEFAULT_PROPS_FILE = "bcp.properties";
	
	static Path watchDir;
	static final String RAW_DIR_KEY = "bcp.rawdata.dir";
	
	static Path processedDir;
	static final String PROCESSED_DIR_KEY = "bcp.processed.dir";
	
	static Path failedDir;
	static final String FAILED_DIR_KEY = "bcp.failed.dir";
	
	Properties props;

	public BusinessCardProcessor(Properties props) throws FileNotFoundException, IOException, RecordProcessingException {
		this.props = props;
		this.cardParser = new BusinessCardParser(this.props);
	}

	/**
	 * Main processing loop. Watches a directory for files that contain newly-OCR'd, raw
	 * business card text files and processes each one in turn.
	 * @param rawDataPath
	 * @throws IOException
	 */
	private void doProcessing() throws IOException {
		// process any files that are already waiting in the watched dir
		catchUp();
		
		// *************************
		// race condition lives here
		// *************************
		
		// WatchService management code cribbed (but then heavily
		// modified) from an example at
		// http://www.codejava.net/java-se/file-io/file-change-notification-example-with-watch-service-api
		WatchService watch = FileSystems.getDefault().newWatchService();
		watchDir.register(watch, StandardWatchEventKinds.ENTRY_CREATE);
		
		System.out.println("watching directory '" + watchDir + "' for new business cards to process...\n");
		
		while (true) {
			WatchKey watchKey;
			try {
				// block on directory events
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
				Path fileName = watchDir.resolve(ev.context());

				if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
					System.out.println("processing file: " + fileName + "\n");
					if (!Files.isDirectory(fileName, LinkOption.NOFOLLOW_LINKS) &&
						!Files.isSymbolicLink(fileName)) {
						processFile(fileName);
					} else {
						System.out.println("file '" + fileName + "' is not a processable file");
						continue;
					}

				} else {
					System.err.println("got an unexpected watch event");
				}
			}

			boolean valid = watchKey.reset();
			if (!valid) {
				break;
			}
		}
	}
	
	/**
	 * Processes all of the (regular) files in the watched directory
	 * @throws IOException
	 */
	private void catchUp() throws IOException {
		try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(watchDir, Files::isRegularFile)) {
			for (Path file : dirStream) {
				System.out.println("processing file: " + file + "\n");
				processFile(file);
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
				Files.move(file, failedDir.resolve(file.getFileName()), StandardCopyOption.REPLACE_EXISTING);
			} else {
				Files.move(file, processedDir.resolve(file.getFileName()), StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException e) {
			System.err.println("unable to clean up file '" + file + "'");
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException, RecordProcessingException {
		if (args.length > 1) {
			System.err.println("Too many arguments. Please provide a properties file.");
			return;
		}
		
		// optionally allow one to specify a different properties file (new file
		// still needs to be (re)packaged in the runnable jar though)
		String propsFile = DEFAULT_PROPS_FILE;
		if (args.length == 1) {
			propsFile = args[0];
		}
		
		Properties props = PropertiesLoader.loadProperties("/" + propsFile);
		doInit(props);
		
		BusinessCardProcessor bcp = new BusinessCardProcessor(props);
		
		// kick off the main loop
		bcp.doProcessing();
	}
	
	/**
	 * Any startup tasks. Mostly directory checking. Missing properties get checked for as
	 * a side effect.
	 * @param props
	 * @throws IOException 
	 * @throws RecordProcessingException 
	 */
	private static void doInit(Properties props) throws IOException, RecordProcessingException {
		// are properties present?
		if (props.get(RAW_DIR_KEY) == null) {
			throw new RecordProcessingException("properties file missing the '" + RAW_DIR_KEY + "' property");
		}
		if (props.get(PROCESSED_DIR_KEY) == null) {
			throw new RecordProcessingException("properties file missing the '" + PROCESSED_DIR_KEY + "' property");
		}
		if (props.get(FAILED_DIR_KEY) == null) {
			throw new RecordProcessingException("properties file missing the '" + FAILED_DIR_KEY + "' property");
		}
		
		// create/check perms on any directories needed
		watchDir = Paths.get((String)props.get(RAW_DIR_KEY));
		if (!Files.exists(watchDir, LinkOption.NOFOLLOW_LINKS)) {
			Files.createDirectory(watchDir);
		}
		if (!Files.isReadable(watchDir)) {
			throw new RecordProcessingException("unable to read from raw data directory '" + watchDir + "'");
		}
		
		processedDir = Paths.get((String)props.get(PROCESSED_DIR_KEY));
		if (!Files.exists(processedDir, LinkOption.NOFOLLOW_LINKS)) {
			Files.createDirectory(processedDir);
		}
		if (!Files.isWritable(processedDir)) {
			throw new RecordProcessingException("unable to write to raw data directory '" + processedDir + "'");
		}
		
		failedDir = Paths.get((String)props.get(FAILED_DIR_KEY));
		if (!Files.exists(failedDir, LinkOption.NOFOLLOW_LINKS)) {
			Files.createDirectory(failedDir);
		}
		if (!Files.isWritable(failedDir)) {
			throw new RecordProcessingException("unable to write to raw data directory '" + failedDir + "'");
		}
	}
}
