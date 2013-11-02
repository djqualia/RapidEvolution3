package com.mixshare.rapid_evolution.data.search.parameters.search;

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
import com.mixshare.rapid_evolution.data.identifier.Identifier;
import com.mixshare.rapid_evolution.data.identifier.IdentifierParser;
import com.mixshare.rapid_evolution.data.index.search.SearchIndex;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.profile.ProfileManager;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.filter.FilterRecord;
import com.mixshare.rapid_evolution.data.record.filter.playlist.OrderedPlaylistRecord;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.data.record.user.UserDataType;
import com.mixshare.rapid_evolution.data.search.SearchResult;
import com.mixshare.rapid_evolution.data.user.LocalUserProfile;
import com.mixshare.rapid_evolution.data.user.UserProfile;
import com.mixshare.rapid_evolution.music.beatintensity.BeatIntensity;
import com.mixshare.rapid_evolution.ui.model.column.comparables.Percentage;
import com.mixshare.rapid_evolution.ui.model.column.comparables.SmartString;
import com.mixshare.rapid_evolution.ui.model.filter.FilterSelection;
import com.mixshare.rapid_evolution.ui.model.profile.SimilarProfilesModel;
import com.mixshare.rapid_evolution.ui.model.search.SearchModelManager;
import com.mixshare.rapid_evolution.ui.widgets.profile.ProfileWidgetUI;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

abstract public class SearchSearchParameters extends AbstractSearchSearchParameters {

    static private Logger log = Logger.getLogger(SearchSearchParameters.class);

    ////////////
    // FIELDS //
    ////////////

    private FilterSelection stylesSelection;
    private FilterSelection tagsSelection;
    private FilterSelection playlistsSelection;

    private BeatIntensity minBeatIntensity;
    private BeatIntensity maxBeatIntensity;

    private boolean internalItemsOnly;
	private boolean externalItemsOnly;

	private byte sortMinedHeader;
    private long minedHeaderCutoffTime;

    private Vector<UserDataTypeFilter> userDataFilters = new Vector<UserDataTypeFilter>();
    private Vector<UserDataTypeWeight> userDataWeights = new Vector<UserDataTypeWeight>();

    protected int[] relativeProfileIds;
	protected Identifier[] relativeProfileIdentifiers;

    private short maxDaysSinceLastAdded;

    private UserProfile userProfile;

	private Map<Integer, Object> excludedIds = new HashMap<Integer, Object>();

	private float randomness;

	private boolean computeUserPreferenceWithResultScore;

    private String styleSearchText;
	private String tagSearchText;

	private boolean enableRelativeSearch;

	transient protected Vector<SearchProfile> relativeProfiles;

	static {
    	try{
    		// in order for the XMLEncoder to skip transient variables, this is needed
    		BeanInfo info = Introspector.getBeanInfo(SearchSearchParameters.class);
    		PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
    		for (int i = 0; i < propertyDescriptors.length; ++i) {
    			PropertyDescriptor pd = propertyDescriptors[i];
    			if (pd.getName().equals("relativeProfiles")) {
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

	public SearchSearchParameters() { }

    public SearchSearchParameters(SearchSearchParameters copy) {
    	super(copy);
    	this.stylesSelection = copy.stylesSelection;
    	this.tagsSelection = copy.tagsSelection;
    	this.playlistsSelection = copy.playlistsSelection;
    	this.minBeatIntensity = copy.minBeatIntensity;
    	this.maxBeatIntensity = copy.maxBeatIntensity;
    	this.relativeProfileIds = copy.relativeProfileIds;
    	this.relativeProfileIdentifiers = copy.relativeProfileIdentifiers;
    	this.internalItemsOnly = copy.internalItemsOnly;
    	this.externalItemsOnly = copy.externalItemsOnly;
    	this.sortMinedHeader = copy.sortMinedHeader;
    	this.minedHeaderCutoffTime = copy.minedHeaderCutoffTime;
    	this.userDataFilters = copy.userDataFilters;
    	this.userDataWeights = copy.userDataWeights;
    	this.maxDaysSinceLastAdded = copy.maxDaysSinceLastAdded;
    	this.userProfile = copy.userProfile;
    	for (int excludeId : copy.excludedIds.keySet())
    		this.excludedIds.put(excludeId, null);
    	this.randomness = copy.randomness;
    	this.styleSearchText = copy.styleSearchText;
    	this.tagSearchText = copy.tagSearchText;
    	this.enableRelativeSearch = copy.enableRelativeSearch;
    	this.computeUserPreferenceWithResultScore = copy.computeUserPreferenceWithResultScore;
    }

    public SearchSearchParameters(LineReader lineReader) {
    	super(lineReader);
    	int version = Integer.parseInt(lineReader.getNextLine());
    	int numStyles = Integer.parseInt(lineReader.getNextLine());
    	if (numStyles == 1)
    		stylesSelection = new FilterSelection(lineReader);
    	int numTags = Integer.parseInt(lineReader.getNextLine());
    	if (numTags == 1)
    		tagsSelection = new FilterSelection(lineReader);
    	int numPlaylists = Integer.parseInt(lineReader.getNextLine());
    	if (numPlaylists == 1)
    		playlistsSelection = new FilterSelection(lineReader);
    	if (version >= 5) {
    		styleSearchText = lineReader.getNextLine();
    		tagSearchText = lineReader.getNextLine();
    	}
    	minBeatIntensity = BeatIntensity.getBeatIntensity(Byte.parseByte(lineReader.getNextLine()));
    	maxBeatIntensity = BeatIntensity.getBeatIntensity(Byte.parseByte(lineReader.getNextLine()));
    	internalItemsOnly = Boolean.getBoolean(lineReader.getNextLine());
    	externalItemsOnly = Boolean.getBoolean(lineReader.getNextLine());
    	sortMinedHeader = Byte.parseByte(lineReader.getNextLine());
    	minedHeaderCutoffTime = Long.parseLong(lineReader.getNextLine());
    	int numUserDataFilters = Integer.parseInt(lineReader.getNextLine());
    	for (int i = 0; i < numUserDataFilters; ++i)
    		userDataFilters.add(new UserDataTypeFilter(lineReader));
    	int numUserDataWeights = Integer.parseInt(lineReader.getNextLine());
    	for (int i = 0; i < numUserDataWeights; ++i)
    		userDataWeights.add(new UserDataTypeWeight(lineReader));
    	if (version < 3) {
    		relativeProfileIds = new int[1];
    		relativeProfileIds[0] = Integer.parseInt(lineReader.getNextLine());
    		int relativeProfileCount = Integer.parseInt(lineReader.getNextLine());
    		relativeProfileIdentifiers = new Identifier[1];
    		if (relativeProfileCount == 1)
    			relativeProfileIdentifiers[0] = IdentifierParser.getIdentifier(lineReader.getNextLine());
    	} else {
    		int numRelativeProfiles = Integer.parseInt(lineReader.getNextLine());
    		relativeProfileIds = new int[numRelativeProfiles];
    		relativeProfileIdentifiers = new Identifier[numRelativeProfiles];
    		for (int i = 0; i < numRelativeProfiles; ++i) {
    			relativeProfileIds[i] = Integer.parseInt(lineReader.getNextLine());
    			relativeProfileIdentifiers[i] = IdentifierParser.getIdentifier(lineReader.getNextLine());
    		}
    	}
    	if (version >= 6) {
    		enableRelativeSearch = lineReader.getNextLine().equals("1");
    	}
    	maxDaysSinceLastAdded = Short.parseShort(lineReader.getNextLine());
        int userProfileCount = Integer.parseInt(lineReader.getNextLine());
        if (userProfileCount > 0)
        	userProfile = new LocalUserProfile(lineReader);
    	if (version >= 2) {
    		int numExcludeIds = Integer.parseInt(lineReader.getNextLine());
    		for (int i = 0; i < numExcludeIds; ++i)
    			excludedIds.put(Integer.parseInt(lineReader.getNextLine()), 0);
    		randomness = Float.parseFloat(lineReader.getNextLine());
    	}
    	if (version >= 4)
    		computeUserPreferenceWithResultScore = Boolean.getBoolean(lineReader.getNextLine());
    }

    /////////////
    // GETTERS //
    /////////////

	public FilterSelection getStylesSelection() { return stylesSelection; }
	public FilterSelection getTagsSelection() { return tagsSelection; }
	public FilterSelection getPlaylistsSelection() { return playlistsSelection; }

    public String getStyleSearchText() { return styleSearchText; }
	public String getTagSearchText() { return tagSearchText; }

	public BeatIntensity getMinBeatIntensity() { return minBeatIntensity; }
	public BeatIntensity getMaxBeatIntensity() { return maxBeatIntensity; }

    public boolean isInternalItemsOnly() { return internalItemsOnly; }
	public boolean isExternalItemsOnly() { return externalItemsOnly; }

    public byte getSortMinedHeader() { return sortMinedHeader; }
	public long getMinedHeaderCutoffTime() { return minedHeaderCutoffTime; }

	public Vector<UserDataTypeFilter> getUserDataFilters() { return userDataFilters; }
	public Vector<UserDataTypeWeight> getUserDataWeights() { return userDataWeights; }

    @Override
	public Vector<SearchProfile> fetchRelativeProfiles() {
    	if (relativeProfiles == null) {
    		Vector<SearchProfile> result = new Vector<SearchProfile>();
    		if (relativeProfileIds != null) {
    			for (int i = 0; i < relativeProfileIds.length; ++i) {
    				SearchProfile relativeProfile = (SearchProfile)ProfileManager.getProfile(relativeProfileIdentifiers[i], relativeProfileIds[i]);
    				if (relativeProfile != null)
    					result.add(relativeProfile);
    			}
    		} else {
    			if (!RE3Properties.getBoolean("server_mode")) {
	    			if (ProfileWidgetUI.instance != null) {
	    				Profile profile = ProfileWidgetUI.instance.getCurrentProfile();
	    				if ((profile != null) && (profile instanceof SearchProfile) && (profile.getRecord().getDataType() == getDataType()))
	    					result.add((SearchProfile)profile);
	    			}
    			}
    		}
    		relativeProfiles = result;
    	}
    	return relativeProfiles;
    }

    public int[] getRelativeProfileIds() { return relativeProfileIds; }
	public Identifier[] getRelativeProfileIdentifiers() { return relativeProfileIdentifiers; }

	public short getMaxDaysSinceLastAdded() { return maxDaysSinceLastAdded; }

	public UserProfile getUserProfile() { return userProfile; }

	public Map<Integer, Object> getExcludedIds() { return excludedIds; }

	public float getRandomness() { return randomness; }

	public boolean isEnableRelativeSearch() { return enableRelativeSearch; }

	public boolean isComputeUserPreferenceWithResultScore() { return computeUserPreferenceWithResultScore; }

    @Override
	public String getUniqueHash() {
    	StringBuffer result = new StringBuffer();
    	result.append(super.getUniqueHash());
    	result.append(queryKeySeperator);
    	if (stylesSelection != null) {
	    	for (FilterRecord style : stylesSelection.getRequiredFilters()) {
	    		result.append(String.valueOf(style.getUniqueId()));
	    		result.append(",");
	    	}
    	}
    	result.append(queryKeySeperator);
    	if (stylesSelection != null) {
	    	for (FilterRecord style : stylesSelection.getOptionalFilters()) {
	    		result.append(String.valueOf(style.getUniqueId()));
	    		result.append(",");
	    	}
    	}
    	result.append(queryKeySeperator);
    	if (stylesSelection != null) {
	    	for (FilterRecord style : stylesSelection.getExcludedFilters()) {
	    		result.append(String.valueOf(style.getUniqueId()));
	    		result.append(",");
	    	}
    	}
    	result.append(queryKeySeperator);
    	if (tagsSelection != null) {
	    	for (FilterRecord tag : tagsSelection.getRequiredFilters()) {
	    		result.append(String.valueOf(tag.getUniqueId()));
	    		result.append(",");
	    	}
    	}
    	result.append(queryKeySeperator);
    	if (tagsSelection != null) {
	    	for (FilterRecord tag : tagsSelection.getOptionalFilters()) {
	    		result.append(String.valueOf(tag.getUniqueId()));
	    		result.append(",");
	    	}
    	}
    	result.append(queryKeySeperator);
    	if (tagsSelection != null) {
	    	for (FilterRecord tag : tagsSelection.getExcludedFilters()) {
	    		result.append(String.valueOf(tag.getUniqueId()));
	    		result.append(",");
	    	}
    	}
    	result.append(queryKeySeperator);
    	if (playlistsSelection != null) {
	    	for (FilterRecord playlist : playlistsSelection.getRequiredFilters()) {
	    		result.append(String.valueOf(playlist.getUniqueId()));
	    		result.append(",");
	    	}
    	}
    	result.append(queryKeySeperator);
    	if (playlistsSelection != null) {
	    	for (FilterRecord playlist : playlistsSelection.getOptionalFilters()) {
	    		result.append(String.valueOf(playlist.getUniqueId()));
	    		result.append(",");
	    	}
    	}
    	result.append(queryKeySeperator);
    	if (playlistsSelection != null) {
	    	for (FilterRecord playlist : playlistsSelection.getExcludedFilters()) {
	    		result.append(String.valueOf(playlist.getUniqueId()));
	    		result.append(",");
	    	}
    	}
    	result.append(queryKeySeperator);
    	result.append(styleSearchText != null ? styleSearchText : "");
    	result.append(queryKeySeperator);
    	result.append(tagSearchText != null ? tagSearchText : "");
    	result.append(queryKeySeperator);
    	result.append(minBeatIntensity != null ? minBeatIntensity.toString() : "");
    	result.append(queryKeySeperator);
    	result.append(maxBeatIntensity != null ? maxBeatIntensity.toString() : "");
    	result.append(queryKeySeperator);
    	if (relativeProfileIds != null) {
    		result.append(String.valueOf(relativeProfileIds.length));
    		result.append("-");
    		int i = 0;
    		for (int relativeProfileId : relativeProfileIds) {
    			result.append(String.valueOf(relativeProfileId));
    			Record record = Database.getRecord(getRelativeProfileIdentifiers()[i]);
    			if (record != null) {
    				result.append("@");
    				result.append(String.valueOf(record.getLastModified()));
    			}
    			result.append(";");
    			++i;
    		}
    	} else {
    		result.append(String.valueOf(0));
    	}
    	result.append(queryKeySeperator);
    	result.append(String.valueOf(enableRelativeSearch));
    	result.append(queryKeySeperator);
    	result.append(String.valueOf(internalItemsOnly));
    	result.append(queryKeySeperator);
    	result.append(String.valueOf(externalItemsOnly));
    	result.append(queryKeySeperator);
    	result.append(String.valueOf(sortMinedHeader));
    	result.append(queryKeySeperator);
    	result.append(String.valueOf(minedHeaderCutoffTime));
    	if (userDataFilters != null) {
	    	for (UserDataTypeFilter userDataFilter : userDataFilters) {
	        	result.append(queryKeySeperator);
	        	result.append(String.valueOf(userDataFilter));
	    	}
    	}
    	if (userDataWeights != null) {
	    	for (UserDataTypeWeight userDataWeight : userDataWeights) {
	        	result.append(queryKeySeperator);
	        	result.append(String.valueOf(userDataWeight));
	    	}
    	}
    	result.append(queryKeySeperator);
    	result.append(String.valueOf(maxDaysSinceLastAdded));
    	result.append(queryKeySeperator);
    	result.append(userProfile != null ? userProfile.getProfileId() : "");
    	result.append(queryKeySeperator);
    	result.append(String.valueOf(excludedIds));
    	result.append(queryKeySeperator);
    	result.append(String.valueOf(randomness));
    	result.append(queryKeySeperator);
    	result.append(String.valueOf(computeUserPreferenceWithResultScore));
    	return result.toString();
    }

    /////////////
    // SETTERS //
    /////////////

    public void setStylesSelection(FilterSelection stylesSelection) { this.stylesSelection = stylesSelection; }
    public void setTagsSelection(FilterSelection tagsSelection) { this.tagsSelection = tagsSelection; }
    public void setPlaylistsSelection(FilterSelection playlistsSelection) { this.playlistsSelection = playlistsSelection; }

	public void setStyleSearchText(String styleSearchText) { this.styleSearchText = styleSearchText; }
	public void setTagSearchText(String tagSearchText) { this.tagSearchText = tagSearchText; }

	public void setMinBeatIntensity(BeatIntensity minBeatIntensity) { this.minBeatIntensity = minBeatIntensity; }
	public void setMaxBeatIntensity(BeatIntensity maxBeatIntensity) { this.maxBeatIntensity = maxBeatIntensity; }

	public void setInternalItemsOnly(boolean internalItemsOnly) { this.internalItemsOnly = internalItemsOnly; }
	public void setExternalItemsOnly(boolean externalItemsOnly) { this.externalItemsOnly = externalItemsOnly; }

	public void setSortMinedHeader(byte sortMinedHeader) { this.sortMinedHeader = sortMinedHeader; }
	public void setMinedHeaderCutoffTime(long minedHeaderCutoffTime) { this.minedHeaderCutoffTime = minedHeaderCutoffTime; }

    public void addUserDataTypeFilter(UserDataType dataType, Object value) { this.userDataFilters.add(new UserDataTypeFilter(dataType, value)); }
	public void setUserDataFilters(Vector<UserDataTypeFilter> userDataFilters) { this.userDataFilters = userDataFilters; }

	public void addUserDataTypeWeight(UserDataType dataType, Object value, float weight) { this.userDataWeights.add(new UserDataTypeWeight(dataType, value, weight)); }
	public void setUserDataWeights(Vector<UserDataTypeWeight> userDataWeights) { this.userDataWeights = userDataWeights; }

	@Override
	public void clearLastSearchResultScores() {
		super.clearLastSearchResultScores();
		relativeProfiles = null;
	}
	public void clearRelativeProfiles() { relativeProfiles = null; }
    @Override
	public void initRelativeProfile(SearchProfile relativeProfile) {
    	this.relativeProfiles = new Vector<SearchProfile>(1);
    	relativeProfiles.add(relativeProfile);
    	this.relativeProfileIds = new int[1];
    	relativeProfileIds[0] = relativeProfile.getUniqueId();
    	this.relativeProfileIdentifiers = new Identifier[1];
    	relativeProfileIdentifiers[0] = relativeProfile.getIdentifier();
    	enableRelativeSearch = true;
    }
    public void initRelativeProfiles(Vector<SearchProfile> relativeProfiles) {
    	this.relativeProfiles = new Vector<SearchProfile>(relativeProfiles.size());
    	for (int i = 0; i < relativeProfiles.size(); ++i)
    		this.relativeProfiles.add(relativeProfiles.get(i));
    	this.relativeProfileIds = new int[relativeProfiles.size()];
    	this.relativeProfileIdentifiers = new Identifier[relativeProfiles.size()];
    	for (int i = 0; i < relativeProfiles.size(); ++i) {
    		relativeProfileIds[i] = relativeProfiles.get(i).getUniqueId();
    		relativeProfileIdentifiers[i] = relativeProfiles.get(i).getIdentifier();
    	}
    	enableRelativeSearch = true;
    }
	public void setRelativeProfileIds(int[] relativeProfileIds) {
		this.relativeProfileIds = relativeProfileIds;
		enableRelativeSearch = true;
	}
	public void setRelativeProfileIdentifiers(Identifier[] relativeProfileIdentifiers) {
		this.relativeProfileIdentifiers = relativeProfileIdentifiers;
		enableRelativeSearch = true;
	}

    public void setMaxDaysSinceLastAdded(short maxDaysSinceLastAdded) { this.maxDaysSinceLastAdded = maxDaysSinceLastAdded; }

	public void setUserProfile(UserProfile userProfile) { this.userProfile = userProfile; }

	public void setRandomness(float randomness) { this.randomness = randomness; }

	public void setComputeUserPreferenceWithResultScore(boolean computeUserPreferenceWithResultScore) { this.computeUserPreferenceWithResultScore = computeUserPreferenceWithResultScore; }

	public void setExcludedIds(Map<Integer, Object> excludedIds) { this.excludedIds = excludedIds; }
	public void setExcludedIds(Vector<Integer> excludeIds) {
		for (int excludeId : excludeIds)
			this.excludedIds.put(excludeId, null);
	}
	public void addExcludedId(int excludedId) {
		this.excludedIds.put(excludedId, null);
	}

	public void setEnableRelativeSearch(boolean enableRelativeSearch) { this.enableRelativeSearch = enableRelativeSearch; }

	/////////////
	// METHODS //
	/////////////

	@Override
	public float matchesSub(Record record, boolean fullCheck) {
		float superScore = super.matchesSub(record, fullCheck);
		if (superScore > 0.0f) {
			SearchRecord searchRecord = (SearchRecord)record;

			if (internalItemsOnly && searchRecord.isExternalItem())
				return 0.0f;
			if (externalItemsOnly && !searchRecord.isExternalItem())
				return 0.0f;
			if (excludedIds.size() > 0) {
				if (excludedIds.containsKey(searchRecord.getUniqueId()))
					return 0.0f;
			}
			if (getMaxDaysSinceLastAdded() != (short)0) {
				long timeSinceAdded = System.currentTimeMillis() - searchRecord.getDateAdded();
				float days = (timeSinceAdded) / 1000.0f / 60.0f / 60.0f / 24.0f;
				if (days > getMaxDaysSinceLastAdded())
					return 0.0f;
			}
			if (minedHeaderCutoffTime != 0) {
				long lastFetched = searchRecord.getLastFetchedMinedProfile(sortMinedHeader);
				if (lastFetched > minedHeaderCutoffTime)
					return 0.0f;
			}

			if ((userDataFilters != null) && (userDataFilters.size() > 0)) {
				for (UserDataTypeFilter userDataFilter : userDataFilters) {
					Object userData = searchRecord.getUserData(userDataFilter.getUserDataType());
					if ((userData == null) || !userData.equals(userDataFilter.getUserDataValue()))
						return 0.0f;
				}
			}

			if (getStylesSelection() != null) {
				if (!getStylesSelection().matches(searchRecord))
					return 0.0f;
				else
					superScore += SearchModelManager.getAverageMatch(getStylesSelection().getIncludedFilterIds(), searchRecord.getActualStyleIds(), searchRecord.getActualStyleDegrees());
			}
			if (getTagsSelection() != null) {
				if (!getTagsSelection().matches(searchRecord))
					return 0.0f;
				else
					superScore += SearchModelManager.getAverageMatch(getTagsSelection().getIncludedFilterIds(), searchRecord.getActualTagIds(), searchRecord.getActualTagDegrees());
			}
			if (getPlaylistsSelection() != null) {
				if (!getPlaylistsSelection().matches(searchRecord))
					return 0.0f;
			}

			if (fullCheck) {
				if ((styleSearchText != null) && (styleSearchText.length() > 0)) {
					float match = getSearchFieldsMatch(record, new String[] { "style" }, styleSearchText);
					if (match == 0.0f)
						return 0.0f;
					superScore += match;
				}
				if ((tagSearchText != null) && (tagSearchText.length() > 0)) {
					float match = getSearchFieldsMatch(record, new String[] { "tag" }, tagSearchText);
					if (match == 0.0f)
						return 0.0f;
					superScore += match;
				}
			}

			// user profile filter
			if ((userProfile != null) && computeUserPreferenceWithResultScore) {
				float preference = userProfile.computePreference(searchRecord);
				if (preference <= 0.0f)
					return 0.0f;
				superScore += preference;
			}

			if (enableRelativeSearch) {
				// similarity to relative profiles
				fetchRelativeProfiles();
				if ((relativeProfiles != null) && (relativeProfiles.size() > 0) && (superScore > 0.0f)) {
					float totalSimilarity = 0.0f;
					for (SearchProfile relativeProfile : relativeProfiles) {
						float similarity = 0.0f;
						if (relativeProfile.getUniqueId() != searchRecord.getUniqueId())
							similarity = relativeProfile.getSimilarity(searchRecord);
						else
							similarity = 1.0f;
						totalSimilarity += similarity;
					}
					if ((relativeProfiles != null) && (relativeProfiles.size() > 0))
						totalSimilarity /= relativeProfiles.size();
					if (totalSimilarity < SimilarProfilesModel.MIN_SIMILARITY_FOR_DISPLAY)
						totalSimilarity = 0.0f;
					// we can't exclude 0.0 matches when the main search view has a similarity column....
					//if (totalSimilarity == 0.0f)
						//return 0.0f;
					superScore += totalSimilarity;
				}
			}

			return superScore;
		} else {
			return 0.0f;
		}
	}

	@Override
	public float computeWeight(Record record) {
		float superWeight = super.computeWeight(record);
		SearchRecord searchRecord = (SearchRecord)record;
		if ((userDataWeights != null) && (userDataWeights.size() > 0)) {
			for (UserDataTypeWeight userDataWeight : userDataWeights) {
				Object userData = searchRecord.getUserData(userDataWeight.getUserDataType());
				if ((userData != null) && userData.equals(userDataWeight.getUserDataValue()))
					superWeight *= userDataWeight.getWeight();
			}
			if (superWeight == 0.0f)
				return 0.0f;
		}
		if (randomness > 0.0f)
			superWeight = (float)((1.0f - randomness) * superWeight + randomness * Math.random());
		return superWeight;
	}

	@Override
	public boolean isEmpty(boolean countIndexed) {
		if (!super.isEmpty(countIndexed))
			return false;

		if ((stylesSelection != null) && !stylesSelection.isEmpty())
			return false;
		if ((tagsSelection != null) && !tagsSelection.isEmpty())
			return false;
		if ((playlistsSelection != null) && !playlistsSelection.isEmpty())
			return false;
		if ((minBeatIntensity != null) && (minBeatIntensity.getBeatIntensityValue() > BeatIntensity.MIN_VALUE))
			return false;
		if ((maxBeatIntensity != null) && (maxBeatIntensity.getBeatIntensityValue() < BeatIntensity.MAX_VALUE))
			return false;
		if ((relativeProfileIdentifiers != null) && (relativeProfileIdentifiers.length > 0))
			return false;
		if (minedHeaderCutoffTime != 0)
			return false;
		if ((userDataFilters != null) && (userDataFilters.size() > 0))
			return false;
		if ((userDataWeights != null) && (userDataWeights.size() > 0))
			return false;
		if (maxDaysSinceLastAdded != (short)0)
			return false;
		if ((userProfile != null) && computeUserPreferenceWithResultScore)
			return false;
		if (excludedIds.size() > 0)
			return false;
		if (randomness > 0.0f)
			return false;
		if (internalItemsOnly)
			return false;
		if (externalItemsOnly)
			return false;
		if ((styleSearchText != null) && (styleSearchText.length() > 0))
			return false;
		if ((tagSearchText != null) && (tagSearchText.length() > 0))
			return false;
		if (enableRelativeSearch)
			return false;
		return true;
	}

	@Override
	protected int compareSub(SearchResult r1, SearchResult r2, byte sortType) {
		SearchRecord s1 = (SearchRecord)r1.getRecord();
		SearchRecord s2 = (SearchRecord)r2.getRecord();
		if (sortType == SORT_BY_SCORE) {
			float f1 = s1.getScore();
			float f2 = s2.getScore();
			if (!Float.isNaN(f1) && !Float.isNaN(f2)) {
				if (f1 > f2)
					return -1;
				if (f1 < f2)
					return 1;
				return 0;
			} else if (!Float.isNaN(f1)) {
				return -1;
			} else if (!Float.isNaN(f2)) {
				return 1;
			}
			return 0;
		} else if (sortType == SORT_BY_MINED_HEADER) {
			long lastMinedS1 = s1.getLastFetchedMinedProfile(sortMinedHeader);
			long lastMinedS2 = s2.getLastFetchedMinedProfile(sortMinedHeader);
			if (lastMinedS1 < lastMinedS2)
				return -1;
			if (lastMinedS1 > lastMinedS2)
				return 1;
			return 0;
		} else if (sortType == SORT_BY_NUM_PLAYS) {
			if (s1.getPlayCount() > s2.getPlayCount())
				return -1;
			if (s1.getPlayCount() < s2.getPlayCount())
				return 1;
			return 0;
		} else if (sortType == SORT_BY_INTERNAL_ITEMS) {
			if (!s1.isExternalItem() && s2.isExternalItem())
				return -1;
			if (s1.isExternalItem() && !s2.isExternalItem())
				return 1;
			return 0;
		} else if (sortType == SORT_BY_POPULARITY) {
			if (s1.getPopularity() > s2.getPopularity())
				return -1;
			if (s1.getPopularity() < s2.getPopularity())
				return 1;
			return 0;
		} else if (sortType == SORT_BY_DATE_ADDED) {
			if (s1.getDateAdded() > s2.getDateAdded())
				return -1;
			if (s1.getDateAdded() < s2.getDateAdded())
				return 1;
			return 0;
		} else if (sortType == SORT_BY_USER_PREFERENCE) {
			if (userProfile == null)
				userProfile = Database.getUserProfile();
			if (userProfile != null) {
				float p1 = userProfile.computePreference(s1);
				float p2 = userProfile.computePreference(s2);
				if (p1 > p2)
					return -1;
				if (p1 < p2)
					return 1;
			}
			return 0;
		} else if (sortType == SORT_BY_DEGREE) { // used in style/tag sub views...
			float p1 = 0.0f;
			float p2 = 0.0f;
			if ((getStylesSelection() != null) && !getStylesSelection().isEmpty()) {
				p1 = s1.getActualStyleDegree(getStylesSelection().getOptionalFilters().get(0).getUniqueId());
				p2 = s2.getActualStyleDegree(getStylesSelection().getOptionalFilters().get(0).getUniqueId());
			} else if ((getTagsSelection() != null) && (!getTagsSelection().isEmpty())) {
				p1 = s1.getActualTagDegree(getTagsSelection().getOptionalFilters().get(0).getUniqueId());
				p2 = s2.getActualTagDegree(getTagsSelection().getOptionalFilters().get(0).getUniqueId());
			}
			if (p1 > p2)
				return -1;
			else if (p1 < p2)
				return 1;
			return 0;
		} else if (sortType == SORT_BY_FILTER_MATCH) {
			float p1 = getLastSearchResultScore(s1);
			float p2 = getLastSearchResultScore(s2);
			if (p1 > p2)
				return -1;
			else if (p1 < p2)
				return 1;
			return 0;
		} else if (sortType == SORT_BY_STYLE_MATCH) {
			Percentage p1 = SearchModelManager.getStyleMatch(getStylesSelection().getIncludedFilterIds(), s1);
			Percentage p2 = SearchModelManager.getStyleMatch(getStylesSelection().getIncludedFilterIds(), s2);
			if (p1.getPercentage() > p2.getPercentage())
				return -1;
			else if (p1.getPercentage() < p2.getPercentage())
				return 1;
			return 0;
		} else if (sortType == SORT_BY_TAG_MATCH) {
			Percentage p1 = SearchModelManager.getTagMatch(getTagsSelection().getIncludedFilterIds(), s1);
			Percentage p2 = SearchModelManager.getTagMatch(getTagsSelection().getIncludedFilterIds(), s2);
			if (p1.getPercentage() > p2.getPercentage())
				return -1;
			else if (p1.getPercentage() < p2.getPercentage())
				return 1;
			return 0;
		} else if (sortType == SORT_BY_COMMENTS) {
			String c1 = s1.getComments();
			String c2 = s2.getComments();
			return SmartString.compareStrings(c1, c2);
		} else if (sortType == SORT_BY_SIMILARITY) {
			fetchRelativeProfiles();
			Float t1 = r1.getSimilarity();
			Float t2 = r2.getSimilarity();
			if (t1 == null) {
				t1 = 0.0f;
				for (SearchProfile relativeProfile : relativeProfiles)
					t1 += relativeProfile.getSimilarity(s1);
				r1.setSimilarity(t1);
			}
			if (t2 == null) {
				t2 = 0.0f;
				for (SearchProfile relativeProfile : relativeProfiles)
					t2 += relativeProfile.getSimilarity(s2);
				r2.setSimilarity(t2);
			}
			if (t1 > t2)
				return -1;
			if (t1 < t2)
				return 1;
			return 0;
		} else if (sortType == SORT_BY_STYLES) {
			String c1 = s1.getActualStyleDescription();
			String c2 = s2.getActualStyleDescription();
			return SmartString.compareStrings(c1, c2);
		} else if (sortType == SORT_BY_TAGS) {
			String c1 = s1.getActualTagDescription();
			String c2 = s2.getActualTagDescription();
			return SmartString.compareStrings(c1, c2);
		} else if (sortType == SORT_BY_LAST_MODIFIED) {
			long p1 = s1.getLastModified();
			long p2 = s2.getLastModified();
			if (p1 > p2)
				return -1;
			else if (p1 < p2)
				return 1;
			return 0;
		} else if (sortType == SORT_BY_PLAYLIST_POSITION) {
			FilterRecord filter = getPlaylistsSelection().getOptionalFilters().get(0);
			if (filter instanceof OrderedPlaylistRecord) {
				OrderedPlaylistRecord op = (OrderedPlaylistRecord)filter;
				int p1 = op.getPositionOf(s1.getUniqueId());
				int p2 = op.getPositionOf(s2.getUniqueId());
				if (p1 < p2)
					return -1;
				else if (p1 > p2)
					return 1;
				return 0;
			}
		}
		return super.compareSub(r1, r2, sortType);
	}

	@Override
	public void write(LineWriter writer) {
		super.write(writer);
		writer.writeLine(6);
		if (stylesSelection != null) {
			writer.writeLine(1);
			stylesSelection.write(writer);
		} else {
			writer.writeLine(0);
		}
		if (tagsSelection != null) {
			writer.writeLine(1);
			tagsSelection.write(writer);
		} else {
			writer.writeLine(0);
		}
		if (playlistsSelection != null) {
			writer.writeLine(1);
			playlistsSelection.write(writer);
		} else {
			writer.writeLine(0);
		}
		writer.writeLine((styleSearchText != null) ? styleSearchText : "");
		writer.writeLine((tagSearchText != null) ? tagSearchText : "");
		if (minBeatIntensity != null)
			writer.writeLine(minBeatIntensity.getBeatIntensityValue());
		else
			writer.writeLine(BeatIntensity.MIN_VALUE);
		if (maxBeatIntensity != null)
			writer.writeLine(maxBeatIntensity.getBeatIntensityValue());
		else
			writer.writeLine(BeatIntensity.MAX_VALUE);
		writer.writeLine(internalItemsOnly);
		writer.writeLine(externalItemsOnly);
		writer.writeLine(sortMinedHeader);
		writer.writeLine(minedHeaderCutoffTime);
		writer.writeLine(userDataFilters.size());
		for (UserDataTypeFilter userDataFilter : userDataFilters)
			userDataFilter.write(writer);
		writer.writeLine(userDataWeights.size());
		for (UserDataTypeWeight userDataWeight : userDataWeights)
			userDataWeight.write(writer);
		if (relativeProfileIds != null) {
			writer.writeLine(relativeProfileIds.length);
			for (int i = 0; i < relativeProfileIds.length; ++i) {
				writer.writeLine(relativeProfileIds[i]);
				if (relativeProfileIdentifiers[i] != null)
					writer.writeLine(relativeProfileIdentifiers[i].getUniqueId());
				else
					writer.writeLine("");
			}
		} else {
			writer.writeLine(String.valueOf(0));
		}
		writer.writeLine(enableRelativeSearch ? "1" : "0");
		writer.writeLine(maxDaysSinceLastAdded);
		if (userProfile != null) {
			writer.writeLine(1);
			userProfile.write(writer);
		} else {
			writer.writeLine(0);
		}
		writer.writeLine(String.valueOf(excludedIds.size()));
		for (int excludeId : excludedIds.keySet())
			writer.writeLine(String.valueOf(excludeId));
		writer.writeLine(String.valueOf(randomness));
		writer.writeLine(String.valueOf(computeUserPreferenceWithResultScore));
	}

	@Override
	public void addSearchFields(Vector<String> searchFields) {
		super.addSearchFields(searchFields);
		searchFields.add("style");
		searchFields.add("tag");
		searchFields.add("comment");
		for (UserDataType userType : ((SearchIndex)getIndex()).getUserDataTypes()) {
    		if (userType.getFieldType() == UserDataType.TYPE_TEXT_FIELD) {
    			searchFields.add(userType.getTitle().toLowerCase());
    		}
    	}
	}
}
