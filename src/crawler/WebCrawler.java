// Xiaoou, Sep,2013
// xli65@usfca.edu

package crawler;

import indexer.InvertedIndex;
import indexer.ThreadSafeInvertedIndex;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;

import org.apache.log4j.Logger;

import parser.HTMLFetcher;
import util.WorkQueue;

/**
 * Web crawler that crawls a seed link with multi-threads
 * 
 * @author xli65
 */
public class WebCrawler extends DirectoryTraverser {
    private static final int INTERVAL = 50;
    private int MAX = 0;
    private final HashSet<String> crawledLinks = new HashSet<String>();
    private final WorkQueue workers;
    private int pending = 0;

    private final Logger logger = Logger.getLogger(WebCrawler.class);

    public WebCrawler(WorkQueue workers) {
        super();
        this.workers = workers;
    }

    /**
     * It creates a worker thread to traverse the directory. The worker thread will create new worker threads to parse
     * files under the directory, then add all words into inverted index recursively in the run method of inner class
     * for worker.
     * 
     * @param directory
     *            the directory will be parsed
     */
    public void crawl(ThreadSafeInvertedIndex index, String seed) {
        synchronized (crawledLinks) {
            crawledLinks.add(seed);
            MAX += INTERVAL;
        }
        workers.execute(new WebCrawlerWorker(seed, index));
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
     * Worker to parse links and index a website into the ThreadSafeInvertedIndex specified.
     */
    private class WebCrawlerWorker implements Runnable {
        private final String url;
        private final ThreadSafeInvertedIndex index;

        public WebCrawlerWorker(String url, ThreadSafeInvertedIndex index) {
            logger.debug("Create a WebCrawler to crawl url: " + url);
            this.url = url;
            this.index = index;
            updatePending(1);
        }

        /**
         * Parse the web page, and index all words into index.
         */
        @Override
        public void run() {
            logger.debug("WebCrawler start working on url: " + url);
            InvertedIndex subIndex = new InvertedIndex();// try to remove the local
            HTMLFetcher fetcher = null;
            try {
                fetcher = new HTMLFetcher(url, subIndex);
                fetcher.fetch();
                for (String link : fetcher.getLinks()) {
                    addLink(link);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            index.addAll(subIndex);
            updatePending(-1);
        }

        /**
         * Takes a link, removes fragment # portion then checks if links.size() < 50 and links set does not contain
         * link, then it will add link to the set links, and will add a new HTMLWorker to the queue.
         * 
         * @param link
         */
        private synchronized void addLink(String link)
                throws MalformedURLException {
            int lastIndexOfPan = link.lastIndexOf('#');
            link = (lastIndexOfPan == -1) ? link : link.substring(0,
                    lastIndexOfPan);
            // get the absolute url
            URL base = new URL(this.url);
            URL absolute = link.startsWith("http://") ? new URL(link)
                    : new URL(base, link);
            String validLink = absolute.toString();

            if (crawledLinks.size() < MAX && !crawledLinks.contains(validLink)) {
                crawledLinks.add(validLink);
                workers.execute(new WebCrawlerWorker(validLink, index));
            }
        }
    }

    /**
     * Wait for all files parsed and indexing complete.
     */
    public void waitForIndexingFinish() {
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
