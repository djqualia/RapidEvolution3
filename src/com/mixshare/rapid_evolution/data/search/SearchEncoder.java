package com.mixshare.rapid_evolution.data.search;

import com.ibm.icu.text.Normalizer;
import com.mixshare.rapid_evolution.util.StringUtil;

/**
 * This class should only be used in transient variables, so that if the method changes it doesn't affect
 * searching of existing objects...
 */
public class SearchEncoder {

    static public String encodeString(String text) { return encodeString(text, true); }
    static public String encodeString(String text, boolean includeSpaces) {
        if (text == null)
        	return "";
        StringBuffer encoded = new StringBuffer();
        for (int iter = 0; iter < text.length(); ++iter) {
            char character = text.charAt(iter);
            if (Character.isLetterOrDigit(character) || (includeSpaces && (character == ' '))) {
            	character = Character.toLowerCase(character);
            	// temp code until a more complete solution is found
            	if (character == 'ø')
            		character = 'o';
                encoded.append(character);
            }
        }
        return Normalizer.decompose(encoded.toString(), false, 0).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    static public String unifyString(String text) {
    	if (text == null)
    		return "";
    	text = text.toLowerCase();
    	if (text.startsWith("the "))
    		text = text.substring(4);
    	if (text.endsWith(", the"))
    		text = text.substring(0, text.length() - 5);
    	text = StringUtil.replace(text, " and ", " & ");
    	text = StringUtil.replace(text, "theatre", "theater");
    	return encodeString(text, false);
    }
    
    static public String unifyRelease(String text) {
    	if (text == null)
    		return "";
    	text = text.toLowerCase();
    	if (text.endsWith(" ep"))
    		text = text.substring(0, text.length() - 3);
    	if (text.endsWith(" (ep)"))
    		text = text.substring(0, text.length() - 5);
    	if (text.endsWith(" [ep]"))
    		text = text.substring(0, text.length() - 5);
    	return unifyString(text);
    }    
    
    static public String[] encodeStrings(String[] tokens) {
    	String[] result = new String[tokens.length];
    	for (int i = 0; i < tokens.length; ++i) {
    		result[i] = encodeString(tokens[i]);
    	}
    	return result;
    }
    
    static public void main(String[] args) {
    	System.out.println(encodeString("Trentemøller"));
    }
	
}
