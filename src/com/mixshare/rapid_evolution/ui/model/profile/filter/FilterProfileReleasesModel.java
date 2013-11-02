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
import com.mixshare.rapid_evolution.data.search.parameters.search.release.ReleaseSearchParameters;
import com.mixshare.rapid_evolution.ui.model.ModelPopulatorInterface;
import com.mixshare.rapid_evolution.ui.model.column.ColumnOrdering;
import com.mixshare.rapid_evolution.ui.model.filter.FilterSelection;
import com.mixshare.rapid_evolution.ui.model.search.release.ReleaseModelManager;

public class FilterProfileReleasesModel implements ModelPopulatorInterface {

	static private Logger log = Logger.getLogger(FilterProfileReleasesModel.class);	
		
    ////////////
    // FIELDS //
    ////////////
    
    private FilterProfile relativeProfile;
    private Vector<Integer> releaseIds;
    private ReleaseModelManager modelManager;
    private Vector<SearchResult> results;
    
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
    public FilterProfileReleasesModel(FilterProfile relativeProfile, ReleaseModelManager modelManager) {
    	this.relativeProfile = relativeProfile;
    	this.modelManager = modelManager;
    }
    
    /////////////
    // GETTERS //
    /////////////
    
	public int getSize() {
		if (releaseIds == null)
			update();		
		return releaseIds.size();
	}
	
	public Iterator<Integer> getIdsIterator() {
		if (releaseIds == null)
			update();		
		return releaseIds.iterator();
	}
	
	public Vector<SearchResult> getResults() {
		return results;
	}
    
	/////////////
	// METHODS //
	/////////////
	
	public boolean update() {	
		if (modelManager.getSearchProxyModel() != null) {
			ReleaseSearchParameters releaseParams = (ReleaseSearchParameters)modelManager.getSearchProxyModel().getSearchParameters();
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
				releaseParams.setSortType(sortTypes);
				releaseParams.setSortDescending(sortDescending);
			} else {
				releaseParams.setSortType(new byte[] { CommonSearchParameters.SORT_BY_DEGREE });
			}			
			releaseParams.setShowDisabled(false);
			if (relativeProfile instanceof StyleProfile)
				releaseParams.setStylesSelection(new FilterSelection(relativeProfile));
			else if (relativeProfile instanceof TagProfile)
				releaseParams.setTagsSelection(new FilterSelection(relativeProfile));
			else if (relativeProfile instanceof PlaylistProfile)
				releaseParams.setPlaylistsSelection(new FilterSelection(relativeProfile));
			results = Database.getReleaseIndex().searchRecords(releaseParams);
	    	releaseIds = new Vector<Integer>(results.size());
	    	for (SearchResult release : results)
	    		releaseIds.add(release.getRecord().getUniqueId());		
	    	if (log.isTraceEnabled())
	    		log.trace("update(): releaseIds=" + releaseIds);
	    	return true;
		} else {
			releaseIds = new Vector<Integer>(0);
		}
		return false;
	}
	
}
