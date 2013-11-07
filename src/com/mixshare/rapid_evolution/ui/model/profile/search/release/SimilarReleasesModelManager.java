package com.mixshare.rapid_evolution.ui.model.profile.search.release;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.profile.search.release.ReleaseProfile;
import com.mixshare.rapid_evolution.data.record.search.release.ReleaseRecord;
import com.mixshare.rapid_evolution.ui.model.ModelPopulatorInterface;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.column.StaticTypeColumn;
import com.mixshare.rapid_evolution.ui.model.column.comparables.Percentage;
import com.mixshare.rapid_evolution.ui.model.profile.SimilarProfilesModel;
import com.mixshare.rapid_evolution.ui.model.profile.search.SimilarModelManagerInterface;
import com.mixshare.rapid_evolution.ui.model.search.release.ReleaseModelManager;
import com.mixshare.rapid_evolution.ui.model.table.RecordTableModelManager;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

public class SimilarReleasesModelManager extends ReleaseModelManager implements SimilarModelManagerInterface {

    static private Logger log = Logger.getLogger(SimilarReleasesModelManager.class);	
    static private final long serialVersionUID = 0L;    

    static public StaticTypeColumn[] ALL_COLUMNS = {		
		COLUMN_RELEASE_DESCRIPTION.getInstance(true),
		COLUMN_ARTIST_DESCRIPTION.getInstance(false),
		COLUMN_RELEASE_TITLE.getInstance(false),
		COLUMN_THUMBNAIL_IMAGE.getInstance(true),
		COLUMN_SIMILARITY.getInstance(true),
		COLUMN_RATING_STARS.getInstance(true),
		COLUMN_RATING_VALUE.getInstance(false),
		COLUMN_NUM_SONGS.getInstance(false),
		COLUMN_LABELS.getInstance(false),
		COLUMN_ORIGINAL_YEAR.getInstance(false),
		COLUMN_COMMENTS.getInstance(false),
		COLUMN_STYLES.getInstance(true),
		COLUMN_TAGS.getInstance(true),
		COLUMN_LAST_MODIFIED.getInstance(false)		
    };    
    
    ////////////
    // FIELDS //
    ////////////
    
    transient private ReleaseProfile relativeRelease;
    transient private SimilarProfilesModel similarProfiles;
    
    static {
    	try{
    		// in order for the XMLEncoder to skip transient variables, this is needed
    		BeanInfo info = Introspector.getBeanInfo(SimilarReleasesModelManager.class);
    		PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
    		for (int i = 0; i < propertyDescriptors.length; ++i) {
    			PropertyDescriptor pd = propertyDescriptors[i];
    			if (pd.getName().equals("relativeRelease") || pd.getName().equals("similarProfiles")) {
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
    
	public SimilarReleasesModelManager() { }
	
	public SimilarReleasesModelManager(LineReader lineReader) {
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
	
	public String getTypeDescription() { return "Similar Release"; }
	
	public Object getSourceData(short columnId, Object record) {
		if (log.isTraceEnabled())
			log.trace("getColumnData(): columnId=" + columnId + ", record=" + record);		
		ReleaseRecord releaseRecord = (ReleaseRecord)record;
		if (columnId == COLUMN_SIMILARITY.getColumnId())
			return new Percentage(relativeRelease.getSimilarity(releaseRecord));
		return super.getSourceData(columnId, record);
	}
	
	public ModelPopulatorInterface getModelPopulator() {
		if (similarProfiles == null)
			similarProfiles = new SimilarProfilesModel(relativeRelease, Database.getReleaseIndex());
		return similarProfiles;
	}
	
	public RecordTableModelManager getTableModelManager() { return this; }		
	
	public boolean excludeExternalItems() { return false; }
	
	public boolean isLazySearchSupported() { return false; }
	
	/////////////
	// SETTERS //
	/////////////
	
	public void setRelativeRelease(ReleaseProfile relativeRelease) {
		this.relativeRelease = relativeRelease;
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
