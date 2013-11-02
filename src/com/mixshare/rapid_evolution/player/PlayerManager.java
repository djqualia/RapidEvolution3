package com.mixshare.rapid_evolution.player;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.RE3StatusBar;
import com.mixshare.rapid_evolution.ui.widgets.mediaplayer.MediaPlayer;
import com.mixshare.rapid_evolution.ui.widgets.mediaplayer.ui_threads.MediaPlayerInit;
import com.mixshare.rapid_evolution.util.timing.Semaphore;
import com.mixshare.rapid_evolution.workflow.CommonTask;
import com.mixshare.rapid_evolution.workflow.TaskManager;
import com.trolltech.qt.gui.QApplication;

public class PlayerManager implements DataConstants {

    static private Logger log = Logger.getLogger(PlayerManager.class);    
	
	static private MediaPlayer mediaPlayer = new MediaPlayer();		
	static private Semaphore playSongsSem = new Semaphore(1);
	static private PlayerUserSessionManager currentUserSessionManager;
	
	static public PlayerUserSessionManager getCurrentUserSessionManager() {
		if (currentUserSessionManager == null)
			currentUserSessionManager = new PlayerUserSessionManager("**LocalUser**");		
		return currentUserSessionManager; 
	}
	
	static public void playSongRecords(Vector<SongRecord> songs) {
		Vector<Integer> songIds = new Vector<Integer>(songs.size());
		for (SongRecord song : songs)
			songIds.add(song.getUniqueId());
		playSongs(songIds);
	}
	static public void playSongs(Vector<Integer> songIds) {
		try {
			if (log.isDebugEnabled())
				log.debug("playSongs(): songIds=" + songIds);
			if (RE3Properties.getBoolean("use_os_media_player")) {
				OSMediaPlayerInvoker.invokePlaylist(songIds);
			} else {
				if (RE3StatusBar.instance != null)
					RE3StatusBar.instance.showStatusMessageLater(Translations.get("launching_media_player_text"));
				TaskManager.runForegroundTask(new PlayTask(songIds));
			}
		} catch (Exception e) {
			log.error("playSongs(): error", e);
		}
	}
	
	static public SongRecord getCurrentSong() { return getCurrentUserSessionManager().getCurrentSong(); }
	static public SongProfile getCurrentSongProfile() { return getCurrentUserSessionManager().getCurrentSongProfile(); }
	static public SongRecord getPreviousSongToPlay() { return getCurrentUserSessionManager().getPreviousSongToPlay(); }	
	static public boolean hasPreviousSong() { return getCurrentUserSessionManager().hasPreviousSong(); }		
	static public SongRecord getNextSongToPlay() { return getCurrentUserSessionManager().getNextSongToPlay(((mediaPlayer != null) && (mediaPlayer.isShuffleEnabled()))); }
	static public void preComputeNextSongToPlay() { getCurrentUserSessionManager().preComputeNextSongToPlay(); }
		
	static private class PlayTask extends CommonTask {
		private Vector<Integer> songIds;
		public PlayTask(Vector<Integer> songIds) {
			this.songIds = songIds;
		}
		public String toString() {
			return new String("Player Manager - Play Task");
		}
		public void execute() {
			try {
				playSongsSem.acquire();				
				
				getCurrentUserSessionManager().init(songIds);
				
				// start playing the first song
				SongRecord song = getNextSongToPlay();
				if (song != null) {
					if (mediaPlayer == null)
						mediaPlayer = new MediaPlayer();
					mediaPlayer.setSong(song);
					QApplication.invokeAndWait(new MediaPlayerInit(mediaPlayer));
				} else {
					log.warn("playSongs(): could not find song to play");
				}		
				
			} catch (Exception e) {
				log.error("execute(): error", e);
			} finally {
				playSongsSem.release();
			}
		}
	}
	
}
