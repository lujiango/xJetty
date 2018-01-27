package org.github.xjetty.core;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.github.xjetty.conf.Config;
import org.github.xjetty.conf.ZkAddress;
import org.github.xjetty.conf.ZkClient;
import org.github.xjetty.core.Entry.Type;
/**
 * 
 * @author lujiango
 *
 */

public class xJetty {
	private static final Logger LOG = Logger.getLogger(xJetty.class);
	public static final int XJETTY_EXIT_CODE = -1;
	
	
	public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, SecurityException {
		xJetty.start("admin:admin@127.0.0.1:2181/x/jetty/xjetty-1");
	}
	
	/**
	 * ����xJetty����
	 * @param args zkUser:zkPasswd@zkIp:zkPortzkPath
	 * eg: admin:admin@127.0.0.1:2181/xjetty/xjetty-1
	 * @throws IOException 
	 */
	public static void start(String args) throws IOException {
		// ��������
		Config conf = Config.getSelf();

        try { 
        	// ��ʼ����log4j_default.properties��ȡ��־����
        	conf.initLogConfig();
        } catch (Exception e) {
        	throw new IOException("Load log4j_default.properties exception.", e);
        }
         
		LOG.info("xJetty starting with [" + args + "]");
		ZkClient client = null;
		try {
			// �����û������zk�����Ϣ������zk����
			client = new ZkClient(new ZkAddress(args));
			client.tryConnect();
		} catch (Exception e) {
			throw e;
		}
		
		conf.setZkClient(client);
		
		conf.setConfigFromZookeeper();
		
		AnnotationScanner.scanSupportedAnnotations();
		
		Startup.startup();
		
		LOG.info("xJetty started");
	}
	
	@Entry(path = "/x/jetty/_get", type = Type.GET)
	public Object get() {
		return "sdf";
	}
	
}
