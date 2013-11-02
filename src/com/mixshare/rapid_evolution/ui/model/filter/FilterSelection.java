package com.mixshare.rapid_evolution.ui.model.filter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.profile.filter.FilterProfile;
import com.mixshare.rapid_evolution.data.record.filter.FilterRecord;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

public class FilterSelection implements Serializable {

	static private Logger log = Logger.getLogger(FilterSelection.class);
    static private final long serialVersionUID = 0L;    
	
	////////////
	// FIELDS //
	////////////
	
	private Vector<FilterRecord> requiredFilters = new Vector<FilterRecord>(); // AND
	private Vector<FilterRecord> optionalFilters = new Vector<FilterRecord>(); // OR
	private Vector<FilterRecord> excludedFilters = new Vector<FilterRecord>(); // NOT
	
	transient private int[] includedFilterIds;
	
	//////////////////
	// CONSTRUCTORS //
	//////////////////
	
	public FilterSelection() { }
	public FilterSelection(LineReader lineReader) {
		int version = Integer.parseInt(lineReader.getNextLine());
		int numRequiredFilters = Integer.parseInt(lineReader.getNextLine());
		for (int i = 0; i < numRequiredFilters; ++i)
			requiredFilters.add(FilterRecord.readFilterRecord(lineReader));
		int numOptionalFilters = Integer.parseInt(lineReader.getNextLine());
		for (int i = 0; i < numOptionalFilters; ++i)
			optionalFilters.add(FilterRecord.readFilterRecord(lineReader));
		int numExcludedFilters = Integer.parseInt(lineReader.getNextLine());
		for (int i = 0; i < numExcludedFilters; ++i)
			excludedFilters.add(FilterRecord.readFilterRecord(lineReader));
	}
	public FilterSelection(FilterRecord filter) {
		optionalFilters.add(filter);
	}
	public FilterSelection(FilterProfile filterProfile) {
		optionalFilters.add(filterProfile.getFilterRecord());
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public Vector<FilterRecord> getRequiredFilters() { return requiredFilters; }
	public Vector<FilterRecord> getOptionalFilters() { return optionalFilters; }
	public Vector<FilterRecord> getExcludedFilters() { return excludedFilters; }
	
	public int size() {
		return requiredFilters.size() + optionalFilters.size() + excludedFilters.size();
	}
	public boolean isEmpty() { return (size() == 0); }
	
	public int[] getIncludedFilterIds() {
		if (includedFilterIds == null) {
			Map<Integer, Object> mapResult = new HashMap<Integer, Object>();
			if (requiredFilters != null) {
				for (FilterRecord filter : requiredFilters)
					mapResult.put(filter.getUniqueId(), null);			
			}
			if (optionalFilters != null) {
				for (FilterRecord filter : optionalFilters)
					mapResult.put(filter.getUniqueId(), null);
			}
			int[] result = new int[mapResult.size()];
			int i = 0;
			for (int val : mapResult.keySet())
				result[i++] = val;
			includedFilterIds = result;
		}
		return includedFilterIds;
	}
	
	/////////////
	// SETTERS //
	/////////////
	
	public void setRequiredFilters(Vector<FilterRecord> requiredFilters) {
		this.requiredFilters = requiredFilters; 
		includedFilterIds = null;
	}	
	public void setOptionalFilters(Vector<FilterRecord> optionalFilters) {
		this.optionalFilters = optionalFilters;
		includedFilterIds = null;
	}
	public void setExcludedFilters(Vector<FilterRecord> excludedFilters) { this.excludedFilters = excludedFilters; }
	
	/////////////
	// METHODS //
	/////////////
	
	public boolean matches(SearchRecord searchRecord) {
		// check excluded list
		boolean excluded = false;
		int f = 0;
		while ((f < excludedFilters.size()) && !excluded) {
			FilterRecord excludedFilter = excludedFilters.get(f);
			if (excludedFilter.matches(searchRecord))
				excluded = true;				
			++f;
		}
		if (excluded)
			return false;
		// check required list
		boolean satisfiesRequired = true;
		f = 0;
		while ((f < requiredFilters.size()) && satisfiesRequired) {
			FilterRecord requiredFilter = requiredFilters.get(f);
			if (!requiredFilter.matches(searchRecord))
				satisfiesRequired = false;
			++f;
		}
		if (!satisfiesRequired)
			return false;
		// now process optional (standard) selections
		boolean matched = false;
		f = 0;
		if (optionalFilters.size() == 0) {
			matched = true;
		} else {
			// this implements an OR logic (i.e. the search record only has to match 1 of the filters)
			while ((f < optionalFilters.size()) && !matched) {
				FilterRecord filter = optionalFilters.get(f);
				if (filter.matches(searchRecord)) {
					matched = true;				
				}
				++f;
			}
		}
		return matched;
	}
	
	public void write(LineWriter writer) {
		writer.writeLine(1); // version
		writer.writeLine(requiredFilters.size());
		for (FilterRecord filterRecord : requiredFilters)
			FilterRecord.writeFilterRecord(filterRecord, writer);
		writer.writeLine(optionalFilters.size());
		for (FilterRecord filterRecord : optionalFilters)
			FilterRecord.writeFilterRecord(filterRecord, writer);
		writer.writeLine(excludedFilters.size());
		for (FilterRecord filterRecord : excludedFilters)
			FilterRecord.writeFilterRecord(filterRecord, writer);
	}
	
}
