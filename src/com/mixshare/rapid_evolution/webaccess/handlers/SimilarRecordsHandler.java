package com.mixshare.rapid_evolution.webaccess.handlers;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.AbstractHandler;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.search.song.SongIdentifier;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.data.search.SearchResult;
import com.mixshare.rapid_evolution.data.search.parameters.search.SearchSearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.search.song.SongSearchParameters;
import com.mixshare.rapid_evolution.data.util.cache.LRUCache;
import com.mixshare.rapid_evolution.webaccess.WebServerManager;

// right now only handles songs, and is used to create the internal admin metrolyrics page which shows similar videos.
public class SimilarRecordsHandler extends AbstractHandler {

    static private Logger log = Logger.getLogger(SimilarRecordsHandler.class);

    private final LRUCache cache;

    public SimilarRecordsHandler(LRUCache cache) {
    	this.cache = cache;
    }

    @Override
	public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException {
    	try {
	    	if (request.getPathInfo().startsWith("/similar_songs")) {
	    		((Request)request).setHandled(true);

	    		String artist = request.getParameter("artist");
	    		String title = request.getParameter("title");
	    		String filename = request.getParameter("filename");

	    		float universalWeight = 0.0f;
	    		try { universalWeight = Float.parseFloat(request.getParameter("universal_weight")); } catch (Exception e) { }
	    		float sonyWeight = 0.0f;
	    		try { sonyWeight = Float.parseFloat(request.getParameter("sony_weight")); } catch (Exception e) { }
	    		boolean publishedOnly = true;
	    		try { publishedOnly = request.getParameter("published_only").equalsIgnoreCase("true"); } catch (Exception e) { }
	    		boolean excludeYoutube = true;
	    		try { excludeYoutube = request.getParameter("exclude_youtube").equalsIgnoreCase("true"); } catch (Exception e) { }

	    		int start = 0;
	    		try { start = Integer.parseInt(request.getParameter("start")); } catch (NumberFormatException e) { }
	    		int count = 400;
	    		try { count = Integer.parseInt(request.getParameter("count")); } catch (NumberFormatException e) { }

	    		if (log.isDebugEnabled())
	    			log.debug("handle(): artist=" + artist + ", title=" + title + ", filename=" + filename + ", universalWeight=" + universalWeight + ", sonyWeight=" + sonyWeight + ", excludeYoutube=" + excludeYoutube);

	    		SongProfile song = null;
	    		if (song == null) {
	    			SongIdentifier songId = new SongIdentifier(artist, title);
	    			song = Database.getSongIndex().getSongProfile(songId);
	    		}

	    		if (song != null) {
	    			SearchSearchParameters searchParams = new SongSearchParameters();
	    			searchParams.initRelativeProfile(song);
	    			searchParams.setInternalItemsOnly(true);

		    		String searchKey = searchParams.getUniqueHash();
		    		Long lastFetched = cache.getTimestamp(searchKey);
		    		Vector<SearchResult> searchResults = (Vector<SearchResult>)cache.get(searchKey);
		    		if ((searchResults != null) && (lastFetched != null)) {
		    			if (System.currentTimeMillis() - lastFetched  > WebServerManager.CACHE_EXPIRE_TIME_MILLIS) {
		    				if (log.isDebugEnabled())
		    					log.debug("handle(): cached result has expired");
		    				searchResults = null;
		    			}
		    		}
		    		if (searchResults == null) {
	    				searchResults = Database.getSongIndex().searchRecords(searchParams);
		    			cache.add(searchKey, searchResults);
		    		} else {
		    			if (log.isDebugEnabled())
		    				log.debug("handle(): cached search query used, hash=" + searchKey);
		    		}

		    		JSONObject finalResult = new JSONObject();
		    		JSONArray arrayResult = new JSONArray();
		    		for (int i = start; i < Math.min(searchResults.size(), start + count); ++i) {
		    			SearchResult result = searchResults.get(i);
		    			Record record = result.getRecord();
		    			JSONArray jsonResult = getJSONSongTableItem((SongRecord)record);
		    			if (jsonResult != null)
		    				arrayResult.put(jsonResult);
		    		}
		    		finalResult.put("aaData", arrayResult);

		    		response.getWriter().println(finalResult.toString());
	    		} else {
	    			log.warn("handle(): song not found, artist=" + artist + ", title=" + title + ", filename=" + filename);
	    		}
	    	}
    	} catch (Exception e) {
    		log.error("handle(): error performing search", e);
    	}
    }

    static private JSONArray getJSONSongTableItem(SongRecord song) {
    	try {
    		JSONArray array = new JSONArray();
	        String encodedImageFilename = URLEncoder.encode(song.getThumbnailImageFilename(), "UTF-8");
    		String imageUrl = "<img src=\"image?filename=" + encodedImageFilename + "&height=90&width=120\" />";
    		array.put(imageUrl);
    		array.put(song.getArtistsDescription());
    		array.put(song.getSongDescription());
    		array.put(""); // deprecated field
    		String label = song.getLabelsDescription();
    		if (label.length() > 20)
    			label = label.substring(0, 20);
    		array.put(label);
    		array.put(song.getOriginalYearReleasedAsString());

    		Boolean isUniversal = (Boolean)song.getUserData(Database.getSongIndex().getUserDataType("Universal"));
    		if ((isUniversal != null) && isUniversal)
        		array.put("Yes");
    		else
    			array.put("No");

    		Boolean isSony = (Boolean)song.getUserData(Database.getSongIndex().getUserDataType("BC Media"));
    		if ((isSony != null) && isSony)
        		array.put("Yes");
    		else
    			array.put("No");

    		Boolean isYoutube = (Boolean)song.getUserData(Database.getSongIndex().getUserDataType("Youtube Content"));
    		if ((isYoutube != null) && isYoutube)
        		array.put("Yes");
    		else
    			array.put("No");

    		return array;
    	} catch (Exception e) {
    		log.error("getJSONSongTableItem(): error", e);
    	}
    	return null;
    }


}
