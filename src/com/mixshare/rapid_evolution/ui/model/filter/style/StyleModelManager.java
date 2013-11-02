package com.mixshare.rapid_evolution.ui.model.filter.style;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.filter.FilterIdentifier;
import com.mixshare.rapid_evolution.data.identifier.filter.style.StyleIdentifier;
import com.mixshare.rapid_evolution.data.index.Index;
import com.mixshare.rapid_evolution.data.record.HierarchicalRecord;
import com.mixshare.rapid_evolution.data.record.filter.style.StyleRecord;
import com.mixshare.rapid_evolution.data.submitted.filter.SubmittedFilterProfile;
import com.mixshare.rapid_evolution.data.submitted.filter.style.SubmittedStyle;
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

public class StyleModelManager extends FilterModelManager {

    static private Logger log = Logger.getLogger(StyleModelManager.class);	
    static private final long serialVersionUID = 0L;    
	
    static public StaticTypeColumn[] ALL_COLUMNS = {
    	COLUMN_STYLE_NAME.getInstance(true),
    	COLUMN_NUM_ARTISTS.getInstance(false),
    	COLUMN_NUM_LABELS.getInstance(false),
    	COLUMN_NUM_RELEASES.getInstance(false),
    	COLUMN_NUM_SONGS.getInstance(false)
    };
    
    ////////////
    // FIELDS //    
    ////////////
    
    transient private StyleCompleterFactory styleCompleterFactory;
    
    //////////////////
    // CONSTRUCTION //
    //////////////////

	public StyleModelManager() {
		setPrimarySortColumn(COLUMN_STYLE_NAME.getColumnId());
	}
	public StyleModelManager(LineReader lineReader) {
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
	public String getTypeDescription() { return Translations.get("text_style"); }
	public FilterIdentifier getFilterIdentifier(String filterName) { return new StyleIdentifier(filterName); }
	public SubmittedFilterProfile getNewSubmittedFilter(String filterName) { return new SubmittedStyle(filterName); }
	public String[] getFilterMimeType() { return new String[] { DragDropUtil.MIME_TYPE_STYLE_INSTANCE_LIST };}
	
	public Object getSourceData(short columnId, Object record) {
		if (log.isTraceEnabled())
			log.trace("getColumnData(): columnId=" + columnId + ", record=" + record);		
		StyleRecord styleRecord = (StyleRecord)record;
		if (columnId == COLUMN_STYLE_NAME.getColumnId())
			return new SmartString(styleRecord.getStyleName());
		return super.getSourceData(columnId, record);
	}

	public TreeHierarchyInstance getTreeHierarchyInstance(HierarchicalRecord obj, TreeHierarchyInstance parentInstance) {
		if (obj != null)
			return new StyleHierarchyInstance(obj, parentInstance);
		return null;
	}
	
	public Index getIndex() {return Database.getStyleIndex(); }		
		
	public QCompleter getStyleCompleter() {
		if (styleCompleterFactory == null)
			styleCompleterFactory = new StyleCompleterFactory();
		return styleCompleterFactory.getCompleter();
	} 

	public boolean isLazySearchSupported() { return RE3Properties.getBoolean("style_model_supports_lazy"); }

	public void write(LineWriter writer) {
		super.write(writer);
		writer.writeLine(1); // version
	}
	
}
