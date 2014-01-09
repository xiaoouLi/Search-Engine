// Xiaoou, Dec,2013
// xli65@usfca.edu

package searcher;

import indexer.InvertedIndex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * PartialSearcher is used to deal with one multi-word search query, and return the sorted list of results from your
 * inverted index that start with the query word(s).
 * 
 * Normally, call the constructor, then it will return the proper result.
 * 
 * @author xli65
 * 
 */
public class PartialSearcher {
    protected HashMap<String, SearchedResult> resultsByDoc = new HashMap<String, SearchedResult>();

    /**
     * Search an query from an inverted index
     * 
     * @param query
     * @param invertedIndex
     */
    public void search(String query, InvertedIndex invertedIndex) {
        ArrayList<String> words = splitQuery(query);
        if (words.isEmpty()) {
            return;
        }

        Iterator<String> itor = words.iterator();

        resultsByDoc = invertedIndex.partialSearch(itor.next());

        while (itor.hasNext()) {
            mergeResult(resultsByDoc, invertedIndex.partialSearch(itor.next()));
        }
    }

    /**
     * Merge sub results to results
     * 
     * @param resultsByDoc
     * @param partialSearch
     */
    protected void mergeResult(HashMap<String, SearchedResult> resultsByDoc,
            HashMap<String, SearchedResult> subResultsByDoc) {
        for (String doc : subResultsByDoc.keySet()) {
            if (resultsByDoc.containsKey(doc)) {
                resultsByDoc.get(doc).merge(subResultsByDoc.get(doc));
            } else {
                resultsByDoc.put(doc, subResultsByDoc.get(doc));
            }
        }
    }

    /**
     * Get sorted results
     * 
     * @return results
     */
    public TreeSet<SearchedResult> getResults() {
        TreeSet<SearchedResult> results = new TreeSet<SearchedResult>();
        for (SearchedResult result : resultsByDoc.values()) {
            results.add(result);
        }
        return results;
    }

    /**
     * Split a query line into multiple queries by regular expression.
     * 
     * @param multiQuery
     *            a query line which is read in from the query file.
     * @return list of queries
     */
    protected static ArrayList<String> splitQuery(String multiQuery) {
        ArrayList<String> multiQueries = new ArrayList<String>();
        multiQuery = multiQuery.replaceAll("[^\\s0-9a-zA-Z]", "")
                .replaceAll("[\\s]+", " ").trim();
        for (String query : multiQuery.split(" ")) {
            multiQueries.add(query);
        }
        return multiQueries;
    }
}
