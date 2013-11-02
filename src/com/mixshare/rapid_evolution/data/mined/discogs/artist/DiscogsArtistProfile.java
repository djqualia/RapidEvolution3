package com.mixshare.rapid_evolution.data.mined.discogs.artist;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.mined.MiningAPIFactory;
import com.mixshare.rapid_evolution.data.mined.discogs.DiscogsCommonProfile;
import com.mixshare.rapid_evolution.data.mined.discogs.label.DiscogsLabelProfile;
import com.mixshare.rapid_evolution.data.mined.discogs.release.DiscogsRelease;
import com.mixshare.rapid_evolution.data.mined.discogs.release.DiscogsReleaseProfile;
import com.mixshare.rapid_evolution.data.mined.discogs.release.DiscogsUserRating;
import com.mixshare.rapid_evolution.data.util.DegreeValue;

public class DiscogsArtistProfile extends DiscogsCommonProfile {

    static private Logger log = Logger.getLogger(DiscogsCommonProfile.class);
    static private final long serialVersionUID = 0L;
    
    ////////////
    // FIELDS //
    ////////////

    private String realName;
    private Vector<String> nameVariations;
    private Vector<String> aliases;
    private String profile;    
    private Vector<String> remixReleaseIds;
    private Vector<String> mixReleaseIds;
    protected Map<String, DiscogsReleaseProfile> remixReleaseProfileMap = new HashMap<String, DiscogsReleaseProfile>();
    protected Map<String, DiscogsReleaseProfile> mixReleaseProfileMap = new HashMap<String, DiscogsReleaseProfile>();
        
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
    public DiscogsArtistProfile() { super(DATA_TYPE_ARTISTS); }
    public DiscogsArtistProfile(DiscogsArtist artist) {
        super(DATA_TYPE_ARTISTS, artist.getArtistName(), artist.getURLs(), artist.getReleaseIDs(), artist.getPrimaryImageURL(), artist.getImageURLs());
        realName = artist.getRealName();
        nameVariations = artist.getNameVariations();
        aliases = artist.getAliases();
        profile = artist.getProfile();
        remixReleaseIds = artist.getRemixReleaseIDs();
        mixReleaseIds = artist.getMixReleaseIDs();
    }        
    
    /////////////
    // GETTERS //
    /////////////
    
    public boolean isValid() { return true; }
    
    public String getRealName() { return realName; }
    
    public Vector<String> getAliases() { return aliases; }
    
    public String getProfile() { return processEmbeddedURLs(profile); }
    public String getProfileText() { return profile; }
    
    public Vector<DegreeValue> getLabelDegrees() {
        Map<String, Float> labelMap = new HashMap<String, Float>();        
        Iterator<DiscogsReleaseProfile> releaseIter = getReleaseProfiles().iterator();
        int labelInstances = 0;
        while (releaseIter.hasNext()) {
            DiscogsReleaseProfile release = releaseIter.next();
            Vector<DegreeValue> labelDegrees = release.getLabelDegrees();
            for (int a = 0; a < labelDegrees.size(); ++a) {
                DegreeValue labelDegree = labelDegrees.get(a);
                String name = labelDegree.getName();
                Float count = labelMap.get(name);
                if (count == null)
                    count = new Float(labelDegree.getPercentage());
                else
                    count = new Float(labelDegree.getPercentage() + count.floatValue());
                labelMap.put(name, count);
                ++labelInstances;
            }
        }
        Iterator<Entry<String,Float>> iter = labelMap.entrySet().iterator();
        Vector<DegreeValue> labels = new Vector<DegreeValue>();
        while (iter.hasNext()) {
            Entry<String,Float> entry = iter.next();
            String labelName = entry.getKey();
            if (DiscogsLabelProfile.isValidLabelName(labelName)) {
                float numOccurrences = entry.getValue();
                float percent = numOccurrences / labelInstances;
                labels.add(new DegreeValue(labelName, percent, DATA_SOURCE_DISCOGS));
            }
        }
        Collections.sort(labels);  
        return labels;
    }
    
    static public boolean isValidArtistName(String name) {
        if (name == null)
            return false;
        if (name.equalsIgnoreCase("Various"))
            return false;
        return true;
    }
    
    public String getDiscogsURL() {
    	try {
    		return "http://www.discogs.com/artist/" + java.net.URLEncoder.encode(name, "UTF-8");
    	} catch (Exception e) { }
    	return null;
    }
    
    public float getAvgRating() { 
        Iterator<DiscogsReleaseProfile> releaseIter = releaseProfileMap.values().iterator();
        float totalAvgRating = 0.0f;
        float totalNumRatings = 0.0f;
        while (releaseIter.hasNext()) {
        	DiscogsReleaseProfile releaseProfile = releaseIter.next();            
            Vector<DiscogsUserRating> userRatings = releaseProfile.getUniqueUserRatings();
            float avgRating = 0.0f;
            int numRatings = 0;
            for (int r = 0; r < userRatings.size(); ++r) {
            	DiscogsUserRating userRating = userRatings.get(r);
                avgRating += userRating.getRating();
                ++numRatings;
            }
            Iterator<DegreeValue> artistDegrees = releaseProfile.getArtistDegrees().iterator();
            boolean found = false;
            while (artistDegrees.hasNext() && !found) {
            	DegreeValue degree = artistDegrees.next();
            	if (degree.getName().equalsIgnoreCase(this.getName())) {
            		found = true;
            		totalAvgRating += degree.getPercentage() * avgRating;
            		totalNumRatings += degree.getPercentage() * numRatings;
            	}
            }
        }
        totalAvgRating /= totalNumRatings;
        return totalAvgRating;
    }
    
    public int getNumRatings() {
        Iterator<DiscogsReleaseProfile> releaseIter = releaseProfileMap.values().iterator();
        float numRatings = 0.0f;
        while (releaseIter.hasNext()) {
        	DiscogsReleaseProfile releaseProfile = releaseIter.next();            
            int numUserRatings = releaseProfile.getNumUniqueRaters();
            Iterator<DegreeValue> artistDegrees = releaseProfile.getArtistDegrees().iterator();
            boolean found = false;
            while (artistDegrees.hasNext() && !found) {
            	DegreeValue degree = artistDegrees.next();
            	if (degree.getName().equalsIgnoreCase(this.getName())) {
            		found = true;
            		numRatings += degree.getPercentage() * numUserRatings;
            	}
            }
        }
        return (int)numRatings;
    }       
    
    public String getUniqueReleaseKey(DiscogsRelease release) {
    	return release.getTitle().toLowerCase();
    }
    
    public Vector<String> getNameVariations() {
    	return nameVariations;
    }
    
    /////////////
    // METHODS //
    /////////////
    
    static public void main(String[] args) {
        try {
        	RapidEvolution3.loadLog4J();
            log.info("result=" + new DiscogsArtistProfile(MiningAPIFactory.getDiscogsAPI().getArtist("Petter Nordkvist")));
            //log.info("result=" + (DiscogsArtistProfile)Serializer.readData("C:/musicdata/data/discogs_newartistprofiles/A Guy Called Gerald"));
        } catch (Exception e) {
            log.error("main(): error", e);
        }        
    }
    
    private void calculateMetadata(Vector<DiscogsRelease> remixReleases, Vector<DiscogsRelease> mixReleases) {
        try {
        	remixReleaseProfileMap.clear();
        	mixReleaseProfileMap.clear();
            for (int i = 0; i < remixReleases.size(); ++i) {
                DiscogsRelease release = remixReleases.get(i);
                if (log.isDebugEnabled())
                    log.debug("calculateMetadata(): processing release=" + release);
                String title = release.getTitle();
                DiscogsReleaseProfile releaseProfile = remixReleaseProfileMap.get(title.toLowerCase());
                if (releaseProfile == null) {
                    releaseProfile = new DiscogsReleaseProfile(title);
                    remixReleaseProfileMap.put(title.toLowerCase(), releaseProfile);
                }
                releaseProfile.addRelease(release);                    
            }
            for (int i = 0; i < mixReleases.size(); ++i) {
                DiscogsRelease release = mixReleases.get(i);
                if (log.isDebugEnabled())
                    log.debug("calculateMetadata(): processing release=" + release);
                String title = release.getTitle();
                DiscogsReleaseProfile releaseProfile = mixReleaseProfileMap.get(title.toLowerCase());
                if (releaseProfile == null) {
                    releaseProfile = new DiscogsReleaseProfile(title);
                    mixReleaseProfileMap.put(title.toLowerCase(), releaseProfile);
                }
                releaseProfile.addRelease(release);                    
            }
        } catch (Exception e) {
            log.error("calculateMetadata(): error", e);
        }        
    }

	public Vector<String> getRemixReleaseIds() {
		return remixReleaseIds;
	}

	public void setRemixReleaseIds(Vector<String> remixReleaseIds) {
		this.remixReleaseIds = remixReleaseIds;
	}

	public Vector<String> getMixReleaseIds() {
		return mixReleaseIds;
	}

	public void setMixReleaseIds(Vector<String> mixReleaseIds) {
		this.mixReleaseIds = mixReleaseIds;
	}

	public Map<String, DiscogsReleaseProfile> getRemixReleaseProfileMap() {
		return remixReleaseProfileMap;
	}

	public void setRemixReleaseProfileMap(
			Map<String, DiscogsReleaseProfile> remixReleaseProfileMap) {
		this.remixReleaseProfileMap = remixReleaseProfileMap;
	}

	public Map<String, DiscogsReleaseProfile> getMixReleaseProfileMap() {
		return mixReleaseProfileMap;
	}

	public void setMixReleaseProfileMap(
			Map<String, DiscogsReleaseProfile> mixReleaseProfileMap) {
		this.mixReleaseProfileMap = mixReleaseProfileMap;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public void setNameVariations(Vector<String> nameVariations) {
		this.nameVariations = nameVariations;
	}

	public void setAliases(Vector<String> aliases) {
		this.aliases = aliases;
	}

	public void setProfile(String profile) {
		this.profile = profile;
	}
	
}
