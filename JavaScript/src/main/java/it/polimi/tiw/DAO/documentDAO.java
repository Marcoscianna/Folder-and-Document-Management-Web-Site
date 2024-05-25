package it.polimi.tiw.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import it.polimi.tiw.beans.document;

public class documentDAO {
	private Connection con;

	public documentDAO(Connection connection) {
		this.con = connection;
	}

	public void createDocument(String owner, String name, String path, String summary, String type)
			throws SQLException {
		String query = "INSERT INTO documents (owner, name, date, path, summary, type) VALUES (?, ?, ?, ?, ?, ?)";
		try (PreparedStatement pstatement = con.prepareStatement(query)) {
			pstatement.setString(1, owner);
			pstatement.setString(2, name);
			pstatement.setDate(3, new java.sql.Date(System.currentTimeMillis()));
			pstatement.setString(4, path);
			pstatement.setString(5, summary);
			pstatement.setString(6, type);
			pstatement.executeUpdate();
		} finally {
		}
	}

	public void deleteDocument(int id, String owner) throws SQLException {
		String query = "DELETE FROM documents WHERE documentId = ? AND owner = ?";
		try (PreparedStatement pstatement = con.prepareStatement(query)) {
			pstatement.setInt(1, id);
			pstatement.setString(2, owner);
			pstatement.executeUpdate();
		} finally {
		}
	}

	public void moveDocument(String newPath, int documentId) throws SQLException {
		String query = "UPDATE documents SET path = ? WHERE documentId = ?";
		try (PreparedStatement pstatement = con.prepareStatement(query)) {
			pstatement.setString(1, newPath);
			pstatement.setInt(2, documentId);
			pstatement.executeUpdate();
		}
	}

	public List<document> findDocumentByPath(String username, String path) throws SQLException {
		List<document> documents = new ArrayList<>();
		String query = "SELECT * FROM documents WHERE path = ? AND owner = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setString(1, path);
			pstatement.setString(2, username);
			result = pstatement.executeQuery();
			while (result.next()) {
				document document = new document();
				document.setPath(result.getString("path"));
				document.setDocumentId(result.getInt("documentId"));
				document.setOwner(result.getString("owner"));
				document.setSummary(result.getString("summary"));
				document.setType(result.getString("type"));
				document.setName(result.getString("name"));
				document.setDate(result.getDate("date"));

				documents.add(document);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		} finally {
			try {
				if (result != null) {
					result.close();
				}
			} catch (Exception e1) {
				throw new SQLException(e1);
			}
			try {
				if (pstatement != null) {
					pstatement.close();
				}
			} catch (Exception e2) {
				throw new SQLException(e2);
			}
		}
		return documents;
	}

	public document findDocumentById(Integer documentId) throws SQLException {
		String query = "SELECT * FROM documents WHERE documentId = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, documentId);
			result = pstatement.executeQuery();
			if (result.next()) {
				document document = new document();
				document.setDocumentId(result.getInt("documentId"));
				document.setOwner(result.getString("owner"));
				document.setSummary(result.getString("summary"));
				document.setType(result.getString("type"));
				document.setName(result.getString("name"));
				document.setPath(result.getString("path"));
				document.setDate(result.getDate("date"));
				return document;
			} else {
				return null; // Nessun documento trovato con l'ID specificato
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		} finally {
			try {
				if (result != null) {
					result.close();
				}
				if (pstatement != null) {
					pstatement.close();
				}
			} catch (SQLException e) {
				throw new SQLException(e);
			}
		}
	}
}