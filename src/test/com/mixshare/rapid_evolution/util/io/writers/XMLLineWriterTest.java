package test.com.mixshare.rapid_evolution.util.io.writers;

import java.util.UUID;
import java.util.Vector;

import org.apache.log4j.Logger;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.util.io.readers.XMLLineReader;
import com.mixshare.rapid_evolution.util.io.writers.XMLLineWriter;

/**
 * Tests both the XML line readers and writer
 */
public class XMLLineWriterTest extends RE3TestCase {

    static private Logger log = Logger.getLogger(XMLLineWriterTest.class);
	
	public void testXMLLineWriter() {
		String nextLine = null;
		try {
			String filename = "data/junit/temp/xmlwritetest.xml";
			XMLLineWriter writer = new XMLLineWriter(filename);
			Vector<String> testStrings = new Vector<String>();
			for (int i = 0; i < 1000; ++i) {
				String randomString = UUID.randomUUID().toString();
				writer.writeLine(randomString);
				testStrings.add(randomString);
			}
			writer.close();
			XMLLineReader reader = new XMLLineReader(filename);
			int i = 0;
			nextLine = reader.getNextLine();
			while (nextLine != null) {
				if (!nextLine.equals(testStrings.get(i)))
					fail("read incorrect line at " + i + ", is=" + nextLine + ", expected=" + testStrings.get(i));
				nextLine = reader.getNextLine();
				++i;
			}
			reader.close();
		} catch (Exception e) {
			log.error("testXMLLineWriter(): error, last line=" + nextLine, e);
			fail(e.toString());
		}
	}
	
}
