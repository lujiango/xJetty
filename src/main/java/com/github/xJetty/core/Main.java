package com.github.xJetty.core;
/**
 * xJetty ������
 * @author lujiang
 *
 */

public class Main {

	
	public static void main(String[] args) {
		setDefaultLogConfig();
		
		connectZookeeper();
		
		readConfigFromZookeeper();
		
		AnnotationMapper.process();
		
		Startup.startup();
	}

}
