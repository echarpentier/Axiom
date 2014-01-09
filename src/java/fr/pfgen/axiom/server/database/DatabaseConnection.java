package fr.pfgen.axiom.server.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 
 */
@Deprecated
public class DatabaseConnection {

	private static Connection connection = null;

	private DatabaseConnection() {
		
	}

	/**
	 * Get the unique instance of <tt>DatabaseConnection</tt>
	 * 
	 * @return the connection to the database.
	 */
	public static Connection getInstance() {
		if (connection == null) {
			try {
				// Loading driver
				try {
					Class.forName("com.mysql.jdbc.Driver");
				} catch (ClassNotFoundException cnfe) {
					connection = null;
					cnfe.printStackTrace();
					return null;
				}
								
				Connection handle = null;

				// Creating connection
				try {
					handle = DriverManager.getConnection("jdbc:mysql://localhost/Axiom_db","axiom","axiom");
				} catch (SQLException se) {
					connection = null;
					se.printStackTrace();
					return null;
				}
				connection = handle;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return connection;
	}

	public static void closeConnection() {
		connection = null;
	}

	public static boolean hasValidConnection() {
		if (getInstance() != null) return true;
		return false;
	}

	public static boolean commit() {
		if (hasValidConnection()) {
			PreparedStatement ps;
			try {
				ps = connection.prepareStatement("COMMIT;");
				ps.executeUpdate();
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		} else {
			return false;
		}
	}

	public static boolean begin() {
		if (hasValidConnection()) {
			PreparedStatement ps;
			try {
				ps = connection.prepareStatement("START TRANSACTION;");
				ps.executeUpdate();
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		} else {
			return false;
		}
	}

	public static boolean rollback() {
		if (hasValidConnection()) {
			PreparedStatement ps;
			try {
				ps = connection.prepareStatement("ROLLBACK;");
				ps.executeUpdate();
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		} else {
			return false;
		}
	}
}
