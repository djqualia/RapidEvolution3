package test.com.mixshare.rapid_evolution.audio.tags;

import java.io.File;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jaudiotagger.tag.TagOptionSingleton;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.audio.tags.TagConstants;
import com.mixshare.rapid_evolution.audio.tags.TagReader;
import com.mixshare.rapid_evolution.audio.tags.TagWriter;
import com.mixshare.rapid_evolution.audio.tags.readers.asf.JAudioASFTagReader;
import com.mixshare.rapid_evolution.audio.tags.readers.flac.JAudioFlacTagReader;
import com.mixshare.rapid_evolution.audio.tags.readers.mp3.JAudioTagReader;
import com.mixshare.rapid_evolution.audio.tags.readers.mp4.JAudioMP4TagReader;
import com.mixshare.rapid_evolution.audio.tags.readers.ogg.JAudioOggTagReader;
import com.mixshare.rapid_evolution.audio.tags.writers.asf.JAudioASFTagWriter;
import com.mixshare.rapid_evolution.audio.tags.writers.flac.JAudioFlacTagWriter;
import com.mixshare.rapid_evolution.audio.tags.writers.mp3.JAudioTagWriter;
import com.mixshare.rapid_evolution.audio.tags.writers.mp4.JAudioMP4TagWriter;
import com.mixshare.rapid_evolution.audio.tags.writers.ogg.JAudioOggTagWriter;
import com.mixshare.rapid_evolution.data.util.DegreeValue;
import com.mixshare.rapid_evolution.music.key.Key;
import com.mixshare.rapid_evolution.util.FileUtil;

public class TagWriterTest extends RE3TestCase implements TagConstants {

	static private Logger log = Logger.getLogger(TagWriterTest.class);	

    static {
    	TagOptionSingleton.getInstance().setPadNumbers(RE3Properties.getBoolean("tag_options_pad_numbers"));
    	TagOptionSingleton.getInstance().setUnsyncTags(RE3Properties.getBoolean("tag_write_unsync"));
    	TagOptionSingleton.getInstance().setTruncateTextWithoutErrors(true);
    }
	
    static private Vector<DegreeValue> testStyles = new Vector<DegreeValue>();
    static private Vector<DegreeValue> testTags = new Vector<DegreeValue>();
	
    static public void setupStyles1() {
		testStyles.clear();
		testStyles.add(new DegreeValue("ambient", 1.0f, DATA_SOURCE_FILE_TAGS));
		testStyles.add(new DegreeValue("idm", 0.6f, DATA_SOURCE_FILE_TAGS));
		testStyles.add(new DegreeValue("downtempo", 0.2f, DATA_SOURCE_FILE_TAGS));
	}

    static public void setupStyles2() {
		testStyles.clear();
		testStyles.add(new DegreeValue("ambient", 1.0f, DATA_SOURCE_FILE_TAGS));
		testStyles.add(new DegreeValue("idm", 0.6f, DATA_SOURCE_FILE_TAGS));
	}

    static public void setupTags1() {
		testTags.clear();
		testTags.add(new DegreeValue("aphex twin", 1.0f, DATA_SOURCE_FILE_TAGS));
		testTags.add(new DegreeValue("rephlex", 0.6f, DATA_SOURCE_FILE_TAGS));
		testTags.add(new DegreeValue("ambient", 0.2f, DATA_SOURCE_FILE_TAGS));
	}

    static public void setupTags2() {
		testTags.clear();
		testTags.add(new DegreeValue("aphex twin", 1.0f, DATA_SOURCE_FILE_TAGS));
		testTags.add(new DegreeValue("rephlex", 0.6f, DATA_SOURCE_FILE_TAGS));
	}
	
    public void setUp() throws Exception {
    	super.setUp();
    	copyTestFileToUserLibrary("data/test/images/albumcovers/aphex twin_selected ambient works 85-92.jpg");
    }
    
	public void testMP3TagWriters() {
		try {
			// create a new temp file to play with
			String sourceFilename = "data/test/audio/songs/backup/tha.mp3";
			String targetFilename = "data/test/audio/songs/temp/tha.mp3";			
			File targetFile = new File(targetFilename);
			if (targetFile.exists())
				targetFile.delete();			
			FileUtil.copy(sourceFilename, targetFilename);
			
			setupStyles1();
			setupTags1();
			
			log.info("testMP3TagWriters(): testing jaudiotagger...");
			testTagWriter(new JAudioTagWriter(targetFilename, TAG_MODE_UPDATE, ID3_V_2_4));
			testTagWriterResultsMP3(new JAudioTagReader(targetFilename));

			targetFile.delete();
			FileUtil.copy(sourceFilename, targetFilename);
			setupStyles2();
			setupTags2();
			
			log.info("testMP3TagWriters(): testing jaudiotagger...");
			testTagWriter(new JAudioTagWriter(targetFilename, TAG_MODE_UPDATE, ID3_V_2_4));
			testTagWriterResultsMP3(new JAudioTagReader(targetFilename));
			
		} catch (Exception e) {
			log.error("testMP3TagWriters(): error", e);
			fail(e.getMessage());
		}
	}
	
	public void testMP4TagWriters() {
		try {
			// create a new temp file to play with
			String sourceFilename = "data/test/audio/songs/backup/rastic chevch.m4a";
			String targetFilename = "data/test/audio/songs/temp/rastic chevch.m4a";			
			File targetFile = new File(targetFilename);
			if (targetFile.exists())
				targetFile.delete();			
			FileUtil.copy(sourceFilename, targetFilename);
			
			setupStyles1();
			setupTags1();
			
			log.info("testMP4TagWriters(): testing jaudiotagger...");
			testTagWriter(new JAudioMP4TagWriter(targetFilename, TAG_MODE_UPDATE));
			testTagWriterResultsMP4(new JAudioMP4TagReader(targetFilename));

			targetFile.delete();
			FileUtil.copy(sourceFilename, targetFilename);
			setupStyles2();
			setupTags2();
			
			log.info("testMP4TagWriters(): testing jaudiotagger...");
			testTagWriter(new JAudioMP4TagWriter(targetFilename, TAG_MODE_UPDATE));
			testTagWriterResultsMP4(new JAudioMP4TagReader(targetFilename));
			
		} catch (Exception e) {
			log.error("testMP4TagWriters(): error", e);
			fail(e.getMessage());
		}		
	}
	
	public void testOGGTagWriters() {
		try {
			// create a new temp file to play with
			String sourceFilename = "data/test/audio/songs/backup/jeroensnake_-_Echo_Guitar_Jeroen.ogg";
			String targetFilename = "data/test/audio/songs/temp/jeroensnake_-_Echo_Guitar_Jeroen.ogg";			
			File targetFile = new File(targetFilename);
			if (targetFile.exists())
				targetFile.delete();			
			FileUtil.copy(sourceFilename, targetFilename);
			
			setupStyles1();
			setupTags1();
			
			log.info("testOGGTagWriters(): testing jaudiotagger...");
			testTagWriter(new JAudioOggTagWriter(targetFilename, TAG_MODE_UPDATE));
			testTagWriterResults(new JAudioOggTagReader(targetFilename));

			targetFile.delete();
			FileUtil.copy(sourceFilename, targetFilename);
			setupStyles2();
			setupTags2();
			
			log.info("testOGGTagWriters(): testing jaudiotagger...");
			testTagWriter(new JAudioOggTagWriter(targetFilename, TAG_MODE_UPDATE));
			testTagWriterResults(new JAudioOggTagReader(targetFilename));
			
		} catch (Exception e) {
			log.error("testOGGTagWriters(): error", e);
			fail(e.getMessage());
		}		
	}	
	
	public void testFLACTagWriters() {
		try {
			// create a new temp file to play with
			String sourceFilename = "data/test/audio/songs/backup/2.flac";
			String targetFilename = "data/test/audio/songs/temp/2.flac";			
			File targetFile = new File(targetFilename);
			if (targetFile.exists())
				targetFile.delete();			
			FileUtil.copy(sourceFilename, targetFilename);
			
			setupStyles1();
			setupTags1();
			
			log.info("testFLACTagWriters(): testing jaudiotagger...");
			testTagWriter(new JAudioFlacTagWriter(targetFilename, TAG_MODE_UPDATE));
			testTagWriterResults(new JAudioFlacTagReader(targetFilename));

			targetFile.delete();
			FileUtil.copy(sourceFilename, targetFilename);
			setupStyles2();
			setupTags2();
			
			log.info("testFLACTagWriters(): testing jaudiotagger...");
			testTagWriter(new JAudioFlacTagWriter(targetFilename, TAG_MODE_UPDATE));
			testTagWriterResults(new JAudioFlacTagReader(targetFilename));
			
		} catch (Exception e) {
			log.error("testFLACTagWriters(): error", e);
			fail(e.getMessage());
		}		
	}		
	
	public void testWMATagWriters() {
		try {
			// create a new temp file to play with
			String sourceFilename = "data/test/audio/songs/backup/sample.wma";
			String targetFilename = "data/test/audio/songs/temp/sample.wma";			
			File targetFile = new File(targetFilename);
			if (targetFile.exists())
				targetFile.delete();			
			FileUtil.copy(sourceFilename, targetFilename);
			
			setupStyles1();
			setupTags1();
			
			log.info("testWMATagWriters(): testing jaudiotagger...");
			testTagWriter(new JAudioASFTagWriter(targetFilename, TAG_MODE_UPDATE));
			testTagWriterResultsWMA(new JAudioASFTagReader(targetFilename));

			targetFile.delete();
			FileUtil.copy(sourceFilename, targetFilename);
			setupStyles2();
			setupTags2();
			
			log.info("testWMATagWriters(): testing jaudiotagger...");
			testTagWriter(new JAudioASFTagWriter(targetFilename, TAG_MODE_UPDATE));
			testTagWriterResultsWMA(new JAudioASFTagReader(targetFilename));
			
		} catch (Exception e) {
			log.error("testWMATagWriters(): error", e);
			fail(e.getMessage());
		}		
	}		
		
	
	static public void testTagWriter(TagWriter tagWriter) {
		try {
			tagWriter.setAlbum("some Album");
			tagWriter.setAlbumCover("data/test/images/albumcovers/aphex twin_selected ambient works 85-92.jpg", "some Album");
			tagWriter.setArtist("some Artist");
			tagWriter.setBeatIntensity(82);
			tagWriter.setBpm(136);
			tagWriter.setBpmStart(136.2f);
			tagWriter.setBpmEnd(72.5f);
			tagWriter.setBpmAccuracy(62);
			tagWriter.setComments("some Comments");
			tagWriter.setContentGroupDescription("09A Em -> 10A Bm");
			tagWriter.setGenre("ambient");
			tagWriter.setKey("Em");
			tagWriter.setKeyStart("Em");
			tagWriter.setKeyEnd("Bm");
			tagWriter.setKeyAccuracy(43);
			tagWriter.setLyrics("lyrics man!");
			tagWriter.setRating(4);
			tagWriter.setPublisher("some Label");
			tagWriter.setRemix("some Remix");
			tagWriter.setReplayGain(3.2f);
			tagWriter.setStyles(testStyles);
			tagWriter.setTags(testTags);
			tagWriter.setTime("4:20");
			tagWriter.setTimeSignature("8/8");
			tagWriter.setTitle("some Title");
			tagWriter.setTrack("4", 10);
			tagWriter.setYear("1992");
			tagWriter.setUserField("custom field 1", "custom value");
			
			if (!tagWriter.save())
				fail("save failed");
		} catch (Exception e) {
			log.error("testMP3TagWriter(): error", e);
			fail(e.getMessage());
		}
	}
	
	
	static public void testTagWriterResults(TagReader tagReader) {
		try {			
			if (!tagReader.getAlbum().equals("some Album"))
				fail("album incorrect, is=" + tagReader.getAlbum());
			if (tagReader.getAlbumCoverFilename() == null)
				fail("null album cover");
			if (!tagReader.getArtist().equals("some Artist"))
				fail("artist incorrect, is=" + tagReader.getArtist());
			if (tagReader.getBeatIntensity() != 82)
				fail("incorrect beat intensity=" + tagReader.getBeatIntensity());
			if (tagReader.getBpmStart() != 136.2f)
				fail("incorrect bpm start=" + tagReader.getBpmStart());
			if (tagReader.getBpmEnd() != 72.5f)
				fail("incorrect bpm end=" + tagReader.getBpmEnd());
			if (tagReader.getBpmAccuracy() != 62)
				fail("incorrect bpm accuracy");
			if (!tagReader.getComments().equals("some Comments"))
				fail("incorrect comments, are=" + tagReader.getComments());
			if (!tagReader.getContentGroupDescription().equals("09A Em -> 10A Bm"))
				fail("incorrect group, is=" + tagReader.getContentGroupDescription());
			if (!tagReader.getGenre().equals("ambient"))
				fail("genre incorrect, is=" + tagReader.getGenre());
			if (!Key.getKey(tagReader.getKeyStart()).equals(Key.getKey("Em")))
				fail("key incorrect, is=" + tagReader.getKeyStart());
			if (!Key.getKey(tagReader.getKeyEnd()).equals(Key.getKey("Bm")))
				fail("key end incorrect, is=" + tagReader.getKeyEnd());
			if (tagReader.getKeyAccuracy() != 43)
				fail("key accuracy incorrect, is=" + tagReader.getKeyAccuracy());
			if (!tagReader.getLyrics().equals("lyrics man!"))
				fail("incorrect lyrics=" + tagReader.getLyrics());
			if (tagReader.getRating() != 4)
				fail("incorrect rating=" + tagReader.getRating());
			if (!tagReader.getPublisher().equals("some Label"))
				fail("incorrect label");
			if (!tagReader.getRemix().equals("some Remix"))
				fail("incorrect remix");
			if (tagReader.getReplayGain() != 3.2f)
				fail("incorrect replay gain");
			if (tagReader.getStyles().size() != testStyles.size())
				fail("incorrect styles size, is=" + tagReader.getStyles().size() + ", styles=" + tagReader.getStyles());
			for (int s = 0; s < testStyles.size(); ++s) {
				if (!tagReader.getStyles().get(s).getName().equals(testStyles.get(s).getName()))
					fail("style incorrect=" + tagReader.getStyles().get(s).getName());
				if (tagReader.getStyles().get(s).getPercentage() != testStyles.get(s).getPercentage())
					fail("style degree incorrect=" + tagReader.getStyles().get(s).getPercentage());
			}
			if (tagReader.getTags().size() != testTags.size())
				fail("incorrect tags size, is=" + tagReader.getTags().size() + ", tags=" + tagReader.getTags());
			for (int s = 0; s < testTags.size(); ++s) {
				if (!tagReader.getTags().get(s).getName().equals(testTags.get(s).getName()))
					fail("tag incorrect=" + tagReader.getTags().get(s).getName());
				if (tagReader.getTags().get(s).getPercentage() != testTags.get(s).getPercentage())
					fail("tag degree incorrect=" + tagReader.getTags().get(s).getPercentage());
			}
			if (!tagReader.getTime().equals("4:20"))
				fail("time incorrect");
			if (!tagReader.getTimeSignature().equals("8/8"))
				fail("time sig incorrect");		
			if (!tagReader.getTitle().equals("some Title"))
				fail("title incorrect");
			if (tagReader.getTotalTracks() != 10)
				fail("incorrect total tracks");
			if (!tagReader.getTrack().equals("4") && !tagReader.getTrack().equals("04"))
				fail("incorrect track, is=" + tagReader.getTrack());
			if (!tagReader.getYear().equals("1992"))
				fail("incorrect year");
			if (!tagReader.getUserField("custom field 1").equals("custom value"))
				fail("incorrect custom value");
			
		} catch (Exception e) {
			log.error("testTagWriterResults(): error", e);
			fail(e.getMessage());
		}
	}
	
	static public void testTagWriterResultsKeyOptions(TagReader tagReader) {
		try {			
			if (!tagReader.getAlbum().equals("some Album"))
				fail("album incorrect, is=" + tagReader.getAlbum());
			if (tagReader.getAlbumCoverFilename() == null)
				fail("null album cover");
			if (!tagReader.getArtist().equals("some Artist"))
				fail("artist incorrect, is=" + tagReader.getArtist());
			if (tagReader.getBeatIntensity() != 82)
				fail("incorrect beat intensity=" + tagReader.getBeatIntensity());
			if (tagReader.getBpmStart() != 136.2f)
				fail("incorrect bpm start=" + tagReader.getBpmStart());
			if (tagReader.getBpmEnd() != 72.5f)
				fail("incorrect bpm end=" + tagReader.getBpmEnd());
			if (tagReader.getBpmAccuracy() != 62)
				fail("incorrect bpm accuracy");
			if (!tagReader.getComments().equals("09A->10A - some Comments"))
				fail("incorrect comments, are=" + tagReader.getComments());
			if (!tagReader.getContentGroupDescription().equals("09A Em -> 10A Bm"))
				fail("incorrect group, is=" + tagReader.getContentGroupDescription());
			if (!tagReader.getGenre().equals("ambient"))
				fail("genre incorrect, is=" + tagReader.getGenre());
			if (!tagReader.getKeyStart().equals("09A"))
				fail("key incorrect, is=" + tagReader.getKeyStart());
			if (!tagReader.getKeyEnd().equals("10A"))
				fail("key end incorrect, is=" + tagReader.getKeyEnd());
			if (tagReader.getKeyAccuracy() != 43)
				fail("key accuracy incorrect, is=" + tagReader.getKeyAccuracy());
			if (!tagReader.getLyrics().equals("lyrics man!"))
				fail("incorrect lyrics=" + tagReader.getLyrics());
			if (tagReader.getRating() != 4)
				fail("incorrect rating=" + tagReader.getRating());
			if (!tagReader.getPublisher().equals("some Label"))
				fail("incorrect label");
			if (!tagReader.getRemix().equals("some Remix"))
				fail("incorrect remix");
			if (tagReader.getReplayGain() != 3.2f)
				fail("incorrect replay gain");
			if (tagReader.getStyles().size() != testStyles.size())
				fail("incorrect styles size, is=" + tagReader.getStyles().size() + ", styles=" + tagReader.getStyles());
			for (int s = 0; s < testStyles.size(); ++s) {
				if (!tagReader.getStyles().get(s).getName().equals(testStyles.get(s).getName()))
					fail("style incorrect=" + tagReader.getStyles().get(s).getName());
				if (tagReader.getStyles().get(s).getPercentage() != testStyles.get(s).getPercentage())
					fail("style degree incorrect=" + tagReader.getStyles().get(s).getPercentage());
			}
			if (tagReader.getTags().size() != testTags.size())
				fail("incorrect tags size, is=" + tagReader.getTags().size() + ", tags=" + tagReader.getTags());
			for (int s = 0; s < testTags.size(); ++s) {
				if (!tagReader.getTags().get(s).getName().equals(testTags.get(s).getName()))
					fail("tag incorrect=" + tagReader.getTags().get(s).getName());
				if (tagReader.getTags().get(s).getPercentage() != testTags.get(s).getPercentage())
					fail("tag degree incorrect=" + tagReader.getTags().get(s).getPercentage());
			}
			if (!tagReader.getTime().equals("4:20"))
				fail("time incorrect");
			if (!tagReader.getTimeSignature().equals("8/8"))
				fail("time sig incorrect");		
			if (!tagReader.getTitle().equals("09A->10A - some Title"))
				fail("title incorrect, is=" + tagReader.getTitle());
			if (tagReader.getTotalTracks() != 10)
				fail("incorrect total tracks");
			if (!tagReader.getTrack().equals("4") && !tagReader.getTrack().equals("04"))
				fail("incorrect track, is=" + tagReader.getTrack());
			if (!tagReader.getYear().equals("1992"))
				fail("incorrect year");
			if (!tagReader.getUserField("custom field 1").equals("custom value"))
				fail("incorrect custom value");
			
		} catch (Exception e) {
			log.error("testTagWriterResultsKeyOptions(): error", e);
			fail(e.getMessage());
		}
	}
	
	public void testTagWriterResultsMP3(TagReader tagReader) {
		try {			
			if (!tagReader.getAlbum().equals("some Album"))
				fail("album incorrect, is=" + tagReader.getAlbum());
			if (tagReader.getAlbumCoverFilename() == null)
				fail("null album cover");
			if (!tagReader.getArtist().equals("some Artist"))
				fail("artist incorrect, is=" + tagReader.getArtist());
			if (tagReader.getBeatIntensity() != 82)
				fail("incorrect beat intensity=" + tagReader.getBeatIntensity());
			if (tagReader.getBpmStart() != 136.2f)
				fail("incorrect bpm start=" + tagReader.getBpmStart());
			if (tagReader.getBpmEnd() != 72.5f)
				fail("incorrect bpm end=" + tagReader.getBpmEnd());
			if (tagReader.getBpmAccuracy() != 62)
				fail("incorrect bpm accuracy");
			if (!tagReader.getComments().equals("some Comments"))
				fail("incorrect comments");
			if (!tagReader.getContentGroupDescription().equals("09A Em -> 10A Bm"))
				fail("incorrect group");
			if (!tagReader.getGenre().equals("ambient"))
				fail("genre incorrect");
			if (!Key.getKey(tagReader.getKeyStart()).equals(Key.getKey("Em")))
				fail("key incorrect, is=" + tagReader.getKeyStart());
			if (!Key.getKey(tagReader.getKeyEnd()).equals(Key.getKey("Bm")))
				fail("key end incorrect, is=" + tagReader.getKeyEnd());
			if (tagReader.getKeyAccuracy() != 43)
				fail("key accuracy incorrect, is=" + tagReader.getKeyAccuracy());
			//if (!tagReader.getLyrics().equals("lyrics man!"))
				//fail("incorrect lyrics=" + tagReader.getLyrics());
			if (tagReader.getRating() != 4)
				fail("incorrect rating=" + tagReader.getRating());
			if (!tagReader.getPublisher().equals("some Label"))
				fail("incorrect label");
			if (!tagReader.getRemix().equals("some Remix"))
				fail("incorrect remix");
			if (tagReader.getReplayGain() != 3.2f)
				fail("incorrect replay gain");
			if (tagReader.getStyles().size() != testStyles.size())
				fail("incorrect styles size, is=" + tagReader.getStyles().size() + ", styles=" + tagReader.getStyles());
			for (int s = 0; s < testStyles.size(); ++s) {
				if (!tagReader.getStyles().get(s).getName().equals(testStyles.get(s).getName()))
					fail("style incorrect=" + tagReader.getStyles().get(s).getName());
				if (tagReader.getStyles().get(s).getPercentage() != testStyles.get(s).getPercentage())
					fail("style degree incorrect=" + tagReader.getStyles().get(s).getPercentage());
			}
			if (tagReader.getTags().size() != testTags.size())
				fail("incorrect tags size, is=" + tagReader.getTags().size() + ", tags=" + tagReader.getTags());
			for (int s = 0; s < testTags.size(); ++s) {
				if (!tagReader.getTags().get(s).getName().equals(testTags.get(s).getName()))
					fail("tag incorrect=" + tagReader.getTags().get(s).getName());
				if (tagReader.getTags().get(s).getPercentage() != testTags.get(s).getPercentage())
					fail("tag degree incorrect=" + tagReader.getTags().get(s).getPercentage());
			}
			if (!tagReader.getTime().equals("4:20"))
				fail("time incorrect");
			if (!tagReader.getTimeSignature().equals("8/8"))
				fail("time sig incorrect");		
			if (!tagReader.getTitle().equals("some Title"))
				fail("title incorrect");
			if (tagReader.getTotalTracks() != 10)
				fail("incorrect total tracks");
			if (!tagReader.getTrack().equals("04"))
				fail("incorrect track, is=" + tagReader.getTrack());
			if (!tagReader.getYear().equals("1992"))
				fail("incorrect year");
			if (!tagReader.getUserField("custom field 1").equals("custom value"))
				fail("incorrect custom value");
			
		} catch (Exception e) {
			log.error("testMP3TagWriterResults(): error", e);
			fail(e.getMessage());
		}
	}		
	
	public void testTagWriterResultsMP4(TagReader tagReader) {
		try {			
			if (!tagReader.getAlbum().equals("some Album"))
				fail("album incorrect, is=" + tagReader.getAlbum());
			if (tagReader.getAlbumCoverFilename() == null)
				fail("null album cover");
			if (!tagReader.getArtist().equals("some Artist"))
				fail("artist incorrect, is=" + tagReader.getArtist());
			//if (tagReader.getBeatIntensity() != 82)
				//fail("incorrect beat intensity=" + tagReader.getBeatIntensity());
			if (tagReader.getBpmStart() != 136.0f)
				fail("incorrect bpm start=" + tagReader.getBpmStart());
			//if (tagReader.getBpmEnd() != 72.5f)
				//fail("incorrect bpm end=" + tagReader.getBpmEnd());
			//if (tagReader.getBpmAccuracy() != 62)
				//fail("incorrect bpm accuracy");
			if (!tagReader.getComments().equals("some Comments"))
				fail("incorrect comments");
			if (!tagReader.getContentGroupDescription().equals("09A Em -> 10A Bm"))
				fail("incorrect group");
			if (!tagReader.getGenre().equals("ambient"))
				fail("genre incorrect");
			if (!Key.getKey(tagReader.getKeyStart()).equals(Key.getKey("Em")))
				fail("key incorrect, is=" + tagReader.getKeyStart());
			if (!Key.getKey(tagReader.getKeyEnd()).equals(Key.getKey("Bm")))
				fail("key end incorrect, is=" + tagReader.getKeyEnd());
			//if (tagReader.getKeyAccuracy() != 43)
				//fail("key accuracy incorrect, is=" + tagReader.getKeyAccuracy());
			if (!tagReader.getLyrics().equals("lyrics man!"))
				fail("incorrect lyrics=" + tagReader.getLyrics());
			if (tagReader.getRating() != 4)
				fail("incorrect rating=" + tagReader.getRating());
			if (!tagReader.getPublisher().equals("some Label"))
				fail("incorrect label");
			if (!tagReader.getRemix().equals("some Remix"))
				fail("incorrect remix");
			if (tagReader.getReplayGain() != 3.2f)
				fail("incorrect replay gain");
			/*
			if (tagReader.getStyles().size() != testStyles.size())
				fail("incorrect styles size, is=" + tagReader.getStyles().size() + ", styles=" + tagReader.getStyles());
			for (int s = 0; s < testStyles.size(); ++s) {
				if (!tagReader.getStyles().get(s).getName().equals(testStyles.get(s).getName()))
					fail("style incorrect=" + tagReader.getStyles().get(s).getName());
				if (tagReader.getStyles().get(s).getPercentage() != testStyles.get(s).getPercentage())
					fail("style degree incorrect=" + tagReader.getStyles().get(s).getPercentage());
			}
			*/
			/*
			if (tagReader.getTags().size() != testTags.size())
				fail("incorrect tags size, is=" + tagReader.getTags().size() + ", tags=" + tagReader.getTags());
			for (int s = 0; s < testTags.size(); ++s) {
				if (!tagReader.getTags().get(s).getName().equals(testTags.get(s).getName()))
					fail("tag incorrect=" + tagReader.getTags().get(s).getName());
				if (tagReader.getTags().get(s).getPercentage() != testTags.get(s).getPercentage())
					fail("tag degree incorrect=" + tagReader.getTags().get(s).getPercentage());
			}
			*/
			//if (!tagReader.getTime().equals("4:20"))
				//fail("time incorrect");
			//if (!tagReader.getTimeSignature().equals("8/8"))
				//fail("time sig incorrect");		
			if (!tagReader.getTitle().equals("some Title"))
				fail("title incorrect");
			if (tagReader.getTotalTracks() != 10)
				fail("incorrect total tracks");
			if (!tagReader.getTrack().equals("4"))
				fail("incorrect track, is=" + tagReader.getTrack());
			//if (!tagReader.getYear().equals("1992"))
				//fail("incorrect year");
			//if (!tagReader.getUserField("custom field 1").equals("custom value"))
				//fail("incorrect custom value");
			
		} catch (Exception e) {
			log.error("testMP3TagWriterResults(): error", e);
			fail(e.getMessage());
		}
	}		
	
	public void testTagWriterResultsWMA(TagReader tagReader) {
		try {			
			if (!tagReader.getAlbum().equals("some Album"))
				fail("album incorrect, is=" + tagReader.getAlbum());
			//if (tagReader.getAlbumCoverFilename() == null)
				//fail("null album cover");
			if (!tagReader.getArtist().equals("some Artist"))
				fail("artist incorrect, is=" + tagReader.getArtist());
			//if (tagReader.getBeatIntensity() != 82)
				//fail("incorrect beat intensity=" + tagReader.getBeatIntensity());
			if (tagReader.getBpmStart() != 136.0f)
				fail("incorrect bpm start=" + tagReader.getBpmStart());
			//if (tagReader.getBpmEnd() != 72.5f)
				//fail("incorrect bpm end=" + tagReader.getBpmEnd());
			//if (tagReader.getBpmAccuracy() != 62)
				//fail("incorrect bpm accuracy");
			if (!tagReader.getComments().equals("some Comments"))
				fail("incorrect comments");
			if (!tagReader.getContentGroupDescription().equals("09A Em -> 10A Bm"))
				fail("incorrect group");
			if (!tagReader.getGenre().equals("ambient"))
				fail("genre incorrect");
			if (!Key.getKey(tagReader.getKeyStart()).equals(Key.getKey("Em")))
				fail("key incorrect, is=" + tagReader.getKeyStart());
			//if (!Key.getKey(tagReader.getKeyEnd()).equals(Key.getKey("Bm")))
				//fail("key end incorrect, is=" + tagReader.getKeyEnd());
			//if (tagReader.getKeyAccuracy() != 43)
				//fail("key accuracy incorrect, is=" + tagReader.getKeyAccuracy());
			//if (!tagReader.getLyrics().equals("lyrics man!"))
				//fail("incorrect lyrics=" + tagReader.getLyrics());
			//if (tagReader.getRating() != 4)
				//fail("incorrect rating=" + tagReader.getRating());
			if (!tagReader.getPublisher().equals("some Label"))
				fail("incorrect label");
			if (!tagReader.getRemix().equals("some Remix"))
				fail("incorrect remix");
			//if (tagReader.getReplayGain() != 3.2f)
				//fail("incorrect replay gain");
			/*
			if (tagReader.getStyles().size() != testStyles.size())
				fail("incorrect styles size, is=" + tagReader.getStyles().size() + ", styles=" + tagReader.getStyles());
			for (int s = 0; s < testStyles.size(); ++s) {
				if (!tagReader.getStyles().get(s).getName().equals(testStyles.get(s).getName()))
					fail("style incorrect=" + tagReader.getStyles().get(s).getName());
				if (tagReader.getStyles().get(s).getPercentage() != testStyles.get(s).getPercentage())
					fail("style degree incorrect=" + tagReader.getStyles().get(s).getPercentage());
			}
			if (tagReader.getTags().size() != testTags.size())
				fail("incorrect tags size, is=" + tagReader.getTags().size() + ", tags=" + tagReader.getTags());
			for (int s = 0; s < testTags.size(); ++s) {
				if (!tagReader.getTags().get(s).getName().equals(testTags.get(s).getName()))
					fail("tag incorrect=" + tagReader.getTags().get(s).getName());
				if (tagReader.getTags().get(s).getPercentage() != testTags.get(s).getPercentage())
					fail("tag degree incorrect=" + tagReader.getTags().get(s).getPercentage());
			}
			*/
			//if (!tagReader.getTime().equals("4:20"))
				//fail("time incorrect");
			//if (!tagReader.getTimeSignature().equals("8/8"))
				//fail("time sig incorrect");		
			if (!tagReader.getTitle().equals("some Title"))
				fail("title incorrect");
			//if (tagReader.getTotalTracks() != 10)
				//fail("incorrect total tracks, is=" + tagReader.getTotalTracks());
			if (!tagReader.getTrack().equals("4"))
				fail("incorrect track, is=" + tagReader.getTrack());
			if (!tagReader.getYear().equals("1992"))
				fail("incorrect year");
			//if (!tagReader.getUserField("custom field 1").equals("custom value"))
				//fail("incorrect custom value");
			
		} catch (Exception e) {
			log.error("testTagWriterResultsWMA(): error", e);
			fail(e.getMessage());
		}
	}
	
}
