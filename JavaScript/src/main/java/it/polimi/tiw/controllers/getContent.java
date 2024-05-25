package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.polimi.tiw.beans.folder;
import it.polimi.tiw.beans.document;
import it.polimi.tiw.DAO.folderDAO;
import it.polimi.tiw.DAO.documentDAO;

import it.polimi.tiw.beans.user;
import it.polimi.tiw.utils.ConnectionHandler;

@WebServlet("/getContent")
public class getContent extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public getContent() {
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
		String folderIdString = request.getParameter("folderId");
		folderDAO folderDAO = new folderDAO(connection);
		documentDAO documentDAO = new documentDAO(connection);

		isBadRequest = user.getUsername().isEmpty() || folderIdString.isEmpty();
		Integer folderId;
		try {
			folderId = Integer.parseInt(folderIdString);
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid document ID format");
			return;
		}
		if (isBadRequest) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("missing values");
			return;
		}
		String path = null;
		try {
			path = folderDAO.findFolderById(folderId).getPath()+"/"+ folderDAO.findFolderById(folderId).getName();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		List<folder> folders;
		List<document> documents;
		try {
			folders = folderDAO.findFolderByPath(user.getUsername(), path);
			folders = folderDAO.sortFolders(folders);
			documents = documentDAO.findDocumentByPath(user.getUsername(), path);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Not possible to recover folders");
			return;
		}

		JsonObject jsonResponse = new JsonObject();
		Gson gson = new GsonBuilder().create();
		jsonResponse.add("folders", gson.toJsonTree(folders));
		jsonResponse.add("documents", gson.toJsonTree(documents));
		String json = gson.toJson(jsonResponse);

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