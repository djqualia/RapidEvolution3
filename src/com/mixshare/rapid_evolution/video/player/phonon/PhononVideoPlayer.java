package com.mixshare.rapid_evolution.video.player.phonon;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.music.duration.Duration;
import com.mixshare.rapid_evolution.player.PlayerCallBack;
import com.mixshare.rapid_evolution.player.PlayerInterface;
import com.mixshare.rapid_evolution.ui.widgets.mediaplayer.MediaPlayer;
import com.mixshare.rapid_evolution.util.io.FileLockManager;
import com.trolltech.qt.core.QUrl;
import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.phonon.AudioOutput;
import com.trolltech.qt.phonon.MediaObject;
import com.trolltech.qt.phonon.MediaSource;
import com.trolltech.qt.phonon.Path;
import com.trolltech.qt.phonon.Phonon;
import com.trolltech.qt.phonon.VideoWidget;

public class PhononVideoPlayer implements PlayerInterface {
    
    static private Logger log = Logger.getLogger(PhononVideoPlayer.class);
	
    private boolean fileSupported = true;
    private PlayerCallBack callBack = null;
    private AudioOutput audioOutput = new AudioOutput(Phonon.Category.VideoCategory);
    private MediaObject mediaObject = new MediaObject();
    private Path audioOutputPath = new Path();   
    private double totalTime;
    private String fileName;
    private VideoWidget videoWidget;
    
    public PhononVideoPlayer(VideoWidget videoWidget) {    	
    	this.videoWidget = videoWidget;
    	
        mediaObject.tick.connect(this, "tick()");
        mediaObject.finished.connect(this, "finished()");
        mediaObject.stateChanged.connect(this, "stateChanged(Phonon$State, Phonon$State)");
    	        
        mediaObject.setTickInterval(MediaPlayer.TICK_INTERVAL_MILLIS);
        audioOutputPath = Phonon.createPath(mediaObject, audioOutput);
        Phonon.createPath(mediaObject, videoWidget);        
    }
    
    /**
     * @return true if it is able to open and play the filename
     */
    public boolean open(String fileName) {
    	boolean success = true;
    	try {
    		this.fileName = fileName;
    		FileLockManager.startFileRead(fileName);
    		MediaSource mediaSource = null;
    		if (fileName.contains("://")) {
    			mediaSource = new MediaSource(new QUrl(fileName));
    		} else {
    			mediaSource = new MediaSource(fileName);
    		}
    		mediaSource.stream();
			mediaObject.setCurrentSource(mediaSource);			
			Duration duration = new Duration(mediaObject.totalTime());
			totalTime = duration.getDurationInSeconds();
			MediaPlayer.instance.setMediaObject(mediaObject);			
	        mediaObject.hasVideoChanged.connect(this, "handleVideoChanged(boolean)");	        
    	} catch (Exception e) {
    		log.error("open(): error=" + e);
    		success = false;
    	}
    	if (!mediaObject.isValid())
    		fileSupported = false;
    	mediaObject.play();
    	try { Thread.sleep(1000); } catch (Exception e) { }    	
    	mediaObject.pause();
    	// the following code will generate the media object error if not supported...
    	if (!isFileSupported())
    		success = false;
    	return success;
    }

    public boolean isFileSupported() {
    	return fileSupported;
    }
    
    public boolean hasVideo() { return true; }
    
    public void close() {
    	try {
    		QApplication.invokeAndWait(new CloseThread());
    	} catch (Exception e) {
    		log.error("close(): error", e);
    	} finally {
    		FileLockManager.endFileRead(fileName);
    	}
    }
        
    public void pause() {
    	mediaObject.pause();
    }
    
    public void stop() {
    	mediaObject.pause();
    	//mediaObject.stop();
        mediaObject.seek(0);
    }
    
    public void start() {
    	mediaObject.play();
    }
    
    /**
     * @return double in seconds
     */
    public double getTotalTime() {
    	if (totalTime == 0.0)
    		totalTime = ((double)mediaObject.totalTime()) / 1000;
    	return totalTime;
    }
    
    /**
     * Where percentage is between 0 and 1.  The player should obey
     * the current running state.  I.e. if it is not playing when this is
     * called, then it won't be playing after, and vice versa.
     */
    public void setPosition(double percentage) {
    	if (log.isDebugEnabled())
    		log.debug("setPosition(): percentage=" + percentage);
    	mediaObject.seek((long)(percentage * mediaObject.totalTime()));    	
    }
    
    public void setVolume(double percentage) {
    	audioOutput.setVolume(percentage * 2.0f);	
    }
    
    public void setCallBack(PlayerCallBack callBack) {
    	this.callBack = callBack;
    }

    private void tick() {
        long len = mediaObject.totalTime(); //(long)(getTotalTime() * 1000);
        long pos = mediaObject.currentTime();
        callBack.setPosition(((double)pos) / len);
    }
    
    private void finished() {
    	callBack.donePlayingSong();
    }
    
    private void stateChanged(Phonon.State newstate, Phonon.State oldstate) {
        switch (newstate) {
            case ErrorState:
            	log.error("stateChanged(): media object error=" + mediaObject.errorString() + ", type=" + mediaObject.errorType());
            	fileSupported = false;
                mediaObject.stop();
                callBack.playerError(mediaObject.errorString());
                break;
            case PausedState:
            	callBack.setIsPlaying(false);
                break;            	
            case StoppedState:
            	if (fileSupported)
            		callBack.setIsPlaying(false);
            	if (mediaObject.currentSource().type() == MediaSource.Type.Invalid){
            		fileSupported = false;
                }
                break;
            case PlayingState:
            case BufferingState:
            	callBack.setIsPlaying(true);
                break;
            case LoadingState:
            	callBack.setIsPlaying(false);
                break;
        }
    }
    
    private void handleVideoChanged(boolean hasVideo) {
    	MediaPlayer.instance.getVideoWindow().handleVideoChanged(hasVideo);
    }
    
    private class CloseThread extends Thread {
    	public void run() { mediaObject.stop(); }
    }
}
