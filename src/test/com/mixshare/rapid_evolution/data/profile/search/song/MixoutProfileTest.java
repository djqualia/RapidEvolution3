package test.com.mixshare.rapid_evolution.data.profile.search.song;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.data.identifier.search.song.MixoutIdentifier;
import com.mixshare.rapid_evolution.data.profile.search.song.MixoutProfile;
import com.mixshare.rapid_evolution.data.record.search.song.MixoutRecord;
import com.mixshare.rapid_evolution.data.util.io.XMLSerializer;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineReaderFactory;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.mixshare.rapid_evolution.util.io.LineWriterFactory;

public class MixoutProfileTest extends RE3TestCase {

	public void testSerialization() {
		try {		
			MixoutProfile mixoutProfile = new MixoutProfile();
			
			MixoutRecord mixout = new MixoutRecord();			
			mixoutProfile.setRecord(mixout);
			
			mixoutProfile.setRatingSource((byte)1);
			
			// mixout profile specific
			// common
			mixout.setId(new MixoutIdentifier(1, 2));			
			mixout.setUniqueId(420);
			
			int[] dupIds = new int[] { 1, 2 };
			mixout.setDuplicateIds(dupIds);
			
			mixout.setRating((byte)80);
			mixout.setDisabled(true);

			// search record specific
			mixout.setBpmDiff(3.4f);
			mixout.setType((byte)2);
			mixout.setSyncedWithMixshare(true);
			mixout.setComments("blah");
			
			mixout.setLastModified(234);
			
			XMLSerializer.saveData(mixoutProfile, "data/junit/temp/mixout_profile.xml");
			mixoutProfile = (MixoutProfile)XMLSerializer.readData("data/junit/temp/mixout_profile.xml");
			mixout = mixoutProfile.getMixoutRecord();			
			
			checkValues(mixoutProfile, mixout);
			
			LineWriter writer = LineWriterFactory.getLineWriter("data/junit/temp/mixout_profile.xml");
			mixoutProfile.write(writer);
			writer.close();
			LineReader lineReader = LineReaderFactory.getLineReader("data/junit/temp/mixout_profile.xml");
			mixoutProfile = new MixoutProfile(lineReader);
			mixout = mixoutProfile.getMixoutRecord();	

			checkValues(mixoutProfile, mixout);
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	private void checkValues(MixoutProfile mixoutProfile, MixoutRecord mixout) {
		if (mixoutProfile.getRatingSource() != (byte)1)
			fail();
		
		// common
		if (!mixout.getId().equals(new MixoutIdentifier(1, 2)))
			fail("id incorrect");			
		if (mixout.getUniqueId() != 420)
			fail("unique id incorrect");

		if (mixout.getDuplicateIds()[0] != 1)
			fail("dup id incorrect");
		if (mixout.getDuplicateIds()[1] != 2)
			fail("dup id incorrect");
		
		if (mixout.getRating() != (byte)80)
			fail("rating incorrect");
		if (!mixout.isDisabled())
			fail("not disabled");

		if (mixout.getBpmDiff() != 3.4f)
			fail();
		if (mixout.getType() != (byte)2)
			fail();
		if (!mixout.isSyncedWithMixshare())
			fail();
		if (!mixout.getComments().equals("blah"))
			fail();
		
		if (mixout.getLastModified() != 234)
			fail("last modified incorrect, is=" + mixout.getLastModified());		
	}
	
}
