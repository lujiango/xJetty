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
     * ����ǰ׺��ӵ��б�
     */
    private static void addAllWithPrefix(List<String> to, String prefix,
            List<String> from) {
        for (String s : from) {
            String tmp = prefix + '/' + s;
            // �������path����/���������//
            tmp = tmp.replaceAll("//", "/");
            to.add(tmp);
        }
    }
	
	private List<String> getChildren1(String parent, boolean recursion) {
		try {
            // �����
            List<String> childrenList = getChildren(parent);
            // ��ʱ��������ÿһ�ε������ӽڵ��ֵ��Ϊ�յ�ʱ������ѭ��
            List<String> tChildrenList = new ArrayList<String>(childrenList);
            while (true) {
                if (tChildrenList.isEmpty()) {
                    break;
                } else {
                    // ��ʱ����Ϊ�˱�֤������tChildrenList���ı�
                    List<String> tChildrenListI = new ArrayList<String>();
                    for (String key : tChildrenList) {
                        tChildrenListI.addAll(getChildren(key));
                    }
                    // ��tChildrenList���¸�ֵΪ���ε������
                    tChildrenList = tChildrenListI;
                    // �����������������
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
     * ��ȡָ���ڵ�����ݡ�����ýڵ㲻�����򷵻�ָ����ȱʡֵ��ע��ڵ�Ҳ�п��ܱ���nullֵ��
     * 
     * @param name
     *            �ڵ�·��
     * @param defaultValue
     *            ȱʡֵ
     * @return �ڵ��Ӧ��ֵ��ȱʡֵ
     */
    public byte[] getBytes(String name, byte[] defaultValue) {
        try {
            return getZookeeper().getData(name, false, null);
        } catch (KeeperException ex) {

            // �������Ϊ�ڵ㲻�����򷵻�ȱʡֵ
            if (ex.code() == KeeperException.Code.NONODE) {
                return defaultValue;
            }

            // �����������쳣
            throw new IllegalStateException(ex.getMessage(), ex);
        } catch (InterruptedException ex) {
            throw new IllegalStateException(ex.toString(), ex);
        }
    }
	
	public static int getInt(String key, int defaultValue) {
		return 0;
	}
	
	/**
     * ȷ��·���ϵĽڵ���ڡ���������򷵻ظýڵ��ֵ������������򴴽�������ָ��ֵ�����ظ�ֵ��
     * �����ڵ�ʱ�ϲ�Ŀ¼Ҳ���𼶴�������дnullֵ���������ڵ������ΪPERSISTENT
     * 
     * @param path
     *            �ڵ�·��
     * @param value
     *            ����ڵ㲻����ʱ�����ֵ
     * @param mode
     *            �ڵ�����
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

        // ���Դ������ڵ�
        int n = path.indexOf('/', 1);
        while (n != -1) {
            String key = path.substring(0, n);
            n = path.indexOf('/', n + 1);
            try {
                // ������
                if (zookeeper.exists(key, false) == null) {
                    zookeeper.create(key, null, Ids.OPEN_ACL_UNSAFE,
                            CreateMode.PERSISTENT);
                }
            } catch (KeeperException ex) {
                // �ڵ��Ѵ������ExceptionΪ�������������֮����������ǲ�����
                if (ex.code() != KeeperException.Code.NODEEXISTS) {
                    throw new IllegalStateException(
                            "Create node " + key + " error", ex);
                }
            } catch (InterruptedException ex) {
                throw new IllegalStateException("Create node " + key + " error",
                        ex);
            }
        }

        // ���Դ���ָ���ڵ�
        try {
            // ������
            if (zookeeper.exists(path, false) == null) {
                zookeeper.create(path, value, Ids.OPEN_ACL_UNSAFE, mode);
                return value;// �����ɹ�
            }

            // �ڵ����
            return getBytes(path, value);
        } catch (KeeperException ex) {
            // �ڵ��Ѵ������ExceptionΪ�������������֮������������ǲ�����
            if (ex.code() != KeeperException.Code.NODEEXISTS) {
                throw new IllegalStateException(
                        "Create node " + path + " error", ex);
            }

            // �ڵ����
            return getBytes(path, value);
        } catch (InterruptedException ex) {
            throw new IllegalStateException("Create node " + path + " error",
                    ex);
        }
    }
	
	
	 /**
     * �жϽڵ��Ƿ���ڡ�
     * 
     * @param path
     *            �ڵ�·��
     * @return �Ƿ����
     */
    public boolean exist(String path) {
        try {
            return zookeeper.exists(path, null) != null;
        } catch (KeeperException | InterruptedException ex) {
            throw new IllegalStateException(ex.toString(), ex);
        }
    }

	/**
     * ��ָ���ڵ㼰��ֱ���ӽڵ����ָ��Watcher�� ��������ע���Watcher���ڴ���������ע�ᣬҲ���ڶ�������������ע�ᡣ
     * ���ڵ�ɾ���󣬲���ע�����
     * 
     * @param path
     *            ·��
     * @param watcher
     *            ������
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
            // �Ѿ�ע���
            LOG.warn("path is already be watched by the same watcher: " + path);
        } else {
            try {
                // path�Ĵ�����ɾ���ļ���
                zookeeper.exists(path, new Watcher() {
                    @Override
                    public void process(WatchedEvent paramWatchedEvent) {
                        if (paramWatchedEvent.getType() == EventType.None) {
                            // zookeeper�������Ͽ��¼��� �����κδ���
                            return;
                        }
                        watcher.process(paramWatchedEvent);
                        if (paramWatchedEvent
                                .getType() != EventType.NodeDeleted) {
                            // ����ע��
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

                // path��ֱ���ӽڵ���¼�����
                zookeeper.getChildren(path, new Watcher() {
                    @Override
                    public void process(WatchedEvent paramWatchedEvent) {
                        if (paramWatchedEvent.getType() == EventType.None) {
                            // zookeeper�������Ͽ��¼��� �����κδ���
                            return;
                        }

                        if (paramWatchedEvent
                                .getType() != EventType.NodeDeleted) {
                            watcher.process(paramWatchedEvent);
                            // ����ע��
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
                    // ������������������Ҫ��watcher��¼
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