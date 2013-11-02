package com.mixshare.rapid_evolution.util;

import java.util.Collection;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.ibm.icu.text.Normalizer;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;

public class StringUtil {

    static private Logger log = Logger.getLogger(StringUtil.class);   
	
	/**
	 * Returns true if "sub" is a subset of "main".
	 * This method is not case sensitive.
	 */
    static public boolean substring(String sub, String main) {
        if ((main == null) || (sub == null))
        	return false;
        if (main.toLowerCase().indexOf(sub.toLowerCase()) >= 0)
        	return true;
        return false;
    }
    
    /**
     * Returns a string representation of a duration provided
     * in seconds, for example, 90 seconds = "1:30"
     */
    public static String getDurationAsString(int seconds) {
    	return getDurationAsString(seconds, true);
    }
    public static String getDurationAsString(int seconds, boolean hideEmpty) {
        if ((seconds == 0) && hideEmpty)
        	return "";
        int minutes = 0;
        while (seconds >= 60) {
        	minutes++;
        	seconds -= 60;
        }
        StringBuffer result = new StringBuffer();
        result.append(String.valueOf(minutes));
        result.append(":");
        if (seconds < 10)
        	result.append("0");
        result.append(String.valueOf(seconds));
        return result.toString();
    }    

    static public int getDurationInSeconds(String time) {
     	if (time != null) {
     		try {
 	    		int index = time.indexOf(":");
 	    		if (index >= 0) {
 	    			int minutes = Integer.parseInt(time.substring(0, index));
 	    			int seconds = Integer.parseInt(time.substring(index + 1));
 	    			return minutes * 60 + seconds;
 	    		}
     		} catch (Exception e) { }
     	}
     	return 0;
    }
    
    /**
     * Should create valid filenames that are transferabble between NTFS and Unix file systems..
     * 
     *  TODO: fully switch to the new method, old method is used for compatibility with existing indexes/data
     */
    static private int MAX_FILENAME_CHARACTERS = 255; // for directories and filenames
    static private int MAX_PATH_LENGTH = 259; // for full paths, a windows kernel limitation according to wikipedia
    static private String[] prohibited = { "con", "prn", "aux", "clock$", "nul", "com0",
                                           	"com1", "com2", "com3", "com4", "com5", "com6", "com7",
                                           	"com8", "com9", "ltp0", "ltp1", "ltp2", "ltp3", "ltp4",
                                           	"ltp5", "ltp6", "ltp7", "ltp8", "ltp9" };
    public static String checkFullPathLength(String input) {
   	 if (input == null)
   		 return null;
   	 if (input.length() > MAX_PATH_LENGTH) {
   		 if (log.isDebugEnabled())
   			 log.debug("checkFullPathLength(): file path limit=" + input);
   		 return input.substring(0, MAX_PATH_LENGTH);
   	 }
   	 return input;    	 
    }
    
    /**
     * This method converts any text into a valid filename.  It also converts to lower case so that
     * the filename will be more compatible between Windows and Linux, where filename case sensitivy
     * differs.
     * 
     * NOTE: DO NOT CHANGE THIS METHOD.  It is used by the LocalProfileFetcher and any changes could
     * impact the ability to fetch existing profiles.
     */
    public static String makeValidFilename(String text) {
    	return makeValidFilename(text, false);
    }
    public static String makeValidFilename(String text, boolean removeSpaces) {
   	 if (text == null)
   		 return null;
   	 text = removeAccents(text);
   	 int tlength = text.length();
   	 StringBuffer result = new StringBuffer(tlength);
   	 boolean done = false;
   	 int i = 0;
   	 while ((i < tlength) && !done) {
   		 char c = text.charAt(i);
   		 boolean ignore = false;
   		 if ((c == '.') && (result.length() == 0)) ignore = true; // remove preceding .'s
   		 if ((c == ' ') && (result.length() == 0)) ignore = true; // remove preceding spaces
   		 if ((c == '/') || (c == '\\') || (c == ':') || (c == '*') ||
   				 (c == '?') || (c == '%') || (c == '|'))
   			 c = '-';
   		 if (c == '\"') c = '\'';
   		 if (c == '<') c = '[';
   		 if (c == '>') c = ']';
   		 if ((c >= 0) && (c <= 31)) ignore = true; // windows kernel prevents characters 1-31
   		 if (removeSpaces && (c == ' ')) ignore = true;
   		 if (!ignore) {
   			 result.append(c);
       		 if (result.length() >= MAX_FILENAME_CHARACTERS)
       			 done = true;
   		 }
   		 ++i;
   	 }
   	 // remove trailing .'s and spaces
   	 while ((result.length() > 0) && ((result.charAt(result.length() - 1) == '.') || (result.charAt(result.length() - 1) == ' ')))
   		 result.deleteCharAt(result.length() - 1);
   	 if (result.length() > 0) {
   		 for (int s = 0; s < prohibited.length; ++s) {
   			 if ((result.toString().equals(prohibited[s])) || (result.toString().startsWith(prohibited[s] + ".")))
   				 result.insert(0, '_');
   		 }
   	 }
        return result.toString();
    }
    
    static public String removeAccents(String text) {
        return Normalizer.decompose(text, false, 0)
                         .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }       

    /**
     * @param target 	is the original string
     * @param from 		is the string to be replaced
     * @param to		is the string which will be used to replace
     * @return
     */
    public static String replace(String target, String from, String to) {   
    	  int start = target.indexOf(from);
    	  if (start == -1)
    		  return target;
    	  int lf = from.length();
    	  char[] targetChars = target.toCharArray();
    	  StringBuffer buffer = new StringBuffer();
    	  int copyFrom = 0;
    	  while (start != -1) {
    		  buffer.append(targetChars, copyFrom, start-copyFrom);
    		  buffer.append(to);
    		  copyFrom = start + lf;
    		  start = target.indexOf(from, copyFrom);
    	  }
    	  buffer.append(targetChars, copyFrom, targetChars.length - copyFrom);
    	  return buffer.toString();
    }
    
    static public Vector<String> getAllInBetweens(String data, String prefix, String suffix) {
        Vector<String> result = new Vector<String>();
        int lastIndex = 0;
        if (data != null) {
	        int startIndex = data.indexOf(prefix, lastIndex);
	        while (startIndex >= 0) {
	            int endIndex = data.indexOf(suffix, startIndex + prefix.length());
	            if (endIndex >= 0) {
	                String value = data.substring(startIndex + prefix.length(), endIndex);
	                result.add(value);
	                lastIndex = endIndex + suffix.length();
	                startIndex = data.indexOf(prefix, lastIndex);
	            } else {
	           	 startIndex = -1;
	            }             
	        }
        }
        return result;
    }
 
    static public Integer parseYear(String text) {
        if (text == null)
            return null;
        boolean isWithinNumber = false;
        int startIndex = -1;
        int endIndex = -1;
        for (int c = 0; c < text.length(); ++c) {
            if (Character.isDigit(text.charAt(c))) {
                if (isWithinNumber) {
                    endIndex = c;
                    if ((endIndex - startIndex + 1) == 4)
                        return new Integer(Integer.parseInt(text.substring(startIndex, endIndex + 1)));
                } else {
                    isWithinNumber = true;
                    startIndex = c;
                }
            } else {
                isWithinNumber = false;
                startIndex = -1;
                endIndex = -1;
            }
        }
        return null;
    }       
    
    static public String stripHtml(String input) {
        if (input != null)
            return input.replaceAll("\\<.*?>","").trim();         
        return null;
    }
    
    static public String validateTime(String input) {
        if ((input == null) || (input.equals("")))
        	return new String("");
        int seperator = 0;
        while (((seperator < input.length()) && ((input.charAt(seperator) != ':')
        		&& (input.charAt(seperator) != '.') && (input.charAt(seperator) != '-'))))
        	seperator++;
        if (seperator == input.length())
        	return new String("");
        try {
        	Integer.parseInt(input.substring(0, seperator));
        	if ((input.length() - 3) != seperator) return new String("");
        	Integer.parseInt(input.substring(seperator + 1, input.length()));
        	return input;
        } catch (Exception e) { }
        return "";
    }   
    
    static public String fixSpecialXMLCharacters(String input) {
    	if (input == null)
    		return input;
    	input = StringUtil.replace(input, "&quot;", "\"");
    	input = StringUtil.replace(input, "&amp;", "&");
    	input = StringUtil.replace(input, "&gt;", ">");
    	input = StringUtil.replace(input, "&lt;", "<");
    	return input;
    }
    
    static public String stripNonAlphaNumeric(String input) {
    	StringBuffer output = new StringBuffer();
   	 	for (int i = 0; i < input.length(); ++i)
   	 		if (Character.isLetterOrDigit(input.charAt(i)))
   	 			output.append(input.charAt(i));
   	 	return output.toString();
    }
    
    static public boolean containsIgnoreCase(Vector<String> collection, String target) {
    	for (String item : collection)
    		if (item.equalsIgnoreCase(target))
    			return true;
    	return false;
    }
    
    static public Vector<String> removeDuplicatesIgnoreCase(Vector<String> input) {
    	Vector<String> output = new Vector<String>(input.size());
    	for (String item : input) {
    		if (!containsIgnoreCase(output, item))
    			output.add(item);
    	}
    	return output;
    }
    
    static public String getTrackString(int trackNumber) {
    	if (trackNumber < 10)
    		return "0" + trackNumber;
    	return String.valueOf(trackNumber);
    }
    
    static public boolean isValid(String value) {
        return ((value != null) && (value.trim().length() > 0));
    }    
    
    static public Float parseNumericalPrefix(String input) {         
        Float result = null;
        try {
            if (input != null) {
                int index = 0;
                char c = input.charAt(index);
                while (Character.isDigit(c) || (c == '.') || (c == '-') || (c == '+')) {
                    ++index;
                    if (index >= input.length())
                        c = '$';
                    else
                        c = input.charAt(index);                    
                }
                result = Float.parseFloat(input.substring(0, index));             
            }
        } catch (Exception e) {
        	log.error("parseNumericalPrefix(): error", e);
        }
        return result;
    }    
    
    static public String getLine(String input, int line) {
    	if (input == null)
    		return null;
        if (input.equals("")) 
        	return null;
        int firstindex = -1;
        int count = 0;
        if (line == 0) 
        	firstindex = 0;
        int countedlines = 0;
        while (true) {
        	if (input.charAt(count) == '\n') {
        		if ((countedlines + 1) == line)
        			firstindex = count + 1;
        		if (countedlines == line) 
        			return input.substring(firstindex, count);
        		countedlines++;
        	}
        	count++;
        	if (input.length() == count) 
        		return input.substring(firstindex, count);
        }
    }
        
    public static String cleanString(String input) {
        if (input == null)
        	return "";
        StringBuffer output = new StringBuffer();
        for (int i = 0; i < input.length(); ++i) {
            char c = input.charAt(i);
            if (!Character.isIdentifierIgnorable(c))
                output.append(c);            
        }
        return stripNonValidXMLCharacters(output.toString().trim());
    }
    
    static private String stripNonValidXMLCharacters(String in) {
        if (in == null || ("".equals(in)))
        	return "";
        StringBuffer out = new StringBuffer();
        char current;
        for (int i = 0; i < in.length(); i++) {
            current = in.charAt(i);
            if ((current == 0x9) ||
                (current == 0xA) ||
                (current == 0xD) ||
                ((current >= 0x20) && (current <= 0xD7FF)) ||
                ((current >= 0xE000) && (current <= 0xFFFD)) ||
                ((current >= 0x10000) && (current <= 0x10FFFF)))
                out.append(current);
        }
        return out.toString();
    }  
    
    // parses title (from remix) and return the original string if can't be parsed
    static public String parseTitle(String name) {
    	while (name.endsWith(" "))
    		name = name.substring(0, name.length() - 1);
    	int index = name.length() - 1;
    	int endindex = -1;
    	int startindex = -1;
    	boolean done = false;
    	while ((index >= 0) && !done) {
    		if ((name.charAt(index) == ')') || (name.charAt(index) == ']')) endindex = index;
    		if ((name.charAt(index) == '(') || (name.charAt(index) == '[')) {
    			startindex = index;
    			done = true;
    		}
    		index--;
    	}
    	if (done) {
    		if (endindex == -1)
    			endindex = name.length();
    		startindex++;
    		endindex--;
    		if ((startindex < name.length()) && (endindex >= 0) && ((endindex == name.length() - 2) || (endindex == name.length() - 1)) && ((startindex - 2) >= 0)) {
    			String songname = name.substring(0, startindex - 1);
    			while (songname.endsWith(" "))
    				songname = songname.substring(0, songname.length() - 1);
    			return songname;
    		}
    	}
    	String suffix = " remix";
    	if (name.toLowerCase().endsWith(suffix)) {
    		String seperator = " - ";
    		int seperatorIndex = name.indexOf(seperator);
    		if (seperatorIndex >= 0) {
    			String songname = name.substring(0, seperatorIndex).trim();
    			return songname;
    		}
    	}  
    	suffix = " original mix";
    	if (name.toLowerCase().endsWith(suffix)) {
    		String seperator = " - ";
    		int seperatorIndex = name.indexOf(seperator);
    		if (seperatorIndex >= 0) {
    			String songname = name.substring(0, seperatorIndex).trim();
    			return songname;
    		}
    	}  
    	return name;
    }

    // parse remix from title field, returns "" if it can't be found
    static public String parseRemix(String name) {
    	while (name.endsWith(" ")) 
    		name = name.substring(0, name.length() - 1);
    	int index = name.length() - 1;
    	int endindex = -1;
    	int startindex = -1;
    	boolean done = false;
    	while ((index >= 0) && !done) {
    		if ((name.charAt(index) == ')') || (name.charAt(index) == ']')) endindex = index;
    		if ((name.charAt(index) == '(') || (name.charAt(index) == '[')) {
    			startindex = index;
    			done = true;
    		}
    		index--;
    	}
    	if (done) {
    		if (endindex == -1) 
    			endindex = name.length();
    		startindex++;
    		endindex--;
    		if ((startindex < name.length()) && (endindex >= 0) && ((endindex == name.length() - 2) || (endindex == name.length() - 1)) && ((startindex - 2) >= 0)) {
    			String remix = name.substring(startindex, endindex + 1);
    			return remix;
    		}
    	}
    	String suffix = " remix";
    	if (name.toLowerCase().endsWith(suffix)) {
    		String seperator = " - ";
    		int seperatorIndex = name.indexOf(seperator);
    		if (seperatorIndex >= 0) {
    			String remix = name.substring(seperatorIndex + 3).trim();
    			remix = remix.substring(0, remix.length() - suffix.length());
    			return remix + " remix";
    		}
    	}
    	return "";
    }
    
    static public String removeUnderscores(String input) {
        if (input.indexOf("_") < 0)
        	return input; // there are no underscores
        else {
        	StringBuffer sb = new StringBuffer("");
        	for (int i = 0; i < input.length(); ++i) {
        		if (input.charAt(i) == '_') sb.append(" ");
        		else sb.append(input.charAt(i));
        	}
        	return sb.toString();
        }
    }
    
    static public String removeSpaces(String input) {
    	StringBuffer result = new StringBuffer();
    	for (int c = 0; c < input.length(); ++c) {
    		char letter = input.charAt(c);
    		if (letter == ' ')
    			result.append("_");
    		else
    			result.append(letter);
    	}
    	return result.toString();
    }

    static public int getNumLines(String input) {
    	if (input == null)
    		return 0;
    	int lines = 1;
    	input = StringUtil.replace(input, "\r", "\n");
    	int lookPast = 0;
    	int index = input.indexOf("\n");
    	while (index >= 0) {
    		++lines;
    		lookPast = index + 1;
    		index = input.indexOf("\n", lookPast);
    	}
    	return lines;
    }
    
    static public String[] getLines(String input) {
    	if (input == null)
    		return new String[0];
    	String[] result = new String[getNumLines(input)];
    	int i = 0;
    	input = StringUtil.replace(input, "\r", "\n");
    	int lookPast = 0;
    	int index = input.indexOf("\n");
    	if (index < 0)
    		result[0] = input;
    	while (index >= 0) {
    		String line = input.substring(lookPast, index);
    		result[i++] = line;
    		lookPast = index + 1;
    		index = input.indexOf("\n", lookPast);
    	}    	
    	return result;
    }
    
    static public String removeNewLines(String input) {
        input = StringUtil.replace(input, "\n", "  ");
        input = StringUtil.replace(input, "\r", "  ");
        return input;
    }
        
    static public String getTruncatedDescription(Collection<String> values) {
    	StringBuffer result = new StringBuffer();
    	int max = 3;
    	int numUsed = 0;
    	for (String value : values) {
    		if (result.length() > 0)
    			result.append(", ");
    		result.append(value);
    		++numUsed;
    		if (numUsed >= max) {
    			if (values.size() > numUsed)
    				result.append("...");
    			break;
    		}
    	}
    	return result.toString();
    }
        
    static public String getTruncatedDescription(Vector<SongRecord> searchRecords) {
    	StringBuffer result = new StringBuffer();
    	int max = 3;
    	int numUsed = 0;
    	for (SearchRecord searchRecord : searchRecords) {
    		if (result.length() > 0)
    			result.append(", ");
    		result.append(searchRecord.toString());
    		++numUsed;
    		if (numUsed >= max) {
    			if (searchRecords.size() > numUsed)
    				result.append("...");
    			break;
    		}
    	}
    	return result.toString();
    }
    
    static public String getTruncatedDescription(Vector<Record> records, boolean newSigArg) {
    	StringBuffer result = new StringBuffer();
    	int max = 3;
    	int numUsed = 0;
    	for (Record record : records) {
    		if (result.length() > 0)
    			result.append(", ");
    		result.append(record.toString());
    		++numUsed;
    		if (numUsed >= max) {
    			if (records.size() > numUsed)
    				result.append("...");
    			break;
    		}
    	}
    	return result.toString();
    }
    
}
