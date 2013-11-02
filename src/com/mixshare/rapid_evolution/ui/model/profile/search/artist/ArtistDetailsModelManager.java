package com.mixshare.rapid_evolution.ui.model.profile.search.artist;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.exceptions.AlreadyExistsException;
import com.mixshare.rapid_evolution.data.mined.discogs.artist.DiscogsArtistProfile;
import com.mixshare.rapid_evolution.data.mined.musicbrainz.artist.MusicbrainzArtistProfile;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.profile.search.artist.ArtistProfile;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.column.StaticTypeColumn;
import com.mixshare.rapid_evolution.ui.model.column.comparables.SmartString;
import com.mixshare.rapid_evolution.ui.model.profile.details.SearchDetailsModelManager;
import com.mixshare.rapid_evolution.ui.model.profile.search.song.SongDetailsModelManager;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.mixshare.rapid_evolution.workflow.TaskManager;
import com.mixshare.rapid_evolution.workflow.maintenance.FieldSetTask;
import com.mixshare.rapid_evolution.workflow.user.ProfileSaveTask;
import com.trolltech.qt.gui.QApplication;

public class ArtistDetailsModelManager extends SearchDetailsModelManager {

    static private Logger log = Logger.getLogger(ArtistDetailsModelManager.class);	
    static private final long serialVersionUID = 0L;    
	
    static public StaticTypeColumn[] ALL_COLUMNS = {
    	COLUMN_ARTIST_NAME.getInstance(true, -1, false, false),
    	COLUMN_DISCOGS_ARTIST_NAME.getInstance(false, -1, false, false),
    	COLUMN_LASTFM_ARTIST_NAME.getInstance(false, -1, false, false),  	
    	COLUMN_MUSICBRAINZ_ID.getInstance(false, -1, false, false),  	
    	COLUMN_COMMENTS.getInstance(true, RE3Properties.getInt("column_height_comments"), true, true),
    	COLUMN_RATING_STARS.getInstance(true, -1, false, true),    	
    	COLUMN_ARTIST_REAL_NAME.getInstance(true, -1, false, false),
    	COLUMN_ARTIST_NAME_VARIATIONS.getInstance(true, -1, false, false),
    	COLUMN_ARTIST_ALIASES.getInstance(true, -1, false, false),
    	COLUMN_ARTIST_TYPE.getInstance(true, -1, false, false),
    	COLUMN_LIFESPAN_BEGIN.getInstance(true, -1, false, false),
    	COLUMN_LIFESPAN_END.getInstance(true, -1, false, false),
    	COLUMN_DISABLED.getInstance(false, -1, false, false),
    	COLUMN_UNIQUE_ID.getInstance(false, -1, false, false),
    	COLUMN_DUPLICATE_IDS.getInstance(false, -1, false, false)
    }; 
    
    //////////////////
    // CONSTRUCTION //
    //////////////////
    
	public ArtistDetailsModelManager() {
		super(Database.getArtistModelManager());
	}
	public ArtistDetailsModelManager(LineReader lineReader) {
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
	public String getTypeDescription() { return Translations.get("artist_details_model_manager_type"); }
	
	public Object getSourceData(short columnId, Object profile) {
		if (log.isTraceEnabled())
			log.trace("getColumnData(): columnId=" + columnId + ", profile=" + profile);		
		ArtistProfile artistProfile = (ArtistProfile)profile;
		DiscogsArtistProfile discogsProfile = (DiscogsArtistProfile)artistProfile.getMinedProfile(DATA_SOURCE_DISCOGS);
		MusicbrainzArtistProfile musicbrainzProfile = (MusicbrainzArtistProfile)artistProfile.getMinedProfile(DATA_SOURCE_MUSICBRAINZ);
		if (COLUMN_ARTIST_NAME.getColumnId() == columnId)
			return artistProfile.getArtistIdentifier().getName();
		if (COLUMN_DISCOGS_ARTIST_NAME.getColumnId() == columnId)
			return artistProfile.getDiscogsArtistName();
		if (COLUMN_LASTFM_ARTIST_NAME.getColumnId() == columnId)
			return artistProfile.getLastfmArtistName();
		if (COLUMN_MUSICBRAINZ_ID.getColumnId() == columnId)
			return artistProfile.getMbId();
		if (discogsProfile != null) {
			if (COLUMN_ARTIST_REAL_NAME.getColumnId() == columnId)
				return discogsProfile.getRealName();
			if (COLUMN_ARTIST_NAME_VARIATIONS.getColumnId() == columnId)
				return new SmartString(discogsProfile.getNameVariations());
			if (COLUMN_ARTIST_ALIASES.getColumnId() == columnId)
				return new SmartString(discogsProfile.getAliases());			
		}
		
		if (musicbrainzProfile != null) {
			if (COLUMN_ARTIST_TYPE.getColumnId() == columnId)
				return musicbrainzProfile.getType();
			if (COLUMN_LIFESPAN_BEGIN.getColumnId() == columnId)
				return musicbrainzProfile.getLifespanBegin();
			if (COLUMN_LIFESPAN_END.getColumnId() == columnId)
				return musicbrainzProfile.getLifespanEnd();			
			if (COLUMN_LIFESPAN_END.getColumnId() == columnId)
				return musicbrainzProfile.getLifespanEnd();			
			
		}		
		return super.getSourceData(columnId, profile);
	}
	
	/////////////
	// SETTERS //
	/////////////
		
	public void setFieldValue(Column column, Object value) {
		ArtistProfile artistProfile = (ArtistProfile)getRelativeProfile();
		setFieldValue(column, value, artistProfile);
	}

	public void setFieldValue(Column column, Object value, Profile profile) {
		if (log.isDebugEnabled())
			log.debug("setFieldValue(): column=" + column + ", value=" + value + ", profile=" + profile);
		try {
			ArtistProfile artistProfile = (ArtistProfile)profile;
			short type = column.getColumnId();
			if (COLUMN_ARTIST_NAME.getColumnId() == type) {
				try {
					if (value.toString().indexOf(";") >= 0) {
						Vector<Integer> songIds = artistProfile.getSongIds();
						Vector<SearchRecord> songRecords = new Vector<SearchRecord>(songIds.size());
						for (int songId : songIds) {
							SongRecord song = Database.getSongIndex().getSongRecord(songId);
							if (song != null)
								songRecords.add(song);
						}
						TaskManager.runForegroundTask(new FieldSetTask((SearchDetailsModelManager)Database.getRelativeModelFactory().getRelativeModelManager(SongDetailsModelManager.class), COLUMN_ARTIST_DESCRIPTION, value.toString(), songRecords));
					} else {
						artistProfile.setArtistName(value.toString());
						artistProfile.save();
					}
				} catch (AlreadyExistsException ae) {
					QApplication.invokeAndWait(new AlreadyExistsHandleThread(artistProfile, ae.getId()));
				} catch (Exception e) {
					log.error("execute(): error", e);
				}
				return;
			} else if (COLUMN_DISCOGS_ARTIST_NAME.getColumnId() == type) {
				artistProfile.setDiscogsArtistName(value.toString(), DATA_SOURCE_USER, true);
			} else if (COLUMN_LASTFM_ARTIST_NAME.getColumnId() == type) {
				artistProfile.setLastfmArtistName(value.toString(), DATA_SOURCE_USER, true);
			} else if (COLUMN_MUSICBRAINZ_ID.getColumnId() == type) {
				artistProfile.setMbId(value.toString(), DATA_SOURCE_USER, true);
			} else {
				super.setFieldValue(column, value, artistProfile);
			}
			//artistProfile.save();
			ProfileSaveTask.save(artistProfile);
		} catch (Exception e) {
			log.error("setFieldValue(): error", e);
		}		
	}
	
	public void write(LineWriter writer) {
		super.write(writer);
		writer.writeLine(1); // version
	}
	
}
