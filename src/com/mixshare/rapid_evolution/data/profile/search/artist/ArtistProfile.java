package com.mixshare.rapid_evolution.data.profile.search.artist;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.exceptions.AlreadyExistsException;
import com.mixshare.rapid_evolution.data.identifier.search.artist.ArtistIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.label.LabelIdentifier;
import com.mixshare.rapid_evolution.data.mined.billboard.artist.BillboardArtistProfile;
import com.mixshare.rapid_evolution.data.mined.discogs.artist.DiscogsArtistProfile;
import com.mixshare.rapid_evolution.data.mined.echonest.artist.EchonestArtistProfile;
import com.mixshare.rapid_evolution.data.mined.idiomag.artist.IdiomagArtistProfile;
import com.mixshare.rapid_evolution.data.mined.lastfm.LastfmCommonProfile;
import com.mixshare.rapid_evolution.data.mined.lastfm.artist.LastfmArtistProfile;
import com.mixshare.rapid_evolution.data.mined.musicbrainz.artist.MusicbrainzArtistProfile;
import com.mixshare.rapid_evolution.data.mined.yahoo.artist.YahooArtistProfile;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.profile.common.image.Image;
import com.mixshare.rapid_evolution.data.profile.common.link.Link;
import com.mixshare.rapid_evolution.data.profile.common.link.VideoLink;
import com.mixshare.rapid_evolution.data.profile.search.ReleaseGroupProfile;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.data.record.search.artist.ArtistRecord;
import com.mixshare.rapid_evolution.data.record.search.label.LabelRecord;
import com.mixshare.rapid_evolution.data.record.search.release.ReleaseRecord;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.data.submitted.SubmittedProfile;
import com.mixshare.rapid_evolution.data.submitted.search.artist.SubmittedArtist;
import com.mixshare.rapid_evolution.data.util.DegreeValue;
import com.mixshare.rapid_evolution.data.util.DegreeValueSetAverager;
import com.mixshare.rapid_evolution.music.rating.Rating;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.mixshare.rapid_evolution.workflow.TaskManager;
import com.mixshare.rapid_evolution.workflow.mining.discogs.artist.DiscogsArtistFetchResultTrigger;
import com.mixshare.rapid_evolution.workflow.mining.discogs.artist.DiscogsArtistFetchTask;
import com.mixshare.rapid_evolution.workflow.mining.lastfm.LastfmArtistFetchTask;
import com.mixshare.rapid_evolution.workflow.mining.musicbrainz.MusicbrainzArtistFetchTask;

public class ArtistProfile extends ReleaseGroupProfile {

    static private final long serialVersionUID = 0L;
    static private Logger log = Logger.getLogger(ArtistProfile.class);

    static private float MIN_SONGS_FOR_SIMILARITY_CONFIDENCE = 10;

    static private float ARTIST_SIMILARITY_CONFIDENCE_THRESHOLD;

    static private float ARTIST_SIMILARITY_STYLES_WEIGHT;
    static private float ARTIST_SIMILARITY_TAGS_WEIGHT;
    static private float ARTIST_SIMILARITY_LASTFM_WEIGHT;
    static private float ARTIST_SIMILARITY_LABELS_WEIGHT;
    static private float ARTIST_SIMILARITY_ECHONEST_WEIGHT;
    static private float ARTIST_SIMILARITY_IDIOMAG_WEIGHT;
    static private float ARTIST_SIMILARITY_YAHOO_WEIGHT;
    static private float ARTIST_SIMILARITY_BEAT_INTENSITY_WEIGHT;

    static private float ARTIST_COMPUTED_STYLES_DISCOGS_WEIGHT;
    static private float ARTIST_COMPUTED_STYLES_MIXSHARE_WEIGHT;
    static private float ARTIST_COMPUTED_STYLES_RELEASE_STYLES_WEIGHT;
    static private float ARTIST_COMPUTED_STYLES_SONG_STYLES_WEIGHT;
    static private float ARTIST_COMPUTED_STYLES_YAHOO_CATEGORIES_WEIGHT;

    static private float ARTIST_COMPUTED_TAGS_LASTFM_WEIGHT;
    static private float ARTIST_COMPUTED_TAGS_MIXSHARE_WEIGHT;
    static private float ARTIST_COMPUTED_TAGS_MUSICBRAINZ_WEIGHT;
    static private float ARTIST_COMPUTED_TAGS_RELEASE_TAGS_WEIGHT;
    static private float ARTIST_COMPUTED_TAGS_SONG_TAGS_WEIGHT;
    static private float ARTIST_COMPUTED_TAGS_IDIOMAG_WEIGHT;
    static private float ARTIST_COMPUTED_TAGS_ECHONEST_WEIGHT;

    static private float ARTIST_SCORE_LASTFM_AVG_PLAYCOUNT_WEIGHT;
    static private float ARTIST_SCORE_LASTFM_AVG_PLAYCOUNT_NORMALIZE_VALUE;
    static private float ARTIST_SCORE_DISCOGS_AVG_RATING_WEIGHT;
    static private float ARTIST_SCORE_ECHONEST_HOTNESS_WEIGHT;
    static private float ARTIST_SCORE_MUSICBRAINZ_AVG_RATING_WEIGHT;

    static private float ARTIST_POPULARITY_LASTFM_NUM_LISTENERS_WEIGHT;
    static private float ARTIST_POPULARITY_LASTFM_NUM_LISTENERS_NORMALIZE_VALUE;
    static private float ARTIST_POPULARITY_DISCOGS_NUM_RATERS_WEIGHT;
    static private float ARTIST_POPULARITY_DISCOGS_NUM_RATERS_NORMALIZE_VALUE;
    static private float ARTIST_POPULARITY_ECHONEST_FAMILIARITY_WEIGHT;
    static private float ARTIST_POPULARITY_MUSICBRAINZ_NUM_RATERS_WEIGHT;
    static private float ARTIST_POPULARITY_MUSICBRAINZ_NUM_RATERS_NORMALIZE_VALUE;
    static private float ARTIST_POPULARITY_BILLBOARD_NUM_WEEKS_WEIGHT;
    static private float ARTIST_POPULARITY_BILLBOARD_NUM_WEEKS_NORMALIZE_VALUE;

    static {
    	loadProperties();
    }

    static public void loadProperties() {
    	ARTIST_SIMILARITY_CONFIDENCE_THRESHOLD = RE3Properties.getFloat("artist_similarity_confidence_threshold");

        ARTIST_SIMILARITY_STYLES_WEIGHT = RE3Properties.getFloat("artist_similarity_styles_weight");
        ARTIST_SIMILARITY_TAGS_WEIGHT = RE3Properties.getFloat("artist_similarity_tags_weight");
        ARTIST_SIMILARITY_LASTFM_WEIGHT = RE3Properties.getFloat("artist_similarity_lastfm_weight");
        ARTIST_SIMILARITY_LABELS_WEIGHT = RE3Properties.getFloat("artist_similarity_labels_weight");
        ARTIST_SIMILARITY_ECHONEST_WEIGHT = RE3Properties.getFloat("artist_similarity_echonest_weight");
        ARTIST_SIMILARITY_IDIOMAG_WEIGHT = RE3Properties.getFloat("artist_similarity_idiomag_weight");
        ARTIST_SIMILARITY_YAHOO_WEIGHT = RE3Properties.getFloat("artist_similarity_yahoo_weight");
        ARTIST_SIMILARITY_BEAT_INTENSITY_WEIGHT = RE3Properties.getFloat("artist_similarity_beat_intensity_weight");

        ARTIST_COMPUTED_STYLES_DISCOGS_WEIGHT = RE3Properties.getFloat("artist_computed_styles_discogs_weight");
        ARTIST_COMPUTED_STYLES_MIXSHARE_WEIGHT = RE3Properties.getFloat("artist_computed_styles_mixshare_weight");
        ARTIST_COMPUTED_STYLES_RELEASE_STYLES_WEIGHT = RE3Properties.getFloat("artist_computed_styles_release_styles_weight");
        ARTIST_COMPUTED_STYLES_SONG_STYLES_WEIGHT = RE3Properties.getFloat("artist_computed_styles_song_styles_weight");
        ARTIST_COMPUTED_STYLES_YAHOO_CATEGORIES_WEIGHT = RE3Properties.getFloat("artist_computed_styles_yahoo_categories_weight");

        ARTIST_COMPUTED_TAGS_LASTFM_WEIGHT = RE3Properties.getFloat("artist_computed_tags_lastfm_weight");
        ARTIST_COMPUTED_TAGS_MIXSHARE_WEIGHT = RE3Properties.getFloat("artist_computed_tags_mixshare_weight");
        ARTIST_COMPUTED_TAGS_MUSICBRAINZ_WEIGHT = RE3Properties.getFloat("artist_computed_tags_musicbrainz_weight");
        ARTIST_COMPUTED_TAGS_RELEASE_TAGS_WEIGHT = RE3Properties.getFloat("artist_computed_tags_release_tags_weight");
        ARTIST_COMPUTED_TAGS_SONG_TAGS_WEIGHT = RE3Properties.getFloat("artist_computed_tags_song_tags_weight");
        ARTIST_COMPUTED_TAGS_IDIOMAG_WEIGHT = RE3Properties.getFloat("artist_computed_tags_idiomag_weight");
        ARTIST_COMPUTED_TAGS_ECHONEST_WEIGHT = RE3Properties.getFloat("artist_computed_tags_echonest_weight");

        ARTIST_SCORE_LASTFM_AVG_PLAYCOUNT_WEIGHT = RE3Properties.getFloat("artist_score_lastfm_avg_playcount_weight");
        ARTIST_SCORE_LASTFM_AVG_PLAYCOUNT_NORMALIZE_VALUE = RE3Properties.getFloat("artist_score_lastfm_avg_playcount_normalize_value");
        ARTIST_SCORE_DISCOGS_AVG_RATING_WEIGHT = RE3Properties.getFloat("artist_score_discogs_avg_rating_weight");
        ARTIST_SCORE_ECHONEST_HOTNESS_WEIGHT = RE3Properties.getFloat("artist_score_echonest_hotness_weight");
        ARTIST_SCORE_MUSICBRAINZ_AVG_RATING_WEIGHT = RE3Properties.getFloat("artist_score_musicbrainz_avg_rating_weight");

        ARTIST_POPULARITY_LASTFM_NUM_LISTENERS_WEIGHT = RE3Properties.getFloat("artist_popularity_lastfm_num_listeners_weight");
        ARTIST_POPULARITY_LASTFM_NUM_LISTENERS_NORMALIZE_VALUE = RE3Properties.getFloat("artist_popularity_lastfm_num_listeners_normalize_value");
        ARTIST_POPULARITY_DISCOGS_NUM_RATERS_WEIGHT = RE3Properties.getFloat("artist_popularity_discogs_num_raters_weight");
        ARTIST_POPULARITY_DISCOGS_NUM_RATERS_NORMALIZE_VALUE = RE3Properties.getFloat("artist_popularity_discogs_num_raters_normalize_value");
        ARTIST_POPULARITY_ECHONEST_FAMILIARITY_WEIGHT = RE3Properties.getFloat("artist_popularity_echonest_familiarity_weight");
        ARTIST_POPULARITY_MUSICBRAINZ_NUM_RATERS_WEIGHT = RE3Properties.getFloat("artist_popularity_musicbrainz_num_raters_weight");
        ARTIST_POPULARITY_MUSICBRAINZ_NUM_RATERS_NORMALIZE_VALUE = RE3Properties.getFloat("artist_popularity_musicbrainz_num_raters_normalize_value");
        ARTIST_POPULARITY_BILLBOARD_NUM_WEEKS_WEIGHT = RE3Properties.getFloat("artist_popularity_billboard_num_weeks_weight");
        ARTIST_POPULARITY_BILLBOARD_NUM_WEEKS_NORMALIZE_VALUE = RE3Properties.getFloat("artist_popularity_billboard_num_weeks_normalize_value");
    }

    ////////////
    // FIELDS //
    ////////////

    private byte discogsArtistNameSource;
    private byte lastfmArtistNameSource;
    private byte mbIdSource;

    static {
    	try{
    		// in order for the XMLEncoder to skip transient variables, this is needed
    		BeanInfo info = Introspector.getBeanInfo(ArtistProfile.class);
    		PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
    		for (int i = 0; i < propertyDescriptors.length; ++i) {
    			PropertyDescriptor pd = propertyDescriptors[i];
    			if (pd.getName().equals("artistName") || pd.getName().equals("discogsArtistName") || pd.getName().equals("lastfmArtistName") || pd.getName().equals("mbId")) {
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

    public ArtistProfile() { };
    public ArtistProfile(ArtistIdentifier artistId, int fileId) {
    	record = new ArtistRecord(artistId, fileId);
    }
    public ArtistProfile(LineReader lineReader) {
    	super(lineReader);
    	int version = Integer.parseInt(lineReader.getNextLine());
    	discogsArtistNameSource = Byte.parseByte(lineReader.getNextLine());
    	lastfmArtistNameSource = Byte.parseByte(lineReader.getNextLine());
    	mbIdSource = Byte.parseByte(lineReader.getNextLine());
    }

    /////////////
    // GETTERS //
    /////////////

    public ArtistRecord getArtistRecord() { return (ArtistRecord)record; }
    public ArtistIdentifier getArtistIdentifier() { return getArtistRecord().getArtistIdentifier(); }

    public String getArtistName() { return getArtistRecord().getArtistName(); }

    public String getDiscogsArtistName() { return getArtistRecord().getDiscogsArtistName(); }
    public String getLastfmArtistName() { return getArtistRecord().getLastfmArtistName(); }
    public String getMbId() { return getArtistRecord().getMbId(); }

    public int[] getLabelIds() { return getArtistRecord().getLabelIds(); }
    public int getNumLabels() { return getArtistRecord().getNumLabels(); }
    public Vector<Integer> getLabelIdsVector() { return getArtistRecord().getLabelIdsVector(); }
    public float getLabelDegree(String labelName) { return getArtistRecord().getLabelDegree(labelName); }

    // for serialization
	public byte getDiscogsArtistNameSource() { return discogsArtistNameSource; }
	public byte getLastfmArtistNameSource() { return lastfmArtistNameSource; }
	public byte getMbIdSource() { return mbIdSource; }

    /////////////
    // SETTERS //
    /////////////

    public void setArtistName(String artistName) throws AlreadyExistsException {
    	if (!getArtistName().equals(artistName)) {
    		ArtistIdentifier oldArtistId = getArtistIdentifier();
    		ArtistIdentifier newArtistId = new ArtistIdentifier(artistName);
    		boolean unlocked = false;
    		try {
    			getRecord().getWriteLockSem().startRead("setArtistName");
    			updateIdentifier(newArtistId, oldArtistId);
    			getRecord().getWriteLockSem().endRead();
    			unlocked = true;
    			// update relational items
    			for (int songId : getSongIds()) {
    				SongRecord songRecord = Database.getSongIndex().getSongRecord(songId);
    				if (songRecord != null)
    					songRecord.update();
    			}
    			for (int releaseId : getReleaseIds()) {
    				ReleaseRecord releaseRecord = Database.getReleaseIndex().getReleaseRecord(releaseId);
    				if (releaseRecord != null)
    					releaseRecord.update();
    			}
    			if (getLabelIds() != null) {
	    			for (int labelId : getLabelIds()) {
	    				LabelRecord labelRecord = Database.getLabelIndex().getLabelRecord(labelId);
	    				if (labelRecord != null)
	    					labelRecord.update();
	    			}
    			}
    		} catch (InterruptedException e) {
    		} finally {
    			if (!unlocked)
    				getRecord().getWriteLockSem().endRead();
    		}
    	}
    }

    public void setDiscogsArtistName(String discogsArtistName, byte discogsArtistNameSource, boolean triggerUpdate) {
    	if ((this.discogsArtistNameSource == DATA_SOURCE_USER) && (discogsArtistNameSource != DATA_SOURCE_USER))
    		return;
    	getArtistRecord().setDiscogsArtistName(discogsArtistName);
    	removeMinedProfile(DATA_SOURCE_DISCOGS);
    	this.discogsArtistNameSource = discogsArtistNameSource;
		if (triggerUpdate && (RE3Properties.getBoolean("enable_triggered_data_miners") || RE3Properties.getBoolean("enable_discogs_data_miners")))
			TaskManager.runBackgroundTask(new DiscogsArtistFetchTask(this, RE3Properties.getInt("discogs_mining_task_priority") + 5, new DiscogsArtistFetchResultTrigger(this)));
    }

    public void setLastfmArtistName(String lastfmArtistName, byte lastfmArtistNameSource, boolean triggerUpdate) {
    	if ((this.lastfmArtistNameSource == DATA_SOURCE_USER) && (lastfmArtistNameSource != DATA_SOURCE_USER))
    		return;
    	getArtistRecord().setLastfmArtistName(lastfmArtistName);
    	removeMinedProfile(DATA_SOURCE_LASTFM);
    	this.lastfmArtistNameSource = lastfmArtistNameSource;
    	if (triggerUpdate && (RE3Properties.getBoolean("enable_triggered_data_miners") || RE3Properties.getBoolean("enable_lastfm_data_miners")))
    		TaskManager.runBackgroundTask(new LastfmArtistFetchTask(this, RE3Properties.getInt("lastfm_mining_task_priority") + 5));
    }

    public void setMbId(String mbId, byte mbidDataSource, boolean triggerUpdate) {
    	if ((this.mbIdSource == DATA_SOURCE_USER) && (mbidDataSource != DATA_SOURCE_USER))
    		return;
    	getArtistRecord().setMbId(mbId);
    	removeMinedProfile(DATA_SOURCE_MUSICBRAINZ);
    	this.mbIdSource = mbidDataSource;
    	if (triggerUpdate && (RE3Properties.getBoolean("enable_triggered_data_miners") || RE3Properties.getBoolean("enable_musicbrainz_data_miners")))
    		TaskManager.runBackgroundTask(new MusicbrainzArtistFetchTask(this, RE3Properties.getInt("musicbrainz_mining_task_priority") + 5));
    }

	// for serialization
	public void setDiscogsArtistNameSource(byte discogsArtistNameSource) { this.discogsArtistNameSource = discogsArtistNameSource; }
	public void setLastfmArtistNameSource(byte lastfmArtistNameSource) { this.lastfmArtistNameSource = lastfmArtistNameSource; }
	public void setMbIdSource(byte mbIdSource) { this.mbIdSource = mbIdSource; }

    /////////////
    // METHODS //
    /////////////

	@Override
	public Map<Record, Object> mergeWith(Profile profile) {
		Map<Record, Object> relatedRecords = super.mergeWith(profile);
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
		// recompute label degrees
    	int total = 0;
    	Map<LabelIdentifier, Integer> labelCount = new HashMap<LabelIdentifier, Integer>(getNumReleases() / 2);
    	for (Integer songId : getSongIds()) {
    		SongRecord songRecord = (SongRecord)Database.getSongIndex().getRecord(songId);
    		if (songRecord != null) {
	    		for (LabelRecord label : songRecord.getLabels()) {
	    			LabelIdentifier labelId = label.getLabelIdentifier();
	    			Integer count = labelCount.get(labelId);
	    			if (count == null)
	    				count = new Integer(1);
	    			else
	    				count = new Integer(1 + count.intValue());
	    			labelCount.put(labelId, count);
	    			++total;
	    		}
    		} else {
    			log.warn("computeMetadataFromSongs(): song record does not exist with uniqueId=" + songId);
    		}
    	}
    	Vector<DegreeValue> labelDegrees = new Vector<DegreeValue>(labelCount.size());
    	for (Entry<LabelIdentifier, Integer> entry : labelCount.entrySet())
    		labelDegrees.add(new DegreeValue(entry.getKey().toString(), (float)entry.getValue() / total, DATA_SOURCE_COMPUTED));
    	if (log.isTraceEnabled())
    		log.trace("computeMetadataFromSongs(): computed label degrees=" + labelDegrees);
    	getArtistRecord().setLabelDegrees(labelDegrees);
    }

	@Override
	public void clearSimilaritySearchTransients() { }
    @Override
	public float getSimilarity(SearchRecord record) {
    	if (record == null)
    		return 0.0f;
    	float numerator = 0.0f;
        float denominator = 0.0f;
        float confidence = 1.0f;

    	ArtistRecord artistRecord = (ArtistRecord)record;

        if (getNumSongs() < MIN_SONGS_FOR_SIMILARITY_CONFIDENCE)
            confidence = confidence * getNumSongs() / MIN_SONGS_FOR_SIMILARITY_CONFIDENCE;
        if (artistRecord.getNumSongs() < MIN_SONGS_FOR_SIMILARITY_CONFIDENCE)
            confidence = confidence * artistRecord.getNumSongs() / MIN_SONGS_FOR_SIMILARITY_CONFIDENCE;

        // STYLE SIMILARITY
        if ((ARTIST_SIMILARITY_STYLES_WEIGHT > 0.0f) && (getNumActualStyles() > 0) && (record.getNumActualStyles() > 0)) {
        	float styleSimilarity = getArtistRecord().computeStyleSimilarity(record);
        	if (!Float.isNaN(styleSimilarity))
        		numerator += styleSimilarity * ARTIST_SIMILARITY_STYLES_WEIGHT * confidence;
        	denominator += ARTIST_SIMILARITY_STYLES_WEIGHT * confidence;
        }

        // TAG SIMILARITY
        if ((ARTIST_SIMILARITY_TAGS_WEIGHT > 0.0f) && (getNumActualTags() > 0) && (record.getNumActualTags() > 0)) {
        	float tagSimilarity = getArtistRecord().computeTagSimilarity(record);
        	if (!Float.isNaN(tagSimilarity))
        		numerator += tagSimilarity * ARTIST_SIMILARITY_TAGS_WEIGHT;
        	denominator += ARTIST_SIMILARITY_TAGS_WEIGHT;
        }

        // LASTFM SIMILARITY
        if (ARTIST_SIMILARITY_LASTFM_WEIGHT > 0.0f) {
        	LastfmArtistProfile artistProfile = (LastfmArtistProfile)getMinedProfile(DATA_SOURCE_LASTFM);
        	if (artistProfile != null) {
        		numerator += artistProfile.getSimilarityWith(artistRecord) * ARTIST_SIMILARITY_LASTFM_WEIGHT;
        	}
        	denominator += ARTIST_SIMILARITY_LASTFM_WEIGHT;
        }

        // ECHONEST SIMILARITY
        if (ARTIST_SIMILARITY_ECHONEST_WEIGHT > 0.0f) {
        	EchonestArtistProfile artistProfile = (EchonestArtistProfile)getMinedProfile(DATA_SOURCE_ECHONEST);
        	if (artistProfile != null) {
        		numerator += artistProfile.getSimilarityWith(artistRecord) * ARTIST_SIMILARITY_ECHONEST_WEIGHT;
        	}
        	denominator += ARTIST_SIMILARITY_ECHONEST_WEIGHT;
        }

        // IDIOMAG SIMILARITY
        if (ARTIST_SIMILARITY_IDIOMAG_WEIGHT > 0.0f) {
        	IdiomagArtistProfile artistProfile = (IdiomagArtistProfile)getMinedProfile(DATA_SOURCE_IDIOMAG);
        	if (artistProfile != null) {
        		numerator += artistProfile.getSimilarityWith(artistRecord) * ARTIST_SIMILARITY_IDIOMAG_WEIGHT;
        	}
        	denominator += ARTIST_SIMILARITY_IDIOMAG_WEIGHT;
        }

        // YAHOO SIMILARITY
        if (ARTIST_SIMILARITY_YAHOO_WEIGHT > 0.0f) {
        	YahooArtistProfile artistProfile = (YahooArtistProfile)getMinedProfile(DATA_SOURCE_YAHOO);
        	if (artistProfile != null) {
        		numerator += artistProfile.getSimilarityWith(artistRecord) * ARTIST_SIMILARITY_YAHOO_WEIGHT;
        	}
        	denominator += ARTIST_SIMILARITY_YAHOO_WEIGHT;
        }

        // LABELS SIMILARITY
        if ((ARTIST_SIMILARITY_LABELS_WEIGHT > 0.0f) && (getNumLabels() > 0) && (artistRecord.getNumLabels() > 0)) {
        	float labelsSimilarity = getArtistRecord().computeLabelSimilarity(artistRecord);
        	if (!Float.isNaN(labelsSimilarity))
        		numerator += labelsSimilarity * ARTIST_SIMILARITY_LABELS_WEIGHT;
        	denominator += ARTIST_SIMILARITY_LABELS_WEIGHT;
        }

        // BEAT INTENSITY SIMILARITY
        if ((ARTIST_SIMILARITY_BEAT_INTENSITY_WEIGHT > 0.0f) && getAvgBeatIntensity().isValid() && artistRecord.getAvgBeatIntensity().isValid()) {
        	float beatIntensitySimilarity = getAvgBeatIntensity().getSimilarityWith(artistRecord.getAvgBeatIntensity());
        	float beatIntensityVarianceSimilarity = getBeatIntensityVariance().getSimilarityWith(artistRecord.getBeatIntensityVariance());
        	numerator += beatIntensitySimilarity * beatIntensityVarianceSimilarity * ARTIST_SIMILARITY_BEAT_INTENSITY_WEIGHT;
        	denominator += ARTIST_SIMILARITY_BEAT_INTENSITY_WEIGHT;
        }

        if (denominator > 0.0f) {
        	float scale = 1.0f;
        	if (denominator < ARTIST_SIMILARITY_CONFIDENCE_THRESHOLD)
        		scale = denominator / ARTIST_SIMILARITY_CONFIDENCE_THRESHOLD;
            return numerator * scale / denominator;
        }
        return 0.0f;
    }

    @Override
	public void computeScore() {
    	float score = 0.0f;
    	float totalWeight = 0.0f;

    	// lastfm play count
    	LastfmCommonProfile lastfmProfile = (LastfmCommonProfile)getMinedProfile(DATA_SOURCE_LASTFM);
    	if ((lastfmProfile != null) && (lastfmProfile.getNumListeners() > 0)) {
    		float avgPlayersPerListener = lastfmProfile.getPlayCount() / lastfmProfile.getNumListeners();
    		float playCountNormalized = avgPlayersPerListener / ARTIST_SCORE_LASTFM_AVG_PLAYCOUNT_NORMALIZE_VALUE;
    		if (playCountNormalized > 1.0f)
    			playCountNormalized = 1.0f;
    		score += playCountNormalized * ARTIST_SCORE_LASTFM_AVG_PLAYCOUNT_WEIGHT;
    		totalWeight += ARTIST_SCORE_LASTFM_AVG_PLAYCOUNT_WEIGHT;
    	}

    	// discogs avg rating
    	DiscogsArtistProfile discogsProfile = (DiscogsArtistProfile)getMinedProfile(DATA_SOURCE_DISCOGS);
    	if (discogsProfile != null) {
    		float avgRating = discogsProfile.getAvgRating(); // 1 to 5 stars
    		if ((avgRating != 0.0f) && !Float.isNaN(avgRating)) {
    			float avgRatingNormalized = (avgRating - 1.0f) / 4.0f;
    			score += avgRatingNormalized * ARTIST_SCORE_DISCOGS_AVG_RATING_WEIGHT;
    			totalWeight += ARTIST_SCORE_DISCOGS_AVG_RATING_WEIGHT;
    		}
    	}

    	// echonest hotness
    	EchonestArtistProfile echonestProfile = (EchonestArtistProfile)getMinedProfile(DATA_SOURCE_ECHONEST);
    	if (echonestProfile != null) {
    		float hotness = echonestProfile.getHotness(); // already normalized between 0 and 1.0
    		score += hotness * ARTIST_SCORE_ECHONEST_HOTNESS_WEIGHT;
    		totalWeight += ARTIST_SCORE_ECHONEST_HOTNESS_WEIGHT;
    	}

    	// musicbrainz avg rating
    	MusicbrainzArtistProfile musicbrainzProfile = (MusicbrainzArtistProfile)getMinedProfile(DATA_SOURCE_MUSICBRAINZ);
    	if (musicbrainzProfile != null) {
    		float avgRating = musicbrainzProfile.getAvgRating(); // 1 to 5 stars
    		if ((avgRating != 0.0f) && !Float.isNaN(avgRating)) {
    			float avgRatingNormalized = (avgRating - 1.0f) / 4.0f;
    			score += avgRatingNormalized * ARTIST_SCORE_MUSICBRAINZ_AVG_RATING_WEIGHT;
    			totalWeight += ARTIST_SCORE_MUSICBRAINZ_AVG_RATING_WEIGHT;
    		}
    	}

    	if (totalWeight > 0.0f) {
    		score /= totalWeight;
    		getSearchRecord().setScore(score);
    	}
    }

    @Override
	public void computePopularity() {
    	float popularity = 0.0f;
    	float totalWeight = 0.0f;

    	// lastfm num listeners
    	LastfmCommonProfile lastfmProfile = (LastfmCommonProfile)getMinedProfile(DATA_SOURCE_LASTFM);
    	if (lastfmProfile != null) {
    		float numListeners = lastfmProfile.getNumListeners(); // unbounded
    		float normalizedNumListeners = numListeners / ARTIST_POPULARITY_LASTFM_NUM_LISTENERS_NORMALIZE_VALUE;
    		if (normalizedNumListeners > 1.0f)
    			normalizedNumListeners = 1.0f;
    		popularity += normalizedNumListeners * ARTIST_POPULARITY_LASTFM_NUM_LISTENERS_WEIGHT;
    		totalWeight += ARTIST_POPULARITY_LASTFM_NUM_LISTENERS_WEIGHT;
    	}

    	// discogs num raters
    	DiscogsArtistProfile discogsProfile = (DiscogsArtistProfile)getMinedProfile(DATA_SOURCE_DISCOGS);
    	if (discogsProfile != null) {
    		float numUniqueRaters = discogsProfile.getNumUniqueRaters(); // unbounded
    		float numUniqueRatersNormalized = numUniqueRaters / ARTIST_POPULARITY_DISCOGS_NUM_RATERS_NORMALIZE_VALUE;
    		if (numUniqueRatersNormalized > 1.0f)
    			numUniqueRatersNormalized = 1.0f;
    		popularity += numUniqueRatersNormalized * ARTIST_POPULARITY_DISCOGS_NUM_RATERS_WEIGHT;
    		totalWeight += ARTIST_POPULARITY_DISCOGS_NUM_RATERS_WEIGHT;
    	}

    	// echonest familiarity
    	EchonestArtistProfile echonestProfile = (EchonestArtistProfile)getMinedProfile(DATA_SOURCE_ECHONEST);
    	if (echonestProfile != null) {
    		float familiarity = echonestProfile.getFamiliarity(); // already normalized
    		popularity += familiarity * ARTIST_POPULARITY_ECHONEST_FAMILIARITY_WEIGHT;
    		totalWeight += ARTIST_POPULARITY_ECHONEST_FAMILIARITY_WEIGHT;
    	}

    	// musicbrainz num raters
    	MusicbrainzArtistProfile musicbrainzProfile = (MusicbrainzArtistProfile)getMinedProfile(DATA_SOURCE_MUSICBRAINZ);
    	if (musicbrainzProfile != null) {
    		float numRaters = musicbrainzProfile.getNumRaters(); // unbounded
    		float numRatersNormalized = numRaters / ARTIST_POPULARITY_MUSICBRAINZ_NUM_RATERS_NORMALIZE_VALUE;
    		if (numRatersNormalized > 1.0f)
    			numRatersNormalized = 1.0f;
    		popularity += numRatersNormalized * ARTIST_POPULARITY_MUSICBRAINZ_NUM_RATERS_WEIGHT;
    		totalWeight += ARTIST_POPULARITY_MUSICBRAINZ_NUM_RATERS_WEIGHT;
    	}

    	// billboard total weeks
    	BillboardArtistProfile billboardProfile = (BillboardArtistProfile)getMinedProfile(DATA_SOURCE_BILLBOARD);
    	if (billboardProfile != null) {
    		float totalWeeks = billboardProfile.getTotalWeeksOn(); // unbounded
    		float totalWeeksNormalized = totalWeeks / ARTIST_POPULARITY_BILLBOARD_NUM_WEEKS_NORMALIZE_VALUE;
    		if (totalWeeksNormalized > 1.0f)
    			totalWeeksNormalized = 1.0f;
    		popularity += totalWeeksNormalized * ARTIST_POPULARITY_BILLBOARD_NUM_WEEKS_WEIGHT;
    		totalWeight += ARTIST_POPULARITY_BILLBOARD_NUM_WEEKS_WEIGHT;
    	}

    	if (totalWeight > 0.0f) {
    		popularity /= totalWeight;
    		getSearchRecord().setPopularity(popularity);
    	}
    }

    @Override
	public void computeTags() {
    	DegreeValueSetAverager averagedTags = new DegreeValueSetAverager();

    	// lastfm
    	LastfmArtistProfile lastfmProfile = (LastfmArtistProfile)getMinedProfile(DATA_SOURCE_LASTFM);
    	if (lastfmProfile != null) {
    		Vector<DegreeValue> tags = lastfmProfile.getTopTags();
    		if ((tags != null) && (tags.size() > 0))
    			averagedTags.addDegreeValueSet(tags, ARTIST_COMPUTED_TAGS_LASTFM_WEIGHT);
    	}

    	// echonest
    	EchonestArtistProfile echonestProfile = (EchonestArtistProfile)getMinedProfile(DATA_SOURCE_ECHONEST);
    	if (echonestProfile != null) {
    		Vector<DegreeValue> tags = echonestProfile.getTags();
    		if ((tags != null) && (tags.size() > 0))
    			averagedTags.addDegreeValueSet(tags, ARTIST_COMPUTED_TAGS_ECHONEST_WEIGHT);
    	}

    	// idiomag
    	IdiomagArtistProfile idiomagProfile = (IdiomagArtistProfile)getMinedProfile(DATA_SOURCE_IDIOMAG);
    	if (idiomagProfile != null) {
    		Vector<DegreeValue> tags = idiomagProfile.getTagDegrees();
    		if ((tags != null) && (tags.size() > 0))
    			averagedTags.addDegreeValueSet(tags, ARTIST_COMPUTED_TAGS_IDIOMAG_WEIGHT);
    	}

    	// musicbrainz
    	MusicbrainzArtistProfile musicbrainzProfile = (MusicbrainzArtistProfile)getMinedProfile(DATA_SOURCE_MUSICBRAINZ);
    	if (musicbrainzProfile != null) {
    		Vector<DegreeValue> tags = musicbrainzProfile.getTagDegrees();
    		if ((tags != null) && (tags.size() > 0))
    			averagedTags.addDegreeValueSet(tags, ARTIST_COMPUTED_TAGS_MUSICBRAINZ_WEIGHT);
    	}

    	// song tags
    	Vector<DegreeValue> songTagDegrees = computeTagDegreesFromSongs();
    	if (songTagDegrees.size() > 0)
    		averagedTags.addDegreeValueSet(songTagDegrees, ARTIST_COMPUTED_TAGS_SONG_TAGS_WEIGHT);

    	// release tags
    	Vector<DegreeValue> releaseTagDegrees = computeTagDegreesFromReleases();
    	if (releaseTagDegrees.size() > 0)
    		averagedTags.addDegreeValueSet(releaseTagDegrees, ARTIST_COMPUTED_TAGS_RELEASE_TAGS_WEIGHT);

    	setComputedTags(averagedTags.getNormalizedDegrees());
    }

    @Override
	public void computeStyles() {
    	setComputedStyles(computeStylesInternal());
    }

    public Vector<DegreeValue> computeStylesInternal() {
    	DegreeValueSetAverager averagedStyles = new DegreeValueSetAverager();

    	// discogs
    	DiscogsArtistProfile discogsProfile = (DiscogsArtistProfile)getMinedProfile(DATA_SOURCE_DISCOGS);
    	if (discogsProfile != null) {
    		Vector<DegreeValue> styles = discogsProfile.getStyleDegrees();
    		if (styles.size() > 0)
    			averagedStyles.addDegreeValueSet(styles, ARTIST_COMPUTED_STYLES_DISCOGS_WEIGHT);
    	}

    	// yahoo categories
    	YahooArtistProfile yahooProfile = (YahooArtistProfile)getMinedProfile(DATA_SOURCE_YAHOO);
    	if (yahooProfile != null) {
    		Vector<DegreeValue> styles = yahooProfile.getStyleDegrees();
    		if (styles.size() > 0)
    			averagedStyles.addDegreeValueSet(styles, ARTIST_COMPUTED_STYLES_YAHOO_CATEGORIES_WEIGHT);
    	}

    	// song styles
    	Vector<DegreeValue> songStyleDegrees = computeStyleDegreesFromSongs();
    	if (songStyleDegrees.size() > 0)
    		averagedStyles.addDegreeValueSet(songStyleDegrees, ARTIST_COMPUTED_STYLES_SONG_STYLES_WEIGHT);

    	// release styles
    	Vector<DegreeValue> releaseStyleDegrees = computeStyleDegreesFromReleases();
    	if (releaseStyleDegrees.size() > 0)
    		averagedStyles.addDegreeValueSet(releaseStyleDegrees, ARTIST_COMPUTED_STYLES_RELEASE_STYLES_WEIGHT);

    	return averagedStyles.getNormalizedDegrees();
    }


    @Override
	public void computeLinks() {

    	// discogs
    	DiscogsArtistProfile discogsProfile = (DiscogsArtistProfile)getMinedProfile(DATA_SOURCE_DISCOGS);
    	if (discogsProfile != null) {
    		for (Link link : discogsProfile.getLinks())
    			if (!links.contains(link))
    				links.add(link);
    	}

    	// lastfm
    	LastfmArtistProfile lastfmProfile = (LastfmArtistProfile)getMinedProfile(DATA_SOURCE_LASTFM);
    	if (lastfmProfile != null) {
    		Link lastfmLink = lastfmProfile.getLink();
    		if (lastfmLink != null)
    			if (!links.contains(lastfmLink))
    				links.add(lastfmLink);
    	}

    	// musicbrainz
    	MusicbrainzArtistProfile musicbrainzProfile = (MusicbrainzArtistProfile)getMinedProfile(DATA_SOURCE_MUSICBRAINZ);
    	if (musicbrainzProfile != null) {
    		for (Link link : musicbrainzProfile.getLinks())
    			if (!links.contains(link))
    				links.add(link);
    	}

    	// echonest
    	EchonestArtistProfile echonestProfile = (EchonestArtistProfile)getMinedProfile(DATA_SOURCE_ECHONEST);
    	if (echonestProfile != null) {
    		for (Link link : echonestProfile.getLinks())
    			if (!links.contains(link))
    				links.add(link);
    	}

    	// idiomag
    	IdiomagArtistProfile idiomagProfile = (IdiomagArtistProfile)getMinedProfile(DATA_SOURCE_IDIOMAG);
    	if (idiomagProfile != null) {
    		for (Link link : idiomagProfile.getLinks())
    			if (!links.contains(link))
    				links.add(link);
    	}

    }

    public void clearVideoLinks() {
    	videoLinks.clear();
    }

    @Override
	public void computeVideoLinks() {
    	computeVideoLinks(false);
    }

    public void computeVideoLinks(boolean clearExistingLinks) {
    	if (clearExistingLinks) {
    		videoLinks.clear();
    	}

    	// echonest
    	EchonestArtistProfile echonestProfile = (EchonestArtistProfile)getMinedProfile(DATA_SOURCE_ECHONEST);
    	if (echonestProfile != null) {
    		for (VideoLink link : echonestProfile.getVideoLinks())
    			if (!videoLinks.contains(link))
    				videoLinks.add(link);
    	}

    	// idiomag
    	IdiomagArtistProfile idiomagProfile = (IdiomagArtistProfile)getMinedProfile(DATA_SOURCE_IDIOMAG);
    	if (idiomagProfile != null) {
    		for (VideoLink link : idiomagProfile.getVideoLinks())
    			if (!videoLinks.contains(link))
    				videoLinks.add(link);
    	}
    }

    @Override
	public void computeImages() {

    	// user images
    	for (Image image : getUserImages()) {
    		if ((getValidImageCount() < RE3Properties.getInt("max_images_per_artist_profile")) && !images.contains(image))
    			images.add(image);
    	}

    	// discogs
    	DiscogsArtistProfile discogsProfile = (DiscogsArtistProfile)getMinedProfile(DATA_SOURCE_DISCOGS);
    	if (discogsProfile != null) {
    		for (Image image : discogsProfile.getImages())
    			if ((getValidImageCount() < RE3Properties.getInt("max_images_per_artist_profile")) && !images.contains(image))
    				images.add(image);
    	}

    	// lastfm
    	LastfmArtistProfile lastfmProfile = (LastfmArtistProfile)getMinedProfile(DATA_SOURCE_LASTFM);
    	if (lastfmProfile != null) {
    		Image lastfmImage = lastfmProfile.getImage();
    		if (lastfmImage != null)
    			if ((getValidImageCount() < RE3Properties.getInt("max_images_per_artist_profile")) && !images.contains(lastfmImage))
    				images.add(lastfmImage);
    	}

    	// echonest
    	EchonestArtistProfile echonestProfile = (EchonestArtistProfile)getMinedProfile(DATA_SOURCE_ECHONEST);
    	if (echonestProfile != null) {
    		for (Image echonestImage : echonestProfile.getREImages())
    			if ((getValidImageCount() < RE3Properties.getInt("max_images_per_artist_profile")) && !images.contains(echonestImage))
    				images.add(echonestImage);
    	}

    	// idiomag
    	IdiomagArtistProfile idiomagProfile = (IdiomagArtistProfile)getMinedProfile(DATA_SOURCE_IDIOMAG);
    	if (idiomagProfile != null) {
    		for (Image image : idiomagProfile.getImages())
    			if ((getValidImageCount() < RE3Properties.getInt("max_images_per_artist_profile")) && !images.contains(image))
    				images.add(image);
    	}

    	if ((images.size() > 0) && !hasThumbnail())
    		setThumbnailImageFilename(images.get(0).getImageFilename(), images.get(0).getDataSource());
    }

    @Override
	public void computeChanges(byte changedDataSource) {
    	if ((changedDataSource == DATA_SOURCE_UNKNOWN) || (changedDataSource == DATA_SOURCE_BILLBOARD) || (changedDataSource == DATA_SOURCE_DISCOGS) || (changedDataSource == DATA_SOURCE_ECHONEST) || (changedDataSource == DATA_SOURCE_LASTFM) || (changedDataSource == DATA_SOURCE_MUSICBRAINZ))
    		computePopularity();
    	if ((changedDataSource == DATA_SOURCE_UNKNOWN) || (changedDataSource == DATA_SOURCE_DISCOGS) || (changedDataSource == DATA_SOURCE_ECHONEST) || (changedDataSource == DATA_SOURCE_LASTFM) || (changedDataSource == DATA_SOURCE_MUSICBRAINZ))
    		computeScore();
    	if ((changedDataSource == DATA_SOURCE_UNKNOWN) || (changedDataSource == DATA_SOURCE_DISCOGS) || (changedDataSource == DATA_SOURCE_YAHOO) || (changedDataSource == DATA_SOURCE_MIXSHARE))
    		computeStyles();
    	if ((changedDataSource == DATA_SOURCE_UNKNOWN) || (changedDataSource == DATA_SOURCE_IDIOMAG) || (changedDataSource == DATA_SOURCE_LASTFM) || (changedDataSource == DATA_SOURCE_MUSICBRAINZ) || (changedDataSource == DATA_SOURCE_MIXSHARE) || (changedDataSource == DATA_SOURCE_ECHONEST))
    		computeTags();
    	if ((changedDataSource == DATA_SOURCE_UNKNOWN) || (changedDataSource == DATA_SOURCE_DISCOGS) || (changedDataSource == DATA_SOURCE_IDIOMAG) || (changedDataSource == DATA_SOURCE_ECHONEST) || (changedDataSource == DATA_SOURCE_LASTFM) || (changedDataSource == DATA_SOURCE_MUSICBRAINZ))
    		computeLinks();
    	if ((changedDataSource == DATA_SOURCE_UNKNOWN) || (changedDataSource == DATA_SOURCE_ECHONEST) || (changedDataSource == DATA_SOURCE_IDIOMAG))
    		computeVideoLinks();
    	if ((changedDataSource == DATA_SOURCE_UNKNOWN) || (changedDataSource == DATA_SOURCE_DISCOGS) || (changedDataSource == DATA_SOURCE_LASTFM) || (changedDataSource == DATA_SOURCE_IDIOMAG) || (changedDataSource == DATA_SOURCE_ECHONEST))
    		computeImages();
    }

    @Override
	public void update(SubmittedProfile submittedProfile, boolean overwrite) {
    	super.update(submittedProfile, overwrite);
    	SubmittedArtist submittedArtist = (SubmittedArtist)submittedProfile;
    }

    public boolean hasPublishedSongs() {
    	for (int songId : getSongIds()) {
    		SongRecord song = Database.getSongIndex().getSongRecord(songId);
    		if (song != null) {
            	Boolean published = (Boolean)song.getUserData(Database.getSongIndex().getUserDataType("Published"));
            	if (published == null)
            		published = false;
            	if (published)
            		return true;
    		}
    	}
    	return false;
    }

    @Override
	public void write(LineWriter writer) {
    	super.write(writer);
    	writer.writeLine("1"); // version
    	writer.writeLine(discogsArtistNameSource);
    	writer.writeLine(lastfmArtistNameSource);
    	writer.writeLine(mbIdSource);
    }

    @Override
	protected Record readRecord(LineReader lineReader) {
    	return new ArtistRecord(lineReader);
    }
}
