package com.mixshare.rapid_evolution.data.mined.bbc.artist;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.mined.MinedProfile;
import com.mixshare.rapid_evolution.data.mined.MinedProfileHeader;
import com.mixshare.rapid_evolution.data.mined.MiningAPIFactory;
import com.mixshare.rapid_evolution.data.mined.util.WebHelper;

/**
 * Only other relevant info worth mining (not from other sources) seems to be releases at the moment, and no XML format is supported for this
 */
public class BBCArtistProfile extends MinedProfile {

    static private Logger log = Logger.getLogger(BBCArtistProfile.class);
    static private final long serialVersionUID = 0L;
                
    ////////////
    // FIELDS //
    ////////////
    
    private boolean isValid = false;
    private String artistMbid;
    private String artistName;
    private String wikipediaContent;
    
    //////////////////
    // CONSTRUCTORS //
    //////////////////
    
    public BBCArtistProfile() {
    	super(new MinedProfileHeader(DATA_TYPE_ARTISTS, DATA_SOURCE_BBC));
    }
    public BBCArtistProfile(String artistName) {
    	super(new MinedProfileHeader(DATA_TYPE_ARTISTS, DATA_SOURCE_BBC));
    	this.artistName = artistName;    	
    	try {
    		artistMbid = MiningAPIFactory.getMusicbrainzAPI().getArtistMbId(artistName);
    		if ((artistMbid != null) && (artistMbid.length() > 0)) {    			
    			StringBuffer artistURL = new StringBuffer();
    			artistURL.append("http://www.bbc.co.uk/music/artists/");			
    			artistURL.append(artistMbid);
    			artistURL.append(".xml");
                if (log.isTraceEnabled())
                	log.trace("BBCArtistProfile(): fetching artist info from url=" + artistURL.toString());
                MiningAPIFactory.getBBCAPI().getRateController().startQuery();
                Document doc = WebHelper.getDocumentResponseFromURL(artistURL.toString());
                if (doc != null) {
    	            XPathFactory factory = XPathFactory.newInstance();
    	            XPath xPath = factory.newXPath();	            
    	            wikipediaContent = xPath.evaluate("//artist/wikipedia_article/content/text()", doc);
    	            if ((wikipediaContent != null) && (wikipediaContent.length() > 0))
    	            	isValid = true;
                }             
    		}            
            if (log.isTraceEnabled())
            	log.trace("BBCArtistProfile(): wikipediaContent=" + wikipediaContent);
    	} catch (Exception e) {
    		log.error("BBCArtistProfile(): error", e);
    	}    	
    }

    /////////////
    // GETTERS //
    /////////////
    
    public boolean isValid() { return isValid; }
    
    public String getArtistName() { return artistName; }    
	public String getArtistMbid() { return artistMbid; }
               
    public String getWikipediaContent() { return wikipediaContent; }
    
    /////////////
    // SETTERS //
    /////////////
    
	public void setArtistMbid(String artistMbid) { this.artistMbid = artistMbid; }
	public void setValid(boolean isValid) { this.isValid = isValid; }
	public void setArtistName(String artistName) { this.artistName = artistName; }
	public void setWikipediaContent(String wikipediaContent) { this.wikipediaContent = wikipediaContent; }    
    
    /////////////
    // METHODS //
    /////////////
    
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append(artistName);
        return result.toString();
    }
    
 
    static public void main(String[] args) {
        try {
        	RapidEvolution3.loadLog4J();
            log.info("result=" + new BBCArtistProfile("Boards of Canada"));            
        } catch (Exception e) {
            log.error("main(): error", e);
        }        
    }
            
}