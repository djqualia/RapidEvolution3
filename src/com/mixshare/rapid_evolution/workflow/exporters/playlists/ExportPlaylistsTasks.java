package com.mixshare.rapid_evolution.workflow.exporters.playlists;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.audio.PlaylistFileTypes;
import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.record.filter.playlist.CategoryPlaylistRecord;
import com.mixshare.rapid_evolution.data.record.filter.playlist.PlaylistRecord;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.data.search.SearchResult;
import com.mixshare.rapid_evolution.ui.dialogs.taskprogress.TaskProgressLauncher;
import com.mixshare.rapid_evolution.util.FileUtil;
import com.mixshare.rapid_evolution.util.StringUtil;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.mixshare.rapid_evolution.util.io.writers.PlainTextLineWriter;
import com.mixshare.rapid_evolution.workflow.CommonTask;
import com.trolltech.qt.gui.QApplication;

public class ExportPlaylistsTasks extends CommonTask implements DataConstants, PlaylistFileTypes {

    static private Logger log = Logger.getLogger(ExportPlaylistsTasks.class);
    static private final long serialVersionUID = 0L;    	
	
    private String directory; 
    
    public ExportPlaylistsTasks(String directory) {
    	this.directory = directory;
    }
    
	public String toString() {
		return "Exporting Playlists To " + directory;
	}    
    
	public void execute() {
		try {
        	if (RE3Properties.getBoolean("show_progress_bars") && !RE3Properties.getBoolean("server_mode"))
        		QApplication.invokeLater(new TaskProgressLauncher(this));        				
			int numProcessed = 0;
			for (int playlistId : Database.getPlaylistIndex().getIds()) {
				if (RapidEvolution3.isTerminated || isCancelled())
					return;
				PlaylistRecord playlist = Database.getPlaylistIndex().getPlaylistRecord(playlistId);
				if ((playlist != null) && !playlist.isDisabled()) {
					if (!(playlist instanceof CategoryPlaylistRecord)) {
						String playlistFilename = directory + "/" + StringUtil.makeValidFilename(playlist.getPlaylistName()) + ".m3u";
						if (log.isDebugEnabled())
							log.debug("execute(): saving playlist=" + playlist + ", to file=" + playlistFilename);
						LineWriter textFile = new PlainTextLineWriter(playlistFilename);
						textFile.writeLine("#EXTM3U");
						for (SearchResult songRecord : playlist.getSongRecords()) {
							SongRecord song = (SongRecord)songRecord.getRecord();							
							if (song.hasValidSongFilename()) {								
								StringBuffer extInfLine = new StringBuffer();
								extInfLine.append("#EXTINF:");
								extInfLine.append(String.valueOf((int)song.getDuration().getDurationInSeconds()));
								extInfLine.append(",");
								extInfLine.append(song.toString());
								textFile.writeLine(extInfLine.toString());
								String filename = song.getSongFilename();
								if (RE3Properties.getBoolean("save_relative_paths_in_playlists"))
									filename = FileUtil.getRelativePath(filename, directory);								
								textFile.writeLine(filename);								
							}							
						}						
						textFile.close();
					}					
				}
				++numProcessed;
				setProgress(((float)numProcessed / Database.getPlaylistIndex().getSize()));
			}
		} catch (Exception e) {
			log.error("execute(): error", e);
		}
	}
	
}
