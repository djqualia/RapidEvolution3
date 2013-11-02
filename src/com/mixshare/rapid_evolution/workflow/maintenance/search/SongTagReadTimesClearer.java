package com.mixshare.rapid_evolution.workflow.maintenance.search;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.workflow.CommonTask;

/**
 * Called to clear the last time the tags were read from each file, so that tag reading will happen again if a change to the tag
 * readers was made
 */
public class SongTagReadTimesClearer extends CommonTask {

	static private Logger log = Logger.getLogger(SongTagReadTimesClearer.class);
    static private final long serialVersionUID = 0L;
	
    private boolean success = false;
        
    public SongTagReadTimesClearer() { }
    
	public void execute() {
    	for (int id : Database.getSongIndex().getIds()) {
    		if (RapidEvolution3.isTerminated || isCancelled())
    			return;
    		SongProfile song = Database.getSongIndex().getSongProfile(id);
    		if (song != null) {
    			if (song.getSongFileLastUpdated() > 0) {
    				song.setSongFileLastUpdated(0);
    				song.save();
    			}
    		}
    	}
		
	}
	
	public Object getResult() { return success; }
	
	public int getTaskPriority() { return RE3Properties.getInt("default_task_priority"); }
	
	public String toString() { return "Clearing Song Tag Read Times"; }

	public boolean isIndefiniteTask() { return true; }
	
}
