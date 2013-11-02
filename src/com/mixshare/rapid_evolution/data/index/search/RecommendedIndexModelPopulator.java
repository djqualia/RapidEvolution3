package com.mixshare.rapid_evolution.data.index.search;

import java.util.Iterator;
import java.util.Vector;

import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.ui.model.ModelPopulatorInterface;

public class RecommendedIndexModelPopulator implements ModelPopulatorInterface {

	private SearchIndex searchIndex;
	
	public RecommendedIndexModelPopulator(SearchIndex searchIndex) {
		this.searchIndex = searchIndex;
	}
		
	public int getSize() { return searchIndex.getSizeExternalItems(); }
	
	public Iterator<Integer> getIdsIterator() {
		Vector<Integer> externalIds = new Vector<Integer>(searchIndex.getSizeExternalItems());		
		for (int id : searchIndex.getIds()) {
			SearchRecord searchRecord = (SearchRecord)searchIndex.getSearchRecord(id);
			if ((searchRecord != null) && (searchRecord.isExternalItem()))
				externalIds.add(searchRecord.getUniqueId());
		}
		return externalIds.iterator();
	}
	
}
