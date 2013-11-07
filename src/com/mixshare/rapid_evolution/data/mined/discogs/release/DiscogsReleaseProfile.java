package com.mixshare.rapid_evolution.data.mined.discogs.release;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.mined.MinedProfile;
import com.mixshare.rapid_evolution.data.mined.MinedProfileHeader;
import com.mixshare.rapid_evolution.data.mined.MiningAPIFactory;
import com.mixshare.rapid_evolution.data.mined.discogs.song.DiscogsSong;
import com.mixshare.rapid_evolution.data.profile.common.image.Image;
import com.mixshare.rapid_evolution.data.profile.common.image.InvalidImageException;
import com.mixshare.rapid_evolution.data.profile.common.link.InvalidLinkException;
import com.mixshare.rapid_evolution.data.profile.common.link.Link;
import com.mixshare.rapid_evolution.data.util.DegreeValue;

/**
 * This class is used to help consolidate the info for a duplicate releases to form unique releases.
 */
public class DiscogsReleaseProfile extends MinedProfile implements Serializable, DataConstants {

	static private Logger log = Logger.getLogger(DiscogsReleaseProfile.class);
	static private final long serialVersionUID = 0L;
           
	////////////
	// FIELDS //
	////////////
	
    private Vector<String> artistNames = new Vector<String>();
    private String title;
    private Integer originalYear = null;
    private Integer latestYear = null;
    private Map<String, Integer> styles = new HashMap<String, Integer>();
    private Vector<DiscogsUserRating> userRatings = new Vector<DiscogsUserRating>();
    private int numDuplicates = 0;
    private Vector<DiscogsReleaseLabelInstance> labelInstances = new Vector<DiscogsReleaseLabelInstance>();
    private Vector<String> uniqueTrackTitles = new Vector<String>();
    private Map<Integer, Vector<DiscogsSong>> releaseTrackSets = new HashMap<Integer, Vector<DiscogsSong>>();
    private String primaryImageURL = "";
    private Vector<String> imageURLs = new Vector<String>();
    private Vector<String> owners = new Vector<String>();
    private Vector<String> wishlist = new Vector<String>();
    private Map<String, Float> recommendedReleases = new HashMap<String, Float>();
    private Vector<Integer> recommendedReleaseIds = new Vector<Integer>();
    private long lastFetchedRecommendedReleases;    
    
    transient private Vector<DegreeValue> labelDegreesCache = null;
    transient private Vector<DiscogsUserRating> uniqueUserRatings;        
    
    static {
    	try{
    		// in order for the XMLEncoder to skip transient variables, this is needed
    		BeanInfo info = Introspector.getBeanInfo(DiscogsReleaseProfile.class);
    		PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
    		for (int i = 0; i < propertyDescriptors.length; ++i) {
    			PropertyDescriptor pd = propertyDescriptors[i];
    			if (pd.getName().equals("labelDegreesCache") || pd.getName().equals("uniqueUserRatings")) {
    				pd.setValue("transient", Boolean.TRUE);
    			}
    		}
    	} catch (IntrospectionException e) {
    		log.error("static(): error", e);
    	}
    }
    
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
    public DiscogsReleaseProfile() {
    	super(new MinedProfileHeader(DATA_TYPE_RELEASES, DATA_SOURCE_DISCOGS));
    }
    public DiscogsReleaseProfile(String title) {
    	super(new MinedProfileHeader(DATA_TYPE_RELEASES, DATA_SOURCE_DISCOGS));
        this.title = title;
    }

    /////////////
    // GETTERS //
    /////////////
    
    public boolean isValid() { return true; }
    
    public Vector<DiscogsSong> getPrimaryTrackSet() {
    	Vector<DiscogsSong> resultSet = new Vector<DiscogsSong>();
    	Iterator<Vector<DiscogsSong>> iter = releaseTrackSets.values().iterator();
    	while (iter.hasNext()) {
    		Vector<DiscogsSong> tracks = iter.next();
    		if (tracks.size() > resultSet.size())
    			resultSet = tracks;
    	}
    	return resultSet;
    }
    
    public String getShortArtistDescription() {
    	if (artistNames.size() > 1)
    		return "Various";
    	else if (artistNames.size() == 1)
    		return (String)artistNames.get(0);
    	return "";
    }
    public String getArtistDescription() {
    	StringBuffer result = new StringBuffer();
    	for (int a = 0; a < artistNames.size(); ++a) {
    		result.append(artistNames.get(a).toString());
    		if (a + 1 < artistNames.size())
    			result.append(" & ");
    	}
    	return result.toString();
    }    
    
    public Vector<DegreeValue> getArtistDegrees() {
        Vector<DegreeValue> result = new Vector<DegreeValue>();
        for (int l = 0; l < artistNames.size(); ++l) {
            String name = (String)artistNames.get(l);
            if (name.equalsIgnoreCase("Various")) {
                int totalArtists = 0;
                Map<String, Integer> artistMap = new HashMap<String, Integer>();
                Iterator<Vector<DiscogsSong>> trackSetIter = releaseTrackSets.values().iterator();
                while (trackSetIter.hasNext()) {
                    Vector<DiscogsSong> trackSet = trackSetIter.next();
                    for (int t = 0; t < trackSet.size(); ++t) {
                        DiscogsSong track = trackSet.get(t);
                        Vector<String> artists = MiningAPIFactory.getDiscogsAPI().getArtists(track, this);
                        for (int a = 0; a < artists.size(); ++a) {
                            String artist = artists.get(a);
                            Integer count = (Integer)artistMap.get(artist.toLowerCase());
                            if (count == null) {
                                count = new Integer(1);
                            } else {
                                count = new Integer(1 + count.intValue());
                            }
                            artistMap.put(artist.toLowerCase(), count);
                            ++totalArtists;
                        }
                    }
                }
                Iterator<Entry<String, Integer>>iter = artistMap.entrySet().iterator();
                while (iter.hasNext()) {
                    Entry<String, Integer> entry = iter.next();
                    String artist = entry.getKey();
                    int count = entry.getValue().intValue();
                    result.add(new DegreeValue(artist, ((float)count) / totalArtists / artistNames.size(), DATA_SOURCE_DISCOGS));
                }
            } else {
                result.add(new DegreeValue(name.toLowerCase(), 1.0f / artistNames.size(), DATA_SOURCE_DISCOGS));
            }
        }
        return result;
    }    
    public Vector<DegreeValue> getLabelDegrees() {
    	if (labelDegreesCache == null) {
	        Map<String, Integer> labelDegrees = new HashMap<String, Integer>();
	        int total = 0;
	        for (int l = 0; l < labelInstances.size(); ++l) {
	            DiscogsReleaseLabelInstance instance = (DiscogsReleaseLabelInstance)labelInstances.get(l);
	            ++total;
	            Integer count = (Integer)labelDegrees.get(instance.getName());
	            if (count == null)
	                count = new Integer(1);
	            else
	                count = new Integer(1 + count.intValue());
	            labelDegrees.put(instance.getName(), count);            
	        }
	        labelDegreesCache = new Vector<DegreeValue>();
	        Iterator<Entry<String,Integer>> iter = labelDegrees.entrySet().iterator();
	        while (iter.hasNext()) {
	        	Entry<String,Integer> entry = iter.next();
	        	String label = entry.getKey();
	        	int count = entry.getValue().intValue();
	        	labelDegreesCache.add(new DegreeValue(label, ((float)count) / total, DATA_SOURCE_DISCOGS));
	        }
	        java.util.Collections.sort(labelDegreesCache);
    	}
        return labelDegreesCache;
    }
    
    public String getTitle() { return title; }
    public String getDescription() {
        StringBuffer result = new StringBuffer();
        for (int a = 0; a < artistNames.size(); ++a) {
            if (result.length() > 0) 
                result.append(", ");
            result.append(artistNames.get(a));
        }
        result.append(" - ");
        result.append(title);
        return result.toString();
    }
    public Vector<DegreeValue> getStyleDegrees() {
        Vector<DegreeValue> result = new Vector<DegreeValue>();
        Iterator<Entry<String,Integer>> iter = styles.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<String,Integer> entry = iter.next();
            String styleName = entry.getKey();
            int count = entry.getValue().intValue();
            float degree = ((float)count) / numDuplicates;
            if (degree > 1.0f) degree = 1.0f;
            result.add(new DegreeValue(styleName, degree, DATA_SOURCE_DISCOGS));
        }
        java.util.Collections.sort(result);
        return result;
    }    
    
    public float getStyleDegree(String name) {
        Iterator<Entry<String,Integer>> iter = styles.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<String,Integer> entry = iter.next();
            String styleName = entry.getKey();
            if (name.equalsIgnoreCase(styleName)) {
                int count = entry.getValue().intValue();
                float degree = ((float)count) / numDuplicates;
                return degree;
            }
        }
        return 0.0f;
    }        
    
    public Vector<DiscogsUserRating> getUniqueUserRatings() {
        if (uniqueUserRatings == null) {
            Map<String, Object> uniqueUsernames = new HashMap<String, Object>();
            for (int r = 0; r < userRatings.size(); ++r) {
            	DiscogsUserRating userRating = userRatings.get(r);
                uniqueUsernames.put(userRating.getUsername(), null);            
            }
            uniqueUserRatings = new Vector<DiscogsUserRating>();
            Iterator<String> nameIter = uniqueUsernames.keySet().iterator();
            while (nameIter.hasNext()) {
                String name = nameIter.next();
                int numRatings = 0;
                float avgRating = 0.0f;
                for (int r = 0; r < userRatings.size(); ++r) {
                	DiscogsUserRating userRating = userRatings.get(r);
                    if (userRating.getUsername().equalsIgnoreCase(name)) {
                        avgRating += userRating.getRating();
                        ++numRatings;
                    }
                }
                avgRating /= numRatings;
                uniqueUserRatings.add(new DiscogsUserRating(name, avgRating));
            }
            java.util.Collections.sort(uniqueUserRatings);
        }
        return uniqueUserRatings;
    }
    public float getAverageUserRating() {
        int numRatings = 0;
        float totalRating = 0.0f;
        Iterator<DiscogsUserRating> iter = getUniqueUserRatings().iterator();
        while (iter.hasNext()) {
        	DiscogsUserRating userRating = iter.next();
            totalRating += userRating.getRating();
            ++numRatings;
        }
        if (numRatings > 0)
            return totalRating / numRatings;            
        return Float.NaN;
    }  
    
    public Vector<DiscogsReleaseLabelInstance> getLabelInstances() { return labelInstances; }
    public Vector<String> getLabelNames() {
    	Vector<String> result = new Vector<String>();
    	if (labelInstances != null) {
    		for (DiscogsReleaseLabelInstance labelInstance : labelInstances)
    			result.add(MiningAPIFactory.getDiscogsAPI().getLocalLabelName(labelInstance.getName()));
    	}
    	return result;
    }
    public Vector<String> getArtistNames() { return artistNames; }
    
    public int getNumRatings() {
    	return userRatings.size();
    }
    public int getNumUniqueRaters() {
    	return getUniqueUserRatings().size();
    }
    
    public float getAvgRating() {
    	return getAverageUserRating();
    }
    
    public Vector<String> getOwners() { return owners; }
    public Vector<String> getWishlist() { return wishlist; }
    public short getOriginalYearShort() {
    	if (originalYear != null)
    		return originalYear.shortValue();
    	return (short)0;
    }
    public Vector<String> getUniqueTrackTitles() { return uniqueTrackTitles; }
    
    public String getPrimaryImageURL() { return primaryImageURL; }
    public void setPrimaryImageURL(String url) { primaryImageURL = url; }
    public Vector<String> getImageURLs() { return imageURLs; }
    public int getNumImageURLs() { return imageURLs.size(); }
    public String getImageURL(int i) { return imageURLs.get(i); }
    public Vector<Image> getImages() {
    	Vector<Image> result = new Vector<Image>();		
    	if ((primaryImageURL != null) && (primaryImageURL.length() > 0)) {
    		try {
    			result.add(new Image(primaryImageURL, DATA_SOURCE_DISCOGS));
    		} catch (InvalidImageException e) {
    			log.warn("getImages(): error=" + e);
    		}
     	}
    	for (String imageUrl : imageURLs) {    		
        	if ((imageUrl != null) && (imageUrl.length() > 0)) {
        		try {
        			result.add(new Image(imageUrl, DATA_SOURCE_DISCOGS));
        		} catch (InvalidImageException e) {
        			log.warn("getImages(): error=" + e);
        		}
         	}
    	}
    	return result;
    }
    
    public Map<Integer, Vector<DiscogsSong>> getReleaseTrackSets() {return releaseTrackSets; }
    
    public int[] getReleaseIds() {
    	int[] result = new int[releaseTrackSets.size()];;
    	Iterator<Integer> iter = releaseTrackSets.keySet().iterator();
    	int count = 0;
    	while (iter.hasNext())
    		result[count++] = iter.next();
    	return result;
    }
    
    public int getNumOwners() { return owners.size(); }
    public int getNumWishlist() { return wishlist.size(); }        
    
    public String getReleaseKey(String artistDescription, String releaseTitle) {
    	return (artistDescription + " - " + releaseTitle).toLowerCase();
    }
    public float getSimilarityWith(String artistDescription, String releaseTitle) {
    	Float similarity = recommendedReleases.get(getReleaseKey(artistDescription, releaseTitle));
    	if (similarity != null)
    		return similarity;
    	return 0.0f;
    }
    
    public Vector<Link> getLinks() {
		Vector<Link> result = new Vector<Link>();
		for (Integer releaseId : releaseTrackSets.keySet()) {
			try {
				result.add(new Link("", "", "http://www.discogs.com/release/" + releaseId, "Discogs", DATA_SOURCE_DISCOGS));
			} catch (InvalidLinkException il) { }
		}
		return result;    	
    }

    public long getLastFetchedRecommendedReleases() { return lastFetchedRecommendedReleases; }
    
    public Vector<Integer> getRecommendedReleaseIds() { return recommendedReleaseIds; }
    
    public boolean isCompilation() {
    	if ((artistNames == null) || (artistNames.size() == 0))
    		return true;
    	String artistDescription = getArtistDescription();
    	if (artistDescription.equalsIgnoreCase("various") || artistDescription.equalsIgnoreCase("various artists"))
    		return true;
    	return false;
    }
    
	public Integer getOriginalYear() {
		return originalYear;
	}        
    
    /////////////
    // SETTERS //
    /////////////

	public void setOriginalYear(Integer originalYear) {
		this.originalYear = originalYear;
	}
	public Integer getLatestYear() {
		return latestYear;
	}
	public void setLatestYear(Integer latestYear) {
		this.latestYear = latestYear;
	}
	public Map<String, Integer> getStyles() {
		return styles;
	}
	public void setStyles(Map<String, Integer> styles) {
		this.styles = styles;
	}
	public Vector<DiscogsUserRating> getUserRatings() {
		return userRatings;
	}
	public void setUserRatings(Vector<DiscogsUserRating> userRatings) {
		this.userRatings = userRatings;
	}
	public int getNumDuplicates() {
		return numDuplicates;
	}
	public void setNumDuplicates(int numDuplicates) {
		this.numDuplicates = numDuplicates;
	}
	public Map<String, Float> getRecommendedReleases() {
		return recommendedReleases;
	}
	public void setRecommendedReleases(Map<String, Float> recommendedReleases) {
		this.recommendedReleases = recommendedReleases;
	}
	public Vector<DegreeValue> getLabelDegreesCache() {
		return labelDegreesCache;
	}
	public void setLabelDegreesCache(Vector<DegreeValue> labelDegreesCache) {
		this.labelDegreesCache = labelDegreesCache;
	}
	public void setArtistNames(Vector<String> artistNames) {
		this.artistNames = artistNames;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public void setLabelInstances(Vector<DiscogsReleaseLabelInstance> labelInstances) {
		this.labelInstances = labelInstances;
	}
	public void setUniqueTrackTitles(Vector<String> uniqueTrackTitles) {
		this.uniqueTrackTitles = uniqueTrackTitles;
	}
	public void setReleaseTrackSets(
			Map<Integer, Vector<DiscogsSong>> releaseTrackSets) {
		this.releaseTrackSets = releaseTrackSets;
	}
	public void setImageURLs(Vector<String> imageURLs) {
		this.imageURLs = imageURLs;
	}
	public void setOwners(Vector<String> owners) {
		this.owners = owners;
	}
	public void setWishlist(Vector<String> wishlist) {
		this.wishlist = wishlist;
	}
	public void setRecommendedReleaseIds(Vector<Integer> recommendedReleaseIds) {
		this.recommendedReleaseIds = recommendedReleaseIds;
	}
	public void setLastFetchedRecommendedReleases(
			long lastFetchedRecommendedReleases) {
		this.lastFetchedRecommendedReleases = lastFetchedRecommendedReleases;
	}
	public void setUniqueUserRatings(Vector<DiscogsUserRating> uniqueUserRatings) {
		this.uniqueUserRatings = uniqueUserRatings;
	}        
    
    public void lastFetchedRecommendedReleases() { lastFetchedRecommendedReleases = System.currentTimeMillis(); }
    
    /**
     * This method is used to add duplicate releases with the same album title.  There are duplicates
     * in discogs due to the format of the release...
     */
    public void addRelease(DiscogsRelease release) {
        if (release == null)
            return;
        ++numDuplicates;
        // ARTISTS
        if (artistNames.size() == 0)
            artistNames = release.getArtistNames();
        // MERGE STYLES
        Vector<String> releaseStyles = release.getUniqueStylesAndGenres();
        for (int s = 0; s < releaseStyles.size(); ++s) {
            String style = releaseStyles.get(s);
            Integer count = (Integer)styles.get(style);
            if (count == null)
                count = new Integer(1);
            else
                count = new Integer(1 + count.intValue());
            styles.put(style, count);
        }
        // MERGE RATINGS
        DiscogsReleaseRatings ratings = release.getRatings();
        if (ratings != null) {
            for (int r = 0; r < ratings.getNumRatings(); ++r) {
                userRatings.add(new DiscogsUserRating(ratings.getUser(r), ratings.getRating(r)));
            }
        }
        // MERGE YEAR
        Integer yearReleased = release.getYearReleased();
        if (yearReleased != null) {
            if (latestYear == null) {
                latestYear = yearReleased;
            } else {
                if (latestYear.intValue() < yearReleased.intValue())
                    latestYear = yearReleased;
            }
            if (originalYear == null) {
                originalYear = yearReleased;
            } else {
                if (originalYear.intValue() > yearReleased.intValue())
                    originalYear = yearReleased;
            }        
        }
        // MERGE RECORD LABEL
        Vector<DiscogsReleaseLabelInstance> releaseLabels = release.getLabelInstances();
        for (int i = 0; i < releaseLabels.size(); ++i) {
        	DiscogsReleaseLabelInstance instance = releaseLabels.get(i);
            if (!labelInstances.contains(instance))
                labelInstances.add(instance);
        }
        // UNIQUE TRACKS
        Vector<DiscogsSong> tracks = release.getSongs();
        for (int t = 0; t < tracks.size(); ++t) {
            DiscogsSong track = tracks.get(t);
            if (!uniqueTrackTitles.contains(track.getTitle().toLowerCase()))
                uniqueTrackTitles.add(track.getTitle().toLowerCase());
        }
        releaseTrackSets.put(new Integer(release.getReleaseId()), tracks);
        // IMAGE URLS
        if (primaryImageURL.equals(""))
            primaryImageURL = release.getPrimaryImageURL();
        Iterator<String> imageIter = release.getImageURLs().iterator();
        while (imageIter.hasNext()) {
            String imageURL = imageIter.next();
            if (!imageURLs.contains(imageURL))
                imageURLs.add(imageURL);
        }
        // OWNERS
        for (int i = 0; i < release.getOwners().size(); ++i) {
            String owner = (String)release.getOwners().get(i);
            if (!owners.contains(owner))
                owners.add(owner);
        }
        // WISHLIST
        for (int i = 0; i < release.getWishlist().size(); ++i) {
            String user = (String)release.getWishlist().get(i);
            if (!wishlist.contains(user))
                wishlist.add(user);
        }        
        // RECOMMENDED
        if ((release.getRecommendations() != null) && (release.getRecommendations().getRecommendedIds() != null)) {
        	for (Integer releaseId : release.getRecommendations().getRecommendedIds()) {
        		if (!recommendedReleaseIds.contains(releaseId))
        			recommendedReleaseIds.add(releaseId);		
        	}
        }
    }   
    
    public void calculateRecommendReleases(Vector<DiscogsRelease> recommendedReleases) {
    	if (recommendedReleases.size() > 0) {
    		this.recommendedReleases.clear();
    		float similarity = 1.0f;
    		float decrement = 0.005f;
    		for (DiscogsRelease recommendedRelease : recommendedReleases) {
    			this.recommendedReleases.put(getReleaseKey(recommendedRelease.getArtistDescription(), recommendedRelease.getTitle()), similarity);
    			similarity -= decrement;
    		}
    		if (log.isDebugEnabled())
    			log.debug("calculateRecommendReleases(): processed " + recommendedReleases.size() + " recommended releases from discogs");
    	}
    }
    
    
    /////////////
    // METHODS //
    /////////////
    
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append(getArtistDescription());
        result.append(" - ");
        result.append(getTitle());
        return result.toString();
    }    
    
    public String toStringFull() {
        StringBuffer result = new StringBuffer();
        result.append("\nTITLE=");
        result.append(title);
        result.append("\nARTISTS=");
        result.append(artistNames);
        result.append("\nORIGINAL YEAR=");
        result.append(originalYear);
        result.append("\nLATEST YEAR=");
        result.append(latestYear);
        result.append("\nSTYLES=");
        result.append(getStyleDegrees());
        result.append("\n# DUPLICATES=");
        result.append(numDuplicates);
        result.append("\nLABEL INSTANCES=");
        result.append(labelInstances);
        result.append("\n# USER RATINGS=");
        result.append(getUniqueUserRatings().size());
        result.append("\nUSER RATINGS=");
        result.append(getUniqueUserRatings());
        result.append("\nAVG. USER RATING=");
        result.append(getAverageUserRating());
        result.append("\nUNIQUE TRACK TITLES=");
        result.append(uniqueTrackTitles);
        result.append("\nRELEASE TRACK SETS=");
        result.append(releaseTrackSets);
        result.append("\nPRIMARY IMAGE URL=");
        result.append(primaryImageURL);
        result.append("\nIMAGE URLS=");
        result.append(imageURLs);        
        result.append("\nOWNERS=");
        result.append(owners);
        result.append("\nWISHLIST=");
        result.append(wishlist);        
        return result.toString();
    }    
    
	public boolean equals(Object o) {
		if (o instanceof DiscogsReleaseProfile) {
			DiscogsReleaseProfile oP = (DiscogsReleaseProfile)o;
			return (oP.getArtistDescription().equals(getArtistDescription()) && oP.getTitle().equals(getTitle()));
		}
		return false;
	}	          
            
    public int hashCode() {
    	StringBuffer result = new StringBuffer();
    	result.append(getArtistDescription());
    	result.append(" - ");
    	result.append(getTitle());
    	return result.toString().toLowerCase().hashCode();
    }
	
    static public void main(String[] args) {
        try {
        	RapidEvolution3.loadLog4J();
            DiscogsReleaseProfile profile = new DiscogsReleaseProfile("Selected Ambient Works 85-92");
            profile.addRelease(MiningAPIFactory.getDiscogsAPI().getRelease(1354641));
            profile.addRelease(MiningAPIFactory.getDiscogsAPI().getRelease(1434823));
            log.info(profile.toStringFull());
        } catch (Exception e) {
            log.error("main(): error", e);
        }        
    }
    
}
