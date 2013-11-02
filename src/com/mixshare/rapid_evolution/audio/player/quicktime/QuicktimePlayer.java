package com.mixshare.rapid_evolution.audio.player.quicktime;

import java.io.File;

import org.apache.log4j.Logger;

import quicktime.app.time.TaskAllMovies;
import quicktime.io.OpenMovieFile;
import quicktime.io.QTFile;
import quicktime.std.clocks.TimeRecord;
import quicktime.std.movies.Movie;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.audio.qt.QTSessionCheck;
import com.mixshare.rapid_evolution.audio.qt.QTUtil;
import com.mixshare.rapid_evolution.player.PlayerCallBack;
import com.mixshare.rapid_evolution.player.PlayerInterface;
import com.mixshare.rapid_evolution.ui.widgets.mediaplayer.MediaPlayer;
import com.mixshare.rapid_evolution.util.FileUtil;
import com.mixshare.rapid_evolution.util.io.FileLockManager;

/**
 * This is Quicktime not Qt (which is Phonon)
 * @author jesse
 *
 */
public class QuicktimePlayer implements PlayerInterface {

	static private Logger log = Logger.getLogger(QuicktimePlayer.class);
    
    private Movie movie = null;
    private PlayerCallBack callBack;
    
    
    private String originalFilename;
    private String newFilename;
    
    /**
     * @return true if it is able to open and play the filename
     */
    public boolean open(String filename) {
    	QTUtil.getQuicktimeLock();
        if (log.isDebugEnabled()) 
        	log.debug("open(): filename=" + filename);
        boolean success = false;
        try {
            File file = new File(filename);
            if (file.exists()) {
                originalFilename = filename;
                FileLockManager.startFileRead(originalFilename);           
                if (RE3Properties.getBoolean("enable_quicktime_temporary_file_rename_fix")) {
	                String name = FileUtil.getFilenameMinusDirectory(filename);
	                if (name.length() > QTUtil.maxSupportedFilenameSize) {
	                    log.debug("init(): filename exceeds maximum supported length for quicktime=" + name);
	                    String extension = FileUtil.getExtension(filename);
	                    String newName = name.substring(0, QTUtil.maxSupportedFilenameSize - extension.length() - 1);
	                    newFilename = FileUtil.getDirectoryFromFilename(filename) + newName + "." + extension;
	                    log.debug("init(): temporarily renaming to=" + newFilename);
	                    if (file.renameTo(new File(newFilename))) {
	                        log.debug("init(): rename succeeded");
	                        filename = newFilename;
	                        file = new File(filename);
	                    } else {
	                        log.debug("init(): file could not be renamed");
	                    }
	                }
                }
                QTSessionCheck.check();            
                QTFile qtf = new QTFile(file);
                OpenMovieFile omf = OpenMovieFile.asRead(qtf);                
                movie = Movie.fromFile(omf);   
                movie.setVolume(1.0f);
                TaskAllMovies.addMovieAndStart();
                movie.prePreroll(0, 1.0f);            
                movie.setRate(1.0f);
                success = true;
            }
        } catch (quicktime.std.StdQTException e) {
        	if (log.isDebugEnabled())
        		log.debug("open(): std qt exception=" + e);
        } catch (Exception e) {
            log.error("open(): error Exception", e);
            movie = null;
        }
        return success;
    }
    
    public void close() {
    	try {
    		stop();
    		TaskAllMovies.removeMovie();        			
            if (RE3Properties.getBoolean("enable_quicktime_temporary_file_rename_fix")) {
            	QTSessionCheck.close();
				if ((originalFilename != null) && (newFilename != null)) {
	    			log.debug("close(): renaming back to original=" + originalFilename);
	    			File file = new File(newFilename);
	    			if (file.renameTo(new File(originalFilename))) {                
	    				log.debug("close(): rename succeeded");
	    			} else {
	    				log.error("close(): rename failed");
	    			}
	    		}
            }
            movie = null;
    	} catch (Exception e) {
    		log.error("close(): error", e);    		
    	} finally {
    		FileLockManager.endFileRead(originalFilename);
    		QTUtil.releaseQuicktimeLock();
    	}
    }
    
    public boolean isFileSupported() {
        return (movie != null);
    }
    
    public boolean hasVideo() { return false; }
    
    public class UpdateThread extends Thread {
        private boolean done = false;
        public void done() { done = true; }
        public void run() {
            try {
                while (!done) {
                    double currentSeconds = ((double)movie.getTime()) / movie.getTimeScale();
                    if (callBack != null) {                
                        callBack.setPosition(currentSeconds / getTotalTime());
                        //callBack.setIsPlaying(true);
                    }
                    if (!done) {
	                    if (movie.isDone()) {
	                        done = true;
	                        if (callBack != null) {
	                            callBack.donePlayingSong();
	                        }
	                    } else {
	                    	Thread.sleep(MediaPlayer.TICK_INTERVAL_MILLIS);
	                    }
                    }
                }            
            } catch (Exception e) {
                log.error("run(): error Exception", e);
            }
        }
    }
    
    private UpdateThread updateThread = null;
        
    public void pause() {
        try {
            if (updateThread != null) {
                updateThread.done();
                updateThread = null;
            }
            if (movie != null)
                movie.stop();
        } catch (Exception e) {
            log.error("stop(): error Exception", e);
        }
    }
    
    public void stop() {
        try {
        	pause();
            setPosition(0.0);
            if (callBack != null) {
                callBack.setIsPlaying(false);
            }
        } catch (Exception e) {
            log.error("stop(): error Exception", e);
        }
    }
    
    public void start() {
        try {
            movie.start();
            if (updateThread != null) {
                updateThread.done();                
            }
            updateThread = new UpdateThread();
            updateThread.start();
            if (callBack != null) {
                callBack.setIsPlaying(true);
            }
        } catch (Exception e) {
            log.error("start(): error Exception", e);
        }
    }
    
    /**
     * @return double in seconds
     */
    public double getTotalTime() {
        double result = 0.0;
        try {
            if (movie != null)
                result = (double)movie.getDuration() / movie.getTimeScale();
        } catch (Exception e) { }
        return result;
    }
    
    /**
     * Where percentage is between 0 and 1.  The player should obey
     * the current running state.  I.e. if it is not playing when this is
     * called, then it won't be playing after, and vice versa.
     */
    public void setPosition(double percentage) {
        try {
        	if (movie != null)
        		movie.setTime(new TimeRecord(movie.getTimeScale(), (long)(movie.getDuration() * percentage)));
        } catch (Exception e) {
            log.error("setPosition(): error Exception", e);
        }
    }
    
    public void setVolume(double percentage) {
        try {
            if (movie != null)
                movie.setVolume((float)percentage);
        } catch (Exception e) {
            log.error("setVolume(): error Exception", e);
        }
    }
    
    public void setCallBack(PlayerCallBack callBack) {
        this.callBack = callBack;
    }
        
}
