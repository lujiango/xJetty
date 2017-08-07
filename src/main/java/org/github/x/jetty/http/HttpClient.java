package org.github.x.jetty.http;

import org.apache.log4j.Logger;
import org.github.x.jetty.core.Entry;

/**
 * 
 * @author lujiango
 *
 */
public class HttpClient {
	private static final Logger LOG = Logger.getLogger(HttpClient.class);
	private org.eclipse.jetty.client.HttpClient client;

	public HttpClient() {
		client = new org.eclipse.jetty.client.HttpClient();
	}

	@Entry(startup = -1000)
	public static void startup() {
		HttpClient client = new HttpClient();
		client.doStart();

	}

	private void doStart() {
		if (client != null) {
			try {
				client.start();
			} catch (Exception e) {
				LOG.fatal("xJetty http client start exception: ", e);
			}
		}
	}
}
