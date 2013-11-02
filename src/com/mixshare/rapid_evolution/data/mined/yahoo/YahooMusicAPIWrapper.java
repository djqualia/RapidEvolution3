package com.mixshare.rapid_evolution.data.mined.yahoo;

import java.net.URLEncoder;
import java.util.Vector;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.mined.CommonMiningAPIWrapper;
import com.mixshare.rapid_evolution.data.mined.MinedProfile;
import com.mixshare.rapid_evolution.data.mined.util.WebHelper;
import com.mixshare.rapid_evolution.data.mined.yahoo.artist.YahooArtistProfile;
import com.mixshare.rapid_evolution.data.mined.yahoo.song.YahooSongProfile;
import com.mixshare.rapid_evolution.data.profile.search.artist.ArtistProfile;
import com.mixshare.rapid_evolution.data.profile.search.label.LabelProfile;
import com.mixshare.rapid_evolution.data.profile.search.release.ReleaseProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;

public class YahooMusicAPIWrapper extends CommonMiningAPIWrapper {
	
    static private Logger log = Logger.getLogger(YahooMusicAPIWrapper.class);
	
	static public String API_KEY = RE3Properties.getEncryptedProperty("yahoo_api_key");
	
	public byte getDataSource() { return DATA_SOURCE_IDIOMAG; }
	
	public MinedProfile fetchArtistProfile(ArtistProfile artistProfile) { return new YahooArtistProfile(artistProfile.getArtistName()); }
	public MinedProfile fetchLabelProfile(LabelProfile labelProfile) { return null; }
	public MinedProfile fetchReleaseProfile(ReleaseProfile releaseProfile) { return null; }
	public MinedProfile fetchSongProfile(SongProfile songProfile) { return new YahooSongProfile(songProfile.getArtistsDescription(), songProfile.getSongDescription()); }	
			
	public String getArtistId(String artistName) {
		String result = null;
		try {
			StringBuffer searchURL = new StringBuffer();
			searchURL.append("http://us.music.yahooapis.com/artist/v1/list/search/artist/");
            String encodedArtistName = URLEncoder.encode(artistName, "UTF-8");
            searchURL.append(encodedArtistName);
            searchURL.append("?appid=");
            searchURL.append(API_KEY);
            if (log.isTraceEnabled())
            	log.trace("getArtistId(): searching with url=" + searchURL.toString());
            getRateController().startQuery();
            Document doc = WebHelper.getDocumentResponseFromURL(searchURL.toString());
            if (doc != null) {
	            XPathFactory factory = XPathFactory.newInstance();
	            XPath xPath = factory.newXPath();
	            
	            Vector<String> artistIds = new Vector<String>();
	            Vector<String> artistNames = new Vector<String>();
	            
	            // ids
	            NodeList idNodes = (NodeList)xPath.evaluate("//Artists/Artist", doc, XPathConstants.NODESET);
	            for (int i = 0; i < idNodes.getLength(); i++) 
	            	artistIds.add(idNodes.item(i).getAttributes().getNamedItem("id").getTextContent());
	            // names
	            NodeList nameNodes = (NodeList)xPath.evaluate("//Artists/Artist", doc, XPathConstants.NODESET);
	            for (int i = 0; i < nameNodes.getLength(); i++) 
	            	artistNames.add(idNodes.item(i).getAttributes().getNamedItem("name").getTextContent());
	            
	            int i = 0;
	            for (String resultArtistName : artistNames) {
	            	if (resultArtistName.equalsIgnoreCase(artistName)) {
	            		result = artistIds.get(i);
	            		break;
	            	}
	            	++i;
	            }	                        	
            }            
		} catch (Exception e) {
			log.error("getArtistId(): error", e);
		}
		return result;
	}
	
	public String getSongId(String artistName, String songTitle) {
		String result = null;
		try {
			String songDescription = songTitle + " - " + artistName;
			StringBuffer searchURL = new StringBuffer();
			searchURL.append("http://us.music.yahooapis.com/track/v1/list/search/track/");
            String encodedSongDescription = URLEncoder.encode(songDescription, "UTF-8");
            searchURL.append(encodedSongDescription);
            searchURL.append("?appid=");
            searchURL.append(API_KEY);
            if (log.isTraceEnabled())
            	log.trace("getSongId(): searching with url=" + searchURL.toString());
            getRateController().startQuery();
            Document doc = WebHelper.getDocumentResponseFromURL(searchURL.toString());
            if (doc != null) {
	            XPathFactory factory = XPathFactory.newInstance();
	            XPath xPath = factory.newXPath();
	            
	            // ids
	            NodeList idNodes = (NodeList)xPath.evaluate("//Tracks/Track", doc, XPathConstants.NODESET);
	            for (int i = 0; i < idNodes.getLength(); i++) {
	            	String id = idNodes.item(i).getAttributes().getNamedItem("id").getTextContent();
	            	String trackName = idNodes.item(i).getAttributes().getNamedItem("title").getTextContent();
		            NodeList artistNameNodes = (NodeList)xPath.evaluate("./Artist", idNodes.item(i), XPathConstants.NODESET);
		            for (int j = 0; j < artistNameNodes.getLength(); j++) { 
		            	String trackArtist = artistNameNodes.item(j).getAttributes().getNamedItem("name").getTextContent();
		            	if ((trackName.equalsIgnoreCase(songDescription)) || ((trackName.equalsIgnoreCase(songTitle)) && (artistName.equalsIgnoreCase(trackArtist)))) {
		            		result = id;
		            		break;
		            	}		            	
		            }		            
	            }
	                        	
            }            
		} catch (Exception e) {
			log.error("getSongId(): error", e);
		}
		return result;
	}
		
}
