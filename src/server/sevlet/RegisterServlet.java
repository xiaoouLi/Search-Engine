// Xiaoou, Dec,2013
// xli65@usfca.edu

package server.sevlet;

import static util.HTMLRenderHelper.*;
import static util.Precondition.*;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * RegisterServlet Provides registration form, and handles form response.
 */
@SuppressWarnings("serial")
public class RegisterServlet extends BaseServlet {

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		// checkMessagesFromRequest(request);
		PrintWriter writer = response.getWriter();
		prepareResponse("Register New User", writer);

		String command = request.getParameter("cmd");
		if (!checkStringNotBlank(command)) {
			renderMessages(writer, messages);
			renderRegistrationForm(writer);
			renderHomePageLink(writer);
			finishResponse(response);
			return;
		} else {
			switch (command) {
			case "register": {
				String username = request.getParameter("username");
				String password = request.getParameter("password");

				if (!validateUsername(username)) {
					renderMessages(writer, messages);
					renderRegistrationForm(writer);
					renderHomePageLink(writer);
					finishResponse(response);
					return;
				}

				if (!validatePassword(password)) {
					renderMessages(writer, messages);
					renderRegistrationForm(writer);
					renderHomePageLink(writer);
					finishResponse(response);
					return;
				}

				Status status = db.registerUser(username, password);
				logger.debug("Status: " + status.message());

				switch (status) {

				case OK:
					logger.debug("Redirecting user to login page.");
					redirectTo(response, "/?message=finish_register");
					return;

				case DUPLICATE_USER:
					messages.add("Error: username is already taken.");
					break;

				case SQL_EXCEPTION:
					messages.add("Error: there's a sql exception.");
					break;

				default:
					messages.add("Error: can not register now, please try again later.");
				}
			}
			default:
				logger.debug("Command " + command + "is not supported");
			}

			renderMessages(writer, messages);
			renderRegistrationForm(writer);
			renderHomePageLink(writer);
			finishResponse(response);
			return;
		}
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		doGet(request, response);
	}
}