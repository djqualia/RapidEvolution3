package com.mixshare.rapid_evolution.data.mined.lastfm;

import java.io.File;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import net.roarsoftware.lastfm.Caller;
import net.roarsoftware.lastfm.cache.FileSystemCache;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.mined.CommonMiningAPIWrapper;
import com.mixshare.rapid_evolution.data.mined.MinedProfile;
import com.mixshare.rapid_evolution.data.mined.MiningAPIFactory;
import com.mixshare.rapid_evolution.data.mined.lastfm.artist.LastfmArtistProfile;
import com.mixshare.rapid_evolution.data.mined.lastfm.release.LastfmReleaseProfile;
import com.mixshare.rapid_evolution.data.mined.lastfm.song.LastfmSongProfile;
import com.mixshare.rapid_evolution.data.mined.util.WebHelper;
import com.mixshare.rapid_evolution.data.profile.search.artist.ArtistProfile;
import com.mixshare.rapid_evolution.data.profile.search.label.LabelProfile;
import com.mixshare.rapid_evolution.data.profile.search.release.ReleaseProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.util.DegreeValue;
import com.mixshare.rapid_evolution.util.OSHelper;

/**
 * http://www.last.fm/api
 */
public class LastfmAPIWrapper extends CommonMiningAPIWrapper {

    static private Logger log = Logger.getLogger(LastfmAPIWrapper.class);
	
	static public String API_KEY = RE3Properties.getEncryptedProperty("lastfm_api_key");	
	
	static private FileSystemCache fsc;
	static {
		try {
			File file = new File(RE3Properties.getProperty("temp_working_directory").length() > 0
					? RE3Properties.getProperty("temp_working_directory")
					: OSHelper.getWorkingDirectory() + "/" + RE3Properties.getProperty("lastfm_cache_directory"));
			if (!file.exists())
				file.mkdirs();
			fsc = new FileSystemCache(file);
			Caller.getInstance().setCache(fsc);
		} catch (Exception e) {
			log.error("LastfmAPIWrapper(): error", e);
		}
	}

	static public void clearCache() {
		if (fsc != null) {
			fsc.clearScrobbleCache();
			fsc.clear();
		}
	}

	public byte getDataSource() { return DATA_SOURCE_LASTFM; }
	
	public MinedProfile fetchArtistProfile(ArtistProfile artistProfile) { return new LastfmArtistProfile(artistProfile.getLastfmArtistName()); }
	public MinedProfile fetchLabelProfile(LabelProfile lastProfile) { return null; }
	public MinedProfile fetchReleaseProfile(ReleaseProfile releaseProfile) { return new LastfmReleaseProfile(releaseProfile.getArtistsDescription(), releaseProfile.getReleaseTitle()); }
	public MinedProfile fetchSongProfile(SongProfile songProfile) { return new LastfmSongProfile(songProfile.getArtistsDescription(), songProfile.getSongDescription()); }	
		
	
	/**
	 * @return Map of song titles to their reach values
	 */
	/*
    static public Map<String, Float> getArtistTopSongs(String artistName) {
    	Map<String, Float> result = new HashMap<String, Float>();
        try {
            StringBuffer getURL = new StringBuffer();
            getURL.append("http://ws.audioscrobbler.com/1.0/artist/");
            String encodedArtistName = URLEncoder.encode(artistName, "UTF-8");
            getURL.append(encodedArtistName);
            getURL.append("/toptracks.xml");
            LastfmAPIWrapper.rateController.startQuery();
            Document doc = WebHelper.getDocumentResponseFromURL(getURL.toString());
            if (doc != null) {            	
                XPathFactory factory = XPathFactory.newInstance();
                XPath xPath = factory.newXPath();
                NodeList trackNodes = (NodeList)xPath.evaluate("//mostknowntracks/track", doc, XPathConstants.NODESET);
                for (int i = 0; i < trackNodes.getLength(); i++) {
                    String trackTitle = null;
                    float reach = 0.0f;
                    NodeList childNodes = trackNodes.item(i).getChildNodes();
                    for (int c = 0; c < childNodes.getLength(); ++c) {
                        Node node = childNodes.item(c);
                        if (node.getNodeName().equals("name"))
                            trackTitle = node.getTextContent();
                        else if (node.getNodeName().equals("reach"))
                            reach = Float.parseFloat(node.getTextContent());
                    }
                    if (trackTitle != null)
                    	result.put(trackTitle.toLowerCase(), reach);
                }                        	
            }
        } catch (Exception e) {
            log.error("getArtistTopSongs(): error", e);
        }
        return result;        
    }   	
    */
	
    static public Vector<DegreeValue> getArtistTopTags(String artistName) {
    	Vector<DegreeValue> result = new Vector<DegreeValue>();
        try {
            StringBuffer getURL = new StringBuffer();
            getURL.append("http://ws.audioscrobbler.com/1.0/artist/");
            String encodedArtistName = URLEncoder.encode(artistName, "UTF-8");
            getURL.append(encodedArtistName);
            getURL.append("/toptags.xml");
            MiningAPIFactory.getLastfmAPI().getRateController().startQuery();
            Document doc = WebHelper.getDocumentResponseFromURL(getURL.toString());
            if (doc != null) {
                XPathFactory factory = XPathFactory.newInstance();
                XPath xPath = factory.newXPath();
                NodeList tagNodes = (NodeList)xPath.evaluate("//toptags/tag", doc, XPathConstants.NODESET);
                for (int i = 0; i < tagNodes.getLength(); i++) {
                    String tagName = null;
                    float degree = 0.0f;
                    NodeList childNodes = tagNodes.item(i).getChildNodes();
                    for (int c = 0; c < childNodes.getLength(); ++c) {
                        Node node = childNodes.item(c);
                        if (node.getNodeName().equals("name"))
                            tagName = node.getTextContent();
                        else if (node.getNodeName().equals("count"))
                    		degree = Float.parseFloat(node.getTextContent()) / 100.0f;                        
                    }
                    if (tagName != null)
                    	result.add(new DegreeValue(tagName, degree, DATA_SOURCE_LASTFM));                    
                }                
            }
        } catch (Exception e) {
            log.error("getArtistTopTags(): error", e);
        }
        return result;                
    }      
    
    static public Map<String, Float> getArtistTopReleases(String artistName) {
    	Map<String, Float> result = new HashMap<String, Float>();
        try {
            StringBuffer getURL = new StringBuffer();
            getURL.append("http://ws.audioscrobbler.com/1.0/artist/");
            String encodedArtistName = URLEncoder.encode(artistName, "UTF-8");
            getURL.append(encodedArtistName);
            getURL.append("/topalbums.xml");
            MiningAPIFactory.getLastfmAPI().getRateController().startQuery();
            Document doc = WebHelper.getDocumentResponseFromURL(getURL.toString());
            if (doc != null) {
                XPathFactory factory = XPathFactory.newInstance();
                XPath xPath = factory.newXPath();
                NodeList albumNodes = (NodeList)xPath.evaluate("//topalbums/album", doc, XPathConstants.NODESET);
                for (int i = 0; i < albumNodes.getLength(); i++) {
                    String albumTitle = null;
                    float reach = 0.0f;
                    NodeList childNodes = albumNodes.item(i).getChildNodes();
                    for (int c = 0; c < childNodes.getLength(); ++c) {
                        Node node = childNodes.item(c);
                        if (node.getNodeName().equals("name"))
                            albumTitle = node.getTextContent();
                        else if (node.getNodeName().equals("reach"))
                            reach = Float.parseFloat(node.getTextContent());
                    }
                    if (albumTitle != null)
                    	result.put(albumTitle.toLowerCase(), reach);                      
                }                
            }
        } catch (Exception e) {
            log.error("getArtistTopReleases(): error", e);
        }
        return result;                
    }       
    
    static public Map<String, Float> getReleaseSongs(String artistName, String albumName) {
    	Map<String, Float> result = new HashMap<String, Float>();
        try {
            StringBuffer getURL = new StringBuffer();
            getURL.append("http://ws.audioscrobbler.com/1.0/album/");
            String encodedArtistName = URLEncoder.encode(artistName, "UTF-8");
            getURL.append(encodedArtistName);
            getURL.append("/");
            String encodedAlbumName = URLEncoder.encode(albumName, "UTF-8");
            getURL.append(encodedAlbumName);
            getURL.append("/info.xml");
            MiningAPIFactory.getLastfmAPI().getRateController().startQuery();
            Document doc = WebHelper.getDocumentResponseFromURL(getURL.toString());
            if (doc != null) {
                XPathFactory factory = XPathFactory.newInstance();
                XPath xPath = factory.newXPath();
                NodeList trackNodes = (NodeList)xPath.evaluate("//album/tracks/track", doc, XPathConstants.NODESET);
                for (int i = 0; i < trackNodes.getLength(); i++) {
                    String title = trackNodes.item(i).getAttributes().getNamedItem("title").getTextContent();
                    float reach = 0.0f;
                    NodeList childNodes = trackNodes.item(i).getChildNodes();
                    for (int c = 0; c < childNodes.getLength(); ++c) {
                        Node node = childNodes.item(c);
                        if (node.getNodeName().equals("reach"))
                            reach = Float.parseFloat(node.getTextContent());
                    }
                    result.put(title.toLowerCase(), reach);
                }          
            }
        } catch (Exception e) {
            log.error("getReleaseSongs(): error", e);
        }
        return result;                
    }      
    
    static public Map<String, Float> getSimilarSongs(String artistName, String trackTitle) {
    	Map<String, Float> result = new LinkedHashMap<String, Float>();
        try {
            StringBuffer getURL = new StringBuffer();
            getURL.append("http://ws.audioscrobbler.com/1.0/track/");
            String encodedArtistName = URLEncoder.encode(artistName, "UTF-8");
            getURL.append(encodedArtistName);
            getURL.append("/");
            String encodedTrackName = URLEncoder.encode(trackTitle, "UTF-8");
            getURL.append(encodedTrackName);            
            getURL.append("/similar.xml");
            MiningAPIFactory.getLastfmAPI().getRateController().startQuery();
            Document doc = WebHelper.getDocumentResponseFromURL(getURL.toString());
            if (doc != null) {
                XPathFactory factory = XPathFactory.newInstance();
                XPath xPath = factory.newXPath();  
                NodeList artistNodes = (NodeList)xPath.evaluate("//similartracks/track", doc, XPathConstants.NODESET);
                for (int i = 0; i < artistNodes.getLength(); i++) {
                    String similarArtistName = null;
                    String similarTrackTitle = null;                    
                    float similarity = 0.0f;
                    NodeList childNodes = artistNodes.item(i).getChildNodes();
                    for (int c = 0; c < childNodes.getLength(); ++c) {
                        Node node = childNodes.item(c);
                        if (node.getNodeName().equals("name"))
                        	similarTrackTitle = node.getTextContent();
                        else if (node.getNodeName().equals("match"))
                            similarity = Float.parseFloat(node.getTextContent()) / 100.0f; // normalized
                        else if (node.getNodeName().equals("artist")) {
                            NodeList childNodes2 = node.getChildNodes();
                            for (int c2 = 0; c2 < childNodes2.getLength(); ++c2) {
                                Node childNode = childNodes2.item(c2);
                                if (childNode.getNodeName().equals("name"))
                                	similarArtistName = childNode.getTextContent();
                            }
                        }
                    }
                    if (similarArtistName != null)
                        result.put(LastfmSongProfile.getSongKey(similarArtistName, similarTrackTitle), similarity);                       
                }     
            }
        } catch (Exception e) {
            log.error("getSimilarSongs(): error", e);
        }
        return result;        
    }    
    
    static public Vector<DegreeValue> getTopSongTags(String artistName, String trackTitle) {
    	Vector<DegreeValue> result = new Vector<DegreeValue>();
        try {
            StringBuffer getURL = new StringBuffer();
            getURL.append("http://ws.audioscrobbler.com/1.0/track/");
            String encodedArtistName = URLEncoder.encode(artistName, "UTF-8");
            getURL.append(encodedArtistName);
            getURL.append("/");
            String encodedTitle = URLEncoder.encode(trackTitle, "UTF-8");
            getURL.append(encodedTitle);
            getURL.append("/toptags.xml");
            MiningAPIFactory.getLastfmAPI().getRateController().startQuery();
            Document doc = WebHelper.getDocumentResponseFromURL(getURL.toString());
            if (doc != null) {
                XPathFactory factory = XPathFactory.newInstance();
                XPath xPath = factory.newXPath();
                NodeList tagNodes = (NodeList)xPath.evaluate("//toptags/tag", doc, XPathConstants.NODESET);
                for (int i = 0; i < tagNodes.getLength(); i++) {
                    String tagName = null;
                    float degree = 0.0f;
                    NodeList childNodes = tagNodes.item(i).getChildNodes();
                    for (int c = 0; c < childNodes.getLength(); ++c) {
                        Node node = childNodes.item(c);
                        if (node.getNodeName().equals("name"))
                            tagName = node.getTextContent();
                        else if (node.getNodeName().equals("count"))
                    		degree = Float.parseFloat(node.getTextContent()) / 100.0f;                        
                    }
                    if (tagName != null) 
                    	result.add(new DegreeValue(tagName, degree, DATA_SOURCE_LASTFM));
                }                
            }
        } catch (Exception e) {
            log.error("getTopSongTags(): error", e);
        }
        return result;                
    }        
    
}