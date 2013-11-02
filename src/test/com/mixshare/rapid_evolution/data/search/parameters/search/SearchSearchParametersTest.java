package test.com.mixshare.rapid_evolution.data.search.parameters.search;

import org.apache.log4j.Logger;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.data.identifier.Identifier;
import com.mixshare.rapid_evolution.data.identifier.search.song.SongIdentifier;
import com.mixshare.rapid_evolution.data.search.parameters.search.song.SongSearchParameters;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineReaderFactory;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.mixshare.rapid_evolution.util.io.LineWriterFactory;

public class SearchSearchParametersTest extends RE3TestCase {

	static private Logger log = Logger.getLogger(SearchSearchParametersTest.class);	
	
	public void testXMLSerialization() {
		try {
			SongSearchParameters params = new SongSearchParameters();
			params.setRelativeProfileIds(new int[] { 1 });
			params.setRelativeProfileIdentifiers(new Identifier[] { new SongIdentifier("test") });
			
			params.setShowDisabled(true);
			
			LineWriter writer = LineWriterFactory.getLineWriter("data/junit/temp/songparams.jso");
			params.write(writer);
			writer.close();
			
			LineReader lineReader = LineReaderFactory.getLineReader("data/junit/temp/songparams.jso");
			params = new SongSearchParameters(lineReader);
			lineReader.close();			
			
			//String path = "data/junit/temp/songparams.xml";
			//XMLSerializer.saveData(params, path);			
			//params = (SongSearchParameters)XMLSerializer.readData(path);
			
			if (params.getRelativeProfileIds()[0] != 1)
				fail("incorrect profile id=" + params.getRelativeProfileIds()[0]);
			if (!params.getRelativeProfileIdentifiers()[0].equals(new SongIdentifier("test")))
				fail("incorrect profile identifier=" + params.getRelativeProfileIdentifiers()[0]);			
			
			if (!params.isShowDisabled())
				fail("not showing disabled");
			
		} catch (Exception e) {
			log.error("testXMLSerialization(): error", e);
			fail(e.getMessage());
		}		
	}
	
}
