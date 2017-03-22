package com.github.xJetty.core;

import java.util.concurrent.BlockingQueue;

/**
 * 
 * @author lujiango
 *
 */
public class Routing {
	private Table table;
	
	public boolean register() {
		return false;
	}
	
	public boolean unRegister() {
		return false;
	}
	
	class Table {
		
	}

	class Item {
		private String service;
		private BlockingQueue<String> addresses;
	}
}
