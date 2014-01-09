// Xiaoou, Dec,2013
// xli65@usfca.edu

package searcher;

import indexer.ThreadSafeInvertedIndex;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import util.MultiReaderLock;
import util.WorkQueue;

/**
 * PartialSearcher is used to deal with one multi-word search query, and return the sorted list of results from your
 * inverted index that start with the query word(s).
 * 
 * Normally, call the constructor, then it will return the proper result.
 * 
 * @author xli65
 */
public class ThreadedPartialSearcher extends PartialSearcher {
    private final WorkQueue threads;
    private int pending = 0;
    private final MultiReaderLock lock = new MultiReaderLock();

    private final Logger logger = Logger.getLogger(ThreadedPartialSearcher.class);

    public ThreadedPartialSearcher(WorkQueue t) {
        super();
        threads = t;
    }

    /**
     * @return the pending number
     */
    public synchronized int getPending() {
        return pending;
    }

    /**
     * Update the pending number.
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
     * Search a query from an index
     * 
     * @param query
     * @param index
     */
    public void search(String query, ThreadSafeInvertedIndex index) {
        logger.debug("Start searching query: " + query);
        ArrayList<String> words = splitQuery(query);

        for (String word : words) {
            threads.execute(new WordSearcher(word, index));
        }

        waitForSearchToFinish();
        logger.debug("Finished searching query: " + query);
    }

    /**
     * The worker that search a word.
     * 
     * @author xli65
     */
    private class WordSearcher implements Runnable {
        private final String word;
        private final ThreadSafeInvertedIndex index;

        public WordSearcher(String word, ThreadSafeInvertedIndex index) {
            logger.debug("Creating a WordSearcher for word: " + word);
            this.word = word;
            this.index = index;
            updatePending(1);
        }

        @Override
        public void run() {
            HashMap<String, SearchedResult> subResultsByDoc = index.partialSearch(word);
            mergeResult(resultsByDoc, subResultsByDoc);
            updatePending(-1);
        }
    }

    @Override
    protected void mergeResult(HashMap<String, SearchedResult> resultsByDoc,
            HashMap<String, SearchedResult> subResultsByDoc) {
        lock.lockWrite();
        super.mergeResult(resultsByDoc, subResultsByDoc);
        lock.unlockWrite();
    }

    private void waitForSearchToFinish() {
        while (pending > 0) {
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }
}
