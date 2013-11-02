package com.mixshare.rapid_evolution.audio.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.apache.log4j.Logger;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.mp3.MP3AudioHeader;
import org.jaudiotagger.audio.mp3.MP3File;

import com.mixshare.rapid_evolution.audio.AudioFileTypes;
import com.mixshare.rapid_evolution.audio.PlaylistFileTypes;
import com.mixshare.rapid_evolution.audio.qt.QTUtil;
import com.mixshare.rapid_evolution.music.duration.Duration;
import com.mixshare.rapid_evolution.util.io.FileLockManager;
import com.mixshare.rapid_evolution.video.util.VideoUtil;

import davaguine.jmac.info.APEInfo;

public class AudioUtil implements AudioFileTypes, PlaylistFileTypes {

	static private Logger log = Logger.getLogger(AudioUtil.class);
	
    static public int getAudioFileType(String filename) {
        if (filename != null) {
            String lc_filename = filename.toLowerCase();
            if (lc_filename.endsWith(".mp3")) return AUDIO_FILE_TYPE_MP3;
            if (lc_filename.endsWith(".ogg")) return AUDIO_FILE_TYPE_OGG;
            if (lc_filename.endsWith(".flac")) return AUDIO_FILE_TYPE_FLAC;
            if (lc_filename.endsWith(".wav")) return AUDIO_FILE_TYPE_WAV;
            if (lc_filename.endsWith(".aif")) return AUDIO_FILE_TYPE_AIF;
            if (lc_filename.endsWith(".aiff")) return AUDIO_FILE_TYPE_AIF;
            if (lc_filename.endsWith(".mpc")) return AUDIO_FILE_TYPE_MPC;
            if (lc_filename.endsWith(".mp+")) return AUDIO_FILE_TYPE_MP_PLUS;
            if (lc_filename.endsWith(".ape")) return AUDIO_FILE_TYPE_APE;
            if (lc_filename.endsWith(".wma")) return AUDIO_FILE_TYPE_WMA;
            if (lc_filename.endsWith(".gsm")) return AUDIO_FILE_TYPE_GSM;
            if (lc_filename.endsWith(".mp4")) return AUDIO_FILE_TYPE_MP4;
            if (lc_filename.endsWith(".m4a")) return AUDIO_FILE_TYPE_MP4;
            if (lc_filename.endsWith(".m4p")) return AUDIO_FILE_TYPE_MP4;
            if (lc_filename.endsWith(".aac")) return AUDIO_FILE_TYPE_AAC;
            if (lc_filename.endsWith(".ra")) return AUDIO_FILE_TYPE_REALAUDIO;
            if (lc_filename.endsWith(".ram")) return AUDIO_FILE_TYPE_REALAUDIO;
            if (lc_filename.endsWith(".asf")) return AUDIO_FILE_TYPE_ASF;
        }
        return AUDIO_FILE_TYPE_UNKNOWN;
    }
    
    static public int getPlaylistFileType(String filename) {
        if (filename != null) {
            String lc_filename = filename.toLowerCase();
            if (lc_filename.endsWith(".m3u")) return PLAYLIST_FILE_TYPE_M3U;
            if (lc_filename.endsWith(".mix")) return PLAYLIST_FILE_TYPE_MIX;
        }
        return PLAYLIST_FILE_TYPE_UNKNOWN;
    }    
    
    static public boolean isSupportedAudioFileType(String filename) {
    	return getAudioFileType(filename) != AUDIO_FILE_TYPE_UNKNOWN; 
    }
    static public boolean isSupportedPlaylistFileType(String filename) {
    	return getPlaylistFileType(filename) != PLAYLIST_FILE_TYPE_UNKNOWN; 
    }
    
    static public String[] getSupportedAudioFileExtensions() {
        return new String[] { ".mp3", ".flac", ".ogg", ".wav", ".mp4", ".m4a", ".m4p", "aac", ".aif", ".aiff", ".mpc", ".mp+", ".ape",
                ".wma", "asf", ".gsm", "ra", "ram" };
    }
    
    static public String[] getSupportedPlaylistFileExtensions() {
    	return new String[] { ".m3u", ".mix" };
    }
	
    static private String getSupportedAudioFileExtensionsDescription() {
    	StringBuffer result = new StringBuffer();
    	result.append("(");
    	for (String extension : getSupportedAudioFileExtensions()) {
    		if (result.length() > 1)
    			result.append(" ");
    		result.append("*");
    		result.append(extension);
    	}
    	result.append(")");
    	return result.toString();
    }

    static private String getSupportedPlaylistFileExtensionsDescription() {
    	StringBuffer result = new StringBuffer();
    	result.append("(");
    	for (String extension : getSupportedPlaylistFileExtensions()) {
    		if (result.length() > 1)
    			result.append(" ");
    		result.append("*");
    		result.append(extension);
    	}
    	result.append(")");
    	return result.toString();
    }
    
    static public List<String> getAudioFilters() {
    	List<String> result = new ArrayList<String>();
    	result.add("ALL audio files " + getSupportedAudioFileExtensionsDescription());
    	result.add("MP3 files (*.mp3)");
    	result.add("MP4 files (*.mp4 *.m4p *.m4a *.aac)");
    	result.add("FLAC files (*.flac)");
    	result.add("OGG files (*.ogg)");
    	result.add("Waveform Audio files (*.wav)");
    	result.add("Monkey Audio files (*.ape)");
    	result.add("Musepack Audio files (*.mpc *.mp+)");
    	result.add("Audio Interchange files (*.aif *.aiff)");
    	result.add("Windows Media Audio files (*.wma *.asf)");
    	result.add("RealAudio files (*.ra *.ram)");
    	result.add("GSM files (*.gsm)");
    	return result;
    }
    
    static public List<String> getPlaylistFilters() {
    	List<String> result = new ArrayList<String>();
    	result.add("ALL playlist files " + getSupportedPlaylistFileExtensionsDescription());
    	result.add("M3U playlist files (*.m3u)");
    	result.add("RE2 MIX files (*.mix)");
    	return result;
    }
    
    static public SourceDataLine getLine(AudioFormat audioFormat) throws LineUnavailableException {
    	SourceDataLine res = null;
    	DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
    	res = (SourceDataLine) AudioSystem.getLine(info);
    	res.open(audioFormat);
    	return res;
    }    
	    
    static public Duration getDuration(String filename) {
        if ((filename == null) || (filename.equals("")))
        	return new Duration(0);
        Duration result = null;
        try {          
        	FileLockManager.startFileRead(filename);
        	if (VideoUtil.isSupportedVideoFileType(filename) && !AudioUtil.isSupportedAudioFileType(filename)) {
        		return VideoUtil.getDuration(filename);
        	}
        	if (result == null) {
	        	if (getAudioFileType(filename) == AUDIO_FILE_TYPE_OGG)
	        		result = readOggTime(filename);
	        	else if (getAudioFileType(filename) == AUDIO_FILE_TYPE_FLAC)
	        		result = readFlacTime(filename);
	        	else if (getAudioFileType(filename) == AUDIO_FILE_TYPE_MP4)
	        		result = readMP4Time(filename);
	        	else if (getAudioFileType(filename) == AUDIO_FILE_TYPE_APE)
	        		result = readAPETime(filename);
	        	else if (getAudioFileType(filename) == AUDIO_FILE_TYPE_MP3)
	        		result = readMP3Time(filename);        	
	        	else if (getAudioFileType(filename) == AUDIO_FILE_TYPE_WAV)
	        		result = readWAVTime(filename);        	
	        	else if (getAudioFileType(filename) == AUDIO_FILE_TYPE_WMA)
	        		result = readWMATime(filename);
        	}
        	if (((result == null) || !result.isValid()) && !VideoUtil.isSupportedVideoFileType(filename))
        		result = readSPITime(filename);
        	if (((result == null) || !result.isValid()) && VideoUtil.isSupportedVideoFileType(filename))
        		result = VideoUtil.getDuration(filename);        	
            if (((result == null) || !result.isValid()) && QTUtil.isQuickTimeSupported())
        		result = new Duration(QTUtil.getTotalSeconds(filename) * 1000);        	
        } catch (Exception e) {
        	log.error("getDuration(): error", e);
        } finally {
        	FileLockManager.endFileRead(filename);
        }
        if (log.isDebugEnabled())
        	log.debug("getDuration(): filename=" + filename + ", result=" + result);
        return result;
    }
    
    static private Duration readOggTime(String filename) {
        try {
        	AudioFile f = AudioFileIO.read(new File(filename));
        	AudioHeader audioHeader = f.getAudioHeader();
        	return new Duration(audioHeader.getTrackLength() * 1000);
        	/*
            JAudioMP4TagReader tagReader = new JAudioMP4TagReader(filename);
            double result = tagReader.getTimeInSeconds();
            if (result != 0.0)
                return new Duration(result * 1000);          
            
            File file = new File(filename);
            // Get AudioFileFormat from given file.
            AudioInputStream in = AudioSystem.getAudioInputStream(file);            
            AudioFileFormat baseFileFormat = AudioSystem.getAudioFileFormat(in);
            if (log.isTraceEnabled())
            	log.trace("readOggTime(): baseFileFormat=" + baseFileFormat + ", properties=" + baseFileFormat.properties());
            if (baseFileFormat instanceof TAudioFileFormat) {
            	Map<String, Object> props = ((TAudioFileFormat)baseFileFormat).properties();
            	if (props != null) {
            		Iterator<Entry<String, Object>> propsIter = props.entrySet().iterator();
            		while (propsIter.hasNext()) {
            			Entry<String, Object> entry = propsIter.next();
            			String id = entry.getKey();
            			Object value = entry.getValue();
            			if (log.isTraceEnabled())
            				log.trace("readOggTime(): id=" + id + ", value=" + value);
            		}                
            		// Length in seconds
            		return new Duration((((Long)props.get("duration")).doubleValue()) / 1000.0);
            	}
            }
            in.close();
            */
        } catch (Exception e) {
        	log.error("readOggTime(): error", e);
        }
        return null;
    }
    
    static private Duration readMP4Time(String filename) {
        try {
        	AudioFile f = AudioFileIO.read(new File(filename));
        	AudioHeader audioHeader = f.getAudioHeader();
        	return new Duration(audioHeader.getTrackLength() * 1000);
        	/*        	
            DefaultMP4TagReader reader = new DefaultMP4TagReader(filename);
            return new Duration(reader.getTimeInSeconds() * 1000);
            */
        } catch (org.jaudiotagger.audio.exceptions.CannotReadException cre) {
        	if (log.isDebugEnabled())
        		log.debug("readMP4Time(): cannot read exception=" + filename);        	
        } catch (Exception e) {
            log.error("readMP4Time(): error", e);
        }
        return null;
    }

    static private Duration readMP3Time(String filename) {
        try {
        	MP3File f = (MP3File)AudioFileIO.read(new File(filename));
        	MP3AudioHeader audioHeader = (MP3AudioHeader)f.getAudioHeader();
        	Duration result = new Duration(audioHeader.getTrackLength() * 1000);
        	return result;        	
        	/*
    		File file = new File(filename);
    		AudioInputStream in = null;          
    		try {
    			AudioFileFormat baseFileFormat = null;
    			in = AudioSystem.getAudioInputStream(file);            
    			baseFileFormat = AudioSystem.getAudioFileFormat(in);
    			if (baseFileFormat instanceof TAudioFileFormat)	{
    				Map<String, Object> properties = ((TAudioFileFormat)baseFileFormat).properties();
    				Long val = (Long) properties.get("duration");
    				result = new Duration(val.doubleValue() / 1000.0);
    			}
    			in.close();
    		} catch (Exception e) {
    			if (log.isTraceEnabled())
    				log.trace("getTrackTime(): exception", e);
    			if (in != null)
    				in.close();
    		}
    		if ((result == null) || !result.isValid()) {
    			FileInputStream inputStream = new FileInputStream(file);
    			Bitstream m_bitstream = new Bitstream(inputStream);
    			Header m_header = m_bitstream.readFrame();
    			double mediaLength = file.length();
    			double nTotalMS = 0.0;
    			if (mediaLength != AudioSystem.NOT_SPECIFIED)
    				nTotalMS = m_header.total_ms((int)mediaLength);
    			result = new Duration(nTotalMS);
    			inputStream.close();
    		}  
    		*/      
        } catch (org.jaudiotagger.audio.exceptions.InvalidAudioFrameException iaf) {
        	if (log.isDebugEnabled())
        		log.debug("readMP3Time(): no audio header found=" + filename);
        } catch (Exception e) {
            log.error("readMP3Time(): error", e);
        }
        return null;
    }
    
    static private Duration readWMATime(String filename) {
    	try {
        	AudioFile f = AudioFileIO.read(new File(filename));
        	AudioHeader audioHeader = f.getAudioHeader();
        	return new Duration(audioHeader.getTrackLength() * 1000);
    	} catch (Exception e) {
    		log.error("readWMATime(): error", e);
    	}
        return null;        	
    }
    
    static private Duration readWAVTime(String filename) {
    	try {
        	AudioFile f = AudioFileIO.read(new File(filename));
        	AudioHeader audioHeader = f.getAudioHeader();
        	return new Duration(audioHeader.getTrackLength() * 1000);
    	} catch (org.jaudiotagger.audio.exceptions.CannotReadException cre) {
    		log.debug("readWAVTime(): cannot read=" + cre);
    	} catch (Exception e) {
    		log.error("readWAVTime(): error", e);
    	}
        return null;        	
    }        
    
    static private Duration readFlacTime(String filename) {
    	try {
        	AudioFile f = AudioFileIO.read(new File(filename));
        	AudioHeader audioHeader = f.getAudioHeader();
        	return new Duration(audioHeader.getTrackLength() * 1000);
        	/*
    		File file = new File(filename);
    		FlacAudioFileReader reader = new FlacAudioFileReader();
    		FLACDecoder decoder = new FLACDecoder(reader.getAudioInputStream(file));          
    		StreamInfo streaminfo = decoder.readStreamInfo();
    		double seconds = ((double)streaminfo.getTotalSamples()) / streaminfo.getSampleRate();
    		return new Duration(seconds * 1000);
    		*/
    	} catch (Exception e) {
    		log.error("readFlacTime(): error", e);
    	}
        return null;        
    }

    static private Duration readAPETime(String filename) {
    	try {
    		APEInfo info = new APEInfo(new File(filename));
    		return new Duration(info.getApeInfoLengthMs());
    	} catch (Exception e) {
    		log.error("readAPETime(): error", e);
    	}
        return null;        
    }  

    static private Duration readSPITime(String filename) {
    	if (log.isTraceEnabled())
    		log.trace("readSPITime(): filename=" + filename);
    	Duration result = null;
    	AudioInputStream in = null;
    	try {
    		// try default java SPI
    		File file = new File(filename);
    		AudioFileFormat aff = AudioSystem.getAudioFileFormat(file);
    		if (log.isTraceEnabled())
    			log.trace("readSPITime(): aff=" + aff);
    		in = AudioSystem.getAudioInputStream(file);
    		AudioFormat baseFormat = in.getFormat();
    		if (log.isTraceEnabled())
    			log.trace("readSPITime(): baseFormat=" + baseFormat);
    		result = new Duration(aff.getFrameLength() * 1000.0f / baseFormat.getFrameRate());
    	} catch (javax.sound.sampled.UnsupportedAudioFileException usafe) {
    		if (log.isDebugEnabled())
    			log.debug("readSPITime(): unsupported audio file=" + filename);
    	} catch (java.io.IOException ioe) {
    		if (log.isDebugEnabled() && ioe.getMessage().equalsIgnoreCase("resetting to invalid mark"))
    			log.debug("readSPITime(): io exception, resetting to invalid mark");
    		else
    			log.error("readSPITime(): io exception=" + ioe);
    	} catch (Exception e) {
    		log.error("readSPITime(): error", e);
    	} finally {
    		if (in != null) {
    			try { in.close(); } catch (IOException ioe) { }
    			in = null;
    		}
    	}
        return result;        
    }      
}
