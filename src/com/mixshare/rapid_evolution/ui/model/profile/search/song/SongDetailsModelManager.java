package com.mixshare.rapid_evolution.ui.model.profile.search.song;

import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.exceptions.AlreadyExistsException;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.music.accuracy.Accuracy;
import com.mixshare.rapid_evolution.music.beatintensity.BeatIntensity;
import com.mixshare.rapid_evolution.music.bpm.Bpm;
import com.mixshare.rapid_evolution.music.duration.Duration;
import com.mixshare.rapid_evolution.music.key.Key;
import com.mixshare.rapid_evolution.music.timesig.TimeSig;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.column.StaticTypeColumn;
import com.mixshare.rapid_evolution.ui.model.column.comparables.SmartFloat;
import com.mixshare.rapid_evolution.ui.model.profile.details.SearchDetailsModelManager;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.profile.ProfileWidgetUI;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.mixshare.rapid_evolution.workflow.user.ProfileSaveTask;
import com.trolltech.qt.gui.QApplication;

public class SongDetailsModelManager extends SearchDetailsModelManager {

    static private Logger log = Logger.getLogger(SongDetailsModelManager.class);	
    static private final long serialVersionUID = 0L;    
	
    static private boolean SET_ACCURACY_TO_100_AUTOMATICALLY = true; // when true, if bpm or key are set manually then the accuracy is set to 100% automatically
    
    static public StaticTypeColumn[] ALL_COLUMNS = {
    	COLUMN_ARTIST_DESCRIPTION.getInstance(true, -1, true, false),
    	COLUMN_FEATURING_ARTISTS.getInstance(true, -1, false, true),
    	COLUMN_RELEASE_TITLE.getInstance(true, -1, true, false),
    	COLUMN_TRACK.getInstance(true, -1, false, true),
    	COLUMN_TITLE.getInstance(true, -1, false, false),
    	COLUMN_REMIX.getInstance(true, -1, false, true),
    	COLUMN_COMMENTS.getInstance(true, RE3Properties.getInt("column_height_comments"), true, true),
    	COLUMN_RATING_STARS.getInstance(true, -1, false, true),
    	COLUMN_DURATION.getInstance(true, -1, false, false),
    	COLUMN_LABELS.getInstance(true, -1, true, true),
    	COLUMN_ORIGINAL_YEAR.getInstance(true, -1, true, false),
    	COLUMN_KEY_START.getInstance(true, -1, false, true),
    	COLUMN_KEY_END.getInstance(true, -1, false, true),
    	COLUMN_KEY_ACCURACY.getInstance(true, -1, false, false),
    	COLUMN_BPM_START.getInstance(true, -1, false, true),
    	COLUMN_BPM_END.getInstance(true, -1, false, true),
    	COLUMN_BPM_ACCURACY.getInstance(true, -1, false, false),
    	COLUMN_TIME_SIGNATURE.getInstance(true, -1, false, false),
    	COLUMN_BEAT_INTENSITY_VALUE.getInstance(true, -1, false, true),
    	COLUMN_NUM_PLAYS.getInstance(true, -1, false, true),
    	COLUMN_REPLAY_GAIN.getInstance(false, -1, false, false),
    	COLUMN_FILEPATH.getInstance(true, -1, false, false),
    	COLUMN_DISABLED.getInstance(false, -1, false, false),
    	COLUMN_UNIQUE_ID.getInstance(false, -1, false, false),
    	COLUMN_DUPLICATE_IDS.getInstance(false, -1, false, false)
    }; 
    
    //////////////////
    // CONSTRUCTION //
    //////////////////
    
	public SongDetailsModelManager() {
		super(Database.getSongModelManager());
	}
	
	public SongDetailsModelManager(LineReader lineReader) {
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
	public String getTypeDescription() { return Translations.get("song_details_model_manager_type"); }
	
	public Object getSourceData(short columnId, Object profile) {
		if (log.isTraceEnabled())
			log.trace("getColumnData(): columnId=" + columnId + ", profile=" + profile);		
		SongProfile songProfile = (SongProfile)profile;
		if (COLUMN_ARTIST_DESCRIPTION.getColumnId() == columnId)
			return songProfile.getArtistsDescription(false);
		if (COLUMN_FEATURING_ARTISTS.getColumnId() == columnId)
			return songProfile.getSongRecord().getFeaturingArtistsDescription();
		if (COLUMN_RELEASE_TITLE.getColumnId() == columnId) {
			return songProfile.getReleaseTitle();
		} if (COLUMN_TRACK.getColumnId() == columnId) {
			return songProfile.getTrack();
		} if (COLUMN_TITLE.getColumnId() == columnId)
			return songProfile.getTitle();
		if (COLUMN_REMIX.getColumnId() == columnId)
			return songProfile.getRemix();
		if (COLUMN_DURATION.getColumnId() == columnId)
			return songProfile.getDuration().toString();
		if (COLUMN_LABELS.getColumnId() == columnId)
			return songProfile.getLabelsDescription();
		if (COLUMN_ORIGINAL_YEAR.getColumnId() == columnId)
			return songProfile.getOriginalYearReleasedAsString();
		if (COLUMN_KEY_START.getColumnId() == columnId)
			return songProfile.getStartKey().toStringExact();
		if (COLUMN_KEY_END.getColumnId() == columnId)
			return songProfile.getEndKey().toStringExact();
		if (COLUMN_KEY_ACCURACY.getColumnId() == columnId)
			return Accuracy.getAccuracy(songProfile.getKeyAccuracy());
		if (COLUMN_BPM_START.getColumnId() == columnId)
			return songProfile.getBpmStart().toString();
		if (COLUMN_BPM_END.getColumnId() == columnId)
			return songProfile.getBpmEnd().toString();
		if (COLUMN_BPM_ACCURACY.getColumnId() == columnId)
			return Accuracy.getAccuracy(songProfile.getBpmAccuracy());
		if (COLUMN_TIME_SIGNATURE.getColumnId() == columnId)
			return songProfile.getTimeSig().toString();
		if (COLUMN_BEAT_INTENSITY_VALUE.getColumnId() == columnId)
			return songProfile.getBeatIntensityValue();
		if (COLUMN_NUM_PLAYS.getColumnId() == columnId)
			return String.valueOf(songProfile.getPlayCount());
		if (COLUMN_FILEPATH.getColumnId() == columnId)
			return songProfile.getSongFilename();		
		if (COLUMN_REPLAY_GAIN.getColumnId() == columnId)
			return new SmartFloat(songProfile.getReplayGain());
		return super.getSourceData(columnId, profile);
	}
	
	/////////////
	// METHODS //
	/////////////
	
	public void setFieldValue(Column column, Object value) {
		SongProfile songProfile = (SongProfile)getRelativeProfile();
		setFieldValue(column, value, songProfile);
	}

	public void setFieldValue(Column column, Object value, Profile profile) {
		if (log.isTraceEnabled())
			log.trace("setFieldValue(): column=" + column + ", value=" + value + ", profile=" + profile);
		try {
			SongProfile songProfile = (SongProfile)profile;
			short type = column.getColumnId();
			if (COLUMN_ARTIST_DESCRIPTION.getColumnId() == type) {				
				String artistDescription = value.toString();
				StringTokenizer tokenizer = new StringTokenizer(artistDescription, ";");
				Vector<String> artistNames = new Vector<String>();
				while (tokenizer.hasMoreTokens())
					artistNames.add(tokenizer.nextToken().trim());				
				try {
					songProfile.setArtistNames(artistNames);
					songProfile.save();
				} catch (AlreadyExistsException ae) {
					QApplication.invokeAndWait(new AlreadyExistsHandleThread(songProfile, ae.getId()));
				} catch (Exception e) {
					log.error("execute(): error", e);
				}				
				return;
			} else if (COLUMN_FEATURING_ARTISTS.getColumnId() == type) {
				String artistDescription = value.toString();
				StringTokenizer tokenizer = new StringTokenizer(artistDescription, ";");
				Vector<String> artistNames = new Vector<String>();
				while (tokenizer.hasMoreTokens())
					artistNames.add(tokenizer.nextToken().trim());
				songProfile.getSongRecord().setFeaturingArtists(artistNames);
			} else if (COLUMN_RELEASE_TITLE.getColumnId() == type) {
				try {
					songProfile.setReleaseTitle(value.toString());
					songProfile.save();
				} catch (AlreadyExistsException ae) {
					QApplication.invokeAndWait(new AlreadyExistsHandleThread(songProfile, ae.getId()));
				} catch (Exception e) {
					log.error("execute(): error", e);
				}				
				return;
			} else if (COLUMN_TRACK.getColumnId() == type) {
				try {
					if (value != null) {
						songProfile.setReleaseTrack(value.toString());
						songProfile.save();
					} else {
						songProfile.setReleaseTrack("");
						songProfile.save();
					}
				} catch (AlreadyExistsException ae) {
					QApplication.invokeAndWait(new AlreadyExistsHandleThread(songProfile, ae.getId()));
				} catch (Exception e) {
					log.error("execute(): error", e);
				}
				return;
			} else if (COLUMN_TITLE.getColumnId() == type) {
				try {
					songProfile.setTitle(value.toString());
					songProfile.save();
				} catch (AlreadyExistsException ae) {
					QApplication.invokeAndWait(new AlreadyExistsHandleThread(songProfile, ae.getId()));
				} catch (Exception e) {
					log.error("execute(): error", e);
				}
				return;
			} else if (COLUMN_REMIX.getColumnId() == type) {
				try {
					if (value != null) {
						songProfile.setRemix(value.toString());
						songProfile.save();
					} else {
						songProfile.setRemix("");
						songProfile.save();
					}
				} catch (AlreadyExistsException ae) {
					QApplication.invokeAndWait(new AlreadyExistsHandleThread(songProfile, ae.getId()));
				} catch (Exception e) {
					log.error("execute(): error", e);
				}
				return;
			} else if (COLUMN_DURATION.getColumnId() == type) {
				songProfile.setDuration(new Duration(value.toString()), DATA_SOURCE_USER);
			} else if (COLUMN_LABELS.getColumnId() == type) {
				if (value != null) {
					String labelDescription = value.toString();
					StringTokenizer tokenizer = new StringTokenizer(labelDescription, ";");
					Vector<String> labelNames = new Vector<String>();
					while (tokenizer.hasMoreTokens())
						labelNames.add(tokenizer.nextToken().trim());
					songProfile.setLabelNames(labelNames);
				} else {
					songProfile.setLabelNames(new Vector<String>());
				}
			} else if (COLUMN_ORIGINAL_YEAR.getColumnId() == type) {
				try {
					songProfile.setOriginalYearReleased(Short.parseShort(value.toString()), DATA_SOURCE_USER);
				} catch (NumberFormatException nfe) { }
			} else if (COLUMN_KEY_START.getColumnId() == type) {
				if ((value == null) || value.toString().equals(""))
					songProfile.setKey(Key.NO_KEY, songProfile.getEndKey(), (byte)0, DATA_SOURCE_USER);
				else {
					songProfile.setKey(Key.getKey(value.toString()), songProfile.getEndKey(), SET_ACCURACY_TO_100_AUTOMATICALLY ? (byte)100 : songProfile.getKeyAccuracy(), DATA_SOURCE_USER);
				}
				ProfileWidgetUI.instance.getStageWidget().setCurrentSong(songProfile);
			} else if (COLUMN_KEY_END.getColumnId() == type) {
				if ((value == null) || value.toString().equals(""))
					songProfile.setKey(songProfile.getStartKey(), Key.NO_KEY, songProfile.getKeyAccuracy(), DATA_SOURCE_USER);
				else
					songProfile.setKey(songProfile.getStartKey(), Key.getKey(value.toString()), SET_ACCURACY_TO_100_AUTOMATICALLY ? (byte)100 : songProfile.getKeyAccuracy(), DATA_SOURCE_USER);
				ProfileWidgetUI.instance.getStageWidget().setCurrentSong(songProfile);
			} else if (COLUMN_KEY_ACCURACY.getColumnId() == type) {
				songProfile.setKey(songProfile.getStartKey(), songProfile.getEndKey(), ((Accuracy)value).getAccuracy(), DATA_SOURCE_USER);
			} else if (COLUMN_BPM_START.getColumnId() == type) {
				try {
					if ((value == null) || value.toString().equals(""))
						songProfile.setBpm(Bpm.NO_BPM, songProfile.getBpmEnd(), (byte)0, DATA_SOURCE_USER);
					else if (value.equals(""))
						songProfile.setBpm(new Bpm(0.0f), songProfile.getBpmEnd(), songProfile.getBpmAccuracy(), DATA_SOURCE_USER);
					else
						songProfile.setBpm(new Bpm(Float.parseFloat(value.toString())), songProfile.getBpmEnd(), SET_ACCURACY_TO_100_AUTOMATICALLY ? (byte)100 : songProfile.getBpmAccuracy(), DATA_SOURCE_USER);
					ProfileWidgetUI.instance.getStageWidget().setCurrentSong(songProfile);
				} catch (java.lang.NumberFormatException nfe) { }
			} else if (COLUMN_BPM_END.getColumnId() == type) {
				try { 
					if ((value == null) || value.toString().equals(""))
						songProfile.setBpm(songProfile.getBpmStart(), Bpm.NO_BPM, songProfile.getKeyAccuracy(), DATA_SOURCE_USER);
					else if (value.equals(""))
						songProfile.setBpm(songProfile.getBpmStart(), new Bpm(0.0f), songProfile.getBpmAccuracy(), DATA_SOURCE_USER);
					else
						songProfile.setBpm(songProfile.getBpmStart(), new Bpm(Float.parseFloat(value.toString())), SET_ACCURACY_TO_100_AUTOMATICALLY ? (byte)100 : songProfile.getBpmAccuracy(), DATA_SOURCE_USER);
					ProfileWidgetUI.instance.getStageWidget().setCurrentSong(songProfile);
				} catch (java.lang.NumberFormatException nfe) { }
			} else if (COLUMN_BPM_ACCURACY.getColumnId() == type) {
				songProfile.setBpm(songProfile.getBpmStart(), songProfile.getBpmEnd(), ((Accuracy)value).getAccuracy(), DATA_SOURCE_USER);
			} else if (COLUMN_TIME_SIGNATURE.getColumnId() == type) {
				songProfile.setTimeSig(TimeSig.getTimeSig(value.toString()), DATA_SOURCE_USER);
			} else if (COLUMN_BEAT_INTENSITY_VALUE.getColumnId() == type) {
				if (value == null)
					songProfile.setBeatIntensity(BeatIntensity.NO_BEAT_INTENSITY, DATA_SOURCE_USER);
				else
					songProfile.setBeatIntensity((BeatIntensity)value, DATA_SOURCE_USER);
			} else if (COLUMN_NUM_PLAYS.getColumnId() == type) {
				try {
					if (value == null)
						songProfile.setPlayCount(0);
					else
						songProfile.setPlayCount(Long.parseLong(value.toString()));
				} catch (NumberFormatException e) { }
			} else if (COLUMN_FILEPATH.getColumnId() == type) {
				songProfile.setSongFilename(value.toString());
			} else if (COLUMN_REPLAY_GAIN.getColumnId() == type) {
				try {
					songProfile.setReplayGain(Float.parseFloat(value.toString()), DATA_SOURCE_USER);
				} catch (NumberFormatException e) { }
			} else {
				super.setFieldValue(column, value, songProfile);
			}
			ProfileSaveTask.save(songProfile);			
		} catch (Exception e) {
			log.error("setFieldValue(): error", e);
		}
	}

	public void write(LineWriter writer) {
		super.write(writer);
		writer.writeLine(1); // version
	}
	
}
