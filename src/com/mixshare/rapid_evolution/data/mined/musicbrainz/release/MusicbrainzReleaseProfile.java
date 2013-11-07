package com.mixshare.rapid_evolution.data.mined.musicbrainz.release;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.JenaException;
import com.ldodds.musicbrainz.Album;
import com.ldodds.musicbrainz.BeanPopulator;
import com.ldodds.musicbrainz.MusicBrainzException;
import com.ldodds.musicbrainz.MusicBrainzImpl;
import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.mined.MiningAPIFactory;
import com.mixshare.rapid_evolution.data.mined.musicbrainz.MusicbrainzCommonProfile;
import com.mixshare.rapid_evolution.data.mined.musicbrainz.song.MusicbrainzSongProfile;
import com.mixshare.rapid_evolution.data.mined.util.WebHelper;

public class MusicbrainzReleaseProfile extends MusicbrainzCommonProfile {

    static private Logger log = Logger.getLogger(MusicbrainzReleaseProfile.class);
    static private final long serialVersionUID = 0L;
            
    static private int MUSICBRAINZ_RELEASE_QUERY_DEPTH = RE3Properties.getInt("musicbrainz_release_query_depth");
    static private int MUSICBRAINZ_MAX_RELEASES_PER_SEARCH = RE3Properties.getInt("musicbrainz_max_releases_per_search");
    
    ////////////
    // FIELDS //
    ////////////
    
    private String artistDescription;
    private String artistMbid;
    private String releaseTitle;
    private String originalYearReleased;
    private String releaseGroupTitle;
    private String releaseGroupId;
    private String asin; // amazon standard identification number
    private String type;
    private Vector<MusicbrainzSongProfile> songs = new Vector<MusicbrainzSongProfile>(); // ordering of songs should coincide with release
    
    //////////////////
    // CONSTRUCTORS //
    //////////////////
    
    public MusicbrainzReleaseProfile() {
    	super(DATA_TYPE_RELEASES);
    }
    public MusicbrainzReleaseProfile(String artistDescription, String releaseTitle) {
    	super(DATA_TYPE_RELEASES);
    	this.artistDescription = artistDescription;
    	this.releaseTitle = releaseTitle;    	
    	try {
    		
    		StringBuffer url = new StringBuffer("http://musicbrainz.org/ws/1/release/?type=xml&title=");
    		url.append(URLEncoder.encode(releaseTitle, "UTF-8"));
    		url.append("&artist=");
    		url.append(URLEncoder.encode(artistDescription, "UTF-8"));
    		
    		Document doc = WebHelper.getDocumentResponseFromURL(url.toString());
    		if (doc != null) {
	            XPathFactory factory = XPathFactory.newInstance();
	            XPath xPath = factory.newXPath();

	            NodeList releaseNodes = (NodeList)xPath.evaluate("//metadata/release-list/release", doc, XPathConstants.NODESET);
	            for (int r = 0; r < releaseNodes.getLength(); ++r) {
	            	Node releaseNode = releaseNodes.item(r);
	            	
	            	String releaseMbid = (String)xPath.evaluate("./@id", releaseNode, XPathConstants.STRING);
	            	String title = (String)xPath.evaluate("./title/text()", releaseNode, XPathConstants.STRING);
	            	String artistName = null;
	            	NodeList artistNodes = (NodeList)xPath.evaluate("./artist", releaseNode, XPathConstants.NODESET);
	            	if ((artistNodes != null) && (artistNodes.getLength() > 1))
	            		artistName = "Various";
	            	else 
	            		artistName = (String)xPath.evaluate("./artist/name/text()", releaseNode, XPathConstants.STRING);
	            	
	            	if (releaseTitle.equalsIgnoreCase(title) && artistDescription.equalsIgnoreCase(artistName)) {
	            		// found a match
	            		fetchAdditionalReleaseInfo(releaseMbid);
	            		break;
	            	}	            	
	            }
    		}    		
    	} catch (JenaException je) {
    		if (log.isDebugEnabled())
    			log.debug("MusicbrainzReleaseProfile(): jena exception=" + je);
    		mbId = null;
    	} catch (MusicBrainzException mbe) {
    		if (log.isDebugEnabled())
    			log.debug("MusicbrainzReleaseProfile(): music brainz exception=" + mbe);
    		mbId = null;    		
    	} catch (Exception e) {
    		log.error("MusicbrainzReleaseProfile(): error", e);
    	}    	
    }
    
    public MusicbrainzReleaseProfile(String mbid) {
    	super(DATA_TYPE_RELEASES);
    	fetchAdditionalReleaseInfo(mbid);
    }    
    
    /////////////
    // GETTERS //
    /////////////
    
    public String getArtistMbId() { return artistMbid; }
    public String getArtistDescription() { return artistDescription; }
    public String getReleaseTitle() { return releaseTitle; }                   
    public String getReleaseGroupTitle() { return releaseGroupTitle; }
    
    public String getAmazonId() { return asin; }
    
    public String getOriginalYearReleased() { return originalYearReleased; }
    public short getOriginalYearReleasedShort() {
    	try {
    		return Short.parseShort(originalYearReleased);
    	} catch (Exception e) { }
    	return (short)0;
    }
    
    public Vector<MusicbrainzSongProfile> getSongs() { return songs; }
    
    public String getType() { return type; }
    
	public String getArtistMbid() {
		return artistMbid;
	}
	public void setArtistMbid(String artistMbid) {
		this.artistMbid = artistMbid;
	}
	public String getReleaseGroupId() {
		return releaseGroupId;
	}
	public void setReleaseGroupId(String releaseGroupId) {
		this.releaseGroupId = releaseGroupId;
	}
	public String getAsin() {
		return asin;
	}
	public void setAsin(String asin) {
		this.asin = asin;
	}
	public void setArtistDescription(String artistDescription) {
		this.artistDescription = artistDescription;
	}
	public void setReleaseTitle(String releaseTitle) {
		this.releaseTitle = releaseTitle;
	}
	public void setOriginalYearReleased(String originalYearReleased) {
		this.originalYearReleased = originalYearReleased;
	}
	public void setReleaseGroupTitle(String releaseGroupTitle) {
		this.releaseGroupTitle = releaseGroupTitle;
	}
	public void setType(String type) {
		this.type = type;
	}
	public void setSongs(Vector<MusicbrainzSongProfile> songs) {
		this.songs = songs;
	}
	
    /////////////
    // METHODS //
    /////////////
    
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append(artistDescription);
        result.append(" - ");
        result.append(releaseTitle);
        return result.toString();
    }
     
    private void fetchAdditionalReleaseInfo(String mbId) {
    	try {
			// additional info
			// some data isn't available in the api library
			StringBuffer releaseURL = new StringBuffer();
			releaseURL.append("http://musicbrainz.org/ws/1/release/");
			releaseURL.append(mbId);
			releaseURL.append("?type=xml&inc=artist+release-events+tracks+release-groups+url-rels+labels+tags+ratings");
			
			MiningAPIFactory.getMusicbrainzAPI().getRateController().startQuery();
			if (log.isTraceEnabled())
				log.trace("fetchAdditionalReleaseInfo(): fetching additional info from url=" + releaseURL.toString());
			Document doc = WebHelper.getDocumentResponseFromURL(releaseURL.toString());
			if (doc != null) {
	            XPathFactory factory = XPathFactory.newInstance();
	            XPath xPath = factory.newXPath();
	            
				this.mbId = xPath.evaluate("//metadata/release/@id", doc);	            
	            artistDescription = xPath.evaluate("//metadata/release/artist/name/text()", doc);
	            artistMbid = xPath.evaluate("//metadata/release/artist/@id", doc);
	            releaseTitle = xPath.evaluate("//metadata/release/title/text()", doc);
	            
	            // urls
	            NodeList urlNodes = (NodeList)xPath.evaluate("//metadata/release/relation-list[@target-type='Url']/relation", doc, XPathConstants.NODESET);
	            for (int i = 0; i < urlNodes.getLength(); i++) {
	                try {
	                    String type = urlNodes.item(i).getAttributes().getNamedItem("type").getTextContent();
	                    String url = urlNodes.item(i).getAttributes().getNamedItem("target").getTextContent();
	                    if (urls.containsKey(type)) {
	                    	Vector<String> typeUrls = urls.get(type);
	                    	typeUrls.add(url);    	                    	
	                    } else {
	                    	Vector<String> typeUrls = new Vector<String>();
	                    	typeUrls.add(url);
	                    	urls.put(type, typeUrls);
	                    }    	                    
	                } catch (Exception e) { }
	            }
	            
	            // tags
	            NodeList tagNodes = (NodeList)xPath.evaluate("//metadata/release/tag-list/tag", doc, XPathConstants.NODESET);
	            for (int i = 0; i < tagNodes.getLength(); i++) {
	                try {
	                    Integer count = Integer.parseInt(tagNodes.item(i).getAttributes().getNamedItem("count").getTextContent());
	                    String tagName = tagNodes.item(i).getTextContent();
	                    tags.put(tagName, count);
	                } catch (Exception e) { }
	            }
	            
	            // ratings
	            String avgRatingStr = xPath.evaluate("//metadata/release/rating/text()", doc);
	            if ((avgRatingStr != null) && (avgRatingStr.length() > 0))  {
	            	int numReleaseRaters = Integer.parseInt(xPath.evaluate("//metadata/release/rating/@votes-count", doc));
	            	avgRating += Float.parseFloat(avgRatingStr) * numReleaseRaters;
	            	numRaters += numReleaseRaters;
	            }
	            
	            // original year released
	            originalYearReleased = xPath.evaluate("//metadata/release/release-event-list/event/@date", doc); 
	            if ((originalYearReleased != null) && (originalYearReleased.length() >= 4)) {
	            	originalYearReleased = originalYearReleased.substring(0, 4);	            	
	            }
	            
	            // release group title
	            releaseGroupTitle = xPath.evaluate("//metadata/release/release-group/title/text()", doc);
	            releaseGroupId = xPath.evaluate("//metadata/release/release-group/@id", doc);
	           
	            // asin
	            asin = xPath.evaluate("//metadata/release/asin/text()", doc);
	            
	            // type
	            type = xPath.evaluate("//metadata/release/@type", doc);
	            
	            // tracks
	            NodeList trackNodes = (NodeList)xPath.evaluate("//metadata/release/track-list/track", doc, XPathConstants.NODESET);
	            for (int i = 0; i < trackNodes.getLength(); i++) {
	            	String trackId = trackNodes.item(i).getAttributes().getNamedItem("id").getTextContent();
	            	String trackTitle = xPath.evaluate("//metadata/release/track-list/track[" + (i + 1) + "]/title/text()", doc);
	            	String trackDurationStr = xPath.evaluate("//metadata/release/track-list/track[" + (i + 1) + "]/duration/text()", doc);
	            	int trackDuration = 0;
	            	if ((trackDurationStr != null) && (trackDurationStr.length() > 0))
	            		trackDuration = Integer.parseInt(trackDurationStr);
	            	Map<String, Integer> trackTags = new HashMap<String, Integer>();	            	
		            NodeList trackTagNodes = (NodeList)xPath.evaluate("//metadata/release/track-list/track[" + (i + 1) + "]/tag-list/tag", doc, XPathConstants.NODESET);
		            for (int t = 0; t < trackTagNodes.getLength(); t++) {
		                try {
		                    Integer count = Integer.parseInt(trackTagNodes.item(t).getAttributes().getNamedItem("count").getTextContent());
		                    String tagName = trackTagNodes.item(t).getTextContent();
		                    trackTags.put(tagName, count);
		                } catch (Exception e) { }
		            }
		            float avgTrackRating = 0.0f;
		            int numTrackRaters = 0;
		            String avgTrackRatingStr = xPath.evaluate("//metadata/release/track-list/track[" + (i + 1) + "]/rating", doc);
		            if ((avgTrackRatingStr != null) && (avgTrackRatingStr.length() > 0))  {
		            	avgTrackRating = Float.parseFloat(avgTrackRatingStr);
		            	numTrackRaters = Integer.parseInt(xPath.evaluate("//metadata/release/track-list/track[" + (i + 1) + "]/rating/@votes-count", doc));
		            }
		            if (log.isTraceEnabled())
		            	log.trace("fetchAdditionalReleaseInfo(): parsed track=" + trackTitle + ", id=" + trackId + ", trackDuration=" + trackDuration + ", trackTags=" + trackTags + ", avgTrackRating=" + avgTrackRating + ", numTrackRaters=" + numTrackRaters);

		            // merge tags with release tags
		            for (Entry<String, Integer> entry : trackTags.entrySet()) {
		            	String trackTagName = entry.getKey();
		            	Integer trackTagCount = entry.getValue();
		            	if (tags.containsKey(trackTagName)) {
		            		Integer existingCount = tags.get(trackTagName);
		            		existingCount += trackTagCount;
		            		tags.put(trackTagName, existingCount);
		            	} else {
		            		tags.put(trackTagName, trackTagCount);
		            	}
		            }
		            // merge ratings
		            if (numTrackRaters > 0) {
		            	avgRating += avgTrackRating * numTrackRaters;
		            	numRaters += numTrackRaters;
		            }
		            
		            MusicbrainzSongProfile song = new MusicbrainzSongProfile(trackId, trackTags, avgTrackRating, numTrackRaters, artistDescription, artistMbid,
		            		trackTitle, trackDuration, releaseTitle, this.mbId);		            
		            songs.add(song);		            
	            }
	            
			}
			if (numRaters > 0)
				avgRating /= numRaters;
			
            if (log.isTraceEnabled())
            	log.trace("fetchAdditionalReleaseInfo(): artistDescription=" + artistDescription + ", artistMbid=" + artistMbid + ", releaseTitle=" + releaseTitle);
            if (log.isTraceEnabled())
            	log.trace("fetchAdditionalReleaseInfo(): urls=" + urls);
            if (log.isTraceEnabled())
            	log.trace("fetchAdditionalReleaseInfo(): originalYearReleased=" + originalYearReleased);
            if (log.isTraceEnabled())
            	log.trace("fetchAdditionalReleaseInfo(): releaseGroupTitle=" + releaseGroupTitle + ", releaseGroupId=" + releaseGroupId);
            if (log.isTraceEnabled())
            	log.trace("fetchAdditionalReleaseInfo(): asin=" + asin);
            if (log.isTraceEnabled())
            	log.trace("fetchAdditionalReleaseInfo(): avgRating="+ avgRating + ", numRaters=" + numRaters);
            if (log.isTraceEnabled())
            	log.trace("fetchAdditionalReleaseInfo(): tags=" + tags);

    	} catch (Exception e) {
    		log.error("fetchAdditionalReleaseInfo(): error", e);
    	}
    }
    
    static public void main(String[] args) {
        try {
        	RapidEvolution3.loadLog4J();
            log.info("result=" + new MusicbrainzReleaseProfile("708614fd-50a3-4808-a05c-2a2a75450532"));            
        } catch (Exception e) {
            log.error("main(): error", e);
        }        
    }        
    
}