package org.github.xjetty.conf;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.github.xjetty.utils.Consts;

/**
 * 
 * @author lujiango
 *
 */
public class Config {
	private static final Config self = new Config();

	private Properties logProperties;

	private Properties confProperties;

	private ZkClient zkClient;
	
	

	public ZkClient getZkClient() {
		return zkClient;
	}

	public void setZkClient(ZkClient zkClient) {
		this.zkClient = zkClient;
	}

	public static Config getSelf() {
		return self;
	}

	private Config() {
		this.logProperties = new Properties();
		this.confProperties = new Properties();
	}

	public void initLogConfig() throws IOException {
		InputStream input = Config.class.getResourceAsStream("log4j_default.properties");
		logProperties.load(input);
		PropertyConfigurator.configure(logProperties);
	}

	public void setConfigFromZookeeper() {
		String zkPath = ZkClient.getZkAdress().getPath();
		List<String> children = ZkClient.getAllChildren(zkPath);
		Watcher cfgWatcher = new Watcher() {
			@Override
			public void process(WatchedEvent event) {
				LOG.info("event: " + event);
			}
		};

		String logRegex = "(/[^/]*){" + (zkPath.split("/").length - 1) + "}/log/log4j[^/]*";

		for (String key : children) {
			String value = ZkClient.getString(key, "");
			confProperties.put(key, value);
			ZkClient.watch(key, cfgWatcher, false);
			if (key.matches(logRegex)) {
				logProperties.put(key, value);
			}
		}
		LOG.info("Log configuretion will be changed");
		if (LOG.isDebugEnabled()) {
			StringBuffer sb = new StringBuffer();
			for (Object k : logProperties.keySet()) {
				sb.append(k);
				sb.append(':');
				sb.append(logProperties.get(k));
				sb.append('\n');
			}
			LOG.debug(sb);
		}
		LogManager.resetConfiguration();
		PropertyConfigurator.configure(logProperties);
	}

	public static Object get(String key, Object defaultValue) {
		return null;
	}

	public static int getAsInt(String key, int defaultValue) {
		return 0;
	}

	public static String getAsString(String key, String defaultValue) {
		return null;
	}

}
