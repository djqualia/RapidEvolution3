package com.mixshare.rapid_evolution.audio.detection.bpm;

import com.mixshare.rapid_evolution.RapidEvolution3;

public class BpmFilter {

	////////////
	// FIELDS //
	////////////
	
	private int blockSize;
	private boolean[] useArray = null;
	private BpmComb bpmComb;
	private int[] dataPointers = null;
	private long[] totalCounted = null;
	private double[] totalDiff = null;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public BpmFilter(int blockSize, boolean[] useArray, BpmComb bpmComb) {
		this.blockSize = blockSize;
		this.useArray = useArray;
		this.bpmComb = bpmComb;
		dataPointers = new int[useArray.length];
		totalCounted = new long[useArray.length];
		totalDiff = new double[useArray.length];
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public int getBlockSize() { return blockSize; }		
	public boolean[] getUseArray() { return useArray; }	
	public int[] getDataPointers() { return dataPointers; }	
	public long[] getTotalCounted() { return totalCounted; }
	public double[] getTotalDiff() { return totalDiff; }
	
	/////////////
	// METHODS //
	/////////////
	
	public void process() {
		for (int i = 0; i < useArray.length; ++i) {
			if (RapidEvolution3.isTerminated)
				return;
			if (useArray[i]) {
				int power = (int)Math.pow(2.0, i);
				while ((dataPointers[i] + (blockSize * 2 * power)) <= bpmComb.getDataStream().size()) {
					int blockPower = blockSize * power;
					totalCounted[i] += blockPower;
					for (int j = 0; j < blockPower; ++j) {
						int indexa = j + dataPointers[i];
						int indexb = j + dataPointers[i] + blockPower;
						totalDiff[i] += Math.abs(Math.abs(bpmComb.getDataStream().get(indexa)) - Math.abs(bpmComb.getDataStream().get(indexb)));
					}
					dataPointers[i] += blockPower;
				}
			} else {
				dataPointers[i] = bpmComb.getDataStream().size();
			}
		}
	}
	
}
