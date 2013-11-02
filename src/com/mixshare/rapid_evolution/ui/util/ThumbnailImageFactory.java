package com.mixshare.rapid_evolution.ui.util;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.profile.common.image.InvalidImageException;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.data.util.cache.LRUCache;
import com.mixshare.rapid_evolution.data.util.filesystem.FileSystemAccess;
import com.mixshare.rapid_evolution.util.FileUtil;
import com.trolltech.qt.core.QSize;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QImage;

/**
 * This class allows for a lazy style loading of thumbnail images.  Each search record has a flag of whether
 * the thumbnail image is loaded, set to false initially.  The table models will check this flag when populating the image
 * column, and if the image is not loaded, they will return null and call the "loadThumbnailImage" method which will add
 * the image filename to a queue, and when the image is finally read by the worker thread, the model/UI will be updated.
 */
public class ThumbnailImageFactory extends Thread {

    static private Logger log = Logger.getLogger(ThumbnailImageFactory.class);    
	        
    ///////////////////
    // STATIC FIELDS //
    ///////////////////
    
    static public final QSize THUMBNAIL_SIZE = new QSize(RE3Properties.getInt("thumbnail_image_size"), RE3Properties.getInt("thumbnail_image_size"));    
    static private LRUCache thumbnails = new LRUCache(RE3Properties.getInt("thumbnail_image_cache_size")); // String to QImage objects
        
    ////////////////////
    // STATIC METHODS //
    ////////////////////
        
    
    /**
     * Reads the image from the disk and stores it in the cache/map for lookup.
     */
	static public QImage fetchThumbnailImage(String imageFilename) {
		if (imageFilename == null)
			return null;
		String fileKey = FileUtil.unify(imageFilename);
		QImage result = (QImage)thumbnails.get(fileKey);
		if (result != null)
			return result;
		try {
			QImage img = fetchThumbnailImageNow(imageFilename);
			if (img != null) {
	            thumbnails.add(fileKey, img);
	            result = img;				
			}
		} catch (Exception e) {
			log.error("fetchThumbnailImage(): error", e);
		}
		return result;
	}
	static public void clearCache(String filename) {
		String fileKey = FileUtil.unify(filename);
		thumbnails.remove(fileKey);
	}	

	static private QImage fetchThumbnailImageNow(String imageFilename) { return fetchThumbnailImageNow(imageFilename, THUMBNAIL_SIZE); }
	static public QImage fetchThumbnailImageNow(String imageFilename, QSize size) {
		try {
			QImage img = null;
			if (!imageFilename.equals(SearchRecord.DEFAULT_THUMBNAIL_IMAGE))
				 img = FileSystemAccess.getFileSystem().readQImage(imageFilename);
			else
				img = new QImage(imageFilename);
				
        	/*
	        QImage result = (img.width() > size.width()
                    || img.height() > size.height())
                    ? img.scaled(size, Qt.AspectRatioMode.KeepAspectRatio, Qt.TransformationMode.SmoothTransformation)
                    : img.copy();
                    */
        	
	        QImage result = null;
	        if ((img.width() > size.width() || img.height() > size.height())) {
        		result = img.scaled(size, Qt.AspectRatioMode.KeepAspectRatio, Qt.TransformationMode.SmoothTransformation);
	        } else {
	        	result = img.copy();
	        }
                    
            img.dispose();
            return result;
		} catch (InvalidImageException ie) {
        	if (log.isDebugEnabled())
        		log.debug("fetchThumbnailImageNow(): could not load image=" + imageFilename);
		} catch (Exception e) {
			log.error("fetchThumbnailImageNow(): error", e);
		}
        return null;
	}	
		
}
