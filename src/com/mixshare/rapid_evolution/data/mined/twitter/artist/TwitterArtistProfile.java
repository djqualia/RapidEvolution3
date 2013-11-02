package com.mixshare.rapid_evolution.data.mined.twitter.artist;

import java.io.Serializable;
import java.net.URLEncoder;
import java.util.Vector;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.mined.MinedProfile;
import com.mixshare.rapid_evolution.data.mined.MinedProfileHeader;
import com.mixshare.rapid_evolution.data.mined.twitter.status.TwitterStatus;
import com.mixshare.rapid_evolution.data.mined.util.WebHelper;

public class TwitterArtistProfile extends MinedProfile implements Serializable, DataConstants {

    static private Logger log = Logger.getLogger(TwitterArtistProfile.class);
    static private final long serialVersionUID = 0L;
        
    ////////////
    // FIELDS //
    ////////////

    private boolean isValid = false;
    private String id;
    private String name;
	private String screenName;
    private String location;
    private String description;
    private String profileImageURL;
    private String url;
    private String lang;
    private String currentStatus;
    private int numFollowers;
    private int numFriends;
    private int numFavorites;
    private int numStatuses; // # tweets
	private Vector<TwitterStatus> recentStatuses = new Vector<TwitterStatus>();
    
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
	public TwitterArtistProfile(String screenName) {
        super(new MinedProfileHeader(DATA_TYPE_ARTISTS, DATA_SOURCE_TWITTER));
        this.screenName = screenName;
        try {
       	 	if (log.isDebugEnabled())
       	 		log.debug("TwitterArtistProfile(): fetching screenName=" + screenName);

            StringBuffer getURL = new StringBuffer();
            getURL.append("http://api.twitter.com/1/users/show.xml?screen_name=");
            String encodedScreenName = URLEncoder.encode(screenName, "UTF-8");
            getURL.append(encodedScreenName);

            XPathFactory factory = XPathFactory.newInstance();
            XPath xPath = factory.newXPath();  
            
       	 	Document doc = WebHelper.getDocumentResponseFromURL(getURL.toString());
       	 	if (doc != null) {
       	 		                
                id = (String)xPath.evaluate("//user/id/text()", doc);
                name = (String)xPath.evaluate("//user/name/text()", doc);
                screenName = (String)xPath.evaluate("//user/screen_name/text()", doc);
                location = (String)xPath.evaluate("//user/location/text()", doc);
                description = (String)xPath.evaluate("//user/description/text()", doc);
                profileImageURL = (String)xPath.evaluate("//user/profile_image_url/text()", doc);
                url = (String)xPath.evaluate("//user/url/text()", doc);
                lang = (String)xPath.evaluate("//user/lang/text()", doc);
                currentStatus = (String)xPath.evaluate("//user/status/text/text()", doc);
                numFollowers = Integer.parseInt(xPath.evaluate("//user/followers_count/text()", doc));
                numFriends = Integer.parseInt(xPath.evaluate("//user/friends_count/text()", doc));
                numFavorites = Integer.parseInt(xPath.evaluate("//user/favourites_count/text()", doc));
                numStatuses = Integer.parseInt(xPath.evaluate("//user/statuses_count/text()", doc));
                
    	        if (numStatuses > 0)
    	        	isValid = true;
       	 	}
       	 	
       	 	getURL = new StringBuffer();
       	 	getURL.append("http://api.twitter.com/1/statuses/user_timeline.xml?screen_name=");
       	 	getURL.append(encodedScreenName);
       	 	doc = WebHelper.getDocumentResponseFromURL(getURL.toString());
    	 	if (doc != null) {
    	 		
    	 		NodeList statusNodes = (NodeList)xPath.evaluate("//statuses/status", doc, XPathConstants.NODESET);
                for (int i = 0; i < statusNodes.getLength(); i++) {
                	recentStatuses.add(new TwitterStatus(statusNodes.item(i)));
                }    	 		
    	 		
    	 	}
       	 	       	 	
        } catch (Exception e) {
        	log.error("TwitterArtistProfile(): error", e);
        }
    }    

    /////////////
    // GETTERS //
    /////////////
    
	public boolean isValid() { return isValid; }
    public String getId() { return id; }
	public String getName() { return name; }
	public String getScreenName() { return screenName; }
	public String getLocation() { return location; }
	public String getDescription() { return description; }
	public String getProfileImageURL() { return profileImageURL; }
	public String getUrl() { return url; }
	public String getLang() { return lang; }
	public String getCurrentStatus() { return currentStatus; }
	public int getNumFollowers() { return numFollowers; }
	public int getNumFriends() { return numFriends; }
	public int getNumFavorites() { return numFavorites; }
	public int getNumStatuses() { return numStatuses; }
    public Vector<TwitterStatus> getRecentStatuses() { return recentStatuses; }

	/////////////
	// SETTERS //
	/////////////
	
	public void setId(String id) { this.id = id; }
	public void setName(String name) { this.name = name; }
	public void setScreenName(String screenName) { this.screenName = screenName; }
	public void setLocation(String location) { this.location = location; }
	public void setDescription(String description) { this.description = description; }
	public void setProfileImageURL(String profileImageURL) { this.profileImageURL = profileImageURL; }
	public void setUrl(String url) { this.url = url; }
	public void setLang(String lang) { this.lang = lang; }
	public void setCurrentStatus(String currentStatus) { this.currentStatus = currentStatus; }
	public void setNumFollowers(int numFollowers) { this.numFollowers = numFollowers; }
	public void setNumFriends(int numFriends) { this.numFriends = numFriends; }
	public void setNumFavorites(int numFavorites) { this.numFavorites = numFavorites; }
	public void setNumStatuses(int numStatuses) { this.numStatuses = numStatuses; }
	public void setValid(boolean isValid) { this.isValid = isValid; }
	public void setRecentStatuses(Vector<TwitterStatus> recentStatuses) { this.recentStatuses = recentStatuses; }	
	
    /////////////
    // METHODS //
    /////////////
    
	public String toString() { return name; }
	
	public String toStringFull() {
		StringBuffer result = new StringBuffer();
		result.append("\nID=");
		result.append(id);
		result.append("\nNAME=");
		result.append(name);
		result.append("\nSCREEN NAME=");
		result.append(screenName);
		result.append("\nLOCATION=");
		result.append(location);
		result.append("\nDESCRIPTION=");
		result.append(description);
		result.append("\nPROFILE IMAGE URL=");
		result.append(profileImageURL);
		result.append("\nURL=");
		result.append(url);
		result.append("\nLANG=");
		result.append(lang);
		result.append("\nCURRENT STATUS=");
		result.append(currentStatus);
		result.append("\n# Followers=");
		result.append(numFollowers);
		result.append("\n# Friends=");
		result.append(numFriends);
		result.append("\n# Favorites=");
		result.append(numFavorites);
		result.append("\n# Statuses=");
		result.append(numStatuses);
		result.append("\nRecent Statuses=");
		for (TwitterStatus status : recentStatuses)
			result.append(status.toStringFull());
		return result.toString();
	}
	
    static public void main(String[] args) {
        try {
        	RapidEvolution3.loadLog4J();
            log.info("main(): profile=" + new TwitterArtistProfile("Kanye_Info").toStringFull());
        } catch (Exception e) {
            log.error("main(): error", e);
        }        
    }
    
}
