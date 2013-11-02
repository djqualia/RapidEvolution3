package com.mixshare.rapid_evolution.workflow.user;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.audio.tags.TagManager;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.record.search.release.ReleaseRecord;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.ui.dialogs.taskprogress.TaskProgressLauncher;
import com.mixshare.rapid_evolution.util.FileUtil;
import com.mixshare.rapid_evolution.util.StringUtil;
import com.mixshare.rapid_evolution.util.io.FileLockManager;
import com.mixshare.rapid_evolution.workflow.CommonTask;
import com.mixshare.rapid_evolution.workflow.Task;
import com.mixshare.rapid_evolution.workflow.event.OrganizeSongListener;
import com.trolltech.qt.gui.QApplication;

public class OrganizeTask extends CommonTask {

	static private Logger log = Logger.getLogger(OrganizeTask.class);
    static private final long serialVersionUID = 0L;

    static public DecimalFormat decimalFormat = new DecimalFormat("###.#");

    static private Vector<OrganizeSongListener> listeners = new Vector<OrganizeSongListener>();
    static public void addListener(OrganizeSongListener listener) { listeners.add(listener); }

    private final Vector<SongRecord> songs;
    private final boolean renameOnly;

	public OrganizeTask(Vector<SongRecord> songs, boolean renameOnly) {
		this.songs = songs;
		this.renameOnly = renameOnly;
	}

	@Override
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
					organizeSong(songProfile);
				++numSongs;
				setProgress(((float)numSongs) / totalSongs);
			}
		} catch (Exception e) {
			log.error("execute(): error", e);
		}
	}

	@Override
	public int getTaskPriority() { return Task.DEFAULT_TASK_PRIORITY; }

	@Override
	public Object getResult() { return null; }

	@Override
	public String toString() { return "Organizing songs " + StringUtil.getTruncatedDescription(songs); }

    public boolean organizeSong(SongProfile song) {
    	boolean success = false;
    	try {
    		if (RE3Properties.getProperty("organize_music_directory").equals(""))
    			return false;
    		File rootDir = new File(RE3Properties.getProperty("organize_music_directory"));
    		File backupDir = (RE3Properties.getProperty("organize_music_backup_directory").length() > 0)
    				? new File(RE3Properties.getProperty("organize_music_backup_directory"))
    				: null;
    		if (!rootDir.exists())
    			return false;
    		if (backupDir != null && !backupDir.exists())
    			return false;
    		boolean deleteOld = RE3Properties.getBoolean("delete_original_files_after_copy");
    		boolean renameFiles = RE3Properties.getBoolean("rename_files_during_organization");
    		boolean writeTags = RE3Properties.getBoolean("write_tags_during_organization");
    		String pattern = RE3Properties.getProperty("organize_music_rename_pattern");

    		boolean renamed = false;
   	       	if (renameFiles)
   	       		renamed = RenameTask.renameSongFileName(song, pattern);
   	       	if (renameOnly) {
   	       		if (renamed)
   	       			for (int i = 0; i < listeners.size(); ++i)
   	       				listeners.get(i).songRenamed(song);
   	       		return renamed;
   	       	}

   	       	if (writeTags)
   	       		TagManager.writeTags(song);

   	       String filename = song.getSongFilename();
   	       if (filename != null) {
   	           File songFile = new File(filename);
                	String artist = song.getArtistsDescription();
                	if ((artist == null) || (artist.equals("")))
                	    artist = "__noartist";
                	char firstLetter = artist.charAt(0);
                	String directory1 = String.valueOf(firstLetter).toLowerCase();
                	if (!Character.isLetter(firstLetter))
                	    directory1 = "__other";

                	File dir1 = new File(rootDir.getAbsolutePath() + FileUtil.getFileSeperator() + directory1);
                	if (!dir1.exists())
                	    dir1.mkdirs();
                	File backup_dir1 = null;
                	if (backupDir != null) {
                	    backup_dir1 = new File(backupDir.getAbsolutePath() + FileUtil.getFileSeperator() + directory1);
                    	if (!backup_dir1.exists())
                    	    backup_dir1.mkdirs();
                	}

                	String directory2 = artist;
                 directory2 = StringUtil.makeValidFilename(directory2);
                 File dir2 = new File(dir1.getAbsolutePath() + FileUtil.getFileSeperator() + directory2);
                 if (!dir2.exists())
                     dir2.mkdirs();
                 File backup_dir2 = null;
                 if (backupDir != null) {
                     backup_dir2 = new File(backup_dir1.getAbsolutePath() + FileUtil.getFileSeperator() + directory2);
                	if (!backup_dir2.exists())
                	    backup_dir2.mkdirs();
                 }

                 String album = song.getReleaseTitle();
                 if ((album == null) || (album.equals("")))
                	 album = "__noalbum";
                 else {
                	 Vector<ReleaseRecord> release = song.getReleases();
                	 if ((release != null) && (release.size() > 0)) {
                         String year = release.get(0).getOriginalYearReleasedAsString();
                         if ((year!=null) && !year.equals("")) {
                             album += " [" + year + "]";
                         }
                	 }
                }
                String directory3 = album;
                directory3 = StringUtil.makeValidFilename(directory3);

                 File dir3 = new File(dir2.getAbsolutePath() + FileUtil.getFileSeperator() + directory3);
                 if (!dir3.exists())
                     dir3.mkdirs();
                 File backup_dir3 = null;
                 if (backupDir != null) {
                     backup_dir3 = new File(backup_dir2.getAbsolutePath() + FileUtil.getFileSeperator() + directory3);
                	if (!backup_dir3.exists())
                	    backup_dir3.mkdirs();
                 }

                 String songFilename = FileUtil.getFilenameMinusDirectory(filename);
                 if (!songFile.exists())
                	 songFilename = RenameTask.getSongFilename(song);

                 String newFilePath = StringUtil.checkFullPathLength(FileUtil.correctPathSeperators(dir3.getAbsolutePath() + FileUtil.getFileSeperator() + songFilename));
                 String newBackupFilePath = null;
                 if (backupDir != null) {
                     newBackupFilePath = StringUtil.checkFullPathLength(FileUtil.correctPathSeperators(backup_dir3.getAbsolutePath() + FileUtil.getFileSeperator() + songFilename));
                 }

                 if (log.isTraceEnabled()) log.trace("organizeSong(): new path=" + newFilePath + ", for song=" + song);

                 File newFile = new File(newFilePath);
                 File newBackupFile = null;
                 if (backupDir != null) {
                     newBackupFile = new File(newBackupFilePath);
                 }
                 if (songFile.exists()) {
                     try {
                         if ((newBackupFile != null) && !newBackupFile.equals(new File(filename))) {
                             FileUtil.copy(filename, newBackupFilePath);
                         }
                     } catch (Exception e) {
                         log.error("organizeSong(): error copying song file to backup location", e);
                             success = false;
                      }
                     if (!newFile.equals(new File(filename))) {

 	  	                FileUtil.copy(filename, newFilePath);
 	  	                song.setSongFilename(newFilePath);

 	  	                if (deleteOld) {
 	  	                    try {
 	  	                    	FileLockManager.startFileWrite(filename);
		 	  	                File oldFile = new File(filename);
		 	  	                File oldDirectory = new File(FileUtil.getDirectoryFromFilename(filename));
		 	  	                oldFile.delete();
		 	  	                File[] oldDirFiles = oldDirectory.listFiles();
		 	  	                while ((oldDirFiles == null) || (oldDirFiles.length == 0)) {
		 	  	                    oldDirectory.delete();
		 	  	                    oldDirectory = oldDirectory.getParentFile();
		 	  	                    if (oldDirectory == null)
		 	  	                        oldDirFiles = null;
		 	  	                    else
		 	  	                        oldDirFiles = oldDirectory.listFiles();
		 	  	                }
                             } catch (Exception e) {
                                  log.error("organizeSong(): error removing old song files", e);
                                  success = false;
                             } finally {
                            	 FileLockManager.endFileWrite(filename);
                             }
 	  	                }

 	  	                song.save();

   	                }

   	           } else {
   	               if (newFile.exists()) {
   	                   song.setSongFilename(newFilePath);
   	                   song.save();
   	               }
   	           }

                 /*
                Vector<Image> images = song.getImages();
                if (images != null) {
                    String[] image_filenames = imageSet.getFiles();
                    if (image_filenames != null) {
                        for (int i = 0; i < image_filenames.length; ++i) {
                            String image_filename = image_filenames[i];
                            File image_file = new File(image_filename);
                            String newImageFilename = FileUtil.correctPathSeperators(dir3.getAbsolutePath() + FileUtil.getFileSeperator() + FileUtil.getFilenameMinusDirectory(image_filename));
                            String newBackupImageFilename = null;
                            if (backupDir != null)
                                newBackupImageFilename = FileUtil.correctPathSeperators(backup_dir3.getAbsolutePath() + FileUtil.getFileSeperator() + FileUtil.getFilenameMinusDirectory(image_filename));
                            File new_image_file = new File(newImageFilename);
                            File new_backup_image_file = null;
                            if (backupDir != null)
                                new_backup_image_file = new File(newBackupImageFilename);
                            if (image_file.exists()) {
                                String image_directory = FileUtil.getDirectoryFromFilename(image_filename);
                                File image_dir = new File(image_directory);
                                if (!dir3.equals(image_dir) && !newImageFilename.equals(image_filename)) {
                                    if (log.isTraceEnabled()) log.trace("organizeSong(): new image path=" + newImageFilename + ", from=" + image_filename);

                                    try {
                                        try {
	                                       if (backupDir != null)
	                                           FileUtil.copy(image_filename, newBackupImageFilename);
                                        } catch (Exception e) {
                                           log.error("organizeSong(): error copying album cover to backup location", e);
 	                                          success = false;
                                        }
 	                                   FileUtil.copy(image_filename, newImageFilename);
 	                                   image_filenames[i] = newImageFilename;

 	                                   if (deleteOld) {
 	                                       try {
		 	                                   File oldFile = new File(image_filename);
		 	                                   File oldDirectory = new File(FileUtil.getDirectoryFromFilename(image_filename));
		 	                                   oldFile.delete();
		 	                                   File[] oldDirFiles = oldDirectory.listFiles();
		 	                                   while ((oldDirFiles == null) || (oldDirFiles.length == 0)) {
		 	                                       oldDirectory.delete();
		 	                                       oldDirectory = oldDirectory.getParentFile();
		 	                                       if (oldDirectory == null)
		 	                                           oldDirFiles = null;
		 	                                       else
		 	                                           oldDirFiles = oldDirectory.listFiles();
		 	                                   }
 	                                       } catch (Exception e) {
 	                                           log.error("organizeSong(): error removing old album covers", e);
 	                                          success = false;
 	                                       }
 	                                   }

                                    } catch (Exception e) {
                                        log.error("organizeSong(): error Exception", e);
                                    }
                                }
                            } else {
                                if (new_image_file.exists()) {
                                    image_filenames[i] = newImageFilename;
                                }
                            }
                        }
                    }
                    imageSet.setFiles(image_filenames);
                }
                */

                success = true;
   	       }

   	   } catch (Exception e) {
   	       log.error("organizeSong(): error organizing song=" + song, e);
   	    success = false;
   	   }
   	   if (success) {
   		   for (int i = 0; i < listeners.size(); ++i)
   			   listeners.get(i).songOrganized(song);
   	   }
   	   return success;
   	}

}
