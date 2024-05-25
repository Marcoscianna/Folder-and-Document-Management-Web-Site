package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.polimi.tiw.beans.document;

import it.polimi.tiw.DAO.documentDAO;

import it.polimi.tiw.beans.user;
import it.polimi.tiw.utils.ConnectionHandler;

@WebServlet("/getDocument")
public class getDocument extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public getDocument() {
		super();
	}

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// If the user is not logged in (not present in session) redirect to the login
		String loginpath = getServletContext().getContextPath() + "/index.html";
		HttpSession session = request.getSession();
		if (session.isNew() || session.getAttribute("user") == null) {
			response.sendRedirect(loginpath);
			return;
		}

		boolean isBadRequest = false;
		user user = (user) session.getAttribute("user");
		String documentIdString = request.getParameter("documentId");
		documentDAO documentDAO = new documentDAO(connection);

		isBadRequest = user.getUsername().isEmpty() || documentIdString.isEmpty();
		Integer documentId;
		try {
			documentId = Integer.parseInt(documentIdString);
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid document ID format");
			return;
		}
		if (isBadRequest) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("missing values");
			return;
		}
		document document;
		try {
			document = documentDAO.findDocumentById(documentId);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Not possible to recover the document");
			return;
		}

		Gson gson = new GsonBuilder().create();
		String json = gson.toJson(document);

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json);
	}


	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}