package com.mixshare.rapid_evolution.ui.widgets;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.audio.tags.TagEventListener;
import com.mixshare.rapid_evolution.audio.tags.TagManager;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.index.event.IndexChangeListener;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.data.submitted.SubmittedProfile;
import com.mixshare.rapid_evolution.data.submitted.search.song.SubmittedSong;
import com.mixshare.rapid_evolution.ui.dialogs.filter.AddFilterDegreeDialog;
import com.mixshare.rapid_evolution.ui.dialogs.taskstatus.TaskStatusDialog;
import com.mixshare.rapid_evolution.ui.updaters.StatusMessageSetter;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.workflow.event.DetectionTaskFinishedListener;
import com.mixshare.rapid_evolution.workflow.event.MiningTaskFinishedListener;
import com.mixshare.rapid_evolution.workflow.event.OrganizeSongListener;
import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QStatusBar;
import com.trolltech.qt.gui.QToolButton;
import com.trolltech.qt.gui.QWidget;

public class RE3StatusBar extends QStatusBar implements IndexChangeListener, MiningTaskFinishedListener, DetectionTaskFinishedListener, TagEventListener, OrganizeSongListener {

	static private Logger log = Logger.getLogger(RE3StatusBar.class);	
	
	static public RE3StatusBar instance = null;
	
	////////////
	// FIELDS //
	////////////
	
	private QToolButton statusButton = new QToolButton(this);
	
	private TaskStatusDialog taskStatusDialog;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public RE3StatusBar(QWidget widget) {
		super(widget);
		Database.getArtistIndex().addIndexChangeListener(this);
		Database.getLabelIndex().addIndexChangeListener(this);
		Database.getReleaseIndex().addIndexChangeListener(this);
		Database.getSongIndex().addIndexChangeListener(this);
		Database.getStyleIndex().addIndexChangeListener(this);
		Database.getTagIndex().addIndexChangeListener(this);
		Database.getPlaylistIndex().addIndexChangeListener(this);
		TagManager.addTagEventListener(this);
		statusButton.setText(Translations.get("status_bar_status_button"));
		addPermanentWidget(statusButton);
		statusButton.clicked.connect(this, "openStatusWindow(Boolean)");
		instance = this;
		taskStatusDialog = new TaskStatusDialog(widget);
	}
	
	/////////////
	// METHODS //
	/////////////
	
    public void showStatusMessage(String message) {
    	if (!RapidEvolution3.isTerminated)
    		QApplication.invokeAndWait(new StatusMessageSetter(this, message));
    }
    public void showStatusMessageLater(String message) {
    	if (!RapidEvolution3.isTerminated)
    		QApplication.invokeLater(new StatusMessageSetter(this, message));
    }
    private void openStatusWindow(Boolean clicked) {
    	try {
	    	taskStatusDialog.show();
	    	taskStatusDialog.raise();
	    	taskStatusDialog.activateWindow();   
	    	taskStatusDialog.startRefreshTask();
    	} catch (Exception e) {
    		log.error("openStatusWindow(): error", e);
    	}
    }
	
    ////////////
    // EVENTS //
    ////////////
    
	public void addedRecord(Record record, SubmittedProfile submittedProfile) {
		if (!RE3Properties.getBoolean("status_bar_show_external_record_updates") && (record instanceof SearchRecord)) {
			if (((SearchRecord)record).isExternalItem())
				return;				
		}
		StringBuffer message = new StringBuffer();
		message.append(Translations.get("status_bar_tags_added_prefix"));
		message.append(" ");
		message.append(record.getIdentifier().getTypeDescription());
		message.append(" \"");
		message.append(record.toString());
		message.append("\"");
		showStatusMessage(message.toString());
	}
	
	public void updatedRecord(Record record) {
		/*
		StringBuffer message = new StringBuffer();
		message.append("Updated ");
		message.append(record.getIdentifier().getTypeDescription());
		message.append(" \"");
		message.append(record.toString());
		message.append("\"");
		showStatusMessage(message.toString());
		*/
	}
	
	public void removedRecord(Record record) {
		if (!RE3Properties.getBoolean("status_bar_show_external_record_updates") && (record instanceof SearchRecord)) {
			if (((SearchRecord)record).isExternalItem())
				return;				
		}
		StringBuffer message = new StringBuffer();
		message.append(Translations.get("status_bar_tags_deleted_prefix"));
		message.append(" ");
		message.append(record.getIdentifier().getTypeDescription());
		message.append(" \"");
		message.append(record.toString());
		message.append("\"");
		showStatusMessage(message.toString());
	}
	
	public void finishedMiningTask(String status) {
		if (RE3Properties.getBoolean("status_bar_show_data_mining_updates"))
			showStatusMessage(status);
	}
	
	public void finishedDetectionTask(String status) {
		if (RE3Properties.getBoolean("status_bar_show_detection_updates"))
			showStatusMessage(status);
	}
	
	public void tagsRead(SubmittedSong song) {
		if (RE3Properties.getBoolean("status_bar_show_tag_reads"))
			showStatusMessage(Translations.get("status_bar_tags_read_prefix") + " \"" + song.getSongFilename() + "\"");
	}
	
	public void tagsWritten(SongProfile song) {
		if (RE3Properties.getBoolean("status_bar_show_tag_writes"))
			showStatusMessage(Translations.get("status_bar_tags_written_prefix") + " \"" + song.toString() + "\"");
		
	}
	
	public void songOrganized(SongProfile song) {
		if (RE3Properties.getBoolean("status_bar_show_organized_songs"))
			showStatusMessage(Translations.get("status_bar_organized_file_prefix") + " \"" + song.toString() + "\"");		
	}
	
	public void songRenamed(SongProfile song) {
		if (RE3Properties.getBoolean("status_bar_show_rename_songs"))
			showStatusMessage(Translations.get("status_bar_renamed_file_prefix") + " \"" + song.toString() + "\"");		
		
	}
	
	
}
