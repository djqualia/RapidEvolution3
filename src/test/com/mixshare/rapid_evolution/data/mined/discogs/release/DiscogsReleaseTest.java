package test.com.mixshare.rapid_evolution.data.mined.discogs.release;

import java.util.Vector;

import org.apache.log4j.Logger;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.mined.MiningAPIFactory;
import com.mixshare.rapid_evolution.data.mined.discogs.release.DiscogsRelease;
import com.mixshare.rapid_evolution.data.mined.discogs.release.DiscogsReleaseLabelInstance;
import com.mixshare.rapid_evolution.data.mined.discogs.song.DiscogsSong;
import com.mixshare.rapid_evolution.data.util.io.XMLSerializer;

public class DiscogsReleaseTest extends RE3TestCase {

    static private Logger log = Logger.getLogger(DiscogsReleaseTest.class);

    public void testCompilationReleaseAPI() {
    	DiscogsRelease release = MiningAPIFactory.getDiscogsAPI().getRelease(148889, true);
    	checkCompilationRelease(release, true);
    	
		XMLSerializer.saveData(release, "data/junit/temp/discogs-release-comp.xml");
		release = (DiscogsRelease)XMLSerializer.readData("data/junit/temp/discogs-release-comp.xml");

		checkCompilationRelease(release, true);
    }
    public void testCompilationReleaseNoAPI() {    	
    	if (RE3Properties.getBoolean("enable_discogs_no_api_access")) {
	    	DiscogsRelease release = MiningAPIFactory.getDiscogsAPI().getRelease(148889, false);
	    	checkCompilationRelease(release, false);
	    	
			XMLSerializer.saveData(release, "data/junit/temp/discogs-release-comp.xml");
			release = (DiscogsRelease)XMLSerializer.readData("data/junit/temp/discogs-release-comp.xml");
	
			checkCompilationRelease(release, false);
    	}
    }        
    public void checkCompilationRelease(DiscogsRelease release, boolean api) {    	

    	if (release == null)
    		fail("failed to get release");

    	// release id
    	if (release.getReleaseId() != 148889)
    		fail("releaseId incorrect");
    	
    	// title
    	if (!release.getTitle().equals("We Are Reasonable People"))
    		fail("title incorrect");
    	    	
    	// artist names
    	if (!areCollectionsEqual(release.getArtistNames(), new String[] { "Various" }))
    		fail("artistNames incorrect, is=" + release.getArtistNames());
    	
    	// country
    	if (!release.getCountry().equals("UK"))
    		fail("country incorrect");
    	    	
    	// genres
    	if (!areCollectionsEqual(release.getGenres(), new String[] { "Electronic" }))
    		fail("genres incorrect");
    	
    	// styles
    	if (!areCollectionsEqual(release.getStyles(), new String[] { "Techno", "Electro", "Future Jazz" }))
    		fail("styles incorrect");

    	// image urls
    	Vector<String> imageURLs = new Vector<String>();
    	imageURLs.add(api ? "http://s.dsimg.com/image/R-148889-1146869126.jpeg" : "http://www.discogs.com/image/R-148889-1146869126.jpeg");
    	imageURLs.add(api ? "http://s.dsimg.com/image/R-148889-1215703168.jpeg" : "http://www.discogs.com/image/R-148889-1215703168.jpeg");
    	if (!areCollectionsEqual(release.getImageURLs(), imageURLs))
    		fail("imageURLs incorrect");
    	
    	// label instances
    	Vector<DiscogsReleaseLabelInstance> labelInstances = new Vector<DiscogsReleaseLabelInstance>();
    	labelInstances.add(new DiscogsReleaseLabelInstance("Warp Records", "WAP100PD"));
    	if (!areCollectionsEqual(release.getLabelInstances(), labelInstances))
    		fail("labelInstances incorrect, is=" + release.getLabelInstances());

    	// tracks
    	Vector<DiscogsSong> tracks = new Vector<DiscogsSong>();
    	tracks.add(new DiscogsSong("A01", "Fishtail Parker", "", new String[] { "Nightmares On Wax" }, new String[] { }));
    	tracks.add(new DiscogsSong("A02", "Wear My Bikini", "", new String[] { "Jimi Tenor" }, new String[] { }));
    	tracks.add(new DiscogsSong("B03", "Circulation", "", new String[] { "Two Lone Swordsmen" }, new String[] { }));
    	tracks.add(new DiscogsSong("B04", "4 Dead Monks (Original Demo)", "", new String[] { "Red Snapper" }, new String[] { }));    	
    	if (!areCollectionsEqual(release.getSongs(), tracks))
    		fail("tracks incorrect, are=" + release.getSongs() + ", should be=" + tracks);    	

    	// year
    	if (release.getYearReleased() != 1998)
    		fail("incorrect year released");
    	
    	if (!release.getReleased().equals("29 Jun 1998") && !release.getReleased().equals("1998-06-29"))
    		fail("incorrect released field, is=" + release.getReleased());
    	
    	// owners
    	if (release.getOwners().size() < 35)
    		fail("incorrect owners, is=" + release.getOwners().size());
    	if (!release.getOwners().contains("dmusict"))
    		fail("incorrect owner");
    	if (!release.getOwners().contains("joachimp"))
    		fail("incorrect owner");
    	if (!release.getOwners().contains("zombek"))
    		fail("incorrect owner");
    	
    	// wishlist (subject to change, so it's tricky...)
    	if (release.getWishlist().size() < 5)
    		fail("incorrect wishlist");
    	if (!release.getWishlist().contains("squarEyes"))
    		fail("incorrect wishlist user");
    	
    	if (!release.getRecommendations().contains(11777))
    		fail("missing recommendations");
    	if (!release.getRecommendations().contains(3666))
    		fail("missing recommendations");
    	if (!release.getRecommendations().contains(24546))
    		fail("missing recommendations");
    	if (release.getRecommendations().contains(190214))
    		fail("false recommendations");

    }

    public void testNormalReleaseAPI() {    	    	
    	DiscogsRelease release = MiningAPIFactory.getDiscogsAPI().getRelease(52211, true);
    	checkNormalRelease(release, true);
    	
		XMLSerializer.saveData(release, "data/junit/temp/discogs-release-norm.xml");
		release = (DiscogsRelease)XMLSerializer.readData("data/junit/temp/discogs-release-norm.xml");

		checkNormalRelease(release, true);		    	    	
    }
    public void testNormalReleaseNoAPI() {
    	if (RE3Properties.getBoolean("enable_discogs_no_api_access")) {
	    	DiscogsRelease release = MiningAPIFactory.getDiscogsAPI().getRelease(52211, false);
	    	checkNormalRelease(release, false);
	
	    	XMLSerializer.saveData(release, "data/junit/temp/discogs-release-norm.xml");
			release = (DiscogsRelease)XMLSerializer.readData("data/junit/temp/discogs-release-norm.xml");
	
			checkNormalRelease(release, false);
    	}
    }        
    public void checkNormalRelease(DiscogsRelease release, boolean api) {    	

    	if (release == null)
    		fail("failed to get release");

    	// release id
    	if (release.getReleaseId() != 52211)
    		fail("releaseId incorrect");
    	
    	// title
    	if (!release.getTitle().equals("Frane's Fantastic Boatride"))
    		fail("title incorrect");
    	
    	// artist names
    	if (!areCollectionsEqual(release.getArtistNames(), new String[] { "DJ Frane" }))
    		fail("artistNames incorrect, is=" + release.getArtistNames());
    	
    	// country
    	if (!release.getCountry().equals("US"))
    		fail("country incorrect");
    	
    	// genres
    	if (!areCollectionsEquivalent(release.getGenres(), new String[] { "Electronic", "Hip Hop" }))
    		fail("genres incorrect, is=" + release.getGenres());
    	
    	// styles
    	if (!areCollectionsEqual(release.getStyles(), new String[] { "Downtempo" }))
    		fail("styles incorrect");
    	
    	// image urls
    	Vector<String> imageURLs = new Vector<String>();
    	imageURLs.add(api ? "http://s.dsimg.com/image/R-52211-1214626748.jpeg" : "http://www.discogs.com/image/R-52211-1214626748.jpeg");
    	imageURLs.add(api ? "http://s.dsimg.com/image/R-52211-1214626769.jpeg" : "http://www.discogs.com/image/R-52211-1214626769.jpeg");
    	imageURLs.add(api ? "http://s.dsimg.com/image/R-52211-1214626779.jpeg" : "http://www.discogs.com/image/R-52211-1214626779.jpeg");
    	if (!areCollectionsEqual(release.getImageURLs(), imageURLs))
    		fail("imageURLs incorrect");    	
    	
    	// label instances
    	Vector<DiscogsReleaseLabelInstance> labelInstances = new Vector<DiscogsReleaseLabelInstance>();
    	labelInstances.add(new DiscogsReleaseLabelInstance("Good Vibe Recordings", "GVRD2011"));
    	if (!areCollectionsEqual(release.getLabelInstances(), labelInstances))
    		fail("labelInstances incorrect");

    	// tracks
    	Vector<DiscogsSong> tracks = new Vector<DiscogsSong>();
    	tracks.add(new DiscogsSong("1", "Boatman", "", new String[] { }, new String[] { }));
    	tracks.add(new DiscogsSong("2", "420247", "", new String[] { }, new String[] { }));
    	tracks.add(new DiscogsSong("3", "Spin", "", new String[] { }, new String[] { }));
    	tracks.add(new DiscogsSong("4", "Every Cloud Can Cause Amazement", "", new String[] { }, new String[] { }));
    	tracks.add(new DiscogsSong("5", "Ode To Old Toby", "", new String[] { }, new String[] { }));
    	tracks.add(new DiscogsSong("6", "Innervisions", "", new String[] { }, new String[] { }));
    	tracks.add(new DiscogsSong("7", "Wet (Part 1)", "", new String[] { }, new String[] { }));
    	tracks.add(new DiscogsSong("8", "Wet (Part 2)", "", new String[] { }, new String[] { }));
    	tracks.add(new DiscogsSong("9", "Starfish Poplock", "", new String[] { }, new String[] { }));
    	tracks.add(new DiscogsSong("10", "Submerge", "", new String[] { }, new String[] { }));
    	tracks.add(new DiscogsSong("11", "Lost", "", new String[] { }, new String[] { }));
    	tracks.add(new DiscogsSong("12", "I Can Do My Thang", "", new String[] { }, new String[] { }));
    	tracks.add(new DiscogsSong("13", "Going Home", "", new String[] { }, new String[] { }));
    	if (!areCollectionsEqual(release.getSongs(), tracks)) {
    		if (release.getSongs().size() == tracks.size()) {
    			for (int i = 0; i < tracks.size(); ++i)
    				if (!release.getSongs().get(i).equals(tracks.get(i)))
    					fail("track #" + i + " doesn't match, is=" + release.getSongs().get(i) + ", should be=" + tracks.get(i));
    		}
    		fail("tracks incorrect, are=" + release.getSongs() + ", is=" + tracks);
    	}

    	// year
    	if (release.getYearReleased() != 1999)
    		fail("incorrect year released");

    	if (!release.getReleased().equals("1999"))
    		fail("incorrect released field");

    	
    	// owners
    	if (release.getOwners().size() < 6)
    		fail("incorrect owners");
    	if (!release.getOwners().contains("AndyBnz"))
    		fail("incorrect owner");
    	if (!release.getOwners().contains("PasiS"))
    		fail("incorrect owner");
    	if (!release.getOwners().contains("Say_Vegin"))
    		fail("incorrect owner");
    	
    	// wishlist (subject to change, so it's tricky...)
    	if (release.getWishlist().size() < 1)
    		fail("incorrect wishlist");
    	if (!release.getWishlist().contains("amenbro"))
    		fail("incorrect wishlist user");
    	
    	if (release.getRecommendations().contains(162342))
    		fail("false recommendations");    	

    }

    public void testRemixNoDurationReleaseAPI() {   
    	DiscogsRelease release = MiningAPIFactory.getDiscogsAPI().getRelease(1087726, true);
    	checkRemixNoDurationRelease(release, true);
    	
    	XMLSerializer.saveData(release, "data/junit/temp/discogs-release-norm2.xml");
		release = (DiscogsRelease)XMLSerializer.readData("data/junit/temp/discogs-release-norm2.xml");

		checkRemixNoDurationRelease(release, true);		    	    	    	
    }
    public void testRemixNoDurationReleaseNoAPI() {
    	if (RE3Properties.getBoolean("enable_discogs_no_api_access")) {
	    	DiscogsRelease release = MiningAPIFactory.getDiscogsAPI().getRelease(1087726, false);
	    	checkRemixNoDurationRelease(release, false);
	    	
	    	XMLSerializer.saveData(release, "data/junit/temp/discogs-release-norm2.xml");
			release = (DiscogsRelease)XMLSerializer.readData("data/junit/temp/discogs-release-norm2.xml");
	
			checkRemixNoDurationRelease(release, false);	
    	}	    	    	    	
    }        
    public void checkRemixNoDurationRelease(DiscogsRelease release, boolean api) {    	

    	if (release == null)
    		fail("failed to get release");

    	// release id
    	if (release.getReleaseId() != 1087726)
    		fail("releaseId incorrect");
    	
    	// title
    	if (!release.getTitle().equals("Total Confusion"))
    		fail("title incorrect");
    	
    	// artist names
    	if (!areCollectionsEqual(release.getArtistNames(), new String[] { "A Homeboy, A Hippie & A Funki Dredd" }))
    		fail("artistNames incorrect, is=" + release.getArtistNames());
    	
    	// country
    	if (!release.getCountry().equals("UK"))
    		fail("country incorrect");
    	
    	// genres
    	if (!areCollectionsEqual(release.getGenres(), new String[] { "Electronic" }))
    		fail("genres incorrect");
    	
    	// styles
    	if (!areCollectionsEqual(release.getStyles(), new String[] { "Breakbeat", "Hardcore" }))
    		fail("styles incorrect");
    	
    	// image urls
    	Vector<String> imageURLs = new Vector<String>();
    	imageURLs.add(api ? "http://s.dsimg.com/image/R-1087726-1191712935.jpeg" : "http://www.discogs.com/image/R-1087726-1191712935.jpeg");
    	imageURLs.add(api ? "http://s.dsimg.com/image/R-1087726-1193252102.jpeg" : "http://www.discogs.com/image/R-1087726-1193252102.jpeg");
    	if (!areCollectionsEquivalent(release.getImageURLs(), imageURLs))
    		fail("imageURLs incorrect, is=" + release.getImageURLs());
    	
    	// label instances
    	Vector<DiscogsReleaseLabelInstance> labelInstances = new Vector<DiscogsReleaseLabelInstance>();
    	labelInstances.add(new DiscogsReleaseLabelInstance("Tam Tam Records", "TTT 031"));
    	if (!areCollectionsEqual(release.getLabelInstances(), labelInstances))
    		fail("labelInstances incorrect");

    	// tracks
    	Vector<DiscogsSong> tracks = new Vector<DiscogsSong>();
    	tracks.add(new DiscogsSong("A1", "Total Confusion (Confusion Mix)", "", new String[] { }, new String[] { }));
    	tracks.add(new DiscogsSong("B1", "Total Confusion (Mellow Mix)", "", new String[] { }, new String[] { }));
    	tracks.add(new DiscogsSong("B2", "Total Confusion (Reprise)", "", new String[] { }, new String[] { }));
    	if (!areCollectionsEqual(release.getSongs(), tracks))
    		fail("tracks incorrect, is=" + release.getSongs());    	

    	// year
    	if (release.getYearReleased() != 1990)
    		fail("incorrect year released");
    	
    	if (!release.getReleased().equals("1990"))
    		fail("incorrect released field");
    	
    	// owners
    	if (release.getOwners().size() < 10)
    		fail("incorrect owners");
    	if (!release.getOwners().contains("burneverything"))
    		fail("incorrect owner");
    	if (!release.getOwners().contains("pacific3000"))
    		fail("incorrect owner");
    	if (!release.getOwners().contains("sinbox"))
    		fail("incorrect owner");
    	
    	// wishlist (subject to change, so it's tricky...)
    	if (release.getWishlist().size() < 1)
    		fail("incorrect wishlist");
    	if (!release.getWishlist().contains("kdrevival"))
    		fail("incorrect wishlist user");
    	
    	if (release.getRecommendations().contains(162342))
    		fail("false recommendations");    	    	

    }
    
    public void testMultipleLabelsReleaseAPI() {
    	DiscogsRelease release = MiningAPIFactory.getDiscogsAPI().getRelease(66030, true);
    	checkMultipleLabelsRelease(release, true);

    	XMLSerializer.saveData(release, "data/junit/temp/discogs-release-multilabel.xml");
		release = (DiscogsRelease)XMLSerializer.readData("data/junit/temp/discogs-release-multilabel.xml");

		checkMultipleLabelsRelease(release, true);		    	    	    	    	
    }
    public void testMultipleLabelsReleaseNoAPI() {
    	if (RE3Properties.getBoolean("enable_discogs_no_api_access")) {
	    	DiscogsRelease release = MiningAPIFactory.getDiscogsAPI().getRelease(66030, false);
	    	checkMultipleLabelsRelease(release, false);
	    	
	    	XMLSerializer.saveData(release, "data/junit/temp/discogs-release-multilabel.xml");
			release = (DiscogsRelease)XMLSerializer.readData("data/junit/temp/discogs-release-multilabel.xml");
	
			checkMultipleLabelsRelease(release, false);	
    	}	    	    	    	    	    	
    }        
    public void checkMultipleLabelsRelease(DiscogsRelease release, boolean usingAPI) {    	

    	if (release == null)
    		fail("failed to get release");

    	// release id
    	if (release.getReleaseId() != 66030)
    		fail("releaseId incorrect");
    	
    	// title
    	if (!release.getTitle().equals("Untitled"))
    		fail("title incorrect");

    	// artist names
    	if (!areCollectionsEqual(release.getArtistNames(), new String[] { "Various" }))
    		fail("artistNames incorrect, is=" + release.getArtistNames());
    	
    	// country
    	if (!release.getCountry().equals("UK"))
    		fail("country incorrect");
    	
    	// genres
    	if (!areCollectionsEqual(release.getGenres(), new String[] { "Electronic" }))
    		fail("genres incorrect");
    	
    	// styles
    	if (!areCollectionsEqual(release.getStyles(), new String[] { "Hardcore" }))
    		fail("styles incorrect, is=" + release.getStyles());

    	// image urls
    	Vector<String> imageURLs = new Vector<String>();
    	imageURLs.add(usingAPI ? "http://s.dsimg.com/image/R-66030-001.jpg" : "http://www.discogs.com/image/R-66030-001.jpg");
    	imageURLs.add(usingAPI ? "http://s.dsimg.com/image/R-66030-1084577553.jpg" : "http://www.discogs.com/image/R-66030-1084577553.jpg");
    	imageURLs.add(usingAPI ? "http://s.dsimg.com/image/R-66030-1118843861.jpg" : "http://www.discogs.com/image/R-66030-1118843861.jpg");
    	imageURLs.add(usingAPI ? "http://s.dsimg.com/image/R-66030-1118843893.jpg" : "http://www.discogs.com/image/R-66030-1118843893.jpg");
    	imageURLs.add(usingAPI ? "http://s.dsimg.com/image/R-66030-1226957512.jpeg" : "http://www.discogs.com/image/R-66030-1226957512.jpeg");    	
    	if (!areCollectionsEqual(release.getImageURLs(), imageURLs))
    		fail("imageURLs incorrect");
    	
    	// label instances
    	Vector<DiscogsReleaseLabelInstance> labelInstances = new Vector<DiscogsReleaseLabelInstance>();
    	labelInstances.add(new DiscogsReleaseLabelInstance("Deathchant", "Dchant 1001"));
    	labelInstances.add(new DiscogsReleaseLabelInstance("Deathchant", "CHANTBOX1"));
    	labelInstances.add(new DiscogsReleaseLabelInstance("Deathchant", "CHANT CD1"));
    	if (!areCollectionsEqual(release.getLabelInstances(), labelInstances))
    		fail("labelInstances incorrect");
    	
    	// tracks
    	Vector<DiscogsSong> tracks = new Vector<DiscogsSong>();
    	tracks.add(new DiscogsSong("1.01", "Brainwave", "", new String[] { "Diplomat" }, new String[] { }));
    	tracks.add(new DiscogsSong("1.02", "King Of The Vari - Speed", "", new String[] { "DJ Producer, The" }, new String[] { }));
    	tracks.add(new DiscogsSong("1.03", "Structurally Un-Sound (Remix)", "", new String[] { "DJ Scorpio" }, new String[] { "Hellfish" }));
    	tracks.add(new DiscogsSong("1.04", "Martian Ambassador (UK Electric Boogie Mix)", "", new String[] { "Hellfish" }, new String[] { }));
    	tracks.add(new DiscogsSong("1.05", "Religion II (Bringer Of War)", "", new String[] { "DJ Producer, The" }, new String[] { }));
    	tracks.add(new DiscogsSong("1.06", "Ultimate Damage '98", "", new String[] { "Hellfish" }, new String[] { }));
    	tracks.add(new DiscogsSong("1.07", "Psychotic Breakz", "", new String[] { "DJ Scorpio", "Wargroover" }, new String[] { }));
    	tracks.add(new DiscogsSong("1.08", "Do Ya Like? (DJ Producer's The Champ Mix)", "", new String[] { "Hellfish" }, new String[] { "DJ Producer, The" }));
    	tracks.add(new DiscogsSong("1.09", "The Crippler (Filters Of Fury Mix)", "", new String[] { "Hellfish" }, new String[] { }));
    	tracks.add(new DiscogsSong("1.10", "The Screamer (Screamers Revenge - Blue Murder Mix)", "", new String[] { "Diplomat" }, new String[] { "DJ Producer, The" }));
    	tracks.add(new DiscogsSong("1.11", "No More Rock N Roll (Koala Fish Mutant Bird Mix)", "", new String[] { "Hellfish & Producer" }, new String[] { }));
    	tracks.add(new DiscogsSong("1.12", "Hardcore Body Harvest (Bunker Clot Mix)", "", new String[] { "Hellfish" }, new String[] { }));
    	tracks.add(new DiscogsSong("1.13", "The DJ Producer Takes Deathchant To The Butchers", "", new String[] { "DJ Producer, The" }, new String[] { }));
    	tracks.add(new DiscogsSong("2.01", "21st  Century Core (Orchestrations For The End Of The World)", "8:21", new String[] { "DJ Producer, The" }, new String[] { }));
    	tracks.add(new DiscogsSong("2.02", "The Crippler (A.K. 47 Muthefucka Remix)", "6:21", new String[] { "Hellfish" }, new String[] { }));
    	tracks.add(new DiscogsSong("2.03", "Screamer (Live @ Rez)", "5:54", new String[] { "Diplomat" }, new String[] { }));
    	tracks.add(new DiscogsSong("2.04", "The Running Man (The Butcher Of  Bakersfield Special Edition)", "8:30", new String[] { "Diplomat" }, new String[] { "Hellfish", "Skeeta" }));
    	tracks.add(new DiscogsSong("2.05", "The Way Of The Homeboy Part. II (The Winter Of Discontent)", "7:20", new String[] { "Hellfish & Producer" }, new String[] { }));
    	tracks.add(new DiscogsSong("2.06", "Ultimate Damage 98/99", "6:58", new String[] { "DJ Producer, The" }, new String[] { }));
    	tracks.add(new DiscogsSong("2.07", "The Ripper (Original)", "5:19", new String[] { "Technological Terror Crew" }, new String[] { }));
    	tracks.add(new DiscogsSong("2.08", "Destined For Destruction", "7:22", new String[] { "Hellfish" }, new String[] { }));
    	tracks.add(new DiscogsSong("2.09", "The Vortices", "7:23", new String[] { "Diplomat", "DJ Producer, The"}, new String[] { }));
    	tracks.add(new DiscogsSong("2.10", "Serious Evil Shit Mission 3", "9:15", new String[] { "Hellfish" }, new String[] { }));
    	tracks.add(new DiscogsSong("A", "Whatever It Takes", "7:03", new String[] { usingAPI ? "Producer" : "Producer" }, new String[] { }));
    	tracks.add(new DiscogsSong("B", "Radical Digital", "5:07", new String[] { "Hellfish" }, new String[] { }));
    	Vector<DiscogsSong> releaseTracks = release.getSongs();
    	for (int t = 0; t < tracks.size(); ++t) {
    		DiscogsSong track = (DiscogsSong)tracks.get(t);
    		DiscogsSong releaseTrack = (DiscogsSong)releaseTracks.get(t);
			if (!releaseTrack.equals(track)) {
				if (t != 23)
					fail("tracks not equal, is=" + releaseTrack + ", should be=" + track);
			}
    	}
    	//if (!areCollectionsEqual(release.getSongs(), tracks))
    		//fail("tracks incorrect, are=" + release.getSongs());    	

    	// year
    	if (release.getYearReleased() != 2000)
    		fail("incorrect year released");
    	
    	if (!release.getReleased().equals("2000"))
    		fail("incorrect released field");
    	
    	// owners
    	if (release.getOwners().size() < 50)
    		fail("incorrect owners");
    	if (!release.getOwners().contains("acidsmiley"))
    		fail("incorrect owner");
    	if (!release.getOwners().contains("ArcOne"))
    		fail("incorrect owner");
    	if (!release.getOwners().contains("darrenc"))
    		fail("incorrect owner");
    	
    	// wishlist (subject to change, so it's tricky...)
    	if (release.getWishlist().size() < 1)
    		fail("incorrect wishlist");
    	if (!release.getWishlist().contains("Annunaki"))
    		fail("incorrect wishlist user");

    	if (!release.getRecommendations().contains(72850))
    		fail("missing recommendations");
    	if (!release.getRecommendations().contains(47845))
    		fail("missing recommendations");
    	if (!release.getRecommendations().contains(136067))
    		fail("missing recommendations");
    	if (release.getRecommendations().contains(1250963))
    		fail("false recommendations=" + release.getRecommendations().toString());
    	    	
    }
    
    public void testMultipleRemixersReleaseAPI() {
    	DiscogsRelease release = MiningAPIFactory.getDiscogsAPI().getRelease(52342, true);
    	checkMultipleRemixersRelease(release, true);
    
    	XMLSerializer.saveData(release, "data/junit/temp/discogs-release-multiremixer.xml");
		release = (DiscogsRelease)XMLSerializer.readData("data/junit/temp/discogs-release-multiremixer.xml");

		checkMultipleRemixersRelease(release, true);		    	    	    	    	

    	
    }
    public void testMultipleRemixersReleaseNoAPI() {
    	if (RE3Properties.getBoolean("enable_discogs_no_api_access")) {
	    	DiscogsRelease release = MiningAPIFactory.getDiscogsAPI().getRelease(52342, false);
	    	checkMultipleRemixersRelease(release, false);
	        
	    	XMLSerializer.saveData(release, "data/junit/temp/discogs-release-multiremixer.xml");
			release = (DiscogsRelease)XMLSerializer.readData("data/junit/temp/discogs-release-multiremixer.xml");
	
			checkMultipleRemixersRelease(release, false);
    	}
    }        
    public void checkMultipleRemixersRelease(DiscogsRelease release, boolean api) {    	

    	if (release == null)
    		fail("failed to get release");

    	// release id
    	if (release.getReleaseId() != 52342)
    		fail("releaseId incorrect");
    	
    	// title
    	if (!release.getTitle().equals("Dark Side Of The Shroom Part 2 - The Remixes"))
    		fail("title incorrect");

    	
    	// artist names
    	if (!areCollectionsEqual(release.getArtistNames(), new String[] { "Dark Side Of The Shroom" }))
    		fail("artistNames incorrect, is=" + release.getArtistNames());
    	
    	// country
    	if (!release.getCountry().equals("US"))
    		fail("country incorrect");
    	
    	// genres
    	if (!areCollectionsEqual(release.getGenres(), new String[] { "Electronic" }))
    		fail("genres incorrect");
    	
    	// styles
    	if (!areCollectionsEqual(release.getStyles(), new String[] { "Breakbeat", "Trip Hop", "Big Beat" }))
    		fail("styles incorrect");
    	
    	// image urls
    	Vector<String> imageURLs = new Vector<String>();
    	imageURLs.add(api ? "http://s.dsimg.com/image/R-52342-1085979827.jpg" : "http://www.discogs.com/image/R-52342-1085979827.jpg");
    	if (!areCollectionsEqual(release.getImageURLs(), imageURLs))
    		fail("imageURLs incorrect");

    	// label instances
    	Vector<DiscogsReleaseLabelInstance> labelInstances = new Vector<DiscogsReleaseLabelInstance>();
    	labelInstances.add(new DiscogsReleaseLabelInstance("Tricked Out Recordings", "TR 005"));
    	if (!areCollectionsEqual(release.getLabelInstances(), labelInstances))
    		fail("labelInstances incorrect");
    	
    	// tracks
    	Vector<DiscogsSong> tracks = new Vector<DiscogsSong>();
    	tracks.add(new DiscogsSong("A1", "Brutal Beatdown (DJ Voodoo & DJ Donovan Remix)", "5:51", new String[] { }, new String[] { "DJ Donovan", "DJ Voodoo (2)" }));
    	tracks.add(new DiscogsSong("A2", "Green Mushroom (Simply Jeff Remix)", "6:38", new String[] { }, new String[] { "Simply Jeff" }));
    	tracks.add(new DiscogsSong("B1", "Oh-Zone Layer (DJ Hardware & DJ Remix Remix)", "4:50", new String[] { }, new String[] { "DJ Hardware", "DJ Remix" }));
    	tracks.add(new DiscogsSong("B2", "Panaramic (Oliver Chesler Remix)", "4:40", new String[] { }, new String[] { "Oliver Chesler" }));
    	tracks.add(new DiscogsSong("B3", "Hard Hop Beats By The Hard Hop Heathen", "2:03", new String[] { }, new String[] { }));
    	if (!areCollectionsEqual(release.getSongs(), tracks))
    		fail("tracks incorrect, is=" + release.getSongs());    	

    	// year
    	if (release.getYearReleased() != 1996)
    		fail("incorrect year released");
    	
    	if (!release.getReleased().equals("1996"))
    		fail("incorrect released field");
    	
    	// owners
    	if (release.getOwners().size() < 1)
    		fail("incorrect owners");
    	if (!release.getOwners().contains("BOMBBOUTIQUE"))
    		fail("incorrect owner");
    	
    	// wishlist (subject to change, so it's tricky...)
    	if (release.getWishlist().size() < 1)
    		fail("incorrect wishlist");
    	if (!release.getWishlist().contains("deusdiabolus"))
    		fail("incorrect wishlist user");

    	if (!release.getRecommendations().contains(104518))
    		fail("missing recommendations");
    	if (!release.getRecommendations().contains(259615))
    		fail("missing recommendations");
    	if (!release.getRecommendations().contains(114635))
    		fail("missing recommendations");
    	if (release.getRecommendations().contains(374853))
    		fail("false recommendations");        	
    }
    
    
    public void testNameVariationAPI() {
    	DiscogsRelease release = MiningAPIFactory.getDiscogsAPI().getRelease(724699, true);
    	checkNameVariation(release);
    	
    	XMLSerializer.saveData(release, "data/junit/temp/discogs-release-namevariation.xml");
		release = (DiscogsRelease)XMLSerializer.readData("data/junit/temp/discogs-release-namevariation.xml");

		checkNameVariation(release);		    	    	    	    	    	
    	
    }
    public void testNameVariationNoAPI() {
    	if (RE3Properties.getBoolean("enable_discogs_no_api_access")) {
	    	DiscogsRelease release = MiningAPIFactory.getDiscogsAPI().getRelease(724699, false);
	    	checkNameVariation(release);
	    	
	    	XMLSerializer.saveData(release, "data/junit/temp/discogs-release-namevariation.xml");
			release = (DiscogsRelease)XMLSerializer.readData("data/junit/temp/discogs-release-namevariation.xml");
	
			checkNameVariation(release);
    	}
    }        
    public void checkNameVariation(DiscogsRelease release) {    	

    	if (release == null)
    		fail("failed to get release");

    	// release id
    	if (release.getReleaseId() != 724699)
    		fail("releaseId incorrect");
    	
    	// title
    	if (!release.getTitle().equals("Some Polyphony"))
    		fail("title incorrect");
    	
    	// artist names
    	if (!areCollectionsEqual(release.getArtistNames(), new String[] { "Petter (5)" }))
    		fail("artistNames incorrect, is=" + release.getArtistNames());
    	
    }    
}
