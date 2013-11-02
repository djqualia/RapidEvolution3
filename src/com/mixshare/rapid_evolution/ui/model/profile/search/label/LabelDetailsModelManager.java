package com.mixshare.rapid_evolution.ui.model.profile.search.label;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.exceptions.AlreadyExistsException;
import com.mixshare.rapid_evolution.data.mined.discogs.label.DiscogsLabelProfile;
import com.mixshare.rapid_evolution.data.mined.musicbrainz.label.MusicbrainzLabelProfile;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.profile.search.label.LabelProfile;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.column.StaticTypeColumn;
import com.mixshare.rapid_evolution.ui.model.column.comparables.SmartString;
import com.mixshare.rapid_evolution.ui.model.profile.details.SearchDetailsModelManager;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.mixshare.rapid_evolution.workflow.user.ProfileSaveTask;
import com.trolltech.qt.gui.QApplication;

public class LabelDetailsModelManager extends SearchDetailsModelManager {

    static private Logger log = Logger.getLogger(LabelDetailsModelManager.class);	
    static private final long serialVersionUID = 0L;    
	
    static public StaticTypeColumn[] ALL_COLUMNS = {
    	COLUMN_LABEL_NAME.getInstance(true, -1, false, false),
    	COLUMN_DISCOGS_LABEL_NAME.getInstance(false, -1, false, false),
    	COLUMN_COMMENTS.getInstance(true, RE3Properties.getInt("column_height_comments"), true, true),
    	COLUMN_RATING_STARS.getInstance(true, -1, false, true),    	
    	COLUMN_LABEL_CONTACT_INFO.getInstance(true, RE3Properties.getInt("column_height_contact_info"), false, false),
    	COLUMN_LABEL_PARENT_LABELS.getInstance(true, -1, true, false),
    	COLUMN_LABEL_SUB_LABELS.getInstance(true, -1, false, false),
    	COLUMN_LABEL_NAME_VARIATIONS.getInstance(true, -1, false, false),
    	COLUMN_LABEL_TYPE.getInstance(true, -1, true, false),
    	COLUMN_LABEL_CODE.getInstance(true, -1, true, false),
    	COLUMN_LABEL_COUNTRY.getInstance(true, -1, true, false),    	
    	COLUMN_LIFESPAN_BEGIN.getInstance(true, -1, false, false),
    	COLUMN_LIFESPAN_END.getInstance(true, -1, false, false),
    	COLUMN_DISABLED.getInstance(false, -1, false, false),
    	COLUMN_UNIQUE_ID.getInstance(false, -1, false, false),
    	COLUMN_DUPLICATE_IDS.getInstance(false, -1, false, false)
    }; 
    
    //////////////////
    // CONSTRUCTION //
    //////////////////
    
	public LabelDetailsModelManager() {
		super(Database.getLabelModelManager());
	}
	public LabelDetailsModelManager(LineReader lineReader) {
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
	public String getTypeDescription() { return Translations.get("label_details_model_manager_type"); }
	
	public Object getSourceData(short columnId, Object profile) {
		if (log.isTraceEnabled())
			log.trace("getColumnData(): columnId=" + columnId + ", profile=" + profile);		
		LabelProfile labelProfile = (LabelProfile)profile;
		DiscogsLabelProfile discogsProfile = (DiscogsLabelProfile)labelProfile.getMinedProfile(DATA_SOURCE_DISCOGS);
		MusicbrainzLabelProfile musicbrainzProfile = (MusicbrainzLabelProfile)labelProfile.getMinedProfile(DATA_SOURCE_MUSICBRAINZ);
		if (COLUMN_LABEL_NAME.getColumnId() == columnId)
			return labelProfile.getLabelIdentifier().getName();
		if (COLUMN_DISCOGS_LABEL_NAME.getColumnId() == columnId)
			return labelProfile.getDiscogslLabelName();
		
		if (discogsProfile != null) {
			if (COLUMN_LABEL_CONTACT_INFO.getColumnId() == columnId)
				return discogsProfile.getContactInfo();
			if (COLUMN_LABEL_NAME.getColumnId() == columnId)
				return discogsProfile.getParentLabel();
			if (COLUMN_LABEL_NAME.getColumnId() == columnId)
				return new SmartString(discogsProfile.getSubLabels());			
		}
		
		if (musicbrainzProfile != null) {
			if (COLUMN_LABEL_NAME_VARIATIONS.getColumnId() == columnId)
				return new SmartString(musicbrainzProfile.getAliases());
			if (COLUMN_LABEL_TYPE.getColumnId() == columnId)
				return musicbrainzProfile.getType();
			if (COLUMN_LABEL_CODE.getColumnId() == columnId)
				return musicbrainzProfile.getLabelCode();		
			if (COLUMN_LABEL_COUNTRY.getColumnId() == columnId)
				return musicbrainzProfile.getCountry();
			if (COLUMN_LIFESPAN_BEGIN.getColumnId() == columnId)
				return musicbrainzProfile.getLifespanBegin();
			if (COLUMN_LIFESPAN_END.getColumnId() == columnId)
				return musicbrainzProfile.getLifespanEnd();						
		}
		
		return super.getSourceData(columnId, profile);
	}
	
	/////////////
	// SETTERS //
	/////////////
	
	public void setFieldValue(Column column, Object value) {
		LabelProfile labelProfile = (LabelProfile)getRelativeProfile();
		setFieldValue(column, value, labelProfile);
	}

	public void setFieldValue(Column column, Object value, Profile profile) {
		if (log.isDebugEnabled())
			log.debug("setFieldValue(): column=" + column + ", value=" + value + ", profile=" + profile);
		try {
			LabelProfile labelProfile = (LabelProfile)profile;
			short type = column.getColumnId();
			if (COLUMN_LABEL_NAME.getColumnId() == type) {
				try {
					labelProfile.setLabelName(value.toString());
					labelProfile.save();
				} catch (AlreadyExistsException ae) {
					QApplication.invokeAndWait(new AlreadyExistsHandleThread(labelProfile, ae.getId()));
				} catch (Exception e) {
					log.error("execute(): error", e);
				}
				return;
			} else if (COLUMN_DISCOGS_LABEL_NAME.getColumnId() == type) {
				labelProfile.setDiscogsLabelName(value.toString(), DATA_SOURCE_USER, true);
			} else {
				super.setFieldValue(column, value, labelProfile);
			}
			ProfileSaveTask.save(labelProfile);
		} catch (Exception e) {
			log.error("setFieldValue(): error", e);
		}		
	}

	public void write(LineWriter writer) {
		super.write(writer);
		writer.writeLine(1); // version
	}	
	
}
