package org.github.x.jetty.http.routing;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.github.x.jetty.http.server.Address;

/**
 * 内部服务的路由表
 * 
 * ---user-1 http://192.168.55.161:12345 user ---user-2
 * https://192.168.55.161:54321
 * 
 * @author lujiango
 *
 */
public class RoutingTable {

	private Map<String, CycleQueue<Entry>> elements;

	RoutingTable() {
		this.elements = new ConcurrentHashMap<>();
	}

	void put(String serv, CycleQueue<Entry> value) {
		elements.put(serv, value);
	}

	CycleQueue<Entry> get(String serv) {
		return elements.get(serv);
	}

	void remove(String serv) {
		elements.remove(serv);
	}

	boolean contain(String serv) {
		return elements.containsKey(serv);
	}

	String getAddress(String serv, String path) {
		CycleQueue<Entry> cyc = get(serv);
		for (Entry e : cyc) {
			if (e.zkPath.equals(path)) {
				return e.address.getAddress();
			}
		}
		return null;
	}

	void removeAddress(String serv, String path) {
		CycleQueue<Entry> cyc = get(serv);
		if (cyc != null) {
			CycleQueue<Entry> removed = new CycleQueue<>();
			for (Entry e : cyc) {
				if (e.zkPath.equals(path)) {
					removed.add(e);
				}
			}
			cyc.removeAll(removed);
		}
		// 如果服务对应的节点信息为空，则删除服务信息
		if (cyc.isEmpty()) {
			remove(serv);
		}
	}

	void setAddress(String serv, String path, Address addr) {
		Entry e = new Entry(path,addr);
		removeAddress(serv, path);
		CycleQueue<Entry> cyc = get(serv);
		if (cyc != null) {
			cyc.add(e);
		} else {
			cyc = new CycleQueue<>();
			cyc.add(e);
			put(serv, cyc);
		}
	}

	public static class Entry {
		String zkPath;
		Address address;

		public Entry(String zkPath, Address addr) {
			this.zkPath = zkPath;
			this.address = addr;
		}
	}
}
