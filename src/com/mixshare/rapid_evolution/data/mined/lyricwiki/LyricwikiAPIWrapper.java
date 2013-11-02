package com.mixshare.rapid_evolution.data.mined.lyricwiki;

import java.net.URLEncoder;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.w3c.dom.Document;

import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.mined.CommonMiningAPIWrapper;
import com.mixshare.rapid_evolution.data.mined.MinedProfile;
import com.mixshare.rapid_evolution.data.mined.MiningAPIFactory;
import com.mixshare.rapid_evolution.data.mined.lyricwiki.song.LyricwikiSongProfile;
import com.mixshare.rapid_evolution.data.mined.util.WebHelper;
import com.mixshare.rapid_evolution.data.profile.search.artist.ArtistProfile;
import com.mixshare.rapid_evolution.data.profile.search.label.LabelProfile;
import com.mixshare.rapid_evolution.data.profile.search.release.ReleaseProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;

/**
 * http://lyrics.wikia.com/LyricWiki:REST
 */
public class LyricwikiAPIWrapper extends CommonMiningAPIWrapper {

    static private Logger log = Logger.getLogger(LyricwikiAPIWrapper.class);
		
	public byte getDataSource() { return DATA_SOURCE_LYRICWIKI; }
	
	public MinedProfile fetchArtistProfile(ArtistProfile artistProfile) { return null; }
	public MinedProfile fetchLabelProfile(LabelProfile labelProfile) { return null; }
	public MinedProfile fetchReleaseProfile(ReleaseProfile releaseProfile) { return null; }
	public MinedProfile fetchSongProfile(SongProfile songProfile) { return new LyricwikiSongProfile(songProfile.getArtistsDescription(), songProfile.getSongDescription()); }
	
	static public String getLyrics(String artistDescription, String songTitle) {
		try {
			StringBuffer url = new StringBuffer();
			url.append("http://lyricwiki.org/api.php?func=getSong&artist=");
			String encodedArtist = URLEncoder.encode(artistDescription, "UTF-8");
			url.append(encodedArtist);
			url.append("&song=");
			String encodedTitle = URLEncoder.encode(songTitle, "UTF-8");
			url.append(encodedTitle);
			url.append("&fmt=xml");
			MiningAPIFactory.getLyricwikiAPI().getRateController().startQuery();
			Document doc = WebHelper.getDocumentResponseFromURL(url.toString());
			if (doc != null) {
	            XPathFactory factory = XPathFactory.newInstance();
	            XPath xPath = factory.newXPath();
	            String lyrics = xPath.evaluate("//LyricsResult/lyrics/text()", doc);
	            if (log.isDebugEnabled())	            	
	            	log.debug("getLyrics(): artist=" + artistDescription + ", songTitle=" + songTitle + ", lyrics=" + lyrics);
	            return lyrics;
			}
		} catch (Exception e) {
			log.error("getLyrics(): error", e);
		}
		return null;
	}	
    
    static public void main(String[] args) {
        try {
        	RapidEvolution3.loadLog4J();
            getLyrics("tool", "schism");
        } catch (Exception e) {
            log.error("main(): error", e);
        }        
    }	
}
