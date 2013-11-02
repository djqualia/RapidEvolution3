package test.com.mixshare.rapid_evolution.util;

import org.apache.log4j.Logger;

import test.RE3TestCase;
import com.mixshare.rapid_evolution.util.StringUtil;

public class StringUtilTest extends RE3TestCase {

    private static Logger log = Logger.getLogger(StringUtilTest.class);

    public void testSubstring() {
    	if (!StringUtil.substring("aphex", "Aphex Twin"))
    		fail("testSubstring");
    	if (!StringUtil.substring("aphex ", "Aphex Twin"))
    		fail("testSubstring");
    	if (!StringUtil.substring("same thing", "same thing"))
    		fail("testSubstring");
    	if (!StringUtil.substring("", "Aphex Twin"))
    		fail("testSubstring");
    	if (StringUtil.substring("aphex q", "Aphex Twin"))
    		fail("testSubstring");
    	if (StringUtil.substring("aphex twins", "Aphex Twin"))
    		fail("testSubstring");
    }
    
    public void testGetDurationAsString() {
    	if (!StringUtil.getDurationAsString(90).equals("1:30"))
    		fail("testGetDurationAsString");
    	if (!StringUtil.getDurationAsString(60).equals("1:00"))
    		fail("testGetDurationAsString");
    	if (!StringUtil.getDurationAsString(65).equals("1:05"))
    		fail("testGetDurationAsString");
    	if (!StringUtil.getDurationAsString(32).equals("0:32"))
    		fail("testGetDurationAsString");
    	if (!StringUtil.getDurationAsString(160).equals("2:40"))
    		fail("testGetDurationAsString");
    }

	
}
