package com.mixshare.rapid_evolution.data.profile.common.image;

import java.awt.image.BufferedImage;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.DataConstantsHelper;
import com.mixshare.rapid_evolution.data.mined.CommonMiningAPIWrapper;
import com.mixshare.rapid_evolution.data.mined.util.WebHelper;
import com.mixshare.rapid_evolution.data.util.filesystem.FileSystemAccess;
import com.mixshare.rapid_evolution.data.util.image.ImageHelper;
import com.mixshare.rapid_evolution.util.FileUtil;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.trolltech.qt.gui.QImage;

public class Image implements Serializable {

	static private Logger log = Logger.getLogger(Image.class);	
    static private final long serialVersionUID = 0L;    
	
    ////////////
    // FIELDS //
    ////////////
    
	private String url;
	private String imageFilename;
	private byte dataSource;
	private boolean disabled;
	private String description;
	
	transient private QImage qImage;
	transient private BufferedImage bufferedImage;
	
    static {
    	try{
    		// in order for the XMLEncoder to skip transient variables, this is needed
    		BeanInfo info = Introspector.getBeanInfo(Image.class);
    		PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
    		for (int i = 0; i < propertyDescriptors.length; ++i) {
    			PropertyDescriptor pd = propertyDescriptors[i];
    			if (pd.getName().equals("qImage") || pd.getName().equals("bufferedImage")) {
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
	
	public Image() { }

	public Image(String url, byte dataSource, String description) throws InvalidImageException {
		this.url = url;
		this.dataSource = dataSource;		
		this.imageFilename = downloadImage(url);
		this.description = description;
		if (this.imageFilename == null)
			throw new InvalidImageException();	
		ensureImageFilenameIsRelative();
	}

	public Image(String url, byte dataSource) throws InvalidImageException {
		this.url = url;
		this.dataSource = dataSource;		
		this.imageFilename = downloadImage(url);
		if (this.imageFilename == null)
			throw new InvalidImageException();	
		ensureImageFilenameIsRelative();
	}

	public Image(String url, String imageFilename, byte dataSource) throws InvalidImageException {
		this.url = url;
		this.dataSource = dataSource;		
		this.imageFilename = imageFilename;
		if (RE3Properties.getBoolean("server_mode")) {			
			BufferedImage img = FileSystemAccess.getFileSystem().readBufferedImage(imageFilename);
		} else {
			this.qImage = FileSystemAccess.getFileSystem().readQImage(imageFilename);
		}
        ensureImageFilenameIsRelative();
	}
	
	public Image(String url, String imageFilename, byte dataSource, String description) throws InvalidImageException {
		this.url = url;
		this.dataSource = dataSource;		
		this.imageFilename = imageFilename;
		this.description = description;
		if (RE3Properties.getBoolean("server_mode")) {			
			BufferedImage img = FileSystemAccess.getFileSystem().readBufferedImage(imageFilename);
		} else {
			this.qImage = FileSystemAccess.getFileSystem().readQImage(imageFilename);
		}
        ensureImageFilenameIsRelative();
	}	
	public Image(LineReader lineReader) {
		int version = Integer.parseInt(lineReader.getNextLine());
		url = lineReader.getNextLine();
		imageFilename = lineReader.getNextLine();
		dataSource = Byte.parseByte(lineReader.getNextLine());
		disabled = Boolean.parseBoolean(lineReader.getNextLine());
		description = lineReader.getNextLine();		
	}

	/////////////
	// GETTERS //
	/////////////
	
	public String getUrl() { return url; }
	public byte getDataSource() { return dataSource; }
	public String getImageFilename() { return imageFilename; }
	public boolean isDisabled() { return disabled; }
	public String getDescription() {
		if (description == null)
			return "";
		return description; 
	}

	public QImage getQImage() {
		if (qImage == null) {
			try {
				qImage = FileSystemAccess.getFileSystem().readQImage(imageFilename);
			} catch (InvalidImageException e) { }
		}
		return qImage;
	}
	
	public BufferedImage getBufferedImage() {
		if (bufferedImage == null) {
			try {
				bufferedImage = FileSystemAccess.getFileSystem().readBufferedImage(imageFilename);
			} catch (InvalidImageException e) { }
		}
		return bufferedImage;
	}
	
	/////////////
	// SETTERS //
	/////////////
	
	public void setUrl(String url) { this.url = url; }
	public void setImageFilename(String imageFilename) {
		this.imageFilename = imageFilename;
		ensureImageFilenameIsRelative();
	}
	public void setDataSource(byte dataSource) { this.dataSource = dataSource; }
	public void setDescription(String description) { this.description = description; }	
	public void setDisabled(boolean disabled) { this.disabled = disabled; }
	
	/////////////
	// METHODS //
	/////////////
	
	private String downloadImage(String url) {
		String filename = WebHelper.getUniqueFilenameFromURL(url);
		if (filename != null) {			
			String imageDirectory = CommonMiningAPIWrapper.getMinedDataDirectory(dataSource) + "images/";
			String fullPath = imageDirectory + filename;
			if (log.isTraceEnabled())
				log.trace("downloadImage(): saving imageURL=" + url + ", to local filepath=" + fullPath);
			if (ImageHelper.saveImageURL(url, fullPath)) {
				if (!RE3Properties.getBoolean("server_mode")) {
					try {						
						qImage = FileSystemAccess.getFileSystem().readQImage(fullPath);
					} catch (InvalidImageException e) {
						return null;
					}
				} else {
					try {
						BufferedImage img = FileSystemAccess.getFileSystem().readBufferedImage(fullPath);
					} catch (InvalidImageException e) {
						return null;
					}
				}
				return fullPath;
			}			
		}
		return null;
	}
		
	public String toString() { return url + " (" + DataConstantsHelper.getDataSourceDescription(dataSource) + ")"; }
	
	public boolean equals(Object o) {
		if (o instanceof Image) {
			Image i = (Image)o;
			return url.equals(i.url);
		}
		return false;
	}
	
	public int hashCode() { return url.hashCode(); }	
	
	static public void main(String[] args) {
		try {
	        PropertyConfigurator.configureAndWatch("log4j.properties");   
	        // ... for testing...
		} catch (Exception e) {
			log.error("main(): error", e);
		}
	}
	
	private void ensureImageFilenameIsRelative() { imageFilename = FileUtil.stripWorkingDirectory(imageFilename); }

    public void write(LineWriter writer) {
    	writer.writeLine("1"); // version
    	writer.writeLine(url);
    	writer.writeLine(imageFilename);
    	writer.writeLine(dataSource);
    	writer.writeLine(disabled);
    	writer.writeLine(description);
    }	
}
