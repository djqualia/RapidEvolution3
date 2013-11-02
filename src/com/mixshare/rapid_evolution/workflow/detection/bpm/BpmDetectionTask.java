package com.mixshare.rapid_evolution.workflow.detection.bpm;

import java.io.File;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.audio.detection.bpm.BpmDetector;
import com.mixshare.rapid_evolution.audio.detection.bpm.DetectedBpm;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.music.bpm.Bpm;
import com.mixshare.rapid_evolution.workflow.Task;
import com.mixshare.rapid_evolution.workflow.detection.DetectionTask;

public class BpmDetectionTask extends DetectionTask {
	
    static private final long serialVersionUID = 0L;    
    static private Logger log = Logger.getLogger(BpmDetectionTask.class);	
    
    static private SongRecord currentSong = null;
    
	public String getDetectionType() { return "Bpm"; }
	
	static public boolean detectBPM(SongRecord song, Task task) {
		boolean success = false;
		SongProfile songProfile = Database.getSongIndex().getSongProfile(song.getUniqueId());
		if (songProfile != null) {
			File test = new File(song.getSongFilename());
			if (test.exists()) {
				DetectedBpm detectedBpm = BpmDetector.detectBpm(song.getSongFilename(), task);
				if ((detectedBpm != null) && detectedBpm.isValid()) {
					songProfile.setBpm(detectedBpm.getBpm(), Bpm.NO_BPM, (byte)(detectedBpm.getAccuracy() * 100), DATA_SOURCE_COMPUTED);
					songProfile.save();
					success = true;
				}
				songProfile.setLastDetectBpmAttempt(System.currentTimeMillis());
			}
		}	
		return success;
	}
	
	public void detect(SongRecord song) {
		try {
			currentSong = song;
			setSuccess(detectBPM(song, this));
		} catch (Exception e) {
			log.error("detect(): error", e);
		}
	}
	
	public boolean isDetectionNeeded(SongRecord song) {
		if (song.getBpmStart().isValid())
			return false;
		if (song.getBpmAccuracy() > 0)
			return false;
		if ((song.getSongFilename() == null) || (song.getSongFilename().equals("")))
			return false;
		File file = new File(song.getSongFilename());
		if (!file.exists())
			return false;		
		SongProfile songProfile = Database.getSongIndex().getSongProfile(song.getUniqueId());
		if (songProfile != null) {
			long timeSinceLastAttempt = System.currentTimeMillis() - songProfile.getLastDetectBpmAttempt();
			if (timeSinceLastAttempt < (RE3Properties.getLong("failed_detection_minimum_wait_interval_days") * 1000 * 60 * 60 * 24))
				return false;
		}				
		return true;
	}

	public String toString() {
		if (currentSong != null)
			return "Detecting BPM from=" + currentSong.getSongFilename();
		else
			return "Detecting BPMs";				
	}	
	
}
