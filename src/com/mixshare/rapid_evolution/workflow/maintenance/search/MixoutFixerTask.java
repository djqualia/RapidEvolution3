package com.mixshare.rapid_evolution.workflow.maintenance.search;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.profile.search.song.MixoutProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.record.search.song.MixoutRecord;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.workflow.CommonTask;

public class MixoutFixerTask extends CommonTask {

	static private Logger log = Logger.getLogger(MixoutFixerTask.class);
    static private final long serialVersionUID = 0L;
	
	public String toString() {
		return "Mixout Fixer Task";
	}
    
	public void execute() {
		try {
			if (log.isDebugEnabled())
				log.debug("execute(): starting...");
			
			Vector<Integer> ids = Database.getMixoutIndex().getIds();
			int progress = 0;
	        for (int mixoutId : ids) {
	        	if (RapidEvolution3.isTerminated || isCancelled())
	        		return;
	        	MixoutRecord mixout = (MixoutRecord)Database.getMixoutIndex().getRecord(mixoutId);
	        	SongRecord fromSong = mixout.getFromSong();
	        	SongRecord toSong = mixout.getToSong();
	        	if ((fromSong != null) && (toSong != null)) {
	        		SongProfile fromSongProfile = Database.getSongIndex().getSongProfile(fromSong.getUniqueId());
	        		if (fromSongProfile != null) {
	        			MixoutProfile mixoutProfile = (MixoutProfile)Database.getMixoutIndex().getProfile(mixoutId);
	        			if (mixoutProfile == null) {
	        				mixoutProfile = new MixoutProfile(mixout);
	        				mixoutProfile.save();	        				
	        			}
	        			if (mixoutProfile != null) {
	        				fromSongProfile.addMixout(mixoutProfile);
	        				fromSongProfile.save();
	        			}
	        		}
	        	}
	        	if (log.isDebugEnabled())
	        		log.debug("read mixout from=" + fromSong + ", to=" + toSong);
	        	++progress;
	        	setProgress(((float)progress) / ids.size());
	        }
	        RE3Properties.setProperty("run_mixout_fixer", "true");

		} catch (Exception e) {
			log.error("execute(): error", e);			
		}
	}
	
	public Object getResult() { return null; }
	
	public int getTaskPriority() {
		return RE3Properties.getInt("default_task_priority") + 5;
	}
	
	public boolean isIndefiniteTask() { return false; }
	
}
