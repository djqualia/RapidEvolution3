package test.com.mixshare.rapid_evolution.data.mined.billboard.song;

import org.apache.log4j.Logger;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.data.mined.billboard.song.BillboardSongProfile;
import com.mixshare.rapid_evolution.data.util.io.XMLSerializer;

public class BillboardSongProfileTest extends RE3TestCase {

    static private Logger log = Logger.getLogger(BillboardSongProfileTest.class);
	
	public void testBillboardSongProfileBasic() {
		try {
			BillboardSongProfile songProfile = new BillboardSongProfile("Beck", "Loser");
			
			testBillboardSongProfileBasicSub(songProfile);
			
			XMLSerializer.saveData(songProfile, "data/junit/temp/billboard-song.xml");
			songProfile = (BillboardSongProfile)XMLSerializer.readData("data/junit/temp/billboard-song.xml");
				
			testBillboardSongProfileBasicSub(songProfile);
			
		} catch (Exception e) {
			log.error("testBillboardSongProfileBasic(): error", e);
			fail(e.getMessage());
		}
	}
	
	private void testBillboardSongProfileBasicSub(BillboardSongProfile songProfile) {
		if (!songProfile.isValid())
			fail("invalid profile retrieved");			
		if (songProfile.getTotalWeeksOn() == 0)
			fail("invalid total weeks on");
		if (songProfile.getChartEntries().size() == 0)
			fail("missing chart entries");
		if (!songProfile.getYearsOnCharts().contains(1994))
			fail("missing year info from charts");		
		if (songProfile.getHeader().getDataType() != DATA_TYPE_SONGS)
			fail("bad data type");
		if (songProfile.getHeader().getDataSource() != DATA_SOURCE_BILLBOARD)
			fail("bad data source");
	}
	
}
