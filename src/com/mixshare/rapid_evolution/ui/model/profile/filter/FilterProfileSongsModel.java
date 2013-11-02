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
import com.mixshare.rapid_evolution.data.search.parameters.search.song.SongSearchParameters;
import com.mixshare.rapid_evolution.ui.model.ModelPopulatorInterface;
import com.mixshare.rapid_evolution.ui.model.column.ColumnOrdering;
import com.mixshare.rapid_evolution.ui.model.filter.FilterSelection;
import com.mixshare.rapid_evolution.ui.model.search.song.SongModelManager;

public class FilterProfileSongsModel implements ModelPopulatorInterface {

	static private Logger log = Logger.getLogger(FilterProfileSongsModel.class);	
		
    ////////////
    // FIELDS //
    ////////////
    
    private FilterProfile relativeProfile;
    private Vector<Integer> songIds;
    private SongModelManager modelManager;
    private Vector<SearchResult> results;
        
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
    public FilterProfileSongsModel(FilterProfile relativeProfile, SongModelManager modelManager) {
    	this.relativeProfile = relativeProfile;
    	this.modelManager = modelManager;
    }
    
    /////////////
    // GETTERS //
    /////////////
    
	public int getSize() {
		if (songIds == null)
			update();		
		return songIds.size();
	}
	
	public Iterator<Integer> getIdsIterator() {
		if (songIds == null)
			update();		
		return songIds.iterator();
	}
	
	public Vector<SearchResult> getResults() {
		return results;
	}
    
	/////////////
	// METHODS //
	/////////////
	
	public boolean update() {	
		if (modelManager.getSearchProxyModel() != null) {
			SongSearchParameters songParams = (SongSearchParameters)modelManager.getSearchProxyModel().getSearchParameters();
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
				songParams.setSortType(sortTypes);
				songParams.setSortDescending(sortDescending);
			} else {
				songParams.setSortType(new byte[] { CommonSearchParameters.SORT_BY_DEGREE });
			}			
			songParams.setShowDisabled(false);
			if (relativeProfile instanceof StyleProfile)
				songParams.setStylesSelection(new FilterSelection(relativeProfile));
			else if (relativeProfile instanceof TagProfile)
				songParams.setTagsSelection(new FilterSelection(relativeProfile));
			else if (relativeProfile instanceof PlaylistProfile)
				songParams.setPlaylistsSelection(new FilterSelection(relativeProfile));
			results = Database.getSongIndex().searchRecords(songParams);
	    	songIds = new Vector<Integer>(results.size());
	    	for (SearchResult song : results)
	    		songIds.add(song.getRecord().getUniqueId());		
	    	if (log.isTraceEnabled())
	    		log.trace("update(): songIds=" + songIds);
	    	return true;
		} else {
			songIds = new Vector<Integer>(0);
		}
		return false;
	}
	
}
