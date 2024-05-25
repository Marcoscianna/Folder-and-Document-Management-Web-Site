package it.polimi.tiw.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import it.polimi.tiw.beans.folder;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

public class folderDAO {
	private Connection con;

	public folderDAO(Connection connection) {
		this.con = connection;
	}

	public void createFolder(String owner, String name, String path) throws SQLException {
		String query = "INSERT INTO folders (owner, name, date, path) VALUES (?, ?, ?, ?)";
		try (PreparedStatement pstatement = con.prepareStatement(query)) {
			pstatement.setString(1, owner);
			pstatement.setString(2, name);
			pstatement.setDate(3, new java.sql.Date(System.currentTimeMillis()));
			pstatement.setString(4, path);
			pstatement.executeUpdate();
		} finally {
		}
	}

	public void deleteFolder(int id, String owner) throws SQLException {
		String query = "DELETE FROM folders WHERE folderId = ? AND owner = ?";
		try (PreparedStatement pstatement = con.prepareStatement(query)) {
			pstatement.setInt(1, id);
			pstatement.setString(2, owner);
			pstatement.executeUpdate();
		} finally {
		}
	}

	public folder findFolderById(int folderId) throws SQLException {
		folder folder = null;
		String query = "SELECT * FROM folders WHERE folderId = ?";
		try (PreparedStatement pstatement = con.prepareStatement(query)) {
			pstatement.setInt(1, folderId);
			try (ResultSet resultSet = pstatement.executeQuery()) {
				if (resultSet.next()) {
					folder = new folder();
					folder.setFolderId(resultSet.getInt("folderId"));
					folder.setName(resultSet.getString("name"));
					folder.setDate(resultSet.getDate("date"));
					folder.setPath(resultSet.getString("path"));
					folder.setOwner(resultSet.getString("owner"));
				}
			}
		}
		return folder;
	}

	public List<folder> findFolderByPath(String username, String path) throws SQLException {
		List<folder> folders = new ArrayList<folder>();
		String query = "SELECT * FROM folders WHERE path = ? AND owner = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setString(1, path);
			pstatement.setString(2, username);
			result = pstatement.executeQuery();
			while (result.next()) {
				folder folder = new folder();
				folder.setPath(result.getString("path"));
				folder.setFolderId(result.getInt("folderId"));
				folder.setOwner(result.getString("owner"));
				folder.setName(result.getString("name"));
				folder.setDate(result.getDate("date"));

				folders.add(folder);
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
		return folders;
	}

	public List<folder> findFolderByUser(String username) throws SQLException {
		List<folder> folders = new ArrayList<folder>();
		String query = "SELECT * FROM folders WHERE owner = ? ORDER BY name ASC";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setString(1, username);
			result = pstatement.executeQuery();
			while (result.next()) {
				folder folder = new folder();
				folder.setPath(result.getString("path"));
				folder.setFolderId(result.getInt("folderId"));
				folder.setOwner(result.getString("owner"));
				folder.setName(result.getString("name"));
				folder.setDate(new Date(result.getTimestamp("date").getTime()));
				folders.add(folder);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);

		} finally {
			try {
				result.close();
			} catch (Exception e1) {
				throw new SQLException(e1);
			}
			try {
				pstatement.close();
			} catch (Exception e2) {
				throw new SQLException(e2);
			}
		}
		return folders;
	}

	public List<folder> sortFolders(List<folder> folders) {
		Collections.sort(folders, (a, b) -> {
			String[] pathA = (a.getPath()+"/"+a.getName()).split("/");
			String[] pathB = (b.getPath()+"/"+b.getName()).split("/");
			int minLength = Math.min(pathA.length, pathB.length);

			for (int i = 1; i < minLength; i++) {
				if (!pathA[i].equals(pathB[i])) {
					return pathA[i].compareTo(pathB[i]);
				}
			}

			if (pathA.length != pathB.length) {
				if(pathA.length-pathB.length>0) return 1;
				else return -1;
			}

			return a.getName().compareTo(b.getName());
		});
		return folders;
	}
}