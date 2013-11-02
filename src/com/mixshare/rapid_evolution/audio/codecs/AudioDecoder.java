package com.mixshare.rapid_evolution.audio.codecs;

import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import java.io.InputStream;

import com.mixshare.rapid_evolution.audio.AudioBuffer;

public interface AudioDecoder {
    
    public boolean isFileSupported();
    
    public AudioBuffer getAudioBuffer();

    public AudioFormat getAudioFormat();
    
    public InputStream getDecodedInputStream();
    
    public double getFrameRate();
    
    public double getMaxFrequency();
    
    public double getSampleRate();
    
    public double getSecondsRead();
    
    public double getTotalSeconds();

    /**
     * Close the resources used by the decoder.
     */
    public void close();

    public long readBytes(byte[] bytes, int offset, int length) throws IOException;
    
    /**
     * A frame is a single set of samples for each channel, stored in the audio buffer.
     * 
     * @return number of frames read
     */
    public long readFrames(long num_frames) throws DecoderException;

    public long readNormalizedFrames(long num_frames)  throws DecoderException;
    
    /**
     * Resets the decoder to the beginning of the file.
     */
    public void reset();

    /**
     * @return number of frames skipped
     */
    public long skipFrames(long num_frames);
    
    public String getFilename();
        
}
