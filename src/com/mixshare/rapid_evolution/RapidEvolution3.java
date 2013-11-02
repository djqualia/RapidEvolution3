package com.mixshare.rapid_evolution;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringBufferInputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogManager;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.mixshare.rapid_evolution.audio.qt.QTUtil;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.index.CommonIndex;
import com.mixshare.rapid_evolution.data.mined.lastfm.LastfmAPIWrapper;
import com.mixshare.rapid_evolution.data.profile.ProfileManager;
import com.mixshare.rapid_evolution.data.profile.io.FileLimitingProfileIO;
import com.mixshare.rapid_evolution.music.duration.Duration;
import com.mixshare.rapid_evolution.net.ClientListener;
import com.mixshare.rapid_evolution.ui.RapidEvolution3UI;
import com.mixshare.rapid_evolution.ui.UIProperties;
import com.mixshare.rapid_evolution.ui.model.column.AllColumns;
import com.mixshare.rapid_evolution.ui.model.profile.ProfileStyleModelManager;
import com.mixshare.rapid_evolution.ui.model.profile.ProfileTagModelManager;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.RE3StatusBar;
import com.mixshare.rapid_evolution.ui.widgets.profile.filter.style.StyleTabTreeWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.profile.filter.tag.TagTabTreeWidgetUI;
import com.mixshare.rapid_evolution.util.FileUtil;
import com.mixshare.rapid_evolution.util.OSHelper;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.mixshare.rapid_evolution.util.io.writers.PlainTextLineWriter;
import com.mixshare.rapid_evolution.webaccess.WebServerManager;
import com.mixshare.rapid_evolution.workflow.SandmanThread;
import com.mixshare.rapid_evolution.workflow.TaskManager;
import com.mixshare.rapid_evolution.workflow.detection.DetectionTaskStarter;
import com.mixshare.rapid_evolution.workflow.importers.filesystem.ImportFilesTask;
import com.mixshare.rapid_evolution.workflow.importers.itunes.ITunesImportTask;
import com.mixshare.rapid_evolution.workflow.importers.re2.RE2DatabaseImporterTask;
import com.mixshare.rapid_evolution.workflow.maintenance.DatabaseCleanerTask;
import com.mixshare.rapid_evolution.workflow.maintenance.EchonestTimbreInspectionTask;
import com.mixshare.rapid_evolution.workflow.maintenance.IdentifierCleanup;
import com.mixshare.rapid_evolution.workflow.maintenance.TempFileCleanup;
import com.mixshare.rapid_evolution.workflow.maintenance.filter.ComputeFilterStats;
import com.mixshare.rapid_evolution.workflow.maintenance.search.MixoutFixerTask;
import com.mixshare.rapid_evolution.workflow.mining.MiningTasksStarter;
import com.mixshare.rapid_evolution.workflow.ui.TagUpdateTask;
import com.mixshare.rapid_evolution.workflow.user.ProfileSaveTask;
import com.mixshare.rapid_evolution.workflow.user.SaveDatabaseTask;
import com.mixshare.rapid_evolution.workflow.user.UserProfileRefresher;
import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QMessageBox;
import com.trolltech.qt.gui.QMessageBox.ButtonRole;
import com.trolltech.qt.gui.QPushButton;

public class RapidEvolution3 implements AllColumns {

	static private Logger log = Logger.getLogger(RapidEvolution3.class);

	static public String RAPID_EVOLUTION_VERSION = "@@@RE3_VERSION@@@";

    static public RapidEvolution3 instance = null;
    static public RapidEvolution3UI ui_instance = null;
    static public boolean isLoaded = false;

    static public boolean isTerminated;

    static public void main(String[] args) {
    	try {
    		File dataDir = OSHelper.getWorkingDirectory();
    		if (!dataDir.exists())
    			dataDir.mkdirs();
    		else
    			RE3Properties.loadUserProperties();

    		if (!RE3Properties.getBoolean("server_mode")) {
    			RapidEvolution3UI.initQApplication();
    			ui_instance = new RapidEvolution3UI();
    		}

    		// this allows backslashes in the properties file to be handled in a "normal" way compatible with what izpack writes
            updateSplashScreen(Translations.get("splash_screen_logging_settings"));
            loadLog4J();

	        // check for lock file (which detects a crash in previous session)
			String lockFilename = OSHelper.getWorkingDirectory() + "/re3.lock";
			File lockFile = new File(lockFilename);
			boolean consistencyCheck = false;
			if (lockFile.exists()) {
				if (!RE3Properties.getBoolean("server_mode")) {
					QMessageBox mb = new QMessageBox(RapidEvolution3UI.instance);
					mb.addButton(QMessageBox.StandardButton.Ok);
					QPushButton pushButton = new QPushButton();
					pushButton.setText("Skip");
					mb.addButton(pushButton, ButtonRole.RejectRole);
					mb.addButton(QMessageBox.StandardButton.Cancel);
					mb.setWindowTitle(Translations.get("lock_file_detected_title"));
					mb.setText(Translations.get("lock_file_detected_description"));
					int result = mb.exec();
					if (result == QMessageBox.StandardButton.Cancel.value()) {
						log.info("Consistency check cancelled, shutting down...");
						System.exit(0);
						return;
					} else if (result == QMessageBox.StandardButton.Ok.value()) {
						consistencyCheck = true;
					} else {
						log.warn("main(): skipping recommended consistency check");
					}
				} else {
					consistencyCheck = true;
				}
			} else {
				LineWriter fileWriter = new PlainTextLineWriter(lockFilename);
				fileWriter.writeLine("So you're a curious one, aren't you...");
				fileWriter.close();
			}

            updateSplashScreen(Translations.get("splash_screen_loading_database"));
	        Database.load(consistencyCheck);
	        instance = new RapidEvolution3();

	        if (ui_instance != null) {
	        	ui_instance.setupUi();

		        // preloading certain elements
	            updateSplashScreen(Translations.get("splash_screen_preloading_styles_tab"));
				new StyleTabTreeWidgetUI((ProfileStyleModelManager)Database.getRelativeModelFactory().getRelativeModelManager(ProfileStyleModelManager.class), COLUMN_DEGREE).lazyLoad();
	            updateSplashScreen(Translations.get("splash_screen_preloading_tags_tab"));
				new TagTabTreeWidgetUI((ProfileTagModelManager)Database.getRelativeModelFactory().getRelativeModelManager(ProfileTagModelManager.class), COLUMN_DEGREE).lazyLoad();
	            updateSplashScreen(Translations.get("splash_screen_launching"));

	            ui_instance.show();
	        	ui_instance.closeSplashScreen();
	        	ui_instance.raise();
	        }

	        isLoaded = true;

	        // launch background tasks
	        new SandmanThread().start();
	        TaskManager.runForegroundTask(new ProfileSaveTask());
	        if (RE3Properties.getBoolean("import_songs_from_re2") && !Database.hasImportedFromRE2())
	        	TaskManager.runForegroundTask(new RE2DatabaseImporterTask());
	        if (RE3Properties.getBoolean("import_songs_from_itunes_automatically") && !Database.hasImportedFromITunes())
	        	TaskManager.runBackgroundTask(new ITunesImportTask(true));
	        SandmanThread.putBackgroundTaskToSleep(new SaveDatabaseTask(), RE3Properties.getInt("autosave_interval_minutes") * 1000 * 60);
	        if (RE3Properties.getBoolean("enable_delayed_tag_updates"))
	        	TaskManager.runForegroundTask(new TagUpdateTask());
	        if (RE3Properties.getBoolean("automatically_scan_root_directory")) {
	        	String directory = OSHelper.getMusicDirectory();
	        	if (directory.length() > 0) {
	        		ImportFilesTask importTask = new ImportFilesTask(directory, false, true);
	        		importTask.setRepeats(true);
	        		importTask.setImportPlaylists(RE3Properties.getBoolean("add_playlists_during_import"));
	        		TaskManager.runBackgroundTask(importTask);
	        	}
	        }
	        Boolean inspectorRun = (Boolean)Database.getProperty("timbre_inspector_run");
	        if ((inspectorRun == null) || (inspectorRun.equals(Boolean.FALSE)))
	        	TaskManager.runForegroundTask(new EchonestTimbreInspectionTask());
	        if (RE3Properties.getBoolean("run_database_cleaner_task_on_start"))
	        	TaskManager.runBackgroundTask(new DatabaseCleanerTask());
	        if (RE3Properties.getBoolean("enable_user_profile_refresher"))
	        	TaskManager.runBackgroundTask(new UserProfileRefresher());
	        TaskManager.runBackgroundTask(new IdentifierCleanup());
	        TaskManager.runBackgroundTask(new TempFileCleanup());
	        DetectionTaskStarter.start();
	        MiningTasksStarter.start();
	        //TaskManager.runForegroundTask(new LastfmSimilarityNormalizer());
	        //TaskManager.runForegroundTask(new EmptyMinedProfileRemover());
	        if (!RE3Properties.getBoolean("run_mixout_fixer")) {
	        	TaskManager.runForegroundTask(new MixoutFixerTask());
	        }
	        if (RE3Properties.getBoolean("enable_fast_filter_counts"))
	        	TaskManager.runForegroundTask(new ComputeFilterStats());

	        if (RE3Properties.getBoolean("logging_disable_standard_out")) {
	        	System.setOut(new PrintStream(new OutputStream() {
	        		@Override
					public void write(int b) { }
	        	}));
	        	try {
	        		LogManager.getLogManager().readConfiguration(new StringBufferInputStream("org.jaudiotagger.level = OFF"));
	        	} catch (SecurityException e) {
	        		log.warn("Fail to suppress the java.util.logger config.", e);
	        	} catch (IOException e) {
	        		log.warn("Fail to suppress the java.util.logger config.", e);
	        	}
	        }

	        log.info("Rapid Evolution 3 " + RAPID_EVOLUTION_VERSION);
	        log.info("Detected OS: " + OSHelper.getPlatformAsString());
	        log.info("JVM Version: " + System.getProperty("java.version"));
	        try {
	            String QTVersion = QTUtil.getVersionString();
	            if (QTVersion != null) {
	                log.info(QTVersion);
	            }
	        } catch (java.lang.Error e) {
	        } catch (Exception e) { }
	        log.info("Max Memory: " + String.valueOf(Runtime.getRuntime().maxMemory() / 1048576) + "mb");
	        if (log.isTraceEnabled()) {
		        log.debug("Used Memory: " + String.valueOf((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576) + "mb");
	            String classPath = System.getProperty("java.class.path",".");
	            log.trace("Classpath: " + classPath);
	        }

	        if (RE3Properties.getBoolean("web_access_enabled"))
	        	WebServerManager.startWebAccess();
	        if (log.isDebugEnabled())
	        	log.debug("run(): re3 is loaded");

	        if (RE3Properties.getBoolean("server_mode")) {
	        	new ClientListener(RE3Properties.getInt("client_listen_port")).start();
	        	while (!RapidEvolution3.isTerminated)
	        		Thread.sleep(2000);
	        	TaskManager.shutdown();
	        	ProfileManager.stopWrites = true;
	        	save();
	        } else {
	        	QApplication.exec();
	        }
	        Database.close();
	        if (!lockFile.delete())
	        	lockFile.deleteOnExit();

	        if (RE3Properties.getBoolean("web_access_enabled"))
	        	WebServerManager.stopWebAccess();
	        cleanupFiles();
	    	System.exit(0);
		} catch (OutOfMemoryError e) {
			log.error("run(): rapid evolution 3 cannot continue, out of memory");
			if (RapidEvolution3UI.instance != null)
				RapidEvolution3UI.instance.notifyOutOfMemory("the main thread executed");
		} catch (Error e) {
			log.error("run(): error", e);
    	} catch (Exception e) {
    		log.error("main(): error", e);
    	}
    }

    /////////////
    // METHODS //
    /////////////

    static public void updateSplashScreen(String status) {
    	if (ui_instance != null)
    		ui_instance.updateSplashScreen(status);
    }

    static public void loadLog4J() {
    	try {
		    Properties log4jproperties = new Properties();
			//properties.load(new FileInputStream(propertiesFilename));
			RE3Properties.customLoadProperties(log4jproperties, "log4j.properties");
			String newFilename = OSHelper.getWorkingDirectory() + "/log4j.properties";
			FileOutputStream out = new FileOutputStream(newFilename);
			log4jproperties.store(out, "---TEMP FILE---");
			out.close();
		    PropertyConfigurator.configureAndWatch(newFilename);
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    }

    static public boolean save() {
    	boolean success = false;
    	try {
			if (log.isDebugEnabled()) {
				log.debug("save(): saving database...");
				if (log.isTraceEnabled()) {
					log.debug("save(): \t# artists=" + Database.getArtistIndex().getSize() + " (" + Database.getArtistIndex().getSizeInternalItems() + "/" + Database.getArtistIndex().getSizeExternalItems() + ")");
					log.debug("save(): \t# labels=" + Database.getLabelIndex().getSize() + " (" + Database.getLabelIndex().getSizeInternalItems() + "/" + Database.getLabelIndex().getSizeExternalItems() + ")");
					log.debug("save(): \t# releases=" + Database.getReleaseIndex().getSize() + " (" + Database.getReleaseIndex().getSizeInternalItems() + "/" + Database.getReleaseIndex().getSizeExternalItems() + ")");
					log.debug("save(): \t# songs=" + Database.getSongIndex().getSize() + " (" + Database.getSongIndex().getSizeInternalItems() + "/" + Database.getSongIndex().getSizeExternalItems() + ")");
				} else {
					log.debug("save(): \t# artists=" + Database.getArtistIndex().getSize());
					log.debug("save(): \t# labels=" + Database.getLabelIndex().getSize());
					log.debug("save(): \t# releases=" + Database.getReleaseIndex().getSize());
					log.debug("save(): \t# songs=" + Database.getSongIndex().getSize());
				}
				log.debug("save(): \t# styles=" + Database.getStyleIndex().getSize());
				log.debug("save(): \t# tags=" + Database.getTagIndex().getSize());
				log.debug("save(): \t# playlists=" + Database.getPlaylistIndex().getSize());
				if (ProfileManager.getProfileIO() instanceof FileLimitingProfileIO) {
					log.debug("save(): \n" + FileLimitingProfileIO.getStatsAsString());
				}
			}
			if (!RE3Properties.getBoolean("server_mode"))
				RE3StatusBar.instance.showStatusMessage(Translations.get("saving_text"));
			for (CommonIndex index : Database.getAllIndexes()) {
				index.getBlockUpdatesSem().startWrite("save");
				index.getMergeProfileSem().tryAcquire(60, TimeUnit.SECONDS);
				if (RE3Properties.getBoolean("lock_all_records_on_save"))
					index.lockAllRecords();
			}
			Database.getSongIndex().getFilenameMapSem().startWrite("save");
			long timeBefore = System.currentTimeMillis();
    		success = Database.save();
	        UIProperties.save();
	        RE3Properties.save();
	        if (log.isDebugEnabled())
	        	log.debug("execute(): done saving, success=" + success);
	        long totalTime = System.currentTimeMillis() - timeBefore;
	        log.info("Database Saved Successfully! (" + new Duration(totalTime).getDurationAsString(false) + "s)");
    	} catch (Exception e) {
    		log.error("save(): error", e);
    	} finally {
    		for (CommonIndex index : Database.getAllIndexes()) {
    			index.getBlockUpdatesSem().endWrite();
    			index.getMergeProfileSem().release();
    			if (RE3Properties.getBoolean("lock_all_records_on_save"))
    				index.unlockAllRecords();
    		}
    		Database.getSongIndex().getFilenameMapSem().endWrite();
    	}
    	return success;
    }

    static private void cleanupFiles() {
        if (RE3Properties.getBoolean("clear_lastfm_cache_on_exit")) {
        	LastfmAPIWrapper.clearCache();
        	FileUtil.deleteDirectory(OSHelper.getWorkingDirectory() + "/" + RE3Properties.getProperty("lastfm_cache_directory"), true);
        }
        if (RE3Properties.getBoolean("clear_temp_working_directory_on_exit")
        		&& (RE3Properties.getProperty("temp_working_directory").length() > 0)) {
        	FileUtil.deleteDirectory(RE3Properties.getProperty("temp_working_directory"), true);
        }
    }
}
