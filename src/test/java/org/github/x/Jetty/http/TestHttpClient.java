package org.github.x.Jetty.http;

import org.github.x.jetty.http.client.HttpClient;
import org.junit.Test;

public class TestHttpClient {
	
	@Test
	public void testStartup() {
		HttpClient.startup();
	}
}
