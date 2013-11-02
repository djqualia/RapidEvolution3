package com.mixshare.rapid_evolution.ui.widgets.mediaplayer.ui_threads;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.ui.widgets.mediaplayer.MediaPlayer;

public class MediaPlayerStopUpdater extends Thread {

	static private Logger log = Logger.getLogger(MediaPlayerStopUpdater.class);
	
	private MediaPlayer mediaPlayer; 
	
	public MediaPlayerStopUpdater(MediaPlayer mediaPlayer) {
		this.mediaPlayer = mediaPlayer;
	}
	
	public void run() {
		mediaPlayer.getSlider().valueChanged.disconnect(mediaPlayer, "seekSliderChanged(Integer)");
		mediaPlayer.getSlider().setValue(0);
        mediaPlayer.getSlider().valueChanged.connect(mediaPlayer, "seekSliderChanged(Integer)");
        mediaPlayer.setCurrentPosition(0.0);
        mediaPlayer.setIsPlayingNow(false, 0.0);
        mediaPlayer.updateInfo();
	}
	
}
