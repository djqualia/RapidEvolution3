package com.mixshare.rapid_evolution.data.mined.lastfm.release;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Vector;

import net.roarsoftware.lastfm.Album;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.mined.MiningAPIFactory;
import com.mixshare.rapid_evolution.data.mined.lastfm.LastfmAPIWrapper;
import com.mixshare.rapid_evolution.data.mined.lastfm.LastfmCommonProfile;
import com.mixshare.rapid_evolution.data.mined.util.MiningLimitReachedException;
import com.mixshare.rapid_evolution.data.util.DegreeValue;

public class LastfmReleaseProfile extends LastfmCommonProfile {

    static private Logger log = Logger.getLogger(LastfmReleaseProfile.class);
    static private final long serialVersionUID = 0L;
            
    ////////////
    // FIELDS //
    ////////////
    
    private String id;
    private String artistName;
    private String releaseName;
    private float reach;
    private Date releasedDate;
    private Map<String, Float> songReaches;
    private Collection<String> topTags;
    
    //////////////////
    // CONSTRUCTORS //
    //////////////////
    
    public LastfmReleaseProfile() { super(DATA_TYPE_RELEASES); }
    public LastfmReleaseProfile(String artistName, String releaseName) {
    	super(DATA_TYPE_RELEASES);
    	this.artistName = artistName;
    	this.releaseName = releaseName;
    	
    	try {
			// new 2.0 style api
    		MiningAPIFactory.getLastfmAPI().getRateController().startQuery();
			Album album = Album.getInfo(artistName, releaseName, LastfmAPIWrapper.API_KEY);
			if (album != null) {
		    	loadCommonInfo(album);
		    	this.id = album.getId();
		    	this.releasedDate = album.getReleaseDate();
		    	this.topTags = album.getTags();	    		    		    	
		    	
		    	// old 1.0 API	    	
		    	songReaches = LastfmAPIWrapper.getReleaseSongs(artistName, releaseName);
		
		    	if (isValid())
		        	log.debug("LastfmReleaseProfile(): fetched release=" + artistName + " - " + releaseName);	    	
			}
    	} catch (net.roarsoftware.lastfm.CallException e) {
    		log.debug("LastfmReleaseProfile(): call exception=" + e);
    	} catch (MiningLimitReachedException e) {
    		log.error("LastfmReleaseProfile(): mining limit reached");
    	}			
    }
    
    public LastfmReleaseProfile(Album album) {
    	super(DATA_TYPE_RELEASES);
    	this.artistName = album.getArtist();
    	this.releaseName = album.getName();
    	loadCommonInfo(album);
    	this.id = album.getId();
    	this.releasedDate = album.getReleaseDate();
    	this.topTags = album.getTags();	    		    		    	    	
    }
    
    /////////////
    // GETTERS //
    /////////////
    
    public String getArtistDescription() { return artistName; }
    public String getReleaseTitle() { return releaseName; }
    
    public Date getReleaseDate() { return releasedDate; }
    
    public Vector<DegreeValue> getTopTags() {
    	Vector<DegreeValue> result = new Vector<DegreeValue>();
    	if (topTags != null) {
    		for (String tag : topTags) {
    			result.add(new DegreeValue(tag, 1.0f, DATA_SOURCE_LASTFM));
    		}
    	}
    	return result;
    }
    
    public int getNumSongs() { return songReaches.size(); }
    
    public float getReachForSong(String title) {
    	Float result = songReaches.get(title.toLowerCase());
    	if (result != null)
    		return result;
    	return 0.0f;
    }
    
    public Date getReleasedDate() { return releasedDate; }
    public short getOriginalYearReleased() {
    	if (releasedDate != null) {
    		Calendar calendar = Calendar.getInstance();
    		calendar.setTime(releasedDate);
    		return (short)calendar.get(Calendar.YEAR);
    	}
    	return (short)0;
    }
    
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getArtistName() {
		return artistName;
	}
	public void setArtistName(String artistName) {
		this.artistName = artistName;
	}
	public String getReleaseName() {
		return releaseName;
	}
	public void setReleaseName(String releaseName) {
		this.releaseName = releaseName;
	}
	public float getReach() {
		return reach;
	}
	public void setReach(float reach) {
		this.reach = reach;
	}
	public Map<String, Float> getSongReaches() {
		return songReaches;
	}
	public void setSongReaches(Map<String, Float> songReaches) {
		this.songReaches = songReaches;
	}
	public void setReleasedDate(Date releasedDate) {
		this.releasedDate = releasedDate;
	}
	public void setTopTags(Collection<String> topTags) {
		this.topTags = topTags;
	}
	
    /////////////
    // METHODS //
    /////////////
    
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append(artistName);
        result.append(" - ");
        result.append(releaseName);
        return result.toString();
    }
    
	public boolean equals(Object o) {
		if (o instanceof LastfmReleaseProfile) {
			LastfmReleaseProfile oP = (LastfmReleaseProfile)o;
			return oP.artistName.equals(artistName) && oP.releaseName.equals(releaseName);
		}
		return false;
	}
	
	public int hashCode() {  return artistName.hashCode() + releaseName.hashCode(); }	
    
    static public void main(String[] args) {
        try {
        	RapidEvolution3.loadLog4J();
            log.info("result=" + new LastfmReleaseProfile("Aphex Twin", "Come to Daddy"));            
        } catch (Exception e) {
            log.error("main(): error", e);
        }        
    }   
        
}