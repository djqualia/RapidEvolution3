package com.mixshare.rapid_evolution.util.inet.httpclient;

import java.io.IOException;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodRetryHandler;
import org.apache.commons.httpclient.NoHttpResponseException;

import com.mixshare.rapid_evolution.RE3Properties;

public class RE3HttpMethodRetryHandler implements HttpMethodRetryHandler {

	public boolean retryMethod(final HttpMethod method, final IOException exception, int executionCount) {
		if (executionCount >= 5) {
			// Do not retry if over max retry count
			return false;
		}
		if (RE3Properties.getBoolean("automatically_retry_failed_requests")) {
			if (exception instanceof NoHttpResponseException) {
				// Retry if the server dropped connection on us
				return true;
			}
			if (!method.isRequestSent()) {
				// Retry if the request has not been sent fully or
				// if it's OK to retry methods that have been sent
				return true;
			}
		}
		// otherwise do not retry
		return false;
	}
	
}
