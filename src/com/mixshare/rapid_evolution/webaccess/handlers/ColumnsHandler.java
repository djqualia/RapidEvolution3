package com.mixshare.rapid_evolution.webaccess.handlers;

import java.io.IOException;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.AbstractHandler;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.search.SearchModelManager;
import com.mixshare.rapid_evolution.webaccess.WebServerManager;

public class ColumnsHandler extends AbstractHandler {

	static private Logger log = Logger.getLogger(ColumnsHandler.class);

    public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException {
    	try {
	    	if (request.getPathInfo().startsWith("/columns")) {
	    		((Request)request).setHandled(true);
	    		
	    		if (request.getMethod().equalsIgnoreCase("put")) {
	    			
		    		String searchType = request.getParameter("type").trim();
		    		SearchModelManager modelManager = Database.getWebServerManager().getModelManager(searchType);	    		    		
		    		
		    		String widths = request.getParameter("widths");
		    		if (widths != null) {
		    			log.info("widths=" + widths);
		    			StringTokenizer tokenizer = new StringTokenizer(widths, ",");
			    		Column column = null;
			    		for (int c = 0; c < modelManager.getNumColumns(); ++c) {
			    			column = modelManager.getViewColumnType(c);
			    			if (!column.isHidden()) {
			    				column.setSize(Short.parseShort(tokenizer.nextToken()));
			    			}
			    		}		    			
		    		} else {		    				    		
		    			if (request.getParameter("id") != null) {
			    			int id = Integer.parseInt(request.getParameter("id").trim());			    			
				    		Column column = null;
				    		for (int c = 0; c < modelManager.getNumColumns(); ++c) {
				    			column = modelManager.getViewColumnType(c);
				    			if (column.getColumnId() == id)
				    				break;
				    		}	
				    		String hidden = request.getParameter("hidden");
				    		if (hidden != null)
				    			column.setHidden(hidden.equalsIgnoreCase("true"));
	    				} else {
	    					int fromId = Integer.parseInt(request.getParameter("from"));
	    					int toId = Integer.parseInt(request.getParameter("to"));
	    					modelManager.columnMoved(null, fromId, toId);
	    				}
		    		}
	    			
	    		} else {
	    		
		    		String searchType = request.getParameter("type").trim();
		    			    		
		    		SearchModelManager modelManager = Database.getWebServerManager().getModelManager(searchType);	    		    		
		    		
		    		JSONObject resultObject = new JSONObject();	    		    		
		    		JSONArray arrayResult = new JSONArray();
		    		for (int i = 0; i < modelManager.getNumColumns(); ++i) {
		    			Column column = modelManager.getViewColumnType(i);	    		    			
		    			JSONObject obj = new JSONObject();
		    			obj.put("id", column.getColumnId());
		    			obj.put("column_title", column.getColumnTitle());
		    			obj.put("description", column.getColumnDescription());
		    			obj.put("type", searchType);
		    			obj.put("hidden", column.isHidden());
		    			arrayResult.put(obj);
		    		}    	    		    		
		    		resultObject.put("identifier", "id");
		    		resultObject.put("numRows", modelManager.getNumColumns());	    		    		
		    		resultObject.put("items", arrayResult);
		    		response.getWriter().println(resultObject.toString());
	    		}
	    	}
    	} catch (Exception e) {
    		log.error("handle(): error returning columns", e);
    	}
    }

}
