package test.com.mixshare.rapid_evolution.data.record.search.label;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.data.identifier.search.label.LabelIdentifier;
import com.mixshare.rapid_evolution.data.record.search.label.LabelRecord;
import com.mixshare.rapid_evolution.data.util.io.XMLSerializer;

public class LabelRecordTest extends RE3TestCase {

	public void testSerialization() {
		try {		
			LabelRecord label = new LabelRecord();
			
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
		    label.setDiscogsLabelName("border community (2)");
		    
		    label.setArtistIds(new int[] { 1 });
		    label.setArtistDegrees(new float[] { 1.0f });		    
		    
			label.setLastModified(234);
			
			XMLSerializer.saveData(label, "data/junit/temp/label.xml");
			label = (LabelRecord)XMLSerializer.readData("data/junit/temp/label.xml");

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
			if (!label.getDiscogsLabelName().equals("border community (2)"))
				fail("is=" + label.getDiscogsLabelName());

			if (label.getArtistIds()[0] != 1)
				fail();
			if (label.getArtistDegrees()[0] != 1.0f)
				fail();
			
			if (label.getLastModified() != 234)
				fail("last modified incorrect, is=" + label.getLastModified());
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
}
