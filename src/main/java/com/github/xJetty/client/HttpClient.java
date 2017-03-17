package com.github.xJetty.client;

public class HttpClient {
	private static org.eclipse.jetty.client.HttpClient httpClient = new org.eclipse.jetty.client.HttpClient();
	
	
	@Entry(startup = -1000)
	public void process() {
		httpClient.setConnectTimeout(5000);
		httpClient.start();
	}
	
	
	
}
