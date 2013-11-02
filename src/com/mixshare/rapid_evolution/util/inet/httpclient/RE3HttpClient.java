package com.mixshare.rapid_evolution.util.inet.httpclient;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodRetryHandler;

public class RE3HttpClient extends HttpClient {

	private HttpMethodRetryHandler retryHandler = null;
	
	public RE3HttpClient() {
		super();
	}
		
}
