// Xiaoou, Dec,2013
// xli65@usfca.edu

package util;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;

import exception.PreconditionException;

/**
 * @author xli65
 */
public class Precondition {

    /**
     * Check string is empty or not
     */
    public static boolean checkStringNotBlank(String text) {
        if (text != null && !text.trim().isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param request
     * @param start
     * @param errors
     * @return
     */
    public static int parseParamToInt(HttpServletRequest request, String name, int defaultValue) {
        int value = defaultValue;

        if (request.getParameter(name) != null && !request.getParameter(name).isEmpty()) {
            try {
                value = Integer.parseInt(request.getParameter(name));
            } catch (NumberFormatException e) {
                throw new PreconditionException(e);
            }
        } else {
            return defaultValue;
        }

        return value;
    }

    /**
     * @param url
     * @return
     */
    public static boolean validateURL(String url) {
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }
}
