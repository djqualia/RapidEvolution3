package com.mixshare.rapid_evolution.audio.detection.color;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.audio.AudioBuffer;
import com.mixshare.rapid_evolution.audio.codecs.AudioDecoder;
import com.mixshare.rapid_evolution.audio.codecs.AudioDecoderFactory;
import com.mixshare.rapid_evolution.audio.detection.bpm.BpmDetector;
import com.mixshare.rapid_evolution.audio.dsp.SubBandSeparator;
import com.mixshare.rapid_evolution.util.io.FileLockManager;
import com.mixshare.rapid_evolution.workflow.Task;

public class ColorDetector {

	static private Logger log = Logger.getLogger(ColorDetector.class);

	static private int analyzechunksize = 8192;

	static public SongColor detectColor(String filename, Task task) {
		if (log.isTraceEnabled())
			log.trace("detectColor(): detecting color from filename=" + filename);
		SongColor result = null;
		AudioDecoder decoder = null;
		try {
			FileLockManager.startFileRead(filename);
			SubBandSeparator subband = null;
			double minbpm = BpmDetector.BPM_DETECTOR_MINIMUM;
			double maxbpm = BpmDetector.BPM_DETECTOR_MAXIMUM; 
			decoder = AudioDecoderFactory.getAudioDecoder(filename);
			if (decoder != null) {
				double seconds = decoder.getTotalSeconds();

				// read WAV data and merge to a single channel, then seperate
				// channel into frequency bands
				if (subband == null)
					subband = new SubBandSeparator((float) decoder.getSampleRate(), minbpm, maxbpm, seconds, task);
				subband.setColorMode(true);

				double[] wavearray = new double[analyzechunksize];
				long frames_read = decoder.readFrames(wavearray.length);
				boolean done = false;
				boolean aborted = false;
				int total_frames_read = 0;
				while ((frames_read > 0) && !done) {
					if (frames_read == analyzechunksize) {
						AudioBuffer buffer = decoder.getAudioBuffer();
						double[][] data = buffer.getSampleData();
						for (int f = 0; f < wavearray.length; ++f) {
							double val = 0.0;
							for (int c = 0; c < buffer.getNumChannels(); ++c)
								val += data[c][f];							
							wavearray[f] = val;
						}
						subband.send(wavearray);
						if (subband.lockedon)
							done = true;
					}
					total_frames_read += frames_read;
					if (RapidEvolution3.isTerminated || ((task != null) && task.isCancelled())) {
						done = true;
						aborted = true;
					}
					if (!done)
						frames_read = decoder.readFrames(wavearray.length);
				}

				if (!aborted) {
					subband.determineBeatProperties(subband.getSongColor());
					if (log.isTraceEnabled())
						log.trace("detectColor(): " + total_frames_read + " frames read");					
					result = subband.getSongColor();					
				}
			}

		} catch (Exception e) {
			log.error("detectColor(): error", e);
		} finally {
			if (decoder != null)
				decoder.close();
			FileLockManager.endFileRead(filename);
		}
		if (log.isDebugEnabled())
			log.debug("detectColor(): result=" + result);
		return result;
	}
}
