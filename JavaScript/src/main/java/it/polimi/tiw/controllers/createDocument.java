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

import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.DAO.documentDAO;
import it.polimi.tiw.DAO.folderDAO;
import it.polimi.tiw.beans.user;
import it.polimi.tiw.beans.folder;


@WebServlet("/createDocument")
public class createDocument extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public createDocument() {
		super();
	}

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// If the user is not logged in (not present in session) redirect to the login
		HttpSession session = request.getSession();
		if (session.isNew() || session.getAttribute("user") == null) {
			String loginpath = getServletContext().getContextPath() + "/index.html";
			response.sendRedirect(loginpath);
			return;
		}
		boolean isBadRequest = false;
		String documentIdString = request.getParameter("documentId");
		String name = request.getParameter("name");
		String summary = request.getParameter("summary");
		String type = request.getParameter("type");
		
		if(name.contains("/")) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Invalid / in name");
			return;
		}
		
		isBadRequest = name.equals(null) || name.isEmpty() || documentIdString.isEmpty() || type.isEmpty() || summary.isEmpty();
		Integer documentId;
		String path;
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

		user user = (user) session.getAttribute("user");
		documentDAO documentDao = new documentDAO(connection);
		folderDAO folderDao = new folderDAO(connection);
		folder folder = new folder();
		String owner = user.getUsername();;

		try {
			folder = folderDao.findFolderById(documentId);
			if (folder == null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "document not found");
				return;
			}
			path = folder.getPath() + "/" + folder.getName();
			try {
				documentDao.createDocument(owner, name, path, summary, type);
			} catch (SQLException e) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "Wrong query");
			}
			if (user.getUsername().equals("")) {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				response.getWriter().println("Resource not found");
				return;
			} else {
				Gson gson = new GsonBuilder().create();
				String json = gson.toJson(user);

				response.setStatus(HttpServletResponse.SC_OK);
				response.setContentType("application/json");
				response.setCharacterEncoding("UTF-8");
				response.getWriter().write(json);
			}
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to retrieve document");
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