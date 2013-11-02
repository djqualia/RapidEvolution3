package com.mixshare.rapid_evolution.data.mined.youtube;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.mined.util.WebHelper;

public class YouTubeAPIHelper {

    static private Logger log = Logger.getLogger(YouTubeAPIHelper.class);
	
    static public String getYouTubeIDFromURL(String url) {
		int youtubeIdIndex = url.lastIndexOf("=");
		if (youtubeIdIndex >= 0) {
			String youtubeId = url.substring(youtubeIdIndex + 1);		
			return youtubeId;
		}
		return null;    	
    }
    
	static public boolean isValidVideo(String youtubeID) {
		try {
			StringBuffer requestURL = new StringBuffer();
			requestURL.append("http://gdata.youtube.com/feeds/api/videos/");
			requestURL.append(youtubeID);			
			String text = WebHelper.getTextResponseFromURL(requestURL.toString());
			if (text.equalsIgnoreCase("Private video"))
				return false;
			Document doc = WebHelper.getDocumentResponseFromURL(requestURL.toString());			
            XPathFactory factory = XPathFactory.newInstance();
            XPath xPath = factory.newXPath();  
            String stateName = xPath.evaluate("//entry/control/state/@name", doc);
            String reasonCode = xPath.evaluate("//entry/control/state/@reasonCode", doc);
            if (log.isDebugEnabled())
            	log.debug("isValidVideo(): stateName=" + stateName);
            if ((stateName != null) && (stateName.equalsIgnoreCase("restricted"))) {
            	if ((reasonCode != null) && (reasonCode.equalsIgnoreCase("requesterRegion")))
            		return false;
            }
		} catch (Exception e) {
			if (log.isDebugEnabled()) // transformer exception was being thrown in some cases which was creating unnecessary error msgs
				log.debug("isValidVideo(): error", e);
		}
		return true;
	}
	
	static public void main(String[] args) {
		try {
			RapidEvolution3.loadLog4J();
			
			log.info("play...");
			log.info(isValidVideo("bWTuKd2lTo4"));
			log.info(isValidVideo("F22MidnAaTY"));		
			log.info(isValidVideo("cKyG1dRoDlA"));
			log.info(isValidVideo("8c-RNbcHcWw"));
			log.info(isValidVideo("Pwoij-B8NuE"));			
			log.info(isValidVideo("erG5rgNYSdk"));
			log.info(isValidVideo("HL_WvOly7mY"));
			log.info(isValidVideo("t0OVpyvey4U"));
			log.info(isValidVideo("bptVcwBrpOU"));
			log.info(isValidVideo("pq-yP7mb8UE"));
			log.info(isValidVideo("1-qcVbD4dRk"));
			
			log.info("don't play...");
			log.info(isValidVideo("5CRc30ynfd4"));
			log.info(isValidVideo("INoAivR-zJ8"));
			log.info(isValidVideo("rfn50lezTxI"));
			log.info(isValidVideo("w6foO8xt4W0"));
			log.info(isValidVideo("4zJQrR297QU"));
			
		} catch (Exception e) {
			log.error("main(): error", e);
		}
	}
	
}
