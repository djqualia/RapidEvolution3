package com.mixshare.rapid_evolution.data.profile.search.release;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.exceptions.AlreadyExistsException;
import com.mixshare.rapid_evolution.data.identifier.search.release.ReleaseIdentifier;
import com.mixshare.rapid_evolution.data.mined.discogs.release.DiscogsReleaseProfile;
import com.mixshare.rapid_evolution.data.mined.lastfm.LastfmCommonProfile;
import com.mixshare.rapid_evolution.data.mined.lastfm.release.LastfmReleaseProfile;
import com.mixshare.rapid_evolution.data.mined.musicbrainz.release.MusicbrainzReleaseProfile;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.profile.common.image.Image;
import com.mixshare.rapid_evolution.data.profile.common.link.Link;
import com.mixshare.rapid_evolution.data.profile.search.SongGroupProfile;
import com.mixshare.rapid_evolution.data.profile.search.artist.ArtistProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.data.record.search.artist.ArtistRecord;
import com.mixshare.rapid_evolution.data.record.search.label.LabelRecord;
import com.mixshare.rapid_evolution.data.record.search.release.ReleaseRecord;
import com.mixshare.rapid_evolution.data.record.search.song.ReleaseInstance;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.data.search.SearchResult;
import com.mixshare.rapid_evolution.data.submitted.SubmittedProfile;
import com.mixshare.rapid_evolution.data.submitted.search.release.SubmittedRelease;
import com.mixshare.rapid_evolution.data.util.DegreeValue;
import com.mixshare.rapid_evolution.data.util.DegreeValueSetAverager;
import com.mixshare.rapid_evolution.data.util.similarity.PearsonSimilarity;
import com.mixshare.rapid_evolution.ui.model.profile.SimilarProfilesModel;
import com.mixshare.rapid_evolution.util.StringUtil;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

public class ReleaseProfile extends SongGroupProfile {

    static private final long serialVersionUID = 0L;
    static private Logger log = Logger.getLogger(ReleaseProfile.class);

    static private float MIN_SONGS_FOR_SIMILARITY_CONFIDENCE = 4;

    static private float RELEASE_SIMILARITY_CONFIDENCE_THRESHOLD;

    static private float RELEASE_SIMILARITY_ARTISTS_WEIGHT;
    static private float RELEASE_SIMILARITY_STYLES_WEIGHT;
    static private float RELEASE_SIMILARITY_TAGS_WEIGHT;
    static private float RELEASE_SIMILARITY_LABELS_WEIGHT;
    static private float RELEASE_SIMILARITY_DISCOGS_WEIGHT;
    static private float RELEASE_SIMILARITY_BEAT_INTENSITY_WEIGHT;

    static private float RELEASE_COMPUTED_STYLES_DISCOGS_WEIGHT;
    static private float RELEASE_COMPUTED_STYLES_SONG_STYLES_WEIGHT;

    static private float RELEASE_COMPUTED_TAGS_LASTFM_WEIGHT;
    static private float RELEASE_COMPUTED_TAGS_MUSICBRAINZ_WEIGHT;
    static private float RELEASE_COMPUTED_TAGS_SONG_TAGS_WEIGHT;

    static private float RELEASE_SCORE_LASTFM_AVG_PLAYCOUNT_WEIGHT;
    static private float RELEASE_SCORE_LASTFM_AVG_PLAYCOUNT_NORMALIZE_VALUE;
    static private float RELEASE_SCORE_DISCOGS_AVG_RATING_WEIGHT;
    static private float RELEASE_SCORE_MUSICBRAINZ_AVG_RATING_WEIGHT;

    static private float RELEASE_POPULARITY_LASTFM_NUM_LISTENERS_WEIGHT;
    static private float RELEASE_POPULARITY_LASTFM_NUM_LISTENERS_NORMALIZE_VALUE;
    static private float RELEASE_POPULARITY_DISCOGS_NUM_RATERS_WEIGHT;
    static private float RELEASE_POPULARITY_DISCOGS_NUM_RATERS_NORMALIZE_VALUE;
    static private float RELEASE_POPULARITY_MUSICBRAINZ_NUM_RATERS_WEIGHT;
    static private float RELEASE_POPULARITY_MUSICBRAINZ_NUM_RATERS_NORMALIZE_VALUE;

    static {
    	loadProperties();
    }

    static public void loadProperties() {
    	RELEASE_SIMILARITY_CONFIDENCE_THRESHOLD = RE3Properties.getFloat("release_similarity_confidence_threshold");

        RELEASE_SIMILARITY_ARTISTS_WEIGHT = RE3Properties.getFloat("release_similarity_artists_weight");
        RELEASE_SIMILARITY_STYLES_WEIGHT = RE3Properties.getFloat("release_similarity_styles_weight");
        RELEASE_SIMILARITY_TAGS_WEIGHT = RE3Properties.getFloat("release_similarity_styles_weight");
        RELEASE_SIMILARITY_LABELS_WEIGHT = RE3Properties.getFloat("release_similarity_labels_weight");
        RELEASE_SIMILARITY_DISCOGS_WEIGHT = RE3Properties.getFloat("release_similarity_discogs_weight");
        RELEASE_SIMILARITY_BEAT_INTENSITY_WEIGHT = RE3Properties.getFloat("release_similarity_beat_intensity_weight");

        RELEASE_COMPUTED_STYLES_DISCOGS_WEIGHT = RE3Properties.getFloat("release_computed_styles_discogs_weight");
        RELEASE_COMPUTED_STYLES_SONG_STYLES_WEIGHT = RE3Properties.getFloat("release_computed_styles_song_styles_weight");

        RELEASE_COMPUTED_TAGS_LASTFM_WEIGHT = RE3Properties.getFloat("release_computed_tags_lastfm_weight");
        RELEASE_COMPUTED_TAGS_MUSICBRAINZ_WEIGHT = RE3Properties.getFloat("release_computed_tags_musicbrainz_weight");
        RELEASE_COMPUTED_TAGS_SONG_TAGS_WEIGHT = RE3Properties.getFloat("release_computed_tags_song_tags_weight");

        RELEASE_SCORE_LASTFM_AVG_PLAYCOUNT_WEIGHT = RE3Properties.getFloat("release_score_lastfm_avg_playcount_weight");
        RELEASE_SCORE_LASTFM_AVG_PLAYCOUNT_NORMALIZE_VALUE = RE3Properties.getFloat("release_score_lastfm_avg_playcount_normalize_value");
        RELEASE_SCORE_DISCOGS_AVG_RATING_WEIGHT = RE3Properties.getFloat("release_score_discogs_avg_rating_weight");
        RELEASE_SCORE_MUSICBRAINZ_AVG_RATING_WEIGHT = RE3Properties.getFloat("release_score_musicbrainz_avg_rating_weight");

        RELEASE_POPULARITY_LASTFM_NUM_LISTENERS_WEIGHT = RE3Properties.getFloat("release_popularity_lastfm_num_listeners_weight");
        RELEASE_POPULARITY_LASTFM_NUM_LISTENERS_NORMALIZE_VALUE = RE3Properties.getFloat("release_popularity_lastfm_num_listeners_normalize_value");
        RELEASE_POPULARITY_DISCOGS_NUM_RATERS_WEIGHT = RE3Properties.getFloat("release_popularity_discogs_num_raters_weight");
        RELEASE_POPULARITY_DISCOGS_NUM_RATERS_NORMALIZE_VALUE = RE3Properties.getFloat("release_popularity_discogs_num_raters_normalize_value");
        RELEASE_POPULARITY_MUSICBRAINZ_NUM_RATERS_WEIGHT = RE3Properties.getFloat("release_popularity_musicbrainz_num_raters_weight");
        RELEASE_POPULARITY_MUSICBRAINZ_NUM_RATERS_NORMALIZE_VALUE = RE3Properties.getFloat("release_popularity_musicbrainz_num_raters_normalize_value");
    }

    //////////////////
    // CONSTRUCTORS //
    //////////////////

    public ReleaseProfile() { };
    public ReleaseProfile(ReleaseIdentifier releaseId, int fileId) {
    	record = new ReleaseRecord(releaseId, fileId);
    }
    public ReleaseProfile(LineReader lineReader) {
    	super(lineReader);
    	int version = Integer.parseInt(lineReader.getNextLine());
    	originalYearReleasedSource = Byte.parseByte(lineReader.getNextLine());
    }

    ////////////
    // FIELDS //
    ////////////

    protected byte originalYearReleasedSource = DATA_SOURCE_UNKNOWN;

    transient private Map<Integer, Map<Integer, Float>> artistSimilarityMap;

    static {
    	try{
    		// in order for the XMLEncoder to skip transient variables, this is needed
    		BeanInfo info = Introspector.getBeanInfo(ReleaseProfile.class);
    		PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
    		for (int i = 0; i < propertyDescriptors.length; ++i) {
    			PropertyDescriptor pd = propertyDescriptors[i];
    			if (pd.getName().equals("artistProfiles") || pd.getName().equals("releaseTitle") || pd.getName().equals("artistNames") || pd.getName().equals("isCompilationRelease") ||
    					pd.getName().equals("labelName") || pd.getName().equals("labelNames") || pd.getName().equals("initialLabelNames") || pd.getName().equals("originalYearReleased")) {
    				pd.setValue("transient", Boolean.TRUE);
    			}
    		}
    	} catch (IntrospectionException e) {
    		log.error("static(): error", e);
    	}
    }

    /////////////
    // GETTERS //
    /////////////

    public ReleaseRecord getReleaseRecord() { return (ReleaseRecord)record; }
    public ReleaseIdentifier getReleaseIdentifier() { return getReleaseRecord().getReleaseIdentifier(); }

    public Vector<ArtistRecord> getArtists() { return getReleaseRecord().getArtists(); }
    public Vector<String> getArtistNames() { return getReleaseRecord().getArtistNames(); }
	public String getArtistsDescription() { return getReleaseRecord().getArtistsDescription(); }
	public String getLastfmArtistsDescription() { return getReleaseRecord().getLastfmArtistsDescription(); }
	public String getDiscogsArtistsDescription() { return getReleaseRecord().getDiscogsArtistsDescription(); }

	public String getReleaseTitle() { return getReleaseRecord().getReleaseTitle(); }

	public boolean isCompilationRelease() { return getReleaseRecord().isCompilationRelease(); }

	public int getNumLabels() { return getReleaseRecord().getNumLabels(); }
	public Vector<String> getSourceLabelNames() { return getReleaseRecord().getSourceLabelNames(); }
	public String getLabelsDescription() { return getReleaseRecord().getLabelsDescription(); }
	public Vector<LabelRecord> getLabels() { return getReleaseRecord().getLabels(); }
	public Vector<String> getLabelNames() { return getReleaseRecord().getLabelNames(); }

    public String getOriginalYearReleasedAsString() { return getReleaseRecord().getOriginalYearReleasedAsString(); }
    public int getOriginalYearReleased() { return getReleaseRecord().getOriginalYearReleased(); }

	public Map<Integer, Map<Integer, Float>> getArtistSimilarityMap() { return artistSimilarityMap; }

    // for serialization
	public byte getOriginalYearReleasedSource() { return originalYearReleasedSource; }

    /////////////
    // SETTERS //
    /////////////

	public void setReleaseTitle(String releaseTitle) throws AlreadyExistsException { setReleaseTitle(releaseTitle, false); }
    public void setReleaseTitle(String releaseTitle, boolean autoMerge) throws AlreadyExistsException {
    	if (!getReleaseTitle().equals(releaseTitle)) {
    		ReleaseIdentifier oldReleaseId = getReleaseIdentifier();
    		ReleaseIdentifier newReleaseId = new ReleaseIdentifier(oldReleaseId.getArtistIds(), releaseTitle);
    		boolean unlocked = false;
    		try {
    			getRecord().getWriteLockSem().startRead("setReleaseTitle");
				updateIdentifier(newReleaseId, oldReleaseId);
				getRecord().getWriteLockSem().endRead();
				unlocked = true;
				for (int songId : getSongIds()) {
					SongRecord songRecord = Database.getSongIndex().getSongRecord(songId);
					if (songRecord != null) {
						songRecord.clearCachedSongDescription();
						songRecord.update();
					}
				}
    		} catch (InterruptedException e) {
    		} catch (AlreadyExistsException ae) {
    			if (autoMerge) {
    				ReleaseProfile existingRelease = Database.getReleaseIndex().getReleaseProfile(newReleaseId);
    				Database.mergeProfiles(existingRelease, this);
    			} else {
    				throw ae;
    			}
    		} finally {
    			if (!unlocked)
    				getRecord().getWriteLockSem().endRead();
    		}
    	}
    }

    public void setArtistNames(Vector<String> artistNames) throws AlreadyExistsException { setArtistNames(artistNames, true); }
    public void setArtistNames(Vector<String> artistNames, boolean setSongArtists) throws AlreadyExistsException {
    	ReleaseIdentifier oldReleaseId = getReleaseIdentifier();
    	ReleaseIdentifier newReleaseId = new ReleaseIdentifier(artistNames, oldReleaseId.getReleaseTitle());
    	if (!oldReleaseId.equals(newReleaseId)) {
    		boolean unlocked = false;
    		try {
        		getRecord().getWriteLockSem().startRead("setArtistNames");
	    		updateIdentifier(newReleaseId, oldReleaseId);
	    		getRecord().getWriteLockSem().endRead();
	    		unlocked = true;
	    		if (setSongArtists && !isCompilationRelease()) {
		    		for (Integer songId : getSongIds()) {
		    			SongProfile songProfile = (SongProfile)Database.getSongIndex().getProfile(songId);
		    			if (songProfile != null) {
		        			String track = songProfile.getTrackForReleaseId(getUniqueId());
		    				songProfile.setArtistNames(artistNames); // will cause the song to lose the release association
		        			addSong(songProfile.getSongRecord(), track);
		    			}
		    		}
		    		save();
	    		}
	    		getRecord().setRelationalItemsChanged(true);
    		} catch (InterruptedException e) {
    		} finally {
    			if (!unlocked)
    				getRecord().getWriteLockSem().endRead();
    		}
    	}
    }

    public void setIsCompilationRelease(boolean isCompilationRelease) throws AlreadyExistsException {
    	if (isCompilationRelease) {
    		setArtistNames(new Vector<String>(), false);
    	} else {
    		Vector<String> artistNames = new Vector<String>();
    		for (int songId : getSongIds()) {
    			SongRecord song = Database.getSongIndex().getSongRecord(songId);
    			if (song != null) {
    				for (ArtistRecord artist : song.getArtists()) {
    					if (!StringUtil.containsIgnoreCase(artistNames, artist.getArtistName()))
    						artistNames.add(artist.getArtistName());
    				}
    			}
    		}
    		setArtistNames(artistNames, false);
    	}
    }

    public void addSong(SongRecord song, String track, boolean primaryReleaseInstance) {
    	addSong(song.getUniqueId());
		song.addReleaseInstance(new ReleaseInstance(getUniqueId(), track), primaryReleaseInstance);
    	song.update();
    }

    public void addSong(SongRecord song, String track) {
    	addSong(song, track, false);
    }

    public void addLabelName(String labelName) {
    	Vector<String> existingLabelNames = getReleaseRecord().getLabelNames();
    	if (!StringUtil.containsIgnoreCase(existingLabelNames, labelName)) {
    		existingLabelNames.add(labelName);
    		setLabelNames(existingLabelNames);
    	}
    }
    public void addLabelNames(Vector<String> labelNames) {
    	Vector<String> existingLabelNames = getReleaseRecord().getLabelNames();
    	for (String labelName : labelNames) {
    		if (!StringUtil.containsIgnoreCase(existingLabelNames, labelName))
	    		existingLabelNames.add(labelName);
    	}
		setLabelNames(existingLabelNames);
    }
    public void removeLabelName(String labelName) {
    	Vector<String> existingLabelNames = getReleaseRecord().getLabelNames();
    	if (existingLabelNames.contains(labelName)) {
    		existingLabelNames.remove(labelName);
    		setLabelNames(existingLabelNames);
    	}
    }
	public void setLabelNames(Vector<String> labelNames) { getReleaseRecord().setLabelNames(labelNames); }
	public void setInitialLabelNames(Vector<String> labelNames) { getReleaseRecord().setLabelNames(labelNames, false, false); }

	public void setOriginalYearReleased(short originalYearReleased, byte originalYearReleasedSource) {
		getReleaseRecord().setOriginalYearReleased(originalYearReleased);
		this.originalYearReleasedSource = originalYearReleasedSource;
	}

    public void setArtistSimilarityMap(Map<Integer, Map<Integer, Float>> artistSimilarityMap) { this.artistSimilarityMap = artistSimilarityMap; }

	// for serialization
	public void setOriginalYearReleasedSource(byte originalYearReleasedSource) { this.originalYearReleasedSource = originalYearReleasedSource; }

    /////////////
    // METHODS //
    /////////////

	@Override
	public Map<Record, Object> mergeWith(Profile profile) {
		Map<Record, Object> relatedRecords = super.mergeWith(profile);
		ReleaseProfile releaseProfile = (ReleaseProfile)profile;
    	if (!isCompilationRelease() && releaseProfile.isCompilationRelease()) {
    		try { setIsCompilationRelease(true); } catch (AlreadyExistsException aee) { }
    	}
		return relatedRecords;
	}

	@Override
	public void clearSimilaritySearchTransients() {
		artistSimilarityMap = null;
	}

    @Override
	public float getSimilarity(SearchRecord record) {
    	if (record == null)
    		return 0.0f;
    	float numerator = 0.0f;
        float denominator = 0.0f;
        float confidence = 1.0f;

    	ReleaseRecord releaseRecord = (ReleaseRecord)record;

        if (getNumSongs() < MIN_SONGS_FOR_SIMILARITY_CONFIDENCE)
            confidence = confidence * getNumSongs() / MIN_SONGS_FOR_SIMILARITY_CONFIDENCE;
        if (releaseRecord.getNumSongs() < MIN_SONGS_FOR_SIMILARITY_CONFIDENCE)
            confidence = confidence * releaseRecord.getNumSongs() / MIN_SONGS_FOR_SIMILARITY_CONFIDENCE;

        // STYLE SIMILARITY
        if ((RELEASE_SIMILARITY_STYLES_WEIGHT > 0.0f) && (getNumActualStyles() > 0) && (record.getNumActualStyles() > 0)) {
        	float styleSimilarity = getReleaseRecord().computeStyleSimilarity(record);
        	if (!Float.isNaN(styleSimilarity))
        		numerator += styleSimilarity * RELEASE_SIMILARITY_STYLES_WEIGHT * confidence;
        	denominator += RELEASE_SIMILARITY_STYLES_WEIGHT * confidence;
        }

        // TAG SIMILARITY
        if ((RELEASE_SIMILARITY_TAGS_WEIGHT > 0.0f) && (getNumActualTags() > 0) && (record.getNumActualTags() > 0)) {
        	float tagSimilarity = getReleaseRecord().computeTagSimilarity(record);
        	if (!Float.isNaN(tagSimilarity))
        		numerator += tagSimilarity * RELEASE_SIMILARITY_TAGS_WEIGHT;
        	denominator += RELEASE_SIMILARITY_TAGS_WEIGHT;
        }

        // ARTIST SIMILARITY
        if (RELEASE_SIMILARITY_ARTISTS_WEIGHT > 0.0f) {
        	if (artistSimilarityMap == null) {
        		artistSimilarityMap = computeArtistSimilarityMap(this);
        	}
        	int[] otherArtistIds = releaseRecord.getArtistIds();
        	float avgSimilarity = 0.0f;
        	int numComparisons = 0;
        	for (int artistId : artistSimilarityMap.keySet()) {
        		if (otherArtistIds != null) {
	        		for (int otherArtistId : otherArtistIds) {
        				avgSimilarity += getArtistSimilarity(artistId, otherArtistId);
	        			++numComparisons;
	        		}
        		}
        	}
        	if (numComparisons > 0) {
        		avgSimilarity /= numComparisons;
        		numerator += avgSimilarity * RELEASE_SIMILARITY_ARTISTS_WEIGHT;
        	}
        	denominator += RELEASE_SIMILARITY_ARTISTS_WEIGHT;
        }

        // LABEL SIMILARITY
        if ((RELEASE_SIMILARITY_LABELS_WEIGHT > 0.0f) && (getNumLabels() > 0) && (releaseRecord.getNumLabels() > 0)) {
        	float labelSimilarity = PearsonSimilarity.computeSimilarity(getReleaseRecord().getLabelIds(), releaseRecord.getLabelIds());
        	if (!Float.isNaN(labelSimilarity))
        		numerator += labelSimilarity * RELEASE_SIMILARITY_LABELS_WEIGHT;
        	denominator += RELEASE_SIMILARITY_LABELS_WEIGHT;
        }

        // DISCOGS RECOMMENDATIONS
        if (RELEASE_SIMILARITY_DISCOGS_WEIGHT > 0.0f) {
        	DiscogsReleaseProfile discogsProfile = (DiscogsReleaseProfile)getMinedProfile(DATA_SOURCE_DISCOGS);
        	if (discogsProfile != null) {
        		numerator += discogsProfile.getSimilarityWith(releaseRecord.getDiscogsArtistsDescription(), releaseRecord.getReleaseTitle()) * RELEASE_SIMILARITY_DISCOGS_WEIGHT;
        		denominator += RELEASE_SIMILARITY_DISCOGS_WEIGHT;
        	}
        }

        // BEAT INTENSITY SIMILARITY
        if ((RELEASE_SIMILARITY_BEAT_INTENSITY_WEIGHT > 0.0f) && getAvgBeatIntensity().isValid() && releaseRecord.getAvgBeatIntensity().isValid()) {
        	float beatIntensitySimilarity = getAvgBeatIntensity().getSimilarityWith(releaseRecord.getAvgBeatIntensity());
        	float beatIntensityVarianceSimilarity = getBeatIntensityVariance().getSimilarityWith(releaseRecord.getBeatIntensityVariance());
        	numerator += beatIntensitySimilarity * beatIntensityVarianceSimilarity * RELEASE_SIMILARITY_BEAT_INTENSITY_WEIGHT;
        	denominator += RELEASE_SIMILARITY_BEAT_INTENSITY_WEIGHT;
        }

        if (denominator > 0.0f) {
        	float scale = 1.0f;
        	if (denominator < RELEASE_SIMILARITY_CONFIDENCE_THRESHOLD)
        		scale = denominator / RELEASE_SIMILARITY_CONFIDENCE_THRESHOLD;
            return numerator * scale / denominator;
        }
        return 0.0f;
    }

    private float getArtistSimilarity(int artistId, int otherArtistId) {
    	Map<Integer, Float> subMap = artistSimilarityMap.get(artistId);
    	if (subMap != null) {
    		Float result = subMap.get(otherArtistId);
    		if (result != null)
    			return result;
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
    		float playCountNormalized = avgPlayersPerListener / RELEASE_SCORE_LASTFM_AVG_PLAYCOUNT_NORMALIZE_VALUE;
    		if (playCountNormalized > 1.0f)
    			playCountNormalized = 1.0f;
    		score += playCountNormalized * RELEASE_SCORE_LASTFM_AVG_PLAYCOUNT_WEIGHT;
    		totalWeight += RELEASE_SCORE_LASTFM_AVG_PLAYCOUNT_WEIGHT;
    	}

    	// discogs avg rating
    	DiscogsReleaseProfile discogsProfile = (DiscogsReleaseProfile)getMinedProfile(DATA_SOURCE_DISCOGS);
    	if (discogsProfile != null) {
    		float avgRating = discogsProfile.getAvgRating(); // 1 to 5 stars
    		if ((avgRating != 0.0f) && !Float.isNaN(avgRating)) {
    			float avgRatingNormalized = (avgRating - 1.0f) / 4.0f;
    			score += avgRatingNormalized * RELEASE_SCORE_DISCOGS_AVG_RATING_WEIGHT;
    			totalWeight += RELEASE_SCORE_DISCOGS_AVG_RATING_WEIGHT;
    		}
    	}

    	// musicbrainz avg rating
    	MusicbrainzReleaseProfile musicbrainzProfile = (MusicbrainzReleaseProfile)getMinedProfile(DATA_SOURCE_MUSICBRAINZ);
    	if (musicbrainzProfile != null) {
    		float avgRating = musicbrainzProfile.getAvgRating(); // 1 to 5 stars
    		if ((avgRating != 0.0f) && !Float.isNaN(avgRating)) {
    			float avgRatingNormalized = (avgRating - 1.0f) / 4.0f;
    			score += avgRatingNormalized * RELEASE_SCORE_MUSICBRAINZ_AVG_RATING_WEIGHT;
    			totalWeight += RELEASE_SCORE_MUSICBRAINZ_AVG_RATING_WEIGHT;
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
    		float normalizedNumListeners = numListeners / RELEASE_POPULARITY_LASTFM_NUM_LISTENERS_NORMALIZE_VALUE;
    		if (normalizedNumListeners > 1.0f)
    			normalizedNumListeners = 1.0f;
    		popularity += normalizedNumListeners * RELEASE_POPULARITY_LASTFM_NUM_LISTENERS_WEIGHT;
    		totalWeight += RELEASE_POPULARITY_LASTFM_NUM_LISTENERS_WEIGHT;
    	}

    	// discogs num raters
    	DiscogsReleaseProfile discogsProfile = (DiscogsReleaseProfile)getMinedProfile(DATA_SOURCE_DISCOGS);
    	if (discogsProfile != null) {
    		float numUniqueRaters = discogsProfile.getNumUniqueRaters(); // unbounded
    		float numUniqueRatersNormalized = numUniqueRaters / RELEASE_POPULARITY_DISCOGS_NUM_RATERS_NORMALIZE_VALUE;
    		if (numUniqueRatersNormalized > 1.0f)
    			numUniqueRatersNormalized = 1.0f;
    		popularity += numUniqueRatersNormalized * RELEASE_POPULARITY_DISCOGS_NUM_RATERS_WEIGHT;
    		totalWeight += RELEASE_POPULARITY_DISCOGS_NUM_RATERS_WEIGHT;
    	}

    	// musicbrainz num raters
    	MusicbrainzReleaseProfile musicbrainzProfile = (MusicbrainzReleaseProfile)getMinedProfile(DATA_SOURCE_MUSICBRAINZ);
    	if (musicbrainzProfile != null) {
    		float numRaters = musicbrainzProfile.getNumRaters(); // unbounded
    		float numRatersNormalized = numRaters / RELEASE_POPULARITY_MUSICBRAINZ_NUM_RATERS_NORMALIZE_VALUE;
    		if (numRatersNormalized > 1.0f)
    			numRatersNormalized = 1.0f;
    		popularity += numRatersNormalized * RELEASE_POPULARITY_MUSICBRAINZ_NUM_RATERS_WEIGHT;
    		totalWeight += RELEASE_POPULARITY_MUSICBRAINZ_NUM_RATERS_WEIGHT;
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
    	LastfmReleaseProfile lastfmProfile = (LastfmReleaseProfile)getMinedProfile(DATA_SOURCE_LASTFM);
    	if (lastfmProfile != null) {
    		Vector<DegreeValue> tags = lastfmProfile.getTopTags();
    		if (tags.size() > 0)
    			averagedTags.addDegreeValueSet(tags, RELEASE_COMPUTED_TAGS_LASTFM_WEIGHT);
    	}

    	// musicbrainz
    	MusicbrainzReleaseProfile musicbrainzProfile = (MusicbrainzReleaseProfile)getMinedProfile(DATA_SOURCE_MUSICBRAINZ);
    	if (musicbrainzProfile != null) {
    		Vector<DegreeValue> tags = musicbrainzProfile.getTagDegrees();
    		if (tags.size() > 0)
    			averagedTags.addDegreeValueSet(tags, RELEASE_COMPUTED_TAGS_MUSICBRAINZ_WEIGHT);
    	}

    	// song tags
    	Vector<DegreeValue> songTagDegrees = computeTagDegreesFromSongs();
    	if (songTagDegrees.size() > 0)
    		averagedTags.addDegreeValueSet(songTagDegrees, RELEASE_COMPUTED_TAGS_SONG_TAGS_WEIGHT);

    	setComputedTags(averagedTags.getNormalizedDegrees());
    }

    @Override
	public void computeStyles() {
    	DegreeValueSetAverager averagedStyles = new DegreeValueSetAverager();

    	// discogs
    	DiscogsReleaseProfile discogsProfile = (DiscogsReleaseProfile)getMinedProfile(DATA_SOURCE_DISCOGS);
    	if (discogsProfile != null) {
    		Vector<DegreeValue> styles = discogsProfile.getStyleDegrees();
    		if (styles.size() > 0)
    			averagedStyles.addDegreeValueSet(styles, RELEASE_COMPUTED_STYLES_DISCOGS_WEIGHT);
    	}

    	// song styles
    	Vector<DegreeValue> songStyleDegrees = computeStyleDegreesFromSongs();
    	if (songStyleDegrees.size() > 0)
    		averagedStyles.addDegreeValueSet(songStyleDegrees, RELEASE_COMPUTED_STYLES_SONG_STYLES_WEIGHT);

    	setComputedStyles(averagedStyles.getNormalizedDegrees());
    }

    @Override
	public void computeLinks() {

    	// discogs
    	DiscogsReleaseProfile discogsProfile = (DiscogsReleaseProfile)getMinedProfile(DATA_SOURCE_DISCOGS);
    	if (discogsProfile != null) {
    		for (Link link : discogsProfile.getLinks())
    			if (!links.contains(link))
    				links.add(link);
    	}

    	// lastfm
    	LastfmReleaseProfile lastfmProfile = (LastfmReleaseProfile)getMinedProfile(DATA_SOURCE_LASTFM);
    	if (lastfmProfile != null) {
    		Link lastfmLink = lastfmProfile.getLink();
    		if (lastfmLink != null)
    			if (!links.contains(lastfmLink))
    				links.add(lastfmLink);
    	}

    	// musicbrainz
    	MusicbrainzReleaseProfile musicbrainzProfile = (MusicbrainzReleaseProfile)getMinedProfile(DATA_SOURCE_MUSICBRAINZ);
    	if (musicbrainzProfile != null) {
    		for (Link link : musicbrainzProfile.getLinks())
    			if (!links.contains(link))
    				links.add(link);
    	}
    }

    @Override
	public void computeVideoLinks() { }

    @Override
	public void computeImages() {
    	// user images
    	for (Image image : getUserImages())
    		if ((getValidImageCount() < RE3Properties.getInt("max_images_per_release_profile")) && !images.contains(image))
    			images.add(image);

    	// discogs
    	DiscogsReleaseProfile discogsProfile = (DiscogsReleaseProfile)getMinedProfile(DATA_SOURCE_DISCOGS);
    	if (discogsProfile != null) {
    		for (Image image : discogsProfile.getImages())
    			if ((getValidImageCount() < RE3Properties.getInt("max_images_per_release_profile")) && !images.contains(image))
    				images.add(image);
    	}

    	// lastfm
    	LastfmReleaseProfile lastfmProfile = (LastfmReleaseProfile)getMinedProfile(DATA_SOURCE_LASTFM);
    	if (lastfmProfile != null) {
    		Image lastfmImage = lastfmProfile.getImage();
    		if (lastfmImage != null)
    			if ((getValidImageCount() < RE3Properties.getInt("max_images_per_release_profile")) && !images.contains(lastfmImage))
    				images.add(lastfmImage);
    	}

    	if ((images.size() > 0) && !hasThumbnail())
    		setThumbnailImageFilename(images.get(0).getImageFilename(), images.get(0).getDataSource());
    }

    public void computeOriginalYear() {
    	if ((getOriginalYearReleased() != 0) && (this.originalYearReleasedSource == DATA_SOURCE_USER))
    		return;

    	// musicbrainz
    	MusicbrainzReleaseProfile musicbrainzProfile = (MusicbrainzReleaseProfile)getMinedProfile(DATA_SOURCE_MUSICBRAINZ);
    	if (musicbrainzProfile != null) {
    		if (musicbrainzProfile.getOriginalYearReleasedShort() != 0) {
    			setOriginalYearReleased(musicbrainzProfile.getOriginalYearReleasedShort(), DATA_SOURCE_MUSICBRAINZ);
    			return;
    		}
    	}

    	// discogs release
    	DiscogsReleaseProfile discogsProfile = (DiscogsReleaseProfile)getMinedProfile(DATA_SOURCE_DISCOGS);
    	if (discogsProfile != null) {
    		if (discogsProfile.getOriginalYearShort() != 0) {
    			setOriginalYearReleased(discogsProfile.getOriginalYearShort(), DATA_SOURCE_DISCOGS);
    			return;
    		}
    	}

    	// lastfm
    	LastfmReleaseProfile lastfmProfile = (LastfmReleaseProfile)getMinedProfile(DATA_SOURCE_LASTFM);
    	if (lastfmProfile != null) {
    		if (lastfmProfile.getOriginalYearReleased() != 0) {
    			setOriginalYearReleased(lastfmProfile.getOriginalYearReleased(), DATA_SOURCE_LASTFM);
    			return;
    		}
    	}

    }


    @Override
	public void computeChanges(byte changedDataSource) {
    	if ((changedDataSource == DATA_SOURCE_UNKNOWN) || (changedDataSource == DATA_SOURCE_DISCOGS))
    		computeStyles();
    	if ((changedDataSource == DATA_SOURCE_UNKNOWN) || (changedDataSource == DATA_SOURCE_DISCOGS) || (changedDataSource == DATA_SOURCE_MUSICBRAINZ) || (changedDataSource == DATA_SOURCE_LASTFM))
    		computeLinks();
    	if ((changedDataSource == DATA_SOURCE_UNKNOWN) || (changedDataSource == DATA_SOURCE_DISCOGS) || (changedDataSource == DATA_SOURCE_MUSICBRAINZ) || (changedDataSource == DATA_SOURCE_LASTFM))
    		computePopularity();
    	if ((changedDataSource == DATA_SOURCE_UNKNOWN) || (changedDataSource == DATA_SOURCE_DISCOGS) || (changedDataSource == DATA_SOURCE_MUSICBRAINZ) || (changedDataSource == DATA_SOURCE_LASTFM))
    		computeScore();
    	if ((changedDataSource == DATA_SOURCE_UNKNOWN) || (changedDataSource == DATA_SOURCE_MUSICBRAINZ) || (changedDataSource == DATA_SOURCE_LASTFM))
    		computeTags();
    	if ((changedDataSource == DATA_SOURCE_UNKNOWN) || (changedDataSource == DATA_SOURCE_DISCOGS) || (changedDataSource == DATA_SOURCE_LASTFM))
    		computeImages();
    	if ((changedDataSource == DATA_SOURCE_UNKNOWN) || (changedDataSource == DATA_SOURCE_MUSICBRAINZ) || (changedDataSource == DATA_SOURCE_LASTFM) || (changedDataSource == DATA_SOURCE_DISCOGS))
    		computeOriginalYear();
    }

    @Override
	public void update(SubmittedProfile submittedProfile, boolean overwrite) {
    	super.update(submittedProfile, overwrite);
    	SubmittedRelease submittedRelease = (SubmittedRelease)submittedProfile;
    	if ((getOriginalYearReleased() == 0) || overwrite)
    		setOriginalYearReleased(submittedRelease.getOriginalYearReleased(), submittedRelease.getOriginalYearReleasedSource());
    	if (submittedRelease.getLabelNames() != null)
    		addLabelNames(submittedRelease.getLabelNames());
    }

    static public Map<Integer, Map<Integer, Float>> computeArtistSimilarityMap(ReleaseProfile release) {
    	Map<Integer, Map<Integer, Float>> result = new HashMap<Integer, Map<Integer, Float>>();
		Vector<ArtistRecord> artistRecords = release.getArtists();
		for (ArtistRecord artistRecord : artistRecords) {
			ArtistProfile artistProfile = Database.getArtistIndex().getArtistProfile(artistRecord.getUniqueId());
			if (artistProfile != null) {
		    	SimilarProfilesModel model = new SimilarProfilesModel(artistProfile, Database.getArtistIndex());
		    	Vector<SearchResult> similarRecords = model.getSimilarRecords();
		    	if (similarRecords.size() > 0) {
			    	Map<Integer, Float> similarArtistMap = new HashMap<Integer, Float>(similarRecords.size());
			    	for (SearchResult similarRecord : similarRecords)
			    		similarArtistMap.put(similarRecord.getRecord().getUniqueId(), similarRecord.getScore());
			    	result.put(artistProfile.getUniqueId(), similarArtistMap);
		    	}
			}
		}
		return result;
    }

    @Override
	public void write(LineWriter writer) {
    	super.write(writer);
    	writer.writeLine("1"); // version
    	writer.writeLine(originalYearReleasedSource);
    }

    @Override
	protected Record readRecord(LineReader lineReader) {
    	return new ReleaseRecord(lineReader);
    }


}
