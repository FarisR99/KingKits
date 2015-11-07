package com.faris.easysql.mysql.helper;

import com.faris.easysql.mysql.MySQLHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StatementInsertTable extends StatementBuilder {

	private static String statementFormat = "INSERT INTO %s%sVALUES (%s)";

	private String table = null;
	private String columns = null;
	private String values = null;

	public StatementInsertTable(MySQLHandler handler) {
		super(handler);
	}

	public StatementInsertTable addColumns(Column... columns) {
		if (columns != null && columns.length > 0) {
			StringBuilder sbColumns = new StringBuilder();
			StringBuilder sbValues = new StringBuilder();
			for (int i = 0; i < columns.length; i++) {
				sbColumns.append(columns[i].getName());
				sbValues.append(columns[i].getValue() instanceof String ? "'" + columns[i].getValue() + "'" : columns[i].getValue());
				if (i < columns.length - 1) {
					sbColumns.append(", ");
					sbValues.append(", ");
				}
			}
			if (this.columns == null) {
				this.columns = sbColumns.toString();
			} else {
				if (!this.columns.isEmpty()) this.columns += ", " + sbColumns.toString();
				else this.columns = sbColumns.toString();
			}
			if (this.values == null || this.values.isEmpty()) {
				this.values = sbValues.toString();
			} else {
				this.values += ", " + sbValues.toString();
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

	public StatementInsertTable setColumns(Column... columns) {
		if (columns != null && columns.length > 0) {
			StringBuilder sbColumns = new StringBuilder();
			StringBuilder sbValues = new StringBuilder();
			for (int i = 0; i < columns.length; i++) {
				sbColumns.append(columns[i].getName());
				sbValues.append(columns[i].getValue() instanceof String ? "'" + columns[i].getValue() + "'" : columns[i].getValue());
				if (i < columns.length - 1) {
					sbColumns.append(", ");
					sbValues.append(", ");
				}
			}
			this.columns = sbColumns.toString();
			this.values = sbValues.toString();
		} else {
			this.columns = null;
			this.values = null;
		}
		return this;
	}

	public StatementInsertTable setTable(String table) {
		this.table = table == null ? null : (table.equals("*") ? "*" : this.getSpecialCharacter() + table + this.getSpecialCharacter());
		return this;
	}

	@Override
	public String toSQLString() {
		return this.table != null && this.values != null ? String.format(statementFormat, this.table, this.columns != null ? " (" + this.columns + ") " : " ", this.values) + ";" : null;
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
