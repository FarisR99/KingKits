package com.faris.easysql.main.databases;

import com.faris.easysql.main.Database;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;

/**
 * Connects to and uses a SQLite database
 *
 * @author tips48
 */
public class SQLite extends Database {

	private final String dbLocation;

	/**
	 * Creates a new SQLite instance
	 *
	 * @param plugin Plugin instance
	 * @param dbLocation Location of the Database (Must end in .db)
	 */
	public SQLite(Plugin plugin, String dbLocation) {
		super(plugin);
		this.dbLocation = dbLocation;
	}

	@Override
	public Connection openConnection() throws SQLException, ClassNotFoundException {
		if (this.checkConnection()) return this.connection;
		if (!this.plugin.getDataFolder().exists()) this.plugin.getDataFolder().mkdirs();
		File file = new File(this.plugin.getDataFolder(), this.dbLocation);
		if (!(file.exists())) {
			try {
				file.createNewFile();
			} catch (IOException ex) {
				this.plugin.getLogger().log(Level.SEVERE, "Unable to create database!", ex);
			}
		}
		Class.forName("org.sqlite.JDBC");
		this.connection = DriverManager.getConnection("jdbc:sqlite:" + this.plugin.getDataFolder().toPath().toString() + "/" + this.dbLocation);
		return this.connection;
	}

}
