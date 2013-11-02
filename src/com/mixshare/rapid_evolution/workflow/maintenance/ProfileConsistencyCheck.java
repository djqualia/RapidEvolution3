package com.mixshare.rapid_evolution.workflow.maintenance;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.index.Index;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.workflow.CommonTask;

public class ProfileConsistencyCheck extends CommonTask {

	static private Logger log = Logger.getLogger(ProfileConsistencyCheck.class);
    static private final long serialVersionUID = 0L;    	
		
	public String toString() {
		return "Profile consistency check";
	}
    
	public void execute() {
		try {			
			log.info("ProfileConsistencyCheck(): started...");
			
			for (Index index : Database.getAllIndexes()) {
				log.info("execute(): processing index=" + index);
				for (int id : index.getIds()) {
					if (RapidEvolution3.isTerminated || isCancelled())
						return;
					Profile profile = index.getProfile(id);
					if (profile == null) {
						try {
							Record record = index.getRecord(id);
							if (record != null) {
								log.warn("execute(): couldn't load profile for record=" + record + ", deleting...");
								index.delete(id);
							} else {
								log.warn("execute(): no record/profile (???) for id=" + id);
								index.delete(id);
							}
						} catch (Exception e) {
							log.error("execute(): error removing id=" + id + ", index=" + index);
						}
					}
				}
			}
			
		} catch (Exception e) {		
			log.error("execute(): error", e);
		}
		log.info("ProfileConsistencyCheck(): done");
	}

	public boolean isIndefiniteTask() { return true; }
	
}
