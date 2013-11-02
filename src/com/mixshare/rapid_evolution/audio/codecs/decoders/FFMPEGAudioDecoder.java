package com.mixshare.rapid_evolution.audio.codecs.decoders;

import java.io.File;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.audio.exceptions.UnsupportedFileException;
import com.mixshare.rapid_evolution.util.FileUtil;
import com.mixshare.rapid_evolution.util.OSHelper;
import com.mixshare.rapid_evolution.util.io.StreamGobbler;

// decodes mp4/aac files
public class FFMPEGAudioDecoder extends DefaultAudioDecoder {

    private static Logger log = Logger.getLogger(FFMPEGAudioDecoder.class);
    
    private String wav_filename = null;
    
    public FFMPEGAudioDecoder(String filename) throws UnsupportedFileException {
        super();
        try {
            if (log.isDebugEnabled())
            	log.debug("FFMPEGAudioDecoder(): filename=" + filename);
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
            	log.debug("FFMPEGAudioDecoder(): temporary wav filename=" + wav_filename);
            // convert to wav
            String command = RE3Properties.getProperty("ffmpeg_decode_command");
            if (!OSHelper.isWindows())
            	command = "./" + command;

            StringTokenizer commandTokens = new StringTokenizer(command, " ");
            String[] commandParts = new String[commandTokens.countTokens()];
            int i = 0;
            while (commandTokens.hasMoreTokens()) {
            	String commandPart = commandTokens.nextToken();
            	if (commandPart.equals("%input%"))
            		commandParts[i++] = filename;
            	else if (commandPart.equals("%output%"))
            		commandParts[i++] = wav_filename;
            	else
            		commandParts[i++] = commandPart;
            }
                        
            if (log.isDebugEnabled())
            	log.debug("FFMPEGAudioDecoder(): command=" + command + ", commandParts.length=" + commandParts.length);

            Process proc = Runtime.getRuntime().exec(commandParts);
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
            if (log.isTraceEnabled()) {
            	log.trace("FFMPEGAudioDecoder(): error output=" + errorGobbler.toString());
            	log.trace("FFMPEGAudioDecoder(): standard output=" + outputGobbler.toString());
            }            
            File outputCheck = new File(wav_filename);
            if (!outputCheck.exists())
            	throw new UnsupportedFileException();
            init(wav_filename);
            this.filename = filename;
        } catch (UnsupportedFileException e) {
        	if (log.isDebugEnabled())
        		log.debug("FFMPEGAudioDecoder(): unsupported file=" + filename);
        	close();
        	throw new UnsupportedFileException();
        } catch (Exception e) {
            log.error("FFMPEGAudioDecoder(): error Exception, current path=" + new File(".").getAbsolutePath(), e);
            close();
            throw new UnsupportedFileException();
        }
    }
	
    public void close() {
        try {
            super.close();
            if (wav_filename != null) {
	            File file = new File(wav_filename);
	            if (!file.delete()) {
	            	if (log.isDebugEnabled())
	            		log.debug("close(): can't immediately delete temp wav file=" + wav_filename);
	            	file.deleteOnExit();
	            }
            }
        } catch (Exception e) {
            log.error("close(): error Exception", e);
        }
    }
    
}
