// Xiaoou, Dec,2013
// xli65@usfca.edu

package driver;

import org.apache.log4j.Logger;

import server.SearchEngine;
import util.ArgumentParser;

/**
 * @author xli65 Driver for the project5
 * 
 */
public class Driver {
    private String seedUrl;
    private int port;
    private int numOfThreads;
    private static SearchEngine engine;

    private static Logger logger = Logger.getLogger(Driver.class);

    /**
     * -u <Url> where -u indicates the next argument <Url> is a seed URL that must be processed and added to an inverted
     * index data structure
     * 
     * p <port> where -p indicates the next argument <port> is the port the web server should use to accept socket
     * connections.
     * 
     * t <threads> where -t indicates the next argument <threads> is the number of threads to use in the work queue.
     * 
     * @param args
     * @return
     */
    private boolean checkArgs(String[] args) {
        ArgumentParser argsMap = new ArgumentParser(args);

        if (argsMap.hasFlag("-debug")) {
            this.seedUrl = "http://cs.usfca.edu/~xli65/setest/folder1/seed";
            this.port = 8080;
            this.numOfThreads = 3;
            return true;
        }

        if (!argsMap.hasFlag("-u")) {
            System.out.println("Please provide -u <seed URL>! Exit!");
            return false;
        }

        if (!argsMap.hasFlag("-t")) {
            System.out.println("Please provide -t <number of threads>! Exit!");
            return false;
        }

        if (!argsMap.hasFlag("-p")) {
            System.out.println("Please provide -p <port number>! Exit!");
            return false;
        }

        this.seedUrl = argsMap.getValue("-u");

        try {
            port = Integer.parseInt(argsMap.getValue("-p"));
        } catch (NumberFormatException e) {
            System.out.println("Port must be an integer!");
            return false;
        }
        if (port <= 0) {
            System.out.println("Port must bigger than 0!");
            return false;
        }

        try {
            numOfThreads = Integer.parseInt(argsMap.getValue("-t"));
        } catch (NumberFormatException e) {
            System.out.println("thread num must be an integer!");
            return false;
        }
        if (numOfThreads <= 0) {
            System.out.println("Thread num must bigger than 0!");
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        Driver driver = new Driver();

        if (!driver.checkArgs(args)) {
            String usage = "Usage: -u <seed_url>\n" + "-p <port>\n" + "-t <threads>";
            System.out.println(usage);
            return;
        }
        engine = new SearchEngine(driver.port, driver.numOfThreads);
        engine.startWithSeed(driver.seedUrl);
        logger.debug("SearchEngine started!");
    }

    public static SearchEngine getSearchEngine() {
        return engine;
    }

}