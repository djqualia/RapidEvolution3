package com.mixshare.rapid_evolution.data.search.autocomplete;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.record.search.release.ReleaseRecord;
import com.mixshare.rapid_evolution.util.sort.SortObjectWrapper;

public class ReleaseTitleAutoCompleter extends AbstractAutocompleter {

	public List<String> getWordList() {
		Map<String, SortObjectWrapper> mapSortObjects = new HashMap<String, SortObjectWrapper>(Database.getReleaseIndex().getSize());
		for (int id : Database.getReleaseIndex().getIds()) {
			ReleaseRecord releaseRecord = Database.getReleaseIndex().getReleaseRecord(id);
			if (releaseRecord != null) {					
				SortObjectWrapper wrapper = mapSortObjects.get(releaseRecord.getReleaseTitle());
				if (wrapper == null) {
					wrapper = new SortObjectWrapper(releaseRecord.getReleaseTitle(), -releaseRecord.getNumSongs());
				} else {
					wrapper.setValue(wrapper.getValue() - releaseRecord.getNumSongs());
				}
				mapSortObjects.put(releaseRecord.getReleaseTitle(), wrapper);
			}
		}
		Vector<SortObjectWrapper> sortObjects = new Vector<SortObjectWrapper>(mapSortObjects.size());
		for (SortObjectWrapper wrapper : mapSortObjects.values())
			sortObjects.add(wrapper);
		java.util.Collections.sort(sortObjects);		
	    List<String> wordList = new Vector<String>(sortObjects.size());
	    for (SortObjectWrapper sortObject : sortObjects) 
	    	wordList.add((String)sortObject.getObject());
	    return wordList;
	}
	
}
