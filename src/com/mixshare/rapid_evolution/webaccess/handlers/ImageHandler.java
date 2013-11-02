package com.mixshare.rapid_evolution.webaccess.handlers;

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.servlet.GzipFilter.GzipStream;

import com.mixshare.rapid_evolution.data.profile.common.image.InvalidImageException;
import com.mixshare.rapid_evolution.data.util.filesystem.FileSystemAccess;
import com.mixshare.rapid_evolution.util.image.ImageUtil;

public class ImageHandler extends AbstractHandler {

	static private Logger log = Logger.getLogger(ImageHandler.class);
	
    public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException {
    	String filename = null;
    	try {
	    	if ((request.getPathInfo().startsWith("/image")) && (!request.getPathInfo().startsWith("/images/"))) {
	    		((Request)request).setHandled(true);
	    		filename = request.getParameter("filename");	    		    		
	    		int height = Integer.parseInt(request.getParameter("height"));
	    		int width = Integer.parseInt(request.getParameter("width"));
	    		if (filename != null) {
	    			BufferedImage image = FileSystemAccess.getFileSystem().readBufferedImage(filename);
	    			if ((image != null) && (image.getWidth() > 0) && (image.getHeight() > 0) && (image.getType() != BufferedImage.TYPE_CUSTOM)) {
	    				BufferedImage scaledImage = ImageUtil.getScaledInstance(image, width, height, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	    				if (scaledImage != null) {
	    					response.setContentType("image/png");
	    					ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	    					ImageIO.write(scaledImage, "png", outputStream);
	    					byte[] b = outputStream.toByteArray();
	    					//OutputStream os = response.getOutputStream(); 
	    					GzipStream os = new GzipStream(request, response, b.length, 8192, 100); 	    		    					
	    					os.write(b);
	    					os.close(); 
	    					return;
	    				}
	    			}
	    		}
	    	}
    	} catch (InvalidImageException ie) {
    		if (log.isDebugEnabled())
    			log.debug("handle(): invalid image, filename=" + filename);
    	} catch (Exception e) {
    		log.error("handle(): error getting song media", e);
    	} finally { }
    }

}
