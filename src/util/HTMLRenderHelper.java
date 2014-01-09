// Xiaoou, Dec,2013
// xli65@usfca.edu

package util;

import static util.DayTimeHelper.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import searcher.SearchedResult;

/**
 * Helper for rendering html code
 * 
 * @author xli65
 */
public class HTMLRenderHelper {
    private static Logger logger = Logger.getLogger(HTMLRenderHelper.class);

    public static void prepareResponse(String title, PrintWriter writer) {
        writer.println("<!DOCTYPE HTML PUBLIC '-//W3C//DTD HTML 4.01//EN' 'http://www.w3.org/TR/html4/strict.dtd'>");
        writer.println("<html>");
        writer.println("<head>");
        writer.println("\t<title>" + title + "</title>");
        writer.println("\t<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>");
        renderJavascript(writer);
        renderCSS(writer);
        writer.println("</head>");
        writer.println("<body>");

        // Logo is generated from here: http://www.flamingtext.com/logo/Design-Funtime
        writer.println("<img src='http://cs.usfca.edu/~xli65/cs212/project5/xiaoou_logo.png' alt='logo'>");
    }

    /**
     * @param writer
     */
    private static void renderCSS(PrintWriter writer) {
        String cssFile = "http://cs.usfca.edu/~xli65/cs212/project5/web.css";
        writer.printf("<link rel='stylesheet' type='text/css' href='" + cssFile + "'>");
    }

    /**
     * @param writer
     */
    private static void renderJavascript(PrintWriter writer) {
        String javascriptFile = "http://cs.usfca.edu/~xli65/cs212/project5/web.js";
        writer.println("<script type='text/javascript' src='http://code.jquery.com/jquery-1.10.2.min.js'></script>");
        writer.println("<script type='text/javascript' src='" + javascriptFile + "'></script>");
    }

    public static void renderLoginUsers(PrintWriter writer, Set<String> users) {
        String usersString = "";
        String[] userArray = users.toArray(new String[0]);
        int i;
        for (i = 0; i < userArray.length - 1; i++) {
            usersString += userArray[i] + ", ";
        }
        if (users.size() > 0) {
            usersString += userArray[i] + ".";
        }
        writer.println("<h4>All current online users:</h4><p>" + usersString + "</p>");
    }

    public static void finishResponse(HttpServletResponse response) {
        try {
            PrintWriter writer = response.getWriter();

            writer.printf("<p class='footer'>");
            writer.printf("Last updated at %s.", getDate());
            writer.println("</p>");
            writer.println("</body>");
            writer.println("</html>");
            writer.flush();

            response.setStatus(HttpServletResponse.SC_OK);
            response.flushBuffer();
        } catch (IOException ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
    }

    /**
     * Render Search result with pagination.
     * 
     * @param results
     * @param response
     * @param resultPerPage
     * @param start
     */
    public static void renderSearchResult(PrintWriter writer, TreeSet<SearchedResult> results, int start,
            int resultPerPage) {
        writer.println("<h3>Search Results: </h3>");

        if (results == null || results.isEmpty()) {
            writer.println("<p class='message'>Sorry, no results found!</p>");
            return;
        }

        int counter = 0;
        Iterator<SearchedResult> itor = results.iterator();
        // writer.println("<h3>Search Results: </h3>");
        writer.println("<ul>");
        while (itor.hasNext()) {
            SearchedResult page = itor.next();
            counter++;
            if (counter >= start && counter < (start + resultPerPage)) {
                logger.debug("counter: " + counter);
                renderSingleResult(writer, page, counter);
            } else if (counter >= (start + resultPerPage)) {
                break;
            }
        }
        writer.println("</ul>");
    }

    /**
     * Render pagination links
     * 
     * @param writer
     * @param query
     * @param total
     * @param start
     * @param resultPerPage
     */
    public static void renderPagenation(PrintWriter writer, String query, int total, int start, int resultPerPage) {
        int currentPage = start / resultPerPage + 1;
        logger.debug("Total: " + total);
        logger.debug("Start: " + start);
        logger.debug("CurrentPage: " + currentPage);

        for (int i = 1; i < currentPage; i++) {
            writer.println("<a href='/search?query=" + query +
                    "&start=" + (1 + (i - 1) * resultPerPage) +
                    "&result_per_page=" + resultPerPage + "'>" + i + "</a>");
        }
        writer.println("<a href='#'>" + currentPage + "</a>");
        for (int i = currentPage + 1; (i < (total / resultPerPage) + 1); i++) {
            writer.println("<a href='/search?query=" + query +
                    "&start=" + (1 + (i - 1) * resultPerPage) +
                    "&result_per_page=" + resultPerPage + "'>" + i + "</a>");
        }
        if ((total - ((total / resultPerPage) * resultPerPage)) > 0 &&
                currentPage < (total / resultPerPage)) {
            writer.println("<a href='/search?query=" + query +
                    "&start=" + (1 + ((total / resultPerPage) * resultPerPage)) +
                    "&result_per_page=" + resultPerPage + "'>" + ((total / resultPerPage) + 1) + "</a>");
        }
    }

    /**
     * Render single result
     * 
     * @param writer
     * @param page
     * @param counter
     */
    public static void renderSingleResult(PrintWriter writer, SearchedResult page, int counter) {
        writer.printf("<li class='search_result'>" +
                "<div class='result_count'>%d. </div>" +
                "<div class='result_url'>Url: <a href='/preview?url=%s'>%s</a></div>" +
                "<div class='result_frequency'>Frequency: %d times</div>" +
                "<div class='result_position'>First Occurrence: %d</div>" +
                "<div class='result_snippet'>Snippet: %s</div>" +
                "</li>",
                counter,
                page.getUrl(), page.getUrl(),
                page.getFrequency(),
                page.getPosition(),
                page.getSnippet());
        renderFavoriteLinkForm(writer, page.getUrl());
    }

    /**
     * Rendering history queries
     * 
     * @param writer
     * @param queries
     */
    @SuppressWarnings("deprecation")
    public static void renderHistoryQueries(PrintWriter writer, ResultSet queries) {
        if (queries == null) {
            logger.debug("queries is null");
            return;
        }

        int counter = 0;
        writer.println("<h3>Search History: </h3>");
        writer.println("<ul>");
        try {
            while (queries.next()) {
                counter++;
                String timeString = queries.getTime("queryTime").toLocaleString();
                timeString = timeString.substring(12, timeString.length());

                writer.printf("<li class='search_history'>%d. Searched: <b>%s</b>, at %s</li>",
                        counter,
                        queries.getString("query"),
                        timeString);
                renderFavoriteQueryForm(writer, queries.getString("query"));
            }
            if (counter == 0) {
                writer.printf("<p class='message'>No history</>");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        writer.println("</ul>");
    }

    /**
     * Render suggested queries
     * 
     * @param writer
     * @param suggestedQueries
     */
    public static void renderSuggestedQueries(PrintWriter writer, ResultSet suggestedQueries) {
        if (suggestedQueries == null) {
            logger.debug("suggestedQueries is null");
            return;
        }

        int counter = 0;
        writer.println("<h3>Suggested queries: </h3>");
        writer.println("<ul>");
        try {
            while (suggestedQueries.next()) {
                counter++;

                @SuppressWarnings("deprecation")
                String timeString = suggestedQueries.getTime("queryTime").toLocaleString();
                timeString = timeString.substring(12, timeString.length());

                writer.printf("<li class='suggested_query'>%d. <b>%s</b></li>",
                        counter,
                        suggestedQueries.getString("query"));
                renderFavoriteQueryForm(writer, suggestedQueries.getString("query"));
            }
            if (counter == 0) {
                writer.printf("<p class='message'>No suggested queries</p>");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        writer.println("</ul>");
    }

    /**
     * Render visited pages
     * 
     * @param writer
     * @param visitedPages
     */
    public static void renderVisitedPages(PrintWriter writer, ResultSet visitedPages) {
        if (visitedPages == null) {
            logger.debug("visitedPages is null");
            return;
        }

        int counter = 0;
        writer.println("<h3>Visited pages: </h3>");
        writer.println("<ul>");
        try {
            while (visitedPages.next()) {
                counter++;
                writer.printf("<li class='visited_page'>%d. Visited: <b>%s</b>, %d times</li>",
                        counter,
                        visitedPages.getString("url"),
                        visitedPages.getInt("count"));
            }
            if (counter == 0) {
                writer.printf("<p class='message'>No visited pages</p>");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        writer.println("</ul>");
    }

    /**
     * Render favorite links
     * 
     * @param writer
     * @param favoriteLinks
     */
    public static void renderFavoriteLinks(PrintWriter writer, ResultSet favoriteLinks) {
        if (favoriteLinks == null) {
            logger.debug("favoriteLinks is null");
            return;
        }

        int counter = 0;
        writer.println("<h3>Favorite links: </h3>");
        writer.println("<ul>");
        try {
            while (favoriteLinks.next()) {
                counter++;
                writer.printf("<li class='favorite_link'>%d. Url: <a href='/preview?url=%s'>%s</a></li>",
                        counter,
                        favoriteLinks.getString("url"),
                        favoriteLinks.getString("url"));
            }
            if (counter == 0) {
                writer.printf("<p class='message'>No links</p>");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        writer.println("</ul>");
    }

    /**
     * Render favorite queries
     * 
     * @param writer
     * @param favoriateLinks
     */
    public static void renderFavoriteQueries(PrintWriter writer, ResultSet favoriteQueries) {
        if (favoriteQueries == null) {
            logger.debug("favoriateQueries is null");
            return;
        }

        int counter = 0;
        writer.println("<h3>Favorite Queries: </h3>");
        writer.println("<ul>");
        try {
            while (favoriteQueries.next()) {
                counter++;
                writer.printf("<li class='favorite_query'>%d. query: <a href='/search?query=%s'>%s</a></li>",
                        counter,
                        favoriteQueries.getString("query"),
                        favoriteQueries.getString("query"));
            }
            if (counter == 0) {
                writer.printf("<p class='message'>No queries</p>");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        writer.println("</ul>");
    }

    /**
     * Render registration form
     * 
     * @param writer
     */
    public static void renderRegistrationForm(PrintWriter writer) {
        writer.println("<form action='/register' method='post' id='registration_form'>");
        writer.println("\t<label for='username_field'>Username:</label>");
        writer.println("\t<input type='text' name='username' size='30' id='username_field'>");
        writer.println("\t<label for='password_field'>Password:</label>");
        writer.println("\t<input type='password' name='password' size='30' id='password_field'>");
        writer.println("\t<input type='hidden' name='cmd' value='register'>");
        writer.println("\t<input type='submit' value='Register'>");
        writer.println("</form>");
    }

    /**
     * Render messages
     * 
     * @param writer
     * @param messages
     */
    public static void renderMessages(PrintWriter writer, List<String> messages) {
        for (String message : messages) {
            if (message.toLowerCase().startsWith("error:")) {
                writer.println("<p class='error'>" + message + "</p>");
            } else if (message.toLowerCase().startsWith("success:")) {
                writer.println("<p class='success'>" + message + "</p>");
            } else {
                writer.println("<p class='message'>" + message + "</p>");
            }
        }
    }

    /**
     * Render registration link
     * 
     * @param writer
     */
    public static void renderNewUserRegistrationLink(PrintWriter writer) {
        writer.println("<p>(<a href='/register' id='register_link'>new user? register here.</a>)</p>");
    }

    /**
     * Render search form with query string
     * 
     * @param writer
     * @param query
     */
    public static void renderSearchFormWithQuery(PrintWriter writer, String query) {
        writer.println("<h3>Enter query to search</h3>");
        writer.println("<form action='/search' method='post' id='search_form'>");
        writer.println("\t<input id='query', name='query' value='" + query + "'></input>");
        writer.println("\t<label for='result_per_page'>Result per page: </label>");
        writer.println("\t<select id='result_per_page', name='result_per_page'>");
        writer.println("\t\t<option value='5'>5</option>");
        writer.println("\t\t<option value='10'>10</option>");
        writer.println("\t\t<option value='20'>20</option>");
        writer.println("\t\t<option value='100'>100</option>");
        writer.println("\t</select>");
        writer.println("\t<label for='private_search'>Private search: </label>");
        writer.println("\t<select id='private_search', name='private_search'>");
        writer.println("\t\t<option value='false'>non private</option>");
        writer.println("\t\t<option value='true'>private</option>");
        writer.println("\t</select>");
        writer.println("\t<input type='submit' value='Search'></input>");
        writer.println("</form>");
    }

    /**
     * Render empty search form
     * 
     * @param writer
     */
    public static void renderSearchForm(PrintWriter writer) {
        renderSearchFormWithQuery(writer, "");
    }

    /**
     * Render login form
     * 
     * @param writer
     */
    public static void renderLoginForm(PrintWriter writer) {
        writer.println("<h3>Please login to search</h3>");
        writer.println("<form action='/' method='post' id='login_form'>");
        writer.println("\t<label for='username_field'>Username:</label>");
        writer.println("\t<input type='text' name='username' size='30' id='username_field'>");
        writer.println("\t<label for='password_field'>Password:</label>");
        writer.println("\t<input type='password' name='password' size='30' id='password_field'>");
        writer.println("\t<input type='hidden' name='cmd' value='login'>");
        writer.println("\t<input type='submit' value='Login'>");
        writer.println("</form>");
    }

    /**
     * Render change password form
     * 
     * @param writer
     * @param username
     */
    public static void renderChangePasswordForm(PrintWriter writer, String username) {
        writer.println("<h3>Change password here:</h3>");
        writer.println("<form action='/' method='post' id='change_password_form'>");
        writer.println("\t<label for='change_password_field'>New Password:</label>");
        writer.println("\t<input type='password' name='new_password' size='30' id='change_password_field'>");
        writer.println("\t<input type='hidden' name='cmd' value='change_password'>");
        writer.println("\t<input type='hidden' name='username' value='" + username + "'>");
        writer.println("\t<input type='submit' value='Change Password'>");
        writer.println("</form>");
    }

    /**
     * Render logout form
     * 
     * @param writer
     */
    public static void renderLogoutForm(PrintWriter writer) {
        writer.println("<form action='/' method='post' id='logout_form'>");
        writer.println("\t<input type='hidden' name='cmd' value='logout'>");
        writer.println("\t<input type='submit' value='Logout'>");
        writer.println("</form>");
    }

    /**
     * Render clear history form
     * 
     * @param writer
     */
    public static void renderClearHistoryForm(PrintWriter writer, String currentUser) {
        writer.println("<form action='/' method='post' id='clear_history_form'>");
        writer.println("\t<input type='hidden' name='cmd' value='clear_history'>");
        writer.println("\t<input type='submit' value='Clear history'>");
        writer.println("</form>");
    }

    /**
     * Render favorite link form
     * 
     * @param writer
     * @param url
     */
    public static void renderFavoriteLinkForm(PrintWriter writer, String url) {
        writer.println("<form action='/' method='post' id='favorite_link_form'>");
        writer.println("\t<input type='hidden' name='cmd' value='save_favorite_link'>");
        writer.println("\t<input type='hidden' name='url' value='" + url + "'>");
        writer.println("\t<input type='submit' value='Favorite this link'>");
        writer.println("</form>");
    }

    /**
     * Render favorite query form
     * 
     * @param writer
     * @param query
     */
    public static void renderFavoriteQueryForm(PrintWriter writer, String query) {
        writer.println("<form action='/' method='post' id='favorite_query_form'>");
        writer.println("\t<input type='hidden' name='cmd' value='save_favorite_query'>");
        writer.println("\t<input type='hidden' name='query' value='" + query + "'>");
        writer.println("\t<input type='submit' value='Favorite this query'>");
        writer.println("</form>");
    }

    /**
     * Render link to home page
     * 
     * @param writer
     */
    public static void renderHomePageLink(PrintWriter writer) {
        writer.println("<p><a href='/' id='home_link'>Home Page</a></p>");
    }

    public static void renderCurrentUser(PrintWriter writer, String username) {
        writer.println("<p>Hi, " + username + "!</p>");
    }
}
