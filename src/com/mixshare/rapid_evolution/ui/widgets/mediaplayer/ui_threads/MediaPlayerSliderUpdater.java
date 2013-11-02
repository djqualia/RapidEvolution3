package com.mixshare.rapid_evolution.ui.widgets.mediaplayer.ui_threads;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.ui.widgets.mediaplayer.MediaPlayer;

public class MediaPlayerSliderUpdater extends Thread {

	static private Logger log = Logger.getLogger(MediaPlayerSliderUpdater.class);
	
	private MediaPlayer mediaPlayer; 
	
	private double value;
	
	public MediaPlayerSliderUpdater(MediaPlayer mediaPlayer, double value) {
		this.mediaPlayer = mediaPlayer;
		this.value = value;
	}
	
	public void run() {
    	if (!mediaPlayer.getSlider().isSliderDown()) {
			mediaPlayer.getSlider().valueChanged.disconnect(mediaPlayer, "seekSliderChanged(Integer)");
			mediaPlayer.setCurrentPosition(value);
			mediaPlayer.getSlider().setValue((int)(value * mediaPlayer.getSlider().getMaxValue()));
			mediaPlayer.updateInfo();
			mediaPlayer.getSlider().valueChanged.connect(mediaPlayer, "seekSliderChanged(Integer)");
    	}
    	if (!mediaPlayer.getVideoWindow().getSlider().isSliderDown()) {
    		mediaPlayer.getVideoWindow().getSlider().valueChanged.disconnect(mediaPlayer.getVideoWindow(), "seekSliderChanged(Integer)");
    		mediaPlayer.getVideoWindow().getSlider().setValue((int)(value * mediaPlayer.getVideoWindow().getSlider().getMaxValue()));
    		mediaPlayer.getVideoWindow().getSlider().valueChanged.connect(mediaPlayer.getVideoWindow(), "seekSliderChanged(Integer)");    		
    	}
	}
	
}
