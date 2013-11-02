package com.mixshare.rapid_evolution.ui.widgets.mediaplayer.ui_threads;

import com.mixshare.rapid_evolution.ui.widgets.mediaplayer.MediaPlayer;


public class MediaPlayerSongUpdate extends Thread {

	private MediaPlayer mediaPlayer; 
	
	public MediaPlayerSongUpdate(MediaPlayer mediaPlayer) {
		this.mediaPlayer = mediaPlayer;
	}
	
	public void run() {
		mediaPlayer.songUpdate();
	}
	
}
