package parser;


/*
 * This class does not take a particularly efficient approach, but this
 * simplifies the process of retreiving and cleaning html code for your
 * web crawler project later.
 */

/**
 * A helper class with several static methods that will help fetch a webpage, strip out all of the html, and parse the
 * resulting plain text into words. Meant to be used for the web crawler project.
 * 
 * @author CS 212 Software Development
 * 
 */
public class HTMLCleaner {

    /**
     * Removes all style and script tags (and any text between those tags), all HTML tags, and all special
     * characters/entities.
     * 
     * @param html
     *            html code to parse
     * @return plain text
     */
    public static String cleanHTML(String html) {
        html = html.replaceAll("<!--.*?-->", " ");
        html = html.replaceAll("(?i)<(style|script).*?</(style|script)>", " ");
        html = html.replaceAll("(?i)<[^>]*?>", " ");
        return html.replaceAll("&[a-zA-Z0-9#]*;", " ");
    }
}