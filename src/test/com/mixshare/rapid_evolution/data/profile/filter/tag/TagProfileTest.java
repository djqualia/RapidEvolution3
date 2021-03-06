package test.com.mixshare.rapid_evolution.data.profile.filter.tag;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.data.identifier.filter.tag.TagIdentifier;
import com.mixshare.rapid_evolution.data.profile.filter.tag.TagProfile;
import com.mixshare.rapid_evolution.data.record.filter.tag.TagRecord;
import com.mixshare.rapid_evolution.data.util.io.XMLSerializer;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineReaderFactory;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.mixshare.rapid_evolution.util.io.LineWriterFactory;

public class TagProfileTest extends RE3TestCase {

	public void testSerialization() {
		try {		
			TagProfile tagProfile = new TagProfile();
			
			TagRecord tag = new TagRecord();			
			tagProfile.setRecord(tag);
			
			tagProfile.setRatingSource((byte)1);
			
			tag.setId(new TagIdentifier("test tag"));			
			tag.setUniqueId(420);
			
			int[] dupIds = new int[] { 1, 2 };
			tag.setDuplicateIds(dupIds);
			
			tag.setRating((byte)80);
			tag.setDisabled(true);
			tag.setLastModified(234);
						
			tag.setNumArtistRecordsCached(1);
			tag.setNumLabelRecordsCached(2);
			tag.setNumReleaseRecordsCached(3);
			tag.setNumSongRecordsCached(4);
			tag.setNumExternalArtistRecordsCached(5);
			tag.setNumExternalLabelRecordsCached(6);
			tag.setNumExternalReleaseRecordsCached(7);
			tag.setNumExternalSongRecordsCached(8);
			
			tag.setRoot(true);
			
			tag.setParentIds(new int[] { 6 });
			tag.setChildIds(new int[] { 7 });
			
			XMLSerializer.saveData(tagProfile, "data/junit/temp/tag_profile.xml");
			tagProfile = (TagProfile)XMLSerializer.readData("data/junit/temp/tag_profile.xml");
			tag = tagProfile.getTagRecord();			
			
			checkValues(tagProfile, tag);
			
			LineWriter writer = LineWriterFactory.getLineWriter("data/junit/temp/tag_profile.xml");
			tagProfile.write(writer);
			writer.close();
			LineReader lineReader = LineReaderFactory.getLineReader("data/junit/temp/tag_profile.xml");
			tagProfile = new TagProfile(lineReader);
			tag = tagProfile.getTagRecord();	
			
			checkValues(tagProfile, tag);

			
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	private void checkValues(TagProfile tagProfile, TagRecord tag) {
		if (tagProfile.getRatingSource() != (byte)1)
			fail();
		
		if (!tag.getId().equals(new TagIdentifier("test tag")))
			fail("id incorrect");			
		if (tag.getUniqueId() != 420)
			fail("unique id incorrect");

		if (tag.getDuplicateIds()[0] != 1)
			fail("dup id incorrect");
		if (tag.getDuplicateIds()[1] != 2)
			fail("dup id incorrect");
		
		if (tag.getRating() != (byte)80)
			fail("rating incorrect");
		if (!tag.isDisabled())
			fail("not disabled");
		if (tag.getLastModified() != 234)
			fail("last modified incorrect");
					
//		if (tag.getNumArtistRecords() != 1)
//			fail("incorrect # artist records");
//		if (tag.getNumLabelRecords() != 2)
//			fail("incorrect # label records");
//		if (tag.getNumReleaseRecords() != 3)
//			fail("incorrect # release records");
//		if (tag.getNumSongRecords() != 4)
//			fail("incorrect # song records");
//		if (tag.getNumExternalArtistRecords() != 5)
//			fail("incorrect # external artist records");
//		if (tag.getNumExternalLabelRecords() != 6)
//			fail("incorrect # external label records");
//		if (tag.getNumExternalReleaseRecords() != 7)
//			fail("incorrect # external  records");
//		if (tag.getNumExternalSongRecords() != 8)
//			fail("incorrect # external  records");
		
		if (!tag.isRoot())
			fail("root didn't persist");
		
		if (tag.getParentIds()[0] != 6)
			fail("parent incorrect");
		if (tag.getChildIds()[0] != 7)
			fail("child incorrect");		
	}
	
}
