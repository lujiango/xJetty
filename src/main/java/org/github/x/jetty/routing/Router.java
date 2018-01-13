package org.github.x.jetty.routing;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.github.x.jetty.conf.ZkClient;
import org.github.x.jetty.http.Address;

import com.huawei.imax.framework.v2.HttpServer;
import com.huawei.imax.framework.v2.Main;
import com.huawei.imax.framework.v2.HttpServer.RoutingTableRegisterType;
import com.huawei.imax.framework.v2.common.CycleList;
import com.huawei.imax.framework.v2.util.ZKUtils;

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
        for (String serv : serverNodes) {
            String instanceName = null;
            try {
                instanceName = serv.substring(serv.lastIndexOf("/") + 1,
                		serv.lastIndexOf("-"));
            } catch (Exception ex) {
                continue;
            }
            CycleQueue<Address> addr;
            if (routingTable.containServ(instanceName)) {
            	addr = routingTable.get(instanceName);
            } else {
            	addr = new CycleQueue<Address>();
                routingTable.put(instanceName, addr);
            }
            addr.add(new Address(zkClient.getString(serv, "")));
        }
        
        serverNodes.add(routingTablePath);
        
        Watcher routeWatcher = new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                String path = event.getPath();
                String serv = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf("-"));
                if (event.getType() == EventType.NodeChildrenChanged) {
                    Set<String> childrenSet = new HashSet<String>();
                    for (Object key : p.keySet()) {
                        if (key.toString().contains(path)) {
                            childrenSet.add(key.toString());
                        }
                    }
                    // 去掉自身节点
                    childrenSet.remove(path);
                    // 获取当前的所有子节点
                    List<String> currentChildrenList = ZKUtils.getChildren(path);

                    // 去除掉已经监听的节点后，看是否还有未监听的节点
                    currentChildrenList.removeAll(childrenSet);
                    LOG.info("new service node will be watched : " + currentChildrenList);
                    for (String watchKey : currentChildrenList) {
                        String value = ZKUtils.getString(watchKey, "");
                        if (value.length() != 0) {
                            p.put(watchKey, value);
                            String instanceName = null;
                            try {
                                instanceName = watchKey.substring(watchKey.lastIndexOf("/") + 1, watchKey.lastIndexOf("-"));
                            } catch (Exception ex) {
                                // 解析routingTable失败，忽略这条
                                LOG.warn("Analysis RoutingTable failed, current path: " + watchKey);
                                // 把错误的从properties中移除
                                p.remove(watchKey);
                                continue;
                            }
                            CycleList<String> hosts;
                            if (rt.containsKey(instanceName)) {
                                hosts = rt.get(instanceName);
                            } else {
                                hosts = new CycleList<String>();
                                rt.put(instanceName, hosts);
                            }
                            hosts.add(value);
                            ZKUtils.watch(watchKey, watcher);
                        }
                    }
                } else if (event.getType() == EventType.NodeDeleted) {
                    if (routingTable.containServ(serv)) {
                            CycleQueue<Address> addr = routingTable.get(serv);
                            addr.remove(zkClient.getString(path, ""));
                            if (addr.isEmpty()) {
                                routingTable.removeServ(serv);
                            }
                            // 如果监听的是自身实例，则需要重新注册routing-table
                            if (path.substring(path.lastIndexOf("/") + 1)
                                    .equals(zkClient.getZkAddress().getAddress().substring(zkClient.getZkAddress().getAddress().lastIndexOf("/") + 1))) {
                            // 需要注册zk节点
                            }
                    }
                } else if (event.getType() == EventType.NodeDataChanged) {
                    if (p.containsKey(path)) {
                        String sValue = p.get(path).toString();
                        String value = ZKUtils.getString(path, "");
                        p.remove(path);
                        if (value.length() != 0) {
                            p.put(path, value);
                            String instanceName = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf("-"));
                            if (rt.containsKey(instanceName)) {
                                rt.get(instanceName).remove(sValue);
                                rt.get(instanceName).add(value);
                            }
                        }
                    }
                }
            }
        };

        for (String key : serverNodes) {
            zkClient.watch(key, routeWatcher);
        }
        
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
			routingTable.removeServ(serviceName);
		}
	}
}
