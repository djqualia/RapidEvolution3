package com.mixshare.rapid_evolution.audio.codecs.decoders;

import java.io.File;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.audio.exceptions.UnsupportedFileException;
import com.mixshare.rapid_evolution.util.FileUtil;
import com.mixshare.rapid_evolution.util.OSHelper;
import com.mixshare.rapid_evolution.util.io.StreamGobbler;

// decodes mp4/aac files
public class FAAD2AudioDecoder extends DefaultAudioDecoder {

    private static Logger log = Logger.getLogger(FAAD2AudioDecoder.class);

    private String wav_filename = null;

    public FAAD2AudioDecoder(String filename) throws UnsupportedFileException {
        super();
        try {
            if (log.isDebugEnabled())
            	log.debug("FAAD2AudioDecoder(): filename=" + filename);
            String extension = FileUtil.getExtension(filename);
            String new_filename = FileUtil.getFilenameMinusDirectory(filename);
            if (extension.length() > 0)
                new_filename = new_filename.substring(0, new_filename.length() - extension.length() - 1);
            new_filename += "_" + String.valueOf((long)(Math.random() * 1000000));

            String tempDir = RE3Properties.getProperty("temp_working_directory");
            if ((tempDir == null) || (tempDir.length() == 0))
            	tempDir = OSHelper.getWorkingDirectory() + "/temp/";
            else {
            	if (!tempDir.endsWith("/") && !tempDir.endsWith("\\"))
            		tempDir += "/";
            }
            File dir = new File(tempDir);
            if (!dir.exists())
            	dir.mkdir();
            wav_filename = tempDir +  new_filename + ".wav";

            if (log.isDebugEnabled())
            	log.debug("FAAD2AudioDecoder(): temporary wav filename=" + wav_filename);
            // convert aac/mp4 to wav using faad
            String[] command = new String[4];
            if (OSHelper.getPlatform() == OSHelper.MACOS)
            	command[0] = "./faad_OSX";
            else if (OSHelper.getPlatform() == OSHelper.LINUX)
            	command[0] = "./faad";
            else
            	command[0] = "faad.exe";
            command[1] = "-o";
            command[2] = wav_filename;
            command[3] = filename;
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
            	log.debug("FAAD2AudioDecoder(): done converting to wav, exit value=" + exitVal);

            File outputCheck = new File(wav_filename);
            if (!outputCheck.exists())
            	throw new UnsupportedFileException();
            init(wav_filename);
            this.filename = filename;
        } catch (Exception e) {
        	if (log.isTraceEnabled())
        		log.trace("FAAD2AudioDecoder(): error Exception", e);
            throw new UnsupportedFileException();
        }
    }

    @Override
	public void close() {
        try {
            super.close();
            File file = new File(wav_filename);
            if (!file.delete()) {
            	if (log.isDebugEnabled())
            		log.debug("close(): can't immediately delete temp wav file=" + wav_filename);
            	file.deleteOnExit();
            }
        } catch (Exception e) {
            log.error("close(): error Exception", e);
        }
    }
}
