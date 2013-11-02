package test.com.mixshare.rapid_evolution.data.profile.search.song;

import java.util.Vector;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.search.song.MixoutIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.song.SongIdentifier;
import com.mixshare.rapid_evolution.data.profile.search.song.Exclude;
import com.mixshare.rapid_evolution.data.profile.search.song.MixoutProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.data.submitted.search.song.SubmittedMixout;
import com.mixshare.rapid_evolution.data.submitted.search.song.SubmittedSong;
import com.mixshare.rapid_evolution.data.util.io.XMLSerializer;
import com.mixshare.rapid_evolution.music.key.Key;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineReaderFactory;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.mixshare.rapid_evolution.util.io.LineWriterFactory;

public class SongProfileTest extends RE3TestCase {

	public void testSerialization() {
		try {
			SubmittedSong song1 = new SubmittedSong("artist", "", "", "title1", "remix");			
			SubmittedSong song2 = new SubmittedSong("artist2", "", "", "title2", "");
			
			SongProfile songProfile = (SongProfile) Database.add(song1);
			SongProfile otherSongProfile = (SongProfile) Database.add(song2);			
			SongRecord song = songProfile.getSongRecord();			
			
			songProfile.setRatingSource((byte)1);
			
			// song profile specific
			songProfile.setKeySource((byte)2);
			songProfile.setBpmSource((byte)3);
			songProfile.setTimeSigSource((byte)4);
			songProfile.setBeatIntensitySource((byte)5);
			songProfile.setDurationSource((byte)6);
			songProfile.setOriginalYearReleasedSource((byte)7);

			songProfile.setReplayGain(2.5f);
			songProfile.setReplayGainSource((byte)8);
			songProfile.setITunesID("poo");					
			songProfile.setSyncedWithMixshare(true);
		    
			MixoutIdentifier mixoutId = new MixoutIdentifier(song.getUniqueId(), otherSongProfile.getUniqueId());
			SubmittedMixout newMixout = new SubmittedMixout(mixoutId, 0.0f);
			MixoutProfile mixout = (MixoutProfile) Database.add(newMixout);
			songProfile.addMixout(mixout);
			
			Vector<Exclude> excludes = new Vector<Exclude>();
			Exclude exclude = new Exclude(6,7);
			excludes.add(exclude);
			songProfile.setExcludes(excludes);

			songProfile.setLyrics("butterflies");
			songProfile.setLyricsSource((byte)9);
			
			songProfile.setLastDetectKeyAttempt(10);
			songProfile.setLastDetectBpmAttempt(11);
			songProfile.setLastDetectBeatIntensityAttempt(12);
			songProfile.setLastDetectRGAAttempt(13);
						
			// common
			song.setId(new SongIdentifier("aphex twin", "tha"));			
			int expectedId = song.getUniqueId();
			
			int[] dupIds = new int[] { 1, 2 };
			song.setDuplicateIds(dupIds);
			
			song.setRating((byte)80);
			song.setDisabled(true);

			// search record specific
			song.setSourceStyleIds(new int[] { 1 });
			song.setSourceStyleDegrees(new float[] { 0.5f });
			song.setSourceStyleSources(new byte[] { (byte)2 });
			song.setActualStyleIds(new int[] { 1 });
			song.setActualStyleDegrees(new float[] { 0.7f });
						
			song.setSourceTagIds(new int[] { 2 });
			song.setSourceTagDegrees(new float[] { 1.0f });
			song.setSourceTagSources(new byte[] { (byte)1 });
			song.setActualTagIds(new int[] { 2 });
			song.setActualTagDegrees(new float[] { 0.9f });
			
		    song.setScore(70.0f);
		    song.setPopularity(30.0f);

		    song.setComments("comments");
		    
		    song.setThumbnailImageFilename("thumb.gif");

		    song.setUserDataTypes(new short[] { 2 });
		    song.setUserData(new Object[] { (Object)3 });

		    song.setMinedProfileSources(new byte[] { (byte)3 });
		    song.setMinedProfileSourcesLastUpdated(new long[] { 123 });

		    song.setExternalItem(true);
		    
		    song.setPlayCount(34);

		    // song specific
		    song.setTrack("track");
			song.setTitle("title");
			song.setRemix("remix");

			song.setReleaseIds(new int[] { 1 });
			song.setReleaseTracks(new String[] { "a1" });
			
			song.setLabelIds(new int[] { 2 });
			
			song.setStartKeyRootValue(2.0f);
			song.setStartKeyScaleType((byte)1);
			
			song.setEndKeyRootValue(Key.ROOT_UNKNOWN);
			song.setEndKeyScaleType((byte)-1);
			
			song.setKeyAccuracy((byte)52);

			song.setStartBpm(102.3f);
			song.setEndBpm(60.0f);
			song.setBpmAccuracy((byte)42);
			
			song.setTimeSigNumerator((byte)3);
			song.setTimeSigDenominator((byte)4);

			song.setBeatIntensity((byte)87);
			
			song.setTimeInMillis(123456);
			
			song.setSongFilename("test.mp3");
			
			song.setOriginalYearReleased((short)1998);
			
			song.setDateAdded(654321);
			
			song.setNumMixouts((short)102);
			
			song.setLastModified(234);
			
			//checkValues(songProfile, song, expectedId, mixout.getUniqueId());
			
//			XMLSerializer.saveData(songProfile, "data/junit/temp/song_profile.xml");
//			songProfile = (SongProfile)XMLSerializer.readData("data/junit/temp/song_profile.xml");
//			song = songProfile.getSongRecord();			
//
//			checkValues(songProfile, song, expectedId, mixout.getUniqueId());
			
			LineWriter writer = LineWriterFactory.getLineWriter("data/junit/temp/song_profile.xml");
			songProfile.write(writer);
			writer.close();
			LineReader lineReader = LineReaderFactory.getLineReader("data/junit/temp/song_profile.xml");
			songProfile = new SongProfile(lineReader);
			song = songProfile.getSongRecord();	
			
			checkValues(songProfile, song, expectedId, mixout.getUniqueId());
			
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	private void checkValues(SongProfile songProfile, SongRecord song, int expectedId, int expectedMixoutId) {
		if (songProfile.getRatingSource() != (byte)1)
			fail();
		
		// song profile specific
		if (songProfile.getKeySource() != (byte)2)
			fail();
		if (songProfile.getBpmSource() != (byte)3)
			fail();
		if (songProfile.getTimeSigSource() != (byte)4)
			fail();
		if (songProfile.getBeatIntensitySource() != (byte)5)
			fail();
		if (songProfile.getDurationSource() != (byte)6)
			fail();
		if (songProfile.getOriginalYearReleasedSource() != (byte)7)
			fail();

		if (songProfile.getReplayGain() != 2.5f)
			fail();
		if (songProfile.getReplayGainSource() != (byte)8)
			fail();
		if (!songProfile.getITunesID().equals("poo"))
			fail();
		if (!songProfile.isSyncedWithMixshare())
			fail();

		if (songProfile.getMixouts().get(0).getUniqueId() != expectedMixoutId)
			fail();

		if (songProfile.getExcludes().get(0).getFromSongId() != 6)
			fail();
		if (songProfile.getExcludes().get(0).getToSongId() != 7)
			fail();
		
		if (!songProfile.getLyrics().equals("butterflies"))
			fail();
		if (songProfile.getLyricsSource() != (byte)9)
			fail();

		if (songProfile.getLastDetectKeyAttempt() != 10)
			fail();
		if (songProfile.getLastDetectBpmAttempt() != 11)
			fail();
		if (songProfile.getLastDetectBeatIntensityAttempt() != 12)
			fail();
		if (songProfile.getLastDetectRGAAttempt() != 13)
			fail();
		
		// common
		if (!song.getId().equals(new SongIdentifier("aphex twin", "tha")))
			fail("id incorrect");			
		if (song.getUniqueId() != expectedId)
			fail("unique id incorrect");

		if (song.getDuplicateIds()[0] != 1)
			fail("dup id incorrect");
		if (song.getDuplicateIds()[1] != 2)
			fail("dup id incorrect");
		
		if (song.getRating() != (byte)80)
			fail("rating incorrect");
		if (!song.isDisabled())
			fail("not disabled");

		// search record specific
		if (song.getSourceStyleIds()[0] != 1)
			fail();
		if (song.getSourceStyleDegrees()[0] != 0.5f)
			fail();
		if (song.getSourceStyleSources()[0] != (byte)2)
			fail();
		if (song.getActualStyleIds()[0] != 1)
			fail();
		if (song.getActualStyleDegrees()[0] != 0.7f)
			fail();

		if (song.getSourceTagIds()[0] != 2)
			fail();
		if (song.getSourceTagDegrees()[0] != 1.0f)
			fail();
		if (song.getSourceTagSources()[0] != (byte)1)
			fail();
		if (song.getActualTagIds()[0] != 2)
			fail();
		if (song.getActualTagDegrees()[0] != 0.9f)
			fail();
		
		if (song.getScore() != 70.0f)
			fail();
		if (song.getPopularity() != 30.0f)
			fail();
			
		if (!song.getComments().equals("comments"))
			fail();
		if (!song.getThumbnailImageFilename().equals("thumb.gif"))
			fail();
		
		if (song.getUserDataTypes()[0] != (byte)2)
			fail();
		if (!song.getUserData()[0].toString().equals("3"))				
			fail("is=" + song.getUserData()[0]);

		if (song.getMinedProfileSources()[0] != (byte)3)
			fail();
		if (song.getMinedProfileSourcesLastUpdated()[0] != 123)
			fail();

		if (!song.isExternalItem())
			fail();
		
		if (song.getPlayCount() != 34)
			fail();
	    
		// song specific			
		if (!song.getTrack().equals("track"))
			fail("track incorrect");
		if (!song.getTitle().equals("title"))
			fail("title incorrect");
		if (!song.getRemix().equals("remix"))
			fail("remix incorrect");
		
		if (song.getReleaseIds()[0] != 1)
			fail("release ids incorrect");
		if (!song.getReleaseTracks()[0].equals("a1"))
			fail("release tracks incorrect");

		if (song.getLabelIds()[0] != 2)
			fail("label ids incorrect");
		
		if (song.getStartKeyRootValue() != 2.0f)
			fail("start key root incorrect");
		if (song.getStartKeyScaleType() != (byte)1)
			fail("start key root incorrect");
		if (!song.getStartKey().isValid())
			fail("start key incorrect");
		
		if (song.getEndKeyRootValue() != Key.ROOT_UNKNOWN)
			fail("end key root incorrect");
		if (song.getEndKeyScaleType() != (byte)-1)
			fail("end key root incorrect");
		if (song.getEndKey().isValid())
			fail("end key incorrect");
		
		if (song.getKeyAccuracy() != (byte)52)
			fail("incorrect key accuracy");

		if (song.getStartBpm() != 102.3f)
			fail("bpm incorrect");
		if (song.getEndBpm() != 60.0f)
			fail("bpm incorrect");
		if (song.getBpmAccuracy() != (byte)42)
			fail("bpm accuracy incorrect");
		
		if (song.getTimeSigNumerator() != (byte)3)
			fail();
		if (song.getTimeSigDenominator() != (byte)4)
			fail();
		
		if (song.getBeatIntensity() != (byte)87)
			fail();

		if (song.getTimeInMillis() != 123456)
			fail();
		
		if (!song.getSongFilename().equals("test.mp3"))
			fail();

		if (song.getOriginalYearReleased() != (short)1998)
			fail();
		
		if (song.getDateAdded() != 654321)
			fail();

		if (song.getNumMixouts() != (short)102)
			fail("# mixouts incorrect, is=" + song.getNumMixouts());

		if (song.getLastModified() != 234)
			fail("last modified incorrect, is=" + song.getLastModified());		
	}
	
}
