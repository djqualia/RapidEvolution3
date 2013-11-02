package com.mixshare.rapid_evolution.data.profile.io;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.DataConstantsHelper;
import com.mixshare.rapid_evolution.data.identifier.Identifier;
import com.mixshare.rapid_evolution.data.identifier.filter.playlist.PlaylistIdentifier;
import com.mixshare.rapid_evolution.data.identifier.filter.style.StyleIdentifier;
import com.mixshare.rapid_evolution.data.identifier.filter.tag.TagIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.artist.ArtistIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.label.LabelIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.release.ReleaseIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.song.MixoutIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.song.SongIdentifier;
import com.mixshare.rapid_evolution.data.mined.MinedProfile;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.profile.filter.playlist.PlaylistProfile;
import com.mixshare.rapid_evolution.data.profile.filter.style.StyleProfile;
import com.mixshare.rapid_evolution.data.profile.filter.tag.TagProfile;
import com.mixshare.rapid_evolution.data.profile.search.artist.ArtistProfile;
import com.mixshare.rapid_evolution.data.profile.search.label.LabelProfile;
import com.mixshare.rapid_evolution.data.profile.search.release.ReleaseProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.MixoutProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.util.io.Serializer;
import com.mixshare.rapid_evolution.util.FileUtil;
import com.mixshare.rapid_evolution.util.OSHelper;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineReaderFactory;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.mixshare.rapid_evolution.util.io.LineWriterFactory;
import com.mixshare.rapid_evolution.util.timing.Semaphore;

/**
 * This class manages the persistence of profiles into the file system, and their retrieval (CRUD operations)...
 */
public class CustomLocalProfileIO implements ProfileIOInterface {

    static private Logger log = Logger.getLogger(CustomLocalProfileIO.class);

    // NOTE: safe saving is a simple mechanism to prevent corruption of profiles if the program abnormally terminates while writing to
    // the hard disk.  if the profile is being overwritten, it is first moved to a temporary directory, then the new profile is written.
    // the first profile is then cleared from the temporary directory upon success.  if the program terminates during this process,
    // the application will detect the existence of the profile in the temp directory upon startup and restore it from backup.
    // this variable should always be true, it is stored as a variable so it can be temporarily turned off during restoration
    static public boolean SAFE_SAVE = true;
    static private int PROFILE_SAVE_MAX_RETRIES = 5;
    static private long PROFILE_SAVE_FAIL_WAIT_RETRY_MILLIS = 5000;

    static private long FILE_DELETE_WAIT_INTERVAL = 1000;
    static private int FILE_DELETE_MAX_TRIES = 3;

    static private String baseProfileDirectory = OSHelper.getWorkingDirectory() + "/profiles/";
    static private String minedProfileBaseProfileDirectory = OSHelper.getWorkingDirectory() + "/mined_profiles/";
    static private Semaphore saveSem = new Semaphore(1);

    // Stats kept for the duration of the program.
    static private final Map<Byte, Long> NUM_PROFILES_SAVED = new HashMap<Byte, Long>();
    static private final Map<Byte, Long> NUM_PROFILES_READ = new HashMap<Byte, Long>();
    static private final Map<Byte, Long> NUM_PROFILES_DELETED = new HashMap<Byte, Long>();
    static private final Map<Byte, Set<Integer>> UNIQUE_PROFILES_SAVED = new HashMap<Byte, Set<Integer>>();
    static private final Map<Byte, Set<Integer>> UNIQUE_PROFILES_READ = new HashMap<Byte, Set<Integer>>();

    static private final Map<Byte, Map<Byte, Long>> NUM_MINED_PROFILES_SAVED = new HashMap<Byte, Map<Byte, Long>>();
    static private final Map<Byte, Map<Byte, Long>> NUM_MINED_PROFILES_READ = new HashMap<Byte, Map<Byte, Long>>();
    static private final Map<Byte, Map<Byte, Long>> NUM_MINED_PROFILES_DELETED = new HashMap<Byte, Map<Byte, Long>>();

	@Override
	public boolean saveProfile(Profile profile) {
		boolean success = false;
		try {
			saveSem.acquire("saveProfile"); // if this semaphore is removed, the temp directory needs to be divided by type, otherwise IDs can collide
			String filename = getFile(profile.getIdentifier(), profile.getUniqueId());
			if (filename != null) {
				File existingFile = new File(filename);
				File tempFile = null;
				if (SAFE_SAVE) {
					if (existingFile.exists()) {
						String tempDirectory = OSHelper.getWorkingDirectory() + "/profiles/temp/" + profile.getIdentifier().getTypeDescription() + "/";
						File tempDir = new File(tempDirectory);
						tempDir.mkdirs();
						String temp_filename = tempDirectory + existingFile.getName();
						tempFile = new File(temp_filename);
						existingFile.renameTo(tempFile);
					}
				}
				LineWriter writer = LineWriterFactory.getLineWriter(filename);
				profile.write(writer);
				writer.close();
				success = true;
				int tries = 0;
				while (!success && (tries < PROFILE_SAVE_MAX_RETRIES)) {
					++tries;
					Thread.sleep(PROFILE_SAVE_FAIL_WAIT_RETRY_MILLIS);
					writer = LineWriterFactory.getLineWriter(filename);
					profile.write(writer);
					writer.close();
					success = true;
				}
				if (tempFile != null) {
					if (success) {
						tries = 0;
						while (!tempFile.delete() && tempFile.exists() && !RapidEvolution3.isTerminated && (tries < FILE_DELETE_MAX_TRIES)) {
							if (log.isDebugEnabled())
								log.debug("saveProfile(): could not delete tempFile=" + tempFile + ", waiting...");
							Thread.sleep(FILE_DELETE_WAIT_INTERVAL);
							++tries;
						}
					} else {
						tries = 0;
						while (!existingFile.delete() && existingFile.exists() && !RapidEvolution3.isTerminated && (tries < FILE_DELETE_MAX_TRIES)) {
							if (log.isDebugEnabled())
								log.debug("saveProfile(): could not delete existingFile=" + existingFile + ", waiting...");
							Thread.sleep(FILE_DELETE_WAIT_INTERVAL);
							++tries;
						}
						tempFile.renameTo(existingFile);
					}
				}
			}
		} catch (Exception e) {
			log.error("saveProfile(): error", e);
		} finally {
			saveSem.release();
			incrementProfileStat(NUM_PROFILES_SAVED, profile.getRecord().getDataType());
			incrementUniqueProfileStat(UNIQUE_PROFILES_SAVED, profile.getRecord().getDataType(), profile.getUniqueId());
		}
		return success;
	}

	@Override
	public Vector<Profile> readProfilesFromFile(String filename, byte dataType) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean deleteProfileFile(int uniqueId, byte dataType, String typeDescription) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Profile getProfile(Identifier id, int profileId) {
		Profile result = null;
		try {
			String filename = getFile(id, profileId);
			if (filename != null) {
				File file = new File(filename);
				if (file.exists()) {
					LineReader lineReader = LineReaderFactory.getLineReader(filename);
					if (lineReader != null) {
						if (id instanceof ArtistIdentifier)
							result = new ArtistProfile(lineReader);
						else if (id instanceof LabelIdentifier)
							result = new LabelProfile(lineReader);
						else if (id instanceof ReleaseIdentifier)
							result = new ReleaseProfile(lineReader);
						else if (id instanceof SongIdentifier)
							result = new SongProfile(lineReader);
						else if (id instanceof MixoutIdentifier)
							result = new MixoutProfile(lineReader);
						else if (id instanceof TagIdentifier)
							result = new TagProfile(lineReader);
						else if (id instanceof StyleIdentifier)
							result = new StyleProfile(lineReader);
						else if (id instanceof PlaylistIdentifier)
							result = PlaylistProfile.readPlaylistProfile(lineReader);
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
			incrementProfileStat(NUM_PROFILES_READ, result != null ? result.getRecord().getDataType() : DataConstants.DATA_TYPE_UNKNOWN);
			incrementUniqueProfileStat(UNIQUE_PROFILES_READ,
					result != null ? result.getRecord().getDataType() : DataConstants.DATA_TYPE_UNKNOWN, profileId);
		}
		return result;
	}

	@Override
	public boolean deleteProfile(Record record) {
		boolean success = false;
		try {
			String filename = getFile(record.getIdentifier(), record.getUniqueId());
			if (filename != null) {
				File deleteFile = new File(filename);
				int tries = 0;
				while (!deleteFile.delete() && deleteFile.exists() && !RapidEvolution3.isTerminated && (tries < FILE_DELETE_MAX_TRIES)) {
					if (log.isDebugEnabled())
						log.debug("deleteProfile(): could not delete deleteFile=" + deleteFile + ", waiting...");
					Thread.sleep(FILE_DELETE_WAIT_INTERVAL);
					++tries;
				}
				success = true;
			}
		} catch (Exception e) {
			log.error("deleteProfile(): error", e);
		} finally {
			incrementProfileStat(NUM_PROFILES_DELETED, record.getDataType());
		}
		return success;
	}

	@Override
	public boolean saveMinedProfile(MinedProfile profile, int uniqueId) {
		try {
			String filename = getMinedFile(profile.getHeader().getDataType(), profile.getHeader().getDataSource(), uniqueId);
			if (filename != null)
				return Serializer.saveCompressedData(profile, filename);
		} catch (Exception e) {
			log.error("saveMinedProfile(): error", e);
		} finally {
			incrementMinedProfileStat(NUM_MINED_PROFILES_SAVED, profile.getHeader().getDataType(), profile.getHeader().getDataSource());
		}
		return false;
	}
	@Override
	public MinedProfile getMinedProfile(byte dataType, byte dataSource, int uniqueId) {
		MinedProfile result = null;
		try {
			String filename = getMinedFile(dataType, dataSource, uniqueId);
			if (filename != null)
				result = (MinedProfile)Serializer.readCompressedData(filename);
		} catch (Exception e) {
			log.error("getMinedProfile(): error", e);
		} finally {
			incrementMinedProfileStat(NUM_MINED_PROFILES_READ, dataType, dataSource);
		}
		return result;
	}
	@Override
	public boolean deleteMinedProfile(byte dataType, byte dataSource, int uniqueId) {
		boolean success = false;
		try {
			String filename = getMinedFile(dataType, dataSource, uniqueId);
			if (filename != null) {
				File deleteFile = new File(filename);
				int tries = 0;
				while (!deleteFile.delete() && deleteFile.exists() && !RapidEvolution3.isTerminated && (tries < FILE_DELETE_MAX_TRIES)) {
					if (log.isDebugEnabled())
						log.debug("deleteMinedProfile(): could not delete deleteFile=" + deleteFile + ", waiting...");
					Thread.sleep(FILE_DELETE_WAIT_INTERVAL);
					++tries;
				}
				success = true;
			}
		} catch (Exception e) {
			log.error("deleteMinedProfile(): error", e);
		} finally {
			incrementMinedProfileStat(NUM_MINED_PROFILES_DELETED, dataType, dataSource);
		}
		return success;
	}

	@Override
	public void savePendingProfiles() { }

	static public String getFile(Identifier id, int fileId) {
		String directory = baseProfileDirectory + id.getTypeDescription() + "/";
		File dir = new File(directory);
		dir.mkdirs();
		return directory + String.valueOf(fileId) + ".jso";
	}

	static public String getMinedFile(byte dataType, byte dataSource, int uniqueId) {
		String directory = minedProfileBaseProfileDirectory + DataConstantsHelper.getDataTypeDescription(dataType).toLowerCase() + "/";
		File dir = new File(directory);
		dir.mkdirs();
		return directory + String.valueOf(uniqueId) + "." + DataConstantsHelper.getDataSourceDescription(dataSource).toLowerCase();
	}

	static public Object readObject(Class<?> dataClass, String filename) {
		return readObject(dataClass, filename, false);
	}
	static public Object readObject(Class<?> dataClass, String filename, boolean useSeparateThread) {
		Object result = null;
		try {
			Constructor<?> constructor = dataClass.getConstructor(LineReader.class);
			if (constructor != null) {
				File file = new File(filename);
				if (file.exists()) {
					LineReader lineReader = LineReaderFactory.getLineReader(filename, useSeparateThread);
					if (lineReader != null) {
						try {
							result = constructor.newInstance(lineReader);
						} catch (Exception e) {
							log.error("readObject(): error", e);
						}
						lineReader.close();
					}
				}
			} else {
				log.warn("readObject(): no suitable constructor for class=" + dataClass);
			}
		} catch (Exception e) {
			log.error("readObject(): error", e);
		}
		return result;
	}

	static private void incrementProfileStat(Map<Byte, Long> counterMap, byte dataType) {
		Long value = counterMap.get(dataType);
		if (value == null) {
			value = new Long(0);
		}
		++value;
		counterMap.put(dataType, value);
	}

	static private void incrementUniqueProfileStat(Map<Byte, Set<Integer>> counterMap, byte dataType, int id) {
		Set<Integer> set = counterMap.get(dataType);
		if (set == null) {
			set = new HashSet<Integer>();
		}
		set.add(id);
		counterMap.put(dataType, set);
	}

	static private void incrementMinedProfileStat(Map<Byte, Map<Byte, Long>> counterMap, byte dataType, byte dataSource) {
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
			if (NUM_PROFILES_READ.containsKey(dataType)
					|| NUM_PROFILES_SAVED.containsKey(dataType)
					|| NUM_PROFILES_DELETED.containsKey(dataType)
					|| NUM_MINED_PROFILES_READ.containsKey(dataType)
					|| NUM_MINED_PROFILES_SAVED.containsKey(dataType)
					|| NUM_MINED_PROFILES_DELETED.containsKey(dataType)) {
				result.append("\n  ");
				result.append(DataConstantsHelper.getDataTypeDescription(dataType).toUpperCase());
				result.append(":\n\tread: ");
				result.append(NUM_PROFILES_READ.get(dataType));
				result.append(", unique=");
				result.append(UNIQUE_PROFILES_READ.containsKey(dataType) ? UNIQUE_PROFILES_READ.get(dataType).size() : 0);
				result.append("\n\twritten: ");
				result.append(NUM_PROFILES_SAVED.get(dataType));
				result.append(", unique=");
				result.append(UNIQUE_PROFILES_SAVED.containsKey(dataType) ? UNIQUE_PROFILES_SAVED.get(dataType).size() : 0);
				result.append("\n\tdeleted: ");
				result.append(NUM_PROFILES_DELETED.get(dataType));
				if ((dataType != DataConstants.DATA_TYPE_STYLES)
						&& (dataType != DataConstants.DATA_TYPE_TAGS)
						&& (dataType != DataConstants.DATA_TYPE_PLAYLISTS)) {
					result.append("\n\tmined_read: ");
					result.append(getStatByDataSource(NUM_MINED_PROFILES_READ.get(dataType)));
					result.append("\n\tmined_written: ");
					result.append(getStatByDataSource(NUM_MINED_PROFILES_SAVED.get(dataType)));
					result.append("\n\tmined_deleted: ");
					result.append(getStatByDataSource(NUM_MINED_PROFILES_DELETED.get(dataType)));
				}
			}
		}
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
}
