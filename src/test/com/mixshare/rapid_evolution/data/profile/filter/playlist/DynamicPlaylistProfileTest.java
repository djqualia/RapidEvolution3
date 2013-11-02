package test.com.mixshare.rapid_evolution.data.profile.filter.playlist;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.data.identifier.filter.playlist.PlaylistIdentifier;
import com.mixshare.rapid_evolution.data.profile.filter.playlist.DynamicPlaylistProfile;
import com.mixshare.rapid_evolution.data.profile.filter.playlist.PlaylistProfile;
import com.mixshare.rapid_evolution.data.record.filter.playlist.DynamicPlaylistRecord;
import com.mixshare.rapid_evolution.data.record.filter.playlist.PlaylistRecord;
import com.mixshare.rapid_evolution.data.util.io.XMLSerializer;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineReaderFactory;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.mixshare.rapid_evolution.util.io.LineWriterFactory;

public class DynamicPlaylistProfileTest extends RE3TestCase {

	public void testSerialization() {
		try {		
			DynamicPlaylistProfile playlistProfile = new DynamicPlaylistProfile();
			
			DynamicPlaylistRecord playlist = new DynamicPlaylistRecord();			
			playlistProfile.setRecord(playlist);			
			
			playlistProfile.setRatingSource((byte)1);
			
			playlist.setId(new PlaylistIdentifier("test playlist"));			
			playlist.setUniqueId(420);
			
			int[] dupIds = new int[] { 1, 2 };
			playlist.setDuplicateIds(dupIds);
			
			playlist.setRating((byte)80);
			playlist.setDisabled(true);
			playlist.setLastModified(234);
						
			playlist.setNumArtistRecordsCached(1);
			playlist.setNumLabelRecordsCached(2);
			playlist.setNumReleaseRecordsCached(3);
			playlist.setNumSongRecordsCached(4);
			playlist.setNumExternalArtistRecordsCached(5);
			playlist.setNumExternalLabelRecordsCached(6);
			playlist.setNumExternalReleaseRecordsCached(7);
			playlist.setNumExternalSongRecordsCached(8);
			
			playlist.setRoot(true);
						
			playlist.setParentIds(new int[] { 6 });
			playlist.setChildIds(new int[] { 7 });
			
			XMLSerializer.saveData(playlistProfile, "data/junit/temp/playlist_profile.xml");
			playlistProfile = (DynamicPlaylistProfile)XMLSerializer.readData("data/junit/temp/playlist_profile.xml");
			playlist = playlistProfile.getDynamicPlaylistRecord();			
			
			checkValues(playlistProfile, playlist);
			
			LineWriter writer = LineWriterFactory.getLineWriter("data/junit/temp/playlist_profile.xml");
			playlistProfile.write(writer);
			writer.close();
			LineReader lineReader = LineReaderFactory.getLineReader("data/junit/temp/playlist_profile.xml");
			playlistProfile = (DynamicPlaylistProfile)PlaylistProfile.readPlaylistProfile(lineReader);
			playlist = (DynamicPlaylistRecord)playlistProfile.getPlaylistRecord();	

			checkValues(playlistProfile, playlist);
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	private void checkValues(PlaylistProfile playlistProfile, PlaylistRecord playlist) {
		if (playlistProfile.getRatingSource() != (byte)1)
			fail();
		
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
					
//		if (playlist.getNumArtistRecordsCached() != 1)
//			fail("incorrect # artist records");
//		if (playlist.getNumLabelRecordsCached() != 2)
//			fail("incorrect # label records");
//		if (playlist.getNumReleaseRecordsCached() != 3)
//			fail("incorrect # release records");
//		if (playlist.getNumSongRecordsCached() != 4)
//			fail("incorrect # song records");
//		if (playlist.getNumExternalArtistRecordsCached() != 5)
//			fail("incorrect # external artist records");
//		if (playlist.getNumExternalLabelRecordsCached() != 6)
//			fail("incorrect # external label records");
//		if (playlist.getNumExternalReleaseRecordsCached() != 7)
//			fail("incorrect # external  records");
//		if (playlist.getNumExternalSongRecordsCached() != 8)
//			fail("incorrect # external  records");
		
		if (!playlist.isRoot())
			fail("root didn't persist");
		
		if (playlist.getParentIds()[0] != 6)
			fail("parent incorrect");
		if (playlist.getChildIds()[0] != 7)
			fail("child incorrect");		
	}
	
	
}
