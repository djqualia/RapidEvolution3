package test.com.mixshare.rapid_evolution.audio.util;

import org.apache.log4j.Logger;

import test.RE3TestCase;
import test.com.mixshare.rapid_evolution.audio.tags.TagReaderTest;

import com.mixshare.rapid_evolution.audio.util.AudioUtil;

public class AudioUtilTest extends RE3TestCase {

	static private Logger log = Logger.getLogger(TagReaderTest.class);	

	public void testGetDuration() {
		try {
			// mp3
			if (!AudioUtil.getDuration("data/test/audio/songs/freaktimebaby.mp3").toString().equals("2:54"))
				fail("incorrect duration=" + AudioUtil.getDuration("data/test/audio/songs/freaktimebaby.mp3").toString());
			if (!AudioUtil.getDuration("data/test/audio/songs/germanic.mp3").toString().equals("3:08"))
				fail("incorrect duration=" + AudioUtil.getDuration("data/test/audio/songs/germanic.mp3").toString());
			if (!AudioUtil.getDuration("data/test/audio/songs/tha.mp3").toString().equals("9:06"))
				fail("incorrect duration=" + AudioUtil.getDuration("data/test/audio/songs/tha.mp3").toString());
			
			// m4a
			if (!AudioUtil.getDuration("data/test/audio/songs/rastic chevch.m4a").toString().equals("5:32"))
				fail("incorrect duration=" + AudioUtil.getDuration("data/test/audio/songs/rastic chevch.m4a").toString());
			
			// ogg
			if (!AudioUtil.getDuration("data/test/audio/songs/jeroensnake_-_Echo_Guitar_Jeroen.ogg").toString().equals("1:25"))
				fail("incorrect duration=" + AudioUtil.getDuration("data/test/audio/songs/jeroensnake_-_Echo_Guitar_Jeroen.ogg").toString());
			
			// flac
			if (!AudioUtil.getDuration("data/test/audio/songs/2.flac").toString().equals("0:11"))
				fail("incorrect duration=" + AudioUtil.getDuration("data/test/audio/songs/2.flac").toString());

			// wma
			if (!AudioUtil.getDuration("data/test/audio/songs/sample.wma").toString().equals("0:05"))
				fail("incorrect duration=" + AudioUtil.getDuration("data/test/audio/songs/sample.wma").toString());

			// ape
			if (!AudioUtil.getDuration("data/test/audio/songs/sh3.ape").toString().equals("0:15"))
				fail("incorrect duration=" + AudioUtil.getDuration("data/test/audio/songs/sh3.ape").toString());

			// wav
			if (!AudioUtil.getDuration("data/test/audio/songs/loop.wav").toString().equals("0:05"))
				fail("incorrect duration=" + AudioUtil.getDuration("data/test/audio/songs/loop.wav").toString());
			
		} catch (Exception e) {
			log.error("testGetDuration(): error", e);
			fail(e.getMessage());
		}
	}
	
}
	