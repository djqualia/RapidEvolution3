package com.mixshare.rapid_evolution.video.util;

import org.apache.log4j.Logger;

import quicktime.io.OpenMovieFile;
import quicktime.io.QTFile;
import quicktime.std.movies.Movie;

import com.mixshare.rapid_evolution.audio.qt.QTSessionCheck;
import com.mixshare.rapid_evolution.audio.qt.QTUtil;
import com.mixshare.rapid_evolution.music.duration.Duration;

public class QuicktimeVideoUtil {

	static private Logger log = Logger.getLogger(QuicktimeVideoUtil.class);
	
	static public Duration getDuration(String filename) {
		Duration result = null;
		try {
			QTUtil.getQuicktimeLock();
			// get duration using Quicktime
            QTSessionCheck.check();            
            QTFile qtf = new QTFile(filename);
            OpenMovieFile omf = OpenMovieFile.asRead(qtf);                
            Movie movie = Movie.fromFile(omf);   
            result = new Duration((double)movie.getDuration() / movie.getTimeScale() * 1000);
            movie = null;
		} catch (java.lang.NoClassDefFoundError e) {
			log.debug("getDuration(): no class def found" + e);
		} catch (java.lang.ExceptionInInitializerError e) {
			log.debug("getDuration(): exception in initialization=" + e);
		} catch (quicktime.std.StdQTException qte) {
			log.debug("getDuration(): qt exception=" + qte);
		} catch (Error e) {
			log.error("getDuration(): error", e);
		} catch (Exception e) {
			log.error("getDuration(): error", e);
		} finally {
			QTUtil.releaseQuicktimeLock();
		}		
		return result;
	}
	
}
