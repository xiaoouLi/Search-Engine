// Xiaoou, Sep,2013
// xli65@usfca.edu

package crawler;

import indexer.InvertedIndex;

import java.io.File;

import parser.FileParser;

/**
 * Traverse specified directory, and generate an InvertedIndex
 * 
 * @author xli65
 * 
 */
public class DirectoryTraverser {
    /**
     * Traverse the directory, building an InvertedIndex out of all text files.
     * 
     * @param file
     *            the directory that will be parsed
     */
    public void parseDir(File file, InvertedIndex invertedIndex) {
        if (file.exists()) {
            if (!file.isHidden()) {
                if (file.isDirectory()) {
                    for (File f : file.listFiles()) {
                        parseDir(f, invertedIndex);
                    }
                } else {
                    if (file.getName().toLowerCase().endsWith(".txt")) {
                        FileParser.parseFile(file, invertedIndex);
                    }
                }
            }
        }
    }
}
