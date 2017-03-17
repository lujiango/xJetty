package com.github.xJetty.server;

import org.eclipse.jetty.server.Server;

import com.github.xJetty.annotation.Entry;

public class HttpServer {

	private static Server server = null;

	private static int port = -1;

	private static String ip = null;

	@Entry(startup = -900)
	public void process() {

	}

	private void start() {
		server = new Server(p);
		server.
	}
}
