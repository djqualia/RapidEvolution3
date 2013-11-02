package com.mixshare.rapid_evolution.webaccess.handlers;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.AbstractHandler;

import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.music.rating.Rating;
import com.mixshare.rapid_evolution.ui.model.search.SearchModelManager;

public class RatingHandler extends AbstractHandler implements DataConstants {

	static private Logger log = Logger.getLogger(RatingHandler.class);

    @Override
	public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException {
    	try {
	    	if (request.getPathInfo().startsWith("/rate_song")) {
	    		((Request)request).setHandled(true);
	    		int id = Integer.parseInt(request.getParameter("id"));
	    		int value = Integer.parseInt(request.getParameter("value"));
	    		SongProfile song = Database.getSongIndex().getSongProfile(id);
	    		if (song != null) {
	    			Rating newRating = Rating.getRating(value * 20);
	    			if (log.isDebugEnabled())
	    				log.debug("handle(): rating song=" + song + ", value=" + newRating);
	    			song.setRating(newRating, DATA_SOURCE_USER);
	    			song.save();
	    		}
	    	} else if (request.getPathInfo().startsWith("/rate")) {
	    		((Request)request).setHandled(true);
	    		int id = Integer.parseInt(request.getParameter("id"));
	    		String searchType = request.getParameter("type").trim();
	    		SearchModelManager modelManager = Database.getWebServerManager().getModelManager(searchType);
	    		int value = Integer.parseInt(request.getParameter("value"));
	    		SearchProfile profile = (SearchProfile)modelManager.getIndex().getProfile(id);
	    		if (profile != null) {
	    			Rating newRating = Rating.getRating(value * 20);
	    			if (log.isDebugEnabled())
	    				log.debug("handle(): rating profile=" + profile + ", value=" + newRating);
	    			profile.setRating(newRating, DATA_SOURCE_USER);
	    			profile.save();
	    		}
	    	}
    	} catch (Exception e) {
    		log.error("handle(): error rating song", e);
    	}
    }

}
