package com.mixshare.rapid_evolution.data.user;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.index.search.SearchIndex;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.data.search.SearchResult;
import com.mixshare.rapid_evolution.ui.model.profile.SimilarProfilesModel;
import com.mixshare.rapid_evolution.workflow.Task;

public class UserProfileUtil {

	static private Logger log = Logger.getLogger(UserProfileUtil.class);
	
	/**
	 * This method will go through the rated items and by looking at the delta
	 * with the average rating, attempt to determine the degree of preference towards
	 * the associated styles. 
	 */
    static public PreferenceMap computeStylePreferencesFromRatings(Vector<RatedSearchRecord> ratedRecords, float avgRating, Task task) {
    	PreferenceMap result = new PreferenceMap();
    	try {
    		if ((avgRating != 0.0f) && !Float.isNaN(avgRating)) {
    			for (RatedSearchRecord ratedRecord : ratedRecords) {
					if (RapidEvolution3.isTerminated || ((task != null) && task.isCancelled()))
						return null;
					float preference = ratedRecord.getRating().getRatingStarsFloat() - avgRating;
					int[] styleIds = ratedRecord.getSearchRecord().getActualStyleIds();
					float[] styleDegrees = ratedRecord.getSearchRecord().getActualStyleDegrees();
					for (int i = 0; i < styleIds.length; ++i)
						result.incrementPreference(styleIds[i], preference, styleDegrees[i] * ratedRecord.getWeight());    				
    			}
	    		result.normalize(true);
    		}
    	} catch (Exception e) {
    		log.error("computeStylePreferencesFromRatings(): error", e);
    	}
    	return result;
    }
    
	/**
	 * This method will go through the rated items and by looking at the delta
	 * with the average rating, attempt to determine the degree of preference towards
	 * the associated tags. 
	 */
    static public PreferenceMap computeTagPreferencesFromRatings(Vector<RatedSearchRecord> ratedRecords, float avgRating, Task task) {
    	PreferenceMap result = new PreferenceMap();
    	try {
    		if ((avgRating != 0.0f) && !Float.isNaN(avgRating)) {
    			for (RatedSearchRecord ratedRecord : ratedRecords) {
					if (RapidEvolution3.isTerminated || ((task != null) && task.isCancelled()))
						return null;
					float preference = ratedRecord.getRating().getRatingStarsFloat() - avgRating;
					int[] tagIds = ratedRecord.getSearchRecord().getActualTagIds();
					float[] tagDegrees = ratedRecord.getSearchRecord().getActualTagDegrees();
					for (int i = 0; i < tagIds.length; ++i)
						result.incrementPreference(tagIds[i], preference, tagDegrees[i] * ratedRecord.getWeight());    				
    			}
	    		result.normalize(true);
    		}
    	} catch (Exception e) {
    		log.error("computeTagPreferencesFromRatings(): error", e);
    	}
    	return result;
    }    
    
	/**
	 * This method will look at rated items by the user and then what's similar to those rated items to try to determine
	 * predicted preference towards other search items...
	 */    
    static public PreferenceMap computeSearchPreferencesFromRatings(Vector<RatedSearchRecord> ratedRecords, float avgRating, Task task) {
    	PreferenceMap result = new PreferenceMap();
    	try {
    		if ((avgRating != 0.0f) && !Float.isNaN(avgRating)) {
    			for (RatedSearchRecord ratedRecord : ratedRecords) {
					if (RapidEvolution3.isTerminated || ((task != null) && task.isCancelled()))
						return null;
    				SearchRecord searchRecord = ratedRecord.getSearchRecord();
    				SearchIndex index = (SearchIndex)searchRecord.getIndex();
					float preference = searchRecord.getRatingValue().getRatingStarsFloat() - avgRating;
					if (Math.abs(preference) > 0.01f) {						
						SearchProfile searchProfile = (SearchProfile)index.getProfile(searchRecord.getUniqueId());
						if (searchProfile != null) {
							SimilarProfilesModel similarModel = new SimilarProfilesModel(searchProfile, index);
							Vector<SearchResult> similarResults = similarModel.getSimilarRecords();
							for (SearchResult similarResult : similarResults)
								if (similarResult.getScore() > 0.01f)
									result.incrementPreference(similarResult.getRecord().getUniqueId(), preference, similarResult.getScore() * ratedRecord.getWeight());
						}
					}
    			}
	    		result.normalize(true);
    		}
    	} catch (Exception e) {
    		log.error("computeSearchPreferencesFromRatings(): error", e);
    	}
    	return result;
    }    
    
    /**
     * This method will compute preferences towards styles based on the frequency of them appearing on rated items, versus
     * using ratings...
     */
    static public PreferenceMap computeStylePreferencesFromFrequency(Vector<WeightedSearchRecord> weightedRecords, Task task) {
    	PreferenceMap result = new PreferenceMap();
    	try {
    		for (WeightedSearchRecord weightedRecord : weightedRecords) {
				if (RapidEvolution3.isTerminated || ((task != null) && task.isCancelled()))
					return null;				    			    			
    			SearchRecord searchRecord = weightedRecord.getSearchRecord();
				int[] styleIds = searchRecord.getActualStyleIds();
				float[] styleDegrees = searchRecord.getActualStyleDegrees();
				for (int i = 0; i < styleIds.length; ++i)
					result.incrementPreference(styleIds[i], weightedRecord.getWeight(), styleDegrees[i]);
    		}
    	} catch (Exception e) {
    		log.error("computeStylePreferencesFromFrequency(): error", e);
    	}
    	return result;
    }       
    
    /**
     * This method will compute preferences towards tags based on the frequency of them appearing on rated items, versus
     * using ratings...
     */    
    static public PreferenceMap computeTagPreferencesFromFrequency(Vector<WeightedSearchRecord> weightedRecords, Task task) {
    	PreferenceMap result = new PreferenceMap();
    	try {
    		for (WeightedSearchRecord weightedRecord : weightedRecords) {
				if (RapidEvolution3.isTerminated || ((task != null) && task.isCancelled()))
					return null;				    			
    			SearchRecord searchRecord = weightedRecord.getSearchRecord();
				int[] tagIds = searchRecord.getActualTagIds();
				float[] tagDegrees = searchRecord.getActualTagDegrees();
				for (int i = 0; i < tagIds.length; ++i)
					result.incrementPreference(tagIds[i], weightedRecord.getWeight(), tagDegrees[i]);
    		}
    	} catch (Exception e) {
    		log.error("computeTagPreferencesFromFrequency(): error", e);
    	}
    	return result;
    }      
    
    /**
     * For each rated item this method will identify similar items and increase their weight accordingly, to attempt
     * to determine user's preference towards items based without using ratings...
     */
    static public PreferenceMap computeSearchPreferencesFromFrequency(Vector<WeightedSearchRecord> weightedRecords, Task task) {
    	PreferenceMap result = new PreferenceMap();
    	try {
    		for (WeightedSearchRecord weightedRecord : weightedRecords) {
				if (RapidEvolution3.isTerminated || ((task != null) && task.isCancelled()))
					return null;				
    			SearchRecord searchRecord = weightedRecord.getSearchRecord();
    			result.incrementPreference(searchRecord.getUniqueId(), weightedRecord.getWeight(), 1.0f);
    			SearchIndex index = (SearchIndex)searchRecord.getIndex();
				SearchProfile searchProfile = (SearchProfile)index.getProfile(searchRecord.getUniqueId());
				if (searchProfile != null) {
					SimilarProfilesModel similarModel = new SimilarProfilesModel(searchProfile, index);
					Vector<SearchResult> similarResults = similarModel.getSimilarRecords();
					for (SearchResult similarResult : similarResults)
						result.incrementPreference(similarResult.getRecord().getUniqueId(), weightedRecord.getWeight(), similarResult.getScore());					
				}
    		}
    		result.normalize(true);
    	} catch (Exception e) {
    		log.error("computeSearchPreferencesFromFrequency(): error", e);
    	}
    	return result;
    }    
   
}
