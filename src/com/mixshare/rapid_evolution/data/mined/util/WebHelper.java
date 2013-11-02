package com.mixshare.rapid_evolution.data.mined.util;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.util.StringUtil;
import com.mixshare.rapid_evolution.util.inet.httpclient.RE3GetMethod;
import com.mixshare.rapid_evolution.util.inet.httpclient.RE3HttpClient;

public class WebHelper {

    static private Logger log = Logger.getLogger(WebHelper.class);
	
    static public Document getDocumentResponseFromURL(String url) {    	
        Document result = null;
        GetMethod method = null;
        try {            
            if (log.isTraceEnabled())
                log.trace("getDocumentResponseFromURL(): url=" + url);
            HttpClient client = new RE3HttpClient();            
            method = new RE3GetMethod(url);
            int statusCode = client.executeMethod(method);
            if (statusCode == 200) {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                //factory.setNamespaceAware(true);
                DocumentBuilder builder = factory.newDocumentBuilder();
                InputStream inputStream = method.getResponseBodyAsStream();
                result = builder.parse(inputStream);                
                inputStream.close();                
            } else if ((statusCode == 404) || (statusCode == 400) || (statusCode == 403)) {
                return null;
            } else {
            	if (RE3Properties.getBoolean("automatically_retry_failed_requests")) {
	                if (log.isTraceEnabled())
	                	log.trace("getDocumentResponseFromURL(): response != 200, waiting, url=" + url + ", statusCode=" + statusCode);
	                method.releaseConnection();
	                method = null;
	                Thread.sleep(10000);
	                return getDocumentResponseFromURL(url);
            	} else {
            		return null;
            	}
            }
        } catch (org.apache.commons.httpclient.CircularRedirectException cre) {
        	if (log.isDebugEnabled())
        		log.debug("getDocumentResponseFromURL(): circular redirection exception=" + cre);        	        	        	
        } catch (java.lang.IllegalArgumentException iae) {        	
        	if (log.isDebugEnabled())
        		log.debug("getDocumentResponseFromURL(): invalid uri", iae);        	        	
        } catch (org.apache.commons.httpclient.NoHttpResponseException nre) {
        	if (log.isDebugEnabled())
        		log.debug("getDocumentResponseFromURL(): no http response exception=" + nre);        	        	
        } catch (java.io.UTFDataFormatException utfe) {
        	if (log.isDebugEnabled())
        		log.debug("getDocumentResponseFromURL(): UTF format exception=" + utfe);        	
        } catch (org.xml.sax.SAXParseException e) {
        	if (log.isDebugEnabled())
        		log.debug("getDocumentResponseFromURL(): parse error=" + e);
        } catch (java.net.ConnectException e) {
        	if (log.isDebugEnabled())
        		log.debug("getDocumentResponseFromURL(): parse error=" + e);
        } catch (Exception e) {
            log.error("getDocumentResponseFromURL(): error getting url=" + url, e);
        } finally {
        	if (method != null)
        		method.releaseConnection();
        }
        return result;
    }	
    
    static public String getTextResponseFromURL(String url) {    	
        String result = null;
        GetMethod method = null;
        try {            
            if (log.isTraceEnabled())
                log.trace("getDocumentResponseFromURL(): url=" + url);
            HttpClient client = new RE3HttpClient();            
            method = new RE3GetMethod(url);
            int statusCode = client.executeMethod(method);
            result = method.getResponseBodyAsString();
        } catch (java.net.ConnectException e) {
        	if (log.isDebugEnabled())
        		log.debug("getDocumentResponseFromURL(): parse error=" + e);
        } catch (Exception e) {
            log.error("getDocumentResponseFromURL(): error getting url=" + url, e);
        } finally {
        	if (method != null)
        		method.releaseConnection();
        }
        return result;
    }	    
	
    static public String getUniqueFilenameFromURL(String url) {
    	if (url != null) {
			int extensionIndex = url.lastIndexOf(".");		
			if (extensionIndex >= 0) {				
				String extension = url.substring(extensionIndex);
				String shortUrl = url.substring(0, extensionIndex);
				String httpPrefix = "http://";
				if (shortUrl.toLowerCase().startsWith(httpPrefix))
					shortUrl = shortUrl.substring(httpPrefix.length());
				return StringUtil.makeValidFilename(StringUtil.stripNonAlphaNumeric(shortUrl) + extension);
			}
    	}
    	return null;
    }
    
	static public boolean doesImageUrlExist(String url) {
		GetMethod method = null;
		try {
            HttpClient client = new RE3HttpClient();
            method = new RE3GetMethod(url);
            int statusCode = client.executeMethod(method);
            if (statusCode == 200)
            	return true;            
		} catch (Exception e) {
			log.error("doesImageUrlExist(): error", e);
		} finally {
			if (method != null)
				method.releaseConnection();
		}
		return false;
	}
	
}
