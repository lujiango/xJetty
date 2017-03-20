package com.github.xJetty.core;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper.States;

public final class ZkClient {
	private static final Logger LOG = Logger.getLogger(ZkClient.class);
	private static final int SESSION_TIMEOUT = 10000;
	private static final int RECONNECT_INTERVAL = 5000;
	private static ZooKeeper zookeeper = null;
	
	private static Watcher connectWatcher = new Watcher() {
		
		@Override
		public void process(WatchedEvent event) {
			LOG.info("receive event: " + event);
			switch(event.getState()){ 
			case KeeperState.SyncConnected : {
				
				}
			}
		}
	};
	private static Param param = new Param("admin", "admin", "127.0.0.1", 2181,
			"/");

	public static void connect() {
		if (zookeeper != null) {
			throw new IllegalArgumentException("ZkClient has connected to " + param.connectString());
		}
		try {
			zookeeper = new ZooKeeper(getParam().connectString(), SESSION_TIMEOUT, connectWatcher);
			
		} catch (IOException e) {
			LOG.warn("Connect zookeeper failed. ", e);
			reconnect();
		}
	}
	
	public static void reconnect() {
		while (getZookeeper().getState() != States.CONNECTED) {
			try {
				Thread.sleep(RECONNECT_INTERVAL);
				zookeeper = new ZooKeeper(getParam().connectString(), SESSION_TIMEOUT, connectWatcher);
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

	public static Param getParam() {
		return param;
	}

	public static void setParam(Param param) {
		ZkClient.param = param;
	}

	public static class Param {
		private String zkUser;
		private String zkPasswd;
		private String zkIp;
		private int zkPort;
		private String zkPath;

		public Param(String zkUser, String zkPasswd, String zkIp, int zkPort,
				String zkPath) {
			this.zkUser = zkUser;
			this.zkPasswd = zkPasswd;
			this.zkIp = zkIp;
			this.zkPort = zkPort;
			this.zkPath = zkPath;
		}

		public String connectString() {
			return zkIp + ":" + zkPort;
		}

	}

}
