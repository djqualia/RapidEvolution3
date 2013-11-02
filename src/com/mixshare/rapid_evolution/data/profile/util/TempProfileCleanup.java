package com.mixshare.rapid_evolution.data.profile.util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.profile.ProfileManager;
import com.mixshare.rapid_evolution.data.profile.filter.playlist.PlaylistProfile;
import com.mixshare.rapid_evolution.data.profile.filter.style.StyleProfile;
import com.mixshare.rapid_evolution.data.profile.filter.tag.TagProfile;
import com.mixshare.rapid_evolution.data.profile.io.CustomLocalProfileIO;
import com.mixshare.rapid_evolution.data.profile.io.FileLimitingProfileIO;
import com.mixshare.rapid_evolution.data.profile.io.LocalProfileIO;
import com.mixshare.rapid_evolution.data.profile.search.artist.ArtistProfile;
import com.mixshare.rapid_evolution.data.profile.search.label.LabelProfile;
import com.mixshare.rapid_evolution.data.profile.search.release.ReleaseProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.MixoutProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.util.io.XMLSerializer;
import com.mixshare.rapid_evolution.util.FileUtil;
import com.mixshare.rapid_evolution.util.OSHelper;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineReaderFactory;

/**
 * This class is used as part of the safe saving mechanism utilized in the ProfileManager.
 * A method is provided which will look for any profiles left behind in the temp directory (which is
 * due to abnormal termination), and attempt to restore them to there original locations.
 */
public class TempProfileCleanup implements DataConstants {

	static private Logger log = Logger.getLogger(TempProfileCleanup.class);

	static public void cleanUpProfiles() {
		try {
			if (RE3Properties.getProperty("profile_manager_io_implementation").equals("com.mixshare.rapid_evolution.data.profile.io.LocalProfileIO")) {
				LocalProfileIO.SAFE_SAVE = false; // must temporarily turn off safe saving otherwise it would overwrite itself in the temp directory...
		        Vector<String> tempFiles = new Vector<String>();
		        FileUtil.recurseFileTree(OSHelper.getWorkingDirectory() + "/profiles/temp/", tempFiles);
		        if (tempFiles.size() > 0) {
		        	if (log.isDebugEnabled())
		        		log.debug("cleanUpProfiles(): # temp files=" + tempFiles.size());
		        }
		        for (int i = 0; i < tempFiles.size(); ++i) {
		        	String filename = tempFiles.get(i);
		        	Object obj = XMLSerializer.readData(filename);
		        	if (obj != null) {
		        		if (obj instanceof Profile) {
		        			Profile profile = (Profile)obj;
		        			try {
		        				log.info("main(): replacing missing/corrupt profile filename=" + filename);
		        			} catch (Exception e) { }
		        			ProfileManager.saveProfile(profile);
		        			File file = new File(filename);
			    			file.delete();
		        		}
		        	} else {
		        		log.info("main(): profile in temp directory appears to be corrupt, deleting=" + filename);
		        		File file = new File(filename);
		        		file.delete();
		        	}
		        }
			} else if (RE3Properties.getProperty("profile_manager_io_implementation").equals("com.mixshare.rapid_evolution.data.profile.io.CustomLocalProfileIO")) {
				CustomLocalProfileIO.SAFE_SAVE = false; // must temporarily turn off safe saving otherwise it would overwrite itself in the temp directory...
				processTempDirectory(OSHelper.getWorkingDirectory() + "/profiles/temp/artist", DATA_TYPE_ARTISTS);
				processTempDirectory(OSHelper.getWorkingDirectory() + "/profiles/temp/label", DATA_TYPE_LABELS);
				processTempDirectory(OSHelper.getWorkingDirectory() + "/profiles/temp/release", DATA_TYPE_RELEASES);
				processTempDirectory(OSHelper.getWorkingDirectory() + "/profiles/temp/song", DATA_TYPE_SONGS);
				processTempDirectory(OSHelper.getWorkingDirectory() + "/profiles/temp/mixout", DATA_TYPE_MIXOUTS);
				processTempDirectory(OSHelper.getWorkingDirectory() + "/profiles/temp/style", DATA_TYPE_STYLES);
				processTempDirectory(OSHelper.getWorkingDirectory() + "/profiles/temp/tag", DATA_TYPE_TAGS);
				processTempDirectory(OSHelper.getWorkingDirectory() + "/profiles/temp/playlist", DATA_TYPE_PLAYLISTS);
			} else if (RE3Properties.getProperty("profile_manager_io_implementation").equals("com.mixshare.rapid_evolution.data.profile.io.FileLimitingProfileIO")) {
				FileLimitingProfileIO.SAFE_SAVE = false; // must temporarily turn off safe saving otherwise it would overwrite itself in the temp directory...
				processTempDirectory2(OSHelper.getWorkingDirectory() + "/profiles/temp/artist", DATA_TYPE_ARTISTS);
				processTempDirectory2(OSHelper.getWorkingDirectory() + "/profiles/temp/label", DATA_TYPE_LABELS);
				processTempDirectory2(OSHelper.getWorkingDirectory() + "/profiles/temp/release", DATA_TYPE_RELEASES);
				processTempDirectory2(OSHelper.getWorkingDirectory() + "/profiles/temp/song", DATA_TYPE_SONGS);
				processTempDirectory2(OSHelper.getWorkingDirectory() + "/profiles/temp/mixout", DATA_TYPE_MIXOUTS);
				processTempDirectory2(OSHelper.getWorkingDirectory() + "/profiles/temp/style", DATA_TYPE_STYLES);
				processTempDirectory2(OSHelper.getWorkingDirectory() + "/profiles/temp/tag", DATA_TYPE_TAGS);
				processTempDirectory2(OSHelper.getWorkingDirectory() + "/profiles/temp/playlist", DATA_TYPE_PLAYLISTS);
			} else {
				log.warn("cleanUpProfiles(): unknown profile IO implementation");
			}
		} catch (Exception e) {
			log.error("cleanUpProfiles(): error", e);
		} finally {
			CustomLocalProfileIO.SAFE_SAVE = true;
			LocalProfileIO.SAFE_SAVE = true;
			FileLimitingProfileIO.SAFE_SAVE = true;
		}
	}

	static private void processTempDirectory(String directory, byte type) {
        Vector<String> tempFiles = new Vector<String>();
        FileUtil.recurseFileTree(directory, tempFiles);
        if (tempFiles.size() > 0) {
        	if (log.isDebugEnabled())
        		log.debug("processTempDirectory(): directory=" + directory + ", # temp files=" + tempFiles.size());
        }
        for (int i = 0; i < tempFiles.size(); ++i) {
        	String filename = tempFiles.get(i);
        	if (log.isTraceEnabled())
        		log.trace("processTempDirectory(): processing file=" + filename);
        	Profile profile = null;
        	try {
        		LineReader lineReader = LineReaderFactory.getLineReader(filename);
        		if (lineReader != null) {
		        	if (type == DATA_TYPE_ARTISTS)
		        		profile = new ArtistProfile(lineReader);
		        	else if (type == DATA_TYPE_LABELS)
		        		profile = new LabelProfile(lineReader);
		        	else if (type == DATA_TYPE_RELEASES)
		        		profile = new ReleaseProfile(lineReader);
		        	else if (type == DATA_TYPE_SONGS)
		        		profile = new SongProfile(lineReader);
		        	else if (type == DATA_TYPE_MIXOUTS)
		        		profile = new MixoutProfile(lineReader);
		        	else if (type == DATA_TYPE_STYLES)
		        		profile = new StyleProfile(lineReader);
		        	else if (type == DATA_TYPE_TAGS)
		        		profile = new TagProfile(lineReader);
		        	else if (type == DATA_TYPE_PLAYLISTS)
		        		profile = PlaylistProfile.readPlaylistProfile(lineReader);
		        	lineReader.close();
        		}
        	} catch (Exception e) {
        		log.error("processTempDirectory(): error", e);
        	}
        	if (profile != null) {
        		long tempLastModified = new File(tempFiles.get(i)).lastModified();
        		if (log.isDebugEnabled())
        			log.debug("processTempDirectory(): read profile from temp directory=" + profile.getUniqueId() + ", timestamp=" + tempLastModified);
        		CustomLocalProfileIO customIO = (CustomLocalProfileIO)ProfileManager.getProfileIO();
        		long currentLastModified = new File(customIO.getFile(profile.getIdentifier(), profile.getUniqueId())).lastModified();
        		Profile currentProfile = customIO.getProfile(profile.getIdentifier(), profile.getUniqueId());
        		if (log.isDebugEnabled())
        			log.debug("processTempDirectory(): current profile timestamp=" + currentLastModified);
        		if ((currentProfile != null) && (currentLastModified >= tempLastModified)) {
            		if (log.isDebugEnabled())
            			log.debug("processTempDirectory(): existing profile is readable and more current, discarding temp profile");
        			File file = new File(filename);
        			if (!file.delete()) {
        				file.deleteOnExit();
            			log.warn("processTempDirectory(): couldn't delete temp file=" + file + ", deleting on exit...");
        			}
        		} else {
	        		try {
	    				log.info("processTempDirectory(): replacing missing/corrupt profile filename=" + filename);
	        			ProfileManager.saveProfile(profile);
	        			File file = new File(filename);
	        			if (!file.delete()) {
	        				file.deleteOnExit();
                			log.warn("processTempDirectory(): couldn't delete temp file=" + file + ", deleting on exit...");
	        			}
	    			} catch (Exception e) { }
        		}
        	} else {
        		log.info("main(): profile in temp directory appears to be corrupt, deleting=" + filename);
        		File file = new File(filename);
    			if (!file.delete()) {
    				file.deleteOnExit();
        			log.warn("processTempDirectory(): couldn't delete temp file=" + file + ", deleting on exit...");
    			}
        	}
        }
	}


	static private void processTempDirectory2(String directory, byte type) {
		try {
	        Vector<String> tempFiles = new Vector<String>();
	        FileUtil.recurseFileTree(directory, tempFiles);
	        if (tempFiles.size() > 0) {
	        	if (log.isDebugEnabled())
	        		log.debug("processTempDirectory(): directory=" + directory + ", # temp files=" + tempFiles.size());
	        }
	        for (int i = 0; i < tempFiles.size(); ++i) {
	        	String filename = tempFiles.get(i);
	        	if (log.isTraceEnabled())
	        		log.trace("processTempDirectory2(): processing file=" + filename);
	        	Map<Integer, Profile> profiles = new HashMap<Integer, Profile>();
	        	boolean success = true;
	        	try {
	        		FileLimitingProfileIO.populateProfileMap(profiles, filename, type);
	        	} catch (Exception e) {
	        		log.debug("processTempDirectory2(): error", e);
	        		success = false;
	        	}
	        	if (success && (profiles.size() > 0)) {
	        		long tempLastModified = new File(filename).lastModified();
	        		if (log.isDebugEnabled())
	        			log.debug("processTempDirectory2(): read profiles from temp directory=" + profiles + ", timestamp=" + tempLastModified);
	        		Profile firstProfile = profiles.values().iterator().next();
	        		String currentFilename = FileLimitingProfileIO.getFile(firstProfile.getIdentifier().getTypeDescription(), FileLimitingProfileIO.getMappedValue(type, firstProfile.getUniqueId()));
	        		long currentLastModified = new File(currentFilename).lastModified();
	        		Map<Integer, Profile> currentProfiles = new HashMap<Integer, Profile>();
	        		try {
	        			FileLimitingProfileIO.populateProfileMap(currentProfiles, currentFilename, type);
	        		} catch (Exception e) {
	        			log.debug("processTempDirectory(): error", e);
	        			success = false;
	        		}
	        		if (log.isDebugEnabled())
	        			log.debug("processTempDirectory(): current profile timestamp=" + currentLastModified);
	        		if (success && (currentProfiles.size() > 0) && (currentLastModified >= tempLastModified)) {
	            		if (log.isDebugEnabled())
	            			log.debug("processTempDirectory2(): existing profile is readable and more current, discarding temp profile");
	        			File file = new File(filename);
	        			if (!file.delete()) {
	        				file.deleteOnExit();
	            			log.warn("processTempDirectory2(): couldn't delete temp file=" + file + ", deleting on exit...");
	        			}
	        		} else {
		        		try {
		    				log.info("processTempDirectory(): replacing missing/corrupt profile filename=" + filename);
		    				FileLimitingProfileIO.writeProfilesToFile(profiles.values(), currentFilename);
		        			File file = new File(filename);
		        			if (!file.delete()) {
		        				file.deleteOnExit();
	                			log.warn("processTempDirectory2(): couldn't delete temp file=" + file + ", deleting on exit...");
		        			}
		    			} catch (Exception e) { }
	        		}
	        	} else {
	        		log.info("main(): profile file in temp directory appears to be corrupt, deleting=" + filename);
	        		File file = new File(filename);
	    			if (!file.delete()) {
	    				file.deleteOnExit();
	        			log.warn("processTempDirectory2(): couldn't delete temp file=" + file + ", deleting on exit...");
	    			}
	        	}
	        }
		} catch (Exception e) {
			log.error("processTempDirectory2(): error=" + e);
		}
	}

}
