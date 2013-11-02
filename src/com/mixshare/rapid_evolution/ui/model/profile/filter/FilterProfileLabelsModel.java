package com.mixshare.rapid_evolution.ui.model.profile.filter;

import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.profile.filter.FilterProfile;
import com.mixshare.rapid_evolution.data.profile.filter.playlist.PlaylistProfile;
import com.mixshare.rapid_evolution.data.profile.filter.style.StyleProfile;
import com.mixshare.rapid_evolution.data.profile.filter.tag.TagProfile;
import com.mixshare.rapid_evolution.data.search.SearchResult;
import com.mixshare.rapid_evolution.data.search.parameters.CommonSearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.search.label.LabelSearchParameters;
import com.mixshare.rapid_evolution.ui.model.ModelPopulatorInterface;
import com.mixshare.rapid_evolution.ui.model.column.ColumnOrdering;
import com.mixshare.rapid_evolution.ui.model.filter.FilterSelection;
import com.mixshare.rapid_evolution.ui.model.search.label.LabelModelManager;

public class FilterProfileLabelsModel implements ModelPopulatorInterface {

	static private Logger log = Logger.getLogger(FilterProfileLabelsModel.class);	
		
    ////////////
    // FIELDS //
    ////////////
    
    private FilterProfile relativeProfile;
    private Vector<Integer> labelIds;
    private LabelModelManager modelManager;
    private Vector<SearchResult> results;
    
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
    public FilterProfileLabelsModel(FilterProfile relativeProfile, LabelModelManager modelManager) {
    	this.relativeProfile = relativeProfile;
    	this.modelManager = modelManager;
    }
    
    /////////////
    // GETTERS //
    /////////////
    
	public int getSize() {
		if (labelIds == null)
			update();		
		return labelIds.size();
	}
	
	public Iterator<Integer> getIdsIterator() {
		if (labelIds == null)
			update();		
		return labelIds.iterator();
	}
	
	public Vector<SearchResult> getResults() {
		return results;
	}
    
	/////////////
	// METHODS //
	/////////////
	
	public boolean update() {	
		if (modelManager.getSearchProxyModel() != null) {
			LabelSearchParameters labelParams = (LabelSearchParameters)modelManager.getSearchProxyModel().getSearchParameters();
			Vector<ColumnOrdering> sortOrdering = modelManager.getSortOrdering();
			if ((sortOrdering != null) && (sortOrdering.size() > 0)) {
				byte[] sortTypes = new byte[sortOrdering.size()];
				boolean[] sortDescending = new boolean[sortOrdering.size()];
				int i = 0;
				for (ColumnOrdering ordering : sortOrdering) {
					sortDescending[i] = !ordering.isAscending();
					sortTypes[i] = CommonSearchParameters.getSortTypeFromColumnId(ordering.getColumnId());
					++i;
				}
				labelParams.setSortType(sortTypes);
				labelParams.setSortDescending(sortDescending);
			} else {
				labelParams.setSortType(new byte[] { CommonSearchParameters.SORT_BY_DEGREE });
			}			
			labelParams.setShowDisabled(false);
			if (relativeProfile instanceof StyleProfile)
				labelParams.setStylesSelection(new FilterSelection(relativeProfile));
			else if (relativeProfile instanceof TagProfile)
				labelParams.setTagsSelection(new FilterSelection(relativeProfile));
			else if (relativeProfile instanceof PlaylistProfile)
				labelParams.setPlaylistsSelection(new FilterSelection(relativeProfile));
			results = Database.getLabelIndex().searchRecords(labelParams);
	    	labelIds = new Vector<Integer>(results.size());
	    	for (SearchResult label : results)
	    		labelIds.add(label.getRecord().getUniqueId());		
	    	if (log.isTraceEnabled())
	    		log.trace("update(): labelIds=" + labelIds);
	    	return true;
		} else {
			labelIds = new Vector<Integer>(0);
		}
		return false;
	}
	
}
