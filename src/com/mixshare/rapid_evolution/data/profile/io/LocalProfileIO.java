package com.mixshare.rapid_evolution.data.profile.io;

import java.io.File;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.DataConstantsHelper;
import com.mixshare.rapid_evolution.data.identifier.Identifier;
import com.mixshare.rapid_evolution.data.mined.MinedProfile;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.util.io.Serializer;
import com.mixshare.rapid_evolution.data.util.io.XMLSerializer;
import com.mixshare.rapid_evolution.util.OSHelper;
import com.mixshare.rapid_evolution.util.timing.Semaphore;

/**
 * This class manages the persistence of profiles into the file system, and their retrieval (CRUD operations)...
 */
public class LocalProfileIO implements ProfileIOInterface {

    static private Logger log = Logger.getLogger(LocalProfileIO.class);

    // NOTE: safe saving is a simple mechanism to prevent corruption of profiles if the program abnormally terminates while writing to
    // the hard disk.  if the profile is being overwritten, it is first moved to a temporary directory, then the new profile is written.
    // the first profile is then cleared from the temporary directory upon success.  if the program terminates during this process,
    // the application will detect the existence of the profile in the temp directory upon startup and restore it from backup.
    // this variable should always be true, it is stored as a variable so it can be temporarily turned off during restoration
    static public boolean SAFE_SAVE = true;
    static private int PROFILE_SAVE_MAX_RETRIES = 5;
    static private long PROFILE_SAVE_FAIL_WAIT_RETRY_MILLIS = 5000;

    static private long FILE_DELETE_WAIT_INTERVAL = 1000;

    static private String baseProfileDirectory = OSHelper.getWorkingDirectory() + "/profiles/";
    static private String minedProfileBaseProfileDirectory = OSHelper.getWorkingDirectory() + "/mined_profiles/";
    static private Semaphore saveSem = new Semaphore(1);

	@Override
	public Vector<Profile> readProfilesFromFile(String filename, byte dataType) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean deleteProfileFile(int uniqueId, byte dataType, String typeDescription) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void savePendingProfiles() { }

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
						String tempDirectory = OSHelper.getWorkingDirectory() + "/profiles/temp/";
						File tempDir = new File(tempDirectory);
						tempDir.mkdirs();
						String temp_filename = tempDirectory + existingFile.getName();
						tempFile = new File(temp_filename);
						existingFile.renameTo(tempFile);
					}
				}
				success = XMLSerializer.saveData(profile, filename);
				int tries = 0;
				while (!success && (tries < PROFILE_SAVE_MAX_RETRIES)) {
					++tries;
					Thread.sleep(PROFILE_SAVE_FAIL_WAIT_RETRY_MILLIS);
					success = XMLSerializer.saveData(profile, filename);
				}
				if (tempFile != null) {
					if (success) {
						while (!tempFile.delete() && tempFile.exists() && !RapidEvolution3.isTerminated) {
							if (log.isDebugEnabled())
								log.debug("saveProfile(): could not delete tempFile=" + tempFile + ", waiting...");
							Thread.sleep(FILE_DELETE_WAIT_INTERVAL);
						}
					} else {
						while (!existingFile.delete() && existingFile.exists() && !RapidEvolution3.isTerminated) {
							if (log.isDebugEnabled())
								log.debug("saveProfile(): could not delete existingFile=" + existingFile + ", waiting...");
							Thread.sleep(FILE_DELETE_WAIT_INTERVAL);
						}
						tempFile.renameTo(existingFile);
					}
				}
			}
		} catch (Exception e) {
			log.error("saveProfile(): error", e);
		} finally {
			saveSem.release();
		}
		return success;
	}

	@Override
	public Profile getProfile(Identifier id, int profileId) {
		Profile result = null;
		try {
			String filename = getFile(id, profileId);
			if (filename != null)
				result = (Profile)XMLSerializer.readData(filename);
		} catch (Exception e) {
			log.error("getProfile(): error getting id=" + id + ", profileId=" + profileId, e);
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
				while (!deleteFile.delete() && deleteFile.exists() && !RapidEvolution3.isTerminated) {
					if (log.isDebugEnabled())
						log.debug("saveProfile(): could not delete deleteFile=" + deleteFile + ", waiting...");
					Thread.sleep(FILE_DELETE_WAIT_INTERVAL);
				}
				success = true;
			}
		} catch (Exception e) {
			log.error("deleteProfile(): error", e);
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
		}
		return false;
	}
	@Override
	public MinedProfile getMinedProfile(byte dataType, byte dataSource, int uniqueId) {
		try {
			String filename = getMinedFile(dataType, dataSource, uniqueId);
			if (filename != null)
				return (MinedProfile)Serializer.readCompressedData(filename);
		} catch (Exception e) {
			log.error("getMinedProfile(): error", e);
		}
		return null;
	}
	@Override
	public boolean deleteMinedProfile(byte dataType, byte dataSource, int uniqueId) {
		boolean success = false;
		try {
			String filename = getMinedFile(dataType, dataSource, uniqueId);
			if (filename != null) {
				File deleteFile = new File(filename);
				while (!deleteFile.delete() && deleteFile.exists() && !RapidEvolution3.isTerminated) {
					if (log.isDebugEnabled())
						log.debug("saveProfile(): could not delete deleteFile=" + deleteFile + ", waiting...");
					Thread.sleep(FILE_DELETE_WAIT_INTERVAL);
				}
				success = true;
			}
		} catch (Exception e) {
			log.error("deleteMinedProfile(): error", e);
		}
		return success;
	}
	static private String getFile(Identifier id, int fileId) {
		String directory = baseProfileDirectory + id.getTypeDescription() + "/";
		File dir = new File(directory);
		dir.mkdirs();
		return directory + String.valueOf(fileId) + ".xml";
	}

	static private String getMinedFile(byte dataType, byte dataSource, int uniqueId) {
		String directory = minedProfileBaseProfileDirectory + DataConstantsHelper.getDataTypeDescription(dataType).toLowerCase() + "/";
		File dir = new File(directory);
		dir.mkdirs();
		return directory + String.valueOf(uniqueId) + "." + DataConstantsHelper.getDataSourceDescription(dataSource).toLowerCase();
	}


}
