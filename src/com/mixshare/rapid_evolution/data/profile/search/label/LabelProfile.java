package com.mixshare.rapid_evolution.data.profile.search.label;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.net.URLEncoder;
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
import com.mixshare.rapid_evolution.data.mined.discogs.label.DiscogsLabelProfile;
import com.mixshare.rapid_evolution.data.mined.musicbrainz.label.MusicbrainzLabelProfile;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.profile.common.image.Image;
import com.mixshare.rapid_evolution.data.profile.common.link.Link;
import com.mixshare.rapid_evolution.data.profile.search.ReleaseGroupProfile;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.data.record.search.artist.ArtistRecord;
import com.mixshare.rapid_evolution.data.record.search.label.LabelRecord;
import com.mixshare.rapid_evolution.data.record.search.release.ReleaseRecord;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.data.submitted.SubmittedProfile;
import com.mixshare.rapid_evolution.data.submitted.search.label.SubmittedLabel;
import com.mixshare.rapid_evolution.data.util.DegreeValue;
import com.mixshare.rapid_evolution.data.util.DegreeValueSetAverager;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.mixshare.rapid_evolution.workflow.TaskManager;
import com.mixshare.rapid_evolution.workflow.mining.discogs.label.DiscogsLabelFetchResultTrigger;
import com.mixshare.rapid_evolution.workflow.mining.discogs.label.DiscogsLabelFetchTask;

public class LabelProfile extends ReleaseGroupProfile {

    static private final long serialVersionUID = 0L;
    static private Logger log = Logger.getLogger(LabelProfile.class);

    static private float MIN_SONGS_FOR_SIMILARITY_CONFIDENCE = 10;

    static private float LABEL_SIMILARITY_CONFIDENCE_THRESHOLD;

    static private float LABEL_SIMILARITY_STYLES_WEIGHT;
    static private float LABEL_SIMILARITY_TAGS_WEIGHT;
    static private float LABEL_SIMILARITY_ARTISTS_WEIGHT;
    static private float LABEL_SIMILARITY_BEAT_INTENSITY_WEIGHT;

    static private float LABEL_COMPUTED_STYLES_DISCOGS_WEIGHT;
    static private float LABEL_COMPUTED_STYLES_RELEASE_STYLES_WEIGHT;
    static private float LABEL_COMPUTED_STYLES_SONG_STYLES_WEIGHT;

    static private float LABEL_COMPUTED_TAGS_MUSICBRAINZ_WEIGHT;
    static private float LABEL_COMPUTED_TAGS_RELEASE_TAGS_WEIGHT;
    static private float LABEL_COMPUTED_TAGS_SONG_TAGS_WEIGHT;

    static private float LABEL_SCORE_DISCOGS_AVG_RATING_WEIGHT;
    static private float LABEL_SCORE_MUSICBRAINZ_AVG_RATING_WEIGHT;

    static private float LABEL_POPULARITY_DISCOGS_NUM_RATERS_WEIGHT;
    static private float LABEL_POPULARITY_DISCOGS_NUM_RATERS_NORMALIZE_VALUE;
    static private float LABEL_POPULARITY_MUSICBRAINZ_NUM_RATERS_WEIGHT;
    static private float LABEL_POPULARITY_MUSICBRAINZ_NUM_RATERS_NORMALIZE_VALUE;

    static {
    	loadProperties();
    }

    static public void loadProperties() {
    	LABEL_SIMILARITY_CONFIDENCE_THRESHOLD = RE3Properties.getFloat("label_similarity_confidence_threshold");

        LABEL_SIMILARITY_STYLES_WEIGHT = RE3Properties.getFloat("label_similarity_styles_weight");
        LABEL_SIMILARITY_TAGS_WEIGHT = RE3Properties.getFloat("label_similarity_tags_weight");
        LABEL_SIMILARITY_ARTISTS_WEIGHT = RE3Properties.getFloat("label_similarity_artists_weight");
        LABEL_SIMILARITY_BEAT_INTENSITY_WEIGHT = RE3Properties.getFloat("label_similarity_beat_intensity_weight");

        LABEL_COMPUTED_STYLES_DISCOGS_WEIGHT = RE3Properties.getFloat("label_computed_styles_discogs_weight");
        LABEL_COMPUTED_STYLES_RELEASE_STYLES_WEIGHT = RE3Properties.getFloat("label_computed_styles_release_styles_weight");
        LABEL_COMPUTED_STYLES_SONG_STYLES_WEIGHT = RE3Properties.getFloat("label_computed_styles_song_styles_weight");

        LABEL_COMPUTED_TAGS_MUSICBRAINZ_WEIGHT = RE3Properties.getFloat("label_computed_tags_musicbrainz_weight");
        LABEL_COMPUTED_TAGS_RELEASE_TAGS_WEIGHT = RE3Properties.getFloat("label_computed_tags_release_tags_weight");
        LABEL_COMPUTED_TAGS_SONG_TAGS_WEIGHT = RE3Properties.getFloat("label_computed_tags_song_tags_weight");

        LABEL_SCORE_DISCOGS_AVG_RATING_WEIGHT = RE3Properties.getFloat("label_score_discogs_avg_rating_weight");
        LABEL_SCORE_MUSICBRAINZ_AVG_RATING_WEIGHT = RE3Properties.getFloat("label_score_musicbrainz_avg_rating_weight");

        LABEL_POPULARITY_DISCOGS_NUM_RATERS_WEIGHT = RE3Properties.getFloat("label_popularity_discogs_num_raters_weight");
        LABEL_POPULARITY_DISCOGS_NUM_RATERS_NORMALIZE_VALUE = RE3Properties.getFloat("label_popularity_discogs_num_raters_normalize_value");
        LABEL_POPULARITY_MUSICBRAINZ_NUM_RATERS_WEIGHT = RE3Properties.getFloat("label_popularity_musicbrainz_num_raters_weight");
        LABEL_POPULARITY_MUSICBRAINZ_NUM_RATERS_NORMALIZE_VALUE = RE3Properties.getFloat("label_popularity_musicbrainz_num_raters_normalize_value");
    }

    //////////////////
    // CONSTRUCTORS //
    //////////////////

    public LabelProfile() { };
    public LabelProfile(LabelIdentifier labelId, int fileId) {
    	record = new LabelRecord(labelId, fileId);
    }
    public LabelProfile(LineReader lineReader) {
    	super(lineReader);
    	int version = Integer.parseInt(lineReader.getNextLine());
    	discogsLabelNameSource = Byte.parseByte(lineReader.getNextLine());
    }


    ////////////
    // FIELDS //
    ////////////

    private byte discogsLabelNameSource;

    static {
    	try{
    		// in order for the XMLEncoder to skip transient variables, this is needed
    		BeanInfo info = Introspector.getBeanInfo(LabelProfile.class);
    		PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
    		for (int i = 0; i < propertyDescriptors.length; ++i) {
    			PropertyDescriptor pd = propertyDescriptors[i];
    			if (pd.getName().equals("labelName") || pd.getName().equals("discogsLabelName")) {
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

    public LabelRecord getLabelRecord() { return (LabelRecord)record; }
    public LabelIdentifier getLabelIdentifier() { return getLabelRecord().getLabelIdentifier(); }

    public String getLabelName() { return getLabelRecord().getLabelName(); }
    public String getDiscogslLabelName() { return getLabelRecord().getDiscogsLabelName(); }

    public int[] getArtistIds() { return getLabelRecord().getArtistIds(); }
    public int getNumArtists() { return getLabelRecord().getNumArtists(); }
    public Vector<Integer> getArtistIdsVector() { return getLabelRecord().getArtistIdsVector(); }
    public float getArtistDegree(String artistName) { return getLabelRecord().getArtistDegree(artistName); }

    // for serialization
	public byte getDiscogsLabelNameSource() { return discogsLabelNameSource; }

    /////////////
    // SETTERS //
    /////////////

    public void setLabelName(String labelName) throws AlreadyExistsException {
    	if (!getLabelName().equals(labelName)) {
    		LabelIdentifier oldLabelId = getLabelIdentifier();
    		LabelIdentifier newLabelId = new LabelIdentifier(labelName);
    		boolean unlocked = false;
    		try {
    			getRecord().getWriteLockSem().startRead("setLabelName");
				updateIdentifier(newLabelId, oldLabelId);
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
				for (int artistId : getArtistIds()) {
					ArtistRecord artistRecord = Database.getArtistIndex().getArtistRecord(artistId);
					if (artistRecord != null)
						artistRecord.update();
				}
    		} catch (InterruptedException e) {
    		} finally {
    			if (!unlocked)
    				getRecord().getWriteLockSem().endRead();
    		}
    	}
    }

    public void setDiscogsLabelName(String discogsLabelName, byte discogsLabelNameSource, boolean triggerUpdate) {
    	if ((this.discogsLabelNameSource == DATA_SOURCE_USER) && (discogsLabelNameSource != DATA_SOURCE_USER))
    		return;
    	getLabelRecord().setDiscogsLabelName(discogsLabelName);
    	this.discogsLabelNameSource = discogsLabelNameSource;
    	if (triggerUpdate && (RE3Properties.getBoolean("enable_triggered_data_miners") || RE3Properties.getBoolean("enable_discogs_data_miners")))
    		TaskManager.runBackgroundTask(new DiscogsLabelFetchTask(this, RE3Properties.getInt("discogs_mining_task_priority") + 5, new DiscogsLabelFetchResultTrigger(this)));
    }

	// for serialization
	public void setDiscogsLabelNameSource(byte discogsLabelNameSource) { this.discogsLabelNameSource = discogsLabelNameSource; }

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
    	super.computeMetadataFromSongs();
    	// recompute artist degrees
    	int total = 0;
    	Map<ArtistIdentifier, Integer> artistCount = new HashMap<ArtistIdentifier, Integer>(getNumReleases() / 2);
    	for (Integer songId : getSongIds()) {
    		SongRecord songRecord = (SongRecord)Database.getSongIndex().getRecord(songId);
    		if (songRecord != null) {
	    		for (ArtistRecord artist : songRecord.getArtists()) {
	    			ArtistIdentifier artistId = artist.getArtistIdentifier();
	    			Integer count = artistCount.get(artistId);
	    			if (count == null)
	    				count = new Integer(1);
	    			else
	    				count = new Integer(1 + count.intValue());
	    			artistCount.put(artistId, count);
	    			++total;
	    		}
    		} else {
    			log.warn("computeMetadataFromSongs(): song record does not exist with uniqueId=" + songId);
    		}
    	}
    	Vector<DegreeValue> artistDegrees = new Vector<DegreeValue>(artistCount.size());
    	for (Entry<ArtistIdentifier, Integer> entry : artistCount.entrySet())
    		artistDegrees.add(new DegreeValue(entry.getKey().toString(), (float)entry.getValue() / total, DATA_SOURCE_COMPUTED));
    	if (log.isTraceEnabled())
    		log.trace("computeMetadataFromSongs(): computed artist degrees=" + artistDegrees);
    	getLabelRecord().setArtistDegrees(artistDegrees);
    }

	@Override
	public void clearSimilaritySearchTransients() {

	}

    @Override
	public float getSimilarity(SearchRecord record) {
    	if (record == null)
    		return 0.0f;
    	float numerator = 0.0f;
        float denominator = 0.0f;
        float confidence = 1.0f;

    	LabelRecord labelRecord = (LabelRecord)record;

        if (getNumSongs() < MIN_SONGS_FOR_SIMILARITY_CONFIDENCE)
            confidence = confidence * getNumSongs() / MIN_SONGS_FOR_SIMILARITY_CONFIDENCE;
        if (labelRecord.getNumSongs() < MIN_SONGS_FOR_SIMILARITY_CONFIDENCE)
            confidence = confidence * labelRecord.getNumSongs() / MIN_SONGS_FOR_SIMILARITY_CONFIDENCE;

        // STYLE SIMILARITY
        if ((LABEL_SIMILARITY_STYLES_WEIGHT > 0.0f) && (getNumActualStyles() > 0) && (record.getNumActualStyles() > 0)) {
	        float styleSimilarity = getLabelRecord().computeStyleSimilarity(record);
	        if (!Float.isNaN(styleSimilarity))
	            numerator += styleSimilarity * LABEL_SIMILARITY_STYLES_WEIGHT * confidence;
	        denominator += LABEL_SIMILARITY_STYLES_WEIGHT * confidence;
        }

        // TAG SIMILARITY
        if ((LABEL_SIMILARITY_TAGS_WEIGHT > 0.0f) && (getNumActualTags() > 0) && (record.getNumActualTags() > 0)) {
        	float tagSimilarity = getLabelRecord().computeTagSimilarity(record);
        	if (!Float.isNaN(tagSimilarity))
        		numerator += tagSimilarity * LABEL_SIMILARITY_TAGS_WEIGHT;
        	denominator += LABEL_SIMILARITY_TAGS_WEIGHT;
        }

        // ARTISTS SIMILARITY
        if ((LABEL_SIMILARITY_ARTISTS_WEIGHT > 0.0f) && (getNumArtists() > 0) && (labelRecord.getNumArtists() > 0)) {
        	float artistsSimilarity = getLabelRecord().computeArtistSimilarity(labelRecord);
        	if (!Float.isNaN(artistsSimilarity))
        		numerator += artistsSimilarity * LABEL_SIMILARITY_ARTISTS_WEIGHT;
        	denominator += LABEL_SIMILARITY_ARTISTS_WEIGHT;
        }

        // BEAT INTENSITY SIMILARITY
        if ((LABEL_SIMILARITY_BEAT_INTENSITY_WEIGHT > 0.0f) && getAvgBeatIntensity().isValid() && labelRecord.getAvgBeatIntensity().isValid()) {
        	float beatIntensitySimilarity = getAvgBeatIntensity().getSimilarityWith(labelRecord.getAvgBeatIntensity());
        	float beatIntensityVarianceSimilarity = getBeatIntensityVariance().getSimilarityWith(labelRecord.getBeatIntensityVariance());
        	numerator += beatIntensitySimilarity * beatIntensityVarianceSimilarity * LABEL_SIMILARITY_BEAT_INTENSITY_WEIGHT;
        	denominator += LABEL_SIMILARITY_BEAT_INTENSITY_WEIGHT;
        }

        if (denominator > 0.0f) {
        	float scale = 1.0f;
        	if (denominator < LABEL_SIMILARITY_CONFIDENCE_THRESHOLD)
        		scale = denominator / LABEL_SIMILARITY_CONFIDENCE_THRESHOLD;
            return numerator * scale / denominator;
        }
        return 0.0f;
    }

    @Override
	public void computeScore() {
    	float score = 0.0f;
    	float totalWeight = 0.0f;

    	// discogs avg rating
    	DiscogsLabelProfile discogsProfile = (DiscogsLabelProfile)getMinedProfile(DATA_SOURCE_DISCOGS);
    	if (discogsProfile != null) {
    		float avgRating = discogsProfile.getAvgRating(); // 1 to 5 stars
    		if ((avgRating != 0.0f) && !Float.isNaN(avgRating)) {
    			float avgRatingNormalized = (avgRating - 1.0f) / 4.0f;
    			score += avgRatingNormalized * LABEL_SCORE_DISCOGS_AVG_RATING_WEIGHT;
    			totalWeight += LABEL_SCORE_DISCOGS_AVG_RATING_WEIGHT;
    		}
    	}

    	// musicbrainz avg rating
    	MusicbrainzLabelProfile musicbrainzProfile = (MusicbrainzLabelProfile)getMinedProfile(DATA_SOURCE_MUSICBRAINZ);
    	if (musicbrainzProfile != null) {
    		float avgRating = musicbrainzProfile.getAvgRating(); // 1 to 5 stars
    		if ((avgRating != 0.0f) && !Float.isNaN(avgRating)) {
    			float avgRatingNormalized = (avgRating - 1.0f) / 4.0f;
    			score += avgRatingNormalized * LABEL_SCORE_MUSICBRAINZ_AVG_RATING_WEIGHT;
    			totalWeight += LABEL_SCORE_MUSICBRAINZ_AVG_RATING_WEIGHT;
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

    	// discogs num raters
    	DiscogsLabelProfile discogsProfile = (DiscogsLabelProfile)getMinedProfile(DATA_SOURCE_DISCOGS);
    	if (discogsProfile != null) {
    		float numUniqueRaters = discogsProfile.getNumUniqueRaters(); // unbounded
    		float numUniqueRatersNormalized = numUniqueRaters / LABEL_POPULARITY_DISCOGS_NUM_RATERS_NORMALIZE_VALUE;
    		if (numUniqueRatersNormalized > 1.0f)
    			numUniqueRatersNormalized = 1.0f;
    		popularity += numUniqueRatersNormalized * LABEL_POPULARITY_DISCOGS_NUM_RATERS_WEIGHT;
    		totalWeight += LABEL_POPULARITY_DISCOGS_NUM_RATERS_WEIGHT;
    	}

    	// musicbrainz num raters
    	MusicbrainzLabelProfile musicbrainzProfile = (MusicbrainzLabelProfile)getMinedProfile(DATA_SOURCE_MUSICBRAINZ);
    	if (musicbrainzProfile != null) {
    		float numRaters = musicbrainzProfile.getNumRaters(); // unbounded
    		float numRatersNormalized = numRaters / LABEL_POPULARITY_MUSICBRAINZ_NUM_RATERS_NORMALIZE_VALUE;
    		if (numRatersNormalized > 1.0f)
    			numRatersNormalized = 1.0f;
    		popularity += numRatersNormalized * LABEL_POPULARITY_MUSICBRAINZ_NUM_RATERS_WEIGHT;
    		totalWeight += LABEL_POPULARITY_MUSICBRAINZ_NUM_RATERS_WEIGHT;
    	}

    	if (totalWeight > 0.0f) {
    		popularity /= totalWeight;
    		getSearchRecord().setPopularity(popularity);
    	}
    }

    @Override
	public void computeTags() {
    	DegreeValueSetAverager averagedTags = new DegreeValueSetAverager();

    	// musicbrainz
    	MusicbrainzLabelProfile musicbrainzProfile = (MusicbrainzLabelProfile)getMinedProfile(DATA_SOURCE_MUSICBRAINZ);
    	if (musicbrainzProfile != null) {
    		Vector<DegreeValue> tags = musicbrainzProfile.getTagDegrees();
    		if (tags.size() > 0)
    			averagedTags.addDegreeValueSet(tags, LABEL_COMPUTED_TAGS_MUSICBRAINZ_WEIGHT);
    	}

    	// song tags
    	Vector<DegreeValue> songTagDegrees = computeTagDegreesFromSongs();
    	if (songTagDegrees.size() > 0)
    		averagedTags.addDegreeValueSet(songTagDegrees, LABEL_COMPUTED_TAGS_SONG_TAGS_WEIGHT);

    	// release tags
    	Vector<DegreeValue> releaseTagDegrees = computeTagDegreesFromReleases();
    	if (releaseTagDegrees.size() > 0)
    		averagedTags.addDegreeValueSet(releaseTagDegrees, LABEL_COMPUTED_TAGS_RELEASE_TAGS_WEIGHT);

    	setComputedTags(averagedTags.getNormalizedDegrees());
    }

    @Override
	public void computeStyles() {
    	DegreeValueSetAverager averagedStyles = new DegreeValueSetAverager();

    	// discogs
    	DiscogsLabelProfile discogsProfile = (DiscogsLabelProfile)getMinedProfile(DATA_SOURCE_DISCOGS);
    	if (discogsProfile != null) {
    		Vector<DegreeValue> styles = discogsProfile.getStyleDegrees();
    		if (styles.size() > 0)
    			averagedStyles.addDegreeValueSet(styles, LABEL_COMPUTED_STYLES_DISCOGS_WEIGHT);
    	}

    	// song styles
    	Vector<DegreeValue> songStyleDegrees = computeStyleDegreesFromSongs();
    	if (songStyleDegrees.size() > 0)
    		averagedStyles.addDegreeValueSet(songStyleDegrees, LABEL_COMPUTED_STYLES_SONG_STYLES_WEIGHT);

    	// release styles
    	Vector<DegreeValue> releaseStyleDegrees = computeStyleDegreesFromReleases();
    	if (releaseStyleDegrees.size() > 0)
    		averagedStyles.addDegreeValueSet(releaseStyleDegrees, LABEL_COMPUTED_STYLES_RELEASE_STYLES_WEIGHT);

    	setComputedStyles(averagedStyles.getNormalizedDegrees());
    }

    @Override
	public void computeLinks() {

    	// discogs
    	DiscogsLabelProfile discogsProfile = (DiscogsLabelProfile)getMinedProfile(DATA_SOURCE_DISCOGS);
    	if (discogsProfile != null) {
    		for (Link link : discogsProfile.getLinks())
    			if (!links.contains(link))
    				links.add(link);
    		try {
    			String url = "http://www.discogs.com/label/" + URLEncoder.encode(discogsProfile.getName(), "UTF-8");
    			Link discogsLink = new Link(discogsProfile.getName() + " Discography at Discogs", "", url, "Discogs", DATA_SOURCE_DISCOGS);
    			if (!links.contains(discogsLink))
    				links.add(discogsLink);
    		} catch (Exception e) {
    			log.error("computeLinks(): error", e);
    		}
    	}

    	// musicbrainz
    	MusicbrainzLabelProfile musicbrainzProfile = (MusicbrainzLabelProfile)getMinedProfile(DATA_SOURCE_MUSICBRAINZ);
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
    		if ((getValidImageCount() < RE3Properties.getInt("max_images_per_label_profile")) && !images.contains(image))
    			images.add(image);

    	// discogs
    	DiscogsLabelProfile discogsProfile = (DiscogsLabelProfile)getMinedProfile(DATA_SOURCE_DISCOGS);
    	if (discogsProfile != null) {
    		for (Image image : discogsProfile.getImages())
    			if ((getValidImageCount() < RE3Properties.getInt("max_images_per_label_profile")) && !images.contains(image))
    				images.add(image);
    	}

    	if ((images.size() > 0) && !hasThumbnail())
    		setThumbnailImageFilename(images.get(0).getImageFilename(), images.get(0).getDataSource());
    }

    @Override
	public void computeChanges(byte changedDataSource) {
    	if ((changedDataSource == DATA_SOURCE_UNKNOWN) || (changedDataSource == DATA_SOURCE_DISCOGS) || (changedDataSource == DATA_SOURCE_MUSICBRAINZ))
    		computeLinks();
    	if ((changedDataSource == DATA_SOURCE_UNKNOWN) || (changedDataSource == DATA_SOURCE_DISCOGS))
    		computeStyles();
    	if ((changedDataSource == DATA_SOURCE_UNKNOWN) || (changedDataSource == DATA_SOURCE_DISCOGS) || (changedDataSource == DATA_SOURCE_MUSICBRAINZ))
    		computePopularity();
    	if ((changedDataSource == DATA_SOURCE_UNKNOWN) || (changedDataSource == DATA_SOURCE_DISCOGS) || (changedDataSource == DATA_SOURCE_MUSICBRAINZ))
    		computeScore();
    	if ((changedDataSource == DATA_SOURCE_UNKNOWN) || (changedDataSource == DATA_SOURCE_MUSICBRAINZ))
    		computeTags();
    	if ((changedDataSource == DATA_SOURCE_UNKNOWN) || (changedDataSource == DATA_SOURCE_DISCOGS))
    		computeImages();
    }

    @Override
	public void update(SubmittedProfile submittedProfile, boolean overwrite) {
    	super.update(submittedProfile, overwrite);
    	SubmittedLabel submittedLabel = (SubmittedLabel)submittedProfile;
    }

    @Override
	public void write(LineWriter writer) {
    	super.write(writer);
    	writer.writeLine("1"); // version
    	writer.writeLine(discogsLabelNameSource);
    }

    @Override
	protected Record readRecord(LineReader lineReader) {
    	return new LabelRecord(lineReader);
    }

}
