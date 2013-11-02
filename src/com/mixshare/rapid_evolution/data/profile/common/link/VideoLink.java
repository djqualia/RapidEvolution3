package com.mixshare.rapid_evolution.data.profile.common.link;

import java.awt.image.BufferedImage;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.mined.CommonMiningAPIWrapper;
import com.mixshare.rapid_evolution.data.mined.util.WebHelper;
import com.mixshare.rapid_evolution.data.mined.youtube.YouTubeAPIHelper;
import com.mixshare.rapid_evolution.data.profile.common.image.InvalidImageException;
import com.mixshare.rapid_evolution.data.util.filesystem.FileSystemAccess;
import com.mixshare.rapid_evolution.data.util.image.ImageHelper;
import com.mixshare.rapid_evolution.ui.util.ThumbnailImageFactory;
import com.mixshare.rapid_evolution.util.FileUtil;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.trolltech.qt.gui.QImage;

public class VideoLink extends Link {

	static private Logger log = Logger.getLogger(VideoLink.class);	
    static private final long serialVersionUID = 0L;    

    ////////////
    // FIELDS //
    ////////////
    
    private String thumbnailUrl;
    private String imageFilename;
    
    transient private QImage image;
    transient private boolean fetched = false;    
    
    static {
    	try{
    		// in order for the XMLEncoder to skip transient variables, this is needed
    		BeanInfo info = Introspector.getBeanInfo(VideoLink.class);
    		PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
    		for (int i = 0; i < propertyDescriptors.length; ++i) {
    			PropertyDescriptor pd = propertyDescriptors[i];
    			if (pd.getName().equals("image") || pd.getName().equals("fetched")) {
    				pd.setValue("transient", Boolean.TRUE);
    			}
    		}
    	} catch (IntrospectionException e) {
    		log.error("static(): error", e);
    	}
    }
    
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
    public VideoLink() { }
	public VideoLink(String title, String description, String url, String thumbnailUrl, String type, byte dataSource) throws InvalidLinkException {
		super(title, description, url, type, dataSource);
		this.thumbnailUrl = thumbnailUrl;
		this.imageFilename = downloadImage(thumbnailUrl);
		ensureImageFilenameIsRelative();
		if ("youtube".equalsIgnoreCase(type)) {
			String youtubeID = YouTubeAPIHelper.getYouTubeIDFromURL(url);
			if ((youtubeID != null) && !YouTubeAPIHelper.isValidVideo(youtubeID))
				throw new InvalidLinkException();
		}
	}
	public VideoLink(LineReader lineReader) {
		super(lineReader);
		int version = Integer.parseInt(lineReader.getNextLine());
		thumbnailUrl = lineReader.getNextLine();
		imageFilename = lineReader.getNextLine();
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public String getImageFilename() { return imageFilename; }

	public QImage getImage() {
		if (!fetched) {
			image = ThumbnailImageFactory.fetchThumbnailImage(imageFilename);
			if ((image == null) || (image.isNull()))
				log.debug("getImage(): couldn't load video link image=" + imageFilename);
			fetched = true;
		}
		return image;
	}
	
	public String getThumbnailUrl() {
		return thumbnailUrl;
	}	
	
	/////////////
	// SETTERS //
	/////////////
	
	public void setThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}
	public void setImageFilename(String imageFilename) {
		this.imageFilename = imageFilename;
		ensureImageFilenameIsRelative();
	}		
	
	/////////////
	// METHODS //
	/////////////
	
	private String downloadImage(String url) {
		String filename = WebHelper.getUniqueFilenameFromURL(url);
		if (filename != null) {			
			String videoImageDirectory = CommonMiningAPIWrapper.getMinedDataDirectory(dataSource) + "videoimages/";
			String fullPath = videoImageDirectory + filename;
			if (log.isTraceEnabled())
				log.trace("downloadImage(): saving videoImageURL=" + url + ", to local filepath=" + fullPath);
			if (ImageHelper.saveImageURL(url, fullPath)) {
				if (RE3Properties.getBoolean("server_mode")) {
					try {
						BufferedImage img = FileSystemAccess.getFileSystem().readBufferedImage(fullPath);
					} catch (InvalidImageException e) {
						return null;
					}
				} else {
					try {
						fetched = true;
						image = FileSystemAccess.getFileSystem().readQImage(fullPath);						
					} catch (InvalidImageException e) {
						return null;
					}
				}
				return fullPath;
			}			
		}
		return null;
	}
	
	private void ensureImageFilenameIsRelative() { imageFilename = FileUtil.stripWorkingDirectory(imageFilename); }

    public void write(LineWriter writer) {
    	super.write(writer);
    	writer.writeLine("1"); // version
    	writer.writeLine(thumbnailUrl);
    	writer.writeLine(imageFilename);
    }
    
}
