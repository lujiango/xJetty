package org.github.xjetty.http.client;

import org.apache.log4j.Logger;
import org.github.xjetty.conf.Config;
import org.github.xjetty.core.Entry;
import org.github.xjetty.http.routing.Router;

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
		LOG.info("HTTPClient starting...");
        client.setConnectBlocking(false);
        client.setIdleTimeout(20000);// ���ж���ʱ��
        client.setConnectTimeout(5000);// ���ӳ�ʱʱ��
        client.setMaxRedirects(0); // ���д����ض���
		try {
			client.start();
		} catch (Exception e) {
			LOG.fatal("xJetty http client start exception: ", e);
		}
	}
}
