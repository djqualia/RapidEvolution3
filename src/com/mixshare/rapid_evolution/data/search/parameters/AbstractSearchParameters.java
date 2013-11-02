package com.mixshare.rapid_evolution.data.search.parameters;

import java.util.Vector;

import com.mixshare.rapid_evolution.data.index.Index;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.search.SearchResult;
import com.mixshare.rapid_evolution.util.io.LineWriter;

abstract public class AbstractSearchParameters implements SearchParameters {
	
	abstract public float matches(Record record);
	abstract public float matches(Record record, boolean fullCheck);
	
	abstract public float matchesSub(Record record, boolean fullCheck);
	abstract public float computeWeight(Record record);
	
	abstract public String getUniqueHash();

	abstract public boolean isEmpty();
	abstract public boolean isEmpty(boolean countIndexed);
	
	abstract protected int compareSub(SearchResult r1, SearchResult r2, byte sortType);
	
	abstract public byte getDataType();
	
	abstract public void write(LineWriter writer);
		
	abstract public void addSearchFields(Vector<String> searchFields);
	
	abstract public Index getIndex();
	
	abstract public void clearLastSearchResultScores();
	
}
