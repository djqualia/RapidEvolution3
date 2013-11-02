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
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.ui.model.search.SearchModelManager;

public class HideHandler extends AbstractHandler {

	static private Logger log = Logger.getLogger(HideHandler.class);

    public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException {
    	try {
	    	if (request.getPathInfo().startsWith("/hide")) {
	    		((Request)request).setHandled(true);
	    		
	    		if (request.getMethod().equalsIgnoreCase("put")) {
	    			
		    		String searchType = request.getParameter("type").trim();		    		
		    		SearchModelManager modelManager = Database.getWebServerManager().getModelManager(searchType);
		    		
		    		StringTokenizer tokenizer = new StringTokenizer(request.getParameter("ids"), ",");
		    		Vector<Integer> hiddenIds = new Vector<Integer>();
		    		while (tokenizer.hasMoreTokens())
		    			hiddenIds.add(Integer.parseInt(tokenizer.nextToken()));
		    		
	    			for (int hiddenId : hiddenIds) {
	    				Record hiddenRecord = modelManager.getIndex().getRecord(hiddenId);	    				
	    				if (hiddenRecord != null) {
	    					log.info("handle(): setting disabled=true for=" + hiddenRecord);
	    					hiddenRecord.setDisabled(true);
	    					hiddenRecord.update();
	    				}
	    			}
	    			
	    		} 
	    	}
    	} catch (Exception e) {
    		log.error("handle(): error returning columns", e);
    	}
    }

}
