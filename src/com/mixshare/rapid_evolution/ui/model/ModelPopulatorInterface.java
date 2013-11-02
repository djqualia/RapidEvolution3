package com.mixshare.rapid_evolution.ui.model;

import java.util.Iterator;

/**
 * A model populator provides a set of records to populate a table or tree with.
 */
public interface ModelPopulatorInterface {

	public int getSize();
	
	public Iterator<Integer> getIdsIterator();
	
}
