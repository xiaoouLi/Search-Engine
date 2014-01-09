// Xiaoou, Dec,2013
// xli65@usfca.edu

package server.sevlet;

import static server.Constant.*;
import static util.HTMLRenderHelper.*;
import static util.Precondition.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import searcher.SearchedResult;
import server.SearchEngine;

/**
 * @author xli65
 * 
 */
@SuppressWarnings("serial")
public class SearchResultServlet extends BaseServlet {

	@SuppressWarnings("unused")
	private final Logger logger = Logger.getLogger(SearchResultServlet.class);

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		doPost(request, response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		PrintWriter writer = response.getWriter();

		// Check authentication
		if (!checkLoggedin(request)) {
			redirectTo(response, "/?message=non_login");
			return;
		}

		// Parse parameters from request
		String query = request.getParameter("query");

		int start = parseParamToInt(request, "start", DEFAULT_START);
		int resultPerPage = parseParamToInt(request, "result_per_page",
				DEFAULT_RESULT_PER_PAGE);

		// Check query not blank
		if (!checkStringNotBlank(query)) {
			redirectTo(response, "/?message=blank_query");
			return;
		}

		if (start <= 0 || resultPerPage <= 0) {
			redirectTo(response, "/?message=invalid_params");
			return;
		}

		prepareResponse("Search Result", writer);
		renderCurrentUser(writer, getCurrentUser());
		renderSearchFormWithQuery(writer, query);

		// Search
		Long startTime = System.nanoTime();
		TreeSet<SearchedResult> results = SearchEngine.getGlobalSearchEngine()
				.search(query);
		Long endTime = System.nanoTime();

		// Search Statistics
		renderSearchStatistics(writer, results.size(), endTime - startTime);
		renderSearchResult(writer, results, start, resultPerPage);
		renderPagenation(writer, query, results.size(), start, resultPerPage);

		// Store history if not private
		String privateSearchFlag = request.getParameter("private_search");

		if (!checkStringNotBlank(privateSearchFlag)
				|| !privateSearchFlag.equals("true")) {
			db.storeQueryHistory(getCurrentUser(), query);
		}

		renderHomePageLink(writer);
		renderLoginUsers(writer, getLoginUsers());
		finishResponse(response);
	}

	/**
	 * @param writer
	 * @param num
	 * @param time
	 */
	private void renderSearchStatistics(PrintWriter writer, int num, long time) {
		writer.printf("<p>%d results (%f seconds)</p>", num,
				time / 1000000000.0);
	}
}
