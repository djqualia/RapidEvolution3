package com.mixshare.rapid_evolution.audio.player.ffmpeg;

import java.io.File;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.player.PlayerCallBack;
import com.mixshare.rapid_evolution.player.PlayerInterface;
import com.mixshare.rapid_evolution.util.FileUtil;
import com.mixshare.rapid_evolution.util.OSHelper;
import com.mixshare.rapid_evolution.util.StringUtil;
import com.mixshare.rapid_evolution.util.io.FileLockManager;
import com.mixshare.rapid_evolution.util.io.StreamGobbler;

public class FFMPEGPlayer implements PlayerInterface {
    
    static private Logger log = Logger.getLogger(FFMPEGPlayer.class);

    private String filename;
    private String wav_filename;
    private String backingPlayerClass;
    private PlayerInterface backingPlayer;
    private boolean fileSupported = false;
        
    public FFMPEGPlayer(String backingPlayerClass) {
    	this.backingPlayerClass = backingPlayerClass;
    }
    
    /**
     * @return true if it is able to open and play the filename
     */
    public boolean open(String filename) {
    	try {    		
    		this.filename = filename;
    		FileLockManager.startFileRead(filename);
            if (log.isDebugEnabled())
            	log.debug("open(): filename=" + filename);
            String extension = FileUtil.getExtension(filename);
            String new_filename = FileUtil.getFilenameMinusDirectory(filename);
            if (extension.length() > 0)
                new_filename = new_filename.substring(0, new_filename.length() - extension.length() - 1); 
            new_filename += "_" + String.valueOf((long)(Math.random() * 1000000));
            
            String tempDir = RE3Properties.getProperty("temp_working_directory");
            if ((tempDir == null) || (tempDir.length() == 0))
            	tempDir = OSHelper.getWorkingDirectory() + "/temp/";
            else {
            	if (!tempDir.endsWith("/") && !tempDir.endsWith("\\"))
            		tempDir += "/";
            }            
            File dir = new File(tempDir);
            if (!dir.exists())
            	dir.mkdir();
            wav_filename = tempDir +  new_filename + ".wav";
            
            if (log.isDebugEnabled())
            	log.debug("open(): temporary wav filename=" + wav_filename);
            // convert to wav
            String command = RE3Properties.getProperty("ffmpeg_decode_command");
            command = StringUtil.replace(command, "%input%", "\"" + filename + "\"");
            command = StringUtil.replace(command, "%output%", "\"" + wav_filename + "\"");
            
            Process proc = Runtime.getRuntime().exec(command);
            // any error message?
            StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), log);
            // any output?
            StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), log);
            // kick them off
            errorGobbler.start();
            outputGobbler.start();                                    
            // any error???
            int exitVal = proc.waitFor();
            if (log.isDebugEnabled())
            	log.debug("open(): done converting to wav, exit value=" + exitVal);
            
            proc.destroy();
            proc = null;
            
            File outputCheck = new File(wav_filename);
            if (!outputCheck.exists())
            	return false;

            backingPlayer = (PlayerInterface)Class.forName(backingPlayerClass).newInstance();
            backingPlayer.open(wav_filename);
            fileSupported = backingPlayer.isFileSupported();                                    
            return fileSupported;
            
    	} catch (Exception e) {
    		log.error("open(): error", e);    		
    	}
    	return false;
    }

    public boolean isFileSupported() { return backingPlayer.isFileSupported(); }
    
    public boolean hasVideo() { return backingPlayer.hasVideo(); }
    
    public void close() {
    	try {
    		backingPlayer.close();
            File file = new File(wav_filename);
            if (!file.delete()) {
            	if (log.isDebugEnabled())
            		log.debug("close(): can't immediately delete temp wav file=" + wav_filename);
            	file.deleteOnExit();
            }
    	} catch (Exception e) {
    		log.error("close(): error", e);
    	} finally {
    		FileLockManager.endFileRead(filename);
    	}
    }
        
    public void pause() { backingPlayer.pause(); }
    
    public void stop() { backingPlayer.stop(); }
    
    public void start() { backingPlayer.start(); }
    
    /**
     * @return double in seconds
     */
    public double getTotalTime() { return backingPlayer.getTotalTime(); }
    
    /**
     * Where percentage is between 0 and 1.  The player should obey
     * the current running state.  I.e. if it is not playing when this is
     * called, then it won't be playing after, and vice versa.
     */
    public void setPosition(double percentage) { backingPlayer.setPosition(percentage); }
    
    public void setVolume(double percentage) { backingPlayer.setVolume(percentage); }
    
    public void setCallBack(PlayerCallBack callBack) { backingPlayer.setCallBack(callBack); }

    static public void main(String[] args) {
    	try {
    		// a little test routine
    		RapidEvolution3.loadLog4J();
    		FFMPEGPlayer player = new FFMPEGPlayer("com.mixshare.rapid_evolution.audio.player.javasound.JavaSoundPlayer");
    		player.open("C:\\Users\\Jesse\\Desktop\\new music\\Smells-like-teen-spirit-Dual-Remix.mp3");
    		if (player.isFileSupported()) {
    			log.info("duration=" + player.getTotalTime() + "s");
    			player.start();
    			log.info("started");
    			Thread.sleep(5000);
    			player.pause();
    			log.info("paused");
    			Thread.sleep(1000);
    			player.start();
    			log.info("started");
    			Thread.sleep(5000);
    			player.stop();
    			log.info("stopped");
    			Thread.sleep(1000);
    			player.close();
    			log.info("closed");
    			Thread.sleep(5000);
    			
    		} else {
    			log.info("unsupported");
    		}
    	} catch (Exception e) {
    		log.error("main(): error", e);
    	}
    }
    
}
