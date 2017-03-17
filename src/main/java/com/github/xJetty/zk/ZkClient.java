package com.github.xJetty.zk;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooKeeper.States;

public class ZkClient {
	private static Logger LOG = Logger.getLogger(ZkClient.class);
	
	private String zkIp;
	private int zkPort;
	private String zkUser;
	private String zkPasswd;
	private static ZooKeeper zookeeper = null;
	
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
					Thread.currentThread().sleep(5000);
				} catch (InterruptedException e1) {
					LOG.warn("zookeeper connect failed. ", e1);
				}
			}
			
			
		}
		
		
		
	}
	
}
