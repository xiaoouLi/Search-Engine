// Xiaoou, Dec,2013
// xli65@usfca.edu

package server.sevlet;

import static util.Precondition.*;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * When user click a search result, it will record here as visited page.
 * 
 * @author xli65
 * 
 */
@SuppressWarnings("serial")
public class PreviewServlet extends BaseServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (checkLoggedinFromCookies(request)) {
            String url = request.getParameter("url");

            if (!checkStringNotBlank(url)) {
                logger.debug("url is empty");
                redirectTo(response, "/");
            } else {
                db.storeVisitedPage(getCurrentUser(), url);
                logger.debug("url: " + url);
                redirectTo(response, url);
            }
        } else {
            logger.debug("need login first");
            redirectTo(response, "/");
        }
    }
}
