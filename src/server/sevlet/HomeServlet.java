// Xiaoou, Dec,2013
// xli65@usfca.edu

package server.sevlet;

import static database.SearchEngineDatabaseHandler.*;
import static util.HTMLRenderHelper.*;
import static util.Precondition.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class HomeServlet extends BaseServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        checkMessagesFromRequest(request);

        PrintWriter writer = response.getWriter();
        String command = request.getParameter("cmd");
        if (!checkStringNotBlank(command)) {
            if (checkLoggedinFromCookies(request)) {
                prepareResponse("Welcome Back", writer);
                renderMessages(writer, messages);
                renderUserPage(writer);
            } else {
                prepareResponse("Welcome", writer);
                renderMessages(writer, messages);
                renderNonUserPage(writer);
            }
        } else {
            logger.debug("Get command: " + command);
            switch (command) {
            case "login":
                String username = request.getParameter("username");
                String password = request.getParameter("password");
                prepareResponse("Login", writer);

                if (!validateUsername(username)) {
                    renderMessages(writer, messages);
                    renderNonUserPage(writer);
                    finishResponse(response);
                    return;
                }

                if (!validatePassword(password)) {
                    renderMessages(writer, messages);
                    renderNonUserPage(writer);
                    finishResponse(response);
                    return;
                }

                if (db.userLogin(username, password)) {
                    setCurrentUser(username);
                    response.addCookie(new Cookie("login", "true"));
                    response.addCookie(new Cookie("username", getCurrentUser()));
                    messages.add("Success: logged in!");
                    renderMessages(writer, messages);
                    renderUserPage(writer);
                } else {
                    messages.add("Error: wrong username or password");
                    renderMessages(writer, messages);
                    renderNonUserPage(writer);
                }

                break;

            case "logout":
                prepareResponse("Logout", writer);
                if (checkLoggedinFromCookies(request)) {
                    remoteCurrentUserFromLoginUsers();
                }

                response.addCookie(new Cookie("login", "false"));
                response.addCookie(new Cookie("username", ""));
                messages.add("Success: logged out.");
                renderMessages(writer, messages);
                renderNonUserPage(writer);
                break;

            case "change_password":
                prepareResponse("Change password", writer);

                if (checkLoggedinFromCookies(request)) {
                    String newPassword = request.getParameter("new_password");
                    response.addCookie(new Cookie("login", "false"));
                    response.addCookie(new Cookie("username", ""));
                    if (validatePassword(newPassword)) {
                        db.changePassword(getCurrentUser(), newPassword);
                        messages.add("Success: password changed, please login using new password");
                    }
                } else {
                    messages.add("Error: you need to login before change your password.");
                }

                renderMessages(writer, messages);
                renderNonUserPage(writer);
                break;

            case "clear_history":
                prepareResponse("Clear history", writer);

                if (checkLoggedinFromCookies(request)) {
                    db.clearQueryHistory(getCurrentUser());
                    messages.add("Success: history cleared.");
                } else {
                    messages.add("Error: you need to login before clear history.");
                }

                renderMessages(writer, messages);
                renderUserPage(writer);
                break;

            case "save_favorite_link":
                prepareResponse("Save favorite link", writer);

                if (checkLoggedinFromCookies(request)) {
                    String url = request.getParameter("url");
                    if (url == null || url.isEmpty()) {
                        messages.add("Error: missing the favorite URL");
                    } else {
                        db.storeFavoriateLinks(getCurrentUser(), url);
                        messages.add("Success: favorite link saved.");
                    }
                    renderMessages(writer, messages);
                    renderUserPage(writer);
                } else {
                    messages.add("Error: you need to login before saving favorite link.");
                    renderMessages(writer, messages);
                    renderNonUserPage(writer);
                }
                break;

            case "save_favorite_query":
                prepareResponse("Save favorite query", writer);

                if (checkLoggedinFromCookies(request)) {
                    String query = request.getParameter("query");
                    if (query == null || query.trim().isEmpty()) {
                        messages.add("Error: missing the favorite query");
                    } else {
                        db.storeFavoriateQueries(getCurrentUser(), query);
                        messages.add("Success: favorite query saved.");
                    }
                    renderMessages(writer, messages);
                    renderUserPage(writer);
                } else {
                    messages.add("Error: you need to login before saving favorite query.");
                    renderMessages(writer, messages);
                    renderNonUserPage(writer);
                }
                break;

            default:
                prepareResponse("Welcome", writer);
                renderNonUserPage(writer);
                logger.debug("Command " + command + " is not supported");
            }
        }

        finishResponse(response);
    }

    /**
     * Render page for non login user
     * 
     * @param writer
     */
    private void renderNonUserPage(PrintWriter writer) {
        renderLoginForm(writer);
        renderNewUserRegistrationLink(writer);
    }

    /**
     * Render page for login user
     * 
     * Yes, render query history, visited page, favorite queries, favorite links together would be slow, since there are
     * database request for each part. But, in the future, they can be rewrite into AJAX manner for performance
     * improvement, which takes more time and not in extra credit list.
     * 
     * @param writer
     */
    private void renderUserPage(PrintWriter writer) {
        String currentUser = getCurrentUser();
        renderCurrentUser(writer, currentUser);
        renderSearchForm(writer);

        ResultSet queries = db.getQueryHistory(currentUser);
        renderHistoryQueries(writer, queries);
        recycleDBResource(queries);

        renderClearHistoryForm(writer, currentUser);

        ResultSet visitedPages = db.getVisitedPages(currentUser);
        renderVisitedPages(writer, visitedPages);
        recycleDBResource(visitedPages);

        ResultSet favoriteQueries = db.getFavoriteQueries(currentUser);
        renderFavoriteQueries(writer, favoriteQueries);
        recycleDBResource(favoriteQueries);

        ResultSet favoriteLinks = db.getFavoriteLinks(currentUser);
        renderFavoriteLinks(writer, favoriteLinks);
        recycleDBResource(favoriteLinks);

        ResultSet suggestedQueries = db.getSuggestedQueries(currentUser);
        renderSuggestedQueries(writer, suggestedQueries);
        recycleDBResource(suggestedQueries);

        renderChangePasswordForm(writer, currentUser);
        renderLogoutForm(writer);

        renderLastLoginTime(writer, currentUser);
        renderLoginUsers(writer, getLoginUsers());
    }

    /**
     * @param writer
     * @param currentUser
     */
    private void renderLastLoginTime(PrintWriter writer, String currentUser) {
        String lastLoginTime = db.getLastLoginTime(currentUser);
        if (checkStringNotBlank(lastLoginTime)) {
            writer.println("<p>Last login time: " + lastLoginTime + "</p>");
        } else {
            writer.println("<p>This is your first time login.</p>");
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }
}
