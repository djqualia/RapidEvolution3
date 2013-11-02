package com.mixshare.rapid_evolution.workflow.detection;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.workflow.TaskManager;
import com.mixshare.rapid_evolution.workflow.detection.beatintensity.BeatIntensityDetectionTask;
import com.mixshare.rapid_evolution.workflow.detection.bpm.BpmDetectionTask;
import com.mixshare.rapid_evolution.workflow.detection.key.KeyDetectionTask;
import com.mixshare.rapid_evolution.workflow.detection.rga.RGADetectionTask;

public class DetectionTaskStarter {

	static public void start() {
		if (RE3Properties.getBoolean("enable_detection_tasks")) {
			TaskManager.runBackgroundTask(new DetectionStateManager(new KeyDetectionTask(), "enable_key_detection_task"));
			TaskManager.runBackgroundTask(new DetectionStateManager(new BpmDetectionTask(), "enable_bpm_detection_task"));
			TaskManager.runBackgroundTask(new DetectionStateManager(new BeatIntensityDetectionTask(), "enable_beat_intensity_detection_task"));
			TaskManager.runBackgroundTask(new DetectionStateManager(new RGADetectionTask(), "enable_replay_gain_detection_task"));
		}
	}
	
	
}
