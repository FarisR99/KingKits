package com.faris.easysql.mysql.helper;

import com.faris.easysql.mysql.MySQLHandler;
import com.faris.kingkits.helper.Debugger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StatementUpdateTable extends StatementBuilder {

	private static String statementFormat = "UPDATE %s SET %s WHERE %s=%s";

	private String table = null;
	private String values = null;
	private String whereKey = null;
	private Object whereValue = null;

	public StatementUpdateTable(MySQLHandler handler) {
		super(handler);
	}

	public StatementUpdateTable addColumns(Column... columns) {
		if (columns != null && columns.length > 0) {
			StringBuilder sbValues = new StringBuilder();
			for (int i = 0; i < columns.length; i++) {
				sbValues.append(this.getSpecialCharacter()).append(columns[i].getName()).append(this.getSpecialCharacter()).append("=").append(columns[i].getValue() instanceof String ? "'" + columns[i].getValue() + "'" : columns[i].getValue());
				if (i < columns.length - 1) sbValues.append(",");
			}
			if (this.values == null || this.values.isEmpty()) {
				this.values = sbValues.toString();
			} else {
				this.values += "," + sbValues.toString();
			}
		}
		return this;
	}

	public boolean execute() {
		Connection connection = this.sqlHandler.getConnection();
		if (connection != null) {
			PreparedStatement preparedStatement = null;
			try {
				String sqlString = this.toSQLString();
				Debugger.debugMessage("SQL: " + sqlString);
				if (sqlString != null) {
					preparedStatement = connection.prepareStatement(sqlString);
					return preparedStatement.execute();
				}
			} catch (SQLException ex) {
				ex.printStackTrace();
			} finally {
				if (preparedStatement != null) {
					try {
						preparedStatement.close();
					} catch (SQLException ignored) {
					}
				}
			}
		}
		return false;
	}

	public StatementUpdateTable setColumns(Column... columns) {
		if (columns != null && columns.length > 0) {
			StringBuilder sbValues = new StringBuilder();
			for (int i = 0; i < columns.length; i++) {
				sbValues.append(this.getSpecialCharacter()).append(columns[i].getName()).append(this.getSpecialCharacter()).append("=").append(columns[i].getValue() instanceof String ? "'" + columns[i].getValue() + "'" : columns[i].getValue());
				if (i < columns.length - 1) sbValues.append(",");
			}
			this.values = sbValues.toString();
		} else {
			this.values = null;
		}
		return this;
	}

	public StatementUpdateTable setTable(String table) {
		this.table = table == null ? null : (table.equals("*") ? "*" : this.getSpecialCharacter() + table + this.getSpecialCharacter());
		return this;
	}

	public StatementUpdateTable setWhere(String key, Object value) {
		this.whereKey = key;
		this.whereValue = value;
		return this;
	}

	@Override
	public String toSQLString() {
		return this.table != null && this.values != null && this.whereKey != null ? String.format(statementFormat, this.table, this.values, this.getSpecialCharacter() + this.whereKey + this.getSpecialCharacter(), this.whereValue != null ? (this.whereValue instanceof String ? "'" + this.whereValue + "'" : this.whereValue) : "null") + ";" : null;
	}

	public static class Column {
		private String name = null;
		private Object value = null;

		public Column(String name, Object value) {
			this.name = name;
			this.value = value;
		}

		public String getName() {
			return this.name;
		}

		public Object getValue() {
			return this.value;
		}

		@Override
		public String toString() {
			return this.name;
		}
	}

}
