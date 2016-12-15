package com.faris.kingkits.controller;

import com.faris.easysql.mysql.MySQLDetails;
import com.faris.easysql.mysql.MySQLHandler;
import com.faris.kingkits.KingKits;

import java.sql.Connection;

public class SQLController implements Controller {

	private static final String TABLE_PLAYERS = "kk_players";

	private static SQLController instance = null;

	private MySQLHandler sqlHandler = null;

	private SQLController() {
		if (ConfigController.getInstance().getSQLDetails() == null) {
			ConfigController.getInstance().setSQLDetails(new MySQLDetails());
		}
		this.sqlHandler = MySQLHandler.newInstance(KingKits.getInstance(), ConfigController.getInstance().getSQLDetails());
	}

	@Override
	public void shutdownController() {
		if (this.sqlHandler != null) this.sqlHandler.closeConnection();

		instance = null;
	}

	public void closeConnection() {
		if (this.sqlHandler != null) this.sqlHandler.closeConnection();
	}

	public MySQLHandler getHandler() {
		return this.sqlHandler;
	}

	public String getPlayersTable() {
		return TABLE_PLAYERS;
	}

	public boolean isEnabled() {
		return ConfigController.getInstance().getSQLDetails().isEnabled();
	}

	public Connection openConnection() {
		return this.sqlHandler != null && this.isEnabled() ? this.sqlHandler.openConnection() : null;
	}

	public void setHandler(MySQLHandler handler) {
		this.sqlHandler = handler;
	}

	public static SQLController getInstance() {
		if (instance == null) instance = new SQLController();
		return instance;
	}

	public static boolean hasInstance() {
		return instance != null;
	}

}
