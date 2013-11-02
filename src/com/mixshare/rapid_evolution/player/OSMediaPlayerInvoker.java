package com.mixshare.rapid_evolution.player;

import java.io.File;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.util.CommandRunner;
import com.mixshare.rapid_evolution.util.OSHelper;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.mixshare.rapid_evolution.util.io.writers.PlainTextLineWriter;
import com.trolltech.qt.core.QUrl;
import com.trolltech.qt.gui.QDesktopServices;

public class OSMediaPlayerInvoker {

	static private Logger log = Logger.getLogger(OSMediaPlayerInvoker.class);	
	
    static public void invokePlaylist(Vector<Integer> songIds) {
    	try {
    		String playlistFilename = new File(OSHelper.getWorkingDirectory() + "/" + "playlist.m3u").getAbsolutePath();
    		LineWriter playlistFile = new PlainTextLineWriter(playlistFilename);
    		String loneFilename = null;
        	playlistFile.writeLine("#EXTM3U");
        	for (Integer songId : songIds) {
        		SongRecord song = (SongRecord)Database.getSongIndex().getRecord(songId);        	             
        		if (!song.getSongFilename().equals("")) {
        			playlistFile.writeLine(song.getSongFilename());
        			loneFilename = song.getSongFilename();
        		}
        	}
    		playlistFile.close();
    		boolean usePlayerPath = false;
    		if (RE3Properties.getProperty("os_media_player_path").length() > 0) {
    			if (new File(RE3Properties.getProperty("os_media_player_path")).exists())
    				usePlayerPath = true;
    		}
    		if (usePlayerPath) {
    			String[] command = new String[2];
    			command[0] = RE3Properties.getProperty("os_media_player_path");
    			command[1] = (songIds.size() == 1) ? loneFilename : playlistFilename;
    			Runtime.getRuntime().exec(command);
    		} else {
	    		// invokes default OS media player (new to Java 6)
	    		// TODO: test on mac/linux    		
	    		if (OSHelper.isWindows()) {
	    			// NOTE: in windows, the desktop.open method did not work when certain media players were set (like windows media player)
	    			// the line below seems to work...
	    			// http://bugs.sun.com/view_bug.do?bug_id=6599987
	    			Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + new File(playlistFilename).toURI());
	    		} else {
	    			QDesktopServices.openUrl(new QUrl(playlistFilename));
	    			//Desktop.getDesktop().open(new File(playlistFilename)); // java 6
	    		}
    		}
    		
    	} catch (Exception e) {
    		log.error("invokePlaylist(): error", e);
    	}
    }
	
}
