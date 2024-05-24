package it.polimi.tiw.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import it.polimi.tiw.beans.user;

public class userDAO {
	private Connection con;

	public userDAO(Connection connection) {
		this.con = connection;
	}

	public user checkUser(String username, String password) throws SQLException {
		user user = null;
		String query = "SELECT * FROM user WHERE username = ? and password = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;

		try {
			pstatement = con.prepareStatement(query);
			pstatement.setString(1, username);
			pstatement.setString(2, password);
			result = pstatement.executeQuery();
			while (result.next()) {
				user = new user();
				user.setEmail(result.getString("email"));
				user.setUsername(result.getString("username"));
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
		return user;
	}

	public boolean checkUsernameAvailability(String username) throws SQLException {
		String query = "SELECT COUNT(*) FROM user WHERE username = ?";
		try (PreparedStatement pstatement = con.prepareStatement(query)) {
			pstatement.setString(1, username);
			try (ResultSet result = pstatement.executeQuery()) {
				if (result.next()) {
					int count = result.getInt(1);
					return count == 0; // Restituisce true se il nome utente non è già presente nel database
				}
			}
		}
		return false; // Se qualcosa va storto, restituisci false per essere prudente
	}

	public boolean repeatPasswordField(String password, String repeatPassword) {
		return password.equals(repeatPassword);
	}
	public String registration(user userBean) {
		String email = userBean.getEmail();
		String username = userBean.getUsername();
		String password = userBean.getPassword();

		String query = "INSERT INTO user(username,password,email) VALUES (?,?,?)"; 
		try(PreparedStatement pstatement = con.prepareStatement(query);){
			pstatement.setString(1, username);
			pstatement.setString(2, password);
			pstatement.setString(3, email);

			int i= pstatement.executeUpdate();

			if (i!=0) 
				return "SUCCESS";
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		return "Oops.. Something went wrong there..!"; 
	}

}
