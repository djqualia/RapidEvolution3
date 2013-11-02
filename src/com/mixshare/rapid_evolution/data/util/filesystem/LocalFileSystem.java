package com.mixshare.rapid_evolution.data.util.filesystem;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.profile.common.image.InvalidImageException;
import com.mixshare.rapid_evolution.util.FileUtil;
import com.mixshare.rapid_evolution.util.OSHelper;
import com.mixshare.rapid_evolution.util.StringUtil;
import com.trolltech.qt.gui.QImage;

public class LocalFileSystem implements FileSystemInterface {

    static private Logger log = Logger.getLogger(LocalFileSystem.class);
    static private final long serialVersionUID = 0L;    	
	
    public String[] getFilenames(String directoryPath) {
    	directoryPath = scrubPath(directoryPath);
    	return new File(OSHelper.getWorkingDirectory() + validateRelativeDirectory(directoryPath)).list();
    }
    
	public boolean doesExist(String relativePath) {
		relativePath = scrubPath(relativePath);
		return new File(OSHelper.getWorkingDirectory() + validateRelativePath(relativePath)).exists();
	}
	
	public long getLastModified(String relativePath) {
		relativePath = scrubPath(relativePath);
		return new File(OSHelper.getWorkingDirectory() + validateRelativePath(relativePath)).lastModified();
	}
    
	public String saveImage(String relativePath, QImage image, boolean overwrite) {
		try {
			relativePath = scrubPath(relativePath);
			String relativeDirectory = FileUtil.getDirectoryFromFilename(relativePath);
			String filename = FileUtil.getFilenameMinusDirectory(relativePath);			
			String directory = OSHelper.getWorkingDirectory() + validateRelativeDirectory(relativeDirectory);
	        File dir = new File(directory);
	        if (!dir.exists())
	        	dir.mkdirs();
	        String fullPath = directory + filename;
	        File file = new File(fullPath);
	        if (overwrite || !file.exists())
	        	image.save(fullPath);	        
	        return relativeDirectory + filename;
		} catch (Exception e) {
			log.error("saveImage(): error", e);
		}
		return null;
	}
	
	
	public String saveImage(String relativePath, BufferedImage image, boolean overwrite) {
		try {
			relativePath = scrubPath(relativePath);
			String relativeDirectory = FileUtil.getDirectoryFromFilename(relativePath);
			String filename = FileUtil.getFilenameMinusDirectory(relativePath);			
			String directory = OSHelper.getWorkingDirectory() + validateRelativeDirectory(relativeDirectory);
	        File dir = new File(directory);
	        if (!dir.exists())
	        	dir.mkdirs();
	        String fullPath = directory + filename;
	        File file = new File(fullPath);
	        if (overwrite || !file.exists())
	        	ImageIO.write(image, "jpg", file);
	        return relativeDirectory + filename;
		} catch (Exception e) {
			log.error("saveImage(): error", e);
		}
		return null;
	}
	
	public String saveData(String relativePath, byte[] data, boolean overwrite) {
		try {			
			relativePath = scrubPath(relativePath);
			String relativeDirectory = FileUtil.getDirectoryFromFilename(relativePath);
			String filename = FileUtil.getFilenameMinusDirectory(relativePath);			
	        String directory = OSHelper.getWorkingDirectory() + validateRelativeDirectory(relativeDirectory);
	        File dir = new File(directory);
	        if (!dir.exists())
	        	dir.mkdirs();
	        String fullPath = directory + filename;
	        File file = new File(fullPath);
	        if (overwrite || !file.exists()) {
                FileOutputStream outputstream = new FileOutputStream(file);
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputstream);
                bufferedOutputStream.write(data);
                bufferedOutputStream.close();
                outputstream.close();                		            	        	
	        }
	        return relativeDirectory + filename;
		} catch (Exception e) {
			log.error("saveData(): error", e);
		}
		return null;
	}
	
	public byte[] readData(String relativePath) {
		try {
			relativePath = scrubPath(relativePath);
			File file = new File(OSHelper.getWorkingDirectory() + validateRelativePath(relativePath));		
			InputStream is = new FileInputStream(file); 
			long length = file.length(); 
			byte[] bytes = new byte[(int)length]; 
			int offset = 0; 
			int numRead = 0; 
			while ((offset < bytes.length) && ((numRead=is.read(bytes, offset, bytes.length-offset)) >= 0))
				offset += numRead;			
			if (offset < bytes.length)
				throw new IOException("Could not completely read file " + file.getName()); 			
			is.close(); 
			return bytes;
		} catch (Exception e) {
			log.error("readData(): error", e);
		}
		return null;
	}
	
	public QImage readQImage(String relativePath) throws InvalidImageException {
		try {
			relativePath = scrubPath(relativePath);
			QImage image = RE3Properties.getProperty("default_thumbnail_image_filename").equals(relativePath) ?
					new QImage(relativePath) :
						new QImage(OSHelper.getWorkingDirectory() + validateRelativePath(relativePath));
			if (image.isNull())
				throw new InvalidImageException();
			return image;
		} catch (InvalidImageException iie) {
			if (log.isDebugEnabled())
				log.debug("readQImage(): invalid image=" + relativePath);
			throw new InvalidImageException();
		} catch (Exception e) {
			log.error("readQImage(): error loading image=" + relativePath, e);
			throw new InvalidImageException();
		}
	}
	
	public BufferedImage readBufferedImage(String relativePath) throws InvalidImageException {
		try {
			relativePath = scrubPath(relativePath);
			BufferedImage img = RE3Properties.getProperty("default_thumbnail_image_filename").equals(relativePath) ?
				ImageIO.read(new File(relativePath)) :
					ImageIO.read(new File(OSHelper.getWorkingDirectory() + validateRelativePath(relativePath)));
			if (img == null)
				throw new InvalidImageException();
			return img;
		} catch (java.awt.color.CMMException cmme) {
			if (log.isDebugEnabled())
				log.debug("readBufferedImage(): invalid image cmmexception=" + cmme);
			throw new InvalidImageException();						
		} catch (java.lang.IllegalArgumentException iae) {
			if (log.isDebugEnabled())
				log.debug("readBufferedImage(): illegal arguments exception=" + iae);
			throw new InvalidImageException();			
		} catch (IIOException iioe) {
			if (log.isDebugEnabled())
				log.debug("readBufferedImage(): image IO exception=" + iioe + ", opening path=" + relativePath);
			throw new InvalidImageException();
		} catch (InvalidImageException iie) {
			if (log.isDebugEnabled())
				log.debug("readQImage(): invalid image=" + relativePath);
			throw new InvalidImageException();
		} catch (Exception e) {
			log.error("readBufferedImage(): error", e);
			throw new InvalidImageException();
		}			
	}	

	private String validateRelativePath(String relativePath) {
		if (!relativePath.startsWith("/"))
			relativePath = "/" + relativePath;
		return relativePath;
	}
	
	private String validateRelativeDirectory(String relativeDirectory) {
		if (relativeDirectory == null)
			relativeDirectory = "";
		if (!relativeDirectory.startsWith("/"))
			relativeDirectory = "/" + relativeDirectory;
		if (!relativeDirectory.endsWith("/"))
			relativeDirectory = relativeDirectory + "/";
		if (relativeDirectory.equals("//"))
			relativeDirectory = "/";
		return relativeDirectory;
	}
	
	static public void main(String[] args) {
		try {
			RapidEvolution3.loadLog4J();
			
			BufferedImage img = ImageIO.read(new File("R:/data/idiomag/images/imagesidiomagcom630671807.jpg"));
			if ((img.getWidth() > 0) && (img.getHeight() > 0))
				log.info("is valid?");
			else
				log.info("gotcha bitch");

		} catch (Exception e) {
			log.error("main(): error", e);
		}
	}
	
	static private String scrubPath(String relativePath) {
		if (relativePath == null)
			return null;
		relativePath = StringUtil.replace(relativePath, "\\", "/");
		return relativePath;
	}
	
}
