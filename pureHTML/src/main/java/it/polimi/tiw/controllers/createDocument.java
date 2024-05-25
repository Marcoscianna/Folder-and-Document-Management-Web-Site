package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.DAO.documentDAO;
import it.polimi.tiw.DAO.folderDAO;
import it.polimi.tiw.beans.user;
import it.polimi.tiw.exceptions.missingValuesException;
import it.polimi.tiw.beans.folder;
import java.util.regex.*;

@WebServlet("/createDocument")
public class createDocument extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
	private Connection connection = null;

	public createDocument() {
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

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// If the user is not logged in (not present in session) redirect to the login
		String loginpath = getServletContext().getContextPath() + "/index.html";
		HttpSession session = request.getSession();
		if (session.isNew() || session.getAttribute("user") == null) {
			response.sendRedirect(loginpath);
			return;
		}
		String name = null;
		String folderIdString = null;
		String owner = null;
		String summary = null;
		String type = null;
		try {
			owner = request.getParameter("owner");
			folderIdString = request.getParameter("folderId");
			name = request.getParameter("name");
			summary = request.getParameter("summary");
			type = request.getParameter("type");
			if (folderIdString.isEmpty() || name.isEmpty() || owner.isEmpty() || type.isEmpty() || summary.isEmpty()) {
				throw new missingValuesException();
			}
		} catch (missingValuesException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("missing values");
			return;
		}

		Pattern pattern = Pattern.compile("[^a-zA-Z0-9 ]");
		Matcher matcher = pattern.matcher(name);
		Matcher matcher1 = pattern.matcher(summary);
		Matcher matcher2 = pattern.matcher(type);
		if (matcher.find() || matcher1.find() || matcher2.find()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid special character");
			return;
		}
		Integer folderId;
		try {
			folderId = Integer.parseInt(folderIdString);
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid document ID format");
			return;
		}

		folderDAO folderDao = new folderDAO(connection);
		try {
			folder folder = folderDao.findFolderById(folderId);
			if (folder == null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "document not found");
				return;
			}
			if (!folder.getOwner().equals(owner)) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "Not permitted");
				return;
			}
			documentDAO documentDao = new documentDAO(connection);
			String path = folder.getPath() + "/" + folder.getName();
			try {
				documentDao.createDocument(owner, name, path, summary, type);
			} catch (SQLException e) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "Wrong query");
			}
			user user = (user) session.getAttribute("user");
			if (user == null) {
				ServletContext servletContext = getServletContext();
				final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
				ctx.setVariable("errorMsg", "Incorrect username or password");
				path = "/index.html";
				templateEngine.process(path, ctx, response.getWriter());
			} else {
				request.getSession().setAttribute("user", user);
				path = getServletContext().getContextPath() + "/goToContentManagementPage";
				response.sendRedirect(path);
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