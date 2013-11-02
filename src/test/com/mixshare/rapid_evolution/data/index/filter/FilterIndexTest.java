package test.com.mixshare.rapid_evolution.data.index.filter;

import org.apache.log4j.Logger;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.profile.filter.style.StyleProfile;
import com.mixshare.rapid_evolution.data.record.filter.style.StyleRecord;
import com.mixshare.rapid_evolution.data.submitted.filter.style.SubmittedStyle;

public class FilterIndexTest extends RE3TestCase {

    static private Logger log = Logger.getLogger(FilterIndexTest.class);    
	
	/**
	 * This tests creating 2 different filters (in this case, styles), and merging them, and making sure
	 * the database behaves as expected (only 1 style exists, but the style can be looked up with both names, etc).
	 */
	public void testDuplicateFilterBasic() {
		try {
			// create style 1			
			SubmittedStyle style1 = new SubmittedStyle("breaks");
			StyleProfile styleProfile1 = Database.getStyleIndex().addStyle(style1);			
			if (styleProfile1 == null)
				fail("style not created");
			
			// create style 2
			SubmittedStyle style2 = new SubmittedStyle("breakbeat");			
			StyleProfile styleProfile2 = Database.getStyleIndex().addStyle(style2);
			if (styleProfile2 == null)
				fail("style not created");
			
			// check state of database
			if (Database.getStyleIndex().getIds().size() != 2)
				fail("incorrect # styles");			
			if (!Database.getStyleIndex().doesExist(style1.getStyleIdentifier()))
				fail("style doesn't exist");
			if (!Database.getStyleIndex().doesExist(style2.getStyleIdentifier()))
				fail("style doesn't exist");
						
			// merge the styles and check the state of the database
			Database.getStyleIndex().mergeProfiles(styleProfile1, styleProfile2);
			
			if (Database.getStyleIndex().getIds().size() != 1)
				fail("incorrect # styles");			
			if (!Database.getStyleIndex().doesExist(style1.getStyleIdentifier()))
				fail("style doesn't exist");
			if (!Database.getStyleIndex().doesExist(style2.getStyleIdentifier()))
				fail("style doesn't exist");
			
			// make sure getting the old style record/profile returns the primary one
			if (!Database.getStyleIndex().getStyleRecord(style2.getIdentifier()).getStyleName().equals("breaks"))
				fail("primary style record not returned");			
			if (!Database.getStyleIndex().getStyleProfile(style2.getIdentifier()).getStyleName().equals("breaks"))
				fail("primary style profile not returned");
			
			// make sure the duplicate id has been properly stored in the primary
			StyleRecord primaryStyle = Database.getStyleIndex().getStyleRecord(style1.getStyleIdentifier());
			if (primaryStyle.getDuplicateId(0) != styleProfile2.getUniqueId())
				fail("duplicate id not recorded");
			
		} catch (Exception e) {
			log.error("testDuplicateFilterBasic(): error", e);
			fail(e.getMessage());
		}
	}
	
}
