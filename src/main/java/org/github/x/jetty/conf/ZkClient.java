package org.github.x.jetty.conf;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooKeeper.States;

public final class ZkClient {
	private static final Logger LOG = Logger.getLogger(ZkClient.class);
	
	private static final int SESSION_TIMEOUT = 10000;
	
	private static final int CONNECT_INTERVAL = 5000;
	
	private static final int RECONNECT_THRESHOLD = 7;
	
	private static final String DIGEST = "digest";
	
	private static ZooKeeper zookeeper = null;
	
	private static ZkAddress zkAdress = new ZkAddress();
	
	
	private static Map<String, List<Watcher>> watchers = new ConcurrentHashMap<String, List<Watcher>>();
	
	private static Watcher connectWatcher = new Watcher() {
		private long preSessionId = 0L;

		@Override
		public void process(WatchedEvent event) {
			LOG.info("receive event: " + event);
			switch (event.getState()) {
				case SyncConnected: {
					if (preSessionId != getZookeeper().getSessionId()) {
						Map<String, List<Watcher>> watcherCopy = new ConcurrentHashMap<String, List<Watcher>>(watchers);
						for (Entry<String, List<Watcher>> entry : watcherCopy.entrySet()) {
							for (Watcher w : entry.getValue()) {
//								watch(entry.getKey(), w, true);
							}
						}
					}
					preSessionId = getZookeeper().getSessionId();

					break;
				}
				case Expired: {
					if (getZookeeper().getState() != States.CLOSED) {
						try {
							getZookeeper().close();
						} catch (Exception e) {
							LOG.warn("Close zookeeper exception: ", e);
						}
						setZookeeper(null);
					}
					connect(zkAdress);

					break;
				}
				default: {
					break;
				}
			}
		}
	};
	
	public static void connect(String args) {
		zkAdress.parse(args);
		connect(zkAdress);
	}

	private static void connect(ZkAddress zkAddress) {
		if (getZookeeper() != null && getZookeeper().getState() == States.CONNECTED) {
			throw new IllegalStateException("ZkClient has connected to " + zkAddress.getAddress());
		}
		try {

			setZookeeper(new ZooKeeper(zkAddress.getAddress(), SESSION_TIMEOUT, connectWatcher));
            
			while (getZookeeper().getState() != States.CONNECTED) {
				try {
					Thread.sleep(CONNECT_INTERVAL);
				} catch (InterruptedException e) {
					LOG.warn("Connect zookeeper exception: ", e);
				}
			}

			getZookeeper().addAuthInfo(DIGEST, zkAddress.authUserPasswd());
			
			LOG.info("ZkClient has connected to " + zkAddress.getAddress());

		} catch (IOException e) {
			LOG.warn("Connect zookeeper failed. ", e);
			reconnect(zkAdress);
		}
	}

	public static void reconnect(ZkAddress zkAddress) {
		int retryTimes = 0;
		while (getZookeeper() == null || getZookeeper().getState() != States.CONNECTED) {
			try {
				Thread.sleep(CONNECT_INTERVAL);
				ZooKeeper zk = new ZooKeeper(zkAddress.getAddress(), SESSION_TIMEOUT, connectWatcher);
				if (retryTimes > RECONNECT_THRESHOLD) {
					throw new IllegalStateException("Reconnect zookeeper " + retryTimes + " times");
				}
				retryTimes++;
				zk.addAuthInfo(DIGEST, zkAddress.authUserPasswd());
				setZookeeper(zk);
			} catch (Exception e) {
				LOG.warn("Reconnect zookeeper failed. ", e);
			}
		}
	}

	public static ZooKeeper getZookeeper() {
		return zookeeper;
	}

	public static void setZookeeper(ZooKeeper zookeeper) {
		ZkClient.zookeeper = zookeeper;
	}
	
	
	

	public static ZkAddress getZkAdress() {
		return zkAdress;
	}

	public static List<String> getAllChildren(String parent) {
		return getChildren1(parent, true);
	}
	
	public static List<String> getChildren(String root) {
		try {
            List<String> retList = new ArrayList<String>();
            addAllWithPrefix(retList, root, zookeeper.getChildren(root, false));
            return retList;
        } catch (Exception ex) {
            throw new IllegalStateException(ex.toString(), ex);
        }
	}
	
	/**
     * 加上前缀添加到列表。
     */
    private static void addAllWithPrefix(List<String> to, String prefix,
            List<String> from) {
        for (String s : from) {
            String tmp = prefix + '/' + s;
            // 这里避免path就是/的情况出现//
            tmp = tmp.replaceAll("//", "/");
            to.add(tmp);
        }
    }
	
	private static List<String> getChildren1(String parent, boolean recursion) {
		try {
            // 结果集
            List<String> childrenList = getChildren(parent);
            // 临时链表，保存每一次迭代的子节点的值，为空的时候跳出循环
            List<String> tChildrenList = new ArrayList<String>(childrenList);
            while (true) {
                if (tChildrenList.isEmpty()) {
                    break;
                } else {
                    // 临时链表，为了保证迭代中tChildrenList不改变
                    List<String> tChildrenListI = new ArrayList<String>();
                    for (String key : tChildrenList) {
                        tChildrenListI.addAll(getChildren(key));
                    }
                    // 将tChildrenList重新赋值为本次迭代结果
                    tChildrenList = tChildrenListI;
                    // 将迭代结果并入结果集
                    childrenList.addAll(tChildrenList);
                }
            }
            return childrenList;
        } catch (Exception ex) {
            throw new IllegalStateException(ex.toString(), ex);
        }
	}
	
	public static Object get(String key, Object defaultValue) {
		return null;
	}
	
	public static String getString(String key, String defaultValue) {
		byte[] b = getBytes(key, null);
        if (b == null) {
            return defaultValue;
        } else {
            try {
                return new String(b, "utf-8");
            } catch (UnsupportedEncodingException ex) {
                LOG.error(ex.toString(), ex);
                return defaultValue;
            }
        }
	}
	
	 /**
     * 获取指定节点的数据。如果该节点不存在则返回指定的缺省值。注意节点也有可能保存null值。
     * 
     * @param name
     *            节点路径
     * @param defaultValue
     *            缺省值
     * @return 节点对应的值或缺省值
     */
    public static byte[] getBytes(String name, byte[] defaultValue) {
        try {
            return getZookeeper().getData(name, false, null);
        } catch (KeeperException ex) {

            // 如果是因为节点不存在则返回缺省值
            if (ex.code() == KeeperException.Code.NONODE) {
                return defaultValue;
            }

            // 其它错误抛异常
            throw new IllegalStateException(ex.getMessage(), ex);
        } catch (InterruptedException ex) {
            throw new IllegalStateException(ex.toString(), ex);
        }
    }
	
	public static int getInt(String key, int defaultValue) {
		return 0;
	}
	
	public static void watch(String path, Watcher watcher, boolean isReconnect) {
		
	}
}