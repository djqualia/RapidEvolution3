package com.mixshare.rapid_evolution.ui.model.search.release;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.index.Index;
import com.mixshare.rapid_evolution.data.record.search.release.ReleaseRecord;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.column.StaticTypeColumn;
import com.mixshare.rapid_evolution.ui.model.column.comparables.SmartInteger;
import com.mixshare.rapid_evolution.ui.model.column.comparables.SmartString;
import com.mixshare.rapid_evolution.ui.model.profile.details.SearchDetailsModelManager;
import com.mixshare.rapid_evolution.ui.model.profile.search.release.ReleaseDetailsModelManager;
import com.mixshare.rapid_evolution.ui.model.search.SongGroupModelManager;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.trolltech.qt.gui.QCompleter;

public class ReleaseModelManager extends SongGroupModelManager {

    static private Logger log = Logger.getLogger(ReleaseModelManager.class);	
    static private final long serialVersionUID = 0L;    
				
    static public StaticTypeColumn[] ALL_COLUMNS = {     	
		COLUMN_RELEASE_DESCRIPTION.getInstance(true),
		COLUMN_ARTIST_DESCRIPTION.getInstance(false),
		COLUMN_RELEASE_TITLE.getInstance(false),
		COLUMN_THUMBNAIL_IMAGE.getInstance(true),
		COLUMN_RATING_STARS.getInstance(true),
		COLUMN_RATING_VALUE.getInstance(false),
		COLUMN_PREFERENCE.getInstance(false),
		COLUMN_SCORE.getInstance(false),
		COLUMN_POPULARITY.getInstance(false),
		COLUMN_FILTERS_MATCH.getInstance(true),
		COLUMN_STYLES_MATCH.getInstance(false),
		COLUMN_TAGS_MATCH.getInstance(false),
		COLUMN_SIMILARITY.getInstance(false),
		COLUMN_NUM_SONGS.getInstance(false),
		COLUMN_NUM_PLAYS.getInstance(false),
		COLUMN_AVERAGE_BEAT_INTENSITY_DESCRIPTION.getInstance(false),
		COLUMN_AVERAGE_BEAT_INTENSITY_VALUE.getInstance(false),
		COLUMN_BEAT_INTENSITY_VARIANCE_DESCRIPTION.getInstance(false),
		COLUMN_BEAT_INTENSITY_VARIANCE_VALUE.getInstance(false),
		COLUMN_LABELS.getInstance(true),
		COLUMN_ORIGINAL_YEAR.getInstance(true),
		COLUMN_RELEASE_IS_COMPILATION.getInstance(false),
		COLUMN_COMMENTS.getInstance(true),
		COLUMN_STYLES.getInstance(false),
		COLUMN_TAGS.getInstance(false),
		COLUMN_DATE_ADDED.getInstance(false),
		COLUMN_LAST_MODIFIED.getInstance(false),
		COLUMN_HAS_LASTFM_PROFILE.getInstance(false),
		COLUMN_HAS_MUSICBRAINZ_PROFILE.getInstance(false),
		COLUMN_HAS_DISCOGS_PROFILE.getInstance(false),
		COLUMN_HAS_MIXSHARE_PROFILE.getInstance(false)
    };
    
    ////////////
    // FIELDS //
    ////////////
    
    transient private ReleaseTitleCompleterFactory releaseTitleCompleter;
    
    static {
    	try{
    		// in order for the XMLEncoder to skip transient variables, this is needed
    		BeanInfo info = Introspector.getBeanInfo(ReleaseModelManager.class);
    		PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
    		for (int i = 0; i < propertyDescriptors.length; ++i) {
    			PropertyDescriptor pd = propertyDescriptors[i];
    			if (pd.getName().equals("releaseTitleCompleter")) {
    				pd.setValue("transient", Boolean.TRUE);
    			}
    		}
    	} catch (IntrospectionException e) {
    		log.error("static(): error", e);
    	}
    }
    
    //////////////////
    // CONSTRUCTION //
    //////////////////
    
	public ReleaseModelManager() {
		setPrimarySortColumn(COLUMN_RELEASE_DESCRIPTION.getColumnId());
	}
	
	public ReleaseModelManager(LineReader lineReader) {
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
	public String getTypeDescription() { return Translations.get("my_release_model_type_text"); }
		
	public Object getSourceData(short columnId, Object record) {
		if (log.isTraceEnabled())
			log.trace("getColumnData(): columnId=" + columnId + ", record=" + record);
		ReleaseRecord releaseRecord = (ReleaseRecord)record;
		if (columnId == COLUMN_RELEASE_DESCRIPTION.getColumnId())
			return releaseRecord.toString();
		if (columnId == COLUMN_ARTIST_DESCRIPTION.getColumnId())
			return new SmartString(releaseRecord.getArtistsDescription());
		if (columnId == COLUMN_RELEASE_TITLE.getColumnId())
			return new SmartString(releaseRecord.getReleaseTitle());
		if (columnId == COLUMN_RELEASE_IS_COMPILATION.getColumnId())
			return new Boolean(releaseRecord.isCompilationRelease()).toString();
		if (columnId == COLUMN_LABELS.getColumnId())
			return new SmartString(releaseRecord.getLabelsDescription());
		if (columnId == COLUMN_ORIGINAL_YEAR.getColumnId())
			return new SmartInteger(releaseRecord.getOriginalYearReleased(), true);
		return super.getSourceData(columnId, record);
	}
	
	public Index getIndex() { return Database.getReleaseIndex(); }
	
	public SearchDetailsModelManager getDetailsModelManager() { return (ReleaseDetailsModelManager)Database.getRelativeModelFactory().getRelativeModelManager(ReleaseDetailsModelManager.class); }	
	
	public boolean excludeExternalItems() { return true; }
	
	public QCompleter getReleaseTitleCompleter() {
		if (releaseTitleCompleter == null)
			releaseTitleCompleter = new ReleaseTitleCompleterFactory();
		return releaseTitleCompleter.getCompleter();
	}
	
	public boolean isLazySearchSupported() { return true; }

	public void write(LineWriter writer) {
		super.write(writer);
		writer.writeLine(1); // version
	}
	
}
