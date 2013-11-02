package com.mixshare.rapid_evolution.video.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.audio.qt.QTUtil;
import com.mixshare.rapid_evolution.audio.xuggle.XuggleUtil;
import com.mixshare.rapid_evolution.music.duration.Duration;
import com.mixshare.rapid_evolution.util.io.FileLockManager;
import com.mixshare.rapid_evolution.video.VideoFileTypes;

public class VideoUtil implements VideoFileTypes {

	static private Logger log = Logger.getLogger(VideoUtil.class);
	
    static public int getVideoFileType(String filename) {
        if (filename != null) {
            String lc_filename = filename.toLowerCase();
            if (lc_filename.endsWith(".avi")) return VIDEO_FILE_TYPE_AVI;
            if (lc_filename.endsWith(".mp4")) return VIDEO_FILE_TYPE_MP4;
            if (lc_filename.endsWith(".m4v")) return VIDEO_FILE_TYPE_MP4;
            if (lc_filename.endsWith(".mkv")) return VIDEO_FILE_TYPE_MKV;
            if (lc_filename.endsWith(".mpg")) return VIDEO_FILE_TYPE_MPG;
            if (lc_filename.endsWith(".wmv")) return VIDEO_FILE_TYPE_WMV;
            if (lc_filename.endsWith(".flv")) return VIDEO_FILE_TYPE_FLV;
            if (lc_filename.endsWith(".mov")) return VIDEO_FILE_TYPE_MOV;
        }
        return VIDEO_FILE_TYPE_UNKNOWN;
    }
    
    static public boolean isSupportedVideoFileType(String filename) {
    	int videoType = getVideoFileType(filename);
    	return videoType != VIDEO_FILE_TYPE_UNKNOWN; 
    }
    
    static public String[] getSupportedVideoFileExtensions() {
        return new String[] { ".avi", ".mp4", ".m4v", ".mkv", ".mpg", ".wmv", ".flv", ".mov" };
    }
    
    static private String getSupportedVideoFileExtensionsDescription() {
    	StringBuffer result = new StringBuffer();
    	result.append("(");
    	for (String extension : getSupportedVideoFileExtensions()) {
    		if (result.length() > 1)
    			result.append(" ");
    		result.append("*");
    		result.append(extension);
    	}
    	result.append(")");
    	return result.toString();
    }
    
    static public List<String> getVideoFilters() {
    	List<String> result = new ArrayList<String>();
    	result.add("ALL video files " + getSupportedVideoFileExtensionsDescription());
    	result.add("MPG files (*.mpg)");
    	result.add("MP4 files (*.mp4 *.m4v)");
    	result.add("AVI files (*.avi)");
    	result.add("Matroska Video files (*.mkv)");
    	result.add("Windows Media Video files (*.wmv)");
    	result.add("Flash Video files (*.flv)");
    	result.add("Quicktime files (*.mov)");
    	return result;
    }    
    
    static public Duration getDuration(String filename) {
        if ((filename == null) || (filename.equals("")))
        	return new Duration(0);
        Duration result = null;
        try {          
        	FileLockManager.startFileRead(filename);
        
        	if (RE3Properties.getBoolean("video_util_enable_phonon_duration_check"))
        		result = QtVideoUtil.getDuration(filename);        	

    		if ((result == null) || !result.isValid()) {
    			if (RE3Properties.getBoolean("video_util_enable_quicktime_duration_check") && QTUtil.isQuickTimeSupported())
    				result = QuicktimeVideoUtil.getDuration(filename);    			
    		}    		
    		
    		if (RE3Properties.getBoolean("enable_xuggle_codec") && ((result == null) || !result.isValid())) {
    			result = XuggleUtil.getVideoDuration(filename);
    		}    		
        } catch (Exception e) {
        	log.error("getDuration(): error", e);
        } finally {
        	FileLockManager.endFileRead(filename);
        }
        if (log.isDebugEnabled())
        	log.debug("getDuration(): filename=" + filename + ", result=" + result);
        return result;
    }  
    
}
