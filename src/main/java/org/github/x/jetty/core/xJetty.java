package org.github.x.jetty.core;

import org.github.x.jetty.conf.Configuration;
/**
 * 
 * @author lujiango
 *
 */

public class xJetty {
	
	public static void main(String[] args) {
		xJetty.start("admin:admin@127.0.0.1:2181/zookeeper/lujiango/xjetty/xjetty-1");
	}
	
	/**
	 * 
	 * @param args zkUser:zkPasswd@zkIp:zkPortzkPath
	 * eg: admin:admin@127.0.0.1:2181/xjetty/xjetty-1
	 */
	public static void start(String args) {
		
		Configuration.setDefaultLogConfig();
		
		ZkClient.connect(args);
		
		Configuration.setConfigFromZookeeper();
		
		AnnotationScanner.scanSupportedAnnotations();
		
		Startup.startup();
	}
	
}
