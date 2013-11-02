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
import com.mixshare.rapid_evolution.data.search.parameters.search.artist.ArtistSearchParameters;
import com.mixshare.rapid_evolution.ui.model.ModelPopulatorInterface;
import com.mixshare.rapid_evolution.ui.model.column.ColumnOrdering;
import com.mixshare.rapid_evolution.ui.model.filter.FilterSelection;
import com.mixshare.rapid_evolution.ui.model.search.artist.ArtistModelManager;

public class FilterProfileArtistsModel implements ModelPopulatorInterface {

	static private Logger log = Logger.getLogger(FilterProfileArtistsModel.class);	
		
    ////////////
    // FIELDS //
    ////////////
    
    private FilterProfile relativeProfile;
    private Vector<Integer> artistIds;
    private ArtistModelManager modelManager;
    private Vector<SearchResult> results;
    
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
    public FilterProfileArtistsModel(FilterProfile relativeProfile, ArtistModelManager modelManager) {
    	this.relativeProfile = relativeProfile;
    	this.modelManager = modelManager;
    }
    
    /////////////
    // GETTERS //
    /////////////
    
	public int getSize() {
		if (artistIds == null)
			update();		
		return artistIds.size();
	}
	
	public Iterator<Integer> getIdsIterator() {
		if (artistIds == null)
			update();		
		return artistIds.iterator();
	}
	
	public Vector<SearchResult> getResults() {
		return results;
	}
    
	/////////////
	// METHODS //
	/////////////
	
	public boolean update() {	
		if (modelManager.getSearchProxyModel() != null) {
			ArtistSearchParameters artistParams = (ArtistSearchParameters)modelManager.getSearchProxyModel().getSearchParameters();
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
				artistParams.setSortType(sortTypes);
				artistParams.setSortDescending(sortDescending);
			} else {
				artistParams.setSortType(new byte[] { CommonSearchParameters.SORT_BY_DEGREE, CommonSearchParameters.SORT_BY_FILTER_MATCH, CommonSearchParameters.SORT_BY_NAME });
			}			
			artistParams.setShowDisabled(false);
			if (relativeProfile instanceof StyleProfile)
				artistParams.setStylesSelection(new FilterSelection(relativeProfile));
			else if (relativeProfile instanceof TagProfile)
				artistParams.setTagsSelection(new FilterSelection(relativeProfile));
			else if (relativeProfile instanceof PlaylistProfile)
				artistParams.setPlaylistsSelection(new FilterSelection(relativeProfile));
			results = Database.getArtistIndex().searchRecords(artistParams);
	    	artistIds = new Vector<Integer>(results.size());
	    	for (SearchResult artist : results)
	    		artistIds.add(artist.getRecord().getUniqueId());		
	    	if (log.isTraceEnabled())
	    		log.trace("update(): artistIds=" + artistIds);
	    	return true;
		} else {
			artistIds = new Vector<Integer>(0);
		}
		return false;
	}
	
}
