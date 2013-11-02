package test.com.mixshare.rapid_evolution.audio.detection.key;

import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.audio.detection.key.DetectedKey;
import com.mixshare.rapid_evolution.audio.detection.key.KeyDetector;
import com.mixshare.rapid_evolution.music.key.Key;

public class KeyDetectorTest extends RE3TestCase {

	static private Logger log = Logger.getLogger(KeyDetectorTest.class);	
		
	static private String[] ALL_CODECS = { "enable_quicktime_codec", "enable_ffmpeg_codec", "enable_faad2_codec", "enable_java_codecs", "enable_phonon_codec", "enable_xuggle_codec"};
	
	static private boolean enableFFMPEG = true;
	static private boolean enableJava = true;
	static private boolean enableXuggle = true;
	static private boolean enableFAAD2 = true;
	static private boolean enableQuicktime = false; // should be false
		
	public void testKeyDetectorOnSongs() {
		if (enableFFMPEG)
			testFFMPEGCodec("enable_ffmpeg_codec");
		if (enableJava)
			testJavaCodec("enable_java_codecs");
		if (enableXuggle)
			testXuggleCodec("enable_xuggle_codec");
		if (enableFAAD2)
			testFAAD2Codec("enable_faad2_codec");
		if (enableQuicktime)
			testXuggleCodec("enable_quicktime_codec");
	}
	
	public void testFFMPEGCodec(String activeCodec) {
		log.info("testKeyDetectorOnSongs(): activeCodec=" + activeCodec);
		for (String disabledCodec : ALL_CODECS)
			RE3Properties.setProperty(disabledCodec, "false");
		RE3Properties.setProperty(activeCodec, "true");
		try {
			RE3Properties.setProperty("detect_start_and_end_keys", "false");
			RE3Properties.setProperty("detect_advanced_keys", "false");
			RE3Properties.setProperty("percent_audio_samples_to_process", "100");
			
			DetectedKey result = KeyDetector.detectKey("data/test/audio/songs/2.flac");
			if (result == null)
				fail("key detector did not return a result");
			if (!result.getStartKey().equals(Key.getKey("E")))
				fail("incorrect key, is=" + result.getStartKey());
			
			result = KeyDetector.detectKey("data/test/audio/songs/germanic.mp3");
			if (result == null)
				fail("key detector did not return a result");
			if (!result.getStartKey().equals(Key.getKey("E"))) // NOTE: should be Em
				fail("incorrect key, is=" + result.getStartKey());			

			result = KeyDetector.detectKey("data/test/audio/songs/sh3.ape");
			if (result == null)
				fail("key detector did not return a result");
			if (!result.getStartKey().equals(Key.getKey("G#m")))
				fail("incorrect key, is=" + result.getStartKey());			
			
			result = KeyDetector.detectKey("data/test/audio/songs/flarefalls.ogg");
			if (result == null)
				fail("key detector did not return a result");
			if (!result.getStartKey().equals(Key.getKey("A")))
				fail("incorrect key, is=" + result.getStartKey());
			
			result = KeyDetector.detectKey("data/test/audio/songs/rastic chevch.m4a");
			if (result == null)
				fail("key detector did not return a result");
			if (!result.getStartKey().equals(Key.getKey("Dm"))) // should be Em?
				fail("incorrect key, is=" + result.getStartKey());	
			
			result = KeyDetector.detectKey("data/test/audio/songs/sample.wma");
			if (result == null)
				fail("key detector did not return a result");
			if (!result.getStartKey().equals(Key.getKey("G")))
				fail("incorrect key, is=" + result.getStartKey());			
			
		} catch (Exception e) {
			log.error("testKeyDetectorOnSongs(): error", e);
			fail(e.getMessage());
		}
	}	
	
	public void testXuggleCodec(String activeCodec) {
		log.info("testKeyDetectorOnSongs(): activeCodec=" + activeCodec);
		for (String disabledCodec : ALL_CODECS)
			RE3Properties.setProperty(disabledCodec, "false");
		RE3Properties.setProperty(activeCodec, "true");
		try {
			RE3Properties.setProperty("detect_start_and_end_keys", "false");
			RE3Properties.setProperty("detect_advanced_keys", "false");
			RE3Properties.setProperty("percent_audio_samples_to_process", "100");
			
			DetectedKey result = KeyDetector.detectKey("data/test/audio/songs/2.flac");
			if (result == null)
				fail("key detector did not return a result");
			if (!result.getStartKey().equals(Key.getKey("E")))
				fail("incorrect key, is=" + result.getStartKey());
			
			result = KeyDetector.detectKey("data/test/audio/songs/germanic.mp3");
			if (result == null)
				fail("key detector did not return a result");
			if (!result.getStartKey().equals(Key.getKey("E"))) // NOTE: should be Em
				fail("incorrect key, is=" + result.getStartKey());			
			
			result = KeyDetector.detectKey("data/test/audio/songs/sh3.ape");
			if (result == null)
				fail("key detector did not return a result");
			if (!result.getStartKey().equals(Key.getKey("G#m")))
				fail("incorrect key, is=" + result.getStartKey());			
			
			result = KeyDetector.detectKey("data/test/audio/songs/flarefalls.ogg");
			if (result == null)
				fail("key detector did not return a result");
			if (!result.getStartKey().equals(Key.getKey("A")))
				fail("incorrect key, is=" + result.getStartKey());
			
			result = KeyDetector.detectKey("data/test/audio/songs/rastic chevch.m4a");
			if (result == null)
				fail("key detector did not return a result");
			if (!result.getStartKey().equals(Key.getKey("F"))) // should be Em?
				fail("incorrect key, is=" + result.getStartKey());	
			
			result = KeyDetector.detectKey("data/test/audio/songs/sample.wma");
			if (result == null)
				fail("key detector did not return a result");
			if (!result.getStartKey().equals(Key.getKey("G")))
				fail("incorrect key, is=" + result.getStartKey());			

// A quick attempt to test 2 xuggle decoders at once..			
//			final CountDownLatch latch = new CountDownLatch(2);
//			new Thread() {
//				public void run() {
//					KeyDetector.detectKey("data/test/audio/songs/sample.wma");
//					latch.countDown();
//				}
//			}.run();
//			final DetectedKey result2 = null;
//			new Thread() {
//				public void run() {
//					KeyDetector.detectKey("data/test/audio/songs/rastic chevch.m4a");
//					latch.countDown();
//				}
//			}.run();
//			latch.await();

		} catch (Exception e) {
			log.error("testKeyDetectorOnSongs(): error", e);
			fail(e.getMessage());
		}
	}	
	
	public void testJavaCodec(String activeCodec) {
		log.info("testKeyDetectorOnSongs(): activeCodec=" + activeCodec);
		for (String disabledCodec : ALL_CODECS)
			RE3Properties.setProperty(disabledCodec, "false");
		RE3Properties.setProperty(activeCodec, "true");
		try {
			RE3Properties.setProperty("detect_start_and_end_keys", "false");
			RE3Properties.setProperty("detect_advanced_keys", "false");
			RE3Properties.setProperty("percent_audio_samples_to_process", "100");
			
			DetectedKey result = KeyDetector.detectKey("data/test/audio/songs/2.flac");
			if (result == null)
				fail("key detector did not return a result");
			if (!result.getStartKey().equals(Key.getKey("E")))
				fail("incorrect key, is=" + result.getStartKey());
			
			result = KeyDetector.detectKey("data/test/audio/songs/sh3.ape");
			if (result == null)
				fail("key detector did not return a result");
			if (!result.getStartKey().equals(Key.getKey("G#m")))
				fail("incorrect key, is=" + result.getStartKey());			
			
			// TODO: oggs SHOULD be supported by the ogg pluggin, bleh
			/*
			result = KeyDetector.detectKey("data/test/audio/songs/flarefalls.ogg");
			if (result == null)
				fail("key detector did not return a result");
			if (!result.getStartKey().equals(Key.getKey("A")))
				fail("incorrect key, is=" + result.getStartKey());
			*/
			
		} catch (Exception e) {
			log.error("testKeyDetectorOnSongs(): error", e);
			fail(e.getMessage());
		}
	}
	
	public void testFAAD2Codec(String activeCodec) {
		log.info("testKeyDetectorOnSongs(): activeCodec=" + activeCodec);
		for (String disabledCodec : ALL_CODECS)
			RE3Properties.setProperty(disabledCodec, "false");
		RE3Properties.setProperty(activeCodec, "true");
		try {
			RE3Properties.setProperty("detect_start_and_end_keys", "false");
			RE3Properties.setProperty("detect_advanced_keys", "false");
			RE3Properties.setProperty("percent_audio_samples_to_process", "100");
						
			DetectedKey result = KeyDetector.detectKey("data/test/audio/songs/rastic chevch.m4a");
			if (result == null)
				fail("key detector did not return a result");
			if (!result.getStartKey().equals(Key.getKey("Dm"))) // should be Em?
				fail("incorrect key, is=" + result.getStartKey());	
			
		} catch (Exception e) {
			log.error("testKeyDetectorOnSongs(): error", e);
			fail(e.getMessage());
		}
	}		
	
	public void testKeyDetectorOnChords() {
		// all wav files...
		if (enableFFMPEG)
			testKeyDetectorOnChords("enable_ffmpeg_codec");
		if (enableJava)
			testKeyDetectorOnChords("enable_java_codecs");
		if (enableXuggle)
			testKeyDetectorOnChords("enable_xuggle_codec");
		if (enableQuicktime)
			testKeyDetectorOnChords("enable_quicktime_codec");
	}
	
	public void testKeyDetectorOnChords(String activeCodec) {
		log.info("testKeyDetectorOnChords(): activeCodec=" + activeCodec);
		for (String disabledCodec : ALL_CODECS)
			RE3Properties.setProperty(disabledCodec, "false");
		RE3Properties.setProperty(activeCodec, "true");
		try {
			RE3Properties.setProperty("detect_start_and_end_keys", "false");
			RE3Properties.setProperty("detect_advanced_keys", "false");
			RE3Properties.setProperty("percent_audio_samples_to_process", "100");
			
			DetectedKey result = KeyDetector.detectKey("data/test/audio/chords/A major.wav");
			if (result == null)
				fail("key detector did not return a result");
			if (!result.getStartKey().equals(Key.getKey("A")))
				fail("incorrect key, is=" + result.getStartKey());
			
			result = KeyDetector.detectKey("data/test/audio/chords/C dom7.aif");
			if (result == null)
				fail("key detector did not return a result");
			if (!result.getStartKey().equals(Key.getKey("C")))
				fail("incorrect key, is=" + result.getStartKey());			
			
			result = KeyDetector.detectKey("data/test/audio/chords/cm-fsharp.wav");
			if (result == null)
				fail("key detector did not return a result");
			if (!result.getStartKey().equals(Key.getKey("F#")))
				fail("incorrect key, is=" + result.getStartKey());
			
			result = KeyDetector.detectKey("data/test/audio/chords/C Maj11.wav");
			if (result == null)
				fail("key detector did not return a result");
			if (!result.getStartKey().equals(Key.getKey("C")))
				fail("incorrect key, is=" + result.getStartKey());			

			result = KeyDetector.detectKey("data/test/audio/chords/C Maj6.wav");
			if (result == null)
				fail("key detector did not return a result");
			if (!result.getStartKey().equals(Key.getKey("C")))
				fail("incorrect key, is=" + result.getStartKey());			
			
			result = KeyDetector.detectKey("data/test/audio/chords/C minor.wav");
			if (result == null)
				fail("key detector did not return a result");
			if (!result.getStartKey().equals(Key.getKey("Cm")))
				fail("incorrect key, is=" + result.getStartKey());				

			result = KeyDetector.detectKey("data/test/audio/chords/Dom11.wav");
			if (result == null)
				fail("key detector did not return a result");
			if (!result.getStartKey().equals(Key.getKey("C")))
				fail("incorrect key, is=" + result.getStartKey());	
			
			result = KeyDetector.detectKey("data/test/audio/chords/Dom9.wav");
			if (result == null)
				fail("key detector did not return a result");
			if (!result.getStartKey().equals(Key.getKey("C")))
				fail("incorrect key, is=" + result.getStartKey());	
			
			result = KeyDetector.detectKey("data/test/audio/chords/F minor.wav");
			if (result == null)
				fail("key detector did not return a result");
			if (!result.getStartKey().equals(Key.getKey("Fm")))
				fail("incorrect key, is=" + result.getStartKey());	
			
			result = KeyDetector.detectKey("data/test/audio/chords/Maj7.wav");
			if (result == null)
				fail("key detector did not return a result");
			if (!result.getStartKey().equals(Key.getKey("C")))
				fail("incorrect key, is=" + result.getStartKey());	

			result = KeyDetector.detectKey("data/test/audio/chords/Maj7sharp11.wav");
			if (result == null)
				fail("key detector did not return a result");
			if (!result.getStartKey().equals(Key.getKey("C")))
				fail("incorrect key, is=" + result.getStartKey());	
			
			result = KeyDetector.detectKey("data/test/audio/chords/Min6.wav");
			if (result == null)
				fail("key detector did not return a result");
			if (!result.getStartKey().equals(Key.getKey("Cm")))
				fail("incorrect key, is=" + result.getStartKey());	
			
			result = KeyDetector.detectKey("data/test/audio/chords/Min7.wav");
			if (result == null)
				fail("key detector did not return a result");
			if (!result.getStartKey().equals(Key.getKey("Cm")))
				fail("incorrect key, is=" + result.getStartKey());	
			
			result = KeyDetector.detectKey("data/test/audio/chords/Min7b9.wav");
			if (result == null)
				fail("key detector did not return a result");
			if (!result.getStartKey().equals(Key.getKey("Cm")))
				fail("incorrect key, is=" + result.getStartKey());	

			result = KeyDetector.detectKey("data/test/audio/chords/Min9.wav");
			if (result == null)
				fail("key detector did not return a result");
			if (!result.getStartKey().equals(Key.getKey("Cm")) && !result.getStartKey().equals(Key.getKey("D#")))
				fail("incorrect key, is=" + result.getStartKey());
			
			result = KeyDetector.detectKey("data/test/audio/chords/Min11.wav");
			if (result == null)
				fail("key detector did not return a result");
			if (!result.getStartKey().equals(Key.getKey("Cm")) && !result.getStartKey().equals(Key.getKey("D#")) && !result.getStartKey().equals(Key.getKey("F")))
				fail("incorrect key, is=" + result.getStartKey());
			
		} catch (Exception e) {
			log.error("testKeyDetectorOnChords(): error", e);
			fail(e.getMessage());
		}
	}
	
	public void testKeyDetectorOnChordsFast() {
		// all wav files...
		if (enableFFMPEG)
			testKeyDetectorOnChords("enable_ffmpeg_codec");
		if (enableJava)
			testKeyDetectorOnChords("enable_java_codecs");
		if (enableXuggle)
			testKeyDetectorOnChords("enable_xuggle_codec");
		if (enableQuicktime)
			testKeyDetectorOnChords("enable_quicktime_codec");		
	}
	
	public void testKeyDetectorOnChordsFast(String activeCodec) {
		log.info("testKeyDetectorOnChords(): activeCodec=" + activeCodec);
		for (String disabledCodec : ALL_CODECS)
			RE3Properties.setProperty(disabledCodec, "false");
		RE3Properties.setProperty(activeCodec, "true");		
		try {
			RE3Properties.setProperty("detect_start_and_end_keys", "false");
			RE3Properties.setProperty("detect_advanced_keys", "false");
			RE3Properties.setProperty("percent_audio_samples_to_process", "10");
			
			DetectedKey result = KeyDetector.detectKey("data/test/audio/chords/A major.wav");
			if (result == null)
				fail("key detector did not return a result");
			if (!result.getStartKey().equals(Key.getKey("A")))
				fail("incorrect key, is=" + result.getStartKey());
			
			result = KeyDetector.detectKey("data/test/audio/chords/C dom7.aif");
			if (result == null)
				fail("key detector did not return a result");
			if (!result.getStartKey().equals(Key.getKey("C")))
				fail("incorrect key, is=" + result.getStartKey());			

			result = KeyDetector.detectKey("data/test/audio/chords/cm-fsharp.wav");
			if (result == null)
				fail("key detector did not return a result");
			if (!result.getStartKey().equals(Key.getKey("Cm"))) // NOTE: should be F#m but due to quality degradation can't help it...
				fail("incorrect key, is=" + result.getStartKey());
			
			result = KeyDetector.detectKey("data/test/audio/chords/C Maj11.wav");
			if (result == null)
				fail("key detector did not return a result");
			if (!result.getStartKey().equals(Key.getKey("C")))
				fail("incorrect key, is=" + result.getStartKey());			

			result = KeyDetector.detectKey("data/test/audio/chords/C Maj6.wav");
			if (result == null)
				fail("key detector did not return a result");
			if (!result.getStartKey().equals(Key.getKey("C")))
				fail("incorrect key, is=" + result.getStartKey());			
			
			result = KeyDetector.detectKey("data/test/audio/chords/C minor.wav");
			if (result == null)
				fail("key detector did not return a result");
			if (!result.getStartKey().equals(Key.getKey("Cm")))
				fail("incorrect key, is=" + result.getStartKey());				

			result = KeyDetector.detectKey("data/test/audio/chords/Dom11.wav");
			if (result == null)
				fail("key detector did not return a result");
			if (!result.getStartKey().equals(Key.getKey("C")))
				fail("incorrect key, is=" + result.getStartKey());	
			
			result = KeyDetector.detectKey("data/test/audio/chords/Dom9.wav");
			if (result == null)
				fail("key detector did not return a result");
			if (!result.getStartKey().equals(Key.getKey("C")))
				fail("incorrect key, is=" + result.getStartKey());	
			
			result = KeyDetector.detectKey("data/test/audio/chords/F minor.wav");
			if (result == null)
				fail("key detector did not return a result");
			if (!result.getStartKey().equals(Key.getKey("Fm")))
				fail("incorrect key, is=" + result.getStartKey());	
			
			result = KeyDetector.detectKey("data/test/audio/chords/Maj7.wav");
			if (result == null)
				fail("key detector did not return a result");
			if (!result.getStartKey().equals(Key.getKey("C")))
				fail("incorrect key, is=" + result.getStartKey());	

			result = KeyDetector.detectKey("data/test/audio/chords/Maj7sharp11.wav");
			if (result == null)
				fail("key detector did not return a result");
			if (!result.getStartKey().equals(Key.getKey("C")))
				fail("incorrect key, is=" + result.getStartKey());	
			
			result = KeyDetector.detectKey("data/test/audio/chords/Min6.wav");
			if (result == null)
				fail("key detector did not return a result");
			if (!result.getStartKey().equals(Key.getKey("Cm")))
				fail("incorrect key, is=" + result.getStartKey());	
			
			result = KeyDetector.detectKey("data/test/audio/chords/Min7.wav");
			if (result == null)
				fail("key detector did not return a result");
			if (!result.getStartKey().equals(Key.getKey("Cm")))
				fail("incorrect key, is=" + result.getStartKey());	
			
			result = KeyDetector.detectKey("data/test/audio/chords/Min7b9.wav");
			if (result == null)
				fail("key detector did not return a result");
			if (!result.getStartKey().equals(Key.getKey("Cm")))
				fail("incorrect key, is=" + result.getStartKey());	

			result = KeyDetector.detectKey("data/test/audio/chords/Min9.wav");
			if (result == null)
				fail("key detector did not return a result");
			if (!result.getStartKey().equals(Key.getKey("D#"))) // NOTE: should probably be Cm? but D# is close...
				fail("incorrect key, is=" + result.getStartKey());
			
			result = KeyDetector.detectKey("data/test/audio/chords/Min11.wav");
			if (result == null)
				fail("key detector did not return a result");
			if (!result.getStartKey().equals(Key.getKey("D#"))) // NOTE: should probably be Cm? but D# is close...
				fail("incorrect key, is=" + result.getStartKey());
			
		} catch (Exception e) {
			log.error("testKeyDetectorOnChordsFast(): error", e);
			fail(e.getMessage());
		}
	}	
	

	public void testKeyDetectorOnChordsStartAndEnd() {
		// all wav files...
		if (enableFFMPEG)
			testKeyDetectorOnChords("enable_ffmpeg_codec");
		if (enableJava)
			testKeyDetectorOnChords("enable_java_codecs");
		if (enableXuggle)
			testKeyDetectorOnChords("enable_xuggle_codec");
		if (enableQuicktime)
			testKeyDetectorOnChords("enable_quicktime_codec");		
	}
	public void testKeyDetectorOnChordsStartAndEnd(String activeCodec) {
		log.info("testKeyDetectorOnChords(): activeCodec=" + activeCodec);
		for (String disabledCodec : ALL_CODECS)
			RE3Properties.setProperty(disabledCodec, "false");
		RE3Properties.setProperty(activeCodec, "true");				
		try {
			RE3Properties.setProperty("detect_start_and_end_keys", "true");
			RE3Properties.setProperty("detect_advanced_keys", "false");
			RE3Properties.setProperty("percent_audio_samples_to_process", "100");

			DetectedKey result = KeyDetector.detectKey("data/test/audio/chords/cm-fsharp.wav");
			if (result == null)
				fail("key detector did not return a result");
			if (!result.getStartKey().equals(Key.getKey("Cm")))
				fail("incorrect key, is=" + result.getStartKey());
			if (!result.getEndKey().equals(Key.getKey("F#")))
				fail("incorrect key, is=" + result.getEndKey());
			
		} catch (Exception e) {
			log.error("testKeyDetectorOnChordsStartAndEnd(): error", e);
			fail(e.getMessage());
		}
	}	

	
}
