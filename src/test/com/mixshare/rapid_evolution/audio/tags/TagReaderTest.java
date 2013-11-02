package test.com.mixshare.rapid_evolution.audio.tags;

import java.util.Vector;

import org.apache.log4j.Logger;
import org.jaudiotagger.tag.TagOptionSingleton;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.audio.tags.TagManager;
import com.mixshare.rapid_evolution.audio.tags.TagReader;
import com.mixshare.rapid_evolution.audio.tags.readers.mp3.JAudioTagReader;
import com.mixshare.rapid_evolution.data.submitted.search.song.SubmittedSong;
import com.mixshare.rapid_evolution.data.util.DegreeValue;
import com.mixshare.rapid_evolution.music.key.Key;

public class TagReaderTest extends RE3TestCase {

	static private Logger log = Logger.getLogger(TagReaderTest.class);

    static {
    	RE3Properties.setPropertyForTest("server_mode", "true");
    	TagOptionSingleton.getInstance().setPadNumbers(RE3Properties.getBoolean("tag_options_pad_numbers"));
    	TagOptionSingleton.getInstance().setUnsyncTags(RE3Properties.getBoolean("tag_write_unsync"));
    	TagOptionSingleton.getInstance().setTruncateTextWithoutErrors(true);
    }

	public void testMP3TagReaders() {
		try {
			RE3Properties.setProperty("preferred_text_case", "none");
			String filename1 = "data/test/audio/songs/freaktimebaby.mp3";
			log.info("testMP3TagReaders(): testing jaudiotagger...");
			testMP3TagReader1(new JAudioTagReader(filename1));

			/*
			log.info("testMP3TagReaders(): testing jid3...");
			testMP3TagReader(new JID3TagReader(filename));

			log.info("testMP3TagReaders(): testing qtid3...");
			testMP3TagReader(new QTID3TagReader(filename));
			*/

			String filename2 = "data/test/audio/songs/tha.mp3";
			log.info("testMP3TagReaders(): testing jaudiotagger...");
			testMP3TagReader2(new JAudioTagReader(filename2));

			String filename3 = "data/test/audio/songs/flat rock-genre_number.mp3";
			log.info("testMP3TagReaders(): testing jaudiotagger...");
			testMP3TagReader3(TagManager.readTags(filename3));

			String filename4 = "data/test/audio/songs/PANTyRAiD_BEBA.mp3";
			log.info("testMP3TagReaders(): testing jaudiotagger...");
			testMP3TagReader4(TagManager.readTags(filename4));

		} catch (Exception e) {
			log.error("testMP3TagReaders(): error", e);
			fail(e.getMessage());
		}
	}

	public void testMP3TagReader1(TagReader tagReader) {
		try {
			// id fields
			if (!tagReader.getArtist().equals("luke vibert"))
				fail("artist incorrect, is=" + tagReader.getArtist());
			if (!tagReader.getAlbum().equals("yoseph"))
				fail("album incorrect, is=" + tagReader.getAlbum());
			if (!tagReader.getTrack().equals("03"))
				fail("track incorrect, is=" + tagReader.getTrack());
			if (!tagReader.getTitle().equals("freaktimebaby"))
				fail("title incorrect, is=" + tagReader.getTitle());
			if (!tagReader.getRemix().equals(""))
				fail("remix incorrect, is=" + tagReader.getRemix());

			if (!tagReader.getGenre().equals("acid"))
				fail("genre incorrect, is=" + tagReader.getGenre());
			if (!tagReader.getYear().equals("2003"))
				fail("year incorrect, is=" + tagReader.getYear());
			if (!tagReader.getPublisher().equals("warp"))
				fail("publisher incorrect, is=" + tagReader.getPublisher());

			if (tagReader.getBpmStart() != 103.8f)
				fail("bpm incorrect, is=" + tagReader.getBpmStart());

			String albumCoverFilename = tagReader.getAlbumCoverFilename();
			if (albumCoverFilename == null)
				fail("null album cover retrieved");

		} catch (Exception e) {
			log.error("testMP3TagReader1(): error", e);
			fail(e.getMessage());
		}
	}


	public void testMP3TagReader2(TagReader tagReader) {
		try {
			// id fields
			if (!tagReader.getArtist().equals("aphex twin"))
				fail("artist incorrect, is=" + tagReader.getArtist());
			if (!tagReader.getAlbum().equals("selected ambient works 85-92"))
				fail("album incorrect, is=" + tagReader.getAlbum());
			if (!tagReader.getTrack().equals("02"))
				fail("track incorrect, is=" + tagReader.getTrack());
			if (!tagReader.getTitle().equals("tha"))
				fail("title incorrect, is=" + tagReader.getTitle());
			if (!tagReader.getRemix().equals(""))
				fail("remix incorrect, is=" + tagReader.getRemix());

			if (!tagReader.getGenre().equals("ambient"))
				fail("genre incorrect, is=" + tagReader.getGenre());
			if (!tagReader.getYear().equals("1993"))
				fail("year incorrect, is=" + tagReader.getYear());
			if (!tagReader.getPublisher().equals("apollo"))
				fail("publisher incorrect, is=" + tagReader.getPublisher());

			if (tagReader.getBpmStart() != 135.0f)
				fail("bpm incorrect, is=" + tagReader.getBpmStart());
			if (tagReader.getBpmEnd() != 0.0f)
				fail("bpm end incorrect, is=" + tagReader.getBpmEnd());
			if (tagReader.getBpmAccuracy() != 0)
				fail("bpm accuracy incorrect, is=" + tagReader.getBpmAccuracy());

			if (!Key.getKey(tagReader.getKeyStart()).equals(Key.getKey("Em")))
				fail("key incorrect, is=" + tagReader.getKeyStart());
			if (!Key.getKey(tagReader.getKeyEnd()).equals(Key.NO_KEY))
				fail("key end incorrect, is=" + tagReader.getKeyEnd());
			if (tagReader.getKeyAccuracy() != 100)
				fail("key accuracy incorrect, is=" + tagReader.getKeyAccuracy());

			if (tagReader.getReplayGain() != -0.84f)
				fail("replay gain incorrect, is=" + tagReader.getReplayGain());

			if (!tagReader.getTimeSignature().equals("4/4"))
				fail("time sig incorrect");
			if (!tagReader.getTime().equals("9:04"))
				fail("time incorrect");

			if (tagReader.getRating().intValue() != 5)
				fail("rating incorrect");

			Vector<DegreeValue> styles = tagReader.getStyles();
			if (styles.size() != 4)
				fail("incorrect # of styles, is=" + styles.size());
			if (!styles.get(0).getName().equals("ambient"))
				fail("style 1 incorrect=" + styles.get(0).getName());
			if (!styles.get(1).getName().equals("ambient, thinking music"))
				fail("style 2 incorrect=" + styles.get(1).getName());
			if (!styles.get(2).getName().equals("braindance"))
				fail("style 3 incorrect=" + styles.get(2).getName());
			if (!styles.get(3).getName().equals("braindance, ambient"))
				fail("style 4 incorrect=" + styles.get(3).getName());

			String albumCoverFilename = tagReader.getAlbumCoverFilename();
			if (albumCoverFilename == null)
				fail("null album cover retrieved");

		} catch (Exception e) {
			log.error("testMP3TagReader2(): error", e);
			fail(e.getMessage());
		}
	}

	public void testMP3TagReader3(SubmittedSong song) {
		try {
			// id fields
			if (!song.getSongIdentifier().getArtistDescription().equals("Wisp"))
				fail("artist incorrect, is=" + song.getSongIdentifier().getArtistDescription());
			if (!song.getRelease().equals("The Shimmering Hour"))
				fail("album incorrect, is=" + song.getRelease());
			if (!song.getSongIdentifier().getSongDescription().equals("Flat Rock"))
				fail("title incorrect, is=" + song.getSongIdentifier().getSongDescription());

			if (!song.getStyleDegreeValues().get(0).getName().equals("Electronic"))
				fail("genre incorrect, is=" + song.getStyleDegreeValues().get(0).getName());
			if (!song.getDuration().toString().equals("3:47"))
				fail("duration incorrect, is=" + song.getDuration().toString());

		} catch (Exception e) {
			log.error("testMP3TagReader3(): error", e);
			fail(e.getMessage());
		}
	}

	public void testMP3TagReader4(SubmittedSong song) {
		try {
			// id fields
			if (!song.getSongIdentifier().getArtistDescription().equals("PANTyRAiD"))
				fail("artist incorrect, is=" + song.getSongIdentifier().getArtistDescription());
			if (!song.getRelease().equals("Beba"))
				fail("album incorrect, is=" + song.getRelease());
			if (!song.getSongIdentifier().getSongDescription().equals("Beba"))
				fail("title incorrect, is=" + song.getSongIdentifier().getSongDescription());
			if (!song.getTrack().equals("01/02"))
				fail("track incorrect, is=" + song.getTrack());

			if (!song.getComments().equals("www.marineparade.net"))
				fail("comments incorrect");

		} catch (Exception e) {
			log.error("testMP3TagReader4(): error", e);
			fail(e.getMessage());
		}
	}

}
