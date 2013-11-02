package com.mixshare.rapid_evolution.workflow.user;

import java.io.File;
import java.text.DecimalFormat;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.audio.qt.QTUtil;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.ui.dialogs.taskprogress.TaskProgressLauncher;
import com.mixshare.rapid_evolution.util.FileUtil;
import com.mixshare.rapid_evolution.util.StringUtil;
import com.mixshare.rapid_evolution.util.io.FileLockManager;
import com.mixshare.rapid_evolution.workflow.CommonTask;
import com.mixshare.rapid_evolution.workflow.Task;
import com.mixshare.rapid_evolution.workflow.event.OrganizeSongListener;
import com.trolltech.qt.gui.QApplication;

public class RenameTask extends CommonTask {
	
	static private Logger log = Logger.getLogger(RenameTask.class);
    static private final long serialVersionUID = 0L;    	
    
    static public DecimalFormat decimalFormat = new DecimalFormat("###.#");
    
    static private Vector<OrganizeSongListener> listeners = new Vector<OrganizeSongListener>();
    
    private Vector<SongRecord> songs;
		
	public RenameTask(Vector<SongRecord> songs) {
		this.songs = songs;
	}
				
	public void execute() {
		try {
			int totalSongs = songs.size();
			int numSongs = 0;
    		String pattern = RE3Properties.getProperty("organize_music_rename_pattern");
    		if (log.isDebugEnabled())
    			log.debug("execute(): renaming songs=" + songs);
        	if (RE3Properties.getBoolean("show_progress_bars") && !RE3Properties.getBoolean("server_mode"))
        		QApplication.invokeLater(new TaskProgressLauncher(this));    		
			for (SongRecord song : songs) {
				if (RapidEvolution3.isTerminated || isCancelled())
					return;
				SongProfile songProfile = Database.getSongIndex().getSongProfile(song.getUniqueId());
				if (songProfile != null)
	   	       		renameSongFileName(songProfile, pattern);
				++numSongs;
				setProgress(((float)numSongs) / totalSongs);
			}
		} catch (Exception e) {
			log.error("execute(): error", e);
		}
	}
	
	public int getTaskPriority() { return Task.DEFAULT_TASK_PRIORITY; }
	
	public Object getResult() { return null; }
	
	public String toString() { return "Renaming songs " + StringUtil.getTruncatedDescription(songs); }
	
    static public boolean renameSongFileName(SongProfile song, String pattern) {
        boolean success = false;
        if (song.getSongFilename() != null) {
            
            String newfilename = StringUtil.checkFullPathLength(FileUtil.correctPathSeperators(getSongFilename(song, pattern)));
            
            if (!newfilename.equals(song.getSongFilename())) {
                if (log.isDebugEnabled()) log.debug("renameSongFileName(): new filename: " + newfilename + ", song: " + song);
                
                
                // rename the song's file
                String songFilename = song.getSongFilename();
                try {
                	FileLockManager.startFileWrite(songFilename);
                    
                    if (log.isTraceEnabled()) log.trace("renameSongFileName(): existing filename=" + song.getSongFilename());
                    String existingFileName = FileUtil.correctPathSeperators(song.getSongFilename());
                    File existingfile = new File(existingFileName);
                                        
                    if ((newfilename.equalsIgnoreCase(existingFileName)) && !newfilename.equals(existingFileName)) {
                        if (log.isTraceEnabled()) log.trace("renameSongFileName(): case change only detected");
                        // changing case of file name
                        File tmp = new File(newfilename + ".rename_temp");
                        success = existingfile.renameTo(tmp);
                        if (!success) {
                        	if (QTUtil.isQuickTimeSupported()) {
                        		log.warn("renameSongFileName(): couldn't rename song, closing QuickTime...");
                        		QTUtil.closeQT();
                        	}
                        	success = existingfile.renameTo(tmp);
                        }                        
                        if (success) {
                            File newfile = new File(newfilename);
                            success = tmp.renameTo(newfile);
                        } else {
                            if (log.isTraceEnabled()) log.trace("renameSongFileName(): could not rename to .rename_temp");
                        }
                    } else if (existingfile.exists()) {
                        if (log.isTraceEnabled()) log.trace("renameSongFileName(): new filename detected");
                        File newfile = new File(newfilename);
                        if (!newfile.exists()) {
                            success = existingfile.renameTo(newfile);
                            if (!success) {
                            	if (QTUtil.isQuickTimeSupported()) {
                            		log.warn("renameSongFileName(): couldn't rename song, closing QuickTime...");
                            		QTUtil.closeQT();
                            	}
                            	success = existingfile.renameTo(newfile);
                            }
                        } else {
                            if (log.isTraceEnabled()) log.trace("renameSongFileName(): newfile already exists!");
                        }
                    }
                    
                    if (success) {                        
                        File asd_file = new File(song.getSongFilename() + ".asd");
                        if (asd_file.exists()) {
                            File new_asd_file = new File(newfilename + ".asd");
                            asd_file.renameTo(new_asd_file);
                        }
                        song.setSongFilename(newfilename);
                        song.getSongRecord().update();
                    }
                    
                } catch (Exception e) {
                    log.error("renameSongFileName(): error", e);
                } finally {
                	FileLockManager.endFileWrite(songFilename);
                }
                
            }
            
        }
        return success;
    }
    
    static public String getSongFilename(SongProfile song) {
    	return getSongFilename(song, RE3Properties.getProperty("organize_music_rename_pattern"), false);
    }
    static private String getSongFilename(SongProfile song, String pattern) {
    	return getSongFilename(song, pattern, true);
    }
    static private String getSongFilename(SongProfile song, String pattern, boolean includeDirectory) {
        String filename = song.getSongFilename();
        try {        
            String directory = FileUtil.getDirectoryFromFilename(song.getSongFilename());
            String file = FileUtil.getFilenameMinusDirectory(song.getSongFilename());            
            
            StringBuffer newname = new StringBuffer();
            StringTokenizer tokenizer = new StringTokenizer(pattern, "{");
            while (tokenizer.hasMoreElements()) {
                String token = tokenizer.nextToken();
                String suffix = "";
                if (token.indexOf("}") >= 0) {
                    if (token.indexOf("}") + 1 != token.length())
                        suffix = token.substring(token.indexOf("}") + 1);
                    token = token.substring(0, token.indexOf("}"));
                }
                if (token.endsWith("}")) token = token.substring(0, token.length() - 1);
                int first_token_start = token.indexOf("%");                
                int first_token_end = token.indexOf("%", first_token_start + 1);
                String variable = token.substring(first_token_start + 1, first_token_end);
                String vartext = getVariableText(song, variable);
                if (!vartext.equals("")) {
                    if (newname.toString().equals("")) {
                        if (first_token_start > 0) {
                            if (token.charAt(first_token_start - 1) == '[') newname.append("[");
                        }
                        newname.append(vartext);
                        newname.append(token.substring(first_token_end + 1));                        
                    } else {
                        newname.append(token.substring(0, first_token_start));
                        newname.append(vartext);
                        newname.append(token.substring(first_token_end + 1));
                    }
                }
                newname.append(suffix);
            }
            int extension_index = file.lastIndexOf(".");
            if (extension_index >= 0)
                newname.append(file.substring(extension_index));
            
            String new_filename = newname.toString();
            
            new_filename = StringUtil.makeValidFilename(new_filename);
            
            if (includeDirectory)
            	return directory + new_filename;
            return new_filename;
        } catch (Exception e) {
            log.error("getSongFilename(): error", e);
            return song.getSongFilename();
        }
    }
    
    static private String getVariableText(SongProfile song, String variable) {
        if (variable.equalsIgnoreCase("artist")) return song.getArtistsDescription();
        if (variable.equalsIgnoreCase("album")) return song.getReleaseTitle();
        if (variable.equalsIgnoreCase("release")) return song.getReleaseTitle();
        if (variable.equalsIgnoreCase("track")) return song.getTrack();
        if (variable.equalsIgnoreCase("title")) return song.getTitle();
        if (variable.equalsIgnoreCase("remix")) return song.getRemix();
        if (variable.equalsIgnoreCase("duration")) return song.getDuration().toString();
        if (variable.equalsIgnoreCase("key")) return song.getStartKey().getPreferredKeyNotation();
        if (variable.equalsIgnoreCase("keycode")) return song.getStartKey().getKeyCode().toFileFriendlyString();
        if (variable.equalsIgnoreCase("filename")) return getFileNameNoDirectoryNoExtension(song);
        if (variable.equalsIgnoreCase("bpm")) {
            if (song.getBpmStart().isValid())
                return myDecimalFormat(decimalFormat.format(song.getStartBpm()));
        }
        if (variable.equalsIgnoreCase("bpmInt")) {
        	if (song.getBpmStart().isValid())
        		return String.valueOf((int)song.getStartBpm());
        }
        return "";
    }

    static private String myDecimalFormat(String bpm) {
        if (bpm.indexOf(".") >= 0) return bpm;
        return bpm + ".0";
    }
    
    static private String getFileNameNoDirectoryNoExtension(SongProfile song) {
    	String filename = FileUtil.getFilenameMinusDirectory(song.getSongFilename());
    	int extensionIndex = filename.lastIndexOf(".");
    	if (extensionIndex >= 0)
    		return filename.substring(0, extensionIndex);
    	return filename;
    }
    
}
