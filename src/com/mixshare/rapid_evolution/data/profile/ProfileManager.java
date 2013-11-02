package com.mixshare.rapid_evolution.data.profile;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.identifier.Identifier;
import com.mixshare.rapid_evolution.data.mined.MinedProfile;
import com.mixshare.rapid_evolution.data.profile.io.ProfileIOInterface;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.util.cache.LRUCache;

/**
 * This class abstracts profile persistence and their retrieval (CRUD operations)...
 * It also implements a layer of caching over the underlying implementation.
 */
public class ProfileManager {

    static private Logger log = Logger.getLogger(ProfileManager.class);

    static private Map<String, LRUCache> profileCaches = new HashMap<String, LRUCache>();
    static private ProfileIOInterface profileIO;

    static public boolean stopWrites = false;

    static {
		String className = RE3Properties.getProperty("profile_manager_io_implementation");
    	try {
    		profileIO = (ProfileIOInterface)Class.forName(className).newInstance();
    	} catch (Exception e) {
    		log.fatal("ProfileManager(): couldn't load profile IO implementation=" + className);
    	}
    }

    static public ProfileIOInterface getProfileIO() { return profileIO; }

    static public Profile getProfile(Identifier id, int profileId) {
    	Profile result = null;
    	if (id == null)
    		return null;
    	try {
    		LRUCache cache = getCache(id.getTypeDescription());
    		result = (Profile)cache.get(new Integer(profileId));
    		if (result == null) {
    			result = profileIO.getProfile(id, profileId);
    			if (result != null)
    				cache.add(new Integer(profileId), result);
    		}
    	} catch (Exception e) {
    		log.error("getProfile(): error reading profile=" + id + ", profileId=" + profileId, e);
    	}
    	return result;
    }

    static public MinedProfile getMinedProfile(byte dataType, byte dataSource, int uniqueId) {
    	return profileIO.getMinedProfile(dataType, dataSource, uniqueId);
    }

    static public Profile testProfileRead(Identifier id, int profileId) {
    	return profileIO.getProfile(id, profileId);
    }

    static public boolean deleteProfile(Record record) {
		if (stopWrites)
			return false;
    	boolean success = profileIO.deleteProfile(record);
    	LRUCache cache = getCache(record.getIdentifier().getTypeDescription());
    	cache.remove(record.getUniqueId());
    	return success;
    }

    static public boolean deleteMinedProfile(byte dataType, byte dataSource, int uniqueId) {
		if (stopWrites)
			return false;
    	boolean success = profileIO.deleteMinedProfile(dataType, dataSource, uniqueId);
    	return success;
    }

    static public boolean saveMinedProfile(MinedProfile minedProfile, int id) {
		if (stopWrites)
			return false;
    	boolean success = profileIO.saveMinedProfile(minedProfile, id);
    	return success;
    }

	static public boolean saveProfile(Profile profile) {
		if (stopWrites)
			return false;
		boolean success = profileIO.saveProfile(profile);
		if (profile instanceof SearchProfile)
			for (MinedProfile minedProfile : ((SearchProfile) profile).getAllMinedProfiles().keySet())
				profileIO.saveMinedProfile(minedProfile, profile.getUniqueId());
		profileIO.saveProfile(profile);
		if (success) {
			LRUCache cache = getCache(profile.getIdentifier().getTypeDescription());
			cache.add(new Integer(profile.getUniqueId()), profile);
		} else {
			log.error("saveProfile(): failed to save profile=" + profile);
		}
		return success;
	}

	static public Vector<Profile> readProfilesFromFile(String filename, byte type) {
		return profileIO.readProfilesFromFile(filename, type);
	}

	static public boolean deleteProfileFile(int uniqueId, byte dataType, String typeDescription) {
		return profileIO.deleteProfileFile(uniqueId, dataType, typeDescription);
	}

	static private LRUCache getCache(String type) {
		LRUCache cache = profileCaches.get(type);
		if (cache == null) {
			cache = new LRUCache(RE3Properties.getInt("profile_cache_size"));
			profileCaches.put(type, cache);
		}
		return cache;
	}

	static public void savePendingProfiles() {
		profileIO.savePendingProfiles();
	}

}