package com.mixshare.rapid_evolution.workflow.maintenance;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.ui.dialogs.taskprogress.TaskProgressLauncher;
import com.mixshare.rapid_evolution.util.StringUtil;
import com.mixshare.rapid_evolution.workflow.CommonTask;
import com.trolltech.qt.gui.QApplication;

public class FileAssociatorTask extends CommonTask implements DataConstants {
	
    static private Logger log = Logger.getLogger(FileAssociatorTask.class);
    static private final long serialVersionUID = 0L;    	

    private Collection<String> filenames;
    private Vector<SongRecord> songs;
    
	public FileAssociatorTask(Collection<String> filenames, Vector<SongRecord> songs) { // filenames could include directories
    	this.filenames = filenames;
    	this.songs = songs;
    }
    
    public String toString() { return "Associating " + StringUtil.getTruncatedDescription(filenames); }
    
    public void execute() {
        try {
    		QApplication.invokeLater(new TaskProgressLauncher(this));        	
    		
    		if (filenames.size() == songs.size()) {
    			Iterator<String> fileIter = filenames.iterator();
    			for (int s = 0; s < filenames.size(); ++s) {
    				if (RapidEvolution3.isTerminated || isCancelled())
    					return;
    				String filename = fileIter.next();
    				SongRecord song = songs.get(s);
    				song.setSongFilename(filename);
    				song.update();
    				setProgress(((float)s + 1) / filenames.size());
    			}
    		}
        } catch (Exception e) {
            log.error("execute(): error importing files=" + filenames, e);
        }
    }
    
    public int getTaskPriority() { return RE3Properties.getInt("default_task_priority"); }
    
    public Object getResult() { return null; }

}
