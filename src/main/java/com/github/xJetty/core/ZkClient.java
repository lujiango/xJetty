package com.github.xJetty.core;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooKeeper.States;

public class ZkClient {
	private static final Logger LOG = Logger.getLogger(ZkClient.class);
	private static ZooKeeper zookeeper = null;
	
	private static String zkIp;
	private int zkPort;
	private String zkUser;
	private String zkPasswd;
	
	
	public ZkClient(String zkIp, int zkPort) {
		this.zkIp = zkIp;
		this.zkPort = zkPort;
	}
	
	public static void connect() {
		try {
			zookeeper = new ZooKeeper("", 10000, null);
		} catch (IOException e) {
			LOG.warn("Connect zookeeper failed. ", e);
			while (zookeeper.getState() != States.CONNECTED) {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					LOG.warn("zookeeper connect failed. ", e1);
				}
			}
		}
	}
}
