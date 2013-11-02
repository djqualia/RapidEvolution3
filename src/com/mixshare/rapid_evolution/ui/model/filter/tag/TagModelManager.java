package com.mixshare.rapid_evolution.ui.model.filter.tag;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.filter.FilterIdentifier;
import com.mixshare.rapid_evolution.data.identifier.filter.tag.TagIdentifier;
import com.mixshare.rapid_evolution.data.index.Index;
import com.mixshare.rapid_evolution.data.record.HierarchicalRecord;
import com.mixshare.rapid_evolution.data.record.filter.tag.TagRecord;
import com.mixshare.rapid_evolution.data.submitted.filter.SubmittedFilterProfile;
import com.mixshare.rapid_evolution.data.submitted.filter.tag.SubmittedTag;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.column.StaticTypeColumn;
import com.mixshare.rapid_evolution.ui.model.column.comparables.SmartString;
import com.mixshare.rapid_evolution.ui.model.filter.FilterModelManager;
import com.mixshare.rapid_evolution.ui.model.tree.TreeHierarchyInstance;
import com.mixshare.rapid_evolution.ui.util.DragDropUtil;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.trolltech.qt.gui.QCompleter;

public class TagModelManager extends FilterModelManager {

    static private Logger log = Logger.getLogger(TagModelManager.class);	
    static private final long serialVersionUID = 0L;    
	
    static public StaticTypeColumn[] ALL_COLUMNS = {
    	COLUMN_TAG_NAME.getInstance(true),
    	COLUMN_NUM_ARTISTS.getInstance(false),
    	COLUMN_NUM_LABELS.getInstance(false),
    	COLUMN_NUM_RELEASES.getInstance(false),
    	COLUMN_NUM_SONGS.getInstance(false)
    };
    
    ////////////
    // FIELDS //
    ////////////
    
    transient private TagCompleterFactory tagCompleterFactory;
    		
    //////////////////
    // CONSTRUCTION //
    //////////////////
    
	public TagModelManager() {
		setPrimarySortColumn(COLUMN_TAG_NAME.getColumnId());
	}
	public TagModelManager(LineReader lineReader) {
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
	public String getTypeDescription() { return Translations.get("text_tag"); }
	public FilterIdentifier getFilterIdentifier(String filterName) { return new TagIdentifier(filterName); }
	public SubmittedFilterProfile getNewSubmittedFilter(String filterName) { return new SubmittedTag(filterName); }
	public String[] getFilterMimeType() { return new String[] { DragDropUtil.MIME_TYPE_TAG_INSTANCE_LIST };}
	
	public Object getSourceData(short columnId, Object record) {
		if (log.isTraceEnabled())
			log.trace("getColumnData(): columnId=" + columnId + ", record=" + record);		
		TagRecord tagRecord = (TagRecord)record;
		if (columnId == COLUMN_TAG_NAME.getColumnId())
			return new SmartString(tagRecord.getTagName());
		return super.getSourceData(columnId, record);
	}

	public TreeHierarchyInstance getTreeHierarchyInstance(HierarchicalRecord obj, TreeHierarchyInstance parentInstance) {
		if (obj != null)
			return new TagHierarchyInstance(obj, parentInstance);
		return null;
	}
	
	public Index getIndex() { return Database.getTagIndex(); }		
	
	public QCompleter getTagCompleter() {
		if (tagCompleterFactory == null)
			tagCompleterFactory = new TagCompleterFactory();
		return tagCompleterFactory.getCompleter();
	} 

	public boolean isLazySearchSupported() { return RE3Properties.getBoolean("tag_model_supports_lazy"); }

	public void write(LineWriter writer) {
		super.write(writer);
		writer.writeLine(1); // version
	}
	
}
