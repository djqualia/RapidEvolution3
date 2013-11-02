package test.com.mixshare.rapid_evolution.data.record.filter.style;

import java.util.Vector;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.filter.style.StyleIdentifier;
import com.mixshare.rapid_evolution.data.profile.search.artist.ArtistProfile;
import com.mixshare.rapid_evolution.data.record.filter.style.StyleRecord;
import com.mixshare.rapid_evolution.data.record.search.artist.ArtistRecord;
import com.mixshare.rapid_evolution.data.submitted.search.artist.SubmittedArtist;
import com.mixshare.rapid_evolution.data.util.io.XMLSerializer;

public class StyleRecordTest extends RE3TestCase {

	public void testSerialization() {
		try {		
			StyleRecord style = new StyleRecord();
			
			style.setId(new StyleIdentifier("test style"));			
			
			int[] dupIds = new int[] { 1, 2 };
			style.setDuplicateIds(dupIds);
			
			style.setRating((byte)80);
			style.setDisabled(true);
			style.setLastModified(234);
			
			style.setCategoryOnly(true);
			
			SubmittedArtist artist = new SubmittedArtist("test");
			artist.addStyleDegreeValue("test style", 1.0f, DataConstants.DATA_SOURCE_USER);
			Database.add(artist); // This indirectly creates "test style" in the DB, and the getArtist count below relies on this association
			
			int uniqueId = Database.getStyleIndex().getStyleRecord(new StyleIdentifier("test style")).getUniqueId();
			style.setUniqueId(uniqueId);

//			Vector<ArtistRecord> artists = new Vector<ArtistRecord>();
//			artists.add(profile.getArtistRecord());
//			style.addArtistRecords(artists);
			
			style.setNumArtistRecordsCached(1);
			style.setNumLabelRecordsCached(2);
			style.setNumReleaseRecordsCached(3);
			style.setNumSongRecordsCached(4);
			style.setNumExternalArtistRecordsCached(5);
			style.setNumExternalLabelRecordsCached(6);
			style.setNumExternalReleaseRecordsCached(7);
			style.setNumExternalSongRecordsCached(8);
			
			style.setRoot(true);
			
			StyleRecord testParent = new StyleRecord();			
			testParent.setId(new StyleIdentifier("test parent"));
			testParent.setUniqueId(123);
			
			StyleRecord testChild = new StyleRecord();			
			testChild.setId(new StyleIdentifier("test child"));
			testChild.setUniqueId(321);
			
			style.setParentIds(new int[] { 6 });
			style.setChildIds(new int[] { 7 });
			
			XMLSerializer.saveData(style, "data/junit/temp/style.xml");
			style = (StyleRecord)XMLSerializer.readData("data/junit/temp/style.xml");
			
			if (!style.getId().equals(new StyleIdentifier("test style")))
				fail("id incorrect");			
			if (style.getUniqueId() != uniqueId)
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
			
			if (!style.isCategoryOnly())
				fail("category only incorrect");
			
			if (style.getNumArtistRecords() != 1)
				fail("incorrect # artist records, is=" + style.getNumArtistRecords());
//			if (style.getNumLabelRecords() != 2)
//				fail("incorrect # label records");
//			if (style.getNumReleaseRecords() != 3)
//				fail("incorrect # release records");
//			if (style.getNumSongRecords() != 4)
//				fail("incorrect # song records");
//			if (style.getNumExternalArtistRecords() != 5)
//				fail("incorrect # external artist records");
//			if (style.getNumExternalLabelRecords() != 6)
//				fail("incorrect # external label records");
//			if (style.getNumExternalReleaseRecords() != 7)
//				fail("incorrect # external  records");
//			if (style.getNumExternalSongRecords() != 8)
//				fail("incorrect # external  records");
			
			if (!style.isRoot())
				fail("root didn't persist");
			
			if (style.getParentIds()[0] != 6)
				fail("parent incorrect");
			if (style.getChildIds()[0] != 7)
				fail("child incorrect");
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
}
