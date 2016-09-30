package org.robyn.lib.zookeeper.coredomain;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;

import java.util.List;

public class ZKBarrierEntry extends BasicWatcherNode {

    private final int size;
    private final String root;
    private static final Integer mutex = -1;

    ZKBarrierEntry(String hostPort, String root, int size) {
        super(hostPort);
        this.root = root;
        this.size = size;

        start();
    }

    private void start() {
        startZK();
        try {
            Stat stat = zooKeeper.exists(root, false);
            if (stat == null) {
                zooKeeper.create(root, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }

        } catch (InterruptedException e) {
            LOG.error("Keeper exception when instantiating queue: " + e.toString());
        } catch (KeeperException e) {
            LOG.error("Interrupted exception");
        }
    }

    public boolean enter() throws KeeperException, InterruptedException {
        zooKeeper.create(root + "/" + nodeId, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL);

        while (true) {
            synchronized (mutex) {
                List<String> list = zooKeeper.getChildren(root, true);

                if (list.size() < size) {
                    mutex.wait();
                } else {
                    return true;
                }
            }
        }
    }

    public boolean leave() throws KeeperException, InterruptedException {
        zooKeeper.delete(root + "/" + nodeId, 0);
        while (true) {
            synchronized (mutex) {
                List<String> list = zooKeeper.getChildren(root, true);
                if (list.size() > 0) {
                    mutex.wait();
                } else {
                    return true;
                }
            }
        }
    }

    @Override
    protected void doProcess(WatchedEvent event) {
        if (event.getType() == Event.EventType.NodeChildrenChanged) {
            synchronized (mutex) {
                mutex.notify();
            }
        }
    }
}
