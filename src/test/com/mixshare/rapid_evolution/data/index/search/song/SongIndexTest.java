package test.com.mixshare.rapid_evolution.data.index.search.song;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.filter.style.StyleIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.artist.ArtistIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.label.LabelIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.release.ReleaseIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.song.SongIdentifier;
import com.mixshare.rapid_evolution.data.index.imdb.LocalIMDB;
import com.mixshare.rapid_evolution.data.index.search.song.SongIndex;
import com.mixshare.rapid_evolution.data.profile.filter.style.StyleProfile;
import com.mixshare.rapid_evolution.data.profile.search.artist.ArtistProfile;
import com.mixshare.rapid_evolution.data.profile.search.label.LabelProfile;
import com.mixshare.rapid_evolution.data.profile.search.release.ReleaseProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.data.record.user.UserDataType;
import com.mixshare.rapid_evolution.data.submitted.search.song.SubmittedSong;
import com.mixshare.rapid_evolution.data.util.io.XMLSerializer;
import com.mixshare.rapid_evolution.data.util.normalization.DuplicateRecordMerger;
import com.mixshare.rapid_evolution.data.util.table.UniqueIdTable;
import com.mixshare.rapid_evolution.ui.model.ModelManagerInterface;
import com.mixshare.rapid_evolution.ui.model.search.song.SongModelManager;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineReaderFactory;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.mixshare.rapid_evolution.util.io.LineWriterFactory;

public class SongIndexTest extends RE3TestCase {

    static private Logger log = Logger.getLogger(SongIndexTest.class);    
	
	/**
	 * This tests merging songs, and the effects on related items (artists/releases/labels/styles/tags/etc)
	 */
	public void testDuplicateSongs() {
		try {
			// song 1
			SubmittedSong submittedSong1 = new SubmittedSong("aphex twin", "selected ambient works", "d1", "heliosphere", "");
			submittedSong1.setLabelName("rephlex");
			submittedSong1.addStyleDegreeValue("Ambient", 1.0f, DATA_SOURCE_UNKNOWN);
			submittedSong1.addStyleDegreeValue("Downtempo", 0.75f, DATA_SOURCE_UNKNOWN);
			submittedSong1.addStyleDegreeValue("Electronica", 1.0f, DATA_SOURCE_UNKNOWN);
			submittedSong1.addTagDegreeValue("AFX", 1.0f, DATA_SOURCE_UNKNOWN);
			submittedSong1.addTagDegreeValue("Chill", 0.75f, DATA_SOURCE_UNKNOWN);
			submittedSong1.addTagDegreeValue("Trippy", 0.5f, DATA_SOURCE_UNKNOWN);
			
			// song 2
			SubmittedSong submittedSong2 = new SubmittedSong("aphex twin", "some compilation album", "10", "heliosphere", "original mix");
			submittedSong2.setLabelName("warp");
			submittedSong2.addStyleDegreeValue("Ambient", 1.0f, DATA_SOURCE_UNKNOWN);
			submittedSong2.addStyleDegreeValue("Thinking Music", 0.75f, DATA_SOURCE_UNKNOWN);
			submittedSong2.addStyleDegreeValue("Electronica", 1.0f, DATA_SOURCE_UNKNOWN);
			submittedSong2.addTagDegreeValue("AFX", 1.0f, DATA_SOURCE_UNKNOWN);
			submittedSong2.addTagDegreeValue("Chill", 0.75f, DATA_SOURCE_UNKNOWN);
			submittedSong2.addTagDegreeValue("Meditation", 0.5f, DATA_SOURCE_UNKNOWN);
						
			// add the songs
			SongProfile profile1 = Database.getSongIndex().addSong(submittedSong1);
			SongProfile profile2 = Database.getSongIndex().addSong(submittedSong2);
			
			// set comment on a style on the merged song to make sure it persists after merge
			StyleProfile styleProfile = Database.getStyleIndex().getStyleProfile(new StyleIdentifier("Thinking Music"));
			styleProfile.setDescription("test");
			styleProfile.save();
			
			// set comments on a label on the merged song to maek sure it persists after merge
			LabelProfile labelProfile = Database.getLabelIndex().getLabelProfile(new LabelIdentifier("warp"));
			labelProfile.setComments("test", DATA_SOURCE_UNKNOWN);
			labelProfile.save();
			
			// check the initial state of the db
			if (Database.getArtistIndex().getIds().size() != 1)
				fail("incorrect # artists");
			if (Database.getLabelIndex().getIds().size() != 2)
				fail("incorrect # labels");			
			if (Database.getReleaseIndex().getIds().size() != 2)
				fail("incorrect # releases");
			if (Database.getSongIndex().getIds().size() != 2)
				fail("incorrect # songs");
						
			// ensure the song contains references to both releases now
			if (profile1.getReleases().size() != 1)
				fail("incorrect # of releases on song");
			if (!profile1.getReleaseInstances().get(0).getRelease().getReleaseTitle().equals("selected ambient works"))
				fail("incorrect release title on song");
			if (!profile1.getReleaseInstances().get(0).getTrack().equals("d1"))
				fail("incorrect release track on song");
			if (profile2.getReleases().size() != 1)
				fail("incorrect # of releases on song");
			if (!profile2.getReleaseInstances().get(0).getRelease().getReleaseTitle().equals("some compilation album"))
				fail("incorrect release title on song");
			if (!profile2.getReleaseInstances().get(0).getTrack().equals("10"))
				fail("incorrect release track on song");
			
			// merge the songs
			Database.getSongIndex().mergeProfiles(profile1, profile2);
			
			// check the state of the db
			if (Database.getArtistIndex().getIds().size() != 1)
				fail("incorrect # artists");
			if (Database.getLabelIndex().getIds().size() != 2)
				fail("incorrect # labels");
			if (Database.getReleaseIndex().getIds().size() != 2)
				fail("incorrect # releases, is=" + Database.getReleaseIndex().getIds().size());
			if (Database.getSongIndex().getIds().size() != 1)
				fail("incorrect # songs");			
			
			// make sure fetching either song id returns the primary song
			SongRecord record1 = Database.getSongIndex().getSongRecord(submittedSong1.getSongIdentifier());
			SongRecord record2 = Database.getSongIndex().getSongRecord(submittedSong2.getSongIdentifier());			
			if (!record1.getIdentifier().equals(record2.getIdentifier()))
				fail("merged record not removed");
			
			// make sure the primary song contains all styles from both songs
			if (!record1.containsSourceStyle("Ambient"))
				fail("styles not merged");
			if (!record1.containsSourceStyle("Downtempo"))
				fail("styles not merged");
			if (!record1.containsSourceStyle("Electronica"))
				fail("styles not merged");
			if (!record1.containsSourceStyle("Thinking Music"))
				fail("styles not merged");
			
			// make sure the primary song contains all tags from both songs
			if (!record1.containsSourceTag("AFX"))
				fail("tags not merged");
			if (!record1.containsSourceTag("Chill"))
				fail("tags not merged");
			if (!record1.containsSourceTag("Trippy"))
				fail("tags not merged");
			if (!record1.containsSourceTag("Meditation"))
				fail("tags not merged");
			
			// make sure the comment on the styles persisted (i.e. style wasn't deleted/re-added)
			styleProfile = Database.getStyleIndex().getStyleProfile(new StyleIdentifier("Thinking Music"));
			if (!styleProfile.getDescription().equals("test"))
				fail("style details were lost");
			
			// make sure the comment on the label persisted (i.e. label wasn't deleted/re-added)
			labelProfile = Database.getLabelIndex().getLabelProfile(new LabelIdentifier("warp"));
			if (!labelProfile.getComments().equals("test"))
				fail("label details were lost");
			
			// ensure both labels contain the now primary song
			if (labelProfile.getNumSongs() != 1)
				fail("label # songs incorrect");						
			if (!labelProfile.containsSong(record1.getSongIdentifier()))
				fail("label doesn't contain song");
			
			labelProfile = Database.getLabelIndex().getLabelProfile(new LabelIdentifier("rephlex"));
			if (labelProfile.getNumSongs() != 1)
				fail("label # songs incorrect");			
			if (!labelProfile.containsSong(record1.getSongIdentifier()))
				fail("label doesn't contain song");
			
			// ensure both releases contain the now primary song
			ReleaseProfile releaseProfile = Database.getReleaseIndex().getReleaseProfile(new ReleaseIdentifier("aphex twin", "selected ambient works"));
			if (releaseProfile.getNumSongs() != 1)
				fail("release # songs incorrect");
			if (!releaseProfile.containsSong(record1.getSongIdentifier()))
				fail("release doesn't contain song");			

			releaseProfile = Database.getReleaseIndex().getReleaseProfile(new ReleaseIdentifier("aphex twin", "some compilation album"));
			if (releaseProfile.getNumSongs() != 1)
				fail("release # songs incorrect");
			if (!releaseProfile.containsSong(record1.getSongIdentifier()))
				fail("release doesn't contain song");			
			
			// ensure the artist is properly updated
			ArtistProfile artistProfile = Database.getArtistIndex().getArtistProfile(new ArtistIdentifier("aphex twin"));
			if (artistProfile.getNumSongs() != 1)
				fail("artist # songs incorrect");
			if (!artistProfile.containsSong(record1.getSongIdentifier()))
				fail("artist doesn't contain song");
			
			// ensure the song contains references to both releases now
			if (profile1.getReleases().size() != 2)
				fail("incorrect # of releases on song");
			if (!profile1.getReleaseInstances().get(0).getRelease().getReleaseTitle().equals("selected ambient works"))
				fail("incorrect release title on song");
			if (!profile1.getReleaseInstances().get(0).getTrack().equals("d1"))
				fail("incorrect release track on song");
			if (!profile1.getReleaseInstances().get(1).getRelease().getReleaseTitle().equals("some compilation album"))
				fail("incorrect release title on song");
			if (!profile1.getReleaseInstances().get(1).getTrack().equals("10"))
				fail("incorrect release track on song");
			
		} catch (Exception e) {
			log.error("testDuplicateSongs(): error", e);
			fail(e.getMessage());
		}
	}
	
	/**
	 * Tests merging songs and the effects on SongGroupProfile related classes (artists/labels/releases)
	 */
	public void testDuplicateSongsOnGroups() {
		try {
			// song 1
			SubmittedSong submittedSong1 = new SubmittedSong("aphex twin", "selected ambient works", "d1", "heliosphere", "");
			submittedSong1.setLabelName("rephlex");
			
			// song 2
			SubmittedSong submittedSong2 = new SubmittedSong("aphex twin", "selected ambient works", "10", "heliosphere", "original mix");
			submittedSong2.setLabelName("rephlex");
						
			// add the songs
			SongProfile profile1 = Database.getSongIndex().addSong(submittedSong1);
			SongProfile profile2 = Database.getSongIndex().addSong(submittedSong2);

			// check the state of the db
			if (Database.getArtistIndex().getIds().size() != 1)
				fail("incorrect # artists");
			if (Database.getLabelIndex().getIds().size() != 1)
				fail("incorrect # labels");
			if (Database.getReleaseIndex().getIds().size() != 1)
				fail("incorrect # releases");
			if (Database.getSongIndex().getIds().size() != 2)
				fail("incorrect # songs");							
						
			// merge the songs
			Database.getSongIndex().mergeProfiles(profile1, profile2);
			
			// check the state of the db
			if (Database.getArtistIndex().getIds().size() != 1)
				fail("incorrect # artists");
			if (Database.getLabelIndex().getIds().size() != 1)
				fail("incorrect # labels");
			if (Database.getReleaseIndex().getIds().size() != 1)
				fail("incorrect # releases");
			if (Database.getSongIndex().getIds().size() != 1)
				fail("incorrect # songs");					
			
			// ensure the artist is properly updated
			ArtistProfile artistProfile = Database.getArtistIndex().getArtistProfile(new ArtistIdentifier("aphex twin"));
			if (artistProfile.getNumSongs() != 1)
				fail("artist # songs incorrect");		
			
			// ensure the label is properly updated
			LabelProfile labelProfile = Database.getLabelIndex().getLabelProfile(new LabelIdentifier("rephlex"));
			if (labelProfile.getNumSongs() != 1)
				fail("label # songs incorrect");	

			// ensure the release is properly updated
			ReleaseProfile releaseProfile = Database.getReleaseIndex().getReleaseProfile(new ReleaseIdentifier("aphex twin", "selected ambient works"));
			if (releaseProfile.getNumSongs() != 1)
				fail("release # songs incorrect");	
			
		} catch (Exception e) {
			log.error("testDuplicateSongsOnGroups(): error", e);
			fail(e.getMessage());
		}
	}	
	
	/**
	 * This method tests updating a song's title, remix, artist, release track, etc
	 */
	public void testSongUpdate() {
		try {
			// song 1
			SubmittedSong submittedSong1 = new SubmittedSong("aphex twin", "selected ambient works", "d1", "heliosphere", "");
			submittedSong1.setLabelName("warp");
			
			// song 2 (added to ensure "aphex twin" doesn't get deleted at a later step)
			SubmittedSong submittedSong2 = new SubmittedSong("aphex twin", "", "", "another random song", "");
			
			// add the song..
			SongProfile songProfile1 = Database.getSongIndex().addSong(submittedSong1);
			Database.add(submittedSong2);
			if (!songProfile1.getTitle().equals("heliosphere"))
				fail("title is incorrect");
			if (!songProfile1.toString().equals("aphex twin - selected ambient works  [d1]  heliosphere"))
				fail("toString is incorrect");

			ReleaseProfile releaseProfile = Database.getReleaseIndex().getReleaseProfile(submittedSong1.getSubmittedRelease().getReleaseIdentifier());
			if (!releaseProfile.containsSong(songProfile1.getUniqueId()))
				fail("release does not contain song");
			if (!releaseProfile.containsSong(songProfile1.getIdentifier()))
				fail("release does not contain song");

			ArtistProfile artistProfile = Database.getArtistIndex().getArtistProfile(new ArtistIdentifier("aphex twin"));
			if (!artistProfile.containsSong(songProfile1.getUniqueId()))
				fail("artist does not contain the song");
			if (!artistProfile.containsSong(songProfile1.getIdentifier()))
				fail("artist does not contain the song");
			
			LabelProfile labelProfile = Database.getLabelIndex().getLabelProfile(new LabelIdentifier("warp"));
			if (!labelProfile.containsSong(songProfile1.getUniqueId()))
				fail("label does not contain the song");						
			if (!labelProfile.containsSong(songProfile1.getIdentifier()))
				fail("label does not contain the song");						
			
			// check state of db
			if (Database.getLabelIndex().getIds().size() != 1) 
				fail("incorrect # labels");
			if (Database.getArtistIndex().getIds().size() != 1)
				fail("incorrect # artists");			
			if (Database.getReleaseIndex().getIds().size() != 1)
				fail("incorrect # releases");
			if (Database.getSongIndex().getIds().size() != 2)
				fail("incorrect # songs");	
			
			// let's update the song's title
			songProfile1.setTitle("heliospan");
			
			if (!songProfile1.getTitle().equals("heliospan"))
				fail("title is incorrect");
			if (!songProfile1.toString().equals("aphex twin - selected ambient works  [d1]  heliospan"))
				fail("toString is incorrect");
			if (!songProfile1.getSongIdentifier().getSongDescription().equals("heliospan"))
				fail("song description is incorrect");
			
			releaseProfile = Database.getReleaseIndex().getReleaseProfile(submittedSong1.getSubmittedRelease().getReleaseIdentifier());
			if (!releaseProfile.containsSong(songProfile1.getUniqueId()))
				fail("release does not contain song");
			if (!releaseProfile.containsSong(songProfile1.getIdentifier()))
				fail("release does not contain song");

			artistProfile = Database.getArtistIndex().getArtistProfile(new ArtistIdentifier("aphex twin"));
			if (!artistProfile.containsSong(songProfile1.getUniqueId()))
				fail("artist does not contain the song");
			if (!artistProfile.containsSong(songProfile1.getIdentifier()))
				fail("artist does not contain the song");
			
			labelProfile = Database.getLabelIndex().getLabelProfile(new LabelIdentifier("warp"));
			if (!labelProfile.containsSong(songProfile1.getUniqueId()))
				fail("label does not contain the song");						
			if (!labelProfile.containsSong(songProfile1.getIdentifier()))
				fail("label does not contain the song");						
			
			// check state of db
			if (Database.getLabelIndex().getIds().size() != 1) 
				fail("incorrect # labels");
			if (Database.getArtistIndex().getIds().size() != 1)
				fail("incorrect # artists");			
			if (Database.getReleaseIndex().getIds().size() != 1)
				fail("incorrect # releases");
			if (Database.getSongIndex().getIds().size() != 2)
				fail("incorrect # songs");				
			
			// now update song's remix
			songProfile1.setRemix("original mix");
			
			if (!songProfile1.getRemix().equals("original mix"))
				fail("title is incorrect");
			if (!songProfile1.toString().equals("aphex twin - selected ambient works  [d1]  heliospan (original mix)"))
				fail("toString is incorrect");
			if (!songProfile1.getSongIdentifier().getSongDescription().equals("heliospan (original mix)"))
				fail("song description is incorrect");
			
			releaseProfile = Database.getReleaseIndex().getReleaseProfile(submittedSong1.getSubmittedRelease().getReleaseIdentifier());
			if (!releaseProfile.containsSong(songProfile1.getUniqueId()))
				fail("release does not contain song");
			if (!releaseProfile.containsSong(songProfile1.getIdentifier()))
				fail("release does not contain song");

			artistProfile = Database.getArtistIndex().getArtistProfile(new ArtistIdentifier("aphex twin"));
			if (!artistProfile.containsSong(songProfile1.getUniqueId()))
				fail("artist does not contain the song");
			if (!artistProfile.containsSong(songProfile1.getIdentifier()))
				fail("artist does not contain the song");
			
			labelProfile = Database.getLabelIndex().getLabelProfile(new LabelIdentifier("warp"));
			if (!labelProfile.containsSong(songProfile1.getUniqueId()))
				fail("label does not contain the song");						
			if (!labelProfile.containsSong(songProfile1.getIdentifier()))
				fail("label does not contain the song");						
			
			// check state of db
			if (Database.getLabelIndex().getIds().size() != 1) 
				fail("incorrect # labels");
			if (Database.getArtistIndex().getIds().size() != 1)
				fail("incorrect # artists");			
			if (Database.getReleaseIndex().getIds().size() != 1)
				fail("incorrect # releases");
			if (Database.getSongIndex().getIds().size() != 2)
				fail("incorrect # songs");		
			
			// now update song's artist
			Vector<String> artistNames = new Vector<String>();
			artistNames.add("afx");
			songProfile1.setArtistNames(artistNames);
			
			if (!songProfile1.getArtistsDescription().equals("afx"))
				fail("artist is incorrect");
			if (!songProfile1.toString().equals("afx - selected ambient works  [d1]  heliospan (original mix)"))
				fail("toString is incorrect");
			if (!songProfile1.getSongIdentifier().getArtistDescription().equals("afx"))
				fail("song description is incorrect");
			
			// check state of db
			if (Database.getLabelIndex().getIds().size() != 1) 
				fail("incorrect # labels");
			if (Database.getArtistIndex().getIds().size() != 2)
				fail("incorrect # artists, is=" + Database.getArtistIndex().getIds().size());			
			if (Database.getReleaseIndex().getIds().size() != 1)
				fail("incorrect # releases");
			if (Database.getSongIndex().getIds().size() != 2)
				fail("incorrect # songs");
			
			releaseProfile = Database.getReleaseIndex().getReleaseProfile(new ReleaseIdentifier("afx", "selected ambient works"));
			if (releaseProfile == null)
				fail("could not lookup release by new id");
			if (!releaseProfile.containsSong(songProfile1.getUniqueId()))
				fail("release does not contain song");
			if (!releaseProfile.containsSong(songProfile1.getIdentifier()))
				fail("release does not contain song");

			artistProfile = Database.getArtistIndex().getArtistProfile(new ArtistIdentifier("afx"));
			if (artistProfile.getNumSongs() != 1)
				fail("incorrect # songs on artist");
			if (!artistProfile.containsSong(songProfile1.getUniqueId()))
				fail("artist does not contain the song");
			if (!artistProfile.containsSong(songProfile1.getIdentifier()))
				fail("artist does not contain the song");
			
			// ensure old artist doesn't contain song
			artistProfile = Database.getArtistIndex().getArtistProfile(new ArtistIdentifier("aphex twin"));
			if (artistProfile.getNumSongs() != 1)
				fail("incorrect # songs on artist");
			if (artistProfile.containsSong(songProfile1.getUniqueId()))
				fail("artist does not contain the song");
			if (artistProfile.containsSong(songProfile1.getIdentifier()))
				fail("artist does not contain the song");
			
			labelProfile = Database.getLabelIndex().getLabelProfile(new LabelIdentifier("warp"));
			if (!labelProfile.containsSong(songProfile1.getUniqueId()))
				fail("label does not contain the song");						
			if (!labelProfile.containsSong(songProfile1.getIdentifier()))
				fail("label does not contain the song");						
						
			// now let's change an artist's release
			songProfile1.setReleaseTitle("selected ambient works volume 1");
			
			if (songProfile1.getReleases().size() != 1)
				fail("incorrect # releases, is=" + songProfile1.getReleases().size());
			if (songProfile1.getReleaseInstances().size() != 1)
				fail("incorrect # releases, is=" + songProfile1.getReleaseInstances().size());
			if (!songProfile1.getReleases().get(0).getReleaseTitle().equals("selected ambient works volume 1"))
				fail("release title is incorrect");
			if (songProfile1.getReleaseInstances().get(0).getRelease() == null)
				fail("release instance is null");
			if (!songProfile1.toString().equals("afx - selected ambient works volume 1  [d1]  heliospan (original mix)"))
				fail("toString is incorrect");
			if (!songProfile1.getSongIdentifier().getSongDescription().equals("heliospan (original mix)"))
				fail("song description is incorrect");
			
			// check state of db
			if (Database.getLabelIndex().getIds().size() != 1) 
				fail("incorrect # labels");
			if (Database.getArtistIndex().getIds().size() != 2)
				fail("incorrect # artists, is=" + Database.getArtistIndex().getIds().size());			
			if (Database.getReleaseIndex().getIds().size() != 1)
				fail("incorrect # releases");
			if (Database.getSongIndex().getIds().size() != 2)
				fail("incorrect # songs");
			
			releaseProfile = Database.getReleaseIndex().getReleaseProfile(new ReleaseIdentifier("afx", "selected ambient works volume 1"));
			if (releaseProfile == null)
				fail("could not lookup release by new id");
			if (releaseProfile.getNumSongs() != 1)
				fail("incorrect # of songs on release");
			if (!releaseProfile.containsSong(songProfile1.getUniqueId()))
				fail("release does not contain song");
			if (!releaseProfile.containsSong(songProfile1.getIdentifier()))
				fail("release does not contain song");

			releaseProfile = Database.getReleaseIndex().getReleaseProfile(new ReleaseIdentifier("afx", "selected ambient works"));
			if (releaseProfile != null)
				fail("old release not removed");
			
			labelProfile = Database.getLabelIndex().getLabelProfile(new LabelIdentifier("warp"));
			if (!labelProfile.containsSong(songProfile1.getUniqueId()))
				fail("label does not contain the song");						
			if (!labelProfile.containsSong(songProfile1.getIdentifier()))
				fail("label does not contain the song");			

			// now let's change an artist's track
			songProfile1.setReleaseTrack("d2");
			
			if (songProfile1.getReleases().size() != 1)
				fail("incorrect # releases, is=" + songProfile1.getReleases().size());
			if (songProfile1.getReleaseInstances().size() != 1)
				fail("incorrect # releases, is=" + songProfile1.getReleaseInstances().size());
			if (songProfile1.getReleaseInstances().get(0).getRelease() == null)
				fail("release instance is null");
			if (!songProfile1.getReleaseInstances().get(0).getTrack().equals("d2"))
				fail("release track is incorrect");
			if (!songProfile1.toString().equals("afx - selected ambient works volume 1  [d2]  heliospan (original mix)"))
				fail("toString is incorrect");
			if (!songProfile1.getSongIdentifier().getSongDescription().equals("heliospan (original mix)"))
				fail("song description is incorrect");
			
			// check state of db
			if (Database.getLabelIndex().getIds().size() != 1) 
				fail("incorrect # labels");
			if (Database.getArtistIndex().getIds().size() != 2)
				fail("incorrect # artists, is=" + Database.getArtistIndex().getIds().size());			
			if (Database.getReleaseIndex().getIds().size() != 1)
				fail("incorrect # releases");
			if (Database.getSongIndex().getIds().size() != 2)
				fail("incorrect # songs");
			
			releaseProfile = Database.getReleaseIndex().getReleaseProfile(new ReleaseIdentifier("afx", "selected ambient works volume 1"));
			if (releaseProfile == null)
				fail("could not lookup release by new id");
			if (releaseProfile.getNumSongs() != 1)
				fail("incorrect # of songs on release");
			if (!releaseProfile.containsSong(songProfile1.getUniqueId()))
				fail("release does not contain song");
			if (!releaseProfile.containsSong(songProfile1.getIdentifier()))
				fail("release does not contain song");
			
			labelProfile = Database.getLabelIndex().getLabelProfile(new LabelIdentifier("warp"));
			if (!labelProfile.containsSong(songProfile1.getUniqueId()))
				fail("label does not contain the song");						
			if (!labelProfile.containsSong(songProfile1.getIdentifier()))
				fail("label does not contain the song");				
			
			// now let's change the label
			Vector<String> newLabelNames = new Vector<String>(1);
			newLabelNames.add("warp records");
			songProfile1.setLabelNames(newLabelNames);
			
			// check state of db
			if (Database.getLabelIndex().getIds().size() != 1) 
				fail("incorrect # labels");
			if (Database.getArtistIndex().getIds().size() != 2)
				fail("incorrect # artists, is=" + Database.getArtistIndex().getIds().size());			
			if (Database.getReleaseIndex().getIds().size() != 1)
				fail("incorrect # releases");
			if (Database.getSongIndex().getIds().size() != 2)
				fail("incorrect # songs");
			
			labelProfile = Database.getLabelIndex().getLabelProfile(new LabelIdentifier("warp records"));
			if (!labelProfile.containsSong(songProfile1.getUniqueId()))
				fail("label does not contain the release");						
			if (!labelProfile.containsRelease(releaseProfile.getUniqueId()))
				fail("label does not contain the release");		
			
			if (!songProfile1.getLabelsDescription().equals("warp records"))
				fail("incorrect label description");
			if (!releaseProfile.getLabelsDescription().equals("warp records"))
				fail("incorrect label description");
			
		} catch (Exception e) {
			log.error("testSongUpdate(): error", e);
			fail(e.getMessage());	
		}
	}	

	/**
	 * This method tests updating a song's release/track
	 */
	public void testSongUpdateSetReleaseExtensive() {
		try {
			// song 1
			SubmittedSong submittedSong1 = new SubmittedSong("aphex twin", "", "", "heliospan", "");
			
			// add the song..
			SongProfile songProfile1 = Database.getSongIndex().addSong(submittedSong1);
			if (!songProfile1.getTitle().equals("heliospan"))
				fail("title is incorrect");
			if (!songProfile1.toString().equals("aphex twin - heliospan"))
				fail("toString is incorrect");				
			
			// check state of db
			if (Database.getArtistIndex().getIds().size() != 1)
				fail("incorrect # artists");			
			if (Database.getReleaseIndex().getIds().size() != 0)
				fail("incorrect # releases");
			if (Database.getSongIndex().getIds().size() != 1)
				fail("incorrect # songs");	
			
			if (songProfile1.getReleases().size() != 0)
				fail("incorrect # releases");
			if (songProfile1.getReleaseInstances().size() != 0)
				fail("incorrect # releases instances");
			
			// let's update the song's title
			songProfile1.setReleaseTitle("selected ambient works volume 1");

			// check state of db
			if (Database.getArtistIndex().getIds().size() != 1)
				fail("incorrect # artists");			
			if (Database.getReleaseIndex().getIds().size() != 1)
				fail("incorrect # releases");
			if (Database.getSongIndex().getIds().size() != 1)
				fail("incorrect # songs");	
			
			if (songProfile1.getReleases().size() != 1)
				fail("incorrect # releases");
			if (songProfile1.getReleaseInstances().size() != 1)
				fail("incorrect # releases instances");
			if (!songProfile1.getReleases().get(0).getReleaseTitle().equals("selected ambient works volume 1"))
				fail("incorrect release title, is=" + songProfile1.getReleases().get(0).getReleaseTitle());
			if (!songProfile1.getReleases().get(0).getArtistsDescription().equals("aphex twin"))
				fail("incorrect release artist description");
			if (!songProfile1.getReleaseInstances().get(0).getTrack().equals(""))
				fail("incorrect release instance track");
			
			if (!songProfile1.toString().equals("aphex twin - selected ambient works volume 1 - heliospan"))
				fail("toString is incorrect, is=" + songProfile1.toString());				
			
			ReleaseProfile releaseProfile = Database.getReleaseIndex().getReleaseProfile(new ReleaseIdentifier("aphex twin", "selected ambient works volume 1"));
			if (releaseProfile == null)
				fail("release profile is null");
			if (!releaseProfile.containsSong(songProfile1.getUniqueId()))
				fail("release does not contain song");
			if (!releaseProfile.containsSong(songProfile1.getIdentifier()))
				fail("release does not contain song");
			
			// now let's specify a track, as a user might
			songProfile1.setReleaseTrack("d1");
			
			// check state of db
			if (Database.getArtistIndex().getIds().size() != 1)
				fail("incorrect # artists");			
			if (Database.getReleaseIndex().getIds().size() != 1)
				fail("incorrect # releases");
			if (Database.getSongIndex().getIds().size() != 1)
				fail("incorrect # songs");	
			
			if (songProfile1.getReleases().size() != 1)
				fail("incorrect # releases");
			if (songProfile1.getReleaseInstances().size() != 1)
				fail("incorrect # releases instances");
			if (!songProfile1.getReleases().get(0).getReleaseTitle().equals("selected ambient works volume 1"))
				fail("incorrect release title, is=" + songProfile1.getReleases().get(0).getReleaseTitle());
			if (!songProfile1.getReleases().get(0).getArtistsDescription().equals("aphex twin"))
				fail("incorrect release artist description");
			if (!songProfile1.getReleaseInstances().get(0).getTrack().equals("d1"))
				fail("incorrect release instance track");
			
			if (!songProfile1.toString().equals("aphex twin - selected ambient works volume 1  [d1]  heliospan"))
				fail("toString is incorrect, is=" + songProfile1.toString());				
			
			releaseProfile = Database.getReleaseIndex().getReleaseProfile(new ReleaseIdentifier("aphex twin", "selected ambient works volume 1"));
			if (releaseProfile == null)
				fail("release profile is null");
			if (!releaseProfile.containsSong(songProfile1.getUniqueId()))
				fail("release does not contain song");
			if (!releaseProfile.containsSong(songProfile1.getIdentifier()))
				fail("release does not contain song");		
			
			// let's update the release
			songProfile1.setReleaseTitle("selected ambient works");
			
			// check state of db
			if (Database.getArtistIndex().getIds().size() != 1)
				fail("incorrect # artists");			
			if (Database.getReleaseIndex().getIds().size() != 1)
				fail("incorrect # releases");
			if (Database.getSongIndex().getIds().size() != 1)
				fail("incorrect # songs");	
			
			if (songProfile1.getReleases().size() != 1)
				fail("incorrect # releases");
			if (songProfile1.getReleaseInstances().size() != 1)
				fail("incorrect # releases instances");
			if (!songProfile1.getReleases().get(0).getReleaseTitle().equals("selected ambient works"))
				fail("incorrect release title, is=" + songProfile1.getReleases().get(0).getReleaseTitle());
			if (!songProfile1.getReleases().get(0).getArtistsDescription().equals("aphex twin"))
				fail("incorrect release artist description");
			if (!songProfile1.getReleaseInstances().get(0).getTrack().equals("d1"))
				fail("incorrect release instance track");
			if (!songProfile1.getTrack().equals("d1"))
				fail("incorrect release instance track");
			
			if (!songProfile1.toString().equals("aphex twin - selected ambient works  [d1]  heliospan"))
				fail("toString is incorrect, is=" + songProfile1.toString());				
			
			releaseProfile = Database.getReleaseIndex().getReleaseProfile(new ReleaseIdentifier("aphex twin", "selected ambient works"));
			if (releaseProfile == null)
				fail("release profile is null");
			if (!releaseProfile.containsSong(songProfile1.getUniqueId()))
				fail("release does not contain song");
			if (!releaseProfile.containsSong(songProfile1.getIdentifier()))
				fail("release does not contain song");	
			
			// now clear the release
			songProfile1.setReleaseTitle("");
			
			// check state of db
			if (Database.getArtistIndex().getIds().size() != 1)
				fail("incorrect # artists");			
			if (Database.getReleaseIndex().getIds().size() != 0)
				fail("incorrect # releases");
			if (Database.getSongIndex().getIds().size() != 1)
				fail("incorrect # songs");	
			
			if (songProfile1.getReleases().size() != 0)
				fail("incorrect # releases");
			if (songProfile1.getReleaseInstances().size() != 0)
				fail("incorrect # releases instances");
			if (!songProfile1.getTrack().equals("d1"))
				fail("incorrect release instance track");
			
			if (!songProfile1.toString().equals("aphex twin  [d1]  heliospan"))
				fail("toString is incorrect, is=" + songProfile1.toString());				
			
			// now clear the track
			songProfile1.setReleaseTrack("");

			// check state of db
			if (Database.getArtistIndex().getIds().size() != 1)
				fail("incorrect # artists");			
			if (Database.getReleaseIndex().getIds().size() != 0)
				fail("incorrect # releases");
			if (Database.getSongIndex().getIds().size() != 1)
				fail("incorrect # songs");	
			
			if (songProfile1.getReleases().size() != 0)
				fail("incorrect # releases");
			if (songProfile1.getReleaseInstances().size() != 0)
				fail("incorrect # releases instances");
			if (!songProfile1.getTrack().equals(""))
				fail("incorrect release instance track");
			
			if (!songProfile1.toString().equals("aphex twin - heliospan"))
				fail("toString is incorrect, is=" + songProfile1.toString());				

			// now let's try again, adding the track first
			songProfile1.setReleaseTrack("d1");
			
			if (Database.getArtistIndex().getIds().size() != 1)
				fail("incorrect # artists");			
			if (Database.getReleaseIndex().getIds().size() != 0)
				fail("incorrect # releases");
			if (Database.getSongIndex().getIds().size() != 1)
				fail("incorrect # songs");	
			
			if (songProfile1.getReleases().size() != 0)
				fail("incorrect # releases");
			if (songProfile1.getReleaseInstances().size() != 0)
				fail("incorrect # releases instances");
			if (!songProfile1.getTrack().equals("d1"))
				fail("incorrect release instance track");
			
			if (!songProfile1.toString().equals("aphex twin  [d1]  heliospan"))
				fail("toString is incorrect, is=" + songProfile1.toString());		
			
			// and now setting a release
			songProfile1.setReleaseTitle("selected ambient works");
			
			// check state of db
			if (Database.getArtistIndex().getIds().size() != 1)
				fail("incorrect # artists");			
			if (Database.getReleaseIndex().getIds().size() != 1)
				fail("incorrect # releases");
			if (Database.getSongIndex().getIds().size() != 1)
				fail("incorrect # songs");	
			
			if (songProfile1.getReleases().size() != 1)
				fail("incorrect # releases");
			if (songProfile1.getReleaseInstances().size() != 1)
				fail("incorrect # releases instances");
			if (!songProfile1.getReleases().get(0).getReleaseTitle().equals("selected ambient works"))
				fail("incorrect release title, is=" + songProfile1.getReleases().get(0).getReleaseTitle());
			if (!songProfile1.getReleases().get(0).getArtistsDescription().equals("aphex twin"))
				fail("incorrect release artist description");
			if (!songProfile1.getReleaseInstances().get(0).getTrack().equals("d1"))
				fail("incorrect release instance track");
			if (!songProfile1.getTrack().equals("d1"))
				fail("incorrect release instance track");
			
			if (!songProfile1.toString().equals("aphex twin - selected ambient works  [d1]  heliospan"))
				fail("toString is incorrect, is=" + songProfile1.toString());				
			
			releaseProfile = Database.getReleaseIndex().getReleaseProfile(new ReleaseIdentifier("aphex twin", "selected ambient works"));
			if (releaseProfile == null)
				fail("release profile is null");
			if (!releaseProfile.containsSong(songProfile1.getUniqueId()))
				fail("release does not contain song");
			if (!releaseProfile.containsSong(songProfile1.getIdentifier()))
				fail("release does not contain song");	
						
			// now let's clear the track first
			songProfile1.setReleaseTrack("");
			
			// check state of db
			if (Database.getArtistIndex().getIds().size() != 1)
				fail("incorrect # artists");			
			if (Database.getReleaseIndex().getIds().size() != 1)
				fail("incorrect # releases");
			if (Database.getSongIndex().getIds().size() != 1)
				fail("incorrect # songs");	
			
			if (songProfile1.getReleases().size() != 1)
				fail("incorrect # releases");
			if (songProfile1.getReleaseInstances().size() != 1)
				fail("incorrect # releases instances");
			if (!songProfile1.getReleases().get(0).getReleaseTitle().equals("selected ambient works"))
				fail("incorrect release title, is=" + songProfile1.getReleases().get(0).getReleaseTitle());
			if (!songProfile1.getReleases().get(0).getArtistsDescription().equals("aphex twin"))
				fail("incorrect release artist description");
			if (!songProfile1.getTrack().equals(""))
				fail("incorrect release instance track");
			if (!songProfile1.getReleaseInstances().get(0).getTrack().equals(""))
				fail("incorrect release instance track");
			
			if (!songProfile1.toString().equals("aphex twin - selected ambient works - heliospan"))
				fail("toString is incorrect, is=" + songProfile1.toString());				
			
			releaseProfile = Database.getReleaseIndex().getReleaseProfile(new ReleaseIdentifier("aphex twin", "selected ambient works"));
			if (releaseProfile == null)
				fail("release profile is null");
			if (!releaseProfile.containsSong(songProfile1.getUniqueId()))
				fail("release does not contain song");
			if (!releaseProfile.containsSong(songProfile1.getIdentifier()))
				fail("release does not contain song");
						
		} catch (Exception e) {
			log.error("testSongUpdateSetReleaseExtensive(): error", e);
			fail(e.getMessage());	
		}
	}	
	

	/**
	 * This method tests updating a song's artist
	 */
	public void testSongUpdateArtist() {
		try {
			// song 1
			SubmittedSong submittedSong1 = new SubmittedSong("aphex twin", "", "a1", "bike pump", "");

			// song 2
			SubmittedSong submittedSong2 = new SubmittedSong("aphex twin", "", "a2", "tha", "");
			
			// add the song..
			SongProfile songProfile1 = Database.getSongIndex().addSong(submittedSong1);
			Database.getSongIndex().addSong(submittedSong2);
			
			// check state of db
			if (Database.getArtistIndex().getIds().size() != 1)
				fail("incorrect # artists");			
			if (Database.getReleaseIndex().getIds().size() != 0)
				fail("incorrect # releases");
			if (Database.getSongIndex().getIds().size() != 2)
				fail("incorrect # songs");	
			
			ArtistProfile artist = Database.getArtistIndex().getArtistProfile(new ArtistIdentifier("aphex twin"));
			if (!artist.containsSong(songProfile1.getUniqueId()))
				fail("artist doesn't contain song");
			
			// rename the artist
			Vector<String> artistNames = new Vector<String>(1);
			artistNames.add("polygon window");
			songProfile1.setArtistNames(artistNames);
			
			// check state of db
			if (Database.getArtistIndex().getIds().size() != 2)
				fail("incorrect # artists");			
			if (Database.getReleaseIndex().getIds().size() != 0)
				fail("incorrect # releases");
			if (Database.getSongIndex().getIds().size() != 2)
				fail("incorrect # songs");	
			
			artist = Database.getArtistIndex().getArtistProfile(new ArtistIdentifier("polygon window"));
			if (!artist.containsSong(songProfile1.getUniqueId()))
				fail("artist doesn't contain song");			
			
			artist = Database.getArtistIndex().getArtistProfile(new ArtistIdentifier("aphex twin"));
			if (artist.containsSong(songProfile1.getUniqueId()))
				fail("artist does contain song");			
					
			// rename the back
			artistNames = new Vector<String>(1);
			artistNames.add("aphex twin");
			songProfile1.setArtistNames(artistNames);
			
			// check state of db
			if (Database.getArtistIndex().getIds().size() != 1)
				fail("incorrect # artists");			
			if (Database.getReleaseIndex().getIds().size() != 0)
				fail("incorrect # releases");
			if (Database.getSongIndex().getIds().size() != 2)
				fail("incorrect # songs");	
			
			artist = Database.getArtistIndex().getArtistProfile(new ArtistIdentifier("aphex twin"));
			if (!artist.containsSong(songProfile1.getUniqueId()))
				fail("artist doesn't contain song");
			
		} catch (Exception e) {
			log.error("testSongUpdateArtist(): error", e);
			fail(e.getMessage());	
		}
	}	

	/**
	 * This method tests updating a song's artist
	 */
	public void testSongAutoMerge() {
		try {
			DuplicateRecordMerger.MERGE_RECORDS_IMMEDIATELY = true; // set for testing purposes
			
			// song 1
			SubmittedSong submittedSong1 = new SubmittedSong("aphex twin", "", "a1", "bike pump", "");

			// song 2
			SubmittedSong submittedSong2 = new SubmittedSong("aphex twin", "", "a1", "bike-pump", "");
			
			// add the song..
			SongProfile songProfile1 = Database.getSongIndex().addSong(submittedSong1);
			Database.getSongIndex().addSong(submittedSong2);
			
			// check state of db
			if (Database.getSongIndex().getIds().size() != 1)
				fail("incorrect # songs");	
			
		} catch (Exception e) {
			log.error("testSongAutoMerge(): error", e);
			fail(e.getMessage());	
		}
	}	
	
	public void testSerialization() {
		try {		
			SongIndex index = new SongIndex();
			
			Map<String,Integer> filenameMap = new HashMap<String,Integer>();
			filenameMap.put("test.mp3", 1);
			index.setFilenameMap(filenameMap);

			UserDataType userType = new UserDataType((byte)1, "title", "descr", (byte)2);
			Vector<UserDataType> userTypes = new Vector<UserDataType>();
			userTypes.add(userType);
			((LocalIMDB)index.getImdb()).setUserDataTypes(userTypes);
			//index.setNextUserDataTypeId((byte)3);
		
			// TODO: recommended model manager
			
			UniqueIdTable uniqueIdTable = new UniqueIdTable(); // maps unique Ids to identifiers and vice versa
			SongIdentifier songId = new SongIdentifier("test song");
		    int songUniqueId = uniqueIdTable.getUniqueIdFromIdentifier(songId);
			SongIdentifier songId2 = new SongIdentifier("test song2");
		    int songUniqueId2 = uniqueIdTable.getUniqueIdFromIdentifier(songId2);
		    ((LocalIMDB)index.getImdb()).setUniqueIdTable(uniqueIdTable);
		    
		    SongRecord songRecord = new SongRecord();
		    songRecord.setId(songId);
		    songRecord.setUniqueId(songUniqueId);

		    SongRecord songRecord2 = new SongRecord();
		    songRecord2.setId(songId2);
		    songRecord2.setUniqueId(songUniqueId2);
		    
			Map<Integer, Record> recordMap = new HashMap<Integer, Record>(); // keeps track of all records, accessed by unique id
			recordMap.put(songUniqueId, songRecord);
			recordMap.put(songUniqueId2, songRecord2);
			((LocalIMDB)index.getImdb()).setRecordMap(recordMap);
			
		    Map<Integer, Record> duplicateIdMap = new HashMap<Integer, Record>(); // keeps track of duplicate ids, is separate so iterators don't pick up duplicatations...
		    duplicateIdMap.put(songUniqueId2, songRecord);
		    ((LocalIMDB)index.getImdb()).setDuplicateIdMap(duplicateIdMap);		    
		    
		    ModelManagerInterface modelManager = new SongModelManager();
		    index.setModelManager(modelManager);
		    int columnCount = modelManager.getNumColumns();

			XMLSerializer.saveData(index, "data/junit/temp/song_index.xml");
			index = (SongIndex)XMLSerializer.readData("data/junit/temp/song_index.xml");

			if (index.getFilenameMap().get("test.mp3") != 1)
				fail();

			if (index.getUserDataTypes().get(0).getId() != 1)
				fail();
			
			//if (index.getNextUserDataTypeId() != (byte)3)
				//fail();
			
			if (((LocalIMDB)index.getImdb()).getUniqueIdTable().getUniqueIdFromIdentifier(songId) != songUniqueId)
				fail("uniqueid table incorrect");
			if (((LocalIMDB)index.getImdb()).getUniqueIdTable().getUniqueIdFromIdentifier(songId2) != songUniqueId2)
				fail("uniqueid table incorrect");
			if (!((LocalIMDB)index.getImdb()).getUniqueIdTable().getIdentifierFromUniqueId(songUniqueId).equals(songId))
				fail("uniqueid table incorrect");
			if (!((LocalIMDB)index.getImdb()).getUniqueIdTable().getIdentifierFromUniqueId(songUniqueId2).equals(songId2))
				fail("uniqueid table incorrect");

			if (!((LocalIMDB)index.getImdb()).getRecordMap().get(songUniqueId).equals(songRecord))
				fail("incorrect record map");
			if (!((LocalIMDB)index.getImdb()).getRecordMap().get(songUniqueId2).equals(songRecord2))
				fail("incorrect record map");

			if (!((LocalIMDB)index.getImdb()).getDuplicateIdMap().get(songUniqueId2).equals(songRecord))
				fail("incorrect duplicate record map");
			
			if (index.getModelManager().getNumColumns() != columnCount)
				fail("incorrect model manager column count");
			
		} catch (Exception e) {
			log.error("testSerialization(): error", e);
			fail(e.getMessage());
		}
	}	

	public void testCustomSerialization() {
		try {
			testSongUpdate();
			LineWriter writer = LineWriterFactory.getLineWriter("data/junit/temp/songindex.jso");			
			Database.getSongIndex().write(writer);
			writer.close();
			LineReader lineReader = LineReaderFactory.getLineReader("data/junit/temp/songindex.jso");
			new SongIndex(lineReader);
			lineReader.close();
		} catch (Exception e) {
			log.error("testCustomSerialization(): error", e);
			fail(e.getMessage());	
		}
	}	
	
}
