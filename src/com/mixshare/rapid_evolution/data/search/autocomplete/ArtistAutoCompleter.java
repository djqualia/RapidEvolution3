package com.mixshare.rapid_evolution.data.search.autocomplete;

import java.util.List;
import java.util.Vector;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.record.search.artist.ArtistRecord;
import com.mixshare.rapid_evolution.util.sort.SortObjectWrapper;

public class ArtistAutoCompleter extends AbstractAutocompleter {
	
	public List<String> getWordList() {
		Vector<SortObjectWrapper> sortObjects = new Vector<SortObjectWrapper>(Database.getArtistIndex().getSize());
		for (int id : Database.getArtistIndex().getIds()) {
			ArtistRecord artistRecord = Database.getArtistIndex().getArtistRecord(id);
			if (artistRecord != null)
				sortObjects.add(new SortObjectWrapper(artistRecord.getArtistName(), -artistRecord.getNumSongs()));
		}
		java.util.Collections.sort(sortObjects);		
	    List<String> wordList = new Vector<String>(sortObjects.size());
	    for (SortObjectWrapper sortObject : sortObjects) 
	    	wordList.add((String)sortObject.getObject());		
	    return wordList;
	}

}
