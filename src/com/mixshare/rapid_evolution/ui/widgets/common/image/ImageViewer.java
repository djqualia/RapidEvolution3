package com.mixshare.rapid_evolution.ui.widgets.common.image;

import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.DataConstantsHelper;
import com.mixshare.rapid_evolution.data.profile.common.image.Image;
import com.mixshare.rapid_evolution.data.profile.filter.FilterProfile;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.util.FileUtil;
import com.mixshare.rapid_evolution.util.image.ImageUtil;
import com.mixshare.rapid_evolution.workflow.TaskManager;
import com.mixshare.rapid_evolution.workflow.user.AddImagesTask;
import com.mixshare.rapid_evolution.workflow.user.ProfileSaveTask;
import com.trolltech.qt.core.QRect;
import com.trolltech.qt.core.QSize;
import com.trolltech.qt.core.QUrl;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.core.Qt.MouseButton;
import com.trolltech.qt.gui.QAction;
import com.trolltech.qt.gui.QBrush;
import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QDropEvent;
import com.trolltech.qt.gui.QFileDialog;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QImage;
import com.trolltech.qt.gui.QLinearGradient;
import com.trolltech.qt.gui.QMessageBox;
import com.trolltech.qt.gui.QMouseEvent;
import com.trolltech.qt.gui.QPaintEvent;
import com.trolltech.qt.gui.QPainter;
import com.trolltech.qt.gui.QPalette;
import com.trolltech.qt.gui.QPixmap;
import com.trolltech.qt.gui.QPolygonF;
import com.trolltech.qt.gui.QResizeEvent;
import com.trolltech.qt.gui.QSizePolicy;
import com.trolltech.qt.gui.QTransform;
import com.trolltech.qt.gui.QWidget;

public class ImageViewer extends QWidget implements DataConstants {
	
	static private Logger log = Logger.getLogger(ImageViewer.class);	
	
	////////////
	// FIELDS //
	////////////
	
	private Vector<Image> images;
	private int currentImageIndex;
	
    private QAction nextAction;
    private QAction previousAction;
    private QAction separator1;
    private QAction setAsThumbnail;
    private QAction separator2;
    private QAction separator3;
    private QAction add;
    private QAction remove;
    private QAction removeAll;
	
    //public Signal1<Boolean> valid = new Signal1<Boolean>();
    
    private SearchProfile searchProfile;

    /////////////////
    // CONSTRUCTOR //
    /////////////////    
    
    public ImageViewer(QWidget parent, SearchProfile searchProfile) {
        super(parent);
        this.searchProfile = searchProfile;
        
        setContextMenuPolicy(Qt.ContextMenuPolicy.ActionsContextMenu);

        int size = 40;
        QPixmap bg = new QPixmap(size, size);
        bg.fill(QColor.white);
        QPainter p = new QPainter();
        p.begin(bg);
        p.fillRect(0, 0, size/2, size/2, new QBrush(QColor.lightGray));
        p.fillRect(size/2, size/2, size/2, size/2, new QBrush(QColor.lightGray));
        p.end();

        QPalette pal = palette();
        pal.setBrush(backgroundRole(), new QBrush(bg));
        setPalette(pal);

        setAutoFillBackground(true);

    	QSizePolicy imageViewerSizePolicy = new QSizePolicy(com.trolltech.qt.gui.QSizePolicy.Policy.Minimum, com.trolltech.qt.gui.QSizePolicy.Policy.Minimum);
    	imageViewerSizePolicy.setHorizontalStretch((byte)1);
    	imageViewerSizePolicy.setVerticalStretch((byte)1);
    	setSizePolicy(imageViewerSizePolicy);	    				
                
        nextAction = new QAction(Translations.get("image_viewer_next"), this);
        nextAction.triggered.connect(this, "nextImage()");
        nextAction.setIcon(new QIcon(RE3Properties.getProperty("menu_next_arrow_icon")));

        previousAction = new QAction(Translations.get("image_viewer_back"), this);
        previousAction.triggered.connect(this, "previousImage()");
        previousAction.setIcon(new QIcon(RE3Properties.getProperty("menu_back_arrow_icon")));
        
        setAsThumbnail = new QAction(Translations.get("image_viewer_set_default"), this);
        setAsThumbnail.triggered.connect(this, "setAsThumbnail()");
        setAsThumbnail.setIcon(new QIcon(RE3Properties.getProperty("menu_set_default_image_icon")));
        
        add = new QAction(Translations.get("image_viewer_add"), this);
        add.triggered.connect(this, "addImages()"); 
        add.setIcon(new QIcon(RE3Properties.getProperty("menu_add_icon")));
        
        remove = new QAction(Translations.get("remove_text"), this);
        remove.triggered.connect(this, "removeImage()");
        remove.setIcon(new QIcon(RE3Properties.getProperty("menu_remove_icon")));

        removeAll = new QAction(Translations.get("remove_all_text"), this);
        removeAll.triggered.connect(this, "removeAllImages()");
        removeAll.setIcon(new QIcon(RE3Properties.getProperty("menu_remove_all_icon")));
        
        separator1 = new QAction("", this);
        separator1.setSeparator(true);
        
        separator2 = new QAction("", this);
        separator2.setSeparator(true);

        separator3 = new QAction("", this);
        separator3.setSeparator(true);
        
        addAction(add);
        
        delayedUpdate.setDelay(10);
               
        setAcceptDrops(true);
    }

    public ImageViewer(QWidget parent, FilterProfile filterProfile) {
        super(parent);
        
        setContextMenuPolicy(Qt.ContextMenuPolicy.ActionsContextMenu);

        int size = 40;
        QPixmap bg = new QPixmap(size, size);
        bg.fill(QColor.white);
        QPainter p = new QPainter();
        p.begin(bg);
        p.fillRect(0, 0, size/2, size/2, new QBrush(QColor.lightGray));
        p.fillRect(size/2, size/2, size/2, size/2, new QBrush(QColor.lightGray));
        p.end();

        QPalette pal = palette();
        pal.setBrush(backgroundRole(), new QBrush(bg));
        setPalette(pal);

        setAutoFillBackground(true);

    	QSizePolicy imageViewerSizePolicy = new QSizePolicy(com.trolltech.qt.gui.QSizePolicy.Policy.Minimum, com.trolltech.qt.gui.QSizePolicy.Policy.Minimum);
    	imageViewerSizePolicy.setHorizontalStretch((byte)1);
    	imageViewerSizePolicy.setVerticalStretch((byte)1);
    	setSizePolicy(imageViewerSizePolicy);	    				
                
        nextAction = new QAction("Next", this);
        nextAction.triggered.connect(this, "nextImage()");

        previousAction = new QAction("Back", this);
        previousAction.triggered.connect(this, "previousImage()");
        
        addAction(nextAction);
        addAction(previousAction);
        
        delayedUpdate.setDelay(10);
    }
    
    protected void mousePressEvent(QMouseEvent event) {
    	if (event.buttons().isSet(MouseButton.LeftButton))
    		nextImage();
    }

    public QImage image() {
        return original;
    }

    public void setImages(Vector<Image> images) {
    	setImages(images, true);
    }
    public void setImages(Vector<Image> images, boolean resetIndex) {
    	this.images = images;
    	if (resetIndex || (currentImageIndex >= images.size()))
    		currentImageIndex = 0;
        removeAction(nextAction);
        removeAction(previousAction);
        if (searchProfile != null) {
        	removeAction(setAsThumbnail); 
        	removeAction(remove);
        	removeAction(add);
        	removeAction(separator1); 
        	removeAction(separator2);
        	removeAction(removeAll);
        	removeAction(separator3);
        }
    	if (images.size() > 1) {
            addAction(nextAction);
            addAction(previousAction);
            if (searchProfile != null) {
            	addAction(separator1);
            	addAction(setAsThumbnail);
            	addAction(separator2);
            	addAction(add);
            	addAction(remove);
            	addAction(separator3);
            	addAction(removeAll);
            }
    	} else if (images.size() == 1) {
    		addAction(add);
    		if (searchProfile != null)
    			addAction(remove);
    	} else {
    		addAction(add);
    	}
    	setImage();
    }
    
    public void setAsThumbnail() {
    	searchProfile.setThumbnailImage(images.get(currentImageIndex));
    	currentImageIndex = 0;
    	ProfileSaveTask.save(searchProfile);
    }
    
    public void removeImage() {
    	images.get(currentImageIndex).setDisabled(true);
    	if (searchProfile.getThumbnailImageFilename().equalsIgnoreCase(images.get(currentImageIndex).getImageFilename())) {
    		Image validImage = null;
    		int index = 0;
    		for (Image image : images) {
    			if (!image.isDisabled()) {
    				validImage = image;
    				break;
    			}
    			++index;
    		}
    		if (validImage != null) {
    			currentImageIndex = index;
    			searchProfile.setThumbnailImage(validImage);
    		} else
    			searchProfile.setThumbnailImageFilename(null, DATA_SOURCE_USER);
    	}
    	ProfileSaveTask.save(searchProfile);
    }
    
    public void removeAllImages() {
		if (QMessageBox.question(this, Translations.get("remove_all_images_title"), Translations.get("remove_all_images_text"), QMessageBox.StandardButton.Yes, QMessageBox.StandardButton.No) != QMessageBox.StandardButton.Yes.value()) {
			return;
		}			    	
		for (Image image : searchProfile.getImages())
			image.setDisabled(true);
		ProfileSaveTask.save(searchProfile);
    }    
    
    public void addImages() {
		QFileDialog fileDialog = new QFileDialog(this);    		
		fileDialog.setFileMode(QFileDialog.FileMode.ExistingFiles);
		fileDialog.setViewMode(QFileDialog.ViewMode.Detail);
		fileDialog.setFilters(ImageUtil.getImageFilters());
		if ((searchProfile != null) && (searchProfile instanceof SongProfile)) {
			SongProfile songProfile = (SongProfile)searchProfile;
			if (songProfile.getSongFilename() != null) {
				String dir = FileUtil.getDirectoryFromFilename(songProfile.getSongFilename());
				if (dir != null)
					fileDialog.setDirectory(dir);				
			}
		}
	    if (fileDialog.exec() == QDialog.DialogCode.Accepted.value()) {
	        List<String> filenames = fileDialog.selectedFiles();
	        TaskManager.runForegroundTask(new AddImagesTask(searchProfile, filenames));
	    }    			
    	
    }
        
    public boolean setImage() {    	
    	if (images.size() > currentImageIndex) {
    		if (log.isDebugEnabled())
    			log.debug("setImage(): setting=" + images.get(currentImageIndex));
    		QImage image = images.get(currentImageIndex).getQImage();
    		if (image != null) {
    			setImage(image);
    			setToolTip(DataConstantsHelper.getDataSourceDescription(images.get(currentImageIndex).getDataSource()) + " (" + image.width() + "x" + image.height() + ")");
    			return true;
    		}    		
    	} else {
    		setImage(null);
    	}
    	return false;
    }
    
    public void setImage(QImage original) {
        this.original = original != null ? original.convertToFormat(QImage.Format.Format_ARGB32_Premultiplied) : null;
        resetImage();

        //valid.emit(original != null);
    }
    
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
			TaskManager.runForegroundTask(new AddImagesTask(searchProfile, filenames));
		}
	}	    

    private void nextImage() {
    	if (images != null) {
	    	++currentImageIndex;
	    	if (currentImageIndex >= images.size())
	    		currentImageIndex = 0;
	    	int startIndex = currentImageIndex;
	    	while (!setImage()) {
		    	++currentImageIndex;
		    	if (currentImageIndex >= images.size())
		    		currentImageIndex = 0;
	    		if (currentImageIndex == startIndex)
	    			return;
	    	}
    	}
    }
    
    private void previousImage() {
    	if (images != null) {
	    	--currentImageIndex;
	    	if (currentImageIndex < 0)
	    		currentImageIndex = images.size() - 1;
	    	int startIndex = currentImageIndex;
	    	while (!setImage()) {
		    	--currentImageIndex;
		    	if (currentImageIndex < 0)
		    		currentImageIndex = images.size() - 1;
	    		if (currentImageIndex == startIndex)
	    			return;	    		
	    	}
    	}
    }
    
    public QImage modifiedImage() {
        return modified;
    }

    public void setColorBalance(int c) {
        colorBalance = c;
        resetImage();
    }

    public void setRedCyan(int c) {
        redCyan = c;
        resetImage();
    }

    public void setGreenMagenta(int c) {
        greenMagenta = c;
        resetImage();
    }

    public void setBlueYellow(int c) {
        blueYellow = c;
        resetImage();
    }

    public void setInvert(boolean b) {
        invert = b;
        resetImage();
    }

    public QSize sizeHint() {
        return new QSize(parentWidget().width(), parentWidget().height());
    }

    protected void paintEvent(QPaintEvent e) {
        if (background == null) {
            background = new QPixmap(size());
            QPainter p = new QPainter(background);
            QLinearGradient lg = new QLinearGradient(0, 0, 0, height());
            lg.setColorAt(0.5, QColor.black);
            lg.setColorAt(0.7, QColor.fromRgbF(0.5, 0.5, 0.6));
            lg.setColorAt(1, QColor.fromRgbF(0.8, 0.8, 0.9));
            p.fillRect(background.rect(), new QBrush(lg));
            p.end();
        }

        QPainter p = new QPainter(this);
        p.drawPixmap(0, 0, background);

        if (modified == null)
            updateImage();

        if (modified != null && !modified.isNull()) {
            p.setViewport(rect().adjusted(10, 10, -10, -10));
            QRect rect = rectForImage(modified);

            p.setRenderHint(QPainter.RenderHint.SmoothPixmapTransform);
            p.drawImage(rect, modified);

            p.drawImage(0, height() - reflection.height() + 10, reflection);

        }

        p.end();
    }

    protected void resizeEvent(QResizeEvent e) {
        if (background != null) {
            background.dispose();
            background = null;
        }

        resetImage();
    }

    private final void resetImage() {
        if (modified != null)
            modified.dispose();
        modified = null;
        delayedUpdate.start();
    }

    private static final QColor decideColor(int value, QColor c1, QColor c2) {
        QColor c = value < 0 ? c1 : c2;
        double sign = value < 0 ? -1.0 : 1.0;
        return QColor.fromRgbF(c.redF(), c.greenF(), c.blueF(), sign * value * 0.5 / 100);
    }

    private void updateImage() {
        if (original == null || original.isNull())
            return;

        if (modified != null)
            modified.dispose();

        modified = original.copy();

        QPainter p = new QPainter();
        p.begin(modified);
        p.setCompositionMode(QPainter.CompositionMode.CompositionMode_SourceAtop);
        if (redCyan != 0) {
            QColor c = decideColor(redCyan, QColor.cyan, QColor.red);
            p.fillRect(0, 0, modified.width(), modified.height(), new QBrush(c));
        }
        if (greenMagenta != 0) {
            QColor c = decideColor(greenMagenta, QColor.magenta, QColor.green);
            p.fillRect(0, 0, modified.width(), modified.height(), new QBrush(c));
        }
        if (blueYellow != 0) {
            QColor c = decideColor(blueYellow, QColor.yellow, QColor.blue);
            p.fillRect(0, 0, modified.width(), modified.height(), new QBrush(c));
        }
        if (colorBalance != 0) {
            QColor c = decideColor(colorBalance, QColor.white, QColor.black);
            p.fillRect(0, 0, modified.width(), modified.height(), new QBrush(c));
        }

        if (invert) {
            p.setCompositionMode(QPainter.CompositionMode.CompositionMode_Difference);
            p.fillRect(modified.rect(), new QBrush(QColor.white));
        }

        p.end();

        reflection = createReflection(modified);
    }

    private QRect rectForImage(QImage image) {
        QSize isize = image.size();
        QSize size = size();

        size.setHeight(size.height() * 3 / 4);
        size.setWidth(size.width() * 3 / 4);

        isize.scale(size, Qt.AspectRatioMode.KeepAspectRatio);

        return new QRect(width() / 2 - isize.width() / 2,
                         size.height() / 2 - isize.height() / 2,
                         isize.width(), isize.height());
    }

    private QImage createReflection(QImage source) {
        if (source == null || source.isNull())
            return null;

        QRect r = rectForImage(source);

        QImage image = new QImage(width(),
                                  height() - r.height() - r.y(),
                                  QImage.Format.Format_ARGB32_Premultiplied);
        image.fill(0);

        double iw = image.width();
        double ih = image.height();

        QPainter pt = new QPainter(image);

        pt.setRenderHint(QPainter.RenderHint.SmoothPixmapTransform);
        pt.setRenderHint(QPainter.RenderHint.Antialiasing);

        pt.save(); {
            QPolygonF imageQuad = new QPolygonF();
            imageQuad.add(0, 0);
            imageQuad.add(0, source.height());
            imageQuad.add(source.width(), source.height());
            imageQuad.add(source.width(), 0);
            QPolygonF targetQuad = new QPolygonF();
            targetQuad.add(0, ih);
            targetQuad.add(iw / 2 - r.width() / 2, 0);
            targetQuad.add(iw / 2 + r.width() / 2, 0);
            targetQuad.add(iw, ih);
            try {
                pt.setTransform(QTransform.quadToQuad(imageQuad, targetQuad));
                pt.drawImage(imageQuad.boundingRect(), source);
            } catch (IllegalArgumentException e) {
                // user has resized the view too small
            }
        } pt.restore();

        QLinearGradient lg = new QLinearGradient(0, 0, 0, image.height());
        lg.setColorAt(0.1, QColor.fromRgbF(0, 0, 0, 0.4));
        lg.setColorAt(0.6, QColor.transparent);
        pt.setCompositionMode(QPainter.CompositionMode.CompositionMode_DestinationIn);
        pt.fillRect(image.rect(), new QBrush(lg));
        pt.end();

        return image;
    }



    private int colorBalance;
    private int redCyan;
    private int greenMagenta;
    private int blueYellow;

    private boolean invert;

    private QImage original;
    private QImage modified;
    private QImage reflection;
    private QPixmap background;

    private Worker delayedUpdate = new Worker(this) {
        public void execute() {
            update();
        }
    };
}
