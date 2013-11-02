package com.mixshare.rapid_evolution.ui.model.profile.search.artist;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.profile.search.artist.ArtistProfile;
import com.mixshare.rapid_evolution.data.record.search.artist.ArtistRecord;
import com.mixshare.rapid_evolution.ui.model.ModelPopulatorInterface;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.column.StaticTypeColumn;
import com.mixshare.rapid_evolution.ui.model.column.comparables.Percentage;
import com.mixshare.rapid_evolution.ui.model.profile.SimilarProfilesModel;
import com.mixshare.rapid_evolution.ui.model.profile.search.SimilarModelManagerInterface;
import com.mixshare.rapid_evolution.ui.model.search.artist.ArtistModelManager;
import com.mixshare.rapid_evolution.ui.model.table.RecordTableModelManager;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

public class SimilarArtistsModelManager extends ArtistModelManager implements SimilarModelManagerInterface {

    static private Logger log = Logger.getLogger(SimilarArtistsModelManager.class);	
    static private final long serialVersionUID = 0L;    

    static public StaticTypeColumn[] ALL_COLUMNS = {
		COLUMN_ARTIST_NAME.getInstance(true),
		COLUMN_THUMBNAIL_IMAGE.getInstance(true),
		COLUMN_SIMILARITY.getInstance(true),
		COLUMN_RATING_STARS.getInstance(true),
		COLUMN_RATING_VALUE.getInstance(false),
		COLUMN_NUM_SONGS.getInstance(false),
		COLUMN_COMMENTS.getInstance(false),
		COLUMN_STYLES.getInstance(true),
		COLUMN_TAGS.getInstance(true),
		COLUMN_LAST_MODIFIED.getInstance(false)
    };    
    
    ////////////
    // FIELDS //
    ////////////
    
    transient private ArtistProfile relativeArtist;
    transient private SimilarProfilesModel similarProfiles;
    
    static {
    	try{
    		// in order for the XMLEncoder to skip transient variables, this is needed
    		BeanInfo info = Introspector.getBeanInfo(SimilarArtistsModelManager.class);
    		PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
    		for (int i = 0; i < propertyDescriptors.length; ++i) {
    			PropertyDescriptor pd = propertyDescriptors[i];
    			if (pd.getName().equals("relativeArtist") || pd.getName().equals("similarProfiles")) {
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
    
	public SimilarArtistsModelManager() { }
	
	public SimilarArtistsModelManager(LineReader lineReader) {
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
	
	public String getTypeDescription() { return "Similar Artist"; }
	
	public Object getSourceData(short columnId, Object record) {
		if (log.isTraceEnabled())
			log.trace("getColumnData(): columnId=" + columnId + ", record=" + record);		
		ArtistRecord artistRecord = (ArtistRecord)record;
		if (columnId == COLUMN_SIMILARITY.getColumnId())
			return new Percentage(relativeArtist.getSimilarity(artistRecord));
		return super.getSourceData(columnId, record);
	}
	
	public ModelPopulatorInterface getModelPopulator() {
		if (similarProfiles == null)
			similarProfiles = new SimilarProfilesModel(relativeArtist, Database.getArtistIndex());
		return similarProfiles;		
	}
	
	public RecordTableModelManager getTableModelManager() { return this; }		
		
	public boolean excludeExternalItems() { return false; }
		
	public boolean isLazySearchSupported() { return false; }
	
	/////////////
	// SETTERS //
	/////////////
	
	public void setRelativeArtist(ArtistProfile relativeArtist) {
		this.relativeArtist = relativeArtist;
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
