package org.robyn.lib.zookeeper.coredomain;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.data.Stat;

import java.util.List;
import java.util.Objects;

import static org.apache.zookeeper.CreateMode.PERSISTENT;
import static org.apache.zookeeper.CreateMode.PERSISTENT_SEQUENTIAL;
import static org.apache.zookeeper.ZooDefs.Ids.OPEN_ACL_UNSAFE;

public class Queue extends BasicWatcherNode {

    private final String root;

    public Queue(String hostPort, String name) {
        super(hostPort);
        this.root = name;

        startZK();

        try {
            Stat s = zooKeeper.exists(root, false);
            if (s == null) {
                zooKeeper.create(root, new byte[0], OPEN_ACL_UNSAFE, PERSISTENT);
            }
        } catch (KeeperException e) {
            LOG.error("Keeper exception when instantiating queue: " + e.toString());
        } catch (InterruptedException e) {
            LOG.error("Interrupted exception");
        }
    }

    public void produce(int value) {
        try {
            zooKeeper.create(root + "/element", Ints.toByteArray(value), OPEN_ACL_UNSAFE, PERSISTENT_SEQUENTIAL);
        } catch (KeeperException e) {
            LOG.error("Keeper exception when produce : " + e.toString());
        } catch (InterruptedException e) {
            LOG.error("Interrupted exception");
        }
    }

    int consume() throws KeeperException, InterruptedException {
        // Get the first element available
        while (true) {
            synchronized (mutex) {
                List<String> list = zooKeeper.getChildren(root, true);
                if (list.size() == 0) {
                    System.out.println("Going to wait");
                    mutex.wait();
                } else {
                    String firstChildPath = root + "/" + list.get(0);
                    byte[] data = zooKeeper.getData(firstChildPath, false, null);
                    zooKeeper.delete(firstChildPath, -1);
                    return Ints.fromByteArray(data);
                }
            }
        }
    }

    @Override
    protected void doProcess(WatchedEvent event) {

    }
}
