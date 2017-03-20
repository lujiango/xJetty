package com.github.xJetty.core;

import com.github.xJetty.conf.Configuration;
/**
 * 
 * @author lujiango
 *
 */

public class xJetty {
	
	public static void main(String[] args) {
		args = new String[1];
		args[0] = "admin:admin@127.0.0.1:2181/xjetty/xjetty-1";
		xJetty.xjetty(args[0]);
	}
	
	/**
	 * 
	 * @param args zkUser:zkPasswd@zkIp:zkPortzkPath
	 * eg: admin:admin@127.0.0.1:2181/xjetty/xjetty-1
	 */
	public static void xjetty(String args) {
		
		Configuration.setDefaultLogConfig();
		
		Configuration.parseArgs(args);
		
		ZkClient.connect();
		
		Configuration.setConfigFromZookeeper();
		
		AnnotationMapper.process();
		
		Startup.startup();
	}
	
}
