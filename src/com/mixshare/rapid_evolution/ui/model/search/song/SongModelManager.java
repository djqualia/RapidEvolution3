package com.mixshare.rapid_evolution.ui.model.search.song;

import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.index.Index;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.music.accuracy.Accuracy;
import com.mixshare.rapid_evolution.music.bpm.Bpm;
import com.mixshare.rapid_evolution.music.bpm.BpmDifference;
import com.mixshare.rapid_evolution.music.key.Key;
import com.mixshare.rapid_evolution.music.key.KeyRelation;
import com.mixshare.rapid_evolution.music.key.SongKeyRelation;
import com.mixshare.rapid_evolution.music.pitch.Cents;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.column.StaticTypeColumn;
import com.mixshare.rapid_evolution.ui.model.column.comparables.Percentage;
import com.mixshare.rapid_evolution.ui.model.column.comparables.SmartInteger;
import com.mixshare.rapid_evolution.ui.model.column.comparables.SmartString;
import com.mixshare.rapid_evolution.ui.model.profile.details.SearchDetailsModelManager;
import com.mixshare.rapid_evolution.ui.model.profile.search.song.SongDetailsModelManager;
import com.mixshare.rapid_evolution.ui.model.search.SearchModelManager;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.profile.ProfileWidgetUI;
import com.mixshare.rapid_evolution.util.FileUtil;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

public class SongModelManager extends SearchModelManager {

	static private Logger log = Logger.getLogger(SongModelManager.class);
    static private final long serialVersionUID = 0L;    	
    
    static private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
						
    static public StaticTypeColumn[] ALL_COLUMNS = {
		COLUMN_SONG_DESCRIPTION.getInstance(true),
		COLUMN_ARTIST_DESCRIPTION.getInstance(false),
		COLUMN_FEATURING_ARTISTS.getInstance(false),
		COLUMN_RELEASE_TITLE.getInstance(false),
		COLUMN_RELEASE_TITLES.getInstance(false),
		COLUMN_TRACK.getInstance(false),
		COLUMN_TITLE.getInstance(false),
		COLUMN_REMIX.getInstance(false),
		COLUMN_THUMBNAIL_IMAGE.getInstance(false),
		COLUMN_RATING_STARS.getInstance(true),
		COLUMN_RATING_VALUE.getInstance(false),
		COLUMN_PREFERENCE.getInstance(false),
		COLUMN_SCORE.getInstance(false),
		COLUMN_POPULARITY.getInstance(false),
		COLUMN_FILTERS_MATCH.getInstance(true),
		COLUMN_STYLES_MATCH.getInstance(false),
		COLUMN_TAGS_MATCH.getInstance(false),
		COLUMN_SIMILARITY.getInstance(false),
		COLUMN_DURATION.getInstance(true),
		COLUMN_TIME_SIGNATURE.getInstance(false),
		COLUMN_BPM.getInstance(true),
		COLUMN_BPM_START.getInstance(false),
		COLUMN_BPM_END.getInstance(false),
		COLUMN_BPM_ACCURACY.getInstance(false),
		COLUMN_BPM_DIFFERENCE.getInstance(false),
		COLUMN_BPM_SHIFT.getInstance(false),
		COLUMN_KEY.getInstance(true),
		COLUMN_KEY_START.getInstance(false),
		COLUMN_KEY_END.getInstance(false),
		COLUMN_ACTUAL_KEY.getInstance(false),
		COLUMN_KEYCODE.getInstance(false),
		COLUMN_KEYCODE_START.getInstance(false),
		COLUMN_KEYCODE_END.getInstance(false),
		COLUMN_ACTUAL_KEYCODE.getInstance(false),
		COLUMN_KEY_ACCURACY.getInstance(false),	
		COLUMN_KEY_RELATION.getInstance(false),
		COLUMN_KEY_LOCK.getInstance(false),
		COLUMN_KEY_CLOSENESS.getInstance(false),
		COLUMN_PITCH_SHIFT.getInstance(false),
		COLUMN_BEAT_INTENSITY_DESCRIPTION.getInstance(true),
		COLUMN_BEAT_INTENSITY_VALUE.getInstance(false),
		COLUMN_NUM_PLAYS.getInstance(false),
		COLUMN_NUM_MIXOUTS.getInstance(false),
		//COLUMN_NUM_ADDONS.getInstance(false),
		COLUMN_LABELS.getInstance(true),
		COLUMN_ORIGINAL_YEAR.getInstance(true),
		COLUMN_COMMENTS.getInstance(true),
		COLUMN_STYLES.getInstance(false),
		COLUMN_TAGS.getInstance(false),
		COLUMN_DATE_ADDED.getInstance(false),
		COLUMN_LAST_MODIFIED.getInstance(false),		
		COLUMN_FILENAME.getInstance(false),
		COLUMN_FILEPATH.getInstance(false),
		COLUMN_HAS_LASTFM_PROFILE.getInstance(false),
		COLUMN_HAS_MUSICBRAINZ_PROFILE.getInstance(false),
		COLUMN_HAS_BILLBOARD_PROFILE.getInstance(false),
		COLUMN_HAS_LYRICSFLY_PROFILE.getInstance(false),
		COLUMN_HAS_LYRICWIKI_PROFILE.getInstance(false),
		COLUMN_HAS_MIXSHARE_PROFILE.getInstance(false),
		COLUMN_HAS_YAHOO_PROFILE.getInstance(false)
    };
    
    //////////////////
    // CONSTRUCTION //
    //////////////////
    
	public SongModelManager() {
		super();
		setPrimarySortColumn(COLUMN_SONG_DESCRIPTION.getColumnId());
	}
	
	public SongModelManager(LineReader lineReader) {
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
	public String getTypeDescription() { return Translations.get("my_song_model_type_text"); }
		
	public Object getSourceData(short columnId, Object record) {
		if (log.isTraceEnabled())
			log.trace("getColumnData(): columnId=" + columnId + ", record=" + record);
		SongRecord songRecord = (SongRecord)record;
		if (columnId == COLUMN_SONG_DESCRIPTION.getColumnId())
			return songRecord.toString();
		if (columnId == COLUMN_ARTIST_DESCRIPTION.getColumnId())
			return new SmartString(songRecord.getArtistsDescription());
		if (columnId == COLUMN_FEATURING_ARTISTS.getColumnId())
			return new SmartString(songRecord.getFeaturingArtistsDescription());
		if (columnId == COLUMN_RELEASE_TITLES.getColumnId())
			return new SmartString(songRecord.getReleases());
		if (columnId == COLUMN_RELEASE_TITLE.getColumnId())
			return new SmartString(songRecord.getReleaseTitle());		
		if (columnId == COLUMN_TITLE.getColumnId())
			return new SmartString(songRecord.getTitle());
		if (columnId == COLUMN_TRACK.getColumnId())
			return new SmartString(songRecord.getTrack());
		if (columnId == COLUMN_REMIX.getColumnId())
			return new SmartString(songRecord.getRemix());
		if (columnId == COLUMN_DURATION.getColumnId())
			return songRecord.getDuration();
		if (columnId == COLUMN_TIME_SIGNATURE.getColumnId())
			return songRecord.getTimeSig();
		if (columnId == COLUMN_BPM.getColumnId())
			return songRecord.getBpm();
		if (columnId == COLUMN_BPM_START.getColumnId())
			return songRecord.getBpmStart();
		if (columnId == COLUMN_BPM_END.getColumnId())
			return songRecord.getBpmEnd();
		if (columnId == COLUMN_KEY.getColumnId())
			return songRecord.getKey();
		if (columnId == COLUMN_KEY_START.getColumnId())
			return songRecord.getStartKey();
		if (columnId == COLUMN_KEY_END.getColumnId())
			return songRecord.getEndKey();
		if (columnId == COLUMN_KEYCODE.getColumnId())
			return songRecord.getKeyCode();
		if (columnId == COLUMN_KEYCODE_START.getColumnId())
			return songRecord.getStartKey().getKeyCode();
		if (columnId == COLUMN_KEYCODE_END.getColumnId())
			return songRecord.getEndKey().getKeyCode();
		if (columnId == COLUMN_BEAT_INTENSITY_DESCRIPTION.getColumnId())
			return songRecord.getBeatIntensityDescription();
		if (columnId == COLUMN_BEAT_INTENSITY_VALUE.getColumnId())
			return songRecord.getBeatIntensityValue();
		if (columnId == COLUMN_FILEPATH.getColumnId())
			return new SmartString(songRecord.getSongFilename());
		if (columnId == COLUMN_FILENAME.getColumnId()) {
			if (songRecord.getSongFilename() != null)
				return new SmartString(FileUtil.getFilenameMinusDirectory(songRecord.getSongFilename()));
			return SmartString.EMPTY_STRING;
		} if (columnId == COLUMN_LABELS.getColumnId())
			return new SmartString(songRecord.getLabelsDescription());
		if (columnId == COLUMN_ORIGINAL_YEAR.getColumnId())
			return new SmartInteger(songRecord.getOriginalYearReleased(), true);
		if (columnId == COLUMN_NUM_MIXOUTS.getColumnId())
			return new SmartInteger((int)songRecord.getNumMixouts());
		//if (columnId == COLUMN_NUM_ADDONS.getColumnId())
			//return new SmartInteger((int)songRecord.getNumAddons());	
		if (columnId == COLUMN_KEY_ACCURACY.getColumnId())
			return Accuracy.getAccuracy(songRecord.getKeyAccuracy());		
		if (columnId == COLUMN_BPM_ACCURACY.getColumnId())
			return Accuracy.getAccuracy(songRecord.getBpmAccuracy());
		
		if (!RE3Properties.getBoolean("server_mode")) {
			if (ProfileWidgetUI.instance != null) {
				Profile profile = ProfileWidgetUI.instance.getCurrentProfile();
				if (profile instanceof SongProfile) {
					SongProfile currentSong = (SongProfile)profile;
					Bpm targetBpm = ProfileWidgetUI.instance.getStageWidget().getCurrentBpm();
					Key targetKey = ProfileWidgetUI.instance.getStageWidget().getCurrentKey();
					if (columnId == COLUMN_BPM_DIFFERENCE.getColumnId()) {
						float targetBpmValue = currentSong.getEndBpm();
						if (targetBpmValue == 0.0f)
							targetBpmValue = currentSong.getStartBpm();
						if ((targetBpmValue != 0.0f) && songRecord.getBpmStart().isValid())
							return new BpmDifference(songRecord.getBpmStart().getDifference(targetBpmValue));
						return BpmDifference.INVALID;
					} if (columnId == COLUMN_BPM_SHIFT.getColumnId()) {
						if (targetBpm.isValid() && songRecord.getBpmStart().isValid())
							return new BpmDifference(songRecord.getBpmStart().getDifference(targetBpm));
						return BpmDifference.INVALID;
					} if (columnId == COLUMN_ACTUAL_KEY.getColumnId()) {
						if (targetBpm.isValid() && songRecord.getBpmStart().isValid()) {
							float bpmDifference = songRecord.getBpmStart().getDifference(targetBpm);	          
							return songRecord.getStartKey().getShiftedKeyByBpmDifference(bpmDifference);
						}
						return Key.NO_KEY;
					} if (columnId == COLUMN_ACTUAL_KEYCODE.getColumnId()) {
						if (targetBpm.isValid() && songRecord.getBpmStart().isValid()) {
							float bpmDifference = songRecord.getBpmStart().getDifference(targetBpm);	          
							return songRecord.getStartKey().getShiftedKeyByBpmDifference(bpmDifference).getKeyCode();
						}
						return Key.NO_KEY;
					} if (columnId == COLUMN_KEY_LOCK.getColumnId()) {
			            SongKeyRelation relation = SongKeyRelation.getSongKeyRelation(songRecord, targetBpm, targetKey);
						return relation.getRecommendedKeyLockSetting();
					} if (columnId == COLUMN_KEY_CLOSENESS.getColumnId()) {			
						float percentInKey = 0.0f;
						SongKeyRelation keyRelation = SongKeyRelation.getSongKeyRelation(songRecord, targetBpm, targetKey);
						if (keyRelation.isCompatible())
							percentInKey = (0.5f - Math.abs(keyRelation.getBestKeyRelation().getDifference())) * 2.0f;
						return new Percentage(percentInKey);				
					} if (columnId == COLUMN_KEY_RELATION.getColumnId()) {
						SongKeyRelation relation = SongKeyRelation.getSongKeyRelation(songRecord, targetBpm, targetKey);
						return relation.getBestKeyRelation();
					} if (columnId == COLUMN_PITCH_SHIFT.getColumnId()) {
						if (songRecord.getBpmStart().isValid()) {
							KeyRelation relation = SongKeyRelation.getSongKeyRelation(songRecord, targetBpm, targetKey).getBestKeyRelation();
							if (relation.hasDifference())
								return Cents.getCents((float)relation.getDifference());
							return Cents.NO_CENTS;
						}
						return Cents.NO_CENTS;
					}				
				}
			}
		}
		
		return super.getSourceData(columnId, record);
	}	
	
	public Index getIndex() { return Database.getSongIndex(); }	
	
	public SearchDetailsModelManager getDetailsModelManager() { return (SongDetailsModelManager)Database.getRelativeModelFactory().getRelativeModelManager(SongDetailsModelManager.class); }
	
	public boolean excludeExternalItems() { return true; }
	
	public boolean isLazySearchSupported() { return true; }
	
	public void write(LineWriter writer) {
		super.write(writer);
		writer.writeLine(1); // version
	}
	
}
