package com.mixshare.rapid_evolution.data.mined.billboard.artist;

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
 * http://api.billboard.com/apisvc/chart/v1/list?artist=daft+punk&api_key=cdgygfrvp3a7jqgdcmva2qkp
 */
public class BillboardArtistProfile extends BillboardCommonProfile {

    static private Logger log = Logger.getLogger(BillboardArtistProfile.class);
    static private final long serialVersionUID = 0L;
                
    ////////////
    // FIELDS //
    ////////////
    
    private String artistName;
    
    //////////////////
    // CONSTRUCTORS //
    //////////////////
    
    public BillboardArtistProfile() { super(DATA_TYPE_ARTISTS); }
    public BillboardArtistProfile(String artistName) {
    	super(DATA_TYPE_ARTISTS);
    	this.artistName = artistName;    	
    	try {
            // categories
			StringBuffer artistURL = new StringBuffer();
			artistURL.append("http://api.billboard.com/apisvc/chart/v1/list?artist=");
            String encodedArtistName = URLEncoder.encode(artistName, "UTF-8");			
			artistURL.append(encodedArtistName);
			artistURL.append("&api_key=");
            artistURL.append(BillboardAPIWrapper.API_KEY);
            artistURL.append("&sdate=1950-01-01&edate=");
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
            artistURL.append(edate);            
            if (log.isTraceEnabled())
            	log.trace("BillboardArtistProfile(): fetching artist info from url=" + artistURL.toString());
            MiningAPIFactory.getBillboardAPI().getRateController().startQuery();            
            Document doc = WebHelper.getDocumentResponseFromURL(artistURL.toString());
            if (doc != null) {
	            XPathFactory factory = XPathFactory.newInstance();
	            XPath xPath = factory.newXPath();	            
	            int totalResults = Integer.parseInt(xPath.evaluate("//searchResults/@totalReturned", doc));
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
			            if (artist.equalsIgnoreCase(artistName)) {
			            	chartEntries.add(new ChartEntry(chartName, chartIssueDate, specType, specId, artist, song, label, peak, weeksOn));
			            }
	            	}	            	
	            }	            
            }             
            
            if (log.isTraceEnabled())
            	log.trace("BillboardArtistProfile(): chartEntries=" + chartEntries);
    	} catch (Exception e) {
    		log.error("BillboardArtistProfile(): error", e);
    	}    	
    }
    
    /////////////
    // GETTERS //
    /////////////
        
    public String getArtistName() { return artistName; }    
           
    /////////////
    // SETTERS //
    /////////////
    
    public void setArtistName(String artistName) { this.artistName = artistName; }
    
    /////////////
    // METHODS //
    /////////////
    
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append(artistName);
        return result.toString();
    }    
 
    static public void main(String[] args) {
        try {
        	RapidEvolution3.loadLog4J();
            log.info("result=" + new BillboardArtistProfile("Daft Punk").getTotalWeeksOn());            
            log.info("result=" + new BillboardArtistProfile("Michael Jackson").getTotalWeeksOn());            
            log.info("result=" + new BillboardArtistProfile("Black Eyed Peas").getTotalWeeksOn());            
            log.info("result=" + new BillboardArtistProfile("Aphex Twin").getTotalWeeksOn());            
            log.info("result=" + new BillboardArtistProfile("Wilco").getTotalWeeksOn());            
        } catch (Exception e) {
            log.error("main(): error", e);
        }        
    }
            
}