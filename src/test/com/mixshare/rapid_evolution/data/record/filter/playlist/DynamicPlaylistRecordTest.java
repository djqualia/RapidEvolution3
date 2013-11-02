package test.com.mixshare.rapid_evolution.data.record.filter.playlist;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.data.identifier.filter.playlist.PlaylistIdentifier;
import com.mixshare.rapid_evolution.data.record.filter.playlist.DynamicPlaylistRecord;
import com.mixshare.rapid_evolution.data.record.filter.playlist.PlaylistRecord;
import com.mixshare.rapid_evolution.data.util.io.XMLSerializer;

public class DynamicPlaylistRecordTest extends RE3TestCase {

	public void testSerialization() {
		try {		
			DynamicPlaylistRecord playlist = new DynamicPlaylistRecord();
			
			playlist.setId(new PlaylistIdentifier("test playlist"));			
			playlist.setUniqueId(420);
			
			int[] dupIds = new int[] { 1, 2 };
			playlist.setDuplicateIds(dupIds);
			
			playlist.setRating((byte)80);
			playlist.setDisabled(true);
			playlist.setLastModified(234);
			
			playlist.addArtist(1);
			playlist.addLabel(2);
			playlist.addLabel(5);
			playlist.addRelease(3);
			playlist.addRelease(6);
			playlist.addRelease(7);
			playlist.addSong(4);
			playlist.addSong(8);
			playlist.addSong(9);
			playlist.addSong(10);
			
			playlist.setNumArtistRecordsCached(1);
			playlist.setNumLabelRecordsCached(2);
			playlist.setNumReleaseRecordsCached(3);
			playlist.setNumSongRecordsCached(4);
			playlist.setNumExternalArtistRecordsCached(5);
			playlist.setNumExternalLabelRecordsCached(6);
			playlist.setNumExternalReleaseRecordsCached(7);
			playlist.setNumExternalSongRecordsCached(8);
			
			playlist.setRoot(true);
			
			DynamicPlaylistRecord testParent = new DynamicPlaylistRecord();			
			testParent.setId(new PlaylistIdentifier("test parent"));
			testParent.setUniqueId(123);
			
			DynamicPlaylistRecord testChild = new DynamicPlaylistRecord();			
			testChild.setId(new PlaylistIdentifier("test child"));
			testChild.setUniqueId(321);
			
			playlist.setParentIds(new int[] { 6 });
			playlist.setChildIds(new int[] { 7 });
			
			XMLSerializer.saveData(playlist, "data/junit/temp/playlist.xml");
			playlist = (DynamicPlaylistRecord)XMLSerializer.readData("data/junit/temp/playlist.xml");
			
			if (!playlist.getId().equals(new PlaylistIdentifier("test playlist")))
				fail("id incorrect");			
			if (playlist.getUniqueId() != 420)
				fail("unique id incorrect");

			if (playlist.getDuplicateIds()[0] != 1)
				fail("dup id incorrect");
			if (playlist.getDuplicateIds()[1] != 2)
				fail("dup id incorrect");
			
			if (playlist.getRating() != (byte)80)
				fail("rating incorrect");
			if (!playlist.isDisabled())
				fail("not disabled");
			if (playlist.getLastModified() != 234)
				fail("last modified incorrect");
			
			if (!playlist.getArtistIds().containsKey(1))
				fail("doesn't contain artist");
			if (!playlist.getLabelIds().containsKey(2))
				fail("doesn't contain label");
			if (!playlist.getReleaseIds().containsKey(3))
				fail("doesn't contain release");
			if (!playlist.getSongIds().containsKey(4))
				fail("doesn't contain song");
			
			if (playlist.getNumArtistRecordsCached() != 1)
				fail("incorrect # artist records");
			if (playlist.getNumLabelRecordsCached() != 2)
				fail("incorrect # label records, is=" + playlist.getNumLabelRecords());
			if (playlist.getNumReleaseRecordsCached() != 3)
				fail("incorrect # release records");
			if (playlist.getNumSongRecordsCached() != 4)
				fail("incorrect # song records");
			if (playlist.getNumExternalArtistRecordsCached() != 5)
				fail("incorrect # external artist records");
			if (playlist.getNumExternalLabelRecordsCached() != 6)
				fail("incorrect # external label records");
			if (playlist.getNumExternalReleaseRecordsCached() != 7)
				fail("incorrect # external  records");
			if (playlist.getNumExternalSongRecordsCached() != 8)
				fail("incorrect # external  records");
			
			if (!playlist.isRoot())
				fail("root didn't persist");
			
			if (playlist.getParentIds()[0] != 6)
				fail("parent incorrect");
			if (playlist.getChildIds()[0] != 7)
				fail("child incorrect");
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
}
