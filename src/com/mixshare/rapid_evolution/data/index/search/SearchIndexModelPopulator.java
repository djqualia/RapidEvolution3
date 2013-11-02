package com.mixshare.rapid_evolution.data.index.search;

import java.util.Iterator;
import java.util.Vector;

import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.ui.model.ModelPopulatorInterface;

/**
 * This class filters out external items for the main search views (artist/label/releas/song), to help reduce memory
 * usage as the source model will store all column values in memory...
 */
public class SearchIndexModelPopulator implements ModelPopulatorInterface {

	private SearchIndex searchIndex;
	
	public SearchIndexModelPopulator(SearchIndex searchIndex) {
		this.searchIndex = searchIndex;
	}
		
	public int getSize() { return searchIndex.getSizeInternalItems(); }
	
	public Iterator<Integer> getIdsIterator() {
		Vector<Integer> internalIds = new Vector<Integer>(searchIndex.getSizeInternalItems());		
		for (int id : searchIndex.getIds()) {
			SearchRecord searchRecord = (SearchRecord)searchIndex.getSearchRecord(id);
			if ((searchRecord != null) && (!searchRecord.isExternalItem()))
				internalIds.add(searchRecord.getUniqueId());
		}
		return internalIds.iterator();
	}

	
	
}
