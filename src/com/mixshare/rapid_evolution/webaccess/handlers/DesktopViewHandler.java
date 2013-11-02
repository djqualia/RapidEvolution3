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

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.ui.model.column.AllColumns;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.search.SearchModelManager;
import com.mixshare.rapid_evolution.util.StringUtil;

public class DesktopViewHandler extends AbstractHandler implements AllColumns {

	static private Logger log = Logger.getLogger(DesktopViewHandler.class);

    public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException {
    	try {
	    	if (request.getPathInfo().startsWith("/gridView")) {
	    		((Request)request).setHandled(true);
	    		String searchType = request.getParameter("type").trim();
	    		
	    		SearchModelManager modelManager = Database.getWebServerManager().getModelManager(searchType);	    		    		
	    		
	    		JSONObject layout = new JSONObject();	    		    		
	    		JSONArray rows = new JSONArray();
	    		for (int i = 0; i < modelManager.getNumColumns(); ++i) {
	    			Column column = modelManager.getViewColumnType(i);	 
	    			if (!column.isHidden()) {
		    			JSONObject obj = new JSONObject();
		    			obj.put("field", column.getColumnTitleId());
		    			obj.put("width", "" + column.getSize() + "px");
		    			obj.put("name", column.getColumnTitle());
		    			if (column.getColumnId() == COLUMN_THUMBNAIL_IMAGE.getColumnId())
		    				obj.put("formatter", "showImage");
		    			else if (column.getColumnId() == COLUMN_RATING_STARS.getColumnId())
		    				obj.put("formatter", "showRating");
		    			rows.put(obj);
	    			}
	    		}    	    		    			    		
	    		layout.put("rows", rows);
	    		JSONObject defaultCell = new JSONObject();
	    		defaultCell.put("width", 8);
	    		defaultCell.put("editable", false);
	    		defaultCell.put("type", "dojox.grid.cells._Widget");
	    		layout.put("defaultCell", defaultCell);
	    		JSONArray result = new JSONArray();
	    		result.put(layout);
	    		
	    		String output = result.toString();
	    		String prefix = "formatter\":";
	    		int index = output.indexOf(prefix);
	    		while (index >= 0) {
	    			int firstQuoteIndex = output.indexOf("\"", index + prefix.length());
	    			int secondQuoteIndex = output.indexOf("\"", firstQuoteIndex + 1);
	    			output = output.substring(0, index + prefix.length()) + output.substring(firstQuoteIndex + 1, secondQuoteIndex) + output.substring(secondQuoteIndex + 1);
	    			index = output.indexOf(prefix, index + 1);
	    		}
	    		output = StringUtil.replace(output, "\"dojox.grid.cells._Widget\"", "dojox.grid.cells._Widget");
	    		response.getWriter().println(output);
	    		
	    	} else if (request.getPathInfo().startsWith("/currentSearchType")) {
	    		((Request)request).setHandled(true);
	    		response.getWriter().println(Database.getWebServerManager().getCurrentSearchType());
	    	}
    	} catch (Exception e) {
    		log.error("handle(): error returning desktop views", e);
    	}
    }
    
}
