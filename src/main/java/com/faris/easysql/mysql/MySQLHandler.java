package com.faris.easysql.mysql;

import com.faris.easysql.main.databases.MySQL;
import com.faris.easysql.mysql.helper.StatementCreateTable;
import com.faris.easysql.mysql.helper.StatementDropTable;
import org.bukkit.plugin.*;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySQLHandler {

	private MySQL mySQL = null;
	private final MySQLDetails mySQLDetails;

	/**
	 * Create a new instance of the MySQL handler.
	 * <p></p>
	 * Use {@link MySQLHandler#newInstance(Plugin, MySQLDetails)} to create a new instance.
	 *
	 * @param plugin The plugin.
	 * @param mySQLDetails The MySQL details.
	 */
	private MySQLHandler(Plugin plugin, MySQLDetails mySQLDetails) {
		this.mySQL = new MySQL(plugin, mySQLDetails.getHostname(), String.valueOf(mySQLDetails.getPort()), mySQLDetails.getDatabase(), mySQLDetails.getUsername(), mySQLDetails.getPassword());
		this.mySQLDetails = mySQLDetails;
	}

	public boolean checkConnection() {
		try {
			return this.mySQL.checkConnection();
		} catch (SQLException ex) {
			return false;
		}
	}

	public boolean closeConnection() {
		try {
			return this.mySQL.getConnection() != null && this.mySQL.closeConnection();
		} catch (SQLException ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public boolean createTable(boolean ifNotExists, String tableName, String primaryColumn, StatementCreateTable.Column... columns) {
		return new StatementCreateTable(this).setTable(tableName).setIfNotExists(ifNotExists).setColumns(columns).setPrimaryColumn(primaryColumn).execute();
	}

	public boolean deleteTables(String... tables) {
		StatementDropTable statementDropTable = new StatementDropTable(this);
		statementDropTable.setTables(tables);
		return statementDropTable.execute();
	}

	/**
	 * Check if a table exists in the database.
	 *
	 * @param tableName The table name.
	 * @return Whether the table exists in the database or not.
	 */
	public boolean doesTableExist(String tableName) {
		Connection connection = this.getConnection();
		if (connection != null) {
			ResultSet resultSet = null;
			try {
				DatabaseMetaData meta = connection.getMetaData();
				resultSet = meta.getTables(null, null, tableName, null);
				return resultSet.next();
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				if (resultSet != null) {
					try {
						resultSet.close();
					} catch (Exception ignored) {
					}
				}
			}
		}
		return false;
	}

	/**
	 * Get an open connection.
	 * If the connection is not opened/is null, it tries to open the connection. If it fails, it returns null.
	 *
	 * @return An open connection, or null if failed to connect.
	 */
	public Connection getConnection() {
		return this.checkConnection() ? this.mySQL.getConnection() : this.openConnection();
	}

	/**
	 * Get the MySQL details.
	 *
	 * @return MySQL details.
	 */
	public MySQLDetails getDetails() {
		return this.mySQLDetails;
	}

	/**
	 * Get the MySQL instance.
	 *
	 * @return The MySQL instance.
	 */
	public MySQL getMySQL() {
		return this.mySQL;
	}

	/**
	 * Attempts to open a connection.
	 * Prints an error if failed to connect.
	 *
	 * @return The open connection, or null if failed to connect.
	 */
	public Connection openConnection() {
		try {
			return this.mySQL.openConnection();
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * Check if the MySQL details are valid or not.
	 *
	 * @return Whether the plugin successfully connected to the database.
	 */
	public boolean testConnection() {
		try {
			if (!this.checkConnection()) {
				if (this.mySQL.openConnection() == null) return false;
				if (this.mySQL.getConnection() != null) this.mySQL.closeConnection();
			}
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	/**
	 * Create a new MySQL handler.
	 *
	 * @param plugin The plugin to attach the handler to.
	 * @param mySQLDetails The MySQL details.
	 * @return A new MySQL handler instance.
	 */
	public static MySQLHandler newInstance(Plugin plugin, MySQLDetails mySQLDetails) {
		if (plugin == null) throw new IllegalArgumentException("Plugin instance cannot be null");
		if (mySQLDetails == null) throw new IllegalArgumentException("SQL details cannot be null");
		return new MySQLHandler(plugin, mySQLDetails);
	}

}
