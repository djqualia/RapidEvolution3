package com.mixshare.rapid_evolution.data.search.autocomplete;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.util.sort.SortObjectWrapper;

public class SongTitleAutoCompleter extends AbstractAutocompleter {

	public List<String> getWordList() {
		Map<String, SortObjectWrapper> mapSortObjects = new HashMap<String, SortObjectWrapper>(Database.getSongIndex().getSize());
		for (int id : Database.getSongIndex().getIds()) {
			SongRecord songRecord = Database.getSongIndex().getSongRecord(id);
			if (songRecord != null) {					
				SortObjectWrapper wrapper = mapSortObjects.get(songRecord.getTitle());
				if (wrapper == null) {
					wrapper = new SortObjectWrapper(songRecord.getTitle(), -1);
				} else {
					wrapper.setValue(wrapper.getValue() - 1);
				}
				mapSortObjects.put(songRecord.getTitle(), wrapper);
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
