package com.mixshare.rapid_evolution.data.search.autocomplete;

import java.util.List;

abstract public class AbstractAutocompleter {
	
	private List<String> wordList = null;
	
	abstract public List<String> getWordList();
	
	public String getFirstMatch(String query) { // for mobile
		if (wordList == null)
			wordList = getWordList();
		query = query.toLowerCase();
		for (String word : wordList)
			if (word.toLowerCase().startsWith(query))
				return word;
		return "";
	}

}
