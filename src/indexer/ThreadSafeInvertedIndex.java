// Xiaoou, Sep,2013
// xli65@usfca.edu

package indexer;

import java.util.HashMap;

import org.apache.log4j.Logger;

import searcher.SearchedResult;
import util.MultiReaderLock;

/**
 * InvertedIndex is the data structure for storing the index I use MultiReaderlock to ensure the synchronizations.
 * 
 * @author xli65
 * 
 */
public class ThreadSafeInvertedIndex extends InvertedIndex {
    @SuppressWarnings("unused")
    private final Logger logger = Logger.getLogger(ThreadSafeInvertedIndex.class);

    private final MultiReaderLock lock = new MultiReaderLock();

    /**
     * Add word and its file and its position into inverted index Not thread safe, so we have to obtain write-lock
     * first, and unlock write-lock after done.(which is done in the lockWrite and unlockWrite method.)
     * 
     * @param absolutePath
     * @param word
     * @param index
     */
    @Override
    public void add(String word, String path, int index) {
        lock.lockWrite();
        super.add(word, path, index);
        lock.unlockWrite();
    }

    /**
     * Add the sub InvertedIndex into the big one
     * 
     * @param subIndex
     */
    @Override
    public void addAll(InvertedIndex subIndex) {
        lock.lockWrite();
        super.addAll(subIndex);
        lock.unlockWrite();
    }

    @Override
    public HashMap<String, SearchedResult> partialSearch(String query) {
        lock.lockRead();
        HashMap<String, SearchedResult> result = super.partialSearch(query);
        lock.unlockRead();
        return result;
    }
}
