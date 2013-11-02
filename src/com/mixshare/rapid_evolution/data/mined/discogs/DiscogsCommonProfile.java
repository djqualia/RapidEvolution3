package com.mixshare.rapid_evolution.data.mined.discogs;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.mined.MinedProfile;
import com.mixshare.rapid_evolution.data.mined.MinedProfileHeader;
import com.mixshare.rapid_evolution.data.mined.discogs.release.DiscogsRelease;
import com.mixshare.rapid_evolution.data.mined.discogs.release.DiscogsReleaseProfile;
import com.mixshare.rapid_evolution.data.mined.discogs.release.DiscogsUserRating;
import com.mixshare.rapid_evolution.data.profile.common.image.Image;
import com.mixshare.rapid_evolution.data.profile.common.image.InvalidImageException;
import com.mixshare.rapid_evolution.data.profile.common.link.InvalidLinkException;
import com.mixshare.rapid_evolution.data.profile.common.link.Link;
import com.mixshare.rapid_evolution.data.util.DegreeValue;

abstract public class DiscogsCommonProfile extends MinedProfile implements Serializable, DataConstants {

    static private Logger log = Logger.getLogger(DiscogsCommonProfile.class);
    static private final long serialVersionUID = 0L;
        
    static public String[] badSuffixes = new String[] { " discography at discogs", " at discogs", " discography" };
    
    ////////////
    // FIELDS //
    ////////////
    
    protected String name;
    protected Vector<String> urls;
    protected Vector<String> releaseIds;
    protected Map<String, DiscogsReleaseProfile> releaseProfileMap = new HashMap<String, DiscogsReleaseProfile>();
    protected Vector<DegreeValue> styles = new Vector<DegreeValue>();
    protected String primaryImageURL = "";
    protected Vector<String> imageURLs = new Vector<String>();
    protected long lastFetchedReleaseIds;
    
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
    public DiscogsCommonProfile() { super(); }    
    public DiscogsCommonProfile(byte dataType) { super(new MinedProfileHeader(dataType, DATA_SOURCE_DISCOGS)); }    
    public DiscogsCommonProfile(byte dataType, String name, Vector<String> urls, Vector<String> releaseIds, String primaryImageURL, Vector<String> imageURLs) {
    	super(new MinedProfileHeader(dataType, DATA_SOURCE_DISCOGS));
        this.name = name;
        this.urls = urls;
        this.primaryImageURL = primaryImageURL;
        this.imageURLs = imageURLs;  
        this.releaseIds = releaseIds;
    }  

    /////////////
    // GETTERS //
    /////////////
    
    public int getNumReleases() { return releaseProfileMap.size(); }    
    public Vector<String> getReleaseIds() { return releaseIds; };
    public Collection<DiscogsReleaseProfile> getReleaseProfiles() { return releaseProfileMap.values(); }
    public Iterator<DiscogsReleaseProfile> getReleaseProfileIterator() { return releaseProfileMap.values().iterator(); }    
    
    public String getName() {
    	if (name != null) {
    		for (int b = 0; b < badSuffixes.length; ++b) {
    			if (name.toLowerCase().endsWith(badSuffixes[b]))
    				return name.substring(0, name.length() - badSuffixes[b].length());
    		}
    	}
    	return name;
    }
    
    public Vector<String> getURLs() { return urls; }
    public Vector<Link> getLinks() {
		Vector<Link> result = new Vector<Link>();
		for (String url : urls) {
			try {
				result.add(new Link("", "", url, "", DATA_SOURCE_DISCOGS));
			} catch (InvalidLinkException il) { }
		}
		return result;    	
    }
    
    public String getPrimaryImageURL() { return primaryImageURL; }
    public Vector<String> getImageURLs() { return imageURLs; }
    public int getNumImageURLs() { return imageURLs.size(); }
    public String getImageURL(int index) { return imageURLs.get(index); }
    public Vector<Image> getImages() {
    	Vector<Image> result = new Vector<Image>();		
    	if ((primaryImageURL != null) && (primaryImageURL.length() > 0)) {
    		try {
    			result.add(new Image(primaryImageURL, DATA_SOURCE_DISCOGS));
    		} catch (InvalidImageException e) { }
     	}
    	for (String imageUrl : imageURLs) {    		
        	if ((imageUrl != null) && (imageUrl.length() > 0)) {
        		try {
        			result.add(new Image(imageUrl, DATA_SOURCE_DISCOGS));
        		} catch (InvalidImageException e) { }
         	}
    	}
    	return result;
    }
    
    public Vector<DegreeValue> getStyleDegrees() { return styles; }
    public float getAvgRating() { 
        Iterator<DiscogsReleaseProfile> releaseIter = releaseProfileMap.values().iterator();
        float avgRating = 0.0f;
        int numRatings = 0;
        while (releaseIter.hasNext()) {
            DiscogsReleaseProfile releaseProfile = releaseIter.next();            
            Vector<DiscogsUserRating> userRatings = releaseProfile.getUniqueUserRatings();
            for (int r = 0; r < userRatings.size(); ++r) {
                DiscogsUserRating userRating = userRatings.get(r);
                avgRating += userRating.getRating();
                ++numRatings;
            }
        }
        avgRating /= numRatings;
        return avgRating;
    }
    public int getNumRatings() {
        Iterator<DiscogsReleaseProfile> releaseIter = releaseProfileMap.values().iterator();
        int numRatings = 0;
        while (releaseIter.hasNext()) {
        	DiscogsReleaseProfile releaseProfile = releaseIter.next();            
            int numUserRatings = releaseProfile.getNumUniqueRaters();
            numRatings += numUserRatings; 
        }
        return numRatings;
    }       
    public int getNumUniqueRaters() {
        Iterator<DiscogsReleaseProfile> releaseIter = releaseProfileMap.values().iterator();
        Map<String, Object> raters = new HashMap<String, Object>();
        int numRaters = 0;
        while (releaseIter.hasNext()) {
        	DiscogsReleaseProfile releaseProfile = releaseIter.next();            
            Vector<DiscogsUserRating> userRatings = releaseProfile.getUniqueUserRatings();
            for (int r = 0; r < userRatings.size(); ++r) {
                DiscogsUserRating userRating = userRatings.get(r);
                if (!raters.containsKey(userRating.getUsername())) {
                    raters.put(userRating.getUsername(), null);
                    ++numRaters;
                }
            }
        }
        return numRaters;        
    }
            
    
    public String getStyleDescription() {        
        StringBuffer result = new StringBuffer();
        for (int s = 0; s < styles.size(); ++s) {
            result.append(styles.get(s));
            if (s + 1 < styles.size())
                result.append(", ");
        }
        return result.toString();
    }
    
    public long getLastFetchedReleaseIds() { return lastFetchedReleaseIds; }

	public Vector<String> getUrls() {
		return urls;
	}
    
    /////////////
    // SETTERS //
    /////////////
    
    public void setLastFetchedReleaseIds() { lastFetchedReleaseIds = System.currentTimeMillis(); }

	public void setUrls(Vector<String> urls) {
		this.urls = urls;
	}

	public Map<String, DiscogsReleaseProfile> getReleaseProfileMap() {
		return releaseProfileMap;
	}

	public void setReleaseProfileMap(
			Map<String, DiscogsReleaseProfile> releaseProfileMap) {
		this.releaseProfileMap = releaseProfileMap;
	}

	public Vector<DegreeValue> getStyles() {
		return styles;
	}

	public void setStyles(Vector<DegreeValue> styles) {
		this.styles = styles;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setReleaseIds(Vector<String> releaseIds) {
		this.releaseIds = releaseIds;
	}

	public void setPrimaryImageURL(String primaryImageURL) {
		this.primaryImageURL = primaryImageURL;
	}

	public void setImageURLs(Vector<String> imageURLs) {
		this.imageURLs = imageURLs;
	}

	public void setLastFetchedReleaseIds(long lastFetchedReleaseIds) {
		this.lastFetchedReleaseIds = lastFetchedReleaseIds;
	}
    
    //////////////////////
    // ABSTRACT METHODS //
    //////////////////////
    
    abstract public String getUniqueReleaseKey(DiscogsRelease release);
    
    /////////////
    // METHODS //
    /////////////
    
    public String toString() { return getName(); }    	    
    
    public void calculateMetadata(Vector<DiscogsRelease> releases) {
        try {
        	releaseProfileMap.clear();
        	styles.clear();
            Map<String, Float> styleMap = new HashMap<String, Float>();
            for (int i = 0; i < releases.size(); ++i) {
                DiscogsRelease release = releases.get(i);
                if (log.isDebugEnabled())
                    log.debug("calculateMetadata(): processing release=" + release.getReleaseId());
                String releaseKey = getUniqueReleaseKey(release);
                DiscogsReleaseProfile releaseProfile = releaseProfileMap.get(releaseKey);
                if (releaseProfile == null) {
                    releaseProfile = new DiscogsReleaseProfile(release.getTitle());
                    releaseProfileMap.put(releaseKey, releaseProfile);
                }
                releaseProfile.addRelease(release);                    
            }
            Iterator<DiscogsReleaseProfile> releaseIter = releaseProfileMap.values().iterator();
            while (releaseIter.hasNext()) {
            	DiscogsReleaseProfile releaseProfile = releaseIter.next();
                Vector<DegreeValue> styles = releaseProfile.getStyleDegrees();
                for (int s = 0; s < styles.size(); ++s) {
                    DegreeValue styleDegree = (DegreeValue)styles.get(s);
                    Float styleCount = (Float)styleMap.get(styleDegree.getName());
                    if (styleCount == null)
                        styleCount = new Float(styleDegree.getPercentage());
                    else
                        styleCount = new Float(styleDegree.getPercentage() + styleCount.floatValue());
                    styleMap.put(styleDegree.getName(), styleCount);
                }                
            }
            
            Iterator<Entry<String,Float>> iter = styleMap.entrySet().iterator();
            while (iter.hasNext()) {
                Entry<String,Float> entry = iter.next();
                String styleName = entry.getKey();
                float numOccurrences = entry.getValue();
                float percent = numOccurrences / getNumReleases();
                styles.add(new DegreeValue(styleName, percent, DATA_SOURCE_DISCOGS));
            }
            Collections.sort(styles);
        } catch (Exception e) {
            log.error("calculateMetadta(): error", e);
        }
    }
        
    protected String processEmbeddedURLs(String input) {
    	if (input == null)
    		return "";
    	String startToken = "[url=";
    	String endToken = "[/url]";
    	int index = input.indexOf(startToken);
    	while (index >= 0) {
    		int nextIndex = input.indexOf("]", index + startToken.length());
    		if (nextIndex >= 0) {
	    		String url = input.substring(index + startToken.length(), nextIndex);
	    		int lastIndex = input.indexOf(endToken);
	    		if (lastIndex >= 0) {
		    		String text = input.substring(nextIndex + 1, lastIndex).trim();
		    		StringBuffer result = new StringBuffer();
		    		result.append(input.substring(0, index));
		    		result.append("<a href=\"");
		    		result.append(url);
		    		result.append("\"");
		    		result.append(DiscogsAPIWrapper.HREF_TARGET_TEXT);
		    		result.append(">");
		    		result.append(text);
		    		result.append("</a>");
		    		result.append(input.substring(lastIndex + endToken.length()));
		    		input = result.toString();
	    		}
    		}
    		index = input.indexOf(startToken, index + 1);
    	}
    	return input;
    }
    
}
