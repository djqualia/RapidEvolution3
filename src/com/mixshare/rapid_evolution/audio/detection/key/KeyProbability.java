package com.mixshare.rapid_evolution.audio.detection.key;

import java.util.Arrays;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.audio.detection.key.filters.KeyProbabilityFilter;
import com.mixshare.rapid_evolution.audio.detection.key.filters.KeyProbabilitySet;
import com.mixshare.rapid_evolution.audio.detection.key.filters.MultiKeyProbabilityFilter;
import com.mixshare.rapid_evolution.audio.detection.key.filters.SingleKeyProbabilityFilter;
import com.mixshare.rapid_evolution.music.key.Key;
import com.mixshare.rapid_evolution.util.sort.SortObjectWrapper;

public class KeyProbability {
    
	static private Logger log = Logger.getLogger(KeyProbability.class);

	static private double analyze_segment_size = 0.1; // seconds;
	
	////////////
	// FIELDS //
	////////////
	
    private double segment_size;
    private double[] segment_totals = new double[12];    
    
    private Vector<KeyProbabilityFilter> filters = new Vector<KeyProbabilityFilter>();
    
    //////////////////
    // CONSTRUCTORS //
    //////////////////
    
    public KeyProbability(double segmentTime) { 
        filters.add(new SingleKeyProbabilityFilter(true, "ionian", 				new double[] { 0.2, 0.0, 0.12, 0.0, 0.16, 0.12, 0.0, 0.16, 0.0, 0.12, 0.0, 0.12 } ));
        filters.add(new SingleKeyProbabilityFilter(true, "lydian",				new double[] { 0.2, 0.0, 0.12, 0.0, 0.16, 0.0, 0.12, 0.16, 0.0, 0.12, 0.0, 0.12 } ));
        filters.add(new SingleKeyProbabilityFilter(true, "mixolydian",			new double[] { 0.2, 0.0, 0.12, 0.0, 0.16, 0.12, 0.0, 0.16, 0.0, 0.12, 0.12, 0.0 } ));
        filters.add(new MultiKeyProbabilityFilter(false, "aeolian",				new double[][] {
                new double[] { 0.2, 0.0, 0.12, 0.16, 0.0, 0.12, 0.0, 0.16, 0.12, 0.0, 0.12, 0.0 },
                new double[] { 0.2, 0.0, 0.12, 0.16, 0.0, 0.12, 0.0, 0.16, 0.12, 0.0, 0.0, 0.12 } // harmonic
        } ));        
        filters.add(new MultiKeyProbabilityFilter(false, "dorian",				new double[][] {
                new double[] { 0.2, 0.0, 0.12, 0.16, 0.0, 0.12, 0.0, 0.16, 0.0, 0.12, 0.12, 0.0 },
                new double[] { 0.2, 0.0, 0.12, 0.16, 0.0, 0.12, 0.0, 0.16, 0.0, 0.12, 0.0, 0.12 } // augmented
        } ));        
        filters.add(new SingleKeyProbabilityFilter(false, "phrygian",			new double[] { 0.2, 0.12, 0.0, 0.16, 0.0, 0.12, 0.0, 0.16, 0.12, 0.0, 0.12, 0.0 } ));
    }
    
    /////////////
    // GETTERS //
    /////////////
    
    public DetectedKey getDetectedKey() { return getDetectedKey(false); }
    public DetectedKey getDetectedKey(boolean logDetails) {
        try {
            Vector<SortObjectWrapper> results = new Vector<SortObjectWrapper>();
            double best_filter_probability = 0.0;
            double max_probability = 0.0;
            double min_probability = Double.MAX_VALUE;
            double total_probability = 0.0;  
            if (filters != null) {
	            for (int f = 0; f < filters.size(); ++f) {
	                KeyProbabilityFilter filter = (KeyProbabilityFilter)filters.get(f);
	                KeyProbabilitySet resultSet = filter.getNormalizedProbabilities();
	                resultSet.addResults(results);
	                double filter_probability = resultSet.getMaxProbability();
	                if (filter_probability > max_probability)
	                    max_probability = filter_probability;                
	                double min_filter_probability = resultSet.getMinProbability();
	                if (min_filter_probability < min_probability)
	                    min_probability = min_filter_probability;                
	                double total = resultSet.getTotalProbability();
	                total_probability += total;
	                if (total > best_filter_probability)
	                    best_filter_probability = total;                
	            }          
	            double average = total_probability / (filters.size() * 12);
	            double range = max_probability - min_probability;
	            double accuracy = (max_probability - average) / range;
	            if (accuracy < 0.0) accuracy = 0.0;
	            if (accuracy > 1.0) accuracy = 1.0;
	            
	            Object[] array = results.toArray();
	            Arrays.sort(array);
	            double[] buckets = new double[12];
	            double minor_pts = 0.0;
	            int num_minors = 0;
	            double major_pts = 0.0;
	            int num_majors = 0;
	            for (int i = array.length - 1; i >= 0; --i) {
	                SortObjectWrapper so = (SortObjectWrapper)array[i];
	                String keycode = Key.getKey(so.getObject().toString()).getKeyCode().toString();
	                if (logDetails && log.isTraceEnabled())
	                	log.trace("debugKeyScores(): key=" + so.getObject().toString() + ", score=" + so.getValue() + ", keycode=" + keycode);
	                StringBuffer keycode_int = new StringBuffer();
	                for (int c = 0; c < keycode.length(); ++c) {
	                    if (Character.isDigit(keycode.charAt(c)))
	                        keycode_int.append(keycode.charAt(c));                    
	                    else if ((keycode.charAt(c) == 'A')) {
	                        minor_pts += so.getValue();
	                        ++num_minors;
	                    } else if ((keycode.charAt(c) == 'B')) {
	                        major_pts += so.getValue();
	                        ++num_majors;
	                    }
	                }
	                int index = Integer.parseInt(keycode_int.toString()) - 1;
	                buckets[index] += so.getValue();                
	            }
	            if (logDetails && log.isTraceEnabled())
	            	log.trace("debugKeyScores(): key region buckets=");
	            double max_bucket = 0.0;
	            int max_index = -1;
	            for (int i = 0; i < 12; ++i) {
	            	if (logDetails && log.isTraceEnabled())
	            		log.trace("debugKeyScores(): \t" + (i + 1) + ": " + buckets[i]);
	                if (buckets[i] > max_bucket) {
	                    max_bucket = buckets[i];
	                    max_index = i + 1;
	                }
	            }
	            if (logDetails && log.isTraceEnabled())
	            	log.trace("debugKeyScores(): max region=" + max_index);
	            if (logDetails && log.isTraceEnabled())
	            	log.trace("debugKeyScores(): major score=" + major_pts);
	            if (logDetails && log.isTraceEnabled())
	            	log.trace("debugKeyScores(): minor score=" + minor_pts);
	            for (int i = array.length - 1; i >= 0; --i) {
	                SortObjectWrapper so = (SortObjectWrapper)array[i];
	                if (so.getValue() == 0.0)
	                	return new DetectedKey(Key.NO_KEY, Key.NO_KEY, 0.0);
	                Key predicted_key = (Key)so.getObject();
	                if (logDetails && log.isTraceEnabled()) 
	                	log.trace("debugKeyScores(): predicted key=" + predicted_key);                    
	                return new DetectedKey(predicted_key, Key.NO_KEY, accuracy);
	            }
            }
        } catch (Exception e) {
            log.error("debugKeyScores(): error creating debug output", e);
        }    
        return null;
    }
    
    public boolean hasNoData() {
        boolean all_zeros = true;
        for (int f = 0; f < filters.size(); ++f) {
            KeyProbabilityFilter filter = (KeyProbabilityFilter)filters.get(f);
            if (!filter.isAllZeros())
            	all_zeros = false;
        }
        return all_zeros;
    }    
    
    /////////////
    // METHODS //
    /////////////
    
    public void add(double[] totals, double time) {
        segment_size += time;
        for (int i = 0; i < totals.length; ++i)
            segment_totals[i] += totals[i];        
        if (segment_size > analyze_segment_size)
            processSegment();        
    }

    public void add(int[] totals, double time) {
        segment_size += time;
        for (int i = 0; i < totals.length; ++i)
            segment_totals[i] += totals[i];        
        if (segment_size > analyze_segment_size)
            processSegment();        
    }
    
    public void finish() {
        if (segment_size > 0)
        	processSegment();        
    }
    
    private void processSegment() {
        double total = 0;
        for (int i = 0; i < 12; ++i)
        	total += segment_totals[i];
        if (total > 0)
        	for (int i = 0; i < 12; ++i)
        		segment_totals[i] /= total;        
        for (int f = 0; f < filters.size(); ++f) {
            KeyProbabilityFilter filter = (KeyProbabilityFilter)filters.get(f);
            filter.add(segment_totals);
        }                    
        segment_size = 0;
        for (int i = 0; i < segment_totals.length; ++i) 
            segment_totals[i] = 0;        
    }
            
    private int safeindex(int index) {
        while (index < 1) index += 12;
        while (index > 12) index -= 12;
        return index;
    }
    
}
