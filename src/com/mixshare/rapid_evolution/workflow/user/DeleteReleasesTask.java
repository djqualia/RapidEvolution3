package com.mixshare.rapid_evolution.workflow.user;

import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.profile.search.release.ReleaseProfile;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.search.release.ReleaseRecord;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.ui.dialogs.taskprogress.TaskProgressLauncher;
import com.mixshare.rapid_evolution.util.StringUtil;
import com.mixshare.rapid_evolution.workflow.CommonTask;
import com.trolltech.qt.gui.QApplication;

public class DeleteReleasesTask extends CommonTask {
	
	static private Logger log = Logger.getLogger(DeleteReleasesTask.class);
    static private final long serialVersionUID = 0L;    	
    
	private Vector<Record> deletedReleases;
	
	public DeleteReleasesTask(Vector<Record> deletedReleases) {
		this.deletedReleases = deletedReleases;
	}
				
	public void execute() {
		try {
			int numReleasesToDelete = deletedReleases.size();
			int numDeleted = 0;
        	if (RE3Properties.getBoolean("show_progress_bars") && !RE3Properties.getBoolean("server_mode"))
        		QApplication.invokeLater(new TaskProgressLauncher(this));        				
			for (Record record : deletedReleases) {
            	if (RapidEvolution3.isTerminated || isCancelled())
            		return;				
				ReleaseRecord release = (ReleaseRecord)record;								
    			ReleaseProfile releaseProfile = (ReleaseProfile)Database.getReleaseIndex().getReleaseProfile(release.getUniqueId());
    			if (releaseProfile != null) {
	    			Iterator<Integer> songIter = releaseProfile.getSongIds().iterator();
	    			while (songIter.hasNext()) {
	                	if (RapidEvolution3.isTerminated || isCancelled())
	                		return;				
	    				SongRecord associatedSong = Database.getSongIndex().getSongRecord(songIter.next());
	    				if (associatedSong != null) {
		    				associatedSong.removeReleaseInstance(release.getUniqueId());
		    				if (associatedSong.getReleaseInstances().size() == 0) {
		    					if (log.isDebugEnabled())
		    						log.debug("run(): deleting=" + record.getIdentifier());    					
		    					Database.delete(associatedSong.getIdentifier());
		    				} else {
		    					associatedSong.update();
		    				}
	    				}
	    			}
    			}
    			Database.getReleaseIndex().delete(release.getUniqueId());
    			++numDeleted;
    			setProgress(((float)numDeleted) / numReleasesToDelete);
			}
		} catch (Exception e) {
			log.error("run(): error", e);
		}
	}
		
	public String toString() { return "Deleting releases " + StringUtil.getTruncatedDescription(deletedReleases, true); }

}
