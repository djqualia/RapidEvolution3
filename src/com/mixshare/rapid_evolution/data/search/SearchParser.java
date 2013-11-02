package com.mixshare.rapid_evolution.data.search;

import java.io.Serializable;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.index.imdb.LocalIMDB;
import com.mixshare.rapid_evolution.util.StringUtil;

/**
 * The job of this class is to convert a user search query string into a Lucene Query that
 * performs the job in a way users have grown to expect. (i.e. to translate from RE2 syntax
 * to Lucene syntax...)
 * 
 * Quotes around objects denote exact searches.
 * The | and & characters can be used to chain together multiple search queries.
 * Phrases are scrubbed to use substring wildcard search by default (allows more lazy searches).
 * 
 * For example, the following search: aphex twin | squarepusher
 * Would actually resemble the Lucene syntax: (*aphex* AND *twin*) OR *squarepusher*
 */
public class SearchParser implements Serializable {
	
	static private Logger log = Logger.getLogger(SearchParser.class);
    static private final long serialVersionUID = 0L;    
	
    static private String escapeChars ="[\\\\+\\-\\!\\(\\)\\:\\^\\]\\{\\}\\~\\*\\?]";
    
	////////////
	// FIELDS //
	////////////
	
    protected String searchText = null;
    protected Vector<Vector<SearchElement>> parsedClauses = new Vector<Vector<SearchElement>>();
    protected StringBuffer luceneQuery = new StringBuffer();
    
    //////////////////
    // CONSTRUCTORS //    
    //////////////////
    
    private SearchParser() { }    
    public SearchParser(String input, String luceneQuery) {
    	searchText = input;
    	this.luceneQuery.append(luceneQuery);    	
    }
    public SearchParser(String input) {
    	try {
    		searchText = input;
	    	input = preProcessString(input);	    	
	    	// first break the query down into fundamental tokens
	        Vector<SearchElement> row = new Vector<SearchElement>();
	        int index = 0;
	        int lastLetter = 0;
	        boolean insideQuotes = false;
	        while (index < input.length()) {
	            if ((input.charAt(index) == '"') || insideQuotes) {
	                if (input.charAt(index) == '"') {
	                    if (!insideQuotes) {
	                    	// the start of quotes for an exact search
	                    	insideQuotes = true;
	                    	int pIndex = index - 1;
	                    	if (!((pIndex >= 0) && (input.charAt(pIndex) == ':'))) {
		                        if (index > lastLetter)
		                            row.add(new SearchElement(input.substring(lastLetter, index), false));
		                        lastLetter = index + 1;
	                    	}
	                    } else {
	                    	// the end of quotes for an exact search
	                    	insideQuotes = false;
	                        if ((index) > lastLetter)
	                            row.add(new SearchElement(input.substring(lastLetter, index), true));
	                        lastLetter = index + 1;
	                    }
	                }
	            } else if (input.charAt(index) == ' ') {
	                if (index > lastLetter)
	                    row.add(new SearchElement(input.substring(lastLetter, index), false));
	                lastLetter = index + 1;
	            } else if ((input.charAt(index) == '|') || (input.charAt(index) == '&')) {
	                if (index > lastLetter)
	                    row.add(new SearchElement(input.substring(lastLetter, index), false));
	                if (row.size() > 0)
	                	parsedClauses.add(row);
	                lastLetter = index + 1;
	                row = new Vector<SearchElement>();
	            }
	            index++;
	        }
	        if (index > lastLetter)
	            row.add(new SearchElement(input.substring(lastLetter, index), insideQuotes));
	        parsedClauses.add(row);
	        
	        String connectClause = " OR ";
	        if (RE3Properties.getBoolean("strict_match_searching"))
	        	connectClause = " AND ";
	        
	        // now rebuild a lucene query for the queryparser with the desired behavior
	        for (Vector<SearchElement> searchElements : parsedClauses) {
	        	if (luceneQuery.length() > 0)
	        		luceneQuery.append(" OR (");
	        	else
	        		luceneQuery.append("(");
	        	StringBuffer subQuery = new StringBuffer();
	        	for (SearchElement searchElement : searchElements) {
	        		boolean addConnect = false;
	        		if (subQuery.length() > 0)
	        			addConnect = true;	        			
	        		if (searchElement.isExact()) {
    					if (addConnect)
    						subQuery.append(connectClause);
	        			if (searchElement.toString().indexOf("\"") == -1)
	        				subQuery.append("\"");
	        			subQuery.append(searchElement.toString());
	        			subQuery.append("\"");
	        		} else {
	        			if (searchElement.getText().startsWith("+") || searchElement.getText().startsWith("-")) { // a require/exlude prefix exists
	        				if (!isStopWord(searchElement.getText().substring(1))) {
	        					if (addConnect)
	        						subQuery.append(connectClause);
	        					subQuery.append(searchElement.getText().charAt(0));
	        					String escaped = processToken(searchElement.getText().substring(1));	        				
		        				if (RE3Properties.getBoolean("add_wildcard_to_searches") && !escaped.endsWith("~") && !escaped.startsWith("*"))
		        					subQuery.append("*");
	        					subQuery.append(escaped);
		        				if (RE3Properties.getBoolean("add_wildcard_to_searches") && !escaped.endsWith("~") && !escaped.endsWith("*"))
		        					subQuery.append("*");
	        				}
	        			} else if (searchElement.getText().indexOf(":") > 0) { // field has been specified
	        				int fieldIndex = searchElement.getText().indexOf(":");
	        				if (!isStopWord(searchElement.getText().substring(fieldIndex + 1))) {
	        					if (addConnect)
	        						subQuery.append(connectClause);
		        				subQuery.append(searchElement.getText().substring(0, fieldIndex));
		        				subQuery.append(":");
		        				String escaped = processToken(searchElement.getText().substring(fieldIndex + 1));
		        				if (RE3Properties.getBoolean("add_wildcard_to_searches") && !escaped.endsWith("~") && !escaped.startsWith("*"))
		        					subQuery.append("*");
		        				subQuery.append(escaped);
		        				if (RE3Properties.getBoolean("add_wildcard_to_searches") && !escaped.endsWith("~") && !escaped.endsWith("*"))
		        					subQuery.append("*");
	        				}
	        			} else {
	        				if (!isStopWord(searchElement.getText())) {
	        					if (addConnect)
	        						subQuery.append(connectClause);
		        				String escaped = processToken(searchElement.getText());
		        				if (RE3Properties.getBoolean("add_wildcard_to_searches") && !escaped.endsWith("~") && !escaped.startsWith("*"))
		        					subQuery.append("*");
		        				subQuery.append(escaped);
		        				if (RE3Properties.getBoolean("add_wildcard_to_searches") && !escaped.endsWith("~") && !escaped.endsWith("*"))
		        					subQuery.append("*");
	        				}
	        			}
	        		}
	        	}	      
	        	luceneQuery.append(subQuery.toString());
	        	luceneQuery.append(")");
	        }
    	} catch (Exception e) {
    		log.error("SearchParser(): error", e);    	
    	}
    }
    
    private String processToken(String input) {
    	String output = input; //.replaceAll(escapeChars, "\\\\$0");
    	if (output.endsWith(".") || output.endsWith("'"))
    		output = output.substring(0, output.length() - 1);
    	if (output.startsWith(".") || output.startsWith("'"))
    		output = output.substring(1);
    	return output;
    }
    
    /**
     * Does the following:
     * 
     *  - Turns dashes within letters to spaces, i.e. "an-ten-nae" to "an ten nae"
     *  - Turns pluses within letters to spaces, i.e. "maps+diagrams" to "maps diagrams"
     */
    private String preProcessString(String input) {
    	input = StringUtil.replace(input, " or ", " | ");
    	input = StringUtil.replace(input, " OR ", " | ");
    	// dashes
    	int lastIndex = 1;
    	int index = input.indexOf("-", lastIndex);
    	while (index >= 1) {
    		int preIndex = index - 1;
    		int postIndex = index + 1;
    		if ((preIndex >= 0) && (postIndex <= input.length())) {
    			if (Character.isLetterOrDigit(input.charAt(preIndex)) && Character.isLetterOrDigit(input.charAt(postIndex)))
    				input = input.substring(0, index) + " " + input.substring(index + 1);    			
    		}    		
    		lastIndex = index + 1;
    		index = input.indexOf("-", lastIndex);
    	}
    	// pluses
    	lastIndex = 1;
    	index = input.indexOf("+", lastIndex);
    	while (index >= 1) {
    		int preIndex = index - 1;
    		int postIndex = index + 1;
    		if ((preIndex >= 0) && (postIndex <= input.length())) {
    			if (Character.isLetterOrDigit(input.charAt(preIndex)) && Character.isLetterOrDigit(input.charAt(postIndex)))
    				input = input.substring(0, index) + " " + input.substring(index + 1);    			
    		}    		
    		lastIndex = index + 1;
    		index = input.indexOf("+", lastIndex);
    	}    	
    	return input;
    }

    /////////////
    // GETTERS //
    /////////////
    
    public boolean isEmpty() {
        for (int i = 0; i < parsedClauses.size(); ++i) {
            Vector<SearchElement> rowdata = parsedClauses.get(i);
            for (int j = 0; j < rowdata.size(); ++j)
                if (!rowdata.get(j).equals(""))
                    return false;
        }
        return true;
    }

    public String getSearchText() { return searchText; }
    public String getLuceneText() { return luceneQuery.toString(); }
    
    /////////////
    // METHODS //
    /////////////
    
    private boolean isStopWord(String word) {
    	for (Object stopWord : StandardAnalyzer.STOP_WORDS_SET) {
    		if (word.equalsIgnoreCase(stopWord.toString()))
    			return true;
    	}
    	return false;
    }
    
	public Query getQuery(String[] fields) {
		try {
			QueryParser parser = new MultiFieldQueryParser(LocalIMDB.LUCENE_VERSION, fields, LocalIMDB.getAnalyzer());
			parser.setAllowLeadingWildcard(true);
			Query query = parser.parse(luceneQuery.toString());
			return query;
		} catch (ParseException pe) { }
		return null;
	}
    
    static private class SearchElement {
    	private String text;
    	private boolean exact; // quotes?
    	public SearchElement(String text, boolean exact) {
    		this.text = text;
    		this.exact = exact;    		
    	}
    	public String getText() { return text; }
    	public boolean isExact() { return exact; }
    	public String toString() { return text; }
    }
    
}
