package com.mixshare.rapid_evolution.workflow.maintenance.search;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.profile.search.artist.ArtistProfile;
import com.mixshare.rapid_evolution.data.profile.search.label.LabelProfile;
import com.mixshare.rapid_evolution.workflow.CommonTask;

public class ReleaseGroupIntegrityCheck extends CommonTask {

	static private Logger log = Logger.getLogger(ReleaseGroupIntegrityCheck.class);
    static private final long serialVersionUID = 0L;
	
    private boolean success = false;
    
	public String toString() {
		return "Release Group Integrity Check";
	}
    
	public void execute() {
		try {
			if (log.isDebugEnabled())
				log.debug("execute(): starting...");
			
			for (int artistId : Database.getArtistIndex().getIds()) {
				if (RapidEvolution3.isTerminated || isCancelled())
					return;
				// artists
				ArtistProfile artist = Database.getArtistIndex().getArtistProfile(artistId);
				if (artist != null) {
					Vector<Integer> removedReleaseIds = new Vector<Integer>();
					for (int releaseId : artist.getReleaseIds()) {
						if (!Database.getReleaseIndex().doesExist(releaseId))
							removedReleaseIds.add(releaseId);						
					}
					if (removedReleaseIds.size() > 0) {
						if (log.isDebugEnabled())
							log.debug("execute(): removing missing release ids from artist=" + artist + ", release ids=" + removedReleaseIds);
						artist.removeReleases(removedReleaseIds);
						artist.save();
					}
				}
			}

			for (int labelId : Database.getLabelIndex().getIds()) {
				if (RapidEvolution3.isTerminated || isCancelled())
					return;
				// labels
				LabelProfile label = Database.getLabelIndex().getLabelProfile(labelId);
				if (label != null) {
					Vector<Integer> removedReleaseIds = new Vector<Integer>();
					for (int releaseId : label.getReleaseIds()) {
						if (!Database.getReleaseIndex().doesExist(releaseId))
							removedReleaseIds.add(releaseId);						
					}
					if (removedReleaseIds.size() > 0) {
						if (log.isDebugEnabled())
							log.debug("execute(): removing missing release ids from label=" + label + ", release ids=" + removedReleaseIds);
						label.removeReleases(removedReleaseIds);
						label.save();
					}
				}
			}
			
			success = true;
			if (log.isDebugEnabled())
				log.debug("execute(): finished...");

		} catch (Exception e) {
			log.error("execute(): error", e);			
		}
	}
	
	public Object getResult() {
		return success;
	}
	
	public int getTaskPriority() {
		return RE3Properties.getInt("default_task_priority") + 20;
	}
	
	public boolean isIndefiniteTask() { return true; }
	
}
