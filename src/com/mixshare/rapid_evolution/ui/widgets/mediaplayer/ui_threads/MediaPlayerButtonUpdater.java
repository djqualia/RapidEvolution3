package com.mixshare.rapid_evolution.ui.widgets.mediaplayer.ui_threads;

import com.mixshare.rapid_evolution.ui.widgets.mediaplayer.MediaPlayer;

public class MediaPlayerButtonUpdater extends Thread {

	private MediaPlayer mediaPlayer;
	private boolean playing;
	private double position;
	
	public MediaPlayerButtonUpdater(MediaPlayer mediaPlayer, boolean playing, double position) {
		this.mediaPlayer = mediaPlayer;
		this.playing = playing;
		this.position = position;
	}
	
	public void run() {
		mediaPlayer.setIsPlayingNow(playing, position);
	}
	
}
