package com.mixshare.rapid_evolution.ui.widgets.common.table;

import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.data.record.search.SongGroupRecord;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.player.PlayerManager;
import com.mixshare.rapid_evolution.ui.model.table.RecordTableModelManager;
import com.mixshare.rapid_evolution.ui.widgets.profile.ProfileWidgetUI;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.gui.QMouseEvent;

abstract public class CommonRecordTableView extends CommonTableView {

	static private Logger log = Logger.getLogger(CommonRecordTableView.class);
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public CommonRecordTableView(RecordTableModelManager modelManager) {
		super(modelManager);				
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public RecordTableModelManager getRecordTableModelManager() { return (RecordTableModelManager)modelManager; }
	
    public Vector<SearchRecord> getSelectedRecords() {    	
    	List<QModelIndex> selectedIndexes = selectionModel().selectedRows();
    	Vector<SearchRecord> selectedRecords = new Vector<SearchRecord>(selectedIndexes.size());
    	for (QModelIndex index : selectedIndexes) {
    		QModelIndex sourceIndex = getProxyModel().mapToSource(index);    		
    		SearchRecord record = (SearchRecord)getRecordTableModelManager().getRecordForRow(sourceIndex.row());
    		if (record != null)
    			selectedRecords.add(record);
    	}
    	return selectedRecords;
    }
    	
	//////////////////////
	// ABSTRACT METHODS //
	//////////////////////
	
	abstract protected void selectionChanged();
	
	/////////////
	// METHODS //
	/////////////
	
	public void setupEventListeners() {
		super.setupEventListeners();
        selectionModel().selectionChanged.connect(this, "selectionChanged()");		
        selectionChanged(); // will trigger the context menu to be setup initially
	}   	
	
    protected void playRecords() {
    	if (log.isTraceEnabled())
    		log.trace("playRecords(): called...");
    	Vector<SearchRecord> selectedRecords = getSelectedRecords();
    	if (log.isTraceEnabled())
    		log.trace("playRecords(): selectedRecords=" + selectedRecords);
    	Vector<Integer> playlist = new Vector<Integer>();
    	for (SearchRecord record : selectedRecords) {
    		if (record instanceof SongGroupRecord) {
    			SongGroupRecord groupRecord = (SongGroupRecord)record;    			
    			Vector<SongRecord> groupSongs = groupRecord.getSongs();
    			for (SongRecord groupSong : groupSongs) {
    				Integer songId = groupSong.getUniqueId();
    				if (!playlist.contains(songId))
    					playlist.add(songId);
    			}
    		} else {
    			// song
    			SongRecord song = (SongRecord)record;
    			Integer songId = song.getUniqueId();
				if (!playlist.contains(songId))
					playlist.add(songId);
    		}
    	}
    	PlayerManager.playSongs(playlist);
    }

    protected void mouseDoubleClickEvent(QMouseEvent event) { mouseDoubleClickEvent(event, false); }
    protected void mouseDoubleClickEvent(QMouseEvent event, boolean bypass) {
    	try {
    		if (bypass) {
    			super.mouseDoubleClickEvent(event);
    		} else {
		    	QModelIndex proxyIndex = indexAt(event.pos());
		    	QModelIndex sourceIndex = getProxyModel().mapToSource(proxyIndex);
		    	Record record = getRecordTableModelManager().getRecordForRow(sourceIndex.row());
		    	if (log.isDebugEnabled())
		    		log.debug("mouseDoubleClickEvent(): record=" + record);
		    	Profile profile = Database.getProfile(record.getIdentifier());
		    	ProfileWidgetUI.instance.showProfile(profile);
    		}
    	} catch (Exception e) {
    		log.error("mouseDoubleClickEvent(): error", e);
    	}    	
    }
	
}
