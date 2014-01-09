// Xiaoou, Dec,2013
// xli65@usfca.edu

package server.sevlet;

import static util.Precondition.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import database.SearchEngineDatabaseHandler;

@SuppressWarnings("serial")
public class BaseServlet extends HttpServlet {
    protected static Logger logger = Logger.getLogger(BaseServlet.class);

    private static Set<String> loginUsers = Collections.synchronizedSet(new HashSet<String>());
    private String currentUsername = null;

    protected static SearchEngineDatabaseHandler db = SearchEngineDatabaseHandler.getGlobalDatabaseHandler();
    protected ArrayList<String> messages = new ArrayList<String>();

    /**
     * Check whether user is logged in or not.
     * 
     * @param request
     * @return
     */
    public boolean checkLoggedin(HttpServletRequest request) {
        if (checkLoggedinFromCookies(request) || checkLoggedinFromParams(request)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Check user is already logged in.
     * 
     * @param request
     * @return
     */
    public boolean checkLoggedinFromCookies(HttpServletRequest request) {
        Map<String, String> cookies = getCookieMap(request);
        String username = cookies.get("username");

        if (username != null &&
                !username.isEmpty() &&
                ("true".equals(cookies.get("login")))) {
            setCurrentUser(username);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Check user post correct info to login.
     * 
     * @param request
     * @return
     */
    public boolean checkLoggedinFromParams(HttpServletRequest request) {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (validateUsername(username) && validatePassword(password)) {
            if (db.userLogin(username, password)) {
                setCurrentUser(username);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Get current user
     */
    public String getCurrentUser() {
        return currentUsername;
    }

    /**
     * Set current user
     */
    public void setCurrentUser(String username) {
        this.currentUsername = username;
        loginUsers.add(username);
    }

    public void debugMessages() {
        for (String msg : messages) {
            logger.debug("msg: " + msg);
        }
    }

    /**
     * @param response
     * @throws IOException
     */
    public static void redirectTo(HttpServletResponse response, String path) throws IOException {
        response.sendRedirect(response.encodeRedirectURL(path));
    }

    /**
     * @param request
     * @return
     */
    protected void checkMessagesFromRequest(HttpServletRequest request) {
        messages = new ArrayList<String>();
        if (request.getParameter("message") != null) {
            String msg = request.getParameter("message");
            switch (msg) {
            case "missing_cmd": {
                messages.add("Error: missing command!");
                break;
            }
            case "missing_seed": {
                messages.add("Error: missing seed url!");
                break;
            }
            case "crawl_done": {
                messages.add("Success: finished crawling, start to search.");
                break;
            }
            case "blank_query": {
                messages.add("Error: please enter some words to search.");
                break;
            }
            case "non_login": {
                messages.add("Error: please login first");
                break;
            }
            case "invalid_params": {
                messages.add("Error: thanks for your testing :) ");
                break;
            }
            case "finish_register": {
                messages.add("Success: please login use the username/password just registered");
                break;
            }
            case "incorrect_url_form": {
                messages.add("Error: please enter a correct url");
                break;
            }
            default:
                logger.debug("Not support: " + msg);
                messages.add("Error");
            }
        }
    }

    /**
     * @param username
     * @return
     */
    protected boolean validateUsername(String username) {
        if (checkStringNotBlank(username) && username.trim().length() <= 20
                && username.replaceAll("\\w", "").trim().isEmpty()) {
            return true;
        } else {
            messages.add("Error: username should be less than 20 charactors, not blank, contains only letters and numbers");
            return false;
        }
    }

    /**
     * @param password
     * @return
     */
    protected boolean validatePassword(String password) {
        if (checkStringNotBlank(password) && password.length() <= 20) {
            return true;
        } else {
            messages.add("Error: password should be less than 20 charactors, and not blank");
            return false;
        }
    }

    // Can only remove current user from login users
    protected void remoteCurrentUserFromLoginUsers() {
        if (checkStringNotBlank(currentUsername)) {
            loginUsers.remove(currentUsername);
        }
    }

    // Prevent others to modify login users
    protected Set<String> getLoginUsers() {
        return new HashSet<String>(loginUsers);
    }

    /**
     * Get cookies from a request.
     * 
     * @param request
     * @return
     */
    private Map<String, String> getCookieMap(HttpServletRequest request) {
        HashMap<String, String> map = new HashMap<String, String>();

        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                logger.debug("get cookie: " + cookie.getName() + " value: " + cookie.getValue());
                map.put(cookie.getName(), cookie.getValue());
            }
        }

        return map;
    }

}
