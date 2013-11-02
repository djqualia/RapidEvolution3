package com.mixshare.rapid_evolution.data.mined.idiomag.artist;

import java.io.Serializable;

public class IdiomagArtistArticle implements Serializable {

    static private final long serialVersionUID = 0L;

    private String title;
    private String url;
    private String description;
    private String author;
    private String sourceUrl;
    private String date;
    
    public IdiomagArtistArticle() { }

    public String toString() { return title + " (" + sourceUrl + ")"; }
    
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getSourceUrl() {
		return sourceUrl;
	}

	public void setSourceUrl(String sourceUrl) {
		this.sourceUrl = sourceUrl;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}
	
}
