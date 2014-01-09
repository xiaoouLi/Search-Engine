// Xiaoou, Sep,2013
// xli65@usfca.edu

package indexer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import searcher.SearchedResult;

/**
 * InvertedIndex is the data structure that store the index.
 * 
 * @author xli65
 * 
 */
public class InvertedIndex {
    private final TreeMap<String, TreeMap<String, TreeSet<Integer>>> invertedIndexMap = new TreeMap<String, TreeMap<String, TreeSet<Integer>>>();

    private final Logger logger = Logger.getLogger(InvertedIndex.class);

    /**
     * Add one record into InvertedIndex instance record contains word, path, position
     * 
     * @param word
     * @param path
     * @param position
     */
    public void add(String word, String path, int position) {
        logger.debug("Add word: " + word + " from uri: " + path + " at: " + position);

        if (!invertedIndexMap.containsKey(word)) {
            invertedIndexMap.put(word, new TreeMap<String, TreeSet<Integer>>());
        }

        TreeMap<String, TreeSet<Integer>> pathMap = invertedIndexMap.get(word);

        if (!pathMap.containsKey(path)) {
            pathMap.put(path, new TreeSet<Integer>());
        }

        invertedIndexMap.get(word).get(path).add(position);
    }

    public HashMap<String, SearchedResult> partialSearch(String word) {
        logger.debug("Start partial search in index");
        HashMap<String, SearchedResult> resultsByDoc = new HashMap<String, SearchedResult>();

        String regex = "(?i)^" + word + "[\\w]*";
        Pattern p = Pattern.compile(regex);
        Matcher m = null;
        for (String w : invertedIndexMap.tailMap(word).keySet()) {
            m = p.matcher(w);
            if (m.find()) {
                logger.debug("find word: " + w);
                for (String doc : invertedIndexMap.get(w).keySet()) {
                    if (!resultsByDoc.containsKey(doc)) {
                        resultsByDoc.put(doc, new SearchedResult(doc,
                                invertedIndexMap.get(w).get(doc).size(),
                                invertedIndexMap.get(w).get(doc).first()));
                    } else {
                        resultsByDoc.put(doc, resultsByDoc.get(doc)
                                .update(invertedIndexMap.get(w).get(doc).size(),
                                        invertedIndexMap.get(w).get(doc).first()));
                    }
                }
            } else {
                break;
            }
        }
        logger.debug("finish partial search in index");
        return resultsByDoc;
    }

    public void addAll(InvertedIndex subIndex) {
        for (String word : subIndex.invertedIndexMap.keySet()) {
            if (!invertedIndexMap.containsKey(word)) {
                invertedIndexMap.put(word,
                        new TreeMap<String, TreeSet<Integer>>());
            }
            invertedIndexMap.get(word).putAll(subIndex.invertedIndexMap.get(word));
        }
    }

    /**
     * Write the content of inverted index into specified file
     * 
     * @param file
     *            specified file
     */
    public void writeToFile(String file) {
        File output = new File(file);
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(output)));
            for (String word : invertedIndexMap.keySet()) {
                writer.println(word);
                for (String path : invertedIndexMap.get(word).keySet()) {
                    writer.print("\"" + path + "\"");
                    for (Integer index : invertedIndexMap.get(word).get(path)) {
                        writer.print(", " + index);
                    }
                    writer.println();
                }
                writer.println();
                writer.flush();
            }
        } catch (FileNotFoundException e) {
            System.out.print(String.format(
                    "The search output file path %s is illegal!", file));
            System.out.println(e.getMessage());
            return;
        } catch (IOException e) {
            System.out.print(String.format("An exception occurrs during writing inverted index to \"%s\" file.", file));
            System.out.println(e.getMessage());
            return;
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}
