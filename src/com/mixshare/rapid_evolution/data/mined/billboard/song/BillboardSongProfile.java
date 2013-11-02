package com.mixshare.rapid_evolution.data.mined.billboard.song;

import java.net.URLEncoder;
import java.util.Calendar;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.mined.MiningAPIFactory;
import com.mixshare.rapid_evolution.data.mined.billboard.BillboardAPIWrapper;
import com.mixshare.rapid_evolution.data.mined.billboard.BillboardCommonProfile;
import com.mixshare.rapid_evolution.data.mined.billboard.ChartEntry;
import com.mixshare.rapid_evolution.data.mined.util.WebHelper;

/**
 * http://api.billboard.com/apisvc/chart/v1/list?song=daft+punk&api_key=cdgygfrvp3a7jqgdcmva2qkp
 */
public class BillboardSongProfile extends BillboardCommonProfile {

    static private Logger log = Logger.getLogger(BillboardSongProfile.class);
    static private final long serialVersionUID = 0L;
                
    ////////////
    // FIELDS //
    ////////////
    
    private String artistDescription;
    private String songName;
    
    //////////////////
    // CONSTRUCTORS //
    //////////////////

    public BillboardSongProfile() { super(DATA_TYPE_SONGS); }    
    public BillboardSongProfile(String artistDescription, String songName) {
    	super(DATA_TYPE_SONGS);
    	this.artistDescription = artistDescription;
    	this.songName = songName;    	
    	try {
            // categories
			StringBuffer songURL = new StringBuffer();
			songURL.append("http://api.billboard.com/apisvc/chart/v1/list?song=");
            String encodedSongName = URLEncoder.encode(songName, "UTF-8");			
			songURL.append(encodedSongName);
			songURL.append("&artist=");
            String encodedArtistDescription = URLEncoder.encode(artistDescription, "UTF-8");			
			songURL.append(encodedArtistDescription);
			songURL.append("&api_key=");
            songURL.append(BillboardAPIWrapper.API_KEY);
            songURL.append("&sdate=1950-01-01&edate=");
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            if (month == 0) { // january
            	month = 12;
            	--year;            	
            }
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            // setting edate to 1 month in the past (can't query the past 30 days on billboards)
            String edate = year + "-" + month + "-" + day;
            songURL.append(edate);
            if (log.isTraceEnabled())
            	log.trace("BillboardSongProfile(): fetching song info from url=" + songURL.toString());
            MiningAPIFactory.getBillboardAPI().getRateController().startQuery();                        
            Document doc = WebHelper.getDocumentResponseFromURL(songURL.toString());
            if (doc != null) {
	            XPathFactory factory = XPathFactory.newInstance();
	            XPath xPath = factory.newXPath();	            
	            int totalResults = 0;
	            try { totalResults = Integer.parseInt(xPath.evaluate("//searchResults/@totalReturned", doc)); } catch (NumberFormatException e) { }
	            if (totalResults > 0) {
	            	isValid = true;
	            	for (int c = 0; c < totalResults; ++c) {
			            String chartName = xPath.evaluate("//searchResults/chartItem[" + (c + 1) + "]/chart/name/text()", doc);
			            String chartIssueDate = xPath.evaluate("//searchResults/chartItem[" + (c + 1) + "]/chart/issueDate/text()", doc);
			            String specType = xPath.evaluate("//searchResults/chartItem[" + (c + 1) + "]/chart/specType/text()", doc);
			            int specId = Integer.parseInt(xPath.evaluate("//searchResults/chartItem[" + (c + 1) + "]/chart/specId/text()", doc));
			            String artist = xPath.evaluate("//searchResults/chartItem[" + (c + 1) + "]/artist/text()", doc);
			            String song = xPath.evaluate("//searchResults/chartItem[" + (c + 1) + "]/song/text()", doc);
			            String label = xPath.evaluate("//searchResults/chartItem[" + (c + 1) + "]/distribution/text()", doc);
			            int peak = Integer.parseInt(xPath.evaluate("//searchResults/chartItem[" + (c + 1) + "]/peak/text()", doc));
			            int weeksOn = Integer.parseInt(xPath.evaluate("//searchResults/chartItem[" + (c + 1) + "]/weeksOn/text()", doc));
			            if (song.equalsIgnoreCase(songName) && artist.equalsIgnoreCase(artistDescription)) {
			            	chartEntries.add(new ChartEntry(chartName, chartIssueDate, specType, specId, song, song, label, peak, weeksOn));
			            }
	            	}	            	
	            }	            
            }             
            
            if (log.isTraceEnabled())
            	log.trace("BillboardSongProfile(): chartEntries=" + chartEntries);
    	} catch (Exception e) {
    		log.error("BillboardSongProfile(): error", e);
    	}    	
    }

    /////////////
    // GETTERS //
    /////////////
        
    public String getSongName() { return songName; }    
	public String getArtistDescription() { return artistDescription; }

	/////////////
	// SETTERS //
	/////////////

	public void setArtistDescription(String artistDescription) { this.artistDescription = artistDescription; }
	public void setSongName(String songName) { this.songName = songName; }
	
    /////////////
    // METHODS //
    /////////////
    
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append(songName);
        return result.toString();
    }    
 
    static public void main(String[] args) {
        try {
        	RapidEvolution3.loadLog4J();
            log.info("result=" + new BillboardSongProfile("Daft Punk", "Alive 2007").getTotalWeeksOn());
            log.info("result=" + new BillboardSongProfile("Michael Jackson", "Beat It").getTotalWeeksOn());
            log.info("result=" + new BillboardSongProfile("Michael Jackson", "Billie Jean").getTotalWeeksOn());
            log.info("result=" + new BillboardSongProfile("Beck", "Loser").getTotalWeeksOn());
        } catch (Exception e) {
            log.error("main(): error", e);
        }        
    }
            
}