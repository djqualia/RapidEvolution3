package com.mixshare.rapid_evolution.data.mined.discogs.release;

import java.io.Serializable;
import java.net.URLDecoder;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.mined.MiningAPIFactory;
import com.mixshare.rapid_evolution.data.mined.discogs.song.DiscogsSong;
import com.mixshare.rapid_evolution.data.mined.util.WebHelper;
import com.mixshare.rapid_evolution.util.StringUtil;

public class DiscogsRelease implements Serializable {

    static private Logger log = Logger.getLogger(DiscogsRelease.class);	
    static private final long serialVersionUID = 0L;
        
    ////////////
    // FIELDS //
    ////////////
    
    private int releaseId;
    private Vector<String> artistNames = new Vector<String>();
    private String title = "";
    private Integer yearReleased = null;
    private String released = null;
    private String country = "";
    private Vector<DiscogsReleaseLabelInstance> labels = new Vector<DiscogsReleaseLabelInstance>();
    private Vector<String> genres = new Vector<String>();
    private Vector<String> styles = new Vector<String>();    
    private Vector<DiscogsSong> tracks = new Vector<DiscogsSong>();
    private String primaryImageURL = "";
    private Vector<String> imageURLs = new Vector<String>();
    private DiscogsReleaseRatings ratings = null;
    private DiscogsReleaseRecommendations recommendations = null;
    private Vector<String> owners = new Vector<String>();
    private Vector<String> wishlist = new Vector<String>();
        
    //////////////////
    // CONSTRUCTORS //
    //////////////////
    
    public DiscogsRelease() { }
    public DiscogsRelease(Document doc, int releaseId) {
        try {
            XPathFactory factory = XPathFactory.newInstance();
            XPath xPath = factory.newXPath();  
            
            // RELEASE ID
            this.releaseId = releaseId;
            // ARTIST NAMES
            Object artistNamesObj = xPath.evaluate("//release/artists/artist/name/text()", doc, XPathConstants.NODESET);
            NodeList artistNodes = (NodeList) artistNamesObj;
            for (int i = 0; i < artistNodes.getLength(); i++)
                artistNames.add(artistNodes.item(i).getNodeValue());
            // TITLE
            title = (String)xPath.evaluate("//release/title/text()", doc);
            // YEAR
            released = (String)xPath.evaluate("//release/released/text()", doc);
            yearReleased = StringUtil.parseYear((String)xPath.evaluate("//release/released/text()", doc));
            // YEAR
            country = (String)xPath.evaluate("//release/country/text()", doc);
            // LABELS
            Object labelsObj = xPath.evaluate("//release/labels/label", doc, XPathConstants.NODESET);
            NodeList labelNodes = (NodeList) labelsObj;
            for (int i = 0; i < labelNodes.getLength(); i++) {
                try {
                    String name = labelNodes.item(i).getAttributes().getNamedItem("name").getTextContent();
                    String catno = labelNodes.item(i).getAttributes().getNamedItem("catno").getTextContent();
                    labels.add(new DiscogsReleaseLabelInstance(name, catno));
                } catch (Exception e) { }
            }
            // GENRES
            Object genresOBj = xPath.evaluate("//release/genres/genre/text()", doc, XPathConstants.NODESET);
            NodeList genreNodes = (NodeList) genresOBj;
            for (int i = 0; i < genreNodes.getLength(); i++)
                genres.add(genreNodes.item(i).getNodeValue().trim());
            // STYLES
            Object stylesObj = xPath.evaluate("//release/styles/style/text()", doc, XPathConstants.NODESET);
            NodeList styleNodes = (NodeList) stylesObj;
            for (int i = 0; i < styleNodes.getLength(); i++)
                styles.add(styleNodes.item(i).getNodeValue().trim());                     
            // TRACKS
            Object tracksObj = xPath.evaluate("//release/tracklist/track", doc, XPathConstants.NODESET);
            NodeList trackNodes = (NodeList) tracksObj;
            for (int i = 0; i < trackNodes.getLength(); i++) {
                try {
                    String position = "";
                    String title = "";
                    String duration = "";
                    Vector<String> artists = new Vector<String>();
                    Vector<String> remixers = new Vector<String>();
                    NodeList childNodes = trackNodes.item(i).getChildNodes();
                    for (int c = 0; c < childNodes.getLength(); ++c) {
                        Node child = childNodes.item(c);
                        if (child.getNodeName().equalsIgnoreCase("position"))
                            position = child.getTextContent();
                        else if (child.getNodeName().equalsIgnoreCase("title"))
                            title = child.getTextContent();
                        else if (child.getNodeName().equalsIgnoreCase("duration"))
                            duration = child.getTextContent();
                        else if (child.getNodeName().equalsIgnoreCase("artists")) {
                            artistNodes = child.getChildNodes();
                            for (int a = 0; a < artistNodes.getLength(); ++a) {
                                if (artistNodes.item(a).getNodeName().equals("artist")) {
                                    NodeList nameNodes = artistNodes.item(a).getChildNodes();
                                    for (int n = 0; n < nameNodes.getLength(); ++n) {
                                    	if (nameNodes.item(n).getNodeName().equals("name"))
                                    		artists.add(nameNodes.item(n).getTextContent().trim());
                                    }
                                }
                            }
                        } else if (child.getNodeName().equalsIgnoreCase("extraartists")) {                        	
                            NodeList extraArtistNodes = child.getChildNodes();
                            for (int a = 0; a < extraArtistNodes.getLength(); ++a) {
                                if (extraArtistNodes.item(a).getNodeName().equals("artist")) {
                                	String name = null;
                                	String role = null;
                                    NodeList extraNodes = extraArtistNodes.item(a).getChildNodes();
                                    for (int n = 0; n < extraNodes.getLength(); ++n) {
                                    	Node extraChild = extraNodes.item(n);
                                        if (extraChild.getNodeName().equalsIgnoreCase("name"))
                                        	name = extraChild.getTextContent();
                                        else if (extraChild.getNodeName().equalsIgnoreCase("role"))
                                        	role = extraChild.getTextContent();
                                    }
                                    if ((name != null) && (role != null)) {
                                    	if (role.equalsIgnoreCase("Remix"))
                                    		remixers.add(name);
                                    }
                                    	 
                                }
                            }
                        }
                    }
                    if (position.length() > 0)
                    	tracks.add(new DiscogsSong(position, title, duration, artists, remixers));
                } catch (Exception e) {
                    log.error("DiscogsRelease(): error", e);
                }
            }
            // IMAGE URLS
            Object imagesObj = xPath.evaluate("//release/images/image", doc, XPathConstants.NODESET);
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
            log.error("DiscogsRelease(): error", e);
        }
    }

    public DiscogsRelease(String httpData, int releaseId) {        
        // RELEASE ID
        this.releaseId = releaseId;
        // ARTIST NAMES                
        String prefix1 = "<div class=\"profile\">";
        String suffix1 = "</h1>";
        int startIndex = httpData.indexOf(prefix1);
        if (startIndex >= 0) {
            int endIndex = httpData.indexOf(suffix1, startIndex + prefix1.length());
            String artistStr = httpData.substring(startIndex + prefix1.length(), endIndex);
            prefix1 = "/artist/";
            suffix1 = "\">";
            artistNames = StringUtil.getAllInBetweens(artistStr, prefix1, suffix1);
            for (int i = 0; i < artistNames.size(); ++i) {
                try {
                    String name = (String)artistNames.get(i);
                    int index = name.indexOf("?anv=");
                    if (index > 0)
                        name = name.substring(0, index);
                    name = URLDecoder.decode(name, "UTF-8");
                    artistNames.set(i, name);
                } catch (Exception e) {
                    artistNames.remove(i);
                    --i;
                }
            }
        }        
        // TITLE
        prefix1 = "<div class=\"profile\">";
        suffix1 = "</h1>";
        startIndex = httpData.indexOf(prefix1);
        if (startIndex >= 0) {
            int endIndex = httpData.indexOf(suffix1, startIndex + prefix1.length());
            String titleStr = httpData.substring(startIndex + prefix1.length(), endIndex);
            prefix1 = "  - ";
            title = titleStr.substring(titleStr.indexOf(prefix1) + prefix1.length());            
        }
        // LABELS
        prefix1 = "<div class=\"head\">Label:</div><div class=\"content\">";
        suffix1 = "</div>";
        startIndex = httpData.indexOf(prefix1);
        if (startIndex >= 0) {
            int endIndex = httpData.indexOf(suffix1, startIndex + prefix1.length());
            String labelText = httpData.substring(startIndex + prefix1.length(), endIndex);
            prefix1 = "<a href=";
            suffix1 = "</a>";
            Vector<String> labelRefs = StringUtil.getAllInBetweens(labelText, prefix1, suffix1);
            String prefix2 = "<div class=\"head\">Catalog#:</div><div class=\"content\">";
            String suffix2 = "</div>";
            int startIndex2 = httpData.indexOf(prefix2);
            StringTokenizer tokenizer = null;
            if (startIndex2 >= 0) {
                int endIndex2 = httpData.indexOf(suffix2, startIndex2 + prefix2.length());
                String catText = httpData.substring(startIndex2 + prefix2.length(), endIndex2);
                tokenizer = new StringTokenizer(catText, ",");
            }           
            if (tokenizer != null) {
	            for (int l = 0; l < labelRefs.size(); ++l) {
	            	String labelRef = (String)labelRefs.get(l);
	            	if (!tokenizer.hasMoreTokens())
	            		break;
	            	String catId = tokenizer.nextToken().trim();
	            	prefix1 = "\">";
	            	startIndex = labelRef.indexOf(prefix1);
	            	if (startIndex >= 0) {
	            		String labelName = labelRef.substring(startIndex + prefix1.length());
	            		labels.add(new DiscogsReleaseLabelInstance(labelName, catId));
	            	}
	            }
            }
        }   
        
        // YEAR
        prefix1 = "<div class=\"head\">Released:</div><div class=\"content\">";
        suffix1 = "</div>";
        startIndex = httpData.indexOf(prefix1);
        if (startIndex >= 0) {
            int endIndex = httpData.indexOf(suffix1, startIndex + prefix1.length());
            String yearStr = StringUtil.stripHtml(httpData.substring(startIndex + prefix1.length(), endIndex));
            released = yearStr;
            yearReleased = StringUtil.parseYear(yearStr);
        }        
        // COUNTRY
        prefix1 = "<div class=\"head\">Country:</div><div class=\"content\">";
        suffix1 = "</div>";
        startIndex = httpData.indexOf(prefix1);
        if (startIndex >= 0) {
            int endIndex = httpData.indexOf(suffix1, startIndex + prefix1.length());
            country = StringUtil.stripHtml(httpData.substring(startIndex + prefix1.length(), endIndex));
        }        
        // GENRES
        prefix1 = "<div class=\"head\">Genre:</div><div class=\"content\">";
        suffix1 = "</div>";
        startIndex = httpData.indexOf(prefix1);
        if (startIndex >= 0) {
            int endIndex = httpData.indexOf(suffix1, startIndex + prefix1.length());
            String genresStr = StringUtil.stripHtml(httpData.substring(startIndex + prefix1.length(), endIndex));
            StringTokenizer tokenizer = new StringTokenizer(genresStr, "\n");
            while (tokenizer.hasMoreTokens()) {
                String genre = tokenizer.nextToken().trim();
                if (genre.endsWith(","))
                    genre = genre.substring(0, genre.length() - 1);
                if (!genre.equals(""))
                    genres.add(genre);
            }
        }
        // STYLES
        prefix1 = "<div class=\"head\">Style:</div><div class=\"content\">";
        suffix1 = "</div>";
        startIndex = httpData.indexOf(prefix1);
        if (startIndex >= 0) {
            int endIndex = httpData.indexOf(suffix1, startIndex + prefix1.length());
            String stylesStr = StringUtil.stripHtml(httpData.substring(startIndex + prefix1.length(), endIndex));
            StringTokenizer tokenizer = new StringTokenizer(stylesStr, "\n");
            while (tokenizer.hasMoreTokens()) {
                String style = tokenizer.nextToken().trim();
                if (style.endsWith(","))
                    style = style.substring(0, style.length() - 1);
                if (!style.equals(""))
                    styles.add(style);
            }        
        }
        // TRACKS
        prefix1 = "<h3>Tracklist</h3>";
        suffix1 = "</table>";
        startIndex = httpData.indexOf(prefix1);
        if (startIndex >= 0) {
            int endIndex = httpData.indexOf(suffix1, startIndex + prefix1.length());
            String tracksTable = httpData.substring(startIndex + prefix1.length(), endIndex);
            Vector<String> tracksHtml = StringUtil.getAllInBetweens(tracksTable, "<tr", "</tr>");
            for (int t = 0; t < tracksHtml.size(); ++t) {
                String position = "";
                String title = "";
                String duration = "";
                Vector<String> artists = new Vector<String>();
                String html = (String)tracksHtml.get(t);
                	
            	String nextLine = null;
            	if (t + 1 < tracksHtml.size())
            		nextLine = (String)tracksHtml.get(t + 1);                
                
                prefix1 = "<td class=\"track_pos\">";
                suffix1 = "</td>";
                startIndex = html.indexOf(prefix1);
                if (startIndex >= 0) {
                    endIndex = html.indexOf(suffix1, startIndex + prefix1.length());
                    position = html.substring(startIndex + prefix1.length(), endIndex).trim();
                }
                
                prefix1 = "<td class=\"track_artists\">";
                startIndex = html.indexOf(prefix1, endIndex);
                if (startIndex > 0) {
                    endIndex = html.indexOf(suffix1, startIndex + prefix1.length());
                    String artistHtml = html.substring(startIndex + prefix1.length(), endIndex);
                    artists = StringUtil.getAllInBetweens(artistHtml, "\">", "</a>");
                }      
                
                prefix1 = "<td class=\"track\">";
                suffix1 = "</td>";
                startIndex = html.indexOf(prefix1);
                if (startIndex >= 0) {
                    endIndex = html.indexOf(suffix1, startIndex + prefix1.length());
                    title = html.substring(startIndex + prefix1.length(), endIndex).trim();
                }

                prefix1 = "<td class=\"track_duration\" align=\"right\">";
                suffix1 = "</td>";
                startIndex = html.indexOf(prefix1);
                if (startIndex >= 0) {
                    endIndex = html.indexOf(suffix1, startIndex + prefix1.length());
                    duration = html.substring(startIndex + prefix1.length(), endIndex).trim();
                }	                
                	        
                Vector<String> remixers = new Vector<String>();
                if (nextLine != null) {
                	prefix1 = "&nbsp;&nbsp;Remix - ";
	            	startIndex = nextLine.indexOf(prefix1);
	            	if (startIndex >= 0) {
	            		prefix1 = "\">";
	            		suffix1 = "</a>";
	            		remixers = StringUtil.getAllInBetweens(nextLine.substring(startIndex), prefix1, suffix1);
	            	}
                }
            	
                if ((title.length() > 0) || (position.length() > 0))
                	tracks.add(new DiscogsSong(position, title, duration, artists, remixers));
            }            
        }
        // IMAGE URLS
        try {
            String imagesURL = "http://www.discogs.com/viewimages?release=" + String.valueOf(releaseId);
            String imagesData = WebHelper.getTextResponseFromURL(imagesURL);
            if (imagesData != null) {
                prefix1 = "<div class=\"main\">";
                suffix1 = "<p class=\"copyright\">";
                startIndex = imagesData.indexOf(prefix1);
                if (startIndex > 0) {
                    int endIndex = imagesData.indexOf(suffix1);
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
            log.error("DiscogsRelease(): error", e);
        }
        
    }
    
    /**
     * This constructor is used for unit testing only
     */
    public DiscogsRelease(int releaseId) {
    	this.releaseId = releaseId;     	
    }
    
    /**
     * This constructor is used for unit testing only
     */
    public DiscogsRelease(int releaseId, Vector<String> artistNames, String title, Integer year, String country,
    		Vector<DiscogsReleaseLabelInstance> labelInstances, Vector<String> genres, Vector<String> styles, Vector<DiscogsSong> tracks, Vector<String> imageURLs,
    		String primaryImageURL, DiscogsReleaseRatings ratings) {    	
    	this.releaseId = releaseId; 
    	setArtistNames(artistNames);
    	setTitle(title);
    	setYearReleased(year);
    	setCountry(country);
    	setLabels(labelInstances);
    	setGenres(genres);
    	setStyles(styles);
    	setTracks(tracks);
    	setImageURLs(imageURLs);
    	setPrimaryImageURL(primaryImageURL);
    	setRatings(ratings);
    }
    
    /////////////
    // GETTERS //
    /////////////    
    
    public String getArtistDescription() {
    	StringBuffer result = new StringBuffer();
    	for (int i = 0; i < artistNames.size(); ++i) {
    		result.append(artistNames.get(i));
    		if (i + 1 < artistNames.size())
    			result.append(" & ");
    	}
    	return result.toString();
    }
    public String getDescription() {
    	StringBuffer result = new StringBuffer();
    	result.append(getArtistDescription());
    	result.append(" - ");
    	result.append(title);
    	return result.toString();
    }
    public Vector<String> getArtistNames() { return artistNames; }
    public int getReleaseId() { return releaseId; }
    public String getTitle() { return title; }
    public Integer getYearReleased() { return yearReleased; }
    public Vector<DiscogsReleaseLabelInstance> getLabelInstances() { return labels; }
    public String getCountry() { return country; }
    public Vector<String> getGenres() { return genres; }
    public Vector<String> getStyles() { return styles; }
    public Vector<String> getUniqueStylesAndGenres() {
        Vector<String> result = new Vector<String>();
        for (int s = 0; s < styles.size(); ++s) {
        	String style = (String)styles.get(s);
            if (!result.contains(style))
            	result.add(style);
        }
        for (int g = 0; g < genres.size(); ++g) {
            String genre = (String)genres.get(g);
            if (!result.contains(genre))
                result.add(genre);
        }
        java.util.Collections.sort(result);
        return result;
    }
    public Vector<DiscogsSong> getSongs() { return tracks; }
    public String getPrimaryImageURL() { return primaryImageURL; }

    /**
     * Will return the imageURLs exactly as they were from Discogs.
     */
    public Vector<String> getImageURLs() { return imageURLs; }
    
    /**
     * These methods will strip out the discogs prefix from the URL for use with the AJAX proxy
     */
    public int getNumImageURLs() { return imageURLs.size(); }
    public String getImageURL(int index) { return (String)imageURLs.get(index); }
    
    public Vector<String> getOwners() { return owners; }
    public Vector<String> getWishlist() { return wishlist; }
    
    public int getNumTracks() { return tracks.size(); }

	public Vector<DiscogsReleaseLabelInstance> getLabels() { return labels; }

	public DiscogsReleaseRatings getRatings() { return ratings; }
	
	public String getReleased() { return released; }

	public Vector<DiscogsSong> getTracks() {
		return tracks;
	}
	
	/////////////
	// SETTERS //
	/////////////
	
	public void setLabels(Vector<DiscogsReleaseLabelInstance> labels) { this.labels = labels; }
	public void setArtistNames(Vector<String> artistNames) { this.artistNames = artistNames; }
	public void setTitle(String title) { this.title = title; }
	public void setYearReleased(Integer yearReleased) { this.yearReleased = yearReleased; }
	public void setCountry(String country) { this.country = country; }
	public void setGenres(Vector<String> genres) { this.genres = genres; }
	public void setStyles(Vector<String> styles) { this.styles = styles; }
	public void setTracks(Vector<DiscogsSong> tracks) { this.tracks = tracks; }
	public void setPrimaryImageURL(String primaryImageURL) { this.primaryImageURL = primaryImageURL; }
	public void setImageURLs(Vector<String> imageURLs) { this.imageURLs = imageURLs; }	
	public void setRatings(DiscogsReleaseRatings ratings) { this.ratings = ratings; }
	public void setReleased(String released) { this.released = released; }
	public DiscogsReleaseRecommendations getRecommendations() { return recommendations; }
	public void setRecommendations(DiscogsReleaseRecommendations recommendations) { this.recommendations = recommendations; }
    public void setOwnerlist(Vector<String> users) { owners = users; }
    public void setWishlist(Vector<String> users) { wishlist = users; }
	public void setReleaseId(int releaseId) {
		this.releaseId = releaseId;
	}

	public void setOwners(Vector<String> owners) {
		this.owners = owners;
	}

    
	/////////////
	// METHODS //
	/////////////
	
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append(artistNames);
        result.append(" - ");
        result.append(title);
        return result.toString();
    }    
    public String toStringFull() {
        StringBuffer result = new StringBuffer();
        result.append("\nRELEASE ID=");
        result.append(getReleaseId());
        result.append("\nARTIST NAMES=");
        result.append(artistNames);
        result.append("\nTITLE=");
        result.append(title);
        result.append("\nYEAR=");
        result.append(getYearReleased());
        result.append("\nLABELS=");
        result.append(getLabelInstances());
        result.append("\nCOUNTRY=");
        result.append(getCountry());
        result.append("\nGENRES=");
        result.append(genres);
        result.append("\nSTYLES=");
        result.append(styles);
        result.append("\nTRACKS=");
        result.append(tracks);
        result.append("\nPRIMARY IMAGE URL=");
        result.append(primaryImageURL);
        result.append("\nIMAGE URLS=");
        result.append(imageURLs);
        result.append("\nOWNERS=");
        result.append(owners);
        result.append("\nWISHLIST=");
        result.append(wishlist);
        result.append("\nRATINGS=");
        result.append(ratings);
        result.append("\nRECOMMENDATIONS=");
        result.append(recommendations);
        return result.toString();
    }
    
    static public void main(String[] args) {
        try {
        	RapidEvolution3.loadLog4J();
            log.info(MiningAPIFactory.getDiscogsAPI().getRelease(543091, false).toStringFull());
        } catch (Exception e) {
            log.error("main(): error", e);
        }        
    }
    
}
