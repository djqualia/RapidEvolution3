package com.mixshare.rapid_evolution.webaccess.handlers;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.AbstractHandler;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.player.PlayerUserSessionManager;
import com.mixshare.rapid_evolution.workflow.TaskManager;
import com.mixshare.rapid_evolution.workflow.user.player.PreComputeNextSongTask;

public class AutoplayHandler extends AbstractHandler {

	static private Logger log = Logger.getLogger(AutoplayHandler.class);
	
	private Map<String, PlayerUserSessionManager> userSessionManagers;

	public AutoplayHandler(Map<String, PlayerUserSessionManager> userSessionManagers) {
		this.userSessionManagers = userSessionManagers;
	}
	
    public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException {
    	try {
	    	if (request.getPathInfo().startsWith("/forward")) {	    		    		
	    		((Request)request).setHandled(true);
	    		String remoteAddr = null;
	    		String remoteAddrParam = request.getParameter("remote_address");
	    		if ((remoteAddrParam != null) && (remoteAddrParam.length() > 0))
	    			remoteAddr = remoteAddrParam;
	    		else
	    			remoteAddr = request.getRemoteAddr(); 	    		    		
	    		PlayerUserSessionManager currentSessionManager = userSessionManagers.get(remoteAddr);
	    		if (currentSessionManager != null) {
	    			SongRecord song = currentSessionManager.getNextSongToPlay(false);
	    			if (song != null) {		    		    			
    					JSONArray jsonArray = new JSONArray();
						JSONObject jsonSong = song.getJSON(Database.getWebServerManager().getSongModelManager());
						if (jsonSong != null)
							jsonArray.put(jsonSong);	    							
    					response.getWriter().println(jsonArray.toString());
    					if (log.isDebugEnabled())
    						log.debug("handle(): returning song id=" + song.getUniqueId());
	    			} else {
	    				if (log.isDebugEnabled())
	    					log.debug("handle(): next song not returned");
	    			}
	    			TaskManager.runForegroundTask(new PreComputeNextSongTask(currentSessionManager));
	    		} else {
	    			if (log.isDebugEnabled())
	    				log.debug("handle(): no current session manager found for remote_address=" + remoteAddr);
	    		}
	    	} else if (request.getPathInfo().startsWith("/back")) {	    		    		
	    		((Request)request).setHandled(true);
	    		String remoteAddr = null;
	    		String remoteAddrParam = request.getParameter("remote_address");
	    		if ((remoteAddrParam != null) && (remoteAddrParam.length() > 0))
	    			remoteAddr = remoteAddrParam;
	    		else
	    			remoteAddr = request.getRemoteAddr(); 	    		    		
	    		PlayerUserSessionManager currentSessionManager = userSessionManagers.get(remoteAddr);
	    		if (currentSessionManager != null) {
	    			if (currentSessionManager.getCurrentSongIndex() != 0) {
		    			SongRecord song = currentSessionManager.getPreviousSongToPlay();
		    			if (song != null) {		    		    			
	    					JSONArray jsonArray = new JSONArray();
							JSONObject jsonSong = song.getJSON(Database.getWebServerManager().getSongModelManager());
							if (jsonSong != null)
								jsonArray.put(jsonSong);	    							
	    					response.getWriter().println(jsonArray.toString());
		    			}
		    			TaskManager.runForegroundTask(new PreComputeNextSongTask(currentSessionManager));
	    			}
	    		}
	    	}
    	} catch (Exception e) {
    		log.error("handle(): error getting next song", e);
    	}
    }
	
}
