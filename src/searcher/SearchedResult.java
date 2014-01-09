// Xiaoou, Dec,2013
// xli65@usfca.edu

package searcher;

import database.SearchEngineDatabaseHandler;

/**
 * Search result for a query,
 * 
 * A Search result contains
 * 
 * 1. url,
 * 
 * 2. frequency,
 * 
 * 3. position,
 * 
 * 4. page snippet
 * 
 * Search result with higher frequency, lower position, is "larger" than the other one.
 * 
 * @author xli65
 */
public class SearchedResult implements Comparable<SearchedResult> {
    private final String url;
    private int frequency;
    private int position;
    private final String snippet;

    public SearchedResult(String url, int frequency, int position) {
        this.url = url;
        this.frequency = frequency;
        this.position = position;
        this.snippet = SearchEngineDatabaseHandler.getGlobalDatabaseHandler().getSnippetForUrl(url);
    }

    /**
     * @return url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @return frequency
     */
    public int getFrequency() {
        return frequency;
    }

    /**
     * @return position
     */
    public int getPosition() {
        return position;
    }

    /**
     * @return snippet
     */
    public String getSnippet() {
        return snippet;
    }

    /**
     * Override the compareTo method of comparable interface. make the compare rules which is required in this project2.
     */
    @Override
    public int compareTo(SearchedResult s) {
        SearchedResult s2 = s;
        if (getFrequency() < s2.getFrequency()) {
            return 1;
        } else if (getFrequency() == s2.getFrequency()) {
            if (getPosition() > s2.getPosition()) {
                return 1;
            } else if (getPosition() == s2.getPosition()) {
                return this.url.compareTo(s2.url);
            }
        }
        return -1;
    }

    /**
     * Update the information of search result with particular file path
     * 
     * @param new frequency value
     * @param new position value
     * @return
     */
    public SearchedResult update(int frequency2, int position2) {
        frequency = frequency + frequency2;
        position = Math.min(position, position2);
        return this;
    }

    /**
     * Merge another search result with self
     * 
     * @param searchedResult
     */
    public void merge(SearchedResult searchedResult) {
        frequency = frequency + searchedResult.frequency;
        position = Math.min(position, searchedResult.position);
    }

    @Override
    public String toString() {
        return String.format("\"%s\", %d, %d%n", url, frequency, position);
    }
}
