package com.mixshare.rapid_evolution.ui.model.filter.tag;

import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.filter.tag.TagIdentifier;
import com.mixshare.rapid_evolution.data.index.event.IndexChangeListener;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.filter.tag.TagRecord;
import com.mixshare.rapid_evolution.data.submitted.SubmittedProfile;
import com.mixshare.rapid_evolution.util.sort.SortObjectWrapper;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QCompleter;

public class TagCompleterFactory implements IndexChangeListener {

    static private Logger log = Logger.getLogger(TagCompleterFactory.class);	
	    
	////////////
	// FIELDS //
	////////////
	
	private QCompleter cachedCompleter;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public TagCompleterFactory() {
		Database.getTagIndex().addIndexChangeListener(this);
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public QCompleter getCompleter() {
		if (cachedCompleter == null) {
			Vector<SortObjectWrapper> sortObjects = new Vector<SortObjectWrapper>(Database.getTagIndex().getSize());
			for (int id : Database.getTagIndex().getIds()) {
				TagRecord tagRecord = Database.getTagIndex().getTagRecord(id);
				if (tagRecord != null) {
					sortObjects.add(new SortObjectWrapper(tagRecord.getTagName(), -(tagRecord.getNumArtistRecordsCached() + tagRecord.getNumLabelRecordsCached() + tagRecord.getNumReleaseRecordsCached() + tagRecord.getNumSongRecordsCached())));
					if (tagRecord.getNumDuplicateIds() > 0) {
						for (int d = 0; d < tagRecord.getNumDuplicateIds(); ++d) {
							TagIdentifier dupId = (TagIdentifier)Database.getTagIndex().getIdentifierFromUniqueId(tagRecord.getDuplicateId(d));
							if (dupId != null)
								sortObjects.add(new SortObjectWrapper(dupId.toString(), -(tagRecord.getNumArtistRecordsCached() + tagRecord.getNumLabelRecordsCached() + tagRecord.getNumReleaseRecordsCached() + tagRecord.getNumSongRecordsCached() - 1)));															
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
