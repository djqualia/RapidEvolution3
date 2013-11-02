package com.mixshare.rapid_evolution.audio.converters.ffmpeg;

import java.io.File;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.audio.codecs.decoders.FFMPEGAudioDecoder;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.util.FileUtil;
import com.mixshare.rapid_evolution.util.OSHelper;
import com.mixshare.rapid_evolution.util.StringUtil;
import com.mixshare.rapid_evolution.util.io.StreamGobbler;

public class FFMPEGConverter {

    private static Logger log = Logger.getLogger(FFMPEGAudioDecoder.class);
	
	static public void convertAudio(String filename) {
		try {
			
            if (log.isDebugEnabled())
            	log.debug("convertAudio(): filename=" + filename);
            String extension = FileUtil.getExtension(filename);
            String new_filename = FileUtil.getFilenameMinusDirectory(filename);
            if (extension.length() > 0)
                new_filename = new_filename.substring(0, new_filename.length() - extension.length() - 1); 
            new_filename += "_" + String.valueOf((long)(Math.random() * 1000000));
            File dir = new File(OSHelper.getWorkingDirectory() + "/temp/");
            if (!dir.exists())
            	dir.mkdir();
            
            String wav_filename = OSHelper.getWorkingDirectory() + "/temp/" +  new_filename + ".wav";
            if (log.isDebugEnabled())
            	log.debug("FFMPEGAudioDecoder(): temporary wav filename=" + wav_filename);
            // convert to wav
            String command = RE3Properties.getProperty("ffmpeg_decode_command");
            command = StringUtil.replace(command, "%input%", "\"" + filename + "\"");
            command = StringUtil.replace(command, "%output%", "\"" + wav_filename + "\"");
            
            Process proc = Runtime.getRuntime().exec(command);
            // any error message?
            StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), log);
            // any output?
            StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), log);
            // kick them off
            errorGobbler.start();
            outputGobbler.start();                                    
            // any error???
            int exitVal = proc.waitFor();
            if (log.isDebugEnabled())
            	log.debug("FFMPEGAudioDecoder(): done converting to wav, exit value=" + exitVal);
            
		} catch (Exception e) {
			log.error("convertAudio(): error", e);
		}
	}

	
}
