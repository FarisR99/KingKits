package com.faris.kingkits.updater;

import org.bukkit.plugin.java.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

/**
 * @author PatoTheBest
 */
public class SpigotUpdater {

	private JavaPlugin plugin;
	private final String API_KEY = "98BE0FE67F88AB82B4C197FAF1DC3B69206EFDCC4D3B80FC83A00037510B99B4";
	private final String REQUEST_METHOD = "POST";
	private String RESOURCE_ID = "";
	private final String HOST = "http://www.spigotmc.org";
	private final String QUERY = "/api/general.php";
	private String WRITE_STRING;

	private String version;
	private String oldVersion;

	private SpigotUpdater.UpdateResult result = SpigotUpdater.UpdateResult.DISABLED;

	private HttpURLConnection connection;

	public enum UpdateResult {
		NO_UPDATE,
		DISABLED,
		FAIL_SPIGOT,
		FAIL_NO_VERSION,
		BAD_RESOURCE_ID,
		UPDATE_AVAILABLE
	}

	public SpigotUpdater(JavaPlugin plugin, Integer resourceId, boolean disabled) {
		RESOURCE_ID = resourceId + "";
		this.plugin = plugin;
		this.oldVersion = this.plugin.getDescription().getVersion();

		if (disabled) {
			this.result = UpdateResult.DISABLED;
			return;
		}

		try {
			this.connection = (HttpURLConnection) new URL(HOST + QUERY).openConnection();
		} catch (IOException e) {
			this.result = UpdateResult.FAIL_SPIGOT;
			return;
		}

		WRITE_STRING = "key=" + API_KEY + "&resource=" + RESOURCE_ID;
		this.run();
	}

	private void run() {
		this.connection.setDoOutput(true);
		try {
			this.connection.setRequestMethod(REQUEST_METHOD);
			this.connection.getOutputStream().write(WRITE_STRING.getBytes("UTF-8"));
		} catch (ProtocolException e1) {
			this.result = UpdateResult.FAIL_SPIGOT;
		} catch (UnsupportedEncodingException e) {
			this.result = UpdateResult.FAIL_SPIGOT;
		} catch (IOException e) {
			this.result = UpdateResult.FAIL_SPIGOT;
		}
		String version;
		try {
			version = new BufferedReader(new InputStreamReader(this.connection.getInputStream())).readLine();
		} catch (Exception e) {
			this.result = UpdateResult.BAD_RESOURCE_ID;
			return;
		}
		if (version.length() <= 7) {
			this.version = version;
			version.replace("[^A-Za-z]", "").replace("|", "");
			this.versionCheck();
			return;
		}
		this.result = UpdateResult.BAD_RESOURCE_ID;
	}

	private void versionCheck() {
		if (this.shouldUpdate(this.oldVersion, this.version)) {
			this.result = UpdateResult.UPDATE_AVAILABLE;
		} else {
			this.result = UpdateResult.NO_UPDATE;
		}
	}

	public boolean shouldUpdate(String localVersion, String remoteVersion) {
		return !localVersion.equalsIgnoreCase(remoteVersion);
	}

	public UpdateResult getResult() {
		return this.result;
	}

	public String getVersion() {
		return this.version;
	}

}
