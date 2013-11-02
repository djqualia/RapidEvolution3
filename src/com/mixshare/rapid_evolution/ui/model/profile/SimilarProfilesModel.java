package com.mixshare.rapid_evolution.ui.model.profile;

import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.index.Index;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.search.SearchResult;
import com.mixshare.rapid_evolution.data.search.parameters.CommonSearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.search.SearchSearchParameters;
import com.mixshare.rapid_evolution.ui.model.ModelPopulatorInterface;
import com.mixshare.rapid_evolution.workflow.SandmanThread;
import com.mixshare.rapid_evolution.workflow.maintenance.search.SearchTransientsClearer;

/**
 * Computes the similar records for a given profile and provides them for the model.
 */
public class SimilarProfilesModel implements ModelPopulatorInterface {

	static private Logger log = Logger.getLogger(SimilarProfilesModel.class);	
	
	static public final float MIN_SIMILARITY_FOR_DISPLAY = RE3Properties.getFloat("minimum_similarity_for_display");	
	static public final int MAX_SIMILAR_ITEMS = RE3Properties.getInt("max_similar_items");
	
    ////////////
    // FIELDS //
    ////////////
    
	private Index index;
    private SearchProfile relativeProfile;
    private Vector<SearchResult> similarRecords;
    
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
    public SimilarProfilesModel(SearchProfile relativeProfile, Index index) {
    	this.relativeProfile = relativeProfile;
    	this.index = index;
    	computeSimilarRecords();
    }
        
    /////////////
    // GETTERS //
    /////////////
    
	public int getSize() { return similarRecords.size(); }
	
	public Iterator<Integer> getIdsIterator() {
		Vector<Integer> ids = new Vector<Integer>(similarRecords.size());
		for (SearchResult record : similarRecords)
			ids.add(record.getRecord().getUniqueId());
		return ids.iterator();
	}
	
	public Vector<SearchResult> getSimilarRecords() { return similarRecords; }
		
	/////////////
	// METHODS //
	/////////////
	
	public void computeSimilarRecords() {
		try {
			if (log.isDebugEnabled())
				log.debug("computeSimilarRecords(): relativeProfile=" + relativeProfile);
			
			// temp code to see where this was being called from...
    		//StackTraceElement[] stackTrace = new Exception().getStackTrace();
    		//for (StackTraceElement element : stackTrace)
    			//log.debug("computeSimilarRecords(): \t" + element);    		
			
			SearchSearchParameters searchParams = (SearchSearchParameters)index.getNewSearchParameters();
			searchParams.initRelativeProfile(relativeProfile);
			searchParams.addExcludedId(relativeProfile.getUniqueId());
			searchParams.setSortType(new byte[] { CommonSearchParameters.SORT_BY_SIMILARITY, CommonSearchParameters.SORT_BY_SCORE });
			if (RE3Properties.getBoolean("enable_source_weight_adjustments")) {
				searchParams.addUserDataTypeFilter(Database.getSongIndex().getUserDataType("Published"), Boolean.TRUE);				
				searchParams.addUserDataTypeWeight(Database.getSongIndex().getUserDataType("Universal"), Boolean.TRUE, RE3Properties.getFloat("song_similarity_umg_scale"));				
				searchParams.addUserDataTypeWeight(Database.getSongIndex().getUserDataType("BC Media"), Boolean.TRUE, RE3Properties.getFloat("song_similarity_bc_scale"));
			}
			similarRecords = index.searchRecords(searchParams, MAX_SIMILAR_ITEMS);
			
			/*
			similarRecords = new Vector<SimilarRecord>(MAX_SIMILAR_ITEMS);		
			Iterator<Integer> ids = index.getIdsIterator();
			while (ids.hasNext()) {
				// TODO: could use a linked list to improve performance?
				SearchRecord record = (SearchRecord)index.getRecord(ids.next());
				if ((record != null) && !record.equals(relativeProfile.getRecord()) && !record.isDisabled()) {
					float similarity = relativeProfile.getSimilarity(record);
					if (similarity > MIN_SIMILARITY_FOR_DISPLAY) {
						boolean inserted = false;
						int i = 0;
						while ((i < similarRecords.size()) && !inserted) {
							SimilarRecord compare = similarRecords.get(i);
							if (similarity > compare.getSimilarity()) {
								inserted = true;
								similarRecords.insertElementAt(new SimilarRecord(record, similarity), i);
							}
							++i;
						}
						if (!inserted && (similarRecords.size() < MAX_SIMILAR_ITEMS))
							similarRecords.add(new SimilarRecord(record, similarity));
						if (similarRecords.size() > MAX_SIMILAR_ITEMS)
							similarRecords.remove(similarRecords.size() - 1);
					}
				}
			}
			*/
			
			if (log.isDebugEnabled())
				log.debug("computeSimilarRecords(): # results=" + similarRecords.size());
			if (!RE3Properties.getBoolean("server_mode"))
				SandmanThread.putForegroundTaskToSleep(new SearchTransientsClearer(relativeProfile), 10000);
			else
				relativeProfile.clearSimilaritySearchTransients();
		} catch (Exception e) {
			log.error("computeSimilarRecords(): error", e);
		}
	}
    
}
