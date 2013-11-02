package com.mixshare.rapid_evolution.ui.widgets.mediaplayer.ui_threads;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.ui.widgets.mediaplayer.MediaPlayer;

public class MediaPlayerDisableBack extends Thread {

	static private Logger log = Logger.getLogger(MediaPlayerDisableBack.class);
	
	private MediaPlayer mediaPlayer; 
	
	public MediaPlayerDisableBack(MediaPlayer mediaPlayer) {
		this.mediaPlayer = mediaPlayer;
	}
	
	public void run() {
		mediaPlayer.getBackButton().setEnabled(false);
		mediaPlayer.getVideoWindow().getBackButton().setEnabled(false);
	}
	
}
