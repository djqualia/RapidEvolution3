package test.com.mixshare.rapid_evolution.data.mined.echonest.song;

import org.apache.log4j.Logger;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.data.mined.echonest.song.EchonestSongProfile;
import com.mixshare.rapid_evolution.data.util.io.XMLSerializer;

public class EchonestSongProfileTest extends RE3TestCase {

    static private Logger log = Logger.getLogger(EchonestSongProfileTest.class);
	
	public void testEchonestSongProfileBasic() {
		try {
			EchonestSongProfile songProfile = new EchonestSongProfile("data/test/audio/songs/germanic.mp3", "Deceptikon", "Germanic");
			
			checkProfile(songProfile);
			
			XMLSerializer.saveData(songProfile, "data/junit/temp/echonest-song.xml");
			songProfile = (EchonestSongProfile)XMLSerializer.readData("data/junit/temp/echonest-song.xml");
			
			checkProfile(songProfile);
						
		} catch (Exception e) {
			log.error("testEchonestSongProfileBasic(): error", e);
			fail(e.getMessage());
		}
	}
	
	private void checkProfile(EchonestSongProfile songProfile) {
		if (songProfile.getBpmValue() != 106.018f)
			fail("incorrect bpm");

		if (!songProfile.getKeyValue().toString().equals("Am"))
			fail("incorrect key");

		if (songProfile.getKeyConfidence() != 0.0f)
			fail("incorrect key confidence, is=" + songProfile.getKeyConfidence());

		if (songProfile.getModeConfidence() != 0.0f)
			fail("incorrect mode confidence, is=" + songProfile.getModeConfidence());
		
		if (songProfile.getOverallLoudness() != -7.606f)
			fail("incorrect loudness");		
	}
	
}
