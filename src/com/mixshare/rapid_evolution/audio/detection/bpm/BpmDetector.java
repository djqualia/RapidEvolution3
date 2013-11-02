package com.mixshare.rapid_evolution.audio.detection.bpm;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.audio.AudioBuffer;
import com.mixshare.rapid_evolution.audio.codecs.AudioDecoder;
import com.mixshare.rapid_evolution.audio.codecs.AudioDecoderFactory;
import com.mixshare.rapid_evolution.audio.codecs.CodecConstants;
import com.mixshare.rapid_evolution.audio.codecs.DecoderException;
import com.mixshare.rapid_evolution.audio.dsp.SubBandSeparator;
import com.mixshare.rapid_evolution.audio.exceptions.UnsupportedSampleRateException;
import com.mixshare.rapid_evolution.ui.RapidEvolution3UI;
import com.mixshare.rapid_evolution.util.io.FileLockManager;
import com.mixshare.rapid_evolution.workflow.Task;

public class BpmDetector {
    
	static private Logger log = Logger.getLogger(BpmDetector.class);

	static public int BPM_DETECTOR_ANALYZE_CHUNK_SIZE = RE3Properties.getInt("bpm_detector_analyze_chunk_size"); // normally 8192
	static public int BPM_DETECTOR_QUALITY = RE3Properties.getInt("bpm_detector_quality"); // 0 to 10
	static public double BPM_DETECTOR_MINIMUM;  // minimum bpm to search (lower bound cap)
	static public double BPM_DETECTOR_MAXIMUM; // maximum bpm to search (upper bound cap)
	  		
	static {
		try {
			BPM_DETECTOR_MINIMUM = RE3Properties.getDouble("bpm_detector_minimum");
		} catch (Exception e) {
			BPM_DETECTOR_MINIMUM = 80.0;
		}
		try {
			BPM_DETECTOR_MAXIMUM = RE3Properties.getDouble("bpm_detector_maximum"); // maximum bpm to search (upper bound cap)
		} catch (Exception e) {
			BPM_DETECTOR_MAXIMUM = 160.0;
		}		
	}

	static public DetectedBpm detectBpm(String filename) { return detectBpm(filename, 1.0, null); }
	static public DetectedBpm detectBpm(String filename, Task task) { return detectBpm(filename, 1.0, task); }
	static public DetectedBpm detectBpm(String filename, double measurescale, Task task) {
		if (log.isTraceEnabled())
			log.trace("detectBpm(): detecting bpm from filename=" + filename + ", measurescale=" + measurescale);
		SubBandSeparator subband = null;
		DetectedBpm detectedbpm = null;
		AudioDecoder decoder = null;    
		try {
			FileLockManager.startFileRead(filename);
			decoder = AudioDecoderFactory.getAudioDecoder(filename);
			if (decoder != null) {
				double seconds = decoder.getTotalSeconds();
       
				// read WAV data and merge to a single channel, then separate channel into frequency bands
				if (subband == null)
					subband = new SubBandSeparator((float)decoder.getSampleRate(), BPM_DETECTOR_MINIMUM, BPM_DETECTOR_MAXIMUM, seconds, task);
				
				boolean done = false;
				boolean aborted = false;
				double[] wavearray = new double[BPM_DETECTOR_ANALYZE_CHUNK_SIZE];
				long frames_read = decoder.readFrames(wavearray.length);
				int total_frames_read = 0;          
				while ((frames_read > 0) && !done) {
					if (frames_read == BPM_DETECTOR_ANALYZE_CHUNK_SIZE) {
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
							if (BPM_DETECTOR_QUALITY == 0)
								task.setProgress((float)(decoder.getSecondsRead() / decoder.getTotalSeconds()));
							else
								task.setProgress((float)(decoder.getSecondsRead() / decoder.getTotalSeconds() / 2));
						}
						frames_read = decoder.readFrames(wavearray.length);
					}
				}          
				if (!aborted) {
					if (log.isTraceEnabled())
						log.trace("detectBpmFromFile(): " + total_frames_read + " frames read");
					detectedbpm = subband.getBpm(decoder);
				}          
			}

		} catch (UnsupportedSampleRateException usr) {
			log.warn("detectBpm(): unsupported sample rate for filename=" + filename);
		} catch (java.io.FileNotFoundException e) {
			log.warn("detectBpm(): file not found=" + filename);
		} catch (java.lang.OutOfMemoryError e) {
			log.error("detectBpm(): out of memory detecting key from filename=" + filename, e);
			if (RapidEvolution3UI.instance != null)
				RapidEvolution3UI.instance.notifyOutOfMemory("detecting the BPM from file=" + filename);
		} catch (DecoderException de) {
			log.error("detectBpm(): decoder exception");
		} catch (Exception e) {
			if (!RapidEvolution3.isTerminated && ((task == null) || !task.isCancelled()))
				log.error("detectBpm(): error Exception", e); 
		} finally {
			if (decoder != null)
				decoder.close();
			FileLockManager.endFileRead(filename);
		}
		if (log.isTraceEnabled())
			log.trace("detectBpm(): detected bpm=" + detectedbpm);
		return detectedbpm;
	}

	static public double GetBpmFromFile2(AudioDecoder decoder, double minbpm1,
            double maxbpm1, double minbpm2, double maxbpm2, double minbpm3,
            double maxbpm3, Task task) {
        double detectedbpm = 0.0;
        try {
            double[] newdata = new double[CodecConstants.DEFAULT_FRAME_BUFFER_SIZE];
            int measures = 1;
            // determines maximum block size for selectiv descent:
            int maxpower = 6; 
            // lower quality level #s mean higher quality and more time
            int qualitylevel = maxpower - (RE3Properties.getInt("bpm_detector_quality") - 1);
            if (qualitylevel < 0)
                qualitylevel = 0;
            if (qualitylevel > maxpower)
                qualitylevel = maxpower;
            double[] combinedAudio = null;
            boolean[] usedarray = new boolean[5]; // 1 2 4 8 16 32
            for (int i = 0; i < usedarray.length; i++)
                usedarray[i] = true;
            BpmComb comb = new BpmComb(usedarray);
            try {
                if (RapidEvolution3.isTerminated || ((task != null) && task.isCancelled()))
                    throw new Exception();
                if (log.isTraceEnabled()) { 
	                log.trace("GetBpmFromFile2(): sub-detecting bpm...");
	                log.trace("GetBpmFromFile2(): -- range 1 -- min: "
	                        + String.valueOf(minbpm1) + ", max: "
	                        + String.valueOf(maxbpm1));
	                log.trace("GetBpmFromFile2(): -- range 2 -- min: "
	                        + String.valueOf(minbpm2) + ", max: "
	                        + String.valueOf(maxbpm2));
	                log.trace("GetBpmFromFile2(): -- range 3 -- min: "
	                        + String.valueOf(minbpm3) + ", max: "
	                        + String.valueOf(maxbpm3));
                }

                float samplescale = (float) decoder.getSampleRate() / 44100.0f;
                if (samplescale > 1.0f)
                    while ((samplescale / 2.0f) >= 1.0f) {
                        samplescale /= 2.0f;
                        maxpower++;
                        qualitylevel++;
                    }
                else if (samplescale < 1.0f)
                    while ((samplescale * 2.0f) <= 1.0f) {
                        samplescale *= 2.0f;
                        maxpower--;
                        qualitylevel--;
                    }
                if (qualitylevel < 0)
                    qualitylevel = 0;

                Vector<Integer> inspect = new Vector<Integer>();
                int chunkscale = (int) Math.pow(2.0, maxpower);
                double effectivesamplerate = decoder.getSampleRate()
                        / ((double) chunkscale);
                double bpm = 60.0 * effectivesamplerate * measures;
                int blocksize = 1;
                while (bpm >= BPM_DETECTOR_MINIMUM * 0.75) {
                    if (bpm <= BPM_DETECTOR_MAXIMUM * 1.25) {
                        if ((bpm >= minbpm1) && (bpm <= maxbpm1))
                            inspect.add(new Integer(blocksize));
                        if ((bpm >= minbpm2) && (bpm <= maxbpm2))
                            inspect.add(new Integer(blocksize));
                        if ((bpm >= minbpm3) && (bpm <= maxbpm3))
                            inspect.add(new Integer(blocksize));
                    }
                    blocksize++;
                    bpm = 60.0 / ((double) blocksize) * effectivesamplerate
                            * measures;
                }
                int numPasses = maxpower - qualitylevel + 1;
                int pass = 0;
                for (int m = maxpower; m >= qualitylevel; m--) {
                	
                    if (RapidEvolution3.isTerminated || ((task != null) && task.isCancelled()))
                        throw new Exception();

                    decoder.reset();
                    
                    chunkscale = (int) Math.pow(2.0, m);
                    effectivesamplerate = decoder.getSampleRate()
                            / ((double) chunkscale);
                    comb.reset(usedarray);
                    double avgbpm = 0;
                    for (int i = 0; i < inspect.size(); ++i) {
                        int isize = ((Integer) inspect.get(i)).intValue();
                        avgbpm += 60.0 / ((double) isize) * effectivesamplerate
                                * measures;
                        comb.addBlock(isize);
                    }
                    avgbpm /= (double) inspect.size();
                    boolean keepgoing = false;
                    for (int i = 0; i < inspect.size(); ++i) {
                        int isize = ((Integer) inspect.get(i)).intValue();
                        double ibpm = 60.0 / ((double) isize)
                                * effectivesamplerate * measures;
                        if (Math.abs(ibpm - avgbpm) > 1.0)
                            keepgoing = true;
                    }
                    if (!keepgoing) {
                        m = 0;
                        break;
                    }
                    if (log.isTraceEnabled())
                    	log.trace("GetBpmFromFile2(): m = " + String.valueOf(m));
                    double[] tempbits = new double[0];
                    AudioBuffer buffer = decoder.getAudioBuffer();
                    if (buffer == null) {
                    	decoder.close();
                    	decoder = AudioDecoderFactory.getAudioDecoder(decoder.getFilename());
                    	buffer = decoder.getAudioBuffer();
                    }
                    double[][] data = buffer.getSampleData();
                    if ((combinedAudio == null)
                            || (combinedAudio.length != BPM_DETECTOR_ANALYZE_CHUNK_SIZE))
                        combinedAudio = new double[BPM_DETECTOR_ANALYZE_CHUNK_SIZE];
                    int tempbitindex = 0;
                    int newdatasize = 0;
                    int index = 0;
                    try {
                        boolean done = false;
                        while (!done) {
                            if (RapidEvolution3.isTerminated || ((task != null) && task.isCancelled())) {
                                done = true;
                                throw new Exception();
                            } else {
                                long frames_read = decoder
                                        .readFrames(BPM_DETECTOR_ANALYZE_CHUNK_SIZE);
                                if (task != null) {
                                	double partialProgress = decoder.getSecondsRead() / decoder.getTotalSeconds();
                                	if (task != null) {
                                		task.setProgress((float)(0.5f + (partialProgress + pass) / numPasses));
                                	}
                                }
                                if (frames_read == 0) {
                                    done = true;
                                } else if (frames_read == BPM_DETECTOR_ANALYZE_CHUNK_SIZE) {
                                    for (int f = 0; f < BPM_DETECTOR_ANALYZE_CHUNK_SIZE; ++f) {
                                        double val = 0.0;
                                        for (int c = 0; c < buffer
                                                .getNumChannels(); ++c) {
                                            val += data[c][f];
                                        }
                                        combinedAudio[f] = val;
                                    }

                                    newdatasize = (int) Math
                                            .floor(((double) (combinedAudio.length + tempbits.length))
                                                    / chunkscale) + 1;
                                    index = 0;
                                    int wavecount = 0;

                                    if (chunkscale > 1) {
                                        if (newdata.length < newdatasize) {
                                            newdata = new double[newdatasize];
                                        }
                                        while (wavecount < combinedAudio.length) {
                                            if (tempbits != null) {
                                                if ((tempbits.length + (combinedAudio.length - wavecount)) >= chunkscale) {
                                                    double avg = 0.0;
                                                    for (int b = 0; b < tempbits.length; ++b)
                                                        avg += Math
                                                                .abs(tempbits[b]);
                                                    for (int b = tempbits.length; b < chunkscale; ++b) {
                                                        avg += Math
                                                                .abs(combinedAudio[wavecount++]);
                                                    }
                                                    newdata[index++] = avg;
                                                    tempbits = null;
                                                } else {
                                                    while (wavecount < combinedAudio.length)
                                                        tempbits[tempbitindex++] = combinedAudio[wavecount++];
                                                }
                                            } else if ((wavecount + chunkscale) < combinedAudio.length) {
                                                double avg = 0.0;
                                                for (int b = 0; b < chunkscale; ++b) {
                                                    avg += Math
                                                            .abs(combinedAudio[wavecount++]);
                                                }
                                                newdata[index++] = avg;
                                            } else {
                                                int tempbitsize = combinedAudio.length
                                                        - wavecount;
                                                while (tempbitsize
                                                        + combinedAudio.length < chunkscale) {
                                                    tempbitsize += combinedAudio.length;
                                                }
                                                tempbits = new double[tempbitsize];
                                                tempbitindex = 0;
                                                while (wavecount < combinedAudio.length)
                                                    tempbits[tempbitindex++] = combinedAudio[wavecount++];
                                            }
                                        }
                                        if (index > 0) comb.pushData(newdata, index);
                                    } else {
                                        comb.pushData(combinedAudio, combinedAudio.length);
                                    }

                                }
                            }
                        }
                    } catch (Exception e) {
                        log.error("GetBpmFromFile2(): error", e);
                        if (log.isTraceEnabled())
                        	log.trace("GetBpmFromFile2(): newdatasize: " + newdatasize);
                    }

                    comb.normalize();

                    double min = 0;
                    for (int i = 0; i < usedarray.length; i++)
                        usedarray[i] = false;
                    double avgcount = 0;
                    for (int i = 0; i < comb.getBpmFilters().size(); ++i) {
                        BpmFilter filter = (BpmFilter) comb.getBpmFilters().get(i);
                        double blockbpm = 60.0 / ((double) filter.getBlockSize()) * effectivesamplerate * measures;
                        double blockdiff = 1.0;
                        double blockbpm2 = 60.0 / ((double) (filter.getBlockSize() + 1)) * effectivesamplerate * measures;
                        blockdiff = Math.abs(blockbpm - blockbpm2);
                        blockbpm2 = 60.0 / ((double) (filter.getBlockSize() - 1)) * effectivesamplerate * measures;
                        double blockdiff2 = Math.abs(blockbpm - blockbpm2);
                        if (blockdiff2 < blockdiff)
                            blockdiff = blockdiff2;
                        int minindex = -1;
                        double mindiff = 0.0;
                        for (int z = 0; z < filter.getUseArray().length; ++z) {
                            if ((filter.getTotalDiff()[z] != 0)
                                    && ((filter.getTotalDiff()[z] < mindiff) || (mindiff == 0))) {
                                mindiff = filter.getTotalDiff()[z];
                                minindex = (int) Math.pow(2.0, z);
                                usedarray[z] = true;
                            }
                        }
                        avgcount += mindiff;
                        if ((mindiff < min) || (min == 0)) {
                            detectedbpm = round(blockbpm,
                                    extractDecimalPlaces(String
                                            .valueOf(blockdiff)));
                            min = mindiff;
                        }
                        if (log.isTraceEnabled())
                        	log.trace("GetBpmFromFile2(): bpm: "
                        			+ String.valueOf(blockbpm) + ", count: "
                        			+ String.valueOf(mindiff) + ", measures: "
                        			+ minindex);
                    }
                    avgcount /= (double) comb.getBpmFilters().size();
                    if (avgcount == 0) {
                        m = maxpower;
                        break;
                    }
                    if (log.isTraceEnabled())
                    	log.trace("GetBpmFromFile2(): avg count: " + String.valueOf(avgcount));
                    inspect = new Vector<Integer>();
                    String inspecting = new String("selecting: ");
                    for (int i = 0; i < comb.getBpmFilters().size(); ++i) {
                        BpmFilter filter = (BpmFilter) comb.getBpmFilters().get(i);
                        double blockbpm = 60.0 / ((double) filter.getBlockSize()) * effectivesamplerate * measures;
                        boolean islessthan = false;
                        for (int z = 0; z < filter.getUseArray().length; ++z)
                            if (filter.getTotalDiff()[z] != 0.0 && (filter.getTotalDiff()[z] < avgcount))
                                islessthan = true;
                        if (islessthan) {
                            inspecting += String.valueOf(blockbpm) + "  ";
                            inspect.add(new Integer((filter.getBlockSize() * 2) - 1));
                            inspect.add(new Integer(filter.getBlockSize() * 2));
                            inspect.add(new Integer((filter.getBlockSize() * 2) + 1));
                        }
                    }
                    if (log.isTraceEnabled()) {
                    	log.trace("GetBpmFromFile2(): " + inspecting);
                    	log.trace("GetBpmFromFile2(): est. bpm: " + String.valueOf(detectedbpm));
                    }
                    
                    ++pass;
                    if (task != null)
                    	task.setProgress(0.5f + ((float)pass) / numPasses);
                    
                }
            } catch (java.lang.OutOfMemoryError e) {
            	log.error("GetBpmFromFile2(): out of memory during detection");
            } catch (Exception e) {
                log.error("GetBpmFromFile2(): error", e);
            }
            if (detectedbpm != 0.0) {
                while (detectedbpm < BPM_DETECTOR_MINIMUM)
                    detectedbpm *= 2.0;
                while (detectedbpm > BPM_DETECTOR_MAXIMUM)
                    detectedbpm /= 2.0;
            }
        } catch (Exception e) {
        	log.error("GetBpmFromFile2(): error", e);
        }
        return detectedbpm;
    }

	static public double round(double val, int places) {
		long factor = (long)Math.pow(10,places);
		// shift the decimal the correct number of places to the right.
		val = val * factor;
		// round to the nearest integer.
		long tmp = Math.round(val);
		// shift the decimal the correct number of places back to the left.
		return (double)tmp / factor;
	}

	static public int extractDecimalPlaces(String input) {
		int returnval = 0;
		boolean pastcomma = false;
		for (int i = 0; 0 < input.length(); ++i) {
			if (input.charAt(i) == '.') 
				pastcomma = true;
			else if (pastcomma) {
				if (input.charAt(i) != '0')
					return returnval + 1;
				else
					returnval++;
			} else {
				if (input.charAt(i) != '0')
					return returnval;
			}
		}
		return returnval;
	}
	
}
