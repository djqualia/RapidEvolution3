package com.mixshare.rapid_evolution.webaccess.handlers;

import java.io.IOException;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.AbstractHandler;

import com.ibm.icu.util.StringTokenizer;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.search.SongGroupRecord;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.player.PlayerUserSessionManager;
import com.mixshare.rapid_evolution.ui.model.search.SearchModelManager;
import com.mixshare.rapid_evolution.ui.model.search.song.SongModelManager;
import com.mixshare.rapid_evolution.workflow.TaskManager;
import com.mixshare.rapid_evolution.workflow.user.player.PreComputeNextSongTask;

public class SeedHandler extends AbstractHandler {

	static private Logger log = Logger.getLogger(SeedHandler.class);

	private final Map<String, PlayerUserSessionManager> userSessionManagers;

	public SeedHandler(Map<String, PlayerUserSessionManager> userSessionManagers) {
		this.userSessionManagers = userSessionManagers;
	}

    @Override
	public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException {
    	try {
	    	if (request.getPathInfo().startsWith("/seed")) {
	    		((Request)request).setHandled(true);
	    		String remoteAddr = null;
	    		String remoteAddrParam = request.getParameter("remote_address");
	    		if ((remoteAddrParam != null) && (remoteAddrParam.length() > 0))
	    			remoteAddr = remoteAddrParam;
	    		else
	    			remoteAddr = request.getRemoteAddr();
	    		String searchType = request.getParameter("type");
	    		if (searchType == null)
	    			searchType = "songs";
	    		SearchModelManager modelManager = Database.getWebServerManager().getModelManager(searchType);
	    		Vector<Integer> ids = new Vector<Integer>();
	    		String paramId = request.getParameter("id");
	    		if ((paramId != null) && (paramId.trim().length() > 0)) {
	    			if (modelManager instanceof SongModelManager)
	    				ids.add(Integer.parseInt(paramId));
	    			else {
	    				Record record = modelManager.getIndex().getRecord(Integer.parseInt(paramId));
	    				if (record instanceof SongGroupRecord) {
	    					for (SongRecord song : ((SongGroupRecord)record).getSongs())
	    						ids.add(song.getUniqueId());
	    				}
	    			}
	    		}
	    		else {
	    			paramId = request.getParameter("ids");
	    			if ((paramId != null) && (paramId.length() > 0)) {
	    				StringTokenizer tokenizer = new StringTokenizer(paramId, ",");
	    				while (tokenizer.hasMoreTokens()) {
	    					if (modelManager instanceof SongModelManager)
	    						ids.add(Integer.parseInt(tokenizer.nextToken().trim()));
	    					else {
	    						Record record = modelManager.getIndex().getRecord(Integer.parseInt(tokenizer.nextToken().trim()));
	    	    				if (record instanceof SongGroupRecord) {
	    	    					for (SongRecord song : ((SongGroupRecord)record).getSongs())
	    	    						ids.add(song.getUniqueId());
	    	    				}
	    					}
	    				}
	    			}
	    		}
	    		if (ids.size() > 0) {
		    		if (log.isDebugEnabled())
		    			log.debug("handle(): setting seed ids=" + ids);
		    		PlayerUserSessionManager currentSessionManager = userSessionManagers.get(remoteAddr);
	    			if (currentSessionManager == null) {
	    				currentSessionManager = new PlayerUserSessionManager(remoteAddr);
	    				userSessionManagers.put(remoteAddr, currentSessionManager);
	    			}

	    			boolean skipFirst = true;
	    			if (request.getParameter("keep_first") != null)
	    				skipFirst = false;

	    			int index = currentSessionManager.getIndexOf(ids.get(0));
	    			if ((index == -1) || !skipFirst) {
	    				currentSessionManager.init(ids);
	    				if (skipFirst) {
	    					if (log.isDebugEnabled())
	    						log.debug("handle(): skipping first song");
	    					currentSessionManager.getNextSongToPlay(false); // skips over the first song
	    				}
	    			} else {
	    				currentSessionManager.setCurrentIndex(index);
	    			}
	    			TaskManager.runForegroundTask(new PreComputeNextSongTask(currentSessionManager));
	    		} else {
	    			log.warn("handle(): no seed id found");
	    		}
	    	}
    	} catch (Exception e) {
    		log.error("handle(): error setting song seed", e);
    	}
    }

}
