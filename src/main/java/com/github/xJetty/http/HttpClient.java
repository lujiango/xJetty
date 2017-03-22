package com.github.xJetty.http;

import org.apache.log4j.Logger;

import com.github.xJetty.annotation.Entry;

/**
 * 
 * @author lujiango
 *
 */
public class HttpClient {
	private static final Logger LOG = Logger.getLogger(HttpClient.class);
	private static org.eclipse.jetty.client.HttpClient httpClient = new org.eclipse.jetty.client.HttpClient();

	@Entry(startup = -1000)
	public void doStart() {
		httpClient.setConnectTimeout(5000);
		try {
			httpClient.start();
		} catch (Exception e) {
			LOG.fatal("start http client occur exception and system exit: ", e);
		}
	}
}
