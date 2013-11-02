package com.mixshare.rapid_evolution.data.mined.musicbrainz.song;

import java.net.URLEncoder;
import java.util.Map;
import java.util.Vector;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.hp.hpl.jena.shared.JenaException;
import com.ldodds.musicbrainz.MusicBrainzException;
import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.mined.MiningAPIFactory;
import com.mixshare.rapid_evolution.data.mined.musicbrainz.MusicbrainzCommonProfile;
import com.mixshare.rapid_evolution.data.mined.util.WebHelper;

public class MusicbrainzSongProfile extends MusicbrainzCommonProfile {

    static private Logger log = Logger.getLogger(MusicbrainzSongProfile.class);
    static private final long serialVersionUID = 0L;
    
    static private int MUSICBRAINZ_SONG_QUERY_DEPTH = RE3Properties.getInt("musicbrainz_song_query_depth");
    static private int MUSICBRAINZ_MAX_SONGS_PER_SEARCH = RE3Properties.getInt("musicbrainz_max_songs_per_search");
    
    ////////////
    // FIELDS //
    ////////////
    
    private Vector<String> additionalMbids = new Vector<String>();
    private String artistDescription;
    private String artistMbid;
    private String songDescription;
    private int duration; // milliseconds
    private Vector<String> releaseTitles = new Vector<String>();
    private Vector<String> releaseMbids = new Vector<String>();
    private Vector<String> puids = new Vector<String>();
    
    //////////////////
    // CONSTRUCTORS //
    //////////////////    
    
    public MusicbrainzSongProfile() {
    	super(DATA_TYPE_SONGS);
    }
    public MusicbrainzSongProfile(String artistDescription, String songDescription) {
    	super(DATA_TYPE_SONGS);
    	this.artistDescription = artistDescription;    	
    	this.songDescription = songDescription;
    	try {
    		
    		StringBuffer trackUrl = new StringBuffer("http://musicbrainz.org/ws/1/track/?type=xml&title=");
    		trackUrl.append(URLEncoder.encode(songDescription, "UTF-8"));
    		trackUrl.append("&artist=");
    		trackUrl.append(URLEncoder.encode(artistDescription, "UTF-8"));
    		
    		Document doc = WebHelper.getDocumentResponseFromURL(trackUrl.toString());
    		if (doc != null) {
	            XPathFactory factory = XPathFactory.newInstance();
	            XPath xPath = factory.newXPath();

	            NodeList trackNodes = (NodeList)xPath.evaluate("//metadata/track-list/track", doc, XPathConstants.NODESET);
	            for (int r = 0; r < trackNodes.getLength(); ++r) {
	            	Node trackNode = trackNodes.item(r);
	            	
	            	String trackMbid = (String)xPath.evaluate("./@id", trackNode, XPathConstants.STRING);
	            	String title = (String)xPath.evaluate("./title/text()", trackNode, XPathConstants.STRING);
	            	String artistName = null;
	            	NodeList artistNodes = (NodeList)xPath.evaluate("./artist", trackNode, XPathConstants.NODESET);
	            	if ((artistNodes != null) && (artistNodes.getLength() > 1))
	            		artistName = "Various";
	            	else 
	            		artistName = (String)xPath.evaluate("./artist/name/text()", trackNode, XPathConstants.STRING);
	            	
	            	if (songDescription.equalsIgnoreCase(title) && artistDescription.equalsIgnoreCase(artistName)) {
	            		// found a match
	    				if (this.mbId == null)
	    					this.mbId = trackMbid;
	    				else
	    					additionalMbids.add(trackMbid);
	    				
	    				duration = Integer.parseInt((String)xPath.evaluate("./duration/text()", trackNode, XPathConstants.STRING));
	    				NodeList releaseNodes = (NodeList)xPath.evaluate("./release-list/release", trackNode, XPathConstants.NODESET);
	    				for (int s = 0; s < releaseNodes.getLength(); ++s) {
	    					Node releaseNode = releaseNodes.item(s);	    					
	    					String releaseTitle = (String)xPath.evaluate("./title/text()", releaseNode, XPathConstants.STRING);
	    					String releaseMbid = (String)xPath.evaluate("./@id", releaseNode, XPathConstants.STRING);
	    					if (!this.releaseTitles.contains(releaseTitle))
	    						this.releaseTitles.add(releaseTitle);
	    					if (!this.releaseMbids.contains(releaseMbid))
	    						this.releaseMbids.add(releaseMbid);    					
	    				}
	    				for (int a = 0; a < artistNodes.getLength(); ++a) {
	    					Node artistNode = artistNodes.item(a);
	    					artistName = (String)xPath.evaluate("./name/text()", artistNode, XPathConstants.STRING);
	    					String artistMbid = (String)xPath.evaluate("./@id", artistNode, XPathConstants.STRING);
	    					this.artistDescription = artistName;
	    					this.artistMbid = artistMbid;
	    				}
	    				this.songDescription = title;	    				
	    				
	    				// additional info
	        			// some data isn't available in the api library
	        			StringBuffer trackURL = new StringBuffer();
	        			trackURL.append("http://musicbrainz.org/ws/1/track/");
	        			trackURL.append(trackMbid);
	        			trackURL.append("?type=xml&inc=artist+releases+puids+url-rels+tags+ratings");
	    				
	        			MiningAPIFactory.getMusicbrainzAPI().getRateController().startQuery();
	        			if (log.isTraceEnabled())
	        				log.trace("MusicbrainzSongProfile(): fetching additional info from url=" + trackURL.toString());
	        			doc = WebHelper.getDocumentResponseFromURL(trackURL.toString());
	        			if (doc != null) {
	        				
	        	            // urls
	        	            NodeList urlNodes = (NodeList)xPath.evaluate("//metadata/track/relation-list[@target-type='Url']/relation", doc, XPathConstants.NODESET);
	        	            for (int i = 0; i < urlNodes.getLength(); i++) {
	        	                try {
	        	                    String type = urlNodes.item(i).getAttributes().getNamedItem("type").getTextContent();
	        	                    String url = urlNodes.item(i).getAttributes().getNamedItem("target").getTextContent();
	        	                    if (urls.containsKey(type)) {
	        	                    	Vector<String> typeUrls = urls.get(type);
	        	                    	if (!typeUrls.contains(url))
	        	                    		typeUrls.add(url);    	                    	
	        	                    } else {
	        	                    	Vector<String> typeUrls = new Vector<String>();
	        	                    	typeUrls.add(url);
	        	                    	urls.put(type, typeUrls);
	        	                    }    	                    
	        	                } catch (Exception e) { }
	        	            }
	        	            
	        	            // tags
	        	            NodeList tagNodes = (NodeList)xPath.evaluate("//metadata/track/tag-list/tag", doc, XPathConstants.NODESET);
	        	            for (int i = 0; i < tagNodes.getLength(); i++) {
	        	                try {
	        	                    Integer count = Integer.parseInt(tagNodes.item(i).getAttributes().getNamedItem("count").getTextContent());
	        	                    String tagName = tagNodes.item(i).getTextContent();
	        	                    if (tags.containsKey(tagName)) {
	        	                    	Integer tagCount = tags.get(tagName);
	        	                    	tagCount += count;
	        	                    	tags.put(tagName, tagCount);
	        	                    } else {
	        	                    	tags.put(tagName, count);
	        	                    }
	        	                } catch (Exception e) { }
	        	            }
	        	            
	        	            // ratings
	        	            String avgRatingStr = xPath.evaluate("//metadata/track/rating/text()", doc);
	        	            if ((avgRatingStr != null) && (avgRatingStr.length() > 0))  {
	    	            		int numReleaseRaters = Integer.parseInt(xPath.evaluate("//metadata/track/rating/@votes-count", doc));
	        	            	avgRating += Float.parseFloat(avgRatingStr) * numReleaseRaters;
	        	            	numRaters += numReleaseRaters;
	        	            }
	        	            
	        	            // puids
	        	            NodeList puidNodes = (NodeList)xPath.evaluate("//metadata/track/puid-list/puid", doc, XPathConstants.NODESET);
	        	            for (int i = 0; i < puidNodes.getLength(); i++) {
	    	                    String puid = puidNodes.item(i).getAttributes().getNamedItem("id").getTextContent();
	    	                    if (!puids.contains(puid))
	    	                    	puids.add(puid);
	        	            }

	        	            // releases
	        	            releaseNodes = (NodeList)xPath.evaluate("//metadata/track/release-list/release", doc, XPathConstants.NODESET);
	        	            for (int i = 0; i < releaseNodes.getLength(); i++) {
	    	                    String releaseMbid = releaseNodes.item(i).getAttributes().getNamedItem("id").getTextContent();
	    	                    if (!releaseMbids.contains(releaseMbid)) {
	    	                    	releaseMbids.add(releaseMbid);
	    	                    }
	        	            }
	        	            NodeList releaseTitlesNodes = (NodeList)xPath.evaluate("//metadata/track/release-list/release/title", doc, XPathConstants.NODESET);
	        	            for (int i = 0; i < releaseTitlesNodes.getLength(); i++) {
	    	                    String releaseTitle = releaseTitlesNodes.item(i).getTextContent();
	    	                    if (!releaseTitles.contains(releaseTitle))
	    	                    	releaseTitles.add(releaseTitle);
	        	            }
	        			}    
	    				
	    			}	            	
	            }
    		}      		
    		
    		if (numRaters > 0)
    			avgRating /= numRaters;
    		
            if (log.isTraceEnabled())
            	log.trace("MusicbrainzSongProfile(): duration=" + duration);
            if (log.isTraceEnabled())
            	log.trace("MusicbrainzSongProfile(): releaseTitles=" + releaseTitles + ", releaseMbids=" + releaseMbids);
            if (log.isTraceEnabled())
            	log.trace("MusicbrainzSongProfile(): urls=" + urls);
            if (log.isTraceEnabled())
            	log.trace("MusicbrainzSongProfile(): tags=" + tags);
            if (log.isTraceEnabled())
            	log.trace("MusicbrainzSongProfile(): avgRating="+ avgRating + ", numRaters=" + numRaters);
            if (log.isTraceEnabled())
            	log.trace("MusicbrainzSongProfile(): puids=" + puids);
    	} catch (JenaException je) {
    		if (log.isDebugEnabled())
    			log.debug("MusicbrainzSongProfile(): jena exception=" + je);
    		mbId = null;    		            
    	} catch (MusicBrainzException mbe) {
    		if (log.isDebugEnabled())
    			log.debug("MusicbrainzSongProfile(): music brainz exception=" + mbe);
    		mbId = null;    		
    	} catch (Exception e) {
    		log.error("MusicbrainzSongProfile(): error", e);
    	}    	
    }
    
    public MusicbrainzSongProfile(String mbId, Map<String, Integer> tags, float avgRating, int numRaters, String artistDescription, String artistMbid,
    		String songDescription, int duration, String releaseTitle, String releaseMbid) {
    	super(DATA_TYPE_SONGS);
    	this.mbId = mbId;
    	this.tags = tags;
    	this.avgRating = avgRating;
    	this.numRaters = numRaters;
    	this.artistDescription = artistDescription;
    	this.artistMbid = artistMbid;
    	this.songDescription = songDescription;
    	this.duration = duration;
    	releaseTitles.add(releaseTitle);
    	releaseMbids.add(releaseMbid);
    }    
    
    /////////////
    // GETTERS //
    /////////////
    
    public String getArtistMbId() { return artistMbid; }
    public String getArtistDescription() { return artistDescription; }
    
    public String getSongName() { return songDescription; }
    
    public int getDuration() { return duration; }
    
    public boolean containsPuid(String puid) { return puids.contains(puid); }
    public boolean containsReleaseTitle(String releaseTitle) { return releaseTitles.contains(releaseTitle); }    
               
	public Vector<String> getAdditionalMbids() {
		return additionalMbids;
	}
	public void setAdditionalMbids(Vector<String> additionalMbids) {
		this.additionalMbids = additionalMbids;
	}
	public String getArtistMbid() {
		return artistMbid;
	}
	public void setArtistMbid(String artistMbid) {
		this.artistMbid = artistMbid;
	}
	public String getSongDescription() {
		return songDescription;
	}
	public void setSongDescription(String songDescription) {
		this.songDescription = songDescription;
	}
	public Vector<String> getReleaseTitles() {
		return releaseTitles;
	}
	public void setReleaseTitles(Vector<String> releaseTitles) {
		this.releaseTitles = releaseTitles;
	}
	public Vector<String> getReleaseMbids() {
		return releaseMbids;
	}
	public void setReleaseMbids(Vector<String> releaseMbids) {
		this.releaseMbids = releaseMbids;
	}
	public Vector<String> getPuids() {
		return puids;
	}
	public void setPuids(Vector<String> puids) {
		this.puids = puids;
	}
	public void setArtistDescription(String artistDescription) {
		this.artistDescription = artistDescription;
	}
	public void setDuration(int duration) {
		this.duration = duration;
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
            log.info("result=" + new MusicbrainzSongProfile("Boards of Canada", "Roygbiv"));            
        } catch (Exception e) {
            log.error("main(): error", e);
        }        
    }
    
}