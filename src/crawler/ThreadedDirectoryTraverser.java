package crawler;

import indexer.InvertedIndex;
import indexer.ThreadSafeInvertedIndex;

import java.io.File;

import parser.FileParser;
import util.WorkQueue;

// Xiaoou, Sep,2013
// xli65@usfca.edu

/**
 * ThreadedDirectoryTraverser traverse a directory, and build a index in a multi-threads way.
 * 
 * @author xli65
 * 
 */
public class ThreadedDirectoryTraverser extends DirectoryTraverser {
    private final WorkQueue threads;
    private int pending = 0;

    public ThreadedDirectoryTraverser(WorkQueue t) {
        super();
        threads = t;
    }

    /**
     * It creates a worker thread to traverse the directory. The worker thread will create new worker threads to parse
     * files under the directory, then add all words into inverted index recursively in the run method of inner class
     * for worker.
     * 
     * @param directory
     *            the directory will be parsed
     */
    public void dirTraverse(File directory, ThreadSafeInvertedIndex index) {
        parseDir(directory, index);
        waitForIndexingFinish();
    }

    /**
     * Overriding parseDir to add a new FileWorker to the queue to parse each file.
     * 
     * @param directory
     *            the directory will be parsed
     * @param index
     *            the InvertedIndex to create
     */
    public void parseDir(File file, ThreadSafeInvertedIndex index) {
        try {
            if (file.exists() && !file.isHidden()) {
                if (file.isDirectory()) {
                    for (File f : file.listFiles()) {
                        parseDir(f, index);
                    }
                } else {
                    if (file.getName().toLowerCase().endsWith(".txt")) {
                        threads.execute(new FileWorker(file, index));
                    }
                }
            }
        } catch (Exception e) {
            System.out.print(e.getMessage());
            System.exit(0);
        }

    }

    /**
     * @return the pending number
     */
    public synchronized int getPending() {
        return pending;
    }

    /**
     * Update the pending number
     * 
     * @param amount
     */
    public synchronized void updatePending(int amount) {
        pending += amount;
        if (pending <= 0) {
            this.notifyAll();
        }
    }

    /**
     * Worker to parse and index a file into the ThreadSafeInvertedIndex specified.
     * 
     * @author xo
     * 
     */
    private class FileWorker implements Runnable {
        private final File file;
        private final ThreadSafeInvertedIndex index;

        public FileWorker(File file, ThreadSafeInvertedIndex index) {
            this.file = file;
            this.index = index;
            updatePending(1);
        }

        /**
         * Parse the file, and index all words into index.
         */
        @Override
        public void run() {
            InvertedIndex temp = new InvertedIndex();
            FileParser.parseFile(file, temp);
            index.addAll(temp);
            updatePending(-1);
        }
    }

    /**
     * Wait for all files parsed and indexing complete.
     */
    private void waitForIndexingFinish() {
        while (getPending() > 0) {
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
        }
    }
}
