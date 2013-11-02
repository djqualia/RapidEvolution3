package com.mixshare.rapid_evolution.workflow.maintenance.search.parsers;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.ui.dialogs.taskprogress.TaskProgressLauncher;
import com.mixshare.rapid_evolution.util.StringUtil;
import com.mixshare.rapid_evolution.workflow.CommonTask;
import com.mixshare.rapid_evolution.workflow.Task;
import com.trolltech.qt.gui.QApplication;

public class ParseTitleForTrackTask extends CommonTask {
	
	static private Logger log = Logger.getLogger(ParseTitleForTrackTask.class);
    static private final long serialVersionUID = 0L;    	
    
    private Vector<SongRecord> songs;
		
	public ParseTitleForTrackTask(Vector<SongRecord> songs) {
		this.songs = songs;
	}
				
	public void execute() {
		try {
			int totalSongs = songs.size();
        	if (RE3Properties.getBoolean("show_progress_bars") && !RE3Properties.getBoolean("server_mode"))
        		QApplication.invokeLater(new TaskProgressLauncher(this));
        	int numSongs = 0;
			for (SongRecord song : songs) {
				if (RapidEvolution3.isTerminated || isCancelled())
					return;
				SongProfile songProfile = Database.getSongIndex().getSongProfile(song.getUniqueId());
				if (songProfile != null)
					fixSong(songProfile);
				++numSongs;				
				setProgress(((float)numSongs) / totalSongs);
			}
		} catch (Exception e) {
			log.error("execute(): error", e);
		}
	}
	
	public int getTaskPriority() { return Task.DEFAULT_TASK_PRIORITY; }
	
	public Object getResult() { return null; }
	
	public String toString() { return "Parsing track from title fields for " + StringUtil.getTruncatedDescription(songs); }

    public void fixSong(SongProfile song) {
    	try {
    		String title = song.getTitle();
    		String separator = " ";
    		int index = title.indexOf(separator);
    		if (index >= 0) {
    			String track = title.substring(0, index);
    			String newTitle = title.substring(index + separator.length(), title.length());
    			if (song.getTrack().equals(""))
    				song.setReleaseTrack(track.trim());
    			song.setTitle(newTitle);
    			song.save();
    		}    		
   	   	} catch (Exception e) {
   	   		log.error("fixSong(): error=" + song, e);
   	   	}
   	}
	
}
