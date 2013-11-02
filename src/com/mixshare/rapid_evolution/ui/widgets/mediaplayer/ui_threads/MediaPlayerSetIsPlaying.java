package com.mixshare.rapid_evolution.ui.widgets.mediaplayer.ui_threads;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.ui.widgets.mediaplayer.MediaPlayer;

public class MediaPlayerSetIsPlaying extends Thread {

	static private Logger log = Logger.getLogger(MediaPlayerSetIsPlaying.class);
	
	private MediaPlayer mediaPlayer; 
	boolean playing;
	double currentPosition;
	
	public MediaPlayerSetIsPlaying(MediaPlayer mediaPlayer, boolean playing, double currentPosition) {
		this.mediaPlayer = mediaPlayer;
		this.playing = playing;
		this.currentPosition = currentPosition;
	}
	
	public void run() {
		if (playing) {
	    	if (mediaPlayer.getCurrentPlayer() != null)
	    		mediaPlayer.getCurrentPlayer().start();			
		} else {
	    	if (mediaPlayer.getCurrentPlayer() != null)
	    		mediaPlayer.getCurrentPlayer().pause();
			
		}
		mediaPlayer.setIsPlayingNow(playing, currentPosition);
	}
	
}
