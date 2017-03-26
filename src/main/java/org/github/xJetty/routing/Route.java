package org.github.xJetty.routing;

/**
 * 
 * @author lujiango
 *
 */
public final class Route {
	private static RoutingTable routingTable = new RoutingTable();

	public static Address routing(String serviceName) {
		CycleQueue<Address> addresses = routingTable.get(serviceName);
		return addresses.cycle();
	}
	
	
	public static void register(String serviceName, Address address) {
		CycleQueue<Address> addresses = routingTable.get(serviceName);
		if (null == addresses) {
			addresses = new CycleQueue<Address>();
			addresses.add(address);
			routingTable.put(serviceName, addresses);
		} else {
			addresses.add(address);
		}
	}
	
	public static boolean unregister(String serviceName, Address address) {
		CycleQueue<Address> addresses = routingTable.get(serviceName);
		if (null == addresses) {
			return false;
		}
		if (!addresses.remove(address)) {
			return false;
		}
		if (addresses.isEmpty()) {
			routingTable.remove(serviceName);
		}
		return true;
	}
}
