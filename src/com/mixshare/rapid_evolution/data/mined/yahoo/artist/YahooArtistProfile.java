package com.mixshare.rapid_evolution.data.mined.yahoo.artist;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.search.artist.ArtistIdentifier;
import com.mixshare.rapid_evolution.data.mined.MiningAPIFactory;
import com.mixshare.rapid_evolution.data.mined.util.WebHelper;
import com.mixshare.rapid_evolution.data.mined.yahoo.YahooCommonProfile;
import com.mixshare.rapid_evolution.data.mined.yahoo.YahooMusicAPIWrapper;
import com.mixshare.rapid_evolution.data.record.search.artist.ArtistRecord;

public class YahooArtistProfile extends YahooCommonProfile {

    static private Logger log = Logger.getLogger(YahooArtistProfile.class);
    static private final long serialVersionUID = 0L;
                
    ////////////
    // FIELDS //
    ////////////
    
    private String artistName;
    private String artistId;
    private Map<String, String> similarArtists = new HashMap<String, String>(); // key is artist name, value is artist id
    private Vector<String> similarArtistNames = new Vector<String>(); // preserves case of artist name
    
    //////////////////
    // CONSTRUCTORS //
    //////////////////
    
    public YahooArtistProfile() {
    	super(DATA_TYPE_ARTISTS);
    }
    public YahooArtistProfile(String artistName) {
    	super(DATA_TYPE_ARTISTS);
    	this.artistName = artistName;    	
    	try {
    		this.artistId = MiningAPIFactory.getYahoomusicAPI().getArtistId(artistName);
    		if (artistId != null) {
    			isValid = true;
                if (log.isTraceEnabled())
                	log.trace("YahooArtistProfile(): fetching yahoo artistName=" + artistName + ", artistId=" + artistId);    			

                // categories
    			StringBuffer artistURL = new StringBuffer();
    			artistURL.append("http://us.music.yahooapis.com/artist/v1/item/");
    			artistURL.append(artistId);
    			artistURL.append("?appid=");
                artistURL.append(YahooMusicAPIWrapper.API_KEY);
                artistURL.append("&response=categories,videos");
                if (log.isTraceEnabled())
                	log.trace("YahooArtistProfile(): fetching artist info from url=" + artistURL.toString());
                Document doc = WebHelper.getDocumentResponseFromURL(artistURL.toString());
                MiningAPIFactory.getYahoomusicAPI().getRateController().startQuery();
                if (doc != null) {
    	            XPathFactory factory = XPathFactory.newInstance();
    	            XPath xPath = factory.newXPath();    	                	            
    	            NodeList categoryNodes = (NodeList)xPath.evaluate("//Artists/Artist/Category[@type='Genre']", doc, XPathConstants.NODESET);
    	            for (int i = 0; i < categoryNodes.getLength(); i++) 
    	            	categories.add(categoryNodes.item(i).getAttributes().getNamedItem("name").getTextContent());    	            
                }                
                
	            // similar items
    			StringBuffer similarURL = new StringBuffer();
    			similarURL.append("http://us.music.yahooapis.com/artist/v1/list/similar/");
    			similarURL.append(artistId);
    			similarURL.append("?appid=");
    			similarURL.append(YahooMusicAPIWrapper.API_KEY);
    			similarURL.append("&count=100");
                if (log.isTraceEnabled())
                	log.trace("YahooArtistProfile(): fetching similar from url=" + similarURL.toString());
                doc = WebHelper.getDocumentResponseFromURL(similarURL.toString());
                if (doc != null) {
    	            XPathFactory factory = XPathFactory.newInstance();
    	            XPath xPath = factory.newXPath();    	            
    	            Vector<String> artistIds = new Vector<String>();
    	            Vector<String> artistNames = new Vector<String>();
    	            NodeList idNodes = (NodeList)xPath.evaluate("//Artists/Artist", doc, XPathConstants.NODESET);
    	            for (int i = 0; i < idNodes.getLength(); i++) 
    	            	artistIds.add(idNodes.item(i).getAttributes().getNamedItem("id").getTextContent());
    	            NodeList nameNodes = (NodeList)xPath.evaluate("//Artists/Artist", doc, XPathConstants.NODESET);
    	            for (int i = 0; i < nameNodes.getLength(); i++) 
    	            	artistNames.add(nameNodes.item(i).getAttributes().getNamedItem("name").getTextContent());    	            
    	            for (int i = 0; i < artistIds.size(); ++i) {
    	            	similarArtists.put(artistNames.get(i).toLowerCase(), artistIds.get(i));
    	            	similarArtistNames.add(artistNames.get(i));
    	            }
                }
    		}
    		    		
            if (log.isTraceEnabled())
            	log.trace("YahooArtistProfile(): categories=" + categories);
            if (log.isTraceEnabled())
            	log.trace("YahooArtistProfile(): similar map=" + similarArtists);
    	} catch (Exception e) {
    		log.error("YahooArtistProfile(): error", e);
    	}    	
    }
    
    /////////////
    // GETTERS //
    /////////////
        
    public String getArtistName() { return artistName; }    
               
    public float getSimilarityWith(ArtistRecord artistRecord) {
    	float maxSimilarity = getSimilarityWith(artistRecord.getArtistName());
    	if (artistRecord.getDuplicateIds() != null) {
    		for (int dupId : artistRecord.getDuplicateIds()) {
    			ArtistIdentifier artistId = (ArtistIdentifier)Database.getArtistIndex().getIdentifierFromUniqueId(dupId);
    			if (artistId != null) {
    				float similarity = getSimilarityWith(artistId.getName());
    				if (similarity > maxSimilarity)
    					maxSimilarity = similarity;
    			}
    		}
    	}
    	return maxSimilarity;				    	
    }
	public float getSimilarityWith(String artistName) {
		if (similarArtists.containsKey(artistName.toLowerCase()))
			return 1.0f;
		return 0.0f;
	}		
	public Vector<String> getSimilarArtistNames() { return similarArtistNames; }
	
	public String getArtistId() {
		return artistId;
	}
	public void setArtistId(String artistId) {
		this.artistId = artistId;
	}
	public Map<String, String> getSimilarArtists() {
		return similarArtists;
	}
	public void setSimilarArtists(Map<String, String> similarArtists) {
		this.similarArtists = similarArtists;
	}
	public void setArtistName(String artistName) {
		this.artistName = artistName;
	}
	public void setSimilarArtistNames(Vector<String> similarArtistNames) {
		this.similarArtistNames = similarArtistNames;
	}
	
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
            log.info("result=" + new YahooArtistProfile("Squarepusher"));            
        } catch (Exception e) {
            log.error("main(): error", e);
        }        
    }    
    
}