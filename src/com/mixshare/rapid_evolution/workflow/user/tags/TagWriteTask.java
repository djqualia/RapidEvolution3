package com.mixshare.rapid_evolution.workflow.user.tags;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.audio.tags.TagManager;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.ui.dialogs.taskprogress.TaskProgressLauncher;
import com.mixshare.rapid_evolution.util.StringUtil;
import com.mixshare.rapid_evolution.workflow.CommonTask;
import com.mixshare.rapid_evolution.workflow.Task;
import com.trolltech.qt.gui.QApplication;

public class TagWriteTask extends CommonTask {
	
	static private Logger log = Logger.getLogger(TagWriteTask.class);
    static private final long serialVersionUID = 0L;    	
    
	private Vector<SongRecord> songs;
	private int totalSongs;
	private int currentSong;
	private boolean showProgress;
	private boolean boostPriority;
	
	public TagWriteTask(Vector<SongRecord> songs, boolean showProgress) {
		this.songs = songs;
		this.showProgress = showProgress;
	}
	public TagWriteTask(Vector<SongRecord> songs, boolean showProgress, boolean boostPriority) {
		this.songs = songs;
		this.showProgress = showProgress;
		this.boostPriority = boostPriority;
	}
				
	public void execute() {
		try {
			totalSongs = songs.size();
			currentSong = 0;			
        	if (showProgress && RE3Properties.getBoolean("show_progress_bars") && !RE3Properties.getBoolean("server_mode"))
        		QApplication.invokeLater(new TaskProgressLauncher(this));						
			for (SongRecord song : songs) {
				if (RapidEvolution3.isTerminated || isCancelled())
					return;
				SongProfile songProfile = Database.getSongIndex().getSongProfile(song.getUniqueId());
				if (songProfile != null)
					TagManager.writeTags(songProfile);
				++currentSong;
				setProgress(((float)currentSong) / totalSongs);
			}
		} catch (Exception e) {
			log.error("execute(): error", e);
		}
	}
	
	public int getTaskPriority() {
		if (boostPriority)
			return Task.DEFAULT_TASK_PRIORITY + 5;
		else
			return Task.DEFAULT_TASK_PRIORITY;
	}
	
	public Object getResult() { return null; }
	
	public String toString() { return "Writing tags to " + StringUtil.getTruncatedDescription(songs); }

}
