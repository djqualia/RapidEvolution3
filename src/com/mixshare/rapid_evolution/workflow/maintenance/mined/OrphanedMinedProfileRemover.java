package com.mixshare.rapid_evolution.workflow.maintenance.mined;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.index.search.SearchIndex;
import com.mixshare.rapid_evolution.ui.dialogs.taskprogress.TaskProgressLauncher;
import com.mixshare.rapid_evolution.util.FileUtil;
import com.mixshare.rapid_evolution.util.OSHelper;
import com.mixshare.rapid_evolution.workflow.CommonTask;
import com.trolltech.qt.gui.QApplication;

public class OrphanedMinedProfileRemover extends CommonTask {

	static private Logger log = Logger.getLogger(OrphanedMinedProfileRemover.class);
    static private final long serialVersionUID = 0L;    	

    private int totalProfiles;
    private int processedProfiles = 0;
    
    // Maps filename to whether a reference was found.
    private final Map<String, Boolean> fileMap = new HashMap<String, Boolean>();
    private final Vector<String> missingReferences = new Vector<String>();

	public String toString() {
		return "Searching for and deleting orphaned mined profiles";
	}
    
	public void execute() {
		try {
			log.info("execute(): starting...");
			
	    	totalProfiles = Database.getArtistIndex().getSize()
	    			+ Database.getLabelIndex().getSize()
	    			+ Database.getReleaseIndex().getSize()
	    			+ Database.getSongIndex().getSize();
	    	if (totalProfiles == 0)
	    		return;

	    	// Artist mined profiles
	    	fileMap.clear();
	    	Vector<String> existingFiles = new Vector<String>();
	    	FileUtil.recurseFileTree(OSHelper.getWorkingDirectory() + "/mined_profiles/artist", existingFiles);
	    	for (String file : existingFiles) {
	    		fileMap.put(new File(file).getCanonicalPath(), false);
	    	}
	    	log.info("execute(): initial # of artist files=" + fileMap.size());
	    	checkIndex(Database.getArtistIndex());
	    	
	    	// Label mined profiles
	    	fileMap.clear();
	    	existingFiles.clear();
	    	FileUtil.recurseFileTree(OSHelper.getWorkingDirectory() + "/mined_profiles/label", existingFiles);
	    	for (String file : existingFiles) {
	    		fileMap.put(new File(file).getCanonicalPath(), false);
	    	}
	    	log.info("execute(): initial # of label files=" + fileMap.size());
	    	checkIndex(Database.getLabelIndex());
	    	
	    	// Release mined profiles
	    	fileMap.clear();
	    	existingFiles.clear();
	    	FileUtil.recurseFileTree(OSHelper.getWorkingDirectory() + "/mined_profiles/release", existingFiles);
	    	for (String file : existingFiles) {
	    		fileMap.put(new File(file).getCanonicalPath(), false);
	    	}
	    	log.info("execute(): initial # of release files=" + fileMap.size());
	    	checkIndex(Database.getReleaseIndex());	    	

	    	// Song mined profiles
	    	fileMap.clear();
	    	existingFiles.clear();
	    	FileUtil.recurseFileTree(OSHelper.getWorkingDirectory() + "/mined_profiles/song", existingFiles);
	    	for (String file : existingFiles) {
	    		fileMap.put(new File(file).getCanonicalPath(), false);
	    	}
	    	log.info("execute(): initial # of song files=" + fileMap.size());
	    	checkIndex(Database.getSongIndex());	    	
		} catch (Exception e) {
			log.error("execute(): error", e);
		}
		
		log.info("execute(): finished");		
	}

	private void checkIndex(SearchIndex index) throws IOException {
    	for (Entry<String, Boolean> entry : fileMap.entrySet()) {
    		int id = Integer.parseInt(FileUtil.getFilenameMinusDirectoryMinusExtension(entry.getKey()));
    		if (index.doesExist(id)) {
    			entry.setValue(true);
    		}
    	}
		
		Vector<String> filesToRemove = new Vector<String>();
    	for (Entry<String, Boolean> entry : fileMap.entrySet()) {
    		if (!entry.getValue()) {
    			filesToRemove.add(entry.getKey());
    		}
    	}
    	log.info("execute(): # candidates to remove=" + filesToRemove.size());
    	//log.info("execute(): candidates=" + filesToRemove);
    	int numSuccessfullyDeleted = 0;
    	for (String file : filesToRemove) {
    		if (!new File(file).delete()) {
    			new File(file).deleteOnExit();
    		} else {
    			++numSuccessfullyDeleted;
    		}
    	}
    	log.info("execute(): numSuccessfullyDeleted=" + numSuccessfullyDeleted);
	}
	
	public boolean isIndefiniteTask() {
		return true;
	}
}
