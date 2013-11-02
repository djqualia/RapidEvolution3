package com.mixshare.rapid_evolution.data.index;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.exceptions.AlreadyExistsException;
import com.mixshare.rapid_evolution.data.exceptions.InsufficientInformationException;
import com.mixshare.rapid_evolution.data.exceptions.UnknownErrorException;
import com.mixshare.rapid_evolution.data.identifier.Identifier;
import com.mixshare.rapid_evolution.data.index.event.IndexChangeListener;
import com.mixshare.rapid_evolution.data.index.event.ProfilesMergedListener;
import com.mixshare.rapid_evolution.data.index.imdb.IMDBInterface;
import com.mixshare.rapid_evolution.data.profile.CommonProfile;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.profile.ProfileManager;
import com.mixshare.rapid_evolution.data.profile.filter.style.StyleProfile;
import com.mixshare.rapid_evolution.data.profile.filter.tag.TagProfile;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.data.search.SearchResult;
import com.mixshare.rapid_evolution.data.search.parameters.SearchParameters;
import com.mixshare.rapid_evolution.data.submitted.SubmittedProfile;
import com.mixshare.rapid_evolution.ui.model.ModelManagerInterface;
import com.mixshare.rapid_evolution.ui.model.ModelPopulatorInterface;
import com.mixshare.rapid_evolution.ui.model.filter.playlist.PlaylistModelManager;
import com.mixshare.rapid_evolution.ui.model.filter.style.StyleModelManager;
import com.mixshare.rapid_evolution.ui.model.filter.tag.TagModelManager;
import com.mixshare.rapid_evolution.ui.model.search.artist.ArtistModelManager;
import com.mixshare.rapid_evolution.ui.model.search.label.LabelModelManager;
import com.mixshare.rapid_evolution.ui.model.search.release.ReleaseModelManager;
import com.mixshare.rapid_evolution.ui.model.search.song.MixoutModelManager;
import com.mixshare.rapid_evolution.ui.model.search.song.SongModelManager;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.mixshare.rapid_evolution.util.timing.RWSemaphore;
import com.mixshare.rapid_evolution.util.timing.Semaphore;
import com.mixshare.rapid_evolution.workflow.TaskManager;
import com.mixshare.rapid_evolution.workflow.user.UpdateRecordsTask;

abstract public class CommonIndex extends AbstractIndex implements ModelPopulatorInterface, Serializable, DataConstants {

	static private Logger log = Logger.getLogger(CommonIndex.class);
    static private final long serialVersionUID = 0L;

	static private long MERGE_PROFILES_SEM_TIMEOUT = RE3Properties.getLong("merge_profiles_sem_timeout_millis");

    ////////////
    // FIELDS //
    ////////////

	protected IMDBInterface imdb;

 	protected ModelManagerInterface modelManager;

    // transients
    transient private Vector<IndexChangeListener> indexChangeListeners; // the UI uses this to respond to data model changes
    transient private Vector<ProfilesMergedListener> profilesMergedListeners;

    transient private Semaphore mergeProfileSem;
    transient private RWSemaphore blockUpdatesSem;

    static {
    	try{
    		// in order for the XMLEncoder to skip transient variables, this is needed
    		BeanInfo info = Introspector.getBeanInfo(CommonIndex.class);
    		PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
    		for (int i = 0; i < propertyDescriptors.length; ++i) {
    			PropertyDescriptor pd = propertyDescriptors[i];
    			if (pd.getName().equals("indexChangeListeners") || pd.getName().equals("profilesMergedListeners")
    					|| pd.getName().equals("blockUpdatesSem") || pd.getName().equals("mergeProfileSem") || pd.getName().equals("modelManager")) {
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

    public CommonIndex() {
    	super();
    	try {
    		imdb = (IMDBInterface)Class.forName(RE3Properties.getProperty("imdb_impl_class")).newInstance();
    		init();
    	} catch (Exception e) {
    		log.error("CommonIndex(): error", e);
    	}
    }
    public CommonIndex(LineReader lineReader) {
    	try {
        	String version = lineReader.getNextLine();
        	String imdbClass = lineReader.getNextLine();
        	int spaceIndex = imdbClass.indexOf(" ");
        	if (spaceIndex >= 0)
        		imdbClass = imdbClass.substring(spaceIndex + 1);
    		imdb = (IMDBInterface)Class.forName(imdbClass).newInstance();
    		imdb.init(lineReader);
    	} catch (Exception e) {
    		log.error("CommonIndex(): error", e);
    	}
    	int modelType = Integer.parseInt(lineReader.getNextLine());
    	if (modelType == 1)
    		modelManager = new ArtistModelManager(lineReader);
    	else if (modelType == 2)
    		modelManager = new LabelModelManager(lineReader);
    	else if (modelType == 3)
    		modelManager = new ReleaseModelManager(lineReader);
    	else if (modelType == 4)
    		modelManager = new SongModelManager(lineReader);
    	else if (modelType == 5)
    		modelManager = new MixoutModelManager(lineReader);
    	else if (modelType == 6)
    		modelManager = new StyleModelManager(lineReader);
    	else if (modelType == 7)
    		modelManager = new TagModelManager(lineReader);
    	else if (modelType == 8)
    		modelManager = new PlaylistModelManager(lineReader);
    }

    //////////////////////
    // ABSTRACT METHODS //
    //////////////////////

    abstract public byte getDataType();

	abstract public ModelManagerInterface createModelManager();

	abstract protected Profile getNewProfile(SubmittedProfile profile, int uniqueId);

	/**
	 * This method provides a hook for an index to update related indexes when a new record is added.
	 * For example, when a song is added the related artist, release and label can be added if they
	 * don't already exist.
	 */
	abstract protected void addRelationalItems(Record addedRecord);

	/**
	 * This method provides a hook for an index to update related indexes when a record is removed.
	 * For example, if a song is deleted and the related artist, release or label no longer references
	 * other songs, these related records can be removed as well.
	 */
	abstract protected void removeRelationalItems(Record removedRecord, boolean deleteRecords);

	@Override
	abstract public SearchParameters getNewSearchParameters();

	/////////////
	// GETTERS //
	/////////////

	public IMDBInterface getImdb() { return imdb; }

	@Override
	public int getSize() { return imdb.getSize(); }

	/**
	 * Returns all of the unique ids for each record in the index.  A copy must be returned so iterating over while
	 * records are being added/removed does not create concurrent modification exceptions.  The result is cached however
	 * and recomputed only when records are added/removed.
	 */
	@Override
	public Vector<Integer> getIds() { return imdb.getIds(); }
	@Override
	public Iterator<Integer> getIdsIterator() { return imdb.getIdsIterator(); }

	/**
	 * These methods allow translations between unique ids and identifiers.  Identifiers can change if the user renames items, etc, but the unique
	 * IDs will not.  The identifiers allow items to be recalled by their actual names as the user would know them, and allows the database to detect
	 * if an item exists or not..
	 */
	@Override
	public int getUniqueIdFromIdentifier(Identifier id) { return imdb.getUniqueIdFromIdentifier(id); }
	@Override
	public Identifier getIdentifierFromUniqueId(int uniqueId) { return imdb.getIdentifierFromUniqueId(uniqueId); }

	@Override
	public Record getRecord(Integer uniqueId) {
		if ((uniqueId != null) && (uniqueId >= 0))
			return imdb.get(uniqueId);
		return null;
	}
	@Override
	public Record getRecord(Identifier id) {
		int uniqueId = getUniqueIdFromIdentifier(id);
		if (uniqueId >= 0)
			return getRecord(uniqueId);
		return null;
	}

	/**
	 * Retrieves the profile from the file system (there is a layer of caching)
	 */
	@Override
	public Profile getProfile(Integer uniqueId) {
		if (uniqueId != null) {
			Record record = getRecord(uniqueId);
	    	if (record != null) {
	    		Profile profile = ProfileManager.getProfile(record.getIdentifier(), record.getUniqueId());
	    		if (profile != null) {
	    			// this is necessary to make sure the most up to date record is used, as the one read from the disk could be old (but is written to disk for redundancy/restorability purposes)
	    			profile.setRecord(record);
	    			return profile;
	    		}
	    	}
		}
    	return null;
	}
	@Override
	public Profile getProfile(Identifier id) {
		int uniqueId = getUniqueIdFromIdentifier(id);
		if (uniqueId >= 0)
			return getProfile(uniqueId);
		return null;
	}

	@Override
	public ModelManagerInterface getModelManager() {
		if (modelManager == null)
			modelManager = createModelManager();
		return modelManager;
	}

	public Semaphore getMergeProfileSem() {
		if (mergeProfileSem == null)
			mergeProfileSem = new Semaphore(1);
		return mergeProfileSem;
	}

	public RWSemaphore getBlockUpdatesSem() {
		if (blockUpdatesSem == null)
			blockUpdatesSem = new RWSemaphore(60000);
		return blockUpdatesSem;
	}

	public int getNextAvailableUniqueId() { return imdb.getNextAvailableUniqueId(); }

	public int searchCount(SearchParameters searchParameters) { return imdb.searchCount(searchParameters); }
	@Override
	public Vector<SearchResult> searchRecords(SearchParameters searchParameters) {
		return searchRecords(searchParameters, RE3Properties.getInt("lazy_search_mode_max_results"));
	}
	@Override
	public Vector<SearchResult> searchRecords(SearchParameters searchParameters, int maxResults) {
		if (maxResults <= 0)
			return imdb.searchRecords(searchParameters, -1);
		return imdb.searchRecords(searchParameters, maxResults);
	}

	/////////////
	// SETTERS //
	/////////////

	public void setImdb(IMDBInterface imdb) { this.imdb = imdb; }

	// for serialization
	public void setModelManager(ModelManagerInterface modelManager) { this.modelManager = modelManager; }

	/////////////
	// METHODS //
	/////////////

	public void init() {
		imdb.setDataType(getDataType());
	}

	@Override
	public boolean doesExist(Identifier id) {
		int uniqueId = imdb.getUniqueIdFromIdentifier(id);
		return doesExist(uniqueId);
	}
	@Override
	public boolean doesExist(Integer uniqueId) { return imdb.doesExist(uniqueId); }
	@Override
	public boolean doesExist(SubmittedProfile submittedProfile) { return doesExist(submittedProfile.getIdentifier()); }

	@Override
	protected void initProfile(Profile profile, SubmittedProfile initialValues) {
		if (initialValues.getRating() != null)
			profile.setRating(initialValues.getRating(), initialValues.getRatingSource());
	}

	/**
	 * Do not call this directly, call the Record's set***() methods
	 */
	@Override
	public void updateIdentifier(Profile profile, Identifier newId, Identifier oldId) throws AlreadyExistsException {
		boolean performUpdate = false;
		int uniqueId = imdb.getUniqueIdFromIdentifier(newId);
		boolean doesExist = imdb.doesExist(uniqueId);
		if (uniqueId == profile.getUniqueId())
			doesExist = false;
		if (!doesExist)
			performUpdate = true;
		else {
    		// check if new tag name matches any of the duplicates
    		for (int i = 0; i < profile.getNumDuplicateIds(); ++i) {
    			Identifier dupId = getIdentifierFromUniqueId(profile.getDuplicateId(i));
    			if ((dupId != null) && dupId.equals(newId)) {
    				performUpdate = true;
    				break;
    			}
    		}
		}
		if (performUpdate) {
			removeRelationalItems(profile.getRecord(), false);
			imdb.updateIdentifier(newId, oldId);
			((CommonProfile)profile).getCommonRecord().setId(newId);
			addRelationalItems(profile.getRecord());
		} else {
			throw new AlreadyExistsException(newId);
		}
	}
	@Override
	public void updateIdentifierEquivalent(Profile profile, Identifier newId, Identifier oldId) throws AlreadyExistsException {
		imdb.updateIdentifier(newId, oldId);
		((CommonProfile)profile).getCommonRecord().setId(newId);
	}

	@Override
	public Profile addOrUpdate(SubmittedProfile submittedProfile) throws InsufficientInformationException, UnknownErrorException {
		try {
			Profile profile = getProfile(submittedProfile.getIdentifier());
			if (profile == null) {
				try {
					return add(submittedProfile);
				} catch (AlreadyExistsException ae) {
					profile = getProfile(submittedProfile.getIdentifier());
				} catch (Exception e) {
					log.error("addOrUpdate(): error", e);
				}
			}
			if (profile != null) {
				profile.update(submittedProfile, false);
				profile.save();
			}
			return profile;
		} catch (Exception e) {
			log.error("addOrUpdate(): error", e);
		}
		return null;
	}

	/**
	 * Adds a SubmittedProfile and returns the new Profile object if successful.
	 */
	@Override
	public Profile add(SubmittedProfile submittedProfile) throws AlreadyExistsException, InsufficientInformationException, UnknownErrorException {
		Profile result = null;
		Identifier id = submittedProfile.getIdentifier();
    	try {
    		if ((id == null) || !id.isValid())
    			throw new InsufficientInformationException();
        	if (log.isTraceEnabled())
        		log.trace("add(): adding " + id.getTypeDescription() + " \"" + id + "\"");
    		if (doesExist(id))
    			throw new AlreadyExistsException(id);

    		result = getNewProfile(submittedProfile, imdb.getUniqueIdFromIdentifier(id));
    		initProfile(result, submittedProfile);

    		try {
    			getBlockUpdatesSem().startRead("add");
    			// profile is created, update indexes/file system...
    			imdb.put(result.getRecord());
    		} catch (Exception e) { } finally {
    			getBlockUpdatesSem().endRead();
    		}

    		// save
        	ProfileManager.saveProfile(result);

        	addRelationalItems(result.getRecord());

        	imdb.update(result.getRecord());

    		if (indexChangeListeners != null) {
    			for (int i = 0; i < indexChangeListeners.size(); ++i) {
                	if (!RapidEvolution3.isTerminated)
                		indexChangeListeners.get(i).addedRecord(result.getRecord(), submittedProfile);
				}
    		}

        	if (log.isDebugEnabled())
        		log.debug("add(): added " + id.getTypeDescription() + " \"" + id + "\"");

    	} catch (InsufficientInformationException e) {
    		log.error("add(): insufficient info=" + id);
    		throw e;
    	} catch (AlreadyExistsException e) {
    		if (log.isDebugEnabled())
    			log.debug("add(): already exists=" + id);
    		throw e;
    	} catch (Exception e) {
    		log.error("add(): error", e);
    		throw new UnknownErrorException();
    	}
    	return result;
	}

	@Override
	public void update(Record record) {
		try {
			if (log.isTraceEnabled())
				log.trace("update(): record=" + record);

			if (record.areRelationalItemsChanged()) {
				addRelationalItems(record);
				record.setRelationalItemsChanged(false);
			}

			imdb.update(record);

        	if (indexChangeListeners != null) {
    			for (int i = 0; i < indexChangeListeners.size(); ++i) {
    				if (!RapidEvolution3.isTerminated)
    					indexChangeListeners.get(i).updatedRecord(record);
				}
			}

		} catch (Exception e) {
			log.error("update(): error", e);
		}
	}

	/**
	 * Removes a record/profile, returns true if successful.
	 */
	@Override
	public boolean delete(Identifier id) { return delete(getUniqueIdFromIdentifier(id)); }
	@Override
	public boolean delete(Integer id) {
    	boolean success = false;
    	try {
    		if (doesExist(id)) {
    			Record removedRecord = getRecord(id);

    			try {
    				getBlockUpdatesSem().startRead("delete");
    				imdb.remove(id);
    			} catch (Exception e) { } finally {
    				getBlockUpdatesSem().endRead();
    			}

    			Profile profile = getProfile(id);
    			if (profile != null) {
    				removedRelatedData(profile);
    			}
    			ProfileManager.deleteProfile(removedRecord);

				for (int d = 0; d < removedRecord.getNumDuplicateIds(); ++d)
					imdb.removeDuplicateId(removedRecord.getDuplicateId(d));

    			removeRelationalItems(removedRecord, true);

				if (removedRecord instanceof SearchRecord) {
	    			for (byte b = 3; b <= MAX_DATA_SOURCE_VALUE; ++b) {
	    				if (((SearchRecord) removedRecord).hasMinedProfileHeader(b)) {
	    					ProfileManager.deleteMinedProfile(removedRecord.getDataType(), b, id);
	    				}
	    			}
				}

        		if (indexChangeListeners != null) {
        			for (int i = 0; i < indexChangeListeners.size(); ++i) {
        				if (!RapidEvolution3.isTerminated)
        					indexChangeListeners.get(i).removedRecord(removedRecord);
					}
        		}

            	if (log.isDebugEnabled())
            		log.debug("delete(): deleted " + removedRecord.getIdentifier().getTypeDescription() + " \"" + removedRecord.getIdentifier() + "\" (id=" + removedRecord.getUniqueId() + ")");
    		}
    		success = true;
    	} catch (Exception e) {
    		log.error("delete(): error", e);
    	}
    	return success;
	}

	/**
	 * Remove related data on disk such as images, called when a profile is deleted.
	 */
	protected void removedRelatedData(Profile profile) {

	}

	/**
	 * Merges 2 profiles, making 1 the duplicate of the other (the primary).  The primary profile is still accessible via
	 * both identifiers, but only the primary will show up when iterating over the collection.
	 */
	@Override
	public void mergeProfiles(Profile primaryProfile, Profile mergedProfile) {
		try {
			getMergeProfileSem().tryAcquire("mergeProfiles", MERGE_PROFILES_SEM_TIMEOUT);
			if (log.isDebugEnabled())
				log.debug("mergeProfiles(): primaryProfile=" + primaryProfile + ", mergedProfile=" + mergedProfile);
			Map<Record, Object> recordsToRefresh = primaryProfile.mergeWith(mergedProfile);
			imdb.setUniqueIdForIdentifier(primaryProfile.getUniqueId(), mergedProfile.getIdentifier());
			for (int d = 0; d < primaryProfile.getNumDuplicateIds(); ++d) {
				int duplicateId = primaryProfile.getDuplicateId(d);
				try {
					getBlockUpdatesSem().startRead("mergeProfiles");
					imdb.addDuplicateMapping(duplicateId, primaryProfile.getRecord());
				} catch (Exception e) {	} finally {
					getBlockUpdatesSem().endRead();
				}
			}
			addRelationalItems(primaryProfile.getRecord()); // establish any new relations so the following delete doesn't remove items now related to the primary
			delete(mergedProfile.getUniqueId());
			// it was found that styles/tags needed to be invalidated here rather than withint he mergeWith method as the UI wasn't properly updating...
			if (primaryProfile instanceof StyleProfile) {
				for (Record record : recordsToRefresh.keySet()) {
					if (record instanceof SearchRecord)
						((SearchRecord)record).invalidateActualStyles();
				}
			} else if (primaryProfile instanceof TagProfile) {
				for (Record record : recordsToRefresh.keySet()) {
					if (record instanceof SearchRecord)
						((SearchRecord)record).invalidateActualTags();
				}
			}
			primaryProfile.save();
    		if (profilesMergedListeners != null) {
    			for (int i = 0; i < profilesMergedListeners.size(); ++i) {
    				ProfilesMergedListener profilesMergedListener = profilesMergedListeners.get(i);
    				if (!RapidEvolution3.isTerminated)
    					profilesMergedListener.profilesMerged(primaryProfile, mergedProfile);
				}
    		}
    		if (!RapidEvolution3.isTerminated)
    			TaskManager.runBackgroundTask(new UpdateRecordsTask(recordsToRefresh.keySet()));
    		if (log.isTraceEnabled())
				log.trace("mergeProfiles(): done merging");
		} catch (Exception e) {
			log.error("mergeProfiles(): error", e);
		} finally {
			getMergeProfileSem().release();
		}
	}
	public void removeDuplicateIdentifier(int uniqueId) {
		try {
			getBlockUpdatesSem().startRead("removeDuplicateIdentifier");
			imdb.removeDuplicateId(uniqueId);
		} catch (Exception e) { } finally {
			getBlockUpdatesSem().endRead();
		}
	}

	public void lockAllRecords() {
		for (int uniqueId : getIds()) {
			Record record = getRecord(uniqueId);
			if (record != null) {
				try {
					record.getWriteLockSem().startWrite("lockWriteAllRecords");
				} catch (Exception e) { }
			}
		}
	}

	public void unlockAllRecords() {
		for (int uniqueId : getIds()) {
			Record record = getRecord(uniqueId);
			if (record != null) {
				try {
					record.getWriteLockSem().endWrite();
				} catch (Exception e) { }
			}
		}
	}

	public void addIndexChangeListener(IndexChangeListener changeListener) {
		if (indexChangeListeners == null)
			indexChangeListeners = new Vector<IndexChangeListener>();
		if (!indexChangeListeners.contains(changeListener))
			indexChangeListeners.add(changeListener);
	}
	public void removeIndexChangeListener(IndexChangeListener changeListener) {
		if (indexChangeListeners != null)
			indexChangeListeners.remove(changeListener);
	}

	public void addProfilesMergedListener(ProfilesMergedListener profilesMergedListener) {
		if (profilesMergedListeners == null)
			profilesMergedListeners = new Vector<ProfilesMergedListener>();
		if (!profilesMergedListeners.contains(profilesMergedListener))
			profilesMergedListeners.add(profilesMergedListener);
	}
	public void removeProfilesMergedListener(ProfilesMergedListener profilesMergedListener) {
		if (profilesMergedListeners != null)
			profilesMergedListeners.remove(profilesMergedListener);
	}

	@Override
	public void computeSearchScores(SearchParameters searchParams) {
		imdb.computeSearchScores(searchParams);
	}

	@Override
	public void write(LineWriter writer) {
		writer.writeLine(1); // version
		writer.writeLine(imdb.getClass().toString());
		imdb.write(writer);
		if (modelManager instanceof ArtistModelManager)
			writer.writeLine(1);
		else if (modelManager instanceof LabelModelManager)
			writer.writeLine(2);
		else if (modelManager instanceof ReleaseModelManager)
			writer.writeLine(3);
		else if (modelManager instanceof SongModelManager)
			writer.writeLine(4);
		else if (modelManager instanceof MixoutModelManager)
			writer.writeLine(5);
		else if (modelManager instanceof StyleModelManager)
			writer.writeLine(6);
		else if (modelManager instanceof TagModelManager)
			writer.writeLine(7);
		else if (modelManager instanceof PlaylistModelManager)
			writer.writeLine(8);
		else if (modelManager == null)
			writer.writeLine(0);
		else {
			log.warn("write(): unexpected model manager type=" + modelManager.getClass());
			writer.writeLine(-1);
		}
		if (modelManager != null)
			modelManager.write(writer);
	}

}
