package com.mixshare.rapid_evolution.ui.model.search;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.index.event.IndexChangeListener;
import com.mixshare.rapid_evolution.data.index.search.SearchIndex;
import com.mixshare.rapid_evolution.data.index.search.SearchIndexModelPopulator;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.record.HierarchicalRecord;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.filter.FilterRecord;
import com.mixshare.rapid_evolution.data.record.filter.style.StyleRecord;
import com.mixshare.rapid_evolution.data.record.filter.tag.TagRecord;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.data.record.user.UserDataType;
import com.mixshare.rapid_evolution.data.search.parameters.SearchParameters;
import com.mixshare.rapid_evolution.data.user.UserProfile;
import com.mixshare.rapid_evolution.data.util.DegreeValue;
import com.mixshare.rapid_evolution.ui.RapidEvolution3UI;
import com.mixshare.rapid_evolution.ui.model.ModelPopulatorInterface;
import com.mixshare.rapid_evolution.ui.model.column.UserDataColumn;
import com.mixshare.rapid_evolution.ui.model.column.comparables.Percentage;
import com.mixshare.rapid_evolution.ui.model.column.comparables.SmartDate;
import com.mixshare.rapid_evolution.ui.model.column.comparables.SmartInteger;
import com.mixshare.rapid_evolution.ui.model.column.comparables.SmartLong;
import com.mixshare.rapid_evolution.ui.model.column.comparables.SmartString;
import com.mixshare.rapid_evolution.ui.model.filter.FilterSelection;
import com.mixshare.rapid_evolution.ui.model.profile.details.SearchDetailsModelManager;
import com.mixshare.rapid_evolution.ui.model.search.artist.ArtistModelManager;
import com.mixshare.rapid_evolution.ui.model.search.label.LabelModelManager;
import com.mixshare.rapid_evolution.ui.model.search.release.ReleaseModelManager;
import com.mixshare.rapid_evolution.ui.model.search.song.SongModelManager;
import com.mixshare.rapid_evolution.ui.model.table.RecordTableModelManager;
import com.mixshare.rapid_evolution.ui.updaters.model.table.TableModelColumnUpdater;
import com.mixshare.rapid_evolution.ui.widgets.common.rating.StarRating;
import com.mixshare.rapid_evolution.ui.widgets.profile.ProfileWidgetUI;
import com.mixshare.rapid_evolution.util.StringUtil;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.mixshare.rapid_evolution.util.timing.Semaphore;
import com.trolltech.qt.core.QObject;
import com.trolltech.qt.gui.QApplication;

abstract public class SearchModelManager extends RecordTableModelManager implements IndexChangeListener {

    static private Logger log = Logger.getLogger(SearchModelManager.class);


    static private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    static protected SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    ////////////
    // FIELDS //
    ////////////

	protected Vector<UserDataColumn> userDataTypeColumns = new Vector<UserDataColumn>();

	transient protected int[] selectedStyleIds;
	transient protected float[] selectedStyleDegrees;
	transient protected int[] selectedTagIds;
	transient protected float[] selectedTagDegrees;

	transient protected ModelPopulatorInterface searchIndexModelPopulator;

	transient Semaphore addColumnSem;

	transient SearchParameters currentSearchParams; // used to associate search match with results..

	static {
    	try{
    		// in order for the XMLEncoder to skip transient variables, this is needed
    		BeanInfo info = Introspector.getBeanInfo(SearchModelManager.class);
    		PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
    		for (int i = 0; i < propertyDescriptors.length; ++i) {
    			PropertyDescriptor pd = propertyDescriptors[i];
    			if (pd.getName().equals("selectedStyleIds") || pd.getName().equals("selectedStyleDegrees") || pd.getName().equals("selectedTagIds") || pd.getName().equals("selectedTagDegrees") || pd.getName().equals("searchIndexModelPopulator") || pd.getName().equals("addColumnSem")) {
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

    public SearchModelManager() { super(); }
	public SearchModelManager(LineReader lineReader) {
		super(lineReader);
		int version = Integer.parseInt(lineReader.getNextLine());
		int numUserColumns = Integer.parseInt(lineReader.getNextLine());
		for (int i = 0; i < numUserColumns; ++i)
			userDataTypeColumns.add(new UserDataColumn(lineReader));
	}

	//////////////////////
	// ABSTRACT METHODS //
	//////////////////////

	abstract public SearchDetailsModelManager getDetailsModelManager();

	/////////////
	// GETTERS //
	/////////////

	@Override
	public Object getSourceData(short columnId, Object record) {
		SearchRecord searchRecord = (SearchRecord)record;
		if (columnId == COLUMN_LAST_MODIFIED.getColumnId()) {
	        Date date = searchRecord.getLastModifiedDate();
	        return new SmartDate(dateTimeFormat.format(date), date.getTime());
		}
		if (columnId == COLUMN_RATING_VALUE.getColumnId())
			return new SmartInteger(searchRecord.getRatingValue().getRatingValue());
		if (columnId == COLUMN_RATING_STARS.getColumnId()) {
			if (RE3Properties.getBoolean("server_mode"))
				return searchRecord.getRatingValue().getRatingStars();
			return new StarRating(searchRecord.getRatingValue());
		} if (columnId == COLUMN_COMMENTS.getColumnId())
			return new SmartString(StringUtil.removeNewLines(searchRecord.getComments()));
		if (columnId == COLUMN_STYLES.getColumnId())
			return new SmartString(searchRecord.getActualStyleDescription());
		if (columnId == COLUMN_TAGS.getColumnId())
			return new SmartString(searchRecord.getActualTagDescription());
		if (columnId == COLUMN_SCORE.getColumnId())
			return new Percentage(searchRecord.getScore());
		if (columnId == COLUMN_POPULARITY.getColumnId())
			return new Percentage(searchRecord.getPopularity());
		if (columnId == COLUMN_NUM_PLAYS.getColumnId())
			return new SmartLong(searchRecord.getPlayCount());
		if (columnId == COLUMN_EXTERNAL_ITEM.getColumnId())
			return searchRecord.isExternalItem();
		if (columnId == COLUMN_TAGS_MATCH.getColumnId()) {
			return getTagMatch(selectedTagIds, searchRecord);
		}
		if (columnId == COLUMN_STYLES_MATCH.getColumnId()) {
			return getStyleMatch(selectedStyleIds, searchRecord);
		}
		if (columnId == COLUMN_FILTERS_MATCH.getColumnId()) {
			SearchProxyModel searchProxy = getSearchProxyModel();
			if (searchProxy != null) {
				SearchParameters searchParams = searchProxy.getSearchParameters();
				if (searchParams != null)
					return new Percentage(searchParams.getLastSearchResultScore(searchRecord));
			}
			if (currentSearchParams != null)
				return new Percentage(currentSearchParams.getLastSearchResultScore(searchRecord));
			return new Percentage(0.0f);
			//return getFilterMatch(selectedStyleIds, selectedTagIds, searchRecord);
		}
		if (columnId == COLUMN_SIMILARITY.getColumnId()) {
			if (!RE3Properties.getBoolean("server_mode")) {
				Profile currentProfile = ProfileWidgetUI.instance.getCurrentProfile();
				if (currentProfile instanceof SearchProfile) {
					if (currentProfile.getRecord().getClass().equals(searchRecord.getClass())) {
						return new Percentage(((SearchProfile)currentProfile).getSimilarity(searchRecord));
					}
				}
			}
			return new Percentage(0.0f);
		}
		if (columnId == COLUMN_THUMBNAIL_IMAGE.getColumnId())	{
			return searchRecord.getThumbnailImageFilename();
		} else if (columnId == COLUMN_PREFERENCE.getColumnId()) {
			UserProfile userProfile = Database.getUserProfile();
			if (userProfile != null)
				return new Percentage(Database.getUserProfile().computePreference(searchRecord));
			return new Percentage(0.0f);
		} else if (columnId == COLUMN_HAS_LASTFM_PROFILE.getColumnId()) {
			return searchRecord.hasMinedProfileHeader(DATA_SOURCE_LASTFM);
		} else if (columnId == COLUMN_HAS_MUSICBRAINZ_PROFILE.getColumnId()) {
			return searchRecord.hasMinedProfileHeader(DATA_SOURCE_MUSICBRAINZ);
		} else if (columnId == COLUMN_HAS_ECHONEST_PROFILE.getColumnId())
			return searchRecord.hasMinedProfileHeader(DATA_SOURCE_ECHONEST);
		else if (columnId == COLUMN_HAS_IDIOMAG_PROFILE.getColumnId())
			return searchRecord.hasMinedProfileHeader(DATA_SOURCE_IDIOMAG);
		else if (columnId == COLUMN_HAS_BBC_PROFILE.getColumnId())
			return searchRecord.hasMinedProfileHeader(DATA_SOURCE_BBC);
		else if (columnId == COLUMN_HAS_BILLBOARD_PROFILE.getColumnId())
			return searchRecord.hasMinedProfileHeader(DATA_SOURCE_BILLBOARD);
		else if (columnId == COLUMN_HAS_DISCOGS_PROFILE.getColumnId())
			return searchRecord.hasMinedProfileHeader(DATA_SOURCE_DISCOGS);
		else if (columnId == COLUMN_HAS_LYRICSFLY_PROFILE.getColumnId())
			return searchRecord.hasMinedProfileHeader(DATA_SOURCE_LYRICSFLY);
		else if (columnId == COLUMN_HAS_LYRICWIKI_PROFILE.getColumnId())
			return searchRecord.hasMinedProfileHeader(DATA_SOURCE_LYRICWIKI);
		else if (columnId == COLUMN_HAS_MIXSHARE_PROFILE.getColumnId())
			return searchRecord.hasMinedProfileHeader(DATA_SOURCE_MIXSHARE);
		else if (columnId == COLUMN_HAS_YAHOO_PROFILE.getColumnId())
			return searchRecord.hasMinedProfileHeader(DATA_SOURCE_YAHOO);
		else if (columnId == COLUMN_DATE_ADDED.getColumnId()) {
			Date date = searchRecord.getDateAddedDate();
			return new SmartDate(dateFormat.format(date), date.getTime());
		}

		for (UserDataColumn userColumn : getSearchIndex().getSearchModelManager().getUserDataTypeColumns()) {
			if (userColumn.getColumnId() == columnId) {
				Object userData = searchRecord.getUserData(userColumn.getUserDataType());
				if (userData != null) {
					if (userColumn.getUserDataType().getFieldType() == UserDataType.TYPE_TEXT_FIELD)
						return new SmartString(userData.toString());
					if (userColumn.getUserDataType().getFieldType() == UserDataType.TYPE_BOOLEAN_FLAG) {
						if (userData instanceof Boolean)
							return ((Boolean)userData).toString();
						return userData.toString();
					}
				}
			}
		}
		return null;
	}

	static public Percentage getStyleMatch(int[] selectedStyleIds, SearchRecord searchRecord) {
		if ((selectedStyleIds != null) && (selectedStyleIds.length > 0)) {
			float stylesMatch = getAverageMatch(selectedStyleIds, searchRecord.getActualStyleIds(), searchRecord.getActualStyleDegrees());
			return new Percentage(stylesMatch);
		} else if ((selectedStyleIds != null) && (selectedStyleIds.length > 0)) {
			return new Percentage(getAverageMatch(selectedStyleIds, searchRecord.getActualStyleIds(), searchRecord.getActualStyleDegrees()));
		}
		return Percentage.ZERO_PERCENT;
	}

	static public Percentage getTagMatch(int[] selectedTagIds, SearchRecord searchRecord) {
		if ((selectedTagIds != null) && (selectedTagIds.length > 0)) {
			float tagsMatch = getAverageMatch(selectedTagIds, searchRecord.getActualTagIds(), searchRecord.getActualTagDegrees());
			return new Percentage(tagsMatch);
		} else if ((selectedTagIds != null) && (selectedTagIds.length > 0)) {
			return new Percentage(getAverageMatch(selectedTagIds, searchRecord.getActualTagIds(), searchRecord.getActualTagDegrees()));
		}
		return Percentage.ZERO_PERCENT;
	}

	static public Percentage getFilterMatch(int[] selectedStyleIds, int[] selectedTagIds, SearchRecord searchRecord) {
		if (((selectedStyleIds != null) && (selectedStyleIds.length > 0)) && ((selectedTagIds != null) && (selectedTagIds.length > 0))) {
			float stylesMatch = getAverageMatch(selectedStyleIds, searchRecord.getActualStyleIds(), searchRecord.getActualStyleDegrees());
			float tagsMatch = getAverageMatch(selectedTagIds, searchRecord.getActualTagIds(), searchRecord.getActualTagDegrees());
			return new Percentage((stylesMatch + tagsMatch) / 2.0f);
		} else if ((selectedStyleIds != null) && (selectedStyleIds.length > 0)) {
			return new Percentage(getAverageMatch(selectedStyleIds, searchRecord.getActualStyleIds(), searchRecord.getActualStyleDegrees()));
		} else if ((selectedTagIds != null) && (selectedTagIds.length > 0)) {
			return new Percentage(getAverageMatch(selectedTagIds, searchRecord.getActualTagIds(), searchRecord.getActualTagDegrees()));
		}
		return Percentage.ZERO_PERCENT;
	}

	@Override
	public boolean excludeInternalItems() { return false; }

	static public float getAverageMatch(int[] searchIds, int[] recordIds, float[] recordDegrees) {
		float avgMatch = 0.0f;
		int numSearchIds = 0;
		for (int searchId : searchIds) {
			boolean found = false;
			int i = 0;
			while ((i < recordIds.length) && !found) {
				if (recordIds[i] == searchId) {
					found = true;
					avgMatch += recordDegrees[i];
				}
				++i;
			}
			++numSearchIds;
		}
		if (numSearchIds > 0)
			return avgMatch / numSearchIds;
		return 1.0f;
	}

	public Vector<UserDataColumn> getUserDataTypeColumns() { return userDataTypeColumns; }

	public SearchIndex getSearchIndex() { return (SearchIndex)getIndex(); }

	@Override
	public ModelPopulatorInterface getModelPopulator() {
		if (searchIndexModelPopulator == null)
			searchIndexModelPopulator = new SearchIndexModelPopulator(getSearchIndex());
		return searchIndexModelPopulator;
	}

	public SearchProxyModel getSearchProxyModel() { return (SearchProxyModel)proxyModel; }

	public Semaphore getAddColumnSem() {
		if (addColumnSem == null)
			addColumnSem = new Semaphore(1);
		return addColumnSem;
	}

	/////////////
	// SETTERS //
	/////////////

	public void setSelectedStyles(FilterSelection StyleSelection) {
    	Map<Integer, Float> uniqueStyles = new HashMap<Integer, Float>(StyleSelection.size());
		for (FilterRecord filter : StyleSelection.getRequiredFilters())
			mergeStyleAndAllParents(filter.getUniqueId(), 1.0f, uniqueStyles);
		for (FilterRecord filter : StyleSelection.getOptionalFilters())
			mergeStyleAndAllParents(filter.getUniqueId(), 1.0f, uniqueStyles);
		Vector<DegreeValue> result = new Vector<DegreeValue>(uniqueStyles.size());
    	for (Entry<Integer, Float> entry : uniqueStyles.entrySet())
    		result.add(new DegreeValue(entry.getKey(), entry.getValue(), DATA_SOURCE_COMPUTED));
		java.util.Collections.sort(result);
		selectedStyleIds = new int[result.size()];
		int i = 0;
		for (DegreeValue StyleDegree : result)
			selectedStyleIds[i++] = (Integer)StyleDegree.getObject();
			++i;
		if (log.isTraceEnabled())
			log.trace("setSelectedStyles(): refreshing columns...");
		if (isColumnVisible(COLUMN_STYLES_MATCH))
			refreshColumn(COLUMN_STYLES_MATCH);
		if (isColumnVisible(COLUMN_FILTERS_MATCH))
			refreshColumn(COLUMN_FILTERS_MATCH);
		if (log.isTraceEnabled())
			log.trace("setSelectedStyles(): done");
	}
    private void mergeStyleAndAllParents(int StyleId, float StyleDegree, Map<Integer, Float> uniqueStyles) {
    	StyleRecord actualStyleRecord = Database.getStyleIndex().getStyleRecord(StyleId);
    	if (actualStyleRecord != null) {
        	Float existingPercenStylee = uniqueStyles.get(actualStyleRecord.getUniqueId());
        	boolean addParents = false;
        	if (existingPercenStylee == null) {
        		existingPercenStylee = StyleDegree;
        		addParents = true;
        	} else {
        		if (StyleDegree > existingPercenStylee)
        			addParents = true;
        		existingPercenStylee = Math.max(existingPercenStylee, StyleDegree);
        	}
    		uniqueStyles.put(actualStyleRecord.getUniqueId(), existingPercenStylee);
    		if (addParents) {
    			HierarchicalRecord[] parentStyles = actualStyleRecord.getParentRecords();
    			for (HierarchicalRecord parentStyle : parentStyles) {
    				if ((parentStyle != null) && !parentStyle.isRoot())
    					mergeStyleAndAllParents(parentStyle.getUniqueId(), StyleDegree, uniqueStyles);
    			}
    		}
    	}
    }

	public void setSelectedTags(FilterSelection tagSelection) {
    	Map<Integer, Float> uniqueTags = new HashMap<Integer, Float>(tagSelection.size());
		for (FilterRecord filter : tagSelection.getRequiredFilters())
			mergeTagAndAllParents(filter.getUniqueId(), 1.0f, uniqueTags);
		for (FilterRecord filter : tagSelection.getOptionalFilters())
			mergeTagAndAllParents(filter.getUniqueId(), 1.0f, uniqueTags);
		Vector<DegreeValue> result = new Vector<DegreeValue>(uniqueTags.size());
    	for (Entry<Integer, Float> entry : uniqueTags.entrySet())
    		result.add(new DegreeValue(entry.getKey(), entry.getValue(), DATA_SOURCE_COMPUTED));
		java.util.Collections.sort(result);
		selectedTagIds = new int[result.size()];
		int i = 0;
		for (DegreeValue tagDegree : result)
			selectedTagIds[i++] = (Integer)tagDegree.getObject();
		if (log.isTraceEnabled())
			log.trace("setSelectedTags(): refreshing columns...");
		if (isColumnVisible(COLUMN_TAGS_MATCH))
			refreshColumn(COLUMN_TAGS_MATCH);
		if (isColumnVisible(COLUMN_FILTERS_MATCH))
			refreshColumn(COLUMN_FILTERS_MATCH);
		if (log.isTraceEnabled())
			log.trace("setSelectedTags(): done");
	}
    private void mergeTagAndAllParents(int tagId, float tagDegree, Map<Integer, Float> uniqueTags) {
    	TagRecord actualTagRecord = Database.getTagIndex().getTagRecord(tagId);
    	if (actualTagRecord != null) {
        	Float existingPercentage = uniqueTags.get(actualTagRecord.getUniqueId());
        	boolean addParents = false;
        	if (existingPercentage == null) {
        		existingPercentage = tagDegree;
        		addParents = true;
        	} else {
        		if (tagDegree > existingPercentage)
        			addParents = true;
        		existingPercentage = Math.max(existingPercentage, tagDegree);
        	}
    		uniqueTags.put(actualTagRecord.getUniqueId(), existingPercentage);
    		if (addParents) {
    			HierarchicalRecord[] parentTags = actualTagRecord.getParentRecords();
    			for (HierarchicalRecord parentTag : parentTags) {
    				if (!parentTag.isRoot())
    					mergeTagAndAllParents(parentTag.getUniqueId(), tagDegree, uniqueTags);
    			}
    		}
    	}
    }

	public void setUserDataTypeColumns(Vector<UserDataColumn> userDataTypeColumns) { this.userDataTypeColumns = userDataTypeColumns; }

	public void setSearchParams(SearchParameters currentSearchParams) { this.currentSearchParams = currentSearchParams; }


	/////////////
	// METHODS //
	/////////////

	@Override
	protected void createSourceModel(QObject parent) {
		model = new SearchItemModel((RE3Properties.getBoolean("lazy_search_mode") && isLazySearchSupported()) ? 0 : getModelPopulator().getSize(), getNumColumns(), parent, this);
		loadTable();
	}
	public void addUserColumn(UserDataType userDataType) {
		try {
			getAddColumnSem().acquire();
			UserDataColumn newColumn = new UserDataColumn(nextUserColumnId++, userDataType, false);
			userDataTypeColumns.add(newColumn);
			sourceColumns.add(newColumn);
			viewColumns.add(newColumn);
			getDetailsModelManager().addUserColumn(newColumn);
			if (RapidEvolution3UI.instance != null) {
				QApplication.invokeAndWait(new TableModelColumnUpdater(getTableItemModel(), sourceColumns.size(), TableModelColumnUpdater.ACTION_ADD));
			}
		} catch (Exception e) {
			log.error("addUserColumn(): error", e);
		} finally {
			getAddColumnSem().release();
		}
	}

	@Override
	public void initialize(QObject parent) {
		try {
			// the code below was added to fix a problem where the user columns were removed, there's no harm in leaving it...
			SearchModelManager searchModel = (SearchModelManager)getIndex().getModelManager();
			for (UserDataColumn userColumn : searchModel.getUserDataTypeColumns()) {
				if (!viewColumns.contains(userColumn)) {
					viewColumns.add(userColumn.getInstance(false));
				}
			}
		} catch (Exception e) {
			log.error("initialize(): error", e);
		}
		super.initialize(parent);
	}

	@Override
	public void write(LineWriter writer) {
		super.write(writer);
		writer.writeLine(1); // version
		writer.writeLine(userDataTypeColumns.size());
		for (UserDataColumn userColumn : userDataTypeColumns)
			userColumn.write(writer);
	}

	@Override
	public void setPrimarySortColumn(short columnId) {
		super.setPrimarySortColumn(columnId);
	}

	@Override
	public boolean shouldAddToView(Record record) {
		if (currentSearchParams != null)
			return (currentSearchParams.matches(record) > 0.0f);
		return false;
	}

	////////////////////
	// STATIC METHODS //
	////////////////////

	static public SearchModelManager readSearchModelManager(LineReader lineReader) {
		int type = Integer.parseInt(lineReader.getNextLine());
		if (type == 1)
			return new ArtistModelManager(lineReader);
		else if (type == 2)
			return new LabelModelManager(lineReader);
		else if (type == 3)
			return new ReleaseModelManager(lineReader);
		else if (type == 4)
			return new SongModelManager(lineReader);
		log.warn("readSearchModelManager(): unknown search model type=" + type);
		return null;
	}

	static public void saveSearchModelManager(SearchModelManager modelManager, LineWriter writer) {
		if (modelManager instanceof ArtistModelManager)
			writer.writeLine(1);
		else if (modelManager instanceof LabelModelManager)
			writer.writeLine(2);
		else if (modelManager instanceof ReleaseModelManager)
			writer.writeLine(3);
		else if (modelManager instanceof SongModelManager)
			writer.writeLine(4);
		else
			log.warn("saveSearchModelManager(): unknown model manager type=" + modelManager.getClass());
		modelManager.write(writer);
	}

}
