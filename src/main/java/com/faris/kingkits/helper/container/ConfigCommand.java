package com.faris.kingkits.helper.container;

public class ConfigCommand {
	private String command = "";
	private String description = "";
	private String config = "";

	/**
	 * Create a new instance of ConfigCommands *
	 */
	public ConfigCommand(String strCommand, String strDescription, String config) {
		this.command = strCommand;
		this.description = strDescription;
		this.config = config;
	}

	/**
	 * Get the config type *
	 */
	public String getConfig() {
		return this.config;
	}

	/**
	 * Get the config command *
	 */
	public String getCommand() {
		return this.command;
	}

	/**
	 * Get the config command's config key *
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * Returns the command and config key as a String *
	 */
	public String toString() {
		return this.command + ":" + this.description;
	}

}
