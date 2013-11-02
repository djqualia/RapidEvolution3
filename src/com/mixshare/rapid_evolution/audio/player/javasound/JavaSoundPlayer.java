package com.mixshare.rapid_evolution.audio.player.javasound;

import java.io.File;

import javax.sound.sampled.Control;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.audio.codecs.AudioDecoder;
import com.mixshare.rapid_evolution.audio.codecs.decoders.DefaultAudioDecoder;
import com.mixshare.rapid_evolution.audio.exceptions.UnsupportedFileException;
import com.mixshare.rapid_evolution.audio.util.AudioUtil;
import com.mixshare.rapid_evolution.music.duration.Duration;
import com.mixshare.rapid_evolution.player.PlayerCallBack;
import com.mixshare.rapid_evolution.player.PlayerInterface;
import com.mixshare.rapid_evolution.util.io.FileLockManager;

public class JavaSoundPlayer implements PlayerInterface {

    private static Logger log = Logger.getLogger(JavaSoundPlayer.class);
    
    private AudioDecoder audioDecoder = null;
    private double currentSeconds;
    private PlayerThread playThread;
    private long bytesRead;
    private String oldTime;
    private PlayerCallBack callBack;
    private boolean stopFlag;
    private String filename;
    
    public boolean open(String filename) {
        if (log.isDebugEnabled()) log.debug("open(): filename=" + filename);
        this.filename = filename;        
        boolean success = false;
        try {
            File file = new File(filename);
            if (file.exists()) {
                FileLockManager.startFileRead(filename);            	
                //audioDecoder = AudioDecoderFactory.getAudioDecoder(file.getAbsolutePath());
                try {
                	audioDecoder = new DefaultAudioDecoder(file.getAbsolutePath());
                } catch (UnsupportedFileException uae) { }
                if ((audioDecoder != null) && (audioDecoder.isFileSupported())) {
                    currentSeconds = 0.0;
                    success = true;
                }
            }
        } catch (java.lang.Error e) {
            log.error("open(): error Exception", e);
        } catch (Exception e) {
            log.error("open(): error Exception", e);
        }
        return success;
    }
    
    public void close() {
    	try {
    		stop();
    		if (audioDecoder != null) {
    			audioDecoder.close();
    			audioDecoder = null;    			
    		}
    		playThread = null;
    	} catch (Exception e) {
    		log.error("close(): error", e);
    	} finally {
    		FileLockManager.endFileRead(filename);
    	}
    }
    
    public boolean isFileSupported() {
        if (audioDecoder != null) {
            return audioDecoder.isFileSupported();
        }
        return false;
    }
    
    public boolean hasVideo() { return false; }
    
    public void pause() {
        stopFlag = true;
        try {
            if (playThread != null) {
                while (playThread.isRunning())
                    Thread.sleep(100);
            }
        } catch (Exception e) {
            log.error("stop(): error Exception", e);
        }
        playThread = null;    }
    
    public void stop() {
        pause();
        if (audioDecoder != null)
        	audioDecoder.reset();
        bytesRead = 0;
    }
    
    public void start() {
        stopFlag = false;
        if (playThread == null) {
            playThread = new PlayerThread();
        }
        if (!playThread.isRunning())
            playThread.start();
    }
    
    public boolean isPlaying() {
        return ((playThread != null) && playThread.isRunning());
    }
    
    public void setCallBack(PlayerCallBack callBack) {
        this.callBack = callBack;
    }
    
    public double getTotalTime() {
        return audioDecoder.getTotalSeconds();
    }
    
    public void setPosition(double percentage) {
        try {
            boolean wasPlaying = isPlaying();
            stop();                
            double seconds = percentage * audioDecoder.getTotalSeconds();
            long newstartplayinglocation = (long)(seconds * audioDecoder.getSampleRate() * audioDecoder.getAudioFormat().getChannels() * (audioDecoder.getAudioFormat().getSampleSizeInBits() / 8));
            if (newstartplayinglocation < bytesRead) {
                audioDecoder.reset();
                bytesRead = 0;
            }
            byte[] data = new byte[1024];
            long gained = 0;
            while (gained < newstartplayinglocation - bytesRead) {
                // TODO: try skip Bytes here! -> i think i did before...
                long skipped = audioDecoder.readBytes(data, 0, data.length);//             currentlyplaying_din.skip(1024);
                if (skipped == -1) break;
                gained += skipped;
            }
            bytesRead += gained;        
            if (wasPlaying)
                start();
        } catch (Exception e) {
            log.error("setPosition(): error Exception", e);
        }
    }
    
    
    private FloatControl volumeControl;
    public void setVolume(double percentage) {        
        if (volumeControl != null) {
        	float logPercent = (float)(Math.log(percentage * 50.0 + 1.0) / Math.log(51.0));
            float range = volumeControl.getMaximum() - volumeControl.getMinimum();            
            volumeControl.setValue((float)(range * logPercent + volumeControl.getMinimum()));
        	//float dB = (float)(Math.log(percentage == 0.0f ? 0.0001f : percentage)/Math.log(10.0)*20.0);
        	//volumeControl.setValue(dB);
        }        
    }
        
    private class PlayerThread extends Thread {
        private boolean isRunning = false;
        public boolean isRunning() { return isRunning; }
        public void run() {
        	SourceDataLine line = null;
            try {
                isRunning = true;
                if (callBack != null)
                    callBack.setIsPlaying(true);                
                byte[] data = new byte[4096];
                line = AudioUtil.getLine(audioDecoder.getAudioFormat());                
                if (log.isTraceEnabled()) {
                    Control[] controls = line.getControls();
                    if (controls != null) {
                        for (int c = 0; c < controls.length; ++c) {
                            log.trace("isRunning(): supported control=" + controls[c].toString());
                        }
                    }
                }
                volumeControl = (FloatControl)line.getControl(FloatControl.Type.MASTER_GAIN);
                if (line != null) {
                    // Start
                    line.start();
                    int nBytesRead = 0;
                    int nBytesWritten = 0;
                    while ((nBytesRead != -1) &&
                            (!stopFlag) &&
                            (!RapidEvolution3.isTerminated)) {
                        try {
                            nBytesRead = (int)audioDecoder.readBytes(data, 0, data.length);
                            if (nBytesRead != -1) {
                                nBytesWritten = line.write(data, 0, nBytesRead);
                                bytesRead += nBytesWritten;
                                currentSeconds = (((double) bytesRead) / audioDecoder.getSampleRate() / audioDecoder.getAudioFormat().getChannels() / (audioDecoder.getAudioFormat().getSampleSizeInBits() / 8));
                                if ((callBack != null) && !stopFlag) {
                                    String newTime = new Duration((int)currentSeconds).getDurationAsString();
                                    if (!newTime.equals(oldTime)) {
                                        oldTime = newTime;
                                    }                          
                                    double totalpercentage = currentSeconds / audioDecoder.getTotalSeconds();
                                    if (!stopFlag)
                                    	callBack.setPosition(totalpercentage);
                                }                          
                            }
                        } catch (Exception e) {
                            log.error("PlayerThread(): error Exception", e);
                        }
                    }
                    // Stop
                    line.drain();
                    line.stop();
                }
            } catch (Exception e) {
                log.error("PlayerThread(): error Exception", e);
            } finally {
            	if (line != null) {
            		line.close();
            	}
            	isRunning = false;
            }
            if ((callBack != null) && !stopFlag) {
                callBack.setIsPlaying(false);                
                callBack.donePlayingSong();    
            }            
        }        
    }
        
}
