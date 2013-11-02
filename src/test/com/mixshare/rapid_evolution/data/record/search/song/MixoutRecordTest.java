package test.com.mixshare.rapid_evolution.data.record.search.song;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.data.identifier.search.song.MixoutIdentifier;
import com.mixshare.rapid_evolution.data.record.search.song.MixoutRecord;
import com.mixshare.rapid_evolution.data.util.io.XMLSerializer;
import com.mixshare.rapid_evolution.music.key.Key;

public class MixoutRecordTest extends RE3TestCase {

	public void testSerialization() {
		try {		
			MixoutRecord mixout = new MixoutRecord();
			
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
			
			XMLSerializer.saveData(mixout, "data/junit/temp/mixout.xml");
			mixout = (MixoutRecord)XMLSerializer.readData("data/junit/temp/mixout.xml");

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
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
}
