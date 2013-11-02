package com.mixshare.rapid_evolution.data.profile.common.link;

import java.io.Serializable;
import java.net.URLDecoder;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;

import com.mixshare.rapid_evolution.util.StringUtil;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

public class Link implements Serializable {

	static private Logger log = Logger.getLogger(Link.class);	
    static private final long serialVersionUID = 0L;    
	
    protected String title;
	protected String description;
	protected String url;
	protected String type;
	protected byte dataSource;
	protected boolean disabled;
	
	public Link() { }
	public Link(String title, String description, String url, String type, byte dataSource) throws InvalidLinkException {
		this.title = title;
		if ((title == null) || (title.length() == 0)) {
			try {								
				Parser parser = new Parser();
				TagNameFilter filter = new TagNameFilter("TITLE");
                Parser.getConnectionManager().setRedirectionProcessingEnabled(true);
                Parser.getConnectionManager().setCookieProcessingEnabled(true);					
                parser.setResource(url);
                NodeList titleList = parser.parse(filter);
                if (titleList.size() > 0)
                	this.title = StringUtil.fixSpecialXMLCharacters(titleList.elementAt(0).toPlainTextString().trim());
			} catch (Exception e) {
				if (log.isDebugEnabled())
					log.debug("Link(): could not get title for url=" + url + ", exception=" + e.getMessage());				
			}
		}
        if ((this.title == null) || this.title.equals(""))
        	throw new InvalidLinkException();
		this.description = description;
		try {
			this.url = URLDecoder.decode(url, "UTF-8");
		} catch (Exception e) {
			this.url = url;
		}
		this.type = type;
		this.dataSource = dataSource;
		if (type.equals("itunes_url"))
			this.type = "iTunes";
		else if (type.equals("amazon_url"))
			this.type = "Amazon";
		else if (type.equals("mb_url"))
			this.type = "MusicBrainz";
		else if (type.equals("aolmusic_url"))
			this.type = "AOL Music";
		else if (type.equals("lastfm_url"))
			this.type = "Lastfm";
		else if (type.equals("discogs_url"))
			this.type = "Discogs";
		else if (type.equals("OfficialHomepage"))
			this.type = "Official Homepage";
		else if (type.equals("youtube"))
			this.type = "Youtube";
		else if (type.equals("countrytabs"))
			this.type = "Country Tabs";
		else if (type.equals("myspace"))
			this.type = "MySpace";
		if (type.equals("")) {
			if (url.toLowerCase().indexOf("idiomag.com") >= 0)
				this.type = "Idiomag";
			if (url.toLowerCase().indexOf("wikipedia.org") >= 0)
				this.type = "Wikipedia";
			if (url.toLowerCase().indexOf("myspace.com") >= 0)
				this.type = "MySpace";
			if (url.toLowerCase().indexOf("musicbrainz.org") >= 0)
				this.type = "MusicBrainz";
			if (url.toLowerCase().indexOf("amazon.com") >= 0)
				this.type = "Amazon";
			if (url.toLowerCase().indexOf("discogs.com") >= 0)
				this.type = "Discogs";
			if (url.toLowerCase().indexOf("last.fm") >= 0)
				this.type = "Lastfm";
			if (url.toLowerCase().indexOf("aol.com") >= 0)
				this.type = "AOL Music";
			if (url.toLowerCase().indexOf("youtube.com") >= 0)
				this.type = "Youtube";
			if (url.toLowerCase().indexOf("imdb.com") >= 0)
				this.type = "IMDb";
			if (url.toLowerCase().indexOf("myspace.com") >= 0)
				this.type = "MySpace";
		}
	}
	public Link(LineReader lineReader) {
		int version = Integer.parseInt(lineReader.getNextLine());
		title = lineReader.getNextLine();
		description = lineReader.getNextLine();
		url = lineReader.getNextLine();
		type = lineReader.getNextLine();
		dataSource = Byte.parseByte(lineReader.getNextLine());
		disabled = Boolean.parseBoolean(lineReader.getNextLine());
	}

	public String getTitle() { return title; }
	public String getDescription() { return description; }
	public String getUrl() { return url; }
	public String getType() { return type; }
	public byte getDataSource() { return dataSource; }
	public boolean isDisabled() { return disabled; }
	
	public void setTitle(String title) { this.title = title; }
	public void setDescription(String description) { this.description = description; }
	public void setUrl(String url) { this.url = url; }
	public void setType(String type) { this.type = type; }
	public void setDisabled(boolean disabled) { this.disabled = disabled; }
	// for serialization
	public void setDataSource(byte dataSource) { this.dataSource = dataSource; }
	
	public String toString() { return title + " (" + url + ")"; }
	
	public boolean equals(Object o) {
		if (o instanceof Link) {
			Link l = (Link)o;
			return url.equals(l.url);
		}
		return false;
	}
	
	public int hashCode() { return url.hashCode(); }
	
    public void write(LineWriter writer) {
    	writer.writeLine("1"); // version    
    	writer.writeLine(title);
    	writer.writeLine(description);
    	writer.writeLine(url);
    	writer.writeLine(type);
    	writer.writeLine(dataSource);
    	writer.writeLine(disabled);
    }
    
	static public void main(String[] args) {
		try {
	        PropertyConfigurator.configureAndWatch("log4j.properties");        
	        new Link("", "", "http://en.wikipedia.org/wiki/Aphex_Twin", "", (byte)0);	        
		} catch (Exception e) {
			log.error("main(): error", e);
		}
	}
	
}
