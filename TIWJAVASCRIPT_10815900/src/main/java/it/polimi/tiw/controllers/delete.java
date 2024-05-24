package it.polimi.tiw.controllers;

import it.polimi.tiw.beans.user;
import it.polimi.tiw.DAO.documentDAO;
import it.polimi.tiw.DAO.folderDAO;
import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.beans.folder;
import it.polimi.tiw.beans.document;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet("/delete")
public class delete extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;

	public delete() {
		super();
	}

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// If the user is not logged in (not present in session) redirect to the login
		HttpSession session = request.getSession();
		if (session.isNew() || session.getAttribute("user") == null) {
			String loginpath = getServletContext().getContextPath() + "/index.html";
			response.sendRedirect(loginpath);
			return;
		}
		user user = (user) request.getSession().getAttribute("user");
		String idString = request.getParameter("id");
		String isFolder = request.getParameter("isFolder");
		Integer id;
		try {
			id = Integer.parseInt(idString);
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid folder ID format");
			return;
		}

		try {
			if (isFolder.equals("false")) {
				deleteDocument(id, user);
			} else {
				deleteAllDocuments(id, user);
				deleteFolder(id, user);
			}
		} catch (Exception e) {
		}
	}

	public void deleteDocument(int id, user user) {
		documentDAO documentDAO = new documentDAO(connection);
		try {
			documentDAO.deleteDocument(id, user.getUsername());
		} catch (Exception e) {
		}
	}

	public void deleteAllDocuments(int folderId, user user) {
		folderDAO folderDAO = new folderDAO(connection);
		documentDAO documentDAO = new documentDAO(connection);

		try {
			// Trova il percorso della cartella
			String folderPath = folderDAO.findFolderById(folderId).getPath() + "/"
					+ folderDAO.findFolderById(folderId).getName();
			// Elimina i documenti nella cartella corrente
			List<document> documentsInFolder = documentDAO.findDocumentByPath(user.getUsername(), folderPath);
			for (document document : documentsInFolder) {
				documentDAO.deleteDocument(document.getDocumentId(), user.getUsername());
			}
			// Trova le sottocartelle e ripete il processo ricorsivamente
			List<folder> subfolders = folderDAO.findFolderByPath(user.getUsername(), folderPath);
			for (folder subfolder : subfolders) {
				deleteAllDocuments(subfolder.getFolderId(), user);
			}
		} catch (Exception e) {
			e.printStackTrace(); 
		}
	}

	public void deleteFolder(int folderId, user user) {
		folderDAO folderDAO = new folderDAO(connection);
		try {
			String folderPath = folderDAO.findFolderById(folderId).getPath() + "/"
					+ folderDAO.findFolderById(folderId).getName();
			List<folder> subfolders = folderDAO.findFolderByUser(user.getUsername());
			for (folder folder : subfolders) {
				if (folder.getPath().contains(folderPath)) {
					folderDAO.deleteFolder(folder.getFolderId(), user.getUsername());
				}
			}
			folderDAO.deleteFolder(folderId, user.getUsername());
		} catch (Exception e) {
			e.printStackTrace(); 
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