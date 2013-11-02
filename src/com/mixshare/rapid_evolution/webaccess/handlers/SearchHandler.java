package com.mixshare.rapid_evolution.webaccess.handlers;

import java.io.IOException;
import java.util.Map;
import java.util.StringTokenizer;
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
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.filter.playlist.DynamicPlaylistRecord;
import com.mixshare.rapid_evolution.data.record.filter.playlist.PlaylistRecord;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.data.search.SearchResult;
import com.mixshare.rapid_evolution.data.search.parameters.CommonSearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.search.SearchSearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.search.artist.ArtistSearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.search.label.LabelSearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.search.release.ReleaseSearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.search.song.SongSearchParameters;
import com.mixshare.rapid_evolution.data.util.cache.LRUCache;
import com.mixshare.rapid_evolution.player.PlayerUserSessionManager;
import com.mixshare.rapid_evolution.ui.model.ModelManagerInterface;
import com.mixshare.rapid_evolution.ui.model.column.ColumnOrdering;
import com.mixshare.rapid_evolution.ui.model.filter.FilterSelection;
import com.mixshare.rapid_evolution.ui.model.search.SearchModelManager;
import com.mixshare.rapid_evolution.webaccess.WebServerManager;

public class SearchHandler extends AbstractHandler {

	static private Logger log = Logger.getLogger(SearchHandler.class);

	private final LRUCache cache;
	private final Map<String, PlayerUserSessionManager> userSessionManagers;

	public SearchHandler(LRUCache cache, Map<String, PlayerUserSessionManager> userSessionManagers) {
		this.cache = cache;
		this.userSessionManagers = userSessionManagers;
	}

    @Override
	public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException {
    	try {
	    	if (request.getPathInfo().startsWith("/songs") || request.getPathInfo().startsWith("/artists") || request.getPathInfo().startsWith("/labels") || request.getPathInfo().startsWith("/releases")) {
	    		((Request)request).setHandled(true);
	    		String query = request.getParameter("q");
	    		if (query == null)
	    			query = request.getParameter("query");
	    		if (query == null)
	    			query = "";
	    		int start = 0;
	    		try { start = Integer.parseInt(request.getParameter("start")); } catch (NumberFormatException e) { }
	    		int count = 10;
	    		try { count = Integer.parseInt(request.getParameter("count")); } catch (NumberFormatException e) { }
	    		String playlist = request.getParameter("playlist");
	    		String[] sort = new String[0];
	    		String sortString = request.getParameter("sort");
	    		if ((sortString != null) && (sortString.length() > 0)) {
	    			StringTokenizer tokenizer = new StringTokenizer(sortString, ",");
	    			sort = new String[tokenizer.countTokens()];
	    			int i = 0;
	    			while (tokenizer.hasMoreTokens())
	    				sort[i++] = tokenizer.nextToken();
	    		}

	    		SearchSearchParameters searchParams = null;
	    		boolean playlistResults = false;
	    		Integer releaseId = null;
	    		ModelManagerInterface modelManager = null;

	    		if (request.getPathInfo().startsWith("/songs")) {
	    			modelManager = Database.getWebServerManager().getSongModelManager();
		    		searchParams = new SongSearchParameters();
		    		if ((playlist != null) && (playlist.length() > 0) && !playlist.equals("null")) {
		    			try {
    		    			int playlistId = Integer.parseInt(playlist.substring("playlist_".length()));
    		    			PlaylistRecord playlistRecord = Database.getPlaylistIndex().getPlaylistRecord(playlistId);
    		    			if (playlistRecord instanceof DynamicPlaylistRecord) {
    		    				DynamicPlaylistRecord view = (DynamicPlaylistRecord)playlistRecord;
    		    				if (view.getFirstSongSearchParameters() != null)
    		    					searchParams = new SongSearchParameters(view.getFirstSongSearchParameters());
    		    				if (view.containsEntries())
    		    					searchParams.setPlaylistsSelection(new FilterSelection(view));
    		    			}
		    			} catch (Exception e) {
		    				log.error("handle(): error", e);
		    			}
		    		}
		    		try {
		    			int artistId = Integer.parseInt(request.getParameter("artist_id"));
		    			((SongSearchParameters)searchParams).setArtistId(artistId);
		    			playlistResults = true;
		    		} catch (NumberFormatException e) { }
		    		try {
		    			int labelId = Integer.parseInt(request.getParameter("label_id"));
		    			((SongSearchParameters)searchParams).setLabelId(labelId);
		    			playlistResults = true;
		    		} catch (NumberFormatException e) { }
		    		try {
		    			releaseId = Integer.parseInt(request.getParameter("release_id"));
		    			((SongSearchParameters)searchParams).setReleaseId(releaseId);
		    			((SongSearchParameters)searchParams).setSortType(new byte[] { CommonSearchParameters.SORT_BY_TRACK, CommonSearchParameters.SORT_BY_NAME });
		    			playlistResults = true;
		    		} catch (NumberFormatException e) { }

	    		} else if (request.getPathInfo().startsWith("/artists")) {
	    			modelManager = Database.getWebServerManager().getArtistModelManager();
		    		searchParams = new ArtistSearchParameters();
		    		if ((playlist != null) && (playlist.length() > 0) && !playlist.equals("null")) {
		    			try {
    		    			int playlistId = Integer.parseInt(playlist.substring("playlist_".length()));
    		    			PlaylistRecord playlistRecord = Database.getPlaylistIndex().getPlaylistRecord(playlistId);
    		    			if (playlistRecord instanceof DynamicPlaylistRecord) {
    		    				DynamicPlaylistRecord view = (DynamicPlaylistRecord)playlistRecord;
    		    				if (view.getFirstArtistSearchParameters() != null)
    		    					searchParams = new ArtistSearchParameters(view.getFirstArtistSearchParameters());
    		    				if (view.containsEntries())
    		    					searchParams.setPlaylistsSelection(new FilterSelection(view));
    		    			}
		    			} catch (Exception e) {
		    				log.error("handle(): error", e);
		    			}
		    		}
	    		} else if (request.getPathInfo().startsWith("/labels")) {
	    			modelManager = Database.getWebServerManager().getLabelModelManager();
		    		searchParams = new LabelSearchParameters();
		    		if ((playlist != null) && (playlist.length() > 0) && !playlist.equals("null")) {
		    			try {
    		    			int playlistId = Integer.parseInt(playlist.substring("playlist_".length()));
    		    			PlaylistRecord playlistRecord = Database.getPlaylistIndex().getPlaylistRecord(playlistId);
    		    			if (playlistRecord instanceof DynamicPlaylistRecord) {
    		    				DynamicPlaylistRecord view = (DynamicPlaylistRecord)playlistRecord;
    		    				if (view.getFirstLabelSearchParameters() != null)
    		    					searchParams = new LabelSearchParameters(view.getFirstLabelSearchParameters());
    		    				if (view.containsEntries())
    		    					searchParams.setPlaylistsSelection(new FilterSelection(view));
    		    			}
		    			} catch (Exception e) {
		    				log.error("handle(): error", e);
		    			}
		    		}
	    		} else if (request.getPathInfo().startsWith("/releases")) {
	    			modelManager = Database.getWebServerManager().getReleaseModelManager();
		    		searchParams = new ReleaseSearchParameters();
		    		if ((playlist != null) && (playlist.length() > 0) && !playlist.equals("null")) {
		    			try {
    		    			int playlistId = Integer.parseInt(playlist.substring("playlist_".length()));
    		    			PlaylistRecord playlistRecord = Database.getPlaylistIndex().getPlaylistRecord(playlistId);
    		    			if (playlistRecord instanceof DynamicPlaylistRecord) {
    		    				DynamicPlaylistRecord view = (DynamicPlaylistRecord)playlistRecord;
    		    				if (view.getFirstReleaseSearchParameters() != null)
    		    					searchParams = new ReleaseSearchParameters(view.getFirstReleaseSearchParameters());
    		    				if (view.containsEntries())
    		    					searchParams.setPlaylistsSelection(new FilterSelection(view));
    		    			}
		    			} catch (Exception e) {
		    				log.error("handle(): error", e);
		    			}
		    		}
	    		}
	    		if (modelManager instanceof SearchModelManager)
	    			((SearchModelManager)modelManager).setSearchParams(searchParams);

	    		if ((query != null) && !query.equals("") && !query.equals("null") && !query.equals("undefined"))
	    			searchParams.setSearchText(query);
	    		searchParams.setInternalItemsOnly(true);
	    		if (sort.length > 0) {
	    			byte[] sortParams = new byte[sort.length];
	    			boolean[] sortDescending = new boolean[sort.length];
	    			int i = 0;
	    			for (String sortParam : sort) {
	    				if (sortParam.startsWith("-")) {
	    					sortDescending[i] = true;
	    					sortParam = sortParam.substring(1);
	    				}
	    				sortParams[i] = CommonSearchParameters.getSortTypeFromDescription(sortParam, modelManager);
	    				++i;
	    			}
	    			searchParams.setSortType(sortParams);
	    			searchParams.setSortDescending(sortDescending);

	    			Vector<ColumnOrdering> ordering = new Vector<ColumnOrdering>(sortParams.length);
	    			for (int s = 0; s < sortParams.length; ++s)
	    				ordering.add(new ColumnOrdering(CommonSearchParameters.getColumnIdFromSortType(sortParams[s], searchParams.getDataType()), !sortDescending[s]));
	    			modelManager.setSortOrdering(ordering);
	    		} else {
	    			if (playlist == null) {
		    			Vector<ColumnOrdering> ordering = modelManager.getSortOrdering();
		    			byte[] sortParams = new byte[ordering.size()];
		    			boolean[] sortDescending = new boolean[ordering.size()];
		    			for (int s = 0; s < ordering.size(); ++s) {
		    				sortParams[s] = CommonSearchParameters.getSortTypeFromColumnId(ordering.get(s).getColumnId());
		    				sortDescending[s] = !ordering.get(s).isAscending();
		    			}
		    			searchParams.setSortType(sortParams);
		    			searchParams.setSortDescending(sortDescending);
	    			}
	    		}

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
	    			if (log.isDebugEnabled())
	    				log.debug("handle(): search query=" + query + ", hash=" + searchKey);
	    			if (request.getPathInfo().startsWith("/songs"))
	    				searchResults = Database.getSongIndex().searchRecords(searchParams);
	    			else if (request.getPathInfo().startsWith("/artists"))
	    				searchResults = Database.getArtistIndex().searchRecords(searchParams);
	    			else if (request.getPathInfo().startsWith("/releases"))
	    				searchResults = Database.getReleaseIndex().searchRecords(searchParams);
	    			else if (request.getPathInfo().startsWith("/labels"))
	    				searchResults = Database.getLabelIndex().searchRecords(searchParams);
	    			cache.add(searchKey, searchResults);
	    		} else {
	    			if (log.isDebugEnabled())
	    				log.debug("handle(): cached search query used, hash=" + searchKey);
	    		}

	    		JSONObject resultObject = new JSONObject();
	    		JSONArray arrayResult = new JSONArray();
	    		for (int i = start; i < Math.min(searchResults.size(), start + count); ++i) {
	    			SearchResult result = searchResults.get(i);
	    			Record record = result.getRecord();
	    			JSONObject jsonResult = null;
    				jsonResult = ((SearchRecord)record).getJSON(modelManager);
    				jsonResult.put("row", i);
	    			if (jsonResult != null)
	    				arrayResult.put(jsonResult);
	    		}
	    		resultObject.put("identifier", "id");
	    		resultObject.put("numRows", searchResults.size());
	    		resultObject.put("items", arrayResult);
	    		if (start + count < searchResults.size()) {
	    			resultObject.put("hasMore", "true");
	    		} else {
	    			resultObject.put("hasMore", "false");
	    		}
	    		response.getWriter().println(resultObject.toString());

	    		if (playlistResults && (searchResults != null) && (start == 0)) {
	    			String remoteAddr = request.getRemoteAddr();
		    		PlayerUserSessionManager currentSessionManager = userSessionManagers.get(remoteAddr);
	    			if (currentSessionManager == null) {
	    				currentSessionManager = new PlayerUserSessionManager(remoteAddr);
	    				userSessionManagers.put(remoteAddr, currentSessionManager);
	    			}
	    			Vector<Integer> songIds = new Vector<Integer>();
	    			for (SearchResult result : searchResults)
	    				songIds.add(result.getRecord().getUniqueId());
	    			currentSessionManager.init(songIds);
	    			currentSessionManager.getNextSongToPlay(false);
	    		}
	    	}
    	} catch (Exception e) {
    		log.error("handle(): error performing search", e);
    	}
    }

}
