package com.mixshare.rapid_evolution.audio.tags;

import java.io.File;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.audio.AudioFileTypes;
import com.mixshare.rapid_evolution.audio.tags.writers.EntaggedTagWriter;
import com.mixshare.rapid_evolution.audio.tags.writers.JAudioGenericTagWriter;
import com.mixshare.rapid_evolution.audio.tags.writers.ape.JmacAPETagWriter;
import com.mixshare.rapid_evolution.audio.tags.writers.asf.JAudioASFTagWriter;
import com.mixshare.rapid_evolution.audio.tags.writers.flac.JAudioFlacTagWriter;
import com.mixshare.rapid_evolution.audio.tags.writers.mp3.JAudioTagWriter;
import com.mixshare.rapid_evolution.audio.tags.writers.mp3.JID3TagWriter;
import com.mixshare.rapid_evolution.audio.tags.writers.mp3.JavaID3TagWriter;
import com.mixshare.rapid_evolution.audio.tags.writers.mp4.JAudioMP4TagWriter;
import com.mixshare.rapid_evolution.audio.tags.writers.ogg.JAudioOggTagWriter;
import com.mixshare.rapid_evolution.audio.util.AudioUtil;

public class TagWriterFactory implements TagConstants, AudioFileTypes {

	static private Logger log = Logger.getLogger(TagWriterFactory.class);
    
	static public TagWriter getTagWriter(String filename) {
		TagWriter writer = null;
        File test_file = new File(filename);
        if (test_file.exists()) {            
	        int audio_file_type = AudioUtil.getAudioFileType(filename);
	        
	        if (audio_file_type == AUDIO_FILE_TYPE_MP3) {	        	
	        	int preferred_ID3_format = ID3_V_2_4;
	            if (RE3Properties.getProperty("preferred_id3_format").equalsIgnoreCase("2.3"))
	                preferred_ID3_format = ID3_V_2_3;    	            
	        	if (RE3Properties.getProperty("preferred_mp3_tag_writer_library").equalsIgnoreCase("jid3"))
                    writer = new JID3TagWriter(filename, TagConstants.TAG_MODE_UPDATE);                
	        	else if (RE3Properties.getProperty("preferred_mp3_tag_writer_library").equalsIgnoreCase("javaid3"))
                    writer = new JavaID3TagWriter(filename, TagConstants.TAG_MODE_UPDATE);                
                else
                    writer = new JAudioTagWriter(filename, TagConstants.TAG_MODE_UPDATE, preferred_ID3_format);	        
            } else if ((audio_file_type == AUDIO_FILE_TYPE_MP4) || (audio_file_type == AUDIO_FILE_TYPE_AAC)) {            	
                writer = new JAudioMP4TagWriter(filename, TagConstants.TAG_MODE_UPDATE);
            } else if (audio_file_type == AUDIO_FILE_TYPE_FLAC) {  
        		writer = new JAudioFlacTagWriter(filename, TagConstants.TAG_MODE_UPDATE);                
            } else if (audio_file_type == AUDIO_FILE_TYPE_OGG) {  
        		writer = new JAudioOggTagWriter(filename, TagConstants.TAG_MODE_UPDATE);                
	        } else if ((audio_file_type == AUDIO_FILE_TYPE_WMA) ||
                    (audio_file_type == AUDIO_FILE_TYPE_ASF)) {	            
	            writer = new JAudioASFTagWriter(filename, TagConstants.TAG_MODE_UPDATE);	        
	        } else if ((audio_file_type == AUDIO_FILE_TYPE_WAV) ||                    
	                   (audio_file_type == AUDIO_FILE_TYPE_REALAUDIO)) {	            
	            writer = new JAudioGenericTagWriter(filename, TagConstants.TAG_MODE_UPDATE);	        
            } else if (audio_file_type == AUDIO_FILE_TYPE_APE) {
	        	if (RE3Properties.getProperty("preferred_ape_tag_writer_library").equalsIgnoreCase("jmac"))
                    writer = new JmacAPETagWriter(filename);                
                else
                    writer = new EntaggedTagWriter(filename, TagConstants.TAG_MODE_UPDATE);	        
	        } else if ((audio_file_type == AUDIO_FILE_TYPE_MPC) ||
	                   (audio_file_type == AUDIO_FILE_TYPE_MP_PLUS)) {	            
	            writer = new EntaggedTagWriter(filename, TagConstants.TAG_MODE_UPDATE);
	        } else {
	            if (log.isDebugEnabled()) log.debug("getTagWriter(): no writer available for filename=" + filename);
	        }
        }
        return writer;
    }
    
}
