package org.github.xJetty.core;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooKeeper.States;
import org.github.xJetty.utils.SecurityUtils;

public final class ZkClient {
	private static final Logger LOG = Logger.getLogger(ZkClient.class);
	
	private static final int SESSION_TIMEOUT = 10000;
	
	private static final int CONNECT_INTERVAL = 5000;
	
	private static final int RECONNECT_THRESHOLD = 7;
	
	private static final String AUTH = "digest";
	
	private static final String ARGS_REGEX = "(\\w+):(\\w+)@([^/]+)(/.+)";
	
	private static ZooKeeper zookeeper = null;
	
	private static ConnectParam cacheParam = new ConnectParam("admin", "admin", "127.0.0.1", 2181, "/");
	
	
	private static Map<String, List<Watcher>> watchers = new ConcurrentHashMap<String, List<Watcher>>();
	
	private static Watcher connectWatcher = new Watcher() {
		private long preSessionId = 0L;

		@Override
		public void process(WatchedEvent event) {
			LOG.info("receive event: " + event);
			switch (event.getState()) {
				case SyncConnected: {
					if (preSessionId != getZookeeper().getSessionId()) {
						Map<String, List<Watcher>> watcherCopy = new ConcurrentHashMap<String, List<Watcher>>(watchers);
						for (Entry<String, List<Watcher>> entry : watcherCopy.entrySet()) {
							for (Watcher w : entry.getValue()) {
//								watch(entry.getKey(), w, true);
							}
						}
					}
					preSessionId = getZookeeper().getSessionId();

					break;
				}
				case Expired: {
					if (getZookeeper().getState() != States.CLOSED) {
						try {
							getZookeeper().close();
						} catch (Exception e) {
							LOG.warn("Close zookeeper exception: ", e);
						}
						setZookeeper(null);
					}
					connect(cacheParam);

					break;
				}
				default: {
					break;
				}
			}
		}
	};

	public static void connect(ConnectParam param) {
		if (getZookeeper() != null) {
			throw new IllegalStateException("ZkClient has connected to " + param.getAddress());
		}
		try {

			ZooKeeper zk = new ZooKeeper(param.getAddress(), SESSION_TIMEOUT, connectWatcher);

			while (zk.getState() != States.CONNECTED) {
				try {
					Thread.sleep(CONNECT_INTERVAL);
				} catch (InterruptedException e) {
					LOG.warn("Connect zookeeper exception: ", e);
				}
			}

			zk.addAuthInfo(AUTH, param.authUserPasswd());

			setZookeeper(zk);

		} catch (IOException e) {
			LOG.warn("Connect zookeeper failed. ", e);
			reconnect(cacheParam);
		}
	}

	public static void connect(String args) {
		Pattern p = Pattern.compile(ARGS_REGEX);
		Matcher m = p.matcher(args);
		if (!m.find() || m.groupCount() != 4) {
			LOG.error("Argument should be seen as zkuser:zkpasswd@zkip:zkport/zkpath, args:" + args);
			throw new IllegalArgumentException("Argument should be seen as zkuser:zkpasswd@zkip:zkport/zkpath");
		}
		cacheParam = new ConnectParam(m.group(0), m.group(1), m.group(2), Integer.parseInt(m.group(3)), m.group(4));
		connect(cacheParam);
	}

	public static void reconnect(ConnectParam param) {
		int retryTimes = 0;
		while (getZookeeper() == null || getZookeeper().getState() != States.CONNECTED) {
			try {
				Thread.sleep(CONNECT_INTERVAL);
				ZooKeeper zk = new ZooKeeper(param.getAddress(), SESSION_TIMEOUT, connectWatcher);
				if (retryTimes > RECONNECT_THRESHOLD) {
					throw new IllegalStateException("Reconnect zookeeper " + retryTimes + " times");
				}
				retryTimes++;
				zk.addAuthInfo(AUTH, param.authUserPasswd());
				setZookeeper(zk);
			} catch (Exception e) {
				LOG.warn("Reconnect zookeeper failed. ", e);
			}
		}
	}

	public static ZooKeeper getZookeeper() {
		return zookeeper;
	}

	public static void setZookeeper(ZooKeeper zookeeper) {
		ZkClient.zookeeper = zookeeper;
	}
	
	
	

	public static ConnectParam getCacheParam() {
		return cacheParam;
	}

	public static List<String> getAllChildren(String parent) {
		return getChildren1(parent, true);
	}
	
	public static List<String> getChildren(String parent) {
		return getChildren1(parent, false);
	}
	private static List<String> getChildren1(String parent, boolean recursion) {
		return null;
	}
	
	public static Object get(String key, Object defaultValue) {
		return null;
	}
	
	public static String getString(String key, String defaultValue) {
		return null;
	}
	
	public static int getInt(String key, int defaultValue) {
		return 0;
	}
	
	public static void watch(String path, Watcher watcher, boolean isReconnect) {
		
	}
	


	public static class ConnectParam {
		private String zkUser;
		private String zkPasswd;
		private String zkIp;
		private int zkPort;
		private String zkPath;

		public ConnectParam(String zkUser, String zkPasswd, String zkIp, int zkPort, String zkPath) {
			this.zkUser = zkUser;
			this.zkPasswd = zkPasswd;
			this.zkIp = zkIp;
			this.zkPort = zkPort;
			this.zkPath = zkPath;
		}

		public String getAddress() {
			return zkIp + ":" + zkPort;
		}
		
		public String getZkPath() {
			return zkPath;
		}

		public byte[] authUserPasswd() {
			byte[] authInfo = null;
			try {
				authInfo = (zkUser + ":" + zkPasswd).getBytes(SecurityUtils.UTF_8);
			} catch (Exception e) {
				LOG.warn("charset not supported: ", e);
			}
			return authInfo;
		}
	}
}