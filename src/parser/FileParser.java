// Xiaoou, Dec,2013
// xli65@usfca.edu

package parser;

import indexer.InvertedIndex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * The class FileParser provides a simple static method: parseFile(File file, InvertedIndex invertedIndex), which takes
 * in a file, parse all words in the file, and add each word in the file to the inverted index.
 * 
 * @author xli65
 * 
 */
public class FileParser {
    public static void parseFile(File file, InvertedIndex temp) {
        int index = 0;
        BufferedReader buffer = null;
        String line = null;
        StringBuffer sb = new StringBuffer();
        try {
            buffer = new BufferedReader(new FileReader(file));
            while ((line = buffer.readLine()) != null) {
                // process the line, removing \s, \n, \t and non-char,
                // non-digits
                line = line.replaceAll("[^\\s0-9a-zA-Z]", "")
                        .replaceAll("\\s+", " ").trim();
                if (line.isEmpty()) {
                    continue;
                } else {
                    // all stored in lower case, and change query into lower
                    // case too
                    line = line.toLowerCase();
                    sb.append(line).append(" ");
                }
            }
            for (String word : sb.toString().split(" ")) {
                if (word.isEmpty()) {
                    continue;
                }
                temp.add(word, file.getAbsolutePath(), ++index);
            }
        } catch (FileNotFoundException e) {
            System.out.println("File cannot be found!");
            System.out.println(e.getMessage());
            return;
        } catch (IOException e) {
            System.out.println("File reading goes wrong! Exit!");
            System.out.println(e.getMessage());
            return;
        } finally {
            if (buffer != null) {
                try {
                    buffer.close();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }
}