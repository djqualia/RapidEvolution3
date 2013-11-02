package com.mixshare.rapid_evolution.data.mined.musicbrainz.artist;

import java.net.URLEncoder;
import java.util.HashMap;
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

import com.hp.hpl.jena.shared.JenaException;
import com.ldodds.musicbrainz.MusicBrainzException;
import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.mined.MiningAPIFactory;
import com.mixshare.rapid_evolution.data.mined.musicbrainz.MusicbrainzCommonProfile;
import com.mixshare.rapid_evolution.data.mined.musicbrainz.release.MusicbrainzReleaseProfile;
import com.mixshare.rapid_evolution.data.mined.util.WebHelper;

public class MusicbrainzArtistProfile extends MusicbrainzCommonProfile {

    static private Logger log = Logger.getLogger(MusicbrainzArtistProfile.class);
    static private final long serialVersionUID = 0L;
            
    static public int MUSICBRAINZ_ARTIST_QUERY_DEPTH = RE3Properties.getInt("musicbrainz_artist_query_depth");
    
    static private String RELEASE_TYPE_SINGLE = "Single";
    static private String RELEASE_TYPE_ALBUM = "Album Official";
    static private String RELEASE_TYPE_LIVE = "Live";
    static private String RELEASE_TYPE_REMIX = "Remix";
    static private String RELEASE_TYPE_COMPILATION = "Compilation";
    static private String RELEASE_TYPE_SOUNDTRACK = "Soundtrack";
    static private String RELEASE_TYPE_INTERVIEW = "Interview";
    static private String RELEASE_TYPE_SPOKENWORD = "Spokenword";
    static private String RELEASE_TYPE_AUDIOBOOK = "Audiobook";
    
    static String[] validReleaseTypesForFetches = { RELEASE_TYPE_SINGLE, RELEASE_TYPE_ALBUM, RELEASE_TYPE_LIVE, RELEASE_TYPE_REMIX };
    static String[] invalidReleaseTypesForFetches = { RELEASE_TYPE_COMPILATION, RELEASE_TYPE_SOUNDTRACK, RELEASE_TYPE_INTERVIEW, RELEASE_TYPE_SPOKENWORD, RELEASE_TYPE_AUDIOBOOK };
    
    ////////////
    // FIELDS //
    ////////////
    
    private String artistName;
    private Vector<String> aliases = new Vector<String>(); // seem more like discogs name variations
    private Vector<String> relatedArtists = new Vector<String>(); // more like discogs aliases
    private Vector<String> labels = new Vector<String>();
    private String type;
    private String lifeSpanBegin;
    private String lifeSpanEnd;
    private Map<String, Vector<String>> allReleaseIds = new HashMap<String, Vector<String>>(); // key is release type, value is mbids
    private Vector<MusicbrainzReleaseProfile> releases = new Vector<MusicbrainzReleaseProfile>();
    
    //////////////////
    // CONSTRUCTORS //
    //////////////////
    
    public MusicbrainzArtistProfile() {
    	super(DATA_TYPE_ARTISTS);
    }
    public MusicbrainzArtistProfile(String artistName) {
    	super(DATA_TYPE_ARTISTS);
    	this.artistName = artistName;
    	try {    		
    		String artistMbid = MiningAPIFactory.getMusicbrainzAPI().getArtistMbId(artistName);
    		if (artistMbid != null)
    			loadArtist(artistMbid);    			
    		
    		/*
    		Model results = server.findArtistByName(artistName, MUSICBRAINZ_ARTIST_QUERY_DEPTH, 10);
    		List<Artist> artists = BeanPopulator.getArtists(results);
    		Vector<Artist> possibleMatches = new Vector<Artist>();
    		for (Artist artist : artists) {
    			if (artist.getName().equalsIgnoreCase(artistName))
    				possibleMatches.add(artist);    			
    		}
    		if (possibleMatches.size() == 1) {
    			loadArtist(possibleMatches.get(0));
    		} else if (possibleMatches.size() > 1) {
    			Vector<Integer> releaseMatches = new Vector<Integer>(possibleMatches.size());
    			int i = 0;
    			for (Artist artist : possibleMatches) {
    				int releaseCount = 0;
    				List<Album> albums = artist.getAlbums();
    				for (Album album : albums) {
    					ReleaseIdentifier releaseId = album.isCompilation() ? new ReleaseIdentifier(album.getName()) : new ReleaseIdentifier(album.getArtist().getName(), album.getName());
    					if (Database.getReleaseIndex().doesExist(releaseId))
    						++releaseCount;
    				}
    				releaseMatches.add(releaseCount);
    				++i;
    			}
    			int maxIndex = -1;
    			for (i = 0; i < releaseMatches.size(); ++i)
    				if ((maxIndex == -1) || (releaseMatches.get(i) > releaseMatches.get(maxIndex)))
    					maxIndex = i;    			
    			if (releaseMatches.get(maxIndex) > 0)
    				loadArtist(possibleMatches.get(maxIndex));    			
    		}
    		*/
    	} catch (JenaException je) {
    		if (log.isDebugEnabled())
    			log.debug("MusicbrainzArtistProfile(): jena exception=" + je);
    		mbId = null;    		
    	} catch (MusicBrainzException mbe) {
    		if (log.isDebugEnabled())
    			log.debug("MusicbrainzArtistProfile(): music brainz exception=" + mbe);
    		mbId = null;    		
    	} catch (Exception e) {
    		log.error("MusicbrainzArtistProfile(): error", e);
    	}
    }

    public MusicbrainzArtistProfile(String mbid, boolean useMbid) {
    	super(DATA_TYPE_ARTISTS);
    	try {
			loadArtist(mbid);    	
    	} catch (JenaException je) {
    		if (log.isDebugEnabled())
    			log.debug("MusicbrainzArtistProfile(): jena exception=" + je);
    		mbId = null;    		
    	} catch (MusicBrainzException mbe) {
    		if (log.isDebugEnabled())
    			log.debug("MusicbrainzArtistProfile(): music brainz exception=" + mbe);
    		mbId = null;    		
    	} catch (Exception e) {    		
    		log.error("MusicbrainzArtistProfile(): error", e);
    	}
    }    
    
    /////////////
    // GETTERS //
    /////////////
    
    public String getArtistName() { return artistName; }    
            
    public Vector<MusicbrainzReleaseProfile> getReleaseProfiles() { return releases; }
    
    public Vector<String> getAliases() { return aliases; }
    public Vector<String> getRelatedArtists() { return relatedArtists; }
    
    public String getType() { return type; }
    
    public String getLifespanBegin() { return lifeSpanBegin; }
    public String getLifespanEnd() { return lifeSpanEnd; }
    
	public Vector<String> getLabels() {
		return labels;
	}
	public void setLabels(Vector<String> labels) {
		this.labels = labels;
	}
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
	public Map<String, Vector<String>> getAllReleaseIds() {
		return allReleaseIds;
	}
	public void setAllReleaseIds(Map<String, Vector<String>> allReleaseIds) {
		this.allReleaseIds = allReleaseIds;
	}
	public Vector<MusicbrainzReleaseProfile> getReleases() {
		return releases;
	}
	public void setReleases(Vector<MusicbrainzReleaseProfile> releases) {
		this.releases = releases;
	}
	public void setArtistName(String artistName) {
		this.artistName = artistName;
	}
	public void setAliases(Vector<String> aliases) {
		this.aliases = aliases;
	}
	public void setRelatedArtists(Vector<String> relatedArtists) {
		this.relatedArtists = relatedArtists;
	}
	public void setType(String type) {
		this.type = type;
	}
    
    /////////////
    // METHODS //
    /////////////
    
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append(artistName);
        return result.toString();
    }
    
    protected boolean isValidReleaseTypeForFetching(String type) {
    	for (String validType : validReleaseTypesForFetches)
    		if (validType.equals(type))
    			return true;
    	for (String validType : invalidReleaseTypesForFetches)
    		if (validType.equals(type))
    			return false;
    	return true;
    }
     
    public void loadArtist(String artistMbid) {
    	try {
			if (log.isTraceEnabled())
				log.trace("loadArtist(): fetching artist=" + artistName + ", mbid=" + artistMbid);    			
			
			this.mbId = artistMbid;
			
			StringBuffer artistUrl = new StringBuffer("http://musicbrainz.org/ws/1/artist/");
			artistUrl.append(artistMbid);
			artistUrl.append("?type=xml&inc=release-rels");

			MiningAPIFactory.getMusicbrainzAPI().getRateController().startQuery();
			Document doc = WebHelper.getDocumentResponseFromURL(artistUrl.toString());
			if (doc != null) {
	            XPathFactory factory = XPathFactory.newInstance();
	            XPath xPath = factory.newXPath();

	            this.artistName = (String)xPath.evaluate("//metadata/artist/name/text()", doc, XPathConstants.STRING);
	            
	            NodeList releaseNodes = (NodeList)xPath.evaluate("//metadata/artist/relation-list/relation/release", doc, XPathConstants.NODESET);
	            if (releaseNodes != null) {
	            	for (int r = 0; r < releaseNodes.getLength(); ++r) {
	            		Node releaseNode = releaseNodes.item(r);
	            		
	            		String type = (String)xPath.evaluate("./@type", releaseNode, XPathConstants.STRING);
	            		String releaseMbid = (String)xPath.evaluate("./@id", releaseNode, XPathConstants.STRING);
	            		
						Vector<String> releaseTypeMbids = allReleaseIds.get(type);
						if (releaseTypeMbids == null) 
							releaseTypeMbids = new Vector<String>();
						releaseTypeMbids.add(releaseMbid);
						allReleaseIds.put(type, releaseTypeMbids);	      
						
						if (isValidReleaseTypeForFetching(type)) {
		    				MusicbrainzReleaseProfile release = new MusicbrainzReleaseProfile(releaseMbid);
		    				if (release.isValid()) {
		    					releases.add(release);    					
		    					// merge release tags
		    		            for (Entry<String, Integer> entry : release.getTags().entrySet()) {
		    		            	String releaseTagName = entry.getKey();
		    		            	Integer releaseTagCount = entry.getValue();
		    		            	if (tags.containsKey(releaseTagName)) {
		    		            		Integer existingCount = tags.get(releaseTagName);
		    		            		existingCount += releaseTagCount;
		    		            		tags.put(releaseTagName, existingCount);
		    		            	} else {
		    		            		tags.put(releaseTagName, releaseTagCount);
		    		            	}
		    		            }
		    		            // merge ratings
		    		            if (release.getNumRaters() > 0) {
		    		            	avgRating += release.getAvgRating() * release.getNumRaters();
		    		            	numRaters += release.getNumRaters();
		    		            }
		    				}							
						}
	            	}
	            }
			}
			
			// some data isn't available in the api library
			StringBuffer artistURL = new StringBuffer();
			artistURL.append("http://musicbrainz.org/ws/1/artist/");
			artistURL.append(artistMbid);
			artistURL.append("?type=xml&inc=url-rels+tags+ratings+aliases+artist-rels+label-rels");
			
			MiningAPIFactory.getMusicbrainzAPI().getRateController().startQuery();
			if (log.isTraceEnabled())
				log.trace("MusicbrainzArtistProfile(): fetching additional info from url=" + artistURL.toString());
			doc = WebHelper.getDocumentResponseFromURL(artistURL.toString());
			if (doc != null) {
	            XPathFactory factory = XPathFactory.newInstance();
	            XPath xPath = factory.newXPath();
	            
	            // urls
	            NodeList urlNodes = (NodeList)xPath.evaluate("//metadata/artist/relation-list[@target-type='Url']/relation", doc, XPathConstants.NODESET);
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
	            NodeList tagNodes = (NodeList)xPath.evaluate("//metadata/artist/tag-list/tag", doc, XPathConstants.NODESET);
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
	            String avgRatingStr = xPath.evaluate("//metadata/artist/rating/text()", doc);
	            if ((avgRatingStr != null) && (avgRatingStr.length() > 0))  {
	            	int numArtistRaters = Integer.parseInt(xPath.evaluate("//metadata/artist/rating/@votes-count", doc));
	            	avgRating += Float.parseFloat(avgRatingStr) * numArtistRaters;
	            	numRaters += numArtistRaters;
	            }
	            
	            // aliases
	            NodeList aliasNodes = (NodeList)xPath.evaluate("//metadata/artist/alias-list/alias", doc, XPathConstants.NODESET);
	            for (int i = 0; i < aliasNodes.getLength(); i++) {
	                try {
	                    String alias = aliasNodes.item(i).getTextContent();
	                    aliases.add(alias);
	                } catch (Exception e) { }
	            }
	            
	            // artist relations
	            NodeList relatedArtistsNodes = (NodeList)xPath.evaluate("//metadata/artist/relation-list[@target-type='Artist']/relation/artist/name", doc, XPathConstants.NODESET);
	            for (int i = 0; i < relatedArtistsNodes.getLength(); i++) {
	                try {
	                    String relatedArtist = relatedArtistsNodes.item(i).getTextContent();
	                    relatedArtists.add(relatedArtist);
	                } catch (Exception e) { }
	            }
	            
                // labels
	            NodeList labelNodes = (NodeList)xPath.evaluate("//metadata/artist/relation-list[@target-type='Label']/relation/label/name", doc, XPathConstants.NODESET);
	            for (int i = 0; i < labelNodes.getLength(); i++) {
	                try {
	                    String label = labelNodes.item(i).getTextContent();
	                    labels.add(label);
	                } catch (Exception e) { }
	            }

                type = xPath.evaluate("//metadata/artist/@type", doc);
                lifeSpanBegin = xPath.evaluate("//metadata/artist/life-span/@begin", doc);
                lifeSpanEnd = xPath.evaluate("//metadata/artist/life-span/@end", doc);    	                	            
			}
			if (numRaters > 0)
				avgRating /= numRaters;
			
		    if (log.isTraceEnabled())
		    	log.trace("MusicbrainzArtistProfile(): urls=" + urls);
		    if (log.isTraceEnabled())
		    	log.trace("MusicbrainzArtistProfile(): tags=" + tags);
		    if (log.isTraceEnabled())
		    	log.trace("MusicbrainzArtistProfile(): avgRating="+ avgRating + ", numRaters=" + numRaters);
		    if (log.isTraceEnabled())
		    	log.trace("MusicbrainzArtistProfile(): aliases=" + aliases);
		    if (log.isTraceEnabled())
		    	log.trace("MusicbrainzArtistProfile(): relatedArtists=" + relatedArtists);
		    if (log.isTraceEnabled())
		    	log.trace("MusicbrainzArtistProfile(): labels=" + labels);
		    if (log.isTraceEnabled())
		    	log.trace("MusicbrainzArtistProfile(): allReleaseIds=" + allReleaseIds);
    		
    	} catch (Exception e) {
    		log.error("loadMbid(): error", e);
    	}
    }
    
    static public void main(String[] args) {
        try {
        	RapidEvolution3.loadLog4J();
            log.info("result=" + new MusicbrainzArtistProfile("fd8f81b4-3752-479a-baeb-e2eea7e90fb5", true));            
        } catch (Exception e) {
            log.error("main(): error", e);
        }        
    }   
                
}