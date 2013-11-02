package com.mixshare.rapid_evolution.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import net.roydesign.mac.MRJAdapter;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.audio.util.AudioUtil;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.profile.ProfileManager;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.ui.dialogs.options.OptionsDialog;
import com.mixshare.rapid_evolution.ui.dialogs.outofmemory.OutOfMemoryThread;
import com.mixshare.rapid_evolution.ui.model.column.AllColumns;
import com.mixshare.rapid_evolution.ui.updaters.WindowCloser;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.CentralWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.RE3SplashScreen;
import com.mixshare.rapid_evolution.ui.widgets.RE3StatusBar;
import com.mixshare.rapid_evolution.util.FileUtil;
import com.mixshare.rapid_evolution.util.StringUtil;
import com.mixshare.rapid_evolution.util.io.TextFileReader;
import com.mixshare.rapid_evolution.workflow.Task;
import com.mixshare.rapid_evolution.workflow.TaskManager;
import com.mixshare.rapid_evolution.workflow.exporters.playlists.ExportPlaylistsTasks;
import com.mixshare.rapid_evolution.workflow.importers.filesystem.ImportFilesTask;
import com.mixshare.rapid_evolution.workflow.importers.itunes.ITunesImportTask;
import com.mixshare.rapid_evolution.workflow.importers.mixmeister.MixmeisterImportTask;
import com.mixshare.rapid_evolution.workflow.importers.playlists.PlaylistImporterTask;
import com.mixshare.rapid_evolution.workflow.importers.re2.RE2DatabaseImporterTask;
import com.mixshare.rapid_evolution.workflow.importers.re3.RE3DatabaseImport;
import com.mixshare.rapid_evolution.workflow.importers.traktor.TraktorImportTask;
import com.mixshare.rapid_evolution.workflow.maintenance.DatabaseCleanerTask;
import com.mixshare.rapid_evolution.workflow.user.GroupExecutionMonitorTask;
import com.mixshare.rapid_evolution.workflow.user.tags.TagWriteTask;
import com.trolltech.qt.core.QSize;
import com.trolltech.qt.core.QUrl;
import com.trolltech.qt.gui.QAction;
import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QCloseEvent;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QDragEnterEvent;
import com.trolltech.qt.gui.QDropEvent;
import com.trolltech.qt.gui.QFileDialog;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QMainWindow;
import com.trolltech.qt.gui.QMenu;
import com.trolltech.qt.gui.QMessageBox;
import com.trolltech.qt.gui.QMoveEvent;
import com.trolltech.qt.gui.QResizeEvent;
import com.trolltech.qt.gui.QSizePolicy;

public class RapidEvolution3UI extends QMainWindow implements AllColumns, ActionListener {

	static private Logger log = Logger.getLogger(RapidEvolution3UI.class);

	static public RapidEvolution3UI instance = null;

	static private boolean notifiedOfLowMemory = false;

	static public void initQApplication() {
		QApplication.initialize(new String[] { "Start Qt" });
        QApplication.setApplicationName(RE3Properties.getProperty("window_title") + " " + RapidEvolution3.RAPID_EVOLUTION_VERSION);
        QApplication.setWheelScrollLines(RE3Properties.getInt("mouse_wheel_scroll_lines"));
	}

    ////////////
    // FIELDS //
    ////////////

	private RE3SplashScreen splashScreen;

    private QAction importDirectoryAction;
    private QAction importAudioFilesAction;
    private QAction importITunesAction;
    private QAction importTraktorAction;
    private QAction importRE2Action;
    private QAction importRE3Action;
    private QAction importPlaylistsAction;
    private QAction importMixmeisterAction;

    private QAction exportPlaylistsAction;
    private QAction exportSongTagsAction;

    private QAction exportSpreadsheetAction;

    private OptionsDialog optionsDialog;

    private QAction toggleBackgroundTasksAction;
    private QAction configureAction;
    private QAction cleanDatabaseAction;

    /////////////////
    // CONSTRUCTOR //
    /////////////////

	public RapidEvolution3UI() {
		super();
		try {
			instance = this;

            splashScreen = new RE3SplashScreen();
            splashScreen.show();
            splashScreen.setGeometry(splashScreen.splashScreenRect());
            // Show in front on mac regardless of if its a bundle or not...
            splashScreen.raise();
            QApplication.processEvents();

	        setStyleSheet(new TextFileReader("re3.css").getText());

	        MRJAdapter.addQuitApplicationListener(this);

		} catch (Exception e) {
			log.error("RapidEvolution3UI(): error", e);
		}
	}

	/////////////
	// METHODS //
	/////////////

    public void updateSplashScreen(String status) {
    	if (splashScreen != null)
    		splashScreen.updateProgress(status);
    }

    public void closeSplashScreen() {
        if (splashScreen != null)
            splashScreen.finish(this);
    }

    ////////////
    // EVENTS //
    ////////////

    @Override
	protected void closeEvent(QCloseEvent closeEvent) {

		if (QMessageBox.question(this, Translations.get("dialog_exit_title"), Translations.get("dialog_exit_text"), QMessageBox.StandardButton.Yes, QMessageBox.StandardButton.No) != QMessageBox.StandardButton.Yes.value()) {
			closeEvent.ignore();
			return;
		}

    	splashScreen = new RE3SplashScreen();
        splashScreen.show();
        splashScreen.setGeometry(splashScreen.splashScreenRect());
        // Show in front on mac regardless of if its a bundle or not...
        splashScreen.raise();
        QApplication.processEvents();
        splashScreen.updateProgress(Translations.get("splash_screen_shut_down"));
        log.info("Shutting Down...");
        RapidEvolution3.isTerminated = true;
        RapidEvolution3.isLoaded = false;
        splashScreen.updateProgress(Translations.get("splash_screen_waiting_for_background_tasks"));
    	TaskManager.shutdown();
    	ProfileManager.stopWrites = true;
        splashScreen.updateProgress(Translations.get("saving_text"));
    	RapidEvolution3.save();
        splashScreen.updateProgress(Translations.get("done_text"));
        splashScreen.finish(this);
    	super.closeEvent(closeEvent);
    }

    public void setupUi() {
    	if (log.isDebugEnabled())
    		log.debug("setupUi(): starting...");
        updateSplashScreen(Translations.get("splash_screen_loading_ui"));
        setObjectName("RapidEvolution3");
        resize(new QSize(800, 600).expandedTo(minimumSizeHint()));
        QSizePolicy sizePolicy = new QSizePolicy(com.trolltech.qt.gui.QSizePolicy.Policy.Expanding, com.trolltech.qt.gui.QSizePolicy.Policy.Expanding);
        sizePolicy.setHorizontalStretch((byte)0);
        sizePolicy.setVerticalStretch((byte)0);
        sizePolicy.setHeightForWidth(sizePolicy().hasHeightForWidth());
        setSizePolicy(sizePolicy);

        setStatusBar(new RE3StatusBar(this));

        CentralWidgetUI centralwidget = new CentralWidgetUI();
        centralwidget.setObjectName("centralwidget");
        setCentralWidget(centralwidget);
        setWindowTitle(Translations.get("window_title") + " " + RapidEvolution3.RAPID_EVOLUTION_VERSION);
        setWindowIcon(new QIcon(RE3Properties.getProperty("application_icon_filename")));

        setAcceptDrops(true);

        if (UIProperties.hasProperty("main_window_width"))
        	resize(new QSize(UIProperties.getInt("main_window_width"), UIProperties.getInt("main_window_height")));
        if (UIProperties.hasProperty("main_window_position_x"))
        	move(checkRange(UIProperties.getInt("main_window_position_x")), checkRange(UIProperties.getInt("main_window_position_y")));
        centralwidget.setSplitterSizes();

        QMenu importMenu = menuBar().addMenu(Translations.get("menu_import_text"));

        importITunesAction = new QAction(Translations.get("menu_import_itunes"), this);
        importITunesAction.triggered.connect(this, "importITunes()");
        importITunesAction.setIcon(new QIcon(RE3Properties.getProperty("menu_import_itunes_icon")));

        importTraktorAction = new QAction(Translations.get("menu_import_traktor"), this);
        importTraktorAction.triggered.connect(this, "importTraktor()");
        importTraktorAction.setIcon(new QIcon(RE3Properties.getProperty("menu_import_traktor_icon")));

        importMixmeisterAction = new QAction(Translations.get("menu_import_mixmeister"), this);
        importMixmeisterAction.triggered.connect(this, "importMixmeister()");
        importMixmeisterAction.setIcon(new QIcon(RE3Properties.getProperty("menu_import_mixmeister_icon")));

        importRE2Action = new QAction(Translations.get("menu_import_re2"), this);
        importRE2Action.triggered.connect(this, "importRE2()");
        importRE2Action.setIcon(new QIcon(RE3Properties.getProperty("menu_import_re2_icon")));

        importRE3Action = new QAction(Translations.get("menu_import_re3"), this);
        importRE3Action.triggered.connect(this, "importRE3()");
        importRE3Action.setIcon(new QIcon(RE3Properties.getProperty("menu_import_re3_icon")));

        importPlaylistsAction  = new QAction(Translations.get("menu_import_playlists"), this);
        importPlaylistsAction.triggered.connect(this, "importPlaylists()");
        importPlaylistsAction.setIcon(new QIcon(RE3Properties.getProperty("menu_import_playlists_icon")));

        importDirectoryAction = new QAction(Translations.get("menu_import_directory"), this);
        importDirectoryAction.triggered.connect(this, "importDirectory()");
        importDirectoryAction.setIcon(new QIcon(RE3Properties.getProperty("menu_import_folder_icon")));

        importAudioFilesAction = new QAction(Translations.get("menu_import_audio_files"), this);
        importAudioFilesAction.triggered.connect(this, "importFileSystem()");
        importAudioFilesAction.setIcon(new QIcon(RE3Properties.getProperty("menu_import_files_icon")));

        importMenu.addAction(importAudioFilesAction);
        importMenu.addAction(importDirectoryAction);
        importMenu.addAction(importPlaylistsAction);
        importMenu.addAction(importITunesAction);
        importMenu.addAction(importTraktorAction);
        importMenu.addAction(importMixmeisterAction);
        importMenu.addAction(importRE2Action);
        importMenu.addAction(importRE3Action);

        QMenu exportMenu = menuBar().addMenu(Translations.get("menu_export_text"));

        exportPlaylistsAction = new QAction(Translations.get("menu_export_playlists"), this);
        exportPlaylistsAction.triggered.connect(this, "exportPlaylists()");
        exportPlaylistsAction.setIcon(new QIcon(RE3Properties.getProperty("menu_export_playlists_icon")));

        exportSongTagsAction = new QAction(Translations.get("menu_export_song_tags"), this);
        exportSongTagsAction.triggered.connect(this, "exportSongTags()");
        exportSongTagsAction.setIcon(new QIcon(RE3Properties.getProperty("menu_export_song_tags_icon")));

        exportSpreadsheetAction = new QAction(Translations.get("menu_export_spreadsheet"), this);
        exportSpreadsheetAction.triggered.connect(this, "exportSpreadsheet()");
        exportSpreadsheetAction.setIcon(new QIcon(RE3Properties.getProperty("menu_export_spreadsheet_icon")));

        exportMenu.addAction(exportPlaylistsAction);
        exportMenu.addAction(exportSongTagsAction);

        QMenu optionsMenu = menuBar().addMenu(Translations.get("menu_options_text"));

        toggleBackgroundTasksAction = new QAction(TaskManager.isPaused() ? Translations.get("menu_options_resume_background_tasks") : Translations.get("menu_options_pause_background_tasks"), this);
        toggleBackgroundTasksAction.triggered.connect(this, "toggleBackgroundTasks()");
        toggleBackgroundTasksAction.setIcon(new QIcon(RE3Properties.getProperty(TaskManager.isPaused() ? "menu_play_icon" : "menu_pause_icon")));

        configureAction = new QAction(Translations.get("menu_options_configure_settings"), this);
        configureAction.triggered.connect(this, "configureSettings()");
        configureAction.setIcon(new QIcon(RE3Properties.getProperty("menu_settings_icon")));

        cleanDatabaseAction = new QAction(Translations.get("menu_options_clean_database"), this);
        cleanDatabaseAction.triggered.connect(this, "cleanDatabase()");
        cleanDatabaseAction.setIcon(new QIcon(RE3Properties.getProperty("menu_clear_icon")));

        optionsMenu.addAction(configureAction);
        optionsMenu.addAction(toggleBackgroundTasksAction);
        optionsMenu.addAction(cleanDatabaseAction);

    	if (log.isDebugEnabled())
    		log.debug("setupUi(): finished");
    }

    private int checkRange(int value) {
    	if (value < 0)
    		return 0;
    	return value;
    }

    @Override
	public void actionPerformed(ActionEvent e) {
        // quit action
        QApplication.invokeLater(new WindowCloser(this));
    }

    public void notifyOutOfMemory(String taskDescription) {
    	if (!notifiedOfLowMemory) {
    		QApplication.invokeLater(new OutOfMemoryThread(this, taskDescription));
    		notifiedOfLowMemory = true;
    	}
    }

    /**
     * Called when files are dropped onto the RE3 window, unless the drop is on a sub widget that specifically accepts drops, like styles...
     *
     * Note: for some reason dropping from the OS doesn't
     */
	@Override
	public void dropEvent(QDropEvent event) {
		if (log.isTraceEnabled())
			log.trace("dropEvent(): event=" + event.mimeData().formats());
		if (event.source() == null) { // make sure the drop originated outside of the application
			List<QUrl> urls = event.mimeData().urls();
			Vector<String> filenames = new Vector<String>(urls.size());
			for (QUrl url : urls) {
				String filename = url.toLocalFile();
				filenames.add(filename);
			}
			if (log.isDebugEnabled())
				log.debug("dropEvent(): dropped files=" + filenames);
			TaskManager.runForegroundTask(new ImportFilesTask(filenames));
		}
	}

	static public String[] acceptedDropMimeTypes = new String[] { "text/uri-list" };
	@Override
	public void dragEnterEvent(QDragEnterEvent event) {
		if (log.isTraceEnabled())
			log.trace("dragEnterEvent(): event=" + event);
		if (event.source() == null) { // make sure the drop originated outside of the application
			for (String acceptedDropMimeType : acceptedDropMimeTypes) {
				if (event.mimeData().hasFormat(acceptedDropMimeType)) {
					event.acceptProposedAction();
					return;
				}
			}
		}
	}

	@Override
	protected void resizeEvent(QResizeEvent resizeEvent) {
		UIProperties.setProperty("main_window_width", String.valueOf(size().width()));
		UIProperties.setProperty("main_window_height", String.valueOf(size().height()));
	}

	@Override
	protected void moveEvent(QMoveEvent moveEvent) {
		UIProperties.setProperty("main_window_position_x", String.valueOf(pos().x()));
		UIProperties.setProperty("main_window_position_y", String.valueOf(pos().y()));
	}

	private void exportSpreadsheet() {
		//TaskManager.runForegroundtask(new ExportSpreadsheetTask());
	}

	private void importFileSystem() {
		try {
			QFileDialog fileDialog = new QFileDialog(this);
			fileDialog.setFileMode(QFileDialog.FileMode.ExistingFiles);
			fileDialog.setViewMode(QFileDialog.ViewMode.Detail);
			fileDialog.setFilters(FileUtil.getSupportedFileFilters());
			if (Database.getProperty("last_import_directory") != null)
				fileDialog.setDirectory((String)Database.getProperty("last_import_directory"));
		    if (fileDialog.exec() == QDialog.DialogCode.Accepted.value()) {
		        List<String> filenames = fileDialog.selectedFiles();
		        String filename = filenames.get(0);
		        Database.setProperty("last_import_directory", FileUtil.getDirectoryFromFilename(filename));
		        TaskManager.runForegroundTask(new ImportFilesTask(filenames));
		    }
		} catch (Exception e) {
			log.error("importFileSystem(): error", e);
		}
	}
	private void importDirectory() {
		try {
			QFileDialog fileDialog = new QFileDialog(this);
			fileDialog.setFileMode(QFileDialog.FileMode.DirectoryOnly);
			fileDialog.setViewMode(QFileDialog.ViewMode.Detail);
			if (Database.getProperty("last_import_directory") != null)
				fileDialog.setDirectory((String)Database.getProperty("last_import_directory"));
		    if (fileDialog.exec() == QDialog.DialogCode.Accepted.value()) {
		        List<String> filenames = fileDialog.selectedFiles();
		        String filename = filenames.get(0);
		        Database.setProperty("last_import_directory", FileUtil.getDirectoryFromFilename(filename));
		        TaskManager.runForegroundTask(new ImportFilesTask(filenames));
		    }
		} catch (Exception e) {
			log.error("importDirectory(): error", e);
		}
	}
	private void importPlaylists() {
		try {
			QFileDialog fileDialog = new QFileDialog(this);
			fileDialog.setFileMode(QFileDialog.FileMode.ExistingFiles);
			fileDialog.setViewMode(QFileDialog.ViewMode.Detail);
			fileDialog.setFilters(AudioUtil.getPlaylistFilters());
			if (Database.getProperty("last_import_directory") != null)
				fileDialog.setDirectory((String)Database.getProperty("last_import_directory"));
		    if (fileDialog.exec() == QDialog.DialogCode.Accepted.value()) {
		        List<String> filenames = fileDialog.selectedFiles();
		        String filename = filenames.get(0);
		        Database.setProperty("last_import_directory", FileUtil.getDirectoryFromFilename(filename));
		        TaskManager.runForegroundTask(new PlaylistImporterTask(filenames));
		    }
		} catch (Exception e) {
			log.error("importPlaylists(): error", e);
		}
	}

	private void importITunes() { TaskManager.runForegroundTask(new ITunesImportTask(false)); }
	private void importTraktor() { TaskManager.runForegroundTask(new TraktorImportTask(false)); }
	private void importMixmeister() { TaskManager.runForegroundTask(new MixmeisterImportTask()); }
	private void importRE2() { TaskManager.runForegroundTask(new RE2DatabaseImporterTask(false)); }
	private void importRE3() {
		QFileDialog fileDialog = new QFileDialog(this);
		fileDialog.setFileMode(QFileDialog.FileMode.DirectoryOnly);
		fileDialog.setViewMode(QFileDialog.ViewMode.Detail);
	    if (fileDialog.exec() == QDialog.DialogCode.Accepted.value()) {
	        List<String> filenames = fileDialog.selectedFiles();
	        String directory = filenames.get(0);
	        TaskManager.runForegroundTask(new RE3DatabaseImport(directory));
	    }
	}

	private void exportPlaylists() {
		QFileDialog fileDialog = new QFileDialog(this);
		fileDialog.setFileMode(QFileDialog.FileMode.DirectoryOnly);
		fileDialog.setViewMode(QFileDialog.ViewMode.Detail);
		if (Database.getProperty("last_export_playlists_dir") != null)
			fileDialog.setDirectory((String)Database.getProperty("last_export_playlists_dir"));
	    if (fileDialog.exec() == QDialog.DialogCode.Accepted.value()) {
	        List<String> filenames = fileDialog.selectedFiles();
	        String directory = filenames.get(0);
	        Database.setProperty("last_export_playlists_dir", directory);
	        TaskManager.runForegroundTask(new ExportPlaylistsTasks(directory));
	    }
	}

	private void exportSongTags() {
		if (QMessageBox.question(this, Translations.get("export_song_tags_dialog_title"), Translations.get("export_song_tags_dialog_text"), QMessageBox.StandardButton.Yes, QMessageBox.StandardButton.No) != QMessageBox.StandardButton.Yes.value())
			return;

		Vector<Integer> ids = Database.getSongIndex().getIds();
		Vector<SongRecord> selectedSongs = new Vector<SongRecord>();
		for (int id : ids) {
			SongRecord song = Database.getSongIndex().getSongRecord(id);
			if (song != null)
				selectedSongs.add(song);
		}
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

	/**
	 * Only call from a GUI thread
	 */
	public void enableBackgroundTasks() {
		if (TaskManager.isPaused()) {
			TaskManager.setPaused(false);
			toggleBackgroundTasksAction.setText(Translations.get("menu_options_pause_background_tasks"));
			toggleBackgroundTasksAction.setIcon(new QIcon(RE3Properties.getProperty(TaskManager.isPaused() ? "menu_play_icon" : "menu_pause_icon")));
		}
	}
	private void toggleBackgroundTasks() {
		if (TaskManager.isPaused()) {
			TaskManager.setPaused(false);
			toggleBackgroundTasksAction.setText(Translations.get("menu_options_pause_background_tasks"));
		} else {
			TaskManager.setPaused(true);
			toggleBackgroundTasksAction.setText(Translations.get("menu_options_resume_background_tasks"));
			if (RE3Properties.getBoolean("cancel_tasks_when_paused"))
				for (Task task : TaskManager.getCurrentBackgroundTasks())
					task.cancel();
		}
		toggleBackgroundTasksAction.setIcon(new QIcon(RE3Properties.getProperty(TaskManager.isPaused() ? "menu_play_icon" : "menu_pause_icon")));
	}
	private void configureSettings() {
		if (optionsDialog == null)
			optionsDialog = new OptionsDialog(this);
		optionsDialog.show();
	}
	private void cleanDatabase() {
		TaskManager.runBackgroundTask(new DatabaseCleanerTask());
	}

}
