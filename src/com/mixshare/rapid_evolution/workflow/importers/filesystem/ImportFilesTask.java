package com.mixshare.rapid_evolution.workflow.importers.filesystem;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.audio.tags.TagManager;
import com.mixshare.rapid_evolution.audio.util.AudioUtil;
import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.filter.playlist.PlaylistIdentifier;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.profile.filter.playlist.PlaylistProfile;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.record.search.artist.ArtistRecord;
import com.mixshare.rapid_evolution.data.record.search.label.LabelRecord;
import com.mixshare.rapid_evolution.data.record.search.release.ReleaseRecord;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.data.submitted.filter.playlist.SubmittedCategoryPlaylist;
import com.mixshare.rapid_evolution.data.submitted.filter.playlist.SubmittedDynamicPlaylist;
import com.mixshare.rapid_evolution.data.submitted.search.song.SubmittedSong;
import com.mixshare.rapid_evolution.ui.dialogs.taskprogress.TaskProgressLauncher;
import com.mixshare.rapid_evolution.ui.model.tree.RecordTreeModelManager;
import com.mixshare.rapid_evolution.ui.updaters.view.filter.SelectAndFocusPlaylist;
import com.mixshare.rapid_evolution.ui.widgets.filter.playlists.PlaylistsWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.profile.ProfileWidgetUI;
import com.mixshare.rapid_evolution.util.FileUtil;
import com.mixshare.rapid_evolution.util.StringUtil;
import com.mixshare.rapid_evolution.util.image.ImageUtil;
import com.mixshare.rapid_evolution.video.util.VideoUtil;
import com.mixshare.rapid_evolution.workflow.CommonTask;
import com.mixshare.rapid_evolution.workflow.SandmanThread;
import com.mixshare.rapid_evolution.workflow.TaskManager;
import com.mixshare.rapid_evolution.workflow.importers.playlists.PlaylistImporterTask;
import com.mixshare.rapid_evolution.workflow.user.AddImagesTask;
import com.trolltech.qt.gui.QApplication;

public class ImportFilesTask extends CommonTask implements DataConstants {
	
    static private Logger log = Logger.getLogger(ImportFilesTask.class);
    static private final long serialVersionUID = 0L;    	

    private boolean createPlaylist = RE3Properties.getBoolean("create_date_added_playlist_when_adding_files");
    private boolean setFocus = true;
    private Collection<String> filenames;
    private boolean hasAddedFiles = false;
    private int totalFilesToProcess = 0;
    private int numFilesProcessed = 0;
    private boolean silentMode = false;
    private boolean importPlaylists;
    private boolean repeats = false;
    
	public ImportFilesTask(Collection<String> filenames) { // filenames could include directories
    	this.filenames = filenames;
    }
    public ImportFilesTask(String filename, boolean setFocus, boolean silentMode) {
    	filenames = new Vector<String>(1);
    	filenames.add(filename);    	
    	this.setFocus = setFocus;
    	this.silentMode = silentMode;
    }
    
    public String toString() { return "Scanning/importing " + StringUtil.getTruncatedDescription(filenames); }
    
    public void execute() {
        try {
        	if (filenames.size() == 1) {
        		SongProfile song = Database.getSongIndex().getSongProfile(filenames.iterator().next());
        		if (song != null) {
        			if (!RE3Properties.getBoolean("server_mode"))
        				QApplication.invokeLater(new EditThread(song));
        			return;
        		}
        	}
         	if (!silentMode && RE3Properties.getBoolean("show_progress_bars") && !RE3Properties.getBoolean("server_mode"))
        		QApplication.invokeLater(new TaskProgressLauncher(this));        	
        	log.info("execute(): scanning locations=" + filenames);
        	ArrayList<String> newFilenames = new ArrayList<String>();
        	for (String filename : filenames)
        		newFilenames.add(filename); 
        	java.util.Collections.sort(newFilenames, new FileSorter());
        	filenames = newFilenames;
        	Vector<PlaylistProfile> playlists = new Vector<PlaylistProfile>();
        	if (containsValidFile() && !isAllPlaylists()) {
	        	if (createPlaylist) {
					// create a playlist to dump the new files        			
					Calendar cal = Calendar.getInstance();
					int dayInt = cal.get(Calendar.DATE);
					String day = (dayInt >= 10) ? String.valueOf(dayInt) : "0" + dayInt;
					int monthInt = cal.get(Calendar.MONTH) + 1;
					String month = (monthInt >= 10) ? String.valueOf(monthInt) : "0" + monthInt;
					int yearInt = cal.get(Calendar.YEAR);        			
					String year = String.valueOf(yearInt);
					PlaylistProfile dateAddedPlaylist = Database.getPlaylistIndex().getPlaylistProfile(new PlaylistIdentifier("date added"));
					if (dateAddedPlaylist == null) {
						SubmittedCategoryPlaylist submittedPlaylist = new SubmittedCategoryPlaylist("date added");
						dateAddedPlaylist = (PlaylistProfile)Database.getPlaylistIndex().addOrUpdate(submittedPlaylist);
					}
					PlaylistProfile yearPlaylist = Database.getPlaylistIndex().getPlaylistProfile(new PlaylistIdentifier(year));
					if (yearPlaylist == null) {
						SubmittedDynamicPlaylist submittedPlaylist = new SubmittedDynamicPlaylist(year);
						submittedPlaylist.setParentInstances(((RecordTreeModelManager)Database.getPlaylistModelManager()).getMatchingInstances(dateAddedPlaylist.getFilterRecord()));
						yearPlaylist = (PlaylistProfile)Database.getPlaylistIndex().addOrUpdate(submittedPlaylist);
						Database.getPlaylistIndex().addRelationship(dateAddedPlaylist.getHierarchicalRecord(), yearPlaylist.getHierarchicalRecord());
					}
					PlaylistProfile monthPlaylist = Database.getPlaylistIndex().getPlaylistProfile(new PlaylistIdentifier(year + "-" + month));
					if (monthPlaylist == null) {
						SubmittedDynamicPlaylist submittedPlaylist = new SubmittedDynamicPlaylist(year + "-" + month);
						submittedPlaylist.setParentInstances(((RecordTreeModelManager)Database.getPlaylistModelManager()).getMatchingInstances(yearPlaylist.getFilterRecord()));
						monthPlaylist = (PlaylistProfile)Database.getPlaylistIndex().addOrUpdate(submittedPlaylist);
						Database.getPlaylistIndex().addRelationship(yearPlaylist.getHierarchicalRecord(), monthPlaylist.getHierarchicalRecord());
					}
					PlaylistProfile dayPlaylist = Database.getPlaylistIndex().getPlaylistProfile(new PlaylistIdentifier(year + "-" + month + "-" + day));
					if (dayPlaylist == null) {
						SubmittedDynamicPlaylist submittedPlaylist = new SubmittedDynamicPlaylist(year + "-" + month + "-" + day);
						submittedPlaylist.setParentInstances(((RecordTreeModelManager)Database.getPlaylistModelManager()).getMatchingInstances(monthPlaylist.getFilterRecord()));						
						dayPlaylist = (PlaylistProfile)Database.getPlaylistIndex().addOrUpdate(submittedPlaylist);
						Database.getPlaylistIndex().addRelationship(monthPlaylist.getHierarchicalRecord(), dayPlaylist.getHierarchicalRecord());
					}      
					playlists.add(dayPlaylist);
					playlists.add(monthPlaylist);
					playlists.add(yearPlaylist);
					if (createPlaylist && setFocus && !RE3Properties.getBoolean("server_mode"))
						QApplication.invokeAndWait(new SelectAndFocusPlaylist(dayPlaylist.getPlaylistRecord()));
	        	}
        	} else if (isAllImages()) {
        		log.info("execute(): is all images");
        		Profile currentProfile = ProfileWidgetUI.instance.getCurrentProfile();
        		if ((currentProfile != null) && (currentProfile instanceof SearchProfile))
        			TaskManager.runForegroundTask(new AddImagesTask((SearchProfile)currentProfile, filenames));
        		return;
        	}
        	totalFilesToProcess = filenames.size();
        	for (String filename : filenames) {
				if (RapidEvolution3.isTerminated || isCancelled())
					return;
        		File file = new File(filename);
        		if (file.isDirectory()) {
        			Vector<String> actualFilenames = new Vector<String>();
        			FileUtil.recurseFileTree(filename, actualFilenames);
        			totalFilesToProcess += actualFilenames.size();
        			for (String actualFilename : actualFilenames) {
        				if (RapidEvolution3.isTerminated || isCancelled())
        					return;
        				addFile(actualFilename, playlists);        
        			}
        		} else {
        			addFile(filename, playlists);
        		}
        	}
        	if (hasAddedFiles)
        		SandmanThread.wakeUpAllBackgroundTasks();
        } catch (Exception e) {
            log.error("execute(): error importing files=" + filenames, e);
        } finally {
        	if (repeats)
        		SandmanThread.putBackgroundTaskToSleep(this, 1000 * 60 * 10); // 10 minutes for now
        }
    }
    
    private boolean isAllPlaylists() {
    	for (String filename : filenames)
    		if (!AudioUtil.isSupportedPlaylistFileType(filename))
    			return false;
    	return true;
    }

    public boolean isImportPlaylists() { return importPlaylists; }
	public void setImportPlaylists(boolean importPlaylists) { this.importPlaylists = importPlaylists; }
    
	public boolean repeats() { return repeats; }
	public void setRepeats(boolean repeats) { this.repeats = repeats; }
	
    private boolean containsValidFile() {
    	for (String filename : filenames) {
    		if (AudioUtil.isSupportedPlaylistFileType(filename))
    			return true;
    		if (AudioUtil.isSupportedAudioFileType(filename))
    			return true;
    		if (VideoUtil.isSupportedVideoFileType(filename))
    			return true;
    		if (new File(filename).isDirectory())
    			return true;
    	}
    	return false;
    }
    
    private boolean isAllImages() {
    	for (String filename : filenames) {
    		if (!ImageUtil.isSupportedImageFileType(filename))
    			return false;
    	}
    	return true;
    }
    
    private void addFile(String filename, Vector<PlaylistProfile> playlistsToAdd) {
    	try {
    		if (AudioUtil.isSupportedAudioFileType(filename) || VideoUtil.isSupportedVideoFileType(filename)) {
    			if (Database.getSongIndex().getSongRecord(filename) == null) {
		    		SubmittedSong song = TagManager.readTags(filename);
		    		boolean addToPlaylists = true;
		    		SongRecord existingSong = Database.getSongIndex().getSongRecord(song.getIdentifier());
		    		if ((existingSong != null) && !existingSong.isExternalItem() && (existingSong.getSongFilename() != null) && (existingSong.getSongFilename().length() > 0)) {
		    			String existingFilenameUnified = FileUtil.unify(existingSong.getSongFilename());
		    			String newFilenameUnified = FileUtil.unify(filename);
		    			if (!existingFilenameUnified.equals(newFilenameUnified)) {
		    				if (RE3Properties.getBoolean("prevent_duplicates_from_being_added")) {
		    					Database.getSongIndex().addDuplicateReference(newFilenameUnified, existingSong.getUniqueId());
		    					return;
		    				} else {
		    					existingSong = null; // will create a new song entry automatically
		    				}
		    			}
		    		}
		    		if ((existingSong != null) && !existingSong.isExternalItem())
		    			addToPlaylists = false;		    		
		    		if (addToPlaylists) {
		    			for (PlaylistProfile playlistToAdd : playlistsToAdd)
		    				song.addInitialPlaylist(playlistToAdd.getPlaylistRecord());
		    		}
		    		if (song.getSongIdentifier().isValid()) {	
		    			long timeBefore = System.currentTimeMillis() - 1;
		    			SongProfile addedSong = (SongProfile)Database.getSongIndex().addOrUpdate(song);		    			
		    			if (addedSong != null) {
		    				for (ArtistRecord artist : addedSong.getArtists()) {
		    					if (artist.getDateAdded() >= timeBefore) {
		    						for (PlaylistProfile playlist : playlistsToAdd)
		    							playlist.getPlaylistRecord().addArtist(artist.getUniqueId());
		    					}
		    				}
		    				for (LabelRecord label : addedSong.getLabels()) {
		    					if (label.getDateAdded() >= timeBefore) {
		    						for (PlaylistProfile playlist : playlistsToAdd)
		    							playlist.getPlaylistRecord().addLabel(label.getUniqueId());
		    					}
		    				}
		    				for (ReleaseRecord release : addedSong.getReleases()) {
		    					if (release.getDateAdded() >= timeBefore) {
		    						for (PlaylistProfile playlist : playlistsToAdd)
		    							playlist.getPlaylistRecord().addRelease(release.getUniqueId());		    						
		    					}
		    				}
		    			}
		    			hasAddedFiles = true;
		    		} else {
		    			log.warn("addFile(): could not get valid song identifier from file=" + filename);
		    		}
    			}
    		} else if (AudioUtil.isSupportedPlaylistFileType(filename)) {
    			if (importPlaylists) {
    				PlaylistImporterTask.addFile(filename, false);
    				hasAddedFiles = true;
    			}
    		}
        } catch (Exception e) {
            log.error("addFile(): error loading database", e);
        } finally {
        	++numFilesProcessed;
        	setProgress(((float)numFilesProcessed) / totalFilesToProcess);
        }
    }
    
    /**
     * Put new filenames first then update existing ones...
     */
    private class FileSorter implements Comparator<String> {
    	public int compare(String filename1, String filename2) {
    		SongRecord existingSong1 = Database.getSongIndex().getSongRecord(filename1);
    		SongRecord existingSong2 = Database.getSongIndex().getSongRecord(filename2);
    		if ((existingSong1 == null) && (existingSong2 != null))
    			return -1;
    		if ((existingSong1 != null) && (existingSong2 == null))
    			return 1;
    		return 0;
    	}
    }
    
    public int getTaskPriority() { return RE3Properties.getInt("default_task_priority") + 5; }
    
    public Object getResult() { return null; }

    private class EditThread extends Thread {
    	private SongProfile song;
    	public EditThread(SongProfile song) {
    		this.song = song;
    	}
    	public void run() {
    		ProfileWidgetUI.instance.editProfile(song);
    	}
    }
    
}
