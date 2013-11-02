package com.mixshare.rapid_evolution.ui.model.search;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.record.search.SongGroupRecord;
import com.mixshare.rapid_evolution.ui.model.column.comparables.SmartInteger;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

abstract public class SongGroupModelManager extends SearchModelManager {

    static private Logger log = Logger.getLogger(SongGroupModelManager.class);    
        	
    //////////////////
    // CONSTRUCTORS //
    //////////////////
    
    public SongGroupModelManager() { super(); }
	public SongGroupModelManager(LineReader lineReader) {
		super(lineReader);
		int version = Integer.parseInt(lineReader.getNextLine());
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public Object getSourceData(short columnId, Object record) {
		SongGroupRecord songGroupRecord = (SongGroupRecord)record;
		if (columnId == COLUMN_AVERAGE_BEAT_INTENSITY_VALUE.getColumnId())
			return songGroupRecord.getAvgBeatIntensity();
		if (columnId == COLUMN_BEAT_INTENSITY_VARIANCE_VALUE.getColumnId())
			return songGroupRecord.getBeatIntensityVariance();
		if (columnId == COLUMN_AVERAGE_BEAT_INTENSITY_DESCRIPTION.getColumnId())
			return songGroupRecord.getAvgBeatIntensityDescription();			
		if (columnId == COLUMN_BEAT_INTENSITY_VARIANCE_DESCRIPTION.getColumnId())
			return songGroupRecord.getBeatIntensityVarianceDescription();		
		if (columnId == COLUMN_NUM_SONGS.getColumnId())
			return new SmartInteger(songGroupRecord.getNumSongs());
		return super.getSourceData(columnId, record);		
	}
	
	/////////////
	// METHODS //
	/////////////
	
	public void write(LineWriter writer) {
		super.write(writer);
		writer.writeLine(1); // version
	}
	
	
}