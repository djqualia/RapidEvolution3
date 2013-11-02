package com.mixshare.rapid_evolution.data.mined.discogs.label;

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
import com.mixshare.rapid_evolution.data.mined.discogs.artist.DiscogsArtistProfile;
import com.mixshare.rapid_evolution.data.mined.discogs.release.DiscogsRelease;
import com.mixshare.rapid_evolution.data.mined.discogs.release.DiscogsReleaseProfile;
import com.mixshare.rapid_evolution.data.util.DegreeValue;

public class DiscogsLabelProfile extends DiscogsCommonProfile {

    static private Logger log = Logger.getLogger(DiscogsLabelProfile.class);	
    static private final long serialVersionUID = 0L;
        
    ////////////
    // FIELDS //
    ////////////
    
    private String contactInfo;
    private String profile;
    private String parentLabel;
    private Vector<String> subLabels = new Vector<String>();
    
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
    public DiscogsLabelProfile() { super(DATA_TYPE_LABELS); }
    public DiscogsLabelProfile(DiscogsLabel label) {
        super(DATA_TYPE_LABELS, label.getName(), label.getURLs(), label.getReleaseIDs(), label.getPrimaryImageURL(), label.getImageURLs());
        contactInfo = label.getContactInfo();
        profile = label.getProfile();
        parentLabel = label.getParentLabelName();
        subLabels = label.getSubLabelNames();
    }
    
    /////////////
    // GETTERS //
    /////////////
    
    public boolean isValid() { return true; }
    
    public String getProfile() { return processEmbeddedURLs(profile); }
    public String getProfileText() { return profile; }
    
    public String getContactInfo() { return contactInfo; }
    public String getParentLabel() { return parentLabel; }
    public Vector<String> getSubLabels() { return subLabels; }
            
    public Vector<DegreeValue> getArtistDegrees() {
        Map<String, Float> artistMap = new HashMap<String, Float>();        
        Iterator<DiscogsReleaseProfile> releaseIter = getReleaseProfiles().iterator();
        while (releaseIter.hasNext()) {
        	DiscogsReleaseProfile release = releaseIter.next();
            Vector<DegreeValue> artistDegrees = release.getArtistDegrees();
            for (int a = 0; a < artistDegrees.size(); ++a) {
                DegreeValue artistDegree = artistDegrees.get(a);
                String name = artistDegree.getName();
                Float count = (Float)artistMap.get(name);
                if (count == null)
                    count = new Float(artistDegree.getPercentage());
                else
                    count = new Float(artistDegree.getPercentage() + count.floatValue());
                artistMap.put(name, count);                
            }
        }
        Iterator<Entry<String, Float>> iter = artistMap.entrySet().iterator();
        Vector<DegreeValue> artists = new Vector<DegreeValue>();
        while (iter.hasNext()) {
            Entry<String, Float> entry = (Entry<String, Float>)iter.next();
            String artistName = (String)entry.getKey();
            if (DiscogsArtistProfile.isValidArtistName(artistName)) {
                float numOccurrences = ((Float)entry.getValue()).floatValue();
                float percent = numOccurrences / getNumReleases();
                artists.add(new DegreeValue(artistName, percent, DATA_SOURCE_DISCOGS));
            }
        }
        Collections.sort(artists);  
        return artists;
    }    
    
    public String getUniqueReleaseKey(DiscogsRelease release) {
    	StringBuffer result = new StringBuffer();
    	result.append(release.getArtistDescription());
    	result.append(" - ");
    	result.append(release.getTitle());
    	return result.toString().toLowerCase();
    }
    
    
    static public boolean isValidLabelName(String name) {
        if (name == null)
            return false;
        if (name.equalsIgnoreCase("Not On Label"))
            return false;
        return true;
    }
    
    public String getDiscogsURL() {
    	try {
    		return "http://www.discogs.com/label/" + java.net.URLEncoder.encode(name, "UTF-8");
    	} catch (Exception e) { }
    	return null;
    }    

    /////////////
    // SETTERS //
    /////////////
    
	public void setContactInfo(String contactInfo) {
		this.contactInfo = contactInfo;
	}

	public void setProfile(String profile) {
		this.profile = profile;
	}

	public void setParentLabel(String parentLabel) {
		this.parentLabel = parentLabel;
	}

	public void setSubLabels(Vector<String> subLabels) {
		this.subLabels = subLabels;
	}        
    
    /////////////
    // METHODS //
    /////////////
    
    static public void main(String[] args) {
        try {
        	RapidEvolution3.loadLog4J();
            new DiscogsLabelProfile(MiningAPIFactory.getDiscogsAPI().getLabel("R & S Records"));            
        } catch (Exception e) {
            log.error("main(): error", e);
        }        
    }

}
