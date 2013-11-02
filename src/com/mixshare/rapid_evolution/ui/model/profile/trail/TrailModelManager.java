package com.mixshare.rapid_evolution.ui.model.profile.trail;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.DataConstantsHelper;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.column.StaticTypeColumn;
import com.mixshare.rapid_evolution.ui.model.column.comparables.SmartInteger;
import com.mixshare.rapid_evolution.ui.model.search.song.SongModelManager;
import com.mixshare.rapid_evolution.ui.model.table.TableItemModel;
import com.mixshare.rapid_evolution.ui.model.table.TableModelManager;
import com.mixshare.rapid_evolution.ui.widgets.common.rating.StarRating;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.trolltech.qt.core.QObject;
import com.trolltech.qt.core.Qt;

public class TrailModelManager extends TableModelManager {

    static private Logger log = Logger.getLogger(TrailModelManager.class);	
    static private final long serialVersionUID = 0L;    
    
    static public StaticTypeColumn[] ALL_COLUMNS = {
    	COLUMN_PLAYLIST_POSITION.getInstance(true),
    	COLUMN_NAME.getInstance(true),
		COLUMN_THUMBNAIL_IMAGE.getInstance(true),
    	COLUMN_RATING_STARS.getInstance(true),
    	COLUMN_RATING_VALUE.getInstance(false),
    	COLUMN_TYPE.getInstance(true),
    	
    	// song specific    	
		COLUMN_DURATION.getInstance(false),
		COLUMN_TIME_SIGNATURE.getInstance(false),
		COLUMN_BPM.getInstance(false),
		COLUMN_KEY.getInstance(false),
		COLUMN_KEYCODE.getInstance(false),
		COLUMN_BEAT_INTENSITY_DESCRIPTION.getInstance(false),
		COLUMN_BEAT_INTENSITY_VALUE.getInstance(false),
		COLUMN_LABELS.getInstance(false),
		COLUMN_ORIGINAL_YEAR.getInstance(false),
		COLUMN_COMMENTS.getInstance(false),
		COLUMN_STYLES.getInstance(false),
		COLUMN_TAGS.getInstance(false),
		COLUMN_FILENAME.getInstance(false),
		COLUMN_FILEPATH.getInstance(false),
    };
    
    ////////////
    // FIELDS //
    ////////////
        
    transient private Vector<Record> profileTrailRecords;
    
    transient private SongModelManager songModelProxy;
    
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
	public TrailModelManager() { }
	
	public TrailModelManager(LineReader lineReader) {
		super(lineReader);
		int version = Integer.parseInt(lineReader.getNextLine());
	}		
	
	public void initColumns() {
		sourceColumns.clear();
		for (Column column : ALL_COLUMNS)
			sourceColumns.add(column);		
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public StaticTypeColumn[] getAllStaticColumns() { return ALL_COLUMNS; }
	
	public String getTypeDescription() { return "Trail Record"; }
	
	public Object getSourceData(short columnId, Object obj) {
		if (log.isTraceEnabled())
			log.trace("getColumnData(): columnId=" + columnId + ", obj=" + obj);
		TrailInstance trailInstance = (TrailInstance)obj;		
		Record record = trailInstance.getRecord();
		// trail specific
		if (columnId == COLUMN_NAME.getColumnId())
			return record.toString();
		if (columnId == COLUMN_PLAYLIST_POSITION.getColumnId())
			return trailInstance.getPosition() + 1;
		if (columnId == COLUMN_TYPE.getColumnId())
			return DataConstantsHelper.getDataTypeDescription(record.getDataType());
		// search record specific
		if (record instanceof SearchRecord) {
			if (columnId == COLUMN_RATING_VALUE.getColumnId())
				return new SmartInteger(((SearchRecord)record).getRatingValue().getRatingValue());
			if (columnId == COLUMN_RATING_STARS.getColumnId())
				return new StarRating(((SearchRecord)record).getRatingValue());
			if (columnId == COLUMN_THUMBNAIL_IMAGE.getColumnId())
				return null;
		}
		// song specific
		if (record instanceof SongRecord) {
			if (songModelProxy == null)
				songModelProxy = new SongModelManager();
			return songModelProxy.getSourceData(columnId, record);
		}
		return null;
	}
		
	public int getSize() { return profileTrailRecords.size(); }
		
	public Record getRecordForRow(int row) {
		return profileTrailRecords.get(row);
	}
	
	/////////////
	// SETTERS //
	/////////////
	
	public void setProfileTrailRecords(Vector<Record> profileTrailRecords) {
		this.profileTrailRecords = profileTrailRecords;
	}
	
	/////////////
	// METHODS //
	/////////////
	
	protected void createSourceModel(QObject parent) {
		model = new TableItemModel(profileTrailRecords.size(), getNumColumns(), parent, this);
		loadTable();
	}
	
	protected void loadTable() {
		if (log.isDebugEnabled())
			log.debug("init(): loading " + getTypeDescription() + " model");
		initViewColumns();
		for (int c = 0; c < getNumColumns(); ++c)
			model.setHeaderData(c, Qt.Orientation.Horizontal, getSourceColumnTitle(c));		
		// load initial data
		for (int row = 0; row < profileTrailRecords.size(); ++row) {
			Record record = profileTrailRecords.get(row);
			TrailInstance instance = new TrailInstance(record, row);
			getRowObjects().add(instance);
			populateRow(instance, row);
		}
		if (log.isDebugEnabled())
			log.debug("init(): done");		
	}	
	
	/**
	 * Don't call directly (in a Java thread), call remove(...)
	 */		
	public void resetModel() {
		getRowObjects().clear();
		getTableItemModel().resetData();
		int modelSize = profileTrailRecords.size();
		if (modelSize > model.rowCount())
			model.insertRows(model.rowCount(), modelSize - model.rowCount());
		if (modelSize < model.rowCount())
			model.removeRows(modelSize, model.rowCount() - modelSize);
		loadTable();
	}	
	
	public void refresh() {
		resetModel();
		for (int row = 0; row < profileTrailRecords.size(); ++row) {
			Record record = profileTrailRecords.get(row);
			TrailInstance instance = new TrailInstance(record, row);
			getRowObjects().add(instance);
			populateRow(instance, row);
		}
	}

	/////////////
	// METHODS //
	/////////////
	
	public void write(LineWriter writer) {
		super.write(writer);
		writer.writeLine(1); // version
	}	

}
