package com.mixshare.rapid_evolution.audio.detection.key.filters;

import com.mixshare.rapid_evolution.music.key.spiral.Point;
import com.mixshare.rapid_evolution.music.key.spiral.SpiralArray;
import com.mixshare.rapid_evolution.music.key.spiral.SpiralKey;
import com.mixshare.rapid_evolution.util.timing.Semaphore;

public class SpiralKeyProbabilityFilter implements KeyProbabilityFilter {

	private Semaphore addLock = new Semaphore(1);
	
    private double[] circle5ths_buffer = new double[12];
    private SpiralKey spiralKey;
    private double[] probability = new double[12];    
    
    public SpiralKeyProbabilityFilter(SpiralKey spiralKey) {
        this.spiralKey = spiralKey;
    }
    
    public void add(double[] values) {
    	try {
    		addLock.acquire();
	        for (int i = 0; i < 12; ++i) 
	        	circle5ths_buffer[i] = 0.0;
	        int index = 0;
	        for (int i = 0; i < 12; ++i) {
	            circle5ths_buffer[i] = values[index];
	            index += 7;
	            if (index >= 12)
	            	index -= 12;            
	        }
	        processData(circle5ths_buffer);
    	} catch (Exception e) { } finally {
    		addLock.release();
    	}    	
    }

    private void processData(double[] circle5ths_buffer) {
        Point centerEffect = SpiralArray.getCenterPoint(circle5ths_buffer);
        if (centerEffect != null) 
            spiralKey.getKeyProbabilities(centerEffect, probability);
    }
    
    public void add(int[] values) {
    	try {
    		addLock.acquire();
	        for (int i = 0; i < 12; ++i) circle5ths_buffer[i] = 0.0;
	        int index = 0;
	        for (int i = 0; i < 12; ++i) {
	            circle5ths_buffer[i] = values[index];
	            index += 7;
	            if (index >= 12) index -= 12;            
	        }
	        processData(circle5ths_buffer);
    	} catch (Exception e) { } finally {
    		addLock.release();
    	}
    }
            
    public KeyProbabilitySet getNormalizedProbabilities() {        
        double[] normalizedProbabilities = new double[12];
        double minimum = Double.MAX_VALUE;
        for (int i = 0; i < 12; ++i) {
            if (probability[i] < minimum) 
                minimum = probability[i];
        }
        int index = 0;
        for (int i = 0; i < 12; ++i) {
            normalizedProbabilities[index] = probability[i] - minimum;
            index += 7;
            if (index >= 12) index -= 12;
        }  
        double total = 0;
        for (int i = 0; i < 12; ++i)       
            total += normalizedProbabilities[i];        
        for (int i = 0; i < 12; ++i)
            normalizedProbabilities[i] = normalizedProbabilities[i] / total;
        return new KeyProbabilitySet(normalizedProbabilities, spiralKey.isMajor(), spiralKey.getType());
    }    
    
    public boolean isAllZeros() {
        for (int i = 0; i < 12; ++i)
            if (probability[i] > 0.0) return false;
        return true;        
    }    
}
