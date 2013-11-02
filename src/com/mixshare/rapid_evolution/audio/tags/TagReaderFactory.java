package com.mixshare.rapid_evolution.audio.tags;

import java.io.File;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.audio.AudioFileTypes;
import com.mixshare.rapid_evolution.audio.qt.QTUtil;
import com.mixshare.rapid_evolution.audio.tags.readers.DefaultTagReader;
import com.mixshare.rapid_evolution.audio.tags.readers.EntaggedTagReader;
import com.mixshare.rapid_evolution.audio.tags.readers.JAudioGenericTagReader;
import com.mixshare.rapid_evolution.audio.tags.readers.ape.JmacAPETagReader;
import com.mixshare.rapid_evolution.audio.tags.readers.asf.JAudioASFTagReader;
import com.mixshare.rapid_evolution.audio.tags.readers.flac.JAudioFlacTagReader;
import com.mixshare.rapid_evolution.audio.tags.readers.mp3.JAudioTagReader;
import com.mixshare.rapid_evolution.audio.tags.readers.mp3.JID3TagReader;
import com.mixshare.rapid_evolution.audio.tags.readers.mp3.JavaID3TagReader;
import com.mixshare.rapid_evolution.audio.tags.readers.mp3.JavaZoomTagReader;
import com.mixshare.rapid_evolution.audio.tags.readers.mp3.QTID3TagReader;
import com.mixshare.rapid_evolution.audio.tags.readers.mp4.JAudioMP4TagReader;
import com.mixshare.rapid_evolution.audio.tags.readers.mp4.QTAACTagReader;
import com.mixshare.rapid_evolution.audio.tags.readers.ogg.JAudioOggTagReader;
import com.mixshare.rapid_evolution.audio.util.AudioUtil;

public class TagReaderFactory implements AudioFileTypes {

    private static Logger log = Logger.getLogger(TagReaderFactory.class);
    
    static public TagReader getTagReader(String filename) {
        TagReader reader = null;
        File test_file = new File(filename);
        if (test_file.exists()) {                
            int audio_file_type = AudioUtil.getAudioFileType(filename);
            if (audio_file_type == AUDIO_FILE_TYPE_MP3) {
                if (RE3Properties.getProperty("preferred_mp3_tag_reader_library").equalsIgnoreCase("jid3")) {
                    reader = new JID3TagReader(filename);
                    if (!reader.isFileSupported())
                        reader = null;
                } else if (RE3Properties.getProperty("preferred_mp3_tag_reader_library").equalsIgnoreCase("quicktime") && QTUtil.isQuickTimeSupported()) {
                    reader = new QTID3TagReader(filename);
                    if (!reader.isFileSupported())
                        reader = null;
                } else if (RE3Properties.getProperty("preferred_mp3_tag_reader_library").equalsIgnoreCase("javaid3")) {
                    reader = new JavaID3TagReader(filename);
                    if (!reader.isFileSupported())
                        reader = null;
                } else if (RE3Properties.getProperty("preferred_mp3_tag_reader_library").equalsIgnoreCase("javazoom")) {
                    reader = new JavaZoomTagReader(filename);
                    if (!reader.isFileSupported())
                        reader = null;
                }
                if (reader == null)
                    reader = new JAudioTagReader(filename);
 	        } else if ((audio_file_type == AUDIO_FILE_TYPE_AAC) || (audio_file_type == AUDIO_FILE_TYPE_MP4)) {
	            if (RE3Properties.getProperty("preferred_mp4_tag_reader_library").equalsIgnoreCase("quicktime")) {
                    reader = new QTAACTagReader(filename);
                    if (!reader.isFileSupported())
                        reader = null;
                }
	            if (reader == null)
	                reader = new JAudioMP4TagReader(filename);
            } else if ((audio_file_type == AUDIO_FILE_TYPE_FLAC)) {
                reader = new JAudioFlacTagReader(filename);
            } else if ((audio_file_type == AUDIO_FILE_TYPE_OGG)) {
                reader = new JAudioOggTagReader(filename);
	        } else if ((audio_file_type == AUDIO_FILE_TYPE_WMA) ||
                    (audio_file_type == AUDIO_FILE_TYPE_ASF)) {
	        	reader = new JAudioASFTagReader(filename);
	        } else if ((audio_file_type == AUDIO_FILE_TYPE_WAV) ||                    
	                   (audio_file_type == AUDIO_FILE_TYPE_REALAUDIO)) {	            
	        	reader = new JAudioGenericTagReader(filename);
            } else if ((audio_file_type == AUDIO_FILE_TYPE_APE)) {
	            if (RE3Properties.getProperty("preferred_ape_tag_reader_library").equalsIgnoreCase("jmac")) {
                    reader = new JmacAPETagReader(filename);
                    if (!reader.isFileSupported())
                        reader = null;
                }
	            if (reader == null)
	            	reader = new EntaggedTagReader(filename);
            } else if ((audio_file_type == AUDIO_FILE_TYPE_MPC) ||
                    (audio_file_type == AUDIO_FILE_TYPE_MP_PLUS)) {
            	reader = new EntaggedTagReader(filename);
            } else {
                if (log.isDebugEnabled()) log.debug("getTagReader(): no specific reader available for filename=" + filename);
                reader = new DefaultTagReader(filename);
            }
        }
        return reader;
    }
    
}
