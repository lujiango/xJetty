package org.github.xJetty.routing;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Table {
	private Map<String, CycleQueue<Address>> elements = new ConcurrentHashMap<String, CycleQueue<Address>>();

	public void put(String key, CycleQueue<Address> value) {
		elements.put(key, value);
	}

	public CycleQueue<Address> get(String key) {
		return elements.get(key);
	}

	public void remove(String key) {
		elements.remove(key);
	}
}
