package com.mixshare.rapid_evolution.ui.model.profile.search.song;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.music.bpm.Bpm;
import com.mixshare.rapid_evolution.music.bpm.BpmDifference;
import com.mixshare.rapid_evolution.music.key.Key;
import com.mixshare.rapid_evolution.music.key.KeyRelation;
import com.mixshare.rapid_evolution.music.key.SongKeyRelation;
import com.mixshare.rapid_evolution.music.pitch.Cents;
import com.mixshare.rapid_evolution.ui.model.column.comparables.Percentage;
import com.mixshare.rapid_evolution.ui.model.search.song.SongModelManager;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

/**
 * There are several columns that apply to relative songs (i.e. bpm % difference,
 * key relation, etc).  This class renders these columns...
 */
abstract public class RelativeSongModelManager extends SongModelManager {

    static private Logger log = Logger.getLogger(RelativeSongModelManager.class);	
    	
    ////////////
    // FIELDS //
    ////////////
    
    transient protected SongProfile relativeSong;
    transient protected Bpm targetBpm;
    transient protected Key targetKey;
    
    static {
    	try{
    		// in order for the XMLEncoder to skip transient variables, this is needed
    		BeanInfo info = Introspector.getBeanInfo(RelativeSongModelManager.class);
    		PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
    		for (int i = 0; i < propertyDescriptors.length; ++i) {
    			PropertyDescriptor pd = propertyDescriptors[i];
    			if (pd.getName().equals("relativeSong") || pd.getName().equals("targetBpm") || pd.getName().equals("targetKey")) {
    				pd.setValue("transient", Boolean.TRUE);
    			}
    		}
    	} catch (IntrospectionException e) {
    		log.error("static(): error", e);
    	}
    }
    
    //////////////////
    // CONSTRUCTORS //
    //////////////////
    
    public RelativeSongModelManager() { super(); }
	public RelativeSongModelManager(LineReader lineReader) {
		super(lineReader);
		int version = Integer.parseInt(lineReader.getNextLine());
	}		    
    
	/////////////
	// GETTERS //
	/////////////
	
	public Object getSourceData(short columnId, Object record) {
		if (log.isTraceEnabled())
			log.trace("getSourceData(): columnId=" + columnId + ", record=" + record);		
		SongRecord songRecord = (SongRecord)record;
		if (columnId == COLUMN_SIMILARITY.getColumnId())
			return new Percentage(relativeSong.getSimilarity(songRecord));
		if (columnId == COLUMN_BPM_DIFFERENCE.getColumnId()) {
			float targetBpm = getRelativeSong().getEndBpm();
			if (targetBpm == 0.0f)
				targetBpm = getRelativeSong().getStartBpm();
			if ((targetBpm != 0.0f) && songRecord.getBpmStart().isValid())
				return new BpmDifference(songRecord.getBpmStart().getDifference(targetBpm));
			return BpmDifference.INVALID;
		} if (columnId == COLUMN_BPM_SHIFT.getColumnId()) {
			if (targetBpm.isValid() && songRecord.getBpmStart().isValid())
				return new BpmDifference(songRecord.getBpmStart().getDifference(targetBpm));
			return BpmDifference.INVALID;
		} if (columnId == COLUMN_ACTUAL_KEY.getColumnId()) {
			if (targetBpm.isValid() && songRecord.getBpmStart().isValid()) {
				float bpmDifference = songRecord.getBpmStart().getDifference(targetBpm);	          
				return songRecord.getStartKey().getShiftedKeyByBpmDifference(bpmDifference);
			}
			return Key.NO_KEY;
		} if (columnId == COLUMN_ACTUAL_KEYCODE.getColumnId()) {
			if (targetBpm.isValid() && songRecord.getBpmStart().isValid()) {
				float bpmDifference = songRecord.getBpmStart().getDifference(targetBpm);	          
				return songRecord.getStartKey().getShiftedKeyByBpmDifference(bpmDifference).getKeyCode();
			}
			return Key.NO_KEY;
		} if (columnId == COLUMN_KEY_LOCK.getColumnId()) {
            SongKeyRelation relation = SongKeyRelation.getSongKeyRelation(songRecord, targetBpm, targetKey);
			return relation.getRecommendedKeyLockSetting();
		} if (columnId == COLUMN_KEY_CLOSENESS.getColumnId()) {			
			float percentInKey = 0.0f;
			SongKeyRelation keyRelation = SongKeyRelation.getSongKeyRelation(songRecord, targetBpm, targetKey);
			if (keyRelation.isCompatible())
				percentInKey = (0.5f - Math.abs(keyRelation.getBestKeyRelation().getDifference())) * 2.0f;
			return new Percentage(percentInKey);				
		} if (columnId == COLUMN_KEY_RELATION.getColumnId()) {
			SongKeyRelation relation = SongKeyRelation.getSongKeyRelation(songRecord, targetBpm, targetKey);
			return relation.getBestKeyRelation();
		} if (columnId == COLUMN_PITCH_SHIFT.getColumnId()) {
			if (songRecord.getBpmStart().isValid()) {
				KeyRelation relation = SongKeyRelation.getSongKeyRelation(songRecord, targetBpm, targetKey).getBestKeyRelation();
				if (relation.hasDifference())
					return Cents.getCents((float)relation.getDifference());
				return Cents.NO_CENTS;
			}
			return Cents.NO_CENTS;
		}
		return super.getSourceData(columnId, record);
	}
	
	public boolean excludeExternalItems() { return false; }

	public Bpm getTargetBpm() { return targetBpm; }
	public Key getTargetKey() { return targetKey; }

	public SongProfile getRelativeSong() { return relativeSong; }

	public boolean isLazySearchSupported() { return false; }
	
	/////////////
	// SETTERS //
	/////////////
	
	public void setRelativeSong(SongProfile relativeSong) {
		this.relativeSong = relativeSong;
		targetBpm = relativeSong.getBpmEnd();
		if (!targetBpm.isValid())
			targetBpm = relativeSong.getBpmStart();
		targetKey = relativeSong.getEndKey();
		if (!targetKey.isValid())
			targetKey = relativeSong.getStartKey();
	}
	
	public void setTargetBpm(Bpm targetBpm) { this.targetBpm = targetBpm; }	
	public void setTargetKey(Key targetKey) { this.targetKey = targetKey; }

	/////////////
	// METHODS //
	/////////////
	
	public void write(LineWriter writer) {
		super.write(writer);
		writer.writeLine(1); //version
	}
	
	
}