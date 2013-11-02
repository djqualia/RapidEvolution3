package com.mixshare.rapid_evolution.workflow.maintenance;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.Identifier;
import com.mixshare.rapid_evolution.data.identifier.filter.FilterIdentifier;
import com.mixshare.rapid_evolution.data.identifier.filter.playlist.PlaylistIdentifier;
import com.mixshare.rapid_evolution.data.identifier.filter.style.StyleIdentifier;
import com.mixshare.rapid_evolution.data.identifier.filter.tag.TagIdentifier;
import com.mixshare.rapid_evolution.data.index.CommonIndex;
import com.mixshare.rapid_evolution.data.index.Index;
import com.mixshare.rapid_evolution.data.index.imdb.LocalIMDB;
import com.mixshare.rapid_evolution.workflow.CommonTask;

public class IdentifierCleanup extends CommonTask {

	static private Logger log = Logger.getLogger(IdentifierCleanup.class);
    static private final long serialVersionUID = 0L;
	    
	public String toString() {
		return "Cleaning ip identifier indexes";
	}
    
	public void execute() {
		try {
			log.debug("execute(): starting...");
			
			for (Index index : Database.getAllIndexes()) {
				if (RapidEvolution3.isTerminated || isCancelled())
					return;
				
				LocalIMDB localIMDB = (LocalIMDB)((CommonIndex)index).getImdb();
				if (log.isDebugEnabled())
					log.debug("execute(): processing index=" + index);
				
				Vector<Identifier> badIds = new Vector<Identifier>();
				for (Identifier id : localIMDB.getUniqueIdTable().getIdentifierMap().keySet()) {
					int uniqueId = localIMDB.getUniqueIdFromIdentifier(id);
					if (!localIMDB.doesExist(uniqueId) && !isRootFilter(id)) {
						if (log.isDebugEnabled())
							log.debug("execute(): identifier with no matching record=" + id);
						badIds.add(id);
					}
				}
				
				for (Identifier id : badIds) {
					localIMDB.getUniqueIdTable().clearIdentier(id);
				}
			}
			
			log.debug("execute(): finished...");
		} catch (Exception e) {
			log.error("execute(): error", e);			
		}
	}
	
	public Object getResult() { return null; }
	
	public int getTaskPriority() {
		return RE3Properties.getInt("default_task_priority") + 5;
	}
	
	private boolean isRootFilter(Identifier id) {
		if (id instanceof FilterIdentifier) {
			if (id instanceof StyleIdentifier) {
				if (id.toString().equals("***ROOT STYLE***"))
					return true;
			}
			if (id instanceof TagIdentifier) {
				if (id.toString().equals("***ROOT TAG***"))
					return true;				
			}
			if (id instanceof PlaylistIdentifier) {
				if (id.toString().equals("***ROOT PLAYLIST***"))
					return true;
			}				
		}
		return false;
	}

	public boolean isIndefiniteTask() { return true; }
	
}
