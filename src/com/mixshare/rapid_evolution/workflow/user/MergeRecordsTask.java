package com.mixshare.rapid_evolution.workflow.user;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.filter.playlist.PlaylistIdentifier;
import com.mixshare.rapid_evolution.data.identifier.filter.style.StyleIdentifier;
import com.mixshare.rapid_evolution.data.identifier.filter.tag.TagIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.artist.ArtistIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.label.LabelIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.release.ReleaseIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.song.MixoutIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.song.SongIdentifier;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.workflow.CommonTask;
import com.mixshare.rapid_evolution.workflow.Task;

public class MergeRecordsTask extends CommonTask {

	static private Logger log = Logger.getLogger(MergeRecordsTask.class);
    static private final long serialVersionUID = 0L;    	
	
	////////////
	// FIELDS //
	////////////
	
	private Record primaryRecord;
	private Vector<Record> mergedRecords;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public MergeRecordsTask(Record primaryRecord, Vector<Record> mergedRecords) {
		this.primaryRecord = primaryRecord;
		this.mergedRecords = mergedRecords;
	}
	public MergeRecordsTask(Record primaryRecord, Record mergedRecord) {
		this.primaryRecord = primaryRecord;
		mergedRecords = new Vector<Record>(1);
		mergedRecords.add(mergedRecord);		
	}
	
	/////////////
	// METHODS //
	/////////////
	
	public void execute() {
		try {
			if (log.isDebugEnabled())
				log.debug("run(): merging primaryRecord=" + primaryRecord + ", mergedRecords=" + mergedRecords);	    						
			Profile primaryProfile = Database.getProfile(primaryRecord.getIdentifier());
			if (primaryProfile != null) {
				int totalMergedRecords = mergedRecords.size();
				int numMerged = 0;
	    		for (Record mergeRecord : mergedRecords) {
	            	if (RapidEvolution3.isTerminated || isCancelled())
	            		return;    			
	    			if (!primaryRecord.equals(mergeRecord)) {
	    				Profile mergedProfile = Database.getProfile(mergeRecord.getIdentifier());
	    				if (mergedProfile == null) {
	    					if (mergeRecord.getIdentifier() instanceof ArtistIdentifier)
	    						mergedProfile = Database.getArtistIndex().getProfile(mergeRecord.getUniqueId());
	    					else if (mergeRecord.getIdentifier() instanceof LabelIdentifier)
	    						mergedProfile = Database.getLabelIndex().getProfile(mergeRecord.getUniqueId());
	    					else if (mergeRecord.getIdentifier() instanceof ReleaseIdentifier)
	    						mergedProfile = Database.getReleaseIndex().getProfile(mergeRecord.getUniqueId());
	    					else if (mergeRecord.getIdentifier() instanceof SongIdentifier)
	    						mergedProfile = Database.getSongIndex().getProfile(mergeRecord.getUniqueId());
	    					else if (mergeRecord.getIdentifier() instanceof StyleIdentifier)
	    						mergedProfile = Database.getStyleIndex().getProfile(mergeRecord.getUniqueId());
	    					else if (mergeRecord.getIdentifier() instanceof TagIdentifier)
	    						mergedProfile = Database.getTagIndex().getProfile(mergeRecord.getUniqueId());
	    					else if (mergeRecord.getIdentifier() instanceof PlaylistIdentifier)
	    						mergedProfile = Database.getPlaylistIndex().getProfile(mergeRecord.getUniqueId());
	    					else if (mergeRecord.getIdentifier() instanceof MixoutIdentifier)
	    						mergedProfile = Database.getMixoutIndex().getProfile(mergeRecord.getUniqueId());
	    				}
	    				if (mergedProfile != null)
	    					Database.mergeProfiles(primaryProfile, mergedProfile);	    				
	    			}
	    			++numMerged;
	    			setProgress(((float)numMerged) / totalMergedRecords); 
	    		}
			}
		} catch (Exception e) {
			log.error("run(): error", e);
		}
	}
	
	public int getTaskPriority() { return Task.DEFAULT_TASK_PRIORITY; }
	
	public Object getResult() { return null; }

	public String toString() { return "Merging records " + mergedRecords; }
	
}
