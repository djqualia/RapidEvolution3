package test.com.mixshare.rapid_evolution.data.profile.search.label;

import org.apache.log4j.Logger;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.data.identifier.search.label.LabelIdentifier;
import com.mixshare.rapid_evolution.data.profile.search.label.LabelProfile;
import com.mixshare.rapid_evolution.data.record.search.label.LabelRecord;
import com.mixshare.rapid_evolution.data.util.io.XMLSerializer;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineReaderFactory;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.mixshare.rapid_evolution.util.io.LineWriterFactory;

public class LabelProfileTest extends RE3TestCase {

    static private Logger log = Logger.getLogger(LabelProfileTest.class);    
	
	public void testSerialization() {
		try {		
			LabelProfile labelProfile = new LabelProfile();
			
			LabelRecord label = new LabelRecord();			
			labelProfile.setRecord(label);
			
			labelProfile.setRatingSource((byte)1);
			
			labelProfile.setDiscogsLabelNameSource((byte)2);
						
			// common
			label.setId(new LabelIdentifier("border community"));			
			label.setUniqueId(420);
			
			int[] dupIds = new int[] { 1, 2 };
			label.setDuplicateIds(dupIds);
			
			label.setRating((byte)80);
			label.setDisabled(true);

			// search record specific
			label.setSourceStyleIds(new int[] { 1 });
			label.setSourceStyleDegrees(new float[] { 0.5f });
			label.setSourceStyleSources(new byte[] { (byte)2 });
			label.setActualStyleIds(new int[] { 1 });
			label.setActualStyleDegrees(new float[] { 0.7f });
						
			label.setSourceTagIds(new int[] { 2 });
			label.setSourceTagDegrees(new float[] { 1.0f });
			label.setSourceTagSources(new byte[] { (byte)1 });
			label.setActualTagIds(new int[] { 2 });
			label.setActualTagDegrees(new float[] { 0.9f });
			
		    label.setScore(70.0f);
		    label.setPopularity(30.0f);

		    label.setComments("comments");
		    
		    label.setThumbnailImageFilename("thumb.gif");

		    label.setUserDataTypes(new short[] { 2 });
		    label.setUserData(new Object[] { (Object)3 });

		    label.setMinedProfileSources(new byte[] { (byte)3 });
		    label.setMinedProfileSourcesLastUpdated(new long[] { 123 });

		    label.setExternalItem(true);
		    
		    label.setPlayCount(34);

		    // label specific
		    label.setArtistIds(new int[] { 5 });
		    label.setArtistDegrees(new float[] { 1.0f });
		    			
			label.setLastModified(234);
			
			XMLSerializer.saveData(labelProfile, "data/junit/temp/label_profile.xml");
			labelProfile = (LabelProfile)XMLSerializer.readData("data/junit/temp/label_profile.xml");
			label = labelProfile.getLabelRecord();			
			
			checkValues(labelProfile, label);
			
			LineWriter writer = LineWriterFactory.getLineWriter("data/junit/temp/label_profile.xml");
			labelProfile.write(writer);
			writer.close();
			LineReader lineReader = LineReaderFactory.getLineReader("data/junit/temp/label_profile.xml");
			labelProfile = new LabelProfile(lineReader);
			label = labelProfile.getLabelRecord();		

			checkValues(labelProfile, label);
			
		} catch (Exception e) {
			log.error("testSerialization(): error", e);
			fail(e.getMessage());
		}
	}

	private void checkValues(LabelProfile labelProfile, LabelRecord label) {
		if (labelProfile.getRatingSource() != (byte)1)
			fail();

		if (labelProfile.getDiscogsLabelNameSource() != (byte)2)
			fail();
		
		// common
		if (!label.getId().equals(new LabelIdentifier("border community")))
			fail("id incorrect");			
		if (label.getUniqueId() != 420)
			fail("unique id incorrect");

		if (label.getDuplicateIds()[0] != 1)
			fail("dup id incorrect");
		if (label.getDuplicateIds()[1] != 2)
			fail("dup id incorrect");
		
		if (label.getRating() != (byte)80)
			fail("rating incorrect");
		if (!label.isDisabled())
			fail("not disabled");

		// search record specific
		if (label.getSourceStyleIds()[0] != 1)
			fail();
		if (label.getSourceStyleDegrees()[0] != 0.5f)
			fail();
		if (label.getSourceStyleSources()[0] != (byte)2)
			fail();
		if (label.getActualStyleIds()[0] != 1)
			fail();
		if (label.getActualStyleDegrees()[0] != 0.7f)
			fail();

		if (label.getSourceTagIds()[0] != 2)
			fail();
		if (label.getSourceTagDegrees()[0] != 1.0f)
			fail();
		if (label.getSourceTagSources()[0] != (byte)1)
			fail();
		if (label.getActualTagIds()[0] != 2)
			fail();
		if (label.getActualTagDegrees()[0] != 0.9f)
			fail();
		
		if (label.getScore() != 70.0f)
			fail();
		if (label.getPopularity() != 30.0f)
			fail();
			
		if (!label.getComments().equals("comments"))
			fail();
		if (!label.getThumbnailImageFilename().equals("thumb.gif"))
			fail();
		
		if (label.getUserDataTypes()[0] != (byte)2)
			fail();
		if (!label.getUserData()[0].toString().equals("3"))				
			fail("is=" + label.getUserData()[0]);

		if (label.getMinedProfileSources()[0] != (byte)3)
			fail();
		if (label.getMinedProfileSourcesLastUpdated()[0] != 123)
			fail();

		if (!label.isExternalItem())
			fail();
		
		if (label.getPlayCount() != 34)
			fail();
	    
		// label specific			
		if (label.getArtistIds()[0] != 5)
			fail();
		
		if (label.getLastModified() != 234)
			fail("last modified incorrect, is=" + label.getLastModified());
		
	}
	
	public void testBadXML() {
		try {		
			LabelProfile labelProfile = (LabelProfile)XMLSerializer.readData("data/junit/bad/267.xml");
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
}
