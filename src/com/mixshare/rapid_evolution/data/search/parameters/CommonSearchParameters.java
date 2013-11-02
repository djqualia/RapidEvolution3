package com.mixshare.rapid_evolution.data.search.parameters;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.RAMDirectory;

import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.DataConstantsHelper;
import com.mixshare.rapid_evolution.data.index.imdb.LocalIMDB;
import com.mixshare.rapid_evolution.data.record.CommonRecord;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.search.SearchParser;
import com.mixshare.rapid_evolution.data.search.SearchResult;
import com.mixshare.rapid_evolution.music.rating.Rating;
import com.mixshare.rapid_evolution.ui.model.ModelManagerInterface;
import com.mixshare.rapid_evolution.ui.model.column.AllColumns;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.column.comparables.SmartString;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

abstract public class CommonSearchParameters extends AbstractSearchParameters implements Serializable, DataConstants, AllColumns, Comparator {

    static private Logger log = Logger.getLogger(CommonSearchParameters.class);

    static protected String queryKeySeperator = "//";

    static public final byte SORT_BY_NOTHING = -1;
    static public final byte SORT_BY_SCORE = 0;
    static public final byte SORT_BY_MINED_HEADER = 1;
    static public final byte SORT_BY_RATING = 2;
    static public final byte SORT_BY_NUM_PLAYS = 3;
    static public final byte SORT_BY_INTERNAL_ITEMS = 4;
    static public final byte SORT_BY_DATE_ADDED = 5;
    static public final byte SORT_BY_POPULARITY = 6;
    static public final byte SORT_BY_DURATION = 7;
    static public final byte SORT_BY_USER_PREFERENCE = 8;
    static public final byte SORT_BY_LASTFM_REACH = 9;
    static public final byte SORT_BY_NAME = 10;
    static public final byte SORT_BY_YEAR = 11;
    static public final byte SORT_BY_FILTER_MATCH = 12;
    static public final byte SORT_BY_STYLE_MATCH = 13;
    static public final byte SORT_BY_TAG_MATCH = 14;
    static public final byte SORT_BY_NUM_ARTISTS = 15;
    static public final byte SORT_BY_NUM_LABELS = 16;
    static public final byte SORT_BY_NUM_RELEASES = 17;
    static public final byte SORT_BY_NUM_SONGS = 18;
    static public final byte SORT_BY_TRACK = 19;
    static public final byte SORT_BY_RESULT_SCORE = 20;
    static public final byte SORT_BY_BPM = 21;
    static public final byte SORT_BY_BPM_START = 22;
    static public final byte SORT_BY_BPM_END = 23;
    static public final byte SORT_BY_KEY = 24;
    static public final byte SORT_BY_KEY_START = 25;
    static public final byte SORT_BY_KEY_END = 26;
    static public final byte SORT_BY_KEYCODE = 27;
    static public final byte SORT_BY_KEYCODE_START = 28;
    static public final byte SORT_BY_KEYCODE_END = 29;
    static public final byte SORT_BY_BEAT_INTENSITY_DESCRIPTION = 30;
    static public final byte SORT_BY_BEAT_INTENSITY_VALUE = 31;
    static public final byte SORT_BY_RATING_STARS = 32;
    static public final byte SORT_BY_RATING_VALUE = 33;
    static public final byte SORT_BY_COMMENTS = 34;
    static public final byte SORT_BY_SIMILARITY = 35;
    static public final byte SORT_BY_ARTIST_DESCRIPTION = 36;
    static public final byte SORT_BY_LABEL_DESCRIPTION = 37;
    static public final byte SORT_BY_FEATURING_ARTISTS = 38;
    static public final byte SORT_BY_RELEASE_TITLES = 39;
    static public final byte SORT_BY_TITLE = 40;
    static public final byte SORT_BY_REMIX = 41;
    static public final byte SORT_BY_TIME_SIGNATURE = 42;
    static public final byte SORT_BY_BPM_ACCURACY = 43;
    static public final byte SORT_BY_KEY_ACCURACY = 44;
    static public final byte SORT_BY_NUM_MIXOUTS = 45;
    static public final byte SORT_BY_STYLES = 46;
    static public final byte SORT_BY_TAGS = 47;
    static public final byte SORT_BY_LAST_MODIFIED = 48;
    static public final byte SORT_BY_FILENAME = 49;
    static public final byte SORT_BY_FILEPATH = 50;
    static public final byte SORT_BY_RELEASE_TITLE = 51;
    static public final byte SORT_BY_BEAT_INTENSITY_VARIANCE_DESCRIPTION = 52;
    static public final byte SORT_BY_BEAT_INTENSITY_VARIANCE_VALUE = 53;
    static public final byte SORT_BY_COMPILATION = 54;
    static public final byte SORT_BY_BPM_PERCENT_DIFF = 55;
    static public final byte SORT_BY_BPM_PERCENT_SHIFT = 56;
    static public final byte SORT_BY_KEY_RELATION= 57;
    static public final byte SORT_BY_KEY_LOCK = 58;
    static public final byte SORT_BY_PITCH_SHIFT = 59;
    static public final byte SORT_BY_ACTUAL_KEY = 60;
    static public final byte SORT_BY_ACTUAL_KEY_CODE = 61;
    static public final byte SORT_BY_DEGREE = 62;
    static public final byte SORT_BY_PLAYLIST_POSITION = 63;
    static public final byte SORT_BY_KEY_CLOSENESS = 64;

    static public byte getSortTypeFromDescription(String name, ModelManagerInterface modelManager) {
    	for (int c = 0; c < modelManager.getNumColumns(); ++c) {
    		Column viewColumn = modelManager.getViewColumnType(c);
    		if (viewColumn.getColumnTitleId().equals(name))
    			return getSortTypeFromColumnId(viewColumn.getColumnId());
    	}
    	return SORT_BY_NOTHING;
    }

    static public byte getSortTypeFromColumnId(int columnId) {
		if (columnId == COLUMN_DATE_ADDED.getColumnId())
			return SORT_BY_DATE_ADDED;
		else if (columnId == COLUMN_RATING_STARS.getColumnId())
			return SORT_BY_RATING_STARS;
		else if (columnId == COLUMN_RATING_VALUE.getColumnId())
			return SORT_BY_RATING_VALUE;
		else if (columnId == COLUMN_SCORE.getColumnId())
			return SORT_BY_SCORE;
		else if (columnId == COLUMN_POPULARITY.getColumnId())
			return SORT_BY_POPULARITY;
		else if (columnId == COLUMN_NUM_PLAYS.getColumnId())
			return SORT_BY_NUM_PLAYS;
		else if (columnId == COLUMN_DURATION.getColumnId())
			return SORT_BY_DURATION;
		else if (columnId == COLUMN_ORIGINAL_YEAR.getColumnId())
			return SORT_BY_YEAR;
		else if (columnId == COLUMN_FILTERS_MATCH.getColumnId())
			return SORT_BY_FILTER_MATCH;
		else if (columnId == COLUMN_STYLES_MATCH.getColumnId())
			return SORT_BY_STYLE_MATCH;
		else if (columnId == COLUMN_TAGS_MATCH.getColumnId())
			return SORT_BY_TAG_MATCH;
		else if (columnId == COLUMN_SONG_DESCRIPTION.getColumnId())
			return SORT_BY_NAME;
		else if (columnId == COLUMN_RELEASE_DESCRIPTION.getColumnId())
			return SORT_BY_NAME;
		else if (columnId == COLUMN_LABEL_NAME.getColumnId())
			return SORT_BY_NAME;
		else if (columnId == COLUMN_ARTIST_NAME.getColumnId())
			return SORT_BY_NAME;
		else if (columnId == COLUMN_PREFERENCE.getColumnId())
			return SORT_BY_USER_PREFERENCE;
		else if (columnId == COLUMN_NUM_ARTISTS.getColumnId())
			return SORT_BY_NUM_ARTISTS;
		else if (columnId == COLUMN_NUM_LABELS.getColumnId())
			return SORT_BY_NUM_LABELS;
		else if (columnId == COLUMN_NUM_RELEASES.getColumnId())
			return SORT_BY_NUM_RELEASES;
		else if (columnId == COLUMN_NUM_SONGS.getColumnId())
			return SORT_BY_NUM_SONGS;
		else if (columnId == COLUMN_TRACK.getColumnId())
			return SORT_BY_TRACK;
		else if (columnId == COLUMN_BPM.getColumnId())
			return SORT_BY_BPM;
		else if (columnId == COLUMN_BPM_START.getColumnId())
			return SORT_BY_BPM_START;
		else if (columnId == COLUMN_BPM_END.getColumnId())
			return SORT_BY_BPM_END;
		else if (columnId == COLUMN_KEY.getColumnId())
			return SORT_BY_KEY;
		else if (columnId == COLUMN_KEY_START.getColumnId())
			return SORT_BY_KEY_START;
		else if (columnId == COLUMN_KEY_END.getColumnId())
			return SORT_BY_KEY_END;
		else if (columnId == COLUMN_KEYCODE.getColumnId())
			return SORT_BY_KEYCODE;
		else if (columnId == COLUMN_KEYCODE_START.getColumnId())
			return SORT_BY_KEYCODE_START;
		else if (columnId == COLUMN_KEYCODE_END.getColumnId())
			return SORT_BY_KEYCODE_END;
		else if (columnId == COLUMN_BEAT_INTENSITY_DESCRIPTION.getColumnId())
			return SORT_BY_BEAT_INTENSITY_DESCRIPTION;
		else if (columnId == COLUMN_BEAT_INTENSITY_VALUE.getColumnId())
			return SORT_BY_BEAT_INTENSITY_VALUE;
		else if (columnId == COLUMN_COMMENTS.getColumnId())
			return SORT_BY_COMMENTS;
		else if (columnId == COLUMN_SIMILARITY.getColumnId())
			return SORT_BY_SIMILARITY;
		else if (columnId == COLUMN_ARTIST_DESCRIPTION.getColumnId())
			return SORT_BY_ARTIST_DESCRIPTION;
		else if (columnId == COLUMN_LABELS.getColumnId())
			return SORT_BY_LABEL_DESCRIPTION;
		else if (columnId == COLUMN_FEATURING_ARTISTS.getColumnId())
			return SORT_BY_FEATURING_ARTISTS;
		else if (columnId == COLUMN_RELEASE_TITLES.getColumnId())
			return SORT_BY_RELEASE_TITLES;
		else if (columnId == COLUMN_TITLE.getColumnId())
			return SORT_BY_TITLE;
		else if (columnId == COLUMN_REMIX.getColumnId())
			return SORT_BY_REMIX;
		else if (columnId == COLUMN_TIME_SIGNATURE.getColumnId())
			return SORT_BY_TIME_SIGNATURE;
		else if (columnId == COLUMN_BPM_ACCURACY.getColumnId())
			return SORT_BY_BPM_ACCURACY;
		else if (columnId == COLUMN_KEY_ACCURACY.getColumnId())
			return SORT_BY_KEY_ACCURACY;
		else if (columnId == COLUMN_NUM_MIXOUTS.getColumnId())
			return SORT_BY_NUM_MIXOUTS;
		else if (columnId == COLUMN_STYLES.getColumnId())
			return SORT_BY_STYLES;
		else if (columnId == COLUMN_TAGS.getColumnId())
			return SORT_BY_TAGS;
		else if (columnId == COLUMN_LAST_MODIFIED.getColumnId())
			return SORT_BY_LAST_MODIFIED;
		else if (columnId == COLUMN_FILENAME.getColumnId())
			return SORT_BY_FILENAME;
		else if (columnId == COLUMN_FILEPATH.getColumnId())
			return SORT_BY_FILEPATH;
		else if (columnId == COLUMN_RELEASE_TITLE.getColumnId())
			return SORT_BY_RELEASE_TITLE;
		else if (columnId == COLUMN_BEAT_INTENSITY_VARIANCE_VALUE.getColumnId())
			return SORT_BY_BEAT_INTENSITY_VARIANCE_VALUE;
		else if (columnId == COLUMN_BEAT_INTENSITY_VARIANCE_DESCRIPTION.getColumnId())
			return SORT_BY_BEAT_INTENSITY_VARIANCE_DESCRIPTION;
		else if (columnId == COLUMN_RELEASE_IS_COMPILATION.getColumnId())
			return SORT_BY_COMPILATION;
		else if (columnId == COLUMN_BPM_DIFFERENCE.getColumnId())
			return SORT_BY_BPM_PERCENT_DIFF;
		else if (columnId == COLUMN_BPM_SHIFT.getColumnId())
			return SORT_BY_BPM_PERCENT_SHIFT;
		else if (columnId == COLUMN_KEY_RELATION.getColumnId())
			return SORT_BY_KEY_RELATION;
		else if (columnId == COLUMN_KEY_LOCK.getColumnId())
			return SORT_BY_KEY_LOCK;
		else if (columnId == COLUMN_PITCH_SHIFT.getColumnId())
			return SORT_BY_PITCH_SHIFT;
		else if (columnId == COLUMN_ACTUAL_KEY.getColumnId())
			return SORT_BY_ACTUAL_KEY;
		else if (columnId == COLUMN_ACTUAL_KEYCODE.getColumnId())
			return SORT_BY_ACTUAL_KEY_CODE;
		else if (columnId == COLUMN_DEGREE.getColumnId())
			return SORT_BY_DEGREE;
		else if (columnId == COLUMN_PLAYLIST_POSITION.getColumnId())
			return SORT_BY_PLAYLIST_POSITION;
		else if (columnId == COLUMN_KEY_CLOSENESS.getColumnId())
			return SORT_BY_KEY_CLOSENESS;
		return SORT_BY_NOTHING;
    }

    static public Short getColumnIdFromSortType(byte sortType, byte dataType) {
    	if (sortType == SORT_BY_SCORE)
    		return COLUMN_SCORE.getColumnId();
    	if (sortType == SORT_BY_RATING)
    		return COLUMN_RATING_STARS.getColumnId();
    	if (sortType == SORT_BY_NUM_PLAYS)
    		return COLUMN_NUM_PLAYS.getColumnId();
    	if (sortType == SORT_BY_DATE_ADDED)
    		return COLUMN_DATE_ADDED.getColumnId();
    	if (sortType == SORT_BY_POPULARITY)
    		return COLUMN_POPULARITY.getColumnId();
    	if (sortType == SORT_BY_DURATION)
    		return COLUMN_DURATION.getColumnId();
    	if (sortType == SORT_BY_USER_PREFERENCE)
    		return COLUMN_PREFERENCE.getColumnId();
    	if (sortType == SORT_BY_NAME) {
    		if (dataType == DATA_TYPE_ARTISTS)
    			return COLUMN_ARTIST_NAME.getColumnId();
    		if (dataType == DATA_TYPE_LABELS)
    			return COLUMN_LABEL_NAME.getColumnId();
    		if (dataType == DATA_TYPE_RELEASES)
    			return COLUMN_RELEASE_DESCRIPTION.getColumnId();
    		if (dataType == DATA_TYPE_SONGS)
    			return COLUMN_SONG_DESCRIPTION.getColumnId();
    	}
    	if (sortType == SORT_BY_YEAR)
    		return COLUMN_ORIGINAL_YEAR.getColumnId();
    	if (sortType == SORT_BY_FILTER_MATCH)
    		return COLUMN_FILTERS_MATCH.getColumnId();
    	if (sortType == SORT_BY_STYLE_MATCH)
    		return COLUMN_STYLES_MATCH.getColumnId();
    	if (sortType == SORT_BY_TAG_MATCH)
    		return COLUMN_TAGS_MATCH.getColumnId();
    	if (sortType == SORT_BY_NUM_ARTISTS)
    		return COLUMN_NUM_ARTISTS.getColumnId();
    	if (sortType == SORT_BY_NUM_LABELS)
    		return COLUMN_NUM_LABELS.getColumnId();
    	if (sortType == SORT_BY_NUM_RELEASES)
    		return COLUMN_NUM_RELEASES.getColumnId();
    	if (sortType == SORT_BY_NUM_SONGS)
    		return COLUMN_NUM_SONGS.getColumnId();
    	if (sortType == SORT_BY_TRACK)
    		return COLUMN_TRACK.getColumnId();
    	if (sortType == SORT_BY_BPM)
    		return COLUMN_BPM.getColumnId();
    	if (sortType == SORT_BY_BPM_START)
    		return COLUMN_BPM_START.getColumnId();
    	if (sortType == SORT_BY_BPM_END)
    		return COLUMN_BPM_END.getColumnId();
    	if (sortType == SORT_BY_KEY)
    		return COLUMN_KEY.getColumnId();
    	if (sortType == SORT_BY_KEY_START)
    		return COLUMN_KEY_START.getColumnId();
    	if (sortType == SORT_BY_KEY_END)
    		return COLUMN_KEY_END.getColumnId();
    	if (sortType == SORT_BY_KEYCODE)
    		return COLUMN_KEYCODE.getColumnId();
    	if (sortType == SORT_BY_KEYCODE_START)
    		return COLUMN_KEYCODE_START.getColumnId();
    	if (sortType == SORT_BY_KEYCODE_END)
    		return COLUMN_KEYCODE_END.getColumnId();
    	if (sortType == SORT_BY_BEAT_INTENSITY_DESCRIPTION)
    		return COLUMN_BEAT_INTENSITY_DESCRIPTION.getColumnId();
    	if (sortType == SORT_BY_BEAT_INTENSITY_VALUE)
    		return COLUMN_BEAT_INTENSITY_VALUE.getColumnId();
    	if (sortType == SORT_BY_RATING_STARS)
    		return COLUMN_RATING_STARS.getColumnId();
    	if (sortType == SORT_BY_RATING_VALUE)
    		return COLUMN_RATING_VALUE.getColumnId();
    	if (sortType == SORT_BY_COMMENTS)
    		return COLUMN_COMMENTS.getColumnId();
		else if (sortType == SORT_BY_SIMILARITY)
			return COLUMN_SIMILARITY.getColumnId();
		else if (sortType == SORT_BY_ARTIST_DESCRIPTION)
			return COLUMN_ARTIST_DESCRIPTION.getColumnId();
		else if (sortType == SORT_BY_LABEL_DESCRIPTION)
			return COLUMN_LABELS.getColumnId();
		else if (sortType == SORT_BY_FEATURING_ARTISTS)
			return COLUMN_FEATURING_ARTISTS.getColumnId();
		else if (sortType == SORT_BY_RELEASE_TITLES)
			return COLUMN_RELEASE_TITLES.getColumnId();
		else if (sortType == SORT_BY_TITLE)
			return COLUMN_TITLE.getColumnId();
		else if (sortType == SORT_BY_REMIX)
			return COLUMN_REMIX.getColumnId();
		else if (sortType == SORT_BY_TIME_SIGNATURE)
			return COLUMN_TIME_SIGNATURE.getColumnId();
		else if (sortType == SORT_BY_KEY_ACCURACY)
			return COLUMN_KEY_ACCURACY.getColumnId();
		else if (sortType == SORT_BY_BPM_ACCURACY)
			return COLUMN_BPM_ACCURACY.getColumnId();
		else if (sortType == SORT_BY_NUM_MIXOUTS)
			return COLUMN_NUM_MIXOUTS.getColumnId();
		else if (sortType == SORT_BY_STYLES)
			return COLUMN_STYLES.getColumnId();
		else if (sortType == SORT_BY_TAGS)
			return COLUMN_TAGS.getColumnId();
		else if (sortType == SORT_BY_LAST_MODIFIED)
			return COLUMN_LAST_MODIFIED.getColumnId();
		else if (sortType == SORT_BY_FILENAME)
			return COLUMN_FILENAME.getColumnId();
		else if (sortType == SORT_BY_FILEPATH)
			return COLUMN_FILEPATH.getColumnId();
		else if (sortType == SORT_BY_RELEASE_TITLE)
			return COLUMN_RELEASE_TITLE.getColumnId();
		else if (sortType == SORT_BY_BEAT_INTENSITY_VARIANCE_VALUE)
			return COLUMN_BEAT_INTENSITY_VARIANCE_VALUE.getColumnId();
		else if (sortType == SORT_BY_BEAT_INTENSITY_VARIANCE_DESCRIPTION)
			return COLUMN_BEAT_INTENSITY_VARIANCE_DESCRIPTION.getColumnId();
		else if (sortType == SORT_BY_COMPILATION)
			return COLUMN_RELEASE_IS_COMPILATION.getColumnId();
		else if (sortType == SORT_BY_BPM_PERCENT_DIFF)
			return COLUMN_BPM_DIFFERENCE.getColumnId();
		else if (sortType == SORT_BY_BPM_PERCENT_SHIFT)
			return COLUMN_BPM_SHIFT.getColumnId();
		else if (sortType == SORT_BY_KEY_RELATION)
			return COLUMN_KEY_RELATION.getColumnId();
		else if (sortType == SORT_BY_KEY_LOCK)
			return COLUMN_KEY_LOCK.getColumnId();
		else if (sortType == SORT_BY_PITCH_SHIFT)
			return COLUMN_PITCH_SHIFT.getColumnId();
		else if (sortType == SORT_BY_ACTUAL_KEY)
			return COLUMN_ACTUAL_KEY.getColumnId();
		else if (sortType == SORT_BY_ACTUAL_KEY_CODE)
			return COLUMN_ACTUAL_KEYCODE.getColumnId();
		else if (sortType == SORT_BY_DEGREE)
			return COLUMN_DEGREE.getColumnId();
		else if (sortType == SORT_BY_PLAYLIST_POSITION)
			return COLUMN_PLAYLIST_POSITION.getColumnId();
		else if (sortType == SORT_BY_KEY_CLOSENESS)
			return COLUMN_KEY_CLOSENESS.getColumnId();
    	return null;
    }

    ////////////
    // FIELDS //
    ////////////

    private String searchText;

    private Rating minRating;
    private boolean includeUnrated;
    private boolean excludeRated;
    private boolean showDisabled;

    protected byte[] sortType;
    protected boolean[] sortDescending;

    transient protected Map<Integer, Float> lastSearchResultScores;

    //////////////////
    // CONSTRUCTORS //
    //////////////////

	public CommonSearchParameters() { }

    public CommonSearchParameters(CommonSearchParameters copy) {
    	this.searchText = copy.searchText;
    	this.minRating = copy.minRating;
    	this.includeUnrated = copy.includeUnrated;
    	this.excludeRated = copy.excludeRated;
    	this.showDisabled = copy.showDisabled;
    	this.sortType = copy.sortType;
    	this.sortDescending = copy.sortDescending;
    }

    public CommonSearchParameters(LineReader lineReader) {
    	int version = Integer.parseInt(lineReader.getNextLine());
    	searchText = lineReader.getNextLine();
    	byte ratingValue = Byte.parseByte(lineReader.getNextLine());
    	if (ratingValue > 0)
    		minRating = Rating.getRating(ratingValue);
    	includeUnrated = Boolean.parseBoolean(lineReader.getNextLine());
    	excludeRated = Boolean.parseBoolean(lineReader.getNextLine());
    	showDisabled = Boolean.parseBoolean(lineReader.getNextLine());
    	int sortLength = Integer.parseInt(lineReader.getNextLine());
    	if (sortLength > 0) {
    		sortType = new byte[sortLength];
    		sortDescending = new boolean[sortLength];
    		for (int i = 0; i < sortLength; ++i)
    			sortType[i] = Byte.parseByte(lineReader.getNextLine());
    		for (int i = 0; i < sortLength; ++i)
    			sortDescending[i] = Boolean.parseBoolean(lineReader.getNextLine());
    	}
    }


    //////////////////////
    // ABSTRACT METHODS //
    //////////////////////

    /////////////
    // GETTERS //
    /////////////

    public String getSearchText() { return (searchText != null) ? searchText : ""; }

	public Rating getMinRating() { return minRating; }
	public boolean includeUnrated() { return includeUnrated; }
	public boolean excludeRated() { return excludeRated; }

    public byte[] getSortType() { return sortType; }
	public boolean[] isSortDescending() { return sortDescending; }

    @Override
	public String toString() { return getUniqueHash(); }

    /**
     * Sub-classes implementing this method should make sure to call super to this to include common parameters...
     */
    @Override
	public String getUniqueHash() {
    	StringBuffer result = new StringBuffer();
    	result.append(String.valueOf(getDataType()));
    	result.append(queryKeySeperator);
    	result.append((searchText != null) ? searchText : "");
    	result.append(queryKeySeperator);
    	result.append(minRating != null ? String.valueOf(minRating.getRatingValue()) : "");
    	result.append(queryKeySeperator);
    	result.append(String.valueOf(includeUnrated));
    	result.append(queryKeySeperator);
    	result.append(String.valueOf(excludeRated));
    	result.append(queryKeySeperator);
    	result.append(String.valueOf(showDisabled));
    	result.append(queryKeySeperator);
    	result.append(String.valueOf(sortType != null ? sortType.length : 0));
    	result.append(queryKeySeperator);
    	if (sortType != null) {
    		for (byte sortTypeByte : sortType)
    			result.append(String.valueOf(sortTypeByte));
    	}
    	result.append(queryKeySeperator);
    	result.append(String.valueOf(sortDescending != null ? sortDescending.length : 0));
    	result.append(queryKeySeperator);
    	if (sortDescending != null) {
    		for (boolean sortDescendingBool : sortDescending)
    			result.append(String.valueOf(sortDescendingBool));
    	}
    	return result.toString();
    }

	// for serialization
	public boolean isIncludeUnrated() { return includeUnrated; }
	public boolean isExcludeRated() { return excludeRated; }
	public boolean isShowDisabled() { return showDisabled; }

    /////////////
    // SETTERS //
    /////////////

    public void setSearchText(String searchText) { this.searchText = searchText; }

	public void setMinRating(Rating rating) { this.minRating = rating; }

	public void setIncludeUnrated(boolean value) { includeUnrated = value; }
	public void setExcludeRated(boolean value) { excludeRated = value; }
	public void setShowDisabled(boolean showDisabled) { this.showDisabled = showDisabled; }

	public void setSortType(byte[] sortType) {
		if (log.isTraceEnabled())
			log.trace("setSortType(): type=" + DataConstantsHelper.getDataTypeDescription(getDataType()));
		this.sortType = sortType;
		if (sortDescending == null)
			sortDescending = new boolean[sortType.length];
	}
	public void setSortDescending(boolean[] sortDescending) { this.sortDescending = sortDescending; }

	/////////////
	// METHODS //
	/////////////

	@Override
	public boolean isEmpty(boolean countIndexed) {
		if (countIndexed) {
			if ((searchText != null) && (searchText.length() > 0))
				return false;
		}
		if (showDisabled)
			return false;
		if ((minRating != null) && (minRating.getRatingValue() > 0))
			return false;
		if (excludeRated)
			return false;
		//if ((sortType != null) && (sortType.length > 0))
			//return false;
		//if ((sortDescending != null) && (sortDescending.length > 0))
			//return false;
		return true;
	}

	@Override
	public boolean isEmpty() { return isEmpty(true); }


	@Override
	public float matches(Record record) {
		return matches(record, true);
	}
	@Override
	public float matches(Record record, boolean fullCheck) {
		float score = matchesSub(record, fullCheck);
		if (score > 0.0f)
			score *= computeWeight(record);
		return score;
	}
	@Override
	public float matchesSub(Record record, boolean fullCheck) {
		if (record == null)
			return 0.0f;
		CommonRecord searchRecord = (CommonRecord)record;
		float score = 0.05f;
		if (!showDisabled && searchRecord.isDisabled())
			return 0.0f;
		if (excludeRated) {
			if (searchRecord.getRatingValue().isValid())
				return 0.0f;
		}
		if (getMinRating() != null) {
			if (searchRecord.getRatingValue().getRatingValue() < getMinRating().getRatingValue()) {
				if (searchRecord.getRatingValue().isValid() || !includeUnrated)
					return 0.0f;
			}
		}
		if (fullCheck) {
			if ((getSearchText() != null) && (getSearchText().length() > 0)) {
				float match = getSearchFieldsMatch(record, getSearchFields(), getSearchText());
				if (match == 0.0f)
					return 0.0f;
				score += match;
			}
		}
		return score;
	}
	@Override
	public float computeWeight(Record record) { return 1.0f; }

	@Override
	public int compare(Object o1, Object o2) {
		if ((o1 instanceof SearchResult) && (o2 instanceof SearchResult))
			return compare((SearchResult)o1, (SearchResult)o2);
		return 0;
	}
	@Override
	public int compare(SearchResult r1, SearchResult r2) {
		if ((sortType == null) || (sortType.length == 0))
			return compareSub(r1, r2, (byte)0);
		for (int i = 0; i < sortType.length; ++i) {
			int cmp = compareSub(r1, r2, sortType[i]);
			if (sortDescending[i])
				cmp = -cmp;
			if (cmp != 0)
				return cmp;
		}
		int u1 = r1.getRecord().getUniqueId();
		int u2 = r2.getRecord().getUniqueId();
		if (u1 < u2)
			return -1;
		if (u1 > u2)
			return -1;
		return 0;
	}

	@Override
	protected int compareSub(SearchResult r1, SearchResult r2, byte sortType) {
		CommonRecord c1 = (CommonRecord)r1.getRecord();
		CommonRecord c2 = (CommonRecord)r2.getRecord();
		if ((sortType == SORT_BY_RATING) || (sortType == SORT_BY_RATING_VALUE)) {
			if (c1.getRating() > c2.getRating())
				return -1;
			if (c1.getRating() < c2.getRating())
				return 1;
			return 0;
		} else if (sortType == SORT_BY_RATING_STARS) {
			if (c1.getRatingValue().getRatingStars() > c2.getRatingValue().getRatingStars())
				return -1;
			if (c1.getRatingValue().getRatingStars() < c2.getRatingValue().getRatingStars())
				return 1;
			return 0;
		} else if (sortType == SORT_BY_NAME) {
			return SmartString.compareStrings(c1.toString(), c2.toString());
		} else if (sortType == SORT_BY_RESULT_SCORE) {
			if (r1.getScore() > r2.getScore())
				return -1;
			if (r1.getScore() < r2.getScore())
				return 1;
			return 0;
		}
		// default to search result score
		if (r1.getScore() > r2.getScore())
			return -1;
		if (r1.getScore() < r2.getScore())
			return 1;
		return 0;
	}

	@Override
	public void write(LineWriter writer) {
		writer.writeLine(1);
		if (searchText != null)
			writer.writeLine(searchText);
		else
			writer.writeLine("");
		if (minRating != null)
			writer.writeLine(minRating.getRatingValue());
		else
			writer.writeLine(0);
		writer.writeLine(includeUnrated);
		writer.writeLine(excludeRated);
		writer.writeLine(showDisabled);
		if (sortType != null) {
			writer.writeLine(sortType.length);
			for (byte value : sortType)
				writer.writeLine(value);
			for (boolean descending : sortDescending)
				writer.writeLine(descending);
		} else {
			writer.writeLine(0);
		}
	}

	@Override
	public String[] getSearchFields() {
		Vector<String> fields = new Vector<String>();
		addSearchFields(fields);
		String[] result = new String[fields.size()];
		int i = 0;
		for (String field : fields)
			result[i++] = field;
		return result;
	}

	@Override
	public void addSearchFields(Vector<String> searchFields) {
		searchFields.add("duplicate_identifiers");
	}

	protected float getSearchFieldsMatch(Record record, String[] searchFields, String searchText) {
		float score = 0.0f;
		try {
			RAMDirectory idx = new RAMDirectory();
			IndexWriter writer = new IndexWriter(idx, new StandardAnalyzer(LocalIMDB.LUCENE_VERSION), IndexWriter.MaxFieldLength.UNLIMITED);
			writer.addDocument(record.getDocument());
			writer.close();
			Searcher searcher = new IndexSearcher(idx);

			Query query = new SearchParser(searchText).getQuery(searchFields);
			TopDocs topDocs = searcher.search(query, 1);
			if ((topDocs != null) && (topDocs.totalHits > 0))
				score = topDocs.scoreDocs[0].score;

			searcher.close();
			idx.close();
		} catch (Exception e) {
			log.error("doesMatchSearchFields(): error", e);
		}
		return score;
	}

	@Override
	public void clearLastSearchResultScores() {
		lastSearchResultScores = null;
	}
	@Override
	public void initLastSearchResultScore(int size) {
		lastSearchResultScores = new HashMap<Integer, Float>(size);
	}
	@Override
	public void addLastSearchResultScore(int uniqueId, float score) {
		lastSearchResultScores.put(uniqueId, score);
	}
	@Override
	public float getLastSearchResultScore(Record record) {
		try {
			if (lastSearchResultScores != null)
				if (lastSearchResultScores.containsKey(record.getUniqueId()))
					return lastSearchResultScores.get(record.getUniqueId());
		} catch (Exception e) {	}
		return 0.0f;
	}

}
