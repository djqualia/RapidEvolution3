package com.mixshare.rapid_evolution.audio.detection.bpm;

import java.util.Vector;

import com.mixshare.rapid_evolution.RapidEvolution3;

public class BpmComb {

	////////////
	// FIELDS //
	////////////
	
	private boolean[] useArray = null;
	private Vector<Double> datastream = new Vector<Double>();
	private Vector<BpmFilter> bpmFilters = new Vector<BpmFilter>();
	private boolean normalized = false;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public BpmComb(boolean[] useArray) {
		this.useArray = useArray;
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public Vector<Double> getDataStream() { return datastream; }

	public Vector<BpmFilter> getBpmFilters() { return bpmFilters; }
	
	/////////////
	// METHODS //
	/////////////
	
	public void addBlock(int blocksize) {
		boolean alreadyexists = false;
		for (int i = 0; i < bpmFilters.size(); i++)
			if (bpmFilters.get(i).getBlockSize() == blocksize)
				alreadyexists = true;		
		if (!alreadyexists)
			bpmFilters.add(new BpmFilter(blocksize, useArray, this));
	}

	public void pushData(double[] data, int size) {
		for (int j = 0; j < size; ++j)
			datastream.add(data[j]);
		int lowestpointer = -1;
		for (int i = 0; i < bpmFilters.size(); ++i) {
			if (RapidEvolution3.isTerminated)
				return;
			BpmFilter filter = bpmFilters.get(i);
			filter.process();
			for (int z = 0; z < filter.getUseArray().length; ++z)
				if ((lowestpointer == -1) || (filter.getDataPointers()[z] < lowestpointer))
					lowestpointer = filter.getDataPointers()[z];			
		}
		if (lowestpointer > (datastream.size() / 2)) {
			Vector<Double> newdata = new Vector<Double>(datastream.size() - lowestpointer);
			for (int i = lowestpointer; i < datastream.size(); ++i)
				newdata.add(datastream.get(i));
			datastream = newdata;
			for (int i = 0; i < bpmFilters.size(); ++i) {
				BpmFilter filter = bpmFilters.get(i);
				for (int z = 0; z < filter.getUseArray().length; ++z)
					filter.getDataPointers()[z] -= lowestpointer;
			}
		}
	}

	public void normalize() {
		if (normalized)
			return;
		long max = -1;
		int maxindex = -1;
		for (int i = 0; i < bpmFilters.size(); ++i) {
			BpmFilter record = bpmFilters.get(i);
			for (int z = 0; z < record.getUseArray().length; z++) {
				if ((maxindex == -1) || (record.getTotalCounted()[z] > max)) {
					max = record.getTotalCounted()[z];
					maxindex = i;
				}
			}
		}
		for (int i = 0; i < bpmFilters.size(); ++i) {
			BpmFilter record = bpmFilters.get(i);
			for (int z = 0; z < record.getUseArray().length; z++) {
				double ratio = ((double) record.getTotalCounted()[z]) / ((double) max);
				if (ratio != 0.0)
					record.getTotalDiff()[z] = (long) (record.getTotalDiff()[z] / ratio);
			}
		}
		normalized = true;
	}

	public void reset(boolean[] useArray) {
		bpmFilters = new Vector<BpmFilter>();
		normalized = false;
		datastream = new Vector<Double>();
		this.useArray = useArray;
	}

}