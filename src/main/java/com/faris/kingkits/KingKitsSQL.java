package com.faris.kingkits;

import org.bukkit.entity.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author KingFaris10
 */
public class KingKitsSQL {
	private static Connection connection;
	public static boolean sqlEnabled = false;

	private static String sqlHost = null, sqlUsername = null, sqlPassword = null, sqlDatabase = null, sqlTablePrefix = "kk_";
	private static int sqlPort = -1;

	public KingKitsSQL(String sqlHost, int sqlPort, String sqlUsername, String sqlPassword, String sqlDatabase, String sqlTablePrefix) {
		KingKitsSQL.sqlHost = sqlHost;
		KingKitsSQL.sqlPort = sqlPort;
		KingKitsSQL.sqlUsername = sqlUsername;
		KingKitsSQL.sqlPassword = sqlPassword;
		KingKitsSQL.sqlDatabase = sqlDatabase;
		KingKitsSQL.sqlTablePrefix = sqlTablePrefix;
	}

	public void onDisable() {
		try {
			if (connection != null && !connection.isClosed()) connection.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		connection = null;
	}

	public static boolean isInitialised() {
		return sqlHost != null && sqlPort != -1 && sqlDatabase != null && sqlUsername != null && sqlPassword != null;
	}

	public static boolean isOpen() {
		try {
			return connection != null && !connection.isClosed();
		} catch (Exception ex) {
			ex.printStackTrace();
			return connection != null;
		}
	}

	public synchronized static void openConnection() {
		try {
			if (isInitialised() && sqlEnabled)
				connection = DriverManager.getConnection("jdbc:mysql://" + sqlHost + ":" + sqlPort + "/" + sqlDatabase, sqlUsername, sqlPassword);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public synchronized static void closeConnection() {
		try {
			if (connection != null) connection.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private synchronized static void createDefaultTable(String table) throws Exception {
		try {
			PreparedStatement createSql = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + sqlDatabase + "." + table + " (uuid varchar(36), score int(11));");
			createSql.execute();
			createSql.close();
		} catch (Exception ex) {
			throw ex;
		}
	}

	private synchronized static boolean tableContainsPlayer(Player player, String table) {
		try {
			if (isInitialised() && player != null) {
				PreparedStatement searchSql = connection.prepareStatement("SELECT * FROM `" + table + "` WHERE uuid=?;");
				searchSql.setString(1, player.getUniqueId().toString());

				ResultSet searchResult = searchSql.executeQuery();
				boolean containsPlayer = searchResult.next();
				searchSql.close();
				searchResult.close();

				return containsPlayer;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}

	public static void setScore(Player player, int score) {
		if (sqlEnabled && isInitialised() && player != null) {
			try {
				openConnection();
				createDefaultTable((sqlTablePrefix + "score"));
				if (tableContainsPlayer(player, (sqlTablePrefix + "score"))) {
					PreparedStatement scoreSql = connection.prepareStatement("UPDATE `" + (sqlTablePrefix + "score") + "` SET score=? WHERE uuid=?;");
					scoreSql.setInt(1, score);
					scoreSql.setString(2, player.getUniqueId().toString());
					scoreSql.executeUpdate();
					scoreSql.close();
				} else {
					PreparedStatement scoreSql = connection.prepareStatement("INSERT INTO `" + (sqlTablePrefix + "score") + "` values(?," + score + ");");
					scoreSql.setString(1, player.getUniqueId().toString());
					scoreSql.execute();
					scoreSql.close();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				closeConnection();
			}
		}
	}

}
