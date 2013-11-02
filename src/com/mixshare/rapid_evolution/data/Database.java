package com.mixshare.rapid_evolution.data;

import java.io.File;
import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.exceptions.AlreadyExistsException;
import com.mixshare.rapid_evolution.data.exceptions.InsufficientInformationException;
import com.mixshare.rapid_evolution.data.exceptions.UnknownErrorException;
import com.mixshare.rapid_evolution.data.identifier.Identifier;
import com.mixshare.rapid_evolution.data.identifier.filter.playlist.PlaylistIdentifier;
import com.mixshare.rapid_evolution.data.identifier.filter.style.StyleIdentifier;
import com.mixshare.rapid_evolution.data.identifier.filter.tag.TagIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.artist.ArtistIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.label.LabelIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.release.ReleaseIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.song.MixoutIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.song.SongIdentifier;
import com.mixshare.rapid_evolution.data.index.CommonIndex;
import com.mixshare.rapid_evolution.data.index.HierarchicalIndex;
import com.mixshare.rapid_evolution.data.index.event.HierarchyChangeListener;
import com.mixshare.rapid_evolution.data.index.filter.FilterIndex;
import com.mixshare.rapid_evolution.data.index.filter.playlist.PlaylistIndex;
import com.mixshare.rapid_evolution.data.index.filter.style.StyleIndex;
import com.mixshare.rapid_evolution.data.index.filter.tag.TagIndex;
import com.mixshare.rapid_evolution.data.index.search.SearchIndex;
import com.mixshare.rapid_evolution.data.index.search.artist.ArtistIndex;
import com.mixshare.rapid_evolution.data.index.search.label.LabelIndex;
import com.mixshare.rapid_evolution.data.index.search.release.ReleaseIndex;
import com.mixshare.rapid_evolution.data.index.search.song.MixoutIndex;
import com.mixshare.rapid_evolution.data.index.search.song.SongIndex;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.profile.ProfileManager;
import com.mixshare.rapid_evolution.data.profile.filter.FilterProfile;
import com.mixshare.rapid_evolution.data.profile.io.CustomLocalProfileIO;
import com.mixshare.rapid_evolution.data.profile.search.ReleaseGroupProfile;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.profile.search.SongGroupProfile;
import com.mixshare.rapid_evolution.data.profile.util.TempProfileCleanup;
import com.mixshare.rapid_evolution.data.record.HierarchicalRecord;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.filter.FilterRecord;
import com.mixshare.rapid_evolution.data.record.filter.style.StyleRecord;
import com.mixshare.rapid_evolution.data.record.filter.tag.TagRecord;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.data.record.search.artist.ArtistRecord;
import com.mixshare.rapid_evolution.data.record.user.UserDataType;
import com.mixshare.rapid_evolution.data.search.SearchResult;
import com.mixshare.rapid_evolution.data.submitted.SubmittedProfile;
import com.mixshare.rapid_evolution.data.user.LocalUserProfile;
import com.mixshare.rapid_evolution.data.user.UserProfile;
import com.mixshare.rapid_evolution.data.util.io.Serializer;
import com.mixshare.rapid_evolution.data.util.io.XMLSerializer;
import com.mixshare.rapid_evolution.event.RE3PropertiesChangeListener;
import com.mixshare.rapid_evolution.ui.model.filter.playlist.PlaylistModelManager;
import com.mixshare.rapid_evolution.ui.model.filter.style.StyleModelManager;
import com.mixshare.rapid_evolution.ui.model.filter.tag.TagModelManager;
import com.mixshare.rapid_evolution.ui.model.profile.RelativeModelFactory;
import com.mixshare.rapid_evolution.ui.model.search.artist.ArtistModelManager;
import com.mixshare.rapid_evolution.ui.model.search.artist.RecommendedArtistModelManager;
import com.mixshare.rapid_evolution.ui.model.search.label.LabelModelManager;
import com.mixshare.rapid_evolution.ui.model.search.label.RecommendedLabelModelManager;
import com.mixshare.rapid_evolution.ui.model.search.release.RecommendedReleaseModelManager;
import com.mixshare.rapid_evolution.ui.model.search.release.ReleaseModelManager;
import com.mixshare.rapid_evolution.ui.model.search.song.MixoutModelManager;
import com.mixshare.rapid_evolution.ui.model.search.song.RecommendedSongModelManager;
import com.mixshare.rapid_evolution.ui.model.search.song.SongModelManager;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.util.OSHelper;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.mixshare.rapid_evolution.util.io.LineWriterFactory;
import com.mixshare.rapid_evolution.webaccess.WebServerManager;

public class Database implements Serializable, HierarchyChangeListener, RE3PropertiesChangeListener {

    static private final long serialVersionUID = 0L;
    static private Logger log = Logger.getLogger(Database.class);

    static private final int MAX_SAVE_ATTEMPTS = 5;

    static private Database instance;

    /////////////////
    // PERSISTENCE //
    /////////////////

    static private String snapshotFilename = OSHelper.getWorkingDirectory() + RE3Properties.getProperty("index_snapshot_location");
    static private String databaseFilename = OSHelper.getWorkingDirectory() + RE3Properties.getProperty("database_snapshot_location");
    static private String xmlFilename = OSHelper.getWorkingDirectory() + RE3Properties.getProperty("index_xml_location");
    static private String backupFilenamePrefix = RE3Properties.getProperty("database_backup_filename_prefix");

    static { init(); }
    static public void init() {
    	try {
			TempProfileCleanup.cleanUpProfiles();
    	} catch (Exception e) {
    		log.error("init(): error", e);
    	}
    }

    static private void consistencyCheck(long snapshotSaved) {
    	try {
    		if (log.isInfoEnabled())
    			log.info("consistencyCheck(): performing consistency check...");
    		if (log.isDebugEnabled())
    			log.debug("consistencyCheck(): snapshotSaved=" + snapshotSaved);
    		RapidEvolution3.updateSplashScreen(Translations.get("splash_screen_checking_consistency"));

    		consistencyCheckIndex(snapshotSaved, DataConstants.DATA_TYPE_ARTISTS, ArtistIdentifier.typeDescription, Database.getArtistIndex());
    		consistencyCheckIndex(snapshotSaved, DataConstants.DATA_TYPE_LABELS, LabelIdentifier.typeDescription, Database.getLabelIndex());
    		consistencyCheckIndex(snapshotSaved, DataConstants.DATA_TYPE_RELEASES, ReleaseIdentifier.typeDescription, Database.getReleaseIndex());
    		consistencyCheckIndex(snapshotSaved, DataConstants.DATA_TYPE_SONGS, SongIdentifier.typeDescription, Database.getSongIndex());
    		consistencyCheckIndex(snapshotSaved, DataConstants.DATA_TYPE_STYLES, StyleIdentifier.typeDescription, Database.getStyleIndex());
    		consistencyCheckIndex(snapshotSaved, DataConstants.DATA_TYPE_TAGS, TagIdentifier.typeDescription, Database.getTagIndex());
    		consistencyCheckIndex(snapshotSaved, DataConstants.DATA_TYPE_PLAYLISTS, PlaylistIdentifier.typeDescription, Database.getPlaylistIndex());

    		if (RE3Properties.getBoolean("recompute_filters_on_consistency_check")) {
				for (SearchIndex searchIndex : Database.getSearchIndexes()) {
					for (int id : searchIndex.getIds()) {
						SearchProfile searchProfile = (SearchProfile)searchIndex.getProfile(id);
						if (searchProfile != null) {
							searchProfile.computeStyles();
							searchProfile.computeTags();
							searchProfile.save();
						}
					}
				}
    		}

    		// check consistency of filter hierarchies
    		for (FilterIndex filterIndex : Database.getFilterIndexes()) {
    			filterIndex.getRootRecord().checkRelations();
    			for (int id : filterIndex.getIds()) {
    				FilterRecord filter = (FilterRecord)filterIndex.getRecord(id);
    				if ((filter != null) && !filter.isRoot()) {
    					filter.checkRelations();
    					if (!filter.isRoot()) {
	    					if ((filter.getParentRecords().length == 0) && !filter.isDisabled()) {
	    						FilterProfile testProfile = (FilterProfile)filterIndex.getProfile(id);
	    						if (testProfile != null) {
	    							log.warn("consistencyCheck(): valid filter with no parent found=" + filter);
	    							filterIndex.addRelationship(filterIndex.getRootRecord(), filter);
	    						}
	    					} else {
	    						boolean validParentFound = false;
	    						for (HierarchicalRecord parentRecord : filter.getParentRecords()) {
	    							if (parentRecord != null) {
	    								if (!parentRecord.isDisabled()) {
	    									// re-establish relationship in case parent lost the child reference (TBD: root cause)
	    									filterIndex.addRelationship(parentRecord, filter);
	    									validParentFound = true;
	    								} else
	    									filterIndex.removeRelationship(parentRecord, filter);
	    							}
	    						}
	    						if (!validParentFound && !filter.isDisabled()) {
	    							log.warn("consistencyCheck(): valid filter with no visible parent found=" + filter);
	        						filterIndex.addRelationship(filterIndex.getRootRecord(), filter);
	    						}
	    					}
	    					if (RE3Properties.getBoolean("recompute_filters_on_consistency_check")) {
    							filter.getNumArtistRecords();
    							filter.getNumLabelRecords();
    							filter.getNumReleaseRecords();
    							filter.getNumSongRecords();
	    					}
    					}
    				}
    			}
    		}
    	} catch (Exception e) {
    		if (RE3Properties.getBoolean("server_mode")) {
    			log.fatal("consistencyCheck(): error", e);
    			System.exit(1);
    		} else {
    			log.error("consistencyCheck(): error", e);
    		}
    	}
    }

    static private class ConsistencyChecker {
		int nextArtistId = Database.getArtistIndex().getNextAvailableUniqueId();
		int nextLabelId = Database.getLabelIndex().getNextAvailableUniqueId();
		int nextReleaseId  =Database.getReleaseIndex().getNextAvailableUniqueId();
		int nextSongId = Database.getSongIndex().getNextAvailableUniqueId();
		int nextStyleId = Database.getStyleIndex().getNextAvailableUniqueId();
		int nextTagId = Database.getTagIndex().getNextAvailableUniqueId();
		int nextPlaylistId = Database.getPlaylistIndex().getNextAvailableUniqueId();
		private ConsistencyChecker() {


		}
    }

    static private void consistencyCheckIndex(long snapshotSaved, byte type, String typeDescription, CommonIndex index) {
    	File profileDir = new File(OSHelper.getWorkingDirectory() + "/profiles/" + typeDescription);
		File[] profileFiles = profileDir.listFiles();
		if (profileFiles != null) {
    		for (File profileFile : profileFiles) {
    			if (profileFile.lastModified() > snapshotSaved) {
    				log.warn("consistencyCheck(): fixing file=" + profileFile.getName());
    				Vector<Profile> profiles = ProfileManager.readProfilesFromFile(profileFile.getAbsolutePath(), type);
    				for (Profile profile : profiles) {
    					if ((profile.getUniqueId() >= index.getNextAvailableUniqueId())
    							|| (index.getRecord(profile.getUniqueId()) == null)) {
    						// The profile was created after the last save point, with no index reference, delete it.
    						ProfileManager.deleteProfileFile(profile.getUniqueId(), type, typeDescription);
    						// Delete potential mined profiles:
    						Record record = profile.getRecord();
    						if (record instanceof SearchRecord) {
    							for (byte source : ((SearchRecord) profile.getRecord()).getMinedProfileSources()) {
    								ProfileManager.deleteMinedProfile(type, source, profile.getUniqueId());
    							}
    						}
    					} else {
    						// This was a previously existing profile, scrub it.
    						if (profile instanceof ReleaseGroupProfile) {
    							((ReleaseGroupProfile) profile).removeAssociatedReleasesBeyond(Database.getReleaseIndex().getNextAvailableUniqueId());
    						}
    						if (profile instanceof SongGroupProfile) {
    							((SongGroupProfile) profile).removeAssociatedSongsBeyond(Database.getSongIndex().getNextAvailableUniqueId());
    						}
    						if (profile instanceof SearchProfile) {
    							((SearchProfile) profile).removeMinedDataAfter(snapshotSaved);
    							if (profile.getRecord() instanceof SearchRecord) {
    								((SearchRecord) profile.getRecord()).removeStyleIdsBeyond(Database.getStyleIndex().getNextAvailableUniqueId());
    								((SearchRecord) profile.getRecord()).removeTagIdsBeyond(Database.getTagIndex().getNextAvailableUniqueId());
    							}
    						}
    						if (profile.getRecord() instanceof ArtistRecord) {
    							((ArtistRecord) profile.getRecord()).removeLabelIdsBeyond(Database.getLabelIndex().getNextAvailableUniqueId());
    						}
    						profile.save();
    					}
    				}
    			}
    		}
		}
		if (RE3Properties.getBoolean("perform_full_consistency_check")) {
    		Vector<Integer> removedProfileIds = new Vector<Integer>();
    		for (int recordId : index.getIds()) {
    			if (index.getProfile(recordId) == null)
    				removedProfileIds.add(recordId);
    		}
    		if (removedProfileIds.size() > 0) {
    			log.warn("consistencyCheck(): deleting profile records with missing profiles (deleted since last save point?)=" + removedProfileIds);
    			for (int removedId : removedProfileIds)
    				index.delete(removedId);
    		}
		}
    }

    static public Database load() { return load(false); }
    static public Database load(boolean consistencyCheck) {
    	try {
	    	// attempt to load normally
	    	if (RE3Properties.getProperty("index_persistence_mode").equalsIgnoreCase("java"))
	    		instance = (Database)Serializer.readCompressedData(snapshotFilename);
	    	else if (RE3Properties.getProperty("index_persistence_mode").equalsIgnoreCase("xml"))
	    		instance = (Database)XMLSerializer.readData(xmlFilename);
	    	else if (RE3Properties.getProperty("index_persistence_mode").equalsIgnoreCase("custom"))
	    		instance = (Database)CustomLocalProfileIO.readObject(Database.class, databaseFilename, true);

	    	if (instance == null) {
	    		// upgrade from old preferred method...
	    		if (RE3Properties.getProperty("index_persistence_mode").equalsIgnoreCase("custom")) {
	    			File javaFile = new File(snapshotFilename);
	    			if (javaFile.exists())
	    				instance = (Database)Serializer.readCompressedData(snapshotFilename);
	    		}
	    	}

	    	// if the db is still not loaded, check backups
	    	long lastSaved = 0;
	    	if (instance == null) {
	    		Vector<String> backupFiles = getBackupFiles();
	    		int i = backupFiles.size() - 1;
	    		while ((i >= 0) && (instance == null)) {
	    			log.warn("load(): attempting to load from backup=" + backupFiles.get(i));

	    			if (RE3Properties.getProperty("index_persistence_mode").equalsIgnoreCase("java"))
	        			instance = (Database)Serializer.readCompressedData(OSHelper.getWorkingDirectory() + "/" + backupFiles.get(i));
	    			else if (RE3Properties.getProperty("index_persistence_mode").equalsIgnoreCase("xml"))
	        			instance = (Database)XMLSerializer.readData(OSHelper.getWorkingDirectory() + "/" + backupFiles.get(i));
	    			else if (RE3Properties.getProperty("index_persistence_mode").equalsIgnoreCase("custom"))
	    				instance = (Database)CustomLocalProfileIO.readObject(Database.class, OSHelper.getWorkingDirectory() + "/" + backupFiles.get(i), true);

	        		if (instance != null)
	        			log.warn("load(): loaded from backup=" + backupFiles.get(i));
	        		lastSaved = new File(OSHelper.getWorkingDirectory() + "/" + backupFiles.get(i)).lastModified();
	        		if (lastSaved == 0)
	        			lastSaved = System.currentTimeMillis();
	    			--i;
	    		}
	    		if (instance != null)
	    			consistencyCheck = true;
	    	} else {
	    		instance.initIndexes();
	    		File file = null;
	    		if (RE3Properties.getProperty("index_persistence_mode").equalsIgnoreCase("java"))
	    			file = new File(snapshotFilename);
	    		else if (RE3Properties.getProperty("index_persistence_mode").equalsIgnoreCase("xml"))
	    			file = new File(xmlFilename);
	    		else if (RE3Properties.getProperty("index_persistence_mode").equalsIgnoreCase("custom"))
	    			file = new File(databaseFilename);
	    		lastSaved = file.lastModified();
	    	}
	    	if (instance == null) {
	    		instance = new Database();
	        	if (log.isDebugEnabled())
	        		log.debug("load(): created a new database");
	    		for (UserDataType userDataType : Database.getArtistIndex().getUserDataTypes())
	    			getArtistIndex().getSearchModelManager().addUserColumn(userDataType);
	    		for (UserDataType userDataType : Database.getLabelIndex().getUserDataTypes())
	    			getLabelIndex().getSearchModelManager().addUserColumn(userDataType);
	    		for (UserDataType userDataType : Database.getReleaseIndex().getUserDataTypes())
	    			getReleaseIndex().getSearchModelManager().addUserColumn(userDataType);
	    		for (UserDataType userDataType : Database.getSongIndex().getUserDataTypes())
	    			getSongIndex().getSearchModelManager().addUserColumn(userDataType);
	    	} else {
	        	if (log.isDebugEnabled()) {
	        		debugDatabaseStats();
	        	}
	        	if (consistencyCheck || RE3Properties.getBoolean("perform_consistency_check_always")) {
	        		// in case there was a crash, clean up any profiles that were modified after snapshot
	        		consistencyCheck(lastSaved);
	        	}
	    	}
	    	instance.instanceInit();
    	} catch (Exception e) {
    		log.error("load(): error", e);
    	}
    	return instance;
    }

    static private void debugDatabaseStats() {
		log.debug("load(): loaded succesfully");
		if (log.isTraceEnabled()) {
			log.debug("load(): \t# artists=" + Database.getArtistIndex().getSize() + " (" + Database.getArtistIndex().getSizeInternalItems() + "/" + Database.getArtistIndex().getSizeExternalItems() + ")");
			log.debug("load(): \t# labels=" + Database.getLabelIndex().getSize() + " (" + Database.getLabelIndex().getSizeInternalItems() + "/" + Database.getLabelIndex().getSizeExternalItems() + ")");
			log.debug("load(): \t# releases=" + Database.getReleaseIndex().getSize() + " (" + Database.getReleaseIndex().getSizeInternalItems() + "/" + Database.getReleaseIndex().getSizeExternalItems() + ")");
			log.debug("load(): \t# songs=" + Database.getSongIndex().getSize() + " (" + Database.getSongIndex().getSizeInternalItems() + "/" + Database.getSongIndex().getSizeExternalItems() + ")");
		} else {
			log.debug("load(): \t# artists=" + Database.getArtistIndex().getSize());
			log.debug("load(): \t# labels=" + Database.getLabelIndex().getSize());
			log.debug("load(): \t# releases=" + Database.getReleaseIndex().getSize());
			log.debug("load(): \t# songs=" + Database.getSongIndex().getSize());
		}
		log.debug("load(): \t# styles=" + Database.getStyleIndex().getSize());
		log.debug("load(): \t# tags=" + Database.getTagIndex().getSize());
		log.debug("load(): \t# playlists=" + Database.getPlaylistIndex().getSize());
    }

    static private String getBackupFilename() {
		Calendar cal = Calendar.getInstance();
		int dayInt = cal.get(Calendar.DATE);
		String day = (dayInt >= 10) ? String.valueOf(dayInt) : "0" + dayInt;
		int monthInt = cal.get(Calendar.MONTH) + 1;
		String month = (monthInt >= 10) ? String.valueOf(monthInt) : "0" + monthInt;
		int yearInt = cal.get(Calendar.YEAR);
		String year = String.valueOf(yearInt);
		StringBuffer filename = new StringBuffer();
		filename.append(backupFilenamePrefix);
		filename.append(year);
		filename.append("-");
		filename.append(month);
		filename.append("-");
		filename.append(day);
		if (RE3Properties.getProperty("index_persistence_mode").equalsIgnoreCase("java"))
			filename.append(".snapshot");
		else if (RE3Properties.getProperty("index_persistence_mode").equalsIgnoreCase("xml"))
			filename.append(".xml");
		else if (RE3Properties.getProperty("index_persistence_mode").equalsIgnoreCase("custom"))
			filename.append(".jso");
		return OSHelper.getWorkingDirectory() + "/" + filename.toString();
    }

    static public void close() {
    	for (CommonIndex index : getAllIndexes())
    		index.getImdb().close();
    }

    static public boolean save() {
    	if (log.isDebugEnabled())
    		log.debug("save(): saving database, free memory=" + (Runtime.getRuntime().freeMemory() / 1024 / 1024) + "mb");
    	boolean result = false;
    	if (instance != null) {

    		ProfileManager.savePendingProfiles();

    		// rename existing to backups..
    		String existingFilename = null;
    		if (RE3Properties.getProperty("index_persistence_mode").equalsIgnoreCase("java"))
    			existingFilename = snapshotFilename;
    		else if (RE3Properties.getProperty("index_persistence_mode").equalsIgnoreCase("xml"))
    			existingFilename = xmlFilename;
    		else if (RE3Properties.getProperty("index_persistence_mode").equalsIgnoreCase("custom"))
    			existingFilename = databaseFilename;
			File existingFile = new File(existingFilename);
			if (existingFile.exists()) {
				File backupFile = new File(getBackupFilename());
				while (backupFile.exists())
					backupFile.delete();
				existingFile.renameTo(backupFile);
			}

    		int saveAttempts = 1;
    		if (RE3Properties.getProperty("index_persistence_mode").equalsIgnoreCase("java"))
    			result = Serializer.saveCompressedData(instance, snapshotFilename);
    		else if (RE3Properties.getProperty("index_persistence_mode").equalsIgnoreCase("xml"))
    			result = XMLSerializer.saveData(instance, xmlFilename);
    		else if (RE3Properties.getProperty("index_persistence_mode").equalsIgnoreCase("custom")) {
    			LineWriter writer = LineWriterFactory.getLineWriter(databaseFilename);
    			instance.write(writer);
    			writer.close();
    			result = true;
    		}

    		while (!result && (saveAttempts < MAX_SAVE_ATTEMPTS)) {
    			try {
    				Thread.sleep(15000);
    				if (RE3Properties.getProperty("index_persistence_mode").equalsIgnoreCase("java"))
    	    			result = Serializer.saveCompressedData(instance, snapshotFilename);
    	    		else if (RE3Properties.getProperty("index_persistence_mode").equalsIgnoreCase("xml"))
    	    			result = XMLSerializer.saveData(instance, xmlFilename);
    	    		else if (RE3Properties.getProperty("index_persistence_mode").equalsIgnoreCase("custom")) {
    	    			LineWriter writer = LineWriterFactory.getLineWriter(databaseFilename);
    	    			instance.write(writer);
    	    			writer.close();
    	    			result = true;
    	    		}
    				++saveAttempts;
    			} catch (Exception e) {
    				log.error("save(): error", e);
    			}
    		}
    		for (CommonIndex index : Database.getAllIndexes()) {
				index.getImdb().commit();
    		}

    		// clean up extra backup files
    		Vector<String> backupFiles = getBackupFiles();
    		java.util.Collections.sort(backupFiles); // will ensure the oldest are deleted first
    		while (backupFiles.size() > RE3Properties.getInt("max_num_database_backup_files")) {
    			String backupFile = backupFiles.remove(0);
    			File file = new File(OSHelper.getWorkingDirectory() + "/" + backupFile);
    			file.delete();
    		}

    	}
    	return result;
    }

    static private Vector<String> getBackupFiles() {
    	File[] allFiles = OSHelper.getWorkingDirectory().listFiles();
    	Vector<String> result = new Vector<String>();
    	for (File file : allFiles) {
    		if (file.getName().startsWith(backupFilenamePrefix))
    			result.add(file.getName());
    	}
    	java.util.Collections.sort(result);
    	return result;
    }

    ///////////////
    // ACCESSORS //
    ///////////////

    static private void checkInit() {
    	// test solution for lazy init
    	//if (instance == null)
    		//instance = new Database();
    }

    static public ArtistIndex getArtistIndex() {
    	checkInit();
    	if (instance != null)
    		return instance.artistsIndex;
    	return null;
    }
    static public LabelIndex getLabelIndex() {
    	checkInit();
    	if (instance != null)
    		return instance.labelsIndex;
    	return null;
    }
    static public ReleaseIndex getReleaseIndex() {
    	checkInit();
    	if (instance != null)
    		return instance.releasesIndex;
    	return null;
    }
    static public SongIndex getSongIndex() {
    	checkInit();
    	if (instance != null)
    		return instance.songsIndex;
    	return null;
    }
    static public MixoutIndex getMixoutIndex() {
    	checkInit();
    	if (instance != null)
    		return instance.mixoutsIndex;
    	return null;
    }
    static public StyleIndex getStyleIndex() {
    	checkInit();
    	if (instance != null)
    		return instance.stylesIndex;
    	return null;
    }
    static public TagIndex getTagIndex() {
    	checkInit();
    	if (instance != null)
    		return instance.tagsIndex;
    	return null;
    }
    static public PlaylistIndex getPlaylistIndex() {
    	checkInit();
    	if (instance != null)
    		return instance.playlistsIndex;
    	return null;
    }

    static public ArtistModelManager getArtistModelManager() { return (ArtistModelManager)instance.artistsIndex.getModelManager(); }
    static public LabelModelManager getLabelModelManager() { return (LabelModelManager)instance.labelsIndex.getModelManager(); }
    static public ReleaseModelManager getReleaseModelManager() { return (ReleaseModelManager)instance.releasesIndex.getModelManager(); }
    static public SongModelManager getSongModelManager() { return (SongModelManager)instance.songsIndex.getModelManager(); }
    static public RecommendedArtistModelManager getRecommendedArtistModelManager() { return (RecommendedArtistModelManager)instance.artistsIndex.getRecommendedModelManager(); }
    static public RecommendedLabelModelManager getRecommendedLabelModelManager() { return (RecommendedLabelModelManager)instance.labelsIndex.getRecommendedModelManager(); }
    static public RecommendedReleaseModelManager getRecommendedReleaseModelManager() { return (RecommendedReleaseModelManager)instance.releasesIndex.getRecommendedModelManager(); }
    static public RecommendedSongModelManager getRecommendedSongModelManager() { return (RecommendedSongModelManager)instance.songsIndex.getRecommendedModelManager(); }
    static public MixoutModelManager getMixoutModelManager() { return (MixoutModelManager)instance.mixoutsIndex.getModelManager(); }
    static public StyleModelManager getStyleModelManager() { return (StyleModelManager)instance.stylesIndex.getModelManager(); }
    static public TagModelManager getTagModelManager() { return (TagModelManager)instance.tagsIndex.getModelManager(); }
    static public PlaylistModelManager getPlaylistModelManager() { return (PlaylistModelManager)instance.playlistsIndex.getModelManager(); }

    ////////////
    // EVENTS //
    ////////////

    static public void styleHierarchyChanged(StyleRecord style) {
    	if (log.isDebugEnabled())
    		log.debug("styleHierarchyChanged(): style=" + style);
    	for (SearchResult searchRecord : style.getArtistRecords())
    		((SearchRecord)searchRecord.getRecord()).invalidateActualStyles();
    	for (SearchResult searchRecord : style.getLabelRecords())
    		((SearchRecord)searchRecord.getRecord()).invalidateActualStyles();
    	for (SearchResult searchRecord : style.getReleaseRecords())
    		((SearchRecord)searchRecord.getRecord()).invalidateActualStyles();
    	for (SearchResult searchRecord : style.getSongRecords())
    		((SearchRecord)searchRecord.getRecord()).invalidateActualStyles();
    	updateFilterAndParents(style);
    }

    static public boolean doUpdateFilterAndParentsOnHierarchyChanged = true; // hack: used as an override while importing from RE2, the method below was causing issues with styles
    static private void updateFilterAndParents(HierarchicalRecord filter) {
    	if (doUpdateFilterAndParentsOnHierarchyChanged) {
    		for (HierarchicalRecord parent : filter.getParentRecords()) {
    			if (!parent.isRoot()) {
    				parent.update();
    				updateFilterAndParents(parent);
    			}
    		}
    	}
    }

    static public void tagHierarchyChanged(TagRecord tag) {
    	if (log.isDebugEnabled())
    		log.debug("tagHierarchyChanged(): tag=" + tag);
    	for (SearchResult searchRecord : tag.getArtistRecords())
    		((SearchRecord)searchRecord.getRecord()).invalidateActualTags();
    	for (SearchResult searchRecord : tag.getLabelRecords())
    		((SearchRecord)searchRecord.getRecord()).invalidateActualTags();
    	for (SearchResult searchRecord : tag.getReleaseRecords())
    		((SearchRecord)searchRecord.getRecord()).invalidateActualTags();
    	for (SearchResult searchRecord : tag.getSongRecords())
    		((SearchRecord)searchRecord.getRecord()).invalidateActualTags();
    	updateFilterAndParents(tag);
    }

    ///////////////////////////
    // CONVENIENCE FUNCTIONS //
    ///////////////////////////

    static public Profile addOrUpdate(SubmittedProfile submittedProfile) throws AlreadyExistsException, InsufficientInformationException, UnknownErrorException {
    	Identifier id = submittedProfile.getIdentifier();
    	if (id instanceof ArtistIdentifier)
    		return getArtistIndex().addOrUpdate(submittedProfile);
    	else if (id instanceof LabelIdentifier)
    		return getLabelIndex().addOrUpdate(submittedProfile);
    	else if (id instanceof ReleaseIdentifier)
    		return getReleaseIndex().addOrUpdate(submittedProfile);
    	else if (id instanceof SongIdentifier)
    		return getSongIndex().addOrUpdate(submittedProfile);
    	else if (id instanceof MixoutIdentifier)
    		return getMixoutIndex().addOrUpdate(submittedProfile);
    	else if (id instanceof StyleIdentifier)
    		return getStyleIndex().addOrUpdate(submittedProfile);
    	else if (id instanceof TagIdentifier)
    		return getTagIndex().addOrUpdate(submittedProfile);
    	else if (id instanceof PlaylistIdentifier)
    		return getPlaylistIndex().addOrUpdate(submittedProfile);
    	return null;
    }

    static public Profile add(SubmittedProfile submittedProfile) throws AlreadyExistsException, InsufficientInformationException, UnknownErrorException {
    	Identifier id = submittedProfile.getIdentifier();
    	if (id instanceof ArtistIdentifier)
    		return getArtistIndex().add(submittedProfile);
    	else if (id instanceof LabelIdentifier)
    		return getLabelIndex().add(submittedProfile);
    	else if (id instanceof ReleaseIdentifier)
    		return getReleaseIndex().add(submittedProfile);
    	else if (id instanceof SongIdentifier)
    		return getSongIndex().add(submittedProfile);
    	else if (id instanceof MixoutIdentifier)
    		return getMixoutIndex().add(submittedProfile);
    	else if (id instanceof StyleIdentifier)
    		return getStyleIndex().add(submittedProfile);
    	else if (id instanceof TagIdentifier)
    		return getTagIndex().add(submittedProfile);
    	else if (id instanceof PlaylistIdentifier)
    		return getPlaylistIndex().add(submittedProfile);
    	return null;
    }

    static public Record getRecord(Identifier id) {
    	if (id instanceof ArtistIdentifier)
    		return getArtistIndex().getRecord(id);
    	else if (id instanceof LabelIdentifier)
    		return getLabelIndex().getRecord(id);
    	else if (id instanceof ReleaseIdentifier)
    		return getReleaseIndex().getRecord(id);
    	else if (id instanceof SongIdentifier)
    		return getSongIndex().getRecord(id);
    	else if (id instanceof MixoutIdentifier)
    		return getMixoutIndex().getRecord(id);
    	else if (id instanceof StyleIdentifier)
    		return getStyleIndex().getRecord(id);
    	else if (id instanceof TagIdentifier)
    		return getTagIndex().getRecord(id);
    	else if (id instanceof PlaylistIdentifier)
    		return getPlaylistIndex().getRecord(id);
    	return null;
    }

    static public Profile getProfile(Identifier id) {
    	if (id instanceof ArtistIdentifier)
    		return getArtistIndex().getProfile(id);
    	else if (id instanceof LabelIdentifier)
    		return getLabelIndex().getProfile(id);
    	else if (id instanceof ReleaseIdentifier)
    		return getReleaseIndex().getProfile(id);
    	else if (id instanceof SongIdentifier)
    		return getSongIndex().getProfile(id);
    	else if (id instanceof MixoutIdentifier)
    		return getMixoutIndex().getProfile(id);
    	else if (id instanceof StyleIdentifier)
    		return getStyleIndex().getProfile(id);
    	else if (id instanceof TagIdentifier)
    		return getTagIndex().getProfile(id);
    	else if (id instanceof PlaylistIdentifier)
    		return getPlaylistIndex().getProfile(id);
    	return null;
    }

    static public void mergeProfiles(Profile primaryProfile, Profile mergedProfile) {
    	if ((primaryProfile == null) || (mergedProfile == null))
    		return;
    	if (primaryProfile.getUniqueId() == mergedProfile.getUniqueId())
    		return;
    	Identifier id = primaryProfile.getIdentifier();
    	if (id instanceof ArtistIdentifier)
    		getArtistIndex().mergeProfiles(primaryProfile, mergedProfile);
    	else if (id instanceof LabelIdentifier)
    		getLabelIndex().mergeProfiles(primaryProfile, mergedProfile);
    	else if (id instanceof ReleaseIdentifier)
    		getReleaseIndex().mergeProfiles(primaryProfile, mergedProfile);
    	else if (id instanceof SongIdentifier)
    		getSongIndex().mergeProfiles(primaryProfile, mergedProfile);
    	else if (id instanceof MixoutIdentifier)
    		getMixoutIndex().mergeProfiles(primaryProfile, mergedProfile);
    	else if (id instanceof StyleIdentifier)
    		getStyleIndex().mergeProfiles(primaryProfile, mergedProfile);
    	else if (id instanceof TagIdentifier)
    		getTagIndex().mergeProfiles(primaryProfile, mergedProfile);
    	else if (id instanceof PlaylistIdentifier)
    		getPlaylistIndex().mergeProfiles(primaryProfile, mergedProfile);
    	return;
    }

    static public void delete(Identifier id) {
    	if (id instanceof ArtistIdentifier)
    		getArtistIndex().delete(id);
    	else if (id instanceof LabelIdentifier)
    		getLabelIndex().delete(id);
    	else if (id instanceof ReleaseIdentifier)
    		getReleaseIndex().delete(id);
    	else if (id instanceof SongIdentifier)
    		getSongIndex().delete(id);
    	else if (id instanceof MixoutIdentifier)
    		getMixoutIndex().delete(id);
    	else if (id instanceof StyleIdentifier)
    		getStyleIndex().delete(id);
    	else if (id instanceof TagIdentifier)
    		getTagIndex().delete(id);
    	else if (id instanceof PlaylistIdentifier)
    		getPlaylistIndex().delete(id);
    }

    static public void delete(Record record) {
    	Identifier id = record.getIdentifier();
    	if (id instanceof ArtistIdentifier)
    		getArtistIndex().delete(record.getUniqueId());
    	else if (id instanceof LabelIdentifier)
    		getLabelIndex().delete(record.getUniqueId());
    	else if (id instanceof ReleaseIdentifier)
    		getReleaseIndex().delete(record.getUniqueId());
    	else if (id instanceof SongIdentifier)
    		getSongIndex().delete(record.getUniqueId());
    	else if (id instanceof MixoutIdentifier)
    		getMixoutIndex().delete(record.getUniqueId());
    	else if (id instanceof StyleIdentifier)
    		getStyleIndex().delete(record.getUniqueId());
    	else if (id instanceof TagIdentifier)
    		getTagIndex().delete(record.getUniqueId());
    	else if (id instanceof PlaylistIdentifier)
    		getPlaylistIndex().delete(record.getUniqueId());
    }

    static public CommonIndex[] getAllIndexes() { return new CommonIndex[] { instance.artistsIndex, instance.labelsIndex, instance.releasesIndex, instance.songsIndex, instance.stylesIndex, instance.tagsIndex, instance.playlistsIndex }; }
    static public SearchIndex[] getSearchIndexes() { return new SearchIndex[] { instance.artistsIndex, instance.labelsIndex, instance.releasesIndex, instance.songsIndex }; }
    static public FilterIndex[] getFilterIndexes() { return new FilterIndex[] { instance.stylesIndex, instance.tagsIndex, instance.playlistsIndex }; }

    static public RelativeModelFactory getRelativeModelFactory() { return instance.relativeModelFactory; }

    static public boolean hasImportedFromITunes() { return instance.properties.containsKey("hasImportedFromITunes"); }
    static public void setHasImportedFromITunes() { instance.properties.put("hasImportedFromITunes", true); }

    static public boolean hasImportedFromTraktor() { return instance.properties.containsKey("hasImportedFromTraktor"); }
    static public void setHasImportedFromTraktor() { instance.properties.put("hasImportedFromTraktor", true); }

    static public boolean hasImportedFromRE2() { return instance.properties.containsKey("hasImportedFromRE2"); }
    static public void setHasImportedFromRE2() { instance.properties.put("hasImportedFromRE2", true); }
    static public void incrementImportedRE2Songs() {
    	int count = 0;
    	if (instance.properties.containsKey("numImportedSongsFromRE2"))
    		count = (Integer)instance.properties.get("numImportedSongsFromRE2");
    	++count;
    	instance.properties.put("numImportedSongsFromRE2", count);
    }
    static public int getNumImportedRE2Songs() {
    	if (instance.properties.containsKey("numImportedSongsFromRE2"))
    		return (Integer)instance.properties.get("numImportedSongsFromRE2");
    	return 0;
    }

    static public Object getProperty(String key) {
    	if (instance != null)
    		return instance.properties.get(key);
    	return null;
    }
    static public void setProperty(String key, Object value) {
    	if (instance != null)
    		instance.properties.put(key, value);
    }

    static public UserProfile getUserProfile(boolean createIfNecessary) {
    	if (createIfNecessary && instance.userProfile == null) {
    		instance.userProfile = new LocalUserProfile();
    	}
    	return instance.userProfile;
    }
    static public UserProfile getUserProfile() { return getUserProfile(true); }

    static public WebServerManager getWebServerManager() {
    	if (instance != null)
    		return instance.webServerManager;
    	return null;
    }

    ////////////
    // FIELDS //
    ////////////

    private ArtistIndex artistsIndex;
    private LabelIndex labelsIndex;
    private ReleaseIndex releasesIndex;
    private SongIndex songsIndex;
    private MixoutIndex mixoutsIndex;
    private StyleIndex stylesIndex;
    private TagIndex tagsIndex;
    private PlaylistIndex playlistsIndex;

    private RelativeModelFactory relativeModelFactory; // kept here for persistence

    private Map<String, Object> properties = new HashMap<String, Object>();

    private UserProfile userProfile = null;

    private WebServerManager webServerManager;

    transient private boolean isNewDatabase; // will default to false

    /////////////////
    // CONSTRUCTOR //
    /////////////////

    public Database() {
    	instance = this;

        artistsIndex = new ArtistIndex();
        labelsIndex = new LabelIndex();
        releasesIndex = new ReleaseIndex();
        songsIndex = new SongIndex();
        mixoutsIndex = new MixoutIndex();
        stylesIndex = new StyleIndex();
        tagsIndex = new TagIndex();
        playlistsIndex = new PlaylistIndex();

        relativeModelFactory = new RelativeModelFactory();
        webServerManager = new WebServerManager();

        isNewDatabase = true;
    }

    public Database(LineReader lineReader) throws Exception {
    	int version = Integer.parseInt(lineReader.getNextLine());
    	if (version >= 2)
    		lineReader.getNextLine(); // START
    	artistsIndex = new ArtistIndex(lineReader);
        labelsIndex = new LabelIndex(lineReader);
        releasesIndex = new ReleaseIndex(lineReader);
        songsIndex = new SongIndex(lineReader);
        mixoutsIndex = new MixoutIndex(lineReader);
        stylesIndex = new StyleIndex(lineReader);
        tagsIndex = new TagIndex(lineReader);
        playlistsIndex = new PlaylistIndex(lineReader);
        try {
        	// This last part is in its own try block as a way of being able to load older databases that are incompatible
        	// due to code that had to be ripped out in order to open source it...
	        relativeModelFactory = new RelativeModelFactory(lineReader);
	        int numProperties = Integer.parseInt(lineReader.getNextLine());
	        for (int i = 0; i < numProperties; ++i) {
	        	String key = lineReader.getNextLine();
	        	Object value = null;
	        	int valueType = Integer.parseInt(lineReader.getNextLine());
	        	if (valueType == 1) {
	        		value = lineReader.getNextLine();
	        	} else if (valueType == 2) {
	        		value = Integer.parseInt(lineReader.getNextLine());
	        	} else if (valueType == 3) {
	        		value = Float.parseFloat(lineReader.getNextLine());
	        	} else if (valueType == 4) {
	        		value = Long.parseLong(lineReader.getNextLine());
	        	} else if (valueType == 5) {
	        		value = Short.parseShort(lineReader.getNextLine());
	        	} else if (valueType == 6) {
	        		value = Byte.parseByte(lineReader.getNextLine());
	        	} else if (valueType == 7) {
	        		value = Boolean.parseBoolean(lineReader.getNextLine());
	        	}
	        	properties.put(key, value);
	        }
	        int userProfileCount = Integer.parseInt(lineReader.getNextLine());
	        if (userProfileCount > 0)
	        	userProfile = new LocalUserProfile(lineReader);
	        if (version >= 4) {
	        	webServerManager = new WebServerManager(lineReader);
	        } else {
	        	webServerManager = new WebServerManager();
	        }
	        isNewDatabase = Boolean.parseBoolean(lineReader.getNextLine());
	        if (version >= 2) {
	        	String endLine = lineReader.getNextLine();
	        	if (!endLine.equals("END"))
	        		throw new Exception();
	        }
        } catch (Exception e) {
        	if (version < 5) {
        		log.error("An older, incompatible database was found.  Attempting to load anyway, but might not not succeed...");
                relativeModelFactory = new RelativeModelFactory();
                userProfile = new LocalUserProfile();
                webServerManager = new WebServerManager();
                isNewDatabase = false;
        	} else {
        		// Something else happened that should be noticed...
        		throw e;
        	}
        }
    }

    public void instanceInit() {
    	stylesIndex.addHierarchyChangeListener(this);
    	tagsIndex.addHierarchyChangeListener(this);
    	RE3Properties.addChangeListener(this);
    }

    @Override
	public void hierarchyChanged(HierarchicalIndex index, HierarchicalRecord record) {
    	if (index == stylesIndex)
    		styleHierarchyChanged((StyleRecord)record);
    	if (index == tagsIndex)
    		tagHierarchyChanged((TagRecord)record);
    }

    /////////////
    // GETTERS //
    /////////////

	public Map<String, Object> getProperties() {
		return properties;
	}

	// for serialization
	public ArtistIndex getArtistsIndex() { return artistsIndex; }
	public LabelIndex getLabelsIndex() { return labelsIndex; }
	public ReleaseIndex getReleasesIndex() { return releasesIndex; }
	public SongIndex getSongsIndex() { return songsIndex; }
	public MixoutIndex getMixoutsIndex() { return mixoutsIndex; }
	public StyleIndex getStylesIndex() { return stylesIndex; }
	public TagIndex getTagsIndex() { return tagsIndex; }
	public PlaylistIndex getPlaylistsIndex() { return playlistsIndex; }

	/////////////
	// SETTERS //
	/////////////

	public void initIndexes() {
		for (CommonIndex index : getAllIndexes()) {
			index.init();
		}
	}

	// for serialization
	public void setProperties(Map<String, Object> properties) { this.properties = properties; }
	public void setArtistsIndex(ArtistIndex artistIndex) { this.artistsIndex = artistIndex; }
	public void setLabelsIndex(LabelIndex labelIndex) { this.labelsIndex = labelIndex; }
	public void setReleasesIndex(ReleaseIndex releaseIndex) { this.releasesIndex = releaseIndex; }
	public void setSongsIndex(SongIndex songIndex) { this.songsIndex = songIndex; }
	public void setMixoutsIndex(MixoutIndex mixoutIndex) { this.mixoutsIndex = mixoutIndex; }
	public void setStylesIndex(StyleIndex styleIndex) { this.stylesIndex = styleIndex; }
	public void setTagsIndex(TagIndex tagIndex) { this.tagsIndex = tagIndex; }
	public void setPlaylistsIndex(PlaylistIndex playlistIndex) { this.playlistsIndex = playlistIndex; }
	public void setRelativeModelFactory(RelativeModelFactory relativeModelFactory) { this.relativeModelFactory = relativeModelFactory; }
	public void setUserProfile(UserProfile userProfile) { this.userProfile = userProfile; }

	//////////////////////
	// INSTANCE METHODS //
	//////////////////////

	@Override
	public void propertiesChanged() {
		UserProfile.initProperties();
	}

	public void write(LineWriter writer) {
		writer.writeLine(5); // version
		writer.writeLine("START");
		artistsIndex.write(writer);
		labelsIndex.write(writer);
		releasesIndex.write(writer);
		songsIndex.write(writer);
		mixoutsIndex.write(writer);
		stylesIndex.write(writer);
		tagsIndex.write(writer);
		playlistsIndex.write(writer);
		relativeModelFactory.write(writer);
		writer.writeLine(properties.size());
		for (Entry<String, Object> entry : properties.entrySet()) {
			writer.writeLine(entry.getKey());
			Object value = entry.getValue();
			if (value instanceof String) {
				writer.writeLine(1);
				writer.writeLine((String)value);
			} else if (value instanceof Integer) {
				writer.writeLine(2);
				writer.writeLine((Integer)value);
			} else if (value instanceof Float) {
				writer.writeLine(3);
				writer.writeLine((Float)value);
			} else if (value instanceof Long) {
				writer.writeLine(4);
				writer.writeLine((Long)value);
			} else if (value instanceof Short) {
				writer.writeLine(5);
				writer.writeLine((Short)value);
			} else if (value instanceof Byte) {
				writer.writeLine(6);
				writer.writeLine((Byte)value);
			} else if (value instanceof Boolean) {
				writer.writeLine(7);
				writer.writeLine((Boolean)value);
			} else {
				if (value != null) {
					log.warn("write(): unknown property type=" + value.getClass());
				}
				writer.writeLine(0);
			}
		}
		if (userProfile != null) {
			writer.writeLine(1);
			userProfile.write(writer);
		} else {
			writer.writeLine(0);
		}
		webServerManager.write(writer);
		writer.writeLine(isNewDatabase);
		writer.writeLine("END");
	}

	//////////
	// TEST //
	//////////

	static public void main(String[] args) {
		try {
			RapidEvolution3.loadLog4J();


		} catch (Exception e) {
			log.error("main(): error", e);
		}
	}

}
