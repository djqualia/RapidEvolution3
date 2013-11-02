package com.mixshare.rapid_evolution.ui.model.profile.search.song;

import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.search.song.MixoutIdentifier;
import com.mixshare.rapid_evolution.data.profile.search.song.MixoutProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.search.song.MixoutRecord;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.data.submitted.search.song.SubmittedMixout;
import com.mixshare.rapid_evolution.music.bpm.Bpm;
import com.mixshare.rapid_evolution.music.bpm.BpmDifference;
import com.mixshare.rapid_evolution.music.key.Key;
import com.mixshare.rapid_evolution.ui.model.ModelPopulatorInterface;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.column.StaticTypeColumn;
import com.mixshare.rapid_evolution.ui.model.column.comparables.SmartInteger;
import com.mixshare.rapid_evolution.ui.model.column.comparables.SmartString;
import com.mixshare.rapid_evolution.ui.widgets.common.rating.StarRating;
import com.mixshare.rapid_evolution.ui.widgets.profile.ProfileWidgetUI;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.trolltech.qt.core.QObject;

public class SongMixoutsModelManager extends RelativeSongModelManager implements ModelPopulatorInterface {

    static private Logger log = Logger.getLogger(SongMixoutsModelManager.class);	
    static private final long serialVersionUID = 0L;    

    static public StaticTypeColumn[] ALL_COLUMNS = {		
		COLUMN_SONG_DESCRIPTION.getInstance(true),
		COLUMN_ARTIST_DESCRIPTION.getInstance(false),
		COLUMN_RELEASE_TITLE.getInstance(false),
		COLUMN_RELEASE_TITLES.getInstance(false),
		COLUMN_TITLE.getInstance(false),
		COLUMN_REMIX.getInstance(false),
		COLUMN_THUMBNAIL_IMAGE.getInstance(false),
		COLUMN_SIMILARITY.getInstance(true),
		COLUMN_MIXOUT_RATING_STARS.getInstance(true),
		COLUMN_MIXOUT_RATING_VALUE.getInstance(false),
		COLUMN_RATING_STARS.getInstance(false),
		COLUMN_RATING_VALUE.getInstance(false),
		COLUMN_DURATION.getInstance(false),
		COLUMN_TIME_SIGNATURE.getInstance(false),
		COLUMN_BPM.getInstance(true),
		COLUMN_BPM_START.getInstance(false),
		COLUMN_BPM_END.getInstance(false),
		COLUMN_MIXOUT_BPM_DIFF.getInstance(true),
		COLUMN_BPM_SHIFT.getInstance(false),
		COLUMN_BPM_ACCURACY.getInstance(false),
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
		COLUMN_BEAT_INTENSITY_DESCRIPTION.getInstance(false),
		COLUMN_BEAT_INTENSITY_VALUE.getInstance(false),
		COLUMN_NUM_PLAYS.getInstance(false),
		COLUMN_NUM_MIXOUTS.getInstance(false),
		//COLUMN_NUM_ADDONS.getInstance(false),
		COLUMN_LABELS.getInstance(false),
		COLUMN_ORIGINAL_YEAR.getInstance(false),
		COLUMN_MIXOUT_TYPE.getInstance(true),
		COLUMN_MIXOUT_COMMENTS.getInstance(true),				
		COLUMN_COMMENTS.getInstance(false),
		COLUMN_STYLES.getInstance(false),
		COLUMN_TAGS.getInstance(false),
		COLUMN_DATE_ADDED.getInstance(false),
		COLUMN_LAST_MODIFIED.getInstance(false),
		COLUMN_FILENAME.getInstance(false),
		COLUMN_FILEPATH.getInstance(false)
    };    
    
    ////////////
    // FIELDS //
    ////////////
    
    
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
	public SongMixoutsModelManager() { }
	
	public SongMixoutsModelManager(LineReader lineReader) {
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
	
	public String getTypeDescription() { return "Mixout"; }
	
	public Object getSourceData(short columnId, Object record) {
		if (log.isTraceEnabled())
			log.trace("getSourceData(): columnId=" + columnId + ", record=" + record);		
		SongRecord songRecord = (SongRecord)record;		
		MixoutIdentifier mixoutId = new MixoutIdentifier(relativeSong.getSongRecord().getUniqueId(), songRecord.getUniqueId());
		MixoutRecord mixoutRecord = (MixoutRecord)Database.getRecord(mixoutId);
		if (mixoutRecord == null) {
			try {
				SongProfile currentSong = (SongProfile)ProfileWidgetUI.instance.getCurrentProfile();
				for (MixoutProfile mixoutProfile : currentSong.getMixoutProfiles()) {
					if (mixoutProfile.getMixoutIdentifier().equals(mixoutId)) {
						SubmittedMixout submittedMixout = new SubmittedMixout(mixoutId, mixoutProfile);
						MixoutProfile actualMixout = (MixoutProfile)Database.getMixoutIndex().add(submittedMixout);
						mixoutRecord = actualMixout.getMixoutRecord();
						break;
					}
				}
			} catch (Exception e) {
				log.error("getSourceData(): error", e);
			}
		}
		// can insert model specific columns here
		if (columnId == COLUMN_MIXOUT_BPM_DIFF.getColumnId()) {
			return new BpmDifference(mixoutRecord.getBpmDiff());
		} else if (columnId == COLUMN_BPM_SHIFT.getColumnId()) {
			return new BpmDifference(mixoutRecord.getBpmDiff() + ProfileWidgetUI.instance.getStageWidget().getCurrentBpmShift());
		} else if (columnId == COLUMN_MIXOUT_TYPE.getColumnId()) {
			return new SmartString(mixoutRecord.getMixoutTypeDescription());
		} else if (columnId == COLUMN_MIXOUT_RATING_STARS.getColumnId()) {
			return new StarRating(mixoutRecord.getRatingValue());
		} else if (columnId == COLUMN_MIXOUT_RATING_VALUE.getColumnId()) {
			return new SmartInteger(mixoutRecord.getRatingValue().getRatingValue());
		} else if (columnId == COLUMN_MIXOUT_COMMENTS.getColumnId()) {
			return new SmartString(mixoutRecord.getComments());
		}
		return super.getSourceData(columnId, record);
	}
	
	public ModelPopulatorInterface getModelPopulator() { 
		if (log.isTraceEnabled())
			log.trace("getModelPopulator(): called");		
		return this; 
	}
	
	public int getSize() {
		int result = relativeSong.getMixouts().size();
		if (log.isDebugEnabled())
			log.debug("getSize(): result=" + result);
		return result;
	}
	
	public Iterator<Integer> getIdsIterator() {
		if (log.isDebugEnabled())
			log.debug("refresh(): called");
		Vector<Integer> result = new Vector<Integer>(getSize());
		for (MixoutRecord mixout : relativeSong.getMixouts()) {
			SongRecord toSong = (SongRecord)Database.getSongIndex().getRecord(mixout.getMixoutIdentifier().getToSongIdentifier());
			if (toSong != null) {
				boolean skip = false;
				if (RE3Properties.getBoolean("prevent_repeat_songplay")) {
					for (Record record : ProfileWidgetUI.instance.getProfileTrailToCurrent())
						if (record instanceof SongRecord)
							if (record.getUniqueId() == toSong.getUniqueId())
								skip = true;
				}
				if (!skip)
					result.add(toSong.getUniqueId());
			}
		}
		return result.iterator();
	}
	
	/////////////
	// SETTERS //
	/////////////
	
	public void setRelativeSong(SongProfile relativeSong) {
		super.setRelativeSong(relativeSong);
	}
	
	/////////////
	// METHODS //
	/////////////
	
	protected void createSourceModel(QObject parent) {
		model = new SongMixoutTableItemModel(getSize(), getNumColumns(), parent, this);
		loadTable();
	}
	
	public void refresh() {
		if (log.isDebugEnabled())
			log.debug("refresh(): called");
		Bpm targetBpm = ProfileWidgetUI.instance.getStageWidget().getCurrentBpm();
		Key targetKey = ProfileWidgetUI.instance.getStageWidget().getCurrentKey();
		if (targetBpm.isValid())
			setTargetBpm(targetBpm);
		if (targetKey.isValid())
			setTargetKey(targetKey);
		if (getSourceModel() != null) {
			resetModel();
			loadTable();
		}
	}	

	public void write(LineWriter writer) {
		super.write(writer);
		writer.writeLine(1); // version
	}	
	
}
