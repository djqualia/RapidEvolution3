package com.mixshare.rapid_evolution.audio.codecs.decoders;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.audio.AudioBuffer;
import com.mixshare.rapid_evolution.audio.codecs.AudioDecoder;
import com.mixshare.rapid_evolution.audio.codecs.CodecConstants;
import com.mixshare.rapid_evolution.audio.exceptions.UnsupportedFileException;
import com.mixshare.rapid_evolution.audio.util.AudioUtil;
import com.mixshare.rapid_evolution.audio.util.ByteToSampleReader;
import com.mixshare.rapid_evolution.music.duration.Duration;

// decodes mp3/ogg/flac/wav/aif
public class DefaultAudioDecoder implements AudioDecoder {

	static private Logger log = Logger.getLogger(DefaultAudioDecoder.class);
    
	////////////
	// FIELDS //
	////////////
	
    private boolean fileSupported = true;
    protected String filename = null;
    private double totalseconds = 0.0;
    protected AudioFormat audioFormat = null;
    protected AudioInputStream audioInputStream = null;
    protected AudioInputStream decodedInputStream = null;
    protected AudioBuffer audioBuffer = null;
    private int bytes_per_sample;
    private int bytes_per_frame;
    private byte[] internal_buffer = null;
    private long frames_processed = 0;
    private boolean skip_supported = false; // enabling this causes problems with encoded files
    
    ////////////
    // PUBLIC //
    ////////////
    
    public DefaultAudioDecoder() { }
    public DefaultAudioDecoder(String filename) throws UnsupportedFileException {
        init(filename);
    }
    
    /////////////
    // GETTERS //
    /////////////
    
    public boolean isFileSupported() {
        return fileSupported;
    }    
    
    public AudioBuffer getAudioBuffer() {
        return audioBuffer;
    }

    public AudioFormat getAudioFormat() {
        return audioFormat;
    }
    
    public AudioInputStream getDecodedInputStream() {
        return decodedInputStream;
    }
    
    public double getFrameRate() {
        return audioFormat.getFrameRate();
	}    

    public double getMaxFrequency() {
        return audioFormat.getSampleRate() / 2;
    }    
    
    public double getSampleRate() {
        return audioFormat.getSampleRate();
    }    
    
    public double getSecondsRead() {
        return ((double)frames_processed) / audioFormat.getSampleRate();        
    }
    
    public double getTotalSeconds() {    	    
        if (totalseconds == 0.0) {
            totalseconds = AudioUtil.getDuration(filename).getDurationInSeconds();
        	//try {
        		//AudioFileFormat aff = AudioSystem.getAudioFileFormat(new File(filename));
        		//totalseconds = new Duration(aff.getFrameLength() * 1000.0f / audioFormat.getFrameRate()).getDurationInSeconds();
        	//} catch (Exception ioe) {
        		//log.error("getTotalSeconds(): exception=" + ioe);
        	//}
        }
        return totalseconds;
    }
    
    public String getFilename() { return filename; }
    
    public long readBytes(byte[] bytes, int offset, int length) throws IOException {
        return decodedInputStream.read(bytes, offset, length);
    }
    
    public long readFrames(long num_frames) {
        return readFrames(num_frames, false);
    }

    public long readNormalizedFrames(long num_frames) {
        return readFrames(num_frames, true);
    }
    
    
	public long skipFrames(long num_frames) {
        long frames_skipped = 0;
        try {
            long length = Math.min(bytes_per_frame * num_frames, internal_buffer.length);
            long bytes_skipped = 0;
            long this_bytes_skipped = 0;
            try {
                if (skip_supported)
                    this_bytes_skipped = decodedInputStream.skip(length);
            } catch (Exception e) {
                if (log.isDebugEnabled()) log.debug("skipFrames(): frame skip not supported=" + e);
                skip_supported = false;                
            }
            if (!skip_supported) {
                this_bytes_skipped = decodedInputStream.read(internal_buffer, 0, (int)length);
            }
            while ((this_bytes_skipped != -1) && (bytes_skipped < length)) {
                bytes_skipped += this_bytes_skipped;
                if (skip_supported)
                    this_bytes_skipped = decodedInputStream.skip(length - bytes_skipped);
                else 
                    this_bytes_skipped = decodedInputStream.read(internal_buffer, (int)bytes_skipped, (int)(length - bytes_skipped));
            }                        
            frames_skipped = bytes_skipped / bytes_per_frame;
        } catch (Exception e) {
            log.error("readSamples(): error Exception", e);
        }
        frames_processed += frames_skipped;
        return frames_skipped;
	}    
    
	public void reset() {
    	if (log.isTraceEnabled())
    		log.trace("reset(): called");
        try {
            if (decodedInputStream.markSupported()) {
                decodedInputStream.reset();
            } else {
                close();
                openStream();
            }
        } catch (Exception e) {
            log.error("reset(): error Exception", e);
        }	    
	}
	
    public void close() {
        try {
        	if (log.isTraceEnabled()) {
        		log.trace("close(): # frames processed=" + frames_processed);
        	}        		
        	if (decodedInputStream != null) {
        		decodedInputStream.close();
        		decodedInputStream = null;
        	}
        	if (audioInputStream != null) {
        		audioInputStream.close();
        		audioInputStream = null;
        	}
        	audioBuffer = null;
        	audioFormat = null;
        } catch (Exception e) {
            log.error("close(): error Exception", e);
        }
    }
    
    protected void init(String filename) throws UnsupportedFileException {
        try {
            this.filename = filename;
            if (log.isDebugEnabled()) log.debug("init(): filename=" + filename);
            Duration duration = AudioUtil.getDuration(filename);
            if (duration != null) {	            
            	totalseconds = duration.getDurationInSeconds();
	            openStream();
	            audioBuffer = new AudioBuffer(CodecConstants.DEFAULT_FRAME_BUFFER_SIZE, audioFormat.getChannels());
	            bytes_per_sample = audioFormat.getSampleSizeInBits() / 8;
	            bytes_per_frame = audioFormat.getChannels() * bytes_per_sample;
	            internal_buffer = new byte[CodecConstants.DEFAULT_FRAME_BUFFER_SIZE * bytes_per_frame];	            
            } else {
            	throw new UnsupportedFileException();
            }
        } catch (UnsupportedFileException ufe) {
        	log.debug("init(): unsupported file=" + filename);
        	fileSupported = false;
        	throw new UnsupportedFileException();
        } catch (Exception e) {
            log.error("init(): error Exception", e);
            fileSupported = false;            
        }        
    }
    
    protected void openStream() throws UnsupportedFileException {
        try {
	        File file = new File(filename);
	        // open the initial audio input stream from the file
	        audioInputStream = AudioSystem.getAudioInputStream(file);	       
	        // get the source audio format
	        AudioFormat sourceFormat = audioInputStream.getFormat();
	        // set the decoded format to PCM_SIGNED
	        AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
	                sourceFormat.getSampleRate(),
	                16,
	                sourceFormat.getChannels(),
	                sourceFormat.getChannels() * 2,
	                sourceFormat.getSampleRate(),
	                false);
	        // get the decoded audio input stream
	        decodedInputStream = AudioSystem.getAudioInputStream(decodedFormat, audioInputStream);
	        audioFormat = decodedInputStream.getFormat();
            frames_processed = 0;
        } catch (IOException ioe) {
        	if (ioe.getMessage().equalsIgnoreCase("resetting to invalid mark"))
        		log.debug("openStream(): resetting to invalid mark for file=" + filename);
        	else
        		log.error("openStream(): io exception", ioe);
        	throw new UnsupportedFileException();
        } catch (javax.sound.sampled.UnsupportedAudioFileException uafe) {
            log.debug("openStream(): unsupported audio file=" + filename);
            throw new UnsupportedFileException();
        } catch (Exception e) {
            log.error("openStream(): error Exception", e);
            throw new UnsupportedFileException();
        }        
    }
    
    private long readFrames(long num_frames, boolean normalized) {
        int frames_read = 0;
        try {
            int length = (int)Math.min(bytes_per_frame * num_frames, internal_buffer.length);
            int bytes_read = 0;
            int this_bytes_read = decodedInputStream.read(internal_buffer, 0, length);
            while ((this_bytes_read != -1) && (bytes_read < length)) {
                bytes_read += this_bytes_read;
                this_bytes_read = decodedInputStream.read(internal_buffer, bytes_read, length - bytes_read);
            }
            int count = 0;
            while (count < bytes_read) {
              for (int channel = 0; channel < audioFormat.getChannels(); ++channel) {
                  double sample = normalized ?
                          ByteToSampleReader.getSampleFromBytesNormalized(internal_buffer, count, bytes_per_sample, audioFormat.isBigEndian())
                          : ByteToSampleReader.getSampleFromBytes(internal_buffer, count, bytes_per_sample, audioFormat.isBigEndian());
                  //if (log.isTraceEnabled())
                	  //log.trace("readFrames(): sample=" + sample);
                  audioBuffer.setSampleValue(frames_read, channel, sample);
                  count += bytes_per_sample;
              }
              ++frames_read;
            }       
        } catch (ArrayIndexOutOfBoundsException aiob) {
        	log.error("readSamples(): decoder error, array index out of bounds, filename=" + filename);
        } catch (Exception e) {
            log.error("readSamples(): error Exception", e);
        }
        frames_processed += frames_read;
        return frames_read;        
    }
}
