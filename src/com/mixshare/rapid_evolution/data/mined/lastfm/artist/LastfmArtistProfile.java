package com.mixshare.rapid_evolution.data.mined.lastfm.artist;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import net.roarsoftware.lastfm.Album;
import net.roarsoftware.lastfm.Artist;
import net.roarsoftware.lastfm.Event;
import net.roarsoftware.lastfm.Track;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.search.artist.ArtistIdentifier;
import com.mixshare.rapid_evolution.data.mined.MiningAPIFactory;
import com.mixshare.rapid_evolution.data.mined.lastfm.LastfmAPIWrapper;
import com.mixshare.rapid_evolution.data.mined.lastfm.LastfmCommonProfile;
import com.mixshare.rapid_evolution.data.mined.lastfm.event.LastfmEvent;
import com.mixshare.rapid_evolution.data.mined.lastfm.release.LastfmReleaseProfile;
import com.mixshare.rapid_evolution.data.mined.lastfm.song.LastfmSongProfile;
import com.mixshare.rapid_evolution.data.mined.util.MiningLimitReachedException;
import com.mixshare.rapid_evolution.data.record.search.artist.ArtistRecord;
import com.mixshare.rapid_evolution.data.util.DegreeValue;

public class LastfmArtistProfile extends LastfmCommonProfile {

    static private Logger log = Logger.getLogger(LastfmArtistProfile.class);
    static private final long serialVersionUID = 0L;

    static private int LASTFM_NUM_SIMILAR_ARTIST_TO_QUERY = RE3Properties.getInt("lastfm_num_similar_artists_to_query"); // appears max is 250 set by server

    ////////////
    // FIELDS //
    ////////////

    private String artistName;
    private Map<String, Float> similarArtistMap = new LinkedHashMap<String, Float>();
    private Vector<String> similarArtistNames = new Vector<String>(); // preserves case
    private Vector<DegreeValue> topTags;
    private Map<String, Float> topReleases = new HashMap<String, Float>();
    private Map<String, Float> topSongs = new HashMap<String, Float>();
    private Vector<LastfmReleaseProfile> artistReleases = new Vector<LastfmReleaseProfile>();
    private Vector<LastfmSongProfile> artistSongs = new Vector<LastfmSongProfile>();
    private Vector<LastfmEvent> events = new Vector<LastfmEvent>();

    //////////////////
    // CONSTRUCTORS //
    //////////////////

	public LastfmArtistProfile() { super(DATA_TYPE_ARTISTS); }
    public LastfmArtistProfile(String artistName) {
    	super(DATA_TYPE_ARTISTS);
    	this.artistName = artistName;

    	try {
			// new 2.0 style api
		    MiningAPIFactory.getLastfmAPI().getRateController().startQuery();
			Artist artist = Artist.getInfo(artistName, LastfmAPIWrapper.API_KEY);
			if (artist != null) {
		    	loadCommonInfo(artist);
		    	MiningAPIFactory.getLastfmAPI().getRateController().startQuery();
		    	for (Artist similarArtist : Artist.getSimilar(artistName, LASTFM_NUM_SIMILAR_ARTIST_TO_QUERY, LastfmAPIWrapper.API_KEY)) {
		    		similarArtistMap.put(similarArtist.getName().toLowerCase(), similarArtist.getSimilarityMatch() / 100.0f);
		    		similarArtistNames.add(similarArtist.getName());
		    	}
		    	normalizeSimilarItems();
		    	MiningAPIFactory.getLastfmAPI().getRateController().startQuery();
		    	try {
			    	Collection<Album> topAlbums = Artist.getTopAlbums(artistName, LastfmAPIWrapper.API_KEY);
			    	if (topAlbums != null) {
				    	for (Album album : topAlbums) {
				    		artistReleases.add(new LastfmReleaseProfile(album));
				    		topReleases.put(album.getName().toLowerCase(), (float)album.getPlaycount());
				    	}
			    	}
		    	} catch (Exception e) {
		    		if (log.isDebugEnabled())
		    			log.debug("LastfmArtistProfile(): error", e);
		    	}
		    	MiningAPIFactory.getLastfmAPI().getRateController().startQuery();
		    	try {
			    	Collection<Track> topTracks = Artist.getTopTracks(artistName, LastfmAPIWrapper.API_KEY);
			    	if (topTracks != null) {
				    	for (Track track : topTracks) {
				    		artistSongs.add(new LastfmSongProfile(track));
				    		topSongs.put(track.getName().toLowerCase(), (float)track.getPlaycount());
				    	}
			    	}
		    	} catch (Exception e) {
		    		if (log.isDebugEnabled())
		    			log.debug("LastfmArtistProfile(): error", e);
		    	}
		    	MiningAPIFactory.getLastfmAPI().getRateController().startQuery();
		    	try {
			    	Collection<Event> topEvents = Artist.getEvents(artistName, LastfmAPIWrapper.API_KEY);
			    	if (topEvents != null) {
				    	for (Event event: topEvents)
				    		events.add(new LastfmEvent(event));
			    	}
		    	} catch (Exception e) {
		    		if (log.isDebugEnabled())
		    			log.debug("LastfmArtistProfile(): error", e);
		    	}

		    	// old 1.0 style api
		    	//topSongs = LastfmAPIWrapper.getArtistTopSongs(artistName);
		    	topTags = LastfmAPIWrapper.getArtistTopTags(artistName);
		    	//topReleases = LastfmAPIWrapper.getArtistTopReleases(artistName);

		    	if (log.isDebugEnabled())
		    		log.debug("LastfmArtistProfile(): fetched artist=" + artistName);
			}
    	} catch (net.roarsoftware.lastfm.CallException ce) {
    		if (log.isDebugEnabled())
    			log.debug("LastfmArtistProfile(): call exception=" + ce);
    	} catch (MiningLimitReachedException e) {
    		log.error("LastfmArtistProfile(): mining limit reached");
    	}
    }

    /////////////
    // GETTERS //
    /////////////

    public String getArtistName() { return artistName; }

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
    public float getSimilarityWith(ArtistRecord artistRecord) {
    	float maxSimilarity = getSimilarityWith(artistRecord.getLastfmArtistName());
    	float similarity = getSimilarityWith(artistRecord.getArtistName());
    	if (similarity > maxSimilarity)
    		maxSimilarity = similarity;
    	if (artistRecord.getDuplicateIds() != null) {
    		for (int dupId : artistRecord.getDuplicateIds()) {
    			ArtistIdentifier artistId = (ArtistIdentifier)Database.getArtistIndex().getIdentifierFromUniqueId(dupId);
    			if (artistId != null) {
    				similarity = getSimilarityWith(artistId.getName());
    				if (similarity > maxSimilarity)
    					maxSimilarity = similarity;
    			}
    		}
    	}
    	return maxSimilarity;
    }
    public float getSimilarityWith(String artistName) {
        Float result = similarArtistMap.get(artistName.toLowerCase());
        if (result != null)
            return result;
        return 0.0f;
    }
    public int getNumSimilarArtists() { return similarArtistMap.size(); }
    public Vector<String> getSimilarArtistNames() { return similarArtistNames; }
    public Map<String, Float> getSimilarArtistMap() { return similarArtistMap; }

    public float getReachForSong(String title) {
    	Float result = topSongs.get(title.toLowerCase());
    	if (result != null)
    		return result;
    	return 0.0f;
    }
    public int getNumTopSongs() { return topSongs.size(); }

    public float getReachForRelease(String title) {
    	Float result = topReleases.get(title.toLowerCase());
    	if (result != null)
    		return result;
    	return 0.0f;
    }
    public int getNumTopReleases() { return topReleases.size(); }

    public Vector<LastfmSongProfile> getSongs() { return artistSongs; }
    public Vector<LastfmReleaseProfile> getReleases() { return artistReleases; }

	public Map<String, Float> getTopReleases() {
		return topReleases;
	}
	public void setTopReleases(Map<String, Float> topReleases) {
		this.topReleases = topReleases;
	}
	public Map<String, Float> getTopSongs() {
		return topSongs;
	}
    public Vector<LastfmEvent> getEvents() {
		return events;
	}

    /////////////
    // SETTERS //
    /////////////

	public void setTopSongs(Map<String, Float> topSongs) {
		this.topSongs = topSongs;
	}
	public Vector<LastfmReleaseProfile> getArtistReleases() {
		return artistReleases;
	}
	public void setArtistReleases(Vector<LastfmReleaseProfile> artistReleases) {
		this.artistReleases = artistReleases;
	}
	public Vector<LastfmSongProfile> getArtistSongs() {
		return artistSongs;
	}
	public void setArtistSongs(Vector<LastfmSongProfile> artistSongs) {
		this.artistSongs = artistSongs;
	}
	public void setArtistName(String artistName) {
		this.artistName = artistName;
	}
	public void setSimilarArtistMap(Map<String, Float> similarArtistMap) {
		this.similarArtistMap = similarArtistMap;
	}
	public void setSimilarArtistNames(Vector<String> similarArtistNames) {
		this.similarArtistNames = similarArtistNames;
	}
	public void setTopTags(Vector<DegreeValue> topTags) {
		this.topTags = topTags;
	}
	public void setEvents(Vector<LastfmEvent> events) {
		this.events = events;
	}

    /////////////
    // METHODS //
    /////////////

	public void normalizeSimilarItems() {
		float maxValue = 0.0f;
		for (Entry<String, Float> entry : similarArtistMap.entrySet()) {
			if (entry.getValue() > maxValue)
				maxValue = entry.getValue();
		}
		if ((maxValue > 0.0f) && (maxValue < 1.0f)) {
			Map newSimilarArtistMap = new LinkedHashMap<String, Float>(similarArtistMap.size());
			for (Entry<String, Float> entry : similarArtistMap.entrySet()) {
				newSimilarArtistMap.put(entry.getKey(), entry.getValue() / maxValue);
			}
			similarArtistMap = newSimilarArtistMap;
		}
	}

    @Override
	public String toString() {
        StringBuffer result = new StringBuffer();
        result.append(artistName);
        return result.toString();
    }

	@Override
	public boolean equals(Object o) {
		if (o instanceof LastfmArtistProfile) {
			LastfmArtistProfile oP = (LastfmArtistProfile)o;
			return oP.artistName.equalsIgnoreCase(artistName);
		}
		return false;
	}

	@Override
	public int hashCode() { return artistName.toLowerCase().hashCode(); }

    static public void main(String[] args) {
        try {
        	RapidEvolution3.loadLog4J();
        	LastfmArtistProfile profile = new LastfmArtistProfile("Gramatik");
            log.info("result tags=" + profile.getTopTags());

        } catch (Exception e) {
            log.error("main(): error", e);
        }
    }

}