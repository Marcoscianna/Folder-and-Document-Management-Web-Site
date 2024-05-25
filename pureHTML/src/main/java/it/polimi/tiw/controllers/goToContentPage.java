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
import it.polimi.tiw.beans.document;
import it.polimi.tiw.DAO.folderDAO;
import it.polimi.tiw.DAO.documentDAO;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.beans.user;
import it.polimi.tiw.utils.ConnectionHandler;

@WebServlet("/goToContentPage")
public class goToContentPage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
	private Connection connection = null;

	public goToContentPage() {
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
		List<folder> folders;
		List<document> documents;
		folder folder;
		Integer folderId;
		String selectedFolderPath= null;
		try {
			String folderIdString= request.getParameter("folderId");
			folderId = Integer.parseInt(folderIdString);
			folder = folderDAO.findFolderById(folderId);
			selectedFolderPath = folder.getPath() + "/" + folder.getName();
			folders = folderDAO.findFolderByPath(user.getUsername(), selectedFolderPath);
			documents = documentDAO.findDocumentByPath(user.getUsername(), selectedFolderPath);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to recover folders");
			return;
		}


		// Redirect to the Home page and add conferences to the parameters
		String path = "/WEB-INF/content.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("folders", folders);
		ctx.setVariable("folder", folder);
		ctx.setVariable("documents", documents);
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