package org.github.xjetty.conf;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper.States;

public final class ZkClient {
	private static final Logger LOG = Logger.getLogger(ZkClient.class);
	
	private static final int SESSION_TIMEOUT = 10000;
	
	private static final int CONNECT_INTERVAL = 5000;
	
	private static final int RECONNECT_THRESHOLD = 7;
	
	private static final String DIGEST = "digest";
	
	private ZooKeeper zookeeper;
	
	private ZkAddress zkAddress;
	
	public ZkClient(ZkAddress address) {
		this.zkAddress = address;
	}
	
	private static Map<String, List<Watcher>> watchers = new ConcurrentHashMap<String, List<Watcher>>();
	
	private Watcher connectWatcher = new Watcher() {
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
					tryConnect();

					break;
				}
				default: {
					break;
				}
			}
		}
	};
	
	public void tryConnect() {
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
			LOG.warn("Connect zookeeper exception: ", e);
			tryConnect();
		}
	}

	public void test() {
		int retryTimes = 0;
		while (getZookeeper() == null || getZookeeper().getState() != States.CONNECTED) {
			if (retryTimes > RECONNECT_THRESHOLD) {
				throw new IllegalStateException("Reconnect zookeeper " + retryTimes + " times");
			}
			try {
				Thread.sleep(CONNECT_INTERVAL);
				ZooKeeper zk = new ZooKeeper(zkAddress.getAddress(), SESSION_TIMEOUT, connectWatcher);
				retryTimes++;
				zk.addAuthInfo(DIGEST, zkAddress.authUserPasswd());
				setZookeeper(zk);
			} catch (Exception e) {
				LOG.warn("Reconnect zookeeper failed. ", e);
			}
		}
	}

	public ZooKeeper getZookeeper() {
		return zookeeper;
	}

	public void setZookeeper(ZooKeeper zookeeper) {
		this.zookeeper = zookeeper;
	}
	
	
	

	public ZkAddress getZkAddress() {
		return zkAddress;
	}

	public List<String> getAllChildren(String parent) {
		return getChildren1(parent, true);
	}
	
	public List<String> getChildren(String root) {
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
	
	private List<String> getChildren1(String parent, boolean recursion) {
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
	
	public String getString(String key, String defaultValue) {
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
    public byte[] getBytes(String name, byte[] defaultValue) {
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
	
	/**
     * 确保路径上的节点存在。如果存在则返回该节点的值；如果不存在则创建并填入指定值并返回该值。
     * 创建节点时上层目录也会逐级创建并填写null值。所创建节点的类型为PERSISTENT
     * 
     * @param path
     *            节点路径
     * @param value
     *            如果节点不存在时填入的值
     * @param mode
     *            节点类型
     * @return byte[]
     */
	public byte[] touch(String path, byte[] value, CreateMode mode) {
        if (path == null) {
            throw new NullPointerException("Path is null");
        }
        if (path.charAt(0) != '/') {
            throw new IllegalArgumentException(
                    "Path not starts with '/' :" + path);
        }
        if (path.length() == 1) {
            throw new IllegalArgumentException("Path can be '/'");
        }

        // 尝试创建父节点
        int n = path.indexOf('/', 1);
        while (n != -1) {
            String key = path.substring(0, n);
            n = path.indexOf('/', n + 1);
            try {
                // 不存在
                if (zookeeper.exists(key, false) == null) {
                    zookeeper.create(key, null, Ids.OPEN_ACL_UNSAFE,
                            CreateMode.PERSISTENT);
                }
            } catch (KeeperException ex) {
                // 节点已存在这个Exception为正常情况，忽略之其它情况则是不正常
                if (ex.code() != KeeperException.Code.NODEEXISTS) {
                    throw new IllegalStateException(
                            "Create node " + key + " error", ex);
                }
            } catch (InterruptedException ex) {
                throw new IllegalStateException("Create node " + key + " error",
                        ex);
            }
        }

        // 尝试创建指定节点
        try {
            // 不存在
            if (zookeeper.exists(path, false) == null) {
                zookeeper.create(path, value, Ids.OPEN_ACL_UNSAFE, mode);
                return value;// 创建成功
            }

            // 节点存在
            return getBytes(path, value);
        } catch (KeeperException ex) {
            // 节点已存在这个Exception为正常情况，忽略之，其它情况则是不正常
            if (ex.code() != KeeperException.Code.NODEEXISTS) {
                throw new IllegalStateException(
                        "Create node " + path + " error", ex);
            }

            // 节点存在
            return getBytes(path, value);
        } catch (InterruptedException ex) {
            throw new IllegalStateException("Create node " + path + " error",
                    ex);
        }
    }
	
	
	 /**
     * 判断节点是否存在。
     * 
     * @param path
     *            节点路径
     * @return 是否存在
     */
    public boolean exist(String path) {
        try {
            return zookeeper.exists(path, null) != null;
        } catch (KeeperException | InterruptedException ex) {
            throw new IllegalStateException(ex.toString(), ex);
        }
    }

	/**
     * 给指定节点及其直接子节点添加指定Watcher。 本方法所注册的Watcher会在触发后重新注册，也会在断连重连后重新注册。
     * 当节点删除后，不在注册监听
     * 
     * @param path
     *            路径
     * @param watcher
     *            监听器
     */
    public void watch(String path, Watcher watcher) {
        watch(path, watcher, false);
    }

    private void watch(String path, final Watcher watcher,
            boolean isReConnect) {
        if (!exist(path)) {
            LOG.warn("path is not exists: " + path);
            return;
        }

        if (watchers.containsKey(path) && watchers.get(path).contains(watcher)
                && !isReConnect) {
            // 已经注册过
            LOG.warn("path is already be watched by the same watcher: " + path);
        } else {
            try {
                // path的创建和删除的监听
                zookeeper.exists(path, new Watcher() {
                    @Override
                    public void process(WatchedEvent paramWatchedEvent) {
                        if (paramWatchedEvent.getType() == EventType.None) {
                            // zookeeper服务器断开事件， 不做任何处理
                            return;
                        }
                        watcher.process(paramWatchedEvent);
                        if (paramWatchedEvent
                                .getType() != EventType.NodeDeleted) {
                            // 重新注册
                            try {
                                zookeeper.exists(paramWatchedEvent.getPath(),
                                        this);
                            } catch (Exception e) {
                                LOG.warn("watch failed...", e);
                            }
                        } else {
                            List<Watcher> watcherList = watchers
                                    .get(paramWatchedEvent.getPath());
                            if (watcherList != null) {
                                watcherList.remove(watcher);
                                if (watcherList.size() == 0) {
                                    watchers.remove(
                                            paramWatchedEvent.getPath());
                                }
                            }
                        }
                    }
                });

                // path的直接子节点的事件监听
                zookeeper.getChildren(path, new Watcher() {
                    @Override
                    public void process(WatchedEvent paramWatchedEvent) {
                        if (paramWatchedEvent.getType() == EventType.None) {
                            // zookeeper服务器断开事件， 不做任何处理
                            return;
                        }

                        if (paramWatchedEvent
                                .getType() != EventType.NodeDeleted) {
                            watcher.process(paramWatchedEvent);
                            // 重新注册
                            try {
                                    zookeeper.getChildren(
                                            paramWatchedEvent.getPath(), this);
                            } catch (Exception e) {
                                LOG.warn("watch failed...", e);
                            }
                        }
                    }
                });
                if (!isReConnect) {
                    // 如果不是重连的情况，要将watcher纪录
                    List<Watcher> watcherList;
                    if (watchers.containsKey(path)) {
                        watcherList = watchers.get(path);
                    } else {
                        watcherList = Collections
                                .synchronizedList(new ArrayList<Watcher>());
                        watchers.put(path, watcherList);
                    }
                    watcherList.add(watcher);
                }
            } catch (Exception e) {
                LOG.warn("watch failed...", e);
            }
        }
    }
	
}