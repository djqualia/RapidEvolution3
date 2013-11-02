package com.mixshare.rapid_evolution.data.mined.discogs;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.NoHttpResponseException;
import org.apache.commons.httpclient.ProtocolException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.search.release.ReleaseIdentifier;
import com.mixshare.rapid_evolution.data.mined.CommonMiningAPIWrapper;
import com.mixshare.rapid_evolution.data.mined.MinedProfile;
import com.mixshare.rapid_evolution.data.mined.discogs.artist.DiscogsArtist;
import com.mixshare.rapid_evolution.data.mined.discogs.artist.DiscogsArtistProfile;
import com.mixshare.rapid_evolution.data.mined.discogs.label.DiscogsLabel;
import com.mixshare.rapid_evolution.data.mined.discogs.label.DiscogsLabelProfile;
import com.mixshare.rapid_evolution.data.mined.discogs.release.DiscogsRelease;
import com.mixshare.rapid_evolution.data.mined.discogs.release.DiscogsReleaseOwnerlist;
import com.mixshare.rapid_evolution.data.mined.discogs.release.DiscogsReleaseProfile;
import com.mixshare.rapid_evolution.data.mined.discogs.release.DiscogsReleaseRatings;
import com.mixshare.rapid_evolution.data.mined.discogs.release.DiscogsReleaseRecommendations;
import com.mixshare.rapid_evolution.data.mined.discogs.release.DiscogsReleaseWishlist;
import com.mixshare.rapid_evolution.data.mined.discogs.song.DiscogsSong;
import com.mixshare.rapid_evolution.data.mined.util.MiningLimitReachedException;
import com.mixshare.rapid_evolution.data.profile.search.artist.ArtistProfile;
import com.mixshare.rapid_evolution.data.profile.search.label.LabelProfile;
import com.mixshare.rapid_evolution.data.profile.search.release.ReleaseProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.record.search.artist.ArtistRecord;
import com.mixshare.rapid_evolution.data.record.search.label.LabelRecord;
import com.mixshare.rapid_evolution.data.search.SearchEncoder;
import com.mixshare.rapid_evolution.data.util.filesystem.FileSystemAccess;
import com.mixshare.rapid_evolution.data.util.io.XMLSerializer;
import com.mixshare.rapid_evolution.util.StringUtil;
import com.mixshare.rapid_evolution.util.inet.httpclient.RE3GetMethod;
import com.mixshare.rapid_evolution.util.inet.httpclient.RE3HttpClient;

public class DiscogsAPIWrapper extends CommonMiningAPIWrapper {

    static private Logger log = Logger.getLogger(DiscogsAPIWrapper.class);
	
    static public boolean UNIT_TEST_MODE = false;
    
	static public String API_KEY = RE3Properties.getEncryptedProperty("discogs_api_key");		
	static public String HREF_TARGET_TEXT = " target=\"_blank\"";
	static public long RELEASE_FILE_CACHE_DURATION = RE3Properties.getLong("discogs_release_minimum_query_interval_days") * 1000 * 60 * 60 * 24;
	
	public byte getDataSource() { return DATA_SOURCE_DISCOGS; }
	
	public MinedProfile fetchArtistProfile(ArtistProfile artistProfile) {
		String discogsArtistName = getDiscogsArtistName(artistProfile.getDiscogsArtistName());
		if (discogsArtistName != null) {
			DiscogsArtist discogsArtist = getArtist(discogsArtistName);
			if (discogsArtist != null)
				return new DiscogsArtistProfile(discogsArtist);
		}
		return null;
	}
	public MinedProfile fetchLabelProfile(LabelProfile labelProfile) {
		// TODO: get getDiscogsLabelName to work to resolve ambiguous label names...
		DiscogsLabel discogsLabel = getLabel(labelProfile.getDiscogslLabelName());
		if (discogsLabel != null)
			return new DiscogsLabelProfile(discogsLabel);
		return null;
	}
	public MinedProfile fetchReleaseProfile(ReleaseProfile releaseProfile) { return null; }
	public MinedProfile fetchSongProfile(SongProfile songProfile) { return null; }
	
	public String getLocalArtistName(String discogsArtistName) {
		for (int artistId : Database.getArtistIndex().getIds()) {
			ArtistRecord artist = Database.getArtistIndex().getArtistRecord(artistId);
			if (artist != null) {
				if (artist.getDiscogsArtistName().equalsIgnoreCase(discogsArtistName)) {
					return artist.getArtistName();
				}
			}
		}
		return discogsArtistName;
	}
	
	public String getLocalLabelName(String discogsLabelName) {
		for (int labelId : Database.getLabelIndex().getIds()) {
			LabelRecord label = Database.getLabelIndex().getLabelRecord(labelId);
			if (label != null) {
				if (label.getDiscogsLabelName().equalsIgnoreCase(discogsLabelName)) {
					return label.getLabelName();
				}
			}
		}
		return discogsLabelName;		
	}
		
    public Vector<String> getArtists(DiscogsSong track, DiscogsReleaseProfile release) {
		Vector<String> artists = track.getArtists();
		if ((artists == null) || (artists.size() == 0))
			artists = release.getArtistNames();
		return artists;
    }
    
    public DiscogsArtist getArtist(String artistName) {
    	return getArtist(artistName, false); // until API returns profile, we will prefer to parse HTML
    }
    public DiscogsArtist getArtist(String artistName, boolean useAPI) {
    	DiscogsArtist result = null;
        try {
            StringBuffer getURL = new StringBuffer();
            getURL.append("http://www.discogs.com/artist/");
            String encodedArtistName = URLEncoder.encode(artistName, "UTF-8");
            getURL.append(encodedArtistName);
            getURL.append("?f=xml&api_key=");
            getURL.append(API_KEY);
            Document doc = null;
            if (useAPI) {
            	doc = getDocumentResponseFromDiscogs(getURL.toString());
            	if (log.isTraceEnabled())
            		debugDocumentResponseFromDiscogs(getURL.toString());
            }
            if (doc != null) {
                result = new DiscogsArtist(doc);            
            } else {
            	if (RE3Properties.getBoolean("enable_discogs_no_api_access")) {
	                getURL = new StringBuffer();
	                getURL.append("http://www.discogs.com/artist/");
	                getURL.append(encodedArtistName);
	                String httpData = getTextResponseFromDiscogs(getURL.toString());
	                if (httpData != null) {
	                    result = new DiscogsArtist(httpData);
	                }
            	}
            }            
            if (result != null) {
            	Vector<String> nameVariations = result.getNameVariations();
            	if (nameVariations != null) {
            		for (int v = 0; v < nameVariations.size(); ++v) {
            			String nameVariation = (String)nameVariations.get(v);
            			if (!nameVariation.equals(artistName)) {
            				DiscogsArtist variation = getArtistVariation(artistName, nameVariation);
            				if (variation != null) {
	            				Vector<String> releaseIDs = variation.getReleaseIDs();
	            				if (releaseIDs != null) {
		            				for (String releaseID : releaseIDs) {
		            					if (!result.getReleaseIDs().contains(releaseID))
		            						result.getReleaseIDs().add(releaseID);
		            				}
	            				}
            				}
            			}
            		}
            	}
            }
        } catch (Exception e) {
            log.error("getArtist(): error", e);
        }
        if (log.isDebugEnabled())
            log.debug("getArtist(): result=" + result);
        return result;
    }
    private DiscogsArtist getArtistVariation(String artistName, String variation) {
    	DiscogsArtist result = null;
        try {
            String encodedArtistName = URLEncoder.encode(artistName, "UTF-8");
            String encodedVariation = URLEncoder.encode(variation, "UTF-8");
            StringBuffer getURL = new StringBuffer();
            getURL.append("http://www.discogs.com/artist/");
            getURL.append(encodedArtistName);
            getURL.append("?anv=");
            getURL.append(encodedVariation);
            String httpData = getTextResponseFromDiscogs(getURL.toString());
            if (httpData != null) {
                result = new DiscogsArtist(httpData);
            }
        } catch (Exception e) {
            log.error("getArtistVariation(): error", e);
        }
        if (log.isDebugEnabled())
            log.debug("getArtistVariation(): result=" + result);
        return result;
    }	
	
    public DiscogsLabel getLabel(String labelName) {
    	return getLabel(labelName, true);
    }
    public DiscogsLabel getLabel(String labelName, boolean useAPI) {
    	DiscogsLabel result = null;
        try {
            StringBuffer getURL = new StringBuffer();
            getURL.append("http://www.discogs.com/label/");
            String encodedLabelName = URLEncoder.encode(labelName, "UTF-8");
            getURL.append(encodedLabelName);
            getURL.append("?f=xml&api_key=");
            getURL.append(API_KEY);
            Document doc = null;
            if (useAPI) {
            	doc = getDocumentResponseFromDiscogs(getURL.toString());
            	if (log.isTraceEnabled())
            		debugDocumentResponseFromDiscogs(getURL.toString());
            }
            boolean fetchNoAPI = false;
            if (doc != null) {
            	try {
            		result = new DiscogsLabel(doc);
            	} catch (DiscogsException de) {
            		fetchNoAPI = true;
            	}
            } else {
            	fetchNoAPI = true;
            }
            if (fetchNoAPI) {
            	if (RE3Properties.getBoolean("enable_discogs_no_api_access")) {
	                getURL = new StringBuffer();
	                getURL.append("http://www.discogs.com/label/");
	                getURL.append(encodedLabelName);
	                String httpData = getTextResponseFromDiscogs(getURL.toString());
	                if (httpData != null) {
	                    result = new DiscogsLabel(httpData);
	                }
            	}
            }
        } catch (Exception e) {
            log.error("getLabel(): error", e);
        }
        if (log.isDebugEnabled())
            log.debug("getLabel(): result=" + result);
        return result;
    }	

    public String getLabelSubPage(String labelName, int page) {
    	try {
    		StringBuffer getURL = new StringBuffer();
    		getURL.append("http://www.discogs.com/label/");
    		String encodedLabelName = URLEncoder.encode(labelName, "UTF-8");
    		getURL.append(encodedLabelName);
    		getURL.append("?page=");
    		getURL.append(String.valueOf(page));
    		String httpData = getTextResponseFromDiscogs(getURL.toString());
    		return httpData;
    	} catch (Exception e) {
    		log.error("getLabelSubPage(): error", e);
    	}
    	return null;
    }    
    
    public DiscogsRelease getRelease(String releaseId) {
    	if ((releaseId == null) || (releaseId.equals("")))
    		return null;
        return getRelease(Integer.parseInt(releaseId));
    }
    public DiscogsRelease getRelease(int releaseId) {
    	return getRelease(releaseId, true);
    }
    public DiscogsRelease getRelease(int releaseId, boolean useAPI) {
    	DiscogsRelease result = null;
        try {
        	String releaseDirectory = getMinedDataDirectory() + "releases/";
        	if (!UNIT_TEST_MODE
        			&& RE3Properties.getBoolean("discogs_cache_release_pages")) {
	            String filename = releaseDirectory + releaseId + ".xml";
	            if (System.currentTimeMillis() - FileSystemAccess.getFileSystem().getLastModified(filename) < RELEASE_FILE_CACHE_DURATION) {
	            	result = (DiscogsRelease)XMLSerializer.readBytes(FileSystemAccess.getFileSystem().readData(filename));
	            	if (result != null)
	            		return result;
	            }
        	}
            StringBuffer getURL = new StringBuffer();
            getURL.append("http://www.discogs.com/release/");
            getURL.append(String.valueOf(releaseId));
            getURL.append("?f=xml&api_key=");
            getURL.append(API_KEY);            
            Document doc = null;            
            if (useAPI) {
            	doc = getDocumentResponseFromDiscogs(getURL.toString());
            	if (log.isTraceEnabled())
            		debugDocumentResponseFromDiscogs(getURL.toString());
            }            
            if (doc != null) {
                result = new DiscogsRelease(doc, releaseId);
            } else {
            	if (RE3Properties.getBoolean("enable_discogs_no_api_access")) {            	
	                getURL = new StringBuffer();
	                getURL.append("http://www.discogs.com/release/");
	                getURL.append(String.valueOf(releaseId));
	                String httpData = getTextResponseFromDiscogs(getURL.toString());
	                if (httpData != null) {
	                    result = new DiscogsRelease(httpData, releaseId);
	                }
            	}
            }
            if (result != null) {
            	//if (RE3Properties.getBoolean("enable_discogs_no_api_access")) {            	
            		// recommendations
            		DiscogsReleaseRecommendations recommendations = getReleaseRecommendations(releaseId);
            		result.setRecommendations(recommendations);
	            	// get stats page HTML
	                getURL = new StringBuffer();
	                getURL.append("http://www.discogs.com/release/stats/");
	                getURL.append(String.valueOf(releaseId));            	
	                String httpData = getTextResponseFromDiscogs(getURL.toString());
	                if (httpData != null) {
	                	// ratings
	                	DiscogsReleaseRatings ratings = new DiscogsReleaseRatings(httpData);
	                	result.setRatings(ratings);
	                	// owners
	                    DiscogsReleaseOwnerlist ownerlist = new DiscogsReleaseOwnerlist(httpData);
	                    result.setOwnerlist(ownerlist.getUsers());
	                    // wishlist
	                    DiscogsReleaseWishlist wantlist = new DiscogsReleaseWishlist(httpData);
	                    result.setWishlist(wantlist.getUsers());
	                }
            	//}
            }
        	if (!UNIT_TEST_MODE) {
        		if ((result != null) && (releaseDirectory != null)) {
        			String filename = releaseDirectory + releaseId + ".xml";
        			FileSystemAccess.getFileSystem().saveData(filename, XMLSerializer.getBytes(result), true);        			
        		}
        	}
        } catch (Exception e) {
            log.error("getRelease(): error", e);
        }
        if (log.isTraceEnabled())
            log.trace("getRelease(): result=" + result);
        return result;        
    }	
    
    public DiscogsReleaseRecommendations getReleaseRecommendations(int releaseId) {
    	DiscogsReleaseRecommendations result = null;
        try {
        	String recommendationsDirectory = getMinedDataDirectory() + "recommendations/";
        	if (!UNIT_TEST_MODE
        			&& RE3Properties.getBoolean("discogs_cache_recommended_pages")) {	        	
	            String filename = recommendationsDirectory + releaseId + ".xml";
	            if (System.currentTimeMillis() - FileSystemAccess.getFileSystem().getLastModified(filename) < RELEASE_FILE_CACHE_DURATION) {                
	            	result = (DiscogsReleaseRecommendations)XMLSerializer.readBytes(FileSystemAccess.getFileSystem().readData(filename));
	                if (result != null)
	                    return result;
	            }
        	}
            StringBuffer getURL = new StringBuffer();
            getURL.append("http://www.discogs.com/release/recommendations/");
            getURL.append(String.valueOf(releaseId));
            String htmlResponse = getTextResponseFromDiscogs(getURL.toString());
            if (htmlResponse != null)
                result = new DiscogsReleaseRecommendations(htmlResponse);
            if (!UNIT_TEST_MODE) {
            	if ((result != null) && (recommendationsDirectory != null)) {
            		String filename = recommendationsDirectory + releaseId + ".xml";
            		FileSystemAccess.getFileSystem().saveData(filename, XMLSerializer.getBytes(result), true);
            	}
            }
        } catch (Exception e) {
            log.error("getReleaseRecommendations(): error", e);
        }
        if (log.isTraceEnabled())
            log.trace("getReleaseRecommendations(): result=" + result);
        return result;        
    }    
    
    public Integer searchForReleaseId(String artist, String releaseTitle) {
    	Integer bestMatch = null;
    	try {
    		if (log.isTraceEnabled())
    			log.trace("searchForReleaseId(): artist=" + artist + ", releaseTitle=" + releaseTitle);

    		StringBuffer url = new StringBuffer("http://www.discogs.com/search?type=release&q=");
    		String searchString = artist + " " + releaseTitle;
    		searchString = StringUtil.replace(searchString, "'s ", " ");
    		url.append(URLEncoder.encode(searchString, "UTF-8"));
    		url.append("&f=xml&api_key=");
    		url.append(API_KEY);    		
    		
            Document doc = getDocumentResponseFromDiscogs(url.toString());
            if (doc != null) {
                XPathFactory factory = XPathFactory.newInstance();
                XPath xPath = factory.newXPath();  
            	
                String compareTitle = artist + " - " + releaseTitle;
                if (artist.length() == 0)
                	compareTitle = releaseTitle;
                compareTitle = SearchEncoder.encodeString(compareTitle);
                NodeList results = (NodeList)xPath.evaluate("//resp/exactresults/result", doc, XPathConstants.NODESET);                
                if ((results == null) || (results.getLength() == 0))
                	results = (NodeList)xPath.evaluate("//resp/searchresults/result", doc, XPathConstants.NODESET);
                if ((results != null) && (results.getLength() > 0)) {
                	int i = 0;
                	while ((bestMatch == null) && (i < results.getLength())) {
                    	Node firstNode = (Node)results.item(i);
                    	String title = (String)xPath.evaluate("./title/text()", firstNode, XPathConstants.STRING);
                    	if (SearchEncoder.encodeString(title).equalsIgnoreCase(compareTitle)) {
	                    	String uri = (String)xPath.evaluate("./uri/text()", firstNode, XPathConstants.STRING);
	                    	int index = uri.indexOf("/release/");
	                    	if (index >= 0)
	                    		bestMatch = Integer.parseInt(uri.substring(index + 9).trim());
                    	}
                		++i;
                	}
                }
            }
             
            /*
            String htmlResponse = method.getResponseBodyAsString();
            Map<Integer, Integer> processedIds = new HashMap<Integer, Integer>();
            boolean done = false;
            int lastIndex = 0;
            while (!done) {
	            String releasePrefix = "/release/";
	            String releaseSuffix = "\">";
	            String altReleaseSuffix = "</span>";
	            String altReleaseSuffix2 = "</div>";
	            int startIndex = htmlResponse.indexOf(releasePrefix, lastIndex);
	            if (startIndex > 0) {
	            	lastIndex = startIndex + releasePrefix.length();
	            	int endIndex = htmlResponse.indexOf(releaseSuffix, startIndex + releasePrefix.length());	            	
	            	int endIndex2 = htmlResponse.indexOf(altReleaseSuffix, startIndex + releasePrefix.length());
	            	int endIndex3 = htmlResponse.indexOf(altReleaseSuffix2, startIndex + releasePrefix.length());
	            	if (endIndex > 0) {
	            		if (endIndex2 > 0)
	            			endIndex = Math.min(endIndex, endIndex2);
	            		if (endIndex3 > 0)
	            			endIndex = Math.min(endIndex, endIndex3);
	            		try {
		            		Integer releaseId = new Integer(htmlResponse.substring(startIndex + releasePrefix.length(), endIndex));
		            		if (!processedIds.containsKey(releaseId)) {
			            		processedIds.put(releaseId, null);
	        					if (bestMatch == null)
	        						bestMatch = releaseId;
	                			// make sure it has an album cover before returning it immediately
	        					int trueStartIndex = htmlResponse.lastIndexOf("<div class=\"number\">", startIndex);
	                			String prefix = "<div class=\"thumb\">";
	                			int imageIndex = htmlResponse.indexOf(prefix, trueStartIndex);
	                			int nextReleaseIndex = htmlResponse.indexOf(releasePrefix, startIndex + 1);
	                			if (nextReleaseIndex < 0)
	                				nextReleaseIndex = htmlResponse.length();
	                			if ((imageIndex > 0) && (imageIndex < nextReleaseIndex)) {
	            					// has an image
		                    		if (log.isDebugEnabled())
		                    			log.debug("searchForReleaseId(): release ID found=" + releaseId);
		                			return releaseId;
	                			}
		            		}
	            		} catch (NumberFormatException nfe) { }
	            	}
	            } else {
	            	done = true;
	            }
            }
            */
    	} catch (Exception e) {
    		log.error("searchForReleaseId(): error", e);
    	}
		if (log.isTraceEnabled())
			log.trace("searchForReleaseId(): best release ID found=" + bestMatch);
    	return bestMatch;
    }    

    public String getDiscogsArtistName(String artistName) {
    	String bestMatchingArtist = artistName;
    	try {
    		if (log.isTraceEnabled())
    			log.trace("getDiscogsArtistName(): artistName=" + artistName);
            StringBuffer getURL = new StringBuffer();
            getURL.append("http://www.discogs.com/search?type=artists&q=");
            String encodedArtistName = URLEncoder.encode(artistName, "UTF-8");
            getURL.append(encodedArtistName);
            getURL.append("&f=xml&api_key=");
            getURL.append(API_KEY);
            Document doc = getDocumentResponseFromDiscogs(getURL.toString());
            if (doc != null) {
	            XPathFactory factory = XPathFactory.newInstance();
	            XPath xPath = factory.newXPath();	          
	            Map<String, String> matches = new LinkedHashMap<String, String>();
	            NodeList uriNodes = (NodeList)xPath.evaluate("//resp/exactresults/result/uri", doc, XPathConstants.NODESET);
	            for (int i = 0; i < uriNodes.getLength(); i++)  {
	            	String uri = uriNodes.item(i).getTextContent();
	            	String prefix = "http://www.discogs.com/artist/";
	            	String matchName = uri.substring(prefix.length());
	            	String suffix = "?anv=";
	            	int index = matchName.lastIndexOf(suffix);
	            	if (index >= 0)
	            		matchName = matchName.substring(0, index);
	                matchName = URLDecoder.decode(matchName, "UTF-8");	            	
	                matches.put(matchName, uri);
	            }
	            if (log.isTraceEnabled())
	            	log.trace("getDiscogsArtistName(): matches=" + matches);
	            if (matches.size() == 1)
	            	bestMatchingArtist = matches.keySet().iterator().next();
	            else if (matches.size() > 1) {
	            	int maxCount = 0;
	            	for (String matchName : matches.keySet()) {
	            		int releaseCount = 0;
	                    getURL = new StringBuffer();
	                    getURL.append("http://www.discogs.com/artist/");
	                    String encodedMatchName = URLEncoder.encode(matchName, "UTF-8");
	                    getURL.append(encodedMatchName);
	                    getURL.append("?f=xml&api_key=");
	                    getURL.append(API_KEY);
	                    doc = getDocumentResponseFromDiscogs(getURL.toString());
	                    if (doc != null) {
	        	            factory = XPathFactory.newInstance();
	        	            xPath = factory.newXPath();	          
	        	            NodeList releaseTitleNodes = (NodeList)xPath.evaluate("//resp/artist/releases/release/title", doc, XPathConstants.NODESET);
	        	            for (int i = 0; i < releaseTitleNodes.getLength(); i++)  {
	        	            	String releaseTitle = releaseTitleNodes.item(i).getTextContent();
	        	            	ReleaseIdentifier releaseId = new ReleaseIdentifier(artistName, releaseTitle);
	        	            	if (Database.getReleaseIndex().doesExist(releaseId))
	        	            		++releaseCount;
	        	            }	                    	
	                    }
	                    if (releaseCount > maxCount) {
	                    	maxCount = releaseCount;
	                    	bestMatchingArtist = matchName;
	                    }
	            	}
	            }
            }
            
            
    	} catch (Exception e) {
    		log.error("getDiscogsArtistName(): error", e);
    	}
		if (log.isTraceEnabled())
			log.trace("getDiscogsArtistName(): best matching artist name=" + bestMatchingArtist);
    	return bestMatchingArtist;
    }    

    /**
     * This method didn't appear to work when tested, but I'm leaving the code to investigate further later...
     */
    public String getDiscogsLabelName(String labelName) {
    	String bestMatchingLabel = null;
    	try {
    		if (log.isTraceEnabled())
    			log.trace("getDiscogsLabelName(): labelName=" + labelName);
            StringBuffer getURL = new StringBuffer();
            getURL.append("http://www.discogs.com/search?type=labels&q=");
            String encodedLabelName = URLEncoder.encode(labelName, "UTF-8");
            getURL.append(encodedLabelName);
            getURL.append("&f=xml&api_key=");
            getURL.append(API_KEY);
            Document doc = getDocumentResponseFromDiscogs(getURL.toString());
            if (doc != null) {
	            XPathFactory factory = XPathFactory.newInstance();
	            XPath xPath = factory.newXPath();	          
	            Map<String, String> matches = new LinkedHashMap<String, String>();
	            NodeList uriNodes = (NodeList)xPath.evaluate("//resp/exactresults/result/uri", doc, XPathConstants.NODESET);
	            for (int i = 0; i < uriNodes.getLength(); i++)  {
	            	String uri = uriNodes.item(i).getTextContent();
	            	String prefix = "http://www.discogs.com/label/";
	            	String matchName = uri.substring(prefix.length());
	            	String suffix = "?anv=";
	            	int index = matchName.lastIndexOf(suffix);
	            	if (index >= 0)
	            		matchName = matchName.substring(0, index);
	                matchName = URLDecoder.decode(matchName, "UTF-8");	            	
	                matches.put(matchName, uri);
	            }
	            if (log.isTraceEnabled())
	            	log.trace("getDiscogsLabelName(): matches=" + matches);
	            if (matches.size() == 1)
	            	bestMatchingLabel = matches.keySet().iterator().next();
	            else if (matches.size() > 1) {
	            	int maxCount = 0;
	            	for (String matchName : matches.keySet()) {
	            		int releaseCount = 0;
	                    getURL = new StringBuffer();
	                    getURL.append("http://www.discogs.com/label/");
	                    String encodedMatchName = URLEncoder.encode(matchName, "UTF-8");
	                    getURL.append(encodedMatchName);
	                    getURL.append("?f=xml&api_key=");
	                    getURL.append(API_KEY);
	                    doc = getDocumentResponseFromDiscogs(getURL.toString());
	                    if (doc != null) {
	        	            factory = XPathFactory.newInstance();
	        	            xPath = factory.newXPath();	          
	        	            NodeList releaseTitleNodes = (NodeList)xPath.evaluate("//resp/label/releases/release/title", doc, XPathConstants.NODESET);
	        	            for (int i = 0; i < releaseTitleNodes.getLength(); i++)  {
	        	            	String releaseTitle = releaseTitleNodes.item(i).getTextContent();
	        	            	ReleaseIdentifier releaseId = new ReleaseIdentifier(labelName, releaseTitle);
	        	            	if (Database.getReleaseIndex().doesExist(releaseId))
	        	            		++releaseCount;
	        	            }	                    	
	                    }
	                    if (releaseCount > maxCount) {
	                    	maxCount = releaseCount;
	                    	bestMatchingLabel = matchName;
	                    }
	            	}
	            }
            }
            
            
    	} catch (Exception e) {
    		log.error("getDiscogsLabelName(): error", e);
    	}
		if (log.isTraceEnabled())
			log.trace("getDiscogsLabelName(): best matching label name=" + bestMatchingLabel);
    	return bestMatchingLabel;
    }    
    
    public Vector<Integer> searchForReleaseIds(String artist, String releaseTitle) {
    	Vector<Integer> result = new Vector<Integer>();
    	GetMethod method = null;
    	try {
    		if (log.isTraceEnabled())
    			log.trace("searchForReleaseIds(): artist=" + artist + ", releaseTitle=" + releaseTitle);
    		HttpClient client = new RE3HttpClient();
            method = new RE3GetMethod("http://www.discogs.com/advanced_search");
            NameValuePair[] namevalue = new NameValuePair[2];
            namevalue[0] = new NameValuePair();
            namevalue[0].setName("artist");
            namevalue[0].setValue(artist);
            namevalue[1] = new NameValuePair();
            namevalue[1].setName("release_title");
            namevalue[1].setValue(releaseTitle);
            method.setQueryString(namevalue);
            int statusCode = client.executeMethod(method);
            if (statusCode == -1)
                return null;
            String htmlResponse = method.getResponseBodyAsString();
            Map<Integer, Integer> processedIds = new HashMap<Integer, Integer>();
            boolean done = false;
            int lastIndex = 0;
            while (!done) {
	            String releasePrefix = "/release/";
	            String releaseSuffix = "\">";
	            String altReleaseSuffix = "</span>";
	            String altReleaseSuffix2 = "</div>";
	            int startIndex = htmlResponse.indexOf(releasePrefix, lastIndex);
	            if (startIndex > 0) {
	            	lastIndex = startIndex + releasePrefix.length();
	            	int endIndex = htmlResponse.indexOf(releaseSuffix, startIndex + releasePrefix.length());	            	
	            	int endIndex2 = htmlResponse.indexOf(altReleaseSuffix, startIndex + releasePrefix.length());
	            	int endIndex3 = htmlResponse.indexOf(altReleaseSuffix2, startIndex + releasePrefix.length());
	            	if (endIndex > 0) {
	            		if (endIndex2 > 0)
	            			endIndex = Math.min(endIndex, endIndex2);
	            		if (endIndex3 > 0)
	            			endIndex = Math.min(endIndex, endIndex3);
	            		try {
		            		Integer releaseId = new Integer(htmlResponse.substring(startIndex + releasePrefix.length(), endIndex));
		            		if (!processedIds.containsKey(releaseId)) {
			            		processedIds.put(releaseId, null);
			            		result.add(releaseId);
		            		}
	            		} catch (NumberFormatException nfe) { }
	            	}
	            } else {
	            	done = true;
	            }
            }
    	} catch (Exception e) {
    		log.error("searchForReleaseIds(): error", e);
    	} finally {
    		if (method != null)
    			method.releaseConnection();
    	}
		if (log.isTraceEnabled())
			log.trace("searchForReleaseIds(): releases IDs found=" + result);
    	return result;
    }        
    
    static public Vector<String> collectReleaseIds(String url) {
    	Vector<String> result = new Vector<String>();
    	return collectReleaseIds(url, result);
    }
    static public Vector<String> collectReleaseIds(String url, Vector<String> result) {    	
    	try {
    		int page = 1;
    		int perPage = 25;
    		boolean done = false;
    		while (!done) {
    			String pageUrl = url + "&page=" + page + "&per=" + perPage;
    			String htmlData = getTextResponseFromDiscogs(pageUrl);    			
    	        Vector<String> releases = StringUtil.getAllInBetweens(htmlData, "/release/", "\">");
    	        for (int i = 0; i < releases.size(); ++i) {
    	            boolean success = false;
    	            try {
    	                Integer.parseInt(releases.get(i).toString());
    	                success = true;
    	            } catch (Exception e) { }
    	            if (!success) {
    	                releases.remove(i);
    	                --i;
    	            }
    	        }    
    	        boolean added = false;
    	        for (int i = 0; i < releases.size(); ++i) {
    	            if (!result.contains(releases.get(i))) {
    	            	added = true;
    	            	result.add(releases.get(i));
    	            }
    	        }         	
    	        if (!added)
    	        	done = true;    			
    			++page;
    		}
    	} catch (Exception e) {
    		log.error("collectReleaseIds(): error", e);
    	}
    	return result;
    }
    
    private Document getDocumentResponseFromDiscogs(String url) {    	
        Document result = null;
        GetMethod method = null;
        try {            
            if (log.isTraceEnabled())
                log.trace("getDocumentResponseFromDiscogs(): url=" + url);
            getRateController().startQuery();
            HttpClient client = new RE3HttpClient();
            method = new RE3GetMethod(url);
            method.addRequestHeader("Accept-Encoding", "gzip");
            method.addRequestHeader("User-Agent", "Rapid Evolution 3");
            int statusCode = client.executeMethod(method);
            if (statusCode == 200) {
                InputStream inputStream = method.getResponseBodyAsStream();
                try {
                    GZIPInputStream inflater = new GZIPInputStream(inputStream);
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    factory.setNamespaceAware(true);
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    result = builder.parse(inflater);                        
                    inflater.close();                        
                } catch (java.io.IOException ioe) {
                    // not in gzip format?
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    factory.setNamespaceAware(true);
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    client.executeMethod(method);
                    inputStream = method.getResponseBodyAsStream();
                    result = builder.parse(inputStream);
                }
                inputStream.close();

                XPathFactory xpathFactory = XPathFactory.newInstance();
                XPath xPath = xpathFactory.newXPath();  
                Object releasesObj = xPath.evaluate("//resp", result, XPathConstants.NODESET);                
                NodeList releaseNodes = (NodeList) releasesObj;
                int numRequests = Integer.parseInt(releaseNodes.item(0).getAttributes().getNamedItem("requests").getTextContent());
                getRateController().setNumRequestsThisInterval(numRequests);                
            } else if ((statusCode == 404) || (statusCode == 400)) {
            	String response = method.getResponseBodyAsString();
            	if (response != null) {
            		if (response.indexOf("Maximum API requests reached") >= 0) {
            			getRateController().setNumRequestsThisInterval(RE3Properties.getInt("discogs_max_queries_per_day") + 1);
            			throw new MiningLimitReachedException();
            		}
            	}
                return null;
            } else {
            	if (RE3Properties.getBoolean("automatically_retry_failed_requests")) {
            		method.releaseConnection();
            		method = null;
            		if (log.isDebugEnabled())
            			log.debug("getDocumentResponseFromDiscogs(): response != 200, waiting, url=" + url + ", statusCode=" + statusCode);
            		Thread.sleep(10000);
            		return getDocumentResponseFromDiscogs(url);
            	} else {
            		return null;
            	}
            }
        } catch (org.xml.sax.SAXParseException spe) {
        	if (log.isDebugEnabled())
        		log.debug("getDocumentResponseFromDiscogs(): sax parse exception=" + spe);
        } catch (ProtocolException pe) {
        	if (log.isDebugEnabled())
        		log.debug("getDocumentResponseFromDiscogs(): protocol exception");        	
        	getRateController().setNumRequestsThisInterval(RE3Properties.getInt("discogs_max_queries_per_day"));
        } catch (MiningLimitReachedException e) {
        	if (log.isDebugEnabled())
        		log.debug("getDocumentResponseFromDiscogs(): mining limit reached");        	
        } catch (org.apache.commons.httpclient.NoHttpResponseException e) {
        	if (log.isDebugEnabled())
        		log.debug("getDocumentResponseFromDiscogs(): no response exception=" + e);
        } catch (Exception e) {
            log.error("getDocumentResponseFromDiscogs(): error", e);
        } finally {
        	if (method != null)
        		method.releaseConnection();
        }
        return result;
    }
        
    static private Document debugDocumentResponseFromDiscogs(String url) {
        Document result = null;
        GetMethod method = null;
        try {
            HttpClient client = new RE3HttpClient();
            method = new RE3GetMethod(url);
            method.addRequestHeader("Accept-Encoding", "gzip");
            method.addRequestHeader("User-Agent", "Rapid Evolution 3");
            int statusCode = client.executeMethod(method);
            if (statusCode == 200) {
                InputStream inputStream = null;
                try {
                    inputStream = new GZIPInputStream(method.getResponseBodyAsStream());
                } catch (java.io.IOException ioe) {
                    client.executeMethod(method);
                    inputStream = method.getResponseBodyAsStream();
                }                    
                ByteArrayOutputStream outputstream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int bytesRead = inputStream.read(buffer);
                while (bytesRead != -1) {
                    outputstream.write(buffer, 0, bytesRead);
                    bytesRead = inputStream.read(buffer);
                }
                String xmlResponseText = new String(outputstream.toByteArray());
                log.trace("debugDocumentResponseFromDiscogs(): response=" + xmlResponseText);
            } else {
                method.releaseConnection();
                log.error("debugDocumentResponseFromDiscogs(): response != 200, waiting");
                Thread.sleep(10000);
                return debugDocumentResponseFromDiscogs(url);                
            }
            method.releaseConnection();
        } catch (Exception e) {
            log.error("debugResponseFromDiscogs(): error", e);
        } finally {
        	if (method != null)
        		method.releaseConnection();
        }
        return result;
    }    
    
    static public String getTextResponseFromDiscogs(String url) {
        String result = null;
        GetMethod method = null;
        try {
            HttpClient client = new RE3HttpClient();
            method = new RE3GetMethod(url);            
            int statusCode = client.executeMethod(method);
            if (statusCode == 200) {
                result = method.getResponseBodyAsString();
            } else if ((statusCode == 404) || (statusCode == 400)) {
                return null;
            } else {
            	if (RE3Properties.getBoolean("automatically_retry_failed_requests")) {
            		method.releaseConnection();
            		method = null;
            		log.error("getTextResponseFromDiscogs(): response != 200, waiting, url=" + url + ", statusCode=" + statusCode);
            		Thread.sleep(10000);
            		return getTextResponseFromDiscogs(url);
            	} else {
            		return null;
            	}
            }
        } catch (NoHttpResponseException nre) {
        	if (log.isDebugEnabled())
        		log.debug("getTextResponseFromDiscogs(): no response exception=" + nre);        	
        } catch (java.net.ConnectException ce) {
        	if (log.isDebugEnabled())
        		log.debug("getTextResponseFromDiscogs(): connection exception=" + ce);
        } catch (Exception e) {
            log.error("getTextResponseFromDiscogs(): error", e);
        } finally {
        	if (method != null)
        		method.releaseConnection();
        }
        return result;
    }    
    
    static public void main(String[] args) {
    	try {
    		RapidEvolution3.loadLog4J();
    	} catch (Exception e) {
    		log.error("main(): error", e);
    	}
    }
	
}