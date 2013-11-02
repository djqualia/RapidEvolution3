package test.com.mixshare.rapid_evolution.data.search;

import java.util.Vector;

import org.apache.log4j.Logger;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.search.SearchParser;
import com.mixshare.rapid_evolution.data.search.SearchResult;
import com.mixshare.rapid_evolution.data.search.parameters.search.song.SongSearchParameters;
import com.mixshare.rapid_evolution.data.submitted.search.song.SubmittedSong;

public class SearchParserTest extends RE3TestCase {
	
    static private Logger log = Logger.getLogger(SearchParserTest.class);    
	
	public void testSearchParserTranslation() {
		if (!(new SearchParser("free the robots").getLuceneText().equals("(*free* AND *robots*)")))
			fail("incorrect lucene text, is=" + new SearchParser("free the robots").getLuceneText());		
		if (!(new SearchParser("lockhead").getLuceneText().equals("(*lockhead*)")))
			fail("incorrect lucene text");		
		if (!(new SearchParser("blo?khead").getLuceneText().equals("(*blo?khead*)")))
			fail("incorrect lucene text");		
		if (!(new SearchParser("aphex twin").getLuceneText().equals("(*aphex* AND *twin*)")))
			fail("incorrect lucene text");		
		if (!(new SearchParser("\"aphex twin\"").getLuceneText().equals("(\"aphex twin\")")))
			fail("incorrect lucene text");		
		if (!(new SearchParser("mr. scruff").getLuceneText().equals("(*mr* AND *scruff*)")))
			fail("incorrect lucene text");		
		if (!(new SearchParser("an ten nae").getLuceneText().equals("(*ten* AND *nae*)")))
			fail("incorrect lucene text, is=" + new SearchParser("an ten nae").getLuceneText());		
		if (!(new SearchParser("an-ten-nae").getLuceneText().equals("(*ten* AND *nae*)")))
			fail("incorrect lucene text, is=" + new SearchParser("an-ten-nae").getLuceneText());		
		if (!(new SearchParser("an-ten-nae | dubstep").getLuceneText().equals("(*ten* AND *nae*) OR (*dubstep*)")))
			fail("incorrect lucene text");		
		if (!(new SearchParser("tag:dubstep").getLuceneText().equals("(tag:*dubstep*)")))
			fail("incorrect lucene text, is=" + new SearchParser("tag:dubstep").getLuceneText());	
		if (!(new SearchParser("tag:\"hip hop\"").getLuceneText().equals("(tag:\"hip hop\")")))
			fail("incorrect lucene text, is=" + new SearchParser("tag:\"hip hop\"").getLuceneText());		
		if (!(new SearchParser("-dubstep").getLuceneText().equals("(-*dubstep*)")))
			fail("incorrect lucene text");		
		if (!(new SearchParser("blockhead label:\"ninja tune\"").getLuceneText().equals("(*blockhead* AND label:\"ninja tune\")")))
			fail("incorrect lucene text, is=" + new SearchParser("blockhead label:\"ninja tune\"").getLuceneText());		
	}
	
	public void testSearchingSongs() {
		try {
			// song 1
			SubmittedSong submittedSong1 = new SubmittedSong("blockhead", "", "", "carnivores unite", "");
			submittedSong1.setLabelName("ninja tune");
			submittedSong1.addStyleDegreeValue("Downtempo", 1.0f, DATA_SOURCE_UNKNOWN);

			// song 2
			SubmittedSong submittedSong2 = new SubmittedSong("mr. scruff", "", "", "get a move on", "");
			submittedSong2.setLabelName("ninja tune");
			submittedSong2.addStyleDegreeValue("nu jazz", 1.0f, DATA_SOURCE_UNKNOWN);

			// song 3
			SubmittedSong submittedSong3 = new SubmittedSong("free the robots", "", "", "dear diary", "");
			submittedSong3.setLabelName("ghostly");
			submittedSong3.addTagDegreeValue("jazzy", 1.0f, DATA_SOURCE_UNKNOWN);
			
			// song 4
			SubmittedSong submittedSong4 = new SubmittedSong("an-ten-nae", "", "", "wub wub wub", "");
			submittedSong4.setLabelName("muti music");
			submittedSong4.addTagDegreeValue("dubstep", 1.0f, DATA_SOURCE_UNKNOWN);

			// song 5
			SubmittedSong submittedSong5 = new SubmittedSong("1.8.7.", "", "", "j-ngle", "");
			submittedSong5.setLabelName("jungle sky");
			submittedSong5.addTagDegreeValue("jungle", 1.0f, DATA_SOURCE_UNKNOWN);

			// song 6
			SubmittedSong submittedSong6 = new SubmittedSong("putsch '79", "", "", "song", "");

			// song 7
			SubmittedSong submittedSong7 = new SubmittedSong("maps+diagrams", "", "", "test", "");

			// song 8
			SubmittedSong submittedSong8 = new SubmittedSong("up, bustle & out", "", "", "test2", "");
			
			// add the songs
			SongProfile profile1 = Database.getSongIndex().addSong(submittedSong1);
			SongProfile profile2 = Database.getSongIndex().addSong(submittedSong2);
			SongProfile profile3 = Database.getSongIndex().addSong(submittedSong3);
			SongProfile profile4 = Database.getSongIndex().addSong(submittedSong4);
			SongProfile profile5 = Database.getSongIndex().addSong(submittedSong5);
			SongProfile profile6 = Database.getSongIndex().addSong(submittedSong6);
			SongProfile profile7 = Database.getSongIndex().addSong(submittedSong7);
			SongProfile profile8 = Database.getSongIndex().addSong(submittedSong8);
			
			SongSearchParameters songParams = new SongSearchParameters();
			
			songParams.setSearchText("blockhead");
			Vector<SearchResult> result = Database.getSongIndex().searchRecords(songParams);
			if (result.size() != 1)
				fail("incorrect results");
			if (!result.get(0).getRecord().equals(profile1.getRecord()))
				fail("incorrect results");
			
			songParams.setSearchText("lockhea");
			result = Database.getSongIndex().searchRecords(songParams);
			if (result.size() != 1)
				fail("incorrect results");
			if (!result.get(0).getRecord().equals(profile1.getRecord()))
				fail("incorrect results");
				
			songParams.setSearchText("\"mr. scruff\"");
			result = Database.getSongIndex().searchRecords(songParams);
			if (result.size() != 1)
				fail("incorrect results");
			if (!result.get(0).getRecord().equals(profile2.getRecord()))
				fail("incorrect results");

			songParams.setSearchText("\"mr scruff\"");
			result = Database.getSongIndex().searchRecords(songParams);
			if (result.size() != 1)
				fail("incorrect results");
			if (!result.get(0).getRecord().equals(profile2.getRecord()))
				fail("incorrect results");
			
			songParams.setSearchText("mr. scruff");
			result = Database.getSongIndex().searchRecords(songParams);
			if (result.size() != 1)
				fail("incorrect results, is=" + result.size());
			if (!result.get(0).getRecord().equals(profile2.getRecord()))
				fail("incorrect results");

			songParams.setSearchText("mr scruff");
			result = Database.getSongIndex().searchRecords(songParams);
			if (result.size() != 1)
				fail("incorrect results");
			if (!result.get(0).getRecord().equals(profile2.getRecord()))
				fail("incorrect results");
			
			songParams.setSearchText("an ten nae");
			result = Database.getSongIndex().searchRecords(songParams);
			if (result.size() != 1)
				fail("incorrect results");
			if (!result.get(0).getRecord().equals(profile4.getRecord()))
				fail("incorrect results");			
			
			songParams.setSearchText("an-ten-nae");
			result = Database.getSongIndex().searchRecords(songParams);
			if (result.size() != 1)
				fail("incorrect results, is=" + result.size());
			if (!result.get(0).getRecord().equals(profile4.getRecord()))
				fail("incorrect results");			

			songParams.setSearchText("\"an-ten-nae\"");
			result = Database.getSongIndex().searchRecords(songParams);
			if (result.size() != 1)
				fail("incorrect results");
			if (!result.get(0).getRecord().equals(profile4.getRecord()))
				fail("incorrect results");			

			songParams.setSearchText("\"an ten nae\"");
			result = Database.getSongIndex().searchRecords(songParams);
			if (result.size() != 1)
				fail("incorrect results");
			if (!result.get(0).getRecord().equals(profile4.getRecord()))
				fail("incorrect results");		
			
			songParams.setSearchText("free the robots");
			result = Database.getSongIndex().searchRecords(songParams);
			if (result.size() != 1)
				fail("incorrect results, is=" + result.size());
			if (!result.get(0).getRecord().equals(profile3.getRecord()))
				fail("incorrect results");			

			songParams.setSearchText("\"free the robots\"");
			result = Database.getSongIndex().searchRecords(songParams);
			if (result.size() != 1)
				fail("incorrect results, is=" + result.size());
			if (!result.get(0).getRecord().equals(profile3.getRecord()))
				fail("incorrect results");			
			
			songParams.setSearchText("free robots");
			result = Database.getSongIndex().searchRecords(songParams);
			if (result.size() != 1)
				fail("incorrect results, is=" + result.size());
			if (!result.get(0).getRecord().equals(profile3.getRecord()))
				fail("incorrect results");					

			songParams.setSearchText("1.8.7.");
			result = Database.getSongIndex().searchRecords(songParams);
			if (result.size() != 1)
				fail("incorrect results, is=" + result.size());
			if (!result.get(0).getRecord().equals(profile5.getRecord()))
				fail("incorrect results");		

			songParams.setSearchText("\"1.8.7.\"");
			result = Database.getSongIndex().searchRecords(songParams);
			if (result.size() != 1)
				fail("incorrect results, is=" + result.size());
			if (!result.get(0).getRecord().equals(profile5.getRecord()))
				fail("incorrect results");		
			
			songParams.setSearchText("\"1.8.7\"");
			result = Database.getSongIndex().searchRecords(songParams);
			if (result.size() != 1)
				fail("incorrect results, is=" + result.size());
			if (!result.get(0).getRecord().equals(profile5.getRecord()))
				fail("incorrect results");	
			
			songParams.setSearchText("1.8.7");
			result = Database.getSongIndex().searchRecords(songParams);
			if (result.size() != 1)
				fail("incorrect results, is=" + result.size());
			if (!result.get(0).getRecord().equals(profile5.getRecord()))
				fail("incorrect results");	
			
			songParams.setSearchText("1 8 7");
			result = Database.getSongIndex().searchRecords(songParams);
			if (result.size() != 1)
				fail("incorrect results, is=" + result.size());
			if (!result.get(0).getRecord().equals(profile5.getRecord()))
				fail("incorrect results");	

			songParams.setSearchText("\"putsch '79\"");
			result = Database.getSongIndex().searchRecords(songParams);
			if (result.size() != 1)
				fail("incorrect results, is=" + result.size());
			if (!result.get(0).getRecord().equals(profile6.getRecord()))
				fail("incorrect results");	

			songParams.setSearchText("putsch '79");
			result = Database.getSongIndex().searchRecords(songParams);
			if (result.size() != 1)
				fail("incorrect results, is=" + result.size());
			if (!result.get(0).getRecord().equals(profile6.getRecord()))
				fail("incorrect results");	

			songParams.setSearchText("\"putsch 79\"");
			result = Database.getSongIndex().searchRecords(songParams);
			if (result.size() != 1)
				fail("incorrect results, is=" + result.size());
			if (!result.get(0).getRecord().equals(profile6.getRecord()))
				fail("incorrect results");	

			songParams.setSearchText("putsch 79");
			result = Database.getSongIndex().searchRecords(songParams);
			if (result.size() != 1)
				fail("incorrect results, is=" + result.size());
			if (!result.get(0).getRecord().equals(profile6.getRecord()))
				fail("incorrect results");	

			songParams.setSearchText("maps+diagrams");
			result = Database.getSongIndex().searchRecords(songParams);
			if (result.size() != 1)
				fail("incorrect results, is=" + result.size());
			if (!result.get(0).getRecord().equals(profile7.getRecord()))
				fail("incorrect results");	

			songParams.setSearchText("\"maps+diagrams\"");
			result = Database.getSongIndex().searchRecords(songParams);
			if (result.size() != 1)
				fail("incorrect results, is=" + result.size());
			if (!result.get(0).getRecord().equals(profile7.getRecord()))
				fail("incorrect results");	

			songParams.setSearchText("maps diagrams");
			result = Database.getSongIndex().searchRecords(songParams);
			if (result.size() != 1)
				fail("incorrect results, is=" + result.size());
			if (!result.get(0).getRecord().equals(profile7.getRecord()))
				fail("incorrect results");	

			songParams.setSearchText("\"maps diagrams\"");
			result = Database.getSongIndex().searchRecords(songParams);
			if (result.size() != 1)
				fail("incorrect results, is=" + result.size());
			if (!result.get(0).getRecord().equals(profile7.getRecord()))
				fail("incorrect results");	

			songParams.setSearchText("up, bustle & out");
			result = Database.getSongIndex().searchRecords(songParams);
			if (result.size() != 1)
				fail("incorrect results, is=" + result.size());
			if (!result.get(0).getRecord().equals(profile8.getRecord()))
				fail("incorrect results");
			
			songParams.setSearchText("\"up, bustle & out\"");
			result = Database.getSongIndex().searchRecords(songParams);
			if (result.size() != 1)
				fail("incorrect results, is=" + result.size());
			if (!result.get(0).getRecord().equals(profile8.getRecord()))
				fail("incorrect results");			
			
			songParams.setSearchText("up bustle out");
			result = Database.getSongIndex().searchRecords(songParams);
			if (result.size() != 1)
				fail("incorrect results, is=" + result.size());
			if (!result.get(0).getRecord().equals(profile8.getRecord()))
				fail("incorrect results");
			
			songParams.setSearchText("up bustle out");
			result = Database.getSongIndex().searchRecords(songParams);
			if (result.size() != 1)
				fail("incorrect results, is=" + result.size());
			if (!result.get(0).getRecord().equals(profile8.getRecord()))
				fail("incorrect results");		
			
			songParams.setSearchText("scruff | blockhead");
			result = Database.getSongIndex().searchRecords(songParams);
			if (result.size() != 2)
				fail("incorrect results, is=" + result.size());

			songParams.setSearchText("scruff or blockhead");
			result = Database.getSongIndex().searchRecords(songParams);
			if (result.size() != 2)
				fail("incorrect results, is=" + result.size());

			songParams.setSearchText("scruff OR blockhead");
			result = Database.getSongIndex().searchRecords(songParams);
			if (result.size() != 2)
				fail("incorrect results, is=" + result.size());
			
			songParams.setSearchText("scruff & blockhead");
			result = Database.getSongIndex().searchRecords(songParams);
			if (result.size() != 2)
				fail("incorrect results, is=" + result.size());

			songParams.setSearchText("scruff || blockhead");
			result = Database.getSongIndex().searchRecords(songParams);
			if (result.size() != 2)
				fail("incorrect results, is=" + result.size());

			songParams.setSearchText("scruff && blockhead");
			result = Database.getSongIndex().searchRecords(songParams);
			if (result.size() != 2)
				fail("incorrect results, is=" + result.size());

			songParams.setSearchText("label:ninja -scruff");
			result = Database.getSongIndex().searchRecords(songParams);
			if (result.size() != 1)
				fail("incorrect results");
			if (!result.get(0).getRecord().equals(profile1.getRecord()))
				fail("incorrect results");

			songParams.setSearchText("bl?ckhead");
			result = Database.getSongIndex().searchRecords(songParams);
			if (result.size() != 1)
				fail("incorrect results");
			if (!result.get(0).getRecord().equals(profile1.getRecord()))
				fail("incorrect results");
			
			songParams.setSearchText("brockhead~");
			result = Database.getSongIndex().searchRecords(songParams);
			if (result.size() != 1)
				fail("incorrect results");
			if (!result.get(0).getRecord().equals(profile1.getRecord()))
				fail("incorrect results");
			
			// field related searches
			
			songParams.setSearchText("label:\"ninja tune\"");
			result = Database.getSongIndex().searchRecords(songParams);
			if (result.size() != 2)
				fail("incorrect results, is=" + result.size());
			
			songParams.setSearchText("label:ninja");
			result = Database.getSongIndex().searchRecords(songParams);
			if (result.size() != 2)
				fail("incorrect results, is=" + result.size());
						
			songParams.setSearchText("blockhead label:ninja");
			result = Database.getSongIndex().searchRecords(songParams);
			if (result.size() != 1)
				fail("incorrect results, is=" + result.size());
			
			songParams.setSearchText("blockhead label:\"ninja tune\"");
			result = Database.getSongIndex().searchRecords(songParams);
			if (result.size() != 1)
				fail("incorrect results, is=" + result.size());
			
		} catch (Exception e) {
			log.error("testSearchingSongs(): error", e);
			fail(e.getMessage());
		}
	}			

}
