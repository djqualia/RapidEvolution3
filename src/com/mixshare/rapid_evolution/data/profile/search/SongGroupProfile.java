package com.mixshare.rapid_evolution.data.profile.search;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.Identifier;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.data.record.search.SongGroupRecord;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.data.submitted.SubmittedProfile;
import com.mixshare.rapid_evolution.data.submitted.search.SubmittedSongGroupProfile;
import com.mixshare.rapid_evolution.data.util.DegreeValue;
import com.mixshare.rapid_evolution.data.util.DegreeValueSetAverager;
import com.mixshare.rapid_evolution.music.beatintensity.BeatIntensity;
import com.mixshare.rapid_evolution.music.beatintensity.BeatIntensityDescription;
import com.mixshare.rapid_evolution.music.beatintensity.BeatIntensityVariance;
import com.mixshare.rapid_evolution.music.beatintensity.BeatIntensityVarianceDescription;
import com.mixshare.rapid_evolution.music.rating.Rating;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.mixshare.rapid_evolution.util.timing.RWSemaphore;

/**
 * This class combines common functionality between non-song data elements, which have a common aspect of
 * grouping together a set of songs (i.e. artist, label, release).
 */
abstract public class SongGroupProfile extends AbstractSongGroupProfile {

    static private Logger log = Logger.getLogger(SongGroupProfile.class);

    static private float STYLE_DISCARD_THRESHOLD = RE3Properties.getFloat("style_discard_threshold");
    static private float TAG_DISCARD_THRESHOLD = RE3Properties.getFloat("tag_discard_threshold");

    ////////////
    // FIELDS //
    ////////////

    private Map<Integer, Object> associatedSongs = new HashMap<Integer, Object>();

    transient private RWSemaphore songIdSem;

    static {
    	try{
    		// in order for the XMLEncoder to skip transient variables, this is needed
    		BeanInfo info = Introspector.getBeanInfo(SongGroupProfile.class);
    		PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
    		for (int i = 0; i < propertyDescriptors.length; ++i) {
    			PropertyDescriptor pd = propertyDescriptors[i];
    			if (pd.getName().equals("songIdSem")) {
    				pd.setValue("transient", Boolean.TRUE);
    			}
    		}
    	} catch (IntrospectionException e) {
    		log.error("static(): error", e);
    	}
    }

    //////////////////
    // CONSTRUCTORS //
    //////////////////

    public SongGroupProfile() { super(); }
    public SongGroupProfile(LineReader lineReader) {
    	super(lineReader);
    	int version = Integer.parseInt(lineReader.getNextLine());
    	int numAssociatedSongs = Integer.parseInt(lineReader.getNextLine());
    	associatedSongs = new HashMap<Integer, Object>(numAssociatedSongs);
    	for (int i = 0; i < numAssociatedSongs; ++i)
    		associatedSongs.put(Integer.parseInt(lineReader.getNextLine()), null);
    }

    /////////////
    // GETTERS //
    /////////////

    public SongGroupRecord getSongGroupRecord() { return (SongGroupRecord)record; }

    public BeatIntensity getAvgBeatIntensity() { return getSongGroupRecord().getAvgBeatIntensity(); }
    public BeatIntensityDescription getAvgBeatIntensityDescription() { return getSongGroupRecord().getAvgBeatIntensityDescription(); }

    public BeatIntensityVariance getBeatIntensityVariance() { return getSongGroupRecord().getBeatIntensityVariance(); }
    public BeatIntensityVarianceDescription getBeatIntensityVarianceDescription() { return getSongGroupRecord().getBeatIntensityVarianceDescription(); }

    public int getNumSongs() { return getSongGroupRecord().getNumSongs(); }

    public boolean containsSong(Integer uniqueId) { return associatedSongs.containsKey(uniqueId); }
    public boolean containsSong(Identifier id) { return associatedSongs.containsKey(Database.getSongIndex().getUniqueIdFromIdentifier(id)); }

    public Vector<Integer> getSongIds() {
    	Vector<Integer> result = new Vector<Integer>(associatedSongs.size());
    	try {
	    	getSongIdSem().startRead("getSongIds");
	    	Iterator<Integer> iter = associatedSongs.keySet().iterator();
	    	while (iter.hasNext())
	    		result.add(iter.next());
    	} catch (Exception e) {
    		log.error("getSongIds(): error", e);
    	} finally {
    		getSongIdSem().endRead();
    	}
    	return result;
    }

    private RWSemaphore getSongIdSem() {
    	if (songIdSem == null)
    		songIdSem = new RWSemaphore(-1);
    	return songIdSem;
    }

    // for serialization
	public Map<Integer, Object> getAssociatedSongs() { return associatedSongs; }

    /////////////
    // SETTERS //
    /////////////

    public void addSong(Integer uniqueId) { addSong(uniqueId, true); }
    public void addSong(Integer uniqueId, boolean computeMetadata) {
    	try {
    		getRecord().getWriteLockSem().startRead("addSong");
    		getSongIdSem().startWrite("addSong");
    		associatedSongs.put(uniqueId, null);
    	} catch (Exception e) {
    		log.error("addSong(): error", e);
    	} finally {
    		getSongIdSem().endWrite();
    		getRecord().getWriteLockSem().endRead();
    	}
    	if (computeMetadata)
    		computeMetadataFromSongs();
		getSongGroupRecord().setNumSongs(associatedSongs.size());
    }

    public void removeSongs(Vector<Integer> uniqueIds) {
    	try {
    		getRecord().getWriteLockSem().startRead("removeSongs");
    		getSongIdSem().startWrite("removeSong");
    		for (int uniqueId : uniqueIds)
    			associatedSongs.remove(uniqueId);
    	} catch (Exception e) {
    		log.error("removeSong(): error", e);
    	} finally {
    		getSongIdSem().endWrite();
    		getRecord().getWriteLockSem().endRead();
    	}
		computeMetadataFromSongs();
		getSongGroupRecord().setNumSongs(associatedSongs.size());
    }

    public void removeSong(Integer uniqueId) {
    	try {
    		getRecord().getWriteLockSem().startRead("removeSong");
    		getSongIdSem().startWrite("removeSong");
    		associatedSongs.remove(uniqueId);
    	} catch (Exception e) {
    		log.error("removeSong(): error", e);
    	} finally {
    		getSongIdSem().endWrite();
    		getRecord().getWriteLockSem().endRead();
    	}
		computeMetadataFromSongs();
		getSongGroupRecord().setNumSongs(associatedSongs.size());
    }

    public void removeAssociatedSongsBeyond(int songId) {
    	Vector<Integer> idsToRemove = new Vector<Integer>();
    	for (Entry<Integer, Object> entry : associatedSongs.entrySet()) {
    		if (entry.getKey() >= songId)
    			idsToRemove.add(entry.getKey());
    	}
    	for (Integer removedSongId : idsToRemove)
    		associatedSongs.remove(removedSongId);
    }

	// for serialization
	public void setAssociatedSongs(Map<Integer, Object> associatedSongs) { this.associatedSongs = associatedSongs; }

    /////////////
    // METHODS //
    /////////////

	@Override
	public Map<Record, Object> mergeWith(Profile profile) {
		Map<Record, Object> relatedRecords = super.mergeWith(profile);
		SongGroupProfile groupProfile = (SongGroupProfile)profile;
		// song
		try {
			getSongIdSem().startWrite("mergeWith");
			Vector<Integer> otherSongIds = groupProfile.getSongIds();
			for (Integer otherSongId : otherSongIds) {
				if (!containsSong(otherSongId))
					associatedSongs.put(otherSongId, null);
			}
		} catch (Exception e) { } finally {
			getSongIdSem().endWrite();
		}
		computeMetadataFromSongs();
		getSongGroupRecord().setNumSongs(associatedSongs.size());
		return relatedRecords;
	}

    @Override
	public void computeMetadataFromSongs() {
    	if (log.isTraceEnabled())
    		log.trace("computeMetadataFromSongs(): this=" + toString());
    	// avg rating
    	Rating avgRating = computeAverageRatingFromSongs();
    	if (avgRating != null)
    		setRating(avgRating, DATA_SOURCE_COMPUTED);
    	// styles
    	computeStyles();
    	// tags
    	computeTags();
    	// beat intensity
    	computeAverageBeatIntensity();
    }

    public Rating computeAverageRatingFromSongs() {
    	float total = 0.0f;
    	int numRatings = 0;
    	try {
	    	getSongIdSem().startRead("computeAverageRatingFromSongs");
	    	Iterator<Integer> songIdIter = associatedSongs.keySet().iterator();
	    	while (songIdIter.hasNext()) {
	    		Integer songId = songIdIter.next();
	    		SearchRecord searchRecord = Database.getSongIndex().getSearchRecord(songId);
	    		if (searchRecord != null) {
		    		if (searchRecord.getRatingValue().getRatingValue() != 0) {
		    			++numRatings;
		    			total += searchRecord.getRatingValue().getRatingValue();
		    		}
	    		}
	    	}
    	} catch (Exception e) {
    		log.error("computeAverageRatingFromSongs(): error", e);
    	} finally {
    		getSongIdSem().endRead();
    	}
    	if (numRatings > 0) {
    		float weight = 1.0f;
    		if (numRatings < RE3Properties.getInt("song_group_profile_average_rating_alpha"))
    			weight = ((float)numRatings) / RE3Properties.getInt("song_group_profile_average_rating_alpha");
    		float globalWeight = 1.0f - weight;
    		float globalAvg = 60; // 3 stars, should probably use actual averaged value at some point
    		byte rating = (byte)(weight * (total / numRatings) + globalWeight * globalAvg);
    		return Rating.getRating(rating);
    	}
    	return null;
    }

    public void computeAverageBeatIntensity() {
    	Vector<Byte> values = new Vector<Byte>();
    	float total = 0.0f;
    	try {
	    	getSongIdSem().startRead("computeAverageBeatIntensity");
	    	Iterator<Integer> songIdIter = associatedSongs.keySet().iterator();
	    	while (songIdIter.hasNext()) {
	    		Integer songId = songIdIter.next();
	    		SongRecord songRecord = Database.getSongIndex().getSongRecord(songId);
	    		if (songRecord != null) {
	    			byte beatIntensity = songRecord.getBeatIntensity();
	    			if (beatIntensity != 0) {
	    				values.add(beatIntensity);
	    				total += beatIntensity;
	    			}
	    		}
	    	}
    	} catch (Exception e) {
    		log.error("computeAverageBeatIntensity(): error", e);
    	} finally {
    		getSongIdSem().endRead();
    	}
    	if (values.size() > 0) {
    		float avgBeatIntensity = total / values.size();
    		float beatIntensityVariance = 0.0f;
    		for (byte value : values)
    			beatIntensityVariance += (avgBeatIntensity - value) * (avgBeatIntensity - value);
    		beatIntensityVariance = (float)Math.sqrt(beatIntensityVariance);
    		getSongGroupRecord().setAvgBeatIntensity(avgBeatIntensity, beatIntensityVariance);
    	}
    }


    public Vector<DegreeValue> computeStyleDegreesFromSongs() {
    	DegreeValueSetAverager stylesAverager = new DegreeValueSetAverager();
    	Vector<Integer> badSongIds = new Vector<Integer>();
    	try {
	    	getSongIdSem().startRead("computeStyleDegreesFromSongs");
	    	Iterator<Integer> songIdIter = associatedSongs.keySet().iterator();
	    	while (songIdIter.hasNext()) {
	    		Integer songId = songIdIter.next();
	    		SearchRecord searchRecord = Database.getSongIndex().getSearchRecord(songId);
	    		if (searchRecord != null) {
		    		Vector<DegreeValue> styleDegrees = searchRecord.getSourceStyleDegreeValues();
		    		if (styleDegrees.size() > 0)
		    			stylesAverager.addDegreeValueSet(styleDegrees, 1.0f);
	    		} else {
	    			if (log.isDebugEnabled())
	    				log.debug("computeMetadataFromSongs(): null song record for id=" + songId + ", identifier=" + Database.getSongIndex().getIdentifierFromUniqueId(songId));
	    			badSongIds.add(songId);
	    		}
	    	}
    	} catch (Exception e) {
    		log.error("computeStyleDegreesFromSongs(): error", e);
    	} finally {
    		getSongIdSem().endRead();
    	}
    	if (badSongIds.size() > 0)
    		removeBadSongIds(badSongIds);
    	Vector<DegreeValue> result = stylesAverager.getDegrees();
    	for (int i = 0; i < result.size(); ++i) {
    		DegreeValue degree = result.get(i);
    		if (degree.getPercentage() < STYLE_DISCARD_THRESHOLD) {
    			result.remove(i);
    			--i;
    		}
    	}
    	return result;
    }

    public Vector<DegreeValue> computeTagDegreesFromSongs() {
    	DegreeValueSetAverager tagsAverager = new DegreeValueSetAverager();
    	Vector<Integer> badSongIds = new Vector<Integer>();
    	try {
	    	getSongIdSem().startRead("computeTagDegreesFromSongs");
	    	Iterator<Integer> songIdIter = associatedSongs.keySet().iterator();
	    	while (songIdIter.hasNext()) {
	    		Integer songId = songIdIter.next();
	    		SearchRecord searchRecord = Database.getSongIndex().getSearchRecord(songId);
	    		if (searchRecord != null) {
		    		Vector<DegreeValue> tagDegrees = searchRecord.getSourceTagDegreeValues();
		    		if (tagDegrees.size() > 0)
		    			tagsAverager.addDegreeValueSet(tagDegrees, 1.0f);
	    		} else {
	    			if (log.isDebugEnabled())
	    				log.debug("computeMetadataFromSongs(): null song record for id=" + songId + ", identifier=" + Database.getSongIndex().getIdentifierFromUniqueId(songId));
	    			badSongIds.add(songId);
	    		}
	    	}
    	} catch (Exception e) {
    		log.error("computeAvgRatingFromSongs(): error", e);
    	} finally {
    		getSongIdSem().endRead();
    	}
    	if (badSongIds.size() > 0)
    		removeBadSongIds(badSongIds);
    	Vector<DegreeValue> result = tagsAverager.getDegrees();
    	for (int i = 0; i < result.size(); ++i) {
    		DegreeValue degree = result.get(i);
    		if (degree.getPercentage() < TAG_DISCARD_THRESHOLD) {
    			result.remove(i);
    			--i;
    		}
    	}
    	return result;
    }

    private void removeBadSongIds(Vector<Integer> badSongIds) {
    	try {
    		getSongIdSem().startWrite("removeBadSongIds");
    		for (int songId : badSongIds)
    			associatedSongs.remove(songId);
    	} catch (Exception e) {
    		log.error("removeBadSongIds(): error", e);
    	} finally {
    		getSongIdSem().endWrite();
    	}
    }

    @Override
	public void update(SubmittedProfile submittedProfile, boolean overwrite) {
    	super.update(submittedProfile, overwrite);
    	SubmittedSongGroupProfile submittedGroup = (SubmittedSongGroupProfile)submittedProfile;
    	if (submittedGroup.getInitialSongId() != null) {
    		SongRecord songRecord = Database.getSongIndex().getSongRecord(submittedGroup.getInitialSongId());
    		if (songRecord != null)
    			this.addSong(songRecord.getUniqueId());
    	}
    }

    @Override
	public void write(LineWriter writer) {
    	super.write(writer);
    	writer.writeLine("1"); // version
    	writer.writeLine(associatedSongs.size());
    	for (int uniqueId : associatedSongs.keySet())
    		writer.writeLine(uniqueId);
    }

}
