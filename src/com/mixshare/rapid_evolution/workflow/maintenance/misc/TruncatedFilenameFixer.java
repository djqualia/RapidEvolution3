package com.mixshare.rapid_evolution.workflow.maintenance.misc;

import java.io.File;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.util.FileUtil;
import com.mixshare.rapid_evolution.workflow.CommonTask;

public class TruncatedFilenameFixer extends CommonTask {

	static private Logger log = Logger.getLogger(TruncatedFilenameFixer.class);
    static private final long serialVersionUID = 0L;    	
		
	public String toString() {
		return "Fixing Truncated Filename References";
	}
    
	public void execute() {
		try {
			log.info("execute(): starting...");
			
			int filenamesFixed = 0;
			int missingFilenames = 0;
			Vector<SongRecord> missingFilenameSongs = new Vector<SongRecord>();
			for (int songId : Database.getSongIndex().getIds()) {
				if (RapidEvolution3.isTerminated || isCancelled())
					return;
				SongRecord song = Database.getSongIndex().getSongRecord(songId);
				if (song != null) {
					String filename = song.getSongFilename();	
					if ((filename != null) && (filename.length() > 0)) {
						File file = new File(filename);
						if (!file.exists()) {
							String bareFilename = FileUtil.getFilenameMinusDirectoryMinusExtension(file.getAbsolutePath());
							String directory = FileUtil.getDirectoryFromFilename(filename);
							File[] directoryFiles = new File(directory).listFiles();
							if (directoryFiles != null) {
								int possibleMatches = 0;
								String possibleMatch = null;
								for (File directoryFile : directoryFiles) {
									String bareDirectoryFilename = FileUtil.getFilenameMinusDirectoryMinusExtension(directoryFile.getAbsolutePath());
									if (bareDirectoryFilename.startsWith(bareFilename)) {
										++possibleMatches;	
										possibleMatch = directoryFile.getAbsolutePath();
									}
								}
								if (possibleMatches == 1) {
									song.setSongFilename(possibleMatch);
									++filenamesFixed;
								} else {
									missingFilenameSongs.add(song);
									++missingFilenames;
								}
							}
						}
					}
				}
			}
						
			log.info("execute(): total # filenamesFixed=" + filenamesFixed);
			log.info("execute(): total # missingFilenames=" + missingFilenames);
			log.info("execute(): missingFilenameSongs=" + missingFilenameSongs);
			
		} catch (Exception e) {
			log.error("execute(): error", e);
		}
		
		log.info("execute(): finished");		
	}
	
	public boolean isIndefiniteTask() { return true; }
	
}
