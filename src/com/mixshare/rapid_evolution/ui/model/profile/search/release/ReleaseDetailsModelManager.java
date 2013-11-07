package com.mixshare.rapid_evolution.ui.model.profile.search.release;

import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.exceptions.AlreadyExistsException;
import com.mixshare.rapid_evolution.data.mined.musicbrainz.release.MusicbrainzReleaseProfile;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.profile.search.release.ReleaseProfile;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.column.StaticTypeColumn;
import com.mixshare.rapid_evolution.ui.model.profile.details.SearchDetailsModelManager;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.mixshare.rapid_evolution.workflow.user.ProfileSaveTask;
import com.trolltech.qt.gui.QApplication;

public class ReleaseDetailsModelManager extends SearchDetailsModelManager {

    static private Logger log = Logger.getLogger(ReleaseDetailsModelManager.class);	
    static private final long serialVersionUID = 0L;    
	
    static public StaticTypeColumn[] ALL_COLUMNS = {
    	COLUMN_ARTIST_DESCRIPTION.getInstance(true, -1, true, false),
    	COLUMN_RELEASE_TITLE.getInstance(true, -1, false, false),
    	COLUMN_RELEASE_IS_COMPILATION.getInstance(true, -1, false, false),
    	COLUMN_COMMENTS.getInstance(true, RE3Properties.getInt("column_height_comments"), true, true),
    	COLUMN_RATING_STARS.getInstance(true, -1, false, true),
    	COLUMN_LABELS.getInstance(true, -1, true, true),
    	COLUMN_ORIGINAL_YEAR.getInstance(true, -1, true, false),
    	COLUMN_RELEASE_TYPE.getInstance(true, 1, true, false),
    	COLUMN_DISABLED.getInstance(false, -1, false, false),
    	COLUMN_UNIQUE_ID.getInstance(false, -1, false, false),
    	COLUMN_DUPLICATE_IDS.getInstance(false, -1, false, false)
    }; 
    
    //////////////////
    // CONSTRUCTION //
    //////////////////
    
	public ReleaseDetailsModelManager() {
		super(Database.getReleaseModelManager());
	}
	
	public ReleaseDetailsModelManager(LineReader lineReader) {
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
	public String getTypeDescription() { return Translations.get("release_details_model_manager_type"); }
	
	public Object getSourceData(short columnId, Object profile) {
		if (log.isTraceEnabled())
			log.trace("getColumnData(): columnId=" + columnId + ", profile=" + profile);		
		ReleaseProfile releaseProfile = (ReleaseProfile)profile;
		MusicbrainzReleaseProfile musicbrainzProfile = (MusicbrainzReleaseProfile)releaseProfile.getMinedProfile(DATA_SOURCE_MUSICBRAINZ);
		if (COLUMN_ARTIST_DESCRIPTION.getColumnId() == columnId)
			return releaseProfile.getArtistsDescription();
		if (COLUMN_RELEASE_TITLE.getColumnId() == columnId)
			return releaseProfile.getReleaseIdentifier().getReleaseTitle();
		if (COLUMN_RELEASE_IS_COMPILATION.getColumnId() == columnId)
			return new Boolean(releaseProfile.isCompilationRelease());
		if (COLUMN_LABELS.getColumnId() == columnId)
			return releaseProfile.getLabelsDescription();
		if (COLUMN_ORIGINAL_YEAR.getColumnId() == columnId)
			return releaseProfile.getOriginalYearReleasedAsString();		
		
		if (musicbrainzProfile != null) {
			if (COLUMN_RELEASE_TYPE.getColumnId() == columnId)
				return musicbrainzProfile.getType();
		}
		
		return super.getSourceData(columnId, profile);
	}
	
	/////////////
	// SETTERS //
	/////////////
	
	public void setFieldValue(Column column, Object value) {
		ReleaseProfile releaseProfile = (ReleaseProfile)getRelativeProfile();
		setFieldValue(column, value, releaseProfile);
	}

	public void setFieldValue(Column column, Object value, Profile profile) {
		if (log.isDebugEnabled())
			log.debug("setFieldValue(): column=" + column + ", value=" + value + ", profile=" + profile);
		try {
			ReleaseProfile releaseProfile = (ReleaseProfile)profile;
			short type = column.getColumnId();
			if (COLUMN_ARTIST_DESCRIPTION.getColumnId() == type) {
				String artistDescription = value.toString();
				StringTokenizer tokenizer = new StringTokenizer(artistDescription, ";");
				Vector<String> artistNames = new Vector<String>();
				while (tokenizer.hasMoreTokens())
					artistNames.add(tokenizer.nextToken().trim());
				try {
					releaseProfile.setArtistNames(artistNames);
					releaseProfile.save();
				} catch (AlreadyExistsException ae) {
					QApplication.invokeAndWait(new AlreadyExistsHandleThread(releaseProfile, ae.getId()));
				} catch (Exception e) {
					log.error("execute(): error", e);
				}
				return;
			} else if (COLUMN_RELEASE_TITLE.getColumnId() == type) {
				try {
					releaseProfile.setReleaseTitle(value.toString());
					releaseProfile.save();
				} catch (AlreadyExistsException ae) {
					QApplication.invokeAndWait(new AlreadyExistsHandleThread(releaseProfile, ae.getId()));
				} catch (Exception e) {
					log.error("execute(): error", e);
				}
				return;
			} else if (COLUMN_RELEASE_IS_COMPILATION.getColumnId() == type) {
				releaseProfile.setIsCompilationRelease((Boolean)value);
			} else if (COLUMN_LABELS.getColumnId() == type) {
				if (value != null) {
					String labelDescription = value.toString();
					StringTokenizer tokenizer = new StringTokenizer(labelDescription, ";");
					Vector<String> labelNames = new Vector<String>();
					while (tokenizer.hasMoreTokens())
						labelNames.add(tokenizer.nextToken().trim());
					releaseProfile.setLabelNames(labelNames);
				} else {
					releaseProfile.setLabelNames(new Vector<String>());
				}
			} else if (COLUMN_ORIGINAL_YEAR.getColumnId() == type) {
				releaseProfile.setOriginalYearReleased(Short.parseShort(value.toString()), DATA_SOURCE_USER);
			} else {
				super.setFieldValue(column, value, releaseProfile);
			}
			ProfileSaveTask.save(releaseProfile);			
		} catch (Exception e) {
			log.error("setFieldValue(): error", e);
		}		
	}

	public void write(LineWriter writer) {
		super.write(writer);
		writer.writeLine(1); // version
	}
	
}
