package com.mixshare.rapid_evolution.data.search.autocomplete;

import java.util.List;
import java.util.Vector;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.record.search.label.LabelRecord;
import com.mixshare.rapid_evolution.util.sort.SortObjectWrapper;

public class LabelAutoCompleter extends AbstractAutocompleter {
	
	public List<String> getWordList() {
		Vector<SortObjectWrapper> sortObjects = new Vector<SortObjectWrapper>(Database.getLabelIndex().getSize());
		for (int id : Database.getLabelIndex().getIds()) {
			LabelRecord labelRecord = Database.getLabelIndex().getLabelRecord(id);
			if (labelRecord != null)
				sortObjects.add(new SortObjectWrapper(labelRecord.getLabelName(), -labelRecord.getNumSongs()));
		}
		java.util.Collections.sort(sortObjects);		
	    List<String> wordList = new Vector<String>(sortObjects.size());
	    for (SortObjectWrapper sortObject : sortObjects) 
	    	wordList.add((String)sortObject.getObject());	
	    return wordList;
	}

}
