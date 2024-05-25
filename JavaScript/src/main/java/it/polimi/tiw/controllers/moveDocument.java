package it.polimi.tiw.controllers;

import it.polimi.tiw.beans.document;
import it.polimi.tiw.beans.user;
import it.polimi.tiw.DAO.documentDAO;
import it.polimi.tiw.DAO.folderDAO;
import it.polimi.tiw.utils.ConnectionHandler;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;


@WebServlet("/moveDocument")
public class moveDocument extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private Connection connection;

	public moveDocument() {
		super();
	}

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String loginpath = getServletContext().getContextPath() + "/index.html";
		HttpSession session = request.getSession();
		if (session.isNew() || session.getAttribute("user") == null) {
			response.sendRedirect(loginpath);
			return;
		}

	
		int documentId = Integer.parseInt(request.getParameter("documentId"));
		int folderId = Integer.parseInt(request.getParameter("folderId"));
		user user = (user) request.getSession().getAttribute("user");

		try {
			documentDAO documentDao = new documentDAO(connection);
			folderDAO folderDao = new folderDAO(connection);
			// Ottieni il documento da spostare
			String path = folderDao.findFolderById(folderId).getPath()+"/"+folderDao.findFolderById(folderId).getName();
			document document = documentDao.findDocumentById(documentId);
			if (document == null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "Documento non trovato.");
				return;
			}

			// Effettua lo spostamento del documento
			documentDao.moveDocument(path, documentId);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to recover folders");
			return;
		}
		// Reindirizza alla pagina dei contenuti aggiornata
		
		Gson gson = new GsonBuilder().create();
		String json = gson.toJson(user);

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