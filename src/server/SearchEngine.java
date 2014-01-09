// Xiaoou, Dec,2013
// xli65@usfca.edu

package server;

import indexer.ThreadSafeInvertedIndex;

import java.util.HashMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

import searcher.SearchedResult;
import searcher.ThreadedPartialSearcher;
import server.sevlet.AdminServlet;
import server.sevlet.HomeServlet;
import server.sevlet.PreviewServlet;
import server.sevlet.RegisterServlet;
import server.sevlet.SearchResultServlet;
import util.WorkQueue;
import crawler.WebCrawler;

/**
 * SearchEngine is a server that enable you to index web pages with a seed and
 * perform partial search.
 * 
 * User need to provide
 * 
 * 1. seed url,
 * 
 * 2. server port,
 * 
 * 3. number of thread that doing index and searching.
 * 
 * Only one search engine for an application.
 * 
 * @author xli65
 */
public class SearchEngine {

	private final Logger logger = Logger.getLogger(SearchEngine.class);

	private static SearchEngine globalSearchEngine = null;

	private final ThreadSafeInvertedIndex index = new ThreadSafeInvertedIndex();
	private final Server server;
	private final WorkQueue workers;
	private final WebCrawler webCrawler;

	private static HashMap<String, TreeSet<SearchedResult>> cache = new HashMap<String, TreeSet<SearchedResult>>();

	public SearchEngine(int port, int numOfThreads) {
		server = new Server(port);
		workers = new WorkQueue(numOfThreads);
		webCrawler = new WebCrawler(workers);
		globalSearchEngine = this;
		setupWebServer();
	}

	private void setupWebServer() {
		ServletHandler servletHandler = new ServletHandler();

		// Setup servlet mappings
		servletHandler.addServletWithMapping(SearchResultServlet.class,
				"/search");
		servletHandler
				.addServletWithMapping(RegisterServlet.class, "/register");
		servletHandler.addServletWithMapping(PreviewServlet.class, "/preview");
		servletHandler.addServletWithMapping(AdminServlet.class, "/admin");
		servletHandler.addServletWithMapping(HomeServlet.class, "/");

		server.setHandler(servletHandler);
	}

	/**
	 * Start the search engine.
	 */
	public void startWithSeed(String seed) {
		crawl(seed);

		try {
			logger.debug("Starting web server...");
			server.start();
			server.join();
		} catch (Exception e) {
			logger.debug(new RuntimeException(e));
		}
	}

	/**
	 * Crawl a seed URL
	 * 
	 * @param index
	 * @param seed
	 */
	public void crawl(String seed) {
		// Finish indexing another seed first
		webCrawler.waitForIndexingFinish();
		logger.debug("Start crawling seed: " + seed);
		webCrawler.crawl(index, seed);
		cache = new HashMap<String, TreeSet<SearchedResult>>();
		webCrawler.waitForIndexingFinish();
		logger.debug("Finish crawling seed: " + seed);
	}

	/**
	 * Stop the search engine.
	 */
	public void stop() throws Exception {
		try {
			logger.debug("Stoping web server...");
			workers.shutdown();
			System.exit(0);
			server.stop();
		} catch (Exception e) {
			logger.debug(e);
			System.exit(-1);
		}
	}

	/**
	 * Search a query
	 * 
	 * @param query
	 * @return collection of SearchedResult for that query
	 */
	public TreeSet<SearchedResult> search(String query) {
		// Wait if there's the crawler is still indexing
		webCrawler.waitForIndexingFinish();

		if (cache.containsKey(query)) {
			return cache.get(query);
		} else {
			ThreadedPartialSearcher searcher = new ThreadedPartialSearcher(
					workers);
			logger.debug("Start search query: " + query);
			searcher.search(query, index);
			TreeSet<SearchedResult> results = searcher.getResults();
			cache.put(query, results); // should synchronized
			return results;
		}
	}

	/**
	 * Get the only search engine.
	 * 
	 * @return
	 */
	public static SearchEngine getGlobalSearchEngine() {
		return globalSearchEngine;
	}

}
