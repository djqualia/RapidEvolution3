package com.mixshare.rapid_evolution.audio.codecs.decoders;

import java.io.File;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;

import org.apache.log4j.Logger;

import quicktime.QTNullPointerException;
import quicktime.io.OpenMovieFile;
import quicktime.io.QTFile;
import quicktime.io.QTIOException;
import quicktime.sound.SoundConstants;
import quicktime.std.StdQTConstants;
import quicktime.std.StdQTException;
import quicktime.std.movies.Movie;
import quicktime.std.movies.media.SoundDescription;
import quicktime.std.qtcomponents.MovieExporter;
import quicktime.util.QTHandle;
import quicktime.util.QTUtils;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.audio.AudioBuffer;
import com.mixshare.rapid_evolution.audio.codecs.AudioDecoder;
import com.mixshare.rapid_evolution.audio.codecs.CodecConstants;
import com.mixshare.rapid_evolution.audio.codecs.DecoderException;
import com.mixshare.rapid_evolution.audio.qt.QTSessionCheck;
import com.mixshare.rapid_evolution.audio.qt.QTUtil;
import com.mixshare.rapid_evolution.audio.util.ByteToSampleReader;
import com.mixshare.rapid_evolution.util.FileUtil;

public class QTAudioDecoder extends InputStream implements AudioDecoder, SoundConstants {

    private static Logger log = Logger.getLogger(QTAudioDecoder.class);    

    ////////////
    // FIELDS //
    ////////////
    
    private String filename;
    private Movie movie = null;
    private MovieExporter exporter = null;
    private AudioFormat audioFormat = null;
    private AudioBuffer audioBuffer = null;
    
    private int numChannels = 2;
    private int sampleRate = 44100;
    private int sampleSizeInBits = 16;
    
    private long frames_processed = 0;
    private int bytes_per_sample;
    private int bytes_per_frame;
    private byte[] internal_buffer;    
    private int total_bytes;
    
    private byte[] quicktime_buffer;
    private int quicktime_position = 0;
    private int quicktime_interval = 5000;
    private double totalSeconds = 0.0;
    
    private String originalFilename = null;
    private String newFilename;

    int totalBytesRead = 0;
    int position = 0;
    
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
    public QTAudioDecoder(String filename) {
        init(filename);
    }

    /////////////
    // GETTERS //
    /////////////
    
    public boolean isFileSupported() {
        return ((movie != null) && (exporter != null));
    }
    
    public AudioBuffer getAudioBuffer() {
        return audioBuffer;
    }

    public AudioFormat getAudioFormat() {
        return audioFormat;
    }
    
    public InputStream getDecodedInputStream() {
        return this;
    }
    
    public double getFrameRate() {
        return audioFormat.getFrameRate();
    }
    
    public double getMaxFrequency() {
        return sampleRate / 2;
    }
    
    public double getSampleRate() {
        return sampleRate;
    }
    
    public double getSecondsRead() {
        //return ((double)frames_processed) / audioFormat.getSampleRate();        
        return ((double)totalBytesRead) / total_bytes *  getTotalSeconds();       
    }
    
    public double getTotalSeconds() {        
        if (totalSeconds == 0.0) {
            totalSeconds = getTotalSeconds(movie);
        }
        return totalSeconds;
    }
    
    static public double getTotalSeconds(Movie movie) {
        double result = 0.0;
        if (movie != null) {
            try {                
                //if (log.isDebugEnabled()) {
                    //log.debug("getTotalSeconds(): timescale=" + movie.getTimeScale());
                    //log.debug("getTotalSeconds(): duration=" + movie.getDuration());
                //}
                result = (double)movie.getDuration() / movie.getTimeScale();
            } catch (Exception e) { }
        }
        //if (log.isDebugEnabled())
            //log.debug("getTotalSeconds(): result=" + result); 
        return result;
    }
    
    public String getFilename() { return filename; }
    
    /////////////
    // METHODS //
    /////////////
    
    private void init(String filename) {
        try {            
        	QTUtil.getQuicktimeLock();        	
            log.debug("init(): filename=" + filename);
            this.filename = filename;
            File file = new File(filename);
            if (file.exists()) {
                String name = FileUtil.getFilenameMinusDirectory(filename);
                if (RE3Properties.getBoolean("enable_quicktime_temporary_file_rename_fix")) {
	                if (name.length() > QTUtil.maxSupportedFilenameSize) {
	                    log.debug("init(): filename exceeds maximum supported length for quicktime=" + name);
	                    String extension = FileUtil.getExtension(filename);
	                    String newName = name.substring(0, QTUtil.maxSupportedFilenameSize - extension.length() - 1);
	                    newFilename = FileUtil.getDirectoryFromFilename(filename) + newName + "." + extension;
	                    log.debug("init(): temporarily renaming to=" + newFilename);
	                    if (file.renameTo(new File(newFilename))) {
	                        log.debug("init(): rename succeeded");
	                        originalFilename = filename;
	                        filename = newFilename;
	                    } else {
	                        log.debug("init(): file could not be renamed");
	                    }
	                }
                }
                QTSessionCheck.check();            
                QTFile qtf = new QTFile(new File(filename));
                OpenMovieFile omf = OpenMovieFile.asRead(qtf);
                movie = Movie.fromFile(omf);
                omf.close();
                if (movie != null) {
                    exporter = new MovieExporter(QTUtils.toOSType("snd "));
                    SoundDescription sd = new SoundDescription(0);
                    sd.setNumberOfChannels(numChannels);
                    sd.setSampleRate(sampleRate);
                    sd.setSampleSize(sampleSizeInBits);
                    sd.setDataFormat(SoundConstants.k16BitBigEndianFormat); //SoundConstants.k8BitOffsetBinaryFormat);
                    sd.lock();
                    exporter.setSampleDescription(sd, StdQTConstants.soundMediaType); 
                    audioFormat = new AudioFormat(sampleRate,
                            sampleSizeInBits,
                            numChannels,
                            true, // signed
                            true); // big endian
                    audioBuffer = new AudioBuffer(CodecConstants.DEFAULT_FRAME_BUFFER_SIZE, numChannels);
                    bytes_per_sample = audioFormat.getSampleSizeInBits() / 8;
                    bytes_per_frame = audioFormat.getChannels() * bytes_per_sample;
                    internal_buffer = new byte[CodecConstants.DEFAULT_FRAME_BUFFER_SIZE * bytes_per_frame];                
                    total_bytes = (int)(getFrameRate() * getTotalSeconds() * bytes_per_frame);
                }
            }
        } catch (QTIOException qtio) {
        	if (log.isDebugEnabled())
        		log.debug("init(): QT I/O exception)", qtio);
        } catch (StdQTException e) {
            int errorCode = e.errorCode();
            if (errorCode == -37) { // bdNameErr
                log.error("init(): qt error -37, bad name=" + filename);
            } else if (errorCode == -208) {
            	if (log.isDebugEnabled())
            		log.debug("init(): qt error -208, bad file format=" + filename);
            } else if (errorCode == -2048) {
            	if (log.isDebugEnabled())
            		log.debug("init(): qt error -2048, no movie found=" + filename);
            	
            } else {                
                log.error("init(): error Exception", e);
            }
            movie = null;
            exporter = null;
        } catch (java.lang.Error e) {
            log.error("init(): error Exception", e);
            movie = null;
            exporter = null;
        } catch (Exception e) {
            log.error("init(): error Exception", e);
            movie = null;
            exporter = null;
        }                
    }
    
    /**
     * Close the resources used by the decoder.
     */
    public void close() {
    	try {
	        if (originalFilename != null) {
	            QTSessionCheck.close();
	            log.debug("close(): renaming back to original=" + originalFilename);
	            File file = new File(newFilename);
	            if (file.renameTo(new File(originalFilename))) {                
	                log.debug("close(): rename succeeded");
	            } else {
	                log.error("close(): rename failed");
	            }
	        }
    	} catch (Exception e) {
    		log.error("close(): error", e);
    	} finally {
    		QTUtil.releaseQuicktimeLock();
    	}
    }

    private boolean checkBuffer() throws QTNullPointerException {
        try {
            if ((quicktime_buffer == null) || (position == quicktime_buffer.length)) {
                int duration = quicktime_interval;
                if (duration + quicktime_position > movie.getDuration()) duration -= (duration + quicktime_position - movie.getDuration());
                if (duration <= 0)
                    return false;
                QTHandle handle = exporter.toHandle(movie, null, quicktime_position, duration);
                quicktime_buffer = handle.getBytes();
                quicktime_position += quicktime_interval;
                position = 0;
            }
            return true;
        } catch (QTNullPointerException qtNull) {
        	log.error("checkBuffer(): quicktime exception" + qtNull);
        	throw qtNull;
        } catch (Exception e) {
            log.error("checkBuffer(): error Exception", e);
        }
        return true;
    }
    
    public long readBytes(byte[] bytes, int offset, int length) throws QTNullPointerException {
        // if (log.isTraceEnabled()) log.trace("readBytes(): bytes=" + bytes + ", offset=" + offset + ", length=" + length);
        int duration = 0;
        try {
            if (checkBuffer()) {
                int bytesToCopy = Math.min(length, quicktime_buffer.length - position);
                if (bytes != null) {
                    for (int i = offset; i < offset + bytesToCopy; ++i) {
                        bytes[i] = quicktime_buffer[position++];
                    }
                }
                totalBytesRead += bytesToCopy;
                // if (log.isTraceEnabled()) log.trace("readBytes(): bytesToCopy=" + bytesToCopy);
                return bytesToCopy;
            }
        } catch (QTNullPointerException qtNull) {
        	throw qtNull;
        } catch (Exception e) {
            log.error("readBytes(): error Exception, quicktime_position=" + quicktime_position + ", duration=" + duration, e);
        }
        return -1;
    }
    
    public long skipBytes(long length) {
        //if (log.isTraceEnabled())
            //log.trace("skipBytes(): length=" + length);
        try {
            position = 0;
            quicktime_buffer = null;
            long new_byte_position = totalBytesRead + length;
            double newSeconds = (double)new_byte_position / total_bytes * getTotalSeconds();
            int mark = (int)Math.floor(newSeconds * movie.getTimeScale());
            quicktime_position = mark;
            return length;
        } catch (Exception e) {
            log.error("skipBytes(): error Exception", e);
        }
        return -1;
    }
    
    /**
     * A frame is a single set of samples for each channel, stored in the audio buffer.
     * 
     * @return number of frames read
     */
    public long readFrames(long num_frames) throws DecoderException {
        return readFrames(num_frames, false);
    }

    public long readNormalizedFrames(long num_frames) throws DecoderException {
        return readFrames(num_frames, true);
    }
    
    /**
     * Resets the decoder to the beginning of the file.
     */
    public void reset() {
        quicktime_position = 0;
        quicktime_buffer = null;
    }

    /**
     * @return number of frames skipped
     */
    public long skipFrames(long num_frames) {
        return skip(num_frames * bytes_per_frame) / bytes_per_frame;
    }
        
    public int available() {
        return quicktime_buffer.length - position;
    }

    public void mark(int readlimit) {
        // TODO: code
    }

    /**
     * This determines if calls to reset() and mark() are valid
     */
    public boolean markSupported() {
        return false;        
    }

    public int read() throws QTNullPointerException {
        checkBuffer();
        return quicktime_buffer[position++];
    }

    public int read(byte[] b) throws QTNullPointerException  {
        return (int)readBytes(b, 0, b.length);
    }

    public int read(byte[] b, int off, int len) throws QTNullPointerException  {
        return (int)readBytes(b, off, len);
    }

    public long skip(long n) {
        return skipBytes(n);
    }

    
    private long readFrames(long num_frames, boolean normalized) throws DecoderException {
        int frames_read = 0;
        try {
            int length = (int)Math.min(bytes_per_frame * num_frames, internal_buffer.length);
            int bytes_read = 0;
            int this_bytes_read = read(internal_buffer, 0, length);
            while ((this_bytes_read != -1) && (bytes_read < length)) {
                bytes_read += this_bytes_read;
                this_bytes_read = read(internal_buffer, bytes_read, length - bytes_read);
            }
            int count = 0;
            while (count < bytes_read) {
              for (int channel = 0; channel < audioFormat.getChannels(); ++channel) {
                  double sample = normalized ?
                          ByteToSampleReader.getSampleFromBytesNormalized(internal_buffer, count, bytes_per_sample, audioFormat.isBigEndian())
                          : ByteToSampleReader.getSampleFromBytes(internal_buffer, count, bytes_per_sample, audioFormat.isBigEndian());
                  audioBuffer.setSampleValue(frames_read, channel, sample);
                  count += bytes_per_sample;
              }
              ++frames_read;
            }            
        } catch (QTNullPointerException qtNull) {
        	throw new DecoderException();
        } catch (Exception e) {
            log.error("readSamples(): error Exception", e);
        }
        frames_processed += frames_read;
        return frames_read;        
    }    
}
