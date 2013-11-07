package test.com.mixshare.rapid_evolution.data.profile.search.release;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.data.identifier.search.release.ReleaseIdentifier;
import com.mixshare.rapid_evolution.data.profile.search.release.ReleaseProfile;
import com.mixshare.rapid_evolution.data.record.search.release.ReleaseRecord;
import com.mixshare.rapid_evolution.data.util.io.XMLSerializer;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineReaderFactory;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.mixshare.rapid_evolution.util.io.LineWriterFactory;

public class ReleaseProfileTest extends RE3TestCase {

	public void testSerialization() {
		try {		
			ReleaseProfile releaseProfile = new ReleaseProfile();
			
			ReleaseRecord release = new ReleaseRecord();			
			releaseProfile.setRecord(release);
			
			releaseProfile.setRatingSource((byte)1);
			
			releaseProfile.setOriginalYearReleasedSource((byte)2);
			
			// common
			release.setId(new ReleaseIdentifier("aphex twin", "selected ambient works"));			
			release.setUniqueId(420);
			
			int[] dupIds = new int[] { 1, 2 };
			release.setDuplicateIds(dupIds);
			
			release.setRating((byte)80);
			release.setDisabled(true);

			// search record specific
			release.setSourceStyleIds(new int[] { 1 });
			release.setSourceStyleDegrees(new float[] { 0.5f });
			release.setSourceStyleSources(new byte[] { (byte)2 });
			release.setActualStyleIds(new int[] { 1 });
			release.setActualStyleDegrees(new float[] { 0.7f });
						
			release.setSourceTagIds(new int[] { 2 });
			release.setSourceTagDegrees(new float[] { 1.0f });
			release.setSourceTagSources(new byte[] { (byte)1 });
			release.setActualTagIds(new int[] { 2 });
			release.setActualTagDegrees(new float[] { 0.9f });
			
		    release.setScore(70.0f);
		    release.setPopularity(30.0f);

		    release.setComments("comments");
		    
		    release.setThumbnailImageFilename("thumb.gif");

		    release.setUserDataTypes(new short[] { 2 });
		    release.setUserData(new Object[] { (Object)3 });

		    release.setMinedProfileSources(new byte[] { (byte)3 });
		    release.setMinedProfileSourcesLastUpdated(new long[] { 123 });

		    release.setExternalItem(true);
		    
		    release.setPlayCount(34);

		    // release specific
		    release.setLabelIds(new int[] { 5 });
		    release.setOriginalYearReleased((short)2001);
		    			
			release.setLastModified(234);
			
			XMLSerializer.saveData(releaseProfile, "data/junit/temp/release_profile.xml");
			releaseProfile = (ReleaseProfile)XMLSerializer.readData("data/junit/temp/release_profile.xml");
			release = releaseProfile.getReleaseRecord();			
			
			checkValues(releaseProfile, release);
			
			LineWriter writer = LineWriterFactory.getLineWriter("data/junit/temp/release_profile.xml");
			releaseProfile.write(writer);
			writer.close();
			LineReader lineReader = LineReaderFactory.getLineReader("data/junit/temp/release_profile.xml");
			releaseProfile = new ReleaseProfile(lineReader);
			release = releaseProfile.getReleaseRecord();					

			checkValues(releaseProfile, release);
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	private void checkValues(ReleaseProfile releaseProfile, ReleaseRecord release) {
		if (releaseProfile.getRatingSource() != (byte)1)
			fail();

		if (releaseProfile.getOriginalYearReleasedSource() != (byte)2)
			fail();

		// common
		if (!release.getId().equals(new ReleaseIdentifier("aphex twin", "selected ambient works")))
			fail("id incorrect");			
		if (release.getUniqueId() != 420)
			fail("unique id incorrect");

		if (release.getDuplicateIds()[0] != 1)
			fail("dup id incorrect");
		if (release.getDuplicateIds()[1] != 2)
			fail("dup id incorrect");
		
		if (release.getRating() != (byte)80)
			fail("rating incorrect");
		if (!release.isDisabled())
			fail("not disabled");

		// search record specific
		if (release.getSourceStyleIds()[0] != 1)
			fail();
		if (release.getSourceStyleDegrees()[0] != 0.5f)
			fail();
		if (release.getSourceStyleSources()[0] != (byte)2)
			fail();
		if (release.getActualStyleIds()[0] != 1)
			fail();
		if (release.getActualStyleDegrees()[0] != 0.7f)
			fail();

		if (release.getSourceTagIds()[0] != 2)
			fail();
		if (release.getSourceTagDegrees()[0] != 1.0f)
			fail();
		if (release.getSourceTagSources()[0] != (byte)1)
			fail();
		if (release.getActualTagIds()[0] != 2)
			fail();
		if (release.getActualTagDegrees()[0] != 0.9f)
			fail();
		
		if (release.getScore() != 70.0f)
			fail();
		if (release.getPopularity() != 30.0f)
			fail();
			
		if (!release.getComments().equals("comments"))
			fail();
		if (!release.getThumbnailImageFilename().equals("thumb.gif"))
			fail();
		
		if (release.getUserDataTypes()[0] != (byte)2)
			fail();
		if (!release.getUserData()[0].toString().equals("3"))				
			fail("is=" + release.getUserData()[0]);

		if (release.getMinedProfileSources()[0] != (byte)3)
			fail();
		if (release.getMinedProfileSourcesLastUpdated()[0] != 123)
			fail();

		if (!release.isExternalItem())
			fail();
		
		if (release.getPlayCount() != 34)
			fail();
	    
		// release specific			
		if (release.getLabelIds()[0] != 5)
			fail();
		if (release.getOriginalYearReleased() != (short)2001)
			fail();
		
		if (release.getLastModified() != 234)
			fail("last modified incorrect, is=" + release.getLastModified());		
	}
	
}
