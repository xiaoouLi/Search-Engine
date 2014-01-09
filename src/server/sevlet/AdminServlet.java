// Xiaoou, Dec,2013
// xli65@usfca.edu

package server.sevlet;

import static util.HTMLRenderHelper.*;
import static util.Precondition.*;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import server.SearchEngine;

/**
 * Admin servlet for the server that handles
 * 
 * 1. crawl new seed
 * 
 * 2. shut down.
 * 
 * @author xli65
 */
@SuppressWarnings("serial")
public class AdminServlet extends BaseServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter writer = response.getWriter();
        prepareResponse("Admin", writer);
        checkMessagesFromRequest(request);
        renderNewCrawlForm(writer);
        renderShutDownForm(writer);
        finishResponse(response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String command = request.getParameter("cmd");
        if (command == null || command.isEmpty()) {
            redirectTo(response, "/admin?message=missing_cmd");
        } else {
            switch (command) {
            case "new_crawl":
                String seed = request.getParameter("seed");
                if (seed == null || seed.isEmpty()) {
                    redirectTo(response, "/admin?message=missing_seed");
                } else if (validateURL(seed)) {
                    SearchEngine.getGlobalSearchEngine().crawl(seed);
                    redirectTo(response, "/?message=crawl_done");
                } else {
                    redirectTo(response, "/admin?message=incorrect_url_form");
                }
                return;

            case "shut_down":
                try {
                    SearchEngine.getGlobalSearchEngine().stop();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                redirectTo(response, "http://cs.usfca.edu");
                return;

            default:
                logger.debug("not supported: " + command);
            }
            redirectTo(response, "/admin?message=error");
        }
    }

    /**
     * Render a shut down form for the search engine
     * 
     * @param writer
     */
    private void renderShutDownForm(PrintWriter writer) {
        writer.println("<h3>Shut down the server</h3>");
        writer.println("<form action='/admin' method='post'>");
        writer.println("\t<input type='submit' value='Shut down'></input>");
        writer.println("\t<input type='hidden' name='cmd' value='shut_down'></input>");
        writer.println("</form>");
    }

    /**
     * Render a new crawl form for the search engine
     * 
     * @param writer
     */
    private void renderNewCrawlForm(PrintWriter writer) {
        writer.println("<h3>Crawl one more seed</h3>");
        writer.println("<form action='/admin' method='post'>");
        writer.println("\t<input type='text' name='seed'></input>");
        writer.println("\t<input type='submit' value='Crawl'></input>");
        writer.println("\t<input type='hidden' name='cmd' value='new_crawl'></input>");
        writer.println("</form>");
    }
}
