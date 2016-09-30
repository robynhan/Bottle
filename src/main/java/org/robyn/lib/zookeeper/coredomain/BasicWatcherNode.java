package org.robyn.lib.zookeeper.coredomain;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Random;

public abstract class BasicWatcherNode implements Watcher {

    protected static Logger LOG = LoggerFactory.getLogger(BasicWatcherNode.class);
    protected static final Integer mutex = -1;

    private Random random = new Random(this.hashCode());
    protected String nodeId;

    protected ZooKeeper zooKeeper;
    protected String hostPort;

    private volatile boolean connected = false;
    private volatile boolean expired = false;

    public BasicWatcherNode(final String hostPort) {
        this.hostPort = hostPort;
        nodeId = Integer.toHexString(random.nextInt());
    }

    public void startZK() {
        try {
            zooKeeper = new ZooKeeper(hostPort, 15000, this);
        } catch (IOException e) {
            LOG.error("Can not start Zookeeper!");
        }
    }

    public void stopZK(){
        try {
            zooKeeper.close();
        } catch (InterruptedException e) {
            LOG.error("Can not stop Zookeeper!");
        }
    }

    @Override
    public void process(WatchedEvent event) {
        LOG.info("Processing event: " + event.toString());

        synchronized (mutex) {
            mutex.notify();
        }

        doProcess(event);

        if(event.getType() == Event.EventType.None){
            switch (event.getState()) {
                case SyncConnected:
                    LOG.debug("connected to zookeeper");
                    connected = true;
                    break;
                case Disconnected:
                    LOG.debug("disconnected from zookeeper");
                    connected = false;
                    break;
                case Expired:
                    expired = true;
                    connected = false;
                    LOG.error("session expiration");
                default:
                    break;
            }
        }
    }

    protected abstract void doProcess(WatchedEvent event) ;

    public boolean isConnected() {
        return connected;
    }

    public boolean isExpired() {
        return expired;
    }
}
