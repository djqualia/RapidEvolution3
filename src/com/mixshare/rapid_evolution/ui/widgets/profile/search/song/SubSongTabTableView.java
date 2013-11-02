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
import com.mixshare.rapid_evolution.ui.model.table.RecordTableModelManager;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.profile.ProfileWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.RecordTabTableView;
import com.trolltech.qt.gui.QAction;
import com.trolltech.qt.gui.QIcon;

public class SubSongTabTableView extends RecordTabTableView {

	static private Logger log = Logger.getLogger(SubSongTabTableView.class);
	
    protected QAction addMixoutAction;;	
	
	public SubSongTabTableView(RecordTableModelManager modelManager) {
		super(modelManager);
		
        // setup actions
		addMixoutAction = new QAction(Translations.get("add_mixouts_menu_text"), this);
		addMixoutAction.triggered.connect(this, "addMixouts()");
		addMixoutAction.setIcon(new QIcon(RE3Properties.getProperty("menu_add_icon")));
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
        	added = true;
        }
        if (selectedRecords.size() > 1) {
        	if (added)
        		addAction(separator1);
        	addAction(mergeAction);
        }
    }	
    
    public void addMixouts() {
    	Vector<SearchRecord> selectedRecords = getSelectedRecords();
    	SongProfile currentSong = (SongProfile)ProfileWidgetUI.instance.getCurrentProfile();
    	for (SearchRecord searchRecord : selectedRecords) {    	
    		try {
    			SongRecord toSong = (SongRecord)searchRecord;
	        	MixoutIdentifier mixoutId = new MixoutIdentifier(currentSong.getUniqueId(), searchRecord.getUniqueId());
	        	float bpmDiff = Bpm.getBpmDifference(toSong.getStartBpm(), currentSong.getBpmEnd().isValid() ? currentSong.getEndBpm() : currentSong.getStartBpm());
	        	SubmittedMixout submittedMixout = new SubmittedMixout(mixoutId, bpmDiff);
	        	submittedMixout.setType(MixoutRecord.TYPE_TRANSITION);
	        	MixoutProfile mixoutProfile = (MixoutProfile)Database.addOrUpdate(submittedMixout);
	        	if (mixoutProfile != null) {
	        		if (RE3Properties.getBoolean("show_mixout_dialog_when_adding")) {
	        			MixoutDialog mixoutDlg = new MixoutDialog(RapidEvolution3UI.instance, mixoutProfile, MixoutDialog.MODE_ADD);
	        			mixoutDlg.show();
	        		}
	        	}
    		} catch (Exception e) {
    			log.error("addMixouts(): error", e);
    		}
    	}
    }
	
}
