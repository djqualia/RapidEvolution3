package com.mixshare.rapid_evolution.data.profile.search.song;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.audio.tags.TagManager;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.exceptions.AlreadyExistsException;
import com.mixshare.rapid_evolution.data.exceptions.InsufficientInformationException;
import com.mixshare.rapid_evolution.data.exceptions.UnknownErrorException;
import com.mixshare.rapid_evolution.data.identifier.search.release.ReleaseIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.song.MixoutIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.song.SongIdentifier;
import com.mixshare.rapid_evolution.data.mined.billboard.song.BillboardSongProfile;
import com.mixshare.rapid_evolution.data.mined.echonest.song.EchonestSongProfile;
import com.mixshare.rapid_evolution.data.mined.echonest.song.EchonestSongSegment;
import com.mixshare.rapid_evolution.data.mined.lastfm.LastfmCommonProfile;
import com.mixshare.rapid_evolution.data.mined.lastfm.song.LastfmSongProfile;
import com.mixshare.rapid_evolution.data.mined.lyricsfly.song.LyricsflySongProfile;
import com.mixshare.rapid_evolution.data.mined.lyricwiki.song.LyricwikiSongProfile;
import com.mixshare.rapid_evolution.data.mined.musicbrainz.song.MusicbrainzSongProfile;
import com.mixshare.rapid_evolution.data.mined.yahoo.song.YahooSongProfile;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.profile.common.image.Image;
import com.mixshare.rapid_evolution.data.profile.common.link.Link;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.profile.search.artist.ArtistProfile;
import com.mixshare.rapid_evolution.data.profile.search.label.LabelProfile;
import com.mixshare.rapid_evolution.data.profile.search.release.ReleaseProfile;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.filter.playlist.PlaylistRecord;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.data.record.search.artist.ArtistRecord;
import com.mixshare.rapid_evolution.data.record.search.label.LabelRecord;
import com.mixshare.rapid_evolution.data.record.search.release.ReleaseRecord;
import com.mixshare.rapid_evolution.data.record.search.song.MixoutRecord;
import com.mixshare.rapid_evolution.data.record.search.song.ReleaseInstance;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.data.search.SearchResult;
import com.mixshare.rapid_evolution.data.submitted.SubmittedProfile;
import com.mixshare.rapid_evolution.data.submitted.search.release.SubmittedRelease;
import com.mixshare.rapid_evolution.data.submitted.search.song.SubmittedMixout;
import com.mixshare.rapid_evolution.data.submitted.search.song.SubmittedSong;
import com.mixshare.rapid_evolution.data.util.DegreeValue;
import com.mixshare.rapid_evolution.data.util.DegreeValueSetAverager;
import com.mixshare.rapid_evolution.data.util.similarity.PearsonSimilarity;
import com.mixshare.rapid_evolution.music.accuracy.Accuracy;
import com.mixshare.rapid_evolution.music.beatintensity.BeatIntensity;
import com.mixshare.rapid_evolution.music.bpm.Bpm;
import com.mixshare.rapid_evolution.music.bpm.CombinedBpm;
import com.mixshare.rapid_evolution.music.duration.Duration;
import com.mixshare.rapid_evolution.music.key.CombinedKey;
import com.mixshare.rapid_evolution.music.key.CombinedKeyCode;
import com.mixshare.rapid_evolution.music.key.Key;
import com.mixshare.rapid_evolution.music.key.KeyCode;
import com.mixshare.rapid_evolution.music.rating.Rating;
import com.mixshare.rapid_evolution.music.timesig.TimeSig;
import com.mixshare.rapid_evolution.ui.model.profile.SimilarProfilesModel;
import com.mixshare.rapid_evolution.util.StringUtil;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

public class SongProfile extends SearchProfile {

    static private final long serialVersionUID = 0L;
    static private Logger log = Logger.getLogger(SongProfile.class);

    static private float SONG_SIMILARITY_CONFIDENCE_THRESHOLD;

    static private float SONG_SIMILARITY_ARTISTS_WEIGHT;
    static private float SONG_SIMILARITY_FEATURED_ARTISTS_WEIGHT;
    static private float SONG_SIMILARITY_LABELS_WEIGHT;
    static private float SONG_SIMILARITY_STYLES_WEIGHT;
    static private float SONG_SIMILARITY_TAGS_WEIGHT;
    static private float SONG_SIMILARITY_LASTFM_WEIGHT;
    static private float SONG_SIMILARITY_BEAT_INTENSITY_WEIGHT;
    static private float SONG_SIMILARITY_BPM_WEIGHT;
    static private float SONG_SIMILARITY_DURATION_WEIGHT;
    static private float SONG_SIMILARITY_TIME_SIG_WEIGHT;
    static private float SONG_SIMILARITY_ARTIST_COMPARE_THRESHOLD;
    static private float SONG_SIMILARITY_TIMBRE_WEIGHT;
    static private float SONG_SIMILARITY_TIMBRE_WEIGHT_DECREASE_FACTOR;
    static private float SONG_SIMILARITY_TIMBRE_RANGE_SCALE;
    static private float SONG_SIMILARITY_TIMBRE_MIN;

    static private float SONG_COMPUTED_TAGS_LASTFM_WEIGHT;
    static private float SONG_COMPUTED_TAGS_MUSICBRAINZ_WEIGHT;

    static private float SONG_COMPUTED_STYLES_YAHOO_CATEGORIES_WEIGHT;

    static private float SONG_SCORE_LASTFM_AVG_PLAYCOUNT_WEIGHT;
    static private float SONG_SCORE_LASTFM_AVG_PLAYCOUNT_NORMALIZE_VALUE;
    static private float SONG_SCORE_ECHONEST_HOTNESS_WEIGHT;
    static private float SONG_SCORE_MUSICBRAINZ_AVG_RATING_WEIGHT;

    static private float SONG_POPULARITY_LASTFM_NUM_LISTENERS_WEIGHT;
    static private float SONG_POPULARITY_LASTFM_NUM_LISTENERS_NORMALIZE_VALUE;
    static private float SONG_POPULARITY_ECHONEST_FAMILIARITY_WEIGHT;
    static private float SONG_POPULARITY_MUSICBRAINZ_NUM_RATERS_WEIGHT;
    static private float SONG_POPULARITY_MUSICBRAINZ_NUM_RATERS_NORMALIZE_VALUE;
    static private float SONG_POPULARITY_BILLBOARD_NUM_WEEKS_WEIGHT;
    static private float SONG_POPULARITY_BILLBOARD_NUM_WEEKS_NORMALIZE_VALUE;

    static {
    	loadProperties();
    }

    static public void loadProperties() {
    	SONG_SIMILARITY_CONFIDENCE_THRESHOLD = RE3Properties.getFloat("song_similarity_confidence_threshold");

        SONG_SIMILARITY_ARTISTS_WEIGHT = RE3Properties.getFloat("song_similarity_artists_weight");
        SONG_SIMILARITY_FEATURED_ARTISTS_WEIGHT = RE3Properties.getFloat("song_similarity_featured_artists_weight");
        SONG_SIMILARITY_ARTIST_COMPARE_THRESHOLD = RE3Properties.getFloat("song_similarity_artist_compare_threshold");
        SONG_SIMILARITY_LABELS_WEIGHT = RE3Properties.getFloat("song_similarity_labels_weight");
        SONG_SIMILARITY_STYLES_WEIGHT = RE3Properties.getFloat("song_similarity_styles_weight");
        SONG_SIMILARITY_TAGS_WEIGHT = RE3Properties.getFloat("song_similarity_tags_weight");
        SONG_SIMILARITY_LASTFM_WEIGHT = RE3Properties.getFloat("song_similarity_lastfm_weight");
        SONG_SIMILARITY_BEAT_INTENSITY_WEIGHT = RE3Properties.getFloat("song_similarity_beat_intensity_weight");
        SONG_SIMILARITY_BPM_WEIGHT = RE3Properties.getFloat("song_similarity_bpm_weight");
        SONG_SIMILARITY_DURATION_WEIGHT = RE3Properties.getFloat("song_similarity_duration_weight");
        SONG_SIMILARITY_TIME_SIG_WEIGHT = RE3Properties.getFloat("song_similarity_time_sig_weight");
        SONG_SIMILARITY_TIMBRE_WEIGHT = RE3Properties.getFloat("song_similarity_timbre_weight");
        SONG_SIMILARITY_TIMBRE_WEIGHT_DECREASE_FACTOR = RE3Properties.getFloat("timbre_weight_decrease_factor");
        SONG_SIMILARITY_TIMBRE_RANGE_SCALE = RE3Properties.getFloat("timbre_range_scale");
        SONG_SIMILARITY_TIMBRE_MIN = RE3Properties.getFloat("timbre_similarity_minimum");

        SONG_COMPUTED_TAGS_LASTFM_WEIGHT = RE3Properties.getFloat("song_computed_tags_lastfm_weight");
        SONG_COMPUTED_TAGS_MUSICBRAINZ_WEIGHT = RE3Properties.getFloat("song_computed_tags_musicbrainz_weight");

        SONG_COMPUTED_STYLES_YAHOO_CATEGORIES_WEIGHT = RE3Properties.getFloat("song_computed_styles_yahoo_categories_weight");

        SONG_SCORE_LASTFM_AVG_PLAYCOUNT_WEIGHT = RE3Properties.getFloat("song_score_lastfm_avg_playcount_weight");
        SONG_SCORE_LASTFM_AVG_PLAYCOUNT_NORMALIZE_VALUE = RE3Properties.getFloat("song_score_lastfm_avg_playcount_normalize_value");
        SONG_SCORE_MUSICBRAINZ_AVG_RATING_WEIGHT = RE3Properties.getFloat("song_score_musicbrainz_avg_rating_weight");

        SONG_POPULARITY_LASTFM_NUM_LISTENERS_WEIGHT = RE3Properties.getFloat("song_popularity_lastfm_num_listeners_weight");
        SONG_POPULARITY_LASTFM_NUM_LISTENERS_NORMALIZE_VALUE = RE3Properties.getFloat("song_popularity_lastfm_num_listeners_normalize_value");
        SONG_POPULARITY_MUSICBRAINZ_NUM_RATERS_WEIGHT = RE3Properties.getFloat("song_popularity_musicbrainz_num_raters_weight");
        SONG_POPULARITY_MUSICBRAINZ_NUM_RATERS_NORMALIZE_VALUE = RE3Properties.getFloat("song_popularity_musicbrainz_num_raters_normalize_value");
        SONG_POPULARITY_BILLBOARD_NUM_WEEKS_WEIGHT = RE3Properties.getFloat("song_popularity_billboard_num_weeks_weight");
        SONG_POPULARITY_BILLBOARD_NUM_WEEKS_NORMALIZE_VALUE = RE3Properties.getFloat("song_popularity_billboard_num_weeks_normalize_value");
    }

    //////////////////
    // CONSTRUCTORS //
    //////////////////

    public SongProfile() { };
    public SongProfile(SongIdentifier songId, int fileId) {
    	record = new SongRecord(songId, fileId);
    }
    public SongProfile(LineReader lineReader) {
    	super(lineReader);
    	int version = Integer.parseInt(lineReader.getNextLine());
    	keySource = Byte.parseByte(lineReader.getNextLine());
    	bpmSource = Byte.parseByte(lineReader.getNextLine());
    	timeSigSource = Byte.parseByte(lineReader.getNextLine());
    	beatIntensitySource = Byte.parseByte(lineReader.getNextLine());
    	durationSource = Byte.parseByte(lineReader.getNextLine());
    	originalYearReleasedSource = Byte.parseByte(lineReader.getNextLine());
    	replayGain = Float.parseFloat(lineReader.getNextLine());
    	replayGainSource = Byte.parseByte(lineReader.getNextLine());
    	iTunesID = lineReader.getNextLine();
    	isSyncedWithMixshare = Boolean.parseBoolean(lineReader.getNextLine());
    	int numMixouts = Integer.parseInt(lineReader.getNextLine());
    	mixoutIds = new HashMap<Integer, Object>(numMixouts);
    	if (version >= 3) {
        	for (int i = 0; i < numMixouts; ++i)
        		mixoutIds.put(Integer.parseInt(lineReader.getNextLine()), null);
    	} else {
        	for (int i = 0; i < numMixouts; ++i)
        		mixoutIds.put(new MixoutProfile(lineReader).getUniqueId(), null);
    	}
    	int numExcludes = Integer.parseInt(lineReader.getNextLine());
    	excludes = new Vector<Exclude>(numExcludes);
    	for (int i = 0; i < numExcludes; ++i)
    		excludes.add(new Exclude(lineReader));
    	lyrics = lineReader.getNextLine();
    	lyricsSource = Byte.parseByte(lineReader.getNextLine());
    	lastDetectKeyAttempt = Long.parseLong(lineReader.getNextLine());
    	lastDetectBpmAttempt = Long.parseLong(lineReader.getNextLine());
    	lastDetectBeatIntensityAttempt = Long.parseLong(lineReader.getNextLine());
    	lastDetectRGAAttempt = Long.parseLong(lineReader.getNextLine());
    	if (version >= 2)
    		songFileLastUpdated = Long.parseLong(lineReader.getNextLine());
    }

    ////////////
    // FIELDS //
    ////////////

    protected byte keySource = DATA_SOURCE_UNKNOWN;
    protected byte bpmSource = DATA_SOURCE_UNKNOWN;
    protected byte timeSigSource = DATA_SOURCE_UNKNOWN;
    protected byte beatIntensitySource = DATA_SOURCE_UNKNOWN;
    protected byte durationSource = DATA_SOURCE_UNKNOWN;
    protected byte originalYearReleasedSource = DATA_SOURCE_UNKNOWN;

    protected Float replayGain = null;
    protected byte replayGainSource = DATA_SOURCE_UNKNOWN;

    protected String iTunesID;

    protected boolean isSyncedWithMixshare;

    protected Map<Integer, Object> mixoutIds = new HashMap<Integer, Object>();
    protected Vector<Exclude> excludes = new Vector<Exclude>();

    private String lyrics;
    private byte lyricsSource;

    private long lastDetectKeyAttempt;
    private long lastDetectBpmAttempt;
    private long lastDetectBeatIntensityAttempt;
    private long lastDetectRGAAttempt;

    private long songFileLastUpdated; // the last modified stamp the last time the tags were read from the file

	transient private Map<Integer, Map<Integer, Float>> artistSimilarityMap;
	transient private Map<Integer, Map<Integer, Float>> featuringArtistSimilarityMap;

    static {
    	try{
    		// in order for the XMLEncoder to skip transient variables, this is needed
    		BeanInfo info = Introspector.getBeanInfo(SongProfile.class);
    		PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
    		for (int i = 0; i < propertyDescriptors.length; ++i) {
    			PropertyDescriptor pd = propertyDescriptors[i];
    			if (pd.getName().equals("artistSimilarityMap") || pd.getName().equals("featuringArtistSimilarityMap") || pd.getName().equals("artistNames") || pd.getName().equals("releaseTrack") || pd.getName().equals("releaseTitle") || pd.getName().equals("releaseTitleAndTrack")
    					 || pd.getName().equals("title") || pd.getName().equals("remix") || pd.getName().equals("titleAndRemix") || pd.getName().equals("labelName") || pd.getName().equals("labelNames")
    					 || pd.getName().equals("initialLabelNames") || pd.getName().equals("key") || pd.getName().equals("bpm") || pd.getName().equals("timeSig") || pd.getName().equals("beatIntensity")
    					 || pd.getName().equals("duration") || pd.getName().equals("songFilename") || pd.getName().equals("originalYearReleased")) {
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

    public SongRecord getSongRecord() { return (SongRecord)record; }

    public SongIdentifier getSongIdentifier() { return (getSongRecord() != null) ? getSongRecord().getSongIdentifier() : null; }

    public Vector<ArtistRecord> getArtists() { return (getSongRecord() != null) ? getSongRecord().getArtists() : null; }
	public Vector<ArtistProfile> getArtistProfiles() {
		Vector<ArtistProfile> result = new Vector<ArtistProfile>();
		int[] artistIds = getSongRecord().getArtistIds();
		if (artistIds != null) {
			for (int artistId : artistIds) {
				ArtistProfile artistProfile = Database.getArtistIndex().getArtistProfile(artistId);
				if (artistProfile != null) {
					if (!result.contains(artistProfile))
						result.add(artistProfile);
				}
			}
		}
		java.util.Collections.sort(result);
		return result;
	}
    public Vector<String> getArtistNames() { return (getSongRecord() != null) ? getSongRecord().getArtistNames() : null; }
	public String getArtistsDescription() { return (getSongRecord() != null) ? getSongRecord().getArtistsDescription() : null; }
	public String getArtistsDescription(boolean includeFeaturingArtists) { return (getSongRecord() != null) ? getSongRecord().getArtistsDescription(includeFeaturingArtists) : null; }

	public Vector<ReleaseRecord> getReleases() { return (getSongRecord() != null) ? getSongRecord().getReleases() : null; }
	public Vector<ReleaseProfile> getReleaseProfiles() {
		Vector<ReleaseProfile> result = new Vector<ReleaseProfile>();
		int[] releaseIds = getSongRecord().getReleaseIds();
		if (releaseIds != null) {
			for (int releaseId : releaseIds) {
				ReleaseProfile releaseProfile = Database.getReleaseIndex().getReleaseProfile(releaseId);
				if (releaseProfile != null) {
					if (!result.contains(releaseProfile))
						result.add(releaseProfile);
				}
			}
		}
		java.util.Collections.sort(result);
		return result;
	}
	public Vector<ReleaseInstance> getReleaseInstances() { return (getSongRecord() != null) ? getSongRecord().getReleaseInstances() : null; }
	public String getReleaseTitle() { return (getSongRecord() != null) ? getSongRecord().getReleaseTitle() : null; }
    public String getTrackForReleaseId(int releaseId) { return (getSongRecord() != null) ? getSongRecord().getTrackForReleaseId(releaseId) : null; }

	public String getTrack() { return (getSongRecord() != null) ? getSongRecord().getTrack() : null; }
	public String getTitle() { return (getSongRecord() != null) ? getSongRecord().getTitle() : null; }
	public String getRemix() { return (getSongRecord() != null) ? getSongRecord().getRemix() : null; }
	public String getSongDescription() { return (getSongRecord() != null) ? getSongRecord().getSongDescription() : null; }

	public int getNumLabels() { return (getSongRecord() != null) ? getSongRecord().getNumLabels() : null; }
	public Vector<String> getSourceLabelNames() { return (getSongRecord() != null) ? getSongRecord().getSourceLabelNames() : null; }
	public String getLabelsDescription() { return (getSongRecord() != null) ? getSongRecord().getLabelsDescription() : null; }
	public Vector<LabelRecord> getLabels() { return (getSongRecord() != null) ? getSongRecord().getLabels() : null; }
	public Vector<LabelProfile> getLabelProfiles() {
		Vector<LabelProfile> result = new Vector<LabelProfile>();
		int[] labelIds = getSongRecord().getLabelIds();
		if (labelIds != null) {
			for (int labelId : labelIds) {
				LabelProfile labelProfile = Database.getLabelIndex().getLabelProfile(labelId);
				if (labelProfile != null) {
					if (!result.contains(labelProfile))
						result.add(labelProfile);
				}
			}
		}
		java.util.Collections.sort(result);
		return result;
	}
	public Vector<String> getLabelNames() { return (getSongRecord() != null) ? getSongRecord().getLabelNames() : null; }

	public CombinedKey getKey() { return (getSongRecord() != null) ? getSongRecord().getKey() : null; }
	public CombinedKeyCode getKeyCode() { return (getSongRecord() != null) ? getSongRecord().getKeyCode() : null; }
    public Key getStartKey() { return (getSongRecord() != null) ? getSongRecord().getStartKey() : null; }
    public Key getEndKey() { return (getSongRecord() != null) ? getSongRecord().getEndKey() : null; }
    public KeyCode getStartKeyCode() { return (getSongRecord() != null) ? getSongRecord().getStartKeyCode() : null; }
    public KeyCode getEndKeyCode() { return (getSongRecord() != null) ? getSongRecord().getEndKeyCode() : null; }
    public byte getKeySource() { return keySource; }
    public byte getKeyAccuracy() { return (getSongRecord() != null) ? getSongRecord().getKeyAccuracy() : null; }

    public CombinedBpm getBpm() { return (getSongRecord() != null) ? getSongRecord().getBpm() : null; }
    public Bpm getBpmStart() { return (getSongRecord() != null) ? getSongRecord().getBpmStart() : null; }
    public float getStartBpm() { return (getSongRecord() != null) ? getSongRecord().getStartBpm() : null; }
    public Bpm getBpmEnd() { return (getSongRecord() != null) ? getSongRecord().getBpmEnd() : null; }
    public float getEndBpm() { return (getSongRecord() != null) ? getSongRecord().getEndBpm() : null; }
    public byte getBpmSource() { return bpmSource; }
    public byte getBpmAccuracy() { return (getSongRecord() != null) ? getSongRecord().getBpmAccuracy() : null; }

    public String getGenre() { return (getSongRecord() != null) ? getSongRecord().getGenre() : null; }

    public TimeSig getTimeSig() { return (getSongRecord() != null) ? getSongRecord().getTimeSig() : null; }
    public byte getTimeSigSource() { return timeSigSource; }

    public byte getBeatIntensity() { return (getSongRecord() != null) ? getSongRecord().getBeatIntensity() : null; }
    public BeatIntensity getBeatIntensityValue() { return (getSongRecord() != null) ? getSongRecord().getBeatIntensityValue() : null; }
    public byte getBeatIntensitySource() { return beatIntensitySource; }

    public Duration getDuration() { return (getSongRecord() != null) ? getSongRecord().getDuration() : null; }
    public int getDurationInMillis() { return (getSongRecord() != null) ? getSongRecord().getDurationInMillis() : null; }
    public byte getDurationSource() { return durationSource; }

    public String getSongFilename() { return (getSongRecord() != null) ? getSongRecord().getSongFilename() : null; }
    public boolean hasValidSongFilename() { return (getSongRecord() != null) ? getSongRecord().hasValidSongFilename() : null; }
    public long getSongFileLastUpdated() { return songFileLastUpdated; }

    public String getOriginalYearReleasedAsString() { return (getSongRecord() != null) ? getSongRecord().getOriginalYearReleasedAsString() : null; }
    public short getOriginalYearReleased() { return (getSongRecord() != null) ? getSongRecord().getOriginalYearReleased() : null; }
    public byte getOriginalYearReleasedSource() { return originalYearReleasedSource; }

	public Float getReplayGain() { return replayGain; }
    public byte getReplayGainSource() { return replayGainSource; }

	public String getITunesID() { return iTunesID; }

	public boolean isSyncedWithMixshare() { return isSyncedWithMixshare; }

	public Set<Integer> getMixoutIds() { return mixoutIds.keySet(); }
	public Vector<MixoutRecord> getMixouts() {
		Vector<MixoutRecord> result = new Vector<MixoutRecord>(getMixoutIds().size());
		for (int mixoutId : mixoutIds.keySet()) {
			MixoutRecord mixoutRecord = Database.getMixoutIndex().getMixoutRecord(mixoutId);
			if (mixoutRecord != null)
				result.add(mixoutRecord);
		}
		return result;
	}
	public Vector<MixoutProfile> getMixoutProfiles() {
		Vector<MixoutProfile> result = new Vector<MixoutProfile>(getMixoutIds().size());
		for (int mixoutId : mixoutIds.keySet()) {
			MixoutProfile mixoutProfile = Database.getMixoutIndex().getMixoutProfile(mixoutId);
			if (mixoutProfile != null)
				result.add(mixoutProfile);
		}
		return result;
	}
	public Vector<Exclude> getExcludes() { return excludes; }

	public String getLyrics() { return lyrics; }

	public long getLastDetectKeyAttempt() { return lastDetectKeyAttempt; }
	public long getLastDetectBpmAttempt() { return lastDetectBpmAttempt; }
	public long getLastDetectBeatIntensityAttempt() { return lastDetectBeatIntensityAttempt; }
	public long getLastDetectRGAAttempt() { return lastDetectRGAAttempt; }

    public int[] getFeaturingArtistIds() { return getSongRecord().getFeaturingArtistIds(); }
	public String getFeaturingArtistsDescription() { return getSongRecord().getFeaturingArtistsDescription(); }

	public Map<Integer, Map<Integer, Float>> getArtistSimilarityMap() { return artistSimilarityMap; }
	public Map<Integer, Map<Integer, Float>> getFeaturingArtistSimilarityMap() { return featuringArtistSimilarityMap; }

    // for serialization
	public byte getLyricsSource() { return lyricsSource; }

    /////////////
    // SETTERS //
    /////////////

    public void setArtistNames(Vector<String> artistNames) throws AlreadyExistsException {
    	if (log.isTraceEnabled())
    		log.trace("setArtistNames(): this=" + this + ", artistNames=" + artistNames);
    	SongIdentifier oldSongId = getSongIdentifier();
    	SongIdentifier newSongId = new SongIdentifier(artistNames, oldSongId.getSongDescription());
    	if (!oldSongId.equals(newSongId)) {
    		boolean unlocked = false;
    		try {
    			// before updating, keep track of which releases referred to the song's artists specifically
	    		Vector<ReleaseInstance> releaseInstances = getReleaseInstances();
				boolean[] containsOldArtistName = new boolean[releaseInstances.size()];
				int r = 0;
	    		for (ReleaseInstance releaseInstance : releaseInstances) {
	    			ReleaseRecord release = releaseInstance.getRelease();
	    			Vector<ArtistRecord> releaseArtists = release.getArtists();
	    			for (int artistId : oldSongId.getArtistIds()) {
	    				ArtistRecord songArtist = (ArtistRecord)Database.getArtistIndex().getRecord(artistId);
	    				if (songArtist != null) {
	    					boolean match = false;
	    					int a = 0;
	    					while ((a < releaseArtists.size()) && !match) {
	    						if (releaseArtists.get(a).equals(songArtist)) {
	    							match = true;
	    						}
	    						++a;
	    					}
	    					if (match)
	    						containsOldArtistName[r] = true;
	    				}
	    			}
	    			++r;
	    		}
	    		getRecord().getWriteLockSem().startRead("setArtistNames");
	    		updateIdentifier(newSongId, oldSongId);
	    		getRecord().getWriteLockSem().endRead();
	    		unlocked = true;
	    		// right now the updateIdentifier method will not remove relations, so remove any artists that might now be orphaned
	    		for (int artistId : oldSongId.getArtistIds()) {
	    			ArtistRecord artistRecord = (ArtistRecord)Database.getArtistIndex().getRecord(artistId);
	    			if ((artistRecord != null) && (artistRecord.getNumSongs() == 0)) {
	    				Database.getArtistIndex().delete(artistRecord.getUniqueId());
	    			}
	    		}
	    		// update the artists on each associated release (if the release referred to an artist on this song)
	    		r = 0;
	    		for (ReleaseInstance releaseInstance : releaseInstances) {
	    			ReleaseRecord release = releaseInstance.getRelease();
					ReleaseProfile releaseProfile = (ReleaseProfile)Database.getReleaseIndex().getProfile(release.getUniqueId());
	    			Vector<ArtistRecord> releaseArtists = release.getArtists();
	    			if (containsOldArtistName[r]) {
	    				// make sure new artist name is on release
	    				for (String songArtistName : artistNames) {
	    					boolean alreadyExists = false;
	    					int a = 0;
	    					while ((a < releaseArtists.size()) && !alreadyExists) {
	    						if (releaseArtists.get(a).toString().equalsIgnoreCase(songArtistName))
	    							alreadyExists = true;
	    						++a;
	    					}
	    					if (!alreadyExists) {
	    						// need to add the new artist, and also make sure remove any old artists of the release that are no longer valid
	    						Vector<String> newArtistNames = new Vector<String>(releaseArtists.size() + 1);
	    						for (ArtistRecord releaseArtist : releaseArtists) {
	    							boolean stillExists = false;
	    							Iterator<SongRecord> songsIter = release.getSongs().iterator();
	    							while ((songsIter.hasNext()) && !stillExists) {
	    								if (songsIter.next().containsArtist(releaseArtist.getUniqueId()))
	    									stillExists = true;
	    							}
	    							if (stillExists)
	    								newArtistNames.add(releaseArtist.getArtistName());
	    						}
	    						newArtistNames.add(songArtistName);
	    						releaseProfile.setArtistNames(newArtistNames, false);
	    					}
	    				}
	    			}
	    			releaseProfile.addSong(getSongRecord(), releaseInstance.getTrack());
	    			releaseProfile.save();
	    			++r;
	    		}
	    		getRecord().setRelationalItemsChanged(true);
    		} catch (InterruptedException e) {
    		} finally {
    			if (!unlocked)
    				getRecord().getWriteLockSem().endRead();
    			getSongRecord().clearCachedSongDescription();
    		}
    	}
    }

    public void setReleaseTrack(String releaseTrack) throws InsufficientInformationException, AlreadyExistsException {
    	getSongRecord().setTrack(releaseTrack);
    	String releaseTitle = "";
    	if (getReleaseInstances().size() >= 1)
    		releaseTitle = getReleaseInstances().get(0).getRelease().getReleaseTitle();
    	setReleaseTitleAndTrack(releaseTitle, releaseTrack, 0);
    }
    public void setReleaseTitle(String releaseTitle) throws InsufficientInformationException, AlreadyExistsException { setReleaseTitle(releaseTitle, false); }
    public void setReleaseTitle(String releaseTitle, boolean autoMerge) throws InsufficientInformationException, AlreadyExistsException {
    	String track = getTrack();
    	if (getReleaseInstances().size() >= 1)
    		track = getReleaseInstances().get(0).getTrack();
    	setReleaseTitleAndTrack(releaseTitle, track, 0, autoMerge);
    }
    protected void setReleaseTitleAndTrack(String releaseTitle, String releaseTrack, int releaseInstanceIndex) throws InsufficientInformationException, AlreadyExistsException {
    	setReleaseTitleAndTrack(releaseTitle, releaseTrack, releaseInstanceIndex, false);
    }
    protected void setReleaseTitleAndTrack(String releaseTitle, String releaseTrack, int releaseInstanceIndex, boolean autoMerge) throws InsufficientInformationException, AlreadyExistsException {
    	SongIdentifier newSongId = null;
    	boolean unlocked = false;
    	try {
    		if (log.isTraceEnabled())
    			log.trace("setReleaseTitleAndTrack(): this=" + this + ", releaseTitle=" + releaseTitle + ", releaseTrack=" + releaseTrack + ", releaseInstanceIndex=" + releaseInstanceIndex);
	    	Vector<ReleaseInstance> releaseInstances = getReleaseInstances();
	    	if (releaseInstances.size() > releaseInstanceIndex) {
	    		ReleaseInstance releaseInstance = releaseInstances.get(releaseInstanceIndex);
	    		ReleaseProfile oldRelease = (ReleaseProfile)Database.getReleaseIndex().getProfile(releaseInstance.getRelease().getUniqueId());
	    		if ((getTitle().length() == 0) && (releaseInstanceIndex == 0)) {
	    			// song ID will be changing...
	    	    	SongIdentifier oldSongId = getSongIdentifier();
	    	    	newSongId = new SongIdentifier(oldSongId.getArtistIds(), SongIdentifier.getSongDescriptionFromReleaseAndTrack(releaseTitle, releaseInstances.get(0).getTrack()));
	    	    	if (!newSongId.isValid())
	    	    		throw new InsufficientInformationException();
	    	    	if (!oldSongId.equals(newSongId)) {
	    	    		getRecord().getWriteLockSem().startRead("setReleaseTitleAndTrack");
	    	    		updateIdentifier(newSongId, oldSongId);
	    	    		getRecord().getWriteLockSem().endRead();
	    	    		unlocked = true;
	    	    	}
	    		}
	    		ReleaseIdentifier newReleaseId = (oldRelease != null) ? new ReleaseIdentifier(oldRelease.getReleaseIdentifier().getArtistIds(), releaseTitle) : new ReleaseIdentifier(releaseTitle);
	    		if (!newReleaseId.equals(oldRelease.getReleaseIdentifier())) {
	        		oldRelease.removeSong(getUniqueId());
	        		if (oldRelease.getNumSongs() == 0)
	        			Database.getReleaseIndex().delete(oldRelease.getUniqueId());
	        		getSongRecord().removeReleaseInstance(oldRelease.getUniqueId());
	        		if (newReleaseId.isValid()) {
			    		ReleaseProfile newRelease = (ReleaseProfile)Database.getReleaseIndex().getProfile(newReleaseId);
			    		if (newRelease == null) {
			    			try {
			    				SubmittedRelease submittedRelease = new SubmittedRelease(oldRelease.getReleaseIdentifier().getArtistIds(), releaseTitle);
			    				newRelease = (ReleaseProfile)Database.getReleaseIndex().add(submittedRelease);
			    			} catch (UnknownErrorException e) {
			    				log.error("setReleaseTitle(): error", e);
			    			}
			    		}
			    		if (newRelease != null) {
		    				newRelease.addSong(getSongRecord(), releaseInstance.getTrack(), true);
		    				newRelease.save();
			    		}
	        		}
	    		} else {
	        		getSongRecord().removeReleaseInstance(oldRelease.getUniqueId());
	    			oldRelease.addSong(getSongRecord(), releaseTrack, true);
	    			oldRelease.save();
	    		}
	    	} else {
	    		// adding a new release
	    		// NOTE: we'll assume its not a compilation release at this point
	    		ReleaseIdentifier releaseId = new ReleaseIdentifier(getSongIdentifier().getArtistIds(), releaseTitle);
	    		if (releaseId.isValid()) {
		    		ReleaseProfile releaseProfile = (ReleaseProfile)Database.getReleaseIndex().getProfile(releaseId);
		    		if (releaseProfile == null) {
		    			SubmittedRelease newRelease = new SubmittedRelease(getSongIdentifier().getArtistIds(), releaseTitle);
		    			try {
		    				releaseProfile = (ReleaseProfile)Database.getReleaseIndex().add(newRelease);
		    			} catch (UnknownErrorException e) {
		    				log.error("setReleaseTitle(): error", e);
		    			}
		    		}
		    		if (releaseProfile != null) {
		    			releaseProfile.addSong(getSongRecord(), releaseTrack);
		    			releaseProfile.save();
		    		}
	    		}
	    	}
	    	getRecord().setRelationalItemsChanged(true);
		} catch (InterruptedException e) {
		} catch (AlreadyExistsException ae) {
			if (autoMerge) {
				SongProfile existingProfile = Database.getSongIndex().getSongProfile(ae.getId());
				Database.mergeProfiles(existingProfile, this);
			} else {
				throw ae;
			}
		} finally {
			if (!unlocked)
				getRecord().getWriteLockSem().endRead();
			getSongRecord().clearCachedSongDescription();
		}
    }

    public void setTitle(String title) throws InsufficientInformationException, AlreadyExistsException { setTitleAndRemix(title, getRemix()); }
    public void setRemix(String remix) throws InsufficientInformationException, AlreadyExistsException { setTitleAndRemix(getTitle(), remix); }
    protected void setTitleAndRemix(String title, String remix) throws InsufficientInformationException, AlreadyExistsException {
    	SongIdentifier oldSongId = getSongIdentifier();
    	SongIdentifier newSongId = null;
    	if (log.isTraceEnabled())
    		log.trace("setTitleAndRemix(): this=" + this + ", title=" + title + ", remix=" + remix);
    	boolean unlocked = false;
    	try {
	    	if (title.length() > 0) {
	    		newSongId = new SongIdentifier(oldSongId.getArtistIds(), SongIdentifier.getSongDescriptionFromTitleAndRemix(title, remix));
	    	} else {
	    		Vector<ReleaseInstance> releaseInstances = getReleaseInstances();
	    		if (releaseInstances.size() > 0)
	    			newSongId = new SongIdentifier(oldSongId.getArtistIds(), SongIdentifier.getSongDescriptionFromReleaseAndTrack(releaseInstances.get(0).getRelease().getReleaseTitle(), releaseInstances.get(0).getTrack()));
	    	}
	    	if ((newSongId == null) || !newSongId.isValid())
	    		throw new InsufficientInformationException();
	    	if (!oldSongId.equals(newSongId)) {
	    		Vector<ReleaseInstance> releaseInstances = getReleaseInstances();
	    		getRecord().getWriteLockSem().startRead("setTitleAndRemix");
	    		updateIdentifier(newSongId, oldSongId);
	    		getRecord().getWriteLockSem().endRead();
	    		getSongRecord().setTitle(title);
	    		getSongRecord().setRemix(remix);
	    		unlocked = true;
	    		for (ReleaseInstance releaseInstance : releaseInstances) {
	    			ReleaseProfile releaseProfile = (ReleaseProfile)Database.getReleaseIndex().getProfile(releaseInstance.getRelease().getUniqueId());
	    			releaseProfile.addSong(getSongRecord(), releaseInstance.getTrack());
	    			releaseProfile.save();
	    		}
	    	}
		} catch (InterruptedException e) {
		} finally {
			if (!unlocked)
				getRecord().getWriteLockSem().endRead();
			getSongRecord().clearCachedSongDescription();
		}
    }

    public void addLabelName(String labelName) {
    	Vector<String> existingLabelNames = getSongRecord().getLabelNames();
    	if (!StringUtil.containsIgnoreCase(existingLabelNames, labelName)) {
    		existingLabelNames.add(labelName);
    		setLabelNames(existingLabelNames);
    	}
    }
    public void addLabelNames(Vector<String> labelNames) {
    	Vector<String> existingLabelNames = getSongRecord().getLabelNames();
    	for (String labelName : labelNames) {
    		if (labelName.length() > 0) {
    			if (!StringUtil.containsIgnoreCase(existingLabelNames, labelName))
    				existingLabelNames.add(labelName);
    		}
    	}
		setLabelNames(existingLabelNames);
    }
    public void setLabelName(String labelName) {
    	Vector<String> labelNames = new Vector<String>(1);
    	labelNames.add(labelName);
    	setLabelNames(labelNames);
    }
    public void removeLabelName(String labelName) {
    	Vector<String> existingLabelNames = getSongRecord().getLabelNames();
    	if (existingLabelNames.contains(labelName)) {
    		existingLabelNames.remove(labelName);
    		setLabelNames(existingLabelNames);
    	}
    }
	public void setLabelNames(Vector<String> labelNames) { getSongRecord().setLabelNames(labelNames); }
	public void setInitialLabelNames(Vector<String> labelNames) { getSongRecord().setLabelNames(labelNames, false, false); }

	public void setKey(Key startKey, Key endKey, Accuracy keyAccuracy, byte keySource) { setKey(startKey, endKey, keyAccuracy.getAccuracy(), keySource); }
	public void setKey(Key startKey, Key endKey, byte keyAccuracy, byte keySource) {
		getSongRecord().setStartKey(startKey);
		getSongRecord().setEndKey(endKey);
		this.keySource = keySource;
		getSongRecord().setKeyAccuracy(keyAccuracy);
	}

	public void setBpm(Bpm startBpm, Bpm endBpm, byte bpmAccuracy, byte bpmSource) {
		getSongRecord().setStartBpm(startBpm);
		getSongRecord().setEndBpm(endBpm);
		this.bpmSource = bpmSource;
		getSongRecord().setBpmAccuracy(bpmAccuracy);
	}

	public void setTimeSig(TimeSig timeSig, byte timeSigSource) {
		getSongRecord().setTimeSig(timeSig);
		this.timeSigSource = timeSigSource;
	}

	public void setBeatIntensity(BeatIntensity beatIntensity, byte beatIntensitySource) {
		if ((beatIntensitySource != DATA_SOURCE_USER) && (this.beatIntensitySource == DATA_SOURCE_USER))
			return;
		getSongRecord().setBeatIntensity(beatIntensity);
		this.beatIntensitySource = beatIntensitySource;
	}

	public void setDuration(Duration duration, byte source) {
		getSongRecord().setDuration(duration);
		durationSource = source;
	}

	public void setSongFilename(String songFilename) {
		getSongRecord().setSongFilename(songFilename);
	}
	public void setSongFileLastUpdated(long songFileLastUpdated) { this.songFileLastUpdated = songFileLastUpdated; }

	public void setOriginalYearReleased(short originalYearReleased, byte originalYearReleasedSource) {
		getSongRecord().setOriginalYearReleased(originalYearReleased);
		this.originalYearReleasedSource = originalYearReleasedSource;
	}

	public void setReplayGain(float replayGain, byte replayGainSource) {
		this.replayGain = replayGain;
		this.replayGainSource = replayGainSource;
	}

	public void setITunesID(String iTunesID) {
		this.iTunesID = iTunesID;
	}

	public void setSyncedWithMixshare(boolean isSyncedWithMixshare) {
		this.isSyncedWithMixshare = isSyncedWithMixshare;
	}

	public void setLyrics(String lyrics, byte lyricsSource) {
		this.lyrics = lyrics;
		this.lyricsSource = lyricsSource;
	}

	public void setLastDetectKeyAttempt(long lastDetectKeyAttempt) { this.lastDetectKeyAttempt = lastDetectKeyAttempt; }
	public void setLastDetectBpmAttempt(long lastDetectBpmAttempt) { this.lastDetectBpmAttempt = lastDetectBpmAttempt; }
	public void setLastDetectBeatIntensityAttempt(long lastDetectBeatIntensityAttempt) { this.lastDetectBeatIntensityAttempt = lastDetectBeatIntensityAttempt; }
	public void setLastDetectRGAAttempt(long lastDetectRGAAttempt) { this.lastDetectRGAAttempt = lastDetectRGAAttempt; }

    public void setArtistSimilarityMap(Map<Integer, Map<Integer, Float>> artistSimilarityMap) { this.artistSimilarityMap = artistSimilarityMap; }
	public void setFeaturingArtistSimilarityMap(Map<Integer, Map<Integer, Float>> featuringArtistSimilarityMap) { this.featuringArtistSimilarityMap = featuringArtistSimilarityMap; }

	@Override
	public void setRating(Rating rating, byte source) {
		super.setRating(rating, source);
		for (ReleaseProfile releaseProfile : getReleaseProfiles()) {
			Rating avgRating = releaseProfile.computeAverageRatingFromSongs();
			if (avgRating != null) {
				releaseProfile.setRating(avgRating, DATA_SOURCE_COMPUTED);
				releaseProfile.save(); //getReleaseRecord().update();
			}
		}
		for (ArtistProfile artistProfile : getArtistProfiles()) {
			Rating avgRating = artistProfile.computeAverageRatingFromSongs();
			if (avgRating != null) {
				artistProfile.setRating(avgRating, DATA_SOURCE_COMPUTED);
				artistProfile.save(); //getArtistRecord().update();
			}
		}
		for (LabelProfile labelProfile : getLabelProfiles()) {
			Rating avgRating = labelProfile.computeAverageRatingFromSongs();
			if (avgRating != null) {
				labelProfile.setRating(avgRating, DATA_SOURCE_COMPUTED);
				labelProfile.save(); //getLabelRecord().update();
			}
		}
	}

	// for serialization
	public void setLyricsSource(byte lyricsSource) { this.lyricsSource = lyricsSource; }
	public void setKeySource(byte keySource) { this.keySource = keySource; }
	public void setBpmSource(byte bpmSource) { this.bpmSource = bpmSource; }
	public void setTimeSigSource(byte timeSigSource) { this.timeSigSource = timeSigSource; }
	public void setBeatIntensitySource(byte beatIntensitySource) { this.beatIntensitySource = beatIntensitySource; }
	public void setDurationSource(byte durationSource) { this.durationSource = durationSource; }
	public void setOriginalYearReleasedSource(byte originalYearReleasedSource) { this.originalYearReleasedSource = originalYearReleasedSource; }
	public void setReplayGain(Float replayGain) { this.replayGain = replayGain; }
	public void setReplayGainSource(byte replayGainSource) { this.replayGainSource = replayGainSource; }
	public void setMixoutIds(Map<Integer, Object> mixoutIds) { this.mixoutIds = mixoutIds; }
	public void setExcludes(Vector<Exclude> excludes) { this.excludes = excludes; }
	public void setLyrics(String lyrics) { this.lyrics = lyrics; }

	/////////////
	// METHODS //
	/////////////

	@Override
	public Map<Record, Object> mergeWith(Profile profile) {
		Map<Record, Object> relatedRecords = super.mergeWith(profile);
		SongProfile songProfile = (SongProfile)profile;
		// replay gain
		if (replayGain != 0.0f)
			replayGain = songProfile.replayGain;
		// itunes id
		if (iTunesID == null)
			iTunesID = songProfile.iTunesID;
		// synced
		isSyncedWithMixshare = false;
		// mixouts
		for (Integer mixoutId : songProfile.getMixoutIds()) {
			try {
				MixoutProfile mixout = Database.getMixoutIndex().getMixoutProfile(mixoutId);
				if (mixout != null) {
					MixoutIdentifier newMixoutId = new MixoutIdentifier(getUniqueId(), mixout.getMixoutIdentifier().getToSongId());
					SubmittedMixout newMixout = new SubmittedMixout(newMixoutId, mixout);
					Database.delete(mixout.getMixoutIdentifier());
					Database.add(newMixout);
				}
			} catch (Exception e) {
				log.error("mergeWith(): error merging mixouts", e);
			}
		}
		// excludes
		for (Exclude exclude : songProfile.getExcludes()) {
			exclude.setFromSongId(getUniqueId());
			if (!excludes.contains(exclude)) {
				excludes.add(exclude);
			}
		}
		return relatedRecords;
	}

	public void addMixout(MixoutProfile mixout) {
		if (!mixoutIds.containsKey(mixout.getUniqueId()))
			mixoutIds.put(mixout.getUniqueId(), null);
		short numMixouts = (short)mixoutIds.size();
		if (numMixouts > 255)
			numMixouts = 255;
		getSongRecord().setNumMixouts(numMixouts);
	}
	public void removeMixout(int mixoutId) {
		mixoutIds.remove(mixoutId);
		short numMixouts = (short)mixoutIds.size();
		if (numMixouts > 255)
			numMixouts = 255;
		getSongRecord().setNumMixouts(numMixouts);
	}

	public void addExclude(Exclude exclude) {
		if (!excludes.contains(exclude))
			excludes.add(exclude);
	}

	@Override
	public void clearSimilaritySearchTransients() {
		artistSimilarityMap = null;
		featuringArtistSimilarityMap = null;
	}

    @Override
	public float getSimilarity(SearchRecord record) {
    	if (record == null)
    		return 0.0f;
    	float numerator = 0.0f;
        float denominator = 0.0f;

        SongRecord songRecord = (SongRecord)record;

        // LABEL SIMILARITY
        if ((SONG_SIMILARITY_LABELS_WEIGHT > 0.0f) && (getNumLabels() > 0) && (songRecord.getNumLabels() > 0)) {
        	float labelSimilarity = PearsonSimilarity.computeSimilarity(getSongRecord().getLabelIds(), songRecord.getLabelIds());
        	if (!Float.isNaN(labelSimilarity))
        		numerator += labelSimilarity * SONG_SIMILARITY_LABELS_WEIGHT;
        	denominator += SONG_SIMILARITY_LABELS_WEIGHT;
        }

        // STYLE SIMILARITY
        if ((SONG_SIMILARITY_STYLES_WEIGHT > 0.0f) && (getNumActualStyles() > 0) && (record.getNumActualStyles() > 0)) {
        	float styleSimilarity = getSongRecord().computeStyleSimilarity(record);
        	if (!Float.isNaN(styleSimilarity))
        		numerator += styleSimilarity * SONG_SIMILARITY_STYLES_WEIGHT;
        	denominator += SONG_SIMILARITY_STYLES_WEIGHT;
        }

        // TAG SIMILARITY
        if ((SONG_SIMILARITY_TAGS_WEIGHT > 0.0f) && (getNumActualTags() > 0) && (record.getNumActualTags() > 0)) {
        	float tagSimilarity = getSongRecord().computeTagSimilarity(record);
        	if (!Float.isNaN(tagSimilarity))
        		numerator += tagSimilarity * SONG_SIMILARITY_TAGS_WEIGHT;
        	denominator += SONG_SIMILARITY_TAGS_WEIGHT;
        }

        // LASTFM SIMILARITY
        if (SONG_SIMILARITY_LASTFM_WEIGHT > 0.0f) {
        	LastfmSongProfile songProfile = (LastfmSongProfile)getMinedProfile(DATA_SOURCE_LASTFM);
        	if (songProfile != null) {
        		numerator += songProfile.getSimilarityWith(songRecord) * SONG_SIMILARITY_LASTFM_WEIGHT;
        	}
        	denominator += SONG_SIMILARITY_LASTFM_WEIGHT;
        }

        // BEAT INTENSITY SIMILARITY
        if ((SONG_SIMILARITY_BEAT_INTENSITY_WEIGHT > 0.0f) && getBeatIntensityValue().isValid() && songRecord.getBeatIntensityValue().isValid()) {
        	numerator += getBeatIntensityValue().getSimilarityWith(songRecord.getBeatIntensityValue()) * SONG_SIMILARITY_BEAT_INTENSITY_WEIGHT;
        	denominator += SONG_SIMILARITY_BEAT_INTENSITY_WEIGHT;
        }

        // BPM SIMILARITY
        if ((SONG_SIMILARITY_BPM_WEIGHT > 0.0f) && getBpmStart().isValid() && songRecord.getBpmStart().isValid()) {
        	numerator += Bpm.getSimilarity(getStartBpm(), songRecord.getStartBpm()) * SONG_SIMILARITY_BPM_WEIGHT;
        	denominator += SONG_SIMILARITY_BPM_WEIGHT;
        }

        // DURATION SIMILARITY
        if ((SONG_SIMILARITY_DURATION_WEIGHT > 0.0f) && getDuration().isValid() && songRecord.getDuration().isValid()) {
        	numerator += Duration.getSimilarity(getDurationInMillis(), songRecord.getDurationInMillis()) * SONG_SIMILARITY_DURATION_WEIGHT;
        	denominator += SONG_SIMILARITY_DURATION_WEIGHT;
        }

        // TIME SIG SIMILARITY
        if ((SONG_SIMILARITY_TIME_SIG_WEIGHT > 0.0f) && getTimeSig().isValid() && songRecord.getTimeSig().isValid()) {
        	numerator += getTimeSig().getSimilarityWith(songRecord.getTimeSig()) * SONG_SIMILARITY_TIME_SIG_WEIGHT;
        	denominator += SONG_SIMILARITY_TIME_SIG_WEIGHT;
        }

        /*
        // TIMBRE SIMILARITY
        if ((SONG_SIMILARITY_TIMBRE_WEIGHT > 0.0f) && (getSongRecord().getTimbre() != null) && (songRecord.getTimbre() != null)) {
        	float total = 0.0f;
        	float diff = 0.0f;
        	for (int i = 0; i < 12; ++i) {
        		float value1 = getSongRecord().getTimbre()[i];
        		float value2 = songRecord.getTimbre()[i];
        		float maxAbs = Math.max(Math.abs(value1), Math.abs(value2));
        		float absDiff = Math.abs(value1 - value2);
        		total += maxAbs * 2;
        		diff += absDiff;
        	}
        	float timbreSimilarity = (total - diff) / total;
        	numerator += timbreSimilarity * SONG_SIMILARITY_TIMBRE_WEIGHT;
    		denominator += SONG_SIMILARITY_TIMBRE_WEIGHT;
        }
    	*/

        // TIMBRE SIMILARITY
        if ((SONG_SIMILARITY_TIMBRE_WEIGHT > 0.0f) && (getSongRecord().getTimbre() != null) && (songRecord.getTimbre() != null)) {
        	float scale = 1.0f;
        	float timbreNumerator = 0.0f;
        	float timbreDenominator = 0.0f;
        	for (int i = 0; i < 12; ++i) {
        		float value1 = getSongRecord().getTimbre()[i];
        		float value2 = songRecord.getTimbre()[i];
        		float variance1 = getSongRecord().getTimbreVariance()[i];
        		float variance2 = songRecord.getTimbreVariance()[i];
        		float min = EchonestSongSegment.TIMBRE_COEFF_MIN[i];
        		float max = EchonestSongSegment.TIMBRE_COEFF_MAX[i];
        		float absDiff = Math.abs(value1 - value2);
        		float absDiffVariance = Math.abs(variance1 - variance2);
        		float range = max - min;
        		range /= SONG_SIMILARITY_TIMBRE_RANGE_SCALE;
        		float similarity = (range - absDiff) / range;
        		float varianceSimilarity = (range - absDiffVariance) / range;
        		similarity *= varianceSimilarity;
        		if (similarity > 1.0f)
        			similarity = 1.0f;
        		if (similarity < 0.0f)
        			similarity = 0.0f;
        		similarity = (similarity - SONG_SIMILARITY_TIMBRE_MIN) * (1.0f / (1.0f - SONG_SIMILARITY_TIMBRE_MIN));
        		timbreNumerator += similarity * scale;
        		timbreDenominator += 1.0f * scale;
        		scale *= SONG_SIMILARITY_TIMBRE_WEIGHT_DECREASE_FACTOR;
        	}
        	float timbreSimilarity = timbreNumerator / timbreDenominator;
        	numerator += timbreSimilarity * SONG_SIMILARITY_TIMBRE_WEIGHT;
            denominator += SONG_SIMILARITY_TIMBRE_WEIGHT;
        }

        float similaritySoFar = 0.0f;
        if (denominator > 0.0f)
        	similaritySoFar = numerator / denominator;

        // ARTIST SIMILARITY
        if ((SONG_SIMILARITY_ARTISTS_WEIGHT > 0.0f) && (similaritySoFar >= SONG_SIMILARITY_ARTIST_COMPARE_THRESHOLD)) {
        	if (artistSimilarityMap == null) {
        		artistSimilarityMap = computeArtistSimilarityMap(this);
        		featuringArtistSimilarityMap = computeFeaturingArtistSimilarityMap(this);
        	}
        	int[] otherArtistIds = songRecord.getArtistIds();
        	int[] otherFeaturingArtistIds = songRecord.getFeaturingArtistIds();
        	float featuredArtistWeight = SONG_SIMILARITY_FEATURED_ARTISTS_WEIGHT;
        	float avgSimilarity = 0.0f;
        	float numComparisons = 0.0f;
        	for (int artistId : artistSimilarityMap.keySet()) {
        		if (otherArtistIds != null) {
	        		for (int otherArtistId : otherArtistIds) {
	        			avgSimilarity += getArtistSimilarity(artistId, otherArtistId);
	        			numComparisons += 1.0f;
	        		}
        		}
        		if (otherFeaturingArtistIds != null) {
	        		for (int otherFeaturingArtistId : otherFeaturingArtistIds) {
        				avgSimilarity += getArtistSimilarity(artistId, otherFeaturingArtistId) * featuredArtistWeight;
	        			numComparisons += featuredArtistWeight;
	        		}
        		}
        	}
        	for (int featuringArtistId : featuringArtistSimilarityMap.keySet()) {
        		if (otherArtistIds != null) {
	        		for (int otherArtistId : otherArtistIds) {
	    				avgSimilarity += getFeaturingArtistSimilarity(featuringArtistId, otherArtistId) * featuredArtistWeight;
	        			numComparisons += featuredArtistWeight;
	        		}
        		}
        		if (otherFeaturingArtistIds != null) {
	        		for (int otherFeaturingArtistId : otherFeaturingArtistIds) {
	    				avgSimilarity += getFeaturingArtistSimilarity(featuringArtistId, otherFeaturingArtistId) * featuredArtistWeight * featuredArtistWeight;
	        			numComparisons += featuredArtistWeight * featuredArtistWeight;
	        		}
        		}
        	}
        	if (numComparisons > 0.0f) {
        		avgSimilarity /= numComparisons;
        		numerator += avgSimilarity * SONG_SIMILARITY_ARTISTS_WEIGHT;
        	}
        	denominator += SONG_SIMILARITY_ARTISTS_WEIGHT;
        }

        if (denominator > 0.0f) {
        	float scale = 1.0f;
        	if (denominator < SONG_SIMILARITY_CONFIDENCE_THRESHOLD)
        		scale = denominator / SONG_SIMILARITY_CONFIDENCE_THRESHOLD;
            float similarity = numerator * scale / denominator;
            return similarity;
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

    private float getFeaturingArtistSimilarity(int featuringArtistId, int otherArtistId) {
    	Map<Integer, Float> subMap = featuringArtistSimilarityMap.get(featuringArtistId);
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
    		float playCountNormalized = avgPlayersPerListener / SONG_SCORE_LASTFM_AVG_PLAYCOUNT_NORMALIZE_VALUE;
    		if (playCountNormalized > 1.0f)
    			playCountNormalized = 1.0f;
    		score += playCountNormalized * SONG_SCORE_LASTFM_AVG_PLAYCOUNT_WEIGHT;
    		totalWeight += SONG_SCORE_LASTFM_AVG_PLAYCOUNT_WEIGHT;
    	}

    	// musicbrainz avg rating
    	MusicbrainzSongProfile musicbrainzProfile = (MusicbrainzSongProfile)getMinedProfile(DATA_SOURCE_MUSICBRAINZ);
    	if (musicbrainzProfile != null) {
    		float avgRating = musicbrainzProfile.getAvgRating(); // 1 to 5 stars
    		if ((avgRating != 0.0f) && !Float.isNaN(avgRating)) {
    			float avgRatingNormalized = (avgRating - 1.0f) / 4.0f;
    			score += avgRatingNormalized * SONG_SCORE_MUSICBRAINZ_AVG_RATING_WEIGHT;
    			totalWeight += SONG_SCORE_MUSICBRAINZ_AVG_RATING_WEIGHT;
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
    		float normalizedNumListeners = numListeners / SONG_POPULARITY_LASTFM_NUM_LISTENERS_NORMALIZE_VALUE;
    		if (normalizedNumListeners > 1.0f)
    			normalizedNumListeners = 1.0f;
    		popularity += normalizedNumListeners * SONG_POPULARITY_LASTFM_NUM_LISTENERS_WEIGHT;
    		totalWeight += SONG_POPULARITY_LASTFM_NUM_LISTENERS_WEIGHT;
    	}

    	// musicbrainz num raters
    	MusicbrainzSongProfile musicbrainzProfile = (MusicbrainzSongProfile)getMinedProfile(DATA_SOURCE_MUSICBRAINZ);
    	if (musicbrainzProfile != null) {
    		float numRaters = musicbrainzProfile.getNumRaters(); // unbounded
    		float numRatersNormalized = numRaters / SONG_POPULARITY_MUSICBRAINZ_NUM_RATERS_NORMALIZE_VALUE;
    		if (numRatersNormalized > 1.0f)
    			numRatersNormalized = 1.0f;
    		popularity += numRatersNormalized * SONG_POPULARITY_MUSICBRAINZ_NUM_RATERS_WEIGHT;
    		totalWeight += SONG_POPULARITY_MUSICBRAINZ_NUM_RATERS_WEIGHT;
    	}

    	// billboard total weeks
    	BillboardSongProfile billboardProfile = (BillboardSongProfile)getMinedProfile(DATA_SOURCE_BILLBOARD);
    	if (billboardProfile != null) {
    		float totalWeeks = billboardProfile.getTotalWeeksOn(); // unbounded
    		float totalWeeksNormalized = totalWeeks / SONG_POPULARITY_BILLBOARD_NUM_WEEKS_NORMALIZE_VALUE;
    		if (totalWeeksNormalized > 1.0f)
    			totalWeeksNormalized = 1.0f;
    		popularity += totalWeeksNormalized * SONG_POPULARITY_BILLBOARD_NUM_WEEKS_WEIGHT;
    		totalWeight += SONG_POPULARITY_BILLBOARD_NUM_WEEKS_WEIGHT;
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
    	LastfmSongProfile lastfmProfile = (LastfmSongProfile)getMinedProfile(DATA_SOURCE_LASTFM);
    	if (lastfmProfile != null) {
    		Vector<DegreeValue> tags = lastfmProfile.getTopTags();
    		if (tags.size() > 0)
    			averagedTags.addDegreeValueSet(tags, SONG_COMPUTED_TAGS_LASTFM_WEIGHT);
    	}

    	// musicbrainz
    	MusicbrainzSongProfile musicbrainzProfile = (MusicbrainzSongProfile)getMinedProfile(DATA_SOURCE_MUSICBRAINZ);
    	if (musicbrainzProfile != null) {
    		Vector<DegreeValue> tags = musicbrainzProfile.getTagDegrees();
    		if (tags.size() > 0)
    			averagedTags.addDegreeValueSet(tags, SONG_COMPUTED_TAGS_MUSICBRAINZ_WEIGHT);
    	}

    	setComputedTags(averagedTags.getNormalizedDegrees());
    }

    @Override
	public void computeStyles() {
    	DegreeValueSetAverager averagedStyles = new DegreeValueSetAverager();

    	// yahoo categories
    	YahooSongProfile yahooProfile = (YahooSongProfile)getMinedProfile(DATA_SOURCE_YAHOO);
    	if (yahooProfile != null) {
    		Vector<DegreeValue> styles = yahooProfile.getStyleDegrees();
    		if (styles.size() > 0)
    			averagedStyles.addDegreeValueSet(styles, SONG_COMPUTED_STYLES_YAHOO_CATEGORIES_WEIGHT);
    	}

    	setComputedStyles(averagedStyles.getNormalizedDegrees());
    }

    public void computeLyrics() {

    	// lyricsfly
    	LyricsflySongProfile lyricsflyProfile = (LyricsflySongProfile)getMinedProfile(DATA_SOURCE_LYRICSFLY);
    	if (lyricsflyProfile != null) {
    		this.lyrics = lyricsflyProfile.getLyricsText();
    		this.lyricsSource = DATA_SOURCE_LYRICSFLY;
    		return;
    	}

    	// lyricwiki
    	LyricwikiSongProfile lyricwikiProfile = (LyricwikiSongProfile)getMinedProfile(DATA_SOURCE_LYRICWIKI);
    	if (lyricwikiProfile != null) {
    		this.lyrics = lyricwikiProfile.getLyricsText();
    		this.lyricsSource = DATA_SOURCE_LYRICWIKI;
    		return;
    	}

    }

    @Override
	public void computeLinks() {

    	// lastfm
    	LastfmSongProfile lastfmProfile = (LastfmSongProfile)getMinedProfile(DATA_SOURCE_LASTFM);
    	if (lastfmProfile != null) {
    		Link lastfmLink = lastfmProfile.getLink();
    		if (lastfmLink != null)
    			if (!links.contains(lastfmLink))
    				links.add(lastfmLink);
    	}

    	// musicbrainz
    	MusicbrainzSongProfile musicbrainzProfile = (MusicbrainzSongProfile)getMinedProfile(DATA_SOURCE_MUSICBRAINZ);
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
    		if ((getValidImageCount() < RE3Properties.getInt("max_images_per_song_profile")) && !images.contains(image))
    			images.add(image);

    	// lastfm
    	LastfmSongProfile lastfmProfile = (LastfmSongProfile)getMinedProfile(DATA_SOURCE_LASTFM);
    	if (lastfmProfile != null) {
    		Image lastfmImage = lastfmProfile.getImage();
    		if (lastfmImage != null)
    			if ((getValidImageCount() < RE3Properties.getInt("max_images_per_song_profile")) && !images.contains(lastfmImage))
    				images.add(lastfmImage);
    	}

    	if ((images.size() > 0) && !hasThumbnail())
    		setThumbnailImageFilename(images.get(0).getImageFilename(), images.get(0).getDataSource());
    }

    public void computeKey() {
    	if ((getKeySource() == DATA_SOURCE_USER) && (getKeyAccuracy() == 100))
    		return;
    	// TODO: use KeyAverager to blend results once more sources are available

    	// echonest
    	EchonestSongProfile echonestProfile = (EchonestSongProfile)getMinedProfile(DATA_SOURCE_ECHONEST);
    	if (echonestProfile != null) {
    		byte echonestAccuracy = (byte)(echonestProfile.getKeyConfidence() * 100);
        	if (!getStartKey().isValid() || (echonestAccuracy > getKeyAccuracy()))
        		setKey(echonestProfile.getKeyValue(), Key.NO_KEY, echonestAccuracy, DATA_SOURCE_ECHONEST);
    	}
    }

    public void computeBpm() {
    	if ((getBpmSource() == DATA_SOURCE_USER) && (getBpmAccuracy() == 100))
    		return;
    	// TODO: use BpmAverager to blend results once more sources are available

    	// echonest
    	EchonestSongProfile echonestProfile = (EchonestSongProfile)getMinedProfile(DATA_SOURCE_ECHONEST);
    	if (echonestProfile != null) {
    		byte echonestAccuracy = (byte)(echonestProfile.getBpmConfidence() * 100);
        	if (!getBpmStart().isValid() || (echonestAccuracy > getBpmAccuracy()))
        		setBpm(new Bpm(echonestProfile.getBpmValue()), new Bpm(0.0f), echonestAccuracy, DATA_SOURCE_ECHONEST);
    	}
    }

    public void computeDuration() {
    	if (getDurationSource() == DATA_SOURCE_USER)
    		return;

		if (getDuration().getDurationInMillis() == 0) {
	    	// echonest
	    	EchonestSongProfile echonestProfile = (EchonestSongProfile)getMinedProfile(DATA_SOURCE_ECHONEST);
	    	if (echonestProfile != null) {
	    		setDuration(new Duration((int)(echonestProfile.getDuration() * 1000.0f)), DATA_SOURCE_ECHONEST);
	    		return;
	    	}

	    	// musicbrainz
	    	MusicbrainzSongProfile musicbrainzProfile = (MusicbrainzSongProfile)getMinedProfile(DATA_SOURCE_MUSICBRAINZ);
	    	if (musicbrainzProfile != null) {
	    		int duration = musicbrainzProfile.getDuration(); // millis
	    		if (duration != 0) {
	    			setDuration(new Duration(duration), DATA_SOURCE_MUSICBRAINZ);
	    			return;
	    		}
	    	}
		}
    }

    public void computeReplayGain() {
		// overall loudness (i'm assuming for now this is similar to RGA)
		if (getReplayGain() == null) {
	    	// echonest
	    	EchonestSongProfile echonestProfile = (EchonestSongProfile)getMinedProfile(DATA_SOURCE_ECHONEST);
	    	if (echonestProfile != null) {
	    		setReplayGain(-echonestProfile.getOverallLoudness(), DATA_SOURCE_ECHONEST);
	    	}
		}
    }

    public void computeTimbre() {
		EchonestSongProfile echonestProfile = (EchonestSongProfile)getMinedProfile(DATA_SOURCE_ECHONEST);
		if (echonestProfile != null) {
			float[] totals = new float[12];
			int count = 0;
			for (EchonestSongSegment segment : echonestProfile.getSegments()) {
				double[] timbre = segment.getTimbre();
				for (int i = 0; i < totals.length; ++i)
					totals[i] += timbre[i];
				++count;
			}
			if (count > 0) {
				for (int i = 0; i < totals.length; ++i)
					totals[i] /= count;
				count = 0;
				float[] variance = new float[12];
				for (EchonestSongSegment segment : echonestProfile.getSegments()) {
					double[] timbre = segment.getTimbre();
					for (int i = 0; i < totals.length; ++i)
						variance[i] += (timbre[i] - totals[i]) * (timbre[i] - totals[i]);
					++count;
				}
				for (int i = 0; i < variance.length; ++i)
					variance[i] = (float)Math.sqrt(variance[i] / count);
				getSongRecord().setTimbre(totals);
				getSongRecord().setTimbreVariance(variance);
			}
		}
    }

    @Override
	public void computeChanges(byte changedDataSource) {
    	if ((changedDataSource == DATA_SOURCE_UNKNOWN) || (changedDataSource == DATA_SOURCE_BILLBOARD) || (changedDataSource == DATA_SOURCE_LASTFM) || (changedDataSource == DATA_SOURCE_MUSICBRAINZ))
    		computePopularity();
    	if ((changedDataSource == DATA_SOURCE_UNKNOWN) || (changedDataSource == DATA_SOURCE_LASTFM) || (changedDataSource == DATA_SOURCE_MUSICBRAINZ))
    		computeLinks();
    	if ((changedDataSource == DATA_SOURCE_UNKNOWN) || (changedDataSource == DATA_SOURCE_LASTFM) || (changedDataSource == DATA_SOURCE_MUSICBRAINZ))
    		computeTags();
    	if ((changedDataSource == DATA_SOURCE_UNKNOWN) || (changedDataSource == DATA_SOURCE_LASTFM) || (changedDataSource == DATA_SOURCE_MUSICBRAINZ))
    		computeScore();
    	if ((changedDataSource == DATA_SOURCE_UNKNOWN) || (changedDataSource == DATA_SOURCE_LYRICSFLY) || (changedDataSource == DATA_SOURCE_LYRICWIKI))
    		computeLyrics();
    	if ((changedDataSource == DATA_SOURCE_UNKNOWN) || (changedDataSource == DATA_SOURCE_YAHOO))
    		computeStyles();
    	if ((changedDataSource == DATA_SOURCE_UNKNOWN) || (changedDataSource == DATA_SOURCE_LASTFM))
    		computeImages();
    	if ((changedDataSource == DATA_SOURCE_UNKNOWN) || (changedDataSource == DATA_SOURCE_ECHONEST))
    		computeKey();
    	if ((changedDataSource == DATA_SOURCE_UNKNOWN) || (changedDataSource == DATA_SOURCE_ECHONEST))
    		computeBpm();
    	if ((changedDataSource == DATA_SOURCE_UNKNOWN) || (changedDataSource == DATA_SOURCE_ECHONEST) || (changedDataSource == DATA_SOURCE_MUSICBRAINZ))
    		computeDuration();
    	if ((changedDataSource == DATA_SOURCE_UNKNOWN) || (changedDataSource == DATA_SOURCE_ECHONEST))
    		computeReplayGain();
    	if (changedDataSource == DATA_SOURCE_ECHONEST)
    		computeTimbre();
    }

    @Override
	public void update(SubmittedProfile submittedProfile, boolean overwrite) {
    	boolean overwriteRelease = false;
    	SubmittedSong submittedSong = (SubmittedSong)submittedProfile;
    	if (isExternalItem() && !submittedSong.isExternalItem()) {
    		overwriteRelease = true;
    	}
    	super.update(submittedProfile, overwrite);
    	if (log.isTraceEnabled())
    		log.trace("update(): this=" + this + ", submittedProfile=" + submittedProfile + ", overwrite=" + overwrite);
    	if (submittedSong.getSubmittedRelease() != null) {
	    	try {
    			//int releaseId = Database.getReleaseIndex().getUniqueIdFromIdentifier(submittedSong.getSubmittedRelease().getReleaseIdentifier());
    			//getSongRecord().addReleaseInstance(new ReleaseInstance(releaseId, submittedSong.getTrack()), false);
				if (overwriteRelease) {
					//getSongRecord().addReleaseInstance(new ReleaseInstance(releaseProfile.getUniqueId(), submittedSong.getTrack()), true);
					try {
						setReleaseTitle(submittedSong.getSubmittedRelease().getReleaseIdentifier().getReleaseTitle());
						setReleaseTrack(submittedSong.getTrack());
					} catch (Exception e) {
						log.error("update(): error", e);
					}
				} else {
					ReleaseProfile releaseProfile = (ReleaseProfile)Database.getReleaseIndex().getProfile(submittedSong.getSubmittedRelease().getReleaseIdentifier());
					if (releaseProfile == null) {
						releaseProfile = (ReleaseProfile)Database.getReleaseIndex().addOrUpdate(submittedSong.getSubmittedRelease());
					}
					if (releaseProfile != null) {
						releaseProfile.addSong(getSongRecord(), submittedSong.getTrack());
						releaseProfile.save();

					}
				}
	    	} catch (Exception e) {
	    		log.error("update(): error", e);
	    	}
    	}
    	if (submittedSong.getLabelNames() != null)
    		addLabelNames(submittedSong.getLabelNames());
    	if (!getStartKey().isValid() || overwrite)
    		setKey(submittedSong.getStartKey(), submittedSong.getEndKey(), submittedSong.getKeyAccuracy(), submittedSong.getKeySource());
    	if (!getBpmStart().isValid() || overwrite)
    		setBpm(submittedSong.getStartBpm(), submittedSong.getEndBpm(), submittedSong.getBpmAccuracy(), submittedSong.getBpmSource());
    	if (!getTimeSig().isValid() || overwrite || getTimeSig().equals(TimeSig.getTimeSig(4,4)))
    		setTimeSig(submittedSong.getTimeSig(), submittedSong.getTimeSigSource());
    	if (!getBeatIntensityValue().isValid() || overwrite)
    		setBeatIntensity(submittedSong.getBeatIntensity(), submittedSong.getBeatIntensitySource());
    	if (!getDuration().isValid() || overwrite)
    		setDuration(submittedSong.getDuration(), submittedSong.getDurationSource());
    	if (!hasValidSongFilename() || overwrite)
    		setSongFilename(submittedSong.getSongFilename());
    	if (hasValidSongFilename())
    		setExternalItem(false);
    	if ((getOriginalYearReleased() == 0) || overwrite)
    		setOriginalYearReleased(submittedSong.getOriginalYearReleased(), submittedSong.getOriginalYearReleasedSource());
    	if ((getReplayGain() == null) || overwrite)
    		setReplayGain(submittedSong.getReplayGain(), submittedSong.getReplayGainSource());
    	if ((getITunesID() == null) || overwrite)
    		setITunesID(submittedSong.getITunesID());
    	if ((getLyrics() == null) || overwrite)
    		setLyrics(submittedSong.getLyrics(), submittedSong.getLyricsSource());
    	if ((submittedSong.getFeaturingArtists().size() > 0) || overwrite)
    		getSongRecord().setFeaturingArtists(submittedSong.getFeaturingArtists());
    	if ((getSongFileLastUpdated() < submittedSong.getSongFilenameLastUpdated()) || overwrite)
    		setSongFileLastUpdated(submittedSong.getSongFilenameLastUpdated());
		for (PlaylistRecord playlist: submittedSong.getPlaylists())
			playlist.addSong(getUniqueId());
    }

    @Override
	public boolean save() {
    	boolean result = super.save();
    	if (RE3Properties.getBoolean("automatically_update_tags") && RapidEvolution3.isLoaded) {
    		if ((getSongFilename() != null) && (getSongFilename().length() > 0)) {
    			File file = new File(getSongFilename());
    			if (file.exists()) {
        			if (log.isDebugEnabled())
        				log.debug("save(): updating tags for filename=" + getSongFilename());
    				TagManager.writeTags(this);
        			if (log.isDebugEnabled())
        				log.debug("save(): done");
    			}
    		}
    	}
    	return result;
    }

    static public Map<Integer, Map<Integer, Float>> computeArtistSimilarityMap(SongProfile song) {
    	Map<Integer, Map<Integer, Float>> result = new HashMap<Integer, Map<Integer, Float>>();
		Vector<ArtistRecord> artistRecords = song.getArtists();
		for (ArtistRecord artistRecord : artistRecords) {
			ArtistProfile artistProfile = Database.getArtistIndex().getArtistProfile(artistRecord.getUniqueId());
			if (artistProfile != null) {
		    	SimilarProfilesModel model = new SimilarProfilesModel(artistProfile, Database.getArtistIndex());
		    	Vector<SearchResult> similarRecords = model.getSimilarRecords();
		    	if (similarRecords.size() > 0) {
			    	Map<Integer, Float> similarArtistMap = new HashMap<Integer, Float>(similarRecords.size());
			    	for (SearchResult similarRecord : similarRecords)
			    		similarArtistMap.put(similarRecord.getRecord().getUniqueId(), similarRecord.getScore());
			    	similarArtistMap.put(artistProfile.getUniqueId(), 1.0f);
			    	result.put(artistProfile.getUniqueId(), similarArtistMap);
		    	}
			}
		}
		return result;
    }

    static public Map<Integer, Map<Integer, Float>> computeFeaturingArtistSimilarityMap(SongProfile song) {
    	Map<Integer, Map<Integer, Float>> result = new HashMap<Integer, Map<Integer, Float>>();
		Vector<ArtistRecord> featuringArtistRecords = song.getSongRecord().getFeaturingArtists();
		for (ArtistRecord artistRecord : featuringArtistRecords) {
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
    	writer.writeLine("3"); // version
    	writer.writeLine(keySource);
    	writer.writeLine(bpmSource);
    	writer.writeLine(timeSigSource);
    	writer.writeLine(beatIntensitySource);
    	writer.writeLine(durationSource);
    	writer.writeLine(originalYearReleasedSource);
    	writer.writeLine(replayGain);
    	writer.writeLine(replayGainSource);
    	writer.writeLine(iTunesID);
    	writer.writeLine(isSyncedWithMixshare);
    	writer.writeLine(mixoutIds.size());
    	for (Integer mixoutId : mixoutIds.keySet())
    		writer.writeLine(mixoutId);
    	writer.writeLine(excludes.size());
    	for (Exclude exclude : excludes)
    		exclude.write(writer);
    	writer.writeLine(lyrics);
    	writer.writeLine(lyricsSource);
    	writer.writeLine(lastDetectKeyAttempt);
    	writer.writeLine(lastDetectBpmAttempt);
    	writer.writeLine(lastDetectBeatIntensityAttempt);
    	writer.writeLine(lastDetectRGAAttempt);
    	writer.writeLine(songFileLastUpdated);
    }

    @Override
	protected Record readRecord(LineReader lineReader) {
    	return new SongRecord(lineReader);
    }

}
