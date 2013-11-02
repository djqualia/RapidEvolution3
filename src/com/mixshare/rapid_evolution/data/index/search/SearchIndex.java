package com.mixshare.rapid_evolution.data.index.search;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.exceptions.AlreadyExistsException;
import com.mixshare.rapid_evolution.data.identifier.filter.style.StyleIdentifier;
import com.mixshare.rapid_evolution.data.identifier.filter.tag.TagIdentifier;
import com.mixshare.rapid_evolution.data.index.CommonIndex;
import com.mixshare.rapid_evolution.data.index.imdb.LocalIMDB;
import com.mixshare.rapid_evolution.data.mined.MinedProfile;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.profile.common.image.Image;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.filter.FilterRecord;
import com.mixshare.rapid_evolution.data.record.filter.playlist.PlaylistRecord;
import com.mixshare.rapid_evolution.data.record.filter.style.StyleRecord;
import com.mixshare.rapid_evolution.data.record.filter.tag.TagRecord;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.data.record.user.UserDataType;
import com.mixshare.rapid_evolution.data.search.SearchResult;
import com.mixshare.rapid_evolution.data.search.parameters.search.SearchSearchParameters;
import com.mixshare.rapid_evolution.data.submitted.SubmittedProfile;
import com.mixshare.rapid_evolution.data.submitted.filter.style.SubmittedStyle;
import com.mixshare.rapid_evolution.data.submitted.filter.tag.SubmittedTag;
import com.mixshare.rapid_evolution.data.submitted.search.SubmittedSearchProfile;
import com.mixshare.rapid_evolution.data.util.DegreeValue;
import com.mixshare.rapid_evolution.data.util.normalization.DuplicateRecordMerger;
import com.mixshare.rapid_evolution.event.RE3PropertiesChangeListener;
import com.mixshare.rapid_evolution.ui.model.filter.FilterSelection;
import com.mixshare.rapid_evolution.ui.model.search.SearchModelManager;
import com.mixshare.rapid_evolution.ui.model.search.artist.RecommendedArtistModelManager;
import com.mixshare.rapid_evolution.ui.model.search.label.RecommendedLabelModelManager;
import com.mixshare.rapid_evolution.ui.model.search.release.RecommendedReleaseModelManager;
import com.mixshare.rapid_evolution.ui.model.search.song.RecommendedSongModelManager;
import com.mixshare.rapid_evolution.util.FileUtil;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.mixshare.rapid_evolution.util.timing.Semaphore;

/**
 * There is a lot in common between the search types (artist/label/release/song), and this abstract class
 * fleshes out the common functionality...
 *
 * For each search type, RE3 allows the user to define any number of custom data types, which the index must
 * keep track of.
 */
abstract public class SearchIndex extends CommonIndex implements RE3PropertiesChangeListener {

	static private Logger log = Logger.getLogger(SearchIndex.class);

	static private boolean ENABLE_FILTER_COUNT_OPTIMIZATIONS = true;

	////////////
	// FIELDS //
	////////////

    protected SearchModelManager recommendedModelManager;

    transient private Semaphore addUserDataTypeLock = new Semaphore(1);

    static {
    	try{
    		// in order for the XMLEncoder to skip transient variables, this is needed
    		BeanInfo info = Introspector.getBeanInfo(SearchIndex.class);
    		PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
    		for (int i = 0; i < propertyDescriptors.length; ++i) {
    			PropertyDescriptor pd = propertyDescriptors[i];
    			if (pd.getName().equals("addUserDataTypeLock") || pd.getName().equals("recommendedModelManager")) {
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

    public SearchIndex() {
    	RE3Properties.addChangeListener(this);
    	if (RE3Properties.getBoolean("attempt_to_automatically_merge_duplicates"))
    		addIndexChangeListener(new DuplicateRecordMerger(this));
    }
    public SearchIndex(LineReader lineReader) {
    	super(lineReader);
    	int version = Integer.parseInt(lineReader.getNextLine());
    	if (version == 1) {
    		int numUserDataTypes = Integer.parseInt(lineReader.getNextLine());
    		Vector<UserDataType> userDataTypes = new Vector<UserDataType>(numUserDataTypes);
    		for (int i = 0; i < numUserDataTypes; ++i)
    			userDataTypes.add(new UserDataType(lineReader));
    		byte nextUserDataTypeId = Byte.parseByte(lineReader.getNextLine());
    		((LocalIMDB)getImdb()).setNextUserDataTypeId(nextUserDataTypeId);
    		((LocalIMDB)getImdb()).setUserDataTypes(userDataTypes);
    	}
    	int modelType = Integer.parseInt(lineReader.getNextLine());
    	if (modelType == 1)
    		recommendedModelManager = new RecommendedArtistModelManager(lineReader);
    	else if (modelType == 2)
    		recommendedModelManager = new RecommendedLabelModelManager(lineReader);
    	else if (modelType == 3)
    		recommendedModelManager = new RecommendedReleaseModelManager(lineReader);
    	else if (modelType == 4)
    		recommendedModelManager = new RecommendedSongModelManager(lineReader);
    }

    /////////////
    // GETTERS //
    /////////////

	public Semaphore getAddUserDataTypeLock() {
		if (addUserDataTypeLock == null)
			addUserDataTypeLock = new Semaphore(1);
		return addUserDataTypeLock;
	}

	public SearchRecord getSearchRecord(Integer uniqueId) { return (SearchRecord)getRecord(uniqueId); }

	public SearchModelManager getSearchModelManager() { return (SearchModelManager)getModelManager(); }

	public Vector<UserDataType> getUserDataTypes() { return imdb.getUserDataTypes(); }

	public UserDataType getUserDataType(String title) {
		for (UserDataType type : imdb.getUserDataTypes()) {
			if (type.getTitle().equalsIgnoreCase(title))
				return type;
		}
		return null;
	}
	public UserDataType getUserDataType(short typeId) {
		for (UserDataType type : imdb.getUserDataTypes()) {
			if (type.getId() == typeId)
				return type;
		}
		return null;
	}

	/**
	public Vector<SearchRecord> getSearchRecords(FilterSelection filterSelection) {
		Vector<SearchRecord> result = new Vector<SearchRecord>();
		Iterator<Integer> idIter = getIdsIterator();
		while (idIter.hasNext()) {
			SearchRecord record = (SearchRecord)getRecord(idIter.next());
			if (filterSelection.matches(record))
				result.add(record);
		}
		return result;
	}
	public Vector<SearchRecord> getInternalSearchRecords(FilterSelection filterSelection) {
		Vector<SearchRecord> result = new Vector<SearchRecord>();
		Iterator<Integer> idIter = getIdsIterator();
		while (idIter.hasNext()) {
			SearchRecord record = (SearchRecord)getRecord(idIter.next());
			if ((record != null) && !record.isExternalItem() && filterSelection.matches(record))
				result.add(record);
		}
		return result;
	}
	public Vector<SearchRecord> getExternalSearchRecords(FilterSelection filterSelection) {
		Vector<SearchRecord> result = new Vector<SearchRecord>();
		Iterator<Integer> idIter = getIdsIterator();
		while (idIter.hasNext()) {
			SearchRecord record = (SearchRecord)getRecord(idIter.next());
			if ((record != null) && record.isExternalItem() && filterSelection.matches(record))
				result.add(record);
		}
		return result;
	}
	*/

	public Vector<SearchResult> getSearchRecords(FilterRecord filter) {
		Vector<FilterRecord> filters = new Vector<FilterRecord>(1);
		filters.add(filter);
		FilterSelection filterSelection = new FilterSelection();
		filterSelection.setOptionalFilters(filters);
		SearchSearchParameters searchParams = (SearchSearchParameters)getNewSearchParameters();
		if (filter instanceof StyleRecord)
			searchParams.setStylesSelection(filterSelection);
		else if (filter instanceof TagRecord)
			searchParams.setTagsSelection(filterSelection);
		else if (filter instanceof PlaylistRecord)
			searchParams.setPlaylistsSelection(filterSelection);
		return searchRecords(searchParams);
		//return getSearchRecords(filterSelection);
	}
	public Vector<SearchResult> getInternalSearchRecords(FilterRecord filter) {
		Vector<FilterRecord> filters = new Vector<FilterRecord>(1);
		filters.add(filter);
		FilterSelection filterSelection = new FilterSelection();
		filterSelection.setOptionalFilters(filters);
		SearchSearchParameters searchParams = (SearchSearchParameters)getNewSearchParameters();
		if (filter instanceof StyleRecord)
			searchParams.setStylesSelection(filterSelection);
		else if (filter instanceof TagRecord)
			searchParams.setTagsSelection(filterSelection);
		else if (filter instanceof PlaylistRecord)
			searchParams.setPlaylistsSelection(filterSelection);
		searchParams.setInternalItemsOnly(true);
		return searchRecords(searchParams);
	}
	public int getInternalSearchRecordsCount(FilterRecord filter) {
		if (ENABLE_FILTER_COUNT_OPTIMIZATIONS) {
			int count = 0;
			if (filter instanceof StyleRecord) {
				for (int id : getIds()) {
					SearchRecord record = getSearchRecord(id);
					if ((record != null) && !record.isExternalItem()) {
						if (record.containsActualStyle(filter.getUniqueId()))
							++count;
					}
				}
			} else if (filter instanceof TagRecord) {
				for (int id : getIds()) {
					SearchRecord record = getSearchRecord(id);
					if ((record != null) && !record.isExternalItem()) {
						if (record.containsActualTag(filter.getUniqueId()))
							++count;
					}
				}
			} else if (filter instanceof PlaylistRecord) {
				PlaylistRecord playlist = (PlaylistRecord)filter;
				for (int id : getIds()) {
					SearchRecord record = getSearchRecord(id);
					if ((record != null) && !record.isExternalItem()) {
						if (playlist.matches(record))
							++count;
					}
				}
			}
			return count;
		} else {
			Vector<FilterRecord> filters = new Vector<FilterRecord>(1);
			filters.add(filter);
			FilterSelection filterSelection = new FilterSelection();
			filterSelection.setOptionalFilters(filters);
			SearchSearchParameters searchParams = (SearchSearchParameters)getNewSearchParameters();
			if (filter instanceof StyleRecord)
				searchParams.setStylesSelection(filterSelection);
			else if (filter instanceof TagRecord)
				searchParams.setTagsSelection(filterSelection);
			else if (filter instanceof PlaylistRecord)
				searchParams.setPlaylistsSelection(filterSelection);
			searchParams.setInternalItemsOnly(true);
			return searchCount(searchParams);
		}
	}
	public Vector<SearchResult> getExternalSearchRecords(FilterRecord filter) {
		Vector<FilterRecord> filters = new Vector<FilterRecord>(1);
		filters.add(filter);
		FilterSelection filterSelection = new FilterSelection();
		filterSelection.setOptionalFilters(filters);
		SearchSearchParameters searchParams = (SearchSearchParameters)getNewSearchParameters();
		if (filter instanceof StyleRecord)
			searchParams.setStylesSelection(filterSelection);
		else if (filter instanceof TagRecord)
			searchParams.setTagsSelection(filterSelection);
		else if (filter instanceof PlaylistRecord)
			searchParams.setPlaylistsSelection(filterSelection);
		searchParams.setExternalItemsOnly(true);
		return searchRecords(searchParams);
		//return getExternalSearchRecords(filterSelection);
	}
	public int getExternalSearchRecordsCount(FilterRecord filter) {
		if (ENABLE_FILTER_COUNT_OPTIMIZATIONS) {
			int count = 0;
			if (filter instanceof StyleRecord) {
				for (int id : getIds()) {
					SearchRecord record = getSearchRecord(id);
					if ((record != null) && record.isExternalItem()) {
						if (record.containsActualStyle(filter.getUniqueId()))
							++count;
					}
				}
			} else if (filter instanceof TagRecord) {
				for (int id : getIds()) {
					SearchRecord record = getSearchRecord(id);
					if ((record != null) && record.isExternalItem()) {
						if (record.containsActualTag(filter.getUniqueId()))
							++count;
					}
				}
			} else if (filter instanceof PlaylistRecord) {
				PlaylistRecord playlist = (PlaylistRecord)filter;
				for (int id : getIds()) {
					SearchRecord record = getSearchRecord(id);
					if ((record != null) && record.isExternalItem()) {
						if (playlist.matches(record))
							++count;
					}
				}
			}
			return count;
		} else {
			Vector<FilterRecord> filters = new Vector<FilterRecord>(1);
			filters.add(filter);
			FilterSelection filterSelection = new FilterSelection();
			filterSelection.setOptionalFilters(filters);
			SearchSearchParameters searchParams = (SearchSearchParameters)getNewSearchParameters();
			if (filter instanceof StyleRecord)
				searchParams.setStylesSelection(filterSelection);
			else if (filter instanceof TagRecord)
				searchParams.setTagsSelection(filterSelection);
			else if (filter instanceof PlaylistRecord)
				searchParams.setPlaylistsSelection(filterSelection);
			searchParams.setExternalItemsOnly(true);
			return searchCount(searchParams);
		}
	}

	public int getSizeInternalItems() {
		SearchSearchParameters searchParams = (SearchSearchParameters)getNewSearchParameters();
		searchParams.setInternalItemsOnly(true);
		return searchCount(searchParams);
	}

	public int getSizeExternalItems() {
		SearchSearchParameters searchParams = (SearchSearchParameters)getNewSearchParameters();
		searchParams.setExternalItemsOnly(true);
		return searchCount(searchParams);
	}

	public SearchModelManager getRecommendedModelManager() {
		if (recommendedModelManager == null)
			recommendedModelManager = createRecommendedModelManager();
		return recommendedModelManager;
	}

	/////////////
	// SETTERS //
	/////////////

	public void addUserDataType(String title, byte fieldType) {
		if ((title == null) || (title.length() == 0))
			return;
		try {
			getAddUserDataTypeLock().acquire();
			boolean found = false;
			for (UserDataType userDataType : imdb.getUserDataTypes()) {
				if (userDataType.getTitle().equalsIgnoreCase(title))
					found = true;
			}
			if (!found) {
				UserDataType newType = new UserDataType(imdb.getNextUserDataTypeIdAndIncrement(), title, "", fieldType);
				imdb.addUserDataType(newType);
				if (!RE3Properties.getBoolean("server_mode"))
					getSearchModelManager().addUserColumn(newType);
			}
		} catch (Exception e) {
			log.error("addUserDataType(): error", e);
		} finally {
			getAddUserDataTypeLock().release();
		}
	}

	// for serialization
	public void setAddUserDataTypeLock(Semaphore addUserDataTypeLock) { this.addUserDataTypeLock = addUserDataTypeLock; }

	/////////////////////
	// ABSTRACT METHOD //
	/////////////////////

	@Override
	abstract public void propertiesChanged();

	abstract protected SearchModelManager createRecommendedModelManager();

	/////////////
	// METHODS //
	/////////////

	@Override
	protected void initProfile(Profile profile, SubmittedProfile initialValues) {
		super.initProfile(profile, initialValues);
		SearchProfile searchProfile = (SearchProfile)profile;
		SubmittedSearchProfile initialSearchValues = (SubmittedSearchProfile)initialValues;
		if (initialSearchValues.getStyleDegreeValues() != null)
			searchProfile.setStyles(initialSearchValues.getStyleDegreeValues());
		if (initialSearchValues.getTagDegreeValues() != null)
			searchProfile.setTags(initialSearchValues.getTagDegreeValues());
		searchProfile.setScore(initialSearchValues.getScore());
		searchProfile.setComments(initialSearchValues.getComments(), initialSearchValues.getCommentsSource());
		searchProfile.setThumbnailImageFilename(FileUtil.stripWorkingDirectory(initialSearchValues.getThumbnailImageFilename()), initialSearchValues.getThumbnailImageFilenameSource());
		for (Image image : initialSearchValues.getImages())
			searchProfile.addImage(image);
		searchProfile.setDisabled(initialValues.isDisabled());
		searchProfile.setExternalItem(initialSearchValues.isExternalItem());
		for (MinedProfile minedProfile : initialSearchValues.getMinedProfiles())
			searchProfile.addMinedProfile(minedProfile, true);
		if (initialSearchValues.getPlayCount() > 0)
			searchProfile.setPlayCount(searchProfile.getPlayCount());
		searchProfile.setDateAdded(initialSearchValues.getDateAdded());
	}

	@Override
	protected void addRelationalItems(Record addedRecord) {
		SearchRecord searchProfile = (SearchRecord)addedRecord;
		// styles
		for (DegreeValue styleDegree : searchProfile.getSourceStyleDegreeValues()) {
			try {
				String styleName = styleDegree.getName();
				if (styleName.length() > 0) {
					StyleIdentifier styleId = new StyleIdentifier(styleName);
					StyleRecord styleRecord = Database.getStyleIndex().getStyleRecord(styleId);
					if (styleRecord == null) {
						// create style
						SubmittedStyle newSubmittedStyle = new SubmittedStyle(styleName);
						// TODO: auto add to hierarchy based on name?
						Database.getStyleIndex().add(newSubmittedStyle);
						searchProfile.invalidateActualStyles(); // now that the style exists, need to update actual styles (TODO: investigate a more efficient way?)
					}
				}
			} catch (AlreadyExistsException ae) {
			} catch (Exception e) {
				log.error("addRelationalItems(): error", e);
			}
		}
		// tags
		for (DegreeValue tagDegree : searchProfile.getSourceTagDegreeValues()) {
			try {
				String tagName = tagDegree.getName();
				if (tagName.length() > 0) {
					TagIdentifier tagId = new TagIdentifier(tagName);
					TagRecord tagRecord = Database.getTagIndex().getTagRecord(tagId);
					if (tagRecord == null) {
						// create tag
						SubmittedTag newSubmittedTag = new SubmittedTag(tagName);
						Profile profile = Database.getTagIndex().add(newSubmittedTag);
						searchProfile.invalidateActualTags(); // now that the tag exists, need to update actual tags (TODO: investigate a more efficient way?)
					}
				}
			} catch (AlreadyExistsException ae) {
			} catch (Exception e) {
				log.error("addRelationalItems(): error", e);
			}
		}
	}

	@Override
	protected void removedRelatedData(Profile profile) {
		// Don't enable the following code unless checks are added to make sure the same image filename is not referenced elsehwere.
//		SearchProfile searchProfile = (SearchProfile) profile;
//		for (Image image : searchProfile.getImages()) {
//			File file = new File(OSHelper.getWorkingDirectory() + "/" + image.getImageFilename());
//			file.delete();
//		}
	}

	@Override
	protected void removeRelationalItems(Record removedRecord, boolean deleteRecords) {
		SearchRecord removedSearchRecord = (SearchRecord)removedRecord;
		// styles
		int[] sourceStyleIds = removedSearchRecord.getSourceStyleIds();
		for (int s = 0; s < removedSearchRecord.getNumSourceStyles(); ++s) {
			try {
				int uniqueId = sourceStyleIds[s];
				StyleRecord styleRecord = (StyleRecord)Database.getStyleIndex().getRecord(uniqueId);
				if (styleRecord != null) {
					if (styleRecord.isOrphaned()) {
						Database.getStyleIndex().delete(uniqueId);
					}
				}
			} catch (Exception e) {
				log.error("removeRelationalItems(): error", e);
			}
		}
		// tags
		int[] sourceTagIds = removedSearchRecord.getSourceTagIds();
		for (int t = 0; t < removedSearchRecord.getNumSourceTags(); ++t) {
			try {
				int uniqueId = sourceTagIds[t];
				TagRecord tagRecord = (TagRecord)Database.getTagIndex().getRecord(uniqueId);
				if (tagRecord != null) {
					if (tagRecord.isOrphaned()) {
						Database.getTagIndex().delete(uniqueId);
					}
				}
			} catch (Exception e) {
				log.error("removeRelationalItems(): error", e);
			}
		}
	}

	@Override
	public void write(LineWriter writer) {
		super.write(writer);
		writer.writeLine(2); // version

		SearchModelManager recommendedModelManager = getRecommendedModelManager();
    	if (recommendedModelManager instanceof RecommendedArtistModelManager)
    		writer.writeLine(1);
    	else if (recommendedModelManager instanceof RecommendedLabelModelManager)
    		writer.writeLine(2);
    	else if (recommendedModelManager instanceof RecommendedReleaseModelManager)
    		writer.writeLine(3);
    	else if (recommendedModelManager instanceof RecommendedSongModelManager)
    		writer.writeLine(4);
    	else
    		log.warn("write(): unexpected recommended model type=" + recommendedModelManager.getClass());
    	recommendedModelManager.write(writer);
	}
}
