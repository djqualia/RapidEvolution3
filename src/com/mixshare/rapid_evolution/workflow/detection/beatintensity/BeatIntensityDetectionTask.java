package com.mixshare.rapid_evolution.workflow.detection.beatintensity;

import java.io.File;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.audio.detection.beatintensity.BeatIntensityDetector;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.music.beatintensity.BeatIntensity;
import com.mixshare.rapid_evolution.workflow.Task;
import com.mixshare.rapid_evolution.workflow.detection.DetectionTask;

public class BeatIntensityDetectionTask extends DetectionTask {
	
    static private final long serialVersionUID = 0L;    
    static private Logger log = Logger.getLogger(BeatIntensityDetectionTask.class);	
    
    static private SongRecord currentSong = null;
    
	public String getDetectionType() { return "Beat Intensity"; }
	
	static public boolean detectBeatIntensity(SongRecord song, Task task) {
		boolean success = false;
		SongProfile songProfile = Database.getSongIndex().getSongProfile(song.getUniqueId());
		if (songProfile != null) {
			File test = new File(song.getSongFilename());
			if (test.exists()) {				
				BeatIntensity detectedBeatIntensity = BeatIntensityDetector.detectBeatIntensity(song.getSongFilename(), task);
				if ((detectedBeatIntensity != null) && detectedBeatIntensity.isValid()) {
					songProfile.setBeatIntensity(detectedBeatIntensity, DATA_SOURCE_COMPUTED);
					songProfile.save();
					success = true;					
				}
				songProfile.setLastDetectBeatIntensityAttempt(System.currentTimeMillis());
			}
		}		
		return success;
	}
	
	public void detect(SongRecord song) {
		try {
			currentSong = song;
			setSuccess(detectBeatIntensity(song, this));
		} catch (Exception e) {
			log.error("detect(): error", e);
		}
	}
	
	public boolean isDetectionNeeded(SongRecord song) {
		if (song.getBeatIntensityValue().isValid())
			return false;
		if ((song.getSongFilename() == null) || (song.getSongFilename().equals("")))
			return false;
		File file = new File(song.getSongFilename());
		if (!file.exists())
			return false;		
		SongProfile songProfile = Database.getSongIndex().getSongProfile(song.getUniqueId());
		if (songProfile != null) {
			long timeSinceLastAttempt = System.currentTimeMillis() - songProfile.getLastDetectBeatIntensityAttempt();
			if (timeSinceLastAttempt < (RE3Properties.getLong("failed_detection_minimum_wait_interval_days") * 1000 * 60 * 60 * 24))
				return false;
		}		
		return true;
	}
	
	public String toString() {
		if (currentSong != null)
			return "Detecting beat intensity from=" + currentSong.getSongFilename();
		else
			return "Detecting beat intensities";				
	}		
}
