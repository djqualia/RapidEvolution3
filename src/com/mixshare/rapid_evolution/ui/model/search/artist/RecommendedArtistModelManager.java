package com.mixshare.rapid_evolution.ui.model.search.artist;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.index.Index;
import com.mixshare.rapid_evolution.data.index.search.RecommendedIndexModelPopulator;
import com.mixshare.rapid_evolution.data.record.search.artist.ArtistRecord;
import com.mixshare.rapid_evolution.ui.model.ModelPopulatorInterface;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.column.StaticTypeColumn;
import com.mixshare.rapid_evolution.ui.model.column.comparables.Percentage;
import com.mixshare.rapid_evolution.ui.model.profile.details.SearchDetailsModelManager;
import com.mixshare.rapid_evolution.ui.model.profile.search.artist.ArtistDetailsModelManager;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

public class RecommendedArtistModelManager extends ArtistModelManager {

    static private Logger log = Logger.getLogger(RecommendedArtistModelManager.class);	
    static private final long serialVersionUID = 0L;    

    static public StaticTypeColumn[] ALL_COLUMNS = {
		COLUMN_ARTIST_NAME.getInstance(true),
		COLUMN_THUMBNAIL_IMAGE.getInstance(true),
		COLUMN_PREFERENCE.getInstance(true),
		COLUMN_SCORE.getInstance(false),
		COLUMN_POPULARITY.getInstance(false),
		COLUMN_FILTERS_MATCH.getInstance(false),
		COLUMN_STYLES.getInstance(false),
		COLUMN_TAGS.getInstance(false)
    }; 
    
    //////////////////
    // CONSTRUCTION //
    //////////////////
    
	public RecommendedArtistModelManager() {
		setPrimarySortColumn(COLUMN_PREFERENCE.getColumnId());
	}
	public RecommendedArtistModelManager(LineReader lineReader) {
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
	public String getTypeDescription() { return Translations.get("recommended_artist_model_type_text"); }
	
	public Object getSourceData(short columnId, Object record) {
		if (log.isTraceEnabled())
			log.trace("getColumnData(): columnId=" + columnId + ", record=" + record);		
		ArtistRecord artistRecord = (ArtistRecord)record;
		if (columnId == COLUMN_PREFERENCE.getColumnId())
			return new Percentage(Database.getUserProfile().computePreference(artistRecord));
		return super.getSourceData(columnId, record);
	}
	
	public Index getIndex() { return Database.getArtistIndex(); }
	
	public ModelPopulatorInterface getModelPopulator() {
		if (searchIndexModelPopulator == null)
			searchIndexModelPopulator = new RecommendedIndexModelPopulator(getSearchIndex()); 
		return searchIndexModelPopulator;
	}	
	
	public SearchDetailsModelManager getDetailsModelManager() { return (ArtistDetailsModelManager)Database.getRelativeModelFactory().getRelativeModelManager(ArtistDetailsModelManager.class); }
	
	public boolean excludeExternalItems() { return false; }
	public boolean excludeInternalItems() { return true; }
	
	public boolean isLazySearchSupported() { return true; }

	public void write(LineWriter writer) {
		super.write(writer);
		writer.writeLine(1); // version
	}
	
}
