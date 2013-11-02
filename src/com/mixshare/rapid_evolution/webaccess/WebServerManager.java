package com.mixshare.rapid_evolution.webaccess;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.HandlerList;
import org.mortbay.jetty.security.Constraint;
import org.mortbay.jetty.security.ConstraintMapping;
import org.mortbay.jetty.security.HashUserRealm;
import org.mortbay.jetty.security.SecurityHandler;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.audio.AudioFileTypes;
import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.filter.playlist.PlaylistIdentifier;
import com.mixshare.rapid_evolution.data.profile.filter.playlist.PlaylistProfile;
import com.mixshare.rapid_evolution.data.record.filter.playlist.PlaylistRecord;
import com.mixshare.rapid_evolution.data.search.parameters.CommonSearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.search.artist.ArtistSearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.search.label.LabelSearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.search.release.ReleaseSearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.search.song.SongSearchParameters;
import com.mixshare.rapid_evolution.data.submitted.filter.playlist.SubmittedCategoryPlaylist;
import com.mixshare.rapid_evolution.data.submitted.filter.playlist.SubmittedDynamicPlaylist;
import com.mixshare.rapid_evolution.data.util.cache.LRUCache;
import com.mixshare.rapid_evolution.player.PlayerUserSessionManager;
import com.mixshare.rapid_evolution.ui.model.filter.playlist.PlaylistHierarchyInstance;
import com.mixshare.rapid_evolution.ui.model.search.SearchModelManager;
import com.mixshare.rapid_evolution.ui.model.search.artist.ArtistModelManager;
import com.mixshare.rapid_evolution.ui.model.search.label.LabelModelManager;
import com.mixshare.rapid_evolution.ui.model.search.release.ReleaseModelManager;
import com.mixshare.rapid_evolution.ui.model.search.song.SongModelManager;
import com.mixshare.rapid_evolution.ui.model.tree.TreeHierarchyInstance;
import com.mixshare.rapid_evolution.util.OSHelper;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.mixshare.rapid_evolution.util.io.writers.PlainTextLineWriter;
import com.mixshare.rapid_evolution.webaccess.handlers.AutocompleteHandler;
import com.mixshare.rapid_evolution.webaccess.handlers.AutoplayHandler;
import com.mixshare.rapid_evolution.webaccess.handlers.ColumnsHandler;
import com.mixshare.rapid_evolution.webaccess.handlers.DesktopResourceHandler;
import com.mixshare.rapid_evolution.webaccess.handlers.DesktopViewHandler;
import com.mixshare.rapid_evolution.webaccess.handlers.HideHandler;
import com.mixshare.rapid_evolution.webaccess.handlers.ImageHandler;
import com.mixshare.rapid_evolution.webaccess.handlers.MediaHandler;
import com.mixshare.rapid_evolution.webaccess.handlers.MergeHandler;
import com.mixshare.rapid_evolution.webaccess.handlers.MobileResourceHandler;
import com.mixshare.rapid_evolution.webaccess.handlers.MobileViewHandler;
import com.mixshare.rapid_evolution.webaccess.handlers.RatingHandler;
import com.mixshare.rapid_evolution.webaccess.handlers.SearchHandler;
import com.mixshare.rapid_evolution.webaccess.handlers.SeedHandler;
import com.mixshare.rapid_evolution.webaccess.handlers.SimilarRecordsHandler;

public class WebServerManager implements AudioFileTypes, DataConstants {

	static private Logger log = Logger.getLogger(WebServerManager.class);

	static private LRUCache cache = new LRUCache(RE3Properties.getInt("web_access_search_cache_size"));
	static public long CACHE_EXPIRE_TIME_MILLIS = 1000 * 60; // 1 minute

	static private Server server;
	static private Map<String, PlayerUserSessionManager> userSessionManagers = new HashMap<String, PlayerUserSessionManager>();

	////////////////////
	// STATIC METHODS //
	////////////////////

    static public void startWebAccess() {
    	if (server != null)
    		return;
    	if (!RE3Properties.getBoolean("web_access_enabled"))
    		return;
    	try {
        	Database.getWebServerManager().init();

        	Handler searchHandler = new SearchHandler(cache, userSessionManagers);
        	Handler columnsHandler = new ColumnsHandler();
        	Handler mergeHandler = new MergeHandler();
        	Handler hideHandler = new HideHandler();
        	Handler desktopViewHandler = new DesktopViewHandler();
    		Handler similarSongsHandler = new SimilarRecordsHandler(cache);
    		Handler mediaHandler = new MediaHandler();
    		Handler imageHandler = new ImageHandler();
    		Handler forwardHandler = new AutoplayHandler(userSessionManagers);
    		Handler seedHandler = new SeedHandler(userSessionManagers);
    		Handler ratingHandler = new RatingHandler();
    		Handler viewHandler = new MobileViewHandler();
    		Handler autoCompleteHandler = new AutocompleteHandler();
    		Handler myMobileResourceHandler = new MobileResourceHandler();
    		Handler myDesktopResourceHandler = new DesktopResourceHandler();

    		Constraint constraint = new Constraint();
    		constraint.setName(Constraint.__BASIC_AUTH);;
    		constraint.setRoles(new String[]{"user"});
    		constraint.setAuthenticate(true);

    		ConstraintMapping cm = new ConstraintMapping();
    		cm.setConstraint(constraint);
    		cm.setPathSpec("/*");

    		String propertiesFilename = OSHelper.getWorkingDirectory() + "/web_roles.properties";
    		LineWriter writer = new PlainTextLineWriter(propertiesFilename);
    		writer.writeLine(RE3Properties.getProperty("web_access_username") + ": " + RE3Properties.getProperty("web_access_password") + " ,user");
    		writer.close();

    		SecurityHandler sh = new SecurityHandler();
    		sh.setUserRealm(new HashUserRealm("RE3WebAccess", propertiesFilename));
    		sh.setConstraintMappings(new ConstraintMapping[]{cm});

            HandlerList handlers = new HandlerList();
            if (RE3Properties.getBoolean("enable_web_access_security"))
            	handlers.addHandler(sh);
    		handlers.addHandler(searchHandler);
    		handlers.addHandler(columnsHandler);
    		handlers.addHandler(mergeHandler);
    		handlers.addHandler(hideHandler);
    		handlers.addHandler(desktopViewHandler);
    		handlers.addHandler(similarSongsHandler);
    		handlers.addHandler(mediaHandler);
    		handlers.addHandler(imageHandler);
    		handlers.addHandler(forwardHandler);
    		handlers.addHandler(ratingHandler);
    		handlers.addHandler(viewHandler);
    		handlers.addHandler(seedHandler);
    		handlers.addHandler(autoCompleteHandler);
    		// these need to be the last 2 resource handlers
    		handlers.addHandler(myMobileResourceHandler);
    		handlers.addHandler(myDesktopResourceHandler);

    		server = new Server(RE3Properties.getInt("web_access_port"));
    		server.setHandler(handlers);
    		server.setStopAtShutdown(true);
    		server.start();

    		PlaylistIdentifier webAccessPlaylistId = new PlaylistIdentifier(RE3Properties.getProperty("web_access_playlist_name"));
    		PlaylistRecord existingWebAccessCategory = Database.getPlaylistIndex().getPlaylistRecord(webAccessPlaylistId);
    		if (existingWebAccessCategory == null) {
    			SubmittedCategoryPlaylist webAccessCategory = new SubmittedCategoryPlaylist(RE3Properties.getProperty("web_access_playlist_name"));
    			PlaylistProfile webAccessProfile = (PlaylistProfile)Database.getPlaylistIndex().addOrUpdate(webAccessCategory);
    			if (webAccessProfile != null) {

    				Vector<TreeHierarchyInstance> parentInstances = new Vector<TreeHierarchyInstance>();
    				parentInstances.add(new PlaylistHierarchyInstance((PlaylistRecord)webAccessProfile.getRecord(), null));

    				// new
    				SongSearchParameters songSearchParams = new SongSearchParameters();
    				songSearchParams.setSortType(new byte[] { CommonSearchParameters.SORT_BY_DATE_ADDED, CommonSearchParameters.SORT_BY_YEAR, CommonSearchParameters.SORT_BY_RESULT_SCORE });
    				songSearchParams.setInternalItemsOnly(true);
    				ArtistSearchParameters artistSearchParams = new ArtistSearchParameters();
    				artistSearchParams.setSortType(new byte[] { CommonSearchParameters.SORT_BY_DATE_ADDED, CommonSearchParameters.SORT_BY_YEAR, CommonSearchParameters.SORT_BY_RESULT_SCORE });
    				artistSearchParams.setInternalItemsOnly(true);
    				LabelSearchParameters labelSearchParams = new LabelSearchParameters();
    				labelSearchParams.setSortType(new byte[] { CommonSearchParameters.SORT_BY_DATE_ADDED, CommonSearchParameters.SORT_BY_YEAR, CommonSearchParameters.SORT_BY_RESULT_SCORE });
    				labelSearchParams.setInternalItemsOnly(true);
    				ReleaseSearchParameters releaseSearchParams = new ReleaseSearchParameters();
    				releaseSearchParams.setSortType(new byte[] { CommonSearchParameters.SORT_BY_DATE_ADDED, CommonSearchParameters.SORT_BY_YEAR, CommonSearchParameters.SORT_BY_RESULT_SCORE });
    				releaseSearchParams.setInternalItemsOnly(true);
    				SubmittedDynamicPlaylist newPlaylist = new SubmittedDynamicPlaylist("New");
    				newPlaylist.setSongSearchParameters(songSearchParams);
    				newPlaylist.setArtistSearchParameters(artistSearchParams);
    				newPlaylist.setLabelSearchParameters(labelSearchParams);
    				newPlaylist.setReleaseSearchParameters(releaseSearchParams);
    				newPlaylist.setParentInstances(parentInstances);
    				Database.getPlaylistIndex().addOrUpdate(newPlaylist);

    				// rated
    				songSearchParams = new SongSearchParameters();
    				songSearchParams.setSortType(new byte[] { CommonSearchParameters.SORT_BY_RATING, CommonSearchParameters.SORT_BY_RESULT_SCORE, CommonSearchParameters.SORT_BY_DATE_ADDED, CommonSearchParameters.SORT_BY_SCORE });
    				songSearchParams.setInternalItemsOnly(true);
    				artistSearchParams = new ArtistSearchParameters();
    				artistSearchParams.setSortType(new byte[] { CommonSearchParameters.SORT_BY_RATING, CommonSearchParameters.SORT_BY_RESULT_SCORE, CommonSearchParameters.SORT_BY_DATE_ADDED, CommonSearchParameters.SORT_BY_SCORE });
    				artistSearchParams.setInternalItemsOnly(true);
    				releaseSearchParams = new ReleaseSearchParameters();
    				releaseSearchParams.setSortType(new byte[] { CommonSearchParameters.SORT_BY_RATING, CommonSearchParameters.SORT_BY_RESULT_SCORE, CommonSearchParameters.SORT_BY_DATE_ADDED, CommonSearchParameters.SORT_BY_SCORE });
    				releaseSearchParams.setInternalItemsOnly(true);
    				labelSearchParams = new LabelSearchParameters();
    				labelSearchParams.setSortType(new byte[] { CommonSearchParameters.SORT_BY_RATING, CommonSearchParameters.SORT_BY_RESULT_SCORE, CommonSearchParameters.SORT_BY_DATE_ADDED, CommonSearchParameters.SORT_BY_SCORE });
    				labelSearchParams.setInternalItemsOnly(true);
    				newPlaylist = new SubmittedDynamicPlaylist("Favorites");
    				newPlaylist.setSongSearchParameters(songSearchParams);
    				newPlaylist.setArtistSearchParameters(artistSearchParams);
    				newPlaylist.setLabelSearchParameters(labelSearchParams);
    				newPlaylist.setReleaseSearchParameters(releaseSearchParams);
    				newPlaylist.setParentInstances(parentInstances);
    				Database.getPlaylistIndex().addOrUpdate(newPlaylist);

    				// fresh
    				songSearchParams = new SongSearchParameters();
    				songSearchParams.setSortType(new byte[] { CommonSearchParameters.SORT_BY_USER_PREFERENCE, CommonSearchParameters.SORT_BY_RESULT_SCORE, CommonSearchParameters.SORT_BY_DATE_ADDED, CommonSearchParameters.SORT_BY_NUM_PLAYS });
    				songSearchParams.setSortDescending(new boolean[] { false, false, false, true });
    				songSearchParams.setExcludeRated(true);
    				songSearchParams.setInternalItemsOnly(true);
    				releaseSearchParams = new ReleaseSearchParameters();
    				releaseSearchParams.setSortType(new byte[] { CommonSearchParameters.SORT_BY_USER_PREFERENCE, CommonSearchParameters.SORT_BY_RESULT_SCORE, CommonSearchParameters.SORT_BY_DATE_ADDED, CommonSearchParameters.SORT_BY_NUM_PLAYS });
    				releaseSearchParams.setSortDescending(new boolean[] { false, false, false, true });
    				releaseSearchParams.setExcludeRated(true);
    				releaseSearchParams.setInternalItemsOnly(true);
    				labelSearchParams = new LabelSearchParameters();
    				labelSearchParams.setSortType(new byte[] { CommonSearchParameters.SORT_BY_USER_PREFERENCE, CommonSearchParameters.SORT_BY_RESULT_SCORE, CommonSearchParameters.SORT_BY_DATE_ADDED, CommonSearchParameters.SORT_BY_NUM_PLAYS });
    				labelSearchParams.setSortDescending(new boolean[] { false, false, false, true });
    				labelSearchParams.setExcludeRated(true);
    				labelSearchParams.setInternalItemsOnly(true);
    				artistSearchParams = new ArtistSearchParameters();
    				artistSearchParams.setSortType(new byte[] { CommonSearchParameters.SORT_BY_USER_PREFERENCE, CommonSearchParameters.SORT_BY_RESULT_SCORE, CommonSearchParameters.SORT_BY_DATE_ADDED, CommonSearchParameters.SORT_BY_NUM_PLAYS });
    				artistSearchParams.setSortDescending(new boolean[] { false, false, false, true });
    				artistSearchParams.setExcludeRated(true);
    				artistSearchParams.setInternalItemsOnly(true);
    				newPlaylist = new SubmittedDynamicPlaylist("Unrated");
    				newPlaylist.setSongSearchParameters(songSearchParams);
    				newPlaylist.setArtistSearchParameters(artistSearchParams);
    				newPlaylist.setLabelSearchParameters(labelSearchParams);
    				newPlaylist.setReleaseSearchParameters(releaseSearchParams);
    				newPlaylist.setParentInstances(parentInstances);
    				Database.getPlaylistIndex().addOrUpdate(newPlaylist);

    				// all
    				songSearchParams = new SongSearchParameters();
    				songSearchParams.setSortType(new byte[] { CommonSearchParameters.SORT_BY_RESULT_SCORE, CommonSearchParameters.SORT_BY_NAME });
    				songSearchParams.setInternalItemsOnly(true);
    				releaseSearchParams = new ReleaseSearchParameters();
    				releaseSearchParams.setSortType(new byte[] { CommonSearchParameters.SORT_BY_RESULT_SCORE, CommonSearchParameters.SORT_BY_NAME });
    				releaseSearchParams.setInternalItemsOnly(true);
    				labelSearchParams = new LabelSearchParameters();
    				labelSearchParams.setSortType(new byte[] { CommonSearchParameters.SORT_BY_RESULT_SCORE, CommonSearchParameters.SORT_BY_NAME });
    				labelSearchParams.setInternalItemsOnly(true);
    				artistSearchParams = new ArtistSearchParameters();
    				artistSearchParams.setSortType(new byte[] { CommonSearchParameters.SORT_BY_RESULT_SCORE, CommonSearchParameters.SORT_BY_NAME });
    				artistSearchParams.setInternalItemsOnly(true);
    				newPlaylist = new SubmittedDynamicPlaylist("All");
    				newPlaylist.setSongSearchParameters(songSearchParams);
    				newPlaylist.setArtistSearchParameters(artistSearchParams);
    				newPlaylist.setLabelSearchParameters(labelSearchParams);
    				newPlaylist.setReleaseSearchParameters(releaseSearchParams);
    				newPlaylist.setParentInstances(parentInstances);
    				Database.getPlaylistIndex().addOrUpdate(newPlaylist);

    			}
    		} else {
    			Database.getPlaylistIndex().addRelationship(Database.getPlaylistIndex().getRootRecord(), existingWebAccessCategory);
    		}

    	} catch (Exception e) {
    		log.error("startServer(): error", e);
    	}
    }

    static public void stopWebAccess() {
    	if (server != null) {
    		try {
    			server.stop();
    		} catch (Exception e) {
    			log.error("stopWebAccess(): error", e);
    		}
    		server = null;
    	}
    }

    static public void main(String[] args) {
    	try {
	    	RapidEvolution3.loadLog4J();
	    	startWebAccess();
	    	while (true)
	    		Thread.sleep(5000);

    	} catch (Exception e) {
    		log.error("main(): error", e);
    	}
    }

	////////////
	// FIELDS //
	////////////

	private ArtistModelManager artistModelManager = new ArtistModelManager();
	private LabelModelManager labelModelManager = new LabelModelManager();
	private ReleaseModelManager releaseModelManager = new ReleaseModelManager();
	private SongModelManager songModelManager = new SongModelManager();
	private String currentSearchType = "songs";

	/////////////////
	// CONSTRUCTOR //
	/////////////////

	public WebServerManager() {
		artistModelManager.initColumns();
		labelModelManager.initColumns();
		releaseModelManager.initColumns();
		songModelManager.initColumns();
	}

	public WebServerManager(LineReader lineReader) {
    	int version = Integer.parseInt(lineReader.getNextLine());
		artistModelManager = new ArtistModelManager(lineReader);
		labelModelManager = new LabelModelManager(lineReader);
		releaseModelManager = new ReleaseModelManager(lineReader);
		songModelManager = new SongModelManager(lineReader);
		if (version >= 2) {
			currentSearchType = lineReader.getNextLine();
		}
	}

	/////////////
	// METHODS //
	/////////////

	public ArtistModelManager getArtistModelManager() {
		return artistModelManager;
	}

	public void setArtistModelManager(ArtistModelManager artistModelManager) {
		this.artistModelManager = artistModelManager;
	}

	public LabelModelManager getLabelModelManager() {
		return labelModelManager;
	}

	public void setLabelModelManager(LabelModelManager labelModelManager) {
		this.labelModelManager = labelModelManager;
	}

	public ReleaseModelManager getReleaseModelManager() {
		return releaseModelManager;
	}

	public void setReleaseModelManager(ReleaseModelManager releaseModelManager) {
		this.releaseModelManager = releaseModelManager;
	}

	public SongModelManager getSongModelManager() {
		return songModelManager;
	}

	public void setSongModelManager(SongModelManager songModelManager) {
		this.songModelManager = songModelManager;
	}

	public String getCurrentSearchType() { return currentSearchType; }
	public void setCurrentSearchType(String currentSearchType) { this.currentSearchType = currentSearchType; }

	public SearchModelManager getModelManager(String searchType) {
		currentSearchType = searchType;
		if (searchType.equals("artists"))
			return artistModelManager;
	    else if (searchType.equals("releases"))
	    	return releaseModelManager;
	    else if (searchType.equals("labels"))
	    	return labelModelManager;
	    else
	    	return songModelManager;
	}

	public void init() {
		if (!RE3Properties.getBoolean("server_mode")) {
			artistModelManager.initialize(null);
			labelModelManager.initialize(null);
			releaseModelManager.initialize(null);
			songModelManager.initialize(null);
		} else {
			artistModelManager.initialize();
			labelModelManager.initialize();
			releaseModelManager.initialize();
			songModelManager.initialize();
		}
	}

	public void write(LineWriter writer) {
		writer.writeLine(2); //version
		artistModelManager.write(writer);
		labelModelManager.write(writer);
		releaseModelManager.write(writer);
		songModelManager.write(writer);
		writer.writeLine(currentSearchType);
	}
}
