package com.mixshare.rapid_evolution.audio.detection.key;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.audio.AudioBuffer;
import com.mixshare.rapid_evolution.audio.codecs.AudioDecoder;
import com.mixshare.rapid_evolution.audio.codecs.AudioDecoderFactory;
import com.mixshare.rapid_evolution.audio.codecs.DecoderException;
import com.mixshare.rapid_evolution.music.key.Key;
import com.mixshare.rapid_evolution.ui.RapidEvolution3UI;
import com.mixshare.rapid_evolution.util.io.FileLockManager;
import com.mixshare.rapid_evolution.util.timing.Semaphore;
import com.mixshare.rapid_evolution.workflow.Task;

public class KeyDetector {
    
	static private Logger log = Logger.getLogger(KeyDetector.class);

	static public int KEY_DETECTOR_ANALYZE_CHUNK_SIZE = RE3Properties.getInt("key_detector_analyze_chunk_size"); // defult is 8192 (0.19 seconds), harmonics produce negligible effects on this sampling size
	static private double KEY_DETECTOR_TRACE_INTERVAL_SECONDS = RE3Properties.getDouble("key_detector_trace_interval_seconds");

	static private Semaphore getMatrixSem = new Semaphore(1);
	static private Map<Integer, KeyDetectionMatrix> matrixMap = new HashMap<Integer, KeyDetectionMatrix>();
    static public KeyDetectionMatrix getMatrix(int maxFrequency) {
    	KeyDetectionMatrix result = null;
    	try {
    		getMatrixSem.acquire();
	    	result = matrixMap.get(maxFrequency);
	    	if (result == null) {          
	    		result = new KeyDetectionMatrix(maxFrequency);
	    		matrixMap.put(maxFrequency, result);
	    	}
    	} catch (Exception e) { } finally {
    		getMatrixSem.release();
    	}
    	return result;
    }
	
    static public DetectedKey detectKey(String filename) {
    	return detectKey(filename, null);
    }
	static public DetectedKey detectKey(String filename, Task task) {
		if (log.isDebugEnabled())
			log.debug("detectKey(): detecting key from file=" + filename);
		
		DetectedKey result = new DetectedKey();		
		AudioDecoder decoder = null;
		KeyProbability segment_probabilities = null;
		double[] norm_keycount = new double[12];
		double[] cwt = new double[12];
		double report_time = KEY_DETECTOR_TRACE_INTERVAL_SECONDS;
		long startTime = System.currentTimeMillis();
		
		try {      
			FileLockManager.startFileRead(filename);
			decoder = AudioDecoderFactory.getAudioDecoder(filename);
			if (decoder != null) {
				double segmentTime = KEY_DETECTOR_ANALYZE_CHUNK_SIZE / decoder.getAudioFormat().getSampleRate() * 1000;
				segment_probabilities = new KeyProbability(segmentTime);
				int percentAudioToProcess = RE3Properties.getInt("percent_audio_samples_to_process"); // 0 to 100
				int percentCount = 0;      
				boolean done = false;
				boolean half_way = false;
				boolean aborted = false;
				while (!done) {
					if (percentCount <= percentAudioToProcess) {
						long frames_read = decoder.readFrames(KEY_DETECTOR_ANALYZE_CHUNK_SIZE);
						if ((frames_read < KEY_DETECTOR_ANALYZE_CHUNK_SIZE) && (frames_read > 0)) {
							if (log.isDebugEnabled())
								log.debug("detectKeyFromFile(): insufficient audio found, looping audio segment to meet minimum length required");
							int i = 0;
							for (long j = frames_read; j < KEY_DETECTOR_ANALYZE_CHUNK_SIZE; ++j) {
								for (int c = 0; c < decoder.getAudioFormat().getChannels(); ++c) {
									double copySample = decoder.getAudioBuffer().getSampleData()[c][i];
									decoder.getAudioBuffer().setSampleValue((int)j, c, copySample);
								}
								++i;	            		  
							}	       
							decoder.skipFrames(KEY_DETECTOR_ANALYZE_CHUNK_SIZE - frames_read);
							frames_read = KEY_DETECTOR_ANALYZE_CHUNK_SIZE;
							done = true;
						}
						if (frames_read == KEY_DETECTOR_ANALYZE_CHUNK_SIZE) {
							analyzeSegment(decoder.getAudioBuffer(), decoder, norm_keycount, segment_probabilities, cwt);
							double audiotime = decoder.getSecondsRead();
							DetectedKey start_section = null;
							if (half_way) {
								start_section = segment_probabilities.getDetectedKey();
								if (start_section != null) {
									if (RE3Properties.getBoolean("detect_start_and_end_keys"))
										result.setEndKey(start_section.getStartKey());
									else
										result.setStartKey(start_section.getStartKey());
									result.setAccuracy(start_section.getAccuracyValue());	                      
								}
							} else {
								start_section = segment_probabilities.getDetectedKey((!half_way && (audiotime * 2 > decoder.getTotalSeconds())));
								if (start_section != null) {
									result.setStartKey(start_section.getStartKey());
									result.setAccuracy(start_section.getAccuracyValue());	                      
								}
							}
							if (!half_way && (audiotime * 2 > decoder.getTotalSeconds())) {
								half_way = true;
								segment_probabilities = new KeyProbability(segmentTime);	           
							}
							if (task != null) {
								task.setProgress((float)(decoder.getSecondsRead() / decoder.getTotalSeconds()));
							}
							if (audiotime > report_time) {
								if (log.isTraceEnabled())
									log.trace("detectKeyFromFile(): time=" + audiotime + "s, key=" + start_section.getStartKey());
								report_time += KEY_DETECTOR_TRACE_INTERVAL_SECONDS;
							}
							if (RapidEvolution3.isTerminated || ((task != null) && task.isCancelled())) {
								aborted = true;
								done = true;
							}
						} else if (frames_read == 0) {
							done = true;
						}
					} else {
						long frames_skipped = decoder.skipFrames(KEY_DETECTOR_ANALYZE_CHUNK_SIZE);
						if (frames_skipped == 0)
							done = true;
					}
					++percentCount;
					if (percentCount > 100)
						percentCount = 0;          
					if (RapidEvolution3.isTerminated || ((task != null) && task.isCancelled())) {
						aborted = true;
						done = true;
					}					
				}      				
				if (!aborted) {
					segment_probabilities.finish();
					if (!segment_probabilities.hasNoData()) {
						DetectedKey end_key = segment_probabilities.getDetectedKey();
						Key key = segment_probabilities.getDetectedKey(true).getStartKey();
						if (result.getStartKey().equals(""))
							result.setStartKey(key);
						else
							result.setEndKey(key);
						if (result.getAccuracyValue() != 0.0)
							result.setAccuracy(Math.min(end_key.getAccuracyValue(), result.getAccuracyValue()));
						else
							result.setAccuracy(end_key.getAccuracyValue());
						if (!RE3Properties.getBoolean("detect_start_and_end_keys")) { // use single key                  
							result.setStartKey(key);
							result.setEndKey(Key.NO_KEY);
							result.setAccuracy(result.getAccuracyValue());
						}
					} else {
						result.setStartKey(Key.NO_KEY);
						result.setAccuracy(0.01);
					}
					Date endtime = new Date();
					long seconds = (endtime.getTime() - startTime) / 1000;
					log.debug("detectKey(): detected key=" + result + ", in " + seconds + " seconds");
				} else {
					result = new DetectedKey();   
				}
			} else {
				if (log.isDebugEnabled())
					log.debug("detectKey(): no decoder available for file=" + filename);
			}
		} catch (java.lang.OutOfMemoryError e) {
			log.error("detectKey(): out of memory error detecting key from file=" + filename);
			if (RapidEvolution3UI.instance != null)
				RapidEvolution3UI.instance.notifyOutOfMemory("detecting the key from file=" + filename);			
		} catch (DecoderException de) {
			log.error("detectKey(): decoder exception");
		} catch (Exception e) {
			log.error("detectKey(): error detecting key from file=" + filename, e); 
		} finally {
			if (decoder != null)
				decoder.close();
			FileLockManager.endFileRead(filename);
		}
		return result;
	}

	static private void analyzeSegment(AudioBuffer audiobuffer, AudioDecoder decoder, double[] norm_keycount, KeyProbability segment_probabilities, double[] cwt) {
		try {
			int num_channels_to_use = 1;
			int maxfrequency = (int) decoder.getMaxFrequency();
			double timeinterval = (double) KEY_DETECTOR_ANALYZE_CHUNK_SIZE / decoder.getSampleRate();
			for (int channel = 0; channel < Math.min(num_channels_to_use, audiobuffer.getNumChannels()); ++channel) {
				for (int i = 0; i < norm_keycount.length; ++i)
					norm_keycount[i] = 0.0;
				countKeyProbabilities( audiobuffer.getSampleData(channel), 0, KEY_DETECTOR_ANALYZE_CHUNK_SIZE, timeinterval, maxfrequency, segment_probabilities, norm_keycount, cwt);
			}
		} catch (Exception e) {
			log.error("analyzeSegment(): error", e);
		}
	}    

	static private void countKeyProbabilities(double[] wavedata, long icount, long amt, double time, int maxfreq, KeyProbability segment_probabilities, double[] norm_keycount, double[] cwt) {
    	KeyDetectionMatrix matrix = getMatrix(maxfreq);         	
    	int icountInt = (int)icount;
    	for (int p = 0; p < matrix.getMaxOctaves(); p++) {
    		for (int ks = 0; ks < matrix.getShifts(); ks++) {
    			for (int z = 0; z < 12; ++z)
    				cwt[z] = 0.0;
    			for (int m = 0; m < amt; ++m) {
    				for (int z = 0; z < 12; ++z) {
    					double x = matrix.getValue(p, ks, m, z);
    					double y = wavedata[m + icountInt];
    					cwt[z] += y * x;
    				}
    			}
    			for (int z = 0; z< 12; ++z)
    				norm_keycount[z] += Math.abs(cwt[z]);
    		}
    	}
    	segment_probabilities.add(norm_keycount, time);
    }
	
}
