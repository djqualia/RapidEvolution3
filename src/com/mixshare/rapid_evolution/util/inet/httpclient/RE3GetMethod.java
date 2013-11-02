package com.mixshare.rapid_evolution.util.inet.httpclient;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

public class RE3GetMethod extends GetMethod {
	
	public RE3GetMethod(String url) {
		super(url);		
		getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new RE3HttpMethodRetryHandler());
        addRequestHeader("Connection", "close");
        addRequestHeader("User-Agent", "Rapid Evolution 3");
	}

}
