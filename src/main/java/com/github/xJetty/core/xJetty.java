package com.github.xJetty.core;

import com.github.xJetty.conf.Configuration;
/**
 * 
 * @author lujiango
 *
 */

public class xJetty {
	
	public static void main(String[] args) {
		xJetty.xjetty("admin:admin@127.0.0.1:2181/zookeeper/lujiango/xjetty/xjetty-1");
	}
	
	/**
	 * 
	 * @param args zkUser:zkPasswd@zkIp:zkPortzkPath
	 * eg: admin:admin@127.0.0.1:2181/xjetty/xjetty-1
	 */
	public static void xjetty(String args) {
		
		Configuration.setDefaultLogConfig();
		
		ZkClient.connect(args);
		
		Configuration.setConfigFromZookeeper();
		
		AnnotationScanner.scanAnnotations();
		
		Startup.startup();
	}
	
}
