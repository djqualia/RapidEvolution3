package com.mixshare.rapid_evolution.data.mined.lyricsfly;

import java.net.URLEncoder;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.w3c.dom.Document;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.mined.CommonMiningAPIWrapper;
import com.mixshare.rapid_evolution.data.mined.MinedProfile;
import com.mixshare.rapid_evolution.data.mined.MiningAPIFactory;
import com.mixshare.rapid_evolution.data.mined.lyricsfly.song.LyricsflySongProfile;
import com.mixshare.rapid_evolution.data.mined.util.WebHelper;
import com.mixshare.rapid_evolution.data.profile.search.artist.ArtistProfile;
import com.mixshare.rapid_evolution.data.profile.search.label.LabelProfile;
import com.mixshare.rapid_evolution.data.profile.search.release.ReleaseProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.search.SearchEncoder;
import com.mixshare.rapid_evolution.util.StringUtil;

/**
 * http://lyricsfly.com/api/#doc
 */
public class LyricsFlyAPIWrapper extends CommonMiningAPIWrapper {

    static private Logger log = Logger.getLogger(LyricsFlyAPIWrapper.class);
	
	static public String API_KEY = RE3Properties.getEncryptedProperty("lyricsfly_api_key");
	    
	public byte getDataSource() { return DATA_SOURCE_LYRICSFLY; }
	
	public MinedProfile fetchArtistProfile(ArtistProfile artistProfile) { return null; }
	public MinedProfile fetchLabelProfile(LabelProfile labelProfile) { return null; }
	public MinedProfile fetchReleaseProfile(ReleaseProfile releaseProfile) { return null; }
	public MinedProfile fetchSongProfile(SongProfile songProfile) { return getLyricsflySongProfile(songProfile.getArtistsDescription(), songProfile.getSongDescription()); }
	
	static public LyricsflySongProfile getLyricsflySongProfile(String artistDescription, String songTitle) {
		LyricsflySongProfile result = null;
		try {
			StringBuffer url = new StringBuffer();
			url.append("http://lyricsfly.com/api/api.php?i=");
			url.append(API_KEY);
			url.append("&a=");
			String encodedArtist = URLEncoder.encode(SearchEncoder.encodeString(artistDescription), "UTF-8");
			url.append(encodedArtist);
			url.append("&t=");
			String encodedTitle = URLEncoder.encode(SearchEncoder.encodeString(songTitle), "UTF-8");
			url.append(encodedTitle);
			MiningAPIFactory.getLyricsflyAPI().getRateController().startQuery();
			if (log.isTraceEnabled())
				log.trace("getLyrics(): fetching from url=" + url);
			Document doc = WebHelper.getDocumentResponseFromURL(url.toString());
			if (doc != null) {
	            XPathFactory factory = XPathFactory.newInstance();
	            XPath xPath = factory.newXPath();
	            String lyrics = xPath.evaluate("//start/sg[1]/tx/text()", doc);
	            if (lyrics != null) {
	            	lyrics = StringUtil.replace(lyrics, "[br]", "\n");

	            	// these 2 fields are needed to construct the edit link
	            	String cs = xPath.evaluate("//start/sg[1]/cs/text()", doc);
	            	String id = xPath.evaluate("//start/sg[1]/id/text()", doc);
	            	
	            	if (log.isDebugEnabled())	            	
	            		log.debug("getLyrics(): artist=" + artistDescription + ", songTitle=" + songTitle + ", id=" + id + ", cs=" + cs + ", lyrics=" + lyrics);
	            	result = new LyricsflySongProfile(artistDescription, songTitle, lyrics, cs, id);
	            }
			}
		} catch (Exception e) {
			log.error("getLyrics(): error", e);
		}
		return result;
	}
	
    static public void main(String[] args) {
        try {
        	RapidEvolution3.loadLog4J();
            log.info(getLyricsflySongProfile("Adam Freeland", "We want your soul").getLyricsText());
        } catch (Exception e) {
            log.error("main(): error", e);
        }        
    }
		
}
