package com.mixshare.rapid_evolution.audio.codecs;

import java.io.File;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.audio.AudioFileTypes;
import com.mixshare.rapid_evolution.audio.codecs.decoders.DefaultAudioDecoder;
import com.mixshare.rapid_evolution.audio.codecs.decoders.FAAD2AudioDecoder;
import com.mixshare.rapid_evolution.audio.codecs.decoders.FFMPEGAudioDecoder;
import com.mixshare.rapid_evolution.audio.codecs.decoders.QTAudioDecoder;
import com.mixshare.rapid_evolution.audio.codecs.decoders.XuggleAudioDecoder;
import com.mixshare.rapid_evolution.audio.exceptions.UnsupportedFileException;
import com.mixshare.rapid_evolution.audio.qt.QTUtil;
import com.mixshare.rapid_evolution.audio.util.AudioUtil;

public class QTAudioDecoderFactory implements AudioFileTypes {

	static private Logger log = Logger.getLogger(QTAudioDecoderFactory.class);
    
    public static AudioDecoder getAudioDecoder(String filename) {
        if (log.isTraceEnabled())
            log.trace("getAudioDecoder(): filename=" + filename);
        AudioDecoder decoder = null;
        try {
            File test_file = new File(filename);
            if (test_file.exists()) {                
                int audio_file_type = AudioUtil.getAudioFileType(filename);
                if ((audio_file_type == AUDIO_FILE_TYPE_MP4) || (audio_file_type == AUDIO_FILE_TYPE_AAC)) {
                	if (decoder == null) {
	                    if (QTUtil.isQuickTimeSupported()) {
	                        decoder = new QTAudioDecoder(filename);
	                        if (!decoder.isFileSupported()) {
	                            decoder.close();
	                            decoder = null;
	                        }
	                    }
                	}
                    if (decoder == null) {
	                	if (RE3Properties.getBoolean("enable_xuggle_codec")) {
		                    try {
		                    	decoder = new XuggleAudioDecoder(filename);
		                    	if (!decoder.isFileSupported()) {
		                    		decoder.close();
		                    		decoder = null;
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
	                    		}
	                    	} catch (UnsupportedFileException ufe) { }
                    	}
                    }
                } else {
                    if (decoder == null) {
	                	if (RE3Properties.getBoolean("enable_xuggle_codec")) {
		                    try {
		                    	decoder = new XuggleAudioDecoder(filename);
		                    	if (!decoder.isFileSupported()) {
		                    		decoder.close();
		                    		decoder = null;
		                    	}
		                    } catch (UnsupportedFileException ufe) { }
	                	}
                    }
                    if (decoder == null) {
                        if (QTUtil.isQuickTimeSupported()) {
                            decoder = new QTAudioDecoder(filename);
                            if (!decoder.isFileSupported()) {
                                decoder.close();                            
                                decoder = null;
                            }
                        }
                    }                    
                	if (decoder == null) {
	                	if (RE3Properties.getBoolean("enable_java_codecs")) {
		                    try {
		                        decoder = new DefaultAudioDecoder(filename);
		                        if (!decoder.isFileSupported()) {
		                            decoder.close();
		                            decoder = null;
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
        		log.debug("getAudioDecoder(): no available decoder for file=" + filename);
        }
        return decoder;
    }
    
}
