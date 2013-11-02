package com.mixshare.rapid_evolution.data.mined.musicbrainz.label;

import java.util.Vector;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.hp.hpl.jena.shared.JenaException;
import com.ldodds.musicbrainz.MusicBrainzException;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.mined.MiningAPIFactory;
import com.mixshare.rapid_evolution.data.mined.musicbrainz.MusicbrainzCommonProfile;
import com.mixshare.rapid_evolution.data.mined.util.WebHelper;

/**
 * The API doesn't seem to allow access to the releases yet (11/18/2009), however when viewing the HTML page of a label
 * on musicbrainz the releases can be listed.  This information could be parsed from the HTML, or perhaps the API will be
 * expanded...
 */
public class MusicbrainzLabelProfile extends MusicbrainzCommonProfile {

    static private Logger log = Logger.getLogger(MusicbrainzLabelProfile.class);
    static private final long serialVersionUID = 0L;
                
    ////////////
    // FIELDS //
    ////////////
    
    private String labelName;
    private Vector<String> aliases = new Vector<String>();
    private String type;
    private String labelCode;
    private String country;
    private String lifeSpanBegin;
    private String lifeSpanEnd;
    private Vector<String> relatedArtistNames = new Vector<String>();
    private Vector<String> relatedArtistIds = new Vector<String>();
    
    //////////////////
    // CONSTRUCTORS //
    //////////////////
    
    public MusicbrainzLabelProfile() {
    	super(DATA_TYPE_LABELS);
    }
    public MusicbrainzLabelProfile(String labelName) {
    	super(DATA_TYPE_LABELS);
    	this.labelName = labelName;    	
    	try {
    		String labelMbid = MiningAPIFactory.getMusicbrainzAPI().getLabelMbid(labelName);
    		if (labelMbid != null) {
    			this.mbId = labelMbid;
    			StringBuffer labelURL = new StringBuffer();
    			labelURL.append("http://musicbrainz.org/ws/1/label/");
    			labelURL.append(labelMbid);
    			labelURL.append("?type=xml&inc=aliases+artist-rels+url-rels+tags+ratings+release-rels");
    			if (log.isTraceEnabled())
    				log.trace("MusicbrainzLabelProfile(): fetching additional info from url=" + labelURL.toString());
    			MiningAPIFactory.getMusicbrainzAPI().getRateController().startQuery();
    			Document doc = WebHelper.getDocumentResponseFromURL(labelURL.toString());
    			if (doc != null) {
    	            XPathFactory factory = XPathFactory.newInstance();
    	            XPath xPath = factory.newXPath();
    	            
    	            // urls
    	            NodeList urlNodes = (NodeList)xPath.evaluate("//metadata/label/relation-list[@target-type='Url']/relation", doc, XPathConstants.NODESET);
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
                    if (log.isTraceEnabled())
                    	log.trace("MusicbrainzLabelProfile(): urls=" + urls);
    	            
    	            // tags
    	            NodeList tagNodes = (NodeList)xPath.evaluate("//metadata/label/tag-list/tag", doc, XPathConstants.NODESET);
    	            for (int i = 0; i < tagNodes.getLength(); i++) {
    	                try {
    	                    Integer count = Integer.parseInt(tagNodes.item(i).getAttributes().getNamedItem("count").getTextContent());
    	                    String tagName = tagNodes.item(i).getTextContent();
    	                    tags.put(tagName, count);
    	                } catch (Exception e) { }
    	            }
                    if (log.isTraceEnabled())
                    	log.trace("MusicbrainzLabelProfile(): tags=" + tags);
    	            
    	            // ratings
    	            String avgRatingStr = xPath.evaluate("//metadata/label/rating/text()", doc);
    	            if ((avgRatingStr != null) && (avgRatingStr.length() > 0))  {
    	            	avgRating = Float.parseFloat(avgRatingStr);
    	            	numRaters = Integer.parseInt(xPath.evaluate("//metadata/label/rating/@votes-count", doc));
	                    if (log.isTraceEnabled())
	                    	log.trace("MusicbrainzLabelProfile(): found rating, avgRating="+ avgRating + ", numRaters=" + numRaters);
    	            }
    	            
    	            // aliases
    	            NodeList aliasNodes = (NodeList)xPath.evaluate("//metadata/label/alias-list/alias", doc, XPathConstants.NODESET);
    	            for (int i = 0; i < aliasNodes.getLength(); i++) {
    	                try {
    	                    String alias = aliasNodes.item(i).getTextContent();
    	                    aliases.add(alias);
    	                } catch (Exception e) { }
    	            }
                    if (log.isTraceEnabled())
                    	log.trace("MusicbrainzLabelProfile(): aliases=" + aliases);
    	            
                    type = xPath.evaluate("//metadata/label/@type", doc);
                    labelCode = xPath.evaluate("//metadata/label/label-code/text()", doc);
                    country = xPath.evaluate("//metadata/label/country/text()", doc);
                    lifeSpanBegin = xPath.evaluate("//metadata/label/life-span/@begin", doc);
                    lifeSpanEnd = xPath.evaluate("//metadata/label/life-span/@end", doc);

                    if (log.isTraceEnabled())
                    	log.trace("MusicbrainzLabelProfile(): type=" + type + ", labelCode=" + labelCode);
                    if (log.isTraceEnabled())
                    	log.trace("MusicbrainzLabelProfile(): country=" + country + ", lifeSpanBegin=" + lifeSpanBegin + ", lifeSpanEnd=" + lifeSpanEnd);

                    
                    // artists
    	            NodeList artistNodes = (NodeList)xPath.evaluate("//metadata/label/relation-list[@target-type='Artist']/relation/artist", doc, XPathConstants.NODESET);
    	            for (int i = 0; i < artistNodes.getLength(); i++) {
	                    String artistId = artistNodes.item(i).getAttributes().getNamedItem("id").getTextContent();
	                    relatedArtistIds.add(artistId);
    	            }
    	            NodeList artistNameNodes = (NodeList)xPath.evaluate("//metadata/label/relation-list[@target-type='Artist']/relation/artist/name", doc, XPathConstants.NODESET);
    	            for (int i = 0; i < artistNameNodes.getLength(); i++) {
	                    String artistName = artistNameNodes.item(i).getTextContent();
	                    relatedArtistNames.add(artistName);
    	            }
                    if (log.isTraceEnabled())
                    	log.trace("MusicbrainzLabelProfile(): relatedArtistIds=" + relatedArtistIds);
                    if (log.isTraceEnabled())
                    	log.trace("MusicbrainzLabelProfile(): relatedArtistNames=" + relatedArtistNames);                    
    			}    			
    		}    		
    	} catch (JenaException je) {
    		if (log.isDebugEnabled())
    			log.debug("MusicbrainzLabelProfile(): jena exception=" + je);
    		mbId = null;    		              
    	} catch (MusicBrainzException mbe) {
    		if (log.isDebugEnabled())
    			log.debug("MusicbrainzLabelProfile(): music brainz exception=" + mbe);
    		mbId = null;    		
    	} catch (Exception e) {
    		log.error("MusicbrainzLabelProfile(): error", e);
    	}    	
    }
    
    /////////////
    // GETTERS //
    /////////////
    
    public String getLabelName() { return labelName; }    
    
    public String getCountry() { return country; }
    
    public String getLifespanBegin() { return lifeSpanBegin; }
    public String getLifespanEnd() { return lifeSpanEnd; }
    
    public String getType() { return type; }    
    
    public Vector<String> getAliases() { return aliases; }
    
    public String getLabelCode() { return labelCode; }
    
	public String getLifeSpanBegin() {
		return lifeSpanBegin;
	}
	public void setLifeSpanBegin(String lifeSpanBegin) {
		this.lifeSpanBegin = lifeSpanBegin;
	}
	public String getLifeSpanEnd() {
		return lifeSpanEnd;
	}
	public void setLifeSpanEnd(String lifeSpanEnd) {
		this.lifeSpanEnd = lifeSpanEnd;
	}
	public Vector<String> getRelatedArtistNames() {
		return relatedArtistNames;
	}
	public void setRelatedArtistNames(Vector<String> relatedArtistNames) {
		this.relatedArtistNames = relatedArtistNames;
	}
	public Vector<String> getRelatedArtistIds() {
		return relatedArtistIds;
	}
	public void setRelatedArtistIds(Vector<String> relatedArtistIds) {
		this.relatedArtistIds = relatedArtistIds;
	}
	public void setLabelName(String labelName) {
		this.labelName = labelName;
	}
	public void setAliases(Vector<String> aliases) {
		this.aliases = aliases;
	}
	public void setType(String type) {
		this.type = type;
	}
	public void setLabelCode(String labelCode) {
		this.labelCode = labelCode;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	
    /////////////
    // METHODS //
    /////////////
    
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append(labelName);
        return result.toString();
    }
        
    static public void main(String[] args) {
        try {
        	RapidEvolution3.loadLog4J();
            log.info("result=" + new MusicbrainzLabelProfile("Rephlex"));            
        } catch (Exception e) {
            log.error("main(): error", e);
        }        
    }
   
}