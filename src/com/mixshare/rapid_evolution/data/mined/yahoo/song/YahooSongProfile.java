package com.mixshare.rapid_evolution.data.mined.yahoo.song;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.mined.MiningAPIFactory;
import com.mixshare.rapid_evolution.data.mined.util.WebHelper;
import com.mixshare.rapid_evolution.data.mined.yahoo.YahooCommonProfile;
import com.mixshare.rapid_evolution.data.mined.yahoo.YahooMusicAPIWrapper;

public class YahooSongProfile extends YahooCommonProfile {

    static private Logger log = Logger.getLogger(YahooSongProfile.class);
    static private final long serialVersionUID = 0L;
                
    ////////////
    // FIELDS //
    ////////////
    
    private String artistDescription;
    private String songDescription;
    private String trackId;
    
    //////////////////
    // CONSTRUCTORS //
    //////////////////
    
    public YahooSongProfile() {
    	super(DATA_TYPE_SONGS);
    }
    public YahooSongProfile(String artistDescription, String songDescription) {
    	super(DATA_TYPE_SONGS);
    	this.artistDescription = artistDescription;
    	this.songDescription = songDescription;
    	try {
    		this.trackId = MiningAPIFactory.getYahoomusicAPI().getSongId(artistDescription, songDescription);
    		if (trackId != null) {
    			isValid = true;
                if (log.isTraceEnabled())
                	log.trace("YahooArtistProfile(): fetching yahoo track=" + songDescription + ", trackId=" + trackId);    			

                // categories
    			StringBuffer trackURL = new StringBuffer();
    			trackURL.append("http://us.music.yahooapis.com/track/v1/item/");
    			trackURL.append(trackId);
    			trackURL.append("?appid=");
    			trackURL.append(YahooMusicAPIWrapper.API_KEY);
    			trackURL.append("&response=categories");
                if (log.isTraceEnabled())
                	log.trace("YahooArtistProfile(): fetching track info from url=" + trackURL.toString());
                Document doc = WebHelper.getDocumentResponseFromURL(trackURL.toString());
                MiningAPIFactory.getYahoomusicAPI().getRateController().startQuery();
                if (doc != null) {
    	            XPathFactory factory = XPathFactory.newInstance();
    	            XPath xPath = factory.newXPath();    	                	            
    	            NodeList categoryNodes = (NodeList)xPath.evaluate("//Tracks/Track/Category[@type='Genre']", doc, XPathConstants.NODESET);
    	            for (int i = 0; i < categoryNodes.getLength(); i++) 
    	            	categories.add(categoryNodes.item(i).getAttributes().getNamedItem("name").getTextContent());    	            
                }                
    		}
    		    		
            if (log.isTraceEnabled())
            	log.trace("YahooArtistProfile(): categories=" + categories);
    	} catch (Exception e) {
    		log.error("YahooArtistProfile(): error", e);
    	}    	
    }
    
    /////////////
    // GETTERS //
    /////////////
    
    public boolean isValid() { return isValid; }
                   
	public String getArtistDescription() {
		return artistDescription;
	}
	public void setArtistDescription(String artistDescription) {
		this.artistDescription = artistDescription;
	}
	public String getSongDescription() {
		return songDescription;
	}
	public void setSongDescription(String songDescription) {
		this.songDescription = songDescription;
	}
	public String getTrackId() {
		return trackId;
	}
	public void setTrackId(String trackId) {
		this.trackId = trackId;
	}
     
    /////////////
    // METHODS //
    /////////////
    
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append(artistDescription);
        result.append(" - ");
        result.append(songDescription);
        return result.toString();
    }
    
 
    static public void main(String[] args) {
        try {
        	RapidEvolution3.loadLog4J();
            log.info("result=" + new YahooSongProfile("Michael Jackson", "Billie Jean"));            
        } catch (Exception e) {
            log.error("main(): error", e);
        }        
    }
    
}