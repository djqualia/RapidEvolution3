package com.mixshare.rapid_evolution.ui.widgets.mediaplayer.ui_threads;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.ui.widgets.mediaplayer.MediaPlayer;

public class MediaPlayerForward extends Thread {

	static private Logger log = Logger.getLogger(MediaPlayerForward.class);
	
	private MediaPlayer mediaPlayer; 
	
	public MediaPlayerForward(MediaPlayer mediaPlayer) {
		this.mediaPlayer = mediaPlayer;
	}
	
	public void run() {
		mediaPlayer.updateInfo();
		mediaPlayer.forward();
	}
	
}
