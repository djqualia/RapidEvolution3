package com.mixshare.rapid_evolution.ui.widgets.mediaplayer.ui_threads;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.ui.widgets.mediaplayer.MediaPlayer;

public class MediaPlayerInit extends Thread {

	static private Logger log = Logger.getLogger(MediaPlayerInit.class);
	
	private boolean showAndRaise = false;
	private boolean enableBack = false;
	private MediaPlayer mediaPlayer; 
	
	public MediaPlayerInit(MediaPlayer mediaPlayer) {
		this.mediaPlayer = mediaPlayer;
		this.showAndRaise = true;
	}
	
	public MediaPlayerInit(MediaPlayer mediaPlayer, boolean showAndRaise, boolean enableBack) {
		this.mediaPlayer = mediaPlayer;
		this.showAndRaise = showAndRaise;
		this.enableBack = enableBack;
	}	
	
	public void run() {
		if (showAndRaise) {
			mediaPlayer.show();
			mediaPlayer.raise();
			mediaPlayer.activateWindow();
		}
		mediaPlayer.play();
		if (enableBack) {
    		mediaPlayer.getBackButton().setEnabled(true);
    		mediaPlayer.getVideoWindow().getBackButton().setEnabled(true);
		}
	}
	
}
