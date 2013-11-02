package com.mixshare.rapid_evolution.audio.qt;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.audio.codecs.decoders.QTAudioDecoder;
import com.mixshare.rapid_evolution.util.timing.Semaphore;

public class QTUtil {

	static private Logger log = Logger.getLogger(QTUtil.class);
    
    static public int maxSupportedFilenameSize = 63; // this excludes the directory part, and is currently a lame and unfortunate quicktime limitation
    
    static private Semaphore quicktimeLock = new Semaphore(1);
    static private boolean isLocked = false;
    
    static public void getQuicktimeLock() {
    	try {
    		quicktimeLock.tryAcquire("getQuicktimeLock", 1000 * 60 * 5);
    		isLocked = true;
    	} catch (Exception e) {
    		log.error("getQuicktimeLock(): error");
    	}
    }
    static public void releaseQuicktimeLock() {
    	isLocked = false;
    	quicktimeLock.release();    	
    }
    
    static public boolean isQuickTimeSupported() {
        try {
        	if (!RE3Properties.getBoolean("enable_quicktime_codec"))
        		return false;
        	if (isLocked)
        		return false;
            return (!QTVersionCheck.getQTJVersion().equals("N/A"));            
        } catch (java.lang.Error e) {
            log.debug("isQuickTimeSupported(): error Exception", e);
        } catch (Exception e) {
            log.debug("isQuickTimeSupported(): error Exception", e);            
        }
        return false;
    }
    
    static public String getVersionString() {
        try {
        	getQuicktimeLock();
            return "QuickTime Version: " + QTVersionCheck.getQTVersion() + ", QTJ Version: " + QTVersionCheck.getQTJVersion();
        } catch (java.lang.Error e) {
            log.debug("getVersionString(): error Exception", e);
        } catch (Exception e) {
            log.debug("getVersionString(): error Exception", e);            
        } finally {
        	releaseQuicktimeLock();
        }
        return null;
    }

    static public void closeQT() {
        try {
        	getQuicktimeLock();
            QTSessionCheck.close();
        } catch (java.lang.Error e) {
            log.debug("closeQT(): error Exception", e);
        } catch (Exception e) {
            log.debug("closeQT(): error Exception", e);            
        } finally {
        	releaseQuicktimeLock();
        }
    }
    
    static public double getTotalSeconds(String filename) {
        double result = 0.0;
        QTAudioDecoder decoder = null;
        try {
        	getQuicktimeLock();
            decoder = new QTAudioDecoder(filename);
            result = decoder.getTotalSeconds();            
        } catch (java.lang.Error e) {
            log.debug("getTotalSeconds(): error Exception", e);
        } catch (Exception e) {
            log.debug("getTotalSeconds(): error Exception", e);
        } finally {
        	if (decoder != null)
        		decoder.close();
        	releaseQuicktimeLock();
        }
        return result;
    }
    
}
