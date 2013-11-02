package com.mixshare.rapid_evolution.player;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.audio.player.AudioPlayerFactory;
import com.mixshare.rapid_evolution.audio.util.AudioUtil;
import com.mixshare.rapid_evolution.ui.widgets.mediaplayer.MediaPlayer;
import com.mixshare.rapid_evolution.util.timing.Semaphore;
import com.mixshare.rapid_evolution.video.player.VideoPlayerFactory;
import com.mixshare.rapid_evolution.video.util.VideoUtil;

public class PlayerFactory {

    static private Logger log = Logger.getLogger(PlayerFactory.class);
    
    static private Semaphore getPlayerSem = new Semaphore(1);
    static private PlayerInterface lastPlayer = null;
    
    static public PlayerInterface getPlayer(String filename, PlayerCallBack callBack) { return getPlayer(filename, callBack, null); }
    static public PlayerInterface getPlayer(String filename, PlayerCallBack callBack, Vector<Class> classesToAvoid) {
    	PlayerInterface result = null;
    	try {
    		getPlayerSem.acquire();
    		if (lastPlayer != null) {
    			lastPlayer.close();
    			lastPlayer = null;
    			new Thread() { public void run() { System.gc(); }}.start();
    		}
        	if (VideoUtil.isSupportedVideoFileType(filename)) {
        		result = VideoPlayerFactory.getPlayer(MediaPlayer.instance.getVideoWidget(), filename, callBack, classesToAvoid);
        		if ((result == null) && AudioUtil.isSupportedAudioFileType(filename)) // i.e. mp4s
        			result = AudioPlayerFactory.getPlayer(filename, callBack, classesToAvoid);        		
        	}
        	else if (AudioUtil.isSupportedAudioFileType(filename))
        		result = AudioPlayerFactory.getPlayer(filename, callBack, classesToAvoid);
        	lastPlayer = result;
    	} catch (Exception e) {
    		log.error("getPlayer(): error", e);
    	} finally {
    		getPlayerSem.release();
    	}
    	return result;
    }
	
}
