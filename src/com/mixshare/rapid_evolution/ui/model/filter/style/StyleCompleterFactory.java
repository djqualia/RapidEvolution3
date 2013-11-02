package com.mixshare.rapid_evolution.ui.model.filter.style;


import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.filter.style.StyleIdentifier;
import com.mixshare.rapid_evolution.data.index.event.IndexChangeListener;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.filter.style.StyleRecord;
import com.mixshare.rapid_evolution.data.submitted.SubmittedProfile;
import com.mixshare.rapid_evolution.util.sort.SortObjectWrapper;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QCompleter;

public class StyleCompleterFactory implements IndexChangeListener {

    static private Logger log = Logger.getLogger(StyleCompleterFactory.class);	
	    
	////////////
	// FIELDS //
	////////////
	
	private QCompleter cachedCompleter;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public StyleCompleterFactory() {
		Database.getStyleIndex().addIndexChangeListener(this);
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public QCompleter getCompleter() {
		if (cachedCompleter == null) {
			Vector<SortObjectWrapper> sortObjects = new Vector<SortObjectWrapper>(Database.getStyleIndex().getSize());
			for (int id : Database.getStyleIndex().getIds()) {
				StyleRecord styleRecord = Database.getStyleIndex().getStyleRecord(id);
				if (styleRecord != null) {
					sortObjects.add(new SortObjectWrapper(styleRecord.getStyleName(), -(styleRecord.getNumArtistRecordsCached() + styleRecord.getNumLabelRecordsCached() + styleRecord.getNumReleaseRecordsCached() + styleRecord.getNumSongRecordsCached())));
					if (styleRecord.getNumDuplicateIds() > 0) {
						for (int d = 0; d < styleRecord.getNumDuplicateIds(); ++d) {
							StyleIdentifier dupId = (StyleIdentifier)Database.getStyleIndex().getIdentifierFromUniqueId(styleRecord.getDuplicateId(d));
							if (dupId != null)
								sortObjects.add(new SortObjectWrapper(dupId.toString(), -(styleRecord.getNumArtistRecordsCached() + styleRecord.getNumLabelRecordsCached() + styleRecord.getNumReleaseRecordsCached() + styleRecord.getNumSongRecordsCached() - 1)));															
						}
					}
					
				}
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
