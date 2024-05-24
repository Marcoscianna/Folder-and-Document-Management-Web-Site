package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.polimi.tiw.beans.folder;
import it.polimi.tiw.DAO.folderDAO;
import it.polimi.tiw.DAO.documentDAO;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.beans.user;
import it.polimi.tiw.beans.document;
import it.polimi.tiw.utils.ConnectionHandler;

@WebServlet("/selectFolder")
public class selectFolder extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
	private Connection connection = null;

	public selectFolder() {
		super();
	}

	public void init() throws ServletException {
		ServletContext servletContext = getServletContext();
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
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

		user user = (user) session.getAttribute("user");
		folderDAO folderDAO = new folderDAO(connection);
		documentDAO documentDAO = new documentDAO(connection);
		document document;
		List<folder> folders;
		String currentFolderString = request.getParameter("folderId");
		String documentIdString = request.getParameter("documentId");
		Integer currentFolder;
		Integer documentId;
		folder actualFolder = null;
		try { 
			currentFolder = Integer.parseInt(currentFolderString);
			documentId = Integer.parseInt(documentIdString);
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid document ID format");
			return;
		}
		try {
			folders = folderDAO.findFolderByUser(user.getUsername());
			folders = folderDAO.sortFolders(folders);
			document = documentDAO.findDocumentById(documentId);
			for (folder folder : folders) {
				if (folder.getFolderId() == currentFolder) {
					folder.setSelected(true);
					actualFolder = folder;
				}
			}
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to recover folders");
			return;
		}

		// Redirect to the Home page and add conferences to the parameters
		String path = "/WEB-INF/home.html";
		Integer move =1;
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("folders", folders);
		ctx.setVariable("move", move);
		ctx.setVariable("document", document);
		ctx.setVariable("actualFolder", actualFolder);
		templateEngine.process(path, ctx, response.getWriter());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}