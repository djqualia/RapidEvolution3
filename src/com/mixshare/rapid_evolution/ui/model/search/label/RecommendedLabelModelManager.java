package com.mixshare.rapid_evolution.ui.model.search.label;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.index.Index;
import com.mixshare.rapid_evolution.data.index.search.RecommendedIndexModelPopulator;
import com.mixshare.rapid_evolution.data.record.search.label.LabelRecord;
import com.mixshare.rapid_evolution.ui.model.ModelPopulatorInterface;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.column.StaticTypeColumn;
import com.mixshare.rapid_evolution.ui.model.column.comparables.Percentage;
import com.mixshare.rapid_evolution.ui.model.profile.details.SearchDetailsModelManager;
import com.mixshare.rapid_evolution.ui.model.profile.search.label.LabelDetailsModelManager;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

public class RecommendedLabelModelManager extends LabelModelManager {

    static private Logger log = Logger.getLogger(RecommendedLabelModelManager.class);	
    static private final long serialVersionUID = 0L;    
				
    static public StaticTypeColumn[] ALL_COLUMNS = {
		COLUMN_LABEL_NAME.getInstance(true),
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
    
	public RecommendedLabelModelManager() {
		setPrimarySortColumn(COLUMN_PREFERENCE.getColumnId());
	}
	public RecommendedLabelModelManager(LineReader lineReader) {
		super(lineReader);
		int version = Integer.parseInt(lineReader.getNextLine());
	}		
	
	public void initColumns() {
		sourceColumns.clear();
		for (Column column : ALL_COLUMNS)
			sourceColumns.add(column);
	}
	
	public boolean isLazySearchSupported() { return true; }
	
	/////////////
	// GETTERS //
	/////////////
	
	public StaticTypeColumn[] getAllStaticColumns() { return ALL_COLUMNS; }	
	public String getTypeDescription() { return Translations.get("recommended_label_model_type_text"); }
		
	public Object getSourceData(short columnId, Object record) {
		if (log.isTraceEnabled())
			log.trace("getColumnData(): columnId=" + columnId + ", record=" + record);		
		LabelRecord labelRecord = (LabelRecord)record;
		if (columnId == COLUMN_PREFERENCE.getColumnId())
			return new Percentage(Database.getUserProfile().computePreference(labelRecord));
		return super.getSourceData(columnId, record);
	}
	
	public Index getIndex() { return Database.getLabelIndex(); }
	
	public ModelPopulatorInterface getModelPopulator() {
		if (searchIndexModelPopulator == null)
			searchIndexModelPopulator = new RecommendedIndexModelPopulator(getSearchIndex()); 
		return searchIndexModelPopulator;
	}	
	
	public SearchDetailsModelManager getDetailsModelManager() { return (LabelDetailsModelManager)Database.getRelativeModelFactory().getRelativeModelManager(LabelDetailsModelManager.class); }
	
	public boolean excludeExternalItems() { return false; }
	public boolean excludeInternalItems() { return true; }
	
	public void write(LineWriter writer) {
		super.write(writer);
		writer.writeLine(1); // version
	}
	
}
