package com.mixshare.rapid_evolution.workflow.maintenance.search;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.profile.search.artist.ArtistProfile;
import com.mixshare.rapid_evolution.data.profile.search.label.LabelProfile;
import com.mixshare.rapid_evolution.data.profile.search.release.ReleaseProfile;
import com.mixshare.rapid_evolution.workflow.CommonTask;

public class SongGroupIntegrityCheck extends CommonTask {

	static private Logger log = Logger.getLogger(SongGroupIntegrityCheck.class);
    static private final long serialVersionUID = 0L;
	
    private boolean success = false;
    
	public String toString() {
		return "Song Group Integrity Check";
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
					Vector<Integer> removedSongIds = new Vector<Integer>();
					for (int songId : artist.getSongIds()) {
						if (!Database.getSongIndex().doesExist(songId))
							removedSongIds.add(songId);						
					}
					if (removedSongIds.size() > 0) {
						if (log.isDebugEnabled())
							log.debug("execute(): removing missing song ids from artist=" + artist + ", song ids=" + removedSongIds);
						artist.removeSongs(removedSongIds);
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
					Vector<Integer> removedSongIds = new Vector<Integer>();
					for (int songId : label.getSongIds()) {
						if (!Database.getSongIndex().doesExist(songId))
							removedSongIds.add(songId);						
					}
					if (removedSongIds.size() > 0) {
						if (log.isDebugEnabled())
							log.debug("execute(): removing missing song ids from label=" + label + ", song ids=" + removedSongIds);
						label.removeSongs(removedSongIds);
						label.save();
					}
				}
			}
			
			for (int releaseId : Database.getReleaseIndex().getIds()) {
				if (RapidEvolution3.isTerminated || isCancelled())
					return;
				// releases
				ReleaseProfile release = Database.getReleaseIndex().getReleaseProfile(releaseId);
				if (release != null) {
					Vector<Integer> removedSongIds = new Vector<Integer>();
					for (int songId : release.getSongIds()) {
						if (!Database.getSongIndex().doesExist(songId))
							removedSongIds.add(songId);						
					}
					if (removedSongIds.size() > 0) {
						if (log.isDebugEnabled())
							log.debug("execute(): removing missing song ids from release=" + release + ", song ids=" + removedSongIds);
						release.removeSongs(removedSongIds);
						release.save();
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
