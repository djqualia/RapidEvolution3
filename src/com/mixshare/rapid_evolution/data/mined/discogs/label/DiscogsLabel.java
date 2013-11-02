package com.mixshare.rapid_evolution.data.mined.discogs.label;

import java.io.Serializable;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.mined.MiningAPIFactory;
import com.mixshare.rapid_evolution.data.mined.discogs.DiscogsException;
import com.mixshare.rapid_evolution.data.mined.util.WebHelper;
import com.mixshare.rapid_evolution.util.StringUtil;

public class DiscogsLabel implements Serializable {

    static private Logger log = Logger.getLogger(DiscogsLabel.class);
    static private final long serialVersionUID = 0L;    
    
    static public String[] badSuffixes = new String[] { " - cds and vinyl at discogs", " discography at discogs", " at discogs", " discography" }; // put longer strings first
    
    ////////////
    // FIELDS //
    ////////////
    
    private String labelName = "";
    private String profile = "";
    private Vector<String> urls = new Vector<String>();
    private Vector<String> releaseIds = new Vector<String>();
    private String primaryImageURL = "";
    private Vector<String> imageURLs = new Vector<String>();
    private String contactInfo = "";
    private String parentLabel = "";
    private Vector<String> subLabels = new Vector<String>();
    private Map<String, Object> formats = new HashMap<String, Object>();
    
    //////////////////
    // CONSTRUCTORS //
    //////////////////
    
    public DiscogsLabel() { }
    public DiscogsLabel(Document doc) throws DiscogsException {
        try {
            XPathFactory factory = XPathFactory.newInstance();
            XPath xPath = factory.newXPath();  
            
            // LABEL NAME
            labelName = (String)xPath.evaluate("//label/name/text()", doc);
            // CONTACT INFO
            contactInfo = (String)xPath.evaluate("//label/contactinfo/text()", doc);
            // PROFILE
            profile = (String)xPath.evaluate("//label/profile/text()", doc);
            // URLS
            Object urlsObj = xPath.evaluate("//label/urls/url/text()", doc, XPathConstants.NODESET);
            NodeList urlNodes = (NodeList) urlsObj;
            for (int i = 0; i < urlNodes.getLength(); i++)
                urls.add(urlNodes.item(i).getNodeValue().trim());
            // PARENT LABEL
            parentLabel = (String)xPath.evaluate("//label/parentLabel/text()", doc);
            if (parentLabel == null) parentLabel = "";
            // SUB LABELS
            Object subLabelsObj = xPath.evaluate("//label/sublabels/label/text()", doc, XPathConstants.NODESET);
            NodeList subLabelNodes = (NodeList) subLabelsObj;
            for (int i = 0; i < subLabelNodes.getLength(); i++)
                subLabels.add(subLabelNodes.item(i).getNodeValue().trim());
            // RELEASE IDS
            Object releasesObj = xPath.evaluate("//label/releases/release", doc, XPathConstants.NODESET);
            NodeList releaseNodes = (NodeList) releasesObj;
            for (int i = 0; i < releaseNodes.getLength(); i++) {
                try {
                	String releaseId = releaseNodes.item(i).getAttributes().getNamedItem("id").getTextContent();
                	if ((releaseId != null) && (releaseId.equals("None"))) {
                		if (log.isDebugEnabled())
                			log.debug("invalid release 'None' found for label=" + labelName);
                	} else {
                		releaseIds.add(releaseId);
                	}
                } catch (Exception e) { }
            }
            /*
             * This info is not available via HTTP so we're hiding it since it's not reliable...
            // ARTISTS
            Object artistsObj = xPath.evaluate("//label/releases/release/artist/text()", doc, XPathConstants.NODESET);
            NodeList artistNodes = (NodeList) artistsObj;
            for (int i = 0; i < artistNodes.getLength(); i++)
                artists.put(artistNodes.item(i).getNodeValue(),  null);
             */
            // FORMATS
            Object formatsObj = xPath.evaluate("//label/releases/release/format/text()", doc, XPathConstants.NODESET);
            NodeList formatNodes = (NodeList) formatsObj;
            for (int i = 0; i < formatNodes.getLength(); i++)
                formats.put(formatNodes.item(i).getNodeValue(),  null);            
            // IMAGE URLS
            Object imagesObj = xPath.evaluate("//label/images/image", doc, XPathConstants.NODESET);
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

    public DiscogsLabel(String httpData) {
        // LABEL NAME
        String prefix1 = "<title>";
        String suffix1 = "</title>";
        int startIndex = httpData.indexOf(prefix1);
        int endIndex = httpData.indexOf(suffix1, startIndex + prefix1.length());
        labelName = StringUtil.stripHtml(httpData.substring(startIndex + prefix1.length(), endIndex));
        // CONTACT INFO
        prefix1 = "<div class=\"head\">Contact&nbsp;Info:</div><div class=\"content\">";
        suffix1 = "</div>";
        startIndex = httpData.indexOf(prefix1);
        if (startIndex >= 0) {
            endIndex = httpData.indexOf(suffix1, startIndex + prefix1.length());
            contactInfo = StringUtil.stripHtml(httpData.substring(startIndex + prefix1.length(), endIndex));
        }
        // PROFILE
        prefix1 = "id=\"profile\">";
        suffix1 = "</div>";
        startIndex = httpData.indexOf(prefix1);
        if (startIndex >= 0) {
            endIndex = httpData.indexOf(suffix1, startIndex + prefix1.length());
            profile = StringUtil.stripHtml(httpData.substring(startIndex + prefix1.length(), endIndex));
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
        // PARENT LABEL
        prefix1 = "<div class=\"head\">Parent&nbsp;Label:</div><div class=\"content\">";
        suffix1 = "</div>";
        startIndex = httpData.indexOf(prefix1);
        if (startIndex >= 0) {
            endIndex = httpData.indexOf(suffix1, startIndex + prefix1.length());
            parentLabel = StringUtil.stripHtml(httpData.substring(startIndex + prefix1.length(), endIndex));
        }
        if (parentLabel == null) parentLabel = "";
        // SUB LABELS (1)
        prefix1 = "<div class=\"head\">Sublabel:</div><div class=\"content\">";
        suffix1 = "</div>";
        startIndex = httpData.indexOf(prefix1);
        if (startIndex >= 0) {
            endIndex = httpData.indexOf(suffix1, startIndex + prefix1.length());
            subLabels.add(StringUtil.stripHtml(httpData.substring(startIndex + prefix1.length(), endIndex)));
        }
        // SUB LABELS (multiple)
        prefix1 = "<div class=\"head\">Sublabels:</div><div class=\"content\">";
        suffix1 = "</div>";
        startIndex = httpData.indexOf(prefix1);
        if (startIndex >= 0) {
            endIndex = httpData.indexOf(suffix1, startIndex + prefix1.length());
            String subLabelsStr = httpData.substring(startIndex + prefix1.length(), endIndex).trim();
            subLabels = StringUtil.getAllInBetweens(subLabelsStr, "\">", "</a>");
        }
        // RELEASE IDS
        releaseIds = StringUtil.getAllInBetweens(httpData, "/release/", "\">");
        for (int i = 0; i < releaseIds.size(); ++i) {
            boolean success = false;
            try {
                Integer.parseInt(releaseIds.get(i).toString());
                success = true;
            } catch (Exception e) { }
            if (!success) {
                releaseIds.remove(i);
                --i;
            }
        }
        // IMAGE URLS
        try {
            String imagesURL = "http://www.discogs.com/viewimages?label=" + URLEncoder.encode(getName(), "UTF-8");  
            String imagesData = WebHelper.getTextResponseFromURL(imagesURL);
            if (imagesData != null) {
                prefix1 = "<div class=\"main\">";
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
            if (imageURLs.size() == 0) {
	            prefix1 = "http://www.discogs.com/image/";
	            suffix1 = "\"";
	            startIndex = httpData.indexOf(prefix1);
	            if (startIndex >= 0) {
	            	endIndex = httpData.indexOf(suffix1, startIndex + prefix1.length());
	            	String imageURL = prefix1 + httpData.substring(startIndex + prefix1.length(), endIndex);
	            	imageURL = StringUtil.replace(imageURL, "s.dsimg.com", "www.discogs.com");
	            	if (!imageURLs.contains(imageURL))
	            		imageURLs.add(imageURL);
	            }
            }
            if (imageURLs.size() > 0)
                primaryImageURL = (String)imageURLs.get(0);
            
        } catch (Exception e) {
            log.error("DiscogsLabel(): error", e);
        }
        // MULTIPLE PAGES
        prefix1 = "<span class=\"pagelinkgrey\">";
        suffix1 = "class=\"pagelinknorm\">Next &gt;";
        startIndex = httpData.indexOf(prefix1);
        if (startIndex >= 0) {
        	// multiple pages
        	// determine max page
        	endIndex = httpData.indexOf(suffix1, startIndex + prefix1.length());
        	String subString = httpData.substring(startIndex, endIndex);            	
        	prefix1 = "class=\"pagelink\">";
        	suffix1 = "</a>";
        	startIndex = subString.lastIndexOf(prefix1);
        	if (startIndex >= 0) {
        		endIndex = subString.indexOf(suffix1, startIndex + prefix1.length());
        		String lastPage = subString.substring(startIndex + prefix1.length(), endIndex);
        		if (log.isDebugEnabled())            			
        			log.debug("DiscogsLabel(): # pages=" + lastPage);
        		int lastPageNum = Integer.parseInt(lastPage);
        		for (int i = 2; i <= lastPageNum; ++i) {
        			String subPage = MiningAPIFactory.getDiscogsAPI().getLabelSubPage(this.getName(), i);
        			if (subPage != null) {
        				 Vector<String> additionalReleaseIds = StringUtil.getAllInBetweens(subPage, "/release/", "\">");
        			        for (int p = 0; p < additionalReleaseIds.size(); ++p) {
        			            boolean success = false;
        			            try {
        			                Integer.parseInt(additionalReleaseIds.get(p).toString());
        			                success = true;
        			            } catch (Exception e) { }
        			            if (!success) {
        			            	additionalReleaseIds.remove(p);
        			                --p;
        			            } else {            			            	
        			            	releaseIds.add(additionalReleaseIds.get(p));
        			            }
        			       }
        			    
        			}
        		}            		
        	}
        }                
        // TODO: ARTISTS
        // TODO: FORMATS
    }
            
    /////////////
    // GETTERS //
    /////////////
    
    public String getName() {
    	if (labelName != null) {
    		for (int b = 0; b < badSuffixes.length; ++b) {
    			if (labelName.toLowerCase().endsWith(badSuffixes[b]))
    				return labelName.substring(0, labelName.length() - badSuffixes[b].length());
    		}
    	}    	
    	return labelName;
    }
    public String getContactInfo() { return contactInfo; }
    public String getProfile() { return profile; }
    public Vector<String> getURLs() { return urls; }
    public String getParentLabelName() { return parentLabel; }
    public Vector<String> getSubLabelNames() { return subLabels; }
    public Vector<String> getReleaseIDs() { return releaseIds; }    
    public Collection<String> getFormats() { return formats.keySet(); }        
    public Map<String, Object> getFormatsRAW() { return formats; }
    public String getPrimaryImageURL() { return primaryImageURL; }
    public Vector<String> getImageURLs() { return imageURLs; }
	public String getLabelName() {
		return labelName;
	}
	public Vector<String> getUrls() {
		return urls;
	}
	public Vector<String> getReleaseIds() {
		return releaseIds;
	}
	public String getParentLabel() {
		return parentLabel;
	}
	public Vector<String> getSubLabels() {
		return subLabels;
	}
    
    /////////////
    // SETTERS //
    /////////////
    
	public void setLabelName(String labelName) { this.labelName = labelName; }
	public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }
	public void setProfile(String profile) { this.profile = profile; }
	public void setUrls(Vector<String> urls) { this.urls = urls; }
	public void setParentLabel(String parentLabel) { this.parentLabel = parentLabel; }
	public void setSubLabels(Vector<String> subLabels) { this.subLabels = subLabels; }
	public void setReleaseIds(Vector<String> releaseIds) { this.releaseIds = releaseIds; }
	public void setFormats(Map<String, Object> formats) { this.formats = formats; }
	public void setPrimaryImageURL(String primaryImageURL) { this.primaryImageURL = primaryImageURL; }
	public void setImageURLs(Vector<String> imageURLs) { this.imageURLs = imageURLs; }

	/////////////
	// METHODS //
	/////////////
	
    public String toString() { return labelName; }
    public String toStringFull() {
        StringBuffer result = new StringBuffer();
        result.append("LABEL NAME=");
        result.append(labelName);
        result.append("\nCONTACT INFO=");
        result.append(contactInfo);
        result.append("\nPROFILE=");
        result.append(profile);
        result.append("\nURLS=");
        result.append(urls);
        result.append("\nPARENT LABEL=");
        result.append(parentLabel);
        result.append("\nSUB LABELS=");
        result.append(subLabels);
        result.append("\n# RELEASES=");
        result.append(releaseIds.size());
        result.append("\nRELEASE IDS=");
        result.append(releaseIds);
        result.append("\nFORMATS=");
        result.append(getFormats());
        result.append("\nPRIMARY IMAGE URL=");
        result.append(primaryImageURL);
        result.append("\nIMAGE URLS=");
        result.append(imageURLs);
        return result.toString();
    }	
    
    static public void main(String[] args) {
        try {
        	RapidEvolution3.loadLog4J();
            log.info("result=" + MiningAPIFactory.getDiscogsAPI().getLabel("Border Community", false));            
        } catch (Exception e) {
            log.error("main(): error", e);
        }
    }
    
}
