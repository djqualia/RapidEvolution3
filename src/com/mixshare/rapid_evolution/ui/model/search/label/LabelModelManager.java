package com.mixshare.rapid_evolution.ui.model.search.label;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.index.Index;
import com.mixshare.rapid_evolution.data.record.search.label.LabelRecord;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.column.StaticTypeColumn;
import com.mixshare.rapid_evolution.ui.model.profile.details.SearchDetailsModelManager;
import com.mixshare.rapid_evolution.ui.model.profile.search.label.LabelDetailsModelManager;
import com.mixshare.rapid_evolution.ui.model.search.ReleaseGroupModelManager;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.trolltech.qt.gui.QCompleter;

public class LabelModelManager extends ReleaseGroupModelManager {
	
    static private Logger log = Logger.getLogger(LabelModelManager.class);	
    static private final long serialVersionUID = 0L;    
				
    static public StaticTypeColumn[] ALL_COLUMNS = {
		COLUMN_LABEL_NAME.getInstance(true),
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
		COLUMN_NUM_RELEASES.getInstance(false),
		COLUMN_NUM_SONGS.getInstance(false),
		COLUMN_NUM_PLAYS.getInstance(false),
		COLUMN_AVERAGE_BEAT_INTENSITY_DESCRIPTION.getInstance(false),
		COLUMN_AVERAGE_BEAT_INTENSITY_VALUE.getInstance(false),
		COLUMN_BEAT_INTENSITY_VARIANCE_DESCRIPTION.getInstance(false),
		COLUMN_BEAT_INTENSITY_VARIANCE_VALUE.getInstance(false),
		COLUMN_COMMENTS.getInstance(true),
		COLUMN_STYLES.getInstance(false),
		COLUMN_TAGS.getInstance(false),
		COLUMN_DATE_ADDED.getInstance(false),
		COLUMN_LAST_MODIFIED.getInstance(false),
		COLUMN_HAS_MUSICBRAINZ_PROFILE.getInstance(false),
		COLUMN_HAS_DISCOGS_PROFILE.getInstance(false),
		COLUMN_HAS_MIXSHARE_PROFILE.getInstance(false)
    };
    
    ////////////
    // FIELDS //
    ////////////
    
    transient private LabelCompleterFactory labelCompleter;
    
    static {
    	try{
    		// in order for the XMLEncoder to skip transient variables, this is needed
    		BeanInfo info = Introspector.getBeanInfo(LabelModelManager.class);
    		PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
    		for (int i = 0; i < propertyDescriptors.length; ++i) {
    			PropertyDescriptor pd = propertyDescriptors[i];
    			if (pd.getName().equals("labelCompleter")) {
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
    
	public LabelModelManager() {
		setPrimarySortColumn(COLUMN_LABEL_NAME.getColumnId());
	}
	
	public LabelModelManager(LineReader lineReader) {
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
	public String getTypeDescription() { return Translations.get("my_label_model_type_text"); }
		
	public Object getSourceData(short columnId, Object record) {
		if (log.isTraceEnabled())
			log.trace("getColumnData(): columnId=" + columnId + ", record=" + record);		
		LabelRecord labelRecord = (LabelRecord)record;
		if (columnId == COLUMN_LABEL_NAME.getColumnId())
			return labelRecord.getLabelName();
		return super.getSourceData(columnId, record);
	}
	
	public Index getIndex() { return Database.getLabelIndex(); }
	
	public SearchDetailsModelManager getDetailsModelManager() { return (LabelDetailsModelManager)Database.getRelativeModelFactory().getRelativeModelManager(LabelDetailsModelManager.class); }
	
	public boolean excludeExternalItems() { return true; }
	
	public QCompleter getLabelCompleter() {
		if (labelCompleter == null)
			labelCompleter = new LabelCompleterFactory();
		return labelCompleter.getCompleter();
	}
	
	public boolean isLazySearchSupported() { return true; }

	public void write(LineWriter writer) {
		super.write(writer);
		writer.writeLine(1); // version
	}
	
}
