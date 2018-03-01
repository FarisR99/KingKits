package com.faris.kingkits.updater;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SpigotUpdater {

	private final String REQUEST_METHOD = "GET";
	private String RESOURCE_ID = "";
	private final String HOST = "https://api.spigotmc.org";
	private final String QUERY = "/legacy/update.php";

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
		RESOURCE_ID = String.valueOf(resourceId);
		this.oldVersion = plugin.getDescription().getVersion();

		if (disabled) {
			this.result = UpdateResult.DISABLED;
			return;
		}

		try {
			this.connection = (HttpURLConnection) new URL(HOST + QUERY + "?resource=" + RESOURCE_ID).openConnection();
		} catch (Exception e) {
			this.result = UpdateResult.FAIL_SPIGOT;
			return;
		}

		this.run();
	}

	public String getCurrentVersion() {
		return this.oldVersion;
	}

	public UpdateResult getResult() {
		return this.result;
	}

	public String getVersion() {
		return this.version;
	}

	private void run() {
		this.connection.setDoOutput(true);
		try {
			this.connection.setRequestMethod(REQUEST_METHOD);
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
			this.version = version.replace("[^A-Za-z]", "").replace("|", "");
			this.versionCheck();
			return;
		}
		this.result = UpdateResult.BAD_RESOURCE_ID;
	}

	public boolean shouldUpdate(String localVersion, String remoteVersion) {
		return !localVersion.equalsIgnoreCase(remoteVersion);
	}

	private void versionCheck() {
		if (this.shouldUpdate(this.oldVersion, this.version)) {
			if (this.oldVersion.contains(".") && this.version.contains(".")) {
				if (versionCompare(this.oldVersion, this.version) >= 0) {
					this.result = UpdateResult.NO_UPDATE;
				} else {
					this.result = UpdateResult.UPDATE_AVAILABLE;
				}
			} else {
				this.result = UpdateResult.UPDATE_AVAILABLE;
			}
		} else {
			this.result = UpdateResult.NO_UPDATE;
		}
	}

	/**
	 * Compare two version strings.
	 * Credits: http://stackoverflow.com/a/6702029/1442718
	 *
	 * @param str1 a string of ordinal numbers separated by decimal points.
	 * @param str2 a string of ordinal numbers separated by decimal points.
	 * @return The result is a negative integer if str1 is numerically less than str2.
	 * The result is a positive integer if str1 is numerically greater than str2.
	 * The result is zero if the strings are numerically equal.
	 */
	private static Integer versionCompare(String str1, String str2) {
		if (str2.contains("-SNAPSHOT") && str2.replace("-SNAPSHOT", "").equals(str1)) return 1;
		if (!(str1.contains("-SNAPSHOT") && str1.replace("-SNAPSHOT", "").equals(str2))) {
			str1 = str1.replaceAll("[^0-9.]", "");
			str2 = str2.replaceAll("[^0-9.]", "");

			String[] vals1 = str1.split("\\.");
			String[] vals2 = str2.split("\\.");
			int i = 0;
			while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) i++;
			if (i < vals1.length && i < vals2.length) {
				int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
				return Integer.signum(diff);
			} else {
				return Integer.signum(vals1.length - vals2.length);
			}
		} else {
			return -1;
		}
	}

}