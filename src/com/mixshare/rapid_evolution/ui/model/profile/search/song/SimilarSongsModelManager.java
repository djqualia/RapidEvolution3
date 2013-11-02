package com.mixshare.rapid_evolution.ui.model.profile.search.song;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.ui.model.ModelPopulatorInterface;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.column.StaticTypeColumn;
import com.mixshare.rapid_evolution.ui.model.profile.SimilarProfilesModel;
import com.mixshare.rapid_evolution.ui.model.profile.search.SimilarModelManagerInterface;
import com.mixshare.rapid_evolution.ui.model.table.RecordTableModelManager;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

public class SimilarSongsModelManager extends RelativeSongModelManager implements SimilarModelManagerInterface {

    static private Logger log = Logger.getLogger(SimilarSongsModelManager.class);	
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
		COLUMN_RATING_STARS.getInstance(true),
		COLUMN_RATING_VALUE.getInstance(false),
		COLUMN_SCORE.getInstance(false),
		COLUMN_POPULARITY.getInstance(false),
		COLUMN_DURATION.getInstance(false),
		COLUMN_TIME_SIGNATURE.getInstance(false),
		COLUMN_BPM.getInstance(false),
		COLUMN_BPM_START.getInstance(false),
		COLUMN_BPM_END.getInstance(false),
		COLUMN_BPM_DIFFERENCE.getInstance(false),
		COLUMN_BPM_SHIFT.getInstance(false),
		COLUMN_BPM_ACCURACY.getInstance(false),
		COLUMN_KEY.getInstance(false),
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
		COLUMN_COMMENTS.getInstance(false),
		COLUMN_STYLES.getInstance(true),
		COLUMN_TAGS.getInstance(true),
		COLUMN_DATE_ADDED.getInstance(false),
		COLUMN_LAST_MODIFIED.getInstance(false),
		COLUMN_FILENAME.getInstance(false),
		COLUMN_FILEPATH.getInstance(false)
    };    
    
    ////////////
    // FIELDS //
    ////////////
    
    transient private SimilarProfilesModel similarProfiles;
    
    static {
    	try{
    		// in order for the XMLEncoder to skip transient variables, this is needed
    		BeanInfo info = Introspector.getBeanInfo(SimilarSongsModelManager.class);
    		PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
    		for (int i = 0; i < propertyDescriptors.length; ++i) {
    			PropertyDescriptor pd = propertyDescriptors[i];
    			if (pd.getName().equals("similarProfiles")) {
    				pd.setValue("transient", Boolean.TRUE);
    			}
    		}
    	} catch (IntrospectionException e) {
    		log.error("static(): error", e);
    	}
    }
    
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
	public SimilarSongsModelManager() { }
	
	public SimilarSongsModelManager(LineReader lineReader) {
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
	
	public String getTypeDescription() { return "Similar Song"; }
	
	public Object getSourceData(short columnId, Object record) {
		if (log.isTraceEnabled())
			log.trace("getColumnData(): columnId=" + columnId + ", record=" + record);		
		SongRecord songRecord = (SongRecord)record;
		// can insert model specific columns here
		return super.getSourceData(columnId, record);
	}
	
	public ModelPopulatorInterface getModelPopulator() {
		if (similarProfiles == null)
			similarProfiles = new SimilarProfilesModel(relativeSong, Database.getSongIndex());
		return similarProfiles;
	}
	
	public RecordTableModelManager getTableModelManager() { return this; }		
	
	/////////////
	// SETTERS //
	/////////////
	
	public void setRelativeSong(SongProfile relativeSong) {
		super.setRelativeSong(relativeSong);
		similarProfiles = null;
	}
	
	/////////////
	// METHODS //
	/////////////
	
	public void reset() {
		similarProfiles = null;
		super.reset();		
	}

	public void write(LineWriter writer) {
		super.write(writer);
		writer.writeLine(1); // version
	}	

}
