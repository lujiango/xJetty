package org.github.x.jetty.routing;

import org.github.x.jetty.http.Address;

/**
 * 内部路由器
 * @author lujiango
 *
 */
public final class Router {
	private RoutingTable routingTable;
	
	public Router() {
		this.routingTable = new RoutingTable();
	}

	public  Address routing(String serviceName) {
		CycleQueue<Address> addresses = routingTable.get(serviceName);
		return addresses.cycle();
	}
	
	
	public void register(String serviceName, Address address) {
		CycleQueue<Address> addresses = routingTable.get(serviceName);
		if (null == addresses) {
			addresses = new CycleQueue<Address>();
			addresses.add(address);
			routingTable.put(serviceName, addresses);
		} else {
			addresses.add(address);
		}
	}
	
	public void unRegister(String serviceName, Address address) {
		CycleQueue<Address> addresses = routingTable.get(serviceName);
		if (null == addresses) {
			return;
		}
		if (addresses.contains(address)) {
			addresses.remove(address);
		}
		if (addresses.isEmpty()) {
			routingTable.remove(serviceName);
		}
	}
}
