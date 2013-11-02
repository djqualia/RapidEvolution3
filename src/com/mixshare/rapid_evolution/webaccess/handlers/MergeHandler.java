package com.mixshare.rapid_evolution.webaccess.handlers;

import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.AbstractHandler;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.ui.model.search.SearchModelManager;
import com.mixshare.rapid_evolution.workflow.TaskManager;
import com.mixshare.rapid_evolution.workflow.user.MergeProfilesTask;

public class MergeHandler extends AbstractHandler {

	static private Logger log = Logger.getLogger(MergeHandler.class);

    public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException {
    	try {
	    	if (request.getPathInfo().startsWith("/merge")) {
	    		((Request)request).setHandled(true);
	    		
	    		if (request.getMethod().equalsIgnoreCase("put")) {
	    			
		    		String searchType = request.getParameter("type").trim();		    		
		    		SearchModelManager modelManager = Database.getWebServerManager().getModelManager(searchType);
		    		
		    		int primaryId = Integer.parseInt(request.getParameter("primary_id"));
		    		StringTokenizer tokenizer = new StringTokenizer(request.getParameter("merged_ids"), ",");
		    		Vector<Integer> mergedIds = new Vector<Integer>();
		    		while (tokenizer.hasMoreTokens())
		    			mergedIds.add(Integer.parseInt(tokenizer.nextToken()));
		    		
		    		Profile primaryProfile = modelManager.getIndex().getProfile(primaryId);
		    		if (primaryProfile != null) {
		    			for (int mergedId : mergedIds) {
		    				Profile duplicateProfile = modelManager.getIndex().getProfile(mergedId);
		    				if (duplicateProfile != null) {
		    					log.info("handle(): merging primary profile=" + primaryProfile + ", with duplicate profile=" + duplicateProfile);
		    					TaskManager.runForegroundTask(new MergeProfilesTask(primaryProfile, duplicateProfile));
		    				}
		    			}
		    		}
	    			
	    		} 
	    	}
    	} catch (Exception e) {
    		log.error("handle(): error returning columns", e);
    	}
    }

}
