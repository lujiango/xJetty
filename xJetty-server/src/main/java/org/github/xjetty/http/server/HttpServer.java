package org.github.xjetty.http.server;

import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.github.xjetty.conf.Config;
import org.github.xjetty.conf.ZkClient;
import org.github.xjetty.core.Entry;
import org.github.xjetty.core.xJetty;

/**
 * 
 * @author lujiango
 *
 */
public class HttpServer  {
	private static final Logger LOG = Logger.getLogger(HttpServer.class);
    private static Server server;
    
    private static ZkClient zkClient = Config.getSelf().getZkClient();

    
    
	@Entry(startup = -900)
	public void doStart() throws URISyntaxException {
		String addrInfo = Config.getAsString(Address.ADDRESS, null);
		Address addr = new Address(addrInfo);
		server = new Server();
		if (addr.http()) {
			ServerConnector connector = new ServerConnector(server);
			connector.setHost(addr.getHost());
			connector.setPort(addr.getPort());
		} else if (addr.https()) {
			SslContextFactory ctx = new SslContextFactory();
			SslConnectionFactory connector = new SslConnectionFactory();
		} else {
			throw new URISyntaxException(addr.getAddress(), "Not supported");
		}
		
		HandlerList hl = new HandlerList();
		hl.setHandlers(new Handler[] { new SessionHandler(), new HttpHandler() });
		server.setHandler(hl);
		try {
			server.start();
			server.join();
		} catch (Exception e) {
			LOG.fatal("xJetty http server start exception: ", e);
			System.exit(xJetty.XJETTY_EXIT_CODE);
		}
		
		registerServer();
		
	}
	
	public void registerServer() {
		LOG.info("Register server to routing-table.");
		Pattern p = Pattern.compile("(/[^/]*){2}");
        Matcher m = p.matcher(zkClient.getZkAddress().getPath());
        m.find();
        String engineRoomName = m.group();
        zkClient.touch(engineRoomName.concat("/routing-table"), new byte[0], CreateMode.PERSISTENT);
        LOG.info("Register server to routing-table successful.");
	}
}
