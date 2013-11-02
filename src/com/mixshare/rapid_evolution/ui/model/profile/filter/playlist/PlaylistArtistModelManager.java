package com.mixshare.rapid_evolution.ui.model.profile.filter.playlist;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.profile.filter.playlist.PlaylistProfile;
import com.mixshare.rapid_evolution.data.record.search.artist.ArtistRecord;
import com.mixshare.rapid_evolution.ui.model.ModelPopulatorInterface;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.column.StaticTypeColumn;
import com.mixshare.rapid_evolution.ui.model.profile.filter.FilterProfileArtistsModel;
import com.mixshare.rapid_evolution.ui.model.search.artist.ArtistModelManager;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

public class PlaylistArtistModelManager extends ArtistModelManager {

    static private Logger log = Logger.getLogger(PlaylistArtistModelManager.class);	
    static private final long serialVersionUID = 0L;    
    
    static public StaticTypeColumn[] ALL_COLUMNS = {
		COLUMN_ARTIST_NAME.getInstance(true),
		COLUMN_THUMBNAIL_IMAGE.getInstance(true),
		COLUMN_RATING_STARS.getInstance(true),
		COLUMN_RATING_VALUE.getInstance(false),
		COLUMN_NUM_SONGS.getInstance(true),
		COLUMN_COMMENTS.getInstance(true),
		COLUMN_STYLES.getInstance(false),
		COLUMN_TAGS.getInstance(false),
		COLUMN_LAST_MODIFIED.getInstance(false)
    };   
    
    ////////////
    // FIELDS //
    ////////////
    
    transient private FilterProfileArtistsModel profileArtistsModel;    
    transient private PlaylistProfile relativePlaylist;
    
    static {
    	try{
    		// in order for the XMLEncoder to skip transient variables, this is needed
    		BeanInfo info = Introspector.getBeanInfo(PlaylistArtistModelManager.class);
    		PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
    		for (int i = 0; i < propertyDescriptors.length; ++i) {
    			PropertyDescriptor pd = propertyDescriptors[i];
    			if (pd.getName().equals("profileArtistsModel") || pd.getName().equals("relativePlaylist")) {
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
    
	public PlaylistArtistModelManager() { }
	public PlaylistArtistModelManager(LineReader lineReader) {
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
	
	public String getTypeDescription() { return "Playlist Artist"; }
	
	public Object getSourceData(short columnId, Object record) {
		if (log.isTraceEnabled())
			log.trace("getColumnData(): columnId=" + columnId + ", record=" + record);		
		ArtistRecord artistRecord = (ArtistRecord)record;
		// can insert model specific columns here
		return super.getSourceData(columnId, record);
	}
	
	public ModelPopulatorInterface getModelPopulator() { return profileArtistsModel; }
	
	public boolean excludeExternalItems() { return false; }
	
	public boolean isLazySearchSupported() { return true; }
	
	/////////////
	// SETTERS //
	/////////////
	
	public void setRelativePlaylist(PlaylistProfile relativePlaylist) {
		profileArtistsModel = new FilterProfileArtistsModel(relativePlaylist, this);
		this.relativePlaylist = relativePlaylist;
	}	
	
	public void reset() {
		profileArtistsModel.update();
		super.reset();
	}		

	public void refresh() {
		if (log.isDebugEnabled())
			log.debug("refresh(): called");
		try {
			loadData(profileArtistsModel.getResults(), getSearchProxyModel().getSearchParameters());
		} catch (Exception e) {
			log.error("refresh(): error", e);
		}
	}	
	
	/////////////
	// METHODS //
	/////////////
	
	public void write(LineWriter writer) {
		super.write(writer);
		writer.writeLine(1); // version;
	}
	
}
