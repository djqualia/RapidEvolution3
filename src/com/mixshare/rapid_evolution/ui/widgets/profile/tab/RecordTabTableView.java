package com.mixshare.rapid_evolution.ui.widgets.profile.tab;

import java.util.Vector;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.search.ReleaseGroupRecord;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.data.record.search.SongGroupRecord;
import com.mixshare.rapid_evolution.data.record.search.release.ReleaseRecord;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.ui.dialogs.merge.MergeRecordsDialog;
import com.mixshare.rapid_evolution.ui.model.search.SearchItemModel;
import com.mixshare.rapid_evolution.ui.model.table.RecordTableModelManager;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.common.table.CommonRecordTableView;
import com.mixshare.rapid_evolution.ui.widgets.profile.ProfileWidgetUI;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.gui.QAbstractItemView;
import com.trolltech.qt.gui.QAction;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QMessageBox;
import com.trolltech.qt.gui.QMouseEvent;

public class RecordTabTableView extends CommonRecordTableView {

	////////////
	// FIELDS //
	////////////
	
    protected QAction playAction;
    protected QAction separator1;
    protected QAction mergeAction;
    protected QAction separator2;
    protected QAction removeAction;

    //////////////////
    // CONSTRUCTORS //
    //////////////////
    
	public RecordTabTableView(RecordTableModelManager modelManager) {
		super(modelManager);		
		
        setDragEnabled(true);
		setEditTriggers(QAbstractItemView.EditTrigger.DoubleClicked);
		
        // setup actions
        playAction = new QAction(Translations.get("play_text"), this);
        playAction.triggered.connect(this, "playRecords()");
        playAction.setIcon(new QIcon(RE3Properties.getProperty("menu_play_icon")));
    
        separator1 = new QAction("", this);
        separator1.setSeparator(true);
        mergeAction = new QAction(Translations.get("search_table_menu_merge"), this);        
        mergeAction.triggered.connect(this, "mergeRecords()");
        mergeAction.setIcon(new QIcon(RE3Properties.getProperty("menu_merge_icon")));

        separator2 = new QAction("", this);
        separator2.setSeparator(true);
        removeAction = new QAction(Translations.get("remove_text"), this);        
        removeAction.triggered.connect(this, "removeRecords()");        
        removeAction.setIcon(new QIcon(RE3Properties.getProperty("menu_remove_icon")));
        
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
        removeAction(separator1);
        removeAction(mergeAction);
        removeAction(separator2);
        removeAction(removeAction);
        boolean added = false;
        if (containsPlayable) {
        	addAction(playAction);
        	added = true;
        }
        if (selectedRecords.size() > 1) {
        	if (added)
        		addAction(separator1);
        	addAction(mergeAction);
        }
        if (selectedRecords.size() > 0) {
        	if (added)
        		addAction(separator2);
        	addAction(removeAction);
        }
    }	
    
    protected void mergeRecords() {
    	Vector<SearchRecord> selectedRecords = getSelectedRecords();
    	Vector<Record> records = new Vector<Record>();
    	for (SearchRecord searchRecord : selectedRecords)
    		records.add(searchRecord);
    	MergeRecordsDialog mergeRecordsDialog = new MergeRecordsDialog(this, records, modelManager.getTypeDescription());
    	if (mergeRecordsDialog.exec() == QDialog.DialogCode.Accepted.value()) {
    		mergeRecordsDialog.mergeRecords();  
    		clearSelection();
    	}    	    	
    }
    
    protected void removeRecords() {
		if (QMessageBox.question(this, Translations.get("dialog_remove_record_title"), Translations.get("dialog_remove_record_text"), QMessageBox.StandardButton.Yes, QMessageBox.StandardButton.No) != QMessageBox.StandardButton.Yes.value()) {
			return;
		}    			
    	Vector<SearchRecord> selectedRecords = getSelectedRecords();
    	for (SearchRecord searchRecord : selectedRecords) {
    		searchRecord.setDisabled(true);    		
    		if (searchRecord instanceof SongGroupRecord) {
    			SongGroupRecord songGroupRecord = (SongGroupRecord)searchRecord;
    			Vector<SongRecord> songs = songGroupRecord.getSongs();
    			for (SongRecord song : songs) {
    				if (song.isExternalItem())
    					song.setDisabled(true);
    			}
    		}
    		if (searchRecord instanceof ReleaseGroupRecord) {
    			ReleaseGroupRecord releaseGropRecord = (ReleaseGroupRecord)searchRecord;
    			Vector<ReleaseRecord> releases = releaseGropRecord.getReleases();
    			for (ReleaseRecord release : releases) {
    				if (release.isExternalItem())
    					release.setDisabled(true);
    			}
    		}
    	}
    	ProfileWidgetUI.instance.refresh();
    	selectionModel().clearSelection();
    }
    
    protected void mouseDoubleClickEvent(QMouseEvent event) {
    	QModelIndex proxyIndex = indexAt(event.pos());
    	QModelIndex sourceIndex = getProxyModel().mapToSource(proxyIndex);
    	if (SearchItemModel.isEditableSearchColumn(modelManager.getSourceColumnType(sourceIndex.column())))
    		super.mouseDoubleClickEvent(event, true);
    	else
    		super.mouseDoubleClickEvent(event);
    }    
    
}
