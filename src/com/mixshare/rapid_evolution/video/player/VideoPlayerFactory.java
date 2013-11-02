package com.mixshare.rapid_evolution.video.player;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.player.PlayerCallBack;
import com.mixshare.rapid_evolution.player.PlayerInterface;
import com.mixshare.rapid_evolution.video.player.phonon.PhononVideoPlayer;
import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.phonon.VideoWidget;

public class VideoPlayerFactory {

    static private Logger log = Logger.getLogger(VideoPlayerFactory.class);
    
    static public PlayerInterface getPlayer(VideoWidget videoWidget, String filename, PlayerCallBack callBack) { return getPlayer(videoWidget, filename, callBack, null); }
    static public PlayerInterface getPlayer(VideoWidget videoWidget, String filename, PlayerCallBack callBack, Vector<Class> classesToAvoid) {
        PlayerInterface result = null;
        try {
        	// try Qt Phonon framework
        	if (RE3Properties.getBoolean("enable_phonon_codec")) {
	        	if (!((classesToAvoid != null) && classesToAvoid.contains(PhononVideoPlayer.class))) {        	
					if (result == null) {
						PhononVideoPlayerCreator creator = new PhononVideoPlayerCreator(videoWidget, filename);
						QApplication.invokeAndWait(creator); // Qt requires the creator to be created in the same thread that it will be manipulated with
						result = creator.getPlayerInterface();
					}
	        	}	        
        	}
            if (result != null) {
                result.setCallBack(callBack);
            }
        } catch (Exception e) {
            log.error("getPlayer(): error Exception", e);
        }
        return result;
    }
    
    static private class PhononVideoPlayerCreator extends Thread {
    	private VideoWidget videoWidget;
    	private PlayerInterface playerInterface;
    	private String filename;
    	public PhononVideoPlayerCreator(VideoWidget videoWidget, String filename) {
    		this.videoWidget = videoWidget;
    		this.filename = filename;
    	}
    	public void run() {
    		playerInterface = new PhononVideoPlayer(videoWidget);
    		playerInterface.open(filename);
    		if (!playerInterface.isFileSupported()) {
    			playerInterface.close();
    			playerInterface = null;
    		} else {
    			if (log.isDebugEnabled())
    				log.debug("getPlayer(): using phonon player for file=" + filename);
    		}
    		
    	}
    	public PlayerInterface getPlayerInterface() { return playerInterface; }
    }
	
}
