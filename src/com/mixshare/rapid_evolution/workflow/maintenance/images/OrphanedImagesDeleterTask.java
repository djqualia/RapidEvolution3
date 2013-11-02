package com.mixshare.rapid_evolution.workflow.maintenance.images;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.index.search.SearchIndex;
import com.mixshare.rapid_evolution.data.profile.common.image.Image;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.ui.dialogs.taskprogress.TaskProgressLauncher;
import com.mixshare.rapid_evolution.util.FileUtil;
import com.mixshare.rapid_evolution.util.OSHelper;
import com.mixshare.rapid_evolution.workflow.CommonTask;
import com.trolltech.qt.gui.QApplication;

public class OrphanedImagesDeleterTask extends CommonTask {

	static private Logger log = Logger.getLogger(OrphanedImagesDeleterTask.class);
    static private final long serialVersionUID = 0L;    	

    private int totalProfiles;
    private int processedProfiles = 0;
    
    // Maps filename to whether a reference was found.
    private final Map<String, Boolean> fileMap = new HashMap<String, Boolean>();
    private final Vector<String> missingReferences = new Vector<String>();

	public String toString() {
		return "Searching for and deleting orphaned image files";
	}
    
	public void execute() {
		try {
			log.info("execute(): starting...");
			
//	    	if (RE3Properties.getBoolean("show_progress_bars") && !RE3Properties.getBoolean("server_mode"))
//	    		QApplication.invokeLater(new TaskProgressLauncher(this));

	    	// Build a map of the files we care to check.
	    	Vector<String> allExistingImages = new Vector<String>();
	    	FileUtil.recurseFileTree(OSHelper.getWorkingDirectory() + "/data/albumcovers", allExistingImages);
	    	FileUtil.recurseFileTree(OSHelper.getWorkingDirectory() + "/data/discogs/images", allExistingImages);
	    	FileUtil.recurseFileTree(OSHelper.getWorkingDirectory() + "/data/echonest/images", allExistingImages);
	    	FileUtil.recurseFileTree(OSHelper.getWorkingDirectory() + "/data/idiomag/images", allExistingImages);
	    	FileUtil.recurseFileTree(OSHelper.getWorkingDirectory() + "/data/lastfm/images", allExistingImages);
	    	for (String file : allExistingImages) {
	    		fileMap.put(new File(file).getCanonicalPath(), false);
	    	}
	    	log.info("execute(): initial # of files in map=" + fileMap.size());
	    	
	    	totalProfiles = Database.getArtistIndex().getSize()
	    			+ Database.getLabelIndex().getSize()
	    			+ Database.getReleaseIndex().getSize()
	    			+ Database.getSongIndex().getSize();
	    	if (totalProfiles == 0)
	    		return;

	    	checkIndex(Database.getArtistIndex());
	    	checkIndex(Database.getLabelIndex());
	    	checkIndex(Database.getReleaseIndex());
	    	checkIndex(Database.getSongIndex());
	    	
	    	Vector<String> filesToRemove = new Vector<String>();
	    	for (Entry<String, Boolean> entry : fileMap.entrySet()) {
	    		if (!entry.getValue()) {
	    			filesToRemove.add(entry.getKey());
	    		}
	    	}
	    	log.info("execute(): # candidates to remove=" + filesToRemove.size());
	    	log.info("execute(): # missingReferences=" + missingReferences.size());
	    	//log.info("execute(): candidates=" + filesToRemove);
	    	//log.info("execute(): missingReferences=" + missingReferences);
	    	
	    	int numSuccessfullyDeleted = 0;
	    	for (String file : filesToRemove) {
	    		if (!new File(file).delete()) {
	    			new File(file).deleteOnExit();
	    		} else {
	    			++numSuccessfullyDeleted;
	    		}
	    	}
	    	log.info("execute(): numSuccessfullyDeleted=" + numSuccessfullyDeleted);
		} catch (Exception e) {
			log.error("execute(): error", e);
		}
		
		log.info("execute(): finished");		
	}

	private void checkIndex(SearchIndex index) throws IOException {
		if (RapidEvolution3.isTerminated || isCancelled())
			return;				
		for (int id : index.getIds()) {
			if (RapidEvolution3.isTerminated || isCancelled())
				return;
			
			SearchRecord record = index.getSearchRecord(id);
			if ((record != null) && record.hasThumbnail()) {
				File reference = new File(OSHelper.getWorkingDirectory() + "/" + record.getThumbnailImageFilename());
				if (reference.exists()) {
					String canonical = reference.getCanonicalPath();
					if (fileMap.containsKey(canonical)) {
						fileMap.put(canonical, true);
					}
				} else {
					missingReferences.add(OSHelper.getWorkingDirectory() + "/" + record.getThumbnailImageFilename());
				}
			}
			SearchProfile profile = (SearchProfile) index.getProfile(id);
			if (profile != null) {
				for (Image image : profile.getImages()) {
					if (!image.isDisabled()) {
						File reference = new File(OSHelper.getWorkingDirectory() + "/" + image.getImageFilename());
						if (reference.exists()) {
							String canonical = reference.getCanonicalPath();
							if (fileMap.containsKey(canonical)) {
								fileMap.put(canonical, true);
							}
						} else {
							missingReferences.add(OSHelper.getWorkingDirectory() + "/" + image.getImageFilename());
						}
					}
				}
			}
			
			++processedProfiles;
			setProgress(((float)processedProfiles) / totalProfiles);
		}
	}
}
