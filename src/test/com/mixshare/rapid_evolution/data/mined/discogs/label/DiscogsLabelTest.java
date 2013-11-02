package test.com.mixshare.rapid_evolution.data.mined.discogs.label;

import java.util.Vector;

import org.apache.log4j.Logger;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.mined.MiningAPIFactory;
import com.mixshare.rapid_evolution.data.mined.discogs.artist.DiscogsArtist;
import com.mixshare.rapid_evolution.data.mined.discogs.label.DiscogsLabel;
import com.mixshare.rapid_evolution.data.util.io.XMLSerializer;

public class DiscogsLabelTest extends RE3TestCase {

    static private Logger log = Logger.getLogger(DiscogsLabelTest.class);
    
    public void testNormalLabelAPI() {    	
    	DiscogsLabel label = MiningAPIFactory.getDiscogsAPI().getLabel("Merck", true);
    	checkNormalLabel(label, true);
    	
		XMLSerializer.saveData(label, "data/junit/temp/discogs-label-norm.xml");
		label = (DiscogsLabel)XMLSerializer.readData("data/junit/temp/discogs-label-norm.xml");

    	checkNormalLabel(label, true);		
    }
    public void testNormalLabelNoAPI() {
    	if (RE3Properties.getBoolean("enable_discogs_no_api_access")) {
	    	DiscogsLabel label = MiningAPIFactory.getDiscogsAPI().getLabel("Merck", false);
	    	checkNormalLabel(label, false);
	
			XMLSerializer.saveData(label, "data/junit/temp/discogs-label-norm.xml");
			label = (DiscogsLabel)XMLSerializer.readData("data/junit/temp/discogs-label-norm.xml");
	
	    	checkNormalLabel(label, false);
    	}
    }        
    public void checkNormalLabel(DiscogsLabel label, boolean api) {  

    	// name
    	if (!label.getName().equals("Merck"))  // && !label.getName().equals("Merck Discography at Discogs"))
    		fail("name incorrect, is=" + label.getName());
    		
    	// contact info
    	if (!label.getContactInfo().equals("m3rck@m3rck.net"))
    		fail("incorrect contact info");
    	
    	// image urls
    	Vector<String> imageURLs = new Vector<String>();
    	imageURLs.add(api ? "http://s.dsimg.com/image/L-434-1300281213.jpeg" : "http://www.discogs.com/image/L-434-1300281213.jpeg");
    	if (!areCollectionsEqual(label.getImageURLs(), imageURLs))
    		fail("imageURLs incorrect, is=" + label.getImageURLs());

    	// primary image url
    	if (!label.getPrimaryImageURL().equals(imageURLs.get(0)))
    		fail("primaryImageURL incorrect");
    	
    	// parent label
    	if (!label.getParentLabelName().equals(""))
    		fail("incorrect parent label");
    	
    	// sub labels
    	if (!areCollectionsEqual(label.getSubLabelNames(), new String[] { "Narita Records" }))
    		fail("incorrect sub labels");
    	
    	// as of now, discogs API doesn't give back profile, but screen scraping does...
		// profile
		if (!label.getProfile().startsWith("Owned and operated by Gabe Koch, based in Miami, Florida, US. Started in January 2000, ended in January 2007"))
			fail("profile incorrect, is=" + label.getProfile());
    			
    	// release ids
    	if (!label.getReleaseIDs().contains("1944"))
    		fail("release ids incorrect, is=" + label.getReleaseIDs());
    	if (!label.getReleaseIDs().contains("592457"))
    		fail("release ids incorrect");
    	if (!label.getReleaseIDs().contains("996582"))
    		fail("release ids incorrect");

    	// urls
    	Vector<String> urls = new Vector<String>();
    	urls.add("http://www.merckrecords.com");
    	urls.add("http://www.myspace.com/merckrecords");
    	if (!areCollectionsEqual(label.getURLs(), urls))
    		fail("urls incorrect");
    	
    }
    
    public void testHugeLabelAPI() {    
    	DiscogsLabel label = MiningAPIFactory.getDiscogsAPI().getLabel("Capitol Records", true);
    	checkHugeLabel(label, true);
    	
		XMLSerializer.saveData(label, "data/junit/temp/discogs-label-norm.xml");
		label = (DiscogsLabel)XMLSerializer.readData("data/junit/temp/discogs-label-norm.xml");

		checkHugeLabel(label, true);		    	    	
    }
    public void testHugeLabelNoAPI() {
    	if (RE3Properties.getBoolean("enable_discogs_no_api_access")) {
	    	DiscogsLabel label = MiningAPIFactory.getDiscogsAPI().getLabel("Capitol Records", false);
	    	checkHugeLabel(label, false);
	    	
			XMLSerializer.saveData(label, "data/junit/temp/discogs-label-norm.xml");
			label = (DiscogsLabel)XMLSerializer.readData("data/junit/temp/discogs-label-norm.xml");
	
			checkHugeLabel(label, false);	
    	}	    	    	
    }        
    public void checkHugeLabel(DiscogsLabel label, boolean api) {  

    	// name
    	if (!label.getName().equals("Capitol Records") && !label.getName().equals("Capitol Records Discography at Discogs"))
    		fail("name incorrect, is=" + label.getName());
    		
    	// contact info
    	if (!label.getContactInfo().contains("CapitolRecords.com"))
    		fail("incorrect contact info");
    	
    	// image urls
    	if (!label.getImageURLs().contains(api ? "http://s.dsimg.com/image/L-654-1240588000.jpeg" : "http://www.discogs.com/image/L-654-1240588000.jpeg"))    			
    		fail("missing image in=" + label.getImageURLs());

    	// primary image url
    	if (!label.getPrimaryImageURL().equals(api ? "http://s.dsimg.com/image/L-654-1231967064.png" : "http://www.discogs.com/image/L-654-1231967064.png"))
    		fail("primaryImageURL incorrect, is=" + label.getPrimaryImageURL());
    	
    	// parent label
    	if (!label.getParentLabelName().equals("EMI"))
    		fail("incorrect parent label, is=" + label.getParentLabelName());
    	
    	// sub labels
    	if (!label.getSubLabelNames().contains("Metro Blue"))
    		fail("missing sub label=Blue Note");
    	if (!label.getSubLabelNames().contains("The Right Stuff"))
    		fail("missing sub label=The Right Stuff");
    	
    	// as of now, discogs API doesn't give back profile, but screen scraping does...
		// profile
		if (!label.getProfile().startsWith("The Capitol Records company was founded by the songwriter Johnny"))
			fail("profile incorrect, is=" + label.getProfile());
    			
    	// release ids
    	if (!label.getReleaseIDs().contains("1671890"))
    		fail("release ids incorrect, is=" + label.getReleaseIDs());
    	if (!label.getReleaseIDs().contains("164519"))
    		fail("release ids incorrect");
    	if (!label.getReleaseIDs().contains("552574"))
    		fail("release ids incorrect");

    	// urls
    	if (!label.getURLs().contains("http://www.capitolrecords.com"))
    		fail("missing url");
    	if (!label.getURLs().contains("http://www.hollywoodandvine.com"))
    		fail("missing url");
    	
    }    

}
