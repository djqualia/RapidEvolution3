package com.mixshare.rapid_evolution.ui.model.profile.filter.tag;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.exceptions.AlreadyExistsException;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.profile.filter.tag.TagProfile;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.column.StaticTypeColumn;
import com.mixshare.rapid_evolution.ui.model.profile.details.FilterDetailsModelManager;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.mixshare.rapid_evolution.workflow.user.ProfileSaveTask;

public class TagDetailsModelManager extends FilterDetailsModelManager {

    static private Logger log = Logger.getLogger(TagDetailsModelManager.class);	
    static private final long serialVersionUID = 0L;    
	
    static public StaticTypeColumn[] ALL_COLUMNS = {
    	COLUMN_TAG_NAME.getInstance(true, -1, false, false),
    	COLUMN_TAG_CATEGORY_ONLY.getInstance(true, -1, false, false),
    	COLUMN_DISABLED.getInstance(false, -1, false, false),
    	COLUMN_UNIQUE_ID.getInstance(false, -1, false, false),
    	COLUMN_DUPLICATE_IDS.getInstance(false, -1, false, false)    	
    }; 
    
    //////////////////
    // CONSTRUCTION //
    //////////////////
    
	public TagDetailsModelManager() {
		super(Database.getTagModelManager());
	}
	
	public TagDetailsModelManager(LineReader lineReader) {
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
	public String getTypeDescription() { return Translations.get("tag_details_model_manager_type"); }
	
	public Object getSourceData(short columnId, Object profile) {
		if (log.isTraceEnabled())
			log.trace("getColumnData(): columnId=" + columnId + ", profile=" + profile);		
		TagProfile tagProfile = (TagProfile)profile;
		if (COLUMN_TAG_NAME.getColumnId() == columnId)
			return tagProfile.getTagName();
		if (COLUMN_TAG_CATEGORY_ONLY.getColumnId() == columnId)
			return tagProfile.isCategoryOnly();
		return super.getSourceData(columnId, profile);
	}
	
	/////////////
	// SETTERS //
	/////////////
		
	public void setFieldValue(Column column, Object value) {
		TagProfile tagProfile = (TagProfile)getRelativeProfile();
		setFieldValue(column, value, tagProfile);
	}

	public void setFieldValue(Column column, Object value, Profile profile) {
		if (log.isDebugEnabled())
			log.debug("setFieldValue(): column=" + column + ", value=" + value + ", profile=" + profile);
		try {
			TagProfile tagProfile = (TagProfile)profile;
			short type = column.getColumnId();
			if (COLUMN_TAG_NAME.getColumnId() == type) {
				try {
					tagProfile.setTagName(value.toString());
				} catch (AlreadyExistsException ae) {
					handleAlreadyExists(tagProfile, ae.getId());
				}
			} else if (COLUMN_TAG_CATEGORY_ONLY.getColumnId() == type) {
				tagProfile.setCategoryOnly((Boolean)value);				
			} else {
				super.setFieldValue(column, value, tagProfile);
			}
			ProfileSaveTask.save(tagProfile);			
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
