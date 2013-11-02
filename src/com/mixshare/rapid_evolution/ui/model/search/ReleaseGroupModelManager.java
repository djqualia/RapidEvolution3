package com.mixshare.rapid_evolution.ui.model.search;


import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.record.search.ReleaseGroupRecord;
import com.mixshare.rapid_evolution.ui.model.column.comparables.SmartInteger;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

abstract public class ReleaseGroupModelManager extends SongGroupModelManager {

    static private Logger log = Logger.getLogger(ReleaseGroupModelManager.class);    
    
    //////////////////
    // CONSTRUCTORS //
    //////////////////
    
    public ReleaseGroupModelManager() { super(); }
	public ReleaseGroupModelManager(LineReader lineReader) {
		super(lineReader);
		int version = Integer.parseInt(lineReader.getNextLine());
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public Object getSourceData(short columnId, Object record) {
		ReleaseGroupRecord releaseGroupRecord = (ReleaseGroupRecord)record;
		if (columnId == COLUMN_NUM_RELEASES.getColumnId())
			return new SmartInteger(releaseGroupRecord.getNumReleases());
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