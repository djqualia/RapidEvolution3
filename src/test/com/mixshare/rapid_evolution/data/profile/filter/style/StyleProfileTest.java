package test.com.mixshare.rapid_evolution.data.profile.filter.style;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.data.identifier.filter.style.StyleIdentifier;
import com.mixshare.rapid_evolution.data.profile.filter.style.StyleProfile;
import com.mixshare.rapid_evolution.data.record.filter.style.StyleRecord;
import com.mixshare.rapid_evolution.data.util.io.XMLSerializer;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineReaderFactory;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.mixshare.rapid_evolution.util.io.LineWriterFactory;

public class StyleProfileTest extends RE3TestCase {

	public void testSerialization() {
		try {		
			StyleProfile styleProfile = new StyleProfile();
			
			StyleRecord style = new StyleRecord();			
			styleProfile.setRecord(style);			
			
			styleProfile.setDescription("blah");
			styleProfile.setRatingSource((byte)1);
			
			style.setId(new StyleIdentifier("test style"));			
			style.setUniqueId(420);
			
			int[] dupIds = new int[] { 1, 2 };
			style.setDuplicateIds(dupIds);
			
			style.setRating((byte)80);
			style.setDisabled(true);
			style.setLastModified(234);
						
			style.setNumArtistRecordsCached(1);
			style.setNumLabelRecordsCached(2);
			style.setNumReleaseRecordsCached(3);
			style.setNumSongRecordsCached(4);
			style.setNumExternalArtistRecordsCached(5);
			style.setNumExternalLabelRecordsCached(6);
			style.setNumExternalReleaseRecordsCached(7);
			style.setNumExternalSongRecordsCached(8);
			
			style.setRoot(true);
						
			style.setParentIds(new int[] { 6 });
			style.setChildIds(new int[] { 7 });
			
			XMLSerializer.saveData(styleProfile, "data/junit/temp/style_profile.xml");
			styleProfile = (StyleProfile)XMLSerializer.readData("data/junit/temp/style_profile.xml");
			style = styleProfile.getStyleRecord();			
			
			checkValues(styleProfile, style);
			
			LineWriter writer = LineWriterFactory.getLineWriter("data/junit/temp/style_profile.xml");
			styleProfile.write(writer);
			writer.close();
			LineReader lineReader = LineReaderFactory.getLineReader("data/junit/temp/style_profile.xml");
			styleProfile = new StyleProfile(lineReader);
			style = styleProfile.getStyleRecord();	
			
			checkValues(styleProfile, style);
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	private void checkValues(StyleProfile styleProfile, StyleRecord style) {
		if (!styleProfile.getDescription().equals("blah"))
			fail();
		if (styleProfile.getRatingSource() != (byte)1)
			fail();
		
		if (!style.getId().equals(new StyleIdentifier("test style")))
			fail("id incorrect");			
		if (style.getUniqueId() != 420)
			fail("unique id incorrect");

		if (style.getDuplicateIds()[0] != 1)
			fail("dup id incorrect");
		if (style.getDuplicateIds()[1] != 2)
			fail("dup id incorrect");
		
		if (style.getRating() != (byte)80)
			fail("rating incorrect");
		if (!style.isDisabled())
			fail("not disabled");
		if (style.getLastModified() != 234)
			fail("last modified incorrect");
					
//		if (style.getNumArtistRecords() != 1)
//			fail("incorrect # artist records");
//		if (style.getNumLabelRecords() != 2)
//			fail("incorrect # label records");
//		if (style.getNumReleaseRecords() != 3)
//			fail("incorrect # release records");
//		if (style.getNumSongRecords() != 4)
//			fail("incorrect # song records");
//		if (style.getNumExternalArtistRecords() != 5)
//			fail("incorrect # external artist records");
//		if (style.getNumExternalLabelRecords() != 6)
//			fail("incorrect # external label records");
//		if (style.getNumExternalReleaseRecords() != 7)
//			fail("incorrect # external  records");
//		if (style.getNumExternalSongRecords() != 8)
//			fail("incorrect # external  records");
		
		if (!style.isRoot())
			fail("root didn't persist");
		
		if (style.getParentIds()[0] != 6)
			fail("parent incorrect");
		if (style.getChildIds()[0] != 7)
			fail("child incorrect");		
	}
	
}
