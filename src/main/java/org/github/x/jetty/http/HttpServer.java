package org.github.x.jetty.http;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.session.SessionHandler;
import org.github.x.jetty.conf.Config;
import org.github.x.jetty.core.Entry;
import org.github.x.jetty.utils.Consts;

/**
 * 
 * @author lujiango
 *
 */
public class HttpServer extends Server {
	private static final Logger LOG = Logger.getLogger(HttpServer.class);
	private static final String ADDRESS = "address";

	public HttpServer() {}

	public HttpServer(String ip, int port) {
		super(port);
	}

	@Entry(startup = -900)
	public void startup() {
		String tmpIp = Config.getAsString(ADDRESS, null);
		if (tmpIp == null) {
			LOG.fatal("Listen-ip is not set and xJetty will exit...");
		}
		if (!tmpIp.matches(Address.IP_REGEX)) {
			LOG.fatal("Listen-ip pattern is unqualified and xJetty will exit...");
		}

		int tmpPort = Config.getAsInt(Consts.XJETTY_LISTEN_PORT, -1);

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
