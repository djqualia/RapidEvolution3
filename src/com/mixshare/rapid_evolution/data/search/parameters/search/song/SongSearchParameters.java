package com.mixshare.rapid_evolution.data.search.parameters.search.song;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.index.Index;
import com.mixshare.rapid_evolution.data.mined.lastfm.song.LastfmSongProfile;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.search.artist.ArtistRecord;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.data.search.SearchEncoder;
import com.mixshare.rapid_evolution.data.search.SearchResult;
import com.mixshare.rapid_evolution.data.search.parameters.search.SearchSearchParameters;
import com.mixshare.rapid_evolution.music.beatintensity.BeatIntensity;
import com.mixshare.rapid_evolution.music.beatintensity.BeatIntensityDescription;
import com.mixshare.rapid_evolution.music.bpm.Bpm;
import com.mixshare.rapid_evolution.music.bpm.CombinedBpm;
import com.mixshare.rapid_evolution.music.duration.Duration;
import com.mixshare.rapid_evolution.music.key.CombinedKey;
import com.mixshare.rapid_evolution.music.key.CombinedKeyCode;
import com.mixshare.rapid_evolution.music.key.Key;
import com.mixshare.rapid_evolution.music.key.KeyCode;
import com.mixshare.rapid_evolution.music.key.KeyRelation;
import com.mixshare.rapid_evolution.music.key.SongKeyRelation;
import com.mixshare.rapid_evolution.music.pitch.Cents;
import com.mixshare.rapid_evolution.music.timesig.TimeSig;
import com.mixshare.rapid_evolution.ui.model.column.comparables.SmartString;
import com.mixshare.rapid_evolution.ui.widgets.profile.ProfileWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.profile.search.song.CompatibleSongTableWidget;
import com.mixshare.rapid_evolution.util.FileUtil;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

public class SongSearchParameters extends SearchSearchParameters {

    static private Logger log = Logger.getLogger(SongSearchParameters.class);
    static private final long serialVersionUID = 0L;

    static private final long DATE_PENALIZATION_MAX_DAYS_SINCE_PUBLISHED = 0;
    static private final long DATE_PENALIZATION_THRESHOLD_DAYS = 0;

    ////////////
    // FIELDS //
    ////////////

    private String artistSearchText;
    private Vector<Integer> artistIds; // for exact artist searches
	private String releaseSearchText;
	private Vector<Integer> releaseIds; // for exact release searches
    private String titleSearchText;
    private String labelSearchText;
    private Vector<Integer> labelIds; // for exact label searches
    private Vector<Key> keys;
    private Bpm minBpm;
    private Bpm maxBpm;
    private Duration minDuration;
    private Duration maxDuration;
    private short minYearReleased;
    private short maxYearReleased;
    private boolean searchForCompatible;
    private Vector<Map<Integer, Map<Integer, Float>>> artistSimilarityMap;
	private Vector<Map<Integer, Map<Integer, Float>>> featuringArtistSimilarityMap;
	private boolean penalizeOldPublished;
	private boolean onlyMobileEncodings;
	private Map<String, Object> excludedSongTitles = new HashMap<String, Object>();
	private Map<Integer, Object> excludeArtistIds = new HashMap<Integer, Object>();
	private boolean validFilenamesOnly;
	private boolean hasBrokenFileLink;
	private byte compatibleMatchType;
	private float compatibleBpmRange;
	private float compatibleBpmShift;

	// for "compatible" searches
	transient private Bpm targetBpm;
	transient private Key targetKey;
	transient private TimeSig targetTimeSig;

    static {
    	try{
    		// in order for the XMLEncoder to skip transient variables, this is needed
    		BeanInfo info = Introspector.getBeanInfo(SongSearchParameters.class);
    		PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
    		for (int i = 0; i < propertyDescriptors.length; ++i) {
    			PropertyDescriptor pd = propertyDescriptors[i];
    			if (pd.getName().equals("targetBpm") || pd.getName().equals("targetKey") || pd.getName().equals("targetTimeSig") || pd.getName().equals("relativeProfile")) {
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

    public SongSearchParameters() { }

    public SongSearchParameters(SongSearchParameters copy) {
    	super(copy);
    	this.artistSearchText = copy.artistSearchText;
    	this.artistIds = copy.artistIds;
    	this.labelIds = copy.labelIds;
    	this.releaseIds = copy.releaseIds;
    	this.releaseSearchText = copy.releaseSearchText;
    	this.titleSearchText = copy.titleSearchText;
    	this.labelSearchText = copy.labelSearchText;
    	this.keys = copy.keys;
    	this.minBpm = copy.minBpm;
    	this.maxBpm = copy.maxBpm;
    	this.minDuration = copy.minDuration;
    	this.maxDuration = copy.maxDuration;
    	this.minYearReleased = copy.minYearReleased;
    	this.maxYearReleased = copy.maxYearReleased;
    	this.searchForCompatible = copy.searchForCompatible;
    	this.artistSimilarityMap = copy.artistSimilarityMap;
    	this.featuringArtistSimilarityMap = copy.featuringArtistSimilarityMap;
    	this.penalizeOldPublished = copy.penalizeOldPublished;
    	this.onlyMobileEncodings = copy.onlyMobileEncodings;
    	for (String songTitleKey : copy.excludedSongTitles.keySet())
    		this.excludedSongTitles.put(songTitleKey, null);
    	for (int artistId : copy.excludeArtistIds.keySet())
    		this.excludeArtistIds.put(artistId, null);
    	this.validFilenamesOnly = copy.validFilenamesOnly;
    	this.hasBrokenFileLink = copy.hasBrokenFileLink;
    	this.compatibleMatchType = copy.compatibleMatchType;
    	this.compatibleBpmRange = copy.compatibleBpmRange;
    	this.compatibleBpmShift = copy.compatibleBpmShift;
    }

    public SongSearchParameters(LineReader lineReader) {
    	super(lineReader);
    	int version = Integer.parseInt(lineReader.getNextLine());
    	artistSearchText = lineReader.getNextLine();
    	releaseSearchText = lineReader.getNextLine();
    	titleSearchText = lineReader.getNextLine();
    	labelSearchText = lineReader.getNextLine();
    	int numArtists = Integer.parseInt(lineReader.getNextLine());
    	if (numArtists > 0) {
    		artistIds = new Vector<Integer>(numArtists);
    		for (int i = 0; i < numArtists; ++i)
    			artistIds.add(Integer.parseInt(lineReader.getNextLine()));
    	}
    	if (version >= 5) {
    		int numLabels = Integer.parseInt(lineReader.getNextLine());
    		labelIds = new Vector<Integer>(numLabels);
    		for (int i = 0; i < numLabels; ++i)
    			labelIds.add(Integer.parseInt(lineReader.getNextLine()));
    		int numReleases = Integer.parseInt(lineReader.getNextLine());
    		releaseIds = new Vector<Integer>(numReleases);
    		for (int i = 0; i < numReleases; ++i)
    			releaseIds.add(Integer.parseInt(lineReader.getNextLine()));
    	}
    	int numKeys = Integer.parseInt(lineReader.getNextLine());
    	if (numKeys > 0) {
    		keys = new Vector<Key>(numKeys);
    		for (int i = 0; i < numKeys; ++i)
    			keys.add(Key.getKey(lineReader.getNextLine()));
    	}
    	float minBpmValue = Float.parseFloat(lineReader.getNextLine());
    	if (minBpmValue != 0.0f)
    		minBpm = new Bpm(minBpmValue);
    	float maxBpmValue = Float.parseFloat(lineReader.getNextLine());
    	if (maxBpmValue != 0.0f)
    		maxBpm = new Bpm(maxBpmValue);
    	int minDurationValue = Integer.parseInt(lineReader.getNextLine());
    	int maxDurationValue = Integer.parseInt(lineReader.getNextLine());
    	if (minDurationValue != 0)
    		minDuration = new Duration(minDurationValue);
    	if (maxDurationValue != Integer.MAX_VALUE)
    		maxDuration = new Duration(maxDurationValue);
    	minYearReleased = Short.parseShort(lineReader.getNextLine());
    	maxYearReleased = Short.parseShort(lineReader.getNextLine());
    	searchForCompatible = Boolean.parseBoolean(lineReader.getNextLine());
    	if (version >= 2)
    		penalizeOldPublished = Boolean.parseBoolean(lineReader.getNextLine());
    	if (version >= 3)
    		onlyMobileEncodings = Boolean.parseBoolean(lineReader.getNextLine());
    	if (version >= 4) {
    		int numExcludeSongTitles = Integer.parseInt(lineReader.getNextLine());
    		for (int i = 0; i < numExcludeSongTitles; ++i)
    			excludedSongTitles.put(lineReader.getNextLine(), 0);
    		int numExcludeArtistIds = Integer.parseInt(lineReader.getNextLine());
    		for (int i = 0; i < numExcludeArtistIds; ++i)
    			excludeArtistIds.put(Integer.parseInt(lineReader.getNextLine()), 0);
    		validFilenamesOnly = Boolean.parseBoolean(lineReader.getNextLine());
    	}
    	if (version >= 6) {
    		hasBrokenFileLink = Boolean.parseBoolean(lineReader.getNextLine());
    	}
    	if (version >= 7) {
    		compatibleMatchType = Byte.parseByte(lineReader.getNextLine());
    		compatibleBpmRange = Float.parseFloat(lineReader.getNextLine());
    	}
    	if (version >= 8) {
    		compatibleBpmShift = Float.parseFloat(lineReader.getNextLine());
    	}
    	// TODO: artist similarity maps
    }

    /////////////
    // GETTERS //
    /////////////

    @Override
	public byte getDataType() { return DATA_TYPE_SONGS; }

    @Override
	public String getUniqueHash() {
    	StringBuffer result = new StringBuffer();
    	result.append(super.getUniqueHash());
    	// insert class specific parameters here...
    	result.append(queryKeySeperator);
    	result.append(artistSearchText != null ? artistSearchText : "");
    	result.append(queryKeySeperator);
    	result.append(artistIds);
    	result.append(queryKeySeperator);
    	result.append(labelIds);
    	result.append(queryKeySeperator);
    	result.append(releaseIds);
    	result.append(queryKeySeperator);
    	result.append(releaseSearchText != null ? releaseSearchText : "");
    	result.append(queryKeySeperator);
    	result.append(titleSearchText != null ? titleSearchText : "");
    	result.append(queryKeySeperator);
    	result.append(labelSearchText != null ? labelSearchText : "");
    	result.append(queryKeySeperator);
    	result.append(keys != null ? keys.toString() : "");
    	result.append(queryKeySeperator);
    	result.append(minBpm != null ? minBpm.toString() : "");
    	result.append(queryKeySeperator);
    	result.append(maxBpm != null ? maxBpm.toString() : "");
    	result.append(queryKeySeperator);
    	result.append(minDuration != null ? minDuration.toString() : "");
    	result.append(queryKeySeperator);
    	result.append(maxDuration != null ? maxDuration.toString() : "");
    	result.append(queryKeySeperator);
    	result.append(String.valueOf(minYearReleased));
    	result.append(queryKeySeperator);
    	result.append(String.valueOf(maxYearReleased));
    	result.append(queryKeySeperator);
    	result.append(String.valueOf(searchForCompatible));
    	result.append(queryKeySeperator);
    	result.append(String.valueOf(penalizeOldPublished));
    	result.append(queryKeySeperator);
    	result.append(String.valueOf(onlyMobileEncodings));
    	result.append(queryKeySeperator);
    	result.append(String.valueOf(excludedSongTitles));
    	result.append(queryKeySeperator);
    	result.append(String.valueOf(excludeArtistIds));
    	result.append(queryKeySeperator);
    	result.append(String.valueOf(validFilenamesOnly));
    	result.append(queryKeySeperator);
    	result.append(String.valueOf(hasBrokenFileLink));
    	result.append(queryKeySeperator);
    	result.append(String.valueOf(compatibleMatchType));
    	result.append(queryKeySeperator);
    	result.append(String.valueOf(compatibleBpmRange));
    	result.append(queryKeySeperator);
    	result.append(String.valueOf(compatibleBpmShift));
    	if ((relativeProfileIds != null) && (relativeProfileIds.length > 0)) {
    		result.append(queryKeySeperator);
    		result.append((targetKey != null) ? targetKey.toString() : "");
    		result.append("-");
    		result.append((targetBpm != null) ? targetBpm.toString() : "");
    	}
    	return result.toString();
    }

	public String getArtistSearchText() { return artistSearchText; }
    public Vector<Integer> getArtistIds() { return artistIds; }
	public String getReleaseSearchText() { return releaseSearchText; }
    public Vector<Integer> getReleaseIds() { return releaseIds; }
	public String getTitleSearchText() { return titleSearchText; }
	public String getLabelSearchText() { return labelSearchText; }
    public Vector<Integer> getLabelIds() { return labelIds; }
	public Vector<Key> getKeys() { return keys; }
	public Bpm getMinBpm() { return minBpm; }
	public Bpm getMaxBpm() { return maxBpm; }
	public Duration getMinDuration() { return minDuration; }
	public Duration getMaxDuration() { return maxDuration; }
	public short getMinYearReleased() { return minYearReleased; }
	public short getMaxYearReleased() { return maxYearReleased; }
    public boolean isSearchForCompatible() { return searchForCompatible; }
    public SongProfile getPrimaryRelativeSongProfile() {
    	Vector<SearchProfile> searchProfiles = fetchRelativeProfiles();
    	if ((searchProfiles != null) && (searchProfiles.size() > 0))
    		return (SongProfile)searchProfiles.get(0);
    	return null;
    }
	public boolean isPenalizeOldPublished() { return penalizeOldPublished; }
	public boolean isOnlyMobileEncodings() { return onlyMobileEncodings; }
	public Map<String, Object> getExcludedSongTitles() { return excludedSongTitles; }
	public Map<Integer, Object> getExcludeArtistIds() { return excludeArtistIds; }
	public boolean isValidFilenamesOnly() { return validFilenamesOnly; }
	public boolean isHasBrokenFileLink() { return hasBrokenFileLink; }
	public byte getCompatibleMatchType() { return compatibleMatchType; }
	public float getCompatibleBpmRange() { return compatibleBpmRange; }
	public float getCompatibleBpmShift() { return compatibleBpmShift; }

    @Override
	public Vector<SearchProfile> fetchRelativeProfiles() {
    	if (relativeProfiles == null) {
	    	Vector<SearchProfile> results = super.fetchRelativeProfiles();
	    	if ((results != null) && (results.size() > 0)) {
	    		int i = 0;
	    		for (SearchProfile result : results) {
	    			SongProfile song = ((SongProfile)result);
		    		if ((song.getArtistSimilarityMap() == null) && (artistSimilarityMap != null))
		    			song.setArtistSimilarityMap(artistSimilarityMap.get(i));
		    		if ((song.getFeaturingArtistSimilarityMap() == null) && (featuringArtistSimilarityMap != null))
		    			song.setFeaturingArtistSimilarityMap(featuringArtistSimilarityMap.get(i));
		    		++i;
	    		}
	    	}
	    	return results;
    	}
    	return relativeProfiles;
    }

	public Vector<Map<Integer, Map<Integer, Float>>> getArtistSimilarityMap() { return artistSimilarityMap; }
	public Vector<Map<Integer, Map<Integer, Float>>> getFeaturingArtistSimilarityMap() { return featuringArtistSimilarityMap; }

	/////////////
	// SETTERS //
	/////////////

	public void setArtistSearchText(String artistSearchText) { this.artistSearchText = artistSearchText; }
	public void setArtistId(int artistId) {
		this.artistIds = new Vector<Integer>(1);
		this.artistIds.add(artistId);
	}
	public void setArtistIds(Vector<Integer> artistIds) { this.artistIds = artistIds; }

	public void setReleaseSearchText(String releaseSearchText) { this.releaseSearchText = releaseSearchText; }
	public void setReleaseId(int releaseId) {
		this.releaseIds = new Vector<Integer>(1);
		this.releaseIds.add(releaseId);
	}
	public void setReleaseIds(Vector<Integer> releaseIds) { this.releaseIds = releaseIds; }

	public void setTitleSearchText(String titleSearchText) { this.titleSearchText = titleSearchText; }

	public void setLabelSearchText(String labelSearchText) { this.labelSearchText = labelSearchText; }
	public void setLabelId(int labelId) {
		this.labelIds = new Vector<Integer>(1);
		this.labelIds.add(labelId);
	}
	public void setLabelIds(Vector<Integer> labelIds) { this.labelIds = labelIds; }

	public void setKeys(Vector<Key> keys) { this.keys = keys; }
	public void setMinBpm(Bpm minBpm) { this.minBpm = minBpm; }
	public void setMaxBpm(Bpm maxBpm) { this.maxBpm = maxBpm; }
	public void setMinDuration(Duration minDuration) { this.minDuration = minDuration; }
	public void setMaxDuration(Duration maxDuration) { this.maxDuration = maxDuration; }
	public void setMinYearReleased(short minYearReleased) { this.minYearReleased = minYearReleased; }
	public void setMaxYearReleased(short maxYearReleased) { this.maxYearReleased = maxYearReleased; }
	public void setSearchForCompatible(boolean searchForCompatible) { this.searchForCompatible = searchForCompatible; }
	public void setPenalizeOldPublished(boolean penalizeOldPublished) { this.penalizeOldPublished = penalizeOldPublished; }
	public void setOnlyMobileEncodings(boolean onlyMobileEncodings) { this.onlyMobileEncodings = onlyMobileEncodings; }
	public void setExcludedSongTitles(Map<String, Object> excludedSongTitles) { this.excludedSongTitles = excludedSongTitles; }
	public void setExcludeSongTitles(Set<String> titleKeys) {
		for (String titleKey : titleKeys)
			excludedSongTitles.put(titleKey, null);
	}
	public void clearExcludedArtistIds() { excludeArtistIds.clear(); };
	public void setExcludeArtistIds(Map<Integer, Object> excludeArtistIds) { this.excludeArtistIds = excludeArtistIds; }
	public void setExcludeArtistIds(Vector<Integer> artistIds) {
		for (int artistId : artistIds)
			excludeArtistIds.put(artistId, null);
	}
	public void setValidFilenamesOnly(boolean validFilenamesOnly) { this.validFilenamesOnly = validFilenamesOnly; }

	public void setArtistSimilarityMap(Vector<Map<Integer, Map<Integer, Float>>> artistSimilarityMap) { this.artistSimilarityMap = artistSimilarityMap; }
	public void setFeaturingArtistSimilarityMap(Vector<Map<Integer, Map<Integer, Float>>> featuringArtistSimilarityMap) { this.featuringArtistSimilarityMap = featuringArtistSimilarityMap; }
	public void setHasBrokenFileLink(boolean hasBrokenFileLink) { this.hasBrokenFileLink = hasBrokenFileLink; }
	public void setCompatibleMatchType(byte value) { compatibleMatchType = value; }
	public void setCompatibleBpmRange(float range) { this.compatibleBpmRange = range; }
	public void setCompatibleBpmShift(float range) { this.compatibleBpmShift = range; }

	@Override
	public void initRelativeProfile(SearchProfile relativeProfile) {
		initRelativeProfile(relativeProfile, null, null);
	}
	public void initRelativeProfile(SearchProfile relativeProfile, Bpm relativeBpm, Key relativeKey) {
		super.initRelativeProfile(relativeProfile);
		if (relativeBpm == null)
			relativeBpm = ((SongProfile)relativeProfile).getBpmEnd();
		if (!relativeBpm.isValid())
			relativeBpm = ((SongProfile)relativeProfile).getBpmStart();
		if (relativeKey == null)
			relativeKey = ((SongProfile)relativeProfile).getEndKey();
		if (!relativeKey.isValid())
			relativeKey = ((SongProfile)relativeProfile).getStartKey();
		targetBpm = relativeBpm;
		targetKey = relativeKey;
		SongProfile relativeSong = (SongProfile)relativeProfile;
		artistSimilarityMap = new Vector<Map<Integer, Map<Integer, Float>>>(1);
		featuringArtistSimilarityMap = new Vector<Map<Integer, Map<Integer, Float>>>(1);
		artistSimilarityMap.add(SongProfile.computeArtistSimilarityMap(relativeSong));
		featuringArtistSimilarityMap.add(SongProfile.computeFeaturingArtistSimilarityMap(relativeSong));
	}
	@Override
	public void initRelativeProfiles(Vector<SearchProfile> relativeProfiles) {
		super.initRelativeProfiles(relativeProfiles);
		artistSimilarityMap = new Vector<Map<Integer, Map<Integer, Float>>>(relativeProfiles.size());
		featuringArtistSimilarityMap = new Vector<Map<Integer, Map<Integer, Float>>>(relativeProfiles.size());
		for (SearchProfile searchProfile : relativeProfiles) {
			SongProfile relativeSong = (SongProfile)searchProfile;
			artistSimilarityMap.add(SongProfile.computeArtistSimilarityMap(relativeSong));
			featuringArtistSimilarityMap.add(SongProfile.computeFeaturingArtistSimilarityMap(relativeSong));
		}
	}

	public void addArtist(ArtistRecord artist) {
		if (artistIds == null)
			artistIds = new Vector<Integer>();
		artistIds.add(artist.getUniqueId());
	}

	/////////////
	// METHODS //
	/////////////

	@Override
	public float matchesSub(Record record, boolean fullCheck) {
		float superScore = super.matchesSub(record, fullCheck);
		if (superScore > 0.0f) {
			SongRecord songRecord = (SongRecord)record;
			try {
				if ((artistIds != null) && (artistIds.size() > 0)) {
					boolean match = false;
					for (int artistId : artistIds) {
						for (int songArtistId : songRecord.getArtistIds()) {
							if (artistId == songArtistId) {
								match = true;
								break;
							}
						}
						if (!match) {
							int[] featArtistIds = songRecord.getFeaturingArtistIds();
							if (featArtistIds != null) {
								for (int featArtistId : featArtistIds) {
									if (featArtistId == artistId) {
										match = true;
										break;
									}
								}
							}
						}
						if (match)
							break;
					}
					if (!match)
						return 0.0f;
				}
				if ((labelIds != null) && (labelIds.size() > 0)) {
					boolean match = false;
					for (int labelId : labelIds) {
						for (int songLabelId : songRecord.getLabelIds()) {
							if (labelId == songLabelId) {
								match = true;
								break;
							}
						}
						if (match)
							break;
					}
					if (!match)
						return 0.0f;
				}
				if ((releaseIds != null) && (releaseIds.size() > 0)) {
					boolean match = false;
					for (int releaseId : releaseIds) {
						for (int songReleaseId : songRecord.getReleaseIds()) {
							if (releaseId == songReleaseId) {
								match = true;
								break;
							}
						}
						if (match)
							break;
					}
					if (!match)
						return 0.0f;
				}
				if (excludedSongTitles.size() > 0) {
					String titleKey = getLogicalSongKey(songRecord);
					if (excludedSongTitles.containsKey(titleKey))
						return 0.0f;
				}
				if (excludeArtistIds.size() > 0) {
					for (int artistId : songRecord.getArtistIds())
						if (excludeArtistIds.containsKey(artistId))
							return 0.0f;
				}
				if (validFilenamesOnly) {
					if (!songRecord.hasValidSongFilename())
						return 0.0f;
				}
				if (hasBrokenFileLink) {
					if (songRecord.hasValidSongFilename())
						return 0.0f;
				}

				if (fullCheck) {
					if ((getArtistSearchText() != null) && (getArtistSearchText().length() > 0)) {
						float match = getSearchFieldsMatch(record, new String[] { "artist", "featuring"}, getArtistSearchText());
						if (match == 0.0f)
							return 0.0f;
						superScore += match;
					}
					if ((getLabelSearchText() != null) && (getLabelSearchText().length() > 0)) {
						float match = getSearchFieldsMatch(record, new String[] { "label" }, getLabelSearchText());
						if (match == 0.0f)
							return 0.0f;
						superScore += match;
					}
					if ((getReleaseSearchText() != null) && (getReleaseSearchText().length() > 0)) {
						float match = getSearchFieldsMatch(record, new String[] { "release", "release_instances"}, getReleaseSearchText());
						if (match == 0.0f)
							return 0.0f;
						superScore += match;
					}
					if ((getTitleSearchText() != null) && (getTitleSearchText().length() > 0)) {
						float match = getSearchFieldsMatch(record, new String[] { "title", "remix"}, getTitleSearchText());
						if (match == 0.0f)
							return 0.0f;
						superScore += match;
					}
				}

				if (getMinBpm() != null) {
					if (!songRecord.getBpmStart().isValid())
						return 0.0f;
					if (songRecord.getBpmStart().getBpmValue() < getMinBpm().getBpmValue())
						return 0.0f;
				}
				if (getMaxBpm() != null) {
					if (!songRecord.getBpmStart().isValid())
						return 0.0f;
					if (songRecord.getBpmStart().getBpmValue() > getMaxBpm().getBpmValue())
						return 0.0f;
				}
				if ((getMinBeatIntensity() != null) && (getMinBeatIntensity().getBeatIntensityValue() > 0)) {
					if (!songRecord.getBeatIntensityValue().isValid())
						return 0.0f;
					if (songRecord.getBeatIntensity() <= getMinBeatIntensity().getBeatIntensityValue())
						return 0.0f;
				}
				if ((getMaxBeatIntensity() != null) && (getMaxBeatIntensity().getBeatIntensityValue() < 100)) {
					if (!songRecord.getBeatIntensityValue().isValid())
						return 0.0f;
					if (songRecord.getBeatIntensity() > getMaxBeatIntensity().getBeatIntensityValue())
						return 0.0f;
				}
				if (getKeys() != null) {
					boolean match = false;
					for (Key key : getKeys()) {
						if (songRecord.getStartKey().equals(key)) {
							match = true;
							break;
						}
					}
					if (!match)
						return 0.0f;
				}
				if (getMinDuration() != null) {
					if (!songRecord.getDuration().isValid())
						return 0.0f;
					if (songRecord.getDuration().getDurationInMillis() < getMinDuration().getDurationInMillis())
						return 0.0f;
				}
				if (getMaxDuration() != null) {
					if (!songRecord.getDuration().isValid())
						return 0.0f;
					if (songRecord.getDuration().getDurationInMillis() > getMaxDuration().getDurationInMillis())
						return 0.0f;
				}
				if (getMinYearReleased() != (short)0) {
					if (songRecord.getOriginalYearReleased() == (short)0)
						return 0.0f;
					if (songRecord.getOriginalYearReleased() < getMinYearReleased())
						return 0.0f;
				}
				if (getMaxYearReleased() != (short)0) {
					if (songRecord.getOriginalYearReleased() == (short)0)
						return 0.0f;
					if (songRecord.getOriginalYearReleased() > getMaxYearReleased())
						return 0.0f;
				}
				if (onlyMobileEncodings) {
					Boolean mobileEncoding = (Boolean)songRecord.getUserData(Database.getSongIndex().getUserDataType("Mobile"));
					boolean isMobile = false;
					if ((mobileEncoding != null) && mobileEncoding.booleanValue())
						isMobile = true;
					if (!isMobile)
						return 0.0f;
				}
				if (searchForCompatible) {
					if ((targetBpm == null) && (getPrimaryRelativeSongProfile() != null)) {
						targetBpm = getPrimaryRelativeSongProfile().getBpmEnd();
						if (!targetBpm.isValid())
							targetBpm = getPrimaryRelativeSongProfile().getBpmStart();
					}
					if ((targetBpm == null) || !targetBpm.isValid()) {
						if (ProfileWidgetUI.instance != null) {
							Record currentRecord = ProfileWidgetUI.instance.getCurrentRecord();
							if (currentRecord instanceof SongRecord) {
								SongRecord currentSong = (SongRecord)currentRecord;
								targetBpm = currentSong.getBpmEnd();
								if (!targetBpm.isValid())
									targetBpm = currentSong.getBpmStart();
							}
						}
					}
					if ((targetKey == null) && (getPrimaryRelativeSongProfile() != null)) {
						targetKey = getPrimaryRelativeSongProfile().getEndKey();
						if (!targetKey.isValid())
							targetKey = getPrimaryRelativeSongProfile().getStartKey();
					}
					if ((targetKey == null) || !targetKey.isValid()) {
						if (ProfileWidgetUI.instance != null) {
							Record currentRecord = ProfileWidgetUI.instance.getCurrentRecord();
							if (currentRecord instanceof SongRecord) {
								SongRecord currentSong = (SongRecord)currentRecord;
								targetKey = currentSong.getEndKey();
								if (!targetKey.isValid())
									targetKey = currentSong.getStartKey();
							}
						}
					}
					if ((targetTimeSig == null) && (getPrimaryRelativeSongProfile() != null)) {
						targetTimeSig = getPrimaryRelativeSongProfile().getTimeSig();
					}
					if ((targetTimeSig == null) || !targetTimeSig.isValid()) {
						if (ProfileWidgetUI.instance != null) {
							Record currentRecord = ProfileWidgetUI.instance.getCurrentRecord();
							if (currentRecord instanceof SongRecord) {
								SongRecord currentSong = (SongRecord)currentRecord;
								targetTimeSig = currentSong.getTimeSig();
							}
						}
					}
					if (compatibleBpmRange == 0.0) {
						if (ProfileWidgetUI.instance != null)
							compatibleBpmRange = ProfileWidgetUI.instance.getStageWidget().getCurrentBpmRange();
					}
					boolean isCompatible = false;
					if (songRecord.getBpmStart().isValid()) {
						// check bpm diff
						float bpmDifference = songRecord.getBpmStart().getDifference(targetBpm);
						if (Math.abs(bpmDifference) <= compatibleBpmRange) {
							// check time sig.
							if (targetTimeSig.isCompatibleWith(songRecord.getTimeSig())) {
								if (compatibleMatchType == CompatibleSongTableWidget.COMPATIBLE_TYPE_BPM_ONLY)
									isCompatible = true;
								else {
									if (songRecord.getStartKey().isValid()) {
										// check key
										SongKeyRelation keyRelation = SongKeyRelation.getSongKeyRelation(songRecord, targetBpm, targetKey);
										if (keyRelation.isCompatible()) {
											// make sure isn't already a mixout
											//boolean isMixout = false;
											//for (MixoutProfile mixout : getPrimaryRelativeSongProfile().getMixouts()) {
												//if (mixout.getToSongUniqueId() == record.getUniqueId()) {
													//isMixout = true;
													//break;
												//}
											//}
											//if (!isMixout)
												//isCompatible = true;
											if (compatibleMatchType == CompatibleSongTableWidget.COMPATIBLE_TYPE_ALL_HARMONIC)
												isCompatible = true;
											else if (compatibleMatchType == CompatibleSongTableWidget.COMPATIBLE_TYPE_KEYLOCK_ONLY)
												isCompatible = keyRelation.getRelationWithKeylock().isCompatible();
											else if (compatibleMatchType == CompatibleSongTableWidget.COMPATIBLE_TYPE_NO_KEYLOCK)
												isCompatible = keyRelation.getRelationWithoutKeylock().isCompatible();
										}
									}
								}
							}
						}
					}
					if (!isCompatible)
						return 0.0f;
				}
				return superScore;
			} catch (Exception e) {
				log.error("matchesSub(): error matching=" + songRecord.getUniqueId(), e);
				return 0.0f;
			}
		} else {
			return 0.0f;
		}
	}

	@Override
	public float computeWeight(Record record) {
		float superWeight = super.computeWeight(record);
		SongRecord songRecord = (SongRecord)record;
		if (penalizeOldPublished) {
			Object pubDateObj = songRecord.getUserData(Database.getSongIndex().getUserDataType("Published Date"));
			long pubDate = 0;
			if (pubDateObj != null)
				pubDate = Long.parseLong((String)pubDateObj);
			long daysSincePublished = (System.currentTimeMillis() - pubDate) / 1000 / 60 / 60 / 24;
			if (daysSincePublished > DATE_PENALIZATION_MAX_DAYS_SINCE_PUBLISHED)
				daysSincePublished = DATE_PENALIZATION_MAX_DAYS_SINCE_PUBLISHED;
			float scale = (float)(1.0f / Math.exp(daysSincePublished / DATE_PENALIZATION_THRESHOLD_DAYS));
			superWeight *= scale;
		}
		return superWeight;
	}

	@Override
	public boolean isEmpty(boolean countIndexed) {
		if (!super.isEmpty(countIndexed))
			return false;
		if (countIndexed) {
			if ((artistSearchText != null) && (artistSearchText.length() > 0))
				return false;
			if ((labelSearchText != null) && (labelSearchText.length() > 0))
				return false;
			if ((titleSearchText != null) && (titleSearchText.length() > 0))
				return false;
			if ((releaseSearchText != null) && (releaseSearchText.length() > 0))
				return false;
		}
		if ((artistIds != null) && (artistIds.size() > 0))
			return false;
		if ((labelIds != null) && (labelIds.size() > 0))
			return false;
		if ((releaseIds != null) && (releaseIds.size() > 0))
			return false;
		if ((keys != null) && (keys.size() > 0))
			return false;
		if (minYearReleased != (short)0)
			return false;
		if (maxYearReleased != (short)0)
			return false;
		if ((minBpm != null) && (minBpm.getBpmValue() > 0.0f))
			return false;
		if ((maxBpm != null) && (maxBpm.getBpmValue() > 0.0f))
			return false;
		if ((minDuration != null) && (minDuration.getDurationInMillis() > 0))
			return false;
		if ((maxDuration != null) && (maxDuration.getDurationInMillis() > 0))
			return false;
		if (searchForCompatible)
			return false;
		if (penalizeOldPublished)
			return false;
		if (onlyMobileEncodings)
			return false;
		if (excludedSongTitles.size() > 0)
			return false;
		if (excludeArtistIds.size() > 0)
			return false;
		if (validFilenamesOnly)
			return false;
		if (hasBrokenFileLink)
			return false;
		return true;
	}

	@Override
	protected int compareSub(SearchResult r1, SearchResult r2, byte sortType) {
		SongRecord s1 = (SongRecord)r1.getRecord();
		SongRecord s2 = (SongRecord)r2.getRecord();
		if (sortType == SORT_BY_DURATION) {
			Duration lastMinedS1 = s1.getDuration();
			Duration lastMinedS2 = s2.getDuration();
			return lastMinedS1.compareTo(lastMinedS2);
		}
		if (sortType == SORT_BY_YEAR) {
			short y1 = s1.getOriginalYearReleased();
			short y2 = s2.getOriginalYearReleased();
			if ((y1 > 0) && (y2 > 0)) {
				if (y1 > y2)
					return -1;
				if (y2 > y1)
					return 1;
				return 0;
			}
			if (y1 > 0)
				return -1;
			if (y2 > 0)
				return 1;
			return 0;
		}
		if (sortType == SORT_BY_TRACK) {
			String t1 = s1.getTrack();
			String t2 = s2.getTrack();
			return SmartString.compareStrings(t1, t2);
		}
		if (sortType == SORT_BY_TITLE) {
			String t1 = s1.getTitle();
			String t2 = s2.getTitle();
			return SmartString.compareStrings(t1, t2);
		}
		if (sortType == SORT_BY_REMIX) {
			String t1 = s1.getRemix();
			String t2 = s2.getRemix();
			return SmartString.compareStrings(t1, t2);
		}
		if (sortType == SORT_BY_TIME_SIGNATURE) {
			TimeSig t1 = s1.getTimeSig();
			TimeSig t2 = s2.getTimeSig();
			return t1.compareTo(t2);
		}
		if (sortType == SORT_BY_NUM_MIXOUTS) {
			short a1 = s1.getNumMixouts();
			short a2 = s2.getNumMixouts();
			if (a1 > a2)
				return -1;
			if (a1 < a2)
				return 1;
			return 0;
		}
		if (sortType == SORT_BY_KEY_ACCURACY) {
			byte a1 = s1.getKeyAccuracy();
			byte a2 = s2.getKeyAccuracy();
			if (a1 > a2)
				return -1;
			if (a1 < a2)
				return 1;
			return 0;
		}
		if (sortType == SORT_BY_BPM_ACCURACY) {
			byte a1 = s1.getBpmAccuracy();
			byte a2 = s2.getBpmAccuracy();
			if (a1 > a2)
				return -1;
			if (a1 < a2)
				return 1;
			return 0;
		}
		if (sortType == SORT_BY_LASTFM_REACH) {
			SongProfile a1 = Database.getSongIndex().getSongProfile(s1.getUniqueId());
			SongProfile a2 = Database.getSongIndex().getSongProfile(s2.getUniqueId());
			float v1 = 0.0f;
			float v2 = 0.0f;
			if (a1 != null) {
				LastfmSongProfile lastfmProfile = (LastfmSongProfile)a1.getMinedProfile(DATA_SOURCE_LASTFM);
				if (lastfmProfile != null)
					v1 = lastfmProfile.getNumListeners();
			}
			if (a2 != null) {
				LastfmSongProfile lastfmProfile = (LastfmSongProfile)a2.getMinedProfile(DATA_SOURCE_LASTFM);
				if (lastfmProfile != null)
					v2 = lastfmProfile.getNumListeners();
			}
			if (v1 > v2)
				return -1;
			if (v1 < v2)
				return 1;
			return 0;
		}
		if (sortType == SORT_BY_BPM) {
			CombinedBpm bpm1 = s1.getBpm();
			CombinedBpm bpm2 = s2.getBpm();
			return bpm1.compareTo(bpm2);
		}
		if (sortType == SORT_BY_BPM_START) {
			Bpm bpm1 = s1.getBpmStart();
			Bpm bpm2 = s2.getBpmStart();
			return bpm1.compareTo(bpm2);
		}
		if (sortType == SORT_BY_BPM_END) {
			Bpm bpm1 = s1.getBpmEnd();
			Bpm bpm2 = s2.getBpmEnd();
			return bpm1.compareTo(bpm2);
		}
		if (sortType == SORT_BY_KEY) {
			CombinedKey key1 = s1.getKey();
			CombinedKey key2 = s2.getKey();
			return key1.compareTo(key2);
		}
		if (sortType == SORT_BY_KEY_START) {
			Key key1 = s1.getStartKey();
			Key key2 = s2.getStartKey();
			return key1.compareTo(key2);
		}
		if (sortType == SORT_BY_KEY_END) {
			Key key1 = s1.getEndKey();
			Key key2 = s2.getEndKey();
			return key1.compareTo(key2);
		}
		if (sortType == SORT_BY_KEYCODE) {
			CombinedKeyCode keyCode1 = s1.getKeyCode();
			CombinedKeyCode keyCode2 = s2.getKeyCode();
			return keyCode1.compareTo(keyCode2);
		}
		if (sortType == SORT_BY_KEYCODE_START) {
			KeyCode keyCode1 = s1.getStartKeyCode();
			KeyCode keyCode2 = s2.getStartKeyCode();
			return keyCode1.compareTo(keyCode2);
		}
		if (sortType == SORT_BY_KEYCODE_END) {
			KeyCode keyCode1 = s1.getEndKeyCode();
			KeyCode keyCode2 = s2.getEndKeyCode();
			return keyCode1.compareTo(keyCode2);
		}
		if (sortType == SORT_BY_BEAT_INTENSITY_DESCRIPTION) {
			BeatIntensityDescription bi1 = s1.getBeatIntensityDescription();
			BeatIntensityDescription bi2 = s2.getBeatIntensityDescription();
			return bi1.compareTo(bi2);
		}
		if (sortType == SORT_BY_BEAT_INTENSITY_VALUE) {
			BeatIntensity bi1 = s1.getBeatIntensityValue();
			BeatIntensity bi2 = s2.getBeatIntensityValue();
			return bi1.compareTo(bi2);
		}
		if (sortType == SORT_BY_ARTIST_DESCRIPTION) {
			String c1 = s1.getArtistsDescription();
			String c2 = s2.getArtistsDescription();
			return SmartString.compareStrings(c1, c2);
		}
		if (sortType == SORT_BY_LABEL_DESCRIPTION) {
			String c1 = s1.getLabelsDescription();
			String c2 = s2.getLabelsDescription();
			return SmartString.compareStrings(c1, c2);
		}
		if (sortType == SORT_BY_FEATURING_ARTISTS) {
			String c1 = s1.getFeaturingArtistsDescription();
			String c2 = s2.getFeaturingArtistsDescription();
			return SmartString.compareStrings(c1, c2);
		}
		if (sortType == SORT_BY_RELEASE_TITLES) {
			SmartString c1 = new SmartString(s1.getReleases());
			SmartString c2 = new SmartString(s2.getReleases());
			return c1.compareTo(c2);
		}
		if (sortType == SORT_BY_RELEASE_TITLE) {
			return SmartString.compareStrings(s1.getReleaseTitle(), s2.getReleaseTitle());
		}
		if (sortType == SORT_BY_FILENAME) {
			String c1 = FileUtil.getFilenameMinusDirectory(s1.getSongFilename());
			String c2 = FileUtil.getFilenameMinusDirectory(s2.getSongFilename());
			return SmartString.compareStrings(c1, c2);
		}
		if (sortType == SORT_BY_FILEPATH) {
			String c1 = s1.getSongFilename();
			String c2 = s2.getSongFilename();
			return SmartString.compareStrings(c1, c2);
		}
		if ((sortType == SORT_BY_BPM_PERCENT_DIFF) || (sortType == SORT_BY_BPM_PERCENT_SHIFT)) {
			SongProfile relativeProfile = getPrimaryRelativeSongProfile();
			if (relativeProfile != null) {
				float targetBpm = relativeProfile.getEndBpm();
				if (targetBpm == 0.0f)
					targetBpm = relativeProfile.getStartBpm();
				if (targetBpm != 0.0f) {
					float diff1 = Float.NaN;
					if (s1.getStartBpm() > 0.0f)
						 diff1 = Bpm.getBpmDifference(s1.getStartBpm(), targetBpm);
					float diff2 = Float.NaN;
					if (s2.getStartBpm() > 0.0f)
						diff2 = Bpm.getBpmDifference(s2.getStartBpm(), targetBpm);
					if (!Float.isNaN(diff1) && !Float.isNaN(diff2)) {
						if (diff1 < diff2)
							return -1;
						if (diff1 > diff2)
							return 1;
						return 0;
					} else if (Float.isNaN(diff2)) {
						return -1;
					} else if (Float.isNaN(diff1)) {
						return 1;
					}
				}
			}
			return 0;
		}
		if (sortType == SORT_BY_KEY_RELATION) {
			SongProfile relativeProfile = getPrimaryRelativeSongProfile();
			if (relativeProfile != null) {
				float targetBpm = relativeProfile.getEndBpm();
				if (targetBpm == 0.0f)
					targetBpm = relativeProfile.getStartBpm();
				if (targetBpm != 0.0f) {
					Key targetKey = relativeProfile.getEndKey();
					if (!targetKey.isValid())
						targetKey = relativeProfile.getStartKey();
					if (targetKey.isValid()) {
						SongKeyRelation relation = SongKeyRelation.getSongKeyRelation(s1, new Bpm(targetBpm), targetKey);
						KeyRelation bestRelation = relation.getBestKeyRelation();
						SongKeyRelation relation2 = SongKeyRelation.getSongKeyRelation(s2, new Bpm(targetBpm), targetKey);
						KeyRelation bestRelation2 = relation2.getBestKeyRelation();
						return bestRelation.compareTo(bestRelation2);
					}
				}
			}
			return 0;
		}
		if (sortType == SORT_BY_KEY_RELATION) {
			SongProfile relativeProfile = getPrimaryRelativeSongProfile();
			if (relativeProfile != null) {
				float targetBpm = relativeProfile.getEndBpm();
				if (targetBpm == 0.0f)
					targetBpm = relativeProfile.getStartBpm();
				if (targetBpm != 0.0f) {
					Key targetKey = relativeProfile.getEndKey();
					if (!targetKey.isValid())
						targetKey = relativeProfile.getStartKey();
					if (targetKey.isValid()) {
						SongKeyRelation relation = SongKeyRelation.getSongKeyRelation(s1, new Bpm(targetBpm), targetKey);
						KeyRelation bestRelation = relation.getBestKeyRelation();
						SongKeyRelation relation2 = SongKeyRelation.getSongKeyRelation(s2, new Bpm(targetBpm), targetKey);
						KeyRelation bestRelation2 = relation2.getBestKeyRelation();
						return bestRelation.compareTo(bestRelation2);
					}
				}
			}
			return 0;
		}
		if (sortType == SORT_BY_KEY_LOCK) {
			SongProfile relativeProfile = getPrimaryRelativeSongProfile();
			if (relativeProfile != null) {
				float targetBpm = relativeProfile.getEndBpm();
				if (targetBpm == 0.0f)
					targetBpm = relativeProfile.getStartBpm();
				if (targetBpm != 0.0f) {
					Key targetKey = relativeProfile.getEndKey();
					if (!targetKey.isValid())
						targetKey = relativeProfile.getStartKey();
					if (targetKey.isValid()) {
						SongKeyRelation relation = SongKeyRelation.getSongKeyRelation(s1, new Bpm(targetBpm), targetKey);
						String c1 = relation.getRecommendedKeyLockSetting();
						SongKeyRelation relation2 = SongKeyRelation.getSongKeyRelation(s2, new Bpm(targetBpm), targetKey);
						String c2 = relation2.getRecommendedKeyLockSetting();
						return -c1.compareToIgnoreCase(c2);
					}
				}
			}
			return 0;
		}
		if (sortType == SORT_BY_KEY_CLOSENESS) {
			SongProfile relativeProfile = getPrimaryRelativeSongProfile();
			if (relativeProfile != null) {
				float targetBpm = relativeProfile.getEndBpm();
				if (targetBpm == 0.0f)
					targetBpm = relativeProfile.getStartBpm();
				if (targetBpm != 0.0f) {
					Key targetKey = relativeProfile.getEndKey();
					if (!targetKey.isValid())
						targetKey = relativeProfile.getStartKey();
					if (targetKey.isValid()) {
						SongKeyRelation relation = SongKeyRelation.getSongKeyRelation(s1, new Bpm(targetBpm), targetKey);
						float p1 = 0.0f;
						if (relation.isCompatible())
							p1 = (0.5f - Math.abs(relation.getBestKeyRelation().getDifference())) * 2.0f;
						SongKeyRelation relation2 = SongKeyRelation.getSongKeyRelation(s2, new Bpm(targetBpm), targetKey);
						float p2 = 0.0f;
						if (relation2.isCompatible())
							p2 = (0.5f - Math.abs(relation2.getBestKeyRelation().getDifference())) * 2.0f;
						if (p1 > p2)
							return -1;
						if (p1 < p2)
							return 1;
						return 0;
					}
				}
			}
			return 0;
		}
		if (sortType == SORT_BY_PITCH_SHIFT) {
			SongProfile relativeProfile = getPrimaryRelativeSongProfile();
			if (relativeProfile != null) {
				float targetBpm = relativeProfile.getEndBpm();
				if (targetBpm == 0.0f)
					targetBpm = relativeProfile.getStartBpm();
				if (targetBpm != 0.0f) {
					Key targetKey = relativeProfile.getEndKey();
					if (!targetKey.isValid())
						targetKey = relativeProfile.getStartKey();
					if (targetKey.isValid()) {
						Cents c1 = Cents.NO_CENTS;
						if (s1.getBpmStart().isValid()) {
							KeyRelation relation = SongKeyRelation.getSongKeyRelation(s1, new Bpm(targetBpm), targetKey).getBestKeyRelation();
							if (relation.hasDifference())
								c1 = Cents.getCents(relation.getDifference());
						}
						Cents c2 = Cents.NO_CENTS;
						if (s2.getBpmStart().isValid()) {
							KeyRelation relation = SongKeyRelation.getSongKeyRelation(s2, new Bpm(targetBpm), targetKey).getBestKeyRelation();
							if (relation.hasDifference())
								c2 = Cents.getCents(relation.getDifference());
						}
						return c1.compareTo(c2);
					}
				}
			}
			return 0;
		}
		if (sortType == SORT_BY_ACTUAL_KEY) {
			SongProfile relativeProfile = getPrimaryRelativeSongProfile();
			if (relativeProfile != null) {
				float targetBpm = relativeProfile.getEndBpm();
				if (targetBpm == 0.0f)
					targetBpm = relativeProfile.getStartBpm();
				if (targetBpm != 0.0f) {
					Key targetKey = relativeProfile.getEndKey();
					if (!targetKey.isValid())
						targetKey = relativeProfile.getStartKey();
					if (targetKey.isValid()) {
						Key k1 = Key.NO_KEY;
						if (s1.getStartBpm() > 0.0f) {
							float bpmDifference = Bpm.getBpmDifference(s1.getStartBpm(), targetBpm);
							k1 = s1.getStartKey().getShiftedKeyByBpmDifference(bpmDifference);
						}
						Key k2 = Key.NO_KEY;
						if (s2.getStartBpm() > 0.0f) {
							float bpmDifference = Bpm.getBpmDifference(s2.getStartBpm(), targetBpm);
							k2 = s2.getStartKey().getShiftedKeyByBpmDifference(bpmDifference);
						}
						return k1.compareTo(k2);
					}
				}
			}
			return 0;
		}
		if (sortType == SORT_BY_ACTUAL_KEY_CODE) {
			SongProfile relativeProfile = getPrimaryRelativeSongProfile();
			if (relativeProfile != null) {
				float targetBpm = relativeProfile.getEndBpm();
				if (targetBpm == 0.0f)
					targetBpm = relativeProfile.getStartBpm();
				if (targetBpm != 0.0f) {
					Key targetKey = relativeProfile.getEndKey();
					if (!targetKey.isValid())
						targetKey = relativeProfile.getStartKey();
					if (targetKey.isValid()) {
						KeyCode k1 = KeyCode.NO_KEYCODE;
						if (s1.getStartBpm() > 0.0f) {
							float bpmDifference = Bpm.getBpmDifference(s1.getStartBpm(), targetBpm);
							k1 = s1.getStartKey().getShiftedKeyByBpmDifference(bpmDifference).getKeyCode();
						}
						KeyCode k2 = KeyCode.NO_KEYCODE;
						if (s2.getStartBpm() > 0.0f) {
							float bpmDifference = Bpm.getBpmDifference(s2.getStartBpm(), targetBpm);
							k2 = s2.getStartKey().getShiftedKeyByBpmDifference(bpmDifference).getKeyCode();
						}
						return k1.compareTo(k2);
					}
				}
			}
			return 0;
		}

		return super.compareSub(r1, r2, sortType);
	}

	@Override
	public void write(LineWriter writer) {
		super.write(writer);
		writer.writeLine(8); //version
		if (artistSearchText != null)
			writer.writeLine(artistSearchText);
		else
			writer.writeLine("");
		if (releaseSearchText != null)
			writer.writeLine(releaseSearchText);
		else
			writer.writeLine("");
		if (titleSearchText != null)
			writer.writeLine(titleSearchText);
		else
			writer.writeLine("");
		if (labelSearchText != null)
			writer.writeLine(labelSearchText);
		else
			writer.writeLine("");
		writer.writeLine(artistIds != null ? artistIds.size() : 0);
		if (artistIds != null)
			for (Integer artistId : artistIds)
				writer.writeLine(artistId);
		writer.writeLine(labelIds != null ? labelIds.size() : 0);
		if (labelIds != null)
			for (Integer labelId : labelIds)
				writer.writeLine(labelId);
		writer.writeLine(releaseIds != null ? releaseIds.size() : 0);
		if (releaseIds != null)
			for (Integer releaseId : releaseIds)
				writer.writeLine(releaseId);
		writer.writeLine(keys != null ? keys.size() : 0);
		if (keys != null)
			for (Key key : keys)
				writer.writeLine(key.toStringExact());
		writer.writeLine(minBpm != null ? minBpm.getBpmValue() : 0.0f);
		writer.writeLine(maxBpm != null ? maxBpm.getBpmValue() : 0.0f);
		writer.writeLine(minDuration != null ? minDuration.getDurationInMillis() : 0);
		writer.writeLine(maxDuration != null ? maxDuration.getDurationInMillis() : Integer.MAX_VALUE);
		writer.writeLine(minYearReleased);
		writer.writeLine(maxYearReleased);
		writer.writeLine(searchForCompatible);
		writer.writeLine(penalizeOldPublished);
		writer.writeLine(onlyMobileEncodings);
		writer.writeLine(String.valueOf(excludedSongTitles.size()));
		for (String excludeTitle : excludedSongTitles.keySet())
			writer.writeLine(excludeTitle);
		writer.writeLine(String.valueOf(excludeArtistIds.size()));
		for (int excludeId : excludeArtistIds.keySet())
			writer.writeLine(String.valueOf(excludeId));
		writer.writeLine(String.valueOf(validFilenamesOnly));
		writer.writeLine(String.valueOf(hasBrokenFileLink));
		writer.writeLine(String.valueOf(compatibleMatchType));
		writer.writeLine(String.valueOf(compatibleBpmRange));
		writer.writeLine(String.valueOf(compatibleBpmShift));
		// TODO: artist similarity maps
	}

	/**
	 * This function should group together unique songs (and their remixes), so that the next recommended song won't keep playing remixes of the same song...
	 */
	static public String getLogicalSongKey(SongRecord song) {
		StringBuffer result = new StringBuffer();
		result.append(SearchEncoder.unifyString(song.getArtistsDescription()));
		result.append("-");
		result.append(SearchEncoder.unifyString(song.getTitle()));
		return result.toString();
	}

	@Override
	public Index getIndex() { return Database.getSongIndex(); }

	@Override
	public void addSearchFields(Vector<String> searchFields) {
		super.addSearchFields(searchFields);
		searchFields.add("artist");
		searchFields.add("featuring");
		searchFields.add("release");
		searchFields.add("track");
		searchFields.add("release_instances");
		searchFields.add("title");
		searchFields.add("remix");
		searchFields.add("label");
		searchFields.add("key");
		searchFields.add("bpm");
		searchFields.add("timesig");
		searchFields.add("filename");
		searchFields.add("year");
	}

}
