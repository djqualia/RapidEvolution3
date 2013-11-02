package com.mixshare.rapid_evolution.ui.model.profile.filter.style;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.exceptions.AlreadyExistsException;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.profile.filter.style.StyleProfile;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.column.StaticTypeColumn;
import com.mixshare.rapid_evolution.ui.model.profile.details.FilterDetailsModelManager;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.mixshare.rapid_evolution.workflow.user.ProfileSaveTask;

public class StyleDetailsModelManager extends FilterDetailsModelManager {

    static private Logger log = Logger.getLogger(StyleDetailsModelManager.class);	
    static private final long serialVersionUID = 0L;    
	
    static public StaticTypeColumn[] ALL_COLUMNS = {
    	COLUMN_STYLE_NAME.getInstance(true, -1, false, false),
    	COLUMN_STYLE_DESCRIPTION.getInstance(true, -1, false, false),
    	COLUMN_STYLE_CATEGORY_ONLY.getInstance(true, -1, false, false),
    	COLUMN_DISABLED.getInstance(false, -1, false, false),
    	COLUMN_UNIQUE_ID.getInstance(false, -1, false, false),
    	COLUMN_DUPLICATE_IDS.getInstance(false, -1, false, false)    	
    }; 
    
    //////////////////
    // CONSTRUCTION //
    //////////////////
    
	public StyleDetailsModelManager() {
		super(Database.getStyleModelManager());
	}
	public StyleDetailsModelManager(LineReader lineReader) {
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
	public String getTypeDescription() { return Translations.get("style_details_model_manager_type"); }
	
	public Object getSourceData(short columnId, Object profile) {
		if (log.isTraceEnabled())
			log.trace("getColumnData(): columnId=" + columnId + ", profile=" + profile);		
		StyleProfile styleProfile = (StyleProfile)profile;
		if (COLUMN_STYLE_NAME.getColumnId() == columnId)
			return styleProfile.getStyleName();
		else if (COLUMN_STYLE_DESCRIPTION.getColumnId() == columnId)
			return styleProfile.getDescription();
		else if (COLUMN_STYLE_CATEGORY_ONLY.getColumnId() == columnId)
			return styleProfile.isCategoryOnly();
		return super.getSourceData(columnId, profile);
	}
	
	/////////////
	// SETTERS //
	/////////////
		
	public void setFieldValue(Column column, Object value) {
		StyleProfile styleProfile = (StyleProfile)getRelativeProfile();
		setFieldValue(column, value, styleProfile);
	}

	public void setFieldValue(Column column, Object value, Profile profile) {
		if (log.isDebugEnabled())
			log.debug("setFieldValue(): column=" + column + ", value=" + value + ", profile=" + profile);
		try {
			StyleProfile styleProfile = (StyleProfile)profile;
			short type = column.getColumnId();
			if (COLUMN_STYLE_NAME.getColumnId() == type) {
				try {
					styleProfile.setStyleName(value.toString());
				} catch (AlreadyExistsException ae) {
					handleAlreadyExists(styleProfile, ae.getId());
				}					
			} else if (COLUMN_STYLE_DESCRIPTION.getColumnId() == type) {
				styleProfile.setDescription(value.toString());
			} else if (COLUMN_STYLE_CATEGORY_ONLY.getColumnId() == type) {
				styleProfile.setCategoryOnly((Boolean)value);
			} else {
				super.setFieldValue(column, value, styleProfile);
			}
			ProfileSaveTask.save(styleProfile);
		} catch (Exception e) {
			log.error("setFieldValue(): error", e);
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
