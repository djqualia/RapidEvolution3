package com.mixshare.rapid_evolution.workflow.importers.playlists;

import java.io.File;
import java.util.Collection;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.audio.PlaylistFileTypes;
import com.mixshare.rapid_evolution.audio.tags.TagManager;
import com.mixshare.rapid_evolution.audio.util.AudioUtil;
import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.profile.filter.playlist.PlaylistProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.data.submitted.filter.playlist.SubmittedOrderedPlaylist;
import com.mixshare.rapid_evolution.data.submitted.search.song.SubmittedSong;
import com.mixshare.rapid_evolution.ui.dialogs.taskprogress.TaskProgressLauncher;
import com.mixshare.rapid_evolution.ui.updaters.view.filter.SelectAndFocusPlaylist;
import com.mixshare.rapid_evolution.util.FileUtil;
import com.mixshare.rapid_evolution.util.StringUtil;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.readers.PlainTextLineReader;
import com.mixshare.rapid_evolution.workflow.CommonTask;
import com.mixshare.rapid_evolution.workflow.SandmanThread;
import com.trolltech.qt.gui.QApplication;

public class PlaylistImporterTask extends CommonTask implements DataConstants, PlaylistFileTypes {
	
    static private Logger log = Logger.getLogger(PlaylistImporterTask.class);
    static private final long serialVersionUID = 0L;    	

    private int totalFilesToProcess = 0;
    private int numFilesProcessed = 0;    
    private Collection<String> filenames;
    
    public PlaylistImporterTask(Collection<String> filenames) { // filenames could include directories
    	this.filenames = filenames;
    }
    
    public String toString() { return "Importing from playlists " + StringUtil.getTruncatedDescription(filenames); }
    
    public void execute() {
        try {
        	if (RE3Properties.getBoolean("show_progress_bars") && !RE3Properties.getBoolean("server_mode"))
        		QApplication.invokeLater(new TaskProgressLauncher(this));        	            	        	        	
        	totalFilesToProcess = filenames.size();
        	for (String filename : filenames) {
        		if (RapidEvolution3.isTerminated || isCancelled())
        			return;
        		File file = new File(filename);
        		if (file.isDirectory()) {
        			Vector<String> actualFilenames = new Vector<String>();
        			FileUtil.recurseFileTree(filename, actualFilenames);
        			totalFilesToProcess += actualFilenames.size();
        			for (String actualFilename : actualFilenames)
        				addFileLocal(actualFilename);
        		} else {
        			addFileLocal(filename);
        		}
        	}
        	SandmanThread.wakeUpAllBackgroundTasks();
        } catch (Exception e) {
            log.error("execute(): error loading database", e);
        }
    }
    
    private void addFileLocal(String filename) {
    	addFile(filename);
    	++numFilesProcessed;
    	setProgress(((float)numFilesProcessed) / numFilesProcessed);
    	
    }
    
    static public void addFile(String filename) {
    	addFile(filename, true);    	
    }
    static public void addFile(String filename, boolean setFocus) {
    	try {
    		if (AudioUtil.isSupportedPlaylistFileType(filename)) {
    			String playlistName = FileUtil.getFilenameMinusDirectory(filename);
    			playlistName = playlistName.substring(0, playlistName.length() - 4);
	    		SubmittedOrderedPlaylist playlist = new SubmittedOrderedPlaylist(playlistName);
    			PlaylistProfile existingProfile = Database.getPlaylistIndex().getPlaylistProfile(playlist.getIdentifier());
    			if (existingProfile != null)
    				return;
    			
    			Vector<Integer> songIds = new Vector<Integer>();
    			int playlistType = AudioUtil.getPlaylistFileType(filename);
    			if (playlistType == PLAYLIST_FILE_TYPE_M3U) {
        			Vector<File> songFilenames = new Vector<File>();
        			LineReader fileReader = new PlainTextLineReader(filename);
    				String line = fileReader.getNextLine();
    				while (line != null) {
    					if (!line.startsWith("#EXTINF")) {
    						File file = new File(line);
    						if (file.isFile() && file.exists()) {
    							// check for absolute path
    							songFilenames.add(file);
    						} else {
    							// check for relative path
    							File m3ufile = new File(filename);
    							int index = m3ufile.getPath().indexOf(m3ufile.getName());
    							String fullPath = m3ufile.getPath().substring(0, index) + line;
    							file = new File(fullPath);
    							if (file.isFile() && file.exists())
    								songFilenames.add(file);
    						}
  			          	}
    					line = fileReader.getNextLine();
    				}
    				fileReader.close();
    				for (File file : songFilenames) {    					
    					SongRecord existingRecord = Database.getSongIndex().getSongRecord(file.getAbsolutePath());
    					if ((existingRecord == null) && file.exists()) {
	    		    		SubmittedSong song = TagManager.readTags(file.getAbsolutePath());
	    		    		if (song.getSongIdentifier().isValid()) {
	    		    			existingRecord = Database.getSongIndex().getSongRecord(song.getSongIdentifier());
	    		    			if (existingRecord == null) {
	    		    				SongProfile songProfile = (SongProfile)Database.getSongIndex().addOrUpdate(song);
	    		    				if (songProfile != null)
	    		    					songIds.add(songProfile.getUniqueId());
	    		    			} else {
	    		    				songIds.add(existingRecord.getUniqueId());
	    		    			}
	    		    		} else {
    		    				log.warn("addFile(): could not get valid song identifier from file=" + filename);
    		    			}
    					} else {
    						songIds.add(existingRecord.getUniqueId());
    					}
    				}
    			} else if (playlistType == PLAYLIST_FILE_TYPE_MIX) {    				
    				LineReader fileReader = new PlainTextLineReader(filename);
    				String version = fileReader.getNextLine();
    				int numSongs = Integer.parseInt(fileReader.getNextLine());
    				String line = fileReader.getNextLine();   				
    				while (line != null) {
    					String songId = line;
    					int uniqueId = Integer.parseInt(fileReader.getNextLine());
    					
    					String s1 = " - ";
    					String s2 = "   [";
    					String s3 = "]   ";
    					String s4 = "  (";
    					String s5 = ")";
    					int index1 = songId.indexOf(s1);
    					int index2 = songId.indexOf(s2, index1);
    					int index3 = songId.indexOf(s3, index2);
    					int index4 = songId.lastIndexOf(s4);
    					int index5 = songId.lastIndexOf(s5);
    					
    					String artist = songId.substring(0, index1);
    					String release = songId.substring(index1 + s1.length(), index2);
    					String track = songId.substring(index2 + s2.length(), index3);			
    					String title = (index4 > 0) ? songId.substring(index3 + s3.length(), index4) : songId.substring(index3 + s3.length());
    					String remix = ((index4 > 0) && (index5 > 0)) ? songId.substring(index4 + s4.length(), index5) : "";
    					
    					SubmittedSong submittedSong = new SubmittedSong(artist, release, track, title, remix);
    					SongRecord song = Database.getSongIndex().getSongRecord(submittedSong.getIdentifier());
    					if (song != null)
    						songIds.add(song.getUniqueId());
    					else
    						log.warn("addFile(): could not find matching song for=" + submittedSong.getIdentifier());
    					
    					line = fileReader.getNextLine();
    				}    				
    			}
	    		playlist.setSongIds(songIds);
	    		if (playlist.getIdentifier().isValid()) {
    				PlaylistProfile playlistProfile = (PlaylistProfile)Database.getPlaylistIndex().addOrUpdate(playlist);
    				if ((playlistProfile != null) && setFocus)
    					QApplication.invokeLater(new SelectAndFocusPlaylist(playlistProfile.getPlaylistRecord()));
	    		} else {
	    			log.warn("addFile(): could not get valid playlist identifier from file=" + filename);
	    		}
    		}
        } catch (Exception e) {
            log.error("addFile(): error loading database", e);
        }
    }
    
    public int getTaskPriority() { return RE3Properties.getInt("default_task_priority"); }
    
    public Object getResult() { return null; }

}
