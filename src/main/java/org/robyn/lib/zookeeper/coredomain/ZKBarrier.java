package org.robyn.lib.zookeeper.coredomain;

public class ZKBarrier {
    private final String hostPort;
    private final String root;
    private final int size;

    public ZKBarrier(String hostPort, String root, int size) {
        this.hostPort = hostPort;
        this.root = root;
        this.size = size;
    }

    public ZKBarrierEntry newEntry() {
        return new ZKBarrierEntry(hostPort, root, size);
    }
}
