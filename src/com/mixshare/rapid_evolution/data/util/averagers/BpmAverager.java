package com.mixshare.rapid_evolution.data.util.averagers;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.music.bpm.Bpm;

/**
 * Given a set of BPM values, this averager will attempt to pick the BPM with the minimum
 * average distance to the other BPMs, ruling out outliers.  When a tie exists, the last
 * modified date is used.
 * 
 * Note: a direct average is not possible due to incorrect/outlier results.  For example,
 * 120,121,122,75 would become 109.5, which is definitely incorrect.  121 would be a better
 * approximation, after ruling out 75...
 */
public class BpmAverager {

    private static Logger log = Logger.getLogger(BpmAverager.class);
    private static float OUTLIER_THRESHOLD_DIFFERENCE = 8.0f;
    
    Vector<BpmRecord> total_records = null;
    int num_values = 0;
    int num_zeros = 0;
    private boolean ignore_0s = true;

    public BpmAverager() {
        if (log.isDebugEnabled()) log.debug("BpmAverager(): new bpm averager created");
        total_records = new Vector<BpmRecord>();
    }
    public BpmAverager(boolean ignore_zero_values) {
        if (log.isDebugEnabled()) log.debug("BpmAverager(): new bpm averager created");
    	ignore_0s = ignore_zero_values;
    	total_records = new Vector<BpmRecord>();
    }
    public BpmAverager(int size) {
        if (log.isDebugEnabled()) log.debug("BpmAverager(): new bpm averager created");
        total_records = new Vector<BpmRecord>(size);
    }
    public BpmAverager(int size, boolean ignore_zero_values) {
        if (log.isDebugEnabled()) log.debug("BpmAverager(): new bpm averager created");
    	ignore_0s = ignore_zero_values;
    	total_records = new Vector<BpmRecord>(size);
    }
    
    public void addValue(float bpm, long timestamp, int accuracy) {
        if (log.isDebugEnabled()) log.debug("addValue(): adding bpm: " + bpm + ", timestamp: " + timestamp + ", accuracy: " + accuracy);
        if (bpm <= 0.0) {
            if (ignore_0s) return;
            ++num_zeros;
            return;
        } else {
        	total_records.add(new BpmRecord(bpm, timestamp, accuracy));
        	++num_values;
        }
    }

    private transient float computedAvgBpm = 0.0f;
    public float getAverageValue() {
    	if (computedAvgBpm == 0.0f) {
	        if (num_zeros > num_values) return 0.0f;
	        if (num_values == 0.0) return 0.0f;
	        // determine max accuracy
	        float max_accuracy = 0.0f;
	        for (int bpm_iter = 0; bpm_iter < total_records.size(); ++bpm_iter) {
	            BpmRecord bpmrecord = (BpmRecord)total_records.get(bpm_iter);
	            if (bpmrecord.getAccuracy() > max_accuracy) max_accuracy = bpmrecord.getAccuracy(); 
	        }        
	        // remove all less than max accuracy:
	        for (int bpm_iter = 0; bpm_iter < total_records.size(); ++bpm_iter) {
	            BpmRecord bpmrecord = (BpmRecord)total_records.get(bpm_iter);
	            if (bpmrecord.getAccuracy() < max_accuracy)
	            	total_records.removeElementAt(bpm_iter--);
	        }        
	        if (total_records.size() == 1) {
	        	BpmRecord bpmrecord = (BpmRecord)total_records.get(0);
	        	return bpmrecord.getBpm();
	        }        	
	        // determine avg distance for all bpms
	        float totalAvgDistance = 0.0f;
	        for (int bpm_iter = 0; bpm_iter < total_records.size(); ++bpm_iter) {
	            BpmRecord bpmrecord = (BpmRecord)total_records.get(bpm_iter);
	            float this_average_distance = 0.0f;
	            for (int avg_iter = 0; avg_iter < total_records.size(); ++avg_iter) {
	                BpmRecord compare_bpmrecord = (BpmRecord)total_records.get(avg_iter);
	                if (compare_bpmrecord != bpmrecord) {
		                float bpmdiff = Bpm.getBpmDifference(compare_bpmrecord.getBpm(), bpmrecord.getBpm());
		                if (bpmdiff < 0)
		                	bpmdiff = Bpm.getBpmDifference(bpmrecord.getBpm(), compare_bpmrecord.getBpm());
		                this_average_distance += bpmdiff;
	                }
	            }
	            bpmrecord.setAvgDistance(this_average_distance / (total_records.size() - 1));
	            totalAvgDistance += bpmrecord.getAvgDistance();
	        }
	        totalAvgDistance /= total_records.size();
	        if (log.isDebugEnabled())
	        	log.debug("getAverageValue(): totalAvgDistance=" + totalAvgDistance);
	        // remove all those > avg total distance
	        boolean removed = false;
	        for (int bpm_iter = 0; bpm_iter < total_records.size(); ++bpm_iter) {
	            BpmRecord bpmrecord = (BpmRecord)total_records.get(bpm_iter);
	            if ((bpmrecord.getAvgDistance() > totalAvgDistance) && (bpmrecord.getAvgDistance() > OUTLIER_THRESHOLD_DIFFERENCE)) {
	            	if (log.isDebugEnabled())
	            		log.debug("getAverageValue(): removing bpmrecord=" + bpmrecord.getBpm());
	            	total_records.removeElementAt(bpm_iter--);
	            	removed = true;
	            }
	        }
	        if (removed) {
	        	// need to recompute distances
		        for (int bpm_iter = 0; bpm_iter < total_records.size(); ++bpm_iter) {
		            BpmRecord bpmrecord = (BpmRecord)total_records.get(bpm_iter);
		            float this_average_distance = 0.0f;
		            for (int avg_iter = 0; avg_iter < total_records.size(); ++avg_iter) {
		                BpmRecord compare_bpmrecord = (BpmRecord)total_records.get(avg_iter);
		                if (compare_bpmrecord != bpmrecord) {
			                float bpmdiff = Bpm.getBpmDifference(compare_bpmrecord.getBpm(), bpmrecord.getBpm());
			                if (bpmdiff < 0)
			                	bpmdiff = Bpm.getBpmDifference(bpmrecord.getBpm(), compare_bpmrecord.getBpm());
			                this_average_distance += bpmdiff;
		                }
		            }
		            bpmrecord.setAvgDistance(this_average_distance / (total_records.size() - 1));
		        }
	        }
	        // finally determine closest from what's left
	        float closest_bpm = 0.0f;
	        float closest_average_distance = 0.0f;
	        long closest_timestamp = 0;
	        for (int bpm_iter = 0; bpm_iter < total_records.size(); ++bpm_iter) {
	            BpmRecord bpmrecord = (BpmRecord)total_records.get(bpm_iter);
	            float this_average_distance = bpmrecord.getAvgDistance();
	            boolean set = false;
	            if ((closest_bpm == 0.0) || (this_average_distance < closest_average_distance)) {
	            	set = true;
	            } else if (this_average_distance == closest_average_distance) {
	                if (closest_timestamp < bpmrecord.getTimestamp()) {
	                	set = true;
	                }
	            }
	            if (set) {
	                closest_bpm = bpmrecord.getBpm();
	                closest_average_distance = this_average_distance;
	                closest_timestamp = bpmrecord.getTimestamp();            	
	            }
	        }
	        // determine the best fit range
	        float eighth_closest = closest_bpm / 8;
	        float fourth_closest = closest_bpm / 4;
	        float half_closest = closest_bpm / 2;
	        float float_closest = closest_bpm * 2;
	        float quadruple_closest = closest_bpm * 4;
	        float octuple_closest = closest_bpm * 8;
	        float eighth_distance = 0;
	        float fourth_distance = 0;
	        float half_distance = 0;
	        float normal_distance = 0;
	        float float_distance = 0;
	        float quadruple_distance = 0;
	        float octuple_distance = 0;
	        for (int avg_iter = 0; avg_iter < total_records.size(); ++avg_iter) {
	            BpmRecord compare_bpmrecord = (BpmRecord)total_records.get(avg_iter);
	            eighth_distance += Math.abs(eighth_closest - compare_bpmrecord.getBpm());
	            fourth_distance += Math.abs(fourth_closest - compare_bpmrecord.getBpm());
	            half_distance += Math.abs(half_closest - compare_bpmrecord.getBpm());
	            normal_distance += Math.abs(closest_bpm - compare_bpmrecord.getBpm());
	            float_distance += Math.abs(float_closest - compare_bpmrecord.getBpm());
	            quadruple_distance += Math.abs(quadruple_closest - compare_bpmrecord.getBpm());
	            octuple_distance += Math.abs(octuple_closest - compare_bpmrecord.getBpm());
	        }
	        float min_distance = normal_distance;
	        float avgbpm = closest_bpm;
	        if ((eighth_distance < min_distance)) {
	        	min_distance = eighth_distance;
	        	avgbpm = eighth_closest;
	        }
	        if ((fourth_distance < min_distance)) {
	        	min_distance = fourth_distance;
	        	avgbpm = fourth_closest;
	        }
	        if ((half_distance < min_distance)) {
	        	min_distance = half_distance;
	        	avgbpm = half_closest;
	        }        
	        if ((float_distance < min_distance)) {
	        	min_distance = float_distance;
	        	avgbpm = float_closest;
	        }        
	        if ((quadruple_distance < min_distance)) {
	        	min_distance = quadruple_distance;
	        	avgbpm = quadruple_closest;
	        }        
	        if ((octuple_distance < min_distance)) {
	        	min_distance = octuple_distance;
	        	avgbpm = octuple_closest;
	        }                
	        if (log.isDebugEnabled()) log.debug("getAverageValue(): returning bpm: " + avgbpm);	        
	        computedAvgBpm = avgbpm;
    	}
    	return computedAvgBpm;
    }

    
    class BpmRecord {
        private float bpm;
        private long timestamp;
        private int accuracy;
        private float avgDistance;
        public BpmRecord(float _bpm, long _timestamp, int _accuracy) {
            bpm = _bpm;
            accuracy = _accuracy;
            timestamp = _timestamp;            
        }
        public float getBpm() { return bpm; };
        public int getAccuracy() { return accuracy; }
        public long getTimestamp() { return timestamp; }
        public void setAvgDistance(float avgDistance) { this.avgDistance = avgDistance; }
        public float getAvgDistance() { return avgDistance; }
    }
}
