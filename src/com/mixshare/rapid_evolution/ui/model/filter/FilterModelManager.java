package com.mixshare.rapid_evolution.ui.model.filter;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.identifier.filter.FilterIdentifier;
import com.mixshare.rapid_evolution.data.index.event.IndexChangeListener;
import com.mixshare.rapid_evolution.data.index.filter.FilterIndex;
import com.mixshare.rapid_evolution.data.record.filter.FilterRecord;
import com.mixshare.rapid_evolution.data.submitted.filter.SubmittedFilterProfile;
import com.mixshare.rapid_evolution.ui.model.column.comparables.SmartInteger;
import com.mixshare.rapid_evolution.ui.model.tree.RecordTreeModelManager;
import com.mixshare.rapid_evolution.ui.model.tree.TreeHierarchyInstance;
import com.mixshare.rapid_evolution.ui.widgets.common.rating.StarRating;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.trolltech.qt.core.QObject;

/**
 * Filters (i.e. styles, tags, playlists) have the common aspect of grouping
 * together songs, releases, etc.
 */
abstract public class FilterModelManager extends RecordTreeModelManager implements IndexChangeListener {
	
    static private Logger log = Logger.getLogger(FilterModelManager.class);	
	        
    //////////////////
    // CONSTRUCTORS //
    //////////////////
    
    public FilterModelManager() { super(); }
	public FilterModelManager(LineReader lineReader) {
		super(lineReader);
		int version = Integer.parseInt(lineReader.getNextLine());
	}
	
	//////////////////////
	// ABSTRACT METHODS //
	//////////////////////

    /**
     * Returns a StyleIdentifier, TagIdentifier, etc based on actual type.
     */
	abstract public FilterIdentifier getFilterIdentifier(String filterName);
	
	/**
	 * Used to add new styles/tags/playlist in an abstract way.
	 */
	abstract public SubmittedFilterProfile getNewSubmittedFilter(String filterName);
	
	/**
	 * This is used for drag and drop operations
	 */
	abstract public String[] getFilterMimeType();
		
	/////////////
	// GETTERS //
	/////////////
		
	public FilterIndex getFilterIndex() { return (FilterIndex)getIndex(); }
	
	public Object getSourceData(short columnId, Object record) {
		FilterRecord filterRecord = (FilterRecord)record;
		if (columnId == COLUMN_RATING_VALUE.getColumnId())
			return new SmartInteger(filterRecord.getRatingValue().getRatingValue());
		if (columnId == COLUMN_RATING_STARS.getColumnId())
			return new StarRating(filterRecord.getRatingValue());
		if (columnId == COLUMN_NUM_ARTISTS.getColumnId()) {
			if (RE3Properties.getBoolean("enable_fast_filter_counts")) {
				int count = filterRecord.getNumArtistRecordsCached();
				if (count != -1)
					return new SmartInteger(count);				
			}
			return new SmartInteger(filterRecord.getNumArtistRecords());
		} if (columnId == COLUMN_NUM_LABELS.getColumnId()) {
			if (RE3Properties.getBoolean("enable_fast_filter_counts")) {
				int count = filterRecord.getNumLabelRecordsCached();
				if (count != -1)
					return new SmartInteger(count);
			}
			return new SmartInteger(filterRecord.getNumLabelRecords());
		} if (columnId == COLUMN_NUM_RELEASES.getColumnId()) {
			if (RE3Properties.getBoolean("enable_fast_filter_counts")) {
				int count = filterRecord.getNumReleaseRecordsCached();
				if (count != -1)
					return new SmartInteger(count);
			}
			return new SmartInteger(filterRecord.getNumReleaseRecords());
		} if (columnId == COLUMN_NUM_SONGS.getColumnId()) {
			if (RE3Properties.getBoolean("enable_fast_filter_counts")) {
				int count = filterRecord.getNumSongRecordsCached();
				if (count != -1)
					return new SmartInteger(count);
			}
			return new SmartInteger(filterRecord.getNumSongRecords());
		}
		return null;
	}		
	
	public Vector<FilterHierarchyInstance> getSelectedInstances() {
		Vector<FilterHierarchyInstance> selectedInstances = new Vector<FilterHierarchyInstance>();
		Vector<TreeHierarchyInstance> allInstances = getInstances();
		for (TreeHierarchyInstance treeInstance : allInstances) {
			FilterHierarchyInstance filterInstance = (FilterHierarchyInstance)treeInstance;
			if ((filterInstance != null) && filterInstance.isSelected())
				selectedInstances.add(filterInstance);
		}
		return selectedInstances;
	}
	
	public FilterSelection getFilterSelection() {
		FilterSelection result = new FilterSelection();
		Vector<TreeHierarchyInstance> allInstances = getInstances();
		for (TreeHierarchyInstance treeInstance : allInstances) {
			FilterHierarchyInstance filterInstance = (FilterHierarchyInstance)treeInstance;
			if (filterInstance.isSelected()) {
				if (filterInstance.getSelectionState() == FilterHierarchyInstance.SELECTION_STATE_OR)
					result.getOptionalFilters().add(filterInstance.getFilterRecord());
				else if (filterInstance.getSelectionState() == FilterHierarchyInstance.SELECTION_STATE_AND)
					result.getRequiredFilters().add(filterInstance.getFilterRecord());
				else if (filterInstance.getSelectionState() == FilterHierarchyInstance.SELECTION_STATE_NOT)
					result.getExcludedFilters().add(filterInstance.getFilterRecord());
			}
		}
		return result;
	}	
	
	/////////////
	// METHODS //
	/////////////	
	
	protected void createSourceModel(QObject parent) {
		model = new FilterItemModel(getHierarchicalIndex().getRootRecords().length, getNumColumns(), parent, this);		
		loadTree();
	}
		
	public void updateInstance(TreeHierarchyInstance treeInstance) { updateInstance(treeInstance, false, false); }

	public void write(LineWriter writer) {
		super.write(writer);
		writer.writeLine(1); // version
	}
	
}
