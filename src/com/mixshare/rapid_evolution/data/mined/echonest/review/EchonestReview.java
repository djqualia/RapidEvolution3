package com.mixshare.rapid_evolution.data.mined.echonest.review;

import java.io.Serializable;
import java.util.Date;

import org.apache.log4j.Logger;

import com.echonest.api.v4.Review;

public class EchonestReview implements Serializable {

    static private Logger log = Logger.getLogger(EchonestReview.class);
    static private final long serialVersionUID = 0L;
	
    ////////////
    // FIELDS //
    ////////////
    
    private String url;
    private String imageUrl;
    private String site;
    private String name;
    private String release;
    private String reviewText;
    private Date dateFound;
    private Date dateReviewed;
    private String summary;
    
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
    public EchonestReview() { }
    public EchonestReview(Review review) {
    	url = review.getURL();
    	imageUrl = review.getImageURL();
    	release = review.getRelease();
    	reviewText = review.getReviewText();
    	name = review.getName();
    	dateFound = review.getDateFound();
    	dateReviewed = review.getDateReviewed();
    	summary = review.getSummary();
    }

    /////////////
    // GETTERS //
    /////////////

	public String getUrl() { return url; }
	public String getImageUrl() { return imageUrl; }
	public String getSite() { return site; }
	public String getName() { return name; }
	public String getRelease() { return release; }
	public String getReviewText() { return reviewText; }
	public Date getDateFound() { return dateFound; }
	public Date getDateReviewed() { return dateReviewed; }
	public String getSummary() { return summary; }		
        
	/////////////
	// SETTERS //
	/////////////
	
	public void setUrl(String url) {
		this.url = url;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	public void setSite(String site) {
		this.site = site;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setRelease(String release) {
		this.release = release;
	}
	public void setReviewText(String reviewText) {
		this.reviewText = reviewText;
	}
	public void setDateFound(Date dateFound) {
		this.dateFound = dateFound;
	}
	public void setDateReviewed(Date dateReviewed) {
		this.dateReviewed = dateReviewed;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	
	/////////////
	// METHODS //
	/////////////
	
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append(name);
		result.append(" (");
		result.append(url);
		result.append(")");
		return result.toString();
	}
	
	public boolean equals(Object o) {
		if (o instanceof EchonestReview) {
			EchonestReview eV = (EchonestReview)o;
			return eV.getUrl().equals(getUrl());
		}
		return false;
	}
	
	public int hashCode() { return url.hashCode(); }
	
}
