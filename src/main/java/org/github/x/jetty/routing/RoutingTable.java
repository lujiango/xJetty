package org.github.x.jetty.routing;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.github.x.jetty.http.Address;
/**
 * 内部服务的路由表
 * @author lujiango
 *
 */
public class RoutingTable {
	private Map<String, CycleQueue<Address>> elements;
	
	RoutingTable() {
		this.elements = new ConcurrentHashMap<>();
	}
	
	void put(String key, CycleQueue<Address> value) {
		elements.put(key, value);
	}

	CycleQueue<Address> get(String key) {
		return elements.get(key);
	}

	void removeServ(String serv) {
		elements.remove(serv);
	}
	
	boolean containServ(String serv) {
		return elements.containsKey(serv);
	}
	
}
