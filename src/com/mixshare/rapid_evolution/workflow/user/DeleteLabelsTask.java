package com.mixshare.rapid_evolution.workflow.user;

import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.profile.search.label.LabelProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.search.label.LabelRecord;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.ui.dialogs.taskprogress.TaskProgressLauncher;
import com.mixshare.rapid_evolution.util.StringUtil;
import com.mixshare.rapid_evolution.workflow.CommonTask;
import com.trolltech.qt.gui.QApplication;

public class DeleteLabelsTask extends CommonTask {
	
	static private Logger log = Logger.getLogger(DeleteLabelsTask.class);
    static private final long serialVersionUID = 0L;    	
    
	private Vector<Record> deletedLabels;
	
	public DeleteLabelsTask(Vector<Record> deletedLabels) {
		this.deletedLabels = deletedLabels;
	}
				
	public void execute() {
		try {
			int numToDelete = deletedLabels.size();
			int numDeleted = 0;
        	if (RE3Properties.getBoolean("show_progress_bars") && !RE3Properties.getBoolean("server_mode"))
        		QApplication.invokeLater(new TaskProgressLauncher(this));        							
			for (Record record : deletedLabels) {
            	if (RapidEvolution3.isTerminated || isCancelled())
            		return;				
				LabelRecord label = (LabelRecord)record;								
    			LabelProfile labelProfile = (LabelProfile)Database.getLabelIndex().getLabelProfile(label.getUniqueId());
    			if (labelProfile != null) {
	    			Iterator<Integer> songIter = labelProfile.getSongIds().iterator();
	    			while (songIter.hasNext()) {
	                	if (RapidEvolution3.isTerminated || isCancelled())
	                		return;				
	    				SongRecord associatedSong = Database.getSongIndex().getSongRecord(songIter.next());
	    				if (associatedSong != null) {
	    					SongProfile songProfile = Database.getSongIndex().getSongProfile(associatedSong.getUniqueId());
	    					if (songProfile != null) {
	    						songProfile.removeLabelName(label.getLabelName());
			    				if (associatedSong.getNumLabels() == 0) {
			    					if (log.isDebugEnabled())
			    						log.debug("run(): deleting=" + record.getIdentifier());    					
			    					Database.delete(associatedSong.getIdentifier());
			    				} else {
			    					songProfile.save();
			    				}	    						
	    					}
	    				}
	    			}
    			}
    			++numDeleted;
    			setProgress(((float)numDeleted) / numToDelete);
			}
		} catch (Exception e) {
			log.error("run(): error", e);
		}
	}
		
	public String toString() { return "Deleting labels " + StringUtil.getTruncatedDescription(deletedLabels, true); }

}
