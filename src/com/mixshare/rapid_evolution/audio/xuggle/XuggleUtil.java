package com.mixshare.rapid_evolution.audio.xuggle;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.audio.codecs.decoders.XuggleAudioDecoder;
import com.mixshare.rapid_evolution.music.duration.Duration;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;

public class XuggleUtil {

	static private Logger log = Logger.getLogger(XuggleUtil.class);
	
    static public String getVersion() {
        try {        	
        	IContainer container = IContainer.make();  
        	container.close();
        	return String.valueOf(com.xuggle.xuggler.Version.getMajorVersion()) + "." + String.valueOf(com.xuggle.xuggler.Version.getMinorVersion());
        } catch (java.lang.Error e) {
        	if (log.isDebugEnabled())
        		log.debug("getVersionString(): error Exception", e);
        } catch (Exception e) {
        	if (log.isDebugEnabled())
        		log.debug("getVersionString(): error Exception", e);            
        }
        return "N/A";
    }
    
    static public Duration getVideoDuration(String filename) {
    	try {
    	    IContainer container = null;
    	    
    	    try {
	    	    container = IContainer.make();
	
	    	    // Open up the container
	    	    if (container.open(filename, IContainer.Type.READ, null) < 0)
	    	    	throw new IllegalArgumentException("getVideoDuration(): could not open file=" + filename);
	    		
	    	    // query how many streams the call to open found
	    	    int numStreams = container.getNumStreams();
	
	    	    // and iterate through the streams to find the first video stream
	    	    int videoStreamId = -1;
	    	    IStreamCoder videoCoder = null;
	    	    for(int i = 0; i < numStreams; i++) {
	    	    	// Find the stream object
	    	    	IStream stream = container.getStream(i);
	    	    	// Get the pre-configured decoder that can decode this stream;
	    	    	IStreamCoder coder = stream.getStreamCoder();
	
	    	    	if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) {
	    	    		videoStreamId = i;
	    	    		videoCoder = coder;
	    	    		break;
	    	    	}
	    	    }
	    	    if (videoStreamId == -1)
	    	    	throw new RuntimeException("getVideoDuration(): could not find video stream in container=" + filename);    	    
	    	    
	    	    double totalSeconds = ((double)container.getDuration()) / 1000000.0; //videoCoder.getStream().getTimeBase().getDouble();	    	    
	    	    return new Duration(totalSeconds * 1000);
	    	    
    	    } finally {
    	    	if (container != null)
    	    		container.close();
    	    }
    	    
    	} catch (java.lang.Error e) {
        	if (log.isDebugEnabled())
        		log.debug("getVideoDuration(): error Exception", e);
        } catch (Exception e) {
        	if (log.isDebugEnabled())
        		log.debug("getVideoDuration(): error Exception", e);            
        }
        return null;
    }
    
    static public Duration getAudioDuration(String filename) {
    	try {
    	    IContainer container = null;
    	    
    	    try {
	    	    container = IContainer.make();
	
	    	    // Open up the container
	    	    if (container.open(filename, IContainer.Type.READ, null) < 0)
	    	    	throw new IllegalArgumentException("getAudioDuration(): could not open file=" + filename);
	    		
	    	    // query how many streams the call to open found
	    	    int numStreams = container.getNumStreams();
	
	    	    // and iterate through the streams to find the first video stream
	    	    int videoStreamId = -1;
	    	    IStreamCoder videoCoder = null;
	    	    for(int i = 0; i < numStreams; i++) {
	    	    	// Find the stream object
	    	    	IStream stream = container.getStream(i);
	    	    	// Get the pre-configured decoder that can decode this stream;
	    	    	IStreamCoder coder = stream.getStreamCoder();
	
	    	    	if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO) {
	    	    		videoStreamId = i;
	    	    		videoCoder = coder;
	    	    		break;
	    	    	}
	    	    }
	    	    if (videoStreamId == -1)
	    	    	throw new RuntimeException("getAudioDuration(): could not find video stream in container=" + filename);    	    
	    	    
	    	    double totalSeconds = ((double)container.getDuration()) / 1000000.0; //videoCoder.getStream().getTimeBase().getDouble();	    	    
	    	    return new Duration(totalSeconds * 1000);
	    	    
    	    } finally {
    	    	if (container != null)
    	    		container.close();
    	    }
    	    
    	} catch (java.lang.Error e) {
        	if (log.isDebugEnabled())
        		log.debug("getAudioDuration(): error Exception", e);
        } catch (Exception e) {
        	if (log.isDebugEnabled())
        		log.debug("getAudioDuration(): error Exception", e);            
        }
        return null;
    }    
    
    static public void main(String[] args) {
    	try {
    		RapidEvolution3.loadLog4J();    		
    		log.info(getAudioDuration("C:/Users/JBickmore/Desktop/new music/Caribou - Swim/01 Odessa.mp3"));
    		log.info(new XuggleAudioDecoder("C:/Users/JBickmore/Desktop/new music/Caribou - Swim/01 Odessa.mp3").getTotalSeconds());
    	} catch (Exception e) {
    		log.error("main(): error", e);
    	}
    }

	
}
