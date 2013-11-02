package com.mixshare.rapid_evolution.workflow.detection.key;

import java.io.File;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.audio.detection.key.DetectedKey;
import com.mixshare.rapid_evolution.audio.detection.key.KeyDetector;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.workflow.Task;
import com.mixshare.rapid_evolution.workflow.detection.DetectionTask;

public class KeyDetectionTask extends DetectionTask {
	
    static private final long serialVersionUID = 0L;    
    static private Logger log = Logger.getLogger(KeyDetectionTask.class);	
    
    static private SongRecord currentSong = null;
    
	public String getDetectionType() { return "Key"; }
	
	static public boolean detectKey(SongRecord song, Task task) {
		boolean success = false;		
		SongProfile songProfile = Database.getSongIndex().getSongProfile(song.getUniqueId());
		if (songProfile != null) {
			File test = new File(song.getSongFilename());
			if (test.exists()) {
				DetectedKey detectedKey = KeyDetector.detectKey(song.getSongFilename(), task);
				if ((detectedKey != null) && detectedKey.isValid()) {
					songProfile.setKey(detectedKey.getStartKey(), detectedKey.getEndKey(), detectedKey.getAccuracy(), DATA_SOURCE_COMPUTED);
					songProfile.save();
					success = true;
				}
				songProfile.setLastDetectKeyAttempt(System.currentTimeMillis());
			}
		}
		return success;
	}
	
	public void detect(SongRecord song) {
		try {
			currentSong = song;
			setSuccess(detectKey(song, this));
		} catch (Exception e) {
			log.error("detect(): error", e);
		}
	}
	
	public boolean isDetectionNeeded(SongRecord song) {
		if (song.getStartKey().isValid())
			return false;
		if (song.getKeyAccuracy() > 0)
			return false;
		if ((song.getSongFilename() == null) || (song.getSongFilename().equals("")))
			return false;
		File file = new File(song.getSongFilename());
		if (!file.exists())
			return false;
		SongProfile songProfile = Database.getSongIndex().getSongProfile(song.getUniqueId());
		if (songProfile != null) {
			long timeSinceLastAttempt = System.currentTimeMillis() - songProfile.getLastDetectKeyAttempt();
			if (timeSinceLastAttempt < (RE3Properties.getLong("failed_detection_minimum_wait_interval_days") * 1000 * 60 * 60 * 24))
				return false;
		}				
		return true;
	}
	
	public String toString() {
		if (currentSong != null)
			return "Detecting key from=" + currentSong.getSongFilename();
		else
			return "Detecting keys";				
	}

}
