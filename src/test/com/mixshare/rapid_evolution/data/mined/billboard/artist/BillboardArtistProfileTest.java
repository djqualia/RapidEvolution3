package test.com.mixshare.rapid_evolution.data.mined.billboard.artist;

import org.apache.log4j.Logger;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.data.mined.billboard.artist.BillboardArtistProfile;
import com.mixshare.rapid_evolution.data.util.io.XMLSerializer;

public class BillboardArtistProfileTest extends RE3TestCase {

    static private Logger log = Logger.getLogger(BillboardArtistProfileTest.class);
	
	public void testBillboardArtistProfileBasic() {
		try {
			BillboardArtistProfile artistProfile = new BillboardArtistProfile("Daft Punk");
			
			testBillboardArtistProfileBasicSub(artistProfile);
			
			XMLSerializer.saveData(artistProfile, "data/junit/temp/billboard-artist.xml");
			artistProfile = (BillboardArtistProfile)XMLSerializer.readData("data/junit/temp/billboard-artist.xml");
				
			testBillboardArtistProfileBasicSub(artistProfile);
			
		} catch (Exception e) {
			log.error("testBillboardArtistProfileBasic(): error", e);
			fail(e.getMessage());
		}
	}
	
	private void testBillboardArtistProfileBasicSub(BillboardArtistProfile artistProfile) {
		if (!artistProfile.isValid())
			fail("invalid profile retrieved");			
		if (artistProfile.getTotalWeeksOn() == 0)
			fail("invalid total weeks on");
		if (artistProfile.getChartEntries().size() == 0)
			fail("missing chart entries");					
		if (artistProfile.getHeader().getDataType() != DATA_TYPE_ARTISTS)
			fail("bad data type");
		if (artistProfile.getHeader().getDataSource() != DATA_SOURCE_BILLBOARD)
			fail("bad data source");
	}
	
}
