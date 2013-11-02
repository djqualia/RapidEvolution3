package com.mixshare.rapid_evolution.audio.codecs.decoders;

import java.io.InputStream;
import java.util.Vector;

import javax.sound.sampled.AudioFormat;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.audio.AudioBuffer;
import com.mixshare.rapid_evolution.audio.codecs.AudioDecoder;
import com.mixshare.rapid_evolution.audio.codecs.CodecConstants;
import com.mixshare.rapid_evolution.audio.codecs.DecoderException;
import com.mixshare.rapid_evolution.audio.exceptions.UnsupportedFileException;
import com.mixshare.rapid_evolution.audio.player.javasound.JavaSoundPlayer;
import com.mixshare.rapid_evolution.audio.util.ByteToSampleReader;
import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.ICodec.Type;

public class XuggleAudioDecoder extends InputStream implements AudioDecoder {

    private static Logger log = Logger.getLogger(XuggleAudioDecoder.class);    

    ////////////
    // FIELDS //
    ////////////
    
    private boolean fileSupported = false;    
    private String filename;	
	private IContainer container;
	private int audioStreamId = -1;
	private IStream stream;
	private IStreamCoder audioCoder = null;
	private AudioFormat audioFormat;
    private IPacket packet;    
    private AudioBuffer audioBuffer = null;
    
    private byte[] xuggle_buffer;
    private double totalSeconds = 0.0;
    private int totalBytesRead = 0;
    private int position = 0;    
    private int bytes_per_sample;
    private int bytes_per_frame;
    private byte[] internal_buffer;    
    private long frames_processed = 0;
    private double total_seconds = 0.0;
    private long duration = 0;
    
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
    public XuggleAudioDecoder(String filename) throws UnsupportedFileException {
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
    
    public InputStream getDecodedInputStream() {
        return this;
    }
    
    public double getFrameRate() {
        return audioCoder.getFrameRate().getDouble();
    }
    
    public double getMaxFrequency() {
       return getSampleRate() / 2;
    }
    
    public double getSampleRate() {
        return audioCoder.getSampleRate();
    }

    public double getSecondsRead() {
        return ((double)frames_processed) / audioFormat.getSampleRate();
        //return ((double)audioCoder.getStream().getCurrentDts()) / audioCoder.getStream().getDuration() * getTotalSeconds();
    }
    
    private long getDuration() {
    	if (duration == 0)
    		duration = audioCoder.getStream().getDuration();
    	return duration;
    }
    
    public double getTotalSeconds() {
    	if (total_seconds == 0.0)
    		total_seconds = container.getDuration() / 1000000;
    		//total_seconds = getDuration() / audioCoder.getStream().getTimeBase().getDenominator();    	
    	return total_seconds;
    }   

    public String getFilename() { return filename; }
    
    /////////////
    // METHODS //
    /////////////
    
    private void init(String filename) throws UnsupportedFileException {
        try {            
            this.filename = filename;
			container = IContainer.make();
			if (container.open(filename, IContainer.Type.READ, null) < 0) {
				if (log.isDebugEnabled())
					log.debug("init(): unsupported file=" + filename);
				throw new UnsupportedFileException();
			} else {
				if (log.isDebugEnabled())
					log.debug("init(): filename=" + filename);
				int numStreams = container.getNumStreams();
				if (log.isTraceEnabled())
					log.trace("init(): # streams=" + numStreams);				
				for (int i = 0; i < container.getNumStreams(); ++i) {
					stream = container.getStream(i);
					IStreamCoder coder = stream.getStreamCoder();
					if (log.isTraceEnabled())
						log.trace("init(): \tfound stream type=" + coder.getCodecType());					
					if (coder.getCodecType() == Type.CODEC_TYPE_AUDIO) {
						audioStreamId = i;
						audioCoder = stream.getStreamCoder();
						if (audioCoder.open() >= 0) {
						    audioFormat = new AudioFormat(audioCoder.getSampleRate(),
						            (int)IAudioSamples.findSampleBitDepth(audioCoder.getSampleFormat()),
						            audioCoder.getChannels(),
						            true, /* xuggler defaults to signed 16 bit samples */
						            false);
						    audioBuffer = new AudioBuffer(CodecConstants.DEFAULT_FRAME_BUFFER_SIZE, audioFormat.getChannels());
		                    bytes_per_sample = audioFormat.getSampleSizeInBits() / 8;
						    bytes_per_frame = audioFormat.getChannels() * bytes_per_sample;
						    internal_buffer = new byte[CodecConstants.DEFAULT_FRAME_BUFFER_SIZE * bytes_per_frame];
				    	    packet = IPacket.make();						    	
					      	fileSupported = true;						      
						}
						break;
					}
				}
			}
        } catch (java.lang.NoClassDefFoundError ncdf) {   
        	if (log.isDebugEnabled())        
        		log.debug("init(): xuggle class def not found");
        	fileSupported = false;
        } catch (java.lang.Error e) {
            log.error("init(): error Exception", e);
        } catch (Exception e) {
            log.error("init(): error Exception", e);
        }      
        if (!fileSupported) {
        	close();
        	throw new UnsupportedFileException();
        }
    }
    
    /**
     * Close the resources used by the decoder.
     */
    public void close() {
    	try {
        	if (log.isTraceEnabled()) {
        		log.trace("close(): # frames processed=" + frames_processed);
        	}        		
	        if (audioCoder != null) {
	        	audioCoder.close();
	        	audioCoder = null;
	        }
	        if (container !=null) {
	        	container.close();
	        	container = null;
	        }
    	} catch (Exception e) {
    		log.error("close(): error", e);
    	} finally { }
    }

    private byte[] getNextPacketBytes() {
    	Vector<Byte> result = new Vector<Byte>(4096);
		int packetReadResult = 0;		
		do {
			try {
    			packetReadResult = container.readNextPacket(packet);
    			if (packetReadResult >= 0) {

    				// Now we have a packet, let's see if it belongs to our audio stream
	    	    	if (packet.getStreamIndex() == audioStreamId) {
		    	        /*
		    	         * We allocate a set of samples with the same number of channels as the
		    	         * coder tells us is in this buffer.
		    	         * 
		    	         * We also pass in a buffer size (1024 in our example), although Xuggler
		    	         * will probably allocate more space than just the 1024 (it's not important why).
		    	         */
		    	        IAudioSamples samples = IAudioSamples.make(1024, audioCoder.getChannels());
	    	        
		    	        /*
		    	         * A packet can actually contain multiple sets of samples (or frames of samples
		    	         * in audio-decoding speak).  So, we may need to call decode audio multiple
		    	         * times at different offsets in the packet's data.  We capture that here.
		    	         */
		    	        int offset = 0;
	    	        
		    	        /*
		    	         * Keep going until we've processed all data
		    	         */
		    	        while (offset < packet.getSize()) {		    	        			    	     
		    	        	int bytesDecoded = audioCoder.decodeAudio(samples, packet, offset);		    	        	
		    	        	if (bytesDecoded < 0) {
			    	        	if (log.isDebugEnabled())
			    	        		log.debug("run(): got error decoding audio in= " + filename);
		    	        		return null;		    	        		
		    	        	}
		    	        	offset += bytesDecoded;
		    	          
		    	        	/*
		    	        	 * Some decoder will consume data in a packet, but will not be able to construct
		    	        	 * a full set of samples yet.  Therefore you should always check if you
		    	        	 * got a complete set of samples from the decoder
		    	        	 */
		    	        	if (samples.isComplete()) {
		    	        		// TODO: accommodate for possible looping of offset? could this be reached multiple times within a packet?
		    	        		byte[] rawBytes = samples.getData().getByteArray(0, samples.getSize());
		    	        		for (byte rawByte : rawBytes)
		    	        			result.add(rawByte);
		    	        	}
		    	        }
	    	    	} else {
	    	    		// This packet isn't part of our audio stream, so we just silently drop it.
	    	    	}
	    	    	
	    	    	if (result.size() >= 0) {
	    	    		byte[] byteResult = new byte[result.size()];
	    	    		int i = 0;
	    	    		for (Byte resultByte : result)
	    	    			byteResult[i++] = resultByte;
	    	    		return byteResult;			    	    		
	    	    	}
    			}
			} catch (Exception e) {
				log.error("run(): error during decode loop", e);
			}
		} while (packetReadResult >= 0); 
		byte[] byteResult = new byte[result.size()];
		int i = 0;
		for (Byte resultByte : result)
			byteResult[i++] = resultByte;
		return byteResult;		
    }
    
    private boolean checkBuffer() {
        try {
        	boolean ended = false;
        	int maxLoops = 100;
        	int loopCount = 0;
            while (((xuggle_buffer == null) || (position == xuggle_buffer.length)) && !ended) {
                xuggle_buffer = getNextPacketBytes();
                position = 0;
                if ((xuggle_buffer == null)) {
                	if (log.isTraceEnabled())
                		log.trace("checkBuffer(): no packets returned, xuggle_buffer is null");                	
                	return false;
                } else {
                	if (log.isTraceEnabled())
                		log.trace("checkBuffer(): " + xuggle_buffer.length + " packets returned");
                	if (xuggle_buffer.length == 0) {
                		if (audioCoder.getStream().getCurrentDts() >= audioCoder.getStream().getDuration())
                			ended = true;
                		else if (getSecondsRead() + 1.0 > getTotalSeconds())
                			ended = true;
                		else {
                			if (log.isTraceEnabled())
                				log.trace("checkBuffer(): looping, seconds read=" + getSecondsRead() + ", total seconds=" + getTotalSeconds());
                			
                		}                		
                	}
                }
                ++loopCount;
                if (loopCount >= maxLoops)
                	return false;
            }
            return true;
        } catch (Exception e) {
            log.error("checkBuffer(): error Exception", e);
        }
        return true;
    }
    
    public long readBytes(byte[] bytes, int offset, int length) {
        // if (log.isTraceEnabled()) log.trace("readBytes(): bytes=" + bytes + ", offset=" + offset + ", length=" + length);
        int duration = 0;
        try {
            if (checkBuffer()) {
            	if (xuggle_buffer.length == 0)
            		return -1;
                int bytesToCopy = Math.min(length, xuggle_buffer.length - position);
                if (bytes != null) {
                    for (int i = offset; i < offset + bytesToCopy; ++i) {
                        bytes[i] = xuggle_buffer[position++];
                    }
                } else {                	
                	position += bytesToCopy;
                }
                totalBytesRead += bytesToCopy;
                // if (log.isTraceEnabled()) log.trace("readBytes(): bytesToCopy=" + bytesToCopy);
                return bytesToCopy;
            }
        } catch (ArrayIndexOutOfBoundsException aiofb) {
        	log.error("readBytes(): array index out of bounds=" + aiofb + ", offset=" + offset + ", length=" + length + ", xuggle_buffer_length=" + xuggle_buffer.length + ", totalBytesRead=" + totalBytesRead);
        } catch (Exception e) {
            log.error("readBytes(): error Exception", e);
        }
        return -1;
    }
    
    public long skipBytes(long length) {
        try {
        	// TODO: more efficient skipping using seek
        	return readBytes(null, 0, (int)length);
        } catch (Exception e) {
            log.error("skipBytes(): error", e);
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
    	if (log.isTraceEnabled())
    		log.trace("reset(): called");
        xuggle_buffer = null;
        position = 0;
    	container.seekKeyFrame(audioStreamId, 0, 0, stream.getDuration(), 0);        
    }

    /**
     * @return number of frames skipped
     */
    public long skipFrames(long num_frames) {
    	if (log.isTraceEnabled())
    		log.trace("skipFrames(): # frames=" + num_frames);
    	// TODO: implement using seek
    	return readFrames(num_frames, false);
        //return skip(num_frames * bytes_per_frame) / bytes_per_frame;
    }
        
    public int available() {
    	return xuggle_buffer.length - position;
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

    public int read() {
        checkBuffer();
        return xuggle_buffer[position++];
    }

    public int read(byte[] b) {
        return (int)readBytes(b, 0, b.length);
    }

    public int read(byte[] b, int off, int len) {
        return (int)readBytes(b, off, len);
    }

    public long skip(long n) {
        return skipBytes(n);
    }
    
    private long readFrames(long num_frames, boolean normalized) {
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
                  //if (log.isTraceEnabled())
                	  //log.trace("readFrames(): sample=" + sample);
                  audioBuffer.setSampleValue(frames_read, channel, sample);
                  count += bytes_per_sample;
              }
              ++frames_read;
            }            
        } catch (Exception e) {
            log.error("readSamples(): error Exception", e);
        }
        frames_processed += frames_read;
        return frames_read;  
    }    

    static public void main(String[] args) {
    	try {
    		// a little test routine
    		RapidEvolution3.loadLog4J();
    		JavaSoundPlayer player = new JavaSoundPlayer();
    		player.open("C:\\Users\\Jesse\\Desktop\\new music\\Smells-like-teen-spirit-Dual-Remix.mp3");
    		if (player.isFileSupported()) {
    			log.info("duration=" + player.getTotalTime() + "s");
    			player.start();
    			log.info("started");
    			Thread.sleep(5000);
    			player.pause();
    			log.info("paused");
    			Thread.sleep(1000);
    			player.start();
    			log.info("started");
    			Thread.sleep(5000);
    			log.info("seeked");
    			player.setPosition(0.5);
    			Thread.sleep(5000);
    			log.info("seeked2");
    			player.setPosition(0.1);
    			Thread.sleep(5000);
    			
    			log.info("volume 0.5");
    			player.setVolume(0.5);
    			Thread.sleep(5000);
    			log.info("stopped");
    			player.stop();
    		} else {
    			log.info("unsupported");
    		}
    	} catch (Exception e) {
    		log.error("main(): error", e);
    	}
    }    
}
