package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.validator.routines.EmailValidator;

import it.polimi.tiw.beans.user;
import it.polimi.tiw.DAO.userDAO;
import it.polimi.tiw.utils.ConnectionHandler;
import org.apache.commons.lang.StringEscapeUtils;

@WebServlet("/registerUser")
@MultipartConfig
public class registerUser extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public registerUser() {
		super();
	}

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String email;
		String username;
		String password;
		String password2;

		try {
			email = request.getParameter("email").trim();
			username = request.getParameter("username").trim();
			password = StringEscapeUtils.escapeJava(request.getParameter("password")).trim();
			password2 = StringEscapeUtils.escapeJava(request.getParameter("password2")).trim();
			boolean validEmail = EmailValidator.getInstance().isValid(email); 
			if (!validEmail)
				throw new Exception("Email not valid");

			if (email == null || username == null || password == null
					|| password2 == null || email.isEmpty() || username.isEmpty()
					|| password.isEmpty() || password2.isEmpty()) {
				throw new Exception("Missing or empty credential value");
			}

		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(e.getMessage());
			return;
		}

		userDAO userDao = new userDAO(connection);

		try {
			if (!userDao.checkUsernameAvailability(username)) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Username duplicate");
				return;
			}

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

		if (!userDao.repeatPasswordField( password , password2)) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Password and Confirm password are different");
			return;
		}

		user userBean = new user();
		userBean.setEmail(email);
		userBean.setPassword(password);
		userBean.setUsername(username);

		String userRegistered = userDao.registration(userBean);

		if (userRegistered.equals("SUCCESS")) {
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().println(userRegistered);
		} else {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Error in creating an account");
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