package test.com.mixshare.rapid_evolution.data.index.search.release;

import java.util.Vector;

import org.apache.log4j.Logger;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.search.artist.ArtistIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.label.LabelIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.release.ReleaseIdentifier;
import com.mixshare.rapid_evolution.data.index.search.release.ReleaseIndex;
import com.mixshare.rapid_evolution.data.profile.search.artist.ArtistProfile;
import com.mixshare.rapid_evolution.data.profile.search.label.LabelProfile;
import com.mixshare.rapid_evolution.data.profile.search.release.ReleaseProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.submitted.search.song.SubmittedSong;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineReaderFactory;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.mixshare.rapid_evolution.util.io.LineWriterFactory;

public class ReleaseIndexTest extends RE3TestCase {

    static private Logger log = Logger.getLogger(ReleaseIndexTest.class);    
	
	public void testDuplicateReleases() {
		try {
			// song 1
			SubmittedSong submittedSong1 = new SubmittedSong("aphex twin", "selected ambient works", "d1", "heliosphere", "");
			submittedSong1.setLabelName("warp");
						
			// song 2
			SubmittedSong submittedSong2 = new SubmittedSong("aphex twin", "selected ambient works lp", "d2", "tha", "");
			submittedSong2.setLabelName("warp");
			
			// add both songs
			SongProfile songProfile1 = Database.getSongIndex().addSong(submittedSong1);
			SongProfile songProfile2 = Database.getSongIndex().addSong(submittedSong2);
			
			// check state of db
			if (Database.getArtistIndex().getIds().size() != 1) 
				fail("incorrect # artists");
			if (Database.getLabelIndex().getIds().size() != 1)
				fail("incorrect # labels");			
			if (Database.getReleaseIndex().getIds().size() != 2)
				fail("incorrect # releases");
			if (Database.getSongIndex().getIds().size() != 2)
				fail("incorrect # songs");			
			
			// get the release profiles and merge them
			ReleaseProfile releaseProfile1 = Database.getReleaseIndex().getReleaseProfile(submittedSong1.getSubmittedRelease().getReleaseIdentifier());
			ReleaseProfile releaseProfile2 = Database.getReleaseIndex().getReleaseProfile(submittedSong2.getSubmittedRelease().getReleaseIdentifier());
			
			Database.mergeProfiles(releaseProfile1, releaseProfile2);
			
			// check the state of the db
			if (Database.getArtistIndex().getIds().size() != 1)
				fail("incorrect # artists");
			if (Database.getLabelIndex().getIds().size() != 1)
				fail("incorrect # labels");			
			if (Database.getReleaseIndex().getIds().size() != 1)
				fail("incorrect # releases");
			if (Database.getSongIndex().getIds().size() != 2)
				fail("incorrect # songs");				
			
			// get each song and make sure the release descriptions are updated
			songProfile1 = (SongProfile)Database.getProfile(submittedSong1.getSongIdentifier());			
			songProfile2 = (SongProfile)Database.getProfile(submittedSong2.getSongIdentifier());
			
			if (!songProfile1.getReleases().get(0).toString().equals("aphex twin - selected ambient works"))
				fail("incorrect release description");
			if (!songProfile2.getReleases().get(0).toString().equals("aphex twin - selected ambient works"))
				fail("incorrect release description");

			// make sure the song's descriptions contain the correct album
			if (!songProfile1.toString().equals("aphex twin - selected ambient works  [d1]  heliosphere"))
				fail("song tostring incorrect");
			if (!songProfile2.toString().equals("aphex twin - selected ambient works  [d2]  tha"))
				fail("song tostring incorrect, is=" + songProfile2.toString());
			
			// make sure the new primary release contains both songs
			releaseProfile1 = (ReleaseProfile)Database.getProfile(submittedSong1.getSubmittedRelease().getReleaseIdentifier());
			releaseProfile2 = (ReleaseProfile)Database.getProfile(submittedSong2.getSubmittedRelease().getReleaseIdentifier());
			if (!releaseProfile1.getIdentifier().equals(releaseProfile2.getIdentifier()))
				fail("old release being returned");
			
			if (!releaseProfile1.containsSong(submittedSong1.getSongIdentifier()))
				fail("release does not contain song");
			if (!releaseProfile1.containsSong(submittedSong2.getSongIdentifier()))
				fail("release does not contain song");
			
			if (!releaseProfile1.toString().equals("aphex twin - selected ambient works"))
				fail("release profile tostring incorrect");
			if (!releaseProfile2.toString().equals("aphex twin - selected ambient works"))
				fail("release profile tostring incorrect");						
			
		} catch (Exception e) {
			log.error("testDuplicateReleases(): error", e);
			fail(e.getMessage());			
		}		
	}	
	
	/**
	 * This method tests updating a release's title and artists
	 */
	public void testReleaseUpdate() {
		try {
			// song 1
			SubmittedSong submittedSong1 = new SubmittedSong("aphex twin", "selected ambient works", "d1", "heliosphere", "");
			submittedSong1.setLabelName("warp");

			// song 2 (added to ensure "aphex twin" doesn't get deleted at a later step)
			SubmittedSong submittedSong2 = new SubmittedSong("aphex twin", "", "", "another random song", "");
			
			// add the songs (which indirectly creates teh release)
			SongProfile songProfile1 = Database.getSongIndex().addSong(submittedSong1);
			Database.add(submittedSong2);
			if (!songProfile1.toString().equals("aphex twin - selected ambient works  [d1]  heliosphere"))
				fail("incorrect song id");

			ReleaseProfile releaseProfile = Database.getReleaseIndex().getReleaseProfile(submittedSong1.getSubmittedRelease().getReleaseIdentifier());
			if (!releaseProfile.getReleaseTitle().equals("selected ambient works"))
				fail("incorrect release title");
			if (!releaseProfile.getArtistsDescription().equals("aphex twin"))
				fail("incorrect artist description");

			ArtistProfile artistProfile = Database.getArtistIndex().getArtistProfile(new ArtistIdentifier("aphex twin"));
			if (!artistProfile.containsRelease(releaseProfile.getUniqueId()))
				fail("artist does not contain the release");
			
			LabelProfile labelProfile = Database.getLabelIndex().getLabelProfile(new LabelIdentifier("warp"));
			if (!labelProfile.containsRelease(releaseProfile.getUniqueId()))
				fail("label does not contain the release");						
			
			// check state of db
			if (Database.getLabelIndex().getIds().size() != 1) 
				fail("incorrect # labels");
			if (Database.getArtistIndex().getIds().size() != 1)
				fail("incorrect # artists");			
			if (Database.getReleaseIndex().getIds().size() != 1)
				fail("incorrect # releases");
			if (Database.getSongIndex().getIds().size() != 2)
				fail("incorrect # songs");	
			
			// now change the release's title
			releaseProfile.setReleaseTitle("selected ambient works volume 1");

			// check state of db
			if (Database.getLabelIndex().getIds().size() != 1) 
				fail("incorrect # labels");
			if (Database.getArtistIndex().getIds().size() != 1)
				fail("incorrect # artists");			
			if (Database.getReleaseIndex().getIds().size() != 1)
				fail("incorrect # releases");
			if (Database.getSongIndex().getIds().size() != 2)
				fail("incorrect # songs");	

			// ensure the new label names shows up in all the related profiles
			if (!releaseProfile.getReleaseTitle().equals("selected ambient works volume 1"))
				fail("incorrect release title");
			if (!releaseProfile.getReleaseIdentifier().getReleaseTitle().equals("selected ambient works volume 1"))
				fail("release identifier title not updated");
			if (!releaseProfile.getArtistsDescription().equals("aphex twin"))
				fail("artist description for release is incorrect");
			
			if (!labelProfile.containsRelease(releaseProfile.getUniqueId()))
				fail("label does not contain the release");						

			if (!artistProfile.containsRelease(releaseProfile.getUniqueId()))
				fail("artist does not contain the release");

			if (!songProfile1.toString().equals("aphex twin - selected ambient works volume 1  [d1]  heliosphere"))
				fail("incorrect song id, is=" + songProfile1.toString());

			if (Database.getReleaseIndex().getRecord(releaseProfile.getReleaseIdentifier()) == null)
				fail("couldn't find by new release id");
			if (Database.getReleaseIndex().getRecord(releaseProfile.getUniqueId()) == null)
				fail("couldn't find by unique id");			
			
			// now change the release's artists
			// NOTE: this shouldn't rename the artist (that is done differently), but set a new artist on the release
			// in this case, "afx" is a different artist than "aphex twin"
			Vector<String> newArtistNames = new Vector<String>(1);
			newArtistNames.add("afx");
			releaseProfile.setArtistNames(newArtistNames);
			
			// check state of db
			if (Database.getArtistIndex().getIds().size() != 2)
				fail("incorrect # artists, is=" + Database.getArtistIndex().getIds().size());			
			if (Database.getLabelIndex().getIds().size() != 1) 
				fail("incorrect # labels");
			if (Database.getReleaseIndex().getIds().size() != 1)
				fail("incorrect # releases, is=" + Database.getReleaseIndex().getIds().size());
			if (Database.getSongIndex().getIds().size() != 2)
				fail("incorrect # songs");	
			
			// ensure the new release title/artists shows up properly in all the related profiles
			if (!releaseProfile.getReleaseTitle().equals("selected ambient works volume 1"))
				fail("incorrect release title");
			if (!releaseProfile.getReleaseIdentifier().getReleaseTitle().equals("selected ambient works volume 1"))
				fail("release identifier title not updated");
			if (!releaseProfile.getArtistsDescription().equals("afx"))
				fail("artist description for release is incorrect");
			
			if (!labelProfile.containsRelease(releaseProfile.getUniqueId()))
				fail("label does not contain the release");						

			// make sure the "aphex twin" artist doesn't contain the release any more
			if (artistProfile.containsRelease(releaseProfile.getUniqueId()))
				fail("old artist contains the release");
			if (artistProfile.getNumReleases() != 0)
				fail("incorrect # releases on artist");
			if (artistProfile.getNumSongs() != 1)
				fail("incorrect # releases on artist");

			// check that the "afx" artist now contains the release
			artistProfile = Database.getArtistIndex().getArtistProfile(new ArtistIdentifier("afx"));
			if (!artistProfile.containsRelease(releaseProfile.getUniqueId()))
				fail("artist does not contain the release");
			if (artistProfile.getNumReleases() != 1)
				fail("incorrect # releases on artist");

			if (!songProfile1.toString().equals("afx - selected ambient works volume 1  [d1]  heliosphere"))
				fail("incorrect song id, is=" + songProfile1.toString());
			if (songProfile1.getReleases().size() != 1)
				fail("incorrect # releases on song");

			if (Database.getReleaseIndex().getRecord(releaseProfile.getReleaseIdentifier()) == null)
				fail("couldn't find by new release id");
			if (Database.getReleaseIndex().getRecord(releaseProfile.getUniqueId()) == null)
				fail("couldn't find by unique id");	
			
			if (releaseProfile.getNumSongs() != 1)
				fail("incorrect # songs, is=" + releaseProfile.getNumSongs());
			if (!releaseProfile.containsSong(songProfile1.getUniqueId()))
				fail("incorrect song membership for release");
			if (!releaseProfile.containsSong(songProfile1.getIdentifier()))
				fail("incorrect song membership for release");
			
			// now let's change the label
			Vector<String> newLabelNames = new Vector<String>(1);
			newLabelNames.add("warp records");
			releaseProfile.setLabelNames(newLabelNames);
			
			// check state of db
			if (Database.getArtistIndex().getIds().size() != 2)
				fail("incorrect # artists, is=" + Database.getArtistIndex().getIds().size());			
			if (Database.getLabelIndex().getIds().size() != 1) 
				fail("incorrect # labels");
			if (Database.getReleaseIndex().getIds().size() != 1)
				fail("incorrect # releases, is=" + Database.getReleaseIndex().getIds().size());
			if (Database.getSongIndex().getIds().size() != 2)
				fail("incorrect # songs");	
			
			labelProfile = Database.getLabelIndex().getLabelProfile(new LabelIdentifier("warp records"));
			if (!labelProfile.containsRelease(releaseProfile.getUniqueId()))
				fail("label does not contain the release");						
			if (!labelProfile.containsSong(songProfile1.getUniqueId()))
				fail("label does not contain the release");						
			
			
		} catch (Exception e) {
			log.error("testReleaseUpdate(): error", e);
			fail(e.getMessage());	
		}
	}	
	
	/**
	 * This tests a particular scenario where the user adds an album with 2 different artists, and then merges them.
	 * In this case, the album is: "jona, claude vonstroke - someone ep".
	 * 
	 * Each song has a different artist, and at the time the songs are added the code
	 * is unable to determine the full artist description for the release (which will happen).
	 * For example, the 2 songs are added in order:
	 * "jona - someone ep [a1] someone"
	 * "claude vonstroke - someone ep [b1] someone (flylikeaneaglemix)"
	 * 
	 * In this case, 2 releases will be created "jona - someone ep" and "claude vonstroke - someone ep"
	 * NOTE: it's not possible to automatically identify and merge the artists, because there are situations
	 * where releases share the same title, but released by different artists (i.e. "boards of canada - peel sessions"
	 * and "aphex twin - peel sessions" are different releases, and it would be wrong to combine their songs into one
	 * release).
	 * 
	 * So, in this case the user or code will have to go and merge the 2 "someone ep" releases at a later point.
	 * This scenario is tested below, and the end result should be a single release with 2 artists...
	 */
	public void testReleaseMerge() {
		try {
			// song 1
			SubmittedSong submittedSong1 = new SubmittedSong("jona", "someone ep", "a1", "someone", "");
			submittedSong1.setLabelName("strobe music");

			SubmittedSong submittedSong2 = new SubmittedSong("claude vonstroke", "someone ep", "b1", "someone", "flylikeaneaglemix");
			submittedSong2.setLabelName("strobe music");
			
			// add the songs
			SongProfile songProfile1 = Database.getSongIndex().addSong(submittedSong1);
			SongProfile songProfile2 = Database.getSongIndex().addSong(submittedSong2);

			// check state of db
			if (Database.getLabelIndex().getIds().size() != 1) 
				fail("incorrect # labels");
			if (Database.getArtistIndex().getIds().size() != 2)
				fail("incorrect # artists");			
			if (Database.getReleaseIndex().getIds().size() != 2)
				fail("incorrect # releases");
			if (Database.getSongIndex().getIds().size() != 2)
				fail("incorrect # songs");	
			
			ReleaseProfile releaseProfile1 = Database.getReleaseIndex().getReleaseProfile(new ReleaseIdentifier("jona", "someone ep"));
			ReleaseProfile releaseProfile2 = Database.getReleaseIndex().getReleaseProfile(new ReleaseIdentifier("claude vonstroke", "someone ep"));

			if (!releaseProfile1.containsSong(songProfile1.getUniqueId()))
				fail("release doesn't contain song");
			if (!releaseProfile2.containsSong(songProfile2.getUniqueId()))
				fail("release doesn't contain song");
			
			// let's merge the releases
			Database.mergeProfiles(releaseProfile1, releaseProfile2);
			
			// check state of db
			if (Database.getLabelIndex().getIds().size() != 1) 
				fail("incorrect # labels");
			if (Database.getArtistIndex().getIds().size() != 2)
				fail("incorrect # artists");			
			if (Database.getReleaseIndex().getIds().size() != 1)
				fail("incorrect # releases");
			if (Database.getSongIndex().getIds().size() != 2)
				fail("incorrect # songs");	

			ReleaseProfile combinedRelease = Database.getReleaseIndex().getReleaseProfile(new ReleaseIdentifier(new String[] { "jona", "claude vonstroke" }, "someone ep"));
			if (combinedRelease == null)
				fail("combined release not found");
			if (!combinedRelease.containsSong(songProfile1.getUniqueId()))
				fail("release doesn't contain song");
			if (!combinedRelease.containsSong(songProfile2.getUniqueId()))
				fail("release doesn't contain song");
			if (!combinedRelease.toString().equals("claude vonstroke; jona - someone ep"))
				fail("release toString incorrect, is=" + combinedRelease.toString());

			// this should pick up the same release (i.e. the ordering of artists in identifier shouldn't matter)
			combinedRelease = Database.getReleaseIndex().getReleaseProfile(new ReleaseIdentifier(new String[] { "claude vonstroke", "jona" }, "someone ep"));			
			if (combinedRelease == null)
				fail("combined release not found");
			if (!combinedRelease.toString().equals("claude vonstroke; jona - someone ep"))
				fail("release toString incorrect");
			
			if (songProfile1.getReleases().size() != 1)
				fail("incorrect # releases on song");
			if (songProfile1.getReleaseInstances().size() != 1)
				fail("incorrect # releases on song");
			if (songProfile1.getSongIdentifier().getNumArtists() != 1)
				fail("incorrect # artists on song");
			if (!songProfile1.toString().equals("jona - someone ep  [a1]  someone"))
				fail("incorret song toString");
			
			if (songProfile2.getReleases().size() != 1)
				fail("incorrect # releases on song");
			if (songProfile2.getReleaseInstances().size() != 1)
				fail("incorrect # releases on song");
			if (songProfile2.getSongIdentifier().getNumArtists() != 1)
				fail("incorrect # artists on song");
			if (!songProfile2.toString().equals("claude vonstroke - someone ep  [b1]  someone (flylikeaneaglemix)"))
				fail("incorret song toString");
			
			// now let's make the release a "compilation"
			combinedRelease.setIsCompilationRelease(true);
			
			// check state of db
			if (Database.getLabelIndex().getIds().size() != 1) 
				fail("incorrect # labels");
			if (Database.getArtistIndex().getIds().size() != 2)
				fail("incorrect # artists");			
			if (Database.getReleaseIndex().getIds().size() != 1)
				fail("incorrect # releases");
			if (Database.getSongIndex().getIds().size() != 2)
				fail("incorrect # songs");	

			combinedRelease = Database.getReleaseIndex().getReleaseProfile(new ReleaseIdentifier("someone ep"));
			if (combinedRelease == null)
				fail("combined release not found");
			if (!combinedRelease.containsSong(songProfile1.getUniqueId()))
				fail("release doesn't contain song");
			if (!combinedRelease.containsSong(songProfile2.getUniqueId()))
				fail("release doesn't contain song");
			if (!combinedRelease.toString().equalsIgnoreCase("various - someone ep"))
				fail("release toString incorrect, is=" + combinedRelease.toString());
			
			if (songProfile1.getReleases().size() != 1)
				fail("incorrect # releases on song");
			if (songProfile1.getReleaseInstances().size() != 1)
				fail("incorrect # releases on song");
			if (songProfile1.getSongIdentifier().getNumArtists() != 1)
				fail("incorrect # artists on song");
			if (!songProfile1.toString().equals("jona - someone ep  [a1]  someone"))
				fail("incorret song toString");
			
			if (songProfile2.getReleases().size() != 1)
				fail("incorrect # releases on song");
			if (songProfile2.getReleaseInstances().size() != 1)
				fail("incorrect # releases on song");
			if (songProfile2.getSongIdentifier().getNumArtists() != 1)
				fail("incorrect # artists on song");
			if (!songProfile2.toString().equals("claude vonstroke - someone ep  [b1]  someone (flylikeaneaglemix)"))
				fail("incorret song toString");			
			
			// now let's make the release NOT a "compilation" (should restore previous state)
			combinedRelease.setIsCompilationRelease(false);
			
			// check state of db
			if (Database.getLabelIndex().getIds().size() != 1) 
				fail("incorrect # labels");
			if (Database.getArtistIndex().getIds().size() != 2)
				fail("incorrect # artists");			
			if (Database.getReleaseIndex().getIds().size() != 1)
				fail("incorrect # releases");
			if (Database.getSongIndex().getIds().size() != 2)
				fail("incorrect # songs");	

			combinedRelease = Database.getReleaseIndex().getReleaseProfile(new ReleaseIdentifier(new String[] { "jona", "claude vonstroke" }, "someone ep"));
			if (combinedRelease == null)
				fail("combined release not found");
			if (!combinedRelease.containsSong(songProfile1.getUniqueId()))
				fail("release doesn't contain song");
			if (!combinedRelease.containsSong(songProfile2.getUniqueId()))
				fail("release doesn't contain song");
			if (!combinedRelease.toString().equals("claude vonstroke; jona - someone ep"))
				fail("release toString incorrect");

			// this should pick up the same release (i.e. the ordering of artists in identifier shouldn't matter)
			combinedRelease = Database.getReleaseIndex().getReleaseProfile(new ReleaseIdentifier(new String[] { "claude vonstroke", "jona" }, "someone ep"));			
			if (combinedRelease == null)
				fail("combined release not found");
			if (!combinedRelease.toString().equals("claude vonstroke; jona - someone ep"))
				fail("release toString incorrect");
			
			if (songProfile1.getReleases().size() != 1)
				fail("incorrect # releases on song");
			if (songProfile1.getReleaseInstances().size() != 1)
				fail("incorrect # releases on song");
			if (songProfile1.getSongIdentifier().getNumArtists() != 1)
				fail("incorrect # artists on song");
			if (!songProfile1.toString().equals("jona - someone ep  [a1]  someone"))
				fail("incorret song toString");
			
			if (songProfile2.getReleases().size() != 1)
				fail("incorrect # releases on song");
			if (songProfile2.getReleaseInstances().size() != 1)
				fail("incorrect # releases on song");
			if (songProfile2.getSongIdentifier().getNumArtists() != 1)
				fail("incorrect # artists on song");
			if (!songProfile2.toString().equals("claude vonstroke - someone ep  [b1]  someone (flylikeaneaglemix)"))
				fail("incorret song toString");			
			
		} catch (Exception e) {
			log.error("testReleaseMerge(): error", e);
			fail(e.getMessage());	
		}
	}

	/**
	 * This method tests a specific scenario encountered in testing...
	 */
	public void testReleaseMergeIndirect() {
		try {
			// song 1
			SubmittedSong submittedSong1 = new SubmittedSong("1.8.7.", "quality rolls", "", "song1", "");

			// song 2 (added to ensure "aphex twin" doesn't get deleted at a later step)
			SubmittedSong submittedSong2 = new SubmittedSong("1.8.7.", "quality rolls", "", "song2", "");
			
			// add the songs (which indirectly creates teh release)
			SongProfile songProfile1 = Database.getSongIndex().addSong(submittedSong1);
			SongProfile songProfile2 = Database.getSongIndex().addSong(submittedSong2);

			// check state of db
			if (Database.getArtistIndex().getIds().size() != 1)
				fail("incorrect # artists");			
			if (Database.getReleaseIndex().getIds().size() != 1)
				fail("incorrect # releases, is=" + Database.getReleaseIndex().getIds().size());
			if (Database.getSongIndex().getIds().size() != 2)
				fail("incorrect # songs");	
			
			// set the release on the song
			songProfile2.setReleaseTitle("quality rolls2");
			
			// check state of db
			if (Database.getArtistIndex().getIds().size() != 1)
				fail("incorrect # artists");			
			if (Database.getReleaseIndex().getIds().size() != 2)
				fail("incorrect # releases");
			if (Database.getSongIndex().getIds().size() != 2)
				fail("incorrect # songs");	
						
			ReleaseProfile releaseProfile1 = Database.getReleaseIndex().getReleaseProfile(new ReleaseIdentifier("1.8.7.", "quality rolls"));
			ReleaseProfile releaseProfile2 = Database.getReleaseIndex().getReleaseProfile(new ReleaseIdentifier("1.8.7.", "quality rolls2"));			
			
			// merge the releases indirectly
			releaseProfile2.setReleaseTitle("quality rolls", true);
			
			// check state of db
			if (Database.getArtistIndex().getIds().size() != 1)
				fail("incorrect # artists");			
			if (Database.getReleaseIndex().getIds().size() != 1)
				fail("incorrect # releases, is=" + Database.getReleaseIndex().getIds().size());
			if (Database.getSongIndex().getIds().size() != 2)
				fail("incorrect # songs");				

			// make sure both releases are accessible by their respective ids
			releaseProfile1 = Database.getReleaseIndex().getReleaseProfile(new ReleaseIdentifier("1.8.7.", "quality rolls"));
			releaseProfile2 = Database.getReleaseIndex().getReleaseProfile(new ReleaseIdentifier("1.8.7.", "quality rolls2"));
			
			if (releaseProfile2 == null)
				fail("couldn't access release from duplicate id");
			
			if (!releaseProfile1.equals(releaseProfile2))
				fail("merge failed");			
			
		} catch (Exception e) {
			log.error("testReleaseUpdate(): error", e);
			fail(e.getMessage());	
		}
	}	

	public void testReleaseMergeDuplicateArtistsA() {
		try {
			// song 1
			SubmittedSong submittedSong1 = new SubmittedSong("ace of clubs", "benefist", "01", "song1", "");

			SubmittedSong submittedSong2 = new SubmittedSong("ace of clubs, the", "benefist", "02", "song2", "");
			
			// add the songs
			SongProfile songProfile1 = Database.getSongIndex().addSong(submittedSong1);
			SongProfile songProfile2 = Database.getSongIndex().addSong(submittedSong2);

			// check state of db
			if (Database.getArtistIndex().getIds().size() != 2)
				fail("incorrect # artists");			
			if (Database.getReleaseIndex().getIds().size() != 2)
				fail("incorrect # releases");
			if (Database.getSongIndex().getIds().size() != 2)
				fail("incorrect # songs");	
			
			ReleaseProfile releaseProfile1 = Database.getReleaseIndex().getReleaseProfile(new ReleaseIdentifier("ace of clubs", "benefist"));
			ReleaseProfile releaseProfile2 = Database.getReleaseIndex().getReleaseProfile(new ReleaseIdentifier("ace of clubs, the", "benefist"));

			if (!releaseProfile1.containsSong(songProfile1.getUniqueId()))
				fail("release doesn't contain song");
			if (!releaseProfile2.containsSong(songProfile2.getUniqueId()))
				fail("release doesn't contain song");
			
			// let's merge the releases
			log.info("testReleaseMergeDuplicateArtistsA(): merging releases...");
			Database.mergeProfiles(releaseProfile1, releaseProfile2);
			
			// check state of db
			if (Database.getArtistIndex().getIds().size() != 2)
				fail("incorrect # artists");			
			if (Database.getReleaseIndex().getIds().size() != 1)
				fail("incorrect # releases");
			if (Database.getSongIndex().getIds().size() != 2)
				fail("incorrect # songs");	

			ReleaseProfile combinedRelease = Database.getReleaseIndex().getReleaseProfile(new ReleaseIdentifier(new String[] { "ace of clubs", "ace of clubs, the" }, "benefist"));
			if (combinedRelease == null)
				fail("combined release not found");
			if (!combinedRelease.containsSong(songProfile1.getUniqueId()))
				fail("release doesn't contain song");
			if (!combinedRelease.containsSong(songProfile2.getUniqueId()))
				fail("release doesn't contain song");
			if (!combinedRelease.toString().equals("ace of clubs; ace of clubs, the - benefist"))
				fail("release toString incorrect, is=" + combinedRelease.toString());
			
			if (songProfile1.getReleases().size() != 1)
				fail("incorrect # releases on song");
			if (songProfile1.getReleaseInstances().size() != 1)
				fail("incorrect # releases on song");
			if (songProfile1.getSongIdentifier().getNumArtists() != 1)
				fail("incorrect # artists on song");
			if (!songProfile1.toString().equals("ace of clubs - benefist  [01]  song1"))
				fail("incorret song toString");
			
			if (songProfile2.getReleases().size() != 1)
				fail("incorrect # releases on song");
			if (songProfile2.getReleaseInstances().size() != 1)
				fail("incorrect # releases on song");
			if (songProfile2.getSongIdentifier().getNumArtists() != 1)
				fail("incorrect # artists on song");
			if (!songProfile2.toString().equals("ace of clubs, the - benefist  [02]  song2"))
				fail("incorret song toString");
			
			// now let's merge the artists (user realizes they're the same)
			log.info("testReleaseMergeDuplicateArtistsA(): merging artists...");
			ArtistProfile artist1 = Database.getArtistIndex().getArtistProfile(new ArtistIdentifier("ace of clubs"));
			ArtistProfile artist2 = Database.getArtistIndex().getArtistProfile(new ArtistIdentifier("ace of clubs, the"));
			Database.mergeProfiles(artist1, artist2);
						
			// check state of db
			if (Database.getArtistIndex().getIds().size() != 1)
				fail("incorrect # artists");			
			if (Database.getReleaseIndex().getIds().size() != 1)
				fail("incorrect # releases, is=" + Database.getReleaseIndex().getIds().size());
			if (Database.getSongIndex().getIds().size() != 2)
				fail("incorrect # songs");	
			
			combinedRelease = Database.getReleaseIndex().getReleaseProfile(new ReleaseIdentifier(new String[] { "ace of clubs" }, "benefist"));
			if (combinedRelease == null)
				fail("combined release not found");
			if (!combinedRelease.containsSong(songProfile1.getUniqueId()))
				fail("release doesn't contain song");
			if (!combinedRelease.containsSong(songProfile2.getUniqueId()))
				fail("release doesn't contain song");
			if (!combinedRelease.toString().equals("ace of clubs - benefist"))
				fail("release toString incorrect");
			
			if (songProfile1.getReleases().size() != 1)
				fail("incorrect # releases on song");
			if (songProfile1.getReleaseInstances().size() != 1)
				fail("incorrect # releases on song");
			if (songProfile1.getSongIdentifier().getNumArtists() != 1)
				fail("incorrect # artists on song");
			if (!songProfile1.toString().equals("ace of clubs - benefist  [01]  song1"))
				fail("incorret song toString");
			
			if (songProfile2.getReleases().size() != 1)
				fail("incorrect # releases on song");
			if (songProfile2.getReleaseInstances().size() != 1)
				fail("incorrect # releases on song, is=" + songProfile2.getReleaseInstances().size() + ", releasesInstances=" + songProfile2.getReleaseInstances());
			if (songProfile2.getSongIdentifier().getNumArtists() != 1)
				fail("incorrect # artists on song");
			if (!songProfile2.toString().equals("ace of clubs - benefist  [02]  song2"))
				fail("incorrect song toString, is=" + songProfile2);			
			
		} catch (Exception e) {
			log.error("testReleaseMergeDuplicateArtistsA(): error", e);
			fail(e.getMessage());	
		}
	}	
	
	public void testReleaseMergeDuplicateArtistsB() {
		try {
			// song 1
			SubmittedSong submittedSong1 = new SubmittedSong("ace of clubs", "benefist", "01", "song1", "");

			SubmittedSong submittedSong2 = new SubmittedSong("ace of clubs, the", "benefist", "02", "song2", "");
			
			// add the songs
			SongProfile songProfile1 = Database.getSongIndex().addSong(submittedSong1);
			SongProfile songProfile2 = Database.getSongIndex().addSong(submittedSong2);
			
			// merging the artists first this time
			log.info("testReleaseMergeDuplicateArtistsB(): merging artists...");
			ArtistProfile artist1 = Database.getArtistIndex().getArtistProfile(new ArtistIdentifier("ace of clubs"));
			ArtistProfile artist2 = Database.getArtistIndex().getArtistProfile(new ArtistIdentifier("ace of clubs, the"));
			Database.mergeProfiles(artist1, artist2);
			
			// check state of db
			if (Database.getArtistIndex().getIds().size() != 1)
				fail("incorrect # artists");			
			if (Database.getReleaseIndex().getIds().size() != 1)
				fail("incorrect # releases, is=" + Database.getReleaseIndex().getIds().size());
			if (Database.getSongIndex().getIds().size() != 2)
				fail("incorrect # songs");	
			
			ReleaseProfile combinedRelease = Database.getReleaseIndex().getReleaseProfile(new ReleaseIdentifier(new String[] { "ace of clubs" }, "benefist"));
			if (combinedRelease == null)
				fail("combined release not found");
			if (!combinedRelease.containsSong(songProfile1.getUniqueId()))
				fail("release doesn't contain song");
			if (!combinedRelease.containsSong(songProfile2.getUniqueId()))
				fail("release doesn't contain song");
			if (!combinedRelease.toString().equals("ace of clubs - benefist"))
				fail("release toString incorrect");
			
			if (songProfile1.getReleases().size() != 1)
				fail("incorrect # releases on song");
			if (songProfile1.getReleaseInstances().size() != 1)
				fail("incorrect # releases on song");
			if (songProfile1.getSongIdentifier().getNumArtists() != 1)
				fail("incorrect # artists on song");
			if (!songProfile1.toString().equals("ace of clubs - benefist  [01]  song1"))
				fail("incorret song toString");
			
			if (songProfile2.getReleases().size() != 1)
				fail("incorrect # releases on song");
			if (songProfile2.getReleaseInstances().size() != 1)
				fail("incorrect # releases on song, is=" + songProfile2.getReleaseInstances().size() + ", releasesInstances=" + songProfile2.getReleaseInstances());
			if (songProfile2.getSongIdentifier().getNumArtists() != 1)
				fail("incorrect # artists on song");
			if (!songProfile2.toString().equals("ace of clubs - benefist  [02]  song2"))
				fail("incorret song toString");			
			
		} catch (Exception e) {
			log.error("testReleaseMergeDuplicateArtistsB(): error", e);
			fail(e.getMessage());	
		}
	}		

	public void testReleaseMergeDuplicateArtistsC() {
		try {
			// song 1
			SubmittedSong submittedSong1 = new SubmittedSong("ace of clubs", "benefist", "01", "song1", "");

			SubmittedSong submittedSong2 = new SubmittedSong("ace of clubs, the", "benefist", "01", "song1", "");
			
			// add the songs
			SongProfile songProfile1 = Database.getSongIndex().addSong(submittedSong1);
			SongProfile songProfile2 = Database.getSongIndex().addSong(submittedSong2);
			
			// check state of db
			if (Database.getArtistIndex().getIds().size() != 2)
				fail("incorrect # artists");			
			if (Database.getReleaseIndex().getIds().size() != 2)
				fail("incorrect # releases");
			if (Database.getSongIndex().getIds().size() != 2)
				fail("incorrect # songs");	
			
			// merging the artists
			log.info("testReleaseMergeDuplicateArtistsC(): merging artists...");
			ArtistProfile artist1 = Database.getArtistIndex().getArtistProfile(new ArtistIdentifier("ace of clubs"));
			ArtistProfile artist2 = Database.getArtistIndex().getArtistProfile(new ArtistIdentifier("ace of clubs, the"));
			Database.mergeProfiles(artist1, artist2);
			
			// check state of db
			if (Database.getArtistIndex().getIds().size() != 1)
				fail("incorrect # artists");			
			if (Database.getReleaseIndex().getIds().size() != 1)
				fail("incorrect # releases, is=" + Database.getReleaseIndex().getIds().size());
			if (Database.getSongIndex().getIds().size() != 1)
				fail("incorrect # songs, is=" + Database.getSongIndex().getIds().size());	
			
			ReleaseProfile combinedRelease = Database.getReleaseIndex().getReleaseProfile(new ReleaseIdentifier(new String[] { "ace of clubs" }, "benefist"));
			if (combinedRelease == null)
				fail("combined release not found");
			if (!combinedRelease.containsSong(songProfile1.getUniqueId()))
				fail("release doesn't contain song");
			if (!combinedRelease.toString().equals("ace of clubs - benefist"))
				fail("release toString incorrect");
			
			if (songProfile1.getReleases().size() != 1)
				fail("incorrect # releases on song");
			if (songProfile1.getReleaseInstances().size() != 1)
				fail("incorrect # releases on song");
			if (songProfile1.getSongIdentifier().getNumArtists() != 1)
				fail("incorrect # artists on song");
			if (!songProfile1.toString().equals("ace of clubs - benefist  [01]  song1"))
				fail("incorret song toString");
			
		} catch (Exception e) {
			log.error("testReleaseMergeDuplicateArtistsC(): error", e);
			fail(e.getMessage());	
		}
	}
	
	public void testReleaseMergeOnSongs() {
		try {
			// song 1
			SubmittedSong submittedSong1 = new SubmittedSong("ace of clubs", "benefist", "01", "", "");

			SubmittedSong submittedSong2 = new SubmittedSong("ace of clubs", "beneffist", "01", "", "");
			
			// add the songs
			SongProfile songProfile1 = Database.getSongIndex().addSong(submittedSong1);
			SongProfile songProfile2 = Database.getSongIndex().addSong(submittedSong2);
			
			// check state of db
			if (Database.getArtistIndex().getIds().size() != 1)
				fail("incorrect # artists");			
			if (Database.getReleaseIndex().getIds().size() != 2)
				fail("incorrect # releases");
			if (Database.getSongIndex().getIds().size() != 2)
				fail("incorrect # songs");	
			
			// merge the releases
			log.info("testReleaseMergeOnSongs(): merging artists...");
			ReleaseProfile release1 = Database.getReleaseIndex().getReleaseProfile(new ReleaseIdentifier("ace of clubs", "benefist"));
			ReleaseProfile release2 = Database.getReleaseIndex().getReleaseProfile(new ReleaseIdentifier("ace of clubs", "beneffist"));
			Database.mergeProfiles(release1, release2);
			
			// check state of db
			if (Database.getArtistIndex().getIds().size() != 1)
				fail("incorrect # artists");			
			if (Database.getReleaseIndex().getIds().size() != 1)
				fail("incorrect # releases, is=" + Database.getReleaseIndex().getIds().size());
			if (Database.getSongIndex().getIds().size() != 1)
				fail("incorrect # songs, is=" + Database.getSongIndex().getIds().size());	
			
			ReleaseProfile combinedRelease = Database.getReleaseIndex().getReleaseProfile(new ReleaseIdentifier(new String[] { "ace of clubs" }, "benefist"));
			if (combinedRelease == null)
				fail("combined release not found");
			if (!combinedRelease.containsSong(songProfile1.getUniqueId()))
				fail("release doesn't contain song");
			if (!combinedRelease.toString().equals("ace of clubs - benefist"))
				fail("release toString incorrect");
			
			if (songProfile1.getReleases().size() != 1)
				fail("incorrect # releases on song");
			if (songProfile1.getReleaseInstances().size() != 1)
				fail("incorrect # releases on song");
			if (songProfile1.getSongIdentifier().getNumArtists() != 1)
				fail("incorrect # artists on song");
			if (!songProfile1.toString().equals("ace of clubs - benefist  [01]"))
				fail("incorret song toString");
			
		} catch (Exception e) {
			log.error("testReleaseMergeOnSongs(): error", e);
			fail(e.getMessage());	
		}
	}

	public void testReleaseUpdateOnSongs() {
		try {
			// song 1
			SubmittedSong submittedSong1 = new SubmittedSong("ace of clubs", "beneffist", "01", "", "");
			
			// add the songs
			SongProfile songProfile1 = Database.getSongIndex().addSong(submittedSong1);
			
			// update the release's title
			log.info("testReleaseMergeOnSongs(): merging artists...");
			ReleaseProfile release1 = Database.getReleaseIndex().getReleaseProfile(new ReleaseIdentifier("ace of clubs", "beneffist"));
			release1.setReleaseTitle("benefist");
			
			// check state of db
			if (Database.getArtistIndex().getIds().size() != 1)
				fail("incorrect # artists");			
			if (Database.getReleaseIndex().getIds().size() != 1)
				fail("incorrect # releases, is=" + Database.getReleaseIndex().getIds().size());
			if (Database.getSongIndex().getIds().size() != 1)
				fail("incorrect # songs, is=" + Database.getSongIndex().getIds().size());	
			
			// check and make sure release title propagates to song description
			if (songProfile1.getReleases().size() != 1)
				fail("incorrect # releases on song");
			if (songProfile1.getReleaseInstances().size() != 1)
				fail("incorrect # releases on song");
			if (!songProfile1.toString().equals("ace of clubs - benefist  [01]"))
				fail("incorret song toString");
			
		} catch (Exception e) {
			log.error("testReleaseMergeOnSongs(): error", e);
			fail(e.getMessage());	
		}
	}
	
	public void testCustomSerialization() {
		try {
			testReleaseUpdate();
			LineWriter writer = LineWriterFactory.getLineWriter("data/junit/temp/releaseindex.jso");			
			Database.getReleaseIndex().write(writer);
			writer.close();
			LineReader lineReader = LineReaderFactory.getLineReader("data/junit/temp/releaseindex.jso");
			new ReleaseIndex(lineReader);
			lineReader.close();
		} catch (Exception e) {
			log.error("testCustomSerialization(): error", e);
			fail(e.getMessage());	
		}
	}
	
}
