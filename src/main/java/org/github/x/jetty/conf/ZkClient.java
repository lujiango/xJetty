package org.github.x.jetty.conf;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooKeeper.States;

public final class ZkClient {
	private static final Logger LOG = Logger.getLogger(ZkClient.class);
	
	private static final int SESSION_TIMEOUT = 10000;
	
	private static final int CONNECT_INTERVAL = 5000;
	
	private static final int RECONNECT_THRESHOLD = 7;
	
	private static final String DIGEST = "digest";
	
	private static ZooKeeper zookeeper = null;
	
	private static ZkAddress zkAdress = new ZkAddress();
	
	
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
					connect(zkAdress);

					break;
				}
				default: {
					break;
				}
			}
		}
	};
	
	public static void connect(String args) {
		zkAdress.parse(args);
		connect(zkAdress);
	}

	private static void connect(ZkAddress zkAddress) {
		if (getZookeeper() != null && getZookeeper().getState() == States.CONNECTED) {
			throw new IllegalStateException("ZkClient has connected to " + zkAddress.getZkAddress());
		}
		try {

			setZookeeper(new ZooKeeper(zkAddress.getZkAddress(), SESSION_TIMEOUT, connectWatcher));
            
			while (getZookeeper().getState() != States.CONNECTED) {
				try {
					Thread.sleep(CONNECT_INTERVAL);
				} catch (InterruptedException e) {
					LOG.warn("Connect zookeeper exception: ", e);
				}
			}

			getZookeeper().addAuthInfo(DIGEST, zkAddress.authUserPasswd());
			
			LOG.info("ZkClient has connected to " + zkAddress.getZkAddress());

		} catch (IOException e) {
			LOG.warn("Connect zookeeper failed. ", e);
			reconnect(zkAdress);
		}
	}

	public static void reconnect(ZkAddress zkAddress) {
		int retryTimes = 0;
		while (getZookeeper() == null || getZookeeper().getState() != States.CONNECTED) {
			try {
				Thread.sleep(CONNECT_INTERVAL);
				ZooKeeper zk = new ZooKeeper(zkAddress.getZkAddress(), SESSION_TIMEOUT, connectWatcher);
				if (retryTimes > RECONNECT_THRESHOLD) {
					throw new IllegalStateException("Reconnect zookeeper " + retryTimes + " times");
				}
				retryTimes++;
				zk.addAuthInfo(DIGEST, zkAddress.authUserPasswd());
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
	
	
	

	public static ZkAddress getZkAdress() {
		return zkAdress;
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
}