package util;

import java.util.LinkedList;

/* Work Queue Implementation
 *
 * Original Source:
 * http://www.ibm.com/developerworks/library/j-jtp0730.html
 *
 * Modifications:
 * -- Specified type for LinkedList
 * -- Added shutdown capability
 * -- Added log4j debug messages
 */

public class WorkQueue {
    private final PoolWorker[] threads;
    private final LinkedList<Runnable> queue;

    private volatile boolean shutdown;

    // Add this line to log4j.properties to turn off DEBUG messages:
    // log4j.logger.WorkQueue=OFF

    public WorkQueue(int numThreads) {
        queue = new LinkedList<Runnable>();
        threads = new PoolWorker[numThreads];

        for (int i = 0; i < numThreads; i++) {
            threads[i] = new PoolWorker();
            threads[i].start();
        }

        shutdown = false;

    }

    // Make sure you understand why we didn't make this method synchronized
    public void execute(Runnable r) {
        synchronized (queue) {
            queue.addLast(r);
            queue.notify();
        }
    }

    // This will let threads finish any work that is still in queue
    public void shutdown() {
        shutdown = true;

        synchronized (queue) {
            queue.notifyAll();
        }
    }

    private class PoolWorker extends Thread {
        @Override
        public void run() {
            Runnable r = null;

            while (true) {
                synchronized (queue) {
                    while (queue.isEmpty() && !shutdown) {
                        try {
                            queue.wait();
                        } catch (InterruptedException ex) {
                        }
                    }

                    // Check why we exited inner while
                    if (shutdown) {
                        break;
                    } else {
                        assert !queue.isEmpty();
                        r = queue.removeFirst();
                    }
                }

                // If we don't catch RuntimeException,
                // the pool could leak threads
                try {
                    r.run();
                } catch (RuntimeException ex) {
                }
            }

        }
    }
}
