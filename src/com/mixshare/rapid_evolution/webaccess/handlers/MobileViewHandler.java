package com.mixshare.rapid_evolution.webaccess.handlers;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.AbstractHandler;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.filter.playlist.PlaylistIdentifier;
import com.mixshare.rapid_evolution.data.record.HierarchicalRecord;
import com.mixshare.rapid_evolution.data.record.filter.playlist.DynamicPlaylistRecord;
import com.mixshare.rapid_evolution.data.record.filter.playlist.PlaylistRecord;

public class MobileViewHandler extends AbstractHandler {

	static private Logger log = Logger.getLogger(MobileViewHandler.class);

    public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException {
    	try {
	    	if (request.getPathInfo().startsWith("/views")) {	
	    		((Request)request).setHandled(true);	    		    		
	    		JSONArray results = new JSONArray();
	    		PlaylistRecord rootPlaylist = Database.getPlaylistIndex().getPlaylistRecord(new PlaylistIdentifier(RE3Properties.getProperty("web_access_playlist_name")));
	    		if (rootPlaylist != null) {
	    			for (HierarchicalRecord child : rootPlaylist.getChildRecords()) {	    		    				
	    				PlaylistRecord view = (PlaylistRecord)child;	    		    				
	    				if (view instanceof DynamicPlaylistRecord) {
	    					JSONObject result = new JSONObject();
	    					result.put("id", view.getUniqueId());
	    					result.put("name", view.getPlaylistName());
	    					results.put(result);
	    				} else {
	    					log.warn("handle(): playlist is not dynamic, view=" + view);
	    				}
	    			}
	    		}
	    		response.getWriter().println(results.toString());
	    	}
    	} catch (Exception e) {
    		log.error("handle(): error getting views", e);
    	}
    }

}
