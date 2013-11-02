package com.mixshare.rapid_evolution.audio.detection.beatintensity;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.audio.AudioBuffer;
import com.mixshare.rapid_evolution.audio.codecs.AudioDecoder;
import com.mixshare.rapid_evolution.audio.codecs.AudioDecoderFactory;
import com.mixshare.rapid_evolution.audio.codecs.DecoderException;
import com.mixshare.rapid_evolution.audio.detection.bpm.BpmDetector;
import com.mixshare.rapid_evolution.audio.dsp.SubBandSeparator;
import com.mixshare.rapid_evolution.music.beatintensity.BeatIntensity;
import com.mixshare.rapid_evolution.util.io.FileLockManager;
import com.mixshare.rapid_evolution.workflow.Task;

public class BeatIntensityDetector {

	static private Logger log = Logger.getLogger(BeatIntensityDetector.class);

	static private int BEAT_INTENSITY_ANALYZE_CHUNK_SIZE = RE3Properties.getInt("beat_intensity_detector_analyze_chunk_size");

	static public BeatIntensity detectBeatIntensity(String filename, Task task) {
		if (log.isTraceEnabled())
			log.trace("detectBeatIntensity(): detecting beat intensity filename=" + filename);
		int result = 0;
		AudioDecoder decoder = null;
		try {
			FileLockManager.startFileRead(filename);
			SubBandSeparator subband = null;
			double minbpm = BpmDetector.BPM_DETECTOR_MINIMUM;
			double maxbpm = BpmDetector.BPM_DETECTOR_MAXIMUM; 
			decoder = AudioDecoderFactory.getAudioDecoder(filename);
			if (decoder != null) {
				double seconds = decoder.getTotalSeconds();

				// read WAV data and merge to a single channel, then separate
				// channel into frequency bands
				if (subband == null)
					subband = new SubBandSeparator((float) decoder.getSampleRate(), minbpm, maxbpm, seconds, task);
				subband.setColorMode(true);

				double[] wavearray = new double[BEAT_INTENSITY_ANALYZE_CHUNK_SIZE];
				long frames_read = decoder.readFrames(wavearray.length);
				boolean done = false;
				boolean aborted = false;
				int total_frames_read = 0;
				while ((frames_read > 0) && !done) {
					if (frames_read == BEAT_INTENSITY_ANALYZE_CHUNK_SIZE) {
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
					if (!done) {
						if (task != null) {
							task.setProgress((float)(decoder.getSecondsRead() / decoder.getTotalSeconds()));
						}
						frames_read = decoder.readFrames(wavearray.length);
					}
				}

				if (!aborted) {
					subband.determineBeatProperties(subband.getSongColor());
					if (log.isTraceEnabled())
						log.trace("detectBeatIntensity(): " + total_frames_read + " frames read");					
					result = subband.getBeatIntensity();
				}
			}
		} catch (DecoderException de) {
			log.error("detectBeatIntensity(): decoder exception");
			result = 0;
		} catch (Exception e) {
			if (!RapidEvolution3.isTerminated && ((task == null) || !task.isCancelled()))
				log.error("detectBeatIntensity(): error", e);
		} finally {
			if (decoder != null)
				decoder.close();
			FileLockManager.endFileRead(filename);
		}
		if (log.isDebugEnabled())
			log.debug("detectBeatIntensity(): result=" + result);
		return BeatIntensity.getBeatIntensity(result);
	}
}
