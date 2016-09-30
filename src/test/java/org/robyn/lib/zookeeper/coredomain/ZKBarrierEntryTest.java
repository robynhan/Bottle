package org.robyn.lib.zookeeper.coredomain;

import org.apache.zookeeper.KeeperException;
import org.junit.Test;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ZKBarrierEntryTest {

    private static final String HOST_PORT = "127.0.0.1:2181";

    @Test
    public void should_barrier() throws InterruptedException {

        ZKBarrier barrierGroup = new ZKBarrier(HOST_PORT, "/root", 3);

        ZKBarrierEntry barrier1 = barrierGroup.newEntry();
        ZKBarrierEntry barrier2 = barrierGroup.newEntry();
        ZKBarrierEntry barrier3 = barrierGroup.newEntry();

        ExecutorService executor = Executors.newFixedThreadPool(3);
        executor.submit(new Thread(new Runner(barrier1, "Contestant No. 1")));
        executor.submit(new Thread(new Runner(barrier2, "Contestant No. 2")));
        executor.submit(new Thread(new Runner(barrier3, "Contestant No. 3")));

        executor.shutdown();
        executor.awaitTermination(2000L, TimeUnit.SECONDS);
    }

    private class Runner implements Runnable {
        private ZKBarrierEntry barrier;

        private String name;

        Runner(ZKBarrierEntry barrier, String name) {
            super();
            this.barrier = barrier;
            this.name = name;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(1000 * (new Random()).nextInt(8));
                System.out.println(name + " Ready...");
                barrier.enter();
                System.out.println(name + " DoSomething...");
                barrier.leave();
            } catch (InterruptedException | KeeperException e) {
                e.printStackTrace();
            }
            System.out.println(name + " Go！");
        }

    }
}
