package org.github.xjetty.http.routing;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.github.xjetty.conf.ZkClient;
import org.github.xjetty.http.routing.RoutingTable.Entry;
import org.github.xjetty.http.server.Address;

/**
 * 内部路由器
 * @author lujiango
 *
 */
public final class Router {
	private RoutingTable routingTable;
	private ZkClient zkClient;
	
	public Router(ZkClient zkClient) {
		this.zkClient = zkClient;
		this.routingTable = new RoutingTable();
	}
	
	public void build() {
		Pattern p = Pattern.compile("(/[^/]*){2}");
        Matcher m = p.matcher(zkClient.getZkAddress().getPath());
        m.find();
        String routingTablePath = m.group().concat("/routing-table");
        zkClient.touch(routingTablePath, new byte[0], CreateMode.PERSISTENT);
        List<String> serverNodes = zkClient.getChildren(routingTablePath);
        String instanceName = null;
        for (String serv : serverNodes) {
            try {
                instanceName = serv.substring(serv.lastIndexOf("/") + 1,
                		serv.lastIndexOf("-"));
            } catch (Exception ex) {
                continue;
            }
            CycleQueue<Entry> addr;
            if (routingTable.contain(instanceName)) {
            	addr = routingTable.get(instanceName);
            } else {
            	addr = new CycleQueue<Entry>();
                routingTable.put(instanceName, addr);
            }
            Entry e = new Entry(serv, new Address(zkClient.getString(serv, "")));
            addr.add(e);
        }
        
        serverNodes.add(routingTablePath);
        
        Watcher routeWatcher = new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                String path = event.getPath();
                String serv = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf("-"));
                if (event.getType() == EventType.NodeChildrenChanged) {
                  // #TODO
                } else if (event.getType() == EventType.NodeDeleted) {
                	routingTable.removeAddress(serv, path);
                    // 如果监听的是自身实例，则需要重新注册routing-table
                    if (path.substring(path.lastIndexOf("/") + 1)
                            .equals(zkClient.getZkAddress().getAddress().substring(zkClient.getZkAddress().getAddress().lastIndexOf("/") + 1))) {
                    // 需要注册zk节点 #TODO
                    }
                } else if (event.getType() == EventType.NodeDataChanged) {
                	Address addr = new Address(zkClient.getString(path, null));
                	routingTable.setAddress(serv, path, addr);
                }
            }
        };

        for (String key : serverNodes) {
            zkClient.watch(key, routeWatcher);
        }
        
	}

	public  Address routing(String serv) {
		CycleQueue<Entry> addr = routingTable.get(serv);
		return addr.cycle().address;
	}
	
	
	public void register(String serv,String path, Address addr) {
		routingTable.setAddress(serv, path, addr);
		
	}
	
	public void unRegister(String serv, String path) {
		routingTable.removeAddress(serv, path);
	}
}
