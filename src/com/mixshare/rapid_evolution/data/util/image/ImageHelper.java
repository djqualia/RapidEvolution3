package com.mixshare.rapid_evolution.data.util.image;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.util.filesystem.FileSystemAccess;

public class ImageHelper {

    static private Logger log = Logger.getLogger(ImageHelper.class);
	
	static public boolean saveImageURL(String imageURL, String filename) {
		InputStream in = null;
		ByteArrayOutputStream out = null;
		try {
			if (!FileSystemAccess.getFileSystem().doesExist(filename)) {
	    	    URL url = new URL(imageURL);			
	    	    in = new BufferedInputStream(url.openStream());
	    	    out = new ByteArrayOutputStream();
	    	    byte[] buffer = new byte[4096];                 	 
	    	    for (int read=0; (read = in.read(buffer)) != -1; out.write(buffer, 0, read));
	    	    FileSystemAccess.getFileSystem().saveData(filename, out.toByteArray(), false);
	    	    out.close();
	    	    out = null;
	    	    in.close();
	    	    in = null;
			}
			return true;
		} catch (FileNotFoundException fnfe) {
			log.debug("saveImageURL(): file not found=" + fnfe);
		} catch (IOException ioe) {
			log.debug("saveImageURL(): IO exception=" + ioe);
		} catch (Exception e) {
			log.error("saveImageURL(): error", e);
		} finally {
			try {
				if (out != null)
					out.close();
				if (in != null)
					in.close();
			} catch (Exception e) { }			
		}
		return false;
	}
	
}
