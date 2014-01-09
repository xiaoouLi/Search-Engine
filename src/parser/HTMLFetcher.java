package parser;

import static server.Constant.*;
import indexer.InvertedIndex;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;

import database.SearchEngineDatabaseHandler;

public class HTMLFetcher {
    private static final int PORT = 80;
    private final URL url;
    private final ArrayList<String> words;
    private final LinkedHashSet<String> links;
    private final InvertedIndex subIndex;
    private final StringBuffer htmlStringBuffer = new StringBuffer();
    private int position = 1;

    public HTMLFetcher(String urlString, InvertedIndex index) throws Exception {
        url = new URL(urlString);
        subIndex = index;
        words = new ArrayList<String>();
        links = new LinkedHashSet<String>();
    }

    private String createRequest() {
        String resource = url.getFile().isEmpty() ? "/" : url.getFile();
        String request = "GET " + resource + " HTTP/1.1\n" + "Host: " + url.getHost()
                + "\n" + "Connection: close\n\r\n";
        return request;
    }

    public static String getURLFromUserInput() {
        BufferedReader reader = null;
        String url = null;

        try {
            System.out.print("Input URL: ");
            reader = new BufferedReader(new InputStreamReader(System.in));
            url = reader.readLine();
        } catch (Exception ex) {
            System.out
                    .println("Warning: Could not properly get URL from user.");
        } finally {
            try {
                reader.close();
            } catch (Exception ignored) {
            }
        }

        return url;
    }

    public void fetch() {
        BufferedReader reader = null;
        PrintWriter writer = null;
        Socket socket = null;

        try {
            // Send message to specified port of server. getHost: get domain
            socket = new Socket(this.url.getHost(), PORT);

            // Write to the client socket, and client socket will send the
            // request to the server socket
            writer = new PrintWriter(socket.getOutputStream());

            // Get information from client socket. the information is replied by
            // the server socket
            reader = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));

            String request = createRequest();

            // Send request to server
            writer.println(request);
            writer.flush();

            String line = null;

            // Skip header block
            while ((line = reader.readLine()) != null) {

                if (line.startsWith("HTTP/1")) {
                    if (!line.contains("200")) {
                        reader.close();
                        writer.close();
                        socket.close();
                        return;
                    }
                }

                if (line.startsWith("Content-Type:")) {
                    if (!line.contains("text/html")) {
                        reader.close();
                        writer.close();
                        socket.close();
                        return;
                    }
                }
                if (line.trim().isEmpty()) {
                    break;
                }
            }

            while ((line = reader.readLine()) != null) {
                htmlStringBuffer.append(line).append(" ");
            }

            String html = htmlStringBuffer.toString();
            String cleanedHtml = HTMLCleaner.cleanHTML(htmlStringBuffer.toString());
            String snippet = cleanedHtml.length() > SNIPPET_SIZE ? cleanedHtml.substring(0,
                    SNIPPET_SIZE)
                    : cleanedHtml;

            links.addAll(HTMLLinkParser.extractLinks(html));
            words.addAll(parseWordsFromCleanedHTML(cleanedHtml));
            for (String word : words) {
                subIndex.add(word, url.toString(), position++);
            }
            SearchEngineDatabaseHandler.getGlobalDatabaseHandler().storePageSnippet(url.toString(), snippet);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            try {
                reader.close();
                writer.close();
                socket.close();
            } catch (Exception ignored) {
            }
        }
    }

    public LinkedHashSet<String> getLinks() {
        return links;
    }

    /**
     * parseWordsFromCleanedHTML
     * 
     * @param cleanedHtml
     * @return
     */
    public static ArrayList<String> parseWordsFromCleanedHTML(String cleanedHtml) {
        ArrayList<String> words = new ArrayList<String>();
        if (cleanedHtml == null)
            return words;

        for (String word : cleanedHtml.split("\\s+")) {
            word = word.toLowerCase().replaceAll("[\\W_]+", "").trim();

            if (!word.isEmpty()) {
                words.add(word);
            }
        }
        return words;
    }
}
