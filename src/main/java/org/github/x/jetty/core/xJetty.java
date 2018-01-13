package org.github.x.jetty.core;

import org.github.x.jetty.conf.Config;
import org.github.x.jetty.conf.ZkClient;
/**
 * 
 * @author lujiango
 *
 */

public class xJetty {
	
	public static void main(String[] args) {
		xJetty.start("admin:admin@127.0.0.1:2181/x/jetty/xjetty-1");
	}
	
	/**
	 * 
	 * @param args zkUser:zkPasswd@zkIp:zkPortzkPath
	 * eg: admin:admin@127.0.0.1:2181/xjetty/xjetty-1
	 */
	public static void start(String args) {
		
		Config.setDefaultLogConfig();
		
		ZkClient.connect(args);
		
//		Config.setConfigFromZookeeper();
		
		AnnotationScanner.scanSupportedAnnotations();
		
		Startup.startup();
	}
	
}
