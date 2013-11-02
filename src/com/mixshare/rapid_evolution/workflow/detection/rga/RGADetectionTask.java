package com.mixshare.rapid_evolution.workflow.detection.rga;

import java.io.File;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.audio.detection.rga.RGAData;
import com.mixshare.rapid_evolution.audio.detection.rga.RGADetector;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.workflow.Task;
import com.mixshare.rapid_evolution.workflow.detection.DetectionTask;

public class RGADetectionTask extends DetectionTask {
	
    static private final long serialVersionUID = 0L;    
    static private Logger log = Logger.getLogger(RGADetectionTask.class);	
    
    static private SongRecord currentSong = null;
    
	public String getDetectionType() { return "Replay Gain"; }
	
	static public boolean detectRGA(SongRecord song, Task task) {
		return detectRGA(song, task, true);
	}
	static public boolean detectRGA(SongRecord song, Task task, boolean overwrite) {
		boolean success = false;
		SongProfile songProfile = Database.getSongIndex().getSongProfile(song.getUniqueId());
		if (songProfile != null) {
			if (!overwrite && (songProfile.getReplayGain() != null))
				return true;
			File test = new File(song.getSongFilename());
			if (test.exists()) {
				RGAData detectedRga = new RGADetector(song.getSongFilename(), task).detectRGA();
				if ((detectedRga != null) && detectedRga.isValid()) {
					songProfile.setReplayGain(detectedRga.getDifference(), DATA_SOURCE_COMPUTED);
					songProfile.save();
					success = true;
				}
				songProfile.setLastDetectRGAAttempt(System.currentTimeMillis());
			}
		}		
		return success;
	}
	
	public void detect(SongRecord song) {
		try {
			currentSong = song;
			setSuccess(detectRGA(song, this));
		} catch (Exception e) {
			log.error("detect(): error", e);
		}
	}
	
	public boolean isDetectionNeeded(SongRecord song) {
		if ((song.getSongFilename() == null) || (song.getSongFilename().equals("")))
			return false;
		File file = new File(song.getSongFilename());
		if (!file.exists())
			return false;		
		SongProfile songProfile = Database.getSongIndex().getSongProfile(song.getUniqueId());
		if (songProfile != null) {
			if ((songProfile.getReplayGain() != null) && (Math.abs(songProfile.getReplayGain()) < 65.0f))
				return false;
			long timeSinceLastAttempt = System.currentTimeMillis() - songProfile.getLastDetectRGAAttempt();
			if (timeSinceLastAttempt < (RE3Properties.getLong("failed_detection_minimum_wait_interval_days") * 1000 * 60 * 60 * 24))
				return false;
			return true;
		}				
		return false;		
	}
	
	public String toString() {
		if (currentSong != null)
			return "Detecting replay gain from=" + currentSong.getSongFilename();
		else
			return "Detecting replay gain values";				
	}			

}
