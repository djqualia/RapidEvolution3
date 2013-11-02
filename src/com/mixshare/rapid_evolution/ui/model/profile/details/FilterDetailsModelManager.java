package com.mixshare.rapid_evolution.ui.model.profile.details;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.profile.filter.FilterProfile;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.filter.FilterModelManager;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;


abstract public class FilterDetailsModelManager extends CommonDetailsModelManager {

    static private Logger log = Logger.getLogger(FilterDetailsModelManager.class);	
	
    ////////////
    // FIELDS //
    ////////////
    
	private FilterModelManager filterModelManager;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public FilterDetailsModelManager() { }
	public FilterDetailsModelManager(FilterModelManager filterModelManager) {
		this.filterModelManager = filterModelManager;
	}
	public FilterDetailsModelManager(LineReader lineReader) {
		super(lineReader);
		int version = Integer.parseInt(lineReader.getNextLine());
	}		
	
	/////////////
	// GETTERS //
	/////////////
	
	public Object getSourceData(short columnId, Object record) {
		FilterProfile filterProfile = (FilterProfile)record;
		return super.getSourceData(columnId, record);
	}
	
	// for serialization
	public FilterModelManager getFilterModelManager() { return filterModelManager; }
		
	/////////////
	// SETTERS //
	/////////////
	
	public void setFieldValue(Column column, Object value) {
		FilterProfile filterProfile = (FilterProfile)getRelativeProfile();
		setFieldValue(column, value, filterProfile);
	}

	public void setFieldValue(Column column, Object value, Profile profile) {
		if (log.isTraceEnabled())
			log.trace("setFieldValue(): column=" + column + ", value=" + value);
		try {
			FilterProfile filterProfile = (FilterProfile)profile;
			short type = column.getColumnId();

		} catch (Exception e) {
			log.error("setFieldValue(): error", e);
		}
	}
	
	// for serialization
	public void setFilterModelManager(FilterModelManager filterModelManager) { this.filterModelManager = filterModelManager; }
	
	/////////////
	// METHODS //
	/////////////
	
	public void write(LineWriter writer) {
		super.write(writer);
		writer.writeLine(1); //version
	}
	
}
