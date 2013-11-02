package com.mixshare.rapid_evolution.data.mined.twitter.status;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.Twitter.Status;
import winterwell.jtwitter.TwitterException.RateLimit;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.util.io.LineReader;

public class TwitterStatus implements Serializable {

    static private Logger log = Logger.getLogger(TwitterStatus.class);
    static private final long serialVersionUID = 0L;
    
	static private SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZ yyyy");
    
    ////////////
    // FIELDS //
    ////////////

    private String id;
	private long createdAt;
    private String text;
    private String source;
    private String userId;
    private int numMentions;
	private int numRetweets;
	private boolean incomplete = false;
	
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
    public TwitterStatus(LineReader lineReader) {
    	int version = Integer.parseInt(lineReader.getNextLine());
    }                		
    public TwitterStatus(Node node) {
        try {
        	XPathFactory factory = XPathFactory.newInstance();
        	XPath xPath = factory.newXPath();
        	
        	id = (String)xPath.evaluate("./id/text()", node);
        	createdAt = dateFormat.parse(xPath.evaluate("./created_at/text()", node)).getTime();
        	text = (String)xPath.evaluate("./text/text()", node);
        	source = (String)xPath.evaluate("./source/text()", node);
        	userId = (String)xPath.evaluate("./user/id/text()", node);
        	
        	Twitter twitter = new Twitter(RE3Properties.getEncryptedProperty("twitter_username"),RE3Properties.getEncryptedProperty("twitter_password"));
        	Status status = twitter.getStatus(Long.parseLong(id));
        	if (status != null) {
        		numMentions =  status.getMentions().size();
        		List<Status> retweets = twitter.getRetweets(status);
        		numRetweets = retweets.size();
        	}
        } catch (RateLimit rl) {
        	log.debug("TwitterStatus(): rate limit reached=" + rl);
        	incomplete = true;
        } catch (Exception e) {
        	log.error("TwitterStatus(): error", e);
        }
    }    
    
    /////////////
    // GETTERS //
    /////////////
    
    public String getId() { return id; }
	public long getCreatedAt() { return createdAt; }
	public String getText() { return text; }
	public String getSource() { return source; }
	public String getUserId() { return userId; }
    public int getNumMentions() { return numMentions; }
	public int getNumRetweets() { return numRetweets; }
	
	public boolean isValid() { return !incomplete; }

	/////////////
	// SETTERS //
	/////////////
	
	public void setId(String id) { this.id = id; }
	public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
	public void setText(String text) { this.text = text; }
	public void setSource(String source) { this.source = source; }
	public void setUserId(String userId) { this.userId = userId; }
	public void setNumMentions(int numMentions) { this.numMentions = numMentions; }
	public void setNumRetweets(int numRetweets) { this.numRetweets = numRetweets; }
	
    /////////////
    // METHODS //
    /////////////
    
	public String toString() { return text; }
	
	public String toStringFull() {
		StringBuffer result = new StringBuffer();
		result.append("\nID=");
		result.append(id);
		result.append("\nCREATED AT=");
		result.append(new Date(createdAt));
		result.append("\nTEXT=");
		result.append(text);
		result.append("\nSOURCE=");
		result.append(source);
		result.append("\nUSER ID=");
		result.append(userId);
		result.append("\n# MENTIONS=");
		result.append(numMentions);
		result.append("\n# RETWEETS=");
		result.append(numRetweets);
		return result.toString();
	}
	
}
