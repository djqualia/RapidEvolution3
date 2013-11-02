package com.mixshare.rapid_evolution.data.mined.echonest.blogs;

import java.io.Serializable;
import java.util.Date;

import org.apache.log4j.Logger;

import com.echonest.api.v4.Blog;

public class EchonestBlog implements Serializable {

    static private Logger log = Logger.getLogger(EchonestBlog.class);
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
    
    public EchonestBlog() { }
    public EchonestBlog(Blog blog) {
    	url = blog.getURL();
    	name = blog.getName();
    	dateFound = blog.getDateFound();
    	datePosted = blog.getDatePosted();
    	summary = blog.getSummary();
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
		if (o instanceof EchonestBlog) {
			EchonestBlog eV = (EchonestBlog)o;
			return eV.getUrl().equals(getUrl());
		}
		return false;
	}
	
	public int hashCode() { return url.hashCode(); }
	
}
