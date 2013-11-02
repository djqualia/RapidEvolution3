package test.com.mixshare.rapid_evolution.data.index;

import org.apache.log4j.Logger;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.filter.style.StyleIdentifier;
import com.mixshare.rapid_evolution.data.profile.filter.style.StyleProfile;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.data.submitted.filter.style.SubmittedStyle;
import com.mixshare.rapid_evolution.data.submitted.search.song.SubmittedSong;

public class HierarchicalIndexTest extends RE3TestCase {

    static private Logger log = Logger.getLogger(HierarchicalIndexTest.class);    
    
    ///////////
    // TESTS //
    ///////////
	
	public void testStyleMembership() {
		try {
			// create style 1
			SubmittedStyle style1 = new SubmittedStyle("breaks");			
			StyleProfile styleProfile1 = Database.getStyleIndex().addStyle(style1);			
			if (styleProfile1 == null)
				fail("style not created");			
			
			// song 1
			SubmittedSong submittedSong1 = new SubmittedSong("aphex twin", "selected ambient works", "d1", "heliosphere", "");
			submittedSong1.setLabelName("rephlex");
			submittedSong1.addStyleDegreeValue("funky breaks", 1.0f, DATA_SOURCE_UNKNOWN);
			
			// add the songs then get the created style profiles
			Database.add(submittedSong1);
			
			StyleProfile breaks = Database.getStyleIndex().getStyleProfile(new StyleIdentifier("breaks"));
			StyleProfile funkyBreaks = Database.getStyleIndex().getStyleProfile(new StyleIdentifier("funky breaks"));
						
			SongRecord song1 = Database.getSongIndex().getSongRecord(submittedSong1.getSongIdentifier());
			
			if (breaks.matches(song1))
				fail("invalid style membership");
			if (!funkyBreaks.matches(song1))
				fail("invalid style membership");
			
			if (breaks.getSongRecords().size() != 0)
				fail("incorrect # of songs in style");
			if (funkyBreaks.getSongRecords().size() != 1)
				fail("incorrect # of songs in style");
			
			Database.getStyleIndex().addRelationship(breaks.getHierarchicalRecord(), funkyBreaks.getHierarchicalRecord());

			if (!breaks.matches(song1))
				fail("invalid style membership");
			if (!funkyBreaks.matches(song1))
				fail("invalid style membership");

			if (breaks.getSongRecords().size() != 1)
				fail("incorrect # of songs in style");
			if (funkyBreaks.getSongRecords().size() != 1)
				fail("incorrect # of songs in style");
			
			Database.getStyleIndex().removeRelationship(breaks.getHierarchicalRecord(), funkyBreaks.getHierarchicalRecord());

			if (breaks.matches(song1))
				fail("invalid style membership");
			if (!funkyBreaks.matches(song1))
				fail("invalid style membership");
			
			if (breaks.getSongRecords().size() != 0)
				fail("incorrect # of songs in style");
			if (funkyBreaks.getSongRecords().size() != 1)
				fail("incorrect # of songs in style");
			
		} catch (Exception e) {
			log.error("testHierarchicalStyleMembership(): error", e);
			fail(e.getMessage());
		}
	}

	public void testMergedStyleMembership() {
		try {
			// create style 1
			SubmittedStyle style1 = new SubmittedStyle("breaks");			
			StyleProfile styleProfile1 = Database.getStyleIndex().addStyle(style1);			
			if (styleProfile1 == null)
				fail("style not created");			
			
			// create style 3
			SubmittedStyle style2 = new SubmittedStyle("breakbeat");			
			StyleProfile styleProfile2 = Database.getStyleIndex().addStyle(style2);			
			if (styleProfile2 == null)
				fail("style not created");				
			
			// song 1
			SubmittedSong submittedSong1 = new SubmittedSong("aphex twin", "selected ambient works", "d1", "heliosphere", "");
			submittedSong1.addStyleDegreeValue("funky breaks", 1.0f, DATA_SOURCE_UNKNOWN);
			
			// song 2
			SubmittedSong submittedSong2 = new SubmittedSong("aphex twin", "selected ambient works", "b2", "tha", "");
			submittedSong2.addStyleDegreeValue("ambient breakbeat", 1.0f, DATA_SOURCE_UNKNOWN);
						
			// add the songs then get the created style profiles
			Database.add(submittedSong1);
			Database.add(submittedSong2);
			
			StyleProfile breaks = Database.getStyleIndex().getStyleProfile(new StyleIdentifier("breaks"));
			StyleProfile funkyBreaks = Database.getStyleIndex().getStyleProfile(new StyleIdentifier("funky breaks"));
			StyleProfile breakbeat = Database.getStyleIndex().getStyleProfile(new StyleIdentifier("breakbeat"));
			StyleProfile ambientBreakbeat = Database.getStyleIndex().getStyleProfile(new StyleIdentifier("ambient breakbeat"));
						
			SongRecord song1 = Database.getSongIndex().getSongRecord(submittedSong1.getSongIdentifier());
			SongRecord song2 = Database.getSongIndex().getSongRecord(submittedSong2.getSongIdentifier());
			
			// check initial style memberships and state of db
			
			if (breaks.matches(song1))
				fail("invalid style membership");
			if (!funkyBreaks.matches(song1))
				fail("invalid style membership");
			if (breakbeat.matches(song2))
				fail("invalid style membership");
			if (!ambientBreakbeat.matches(song2))
				fail("invalid style membership");
			
			if (breaks.getSongRecords().size() != 0)
				fail("incorrect # of songs in style");
			if (funkyBreaks.getSongRecords().size() != 1)
				fail("incorrect # of songs in style");
			if (breakbeat.getSongRecords().size() != 0)
				fail("incorrect # of songs in style");
			if (ambientBreakbeat.getSongRecords().size() != 1)
				fail("incorrect # of songs in style");
			
			// setup parent/child relationships and check the db
			
			Database.getStyleIndex().addRelationship(breaks.getHierarchicalRecord(), funkyBreaks.getHierarchicalRecord());
			Database.getStyleIndex().addRelationship(breakbeat.getHierarchicalRecord(), ambientBreakbeat.getHierarchicalRecord());

			if (!breaks.matches(song1))
				fail("invalid style membership");
			if (!funkyBreaks.matches(song1))
				fail("invalid style membership");
			if (!breakbeat.matches(song2))
				fail("invalid style membership");
			if (!ambientBreakbeat.matches(song2))
				fail("invalid style membership");

			if (breaks.getSongRecords().size() != 1)
				fail("incorrect # of songs in style");
			if (funkyBreaks.getSongRecords().size() != 1)
				fail("incorrect # of songs in style");
			if (breakbeat.getSongRecords().size() != 1)
				fail("incorrect # of songs in style");
			if (ambientBreakbeat.getSongRecords().size() != 1)
				fail("incorrect # of songs in style");
			
			// merge the parent styles, and check the relationships
			Database.mergeProfiles(breaks, breakbeat);
			
			funkyBreaks = Database.getStyleIndex().getStyleProfile(new StyleIdentifier("funky breaks"));
			ambientBreakbeat = Database.getStyleIndex().getStyleProfile(new StyleIdentifier("ambient breakbeat"));
			if (funkyBreaks == null)
				fail("child was deleted");
			if (ambientBreakbeat == null)
				fail("child was deleted");
			
			if (!breaks.matches(song1))
				fail("invalid style membership");
			if (!funkyBreaks.matches(song1))
				fail("invalid style membership");
			if (funkyBreaks.matches(song2))
				fail("invalid style membership");
			
			if (!breaks.matches(song2))
				fail("invalid style membership");
			if (!ambientBreakbeat.matches(song2))
				fail("invalid style membership");
			if (ambientBreakbeat.matches(song1))
				fail("invalid style membership");
			
			if (breaks.getSongRecords().size() != 2)
				fail("incorrect # of songs in style, is=" + breaks.getSongRecords().size() + ", set=" + breaks.getSongRecords());
			//if (breakbeat.getSongRecords().size() != 0)
				//fail("incorrect # of songs in style, is=" + breakbeat.getSongRecords().size());
			if (ambientBreakbeat.getSongRecords().size() != 1)
				fail("incorrect # of songs in style");	
									
		} catch (Exception e) {
			log.error("testHierarchicalStyleMembership(): error", e);
			fail(e.getMessage());
		}
	}
	
}
