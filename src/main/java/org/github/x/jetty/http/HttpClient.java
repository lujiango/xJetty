package org.github.x.jetty.http;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.github.x.jetty.conf.Config;
import org.github.x.jetty.conf.ZkClient;
import org.github.x.jetty.core.Entry;
import org.github.x.jetty.routing.Router;

/**
 * 
 * @author lujiango
 *
 */
public class HttpClient {
	private static final Logger LOG = Logger.getLogger(HttpClient.class);
	
	private static org.eclipse.jetty.client.HttpClient client;
	
	private static Router router;
	

	public HttpClient() {
		client = new org.eclipse.jetty.client.HttpClient();
		router = new Router(Config.getSelf().getZkClient());
	}

	@Entry(startup = -1000)
	public void doStart() {
		HttpClient client = new HttpClient();
		router.build();
		client.doStart1();
	}

	private void doStart1() {
		if (client != null) {
			try {
				client.start();
			} catch (Exception e) {
				LOG.fatal("xJetty http client start exception: ", e);
			}
		}
	}
}
