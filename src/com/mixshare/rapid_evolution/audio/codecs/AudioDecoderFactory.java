package com.mixshare.rapid_evolution.audio.codecs;

import java.io.File;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.audio.AudioFileTypes;
import com.mixshare.rapid_evolution.audio.codecs.decoders.DefaultAudioDecoder;
import com.mixshare.rapid_evolution.audio.codecs.decoders.FAAD2AudioDecoder;
import com.mixshare.rapid_evolution.audio.codecs.decoders.FFMPEGAudioDecoder;
import com.mixshare.rapid_evolution.audio.codecs.decoders.XuggleAudioDecoder;
import com.mixshare.rapid_evolution.audio.exceptions.UnsupportedFileException;
import com.mixshare.rapid_evolution.audio.qt.QTUtil;
import com.mixshare.rapid_evolution.audio.util.AudioUtil;

public class AudioDecoderFactory implements AudioFileTypes {

	static private Logger log = Logger.getLogger(AudioDecoderFactory.class);
    
	static public AudioDecoder getAudioDecoder(String filename) {
        if (log.isTraceEnabled())
            log.trace("getAudioDecoder(): filename=" + filename);
        try {
            if (QTUtil.isQuickTimeSupported())
                return QTAudioDecoderFactory.getAudioDecoder(filename);            
        } catch (java.lang.Error e) { 
        } catch (Exception e) { }
        AudioDecoder decoder = null;
        try {
            File test_file = new File(filename);
            if (test_file.exists()) {                
                int audio_file_type = AudioUtil.getAudioFileType(filename);
                if ((audio_file_type == AUDIO_FILE_TYPE_MP4) || (audio_file_type == AUDIO_FILE_TYPE_AAC)) {
                	if (decoder == null) {
	                	if (RE3Properties.getBoolean("enable_ffmpeg_codec")) {
		                    try {
		                    	decoder = new FFMPEGAudioDecoder(filename);
		                    	if (!decoder.isFileSupported()) {
		                    		decoder.close();
		                    		decoder = null;
		                    	} else {
		                    		if (log.isDebugEnabled())
		                    			log.debug("getAudioDecoder(): utilizing ffmpeg decoder");
		                    	}
		                    } catch (UnsupportedFileException ufe) { }
	                	}
                	}
                	if (decoder == null) {
	                	if (RE3Properties.getBoolean("enable_xuggle_codec")) {
		                    try {
		                    	decoder = new XuggleAudioDecoder(filename);
		                    	if (!decoder.isFileSupported()) {
		                    		decoder.close();
		                    		decoder = null;
		                    	} else {
		                    		if (log.isDebugEnabled())
		                    			log.debug("getAudioDecoder(): utilizing xuggle decoder");		                    		
		                    	}
		                    } catch (UnsupportedFileException ufe) { }
	                	}
                	}
                    if (decoder == null) {
                    	if (RE3Properties.getBoolean("enable_faad2_codec")) {
		                    try {
		                    	decoder = new FAAD2AudioDecoder(filename);
		                    	if (!decoder.isFileSupported()) {
		                    		decoder.close();
		                    		decoder = null;
		                    	} else {
		                    		if (log.isDebugEnabled())
		                    			log.debug("getAudioDecoder(): utilizing faad2 decoder");		                    		
		                    	}
		                    } catch (UnsupportedFileException ufe) { }
                    	}
                    }
                } else {
                	if (decoder == null) {
	                	if (RE3Properties.getBoolean("enable_java_codecs")) {
		                    try {
		                    	decoder = new DefaultAudioDecoder(filename);
		                    	if (!decoder.isFileSupported()) {
		                    		decoder.close();
		                    		decoder = null;
		                    	} else {
		                    		if (log.isDebugEnabled())
		                    			log.debug("getAudioDecoder(): utilizing default java decoder");		                    		
		                    	}
		                    } catch (UnsupportedFileException ufe) { }
	                	}
                	}
	                if (decoder == null) {
	                	if (RE3Properties.getBoolean("enable_ffmpeg_codec")) {
	                        try {
	                        	decoder = new FFMPEGAudioDecoder(filename);
	                        	if (!decoder.isFileSupported()) {
	                        		decoder.close();
	                        		decoder = null;
	                        	} else {
		                    		if (log.isDebugEnabled())
		                    			log.debug("getAudioDecoder(): utilizing ffmpeg decoder");	                        		
	                        	}
	                        } catch (UnsupportedFileException ufe) { }                    	
	                    }
                	}
                	if (decoder == null) {
	                	if (RE3Properties.getBoolean("enable_xuggle_codec")) {
		                    try {
		                    	decoder = new XuggleAudioDecoder(filename);
		                    	if (!decoder.isFileSupported()) {
		                    		decoder.close();
		                    		decoder = null;
		                    	} else {
		                    		if (log.isDebugEnabled())
		                    			log.debug("getAudioDecoder(): utilizing xuggle decoder");		                    		
		                    	}
		                    } catch (UnsupportedFileException ufe) { }
	                	}
                	}
                }
            }            
        } catch (Exception e) {
            log.error("getAudioDecoder(): error Exception", e);
        }
        if (decoder == null) {
        	if (log.isDebugEnabled())
        		log.debug("getAudioDecoder(): no decoder available for file=" + filename);
        }
        return decoder;
    }
    
}
