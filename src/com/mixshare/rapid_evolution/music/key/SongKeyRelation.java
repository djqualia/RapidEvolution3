package com.mixshare.rapid_evolution.music.key;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.music.bpm.Bpm;

/**
 * A song's key relation should be considered in 2 ways, with and without the use of key/pitch lock.
 * By doing this, more harmonically compatible songs can be found.
 */
public class SongKeyRelation {

    static private Logger log = Logger.getLogger(SongKeyRelation.class);
    
    static public boolean DISABLE_KEYLOCK_FUNCTIONALITY = false;
    static public boolean EXCLUDE_MATCH_WITHOUT_KEYLOCK = false;
    
    ////////////
    // FIELDS //
    ////////////
    
    private KeyRelation withKeylock = null;
    private KeyRelation withoutKeylock = null;
    
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
    public SongKeyRelation() { }

    /////////////
    // GETTERS //
    /////////////
    
    public KeyRelation getRelationWithKeylock() {
        if (withKeylock == null)
            return KeyRelation.INVALID_RELATION;
        return withKeylock;
    }
    public KeyRelation getRelationWithoutKeylock() {
        if (withoutKeylock == null)
            return KeyRelation.INVALID_RELATION;
        return withoutKeylock;
    }

    public KeyRelation getBestKeyRelation() {
        if ((withKeylock != null) && (withoutKeylock != null)) {
            if (EXCLUDE_MATCH_WITHOUT_KEYLOCK ||
                (Math.abs(withKeylock.getDifference()) < Math.abs(withoutKeylock.getDifference())))
                return withKeylock;
            else
                return withoutKeylock;
        } else if (withoutKeylock != null) {
            return withoutKeylock;
        } else {
            return withKeylock;
        }        
    }
 
    public boolean isBestRelationWithKeylock() {
        if ((withKeylock != null) && (withoutKeylock != null)) {
            if (Math.abs(withKeylock.getDifference()) < Math.abs(withoutKeylock.getDifference()))
                return true;
        }
        return false;
    }
    
    public boolean isCompatibleWithKeylock() {
        return ((withKeylock != null) && withKeylock.isCompatible() && !DISABLE_KEYLOCK_FUNCTIONALITY);
    }

    public boolean isCompatibleWithoutKeylock() {
        return ((withoutKeylock != null) && withoutKeylock.isCompatible() && (!EXCLUDE_MATCH_WITHOUT_KEYLOCK || DISABLE_KEYLOCK_FUNCTIONALITY));
    }
    
    public boolean isCompatible() {
        return (isCompatibleWithKeylock() || isCompatibleWithoutKeylock());
    }
    
    public String getRecommendedKeyLockSetting() {
        if (((withKeylock == null) || !withKeylock.isValid()) && ((withoutKeylock == null) || !withoutKeylock.isValid())) return "";
        if (!DISABLE_KEYLOCK_FUNCTIONALITY) {
            if (EXCLUDE_MATCH_WITHOUT_KEYLOCK ||
                (getRelationWithKeylock().isValid() && getRelationWithKeylock().isCompatible() && (!getRelationWithoutKeylock().isValid() || (Math.abs(getRelationWithKeylock().getDifference()) < Math.abs(getRelationWithoutKeylock().getDifference()))))) {
                return "yes";
            }
        }
        if (getRelationWithoutKeylock().isValid() && getRelationWithoutKeylock().isCompatible()) {
            return "no";
        }
        return "***";        
    }
    
    /////////////
    // SETTERS //
    /////////////
    
    public void setRelationWithKeylock(KeyRelation relation) { withKeylock = relation; }        
    public void setRelationWithoutKeylock(KeyRelation relation) { withoutKeylock = relation; }
    
    ///////////////////
    // STATIC METHOD //
    ///////////////////
    
    static public SongKeyRelation getSongKeyRelation(SongRecord sourceSong, Bpm targetBpm, Key targetKey) {
    	return getSongKeyRelation(sourceSong.getBpmStart(), sourceSong.getStartKey(), targetBpm, targetKey);
    }

    static public SongKeyRelation getSongKeyRelation(Bpm sourceBpm, Key sourceKey, Bpm targetBpm, Key targetKey) {
        SongKeyRelation result = new SongKeyRelation();        
        KeyRelation relationWithoutKeylock = KeyRelation.INVALID_RELATION;
        if (sourceBpm.isValid() && targetBpm.isValid())
        	relationWithoutKeylock = Key.getClosestKeyRelation(sourceBpm.getBpmValue(), sourceKey, targetBpm.getBpmValue(), targetKey);
        result.setRelationWithoutKeylock(relationWithoutKeylock);
        if (!DISABLE_KEYLOCK_FUNCTIONALITY) {
            KeyRelation relationWithKeylock = KeyRelation.INVALID_RELATION;
            if (targetBpm.isValid())
            	relationWithKeylock = Key.getClosestKeyRelation(targetBpm.getBpmValue(), sourceKey, targetBpm.getBpmValue(), targetKey);
            result.setRelationWithKeylock(relationWithKeylock);
        }
        return result;
    }
    
    ///////////////////////
    // FOR SERIALIZATION //
    ///////////////////////
    
	public KeyRelation getWithKeylock() {
		return withKeylock;
	}

	public void setWithKeylock(KeyRelation withKeylock) {
		this.withKeylock = withKeylock;
	}

	public KeyRelation getWithoutKeylock() {
		return withoutKeylock;
	}

	public void setWithoutKeylock(KeyRelation withoutKeylock) {
		this.withoutKeylock = withoutKeylock;
	}    
     
}
