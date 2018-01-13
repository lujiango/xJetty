package org.github.x.jetty.core;

import org.apache.log4j.Logger;
import org.github.x.jetty.conf.Config;
import org.github.x.jetty.core.Entry.Type;
/**
 * 
 * @author lujiango
 *
 */

public class xJetty {
	private static final Logger LOG = Logger.getLogger(xJetty.class);
	
	public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, SecurityException {
		xJetty.start("admin:admin@127.0.0.1:2181/x/jetty/xjetty-1");
	}
	
	/**
	 * 
	 * @param args zkUser:zkPasswd@zkIp:zkPortzkPath
	 * eg: admin:admin@127.0.0.1:2181/xjetty/xjetty-1
	 */
	public static void start(String args) {
		
		Config.setDefaultLogConfig();
		LOG.info("xJetty starting with [" + args + "]");
		
//		ZkClient.connect(args);
		 
//		Config.setConfigFromZookeeper();
		
		AnnotationScanner.scanSupportedAnnotations();
		
		Startup.startup();
		
		LOG.info("xJetty started");
	}
	
	@Entry(path = "/x/jetty/_get", type = Type.GET)
	public Object get() {
		return "sdf";
	}
	
}
