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
import it.polimi.tiw.beans.folder;
import it.polimi.tiw.DAO.folderDAO;

import it.polimi.tiw.beans.user;
import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.exceptions.missingValuesException;

@WebServlet("/createFolder")
public class createFolder extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public createFolder() {
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
		String name = null;
		String folderIdString = null;
		try {
			folderIdString = request.getParameter("folderId");
			name = request.getParameter("name");
			if (folderIdString.isEmpty() || name.isEmpty()) {
				throw new missingValuesException();
			}
		} catch (missingValuesException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("missing values");
			return;
		}

		if (name.contains("/")) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Invalid / in name");
			return;
		}

		Integer folderId;
		String path;
		try {
			folderId = Integer.parseInt(folderIdString);
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid folder ID format");
			return;
		}

		user user = (user) session.getAttribute("user");

		folder folder = new folder();
		String owner = user.getUsername();

		folderDAO folderDao = new folderDAO(connection);
		try {
			if (folderId != -1) {
				folder = folderDao.findFolderById(folderId);
				if (folder == null) {
					response.sendError(HttpServletResponse.SC_NOT_FOUND, "Folder not found");
					return;
				}
				path = folder.getPath() + "/" + folder.getName();
			} else {
				path = "home";
			}
			try {
				folderDao.createFolder(owner, name, path);
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
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to retrieve folder");
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