package com.mixshare.rapid_evolution.ui.model.filter.playlist;

import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.index.event.IndexChangeListener;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.filter.playlist.PlaylistRecord;
import com.mixshare.rapid_evolution.data.submitted.SubmittedProfile;
import com.mixshare.rapid_evolution.util.sort.SortObjectWrapper;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QCompleter;

public class PlaylistCompleterFactory implements IndexChangeListener {

    static private Logger log = Logger.getLogger(PlaylistCompleterFactory.class);	
	    
	////////////
	// FIELDS //
	////////////
	
	private QCompleter cachedCompleter;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public PlaylistCompleterFactory() {
		Database.getPlaylistIndex().addIndexChangeListener(this);
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public QCompleter getCompleter() {
		if (cachedCompleter == null) {
			Vector<SortObjectWrapper> sortObjects = new Vector<SortObjectWrapper>(Database.getPlaylistIndex().getSize());
			for (int id : Database.getPlaylistIndex().getIds()) {
				PlaylistRecord playlistRecord = Database.getPlaylistIndex().getPlaylistRecord(id);
				if (playlistRecord != null)
					sortObjects.add(new SortObjectWrapper(playlistRecord.getPlaylistName(), -(playlistRecord.getNumArtistRecordsCached() + playlistRecord.getNumLabelRecordsCached() + playlistRecord.getNumReleaseRecordsCached() + playlistRecord.getNumSongRecordsCached())));
			}
			java.util.Collections.sort(sortObjects);		
		    List<String> wordList = new Vector<String>(sortObjects.size());
		    for (SortObjectWrapper sortObject : sortObjects) 
		    	wordList.add((String)sortObject.getObject());
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
