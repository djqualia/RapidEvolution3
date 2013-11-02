package com.mixshare.rapid_evolution.video.util;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.music.duration.Duration;
import com.trolltech.qt.core.QUrl;
import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.phonon.MediaObject;
import com.trolltech.qt.phonon.MediaSource;
import com.trolltech.qt.phonon.Phonon;
import com.trolltech.qt.phonon.VideoWidget;

public class QtVideoUtil {
	
	static private Logger log = Logger.getLogger(QtVideoUtil.class);
	
	static public Duration getDuration(String filename) {
		Duration result = null;
    	// get duration using Qt/phonon
		DurationReader durationReader = new DurationReader(filename);
		QApplication.invokeAndWait(durationReader);
		while (result == null)
			result = durationReader.getDuration();	
		return result;
	}

    static private class DurationReader extends Thread {
    	private String filename;
    	private Duration duration = null;
    	private MediaObject mediaObject;
    	public DurationReader(String filename) {
    		this.filename = filename;
    	}
    	public void run() {
    		try {
	    		MediaSource mediaSource = null;
	    		if (filename.contains("://")) {
	    			mediaSource = new MediaSource(new QUrl(filename));
	    		} else {
	    			mediaSource = new MediaSource(filename);
	    		}
	    		mediaObject = new MediaObject();
	    		mediaObject.setCurrentSource(mediaSource);		
	    		mediaObject.stateChanged.connect(this, "stateChanged(Phonon$State, Phonon$State)");
	    		Phonon.createPath(mediaObject, new VideoWidget());
    		} catch (Exception e) {
    			log.error("run(): error", e);
    		}
    	}    
    	public Duration getDuration() { return duration; }
        private void stateChanged(Phonon.State newstate, Phonon.State oldstate) {
        	if (log.isDebugEnabled())
        		log.debug("stateChanged(): new state=" + newstate);
            switch (newstate) {
            	case ErrorState:
            		duration = new Duration(0);
            		break;
                case StoppedState:
                	duration = new Duration(mediaObject.totalTime());
                	if (log.isDebugEnabled())
                		log.debug("stateChanged(): duration=" + duration);
                    break;                
            }
            
        }    	
    }    
		
}
