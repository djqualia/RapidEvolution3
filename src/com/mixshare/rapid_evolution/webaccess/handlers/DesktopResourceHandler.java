package com.mixshare.rapid_evolution.webaccess.handlers;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.mortbay.jetty.MimeTypes;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.servlet.GzipFilter.GzipStream;

import com.mixshare.rapid_evolution.util.FileUtil;

public class DesktopResourceHandler extends AbstractHandler {

	static private Logger log = Logger.getLogger(DesktopResourceHandler.class);

	static private MimeTypes mt = new MimeTypes();

    public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException {
    	try {    		    		
    		((Request)request).setHandled(true);
    		log.info("user agent=" + request.getHeader("User-Agent"));    		    		
    		String resource = request.getPathInfo();
    		if (resource.equals("/"))
    			resource = "/index.html";
    		byte[] b = FileUtil.getBytesFromFile(new File("web/desktop" + resource));
			GzipStream os = new GzipStream(request, response, b.length, 8192, 100); 	    		    					
			os.write(b);
			os.close(); 
			if (resource.endsWith(".js")) {
				response.setContentType("text/javascript");
			} else {
			   String s = mt.getMimeByExtension(resource).toString();  
			   response.setContentType(s);
			}     		    			
    	} catch (Exception e) {
    		log.error("handle(): error getting views", e);
    	}
    }

}
