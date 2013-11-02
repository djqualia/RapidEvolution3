package com.mixshare.rapid_evolution.ui.model.search.song;

import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.index.Index;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.search.song.MixoutRecord;
import com.mixshare.rapid_evolution.music.bpm.BpmDifference;
import com.mixshare.rapid_evolution.ui.model.ModelPopulatorInterface;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.column.StaticTypeColumn;
import com.mixshare.rapid_evolution.ui.model.column.comparables.SmartString;
import com.mixshare.rapid_evolution.ui.model.table.RecordTableModelManager;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

/**
 * This for the full table of mixouts, not the song profile mixouts tab!
 */
public class MixoutModelManager extends RecordTableModelManager {
	
	static private Logger log = Logger.getLogger(MixoutModelManager.class);
    static private final long serialVersionUID = 0L;    	
    
    static private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
						
    static public StaticTypeColumn[] ALL_COLUMNS = {
		COLUMN_FROM_SONG_DESCRIPTION.getInstance(true),
		COLUMN_TO_SONG_DESCRIPTION.getInstance(true),
		COLUMN_RATING_STARS.getInstance(true),
		COLUMN_RATING_VALUE.getInstance(false),
		COLUMN_MIXOUT_BPM_DIFF.getInstance(false),
		COLUMN_MIXOUT_TYPE.getInstance(false)
    };
    
    //////////////////
    // CONSTRUCTION //
    //////////////////
    
	public MixoutModelManager() {
		setPrimarySortColumn(COLUMN_SONG_DESCRIPTION.getColumnId());
	}
	public MixoutModelManager(LineReader lineReader) {
		super(lineReader);
		int version = Integer.parseInt(lineReader.getNextLine());
	}			
	
	public void initColumns() {
		sourceColumns.clear();
		for (Column column : ALL_COLUMNS) {
			if (column.getColumnId() == COLUMN_THUMBNAIL_IMAGE.getColumnId())
				column.setHidden(true);
			sourceColumns.add(column);
		}		
	}	
	
	/////////////
	// GETTERS //
	/////////////
	
	public StaticTypeColumn[] getAllStaticColumns() { return ALL_COLUMNS; }
	public String getTypeDescription() { return "Mixout"; }
		
	public Object getSourceData(short columnId, Object record) {
		if (log.isTraceEnabled())
			log.trace("getColumnData(): columnId=" + columnId + ", record=" + record);
		MixoutRecord mixoutRecord = (MixoutRecord)record;
		if (columnId == COLUMN_FROM_SONG_DESCRIPTION.getColumnId())
			return new SmartString(mixoutRecord.getMixoutIdentifier().getFromSongIdentifier().toString());
		if (columnId == COLUMN_TO_SONG_DESCRIPTION.getColumnId())
			return new SmartString(mixoutRecord.getMixoutIdentifier().getToSongIdentifier().toString());
		if (columnId == COLUMN_MIXOUT_TYPE.getColumnId())
			return new SmartString(mixoutRecord.getMixoutTypeDescription());
		if (columnId == COLUMN_MIXOUT_BPM_DIFF.getColumnId())
			return new BpmDifference(mixoutRecord.getBpmDiff());
		return null;
	}	
	
	public ModelPopulatorInterface getModelPopulator() { return Database.getMixoutIndex(); }	
	
	public Index getIndex() { return Database.getMixoutIndex(); }
	
	public boolean excludeExternalItems() { return true; }
	public boolean excludeInternalItems() { return false; }

	public boolean isLazySearchSupported() { return true; }

	public void write(LineWriter writer) {
		super.write(writer);
		writer.writeLine(1); // version
	}
	
	public boolean shouldAddToView(Record record) { return true; }
	
}

