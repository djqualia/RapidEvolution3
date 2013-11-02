package com.mixshare.rapid_evolution.data.mined.echonest.artist;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.echonest.api.v4.Artist;
import com.echonest.api.v4.Audio;
import com.echonest.api.v4.Biography;
import com.echonest.api.v4.EchoNestException;
import com.echonest.api.v4.Image;
import com.echonest.api.v4.Params;
import com.echonest.api.v4.Term;
import com.echonest.api.v4.Video;
import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.search.artist.ArtistIdentifier;
import com.mixshare.rapid_evolution.data.mined.MinedProfile;
import com.mixshare.rapid_evolution.data.mined.MinedProfileHeader;
import com.mixshare.rapid_evolution.data.mined.echonest.EchonestAPIWrapper;
import com.mixshare.rapid_evolution.data.mined.echonest.audio.EchonestAudio;
import com.mixshare.rapid_evolution.data.mined.echonest.biography.EchonestBiography;
import com.mixshare.rapid_evolution.data.mined.echonest.blogs.EchonestBlog;
import com.mixshare.rapid_evolution.data.mined.echonest.image.EchonestImage;
import com.mixshare.rapid_evolution.data.mined.echonest.news.EchonestNews;
import com.mixshare.rapid_evolution.data.mined.echonest.review.EchonestReview;
import com.mixshare.rapid_evolution.data.mined.echonest.video.EchonestVideo;
import com.mixshare.rapid_evolution.data.profile.common.image.InvalidImageException;
import com.mixshare.rapid_evolution.data.profile.common.link.InvalidLinkException;
import com.mixshare.rapid_evolution.data.profile.common.link.Link;
import com.mixshare.rapid_evolution.data.profile.common.link.VideoLink;
import com.mixshare.rapid_evolution.data.record.search.artist.ArtistRecord;
import com.mixshare.rapid_evolution.data.util.DegreeValue;

/**
 * As of now (11/18/2009), the news/reviews/blogs queries do not seem to work (cause 503 http response codes), therefore I commented them out...
 */
public class EchonestArtistProfile extends MinedProfile implements Serializable {

    static private Logger log = Logger.getLogger(EchonestArtistProfile.class);
    static private final long serialVersionUID = 0L;
    
    static private final int MAX_ARTIST_RESULTS = 99;
    static private final int MAX_SIMILAR_RESULTS = 99;
    
    static private int maxSimilarArtistsToQuery = RE3Properties.getInt("echonest_num_similar_to_retrieve");
    static private int maxSimilarArtistsPerQuery = RE3Properties.getInt("echonest_max_similar_per_query");
    static private int maxImagesToQuery = RE3Properties.getInt("echonest_num_images_to_retrieve");
    static private int maxImagesPerQuery = RE3Properties.getInt("echonest_max_images_per_query");
    static private int maxBiographiesToQuery = RE3Properties.getInt("echonest_num_biographies_to_retrieve");
    static private int maxBiographiesPerQuery = RE3Properties.getInt("echonest_max_biographies_per_query");
    static private int maxVideosToQuery = RE3Properties.getInt("echonest_num_videos_to_retrieve");
    static private int maxVideosPerQuery = RE3Properties.getInt("echonest_max_videos_per_query");
    static private int maxReviewsToQuery = RE3Properties.getInt("echonest_num_reviews_to_retrieve");
    static private int maxReviewsPerQuery = RE3Properties.getInt("echonest_max_reviews_per_query");
    static private int maxNewsToQuery = RE3Properties.getInt("echonest_num_news_to_retrieve");
    static private int maxNewsPerQuery = RE3Properties.getInt("echonest_max_news_per_query");
    static private int maxBlogsToQuery = RE3Properties.getInt("echonest_num_blogs_to_retrieve");
    static private int maxBlogsPerQuery = RE3Properties.getInt("echonest_max_blogs_per_query");
    static private int maxAudioToQuery = RE3Properties.getInt("echonest_num_audio_to_retrieve");
    static private int maxAudioPerQuery = RE3Properties.getInt("echonest_max_audio_per_query");
    
    ////////////
    // FIELDS //
    ////////////

    private boolean isValid = false;            
    private float hotness;
    private float familiarity;
    private Vector<String> similarArtistNames = new Vector<String>(); // preserves case of artist names
    private Map<String, Float> similarArtists = new LinkedHashMap<String, Float>();
    private Map<String, String> urls; // key is url type, value is url
    private Vector<EchonestVideo> videos = new Vector<EchonestVideo>();
    private Vector<EchonestReview> reviews = new Vector<EchonestReview>();
    private Vector<EchonestNews> news = new Vector<EchonestNews>();
    private Vector<EchonestBlog> blogs = new Vector<EchonestBlog>();
    private Vector<EchonestAudio> audio = new Vector<EchonestAudio>();
    private Vector<EchonestImage> images = new Vector<EchonestImage>();
    private Vector<EchonestBiography> biographies = new Vector<EchonestBiography>();
    private String artistName;
    private Vector<DegreeValue> tags = new Vector<DegreeValue>(); 
    
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
	public EchonestArtistProfile() {
    	super(new MinedProfileHeader(DATA_TYPE_ARTISTS, DATA_SOURCE_ECHONEST));
    }
    public EchonestArtistProfile(String artistName) {
        super(new MinedProfileHeader(DATA_TYPE_ARTISTS, DATA_SOURCE_ECHONEST));
        this.artistName = artistName;
        try {
       	 	if (log.isDebugEnabled())
       	 		log.debug("EchonestArtistProfile(): fetching artist=" + artistName);
       	 	
       	    Params p = new Params();
            p.add("name", artistName);
            p.add("results", MAX_ARTIST_RESULTS);       	 	
       	 	       	 	
            List<Artist> artists = EchonestAPIWrapper.getEchoNestAPI().searchArtists(p);
	        for (Artist artist : artists) {
	        	if (artist.getName().equalsIgnoreCase(artistName)) {
	        		
	        		// similar artists
	        		try {
		        		int numSimilar = 0;
		        		boolean done = false;
		        		while ((numSimilar < maxSimilarArtistsToQuery) && !done) {
		        			
		        			List<Artist> similars = artist.getSimilar(MAX_SIMILAR_RESULTS);
		        			for (Artist simArtist : similars) {
		        				String similarArtistName = simArtist.getName();
		        				float similarScore = 1.0f; //(float)simArtist.get();
		        				if (similarArtists.containsKey(similarArtistName.toLowerCase())) {
		        					done = true; // repeating itself...
		        				} else {
			        				if (log.isTraceEnabled())
			        					log.trace("EchonestArtistProfile(): found similar artist=" + similarArtistName + ", score=" + similarScore);
		        					similarArtists.put(similarArtistName.toLowerCase(), similarScore);
		        					similarArtistNames.add(similarArtistName);
		        					++numSimilar;
		        				}	        				
		        			}
		        			if (similars.size() < maxSimilarArtistsPerQuery)
		        				done = true;
		        			// NOTE: got an exception "Invalid parameter: "start" must be less than or equal to 0" on v0.3 of API, hence the line below 
		        			done = true;
		        		}	 
	        		} catch (Exception e) {
	        			log.debug("EchonestArtistProfile(): error=" + e);
	        		}
	        		
	        		// images
	        		try {
		        		int numImages = 0;
		        		boolean done = false;
		        		while ((numImages < maxImagesToQuery) && !done) {
		        			List<Image> fetchedImages = artist.getImages(numImages, maxImagesPerQuery);
		        			for (Image fetchedImage : fetchedImages) {
		        				EchonestImage echonestImage = new EchonestImage(fetchedImage);
		        				if (images.contains(echonestImage)) {
		        					done = true; // repeating itself...
		        				} else {
			        				if (log.isTraceEnabled())
			        					log.trace("EchonestArtistProfile(): found image=" + echonestImage);
			        				images.add(echonestImage);		        					
		        					++numImages;
		        				}	        				
		        			}
		        			if (fetchedImages.size() < maxImagesPerQuery)
		        				done = true;
		        		}	 
	        		} catch (Exception e) {
	        			log.debug("EchonestArtistProfile(): error=" + e);
	        		}	        		
	        		
	        		// biographies
	        		try {
		        		int numBiographies = 0;
		        		boolean done = false;
		        		while ((numBiographies < maxBiographiesToQuery) && !done) {
		        			List<Biography> fetchedBiographies = artist.getBiographies(numBiographies, maxBiographiesPerQuery);
		        			for (Biography fetchedBiography : fetchedBiographies) {		        				
		        				EchonestBiography echonestBiography = new EchonestBiography(fetchedBiography);
		        				if (biographies.contains(echonestBiography)) {
		        					done = true; // repeating itself...
		        				} else {
			        				if (log.isTraceEnabled())
			        					log.trace("EchonestArtistProfile(): found biography=" + echonestBiography);
			        				biographies.add(echonestBiography);		        					
		        					++numBiographies;
		        				}	        				
		        			}
		        			if (fetchedBiographies.size() < maxBiographiesPerQuery)
		        				done = true;
		        		}	 
	        		} catch (Exception e) {
	        			log.debug("EchonestArtistProfile(): error=" + e);
	        		}	        		
	        			        		
	        		// urls
	        		try {	        			
	        			urls = artist.getUrls();
	        			if (log.isTraceEnabled())
	        				log.trace("EchonestArtistProfile(): urls=" + urls);
	        		} catch (Exception e) {
	        			log.debug("EchonestArtistProfile(): error=" + e);
	        		}
	        		
	        		// videos
	        		if (RE3Properties.getBoolean("echonest_enable_videos_query")) {
	    				try {
			        		int numVideos = 0;
			        		boolean done = false;
			        		while ((numVideos < maxVideosToQuery) && !done) {		        			
			        			List<Video> documentVideos = artist.getVideos(numVideos, maxVideosPerQuery);
			        			for (Video video : documentVideos) {
			        				EchonestVideo videoProfile = new EchonestVideo(video);
			        				if (!videos.contains(videoProfile)) {
			        					if (log.isTraceEnabled())
			        						log.trace("EchonestArtistProfile(): added video=" + videoProfile);
			        					videos.add(videoProfile);
			        					++numVideos;
			        				} else {
			        					done = true; // repeating itself
			        				}        				
			        			}
			        			if (documentVideos.size() < maxVideosPerQuery)
			        				done = true;
			        		}
	    				} catch (Exception e) {
	    					log.debug("EchonestArtistProfile(): error=" + e);
	    				}
	        		}

    				/*
	        		// reviews
	        		try {
		        		int numReviews = 0;
		        		boolean done = false;
		        		while ((numReviews < maxReviewsToQuery) && !done) {
		        			DocumentList<Review> documentReviews = artistAPI.getReviews(artist, numReviews, maxReviewsPerQuery);	        			
		        			for (Review review : documentReviews.getDocuments()) {
		        				EchonestReview reviewProfile = new EchonestReview(review);
		        				if (!reviews.contains(reviewProfile)) {
		        					if (log.isTraceEnabled())
		        						log.trace("EchonestArtistProfile(): added review=" + reviewProfile);
		        					reviews.add(reviewProfile);
		        					++numReviews;
		        				} else {
		        					done = true; // repeating itself
		        				}        				
		        			}
		        			if (documentReviews.getDocuments().size() < maxReviewsPerQuery)
		        				done = true;
		        		}
	        		} catch (Exception e) {
	        			log.debug("EchonestArtistProfile(): error=" + e);
	        		}

	        		// news
	        		try {
		        		int numNews = 0;
		        		boolean done = false;
		        		while ((numNews < maxNewsToQuery) && !done) {
		        			DocumentList<News> documentNews = artistAPI.getNews(artist, numNews, maxNewsPerQuery);	        			
		        			for (News newsItem : documentNews.getDocuments()) {
		        				EchonestNews newsProfile = new EchonestNews(newsItem);
		        				if (!news.contains(newsProfile)) {
		        					if (log.isTraceEnabled())
		        						log.trace("EchonestArtistProfile(): added news=" + newsProfile);
		        					news.add(newsProfile);
		        					++numNews;
		        				} else {
		        					done = true; // repeating itself
		        				}        				
		        			}
		        			if (documentNews.getDocuments().size() < maxNewsPerQuery)
		        				done = true;
		        		}
	        		} catch (Exception e) {
	        			log.debug("EchonestArtistProfile(): error=" + e);
	        		}
	        		*/
	        		
	        		// hotness
	        		try {
		        		hotness = (float)artist.getHotttnesss();
		        		if (log.isTraceEnabled())
		        			log.trace("EchonestArtistProfile(): hotness=" + hotness);
	        		} catch (Exception e) {
	        			log.debug("EchonestArtistProfile(): error=" + e);
	        		}

	        		try {
		        		// familiarity
		        		familiarity = (float)artist.getFamiliarity();
		        		if (log.isTraceEnabled())
		        			log.trace("EchonestArtistProfile(): familiarity=" + familiarity);
	        		} catch (Exception e) {
	        			log.debug("EchonestArtistProfile(): error=" + e);
	        		}

	        		try {
		        		List<Term> terms = artist.getTerms();
		        		if (terms != null) {
		        			for (Term term : terms)
		        				tags.add(new DegreeValue(term.getName(), (float)term.getWeight(), DATA_SOURCE_ECHONEST));		        			
		        		}
	        		} catch (Exception e) {
	        			log.debug("EchonestArtistProfile(): error=" + e);
	        		}
	        		
	        		/*
	        		try {
		        		// blogs
		        		int numBlogs = 0;
		        		boolean done = false;
		        		while ((numBlogs < maxBlogsToQuery) && !done) {
		        			DocumentList<Blog> documentBlogs = artistAPI.getBlogs(artist, numBlogs, maxBlogsPerQuery);	        			
		        			for (Blog blogItem : documentBlogs.getDocuments()) {
		        				EchonestBlog blogProfile = new EchonestBlog(blogItem);
		        				if (!blogs.contains(blogProfile)) {
		        					if (log.isTraceEnabled())
		        						log.trace("EchonestArtistProfile(): added blog=" + blogProfile);
		        					blogs.add(blogProfile);
		        					++numBlogs;
		        				} else {
		        					done = true; // repeating itself
		        				}        				
		        			}
		        			if (documentBlogs.getDocuments().size() < maxBlogsPerQuery)
		        				done = true;
		        		}
	        		} catch (Exception e) {
	        			log.debug("EchonestArtistProfile(): error=" + e);
	        		}
	        		*/
	        		
	        		// audio
	        		try {
		        		int numAudio = 0;
		        		boolean done = false;
		        		while ((numAudio < maxAudioToQuery) && !done) {
		        			List<Audio> documentAudio = artist.getAudio(numAudio, maxAudioPerQuery);	        			
		        			for (Audio audioItem : documentAudio) {
		        				EchonestAudio audioProfile = new EchonestAudio(audioItem);
		        				if (!audio.contains(audioProfile)) {
		        					if (log.isTraceEnabled())
		        						log.trace("EchonestArtistProfile(): added audio=" + audioProfile);
		        					audio.add(audioProfile);
		        					++numAudio;
		        				} else {
		        					done = true; // repeating itself
		        				}        				
		        			}
		        			if (documentAudio.size() < maxAudioPerQuery)
		        				done = true;
		        		}	        		
	        		} catch (Exception e) {
	        			log.debug("EchonestArtistProfile(): error", e);
	        		}
	        		
	        	}	        	
	        }
	        if (similarArtists.size() > 0)
	        	isValid = true;
        } catch (EchoNestException ie) {
        	log.debug("EchonestArtistProfile(): echonest exception=" + ie);
        } catch (Exception e) {
        	log.error("EchonestArtistProfile(): error", e);
        }
    }    
    
    /////////////
    // GETTERS //
    /////////////
    
	public boolean isValid() { return isValid; }
	public float getHotness() { return hotness; }
	public float getFamiliarity() { return familiarity; }
	public Map<String, String> getUrls() { return urls; }
	public Vector<EchonestVideo> getVideos() { return videos; }
	public Vector<EchonestReview> getReviews() { return reviews; }
	public Vector<EchonestNews> getNews() { return news; }
	public Vector<EchonestBlog> getBlogs() { return blogs; }
	public Vector<EchonestAudio> getAudio() { return audio; }
    public Vector<DegreeValue> getTags() {
    	if (tags == null)
    		tags = new Vector<DegreeValue>();
		return tags;
	}
    
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
		Float result = similarArtists.get(artistName.toLowerCase());
		if (result != null)
			return result;
		return 0.0f;
	}
	public Vector<String> getSimilarArtistNames() { return similarArtistNames; }
	
	public String getUrl(String type) { return urls.get(type); }
	public Vector<Link> getLinks() {
		Vector<Link> result = new Vector<Link>();
		if (urls != null) {
			for (Entry<String, String> entry : urls.entrySet()) {
				try {
					result.add(new Link("", "", entry.getValue(), entry.getKey(), DATA_SOURCE_ECHONEST));
				} catch (InvalidLinkException il) { }
			}
		}
		return result;
	}
	
	public Vector<VideoLink> getVideoLinks() {
		Vector<VideoLink> result = new Vector<VideoLink>();
		for (EchonestVideo video : videos) {
			try {
				result.add(new VideoLink(video.getTitle(), "", video.getUrl(), video.getImageUrl(), video.getSite(), DATA_SOURCE_ECHONEST));
			} catch (InvalidLinkException il) { }
		}
		return result;
	}
	
	public Vector<EchonestImage> getImages() { return images; } // for serialization
	public Vector<com.mixshare.rapid_evolution.data.profile.common.image.Image> getREImages() {
		Vector<com.mixshare.rapid_evolution.data.profile.common.image.Image> images = new Vector<com.mixshare.rapid_evolution.data.profile.common.image.Image>();
		for (EchonestImage echonestImage : this.images) {
	    	try {
	    		images.add(new com.mixshare.rapid_evolution.data.profile.common.image.Image(echonestImage.getImageUrl(), DATA_SOURCE_ECHONEST));
	    	} catch (InvalidImageException ie) { }
		}
		return images;
	}
	
	public Map<String, Float> getSimilarArtists() {
		return similarArtists;
	}
	public void setSimilarArtists(Map<String, Float> similarArtists) {
		this.similarArtists = similarArtists;
	}
	public Vector<EchonestBiography> getBiographies() {
		return biographies;
	}
	public void setBiographies(Vector<EchonestBiography> biographies) {
		this.biographies = biographies;
	}
	public String getArtistName() {
		return artistName;
	}
	public void setArtistName(String artistName) {
		this.artistName = artistName;
	}
	public void setValid(boolean isValid) {
		this.isValid = isValid;
	}
	public void setHotness(float hotness) {
		this.hotness = hotness;
	}
	public void setFamiliarity(float familiarity) {
		this.familiarity = familiarity;
	}
	public void setSimilarArtistNames(Vector<String> similarArtistNames) {
		this.similarArtistNames = similarArtistNames;
	}
	public void setUrls(Map<String, String> urls) {
		this.urls = urls;
	}
	public void setVideos(Vector<EchonestVideo> videos) {
		this.videos = videos;
	}
	public void setReviews(Vector<EchonestReview> reviews) {
		this.reviews = reviews;
	}
	public void setNews(Vector<EchonestNews> news) {
		this.news = news;
	}
	public void setBlogs(Vector<EchonestBlog> blogs) {
		this.blogs = blogs;
	}
	public void setAudio(Vector<EchonestAudio> audio) {
		this.audio = audio;
	}
	public void setImages(Vector<EchonestImage> images) {
		this.images = images;
	}
	
    /////////////
    // METHODS //
    /////////////
    
	public String toString() { return artistName; }
	
    static public void main(String[] args) {
        try {
        	RapidEvolution3.loadLog4J();
            log.info("main(): similar=" + new EchonestArtistProfile("Daft Punk").getSimilarArtists());

            /*
	        ArtistAPI artistAPI = new ArtistAPI(EchonestAPIWrapper.API_KEY);	
	        List<Artist> artists = artistAPI.searchArtist("Justice", false);	
	        for (Artist artist : artists) {
	        	log.info("artist=" + artist + ", id=" + artist.getName());	        	
	        }
            */            
        } catch (Exception e) {
            log.error("main(): error", e);
        }        
    }
    
}
