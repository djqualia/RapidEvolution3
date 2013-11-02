package com.mixshare.rapid_evolution.ui.widgets.search.song;

import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.ui.RapidEvolution3UI;
import com.mixshare.rapid_evolution.ui.model.profile.details.SearchDetailsModelManager;
import com.mixshare.rapid_evolution.ui.model.profile.search.song.SongDetailsModelManager;
import com.mixshare.rapid_evolution.ui.model.search.SearchModelManager;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.profile.search.song.HarmonicColoringItemDelegate;
import com.mixshare.rapid_evolution.ui.widgets.search.InternalSearchTableView;
import com.mixshare.rapid_evolution.ui.widgets.search.SearchWidgetUI;
import com.mixshare.rapid_evolution.util.CommandRunner;
import com.mixshare.rapid_evolution.util.FileUtil;
import com.mixshare.rapid_evolution.util.OSHelper;
import com.mixshare.rapid_evolution.util.StringUtil;
import com.mixshare.rapid_evolution.workflow.Task;
import com.mixshare.rapid_evolution.workflow.TaskManager;
import com.mixshare.rapid_evolution.workflow.maintenance.FileAssociatorTask;
import com.mixshare.rapid_evolution.workflow.maintenance.search.parsers.ParseTitleForArtistTask;
import com.mixshare.rapid_evolution.workflow.maintenance.search.parsers.ParseTitleForTrackTask;
import com.mixshare.rapid_evolution.workflow.user.DeleteRecordsTask;
import com.mixshare.rapid_evolution.workflow.user.GroupExecutionMonitorTask;
import com.mixshare.rapid_evolution.workflow.user.OrganizeTask;
import com.mixshare.rapid_evolution.workflow.user.RenameTask;
import com.mixshare.rapid_evolution.workflow.user.detection.OnDemandBPMDetectionTask;
import com.mixshare.rapid_evolution.workflow.user.detection.OnDemandBeatIntensityDetectionTask;
import com.mixshare.rapid_evolution.workflow.user.detection.OnDemandKeyDetectionTask;
import com.mixshare.rapid_evolution.workflow.user.detection.OnDemandReplayGainDetectionTask;
import com.mixshare.rapid_evolution.workflow.user.tags.TagReadTask;
import com.mixshare.rapid_evolution.workflow.user.tags.TagRemoveTask;
import com.mixshare.rapid_evolution.workflow.user.tags.TagWriteTask;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.core.Qt.KeyboardModifier;
import com.trolltech.qt.gui.QAction;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QFileDialog;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QKeyEvent;
import com.trolltech.qt.gui.QMenu;
import com.trolltech.qt.gui.QMessageBox;

public class SongTableView extends InternalSearchTableView {

	static private Logger log = Logger.getLogger(SongTableView.class);

	////////////
	// FIELDS //
	////////////

	protected QMenu tagMenu;
	protected QAction tagMenuAction;
    protected QAction writeTags;
    protected QAction readTags;
    protected QAction removeTags;

    protected QMenu fileMenu;
    protected QAction fileMenuAction;
    protected QAction organizeAction;
    protected QAction renameAction;
    protected QAction associateAction;
    protected QAction showInExplorerAction;

    protected QMenu detectMenu;
    protected QAction detectMenuAction;
    protected QAction detectAllAction;
    protected QAction detectKeyAction;
    protected QAction detectBPMAction;
    protected QAction detectBeatIntensityAction;
    protected QAction detectReplayGainAction;

    protected QAction parseTitleForArtistAction;
    protected QAction parseTitleForTrackAction;

    /////////////////
    // CONSTRUCTOR //
    /////////////////

	public SongTableView(SearchModelManager modelManager) {
		super(modelManager);

		setItemDelegate(new HarmonicColoringItemDelegate(this, modelManager));

		writeTags = new QAction(Translations.get("song_table_menu_tags_write"), this);
		writeTags.triggered.connect(this, "writeTags()");
		writeTags.setIcon(new QIcon(RE3Properties.getProperty("menu_tags_write_icon")));

        readTags = new QAction(Translations.get("song_table_menu_tags_read"), this);
        readTags.triggered.connect(this, "readTags()");
        readTags.setIcon(new QIcon(RE3Properties.getProperty("menu_tags_read_icon")));

        removeTags = new QAction(Translations.get("song_table_menu_tags_remove"), this);
        removeTags.triggered.connect(this, "removeTags()");
        removeTags.setIcon(new QIcon(RE3Properties.getProperty("menu_remove_icon")));

        tagMenu = new QMenu(Translations.get("song_table_menu_tags"), this);
        tagMenu.addAction(readTags);
        tagMenu.addAction(writeTags);
        tagMenu.addAction(removeTags);
        tagMenuAction = new QAction(Translations.get("song_table_menu_tags"), this);
        tagMenuAction.setMenu(tagMenu);
        tagMenuAction.setIcon(new QIcon(RE3Properties.getProperty("menu_tags_icon")));

		organizeAction = new QAction(Translations.get("song_table_menu_organize"), this);
		organizeAction.triggered.connect(this, "organizeSongs()");
		organizeAction.setIcon(new QIcon(RE3Properties.getProperty("menu_organize_icon")));

		renameAction = new QAction(Translations.get("song_table_menu_rename"), this);
		renameAction.triggered.connect(this, "renameFiles()");
		renameAction.setIcon(new QIcon(RE3Properties.getProperty("menu_file_rename_icon")));

		associateAction = new QAction(Translations.get("song_table_menu_associate"), this);
		associateAction.triggered.connect(this, "associateFiles()");
		associateAction.setIcon(new QIcon(RE3Properties.getProperty("menu_file_associate_icon")));

		showInExplorerAction = new QAction(Translations.get("song_table_menu_open_folder"), this);
		showInExplorerAction.triggered.connect(this, "showInExplorer()");
		showInExplorerAction.setIcon(new QIcon(RE3Properties.getProperty("menu_open_folder_icon")));

		fileMenu = new QMenu(Translations.get("song_table_menu_files"), this);
		fileMenu.addAction(tagMenuAction);
		fileMenu.addAction(organizeAction);
		fileMenu.addAction(renameAction);
		fileMenu.addAction(associateAction);
		fileMenuAction = new QAction(Translations.get("song_table_menu_files"), this);
		fileMenuAction.setMenu(fileMenu);
		fileMenuAction.setIcon(new QIcon(RE3Properties.getProperty("menu_file_operations_icon")));

		detectAllAction = new QAction(Translations.get("detect_all_option"), this);
		detectAllAction.triggered.connect(this, "detectAll()");

		detectKeyAction = new QAction(Translations.get("column_key_title"), this);
		detectKeyAction.triggered.connect(this, "detectKey()");

		detectBPMAction = new QAction(Translations.get("column_bpm_title"), this);
		detectBPMAction.triggered.connect(this, "detectBPM()");

		detectBeatIntensityAction = new QAction(Translations.get("column_beat_intensity_title"), this);
		detectBeatIntensityAction.triggered.connect(this, "detectBeatIntensity()");

		detectReplayGainAction = new QAction(Translations.get("column_replay_gain_title"), this);
		detectReplayGainAction.triggered.connect(this, "detectReplayGain()");

		detectMenu = new QMenu(Translations.get("song_table_detect"), this);
		detectMenu.addAction(detectAllAction);
		detectMenu.addAction(detectKeyAction);
		detectMenu.addAction(detectBPMAction);
		detectMenu.addAction(detectBeatIntensityAction);
		detectMenu.addAction(detectReplayGainAction);
		detectMenuAction = new QAction(Translations.get("song_table_detect"), this);
		detectMenuAction.setMenu(detectMenu);
		detectMenuAction.setIcon(new QIcon(RE3Properties.getProperty("search_icon")));

		fieldsParseMenuAction.setVisible(true);
		parseTitleForArtistAction = new QAction(Translations.get("song_table_menu_parse_title_for_artist"), this);
		parseTitleForArtistAction.triggered.connect(this, "parseTitleForArtist()");
		//parseTitleForArtistAction.setIcon(new QIcon(RE3Properties.getProperty("")));

		parseTitleForTrackAction = new QAction(Translations.get("song_table_menu_parse_title_for_track"), this);
		parseTitleForTrackAction.triggered.connect(this, "parseTitleForTrack()");
		//parseTitleForTrackAction.setIcon(new QIcon(RE3Properties.getProperty("")));

		fieldsParseMenu.addAction(parseTitleForArtistAction);
		fieldsParseMenu.addAction(parseTitleForTrackAction);


	}

	/////////////
	// GETTERS //
	/////////////

	public Vector<SongRecord> getSelectedSongs() {
		Vector<SearchRecord> searchRecords = getSelectedRecords();
		Vector<SongRecord> result = new Vector<SongRecord>(searchRecords.size());
		for (SearchRecord searchRecord : searchRecords)
			result.add((SongRecord)searchRecord);
		return result;
	}

	@Override
	protected SearchDetailsModelManager getDetailsModelManager() {
		return (SongDetailsModelManager)Database.getRelativeModelFactory().getRelativeModelManager(SongDetailsModelManager.class);
	}

	/////////////
	// METHODS //
	/////////////

	@Override
	protected void selectionChanged() {
    	Vector<SearchRecord> selectedStyles = getSelectedRecords();
        removeAction(playAction);
        removeAction(addAction);
        removeAction(deleteAction);
        removeAction(editAction);
        removeAction(mergeAction);
        removeAction(separator1);
        removeAction(separator2);
        removeAction(fileMenuAction);
        removeAction(detectMenuAction);
        removeAction(fieldsMenuAction);
        removeAction(addToMenuAction);
        removeAction(separator3);
        removeAction(showInExplorerAction);
    	if (selectedStyles.size() == 0) {
            addAction(addAction);
    	} else if (selectedStyles.size() == 1) {
            addAction(playAction);
            addAction(separator1);
            addAction(editAction);
            addAction(showInExplorerAction);
            addAction(addToMenuAction);
            addAction(fieldsMenuAction);
            addAction(fileMenuAction);
            addAction(detectMenuAction);
            addAction(separator3);
            addAction(addAction);
            addAction(deleteAction);
    	} else if (selectedStyles.size() > 1) {
            addAction(playAction);
            addAction(separator1);
            addAction(addToMenuAction);
            addAction(mergeAction);
            addAction(fieldsMenuAction);
            addAction(fileMenuAction);
            addAction(detectMenuAction);
            addAction(separator3);
            addAction(addAction);
            addAction(deleteAction);
    	}
    	SearchWidgetUI.instance.updateSelectedLabel(selectedStyles.size());

    }

	protected void removeTags() {
		if (QMessageBox.question(this, Translations.get("dialog_remove_tags_title"), Translations.get("dialog_remove_tags_text"), QMessageBox.StandardButton.Yes, QMessageBox.StandardButton.No) != QMessageBox.StandardButton.Yes.value()) {
			return;
		}
		RapidEvolution3UI.instance.enableBackgroundTasks();
		Vector<SongRecord> selectedSongs = getSelectedSongs();
		if (selectedSongs.size() == 1)
			TaskManager.runForegroundTask(new TagRemoveTask(selectedSongs, true));
		else {
			int numBackgroundTasks = TaskManager.getNumBackgroundExecutors();
			Vector<Vector<SongRecord>> selectedSongsSplice = new Vector<Vector<SongRecord>>(numBackgroundTasks);
			for (int i = 0; i < numBackgroundTasks; ++i)
				selectedSongsSplice.add(new Vector<SongRecord>((int)Math.ceil(((float)selectedSongs.size()) / numBackgroundTasks)));
			int i = 0;
			for (SongRecord selectedSong : selectedSongs) {
				selectedSongsSplice.get(i).add(selectedSong);
				++i;
				if (i >= numBackgroundTasks)
					i = 0;
			}
			Vector<Task> tasks = new Vector<Task>(numBackgroundTasks);
			for (Vector<SongRecord> splice : selectedSongsSplice) {
				if (splice.size() > 0)
					tasks.add(new TagRemoveTask(splice, false, true));
			}
			TaskManager.runForegroundTask(new GroupExecutionMonitorTask(tasks, Translations.get("removing_tags_batch_text") + " " + StringUtil.getTruncatedDescription(selectedSongs)));
			for (Task task : tasks)
				TaskManager.runBackgroundTask(task);
		}
	}

	protected void readTags() {
		if (QMessageBox.question(this, Translations.get("dialog_read_tags_title"), Translations.get("dialog_read_tags_text"), QMessageBox.StandardButton.Yes, QMessageBox.StandardButton.No) != QMessageBox.StandardButton.Yes.value()) {
			return;
		}
		RapidEvolution3UI.instance.enableBackgroundTasks();
		Vector<SongRecord> selectedSongs = getSelectedSongs();
		if (selectedSongs.size() == 1)
			TaskManager.runForegroundTask(new TagReadTask(selectedSongs, true));
		else {
			int numBackgroundTasks = TaskManager.getNumBackgroundExecutors();
			Vector<Vector<SongRecord>> selectedSongsSplice = new Vector<Vector<SongRecord>>(numBackgroundTasks);
			for (int i = 0; i < numBackgroundTasks; ++i)
				selectedSongsSplice.add(new Vector<SongRecord>((int)Math.ceil(((float)selectedSongs.size()) / numBackgroundTasks)));
			int i = 0;
			for (SongRecord selectedSong : selectedSongs) {
				selectedSongsSplice.get(i).add(selectedSong);
				++i;
				if (i >= numBackgroundTasks)
					i = 0;
			}
			Vector<Task> tasks = new Vector<Task>(numBackgroundTasks);
			for (Vector<SongRecord> splice : selectedSongsSplice) {
				if (splice.size() > 0)
					tasks.add(new TagReadTask(splice, false, true));
			}
			TaskManager.runForegroundTask(new GroupExecutionMonitorTask(tasks, Translations.get("reading_tags_batch_text") + " " + StringUtil.getTruncatedDescription(selectedSongs)));
			for (Task task : tasks)
				TaskManager.runBackgroundTask(task);
		}
	}
	protected void writeTags() {
		if (QMessageBox.question(this, Translations.get("dialog_write_tags_title"), Translations.get("dialog_write_tags_text"), QMessageBox.StandardButton.Yes, QMessageBox.StandardButton.No) != QMessageBox.StandardButton.Yes.value()) {
			return;
		}
		RapidEvolution3UI.instance.enableBackgroundTasks();
		Vector<SongRecord> selectedSongs = getSelectedSongs();
		if (selectedSongs.size() == 1)
			TaskManager.runForegroundTask(new TagWriteTask(selectedSongs, true));
		else {
			int numBackgroundTasks = TaskManager.getNumBackgroundExecutors();
			Vector<Vector<SongRecord>> selectedSongsSplice = new Vector<Vector<SongRecord>>(numBackgroundTasks);
			for (int i = 0; i < numBackgroundTasks; ++i)
				selectedSongsSplice.add(new Vector<SongRecord>((int)Math.ceil(((float)selectedSongs.size()) / numBackgroundTasks)));
			int i = 0;
			for (SongRecord selectedSong : selectedSongs) {
				selectedSongsSplice.get(i).add(selectedSong);
				++i;
				if (i >= numBackgroundTasks)
					i = 0;
			}
			Vector<Task> tasks = new Vector<Task>(numBackgroundTasks);
			for (Vector<SongRecord> splice : selectedSongsSplice) {
				if (splice.size() > 0)
					tasks.add(new TagWriteTask(splice, false, true));
			}
			TaskManager.runForegroundTask(new GroupExecutionMonitorTask(tasks, Translations.get("writing_tags_batch_text") + " " + StringUtil.getTruncatedDescription(selectedSongs)));
			for (Task task : tasks)
				TaskManager.runBackgroundTask(task);
		}
	}

	protected void organizeSongs() {
		if (RE3Properties.getProperty("organize_music_directory").equals("")) {
			QMessageBox.information(this, Translations.get("dialog_configure_organization_title"), Translations.get("dialog_configure_organization_text"));
			return;
		}
		if (QMessageBox.question(this, Translations.get("dialog_organize_files_title"), Translations.get("dialog_organize_files_text"), QMessageBox.StandardButton.Yes, QMessageBox.StandardButton.No) != QMessageBox.StandardButton.Yes.value()) {
			return;
		}
		TaskManager.runForegroundTask(new OrganizeTask(getSelectedSongs(), false));
	}

	protected void parseTitleForArtist() {
		TaskManager.runForegroundTask(new ParseTitleForArtistTask(getSelectedSongs()));
	}

	protected void parseTitleForTrack() {
		TaskManager.runForegroundTask(new ParseTitleForTrackTask(getSelectedSongs()));
	}

	protected void showInExplorer()  {
		Vector<SongRecord> selectedSongs = getSelectedSongs();
		if (selectedSongs.size() == 1) {
			String directory = selectedSongs.get(0).getSongDirectory();
			String filename = selectedSongs.get(0).getSongFilename();
			if (OSHelper.isWindows()) {
				String command = "explorer /select,\"" + StringUtil.replace(filename, "/", "\\") + "\"";
				if (log.isDebugEnabled())
					log.debug("showInExplorer(): command=" + command);
				new CommandRunner(command);
			} else if (OSHelper.isMacOS()) {
				String[] command = new String[2];
				command[0] = "open";
				command[1] = directory;
				new CommandRunner(command);
			} else {
				// linux
				String[] command = new String[2];
				command[0] = "xdg-open";
				command[1] = directory;
				new CommandRunner(command);
			}
		}
	}

    @Override
	protected void keyPressEvent(QKeyEvent keyEvent) {
    	super.keyPressEvent(keyEvent);
		if (((keyEvent.key() == Qt.Key.Key_F.value()) || (keyEvent.key() == Qt.Key.Key_R.value())) && keyEvent.modifiers().isSet(KeyboardModifier.ControlModifier)) {
			showInExplorer();
		}
    }


	protected void renameFiles() {
		if (QMessageBox.question(this, Translations.get("dialog_rename_files_title"), Translations.get("dialog_rename_files_text"), QMessageBox.StandardButton.Yes, QMessageBox.StandardButton.No) != QMessageBox.StandardButton.Yes.value()) {
			return;
		}
		TaskManager.runForegroundTask(new RenameTask(getSelectedSongs()));
	}

	protected void associateFiles() {
		QFileDialog fileDialog = new QFileDialog(this);
		fileDialog.setFileMode(QFileDialog.FileMode.ExistingFiles);
		fileDialog.setViewMode(QFileDialog.ViewMode.Detail);
		fileDialog.setFilters(FileUtil.getSupportedFileFilters());
		if (Database.getProperty("last_associated_records_directory") != null)
			fileDialog.setDirectory((String)Database.getProperty("last_associated_records_directory"));
	    if (fileDialog.exec() == QDialog.DialogCode.Accepted.value()) {
	        List<String> filenames = fileDialog.selectedFiles();
	        String filename = filenames.get(0);
	        Database.setProperty("last_associated_records_directory", FileUtil.getDirectoryFromFilename(filename));
	        TaskManager.runForegroundTask(new FileAssociatorTask(filenames, getSelectedSongs()));
	    }
	}

    @Override
	protected void deleteRecords() {
		if (QMessageBox.question(this, Translations.get("dialog_delete_record_title"), Translations.get("dialog_delete_record_text"), QMessageBox.StandardButton.Yes, QMessageBox.StandardButton.No) != QMessageBox.StandardButton.Yes.value()) {
			return;
		}
    	List<QModelIndex> deletedIndexes = selectionModel().selectedRows();
    	Vector<Record> deletedRecords = new Vector<Record>(deletedIndexes.size());
    	for (QModelIndex index : deletedIndexes) {
    		QModelIndex sourceIndex = getProxyModel().mapToSource(index);
    		SearchRecord record = (SearchRecord)getRecordTableModelManager().getRecordForRow(sourceIndex.row());
			// songs
			if (!deletedRecords.contains(record))
				deletedRecords.add(record);
    	}
    	TaskManager.runForegroundTask(new DeleteRecordsTask(deletedRecords));
    }

    protected void detectAll() {
    	RapidEvolution3UI.instance.enableBackgroundTasks();
    	Vector<Task> detectionTasks = new Vector<Task>();
    	Vector<SongRecord> selectedSongs = getSelectedSongs();
    	for (SongRecord song : selectedSongs) {
    		if (!song.getStartKey().isValid()) {
    			OnDemandKeyDetectionTask keyTask = new OnDemandKeyDetectionTask(song);
    			detectionTasks.add(keyTask);
    			TaskManager.runBackgroundTask(keyTask);
    		}

    		if (!song.getBpmStart().isValid()) {
    			OnDemandBPMDetectionTask bpmTask = new OnDemandBPMDetectionTask(song);
    			detectionTasks.add(bpmTask);
    			TaskManager.runBackgroundTask(bpmTask);
    		}

    		if (!song.getBeatIntensityValue().isValid()) {
    			OnDemandBeatIntensityDetectionTask biTask = new OnDemandBeatIntensityDetectionTask(song);
    			detectionTasks.add(biTask);
    			TaskManager.runBackgroundTask(biTask);
    		}

    		OnDemandReplayGainDetectionTask rgaTask = new OnDemandReplayGainDetectionTask(song);
    		detectionTasks.add(rgaTask);
    		TaskManager.runBackgroundTask(rgaTask);
    	}
    	if (detectionTasks.size() > 0)
    		TaskManager.runForegroundTask(new GroupExecutionMonitorTask(detectionTasks, Translations.get("all_detection_task_progress_title") + " " + StringUtil.getTruncatedDescription(selectedSongs)));
    }

    protected void detectKey() {
    	RapidEvolution3UI.instance.enableBackgroundTasks();
    	Vector<Task> detectionTasks = new Vector<Task>();
    	Vector<SongRecord> selectedSongs = getSelectedSongs();
    	for (SongRecord song : selectedSongs) {
    		if (!song.getStartKey().isValid()) {
	    		OnDemandKeyDetectionTask task = new OnDemandKeyDetectionTask(song);
	    		detectionTasks.add(task);
	    		TaskManager.runBackgroundTask(task);
    		}
    	}
    	if (detectionTasks.size() > 0)
    		TaskManager.runForegroundTask(new GroupExecutionMonitorTask(detectionTasks, Translations.get("key_detection_task_progress_title") + " " + StringUtil.getTruncatedDescription(selectedSongs)));
    }

    protected void detectBPM() {
    	RapidEvolution3UI.instance.enableBackgroundTasks();
    	Vector<Task> detectionTasks = new Vector<Task>();
    	Vector<SongRecord> selectedSongs = getSelectedSongs();
    	for (SongRecord song : selectedSongs) {
    		if (!song.getBpmStart().isValid()) {
    			OnDemandBPMDetectionTask task = new OnDemandBPMDetectionTask(song);
    			detectionTasks.add(task);
    			TaskManager.runBackgroundTask(task);
    		}
    	}
    	if (detectionTasks.size() > 0)
    		TaskManager.runForegroundTask(new GroupExecutionMonitorTask(detectionTasks, Translations.get("bpm_detection_task_progress_title") + " " + StringUtil.getTruncatedDescription(selectedSongs)));
    }

    protected void detectBeatIntensity() {
    	RapidEvolution3UI.instance.enableBackgroundTasks();
    	Vector<Task> detectionTasks = new Vector<Task>();
    	Vector<SongRecord> selectedSongs = getSelectedSongs();
    	for (SongRecord song : selectedSongs) {
    		if (!song.getBeatIntensityValue().isValid()) {
    			OnDemandBeatIntensityDetectionTask task = new OnDemandBeatIntensityDetectionTask(song);
    			detectionTasks.add(task);
    			TaskManager.runBackgroundTask(task);
    		}
    	}
    	if (detectionTasks.size() > 0)
    		TaskManager.runForegroundTask(new GroupExecutionMonitorTask(detectionTasks, Translations.get("beat_intensity_detection_task_progress_title") + " " + StringUtil.getTruncatedDescription(selectedSongs)));
    }

    protected void detectReplayGain() {
    	RapidEvolution3UI.instance.enableBackgroundTasks();
    	Vector<Task> detectionTasks = new Vector<Task>();
    	Vector<SongRecord> selectedSongs = getSelectedSongs();
    	for (SongRecord song : selectedSongs) {
    		OnDemandReplayGainDetectionTask task = new OnDemandReplayGainDetectionTask(song);
    		detectionTasks.add(task);
    		TaskManager.runBackgroundTask(task);
    	}
    	if (detectionTasks.size() > 0)
    		TaskManager.runForegroundTask(new GroupExecutionMonitorTask(detectionTasks, Translations.get("replay_gain_detection_task_progress_title") + " " + StringUtil.getTruncatedDescription(selectedSongs)));
    }

}
