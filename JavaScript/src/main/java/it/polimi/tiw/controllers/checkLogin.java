package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.annotation.MultipartConfig;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;

import it.polimi.tiw.beans.user;
import it.polimi.tiw.DAO.userDAO;
import it.polimi.tiw.utils.ConnectionHandler;

@WebServlet("/checkLogin")
@MultipartConfig
public class checkLogin extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public checkLogin() {
		super();
	}

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		String username;
		String password;
		username = request.getParameter("username").trim(); 
		password = StringEscapeUtils.escapeJava(request.getParameter("password")).trim();
		if (username == null || password == null || username.isEmpty() || password.isEmpty() ) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Missing or empty credential value");
			return;
		}
		// query db to authenticate for user
		userDAO userDao = new userDAO(connection);
		user user;
		try {
			user = userDao.checkUser(username, password);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Internal server error, retry later");
			return;
		}

		// If the user exists, add info to the session and go to home page, otherwise return an error status code and message
		if (user == null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().println("Incorrect credentials");
		} else {
			request.getSession().setAttribute("user", user);
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().println(username);
		}
	}

	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}