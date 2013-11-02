package com.mixshare.rapid_evolution.data.search.parameters;

import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.search.SearchResult;
import com.mixshare.rapid_evolution.util.io.LineWriter;

public interface SearchParameters {

	/**
	 * Returns a score, 0.0 means "no match"
	 */
	public float matches(Record record);
	public float matches(Record record, boolean fullCheck);
	
	/**
	 * Each possible variation of search parameters should result in a unique hash that
	 * can be used to cache the results of the search if necessary...
	 */
    public String getUniqueHash();
    
    public boolean isEmpty();
    public boolean isEmpty(boolean countIndexed);    
    
    public int compare(SearchResult r1, SearchResult r2);
    
    public byte getDataType();
    
    public void write(LineWriter writer);
    
    public String[] getSearchFields();
    
	public void clearLastSearchResultScores();
	public void initLastSearchResultScore(int size);
	public void addLastSearchResultScore(int uniqueId, float score);
	public float getLastSearchResultScore(Record record);
	
}

