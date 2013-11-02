package com.mixshare.rapid_evolution.workflow.importers.mixmeister;

import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.audio.tags.TagManager;
import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.submitted.search.song.SubmittedSong;
import com.mixshare.rapid_evolution.music.bpm.Bpm;
import com.mixshare.rapid_evolution.music.duration.Duration;
import com.mixshare.rapid_evolution.music.key.Key;
import com.mixshare.rapid_evolution.ui.dialogs.taskprogress.TaskProgressLauncher;
import com.mixshare.rapid_evolution.ui.util.FileNameSelectThread;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.util.io.readers.PlainTextLineReader;
import com.mixshare.rapid_evolution.workflow.CommonTask;
import com.mixshare.rapid_evolution.workflow.SandmanThread;
import com.trolltech.qt.gui.QApplication;

public class MixmeisterImportTask extends CommonTask implements DataConstants {
	
    static private Logger log = Logger.getLogger(MixmeisterImportTask.class);
    static private final long serialVersionUID = 0L;    	

    public MixmeisterImportTask() { }
    
    public String toString() { return "Importing From Mixmeister"; }   
    
    public void execute() {
        try {
        	String mixmeisterFilename = null;
    		FileNameSelectThread filenameSelected = new FileNameSelectThread(Translations.get("import_mixmeister_filter_text"), "(*.txt)");        		
    		QApplication.invokeAndWait(filenameSelected);
    		if (filenameSelected.getFilename() != null) {
    			mixmeisterFilename = filenameSelected.getFilename();
    	        if (log.isTraceEnabled())
    	        	log.trace("browseFilename(): selected filename=" + mixmeisterFilename);
    	    }        		
        	if (mixmeisterFilename != null) {        		
            	if (RE3Properties.getBoolean("show_progress_bars"))
            		QApplication.invokeLater(new TaskProgressLauncher(this));
        		if (log.isDebugEnabled())
        			log.debug("execute(): importing mixmeister filename=" + mixmeisterFilename); 
        		
        		PlainTextLineReader lineReader = new PlainTextLineReader(mixmeisterFilename);
        		int numLines = 0;
        		String nextLine = lineReader.getNextLine();
        		while (nextLine != null) {
        			++numLines;
        			nextLine = lineReader.getNextLine();
        		}
        		
        		int linesProcessed = 0;
        		lineReader = new PlainTextLineReader(mixmeisterFilename);
        		nextLine = lineReader.getNextLine();
        		while (nextLine != null) {   
        			if (RapidEvolution3.isTerminated || isCancelled())
        				return;
        			StringTokenizer tokenizer = new StringTokenizer(nextLine, "\t");
        			
        			try {
        				String filename = tokenizer.nextToken();
        				String title = tokenizer.nextToken();
        				String artist = tokenizer.nextToken();
        				String release = tokenizer.nextToken();
        				float bpm = 0.0f;
        				try { bpm = Float.parseFloat(tokenizer.nextToken()); } catch (NumberFormatException e) { }
        				short originalYear = 0;
        				try { originalYear = Short.parseShort(tokenizer.nextToken()); } catch (NumberFormatException e) { }
        				String style = tokenizer.nextToken(); // genre 
        				String duration = tokenizer.nextToken();
        				String key = tokenizer.nextToken();
        				
        				SubmittedSong submittedSong = new SubmittedSong(artist, release, "", title, "");
        				if (!submittedSong.getIdentifier().isValid())
        					submittedSong = TagManager.readTags(filename);
        				if (bpm != 0.0f)
        					submittedSong.setBpm(new Bpm(bpm), Bpm.NO_BPM, (byte)0, DATA_SOURCE_MIXMEISTER);
        				if (originalYear != 0)
        					submittedSong.setOriginalYearReleased(originalYear, DATA_SOURCE_MIXMEISTER);
        				if ((style != null) && (style.length() > 0))
        					submittedSong.addStyleDegreeValue(style, 1.0f, DATA_SOURCE_MIXMEISTER);
        				submittedSong.setDuration(new Duration(duration), DATA_SOURCE_MIXMEISTER);
        				if ((key != null) && (key.length() > 0)) 
        					submittedSong.setKey(Key.getKey(key), Key.NO_KEY, (byte)0, DATA_SOURCE_MIXMEISTER);
        				
        				if (log.isDebugEnabled())
        					log.debug("execute(): adding/updating song=" + submittedSong);
        				Database.getSongIndex().addOrUpdate(submittedSong);
        				
        			} catch (Exception e) {
        				log.error("execute(): error", e);
        			}
        			
        			++linesProcessed;
        			setProgress(((float)linesProcessed) / numLines);
        			nextLine = lineReader.getNextLine();
        		}
        		
        		SandmanThread.wakeUpAllBackgroundTasks();
        	} else {
        		log.warn("execute(): couldn't locate Mixmeister TXT");
        	}
        } catch (Exception e) {
            log.error("execute(): error loading database", e);
        }
    }
    
    public int getTaskPriority() { return RE3Properties.getInt("default_task_priority"); }
    
    public Object getResult() { return null; }

}
