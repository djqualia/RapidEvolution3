package com.mixshare.rapid_evolution.ui.widgets.mediaplayer;

import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.audio.player.util.ToggleLink;
import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.index.event.IndexChangeListener;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.data.submitted.SubmittedProfile;
import com.mixshare.rapid_evolution.music.duration.Duration;
import com.mixshare.rapid_evolution.player.PlayerCallBack;
import com.mixshare.rapid_evolution.player.PlayerFactory;
import com.mixshare.rapid_evolution.player.PlayerInterface;
import com.mixshare.rapid_evolution.player.PlayerManager;
import com.mixshare.rapid_evolution.ui.RapidEvolution3UI;
import com.mixshare.rapid_evolution.ui.dialogs.filter.AddFilterDegreeDialog;
import com.mixshare.rapid_evolution.ui.dialogs.trail.TrailDialog;
import com.mixshare.rapid_evolution.ui.util.ThumbnailImageFactory;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.common.rating.StarRating;
import com.mixshare.rapid_evolution.ui.widgets.mediaplayer.ui_threads.MediaPlayerButtonUpdater;
import com.mixshare.rapid_evolution.ui.widgets.mediaplayer.ui_threads.MediaPlayerDisableBack;
import com.mixshare.rapid_evolution.ui.widgets.mediaplayer.ui_threads.MediaPlayerForward;
import com.mixshare.rapid_evolution.ui.widgets.mediaplayer.ui_threads.MediaPlayerInit;
import com.mixshare.rapid_evolution.ui.widgets.mediaplayer.ui_threads.MediaPlayerSetIsPlaying;
import com.mixshare.rapid_evolution.ui.widgets.mediaplayer.ui_threads.MediaPlayerSliderUpdater;
import com.mixshare.rapid_evolution.ui.widgets.mediaplayer.ui_threads.MediaPlayerSongUpdate;
import com.mixshare.rapid_evolution.ui.widgets.mediaplayer.ui_threads.MediaPlayerStopUpdater;
import com.mixshare.rapid_evolution.ui.widgets.profile.ProfileWidgetUI;
import com.mixshare.rapid_evolution.workflow.CommonTask;
import com.mixshare.rapid_evolution.workflow.SandmanThread;
import com.mixshare.rapid_evolution.workflow.TaskManager;
import com.mixshare.rapid_evolution.workflow.event.NoDecoderError;
import com.mixshare.rapid_evolution.workflow.user.player.PreComputeNextSongTask;
import com.trolltech.qt.core.QPointF;
import com.trolltech.qt.core.QSize;
import com.trolltech.qt.core.QUrl;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QAction;
import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QBrush;
import com.trolltech.qt.gui.QCloseEvent;
import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QDragEnterEvent;
import com.trolltech.qt.gui.QDropEvent;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QKeyEvent;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QLinearGradient;
import com.trolltech.qt.gui.QListView;
import com.trolltech.qt.gui.QMenu;
import com.trolltech.qt.gui.QMenuBar;
import com.trolltech.qt.gui.QPalette;
import com.trolltech.qt.gui.QPixmap;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QSizePolicy;
import com.trolltech.qt.gui.QSlider;
import com.trolltech.qt.gui.QStandardItemModel;
import com.trolltech.qt.gui.QTextEdit;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;
import com.trolltech.qt.phonon.MediaObject;
import com.trolltech.qt.phonon.VideoWidget;

public class MediaPlayer extends QWidget implements PlayerCallBack, IndexChangeListener, DataConstants {

	static private Logger log = Logger.getLogger(MediaPlayer.class);	
	
	static public final int INFO_HEIGHT = RE3Properties.getInt("media_player_info_section_height");
	static public final int RATING_HEIGHT = 25;
	static public final int RATING_WIDTH = 105;
	static public final int TICK_INTERVAL_MILLIS = RE3Properties.getInt("media_player_tick_interval_millis");
	static public final double LOG_10_OVER_20 = 0.1151292546497022842; // ln(10) / 20
	
	static public MediaPlayer instance = null;
	
	////////////
	// FIELDS //
	////////////
	
	private boolean isPlaying = false;
	private boolean isVisible = true;
    private QPushButton pauseButton = null;
    private QPushButton playButton = null;
    private QPushButton backButton = null;
    private QPushButton stopButton = null;
    private QPushButton forwardButton = null;
    private QTextEdit info = null;
    private QMenu fileMenu = null;
    private REMediaSlider slider = null;
    private QSlider volume = null;
    private QLabel albumCoverLabel;
    private SongRecord currentSong = null;
    private PlayerInterface currentPlayer = null;
    private double currentPosition = 0.0;
    private QListView view;
    private long timeOfLastSetPosition;
    private Vector<Class> badPlayers = new Vector<Class>();
    private ToggleLink shuffleLabel;

    // actions
    private QAction openSongProfileAction;
    private QAction addStyleAction;
    private QAction addTagAction;
    
    // video specific
    private MediaPlayerVideoWindow videoWindow;
    
    //////////////////
    // CONSTRUCTORS //
    //////////////////
    
    public MediaPlayer() {
        this(null);
    }

    public MediaPlayer(SongRecord songRecord) {
    	try {
    		instance = this;
    		
	        setWindowTitle(Translations.get("media_player_window_title"));
	        setWindowIcon(new QIcon(RE3Properties.getProperty("application_icon_filename")));
	        setAttribute(Qt.WidgetAttribute.WA_MacBrushedMetal);
	
	        QSize buttonSize = new QSize(34, 28);
		
	        backButton = new QPushButton(this);
	        backButton.setMinimumSize(buttonSize);
	        backButton.setIcon(new QIcon(new QPixmap(RE3Properties.getProperty("media_player_back_icon_filename"))));
	        
	        stopButton = new QPushButton(this);
	        stopButton.setMinimumSize(buttonSize);
	        stopButton.setIcon(new QIcon(new QPixmap(RE3Properties.getProperty("media_player_stop_icon_filename"))));
	
	        forwardButton = new QPushButton(this);
	        forwardButton.setMinimumSize(buttonSize);
	        forwardButton.setIcon(new QIcon(new QPixmap(RE3Properties.getProperty("media_player_forward_icon_filename"))));
	        
	        playButton = new QPushButton(this);
	        playButton.setMinimumSize(buttonSize);
	        playButton.setIcon(new QIcon(new QPixmap(RE3Properties.getProperty("media_player_play_icon_filename"))));
	
	        pauseButton = new QPushButton(this);
	        pauseButton.setMinimumSize(buttonSize);
	        pauseButton.setIcon(new QIcon(new QPixmap(RE3Properties.getProperty("media_player_pause_icon_filename"))));
	
	        slider = new REMediaSlider(this);
	        volume = new QSlider(Qt.Orientation.Horizontal, this);
	
	        QVBoxLayout vLayout = new QVBoxLayout(this);
	        QHBoxLayout layout = new QHBoxLayout();
	        
	        QHBoxLayout infoLayout = new QHBoxLayout();
	
	        info = new QTextEdit(this);
	        info.setMaximumHeight(INFO_HEIGHT);
	        info.setReadOnly(true);
	        info.setAcceptDrops(false);
	        info.setVerticalScrollBarPolicy(Qt.ScrollBarPolicy.ScrollBarAlwaysOff);
	        info.setTextInteractionFlags(Qt.TextInteractionFlag.NoTextInteraction);
	        info.setContextMenuPolicy(Qt.ContextMenuPolicy.ActionsContextMenu);

	        openSongProfileAction = new QAction(Translations.get("media_player_open_profile_action"), this);
	        addStyleAction = new QAction(Translations.get("add_style_text"), this);
	        addTagAction = new QAction(Translations.get("add_tag_text"), this);	        	        
	        info.addAction(openSongProfileAction);
	        info.addAction(addStyleAction);
	        info.addAction(addTagAction);
	
	        if (System.getProperty("os.name").equals("Mac OS X")) {
	            QLinearGradient bgBrush = new QLinearGradient(new QPointF(0, 0), new QPointF(0, 50));
	            bgBrush.setColorAt(0, QColor.fromRgb(40, 50, 60));
	            bgBrush.setColorAt(1, QColor.fromRgb(120, 130, 140));
	            QPalette palette = new QPalette();
	            palette.setBrush(QPalette.ColorRole.Base, new QBrush(bgBrush));
	            info.setPalette(palette);
	        } else {
	            info.setStyleSheet("background-color:qlinearGradient(x1:0, y1:0, x2:0, y2:1, stop:0 #335577, " +
	                               "stop:1 #6688AA); color: #eeeeff");
	        }
	
	        info.setMinimumWidth(300);
	        volume.setRange(0, 100);
	        volume.setValue(RE3Properties.getInt("media_player_volume"));
	        volume.setMinimumWidth(40);
	
	        layout.addWidget(backButton);
	        layout.addWidget(stopButton);
	        layout.addWidget(playButton);
	        layout.addWidget(pauseButton);
	        layout.addWidget(forwardButton);

	        shuffleLabel = new ToggleLink(this, RE3Properties.getProperty("media_player_shuffle_disabled_icon_filename"), RE3Properties.getProperty("media_player_shuffle_enabled_icon_filename"), "media_player_shuffle_mode");
	        shuffleLabel.setToolTip(Translations.get("shuffle_text"));
	        
	        QLabel volumeLabel = new QLabel(this);
	        volumeLabel.setPixmap(new QPixmap(RE3Properties.getProperty("media_player_volume_icon_filename")));
	        layout.addWidget(shuffleLabel);
	        layout.addWidget(volumeLabel);
	        layout.addWidget(volume);
	        
	        albumCoverLabel = new QLabel(this);
	        albumCoverLabel.setText("No album cover");
	        
	        QVBoxLayout coverRatingLayout = new QVBoxLayout();
	        
	        view = new QListView(this);
	    	QSizePolicy viewSizePolicy = new QSizePolicy(com.trolltech.qt.gui.QSizePolicy.Policy.Minimum, com.trolltech.qt.gui.QSizePolicy.Policy.Minimum);
	    	view.setSizePolicy(viewSizePolicy);
	    	view.setHorizontalScrollBarPolicy(Qt.ScrollBarPolicy.ScrollBarAlwaysOff);
	    	view.setVerticalScrollBarPolicy(Qt.ScrollBarPolicy.ScrollBarAlwaysOff);	    	
	        view.setModel(new QStandardItemModel());
	        view.setItemDelegate(new MediaRatingDelegate(this));
	        view.model().insertColumn(0);
	        view.model().insertRow(0);	        
	        view.setMaximumSize(RATING_WIDTH, RATING_HEIGHT);
	        
	        albumCoverLabel.setAutoFillBackground(true);
	        view.setAutoFillBackground(true);
	        
	        coverRatingLayout.addWidget(albumCoverLabel);
	        coverRatingLayout.addWidget(view);
	        
	        infoLayout.addWidget(info);
	        infoLayout.addLayout(coverRatingLayout);
	        
	        vLayout.addLayout(infoLayout);	        
	        vLayout.addLayout(layout);
	        vLayout.addWidget(slider);
	
	        QHBoxLayout labelLayout = new QHBoxLayout();
	
	        vLayout.addLayout(labelLayout);
	        setLayout(vLayout);
	
	        // Create menu bar:
	        QMenuBar menubar = new QMenuBar();
	        fileMenu = menubar.addMenu(tr("&File"));
	        QAction settingsAction = fileMenu.addAction(tr("&Settings"));
	        
	        setContextMenuPolicy(Qt.ContextMenuPolicy.ActionsContextMenu);
	        addAction(settingsAction);
	        
	        //volumeLabel.setContextMenuPolicy(Qt.ContextMenuPolicy.ActionsContextMenu);
	        //volumeLabel.addAction(settingsAction);
	        
	        setAcceptDrops(true);
	    	
	        setFixedSize(sizeHint());
	        Database.getSongIndex().addIndexChangeListener(this);
	        
	        // setup signal connections...
	        backButton.clicked.connect(this, "back()");
	        stopButton.clicked.connect(this, "stop()");
	        forwardButton.clicked.connect(this, "forward()");
	        pauseButton.clicked.connect(this, "pause()");
	        playButton.clicked.connect(this, "play()");
	        volume.valueChanged.connect(this, "setVolume(int)");
	        //settingsAction.triggered.connect(this, "showSettingsDialog()");		
	        slider.valueChanged.connect(this, "seekSliderChanged(Integer)");
	        slider.sliderMoved.connect(this, "seekSliderMoved(Integer)");
	        openSongProfileAction.triggered.connect(this, "openSongProfile()");		
	        addStyleAction.triggered.connect(this, "addStyle()");		
	        addTagAction.triggered.connect(this, "addTag()");		

	        // init button states
	        backButton.setEnabled(false);
	        stopButton.setEnabled(false);
	        pauseButton.setEnabled(false);
	        playButton.setEnabled(false);	        
	
	        videoWindow = new MediaPlayerVideoWindow();
	        
	        if (songRecord != null)
	            setSong(songRecord);
	        
    	} catch (Exception e) {
    		log.error("MediaPlayer(): error", e);
    	}
    }

    /////////////
    // GETTERS //
    /////////////
    
    public REMediaSlider getSlider() { return slider; } 
    
    public boolean isShuffleEnabled() { return shuffleLabel.isLabelEnabled(); }
    public boolean isPlaying() { return isPlaying; }
    
    public double getCurrentPosition() { return currentPosition; }
    
    public MediaPlayerVideoWindow getVideoWindow() { return videoWindow; }
    public VideoWidget getVideoWidget() { return videoWindow.getVideoWidget(); }
    
    public QPushButton getBackButton() { return backButton; }
    
    public PlayerInterface getCurrentPlayer() { return currentPlayer; }
    
    /////////////
    // SETTERS //
    /////////////
        
    public void setSong(SongRecord songRecord) {
    	if (log.isTraceEnabled())
    		log.trace("setSong(): songRecord=" + songRecord);
    	if (currentPlayer != null) {
    		currentPlayer.close();
    		currentPlayer = null;
    	}
    	badPlayers.clear();
    	currentSong = songRecord;
    	String fileName = songRecord.getSongFilename();
    	currentPlayer = PlayerFactory.getPlayer(fileName, this);
    	if (currentPlayer != null) {
    		QApplication.invokeAndWait(new MediaPlayerSongUpdate(this));
    	} else {
    		if (RE3Properties.getBoolean("status_alert_no_decoder_message_enabled")) {
    			RE3Properties.setProperty("status_alert_no_decoder_message_enabled", "false");
    			if (NoDecoderError.instance == null) {
    				QApplication.invokeLater(new NoDecoderError(RapidEvolution3UI.instance.instance, currentSong.getSongFilename()));    				
    			}
    		}
    		if (isVisible)
    			forward();
    	}
		TaskManager.runForegroundTask(new PreComputeNextSongTask(songRecord));
		SandmanThread.putForegroundTaskToSleep(new PreComputeNextSongTask(songRecord), Math.max(15000, songRecord.getDurationInMillis()  / 2));    	
    }
                
    public void setIsPlayingNow(boolean playing, double currentPosition) {
    	if (log.isTraceEnabled())
    		log.trace("setIsPlayingNow(): playing=" + playing + ", currentPosition=" + currentPosition);
    	if (playing) {
    		isPlaying = true;
    		playButton.setEnabled(false);
    		stopButton.setEnabled(true);
    		pauseButton.setEnabled(true);
    		pauseButton.setVisible(true);
    		playButton.setVisible(false);
    	} else {
    		isPlaying = false;
    		playButton.setEnabled(true);
    		playButton.setVisible(true);
    		pauseButton.setEnabled(false);
    		pauseButton.setVisible(false);
    		if (currentPosition == 0.0)
    			stopButton.setEnabled(false);
    		else
    			stopButton.setEnabled(true);
    	}
    	if (videoWindow != null)
    		videoWindow.setIsPlayingNow(playing, currentPosition);    
    }

    public void setCurrentPosition(double position) { currentPosition = position; }    

    public void setMediaObject(MediaObject mediaObject) { videoWindow.setMediaObject(mediaObject); }
    
    /////////////
    // METHODS //
    /////////////
    
    public void updateInfo() {
    	if (currentPlayer == null)
    		return;
    	
        String font = "<font color=#ffffd0>";
        String fontmono = "<font family=\"monospace,courier new\" color=#ffffd0>";

        //Map <String, List<String>> metaData = mediaObject.metaData();        
        String trackArtist = currentSong.getArtistsDescription();
        String trackTitle = currentSong.getTitle();
        if (currentSong.getRemix().length() > 0)
        	trackTitle += " (" + currentSong.getRemix() + ")";
        String trackRelease = currentSong.getReleaseTitle();
        if (trackRelease.length() > 0) {
        	String year = currentSong.getOriginalYearReleasedAsString();
        	if (year.length() > 0)
        		trackRelease += "  [" + year + "]";
        }
        
    	long len = (long)currentPlayer.getTotalTime() * 1000;
    	long pos = (long)(currentPosition * len);
        
    	Duration currentPositionDuration = new Duration(currentPosition * currentPlayer.getTotalTime() * 1000);
    	Duration currentDuration = new Duration(currentPlayer.getTotalTime() * 1000);
    	
    	StringBuffer timeString = new StringBuffer();
        timeString.append(currentPositionDuration.getDurationAsString(false) );
        timeString.append("</font>");
        timeString.append("&nbsp; Duration: ");
        timeString.append(fontmono);
        timeString.append(currentDuration.getDurationAsString(false));
        timeString.append("</font>");
        String time = "Time: " + font + timeString + "</font>";

        String title = "";
        if (trackTitle.length() > 0)
            title = "Title: " + font + trackTitle + "<br></font>";

        String artist = "";
        if (trackArtist.length() > 0)
            artist = "Artist:  " + font + trackArtist + "<br></font>";
        
        String release = "";
        if (trackRelease.length() > 0)
        	release = "Release:  " + font + trackRelease + "<br></font>";

        String year = "";
        if (currentSong.getOriginalYearReleased() != 0)
        	year = "Year:  " + font + currentSong.getOriginalYearReleasedAsString() + "<br></font>";

        String labels = "";
        if (!currentSong.getLabelsDescription().equals(""))        	
        	year = "Label(s):  " + font + currentSong.getLabelsDescription() + "<br></font>";        
        
        String bpm = "";
        if (currentSong.getBpmStart().isValid())
        	bpm = "BPM:  " + font + currentSong.getBpm().toString() + "<br></font>";
        
        String key = "";
        if (currentSong.getStartKey().isValid())
        	key = "Key:  " + font + currentSong.getKey().toString() + "<br></font>";
        
        info.setHtml(title + artist + release + year + labels + key + bpm + time);        
    }    
    
    ////////////
    // EVENTS //
    ////////////
    
    protected void closeEvent(QCloseEvent e) {
    	if (videoWindow != null) {
    		videoWindow.close();
    		videoWindow.hide();
    	}
        if (currentPlayer != null) {
        	currentPlayer.stop();
        	currentPlayer.close();
        	currentPlayer = null;
        }
        isVisible = false;
    }

    /*
    private void showSettingsDialog() {
        Ui_Dialog ui = new Ui_Dialog();
        QDialog dialog = new QDialog();
        ui.setupUi(dialog);
        dialog.setWindowTitle(Translations.get("media_player_settings_window_title"));

        ui.crossFadeSlider.setValue((int)(2 * mediaObject.transitionTime() / 1000.0f));

        // Insert audio devices:
        List<AudioOutputDevice> devices = BackendCapabilities.availableAudioOutputDevices();
        for (int i=0; i<devices.size(); i++){
            ui.deviceCombo.addItem(devices.get(i).name() + " (" + devices.get(i).description() + ')');
            if (devices.get(i) == audioOutput.outputDevice())
                ui.deviceCombo.setCurrentIndex(i);
        }

        // Insert audio effects:
        ui.audioEffectsCombo.addItem("<no effect>");
        List<Effect> currEffects = audioOutputPath.effects();
        Effect currEffect = currEffects.size() > 0 ? currEffects.get(0) : null;
        List<EffectDescription> availableEffects = BackendCapabilities.availableAudioEffects();
        for (int i=0; i<availableEffects.size(); i++){
            ui.audioEffectsCombo.addItem(availableEffects.get(i).name());
            if (currEffect != null && availableEffects.get(i).equals(currEffect.description()))
                ui.audioEffectsCombo.setCurrentIndex(i+1);
        }

        dialog.exec();

        if (dialog.result() == QDialog.DialogCode.Accepted.value()){
            mediaObject.setTransitionTime((int)(1000 * (float)ui.crossFadeSlider.value() / 2.0f));
            audioOutput.setOutputDevice(devices.get(ui.deviceCombo.currentIndex()));
        }

        if (ui.audioEffectsCombo.currentIndex() > 0){
            EffectDescription chosenEffect = availableEffects.get(ui.audioEffectsCombo.currentIndex() - 1);
            if (currEffect == null || !currEffect.description().equals(chosenEffect)){
                for (Effect effect : currEffects)
                    audioOutputPath.removeEffect(effect);
                audioOutputPath.insertEffect(chosenEffect);
            }
        } else {
            for (Effect effect : currEffects)
                audioOutputPath.removeEffect(effect);
        }
    }
    */

    @Override
    protected void keyPressEvent(QKeyEvent keyEvent) {
    	super.keyPressEvent(keyEvent);
		if (keyEvent.key() == Qt.Key.Key_Escape.value())
			close();	
    }        
    
    @Override
    protected void dropEvent(QDropEvent e) {
        if (e.mimeData().hasUrls())
            e.acceptProposedAction();
        List<QUrl> urls = e.mimeData().urls();
        Vector<SongRecord> songs = new Vector<SongRecord>(urls.size());
        for (QUrl url : urls) {
        	String filename = url.toLocalFile();
        	SongRecord song = Database.getSongIndex().getSongRecord(filename);
        	if (song != null) {
        		songs.add(song);
        	}        	
        }
        if (e.keyboardModifiers().isSet(Qt.KeyboardModifier.ShiftModifier)){
            // TODO: just add to queue?
            if (songs.size() > 0)
            	PlayerManager.playSongRecords(songs);
        } else {
            // create a new queue
            if (songs.size() > 0)
            	PlayerManager.playSongRecords(songs);
        }
    }

    @Override
    protected void dragEnterEvent(QDragEnterEvent e) {
        if (e.mimeData().hasUrls())
            e.acceptProposedAction();
    }
    
    public void openSongProfile() {
    	if (currentSong != null) {
    		SongProfile songProfile = Database.getSongIndex().getSongProfile(currentSong.getUniqueId());
    		if (songProfile != null) {
    			ProfileWidgetUI.instance.editProfile(songProfile);
    			//RapidEvolution3.instance.setVisible(true);
    			//RapidEvolution3.instance.show();
    			//RapidEvolution3.instance.raise();
    			//RapidEvolution3.instance.setFocus();
    		}
    	}
    }
    
    public void addStyle() {
    	try {
			AddFilterDegreeDialog addFilterDialog = new AddFilterDegreeDialog(Database.getStyleModelManager().getStyleCompleter(), Database.getStyleModelManager(), PlayerManager.getCurrentSongProfile());
	    	addFilterDialog.setTitle(Translations.get("add_style_text"));
	    	addFilterDialog.setLabel(Translations.get("style_name_text"));
	    	addFilterDialog.show();
	    	addFilterDialog.raise();
	    	addFilterDialog.activateWindow();
    	} catch (Exception e) {
    		log.error("addStyle(): error", e);
    	}
    }
    
    public void addTag() {
    	try {
			AddFilterDegreeDialog addFilterDialog = new AddFilterDegreeDialog(Database.getTagModelManager().getTagCompleter(), Database.getTagModelManager(), PlayerManager.getCurrentSongProfile());
	    	addFilterDialog.setTitle(Translations.get("add_tag_text"));
	    	addFilterDialog.setLabel(Translations.get("tag_name_text"));	    	
	    	addFilterDialog.show();
	    	addFilterDialog.raise();
	    	addFilterDialog.activateWindow();
    	} catch (Exception e) {
    		log.error("addTag(): error", e);
    	}
    }
    
    public void songUpdate() {
    	if (currentPlayer != null) {
    		setVolume(volume.value());
    		view.model().setData(view.model().index(0,0), new StarRating(currentSong.getRatingValue()));
    		String thumbnailFilename = currentSong.getThumbnailImageFilename();
    		if (!currentSong.hasThumbnail())
    			thumbnailFilename = currentSong.getBestRelatedThumbnailImageFilename();
    		albumCoverLabel.setPixmap(QPixmap.fromImage(ThumbnailImageFactory.fetchThumbnailImageNow(thumbnailFilename, new QSize(INFO_HEIGHT - RATING_HEIGHT, INFO_HEIGHT - RATING_HEIGHT))));
    		updateInfo();
    		if (currentPlayer.hasVideo()) {   			
    			slider.hide();
    			videoWindow.getVideoWidget().setAspectRatio(VideoWidget.AspectRatio.AspectRatioWidget); // helps clear background stuff
    			if (!videoWindow.isVisible()) {
    				videoWindow.show();
    				videoWindow.raise();
    				videoWindow.activateWindow();
    			}
    			Duration duration = new Duration(currentPlayer.getTotalTime() * 1000);
    			if (!duration.equals(currentSong.getDuration())) {
    				currentSong.setDuration(duration);
    				currentSong.update();
    			}
    		} else {
    			if (videoWindow != null)
    				videoWindow.hide();
    			slider.show();
    		}
    	}
    }
        
    public void seekSliderMoved(Integer value) {
    	if (log.isTraceEnabled())
    		log.trace("seekSliderMoved(): value=" + value);
    	double position = ((double)value / slider.getMaxValue());
    	currentPosition = position;
    	updateInfo();
    }
    public void seekSliderChanged(Integer value) {
    	if (log.isDebugEnabled())
    		log.debug("seekSliderChanged(): value=" + value);
    	double position = ((double)value / slider.getMaxValue());
    	if (currentPlayer != null)
    		currentPlayer.setPosition(position);
    }        

    private void stop() {
    	if (log.isTraceEnabled())
    		log.trace("stop(): called");
    	if (currentPlayer != null)
    		currentPlayer.stop();
    	QApplication.invokeAndWait(new MediaPlayerStopUpdater(this));
    }

    public void play() {
    	QApplication.invokeAndWait(new MediaPlayerSetIsPlaying(this, true, currentPosition));
    }

    public void pause() {
    	QApplication.invokeAndWait(new MediaPlayerSetIsPlaying(this, false, currentPosition));
    }
    
    private void back() {
    	stop();
    	TaskManager.runForegroundTask(new PreviousSongTask());
    }
    
    public void forward() {
    	stop();    	
    	TaskManager.runForegroundTask(new AdvanceSongTask());
    }    
    
    private void setVolume(int volume) {
    	if (currentPlayer != null) {
    		if (RE3Properties.getBoolean("media_player_enable_replay_gain")) {
    			float initVolume = 0.75f * volume / 100.0f; // scale between 0 and 0.75
        		SongProfile songProfile = Database.getSongIndex().getSongProfile(currentSong.getUniqueId());
        		if (songProfile != null) {
        			Float replayGain = songProfile.getReplayGain();
        			if ((replayGain != null) && (Math.abs(replayGain) < 65.0f)) {
        				double scale = Math.exp(replayGain * LOG_10_OVER_20);
        				if (log.isTraceEnabled())
        					log.trace("setVolume(): replay gain=" + replayGain + ", scale=" + scale);
        				initVolume *= scale;
        				if (initVolume > 1.0f)
        					initVolume = 1.0f;
        				if (initVolume < 0.0f)
        					initVolume = 0.0f;
        			}
        		}    		
    		 	currentPlayer.setVolume(initVolume);
    		} else {
    			currentPlayer.setVolume(volume/100.0f);
    		}
    	}
    	RE3Properties.setProperty("media_player_volume", String.valueOf(volume));
    }
    
    ///////////////////////////
    // PLAYERCALLBACK EVENTS //
    ///////////////////////////
    
    /**
     * Where value is between 0 and 1
     */
    public void setPosition(double value) {
    	if (log.isTraceEnabled())
    		log.trace("setPosition(): value=" + value);
    	long timeSinceLastSetPosition = System.currentTimeMillis() - timeOfLastSetPosition;
    	if (timeSinceLastSetPosition < TICK_INTERVAL_MILLIS)
    		return;    	
		QApplication.invokeLater(new MediaPlayerSliderUpdater(this, value)); // invokeAndWait was causing a deadlock with flac decoder on close
    	timeOfLastSetPosition = System.currentTimeMillis();
    }
    
    public void donePlayingSong() {
    	if (RE3Properties.getBoolean("autoplay_enabled"))
    		QApplication.invokeAndWait(new MediaPlayerForward(this));        
    }

    public void setIsPlaying(boolean playing) {
    	QApplication.invokeAndWait(new MediaPlayerButtonUpdater(this, playing, currentPosition));
    }
    
    public void playerError(String description) {
    	try {
	    	if (log.isTraceEnabled())
	    		log.trace("playerError(): description=" + description);
	    	if (currentPlayer != null) {
	    		currentPlayer.close();
	    		badPlayers.add(currentPlayer.getClass());
	    	}
    		if (log.isDebugEnabled())
    			log.debug("playerError(): bad players=" + badPlayers);
	    	String fileName = currentSong.getSongFilename();
	    	if (currentPlayer != null)
	    		currentPlayer = PlayerFactory.getPlayer(fileName, this, badPlayers);
	    	if (currentPlayer != null) {    		
	    		setVolume(volume.value());
	    		QApplication.invokeAndWait(new MediaPlayerInit(this, false, false));
	    	} else {
	    		info.setHtml("Unsupported media file");
	    		if (log.isDebugEnabled())
	    			log.debug("playerError(): advancing to next song");
	    		forward();
	    	}
    	} catch (Exception e) {
    		log.error("playerError(): error", e);
    	}
    }
    
    //////////////////////////////////
    // INDEX CHANGE LISTENER EVENTS //
    //////////////////////////////////
    
	public void addedRecord(Record record, SubmittedProfile submittedProfile) { }
	public void updatedRecord(Record record) {
		SongRecord song = PlayerManager.getCurrentSong();
		if ((song != null) && (song.equals(record))) {
			if (log.isDebugEnabled())
				log.debug("updatedRecord(): updating media player datails=" + currentSong);
			if (currentPlayer != null)
				QApplication.invokeLater(new MediaPlayerSongUpdate(this));
		}
	}
	public void removedRecord(Record record) { }
	
	///////////
	// TASKS //
	///////////
	
	private class AdvanceSongTask extends CommonTask {
		public String toString() {
			return "Advancing Song";
		}		
		public void execute() {
	    	SongRecord song = PlayerManager.getNextSongToPlay();
	    	if (song != null) {
	    		setSong(song);    
	    		QApplication.invokeAndWait(new MediaPlayerInit(MediaPlayer.instance, false, true));
	    	}			
		}
	}
	
	private class PreviousSongTask extends CommonTask {
		public String toString() {
			return "Previous Song";
		}		
		public void execute() {
	    	SongRecord song = PlayerManager.getPreviousSongToPlay();
	    	if (song != null) {
	    		setSong(song);    
	    		QApplication.invokeAndWait(new MediaPlayerInit(MediaPlayer.instance, false, true));
	    		if (!PlayerManager.hasPreviousSong())
	    			QApplication.invokeAndWait(new MediaPlayerDisableBack(MediaPlayer.instance));
	    	}
			
		}		
	}
    
}
