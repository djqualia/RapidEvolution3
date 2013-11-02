package com.mixshare.rapid_evolution.ui.model.profile.filter.playlist;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.exceptions.AlreadyExistsException;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.profile.filter.playlist.PlaylistProfile;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.column.StaticTypeColumn;
import com.mixshare.rapid_evolution.ui.model.profile.details.FilterDetailsModelManager;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.mixshare.rapid_evolution.workflow.user.ProfileSaveTask;

public class PlaylistDetailsModelManager extends FilterDetailsModelManager {

    static private Logger log = Logger.getLogger(PlaylistDetailsModelManager.class);	
    static private final long serialVersionUID = 0L;    
	
    static public StaticTypeColumn[] ALL_COLUMNS = {
    	COLUMN_PLAYLIST_NAME.getInstance(true, -1, false, false),
    	COLUMN_DISABLED.getInstance(false, -1, false, false),
    	COLUMN_UNIQUE_ID.getInstance(false, -1, false, false),
    	COLUMN_DUPLICATE_IDS.getInstance(false, -1, false, false)    	
    }; 
    
    //////////////////
    // CONSTRUCTION //
    //////////////////
    
	public PlaylistDetailsModelManager() {
		super(Database.getPlaylistModelManager());
	}
	public PlaylistDetailsModelManager(LineReader lineReader) {
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
	public String getTypeDescription() { return Translations.get("playlist_details_model_manager_type"); }
	
	public Object getSourceData(short columnId, Object profile) {
		if (log.isTraceEnabled())
			log.trace("getColumnData(): columnId=" + columnId + ", profile=" + profile);		
		PlaylistProfile playlistProfile = (PlaylistProfile)profile;
		if (COLUMN_PLAYLIST_NAME.getColumnId() == columnId)
			return playlistProfile.getPlaylistName();
		return super.getSourceData(columnId, profile);
	}
	
	/////////////
	// SETTERS //
	/////////////
		
	public void setFieldValue(Column column, Object value) {
		PlaylistProfile playlistProfile = (PlaylistProfile)getRelativeProfile();
		setFieldValue(column, value, playlistProfile);
	}

	public void setFieldValue(Column column, Object value, Profile profile) {
		if (log.isDebugEnabled())
			log.debug("setFieldValue(): column=" + column + ", value=" + value + ", profile=" + profile);
		try {
			PlaylistProfile playlistProfile = (PlaylistProfile)profile;
			short type = column.getColumnId();
			if (COLUMN_PLAYLIST_NAME.getColumnId() == type) {
				try {
					playlistProfile.setPlaylistName(value.toString());
				} catch (AlreadyExistsException ae) {
					handleAlreadyExists(playlistProfile, ae.getId());								
				}					
			} else {
				super.setFieldValue(column, value, playlistProfile);
			}
			ProfileSaveTask.save(playlistProfile);			
		} catch (Exception e) {
			log.error("setFieldValue(): error", e);
		}		
	}

	public void write(LineWriter writer) {
		super.write(writer);
		writer.writeLine(1); // version;
	}
	
}
