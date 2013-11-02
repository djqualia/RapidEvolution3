package com.mixshare.rapid_evolution.ui.model.search.artist;

import java.util.List;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.index.event.IndexChangeListener;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.search.autocomplete.ArtistAutoCompleter;
import com.mixshare.rapid_evolution.data.submitted.SubmittedProfile;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QCompleter;

public class ArtistCompleterFactory implements IndexChangeListener {

    static private Logger log = Logger.getLogger(ArtistCompleterFactory.class);	
	    
	////////////
	// FIELDS //
	////////////
	
	private QCompleter cachedCompleter;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public ArtistCompleterFactory() {
		Database.getArtistIndex().addIndexChangeListener(this);
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public QCompleter getCompleter() {
		if (cachedCompleter == null) {
		    List<String> wordList = new ArtistAutoCompleter().getWordList();
		    if (log.isTraceEnabled())
		    	log.trace("getCompleter(): word list=" + wordList);
			QCompleter result = new QCompleter(wordList);
			result.setCaseSensitivity(Qt.CaseSensitivity.CaseInsensitive);
			cachedCompleter = result;			
		}
		return cachedCompleter;
	}
	
	////////////
	// EVENTS //
	////////////
	
	public void addedRecord(Record record, SubmittedProfile submittedProfile) {
		cachedCompleter = null;
	}
	
	public void updatedRecord(Record record) {
		cachedCompleter = null;
	}
	
	public void removedRecord(Record record) {
		cachedCompleter = null;
	}
	
}
