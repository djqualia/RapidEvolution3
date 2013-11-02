package com.mixshare.rapid_evolution.workflow.maintenance.search;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.index.search.SearchIndex;
import com.mixshare.rapid_evolution.data.profile.common.image.Image;
import com.mixshare.rapid_evolution.data.profile.common.image.InvalidImageException;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.util.filesystem.FileSystemAccess;
import com.mixshare.rapid_evolution.workflow.CommonTask;

public class RemoveBadImagesTask extends CommonTask {

	static private Logger log = Logger.getLogger(RemoveBadImagesTask.class);
    static private final long serialVersionUID = 0L;
	
    private boolean success = false;
    
	public void execute() {
		try {
			if (log.isInfoEnabled())
				log.info("execute(): starting...");
			
			for (SearchIndex searchIndex : Database.getSearchIndexes()) {
				for (int searchId : searchIndex.getIds()) {
					if (RapidEvolution3.isTerminated || isCancelled())
						return;
					SearchProfile searchProfile = (SearchProfile)searchIndex.getProfile(searchId);					
					if (searchProfile != null) {
						for (Image image : searchProfile.getAllImages()) {
							try {
								FileSystemAccess.getFileSystem().readBufferedImage(image.getImageFilename());
								image.setDisabled(false);
							} catch (InvalidImageException iie) {
								log.warn("execute(): removing bad image=" + image.getImageFilename() + ", from profile=" + searchProfile);
								image.setDisabled(true);								
							}
						}						
						searchProfile.save();
					}
				}
			}

			
			success = true;
			if (log.isInfoEnabled())
				log.info("execute(): finished!");

		} catch (Exception e) {
			log.error("execute(): error", e);			
		}
	}
	
	public Object getResult() {
		return success;
	}
	
	public int getTaskPriority() {
		return RE3Properties.getInt("default_task_priority") + 5;
	}
	
	public String toString() { return "Removing bad images"; }

	public boolean isIndefiniteTask() { return true; }
	
}
