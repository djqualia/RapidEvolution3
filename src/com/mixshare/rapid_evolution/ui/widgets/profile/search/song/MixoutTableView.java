package com.mixshare.rapid_evolution.ui.widgets.profile.search.song;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.search.song.MixoutIdentifier;
import com.mixshare.rapid_evolution.data.profile.search.song.MixoutProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.data.record.search.song.MixoutRecord;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.data.submitted.search.song.SubmittedMixout;
import com.mixshare.rapid_evolution.music.bpm.Bpm;
import com.mixshare.rapid_evolution.ui.RapidEvolution3UI;
import com.mixshare.rapid_evolution.ui.dialogs.mixout.MixoutDialog;
import com.mixshare.rapid_evolution.ui.model.profile.search.song.SongMixoutTableItemModel;
import com.mixshare.rapid_evolution.ui.model.table.RecordTableModelManager;
import com.mixshare.rapid_evolution.ui.util.DragDropUtil;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.profile.ProfileWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.RecordTabTableView;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.gui.QAbstractItemView;
import com.trolltech.qt.gui.QAction;
import com.trolltech.qt.gui.QDropEvent;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QMessageBox;
import com.trolltech.qt.gui.QMouseEvent;

public class MixoutTableView extends RecordTabTableView {

	static private Logger log = Logger.getLogger(MixoutTableView.class);
	
    protected QAction removeMixoutAction;
    protected QAction editAction;
    protected QAction separator3;
	
	public MixoutTableView(RecordTableModelManager modelManager) {
		super(modelManager);
		setEditTriggers(QAbstractItemView.EditTrigger.DoubleClicked);
		setDropIndicatorShown(true);
		setAcceptDrops(true);
		
		removeMixoutAction = new QAction(Translations.get("remove_text"), this);        
		removeMixoutAction.triggered.connect(this, "removeMixouts()");
		removeMixoutAction.setIcon(new QIcon(RE3Properties.getProperty("menu_remove_icon")));
		
		editAction = new QAction(Translations.get("mixout_context_edit_text"), this);        
		editAction.triggered.connect(this, "editMixout()");
		editAction.setIcon(new QIcon(RE3Properties.getProperty("menu_edit_icon")));
	
		separator3 = new QAction("", this);
		separator3.setSeparator(true);		
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
        removeAction(removeMixoutAction);
        removeAction(editAction);
        removeAction(separator3);
        boolean added = false;
        if (selectedRecords.size() == 1) {
        	addAction(editAction);
        	addAction(separator3);
        }
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
        	addAction(removeMixoutAction);
        }        
    }	
    
    ////////////
    // EVENTS //
    ////////////
    
    protected void removeMixouts() {
		if (QMessageBox.question(this, Translations.get("dialog_remove_mixout_title"), Translations.get("dialog_remove_mixout_text"), QMessageBox.StandardButton.Yes, QMessageBox.StandardButton.No) != QMessageBox.StandardButton.Yes.value()) {
			return;
		}    			
		SongProfile currentSong = (SongProfile)ProfileWidgetUI.instance.getCurrentProfile();
    	Vector<SearchRecord> selectedRecords = getSelectedRecords();
    	for (SearchRecord searchRecord : selectedRecords)
    		currentSong.removeMixout(Database.getMixoutIndex().getMixoutRecord(new MixoutIdentifier(currentSong.getUniqueId(), searchRecord.getUniqueId())).getUniqueId());    	
    	currentSong.save();
    	selectionModel().clearSelection();
    }
    
    protected void editMixout() {
		SongProfile currentSong = (SongProfile)ProfileWidgetUI.instance.getCurrentProfile();
    	Vector<SearchRecord> selectedRecords = getSelectedRecords();
    	SearchRecord searchRecord = getSelectedRecords().get(0);
    	MixoutProfile mixoutProfile = Database.getMixoutIndex().getMixoutProfile(new MixoutIdentifier(currentSong.getUniqueId(), searchRecord.getUniqueId()));
    	new MixoutDialog(RapidEvolution3UI.instance, mixoutProfile, MixoutDialog.MODE_EDIT).show();
    }
    
    protected void mouseDoubleClickEvent(QMouseEvent event) {
    	QModelIndex proxyIndex = indexAt(event.pos());
    	QModelIndex sourceIndex = getProxyModel().mapToSource(proxyIndex);
    	if (SongMixoutTableItemModel.isEditableMixoutColumn(modelManager.getSourceColumnType(sourceIndex.column())))
    		super.mouseDoubleClickEvent(event, true);
    	else
    		super.mouseDoubleClickEvent(event);
    }
    	
	public void dropEvent(QDropEvent event) {				
		if (log.isTraceEnabled())
			log.trace("dropEvent(): event=" + event);
		if (DragDropUtil.containsSearchRecords(event.mimeData())) {
	    	SongProfile currentSong = (SongProfile)ProfileWidgetUI.instance.getCurrentProfile();
			Vector<SongRecord> songs = DragDropUtil.getSongs(event.mimeData());				
			for (SongRecord toSong : songs) {    	
	    		try {	    			
		        	MixoutIdentifier mixoutId = new MixoutIdentifier(currentSong.getUniqueId(), toSong.getUniqueId());
		        	float bpmDiff = Float.NaN;
		        	try { bpmDiff = Bpm.getBpmDifference(toSong.getStartBpm(), currentSong.getBpmEnd().isValid() ? currentSong.getEndBpm() : currentSong.getStartBpm()); } catch (Exception e) { }
		        	SubmittedMixout submittedMixout = new SubmittedMixout(mixoutId, bpmDiff);
		        	submittedMixout.setType(MixoutRecord.TYPE_TRANSITION);
		        	Database.addOrUpdate(submittedMixout);
	    		} catch (Exception e) {
	    			log.error("dropEvent(): error", e);
	    		}
	    	}
		}		
	}
	    
}
