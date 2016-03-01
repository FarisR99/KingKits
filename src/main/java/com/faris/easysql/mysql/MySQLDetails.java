package com.faris.easysql.mysql;

import com.faris.kingkits.helper.util.ObjectUtilities;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

public class MySQLDetails implements ConfigurationSerializable {

	private final String hostname;
	private final int port;
	private final String username;
	private final String password;
	private final String database;

	private boolean enabled = false;

	public MySQLDetails() {
		this("localhost", 3306, "root", "password", "database");
	}

	public MySQLDetails(String hostname, int port, String username, String password, String database) {
		this.hostname = hostname;
		this.port = port;
		this.username = username;
		this.password = password;
		this.database = database;
	}

	public String getDatabase() {
		return this.database;
	}

	public String getHostname() {
		return this.hostname;
	}

	public String getPassword() {
		return this.password;
	}

	public int getPort() {
		return this.port;
	}

	public String getUsername() {
		return this.username;
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public MySQLDetails setEnabled(boolean flag) {
		this.enabled = flag;
		return this;
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> serializedDetails = new LinkedHashMap<>();
		serializedDetails.put("Enabled", this.enabled);
		serializedDetails.put("Host", this.hostname);
		serializedDetails.put("Port", this.port);
		serializedDetails.put("Username", this.username);
		serializedDetails.put("Password", this.password);
		serializedDetails.put("Database", this.database);
		return serializedDetails;
	}

	public static MySQLDetails deserialize(Map<String, Object> serializedDetails) {
		try {
			return new MySQLDetails((String) serializedDetails.get("Host"), (Integer) serializedDetails.get("Port"), (String) serializedDetails.get("Username"), (String) serializedDetails.get("Password"), (String) serializedDetails.get("Database")).setEnabled(ObjectUtilities.getObject(serializedDetails, Boolean.class, "Enabled", false));
		} catch (Exception ex) {
			Bukkit.getServer().getLogger().log(Level.WARNING, "Failed to deserialize the MySQL details", ex);
			return new MySQLDetails();
		}
	}

}
