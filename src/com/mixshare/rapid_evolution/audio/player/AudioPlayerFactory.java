package com.mixshare.rapid_evolution.audio.player;

import java.io.File;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.audio.player.ffmpeg.FFMPEGPlayer;
import com.mixshare.rapid_evolution.audio.player.javasound.JavaSoundPlayer;
import com.mixshare.rapid_evolution.audio.player.phonon.PhononPlayer;
import com.mixshare.rapid_evolution.audio.player.quicktime.QuicktimePlayer;
import com.mixshare.rapid_evolution.audio.player.xuggle.XugglePlayer;
import com.mixshare.rapid_evolution.audio.qt.QTUtil;
import com.mixshare.rapid_evolution.audio.util.AudioUtil;
import com.mixshare.rapid_evolution.player.PlayerCallBack;
import com.mixshare.rapid_evolution.player.PlayerInterface;
import com.trolltech.qt.gui.QApplication;

public class AudioPlayerFactory {

    static private Logger log = Logger.getLogger(AudioPlayerFactory.class);
    
    static public PlayerInterface getPlayer(String filename, PlayerCallBack callBack) { return getPlayer(filename, callBack, null); }
    static public PlayerInterface getPlayer(String filename, PlayerCallBack callBack, Vector<Class> classesToAvoid) {
        PlayerInterface result = null;
        try {
            File test_file = new File(filename);
            if (test_file.exists()) {                
                int audio_file_type = AudioUtil.getAudioFileType(filename);
        	
                // try xuggle framework
                if (RE3Properties.getBoolean("enable_xuggle_codec")) {
		        	if (!((classesToAvoid != null) && classesToAvoid.contains(XugglePlayer.class))) {
		            	try {
		            		result = new XugglePlayer();
		            		result.open(filename);    
		            		if (!result.isFileSupported()) {
		            			result.close();
		            			result = null;
		            		} else {
		            			if (log.isDebugEnabled())
		            				log.debug("getPlayer(): using xuggle player for file=" + filename);
		            		}
		            	} catch (Exception e) {
		            		log.error("getPlayer(): error", e);
		            	}
		        	}                	
                }
                
	        	// try Quicktime framework
	        	if (QTUtil.isQuickTimeSupported()) {
		        	if (!((classesToAvoid != null) && classesToAvoid.contains(QuicktimePlayer.class))) {
			        	try {
				            if ((result == null) && QTUtil.isQuickTimeSupported()) {
				                result = new QuicktimePlayer();
				                result.open(filename);
				                if (!result.isFileSupported()) {
				                    result.close();
				                    result = null;
				                } else {
				                	if (log.isDebugEnabled())
				                		log.debug("getPlayer(): using quicktime player for file=" + filename);
				                }
				            }
			        	} catch (Exception e) {
			        		log.error("getPlayer(): error", e);
			        		result = null;
			        	}
		        	}
	        	}
	        	// try Qt Phonon framework
	        	if (RE3Properties.getBoolean("enable_phonon_codec")) {
		        	if (!((classesToAvoid != null) && classesToAvoid.contains(PhononPlayer.class))) {
		        		try {        
		        			if (result == null) {
		        				PhononAudioPlayerCreator playerCreator = new PhononAudioPlayerCreator(filename);
		        				QApplication.invokeAndWait(playerCreator);
					    		result = playerCreator.getPlayerInterface();
		        			}
				    	} catch (Exception e) {
				    		log.error("getPlayer(): error", e);
				    		result = null;
				    	}
		        	}
	        	}
	        	
	        	// try javasound framework
	            if (result == null) {
	            	if (RE3Properties.getBoolean("enable_java_codecs")) {	            	
			        	if (!((classesToAvoid != null) && classesToAvoid.contains(JavaSoundPlayer.class))) {	        		
			            	try {
			            		result = new JavaSoundPlayer();
			            		result.open(filename);    
			            		if (!result.isFileSupported()) {
			            			result.close();
			            			result = null;
			            		} else {
			            			if (log.isDebugEnabled())
			            				log.debug("getPlayer(): using javasound player for file=" + filename);
			            		}
			            	} catch (Exception e) {
			            		log.error("getPlayer(): error", e);
			            	}
			        	}
	            	}
	            }
	            
	        	// try FFMPEG player
	        	if (RE3Properties.getBoolean("enable_ffmpeg_codec")) {
		        	if (!((classesToAvoid != null) && classesToAvoid.contains(FFMPEGPlayer.class))) {	        		
		        		try {        
		        			if (result == null) {
		        				FFMPEGPhononAudioPlayerCreator playerCreator = new FFMPEGPhononAudioPlayerCreator(filename);
		        				QApplication.invokeAndWait(playerCreator);
					    		result = playerCreator.getPlayerInterface();
		        			}
				    	} catch (Exception e) {
				    		log.error("getPlayer(): error", e);
				    		result = null;
				    	}
		        	}
	        	}
	            
	            if (result != null) {
	                result.setCallBack(callBack);
	            }
            }
        } catch (Exception e) {
            log.error("getPlayer(): error Exception", e);
        }
        return result;
    }
    
    static private class PhononAudioPlayerCreator extends Thread {
    	private PlayerInterface playerInterface;
    	private String filename;
    	public PhononAudioPlayerCreator(String filename) {
    		this.filename = filename;
    	}
    	public void run() {
    		playerInterface = new PhononPlayer();
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

    static private class FFMPEGPhononAudioPlayerCreator extends Thread {
    	private PlayerInterface playerInterface;
    	private String filename;
    	public FFMPEGPhononAudioPlayerCreator(String filename) {
    		this.filename = filename;
    	}
    	public void run() {
    		playerInterface = new FFMPEGPlayer("com.mixshare.rapid_evolution.audio.player.phonon.PhononPlayer");    		
    		playerInterface.open(filename);
    		if (!playerInterface.isFileSupported()) {
    			playerInterface.close();
    			playerInterface = null;
    		} else {
    			if (log.isDebugEnabled())
    				log.debug("getPlayer(): using ffmpeg phonon player for file=" + filename);
    		}
    	}
    	public PlayerInterface getPlayerInterface() { return playerInterface; }
    }

    
}
