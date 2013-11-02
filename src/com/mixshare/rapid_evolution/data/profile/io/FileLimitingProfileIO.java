package com.mixshare.rapid_evolution.data.profile.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.DataConstantsHelper;
import com.mixshare.rapid_evolution.data.identifier.Identifier;
import com.mixshare.rapid_evolution.data.mined.MinedProfile;
import com.mixshare.rapid_evolution.data.mined.MinedProfileHeader;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.profile.filter.playlist.PlaylistProfile;
import com.mixshare.rapid_evolution.data.profile.filter.style.StyleProfile;
import com.mixshare.rapid_evolution.data.profile.filter.tag.TagProfile;
import com.mixshare.rapid_evolution.data.profile.io.FileActionBuffer.ActionApplicator;
import com.mixshare.rapid_evolution.data.profile.io.FileActionBuffer.ActionApplicatorFactory;
import com.mixshare.rapid_evolution.data.profile.search.artist.ArtistProfile;
import com.mixshare.rapid_evolution.data.profile.search.label.LabelProfile;
import com.mixshare.rapid_evolution.data.profile.search.release.ReleaseProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.MixoutProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.util.cache.LRUCache;
import com.mixshare.rapid_evolution.data.util.io.Serializer;
import com.mixshare.rapid_evolution.util.FileUtil;
import com.mixshare.rapid_evolution.util.OSHelper;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineReaderFactory;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.mixshare.rapid_evolution.util.io.LineWriterFactory;

/**
 * This class manages the persistence of profiles into the file system, and their retrieval (CRUD operations)...
 * <p>
 * It caps each data type to a maximum number of file handles to store the profiles.
 */
public class FileLimitingProfileIO implements ProfileIOInterface {

    static private Logger log = Logger.getLogger(FileLimitingProfileIO.class);

    // NOTE: safe saving is a simple mechanism to prevent corruption of profiles if the program abnormally terminates while writing to
    // the hard disk.  if the profile is being overwritten, it is first moved to a temporary directory, then the new profile is written.
    // the first profile is then cleared from the temporary directory upon success.  if the program terminates during this process,
    // the application will detect the existence of the profile in the temp directory upon startup and restore it from backup.
    // this variable should always be true, it is stored as a variable so it can be temporarily turned off during restoration
    static public boolean SAFE_SAVE = true;
    static private int PROFILE_SAVE_MAX_RETRIES = 5;
    static private long PROFILE_SAVE_FAIL_WAIT_RETRY_MILLIS = 5000;

    static long FILE_DELETE_WAIT_INTERVAL = 1000;
    static int FILE_DELETE_MAX_TRIES = 3;

    // TEMP: when switching to use this, rename the directories to be as expected.
    static private String baseProfileDirectory = OSHelper.getWorkingDirectory() + "/profiles/";
    static private String minedProfileBaseProfileDirectory = OSHelper.getWorkingDirectory() + "/mined_profiles/";

    static private Map<Byte, Map<Integer, FileActionBuffer<Profile, Integer>>> profileFileBuffers =
    		new HashMap<Byte, Map<Integer, FileActionBuffer<Profile, Integer>>>();
    static private Map<Byte, Map<Integer, FileActionBuffer<MinedProfile, MetaMinedProfileHeader>>> minedProfileFileBuffers =
    		new HashMap<Byte, Map<Integer, FileActionBuffer<MinedProfile, MetaMinedProfileHeader>>>();

    static {
    	for (final byte dataType : DataConstants.ALL_DATA_TYPES) {
    		String maxFilesForTypeKey = "max_" + DataConstantsHelper.getDataTypeDescriptionForFiles(dataType) + "_profile_files";
    		int maxFilesForType = RE3Properties.getInt(maxFilesForTypeKey);
    		if (maxFilesForType <= 0) {
    			RapidEvolution3.isTerminated = true;
    			log.fatal("FileLimitingProfileIO(): no max files for type=" + maxFilesForTypeKey);
    		}
    		Map<Integer, FileActionBuffer<Profile, Integer>> typeProfileFileBuffers =
    				new HashMap<Integer, FileActionBuffer<Profile, Integer>>();
    		Map<Integer, FileActionBuffer<MinedProfile, MetaMinedProfileHeader>> typeMinedProfileFileBuffers =
    				new HashMap<Integer, FileActionBuffer<MinedProfile, MetaMinedProfileHeader>>();
    		for (int i = 0; i < maxFilesForType; ++i) {
    			String profileFilename = getFile(DataConstantsHelper.getDataTypeDescriptionForFiles(dataType), i);
    			typeProfileFileBuffers.put(i,
    					new FileActionBuffer<Profile, Integer>(profileFilename, new ActionApplicatorFactory<Profile, Integer>() {
							@Override
							public ActionApplicator<Profile, Integer> getNewInstance() {
								return new ProfileActionApplicator(dataType);
							}

    					}));
    			String minedProfileFilename = getMinedFile(dataType, i);
    			typeMinedProfileFileBuffers.put(i,
    					new FileActionBuffer<MinedProfile, MetaMinedProfileHeader>(minedProfileFilename, new ActionApplicatorFactory<MinedProfile, MetaMinedProfileHeader>() {
							@Override
							public ActionApplicator<MinedProfile, MetaMinedProfileHeader> getNewInstance() {
								return new MinedProfileActionApplicator(dataType);
							}
    					}));

    		}
    		profileFileBuffers.put(dataType, typeProfileFileBuffers);
    		minedProfileFileBuffers.put(dataType, typeMinedProfileFileBuffers);
    	}
    }

    // Stats kept for the duration of the program.
    static private final Map<Byte, Long> NUM_SAVE_PROFILE_CALLS = new HashMap<Byte, Long>();
    static private final Map<Byte, Long> NUM_READ_PROFILE_CALLS = new HashMap<Byte, Long>();
    static private final Map<Byte, Long> NUM_DELETE_PROFILE_CALLS = new HashMap<Byte, Long>();
    static private final Map<Byte, Set<Integer>> UNIQUE_PROFILES_SAVED = new HashMap<Byte, Set<Integer>>();
    static private final Map<Byte, Set<Integer>> UNIQUE_PROFILES_READ = new HashMap<Byte, Set<Integer>>();

    static private final Map<Byte, Map<Byte, Long>> NUM_SAVE_MINED_PROFILE_CALLS = new HashMap<Byte, Map<Byte, Long>>();
    static private final Map<Byte, Map<Byte, Long>> NUM_READ_MINED_PROFILE_CALLS = new HashMap<Byte, Map<Byte, Long>>();
    static private final Map<Byte, Map<Byte, Long>> NUM_DELETE_MINED_PROFILE_CALLS = new HashMap<Byte, Map<Byte, Long>>();

	@Override
	public boolean saveProfile(Profile profile) {
		boolean success = false;
		byte dataType = profile.getRecord().getDataType();
		int mappedId = getMappedValue(dataType, profile.getUniqueId());
		FileActionBuffer<Profile, Integer> buffer = profileFileBuffers.get(dataType).get(mappedId);
		try {
			buffer.getSem().startWrite("saveProfile");
			buffer.addOrUpdate(profile.getUniqueId(), profile);
			success = true;
		} catch (Exception e) {
			log.error("saveProfile(): error", e);
		} finally {
			buffer.getSem().endWrite();
			incrementProfileStat(NUM_SAVE_PROFILE_CALLS, profile.getRecord().getDataType());
			incrementUniqueProfileStat(UNIQUE_PROFILES_SAVED, profile.getRecord().getDataType(), profile.getUniqueId());
		}
		return success;
	}

	@Override
	public Profile getProfile(Identifier id, int profileId) {
		byte dataType = id.getType();
		int mappedId = getMappedValue(dataType, profileId);
		FileActionBuffer<Profile, Integer> buffer = profileFileBuffers.get(dataType).get(mappedId);
		Profile result = null;
		try {
			buffer.getSem().startRead("getProfile");
			// Check for un-applied writes.
			result = buffer.getPendingObject(profileId);
			if (result == null) {
				File file = new File(buffer.getFilename());
				if (file.exists()) {
					LineReader lineReader = LineReaderFactory.getLineReader(buffer.getFilename());
					if (lineReader != null) {
						int numProfiles = Integer.parseInt(lineReader.getNextLine());
						for (int i = 0; i < numProfiles; ++i) {
							Profile profile = getProfileOffLineReader(dataType, lineReader);
							if (profile.getUniqueId() == profileId) {
								result = profile;
								continue;
							}
						}
						lineReader.close();
						lineReader = null;
					}
				}
			}
		} catch (java.io.FileNotFoundException fne) {
			String error = fne.getMessage();
			if (error.indexOf("Too many open files") >= 0) {
				// getting "Too many open files"
				log.warn("getProfile(): too many open file handles, open files=");
				FileUtil.logOpenFileHandles();
				log.warn("getProfile(): shuttind down...");
				RapidEvolution3.isTerminated = true;
			}
		} catch (Exception e) {
			log.error("getProfile(): error getting id=" + id + ", profileId=" + profileId, e);
		} finally {
			buffer.getSem().endRead();
			incrementProfileStat(NUM_READ_PROFILE_CALLS, result != null ? result.getRecord().getDataType() : DataConstants.DATA_TYPE_UNKNOWN);
			incrementUniqueProfileStat(UNIQUE_PROFILES_READ,
					result != null ? result.getRecord().getDataType() : DataConstants.DATA_TYPE_UNKNOWN, profileId);
		}
		return result;
	}

	@Override
	public boolean deleteProfile(Record record) {
		return deleteProfileFile(record.getUniqueId(), record.getDataType(), record.getIdentifier().getTypeDescription());
	}

	@Override
	public boolean deleteProfileFile(int uniqueId, byte dataType, String typeDescription) {
		boolean success = false;
		int mappedId = getMappedValue(dataType, uniqueId);
		FileActionBuffer<Profile, Integer> buffer = profileFileBuffers.get(dataType).get(mappedId);
		try {
			buffer.getSem().startWrite("deleteProfile");
			buffer.delete(uniqueId);
			success = true;
		} catch (Exception e) {
			log.error("saveProfile(): error", e);
		} finally {
			buffer.getSem().endWrite();
			incrementProfileStat(NUM_DELETE_PROFILE_CALLS, dataType);
		}
		return success;
	}

	// MINED PROFILE STUFF:
	static private LRUCache minedProfileCache = new LRUCache(RE3Properties.getInt("mined_profile_cache_size"));

	@Override
	public boolean saveMinedProfile(MinedProfile profile, int uniqueId) {
		boolean success = false;
		byte dataType = profile.getHeader().getDataType();
		int mappedId = getMappedValue(dataType, uniqueId);
		FileActionBuffer<MinedProfile, MetaMinedProfileHeader> buffer = minedProfileFileBuffers.get(dataType).get(mappedId);
		try {
			buffer.getSem().startWrite("saveMinedProfile");
			MetaMinedProfileHeader header = new MetaMinedProfileHeader(dataType, profile.getHeader().getDataSource(), uniqueId);
			buffer.addOrUpdate(header, profile);
			success = true;
		} catch (Exception e) {
			log.error("saveMinedProfile(): error", e);
		} finally {
			buffer.getSem().endWrite();
			incrementMinedProfileStat(NUM_SAVE_MINED_PROFILE_CALLS, dataType, profile.getHeader().getDataSource());
		}
		return success;
	}
	@Override
	public MinedProfile getMinedProfile(byte dataType, byte dataSource, int uniqueId) {
		int mappedId = getMappedValue(dataType, uniqueId);
		FileActionBuffer<MinedProfile, MetaMinedProfileHeader> buffer = minedProfileFileBuffers.get(dataType).get(mappedId);
		MinedProfile result = null;
		try {
			buffer.getSem().startRead("getMinedProfile");
			MetaMinedProfileHeader header = new MetaMinedProfileHeader(dataType, dataSource, uniqueId);
			// Check for un-applied writes.
			result = buffer.getPendingObject(header);
			if (result == null) {
				File file = new File(buffer.getFilename());
				if (file.exists()) {
					Map<MetaMinedProfileHeader, MinedProfile> profileMap = new HashMap<MetaMinedProfileHeader, MinedProfile>();
					populateMinedProfileMap(profileMap, Serializer.readCompressedDataList(buffer.getFilename()));
					result = profileMap.get(header);
				}
			}
		} catch (Exception e) {
			log.error("getMinedProfile(): error getting dataType=" + dataType + ", dataSource=" + dataSource + ", uniqueId=" + uniqueId);
		} finally {
			buffer.getSem().endRead();
			incrementMinedProfileStat(NUM_READ_MINED_PROFILE_CALLS, dataType, dataSource);
		}
		return result;
	}
	@Override
	public boolean deleteMinedProfile(byte dataType, byte dataSource, int uniqueId) {
		boolean success = false;
		int mappedId = getMappedValue(dataType, uniqueId);
		FileActionBuffer<MinedProfile, MetaMinedProfileHeader> buffer = minedProfileFileBuffers.get(dataType).get(mappedId);
		try {
			buffer.getSem().startWrite("deleteMinedProfile");
			MetaMinedProfileHeader header = new MetaMinedProfileHeader(dataType, dataSource, uniqueId);
			buffer.delete(header);
			minedProfileCache.remove(header);
			success = true;
		} catch (Exception e) {
			log.error("deleteMinedProfile(): error", e);
		} finally {
			buffer.getSem().endWrite();
			incrementMinedProfileStat(NUM_DELETE_MINED_PROFILE_CALLS, dataType, dataSource);
		}
		return success;
	}

	@Override
	public void savePendingProfiles() {
		for (Entry<Byte, Map<Integer, FileActionBuffer<Profile, Integer>>> entry : profileFileBuffers.entrySet()) {
			// For each data type
			for (FileActionBuffer<Profile, Integer> buffer : entry.getValue().values()) {
				// Flush all related file buffers
				buffer.applyActions();
			}
		}
		for (Entry<Byte, Map<Integer, FileActionBuffer<MinedProfile, MetaMinedProfileHeader>>> entry : minedProfileFileBuffers.entrySet()) {
			// For each data type
			for (FileActionBuffer<MinedProfile, MetaMinedProfileHeader> buffer : entry.getValue().values()) {
				// Flush all related file buffers
				buffer.applyActions();
			}
		}
	}

	static public String getFile(String typeDescription, int fileId) {
		String directory = baseProfileDirectory + typeDescription + "/";
		File dir = new File(directory);
		dir.mkdirs();
		return directory + String.valueOf(fileId) + ".jso";
	}

	static public String getMinedFile(byte dataType, int uniqueId) {
		String directory = minedProfileBaseProfileDirectory + DataConstantsHelper.getDataTypeDescriptionForFiles(dataType) + "/";
		File dir = new File(directory);
		dir.mkdirs();
		return directory + String.valueOf(uniqueId) + ".cdl";
	}

	static private void incrementProfileStat(Map<Byte, Long> counterMap, byte dataType) {
		if (!RE3Properties.getBoolean("generate_profile_io_stats"))
			return;
		Long value = counterMap.get(dataType);
		if (value == null) {
			value = new Long(0);
		}
		++value;
		counterMap.put(dataType, value);
	}

	static private void incrementUniqueProfileStat(Map<Byte, Set<Integer>> counterMap, byte dataType, int id) {
		if (!RE3Properties.getBoolean("generate_profile_io_stats"))
			return;
		Set<Integer> set = counterMap.get(dataType);
		if (set == null) {
			set = new HashSet<Integer>();
		}
		set.add(id);
		counterMap.put(dataType, set);
	}

	static private void incrementMinedProfileStat(Map<Byte, Map<Byte, Long>> counterMap, byte dataType, byte dataSource) {
		if (!RE3Properties.getBoolean("generate_profile_io_stats"))
			return;
		Map<Byte, Long> typeMap = counterMap.get(dataType);
		if (typeMap == null) {
			typeMap = new HashMap<Byte, Long>();
			counterMap.put(dataType, typeMap);
		}
		Long value = typeMap.get(dataSource);
		if (value == null) {
			value = new Long(0);
		}
		++value;
		typeMap.put(dataSource, value);
	}

	static public String getStatsAsString() {
		StringBuffer result = new StringBuffer("== PROFILE STATS ==");
		for (byte dataType : DataConstants.ALL_DATA_TYPES) {
			if (NUM_READ_PROFILE_CALLS.containsKey(dataType)
					|| NUM_SAVE_PROFILE_CALLS.containsKey(dataType)
					|| NUM_DELETE_PROFILE_CALLS.containsKey(dataType)
					|| NUM_READ_MINED_PROFILE_CALLS.containsKey(dataType)
					|| NUM_SAVE_MINED_PROFILE_CALLS.containsKey(dataType)
					|| NUM_DELETE_MINED_PROFILE_CALLS.containsKey(dataType)) {
				result.append("\n  ");
				result.append(DataConstantsHelper.getDataTypeDescription(dataType).toUpperCase());
				result.append(":\n\tread calls: ");
				result.append(NUM_READ_PROFILE_CALLS.get(dataType));
				result.append(", unique=");
				result.append(UNIQUE_PROFILES_READ.containsKey(dataType) ? UNIQUE_PROFILES_READ.get(dataType).size() : 0);
				result.append("\n\twrite calls: ");
				result.append(NUM_SAVE_PROFILE_CALLS.get(dataType));
				result.append(", unique=");
				result.append(UNIQUE_PROFILES_SAVED.containsKey(dataType) ? UNIQUE_PROFILES_SAVED.get(dataType).size() : 0);
				result.append("\n\tdelete calls: ");
				result.append(NUM_DELETE_PROFILE_CALLS.get(dataType));
				if ((dataType != DataConstants.DATA_TYPE_STYLES)
						&& (dataType != DataConstants.DATA_TYPE_TAGS)
						&& (dataType != DataConstants.DATA_TYPE_PLAYLISTS)) {
					result.append("\n\tmined_read calls: ");
					result.append(getStatByDataSource(NUM_READ_MINED_PROFILE_CALLS.get(dataType)));
					result.append("\n\tmined_write calls: ");
					result.append(getStatByDataSource(NUM_SAVE_MINED_PROFILE_CALLS.get(dataType)));
					result.append("\n\tmined_delete calls: ");
					result.append(getStatByDataSource(NUM_DELETE_MINED_PROFILE_CALLS.get(dataType)));
				}
			}
		}

		result.append("\n  Pending actions collapsed: ");
		result.append(FileActionBuffer.PENDING_ACTIONS_COLLAPSED);
		result.append("\n  Pending actions added: ");
		result.append(FileActionBuffer.PENDING_ACTIONS_ADDED);
		result.append("\n  Percentage of actions collapsed: ");
		result.append(100.0f * FileActionBuffer.PENDING_ACTIONS_COLLAPSED / (FileActionBuffer.PENDING_ACTIONS_COLLAPSED + FileActionBuffer.PENDING_ACTIONS_ADDED));
		result.append("%");

		result.append("\n  New mutation count: ");
		result.append(FileActionBuffer.NEW_MUTATIONS_COUNT);
		result.append("\n  Batched mutation count: ");
		result.append(FileActionBuffer.BATCHED_MUTATIONS_COUNT);
		result.append("\n  Percentage of mutations batched: ");
		result.append(100.0f * FileActionBuffer.BATCHED_MUTATIONS_COUNT / (FileActionBuffer.BATCHED_MUTATIONS_COUNT + FileActionBuffer.NEW_MUTATIONS_COUNT));
		result.append("%");
		return result.toString();
	}

	static private String getStatByDataSource(Map<Byte, Long> minedStat) {
		StringBuffer result = new StringBuffer();
		if (minedStat != null) {
			for (Entry<Byte, Long> entry : minedStat.entrySet()) {
				if (result.length() > 0) {
					result.append(",");
				}
				result.append(DataConstantsHelper.getDataSourceDescription(entry.getKey()));
				result.append("=");
				result.append(entry.getValue());
			}
		}
		return result.toString();
	}

	static public int getMappedValue(byte dataType, int uniqueId) {
		int max = profileFileBuffers.get(dataType).size();
		return uniqueId % max;
	}

	static private Profile getProfileOffLineReader(byte dataType, LineReader lineReader) {
		Profile result;
		if (dataType == DataConstants.DATA_TYPE_ARTISTS)
			result = new ArtistProfile(lineReader);
		else if (dataType == DataConstants.DATA_TYPE_LABELS)
			result = new LabelProfile(lineReader);
		else if (dataType == DataConstants.DATA_TYPE_RELEASES)
			result = new ReleaseProfile(lineReader);
		else if (dataType == DataConstants.DATA_TYPE_SONGS)
			result = new SongProfile(lineReader);
		else if (dataType == DataConstants.DATA_TYPE_MIXOUTS)
			result = new MixoutProfile(lineReader);
		else if (dataType == DataConstants.DATA_TYPE_TAGS)
			result = new TagProfile(lineReader);
		else if (dataType == DataConstants.DATA_TYPE_STYLES)
			result = new StyleProfile(lineReader);
		else if (dataType == DataConstants.DATA_TYPE_PLAYLISTS)
			result = PlaylistProfile.readPlaylistProfile(lineReader);
		else throw new RuntimeException("Unknown dataType=" + dataType);
		return result;
	}

	static public void populateProfileMap(Map<Integer, Profile> map, String filename, byte dataType) throws FileNotFoundException {
		LineReader lineReader = LineReaderFactory.getLineReader(filename);
		if (lineReader != null) {
			int numProfiles = Integer.parseInt(lineReader.getNextLine());
			for (int i = 0; i < numProfiles; ++i) {
				Profile existingProfile = getProfileOffLineReader(dataType, lineReader);
				map.put(existingProfile.getUniqueId(), existingProfile);
			}
			lineReader.close();
			lineReader = null;
		}
	}

	@Override
	public Vector<Profile> readProfilesFromFile(String filename, byte dataType) {
		Vector<Profile> result = new Vector<Profile>();
		Map<Integer, Profile> map = new HashMap<Integer, Profile>();
		try {
			populateProfileMap(map, filename, dataType);
			result.addAll(map.values());
		} catch (FileNotFoundException e) { }
		return result;
	}

	static private void populateMinedProfileMap(Map<MetaMinedProfileHeader, MinedProfile> map, List<Object> objects) {
		for (int i = 0; i < objects.size(); i += 2) {
			MetaMinedProfileHeader header = (MetaMinedProfileHeader) objects.get(i);
			if (objects.get(i + 1) instanceof MinedProfile) {
				MinedProfile profile = (MinedProfile) objects.get(i + 1);
				minedProfileCache.add(header, profile);
				map.put(header, profile);
			}
		}
	}

	static public void writeProfilesToFile(Collection<Profile> profiles, String filename) {
		LineWriter writer = LineWriterFactory.getLineWriter(filename);
		writer.writeLine(profiles.size());
		for (Profile profileToWrite : profiles) {
			profileToWrite.write(writer);
		}
		writer.close();
	}

	static private boolean writeMinedProfilesToFile(Map<MetaMinedProfileHeader, MinedProfile> existingProfileMap, String filename) {
		List<Object> serialized = new ArrayList<Object>();
		for (Entry<MetaMinedProfileHeader, MinedProfile> entry : existingProfileMap.entrySet()) {
			serialized.add(entry.getKey());
			serialized.add(entry.getValue());
		}
		return Serializer.saveCompressedDataList(serialized, filename);
	}

	// Includes the ID of the profile into the key, since multiple IDs will map to the same files...
	static private class MetaMinedProfileHeader extends MinedProfileHeader {
	    static private final long serialVersionUID = 0L;

	    final int id;

	    public MetaMinedProfileHeader(byte dataType, byte dataSource, int id) {
			super(dataType, dataSource);
			this.id = id;
		}

	    @Override
		public boolean equals(Object o) {
			if (o instanceof MetaMinedProfileHeader) {
				MetaMinedProfileHeader m = (MetaMinedProfileHeader)o;
				if ((dataType == m.dataType)
						&& (dataSource == m.dataSource)
						&& (id == m.id)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public int hashCode() {
			return (dataType << 16) + (dataSource << 8) + id;
		}
	}

    static private class ProfileActionApplicator implements ActionApplicator<Profile, Integer> {
    	final byte type;
    	public ProfileActionApplicator(byte type) {
    		this.type = type;
    	}
		@Override
		public Map<Integer, Profile> load(String filename) {
			Map<Integer, Profile> result = new HashMap<Integer, Profile>();
			try {
				populateProfileMap(result, filename, type);
			} catch (java.io.FileNotFoundException fne) {
				String error = fne.getMessage();
				if (error.indexOf("Too many open files") >= 0) {
					// getting "Too many open files"
					log.warn("getProfile(): too many open file handles, open files=");
					FileUtil.logOpenFileHandles();
					log.warn("getProfile(): shuttind down...");
					RapidEvolution3.isTerminated = true;
				}
			}
			return result;
		}
		@Override
		public boolean save(String filename, Map<Integer, Profile> data) {
			boolean success = false;
			File existingFile = new File(filename);
			File tempFile = null;
			try {
				if (existingFile.exists() && SAFE_SAVE) {
					String tempDirectory = baseProfileDirectory + "temp/" + DataConstantsHelper.getDataTypeDescriptionForFiles(type) + "/";
					File tempDir = new File(tempDirectory);
					tempDir.mkdirs();
					String temp_filename = tempDirectory + existingFile.getName();
					tempFile = new File(temp_filename);
					existingFile.renameTo(tempFile);
				}
				writeProfilesToFile(data.values(), filename);
				if (tempFile != null) {
					int tries = 0;
					while (!tempFile.delete() && tempFile.exists() && !RapidEvolution3.isTerminated && (tries < FILE_DELETE_MAX_TRIES)) {
						if (log.isDebugEnabled())
							log.debug("saveProfile(): could not delete tempFile=" + tempFile + ", waiting...");
						try {
							Thread.sleep(FileLimitingProfileIO.FILE_DELETE_WAIT_INTERVAL);
						} catch (InterruptedException e) { }
						++tries;
					}
					tempFile = null;
				}
				success = true;
			} finally {
				if ((tempFile != null) && tempFile.exists() && !success) {
					int tries = 0;
					while (!existingFile.delete() && existingFile.exists() && !RapidEvolution3.isTerminated && (tries < FILE_DELETE_MAX_TRIES)) {
						if (log.isDebugEnabled())
							log.debug("saveProfile(): could not delete existingFile=" + existingFile + ", waiting...");
						try { Thread.sleep(FILE_DELETE_WAIT_INTERVAL); } catch (InterruptedException e) { }
						++tries;
					}
					tempFile.renameTo(existingFile);
				}
			}
			return success;
		}
    }

    static private class MinedProfileActionApplicator implements ActionApplicator<MinedProfile, MetaMinedProfileHeader> {
    	final byte type;
    	public MinedProfileActionApplicator(byte type) {
    		this.type = type;
    	}
		@Override
		public Map<MetaMinedProfileHeader, MinedProfile> load(String filename) {
			Map<MetaMinedProfileHeader, MinedProfile> result = new HashMap<MetaMinedProfileHeader, MinedProfile>();
			File file = new File(filename);
			if (file.exists()) {
				populateMinedProfileMap(result, Serializer.readCompressedDataList(filename));
			}
			return result;
		}
		@Override
		public boolean save(String filename, Map<MetaMinedProfileHeader, MinedProfile> data) {
			boolean success = false;
			File existingFile = new File(filename);
			File tempFile = null;
			try {
				if (existingFile.exists() && SAFE_SAVE) {
					String tempDirectory = baseProfileDirectory + "temp/" + DataConstantsHelper.getDataTypeDescriptionForFiles(type) + "/";
					File tempDir = new File(tempDirectory);
					tempDir.mkdirs();
					String temp_filename = tempDirectory + existingFile.getName();
					tempFile = new File(temp_filename);
					existingFile.renameTo(tempFile);
				}
				success = writeMinedProfilesToFile(data, filename);
				if (tempFile != null) {
					int tries = 0;
					while (!tempFile.delete() && tempFile.exists() && !RapidEvolution3.isTerminated && (tries < FILE_DELETE_MAX_TRIES)) {
						if (log.isDebugEnabled())
							log.debug("saveProfile(): could not delete tempFile=" + tempFile + ", waiting...");
						try {
							Thread.sleep(FileLimitingProfileIO.FILE_DELETE_WAIT_INTERVAL);
						} catch (InterruptedException e) { }
						++tries;
					}
					tempFile = null;
				}
			} finally {
				if ((tempFile != null) && tempFile.exists() && !success) {
					int tries = 0;
					while (!existingFile.delete() && existingFile.exists() && !RapidEvolution3.isTerminated && (tries < FILE_DELETE_MAX_TRIES)) {
						if (log.isDebugEnabled())
							log.debug("saveProfile(): could not delete existingFile=" + existingFile + ", waiting...");
						try { Thread.sleep(FILE_DELETE_WAIT_INTERVAL); } catch (InterruptedException e) { }
						++tries;
					}
					tempFile.renameTo(existingFile);
				}
			}
			return success;
		}
    }
}
