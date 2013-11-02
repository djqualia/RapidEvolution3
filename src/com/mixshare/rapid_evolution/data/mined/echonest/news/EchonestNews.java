package com.mixshare.rapid_evolution.data.mined.echonest.news;

import java.io.Serializable;
import java.util.Date;

import org.apache.log4j.Logger;

import com.echonest.api.v4.News;

public class EchonestNews implements Serializable {

    static private Logger log = Logger.getLogger(EchonestNews.class);
    static private final long serialVersionUID = 0L;
	
    ////////////
    // FIELDS //
    ////////////
    
    private String url;
    private String name;
    private Date dateFound;
    private Date datePosted;
    private String summary;
    
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
    public EchonestNews() { }
    public EchonestNews(News news) {
    	url = news.getURL();
    	name = news.getName();
    	dateFound = news.getDateFound();
    	datePosted = news.getDatePosted();
    	summary = news.getSummary();
    }

    /////////////
    // GETTERS //
    /////////////

	public String getUrl() { return url; }
	public String getName() { return name; }
	public Date getDateFound() { return dateFound; }
	public Date getDatePosted() { return datePosted; }
	public String getSummary() { return summary; }		

	/////////////
	// SETTERS //
	/////////////
	
	public void setUrl(String url) {
		this.url = url;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setDateFound(Date dateFound) {
		this.dateFound = dateFound;
	}
	public void setDatePosted(Date datePosted) {
		this.datePosted = datePosted;
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
		if (o instanceof EchonestNews) {
			EchonestNews eV = (EchonestNews)o;
			return eV.getUrl().equals(getUrl());
		}
		return false;
	}
	
	public int hashCode() { return url.hashCode(); }
	
}
