package com.mixshare.rapid_evolution.ui.model.profile.search.song;

import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.index.Index;
import com.mixshare.rapid_evolution.data.profile.search.song.Exclude;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.data.search.SearchResult;
import com.mixshare.rapid_evolution.data.search.parameters.CommonSearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.search.song.SongSearchParameters;
import com.mixshare.rapid_evolution.music.bpm.Bpm;
import com.mixshare.rapid_evolution.music.key.Key;
import com.mixshare.rapid_evolution.music.rating.Rating;
import com.mixshare.rapid_evolution.ui.model.ModelPopulatorInterface;
import com.mixshare.rapid_evolution.ui.model.column.ColumnOrdering;
import com.mixshare.rapid_evolution.ui.widgets.profile.ProfileWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.profile.search.song.CompatibleSongProxyModel;

public class CompatibleSongsModel implements ModelPopulatorInterface {

	static private Logger log = Logger.getLogger(CompatibleSongsModel.class);	
	
	static public final float MIN_SIMILARITY_FOR_DISPLAY = RE3Properties.getFloat("minimum_similarity_for_display");	
	static public final int MAX_SIMILAR_ITEMS = RE3Properties.getInt("max_similar_items");
	static public float COMPATIBLE_BPM_RANGE = RE3Properties.getFloat("compatible_bpm_range");
	static public boolean COMPATIBLE_KEY_STRICT_MATCH = RE3Properties.getBoolean("compatible_key_strict_match");
	
    ////////////
    // FIELDS //
    ////////////
    
	private Index index;
    private SongProfile relativeProfile;
    private Vector<SearchResult> compatibleRecords;
    private CompatibleSongsModelManager modelManager;
    
    transient private SongSearchParameters searchParams;
    transient private SongSearchParameters lastSearchParams;
    
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
    public CompatibleSongsModel(SongProfile relativeProfile, Index index, CompatibleSongsModelManager modelManager) {
    	this.relativeProfile = relativeProfile;
    	this.index = index;
    	this.modelManager = modelManager;

    	computeCompatibleRecords();
    }
    
    /////////////
    // GETTERS //
    /////////////
    
	public int getSize() { return compatibleRecords.size(); }
	
	public Iterator<Integer> getIdsIterator() {
		Vector<Integer> ids = new Vector<Integer>(compatibleRecords.size());
		for (SearchResult record : compatibleRecords)
			ids.add(record.getRecord().getUniqueId());
		return ids.iterator();
	}
	
	public Vector<SearchResult> getResults() { return compatibleRecords; }
	
	public SongSearchParameters getSearchParameters() {
		if (searchParams == null)
			searchParams = (SongSearchParameters)index.getNewSearchParameters();		
		return searchParams; 
	}
	
	/////////////
	// METHODS //
	/////////////
	
	public boolean computeCompatibleRecords() {
		try {	
			if (modelManager.getProxyModel() != null)
				((CompatibleSongProxyModel)modelManager.getProxyModel()).setSearchParameters(getSearchParameters());
			Bpm targetBpm = ProfileWidgetUI.instance.getStageWidget().getCurrentBpm();
			Key targetKey = ProfileWidgetUI.instance.getStageWidget().getCurrentKey();
			getSearchParameters().initRelativeProfile(relativeProfile, targetBpm, ProfileWidgetUI.instance.getStageWidget().getCurrentKey());
			if (targetBpm.isValid())
				modelManager.setTargetBpm(targetBpm);
			if (targetKey.isValid())
				modelManager.setTargetKey(targetKey);
			if (log.isTraceEnabled())
				log.trace("computeCompatibleRecords(): targetBpm=" + targetBpm + ", targetKey=" + targetKey);
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
				getSearchParameters().setSortType(sortTypes);
				getSearchParameters().setSortDescending(sortDescending);
			} else {
				getSearchParameters().setSortType(new byte[] { CommonSearchParameters.SORT_BY_FILTER_MATCH });
			}
			getSearchParameters().addExcludedId(relativeProfile.getUniqueId());
			if (RE3Properties.getBoolean("prevent_repeat_songplay")) {
				for (Record record : ProfileWidgetUI.instance.getProfileTrailToCurrent())
					if (record instanceof SongRecord)
						getSearchParameters().addExcludedId(record.getUniqueId());
			}
			for (Exclude exclude : relativeProfile.getExcludes())
				getSearchParameters().addExcludedId(exclude.getToSongId());							
			getSearchParameters().setSearchForCompatible(true);
			getSearchParameters().setInternalItemsOnly(true);
			getSearchParameters().setCompatibleBpmRange(ProfileWidgetUI.instance.getStageWidget().getCurrentBpmRange());
			getSearchParameters().setCompatibleBpmShift(ProfileWidgetUI.instance.getStageWidget().getCurrentBpmShift());
			getSearchParameters().setMinRating(Rating.getRating(RE3Properties.getInt("compatible_tab_min_rating") * 20));
			if (RE3Properties.getInt("compatible_tab_min_rating") > 0)
				getSearchParameters().setIncludeUnrated(true);	
									
			if ((lastSearchParams == null) || (!lastSearchParams.getUniqueHash().equals(getSearchParameters().getUniqueHash()))) {
				compatibleRecords = index.searchRecords(getSearchParameters(), MAX_SIMILAR_ITEMS);
				lastSearchParams = new SongSearchParameters(getSearchParameters());
				if (log.isDebugEnabled())
					log.debug("computeCompatibleRecords(): # results=" + compatibleRecords.size() + " (max=" + MAX_SIMILAR_ITEMS + ")");
				return true;
			} else {
				if (log.isDebugEnabled())
					log.debug("computeCompatibleRecords(): search skipped (no criteria changed), hash=" + getSearchParameters().getUniqueHash());
				return false;
			}
		} catch (Exception e) {
			log.error("computeCompatibleRecords(): error", e);
		}
		return false;
	}
    
}
