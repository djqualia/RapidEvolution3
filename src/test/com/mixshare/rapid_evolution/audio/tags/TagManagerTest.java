package test.com.mixshare.rapid_evolution.audio.tags;

import java.io.File;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jaudiotagger.tag.TagOptionSingleton;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.audio.tags.TagConstants;
import com.mixshare.rapid_evolution.audio.tags.TagManager;
import com.mixshare.rapid_evolution.audio.tags.TagWriter;
import com.mixshare.rapid_evolution.audio.tags.TagWriterFactory;
import com.mixshare.rapid_evolution.audio.tags.readers.ogg.JAudioOggTagReader;
import com.mixshare.rapid_evolution.audio.tags.writers.ogg.JAudioOggTagWriter;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.record.user.UserData;
import com.mixshare.rapid_evolution.data.submitted.search.song.SubmittedSong;
import com.mixshare.rapid_evolution.data.util.DegreeValue;
import com.mixshare.rapid_evolution.music.beatintensity.BeatIntensity;
import com.mixshare.rapid_evolution.music.bpm.Bpm;
import com.mixshare.rapid_evolution.music.duration.Duration;
import com.mixshare.rapid_evolution.music.key.Key;
import com.mixshare.rapid_evolution.music.rating.Rating;
import com.mixshare.rapid_evolution.music.timesig.TimeSig;
import com.mixshare.rapid_evolution.util.FileUtil;

public class TagManagerTest extends RE3TestCase implements TagConstants {

	static private Logger log = Logger.getLogger(TagReaderTest.class);

    static {
    	RE3Properties.setPropertyForTest("server_mode", "true");
    	TagOptionSingleton.getInstance().setPadNumbers(RE3Properties.getBoolean("tag_options_pad_numbers"));
    	TagOptionSingleton.getInstance().setUnsyncTags(RE3Properties.getBoolean("tag_write_unsync"));
    	TagOptionSingleton.getInstance().setTruncateTextWithoutErrors(true);
    }

    @Override
    public void setUp() throws Exception {
    	super.setUp();
		RE3Properties.setPropertyForTest("tag_write_custom_fields", "true");
    }

	public void testTagManagerRead() {
		try {
			RE3Properties.setProperty("preferred_text_case", "none");
			Database.getSongIndex().addUserDataType("custom field 1", (byte)1);

			String filename = "data/test/audio/songs/tagged.ogg";

			Vector<DegreeValue> testStyles = new Vector<DegreeValue>();
			testStyles.add(new DegreeValue("ambient", 1.0f, DATA_SOURCE_FILE_TAGS));
			testStyles.add(new DegreeValue("idm", 0.6f, DATA_SOURCE_FILE_TAGS));

			Vector<DegreeValue> testTags = new Vector<DegreeValue>();
			testTags.add(new DegreeValue("aphex twin", 1.0f, DATA_SOURCE_FILE_TAGS));
			testTags.add(new DegreeValue("rephlex", 0.6f, DATA_SOURCE_FILE_TAGS));

			SubmittedSong submittedSong = TagManager.readTags(filename);

			if (!submittedSong.getRelease().equals("some Album"))
				fail("album incorrect, is=" + submittedSong.getRelease());
			//if (submittedSong.getThumbnailImageFilename() == null)
				//fail("null album cover");
			if (!submittedSong.getSongIdentifier().getArtistDescription().equals("some Artist"))
				fail("artist incorrect, is=" + submittedSong.getSongIdentifier().getArtistDescription());
			if (submittedSong.getBeatIntensity().getBeatIntensityValue() != 82)
				fail("incorrect beat intensity=" + submittedSong.getBeatIntensity());
			if (submittedSong.getStartBpm().getBpmValue() != 136.2f)
				fail("incorrect bpm start=" + submittedSong.getStartBpm());
			if (submittedSong.getEndBpm().getBpmValue() != 72.5f)
				fail("incorrect bpm end=" + submittedSong.getEndBpm());
			if (submittedSong.getBpmAccuracy() != 62)
				fail("incorrect bpm accuracy");
			if (!submittedSong.getComments().equals("some Comments"))
				fail("incorrect comments");
			if (!submittedSong.getStartKey().equals(Key.getKey("Em")))
				fail("key incorrect, is=" + submittedSong.getStartKey());
			if (!submittedSong.getEndKey().equals(Key.getKey("Bm")))
				fail("key end incorrect, is=" + submittedSong.getEndKey());
			if (submittedSong.getKeyAccuracy() != 43)
				fail("key accuracy incorrect, is=" + submittedSong.getKeyAccuracy());
			if (!submittedSong.getLyrics().equals("lyrics man!"))
				fail("incorrect lyrics=" + submittedSong.getLyrics());
			if (submittedSong.getRating().getRatingStars() != 4)
				fail("incorrect rating=" + submittedSong.getRating());
			if (!submittedSong.getLabelNames().get(0).equals("some Label"))
				fail("incorrect label");
			if (!submittedSong.getRemix().equals("some Remix"))
				fail("incorrect remix");
			if (submittedSong.getReplayGain() != 3.2f)
				fail("incorrect replay gain");
			if (submittedSong.getStyleDegreeValues().size() != testStyles.size())
				fail("incorrect styles size, is=" + submittedSong.getStyleDegreeValues().size() + ", styles=" + submittedSong.getStyleDegreeValues());
			for (int s = 0; s < testStyles.size(); ++s) {
				if (!submittedSong.getStyleDegreeValues().get(s).getName().equals(testStyles.get(s).getName()))
					fail("style incorrect=" + submittedSong.getStyleDegreeValues().get(s).getName());
				if (submittedSong.getStyleDegreeValues().get(s).getPercentage() != testStyles.get(s).getPercentage())
					fail("style degree incorrect=" + submittedSong.getStyleDegreeValues().get(s).getPercentage());
			}
			if (submittedSong.getTagDegreeValues().size() != testTags.size())
				fail("incorrect tags size, is=" + submittedSong.getTagDegreeValues().size() + ", tags=" + submittedSong.getTagDegreeValues());
			for (int s = 0; s < testTags.size(); ++s) {
				if (!submittedSong.getTagDegreeValues().get(s).getName().equals(testTags.get(s).getName()))
					fail("tag incorrect=" + submittedSong.getTagDegreeValues().get(s).getName());
				if (submittedSong.getTagDegreeValues().get(s).getPercentage() != testTags.get(s).getPercentage())
					fail("tag degree incorrect=" + submittedSong.getTagDegreeValues().get(s).getPercentage());
			}
			if (!submittedSong.getDuration().toString().equals("4:20"))
				fail("time incorrect");
			if (!submittedSong.getTimeSig().toString().equals("8/8"))
				fail("time sig incorrect");
			if (!submittedSong.getTitle().equals("some Title"))
				fail("title incorrect");
			if (!submittedSong.getTrack().equals("04/10"))
				fail("incorrect track, is=" + submittedSong.getTrack());
			if (submittedSong.getOriginalYearReleased() != 1992)
				fail("incorrect year, is=" + submittedSong.getOriginalYearReleased());
			if (!submittedSong.getUserData().get(0).getData().toString().equals("custom value"))
				fail("incorrect custom value");

		} catch (Exception e) {
			log.error("testTagManagerRead(): error", e);
			fail(e.getMessage());
		}
	}

	public void testTagManagerReadFilenameParse() {
		try {
			RE3Properties.setProperty("preferred_text_case", "none");
			RE3Properties.setProperty("tag_reading_parse_title", "true");
			RE3Properties.setProperty("use_filename_as_title_if_necessary", "true");

			String filename = "data/test/audio/songs/parseTests/squarepusher - square window - [03] - ultravisitor (qualia remix).wav";

			// there should be no tag information, so the artist/album/track/title/remix should be parsed from the filename

			SubmittedSong submittedSong = TagManager.readTags(filename);

			if (!submittedSong.getRelease().equals("square window"))
				fail("album incorrect, is=" + submittedSong.getRelease());
			if (!submittedSong.getSongIdentifier().getArtistDescription().equals("squarepusher"))
				fail("artist incorrect, is=" + submittedSong.getSongIdentifier().getArtistDescription());
			if (!submittedSong.getTitle().equals("ultravisitor"))
				fail("title incorrect, is=" + submittedSong.getTitle());
			if (!submittedSong.getTrack().equals("03"))
				fail("incorrect track, is=" + submittedSong.getTrack());
			if (!submittedSong.getRemix().equals("qualia remix"))
				fail("incorrect remix");

			filename = "data/test/audio/songs/parseTests/squarepusher - square window - ultravisitor (qualia remix).wav";
			submittedSong = TagManager.readTags(filename);
			if (!submittedSong.getRelease().equals("square window"))
				fail("album incorrect, is=" + submittedSong.getRelease());
			if (!submittedSong.getSongIdentifier().getArtistDescription().equals("squarepusher"))
				fail("artist incorrect, is=" + submittedSong.getSongIdentifier().getArtistDescription());
			if (!submittedSong.getTitle().equals("ultravisitor"))
				fail("title incorrect, is=" + submittedSong.getTitle());
			if (!submittedSong.getTrack().equals(""))
				fail("incorrect track, is=" + submittedSong.getTrack());
			if (!submittedSong.getRemix().equals("qualia remix"))
				fail("incorrect remix");

			filename = "data/test/audio/songs/parseTests/squarepusher - ultravisitor (qualia remix).wav";
			submittedSong = TagManager.readTags(filename);
			if (!submittedSong.getRelease().equals(""))
				fail("album incorrect, is=" + submittedSong.getRelease());
			if (!submittedSong.getSongIdentifier().getArtistDescription().equals("squarepusher"))
				fail("artist incorrect, is=" + submittedSong.getSongIdentifier().getArtistDescription());
			if (!submittedSong.getTitle().equals("ultravisitor"))
				fail("title incorrect, is=" + submittedSong.getTitle());
			if (!submittedSong.getTrack().equals(""))
				fail("incorrect track, is=" + submittedSong.getTrack());
			if (!submittedSong.getRemix().equals("qualia remix"))
				fail("incorrect remix");

			filename = "data/test/audio/songs/parseTests/squarepusher - ultravisitor.wav";
			submittedSong = TagManager.readTags(filename);
			if (!submittedSong.getRelease().equals(""))
				fail("album incorrect, is=" + submittedSong.getRelease());
			if (!submittedSong.getSongIdentifier().getArtistDescription().equals("squarepusher"))
				fail("artist incorrect, is=" + submittedSong.getSongIdentifier().getArtistDescription());
			if (!submittedSong.getTitle().equals("ultravisitor"))
				fail("title incorrect, is=" + submittedSong.getTitle());
			if (!submittedSong.getTrack().equals(""))
				fail("incorrect track, is=" + submittedSong.getTrack());
			if (!submittedSong.getRemix().equals(""))
				fail("incorrect remix");

			filename = "data/test/audio/songs/parseTests/squarepusher - [03] - ultravisitor.wav";
			submittedSong = TagManager.readTags(filename);
			if (!submittedSong.getRelease().equals(""))
				fail("album incorrect, is=" + submittedSong.getRelease());
			if (!submittedSong.getSongIdentifier().getArtistDescription().equals("squarepusher"))
				fail("artist incorrect, is=" + submittedSong.getSongIdentifier().getArtistDescription());
			if (!submittedSong.getTitle().equals("ultravisitor"))
				fail("title incorrect, is=" + submittedSong.getTitle());
			if (!submittedSong.getTrack().equals("03"))
				fail("incorrect track, is=" + submittedSong.getTrack());
			if (!submittedSong.getRemix().equals(""))
				fail("incorrect remix");

			filename = "data/test/audio/songs/parseTests/13 wisp - the fire above.wav";
			submittedSong = TagManager.readTags(filename);
			if (!submittedSong.getRelease().equals(""))
				fail("album incorrect, is=" + submittedSong.getRelease());
			if (!submittedSong.getSongIdentifier().getArtistDescription().equals("wisp"))
				fail("artist incorrect, is=" + submittedSong.getSongIdentifier().getArtistDescription());
			if (!submittedSong.getTitle().equals("the fire above"))
				fail("title incorrect, is=" + submittedSong.getTitle());
			if (!submittedSong.getTrack().equals("13"))
				fail("incorrect track, is=" + submittedSong.getTrack());
			if (!submittedSong.getRemix().equals(""))
				fail("incorrect remix");

			filename = "data/test/audio/songs/parseTests/01 No Music (remixxxed).wav";
			submittedSong = TagManager.readTags(filename);
			if (!submittedSong.getRelease().equals(""))
				fail("album incorrect, is=" + submittedSong.getRelease());
			if (!submittedSong.getSongIdentifier().getArtistDescription().equals(""))
				fail("artist incorrect, is=" + submittedSong.getSongIdentifier().getArtistDescription());
			if (!submittedSong.getTitle().equals("No Music"))
				fail("title incorrect, is=" + submittedSong.getTitle());
			if (!submittedSong.getTrack().equals("01"))
				fail("incorrect track, is=" + submittedSong.getTrack());
			if (!submittedSong.getRemix().equals("remixxxed"))
				fail("incorrect remix");

		} catch (Exception e) {
			log.error("testTagManagerReadFilenameParse(): error", e);
			fail(e.getMessage());
		}
	}

	public void testTagManagerReadDoubleRemix() {
		try {
			RE3Properties.setProperty("preferred_text_case", "none");

			// create a new temp file to play with
			String sourceFilename = "data/test/audio/songs/backup/freaktimebaby.mp3";
			String targetFilename = "data/test/audio/songs/temp/freaktimebaby.mp3";
			File targetFile = new File(targetFilename);
			if (targetFile.exists())
				targetFile.delete();
			FileUtil.copy(sourceFilename, targetFilename);

			TagWriter tagWriter = TagWriterFactory.getTagWriter(targetFilename);
			tagWriter.setRemix("some remix");
			tagWriter.setTitle("some title (some remix)");
			tagWriter.setAlbum("");
			tagWriter.setTrack("", null);
			tagWriter.setArtist("some artist");
			tagWriter.save();

			SubmittedSong submittedSong = TagManager.readTags(targetFilename);

			if (!submittedSong.getSongIdentifier().toString().equals("some artist - some title (some remix)"))
				fail("song id is incorrect=" + submittedSong.getSongIdentifier());

		} catch (Exception e) {
			log.error("testTagManagerReadDoubleRemix(): error", e);
			fail(e.getMessage());
		}
	}

	public void testTagManagerWrite() {
		try {
			RE3Properties.setPropertyForTest("preferred_text_case", "none");
			RE3Properties.setPropertyForTest("tag_write_remix_with_title", "false");
			Database.getSongIndex().addUserDataType("custom field 1", (byte)1);

			SubmittedSong song = new SubmittedSong("some Artist", "some Album", "04/10", "some Title", "some Remix");
			SongProfile songProfile = (SongProfile)Database.getSongIndex().add(song);

			Vector<DegreeValue> testStyles = new Vector<DegreeValue>();
			testStyles.add(new DegreeValue("ambient", 1.0f, DATA_SOURCE_FILE_TAGS));
			testStyles.add(new DegreeValue("idm", 0.6f, DATA_SOURCE_FILE_TAGS));

			Vector<DegreeValue> testTags = new Vector<DegreeValue>();
			testTags.add(new DegreeValue("aphex twin", 1.0f, DATA_SOURCE_FILE_TAGS));
			testTags.add(new DegreeValue("rephlex", 0.6f, DATA_SOURCE_FILE_TAGS));

			TagWriterTest.setupStyles2();
			TagWriterTest.setupTags2();

			songProfile.setThumbnailImageFilename("data/test/images/albumcovers/aphex twin_selected ambient works 85-92.jpg", DATA_SOURCE_FILE_TAGS);
			songProfile.setBeatIntensity(BeatIntensity.getBeatIntensity(82), DATA_SOURCE_FILE_TAGS);
			songProfile.setBpm(new Bpm(136.2f), new Bpm(72.5f), (byte)62, DATA_SOURCE_FILE_TAGS);
			songProfile.setComments("some Comments", DATA_SOURCE_FILE_TAGS);
			songProfile.setKey(Key.getKey("Em"), Key.getKey("Bm"), (byte)43, DATA_SOURCE_FILE_TAGS);
			songProfile.setLyrics("lyrics man!", DATA_SOURCE_FILE_TAGS);
			songProfile.setRating(Rating.getRating(4 * 20), DATA_SOURCE_FILE_TAGS);
			songProfile.setLabelName("some Label");
			songProfile.setReplayGain(3.2f, DATA_SOURCE_FILE_TAGS);
			songProfile.setStyles(testStyles);
			songProfile.setTags(testTags);
			songProfile.setDuration(new Duration("4:20"), DATA_SOURCE_FILE_TAGS);
			songProfile.setTimeSig(TimeSig.getTimeSig("8/8"), DATA_SOURCE_FILE_TAGS);
			songProfile.setOriginalYearReleased((short)1992, DATA_SOURCE_FILE_TAGS);
			songProfile.setUserData(new UserData(Database.getSongIndex().getUserDataTypes().get(0), "custom value"));
			songProfile.save();

			// create a new temp file to play with
			String sourceFilename = "data/test/audio/songs/backup/jeroensnake_-_Echo_Guitar_Jeroen.ogg";
			String targetFilename = "data/test/audio/songs/temp/jeroensnake_-_Echo_Guitar_Jeroen.ogg";
			File targetFile = new File(targetFilename);
			if (targetFile.exists())
				targetFile.delete();
			FileUtil.copy(sourceFilename, targetFilename);

			songProfile.setSongFilename(targetFilename);

			TagManager.writeTags(songProfile);

			// now check the file directly at the tag level
			TagWriterTest.testTagWriterResults(new JAudioOggTagReader(targetFilename));


		} catch (Exception e) {
			log.error("testTagManagerWrite(): error", e);
			fail(e.getMessage());
		}
	}

	public void testTagManagerWriteWithKeyOptions() {
		try {
			RE3Properties.setProperty("preferred_text_case", "none");
			RE3Properties.setProperty("tag_write_remix_with_title", "false");

			RE3Properties.setProperty("tag_write_key_to_comments", "true");
			RE3Properties.setProperty("tag_write_key_to_title", "true");
			RE3Properties.setProperty("tag_write_key_codes", "true");

			Database.getSongIndex().addUserDataType("custom field 1", (byte)1);

			SubmittedSong song = new SubmittedSong("some Artist", "some Album", "04/10", "some Title", "some Remix");
			SongProfile songProfile = (SongProfile)Database.getSongIndex().add(song);

			Vector<DegreeValue> testStyles = new Vector<DegreeValue>();
			testStyles.add(new DegreeValue("ambient", 1.0f, DATA_SOURCE_FILE_TAGS));
			testStyles.add(new DegreeValue("idm", 0.6f, DATA_SOURCE_FILE_TAGS));

			Vector<DegreeValue> testTags = new Vector<DegreeValue>();
			testTags.add(new DegreeValue("aphex twin", 1.0f, DATA_SOURCE_FILE_TAGS));
			testTags.add(new DegreeValue("rephlex", 0.6f, DATA_SOURCE_FILE_TAGS));

			TagWriterTest.setupStyles2();
			TagWriterTest.setupTags2();

			songProfile.setThumbnailImageFilename("data/test/images/albumcovers/aphex twin_selected ambient works 85-92.jpg", DATA_SOURCE_FILE_TAGS);
			songProfile.setBeatIntensity(BeatIntensity.getBeatIntensity(82), DATA_SOURCE_FILE_TAGS);
			songProfile.setBpm(new Bpm(136.2f), new Bpm(72.5f), (byte)62, DATA_SOURCE_FILE_TAGS);
			songProfile.setComments("some Comments", DATA_SOURCE_FILE_TAGS);
			songProfile.setKey(Key.getKey("Em"), Key.getKey("Bm"), (byte)43, DATA_SOURCE_FILE_TAGS);
			songProfile.setLyrics("lyrics man!", DATA_SOURCE_FILE_TAGS);
			songProfile.setRating(Rating.getRating(4 * 20), DATA_SOURCE_FILE_TAGS);
			songProfile.setLabelName("some Label");
			songProfile.setReplayGain(3.2f, DATA_SOURCE_FILE_TAGS);
			songProfile.setStyles(testStyles);
			songProfile.setTags(testTags);
			songProfile.setDuration(new Duration("4:20"), DATA_SOURCE_FILE_TAGS);
			songProfile.setTimeSig(TimeSig.getTimeSig("8/8"), DATA_SOURCE_FILE_TAGS);
			songProfile.setOriginalYearReleased((short)1992, DATA_SOURCE_FILE_TAGS);
			songProfile.setUserData(new UserData(Database.getSongIndex().getUserDataTypes().get(0), "custom value"));
			songProfile.save();

			// create a new temp file to play with
			String sourceFilename = "data/test/audio/songs/backup/jeroensnake_-_Echo_Guitar_Jeroen.ogg";
			String targetFilename = "data/test/audio/songs/temp/jeroensnake_-_Echo_Guitar_Jeroen.ogg";
			File targetFile = new File(targetFilename);
			if (targetFile.exists())
				targetFile.delete();
			FileUtil.copy(sourceFilename, targetFilename);

			songProfile.setSongFilename(targetFilename);

			TagManager.writeTags(songProfile);

			// now check the file directly at the tag level
			TagWriterTest.testTagWriterResultsKeyOptions(new JAudioOggTagReader(targetFilename));

			SubmittedSong submittedSong = TagManager.readTags(targetFilename);
			if (!submittedSong.getTitle().equals("some Title"))
				fail("incorrect title=" + submittedSong.getTitle());
			if (!submittedSong.getComments().equals("some Comments"))
				fail("incorrect comments=" + submittedSong.getComments());

			JAudioOggTagWriter tagWriter = new JAudioOggTagWriter(targetFilename, TAG_MODE_UPDATE);
			tagWriter.setComments("Em - my Comments");
			tagWriter.setTitle("Em - my Title");
			tagWriter.setKey("");
			tagWriter.setKeyStart("");
			tagWriter.setKeyEnd("");
			tagWriter.save();

			submittedSong = TagManager.readTags(targetFilename);
			if (!submittedSong.getTitle().equals("my Title"))
				fail("incorrect title=" + submittedSong.getTitle());
			if (!submittedSong.getComments().equals("my Comments"))
				fail("incorrect comments=" + submittedSong.getComments());
			if (!submittedSong.getStartKey().equals(Key.getKey("Em")))
				fail("incorrect start key incorrect=" + submittedSong.getStartKey());

		} catch (Exception e) {
			log.error("testTagManagerWriteWithKeyOptions(): error", e);
			fail(e.getMessage());
		}
	}

}
