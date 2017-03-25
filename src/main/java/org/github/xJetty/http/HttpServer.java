package org.github.xJetty.http;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.session.SessionHandler;
import org.github.xJetty.annotation.Entry;
import org.github.xJetty.conf.Configuration;
import org.github.xJetty.utils.Constants;
/**
 * 
 * @author lujiango
 *
 */
public class HttpServer extends Server {
private static final Logger LOG = Logger.getLogger(HttpServer.class);
	
	public HttpServer(String ip, int port) {
		super(port);
	}
	
	@Entry(startup = -900)
	public void startup() {
		String tmpIp = Configuration.getAsString(Constants.XJETTY_LISTEN_IP, null);
		if (tmpIp == null) {
			LOG.fatal("Listen-ip is not set and xJetty will exit...");
		}
		if (!tmpIp.matches(Constants.IP_REGEX)) {
			LOG.fatal("Listen-ip pattern is unqualified and xJetty will exit...");
		}
		
		int tmpPort = Configuration.getAsInt(Constants.XJETTY_LISTEN_PORT, -1);
		
		HttpServer server = new HttpServer(tmpIp, tmpPort);
		HandlerList hl = new HandlerList();
		
	    hl.setHandlers(new Handler[] { new SessionHandler(), new HttpHandler() });
		server.setHandler(hl);
		try {
			server.start();
			server.join();
		} catch (Exception e) {
			LOG.fatal("xJetty http server start exception: ", e);
		}
	}
}
