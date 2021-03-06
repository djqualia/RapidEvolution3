package com.mixshare.rapid_evolution.ui.model.profile.filter.style;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.profile.filter.style.StyleProfile;
import com.mixshare.rapid_evolution.data.record.search.release.ReleaseRecord;
import com.mixshare.rapid_evolution.ui.model.ModelPopulatorInterface;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.column.StaticTypeColumn;
import com.mixshare.rapid_evolution.ui.model.column.comparables.Percentage;
import com.mixshare.rapid_evolution.ui.model.profile.filter.FilterProfileReleasesModel;
import com.mixshare.rapid_evolution.ui.model.search.release.ReleaseModelManager;
import com.mixshare.rapid_evolution.ui.widgets.profile.filter.FilterTabTableItemModel;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.trolltech.qt.core.QObject;

public class StyleReleaseModelManager extends ReleaseModelManager {

    static private Logger log = Logger.getLogger(StyleReleaseModelManager.class);	
    static private final long serialVersionUID = 0L;    
    
    static public StaticTypeColumn[] ALL_COLUMNS = {     	
		COLUMN_RELEASE_DESCRIPTION.getInstance(true),
		COLUMN_ARTIST_DESCRIPTION.getInstance(false),
		COLUMN_RELEASE_TITLE.getInstance(false),
		COLUMN_THUMBNAIL_IMAGE.getInstance(true),
		COLUMN_DEGREE.getInstance(true),
		COLUMN_RATING_STARS.getInstance(true),
		COLUMN_RATING_VALUE.getInstance(false),
		COLUMN_SCORE.getInstance(true),
		COLUMN_POPULARITY.getInstance(true),
		COLUMN_NUM_SONGS.getInstance(true),
		COLUMN_LABELS.getInstance(true),
		COLUMN_ORIGINAL_YEAR.getInstance(true),
		COLUMN_COMMENTS.getInstance(true),
		COLUMN_STYLES.getInstance(false),
		COLUMN_TAGS.getInstance(false),
		COLUMN_LAST_MODIFIED.getInstance(false)
    };
    
    ////////////
    // FIELDS //
    ////////////
    
    transient private FilterProfileReleasesModel profileReleasesModel;
    transient private StyleProfile relativeStyle;
    
    static {
    	try{
    		// in order for the XMLEncoder to skip transient variables, this is needed
    		BeanInfo info = Introspector.getBeanInfo(StyleReleaseModelManager.class);
    		PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
    		for (int i = 0; i < propertyDescriptors.length; ++i) {
    			PropertyDescriptor pd = propertyDescriptors[i];
    			if (pd.getName().equals("profileReleasesModel") || pd.getName().equals("relativeStyle")) {
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
    
	public StyleReleaseModelManager() { }
	
	public StyleReleaseModelManager(LineReader lineReader) {
		super(lineReader);
		int version = Integer.parseInt(lineReader.getNextLine());
	}		
	
	public void initColumns() {
		sourceColumns.clear();
		for (Column column : ALL_COLUMNS)
			sourceColumns.add(column);		
	}
	
	protected void createSourceModel(QObject parent) {
		model = new FilterTabTableItemModel(getModelPopulator().getSize(), getNumColumns(), parent, this);
		loadTable();
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public StaticTypeColumn[] getAllStaticColumns() { return ALL_COLUMNS; }
	
	public String getTypeDescription() { return "Style Release"; }
	
	public Object getSourceData(short columnId, Object record) {
		if (log.isTraceEnabled())
			log.trace("getColumnData(): columnId=" + columnId + ", record=" + record);		
		ReleaseRecord releaseRecord = (ReleaseRecord)record;
		// can insert model specific columns here
		if (columnId == COLUMN_DEGREE.getColumnId()) {
			return new Percentage(releaseRecord.getActualStyleDegree(relativeStyle.getUniqueId()));
		}
		return super.getSourceData(columnId, record);
	}
	
	public ModelPopulatorInterface getModelPopulator() { return profileReleasesModel; }
	
	public boolean excludeExternalItems() { return false; }
	
	public boolean isLazySearchSupported() { return true; }
	
	/////////////
	// SETTERS //
	/////////////
	
	public void setRelativeStyle(StyleProfile relativeStyle) {
		profileReleasesModel = new FilterProfileReleasesModel(relativeStyle, this);
		this.relativeStyle = relativeStyle;
	}
	
	public void reset() {
		profileReleasesModel.update();
		super.reset();
	}
	
	public void refresh() {
		if (log.isDebugEnabled())
			log.debug("refresh(): called");
		try {
			loadData(profileReleasesModel.getResults(), getSearchProxyModel().getSearchParameters());
		} catch (Exception e) {
			log.error("refresh(): error", e);
		}
	}		

	/////////////
	// METHODS //
	/////////////
	
	public void write(LineWriter writer) {
		super.write(writer);
		writer.writeLine(1); // version
	}	
	
}
