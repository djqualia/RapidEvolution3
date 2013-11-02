package com.mixshare.rapid_evolution.data.mined.discogs.artist;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Vector;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.mined.MiningAPIFactory;
import com.mixshare.rapid_evolution.data.mined.discogs.DiscogsAPIWrapper;
import com.mixshare.rapid_evolution.data.mined.util.WebHelper;
import com.mixshare.rapid_evolution.util.StringUtil;

public class DiscogsArtist implements Serializable {

    static private Logger log = Logger.getLogger(DiscogsArtist.class);
    static private final long serialVersionUID = 0L;
        
    ////////////
    // FIELDS //
    ////////////
    
    private String artistName = "";
    private String realName = "";
    private String profile = "";
    private Vector<String> nameVariations = new Vector<String>();
    private Vector<String> aliases = new Vector<String>();
    private Vector<String> urls = new Vector<String>();
    private Vector<String> releaseIds = new Vector<String>();
    private Vector<String> remixReleaseIds = new Vector<String>();
    private Vector<String> mixReleaseIds = new Vector<String>();
    private Vector<String> appearsOnReleases = new Vector<String>();
    private String primaryImageURL = "";
    private Vector<String> imageURLs = new Vector<String>();    
    
    //////////////////
    // CONSTRUCTORS //
    //////////////////

    public DiscogsArtist() { }    
    public DiscogsArtist(Document doc) {
        try {
            XPathFactory factory = XPathFactory.newInstance();
            XPath xPath = factory.newXPath();  
            
            // ARTIST NAME
            artistName = (String)xPath.evaluate("//artist/name/text()", doc);
            // REAL NAME
            realName = (String)xPath.evaluate("//artist/realname/text()", doc);
            // PROFILE
            profile = (String)xPath.evaluate("//artist/profile/text()", doc);
            // NAME VARIATIONS
            Object nameObj = xPath.evaluate("//artist/namevariations/name/text()", doc, XPathConstants.NODESET);
            NodeList nameNodes = (NodeList) nameObj;
            for (int i = 0; i < nameNodes.getLength(); i++)
                nameVariations.add(nameNodes.item(i).getNodeValue().trim());
            // ALIASES
            Object aliasesObj = xPath.evaluate("//artist/aliases/name/text()", doc, XPathConstants.NODESET);
            NodeList aliasNodes = (NodeList) aliasesObj;
            for (int i = 0; i < aliasNodes.getLength(); i++)
                aliases.add(aliasNodes.item(i).getNodeValue().trim());
            // URLS
            Object urlsObj = xPath.evaluate("//artist/urls/url/text()", doc, XPathConstants.NODESET);
            NodeList urlNodes = (NodeList) urlsObj;
            for (int i = 0; i < urlNodes.getLength(); i++)
                urls.add(urlNodes.item(i).getNodeValue().trim());
            // RELEASE IDS
            Object releasesObj = xPath.evaluate("//artist/releases/release", doc, XPathConstants.NODESET);
            NodeList releaseNodes = (NodeList) releasesObj;
            for (int i = 0; i < releaseNodes.getLength(); i++) {
                try {
                    String type = releaseNodes.item(i).getAttributes().getNamedItem("type").getTextContent();
                    if (type.equals("Main")) {
                        releaseIds.add(releaseNodes.item(i).getAttributes().getNamedItem("id").getTextContent());
                    } else if (type.equals("Remix")) {
                        remixReleaseIds.add(releaseNodes.item(i).getAttributes().getNamedItem("id").getTextContent());                    
                    } else if (type.equals("Mixed by")) {
                        mixReleaseIds.add(releaseNodes.item(i).getAttributes().getNamedItem("id").getTextContent());                    
                    } else if (type.equals("Appearance")) {
                        appearsOnReleases.add(releaseNodes.item(i).getAttributes().getNamedItem("id").getTextContent());                    
                    } else if (type.equals("TrackAppearance")) {
                        appearsOnReleases.add(releaseNodes.item(i).getAttributes().getNamedItem("id").getTextContent());                    
                    } else if (type.equals("UnofficialRelease")) {
                        releaseIds.add(releaseNodes.item(i).getAttributes().getNamedItem("id").getTextContent());
                    }
                } catch (Exception e) { }
            }
            // IMAGE URLS
            Object imagesObj = xPath.evaluate("//artist/images/image", doc, XPathConstants.NODESET);
            NodeList imagesNodes = (NodeList) imagesObj;
            for (int i = 0; i < imagesNodes.getLength(); i++) {
                try {
                    String type = imagesNodes.item(i).getAttributes().getNamedItem("type").getTextContent();
                    String uri = imagesNodes.item(i).getAttributes().getNamedItem("uri").getTextContent();
                    if (type.equals("primary") || primaryImageURL.equals("")) {
                        primaryImageURL = uri;
                    }
                    imageURLs.add(uri);
                } catch (Exception e) { }                
            }
        } catch (Exception e) {
            log.error("DiscogsLabel(): error", e);
        }
    }
    
    public DiscogsArtist(String httpData) {
    	// ARTIST ID 
        String prefix1 = "favorite_add?artist=";
        String suffix1 = "')";
        int startIndex = httpData.indexOf(prefix1);
        int endIndex = httpData.indexOf(suffix1, startIndex + prefix1.length());
        String artistId = null;
        if ((startIndex >= 0) && (endIndex >= 0))
        	artistId = httpData.substring(startIndex + prefix1.length(), endIndex).trim();    	    	
        // ARTIST NAME
        prefix1 = "<div class=\"profile\">";
        suffix1 = "</h1>";
        startIndex = httpData.indexOf(prefix1);
        endIndex = httpData.indexOf(suffix1, startIndex + prefix1.length());
        artistName = StringUtil.stripHtml(httpData.substring(startIndex + prefix1.length(), endIndex));
        if (artistId == null) {
        	try { artistId = URLEncoder.encode(artistName, "UTF-8"); } catch (UnsupportedEncodingException e) { }
        }
        // REAL NAME
        prefix1 = "<div class=\"head\">Real Name:</div><div class=\"content\">";
        suffix1 = "</div>";
        startIndex = httpData.indexOf(prefix1);
        if (startIndex >= 0) {
            endIndex = httpData.indexOf(suffix1, startIndex + prefix1.length());
            realName = StringUtil.stripHtml(httpData.substring(startIndex + prefix1.length(), endIndex));
        }
        // PROFILE
        prefix1 = "<div class=\"head\">Profile:</div>";
        suffix1 = "</div></div>";
        startIndex = httpData.indexOf(prefix1);
        if (startIndex >= 0) {
            endIndex = httpData.indexOf(suffix1, startIndex + prefix1.length());
            profile = StringUtil.stripHtml(httpData.substring(startIndex + prefix1.length(), endIndex));
        }
        // NAME VARIATIONS
        prefix1 = "<div id=\"anvs\">";
        suffix1 = "</div>";
        startIndex = httpData.indexOf(prefix1);
        if (startIndex >= 0) {
            int lastIndex = startIndex + prefix1.length();
            endIndex = httpData.indexOf(suffix1, lastIndex);
            String nameVariationsText = httpData.substring(lastIndex, endIndex).trim();
            prefix1 = "<a href=\"/artist/";
            suffix1 = "</a>";
            lastIndex = 0;
            startIndex = nameVariationsText.indexOf(prefix1, lastIndex);
            while (startIndex >= 0) {
                lastIndex = nameVariationsText.indexOf(suffix1, startIndex);
                String text = nameVariationsText.substring(startIndex + prefix1.length(), lastIndex);
                String prefix2 = "\">";
                String nameVariation = text.substring(text.indexOf(prefix2) + prefix2.length()).trim();
                if (!nameVariation.equalsIgnoreCase(artistName))
                	nameVariations.add(nameVariation);
                startIndex = nameVariationsText.indexOf(prefix1, lastIndex);
            }
        }
        // ALIASES
        prefix1 = "<div class=\"head\">Aliases:</div><div class=\"content\">";
        suffix1 = "</div>";
        startIndex = httpData.indexOf(prefix1);
        if (startIndex >= 0) {
            int lastIndex = startIndex + prefix1.length();
            endIndex = httpData.indexOf(suffix1, lastIndex);
            String aliasText = httpData.substring(lastIndex, endIndex).trim();
            prefix1 = "<a href=\"/artist/";
            suffix1 = "</a>";
            lastIndex = 0;
            startIndex = aliasText.indexOf(prefix1, lastIndex);
            while (startIndex >= 0) {
                lastIndex = aliasText.indexOf(suffix1, startIndex);
                String text = aliasText.substring(startIndex + prefix1.length(), lastIndex);
                String prefix2 = "\">";
                String alias = text.substring(text.indexOf(prefix2) + prefix2.length());
                aliases.add(alias.trim());
                startIndex = aliasText.indexOf(prefix1, lastIndex);
            }
        }
        // URLS
        prefix1 = "<div class=\"head\">Sites:</div><div class=\"content\">";
        suffix1 = "</div>";
        startIndex = httpData.indexOf(prefix1);
        if (startIndex >= 0) {
            endIndex = httpData.indexOf(suffix1, startIndex + prefix1.length());
            String urlsString = httpData.substring(startIndex + prefix1.length(), endIndex).trim();
            urls = StringUtil.getAllInBetweens(urlsString, "href=\"", "\"");
        }
        // RELEASE IDS  
        releaseIds = DiscogsAPIWrapper.collectReleaseIds("http://www.discogs.com/artist/" + artistId + "/get_releases/Releases/All?sort=year-asc");
        // UNOFFICIAL RELEASES
        releaseIds = DiscogsAPIWrapper.collectReleaseIds("http://www.discogs.com/artist/" + artistId + "/get_releases/Unofficial/All?sort=year-asc", releaseIds);
        // REMIX RELEASES
        remixReleaseIds = DiscogsAPIWrapper.collectReleaseIds("http://www.discogs.com/artist/" + artistId + "/get_releases/Credits/Remix?sort=year-asc");
        // MIX RELEASE IDS
        mixReleaseIds = DiscogsAPIWrapper.collectReleaseIds("http://www.discogs.com/artist/" + artistId + "/get_releases/Appearances/Mixes?sort=year-asc");        
        // APPEARANCE RELEASE IDS
        appearsOnReleases = DiscogsAPIWrapper.collectReleaseIds("http://www.discogs.com/artist/" + artistId + "/get_releases/Appearances/All?sort=year-asc");
        // IMAGE URLS
        try {
            String imagesURL = "http://www.discogs.com/viewimages?artist=" + URLEncoder.encode(artistName, "UTF-8");
            String imagesData = WebHelper.getTextResponseFromURL(imagesURL);
            if (imagesData != null) {
                prefix1 = "Update These Images</a>";
                suffix1 = "<p class=\"copyright\">";
                startIndex = imagesData.indexOf(prefix1);
                if (startIndex > 0) {
                    endIndex = imagesData.indexOf(suffix1);
                    if (endIndex > 0) {
                    	String imagesSubdata = imagesData.substring(startIndex + prefix1.length(), endIndex);
                    	Vector<String> retrievedImageURLs = StringUtil.getAllInBetweens(imagesSubdata, "<img src=\"", "\"");
                    	for (String imageURL : retrievedImageURLs) {
                    		imageURL = StringUtil.replace(imageURL, "s.dsimg.com", "www.discogs.com");
                    		imageURLs.add(imageURL);
                    	}
                    	if (imageURLs.size() > 0)
                    		primaryImageURL = (String)imageURLs.get(0);                    	
                    }
                }
            }                            
        } catch (Exception e) {
            log.error("DiscogsArtist(): error", e);
        }
    }        

	/////////////
	// GETTERS //
	/////////////
    
    public String getArtistName() { return artistName; }
    public String getRealName() { return realName; }
    public Vector<String> getNameVariations() { return nameVariations; }
    public Vector<String> getAliases() { return aliases; }
    public Vector<String> getURLs() { return urls; }
    public Vector<String> getReleaseIDs() { return releaseIds; }    
    public Vector<String> getRemixReleaseIDs() { return remixReleaseIds; }
    public Vector<String> getMixReleaseIDs() { return mixReleaseIds; }
    public Vector<String> getAppearsOnReleaseIDs() { return appearsOnReleases; }
    public String getPrimaryImageURL() { return primaryImageURL; }
    public Vector<String> getImageURLs() { return imageURLs; }
    public String getProfile() { return profile; }
	public Vector<String> getUrls() {
		return urls;
	}
	public Vector<String> getReleaseIds() {
		return releaseIds;
	}
	public Vector<String> getRemixReleaseIds() {
		return remixReleaseIds;
	}
	public Vector<String> getMixReleaseIds() {
		return mixReleaseIds;
	}
	public Vector<String> getAppearsOnReleases() {
		return appearsOnReleases;
	}

    /////////////
    // SETTERS //
    /////////////
    
	public void setArtistName(String artistName) { this.artistName = artistName; }
	public void setRealName(String realName) { this.realName = realName; }
	public void setProfile(String profile) { this.profile = profile; }	
	public void setNameVariations(Vector<String> nameVariations) { this.nameVariations = nameVariations; }
	public void setAliases(Vector<String> aliases) { this.aliases = aliases; }
	public void setUrls(Vector<String> urls) { this.urls = urls; }
	public void setReleaseIds(Vector<String> releaseIds) { this.releaseIds = releaseIds; }
	public void setRemixReleaseIds(Vector<String> remixReleaseIds) { this.remixReleaseIds = remixReleaseIds; }
	public void setMixReleaseIds(Vector<String> mixReleaseIds) { this.mixReleaseIds = mixReleaseIds; }
	public void setAppearsOnReleases(Vector<String> appearsOnReleases) { this.appearsOnReleases = appearsOnReleases; }
	public void setPrimaryImageURL(String primaryImageURL) { this.primaryImageURL = primaryImageURL; }
	public void setImageURLs(Vector<String> imageURLs) { this.imageURLs = imageURLs; }

	/////////////
	// METHODS //
	/////////////
	
    public String toString() { return artistName; }    
    public String toStringFull() {
        StringBuffer result = new StringBuffer();
        result.append("ARTIST NAME=");
        result.append(artistName);
        result.append("\nPROFILE=");
        result.append(profile);
        result.append("\nREAL NAME=");
        result.append(realName);
        result.append("\nNAME VARIATIONS=");
        result.append(nameVariations);
        result.append("\nALIASES=");
        result.append(aliases);
        result.append("\nURLS=");
        result.append(urls);
        result.append("\n# RELEASES=");
        result.append(releaseIds.size());
        result.append("\nRELEASE IDS=");
        result.append(releaseIds);
        result.append("\n# REMIX RELEASES=");
        result.append(remixReleaseIds.size());
        result.append("\nREMIX RELEASE IDS=");
        result.append(remixReleaseIds);
        result.append("\n# MIX RELEASES=");
        result.append(mixReleaseIds.size());
        result.append("\nMIX RELEASE IDS=");
        result.append(mixReleaseIds);
        result.append("\n# APPEARANCE RELEASES=");
        result.append(appearsOnReleases.size());
        result.append("\nAPPEARANCE RELEASE IDS=");
        result.append(appearsOnReleases);
        result.append("\nPRIMARY IMAGE URL=");
        result.append(primaryImageURL);
        result.append("\nIMAGE URLS=");
        result.append(imageURLs);
        return result.toString();
    }    
    
    static public void main(String[] args) {
        try {
        	RapidEvolution3.loadLog4J();
            log.info(MiningAPIFactory.getDiscogsAPI().getArtist("3 Channels", false).toStringFull());            
        } catch (Exception e) {
            log.error("main(): error", e);
        }
    }
    
}
