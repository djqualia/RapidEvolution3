package test.com.mixshare.rapid_evolution.data.mined.discogs.artist;

import java.util.Vector;

import org.apache.log4j.Logger;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.mined.MiningAPIFactory;
import com.mixshare.rapid_evolution.data.mined.discogs.DiscogsAPIWrapper;
import com.mixshare.rapid_evolution.data.mined.discogs.artist.DiscogsArtist;
import com.mixshare.rapid_evolution.data.submitted.search.song.SubmittedSong;
import com.mixshare.rapid_evolution.data.util.io.XMLSerializer;

public class DiscogsArtistTest extends RE3TestCase {

    static private Logger log = Logger.getLogger(DiscogsArtistTest.class);

    public void testActualArtistNameA() {
    	try {
			SubmittedSong submittedSong1 = new SubmittedSong("petter", "some polyphony", "", "song1", "");
			Database.getSongIndex().addSong(submittedSong1);
			// since the "some polyphony" album exists in the collection, the request to resolve the ambiguous name "petter" should resolve to 
			String actualArtistName = new DiscogsAPIWrapper().getDiscogsArtistName("petter");
			if (actualArtistName == null)
				fail("no result!");
			if (!actualArtistName.equalsIgnoreCase("Petter (5)"))
				fail("actual name is incorrect=" + actualArtistName);
			
			actualArtistName = new DiscogsAPIWrapper().getDiscogsArtistName("Petter Nordkvist");
			if (!actualArtistName.equalsIgnoreCase("Petter Nordkvist"))
				fail("actual name is incorrect=" + actualArtistName);
    	} catch (Exception e) {
    		log.error("testActualArtistNameA(): error", e);
    		fail(e.getMessage());
    	}
    }

    public void testActualArtistNameB() {
    	try {
			SubmittedSong submittedSong1 = new SubmittedSong("justice", "we are your friends", "", "song1", "");
			Database.getSongIndex().addSong(submittedSong1);
			String actualArtistName = new DiscogsAPIWrapper().getDiscogsArtistName("justice");
			if (!actualArtistName.equalsIgnoreCase("Justice (3)"))
				fail("actual name is incorrect=" + actualArtistName);
			
			actualArtistName = new DiscogsAPIWrapper().getDiscogsArtistName("justice (3)");
			if (!actualArtistName.equalsIgnoreCase("Justice (3)"))
				fail("actual name is incorrect=" + actualArtistName);
    	} catch (Exception e) {
    		log.error("testActualArtistNameB(): error", e);
    		fail(e.getMessage());
    	}
    }
    
    public void testNormalArtistAPI() {    	
    	DiscogsArtist discogsArtistProfile = MiningAPIFactory.getDiscogsAPI().getArtist("DJ Frane", true);	
    	checkNormalArtist(discogsArtistProfile, false, true);
    	
		XMLSerializer.saveData(discogsArtistProfile, "data/junit/temp/discogs-artist-norm.xml");
		discogsArtistProfile = (DiscogsArtist)XMLSerializer.readData("data/junit/temp/discogs-artist-norm.xml");
			
		checkNormalArtist(discogsArtistProfile, false, true);    	
    }
    public void testNormalArtistNoAPI() {
    	if (RE3Properties.getBoolean("enable_discogs_no_api_access")) {
	    	DiscogsArtist discogsArtistProfile = MiningAPIFactory.getDiscogsAPI().getArtist("DJ Frane", false);
	    	checkNormalArtist(discogsArtistProfile, true, false);
	    	
			XMLSerializer.saveData(discogsArtistProfile, "data/junit/temp/discogs-artist-norm.xml");
			discogsArtistProfile = (DiscogsArtist)XMLSerializer.readData("data/junit/temp/discogs-artist-norm.xml");
	    	
			checkNormalArtist(discogsArtistProfile, true, false);
    	}
    }        
    public void checkNormalArtist(DiscogsArtist artist, boolean testProfileText, boolean api) {  

    	// aliases
    	if (artist.getAliases().size() != 0)
    		fail("incorrect aliases, is=" + artist.getAliases());
    	
    	// artist name
    	if (!artist.getArtistName().equals("DJ Frane"))
    		fail("incorrect artist");
    	
    	// image urls
    	Vector<String> imageURLs = new Vector<String>();
    	imageURLs.add(api ? "http://s.dsimg.com/image/A-42080-1211525634.jpeg" : "http://www.discogs.com/image/A-42080-1211525634.jpeg");
    	if (!areCollectionsEqual(artist.getImageURLs(), imageURLs))
    		fail("imageURLs incorrect, retrieved=" + artist.getImageURLs());

    	// mix release ids
    	if (artist.getMixReleaseIDs().size() != 0)
    		fail("incorrect mix releases");
    	
    	// name variations
    	if (artist.getNameVariations().size() != 0)
    		fail("incorrect name variations");
    	
    	// primary image url
    	if (!artist.getPrimaryImageURL().equals(imageURLs.get(0)))
    		fail("primaryImageURL incorrect");

    	// as of now, discogs API doesn't give back profile, but screen scraping does...
    	if (testProfileText) {
    		// profile
    		if (!artist.getProfile().equals("He gained recognition as a DJ through his mixtapes and his acrobatic turntable solos opening for rappers like Doug E. Fresh, Medusa, and Tha Liks. He made underground beats for members of Warren G's G-Funk clique. He honed his musical chops collaborating with funk legends like Mandrill and Blowfly. And now DJ Frane earns his acclaim producing instrumental concept albums that sound like nothing youve ever heard. His first solo album, Frane's Fantastic Boatride (or beats to blaze to volume 1), was released on Goodvibe Recordings and became a college radio favorite. Used for the joint-smoking scene in the film Romeo Must Die, it also became the soundtrack for real-life herb smoking sessions around the world and was named Best Record to Freestyle Over by Vice Magazine. The album cover was created by Mike Shinoda, rapper for Linkin Park."))
    			fail("profile incorrect");
    	}
    			
    	// real name
    	if (!artist.getRealName().equals(""))
    		fail("realname incorrect");
    	
    	// release ids
    	if (!areCollectionsEquivalent(artist.getReleaseIDs(), new String[] { "52211", "350137", "307165", "915345", "1357157" }))
    		fail("release ids incorrect, are=" + artist.getReleaseIDs());

    	// remix release ids
    	if (artist.getRemixReleaseIDs().size() != 0)
    		fail("remix ids incorrect");

    	// urls
    	Vector<String> urls = new Vector<String>();
    	urls.add("http://www.myspace.com/djfrane");
    	if (!areCollectionsEqual(artist.getURLs(), urls))
    		fail("urls incorrect");
    	
    }

    public void testExtensiveArtistAPI() { 
    	DiscogsArtist discogsArtistProfile = MiningAPIFactory.getDiscogsAPI().getArtist("Aphex Twin", true); 
    	checkExtensiveArtist(discogsArtistProfile, false);
    	
		XMLSerializer.saveData(discogsArtistProfile, "data/junit/temp/discogs-artist-ext.xml");
		discogsArtistProfile = (DiscogsArtist)XMLSerializer.readData("data/junit/temp/discogs-artist-ext.xml");
    	
		checkExtensiveArtist(discogsArtistProfile, false);    	    	
    }
    public void testExtensiveArtistNoAPI() {    	
    	if (RE3Properties.getBoolean("enable_discogs_no_api_access")) {
	    	DiscogsArtist discogsArtistProfile = MiningAPIFactory.getDiscogsAPI().getArtist("Aphex Twin", false);
	    	checkExtensiveArtist(discogsArtistProfile, true);
	    	
			XMLSerializer.saveData(discogsArtistProfile, "data/junit/temp/discogs-artist-ext.xml");
			discogsArtistProfile = (DiscogsArtist)XMLSerializer.readData("data/junit/temp/discogs-artist-ext.xml");
	    	
			checkExtensiveArtist(discogsArtistProfile, true); 
    	}   	    	
    }        
    public void checkExtensiveArtist(DiscogsArtist artist, boolean testProfileText) {  

    	// aliases
    	if (!areCollectionsEqual(artist.getAliases(), new String[] { "Blue Calx (2)", "Bradley Strider", "Brian Tregaskin", "Caustic Window", "Dice Man, The", "GAK", "Karen Tregaskin", "PBoD", "Polygon Window", "Power-Pill", "Q-Chastic", "Richard D. James", "Smojphace", "Soit-P.P.", "Tuss, The" }))
    		fail("incorrect aliases");
    	    	
    	// artist name
    	if (!artist.getArtistName().equals("Aphex Twin"))
    		fail("incorrect artist");
    	
    	// mix release ids
    	//if (!areCollectionsEqual(artist.getMixReleaseIDs(), new String[] { "154097", "8499", "45776", "95101" }))
    		//fail("incorrect mix releases, is=" + artist.getMixReleaseIDs());
    	if (!artist.getMixReleaseIDs().contains("154097"))
    		fail("incorrect mix releases, is=" + artist.getMixReleaseIDs());
    	if (!artist.getMixReleaseIDs().contains("8499"))
    		fail("incorrect mix releases, is=" + artist.getMixReleaseIDs());
    	
    	// name variations
    	//if (!areCollectionsEquivalent(artist.getNameVariations(), new String[] { "A-F-X Twin", "A.F.X.", "AFX", "Aphex Twin, The", "Aphex Twins", "A. Twin", "Apex Twin", "The Aphex Twins", "Aphextwin" }))
    		//fail("incorrect name variations, are=" + artist.getNameVariations());
    	if (!artist.getNameVariations().contains("AFX"))
    		fail("incorrect name variations, are=" + artist.getNameVariations());
    	if (!artist.getNameVariations().contains("A-F-X Twin"))
    		fail("incorrect name variations, are=" + artist.getNameVariations());
    	
    	// as of now, discogs API doesn't give back profile, but screen scraping does...
    	if (testProfileText) {
    		// profile
    		if (!artist.getProfile().startsWith("Born: August 18, 1971 in Limerick, Ireland"))
    			fail("profile incorrect, is=" + artist.getProfile());
    	}
    			
    	// real name
    	if (!artist.getRealName().equals("Richard David James") && !artist.getRealName().equals("Richard Dick James")) // i have no clue why API returns the 2nd variation
    		fail("realname incorrect, is=" + artist.getRealName());
    	
    	// release ids
    	if (!artist.getReleaseIDs().contains("482391"))
    		fail("remix ids incorrect");
    	if (!artist.getReleaseIDs().contains("164322"))
    		fail("remix ids incorrect");
    	if (!artist.getReleaseIDs().contains("1267450"))
    		fail("remix ids incorrect");

    	// remix release ids
    	if (!artist.getRemixReleaseIDs().contains("397048"))
    		fail("remix ids incorrect");
    	if (!artist.getRemixReleaseIDs().contains("207914"))
    		fail("remix ids incorrect");
    	if (!artist.getRemixReleaseIDs().contains("962452"))
    		fail("remix ids incorrect");

    	// urls
    	Vector<String> urls = new Vector<String>();
    	urls.add("http://www.facebook.com/aphextwinafx");
    	urls.add("http://www.drukqs.net");
    	urls.add("http://en.wikipedia.org/wiki/Aphex_twin");
    	//if (!areCollectionsEqual(artist.getURLs(), urls))
    		//fail("urls incorrect, are=" + artist.getURLs());
    	if (!artist.getURLs().contains("http://en.wikipedia.org/wiki/Aphex_twin"))
    		fail("urls incorrect, are=" + artist.getURLs());
    	if (!artist.getURLs().contains("http://warp.net/records/aphex-twin"))
    		fail("urls incorrect, are=" + artist.getURLs());
    	
    }
    
}
