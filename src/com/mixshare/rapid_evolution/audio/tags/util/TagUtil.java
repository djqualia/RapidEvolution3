package com.mixshare.rapid_evolution.audio.tags.util;

import com.mixshare.rapid_evolution.audio.tags.TagConstants;

public class TagUtil {
    
    public static String getStyleTagId(int style_number) {
        String styleNumStr = String.valueOf(style_number);
        if (styleNumStr.length() == 1) styleNumStr = "0" + styleNumStr;
        String identifier = TagConstants.TXXX_STYLES_PREFIX + styleNumStr;            
        return identifier;
    }

    public static String getStyleTagDegree(int style_number) {
        String styleNumStr = String.valueOf(style_number);
        if (styleNumStr.length() == 1) styleNumStr = "0" + styleNumStr;
        String identifier = TagConstants.TXXX_STYLES_PREFIX + styleNumStr + "_degree";            
        return identifier;
    }

    public static String getTagTagId(int tag_number) {
        String tagNumStr = String.valueOf(tag_number);
        if (tagNumStr.length() == 1) tagNumStr = "0" + tagNumStr;
        String identifier = TagConstants.TXXX_TAGS_PREFIX + tagNumStr;            
        return identifier;
    }

    public static String getTagTagDegree(int tag_number) {
        String tagNumStr = String.valueOf(tag_number);
        if (tagNumStr.length() == 1) tagNumStr = "0" + tagNumStr;
        String identifier = TagConstants.TXXX_TAGS_PREFIX + tagNumStr + "_degree";            
        return identifier;
    }
    
    static public String convertToValidTagId(String value) {
    	// the line below fixes a problem with traktor ratings, traktor will not read "RATING_WMP"...
    	if ((value != null) && (value.equalsIgnoreCase("RATING WMP")))
    		return value;
        value = value.trim();
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < value.length(); ++i) {
            char c = value.charAt(i);
            if (c == ' ') result.append("_");
            else result.append(c);
        }
        return result.toString().toUpperCase();
    }
    
}
