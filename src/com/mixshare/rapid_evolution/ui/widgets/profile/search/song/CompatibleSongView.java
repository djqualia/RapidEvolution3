package com.mixshare.rapid_evolution.ui.widgets.profile.search.song;

import java.util.Vector;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.profile.search.song.Exclude;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.ui.model.column.AllColumns;
import com.mixshare.rapid_evolution.ui.model.search.SearchItemModel;
import com.mixshare.rapid_evolution.ui.model.table.RecordTableModelManager;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.profile.ProfileWidgetUI;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.gui.QAction;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QMessageBox;
import com.trolltech.qt.gui.QMouseEvent;

public class CompatibleSongView extends SubSongTabTableView implements AllColumns {

    protected QAction excludeAction;	
	
	public CompatibleSongView(RecordTableModelManager modelManager) {
		super(modelManager);
		
        // setup actions
		excludeAction = new QAction(Translations.get("add_exclude_menu_text"), this);
		excludeAction.triggered.connect(this, "addExclude()");
		excludeAction.setIcon(new QIcon(RE3Properties.getProperty("menu_add_exclude_icon")));		
	}	

    protected void mouseDoubleClickEvent(QMouseEvent event) {
    	QModelIndex proxyIndex = indexAt(event.pos());
    	QModelIndex sourceIndex = getProxyModel().mapToSource(proxyIndex);
    	if (SearchItemModel.isEditableSearchColumn(modelManager.getSourceColumnType(sourceIndex.column())))
    		super.mouseDoubleClickEvent(event, true);
    	else {
    		Record record = ((RecordTableModelManager)modelManager).getRecordForRow(sourceIndex.row());
    		if (record != null) {
    			if (modelManager.getSourceData(COLUMN_KEY_LOCK.getColumnId(), record).toString().equals("yes"))
    				ProfileWidgetUI.instance.getStageWidget().setKeyLockNoUpdate(true);
    			else if (modelManager.getSourceData(COLUMN_KEY_LOCK.getColumnId(), record).toString().equals("no"))
    				ProfileWidgetUI.instance.getStageWidget().setKeyLockNoUpdate(false);
    		}
    		super.mouseDoubleClickEvent(event);
    	}
    }   
    
    protected void selectionChanged() {    	
    	Vector<SearchRecord> selectedRecords = getSelectedRecords();
    	boolean containsPlayable = false;
    	for (SearchRecord searchRecord : selectedRecords) {
    		if (!searchRecord.isExternalItem()) {
    			containsPlayable = true;
    			break;
    		}
    	}
        removeAction(playAction);
        removeAction(addMixoutAction);
        removeAction(excludeAction);
        removeAction(separator1);
        removeAction(mergeAction);
        removeAction(separator2);
        removeAction(removeAction);
        boolean added = false;
        if (containsPlayable) {
        	addAction(playAction);
        	added = true;
        }
        if (selectedRecords.size() >= 1) {
        	addAction(addMixoutAction);
        	addAction(excludeAction);
        	added = true;
        }
        if (selectedRecords.size() > 1) {
        	if (added)
        		addAction(separator1);
        	addAction(mergeAction);
        }
    }	    
    
    public void addExclude() {
    	if (QMessageBox.question(this, Translations.get("add_exclude_title"), Translations.get("add_exclude_text"), QMessageBox.StandardButton.Yes, QMessageBox.StandardButton.No) != QMessageBox.StandardButton.Yes.value())
    		return;    	
    	Vector<SearchRecord> selectedRecords = getSelectedRecords();
    	SongProfile currentSong = (SongProfile)ProfileWidgetUI.instance.getCurrentProfile();
    	for (SearchRecord searchRecord : selectedRecords) {
    		currentSong.addExclude(new Exclude(currentSong.getUniqueId(), searchRecord.getUniqueId()));
    		getRecordTableModelManager().removeRow(searchRecord);
    	}
    	ProfileWidgetUI.instance.stageChanged();
		currentSong.save();
    }    
    
}
