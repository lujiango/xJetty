package com.github.xJetty.http;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;

import com.github.xJetty.annotation.Entry;
/**
 * 
 * @author lujiango
 *
 */
public class HttpServer {
	private static final Logger LOG = Logger.getLogger(HttpServer.class);

	private static Server server = null;

	private static int port = -1;

	private static String ip = null;

	@Entry(startup = -900)
	public void doStart() {

	}

	private void start() {
		server = new Server(port);
		try {
			server.start();
		} catch (Exception e) {
			LOG.fatal("start http server occur exception and system exit: ", e);
		}
	}
}
