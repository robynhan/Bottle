package org.robyn.lib.zookeeper.coredomain;

import org.apache.zookeeper.KeeperException;
import org.junit.Test;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class QueueTest {

    class Consumer extends Thread {
        private Queue queue;
        private int number;

        public Consumer(Queue queue, int number) {
            this.queue = queue;
            this.number = number;
        }

        public void run() {
            for (int i = 0; i < 10; i++) {
                try {
                    int value = queue.consume();
                    System.out.println("Consumer #" + this.number + " got: " + value);
                } catch (KeeperException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class Producer extends Thread {
        private Queue queue;
        private int number;

        public Producer(Queue queue, int number) {
            this.queue = queue;
            this.number = number;
        }

        public void run() {
            for (int i = 0; i < 10; i++) {
                queue.produce(new Random().nextInt());

                System.out.println("Producer #" + this.number + " put: " + i);
                try {
                    sleep((int) (Math.random() * 100));
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    @Test
    public void queueTest() throws KeeperException, InterruptedException {
        Queue q = new Queue("127.0.0.1:2181", "/queue1");

        ExecutorService executor = Executors.newFixedThreadPool(10);
        executor.submit(new Producer(q, 1));
        executor.submit(new Consumer(q, 1));
        executor.submit(new Producer(q, 2));
        executor.submit(new Producer(q, 3));
        executor.submit(new Producer(q, 4));
        executor.submit(new Producer(q, 5));

        executor.submit(new Consumer(q, 2));
        executor.submit(new Consumer(q, 3));
        executor.submit(new Consumer(q, 4));
        executor.submit(new Consumer(q, 5));

        executor.shutdown();
        executor.awaitTermination(2000L, TimeUnit.SECONDS);
    }
}