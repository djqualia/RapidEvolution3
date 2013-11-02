package com.mixshare.rapid_evolution.data.mined.idiomag.artist;

import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.search.artist.ArtistIdentifier;
import com.mixshare.rapid_evolution.data.mined.MinedProfile;
import com.mixshare.rapid_evolution.data.mined.MinedProfileHeader;
import com.mixshare.rapid_evolution.data.mined.MiningAPIFactory;
import com.mixshare.rapid_evolution.data.mined.idiomag.IdiomagAPIWrapper;
import com.mixshare.rapid_evolution.data.mined.util.WebHelper;
import com.mixshare.rapid_evolution.data.profile.common.image.Image;
import com.mixshare.rapid_evolution.data.profile.common.image.InvalidImageException;
import com.mixshare.rapid_evolution.data.profile.common.link.InvalidLinkException;
import com.mixshare.rapid_evolution.data.profile.common.link.Link;
import com.mixshare.rapid_evolution.data.profile.common.link.VideoLink;
import com.mixshare.rapid_evolution.data.record.search.artist.ArtistRecord;
import com.mixshare.rapid_evolution.data.util.DegreeValue;

public class IdiomagArtistProfile extends MinedProfile {

    static private Logger log = Logger.getLogger(IdiomagArtistProfile.class);
    static private final long serialVersionUID = 0L;
                
    ////////////
    // FIELDS //
    ////////////
    
    private boolean isValid = false;
    private String artistName;
    private Vector<String> recommendedArtistNames = new Vector<String>(); // preserves case of artist names
    private Map<String, Float> recommendedArtists = new LinkedHashMap<String, Float>();
    private Vector<DegreeValue> tags = new Vector<DegreeValue>();
    private Vector<String> photoUrls = new Vector<String>();
    private Vector<String> urls = new Vector<String>();
    private Vector<IdiomagArtistArticle> articles = new Vector<IdiomagArtistArticle>();
    private Vector<IdiomagArtistVideo> videos = new Vector<IdiomagArtistVideo>();
    
    //////////////////
    // CONSTRUCTORS //
    //////////////////
    
    public IdiomagArtistProfile() {
    	super(new MinedProfileHeader(DATA_TYPE_ARTISTS, DATA_SOURCE_IDIOMAG));
    }
    public IdiomagArtistProfile(String artistName) {
    	super(new MinedProfileHeader(DATA_TYPE_ARTISTS, DATA_SOURCE_IDIOMAG));
    	this.artistName = artistName;    	
    	try {    		
            // recommended
			StringBuffer recommendedURL = new StringBuffer();
			recommendedURL.append("http://www.idiomag.com/api/recommendation/artists/xml?key=");
			recommendedURL.append(IdiomagAPIWrapper.API_KEY);
			recommendedURL.append("&artists[]=");		
            String encodedArtistName = URLEncoder.encode(artistName, "UTF-8");			
            recommendedURL.append(encodedArtistName);
            if (log.isTraceEnabled())
            	log.trace("IdiomagArtistProfile(): fetching recommended from url=" + recommendedURL.toString());
            MiningAPIFactory.getIdiomagAPI().getRateController().startQuery();
            Document doc = WebHelper.getDocumentResponseFromURL(recommendedURL.toString());
            if (doc != null) {
            	isValid = true;
	            XPathFactory factory = XPathFactory.newInstance();
	            XPath xPath = factory.newXPath();	            
	            
	            Vector<String> artistNames = new Vector<String>();
	            Vector<Float> degrees = new Vector<Float>();
	            
	            // names
	            NodeList nameNodes = (NodeList)xPath.evaluate("//artists/artist/title", doc, XPathConstants.NODESET);
	            for (int i = 0; i < nameNodes.getLength(); i++) 
	            	artistNames.add(nameNodes.item(i).getTextContent());
	            // degrees
	            NodeList degreeNodes = (NodeList)xPath.evaluate("//artists/artist/value", doc, XPathConstants.NODESET);
	            for (int i = 0; i < degreeNodes.getLength(); i++) 
	            	degrees.add(Float.parseFloat(degreeNodes.item(i).getTextContent()) / 100.0f);
	            
	            int i = 0;
	            for (String recommendedArtistName : artistNames) {
	            	recommendedArtistNames.add(recommendedArtistName);
	            	recommendedArtists.put(recommendedArtistName.toLowerCase(), degrees.get(i));
	            	++i;
	            }	 	                   
            } else {
            	return;
            }
            
            // tags
			StringBuffer tagsURL = new StringBuffer();
			tagsURL.append("http://www.idiomag.com/api/artist/tags/xml?key=");
			tagsURL.append(IdiomagAPIWrapper.API_KEY);
			tagsURL.append("&artist=");		
			tagsURL.append(encodedArtistName);
            if (log.isTraceEnabled())
            	log.trace("IdiomagArtistProfile(): fetching tags from url=" + tagsURL.toString());
            doc = WebHelper.getDocumentResponseFromURL(tagsURL.toString());
            MiningAPIFactory.getIdiomagAPI().getRateController().startQuery();
            if (doc != null) {
	            XPathFactory factory = XPathFactory.newInstance();
	            XPath xPath = factory.newXPath();	            
	            
	            Vector<String> tagNames = new Vector<String>();
	            Vector<Float> degrees = new Vector<Float>();
	            
	            // names
	            NodeList nameNodes = (NodeList)xPath.evaluate("//profile/tags/tag/name", doc, XPathConstants.NODESET);
	            for (int i = 0; i < nameNodes.getLength(); i++) 
	            	tagNames.add(nameNodes.item(i).getTextContent());
	            // degrees
	            NodeList degreeNodes = (NodeList)xPath.evaluate("//profile/tags/tag/value", doc, XPathConstants.NODESET);
	            for (int i = 0; i < degreeNodes.getLength(); i++) 
	            	degrees.add(Float.parseFloat(degreeNodes.item(i).getTextContent()));
	            
	            int i = 0;
	            for (String tagName : tagNames) {
	            	tags.add(new DegreeValue(tagName, degrees.get(i), DATA_SOURCE_IDIOMAG));
	            	++i;
	            }	 	                   
            }    
            
            // photos
			StringBuffer photosURL = new StringBuffer();
			photosURL.append("http://www.idiomag.com/api/artist/photos/xml?key=");
			photosURL.append(IdiomagAPIWrapper.API_KEY);
			photosURL.append("&artist=");		
			photosURL.append(encodedArtistName);
            if (log.isTraceEnabled())
            	log.trace("IdiomagArtistProfile(): fetching photos from url=" + photosURL.toString());
            doc = WebHelper.getDocumentResponseFromURL(photosURL.toString());
            MiningAPIFactory.getIdiomagAPI().getRateController().startQuery();
            if (doc != null) {
	            XPathFactory factory = XPathFactory.newInstance();
	            XPath xPath = factory.newXPath();	            
	            
	            // photo urls
	            NodeList urlNodes = (NodeList)xPath.evaluate("//photos/photo/url", doc, XPathConstants.NODESET);
	            for (int i = 0; i < urlNodes.getLength(); i++) 
	            	photoUrls.add(urlNodes.item(i).getTextContent());	            
            }   
            
            // info
			StringBuffer infoURL = new StringBuffer();
			infoURL.append("http://www.idiomag.com/api/artist/info/xml?key=");
			infoURL.append(IdiomagAPIWrapper.API_KEY);
			infoURL.append("&artist=");		
			infoURL.append(encodedArtistName);
            if (log.isTraceEnabled())
            	log.trace("IdiomagArtistProfile(): fetching info from url=" + infoURL.toString());
            doc = WebHelper.getDocumentResponseFromURL(infoURL.toString());
            MiningAPIFactory.getIdiomagAPI().getRateController().startQuery();
            if (doc != null) {
	            XPathFactory factory = XPathFactory.newInstance();
	            XPath xPath = factory.newXPath();	            
	            
	            // urls
	            NodeList urlNodes = (NodeList)xPath.evaluate("//artist/links/url", doc, XPathConstants.NODESET);
	            for (int i = 0; i < urlNodes.getLength(); i++) 
	            	urls.add(urlNodes.item(i).getTextContent());	            
            }           
            
            // articles
			StringBuffer articlesURL = new StringBuffer();
			articlesURL.append("http://www.idiomag.com/api/artist/articles/xml?key=");
			articlesURL.append(IdiomagAPIWrapper.API_KEY);
			articlesURL.append("&artist=");		
			articlesURL.append(encodedArtistName);
            if (log.isTraceEnabled())
            	log.trace("IdiomagArtistProfile(): fetching articles from url=" + articlesURL.toString());
            doc = WebHelper.getDocumentResponseFromURL(articlesURL.toString());
            MiningAPIFactory.getIdiomagAPI().getRateController().startQuery();
            if (doc != null) {
	            XPathFactory factory = XPathFactory.newInstance();
	            XPath xPath = factory.newXPath();	            

	            // titles	            
	            Vector<String> titles = new Vector<String>();
	            NodeList titleNodes = (NodeList)xPath.evaluate("//articles/article/title", doc, XPathConstants.NODESET);
	            for (int i = 0; i < titleNodes.getLength(); i++) 
	            	titles.add(titleNodes.item(i).getTextContent());
	            
	            for (int i = 0; i < titles.size(); ++i) {
		            IdiomagArtistArticle article = new IdiomagArtistArticle();
		            article.setTitle(titles.get(i));
		            article.setUrl(xPath.evaluate("//articles/article[" + (i + 1) + "]/url/text()", doc));
		            article.setDescription(xPath.evaluate("//articles/article[" + (i + 1) + "]/description/text()", doc));
		            article.setAuthor(xPath.evaluate("//articles/article[" + (i + 1) + "]/author/text()", doc));
		            article.setSourceUrl(xPath.evaluate("//articles/article[" + (i + 1) + "]/sourceUrl/text()", doc));
		            article.setDate(xPath.evaluate("//articles/article[" + (i + 1) + "]/date/text()", doc));
		            articles.add(article);
	            }
            }      
            
            if (RE3Properties.getBoolean("idiomag_enable_videos_query")) {
	            // videos
				StringBuffer videosURL = new StringBuffer();
				videosURL.append("http://www.idiomag.com/api/artist/videos/xml?key=");
				videosURL.append(IdiomagAPIWrapper.API_KEY);
				videosURL.append("&artist=");		
				videosURL.append(encodedArtistName);
	            if (log.isTraceEnabled())
	            	log.trace("IdiomagArtistProfile(): fetching videos from url=" + videosURL.toString());
	            MiningAPIFactory.getIdiomagAPI().getRateController().startQuery();
	            doc = WebHelper.getDocumentResponseFromURL(videosURL.toString());
	            if (doc != null) {
		            XPathFactory factory = XPathFactory.newInstance();
		            XPath xPath = factory.newXPath();	            
	
		            // titles	            
		            Vector<String> titles = new Vector<String>();
		            NodeList titleNodes = (NodeList)xPath.evaluate("//tracks/track/title", doc, XPathConstants.NODESET);
		            for (int i = 0; i < titleNodes.getLength(); i++) 
		            	titles.add(titleNodes.item(i).getTextContent());
		            
		            for (int i = 0; i < titles.size(); ++i) {
			            IdiomagArtistVideo video = new IdiomagArtistVideo();
			            video.setTitle(titles.get(i));
			            video.setLocation(xPath.evaluate("//tracks/track[" + (i + 1) + "]/location/text()", doc));
			            video.setInfo(xPath.evaluate("//tracks/track[" + (i + 1) + "]/info/text()", doc));
			            video.setThumb(xPath.evaluate("//tracks/track[" + (i + 1) + "]/thumb/text()", doc));
			            videos.add(video);
		            }
	            }
            }
            
            if (log.isTraceEnabled())
            	log.trace("IdiomagArtistProfile(): recommendedArtists=" + recommendedArtists);
            if (log.isTraceEnabled())
            	log.trace("IdiomagArtistProfile(): tags=" + tags);
            if (log.isTraceEnabled())
            	log.trace("IdiomagArtistProfile(): photoUrls=" + photoUrls);
            if (log.isTraceEnabled())
            	log.trace("IdiomagArtistProfile(): articles=" + articles);
            if (log.isTraceEnabled())
            	log.trace("IdiomagArtistProfile(): videos=" + videos);
            if (log.isTraceEnabled())
            	log.trace("IdiomagArtistProfile(): urls=" + urls);
            
    	} catch (Exception e) {
    		log.error("IdiomagArtistProfile(): error", e);
    	}    	
    }
    
    /////////////
    // GETTERS //
    /////////////
    
    public boolean isValid() { return isValid; }
    
    public String getArtistName() { return artistName; }    
           
    public float getSimilarityWith(ArtistRecord artistRecord) {
    	float maxSimilarity = getSimilarityWith(artistRecord.getArtistName());
    	if (artistRecord.getDuplicateIds() != null) {
    		for (int dupId : artistRecord.getDuplicateIds()) {
    			ArtistIdentifier artistId = (ArtistIdentifier)Database.getArtistIndex().getIdentifierFromUniqueId(dupId);
    			if (artistId != null) {
    				float similarity = getSimilarityWith(artistId.getName());
    				if (similarity > maxSimilarity)
    					maxSimilarity = similarity;
    			}
    		}
    	}
    	return maxSimilarity;				    	
    }    
	public float getSimilarityWith(String artistName) {
		Float result = recommendedArtists.get(artistName.toLowerCase());
		if (result != null)
			return result;
		return 0.0f;
	}
	public Vector<String> getSimilarArtistNames() { return recommendedArtistNames; }
	
	public Vector<String> getPhotoURLs() { return photoUrls; }
    public Vector<Image> getImages() {
    	Vector<Image> result = new Vector<Image>();		
    	for (String imageUrl : photoUrls) {    		
        	if ((imageUrl != null) && (imageUrl.length() > 0)) {
        		try {
        			result.add(new Image(imageUrl, DATA_SOURCE_IDIOMAG));
        		} catch (InvalidImageException e) { }
         	}
    	}
    	return result;
    }
	
	public Vector<DegreeValue> getTagDegrees() { return tags; }
	public float getTagDegree(String tagName) {
		for (DegreeValue degree : tags) {
			if (tagName.equalsIgnoreCase(degree.getName()))
				return degree.getPercentage();			
		}
		return 0.0f;
	}
	
	public Vector<IdiomagArtistVideo> getVideos() { return videos; }
	
	public Vector<IdiomagArtistArticle> getArticles() { return articles; }
	
	public Vector<String> getURLs() { return urls; }
    public Vector<Link> getLinks() {
		Vector<Link> result = new Vector<Link>();
		for (String url : urls) {
			try {
				result.add(new Link("", "", url, "", DATA_SOURCE_IDIOMAG));
			} catch (InvalidLinkException il) { }
		}
		return result;    	
    }	
    
    public Vector<VideoLink> getVideoLinks() {
    	Vector<VideoLink> result = new Vector<VideoLink>();
		for (IdiomagArtistVideo video : videos) {
			try {
				result.add(new VideoLink(video.getTitle(), video.getInfo(), video.getLocation(), video.getThumb(), "", DATA_SOURCE_IDIOMAG));
			} catch (InvalidLinkException il) { }
		}
    	return result;
    }
	
	public Vector<String> getRecommendedArtistNames() {
		return recommendedArtistNames;
	}
    
    /////////////
    // SETTERS //
    /////////////
    
	public void setRecommendedArtistNames(Vector<String> recommendedArtistNames) {
		this.recommendedArtistNames = recommendedArtistNames;
	}
	public Map<String, Float> getRecommendedArtists() {
		return recommendedArtists;
	}
	public void setRecommendedArtists(Map<String, Float> recommendedArtists) {
		this.recommendedArtists = recommendedArtists;
	}
	public Vector<DegreeValue> getTags() {
		return tags;
	}
	public void setTags(Vector<DegreeValue> tags) {
		this.tags = tags;
	}
	public Vector<String> getPhotoUrls() {
		return photoUrls;
	}
	public void setPhotoUrls(Vector<String> photoUrls) {
		this.photoUrls = photoUrls;
	}
	public Vector<String> getUrls() {
		return urls;
	}
	public void setUrls(Vector<String> urls) {
		this.urls = urls;
	}
	public void setValid(boolean isValid) {
		this.isValid = isValid;
	}
	public void setArtistName(String artistName) {
		this.artistName = artistName;
	}
	public void setArticles(Vector<IdiomagArtistArticle> articles) {
		this.articles = articles;
	}
	public void setVideos(Vector<IdiomagArtistVideo> videos) {
		this.videos = videos;
	}
	
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
            log.info("result=" + new IdiomagArtistProfile("Lil Scrappy").getRecommendedArtists());            
        } catch (Exception e) {
            log.error("main(): error", e);
        }        
    }
 
}