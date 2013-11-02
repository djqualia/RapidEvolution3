package test.com.mixshare.rapid_evolution.audio.detection.bpm;

import org.apache.log4j.Logger;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.audio.detection.bpm.BpmDetector;
import com.mixshare.rapid_evolution.audio.detection.bpm.DetectedBpm;

public class BpmDetectorTest extends RE3TestCase {

	static private Logger log = Logger.getLogger(BpmDetectorTest.class);	
	
	static private String[] ALL_CODECS = { "enable_quicktime_codec", "enable_ffmpeg_codec", "enable_faad2_codec", "enable_java_codecs", "enable_phonon_codec", "enable_xuggle_codec"};
	
	public void testBpmDetector() {
		testJavaCodec("enable_java_codecs");
		testFFMPEGCodec("enable_ffmpeg_codec");
		testXuggleCodec("enable_xuggle_codec");
		testFAAD2Codec("enable_faad2_codec");
	}
	
	public void testFFMPEGCodec(String activeCodec) {
		log.info("testBpmDetectorOnSongs(): activeCodec=" + activeCodec);
		for (String disabledCodec : ALL_CODECS)
			RE3Properties.setProperty(disabledCodec, "false");
		RE3Properties.setProperty(activeCodec, "true");		
		try {
			RE3Properties.setProperty("bpm_detector_quality", "0");
			
			DetectedBpm result = BpmDetector.detectBpm("data/test/audio/songs/germanic.mp3");
			if (result == null)
				fail("bpm detector did not return a result");
			if (result.getBpm().getBpmValue() != 105.8f)
				fail("incorrect bpm, is=" + result.getBpm());
			
			result = BpmDetector.detectBpm("data/test/audio/songs/freaktimebaby.mp3");
			if (result == null)
				fail("bpm detector did not return a result");
			if (result.getBpm().getBpmValue() != 103.6f)
				fail("incorrect bpm, is=" + result.getBpm());			

			result = BpmDetector.detectBpm("data/test/audio/songs/2.flac");
			if (result == null)
				fail("bpm detector did not return a result");
			if (result.getBpm().getBpmValue() != 114.7f)
				fail("incorrect bpm, is=" + result.getBpm());	
			
			result = BpmDetector.detectBpm("data/test/audio/songs/sh3.ape");
			if (result == null)
				fail("bpm detector did not return a result");
			if (result.getBpm().getBpmValue() != 99.0f)
				fail("incorrect bpm, is=" + result.getBpm());
			
			
			result = BpmDetector.detectBpm("data/test/audio/songs/rastic chevch.m4a");
			if (result == null)
				fail("bpm detector did not return a result");
			if (result.getBpm().getBpmValue() != 99.8f)
				fail("incorrect bpm, is=" + result.getBpm());				
			
		} catch (Exception e) {
			log.error("testBpmDetector(): error", e);
			fail(e.getMessage());
		}
	}	
	
	public void testXuggleCodec(String activeCodec) {
		log.info("testBpmDetectorOnSongs(): activeCodec=" + activeCodec);
		for (String disabledCodec : ALL_CODECS)
			RE3Properties.setProperty(disabledCodec, "false");
		RE3Properties.setProperty(activeCodec, "true");		
		try {
			RE3Properties.setProperty("bpm_detector_quality", "0");
			
			DetectedBpm result = BpmDetector.detectBpm("data/test/audio/songs/germanic.mp3");
			if (result == null)
				fail("bpm detector did not return a result");
			if (result.getBpm().getBpmValue() != 105.8f)
				fail("incorrect bpm, is=" + result.getBpm());
			
			/*
			result = BpmDetector.detectBpm("data/test/audio/songs/freaktimebaby.mp3");
			if (result == null)
				fail("bpm detector did not return a result");
			if (result.getBpm().getBpmValue() != 103.6f)
				fail("incorrect bpm, is=" + result.getBpm());
			 */
			
			result = BpmDetector.detectBpm("data/test/audio/songs/2.flac");
			if (result == null)
				fail("bpm detector did not return a result");
			if (result.getBpm().getBpmValue() != 114.7f)
				fail("incorrect bpm, is=" + result.getBpm());	
			
			/*
			result = BpmDetector.detectBpm("data/test/audio/songs/sh3.ape");
			if (result == null)
				fail("bpm detector did not return a result");
			if (result.getBpm().getBpmValue() != 99.0f)
				fail("incorrect bpm, is=" + result.getBpm());
			*/
			
			result = BpmDetector.detectBpm("data/test/audio/songs/rastic chevch.m4a");
			if (result == null)
				fail("bpm detector did not return a result");
			if (result.getBpm().getBpmValue() != 99.8f)
				fail("incorrect bpm, is=" + result.getBpm());				
			
		} catch (Exception e) {
			log.error("testBpmDetector(): error", e);
			fail(e.getMessage());
		}
	}		
	
	public void testJavaCodec(String activeCodec) {
		log.info("testBpmDetectorOnSongs(): activeCodec=" + activeCodec);
		for (String disabledCodec : ALL_CODECS)
			RE3Properties.setProperty(disabledCodec, "false");
		RE3Properties.setProperty(activeCodec, "true");		
		try {
			RE3Properties.setProperty("bpm_detector_quality", "0");
			
			/*
			DetectedBpm result = BpmDetector.detectBpm("data/test/audio/songs/germanic.mp3");
			if (result == null)
				fail("bpm detector did not return a result");
			if (result.getBpm().getBpmValue() != 106.1f)
				fail("incorrect bpm, is=" + result.getBpm());
			*/
			
			DetectedBpm result = BpmDetector.detectBpm("data/test/audio/songs/freaktimebaby.mp3");
			if (result == null)
				fail("bpm detector did not return a result");
			if (result.getBpm().getBpmValue() != 103.6f)
				fail("incorrect bpm, is=" + result.getBpm());			

			result = BpmDetector.detectBpm("data/test/audio/songs/2.flac");
			if (result == null)
				fail("bpm detector did not return a result");
			if (result.getBpm().getBpmValue() != 114.7f)
				fail("incorrect bpm, is=" + result.getBpm());	
						
			/*
			result = BpmDetector.detectBpm("data/test/audio/songs/jeroensnake_-_Echo_Guitar_Jeroen.ogg");
			if (result == null)
				fail("bpm detector did not return a result");
			if (result.getBpm().getBpmValue() != 103.8f)
				fail("incorrect bpm, is=" + result.getBpm());	
			*/
			
		} catch (Exception e) {
			log.error("testBpmDetector(): error", e);
			fail(e.getMessage());
		}
	}	
		
	public void testFAAD2Codec(String activeCodec) {
		log.info("testBpmDetectorOnSongs(): activeCodec=" + activeCodec);
		for (String disabledCodec : ALL_CODECS)
			RE3Properties.setProperty(disabledCodec, "false");
		RE3Properties.setProperty(activeCodec, "true");		
		try {
			RE3Properties.setProperty("bpm_detector_quality", "0");			
			
			DetectedBpm result = BpmDetector.detectBpm("data/test/audio/songs/rastic chevch.m4a");
			if (result == null)
				fail("bpm detector did not return a result");
			if (result.getBpm().getBpmValue() != 99.8f)
				fail("incorrect bpm, is=" + result.getBpm());				
			
		} catch (Exception e) {
			log.error("testBpmDetector(): error", e);
			fail(e.getMessage());
		}
	}	
	
}
