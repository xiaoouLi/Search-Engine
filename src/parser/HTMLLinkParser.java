// Xiaoou, Dec,2013
// xli65@usfca.edu

package parser;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/*
 * Assumptions:
 * Assume the HTML is valid (i.e. passes http://validator.w3.org/) and
 * all href attributes are quoted and URL encoded.
 */

public class HTMLLinkParser {

    public static final String regex = "(?i)<a.*?href[ ]*?=[ ]*?\"(.*?)\".*?>";

    private static final Logger logger = Logger.getLogger(HTMLLinkParser.class);

    /*
     * If you only have one capturing group for the link, you can use this function directly. Otherwise, you will need
     * to change which group is added to the list of links.
     */
    public static ArrayList<String> extractLinks(String text) {
        ArrayList<String> links = new ArrayList<String>();

        Pattern p = Pattern.compile(regex);

        int start = 0;
        text = text.replaceAll("\n", " ");
        text = text.replaceAll("\\s", " ");

        Matcher m = p.matcher(text);

        while (m.find(start)) {
            links.add(m.group(1));
            start = m.end();
        }

        if (logger.isDebugEnabled() && !links.isEmpty()) {
            logger.debug("Get links: ");
            for (String link : links) {
                logger.debug("\t" + link);
            }
        }

        return links;
    }
}