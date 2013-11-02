package com.mixshare.rapid_evolution.net;

import java.net.Socket;
import java.util.Vector;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.ibm.icu.util.StringTokenizer;
import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.DataConstantsHelper;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.filter.style.StyleIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.artist.ArtistIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.song.SongIdentifier;
import com.mixshare.rapid_evolution.data.mined.discogs.artist.DiscogsArtistProfile;
import com.mixshare.rapid_evolution.data.mined.yahoo.artist.YahooArtistProfile;
import com.mixshare.rapid_evolution.data.profile.search.artist.ArtistProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.record.filter.style.StyleRecord;
import com.mixshare.rapid_evolution.data.record.search.artist.ArtistRecord;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.data.util.DegreeValue;
import com.mixshare.rapid_evolution.workflow.SandmanThread;
import com.mixshare.rapid_evolution.workflow.Task;
import com.mixshare.rapid_evolution.workflow.TaskManager;
import com.mixshare.rapid_evolution.workflow.maintenance.IdentifierCleanup;
import com.mixshare.rapid_evolution.workflow.maintenance.ProfileConsistencyCheck;
import com.mixshare.rapid_evolution.workflow.maintenance.filter.ComputeFilterStats;
import com.mixshare.rapid_evolution.workflow.maintenance.search.RemoveBadImagesTask;

public class ClientServer extends CommandServer implements DataConstants {

    private static Logger log = Logger.getLogger(ClientServer.class);

    private final String userName = null;
    private boolean stopped_serving = false;

    public ClientServer(Socket sock) {
        super(sock);
    }

    @Override
	protected void finalize() throws Throwable {
        try {
        	ConnectionManager.removeConnection(userName);
        } finally {
            super.finalize();
        }
    }

    @Override
	protected boolean authenticate() {
    	String version = receiveLine(); // receives some of the crap from the putty program... used as a temp. fix for now
    	if (version == null)
    		return false;
        String username = receiveLine();
        String password = receiveLine();
        if (username.equals(RE3Properties.getProperty("console_username")) && password.equals(RE3Properties.getProperty("console_password"))) {
            ConnectionManager.addConnection(userName);
            return true;
        } else {
        	log.warn("authenticate(): invalid username=" + username + ", password=" + password);
        }
        return false;
    }

    @Override
	protected void ProcessCommand(String command) {
        try {
        	if (command.startsWith("info")) {
        		StringTokenizer tokenizer = new StringTokenizer(command, " ");
        		tokenizer.nextToken(); // info
        		if (!tokenizer.hasMoreTokens()) {
	        		sendLine("version=" + RapidEvolution3.RAPID_EVOLUTION_VERSION);
	        		sendLine("# artists=" + Database.getArtistIndex().getSize() + " (" + Database.getArtistIndex().getSizeInternalItems() + "/" + Database.getArtistIndex().getSizeExternalItems() + ")");
	        		sendLine("# labels=" + Database.getLabelIndex().getSize() + " (" + Database.getLabelIndex().getSizeInternalItems() + "/" + Database.getLabelIndex().getSizeExternalItems() + ")");
	        		sendLine("# releases=" + Database.getReleaseIndex().getSize() + " (" + Database.getReleaseIndex().getSizeInternalItems() + "/" + Database.getReleaseIndex().getSizeExternalItems() + ")");
	        		sendLine("# songs=" + Database.getSongIndex().getSize() + " (" + Database.getSongIndex().getSizeInternalItems() + "/" + Database.getSongIndex().getSizeExternalItems() + ")");
	        		sendLine("# styles=" + Database.getStyleIndex().getSize());
	        		sendLine("# tags=" + Database.getTagIndex().getSize());
	        		sendLine("# playlists=" + Database.getPlaylistIndex().getSize());
        		} else {
        			SongRecord record = null;
        			String filename = tokenizer.nextToken();
    				String songId = filename;
    				while (tokenizer.hasMoreTokens())
    					songId += " " + tokenizer.nextToken();
    				int artistPrefix = songId.indexOf("\"");
    				int artistSuffix = songId.indexOf("\"", artistPrefix + 1);
    				int titlePrefix = songId.indexOf("\"", artistSuffix + 1);
    				int titleSuffix = songId.indexOf("\"", titlePrefix + 1);
    				String artist = songId.substring(artistPrefix + 1, artistSuffix);
    				String title = songId.substring(titlePrefix + 1, titleSuffix);
    				SongIdentifier id = new SongIdentifier(artist, title);
    				record = Database.getSongIndex().getSongRecord(id);
        			if (record != null) {
        				sendLine("Video found");
        				sendLine("\tArtist: " + record.getArtistsDescription());
        				sendLine("\tSong: " + record.getSongDescription());
        				sendLine("\tUnique ID: " + record.getUniqueId());
        				sendLine("\tStyles: " + record.getActualStyleDescription(true, 10));
        				sendLine("\tTags: " + record.getActualTagDescription(true, 10));
        				sendLine("\tPublished: " + record.getUserData(Database.getSongIndex().getUserDataType("Published")));
        				sendLine("\tInternal: " + !record.isExternalItem());
        				SongProfile profile = Database.getSongIndex().getSongProfile(record.getUniqueId());
        				if (profile != null) {
        				} else {
        					sendLine("*** Profile is missing! ***");
        				}
        			} else {
        				sendLine("Video not found");
        			}
        		}
        	} else if (command.equalsIgnoreCase("status")) {
        		sendLine("# queued foreground tasks=" + TaskManager.getNumQueuedForegroundTasks());
        		sendLine("# queued background tasks=" + TaskManager.getNumQueuedBackgroundTasks());
        		Vector<String> foregroundStatuses = TaskManager.getCurrentForegroundStatuses();
        		sendLine("# foreground task executors=" + foregroundStatuses.size());
        		for (String status : foregroundStatuses)
        			sendLine("\tstatus=" + status);
        		Vector<String> backgroundStatuses = TaskManager.getCurrentBackgroundStatuses();
        		sendLine("# background task executors=" + backgroundStatuses.size());
        		for (String status : backgroundStatuses)
        			sendLine("\tstatus=" + status);
        		sendLine("# sleeping foreground tasks=" + SandmanThread.sleepingForegroundTasks.size());
        		for (Task task : SandmanThread.sleepingForegroundTasks.keySet())
        			sendLine("\tstatus=" + task.toString());
        		sendLine("# sleeping background tasks=" + SandmanThread.sleepingBackgroundTasks.size());
        		for (Task task : SandmanThread.sleepingBackgroundTasks.keySet())
        			sendLine("\tstatus=" + task.toString());
        	} else if (command.equalsIgnoreCase("save")) {
        		RapidEvolution3.save();
        		sendLine("\tsaved!");
        	} else if (command.equalsIgnoreCase("shutdown")) {
        		RapidEvolution3.isTerminated = true;
        		//ClientListener.shutdown();
        		//disconnect();
        	} else if (command.equalsIgnoreCase("disconnect")) {
                stoppedServing();
                disconnect();
        	} else if (command.toLowerCase().startsWith("recompute ")) {
        		StringTokenizer tokenizer = new StringTokenizer(command, " ");
        		tokenizer.nextToken(); // recompute
        		String recomputeCommand = tokenizer.nextToken();
        	} else if (command.toLowerCase().startsWith("start ")) {
        		StringTokenizer tokenizer = new StringTokenizer(command, " ");
        		tokenizer.nextToken(); // start
        		String importCommand = tokenizer.nextToken();
        		if (importCommand.equalsIgnoreCase("computefilterstats"))
        			TaskManager.runBackgroundTask(new ComputeFilterStats());
        		else if (importCommand.equalsIgnoreCase("removebadimages"))
        			TaskManager.runBackgroundTask(new RemoveBadImagesTask());
        		else if (importCommand.equalsIgnoreCase("profileconsistencycheck"))
        			TaskManager.runBackgroundTask(new ProfileConsistencyCheck());
        		else if (importCommand.equalsIgnoreCase("idcleanup"))
        			TaskManager.runBackgroundTask(new IdentifierCleanup());
        	} else if (command.toLowerCase().startsWith("get ")) {
        		String property = command.substring(4);
        		sendLine(RE3Properties.getProperty(property));
        	} else if (command.toLowerCase().startsWith("set ")) {
        		StringTokenizer tokenizer = new StringTokenizer(command, " ");
        		tokenizer.nextToken(); // set
        		String propertyName = tokenizer.nextToken();
        		String value = tokenizer.nextToken();
        		if (log.isInfoEnabled())
        			log.info("processCommand(): setting property=" + propertyName + ", value=" + value);
        		RE3Properties.setProperty(propertyName, value);
        	} else if (command.toLowerCase().startsWith("artistinfo")) {
        		String artistName = command.substring(11).trim();
        		ArtistRecord artist = Database.getArtistIndex().getArtistRecord(new ArtistIdentifier(artistName));
        		ArtistProfile artistProfile = Database.getArtistIndex().getArtistProfile(new ArtistIdentifier(artistName));
        		if (artist != null) {
        			sendLine("Found artist=" + artistName);

        			sendLine("\tactual styles=" + artist.getActualStyleDescription(true, Integer.MAX_VALUE));

        			Vector<DegreeValue> sourceStyles = artist.getSourceStyleDegreeValues();
        			for (DegreeValue sourceStyle : sourceStyles)
        				sendLine("\tsource style=" + sourceStyle.getName() + ", " + sourceStyle.getPercentage() + "%, " + DataConstantsHelper.getDataSourceDescription(sourceStyle.getSource()));

        			DiscogsArtistProfile discogsProfile = (DiscogsArtistProfile)artistProfile.getMinedProfile(DATA_SOURCE_DISCOGS);
        			if (discogsProfile != null)
        				sendLine("\tdiscogs styles=" + discogsProfile.getStyles());
        			else
        				sendLine("\tdiscogs styles=none");

        			YahooArtistProfile yahooProfile = (YahooArtistProfile)artistProfile.getMinedProfile(DATA_SOURCE_YAHOO);
        			if (yahooProfile != null)
        				sendLine("\tyahoo styles=" + yahooProfile.getStyleDegrees());
        			else
        				sendLine("\tyahoo styles=none");

        			Vector<DegreeValue> releaseStyles = artistProfile.computeStyleDegreesFromReleases();
    				sendLine("\trelease styles=" + releaseStyles);

        			Vector<DegreeValue> songStyles = artistProfile.computeStyleDegreesFromSongs();
    				sendLine("\tsong styles=" + songStyles);

    				Vector<DegreeValue> computedStyles = artistProfile.computeStylesInternal();
    				sendLine("\tcomputed styles=" + computedStyles);

    				for (DegreeValue computedStyle : computedStyles) {
    					StyleIdentifier styleIdentifier = new StyleIdentifier(computedStyle.getName());
    					int styleId = Database.getStyleIndex().getUniqueIdFromIdentifier(styleIdentifier);
    					StyleRecord style = Database.getStyleIndex().getStyleRecord(styleId);
    					sendLine("\t\tactual style=" + style);
    				}

        			sendLine("\tactual tags=" + artist.getActualTagDescription(true, Integer.MAX_VALUE));
        			Vector<DegreeValue> sourceTags = artist.getSourceTagDegreeValues();
        			for (DegreeValue sourceTag : sourceTags)
        				sendLine("\tsource tags=" + sourceTag.getName() + ", " + sourceTag.getPercentage() + "%, " + DataConstantsHelper.getDataSourceDescription(sourceTag.getSource()));
        			sendLine("\t# songs=" + artist.getNumSongs());
        			Vector<SongRecord> songs = artist.getSongs();
        			for (SongRecord song : songs) {
        				sendLine("\tSONG ID=" + song.getUniqueId());
        				sendLine("\t\tname==" + song.toString());
        				sendLine("\t\tstyles==" + song.getActualStyleDescription(true, Integer.MAX_VALUE));
        				sendLine("\t\ttags==" + song.getActualTagDescription(true, Integer.MAX_VALUE));
        			}
        		} else {
        			sendLine("Couldn't find artist=" + artistName);
        		}
        	} else if (command.toLowerCase().startsWith("resave")) {
        		String artistName = command.substring(7).trim();
        		ArtistProfile artist = Database.getArtistIndex().getArtistProfile(new ArtistIdentifier(artistName));
        		if (artist != null) {
        			sendLine("Saving artist=" + artistName);
        			artist.computeMetadataFromSongs();
        			artist.save();
        			sendLine("Done");
        		} else {
        			sendLine("Couldn't find artist=" + artistName);
        		}
        	} else if (command.toLowerCase().startsWith("delete")) {
        		StringTokenizer tokenizer = new StringTokenizer(command, " ");
        		tokenizer.nextToken(); // delete
        		int id = Integer.parseInt(tokenizer.nextToken());
        		sendLine("Deleting song ID=" + id);
        		Database.getSongIndex().delete(id);
        	} else if (command.toLowerCase().startsWith("logging ")) {
        		StringTokenizer tokenizer = new StringTokenizer(command, " ");
        		tokenizer.nextToken(); // logging
        		String l = tokenizer.nextToken(); // level
        		String className = tokenizer.nextToken();
        		Level level = null;
        		if (l.equalsIgnoreCase("off"))
        			level = Level.OFF;
        		else if (l.equalsIgnoreCase("trace"))
        			level = Level.TRACE;
        		else if (l.equalsIgnoreCase("debug"))
        			level = Level.DEBUG;
        		else if (l.equalsIgnoreCase("info"))
        			level = Level.INFO;
        		else if (l.equalsIgnoreCase("error"))
        			level = Level.ERROR;
        		else if (l.equalsIgnoreCase("fatal"))
        			level = Level.FATAL;
        		else if (l.equalsIgnoreCase("warn"))
        			level = Level.WARN;
        		if (level != null) {
        			Logger.getLogger(className).setLevel(level);
        		}
        	} else if (command.equalsIgnoreCase("help")) {
        		sendLine("available commands:");
        		sendLine("\tdisconnect");
        		sendLine("\tartistinfo %artistname%");
        		sendLine("\tresave %artistname%");
        		sendLine("\tdelete %song_id%");
        		sendLine("\tget %property_name%");
        		sendLine("\tinfo [%videoname%]");
        		sendLine("\tlogging [%level%] [%classname%]");
        		sendLine("\trecompute [playcounts/similar] [%parameter%]");
        		sendLine("\tsave");
        		sendLine("\tset %property_name% %value%");
        		sendLine("\tshutdown");
        		sendLine("\tstatus");
        	}
        } catch (Exception e) {
            log.error("ProcessCommand(): exception occurred: ", e);
            stoppedServing();
            disconnect();
        }
    }

    @Override
	protected void stoppedServing() {
        if (stopped_serving)
        	return;
        stopped_serving = true;
    	ConnectionManager.removeConnection(userName);
    }


    public String getUserName() { return userName; }

}