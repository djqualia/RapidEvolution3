package com.mixshare.rapid_evolution.workflow.user.player;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.player.PlayerManager;
import com.mixshare.rapid_evolution.player.PlayerUserSessionManager;
import com.mixshare.rapid_evolution.workflow.CommonTask;

public class PreComputeNextSongTask extends CommonTask {
	
    static private final long serialVersionUID = 0L;    	
    static private Logger log = Logger.getLogger(PreComputeNextSongTask.class);	
	
    private SongRecord currentItemCheck;
	private PlayerUserSessionManager session;
	
	public PreComputeNextSongTask(SongRecord currentItemCheck) {
		this.session = PlayerManager.getCurrentUserSessionManager();
		this.currentItemCheck = currentItemCheck;
	}
	public PreComputeNextSongTask(PlayerUserSessionManager session) {
		this.session = session;
	}
	
	public String toString() { return "Pre-computing next song for session " + session.getUserName(); }
	
	public void execute() {
		try {			
			if (session != null) {
				if (currentItemCheck != null) {
					if (!currentItemCheck.equals(PlayerManager.getCurrentSong()))
						return;					
				}
				session.preComputeNextSongToPlay();
			}
		} catch (Exception e) {
			log.error("execute(): error", e);
		}
	}

	public int getTaskPriority() { return RE3Properties.getInt("default_task_priority"); }

	public boolean isIndefiniteTask() { return true; }
	
}
