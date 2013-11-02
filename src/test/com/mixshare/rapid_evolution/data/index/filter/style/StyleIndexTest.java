package test.com.mixshare.rapid_evolution.data.index.filter.style;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.filter.style.StyleIdentifier;
import com.mixshare.rapid_evolution.data.index.filter.style.StyleIndex;
import com.mixshare.rapid_evolution.data.index.imdb.LocalIMDB;
import com.mixshare.rapid_evolution.data.profile.filter.style.StyleProfile;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.filter.style.StyleRecord;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.data.submitted.search.song.SubmittedSong;
import com.mixshare.rapid_evolution.data.util.io.XMLSerializer;
import com.mixshare.rapid_evolution.data.util.table.UniqueIdTable;
import com.mixshare.rapid_evolution.ui.model.ModelManagerInterface;
import com.mixshare.rapid_evolution.ui.model.filter.style.StyleModelManager;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineReaderFactory;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.mixshare.rapid_evolution.util.io.LineWriterFactory;

public class StyleIndexTest extends RE3TestCase {
	
    static private Logger log = Logger.getLogger(StyleIndexTest.class);    

    /**
     * This tests merging styles and the effects on related songs.
     */
	public void testDuplicateStylesOnSongs() {
		try {
			// song 1
			SubmittedSong submittedSong1 = new SubmittedSong("aphex twin", "selected ambient works", "d1", "heliosphere", "");
			submittedSong1.setLabelName("rephlex");
			submittedSong1.addStyleDegreeValue("breaks", 1.0f, DATA_SOURCE_UNKNOWN);
						
			// song 2
			SubmittedSong submittedSong2 = new SubmittedSong("aphex twin", "some compilation album", "10", "heliosphere", "original mix");
			submittedSong2.setLabelName("warp");
			submittedSong2.addStyleDegreeValue("breakbeat", 1.0f, DATA_SOURCE_UNKNOWN);
			
			// song 3
			SubmittedSong submittedSong3 = new SubmittedSong("aphex twin", "selected ambient works volume 2", "a1", "stone in focus", "");
			submittedSong3.addStyleDegreeValue("breakbeat", 1.0f, DATA_SOURCE_UNKNOWN);
			submittedSong3.addStyleDegreeValue("breaks", 1.0f, DATA_SOURCE_UNKNOWN);
			
			// add the songs then get the created style profiles
			Database.add(submittedSong1);
			Database.add(submittedSong2);
			Database.add(submittedSong3);
						
			StyleProfile breaks = Database.getStyleIndex().getStyleProfile(new StyleIdentifier("breaks"));
			StyleProfile breakbeat = Database.getStyleIndex().getStyleProfile(new StyleIdentifier("breakbeat"));
			
			// merge the styles and then ensure each song now belongs to the primary style
			Database.mergeProfiles(breaks, breakbeat);
			
			SongRecord song1 = Database.getSongIndex().getSongRecord(submittedSong1.getSongIdentifier());			
			SongRecord song2 = Database.getSongIndex().getSongRecord(submittedSong2.getSongIdentifier());			
			SongRecord song3 = Database.getSongIndex().getSongRecord(submittedSong3.getSongIdentifier());			
			StyleRecord breaksRecord = Database.getStyleIndex().getStyleRecord(new StyleIdentifier("breaks"));
			
			if (!breaksRecord.matches(song1))
				fail("song doesn't belong to style");
			if (!breaksRecord.matches(song2))
				fail("song doesn't belong to style");
			if (!breaksRecord.matches(song3))
				fail("song doesn't belong to style");
			
			// check style descriptions on songs
			if (!song1.getActualStyleDescription().equals("breaks (100%)"))
				fail("incorrect style description, is=" + song1.getActualStyleDescription());
			if (!song1.getSourceStyleDescription().equals("breaks (100%)"))
				fail("incorrect style description, is=" + song1.getSourceStyleDescription());
			if (!song2.getActualStyleDescription().equals("breaks (100%)"))
				fail("incorrect style description, is=" + song2.getActualStyleDescription());
			if (!song2.getSourceStyleDescription().equals("breakbeat (100%)"))
				fail("incorrect style description, is=" + song2.getSourceStyleDescription());
			if (!song3.getActualStyleDescription().equals("breaks (100%)"))
				fail("incorrect style description, is=" + song3.getActualStyleDescription());
			if (!song3.getSourceStyleDescription().equals("breakbeat (100%); breaks (100%)"))
				fail("incorrect style description, is=" + song3.getSourceStyleDescription());
			
			// check # exact/actuals
			if (song3.getNumSourceStyles() != 2)
				fail("incorrect exact styles");
			if (!song3.getSourceStyleDegreeValues().toString().equals("[breakbeat (100%), breaks (100%)]"))
				fail("incorrect exact styles, is=" + song3.getSourceStyleDegreeValues());
			if (!song3.containsSourceStyle("breakbeat"))
				fail("incorrect exact styles");
			if (!song3.containsSourceStyle("breaks"))
				fail("incorrect exact styles");
			
			if (song3.getNumActualStyles() != 1)
				fail("incorrect actual styles");
			if (!song3.getActualStyleDegreeValues().toString().equals("[breaks (100%)]"))
				fail("incorrect exact styles");
			if (!song3.containsActualStyle("breakbeat"))
				fail("incorrect exact styles");
			if (!song3.containsActualStyle("breaks"))
				fail("incorrect exact styles");					
			
		} catch (Exception e) {
			log.error("testDuplicateStylesOnSongs(): error", e);
			fail(e.getMessage());
		}
	}
	
	public void testSerialization() {
		try {		
			StyleIndex index = new StyleIndex();
			
			UniqueIdTable uniqueIdTable = new UniqueIdTable(); // maps unique Ids to identifiers and vice versa
			StyleIdentifier styleId = new StyleIdentifier("test style");
		    int styleUniqueId = uniqueIdTable.getUniqueIdFromIdentifier(styleId);
			StyleIdentifier styleId2 = new StyleIdentifier("test style2");
		    int styleUniqueId2 = uniqueIdTable.getUniqueIdFromIdentifier(styleId2);
		    ((LocalIMDB)index.getImdb()).setUniqueIdTable(uniqueIdTable);
		    
		    StyleRecord styleRecord = new StyleRecord();
		    styleRecord.setId(styleId);
		    styleRecord.setUniqueId(styleUniqueId);

		    StyleRecord styleRecord2 = new StyleRecord();
		    styleRecord2.setId(styleId2);
		    styleRecord2.setUniqueId(styleUniqueId2);
		    
			Map<Integer, Record> recordMap = new HashMap<Integer, Record>(); // keeps track of all records, accessed by unique id
			recordMap.put(styleUniqueId, styleRecord);
			recordMap.put(styleUniqueId2, styleRecord2);
			((LocalIMDB)index.getImdb()).setRecordMap(recordMap);
			
		    Map<Integer, Record> duplicateIdMap = new HashMap<Integer, Record>(); // keeps track of duplicate ids, is separate so iterators don't pick up duplicatations...
		    duplicateIdMap.put(styleUniqueId2, styleRecord);
		    ((LocalIMDB)index.getImdb()).setDuplicateIdMap(duplicateIdMap);		    
		    
		    ModelManagerInterface modelManager = new StyleModelManager();
		    index.setModelManager(modelManager);
		    int columnCount = modelManager.getNumColumns();

			if (((LocalIMDB)index.getImdb()).getUniqueIdTable().getUniqueIdFromIdentifier(styleId) != styleUniqueId)
				fail("uniqueid table incorrect, is=" + ((LocalIMDB)index.getImdb()).getUniqueIdTable().getUniqueIdFromIdentifier(styleId) + ", expecting=" + styleUniqueId);
			if (((LocalIMDB)index.getImdb()).getUniqueIdTable().getUniqueIdFromIdentifier(styleId2) != styleUniqueId2)
				fail("uniqueid table incorrect");
			if (!((LocalIMDB)index.getImdb()).getUniqueIdTable().getIdentifierFromUniqueId(styleUniqueId).equals(styleId))
				fail("uniqueid table incorrect");
			if (!((LocalIMDB)index.getImdb()).getUniqueIdTable().getIdentifierFromUniqueId(styleUniqueId2).equals(styleId2))
				fail("uniqueid table incorrect");
			
			XMLSerializer.saveData(index, "data/junit/temp/style_index.xml");
			index = (StyleIndex)XMLSerializer.readData("data/junit/temp/style_index.xml");
			
			if (((LocalIMDB)index.getImdb()).getUniqueIdTable().getUniqueIdFromIdentifier(styleId) != styleUniqueId)
				fail("uniqueid table incorrect, is=" + ((LocalIMDB)index.getImdb()).getUniqueIdTable().getUniqueIdFromIdentifier(styleId) + ", expecting=" + styleUniqueId);
			if (((LocalIMDB)index.getImdb()).getUniqueIdTable().getUniqueIdFromIdentifier(styleId2) != styleUniqueId2)
				fail("uniqueid table incorrect");
			if (!((LocalIMDB)index.getImdb()).getUniqueIdTable().getIdentifierFromUniqueId(styleUniqueId).equals(styleId))
				fail("uniqueid table incorrect");
			if (!((LocalIMDB)index.getImdb()).getUniqueIdTable().getIdentifierFromUniqueId(styleUniqueId2).equals(styleId2))
				fail("uniqueid table incorrect");

			if (!((LocalIMDB)index.getImdb()).getRecordMap().get(styleUniqueId).equals(styleRecord))
				fail("incorrect record map");
			if (!((LocalIMDB)index.getImdb()).getRecordMap().get(styleUniqueId2).equals(styleRecord2))
				fail("incorrect record map");

			if (!((LocalIMDB)index.getImdb()).getDuplicateIdMap().get(styleUniqueId2).equals(styleRecord))
				fail("incorrect duplicate record map");
			
			if (index.getModelManager().getNumColumns() != columnCount)
				fail("incorrect model manager column count");
			
			LineWriter writer = LineWriterFactory.getLineWriter("data/junit/temp/styleindex.jso");			
			index.write(writer);
			writer.close();
			LineReader lineReader = LineReaderFactory.getLineReader("data/junit/temp/styleindex.jso");
			index = new StyleIndex(lineReader);
			lineReader.close();
			
			if (((LocalIMDB)index.getImdb()).getUniqueIdTable().getUniqueIdFromIdentifier(styleId) != styleUniqueId)
				fail("uniqueid table incorrect, is=" + ((LocalIMDB)index.getImdb()).getUniqueIdTable().getUniqueIdFromIdentifier(styleId) + ", expecting=" + styleUniqueId);
			if (((LocalIMDB)index.getImdb()).getUniqueIdTable().getUniqueIdFromIdentifier(styleId2) != styleUniqueId2)
				fail("uniqueid table incorrect");
			if (!((LocalIMDB)index.getImdb()).getUniqueIdTable().getIdentifierFromUniqueId(styleUniqueId).equals(styleId))
				fail("uniqueid table incorrect");
			if (!((LocalIMDB)index.getImdb()).getUniqueIdTable().getIdentifierFromUniqueId(styleUniqueId2).equals(styleId2))
				fail("uniqueid table incorrect");

			if (!((LocalIMDB)index.getImdb()).getRecordMap().get(styleUniqueId).equals(styleRecord))
				fail("incorrect record map");
			if (!((LocalIMDB)index.getImdb()).getRecordMap().get(styleUniqueId2).equals(styleRecord2))
				fail("incorrect record map");

			if (((LocalIMDB)index.getImdb()).getDuplicateIdMap().size() != 1)
				fail("duplicate map not the right size, is=" + ((LocalIMDB)index.getImdb()).getDuplicateIdMap().size());
			Record record = ((LocalIMDB)index.getImdb()).getDuplicateIdMap().get(styleUniqueId2);
			if (record == null)
				fail("couldn't lookup by duplicate entry");
			if (!record.equals(styleRecord))
				fail("incorrect duplicate record map");
			
			if (index.getModelManager().getNumColumns() != columnCount)
				fail("incorrect model manager column count");
			
		} catch (Exception e) {
			log.error("testSerialization(): error", e);
			fail(e.getMessage());
		}
	}
	
}
