package com.mixshare.rapid_evolution.data.record.search.song;

import java.io.Serializable;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.search.release.ReleaseIdentifier;
import com.mixshare.rapid_evolution.data.record.search.release.ReleaseRecord;

public class ReleaseInstance implements Serializable, Comparable<ReleaseInstance> {

    static private final long serialVersionUID = 0L;    
    static private Logger log = Logger.getLogger(ReleaseInstance.class);
    
    ////////////
    // FIELDS //
    ////////////
    
    private int releaseId;
    private String track;
    
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
    public ReleaseInstance() { }
    public ReleaseInstance(int releaseId, String track) {
    	this.releaseId = releaseId;
    	this.track = track;
    }
    
    /////////////
    // GETTERS //
    /////////////
    
    public ReleaseRecord getRelease() { return (ReleaseRecord)Database.getReleaseIndex().getRecord(releaseId); }
	public int getReleaseId() { return releaseId; }
	public int getActualReleaseId() { return Database.getReleaseIndex().getReleaseRecord(releaseId).getUniqueId(); }
    public ReleaseIdentifier getReleaseIdentifier() { return (ReleaseIdentifier)Database.getReleaseIndex().getIdentifierFromUniqueId(releaseId); }

	public String getTrack() { return track; }
	
	/////////////
	// SETTERS //
	/////////////
	
	public void setReleaseId(int releaseId) { this.releaseId = releaseId; }
	public void setTrack(String track) { this.track = track; }
	
	/////////////
	// METHODS //
	/////////////
    
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append(Database.getReleaseIndex().getIdentifierFromUniqueId(releaseId).toString());
		result.append("  [");
		result.append(track);
		result.append("]");
		return result.toString();
	}
	
	public boolean equals(Object o) {
		if (o instanceof ReleaseInstance) {
			ReleaseInstance r = (ReleaseInstance)o;
			if ((releaseId == r.releaseId) && track.equalsIgnoreCase(r.track))
				return true;
		}
		return false;
	}
	
	public int hashCode() {
		return toString().hashCode();
	}
	
	public int compareTo(ReleaseInstance r) {
		ReleaseIdentifier releaseId1 = (ReleaseIdentifier)Database.getReleaseIndex().getIdentifierFromUniqueId(releaseId);
		ReleaseIdentifier releaseId2 = (ReleaseIdentifier)Database.getReleaseIndex().getIdentifierFromUniqueId(r.releaseId);
		if ((releaseId1 == null) && (releaseId2 == null))
			return 0;
		if (releaseId1 == null)
			return 1;
		if (releaseId2 == null)
			return -1;
		int idCmp = releaseId1.compareTo(releaseId2);
		if (idCmp != 0)
			return idCmp;
		return track.compareToIgnoreCase(r.track);
	}
	
}
