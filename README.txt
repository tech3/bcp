BusinessCardProcessor is a program that accepts files containing data
from a notional process that OCRs business cards. It will process
files and extract several fields of information from the text and
produce a structured record.

============
HOW TO BUILD
============

1. Have a JDK and JAVA_HOME set.
2. Have Ant. Make sure it's in your command-line environment's path.
3. Clone the git repo at https://github.com/tech3/bcp.git
4. In that repo's BusinessCardProcessor directory, run "ant". The default ant target
   will cause a jar file to be built and put in BusinessCardProcessor/target.
5. If, in the target directory, you now find a file named "bcp.jar", then you
   have built the program.

==========
HOW TO RUN
==========

1. Have a JRE. Make sure the java executable is in your path.
2. bcp.jar is packaged as a runnable jar file. So, at the command-line, go to
   where bcp.jar is (we'll call this <bcp-home>) and run:

	> java -jar bcp.jar
	
3. The program is now running. It ought to create several directories and then
   say that it's watching a directory for new data and then not do much more.
4. Give the program some data. Copy the text files from BusinessCardProcessor/bcards
   into <bcp-home>/new_bcards
5. The program will go into action and process the files.
6. A text file will be produced, named "contact-data.txt". For each file processed,
   the contents of each text file will be listed, followed by a some formatted
   output of the structured data record that has been extracted by the program.
7. Processed files will be copied to either <bcp-home>/processed_cards or
   <bcp-home>/failed_cards, depending on the outcome of being processed.
8. The program can be terminated by typing Ctrl-C on the command line.
