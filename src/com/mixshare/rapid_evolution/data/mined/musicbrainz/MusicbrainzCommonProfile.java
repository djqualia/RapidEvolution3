package com.mixshare.rapid_evolution.data.mined.musicbrainz;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import com.mixshare.rapid_evolution.data.mined.MinedProfile;
import com.mixshare.rapid_evolution.data.mined.MinedProfileHeader;
import com.mixshare.rapid_evolution.data.profile.common.link.InvalidLinkException;
import com.mixshare.rapid_evolution.data.profile.common.link.Link;
import com.mixshare.rapid_evolution.data.util.DegreeValue;

abstract public class MusicbrainzCommonProfile extends MinedProfile {

	////////////
	// FIELDS //
	////////////
	
	protected String mbId; // musicbrainz unique id
    protected Map<String, Vector<String>> urls = new HashMap<String, Vector<String>>(); // key is type, value is url
    protected Map<String, Integer> tags = new HashMap<String, Integer>(); // key is tag, value is count
    protected float avgRating;
    protected int numRaters;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
    public MusicbrainzCommonProfile() {
    	super();
    }
	public MusicbrainzCommonProfile(byte dataType) {
		super(new MinedProfileHeader(dataType, DATA_SOURCE_MUSICBRAINZ));
	}
	
	public MusicbrainzCommonProfile(byte dataType, String mbId, Map<String, Vector<String>> urls, Map<String, Integer> tags, float avgRating, int numRaters) {
		super(new MinedProfileHeader(dataType, DATA_SOURCE_MUSICBRAINZ));
		this.mbId = mbId;
		this.urls = urls;
		this.tags = tags;
		this.avgRating = avgRating;
		this.numRaters = numRaters;
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public boolean isValid() { return (mbId != null) && (mbId.length() > 0); }
	public String getMbId() { return mbId; }
	
	public Map<String, Integer> getTags() { return tags; }
	public float getAvgRating() { return avgRating; }
	public int getNumRaters() { return numRaters; }
	
	public Vector<DegreeValue> getTagDegrees() {
		int maxTagCount = 0;
		for (Integer count : tags.values())
			if (count > maxTagCount)
				maxTagCount = count;		
		Vector<DegreeValue> result = new Vector<DegreeValue>(tags.size());
		for (Entry<String, Integer> entry : tags.entrySet())
			result.add(new DegreeValue(entry.getKey(), ((float)entry.getValue()) / maxTagCount, DATA_SOURCE_MUSICBRAINZ));
		return result;
	}
	public float getTagDegree(String tagName) {
		for (DegreeValue degree : getTagDegrees())
			if (degree.getName().equalsIgnoreCase(tagName))
				return degree.getPercentage();
		return 0.0f;
	}
	
	public Map<String, Vector<String>> getUrlsMap() { return urls; }
	public Vector<String> getUrls(String type) {
		return urls.get(type);
	}
	public Vector<Link> getLinks() {
		Vector<Link> result = new Vector<Link>();
		for (Entry<String, Vector<String>> entry : urls.entrySet()) {
			for (String url : entry.getValue()) {
				try {
					result.add(new Link("", "", url, entry.getKey(), DATA_SOURCE_MUSICBRAINZ));
				} catch (InvalidLinkException il) { }
			}
		}
		return result;
	}
	
	public Map<String, Vector<String>> getUrls() {
		return urls;
	}	
	
	/////////////
	// SETTERS //
	/////////////
	
	public void setUrls(Map<String, Vector<String>> urls) {
		this.urls = urls;
	}
	public void setMbId(String mbId) {
		this.mbId = mbId;
	}
	public void setTags(Map<String, Integer> tags) {
		this.tags = tags;
	}
	public void setAvgRating(float avgRating) {
		this.avgRating = avgRating;
	}
	public void setNumRaters(int numRaters) {
		this.numRaters = numRaters;
	}	
		
    /////////////
    // METHODS //
    /////////////
    
	public boolean equals(Object o) {
		if (o instanceof MusicbrainzCommonProfile) {
			MusicbrainzCommonProfile oP = (MusicbrainzCommonProfile)o;
			return oP.mbId.equals(mbId);
		}
		return false;
	}
	
	public int hashCode() {  return mbId.hashCode(); }
	
}
