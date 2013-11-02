package com.mixshare.rapid_evolution.ui.widgets.mediaplayer;

import java.io.File;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.profile.common.image.Image;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.util.filesystem.FileSystemAccess;
import com.mixshare.rapid_evolution.player.PlayerManager;
import com.mixshare.rapid_evolution.ui.util.ThumbnailImageFactory;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.util.OSHelper;
import com.mixshare.rapid_evolution.workflow.user.ProfileSaveTask;
import com.trolltech.qt.core.QPoint;
import com.trolltech.qt.core.QRect;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.core.Qt.MouseButton;
import com.trolltech.qt.gui.QAction;
import com.trolltech.qt.gui.QCloseEvent;
import com.trolltech.qt.gui.QDesktopWidget;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QKeyEvent;
import com.trolltech.qt.gui.QMouseEvent;
import com.trolltech.qt.gui.QPixmap;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QSlider;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;
import com.trolltech.qt.phonon.MediaObject;
import com.trolltech.qt.phonon.VideoWidget;

public class MediaPlayerVideoWindow extends QWidget implements DataConstants {
	
	static private Logger log = Logger.getLogger(MediaPlayerVideoWindow.class);	
	
	static public final int SLIDER_RANGE = 5;
	static private final long SINGLE_CLICK_DELAY_MILLIS = RE3Properties.getLong("single_click_delay_millis");
	
	////////////
	// FIELDS //
	////////////
	
    private VideoWidget videoWidget = new VideoWidget();
    private REMediaSlider slider = null;

    private MediaObject mediaObject;
    
	private QPushButton backButton = null;
    private QPushButton forwardButton = null;
    
    private QAction pauseAction;
    private QAction resumeAction;
    private QAction saveAsThumbnail;
    
    private SingleClickDetector clickDetector = null;
    
    /////////////////
    // CONSTRUCTOR //
    /////////////////    
	
	public MediaPlayerVideoWindow() {
        QVBoxLayout videoLayout = new QVBoxLayout();
        QHBoxLayout sliderLayout = new QHBoxLayout();

        videoLayout.setMargin(0);
        sliderLayout.setMargin(0);
        
        QSlider brightnessSlider = new QSlider(Qt.Orientation.Horizontal);
        brightnessSlider.setRange(-SLIDER_RANGE, SLIDER_RANGE);
        brightnessSlider.setValue(0);
        brightnessSlider.valueChanged.connect(this, "setBrightness(int)");

        QSlider hueSlider = new QSlider(Qt.Orientation.Horizontal);
        hueSlider.setRange(-SLIDER_RANGE, SLIDER_RANGE);
        hueSlider.setValue(0);
        hueSlider.valueChanged.connect(this, "setHue(int)");

        QSlider saturationSlider = new QSlider(Qt.Orientation.Horizontal);
        saturationSlider.setRange(-SLIDER_RANGE, SLIDER_RANGE);
        saturationSlider.setValue(0);
        saturationSlider.valueChanged.connect(this, "setSaturation(int)");

        QSlider contrastSlider = new QSlider(Qt.Orientation.Horizontal);
        contrastSlider.setRange(-SLIDER_RANGE, SLIDER_RANGE);
        contrastSlider.setValue(0);
        contrastSlider.valueChanged.connect(this, "setContrast(int)");

        slider = new REMediaSlider(this);
        slider.setMaximumHeight(20);
        
        forwardButton = new QPushButton(this);
        forwardButton.setIcon(new QIcon(new QPixmap(RE3Properties.getProperty("media_player_forward_icon_filename"))));
        
        backButton = new QPushButton(this);
        backButton.setIcon(new QIcon(new QPixmap(RE3Properties.getProperty("media_player_back_icon_filename"))));
        
        /*
        sliderLayout.addWidget(new QLabel("bright"));
        sliderLayout.addWidget(brightnessSlider);
        sliderLayout.addWidget(new QLabel("col"));
        sliderLayout.addWidget(hueSlider);
        sliderLayout.addWidget(new QLabel("sat"));
        sliderLayout.addWidget(saturationSlider);
        sliderLayout.addWidget(new QLabel("cont"));
        sliderLayout.addWidget(contrastSlider);
        */
        sliderLayout.addWidget(backButton);
        sliderLayout.addWidget(slider);
        sliderLayout.addWidget(forwardButton);

        videoLayout.addWidget(videoWidget);
        videoLayout.addLayout(sliderLayout);
        setLayout(videoLayout);
        setWindowTitle(Translations.get("video_window_title"));
        setAttribute(Qt.WidgetAttribute.WA_QuitOnClose, false);
        setAttribute(Qt.WidgetAttribute.WA_MacBrushedMetal);
        setMinimumSize(100, 100);
            
        setWindowIcon(new QIcon(RE3Properties.getProperty("application_icon_filename")));
        
        backButton.clicked.connect(MediaPlayer.instance, "back()");
        forwardButton.clicked.connect(MediaPlayer.instance, "forward()");        
        
        videoWidget.setContextMenuPolicy(Qt.ContextMenuPolicy.ActionsContextMenu);
        
        pauseAction = new QAction(Translations.get("pause_text"), this);
        pauseAction.triggered.connect(MediaPlayer.instance, "pause()");        
        pauseAction.setIcon(new QIcon(new QPixmap(RE3Properties.getProperty("media_player_pause_icon_filename"))));

        resumeAction = new QAction(Translations.get("resume_text"), this);
        resumeAction.triggered.connect(MediaPlayer.instance, "play()");        
        resumeAction.setIcon(new QIcon(new QPixmap(RE3Properties.getProperty("media_player_play_icon_filename"))));

        saveAsThumbnail = new QAction(Translations.get("save_as_thumbnail_text"), this);
        saveAsThumbnail.triggered.connect(this, "saveAsThumbnail()");        
        saveAsThumbnail.setIcon(new QIcon(new QPixmap(RE3Properties.getProperty("menu_set_default_image_icon"))));

        backButton.setEnabled(false);
        
        videoWidget.addAction(pauseAction);
        videoWidget.setAutoFillBackground(true);
        
        slider.valueChanged.connect(this, "seekSliderChanged(Integer)");
        slider.sliderMoved.connect(this, "seekSliderMoved(Integer)");        
	}

	/////////////
	// GETTERS //
	/////////////
	
    public VideoWidget getVideoWidget() { return videoWidget; }	
    
	public QPushButton getBackButton() { return backButton; }
	
    public MediaObject getMediaObject() { return mediaObject; }
	
    public REMediaSlider getSlider() { return slider; }
    
	/////////////
	// SETTERS //
	/////////////
	
    public void setIsPlayingNow(boolean playing, double currentPosition) {
    	videoWidget.removeAction(pauseAction);
    	videoWidget.removeAction(resumeAction);
    	videoWidget.removeAction(saveAsThumbnail);
    	if (playing) {
    		videoWidget.addAction(pauseAction);
    	} else {
    		videoWidget.addAction(resumeAction);
    		videoWidget.addAction(saveAsThumbnail);
    	}
    }	
	
    private void setSaturation(int val) { videoWidget.setSaturation(val / (float)SLIDER_RANGE); }
    private void setHue(int val) { videoWidget.setHue(val / (float)SLIDER_RANGE); }
    private void setBrightness(int val) { videoWidget.setBrightness(val / (float)SLIDER_RANGE); }
    private void setContrast(int val) { videoWidget.setContrast(val / (float)SLIDER_RANGE); }	

	public void setMediaObject(MediaObject mediaObject) { this.mediaObject = mediaObject; }
    	
    /////////////
    // METHODS //
    /////////////
    
    
	////////////
	// EVENTS //
	////////////
	
    protected void keyPressEvent(QKeyEvent keyEvent) {
		if (keyEvent.key() == Qt.Key.Key_Space.value()) {
			if (MediaPlayer.instance.isPlaying())
				MediaPlayer.instance.pause();
			else
				MediaPlayer.instance.play();			
		}
    }
    
    protected void mouseDoubleClickEvent(QMouseEvent mouseEvent) {
    	if (clickDetector != null)
    		clickDetector.disable();
    	if (isFullScreen())
    		setWindowState(Qt.WindowState.WindowActive);
    	else
    		setWindowState(Qt.WindowState.WindowFullScreen);
    }
    
    protected void mousePressEvent(QMouseEvent mouseEvent) {
    	if (log.isDebugEnabled())
    		log.debug("mousePressEvent(): mouseEvent=" + mouseEvent);
    	super.mousePressEvent(mouseEvent);
    	if (RE3Properties.getBoolean("media_player_video_single_click_pause")) {
    		if (mouseEvent.buttons().isSet(MouseButton.LeftButton)) {
    			clickDetector = new SingleClickDetector();
    			clickDetector.start();
    		}
    	}
    	//MediaPlayer.instance.setIsPlayingNow(!MediaPlayer.instance.isPlaying(), MediaPlayer.instance.getCurrentPosition());
    }
    
	protected void closeEvent(QCloseEvent closeEvent) {
		MediaPlayer.instance.close();		
	}	
	
    public void handleVideoChanged(boolean hasVideo) {
        if (hasVideo){
        	if (RE3Properties.getBoolean("video_player_auto_resize") && !isFullScreen()) {
        		QDesktopWidget desktop = new QDesktopWidget();
        		QRect videoHintRect = new QRect(new QPoint(0, 0), sizeHint());
        		QRect newVideoRect = desktop.screenGeometry().intersected(videoHintRect);
        		resize(newVideoRect.size());
        	}
        }
        setVisible(hasVideo);
    }
    
    protected void saveAsThumbnail() {
    	try {
	    	QPixmap pixMap = QPixmap.grabWidget(videoWidget);
	    	String filename = "/data/videocapture/" + PlayerManager.getCurrentSong().getUniqueId() + ".jpg";
	    	FileSystemAccess.getFileSystem().saveImage(filename, pixMap.toImage(), true);
	    	SongProfile currentSong = PlayerManager.getCurrentSongProfile();
	    	currentSong.addImage(new Image(filename, filename, DATA_SOURCE_USER), true);
	    	ThumbnailImageFactory.clearCache(filename);
	    	ProfileSaveTask.save(currentSong);
    	} catch (Exception e) {
    		log.error("saveAsThumbnail(): error", e);
    	}
    }
	
    private class SingleClickDetector extends Thread {
    	private boolean disabled;
    	public void run() {
    		try {
    			Thread.sleep(SINGLE_CLICK_DELAY_MILLIS);
    			if (!disabled) {
    				if (log.isTraceEnabled())
    					log.trace("run(): single click detected");
    				if (MediaPlayer.instance.isPlaying())
    					MediaPlayer.instance.pause();
    				else
    					MediaPlayer.instance.play();
    			}
    		} catch (Exception e) {
    			log.error("run(): error", e);
    		}
    		clickDetector = null;
    	}
    	public void disable() { disabled = true; }
    }
    
    public void seekSliderMoved(Integer value) {
    	if (log.isTraceEnabled())
    		log.trace("seekSliderMoved(): value=" + value);
    	double percent = ((double)value / slider.getMaxValue());
    	mediaObject.seek((long)(percent * mediaObject.totalTime()));
    }
    public void seekSliderChanged(Integer value) {
    	if (log.isDebugEnabled())
    		log.debug("seekSliderChanged(): value=" + value);
    	double percent = ((double)value / slider.getMaxValue());
    	mediaObject.seek((long)(percent * mediaObject.totalTime()));
    }        

}
