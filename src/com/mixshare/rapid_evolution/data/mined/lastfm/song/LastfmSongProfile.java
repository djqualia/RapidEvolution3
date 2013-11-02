package com.mixshare.rapid_evolution.data.mined.lastfm.song;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import net.roarsoftware.lastfm.Track;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.search.artist.ArtistIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.song.SongIdentifier;
import com.mixshare.rapid_evolution.data.mined.MiningAPIFactory;
import com.mixshare.rapid_evolution.data.mined.lastfm.LastfmAPIWrapper;
import com.mixshare.rapid_evolution.data.mined.lastfm.LastfmCommonProfile;
import com.mixshare.rapid_evolution.data.mined.util.MiningLimitReachedException;
import com.mixshare.rapid_evolution.data.record.search.artist.ArtistRecord;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.data.util.DegreeValue;
import com.mixshare.rapid_evolution.util.timing.Semaphore;

public class LastfmSongProfile extends LastfmCommonProfile {

    private static final long serialVersionUID = 0L;    
    static private Logger log = Logger.getLogger(LastfmSongProfile.class);

    ////////////
    // FIELDS //
    ////////////
    
    private String artistName;
    private String artistMbid;
    private String songTitle;
    private String releaseName;
    private String releaseMbid;
    private int position;
    private String location;
    private int duration; // seconds
    private boolean isFullTrackAvailable;
    private boolean isNowPlaying;    
    private String freeSongURL;
    private String buySongURL;
    private String buyReleaseURL;
    private Vector<LastfmSongProfile> similarSongs = new Vector<LastfmSongProfile>();
    private Map<String, Float> songSimilarityMap = new LinkedHashMap<String, Float>();
    private Vector<DegreeValue> topTags = new Vector<DegreeValue>();
    
    transient private boolean normalizedSimilarity = false;
    transient private Map<String, Float> normalizedMap;
    transient private Semaphore checkNormalizationSem;
    
    static {
    	try{
    		// in order for the XMLEncoder to skip transient variables, this is needed
    		BeanInfo info = Introspector.getBeanInfo(LastfmSongProfile.class);
    		PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
    		for (int i = 0; i < propertyDescriptors.length; ++i) {
    			PropertyDescriptor pd = propertyDescriptors[i];
    			if (pd.getName().equals("normalizedSimilarity") || pd.getName().equals("normalizedMap") || pd.getName().equals("checkNormalizationSem")) {
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
    
    public LastfmSongProfile() { super(DATA_TYPE_SONGS); }
    public LastfmSongProfile(String artistName, String songTitle) {
    	super(DATA_TYPE_SONGS);    	
    	this.artistName = artistName;
    	this.songTitle = songTitle;    	

    	try {
	    	// 2.0 api
    		MiningAPIFactory.getLastfmAPI().getRateController().startQuery();
	    	Track track = Track.getInfo(artistName, songTitle, LastfmAPIWrapper.API_KEY);
	    	if (track != null) {
	    		loadTrack(track);
	    		
	    		try {
	    			Collection<Track> similarTracks = Track.getSimilar(artistName, songTitle, null, LastfmAPIWrapper.API_KEY);
	    			for (Track similarTrack : similarTracks)
	    				similarSongs.add(new LastfmSongProfile(similarTrack));
	    		} catch (Exception e) {
	    			log.error("LastfmSongProfile(): error=" + e); // a null pointer was thrown from within the Track.getSimilar method once, this allows it to continue if so	    		
	    		}
	    		
		    	// old 1.0 api (new api has degrees, but didn't seem to be in the 3rd party wrapper used above)
	    		songSimilarityMap = LastfmAPIWrapper.getSimilarSongs(artistName, songTitle);
	    		normalizeSimilarItems();
		        topTags = LastfmAPIWrapper.getTopSongTags(artistName, songTitle);
		        if (isValid())
		        	log.debug("LastfmSongProfile(): fetched song=" + artistName + " - " + songTitle);
	    	}
    	} catch (net.roarsoftware.lastfm.CallException ce) {
    		log.debug("LastfmSongProfile(): call exception=" + ce);
    		url = null; // to make valid = false
    	} catch (MiningLimitReachedException e) {
    		log.error("LastfmSongProfile(): mining limit reached");
    	}	
    }    
    
    public LastfmSongProfile(Track track) {
    	super(DATA_TYPE_SONGS);    	
    	this.artistName = track.getArtist();
    	this.songTitle = track.getName();
    	loadTrack(track);
    }
    
    protected void loadTrack(Track track) {
    	loadCommonInfo(track);
    	this.artistMbid = track.getArtistMbid();
    	this.duration = track.getDuration();
    	this.releaseName = track.getAlbum();
    	this.releaseMbid = track.getAlbumMbid();
    	this.location = track.getLocation();
    	this.position = track.getPosition();
    	this.isFullTrackAvailable = track.isFullTrackAvailable();
    	this.isNowPlaying = track.isNowPlaying();
    	this.freeSongURL = track.getLastFmInfo("freeTrackURL");
    	this.buySongURL = track.getLastFmInfo("buyTrackURL");
    	this.buyReleaseURL = track.getLastFmInfo("buyAlbumURL");    	
    }
    

    /////////////
    // GETTERS //
    /////////////
    
    public String getArtistName() { return artistName; }
    public String getSongTitle() { return songTitle; }
    
    // tags
    public Vector<DegreeValue> getTopTags() { return topTags; }
    public int getNumTags() { return topTags.size(); }
    public float getTagDegree(String tagName) {
    	for (DegreeValue degree : topTags)
    		if (degree.getName().equalsIgnoreCase(tagName))
    			return degree.getPercentage();
    	return 0.0f;
    }
    
    // similarity
    static public String getSongKey(String artist, String title) {
    	StringBuffer result = new StringBuffer(artist.length() + title.length() + 3);
    	result.append(artist.toLowerCase());
    	result.append(" - ");
    	result.append(title.toLowerCase());
    	return result.toString();
    }    
    public Vector<LastfmSongProfile> getSimilarSongs() { return similarSongs; }

    
    public float getSimilarityWith(SongRecord songRecord) {    	
    	float maxSimilarity = getSimilarityWith(songRecord.getArtistIds(), songRecord.getSongIdentifier().getSongDescription());
    	if (songRecord.getDuplicateIds() != null) {
    		for (int dupId : songRecord.getDuplicateIds()) {
    			SongIdentifier dupSongId = (SongIdentifier)Database.getSongIndex().getIdentifierFromUniqueId(dupId);
    			if (dupSongId != null) {
    				float similarity = getSimilarityWith(songRecord.getArtistIds(), dupSongId.getSongDescription());
    				if (similarity > maxSimilarity)
    					maxSimilarity = similarity;
    			}
    		}
    	}
    	return maxSimilarity;
    }
    
    private float getSimilarityWith(int[] artistIds, String songDescription) {
    	float maxSimilarity = 0.0f;
    	// TODO: fix so multiple artists will be handled properly
    	for (int artistId : artistIds) {
    		ArtistRecord artistRecord = Database.getArtistIndex().getArtistRecord(artistId);
    		if (artistRecord != null) {
    			float similarity = getSimilarityWith(artistRecord.getLastfmArtistName(), songDescription);
    			if (similarity > maxSimilarity)
    				maxSimilarity = similarity;
    			if (artistRecord.getDuplicateIds() != null) {
    				for (int dupArtistId : artistRecord.getDuplicateIds()) {
    					ArtistIdentifier dupArtistIdentifier = (ArtistIdentifier)Database.getArtistIndex().getIdentifierFromUniqueId(dupArtistId);
    		    		if (dupArtistIdentifier != null) {
    		    			similarity = getSimilarityWith(dupArtistIdentifier.getName(), songDescription);
    		    			if (similarity > maxSimilarity)
    		    				maxSimilarity = similarity;
    		    		}
    				}    					
    			}
    		}
    	}
    	return maxSimilarity;
    }
    
    public float getSimilarityWith(String artist, String title) {
    	if (!normalizedSimilarity)
    		checkNormalization();
    	String key = getSongKey(artist, title);
    	Float result = (Float)normalizedMap.get(key);
    	if (result != null)
    		return result.floatValue();
    	return 0.0f;
    }
    
    public String getReleaseName() {
    	if (releaseName == null)
    		return "";
    	return releaseName;
    }
    public String getReleaseTrack() {
    	if (position >= 0) {
    		int track = position + 1;
    		if (track < 10)
    			return "0" + track;
    		return String.valueOf(track);
    	}
    	return "";
    }
    
    /**
     * Returns duration in seconds.
     */
    public int getDuration() { return duration; }
    
    public Semaphore getCheckNormalizationSem() {
    	if (checkNormalizationSem == null)
    		checkNormalizationSem = new Semaphore(1);
    	return checkNormalizationSem;
    }
    
	public String getArtistMbid() {
		return artistMbid;
	}
	public void setArtistMbid(String artistMbid) {
		this.artistMbid = artistMbid;
	}
	public String getReleaseMbid() {
		return releaseMbid;
	}
	public void setReleaseMbid(String releaseMbid) {
		this.releaseMbid = releaseMbid;
	}
	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public boolean isFullTrackAvailable() {
		return isFullTrackAvailable;
	}
	public void setFullTrackAvailable(boolean isFullTrackAvailable) {
		this.isFullTrackAvailable = isFullTrackAvailable;
	}
	public boolean isNowPlaying() {
		return isNowPlaying;
	}
	public void setNowPlaying(boolean isNowPlaying) {
		this.isNowPlaying = isNowPlaying;
	}
	public String getFreeSongURL() {
		return freeSongURL;
	}
	public void setFreeSongURL(String freeSongURL) {
		this.freeSongURL = freeSongURL;
	}
	public String getBuySongURL() {
		return buySongURL;
	}
	public void setBuySongURL(String buySongURL) {
		this.buySongURL = buySongURL;
	}
	public String getBuyReleaseURL() {
		return buyReleaseURL;
	}
	public void setBuyReleaseURL(String buyReleaseURL) {
		this.buyReleaseURL = buyReleaseURL;
	}
	public Map<String, Float> getSongSimilarityMap() {
		return songSimilarityMap;
	}
	public void setSongSimilarityMap(Map<String, Float> songSimilarityMap) {
		this.songSimilarityMap = songSimilarityMap;
	}
	public boolean isNormalizedSimilarity() {
		return normalizedSimilarity;
	}
	public void setNormalizedSimilarity(boolean normalizedSimilarity) {
		this.normalizedSimilarity = normalizedSimilarity;
	}
	public Map<String, Float> getNormalizedMap() {
		return normalizedMap;
	}
	public void setNormalizedMap(Map<String, Float> normalizedMap) {
		this.normalizedMap = normalizedMap;
	}
	public void setArtistName(String artistName) {
		this.artistName = artistName;
	}
	public void setSongTitle(String songTitle) {
		this.songTitle = songTitle;
	}
	public void setReleaseName(String releaseName) {
		this.releaseName = releaseName;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}
	public void setSimilarSongs(Vector<LastfmSongProfile> similarSongs) {
		this.similarSongs = similarSongs;
	}
	public void setTopTags(Vector<DegreeValue> topTags) {
		this.topTags = topTags;
	}
	public void setCheckNormalizationSem(Semaphore checkNormalizationSem) {
		this.checkNormalizationSem = checkNormalizationSem;
	}
    
    
    /////////////
    // METHODS //    
    /////////////
    
	public void normalizeSimilarItems() {
		float maxValue = 0.0f;
		for (Entry<String, Float> entry : songSimilarityMap.entrySet()) {
			if (entry.getValue() > maxValue)
				maxValue = entry.getValue();
		}
		if ((maxValue > 0.0f) && (maxValue < 1.0f)) {
			Map newSongSimilarityMap = new LinkedHashMap<String, Float>(songSimilarityMap.size());
			for (Entry<String, Float> entry : songSimilarityMap.entrySet()) {
				newSongSimilarityMap.put(entry.getKey(), entry.getValue() / maxValue);
			}
			songSimilarityMap = newSongSimilarityMap;
		}
	}
	
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append(artistName);
        result.append(" - ");
        result.append(songTitle);
        return result.toString();
    }
    
    public String toStringFull() {
        StringBuffer result = new StringBuffer();
        result.append("\nARTIST NAME=");
        result.append(artistName);
        result.append("\nARTIST MBID=");
        result.append(artistMbid);
        result.append("\nSONG TITLE=");
        result.append(songTitle);
        result.append("\nRELEASE NAME=");
        result.append(releaseName);
        result.append("\nRELEASE MBID=");
        result.append(releaseMbid);
        result.append("\nPOSITION=");
        result.append(position);
        result.append("\nLOCATION=");
        result.append(location);
        result.append("\nDURATION=");
        result.append(duration);
        result.append("\nTOP TAGS=");
        result.append(topTags);
        result.append("\nSIMILAR SONGS=");
        result.append(similarSongs);
        result.append("\nSONG SIMILARITY MAP=");
        result.append(songSimilarityMap);
        return result.toString();
    }    
    
	public boolean equals(Object o) {		
		if (o instanceof LastfmSongProfile) {
			LastfmSongProfile oP = (LastfmSongProfile)o;
			return oP.artistName.equals(artistName) && oP.songTitle.equals(songTitle);
		}
		return false;
	}
	
	public int hashCode() { return artistName.hashCode() + songTitle.hashCode(); }
	    
	private void checkNormalization() {
		try {
			getCheckNormalizationSem().acquire();
	    	if (!normalizedSimilarity) {
	    		normalizedMap = new HashMap<String, Float>();
				float maxSimilarity = 0.0f;
	    		for (Entry<String, Float> similarSong : songSimilarityMap.entrySet()) {
					float similarity = similarSong.getValue();
					if (similarity > maxSimilarity)
						maxSimilarity = similarity;    			
	    		}
				if (maxSimilarity > 0.0f) {
					for (Entry<String, Float> similarSong : songSimilarityMap.entrySet()) {
						float similarity = similarSong.getValue() / maxSimilarity;
						normalizedMap.put(similarSong.getKey(), new Float(similarity));
					}
				}
	    		normalizedSimilarity = true;
	    	}
		} catch (Exception e) {
			log.error("checkNormalization(): error", e);
		} finally {
			getCheckNormalizationSem().release();
		}
    }
    
    static public void main(String[] args) {
        try {
        	RapidEvolution3.loadLog4J();
        	LastfmSongProfile lastfmProfile = new LastfmSongProfile("MGMT", "Electric Feel");
            log.info("main(): toStringFull=" + lastfmProfile.toStringFull());
            log.info("main(): similar songs=" + lastfmProfile.getSimilarSongs());
        } catch (Exception e) {
            log.error("main(): error", e);
        }        
    }

}
