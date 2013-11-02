package com.mixshare.rapid_evolution.audio.player.xuggle;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.audio.exceptions.UnsupportedFileException;
import com.mixshare.rapid_evolution.music.duration.Duration;
import com.mixshare.rapid_evolution.player.PlayerCallBack;
import com.mixshare.rapid_evolution.player.PlayerInterface;
import com.mixshare.rapid_evolution.util.io.FileLockManager;
import com.mixshare.rapid_evolution.util.timing.Semaphore;
import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.ICodec.Type;

public class XugglePlayer implements PlayerInterface {

	static private Logger log = Logger.getLogger(XugglePlayer.class);
    
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
	private SourceDataLine mLine;	
	private PlayerThread player;
	private FloatControl volumeControl;
    private PlayerCallBack callBack;
    private String lastCallBackTime;
    private boolean stopFlag;
    private boolean isClosed;
    private Semaphore nativeLock = new Semaphore(1); // not sure if this is completely necessary but attempting to fix intermittent issues with seek
    private IPacket packet;    
    
    /////////////
    // METHODS //
    /////////////
    
    public boolean isFileSupported() { return fileSupported; }
    
    /**
     * @return true if it is able to open and play the filename
     */
    public boolean open(String filename) {
        try {
            this.filename = filename;
            FileLockManager.startFileRead(filename);
			container = IContainer.make();
			if (container.open(filename, IContainer.Type.READ, null) < 0) {
				if (log.isDebugEnabled())
					log.debug("open(): unsupported file=" + filename);
				throw new UnsupportedFileException();
			} else {
				if (log.isDebugEnabled())
					log.debug("open(): filename=" + filename);
				int numStreams = container.getNumStreams();
				if (log.isTraceEnabled())
					log.trace("open(): # streams=" + numStreams);				
				for (int i = 0; i < container.getNumStreams(); ++i) {
					stream = container.getStream(i);
					IStreamCoder coder = stream.getStreamCoder();
					if (log.isTraceEnabled())
						log.trace("open(): \tfound stream type=" + coder.getCodecType());					
					if (coder.getCodecType() == Type.CODEC_TYPE_AUDIO) {
						audioStreamId = i;
						audioCoder = stream.getStreamCoder();
						if (audioCoder.open() >= 0) {
						    audioFormat = new AudioFormat(audioCoder.getSampleRate(),
						            (int)IAudioSamples.findSampleBitDepth(audioCoder.getSampleFormat()),
						            audioCoder.getChannels(),
						            true, /* xuggler defaults to signed 16 bit samples */
						            false);
						    DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
						    try {
						    	mLine = (SourceDataLine) AudioSystem.getLine(info);						      
						    	// if that succeeded, try opening the line.
						    	mLine.open(audioFormat);
						    	// And if that succeed, start the line.
						    	mLine.start();
						    	volumeControl = (FloatControl)mLine.getControl(FloatControl.Type.MASTER_GAIN);
					    	    packet = IPacket.make();						    	
						      	fileSupported = true;						      
						    } catch (LineUnavailableException e) {
						    	log.debug("open(): could not open audio line");
						    	throw new UnsupportedFileException();
						    }
						}
						break;
					}
				}
			}
        } catch (java.lang.NoClassDefFoundError ncdfe) {
        	log.debug("init(): xuggle classes not found");
        	fileSupported = false;
        } catch (UnsupportedFileException ufe) {
        	log.debug("init(): unsupported file=" + filename);
        	fileSupported = false;
        } catch (java.lang.UnsatisfiedLinkError ule) {
        	log.debug("xuggle libraries not find (unsatisfied link)");
        	fileSupported = false;
        } catch (Error e) {
        	log.error("init(): Error", e);
        	fileSupported = false;
        } catch (Exception e) {
            log.error("init(): error Exception", e);
            fileSupported = false;            
        }      
        return fileSupported;
    }
    
    public void start() {
        stopFlag = false;
        if (player == null) {
        	player = new PlayerThread();
        	player.start();
        }
        //if (!player.isRunning())
        	//player.start();
    }
    
    public void pause() {
        stopFlag = true;
    }
    
    public boolean isPlaying() {
        return ((player != null) && !stopFlag);
    }
    
    public void stop() {
    	pause();
    	setPosition(0.0);
    }
    
    public void close() {
    	try {
	        if (mLine != null) {
	        	// Wait for the line to finish playing
	        	mLine.drain();
	        	mLine.close();
	        	mLine = null;
	        }        
	        if (audioCoder != null) {
	        	audioCoder.close();
	        	audioCoder = null;
	        }
	        if (container !=null) {
	        	container.close();
	        	container = null;
	        }
    	} catch (Error e) {
    		log.error("close(): Error=" + e);
    	} catch (Exception e) {
    		log.error("close(): error", e);
    	} finally {
    		isClosed = true;
    		FileLockManager.endFileRead(filename);
    	}
    }
    
    public boolean hasVideo() { return false; }
    
    /**
     * @return double in seconds
     */
    public double getTotalTime() {     	
    	//return ((double)audioCoder.getStream().getDuration()) / audioCoder.getStream().getTimeBase().getDenominator();
    	return container.getDuration() / 1000000.0;
    }
    
    /**
     * Where percentage is between 0 and 1.  The player should obey
     * the current running state.  I.e. if it is not playing when this is
     * called, then it won't be playing after, and vice versa.
     */
    public void setPosition(double percentage) {
    	try {
    		nativeLock.acquire();
	    	if (container.seekKeyFrame(audioStreamId, 0, (long)(percentage * stream.getDuration()), stream.getDuration(), 0) >= 0) {
	    		// success
	    	} else {
	    		float currentPercentage = stream.getCurrentDts() / stream.getDuration();
	    		if (log.isDebugEnabled())
	    			log.debug("setPosition(): seek failed, percentage=" + percentage + ", currentPercentage=" + currentPercentage);
	    		if (callBack != null) {
	    			callBack.setPosition(currentPercentage);
	    		}
	    	}
    	} catch (Exception e) {
    		log.error("setPosition(): error", e);
    	} finally {
    		nativeLock.release();
    	}
    }
    
    public void setVolume(double percentage) {
        if (volumeControl != null) {
        	if (log.isTraceEnabled())
        		log.trace("setVolume(): max=" + volumeControl.getMaximum() + ", min=" + volumeControl.getMinimum());
        	float max = Math.min(volumeControl.getMaximum(), 0.0f);
        	float min = Math.max(volumeControl.getMinimum(), -20.0f);
            float range = max - min;         
            if (percentage == 0.0)
            	volumeControl.setValue(volumeControl.getMinimum());
            else
            	volumeControl.setValue((float)(range * percentage + min));
        }
    }
    
    public void setCallBack(PlayerCallBack callBack) {
        this.callBack = callBack;
    }
    
    private class PlayerThread extends Thread {
    	public PlayerThread() {
    		setDaemon(true);
    		setPriority(MAX_PRIORITY);
    	}
        private boolean stopped = false;
        public boolean setStopped() { return stopped = true; }
        
    	public void run() {
    		try {
	            if (callBack != null)
	                callBack.setIsPlaying(true);
	        		
        		int packetReadResult = 0;
        		do {
        			try {
        				nativeLock.acquire();
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
				    	        	while (stopFlag && !isClosed) {
				    	        		nativeLock.release();
				    	        		Thread.sleep(10);
				    	        		nativeLock.acquire();
				    	        	}
				    	        	if (isClosed || stopped)
				    	        		return;
				    	        			    	     
				    	        	int bytesDecoded = audioCoder.decodeAudio(samples, packet, offset);
				    	        	if (bytesDecoded < 0) {
					    	        	if (log.isDebugEnabled())
					    	        		log.debug("run(): got error decoding audio in= " + filename);
				    	        		//fileSupported = false;
				    	        		break;
				    	        	}
				    	        	offset += bytesDecoded;
				    	          
				    	        	/*
				    	        	 * Some decoder will consume data in a packet, but will not be able to construct
				    	        	 * a full set of samples yet.  Therefore you should always check if you
				    	        	 * got a complete set of samples from the decoder
				    	        	 */
				    	        	if (samples.isComplete()) {
				    	        		playJavaSound(samples);
				    	        		
			                            if ((callBack != null) && !stopFlag) {
			                            	double currentSeconds = stream.getCurrentDts() / stream.getTimeBase().getDenominator();
			                                String newTime = new Duration((int)currentSeconds).getDurationAsString();
			                                if (!newTime.equals(lastCallBackTime)) {
			                                	lastCallBackTime = newTime;
			                                }                          
			                                double totalpercentage = currentSeconds / getTotalTime();
			                                if (!stopFlag)
			                                	callBack.setPosition(totalpercentage);
			                            }                          
				    	        		
				    	        	}
				    	        }
			    	    	} else {
			    	    		// This packet isn't part of our audio stream, so we just silently drop it.
			    	    	}   	        				
	        			}
        			} catch (Exception e) {
        				log.error("run(): error during decode loop", e);
        			} finally {
    	        		nativeLock.release();
    	        	}
        		} while (packetReadResult >= 0);        		
		    	    
    		} catch (Exception e) {
    			log.error("run(): error", e);
    		} finally {
                if ((callBack != null) && !stopFlag) {
                    callBack.setIsPlaying(false);                
                    callBack.donePlayingSong();    
                }                			
    		}
    	}
    }
    
    private void playJavaSound(IAudioSamples aSamples) {
    	byte[] rawBytes = aSamples.getData().getByteArray(0, aSamples.getSize());
    	mLine.write(rawBytes, 0, aSamples.getSize());
    }
    
    static public void main(String[] args) {
    	try {
    		// a little test routine
    		RapidEvolution3.loadLog4J();
    		XugglePlayer player = new XugglePlayer();
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
    			player.stop();
    			log.info("stopped");
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
