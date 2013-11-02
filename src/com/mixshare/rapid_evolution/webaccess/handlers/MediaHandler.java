package com.mixshare.rapid_evolution.webaccess.handlers;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.AbstractHandler;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.webaccess.util.FileServletUtil;

public class MediaHandler extends AbstractHandler {

	static private Logger log = Logger.getLogger(MediaHandler.class);

    @Override
	public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException {
    	if (request.getPathInfo().startsWith("/media")) {
	    	try {
	    		int id = Integer.parseInt(request.getParameter("id"));
	    		SongRecord song = Database.getSongIndex().getSongRecord(id);
	    		if ((song != null) && song.hasValidSongFilename()) {
	    			String userAgent = request.getHeader("User-Agent");
    				FileServletUtil.sendFile(song.getSongFilename(), request, response, true);
	    		}
	    	} catch (org.mortbay.jetty.EofException e) {
	    		if (log.isTraceEnabled())
	    			log.trace("handle(): eof exception (connection closed?)");
	    	} catch (Exception e) {
	    		log.error("handle(): error getting song media", e);
	    	} finally {
	    		((Request)request).setHandled(true);
	    	}
    	}
    }

}
