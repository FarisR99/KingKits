package com.faris.easysql.mysql.helper;

import com.faris.easysql.mysql.MySQLHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StatementCreateTable extends StatementBuilder {

	private static String statementFormat = "CREATE TABLE %s";

	private String tableName = null;
	private Column[] columns = null;
	private String primaryColumn = null;

	private boolean ifNotExists = false;
	private String engine = null;

	private String likeTable = null;
	private StatementSelectTable selectTable = null;

	public StatementCreateTable(MySQLHandler handler) {
		super(handler);
	}

	public boolean execute() {
		Connection connection = this.sqlHandler.getConnection();
		if (connection != null) {
			PreparedStatement preparedStatement = null;
			String sqlString = this.toSQLString();
			String[] sqlStringSplit = sqlString.contains(System.lineSeparator()) ? sqlString.split(System.lineSeparator()) : new String[]{sqlString};
			boolean isResultSet = true;
			for (String sqlQuery : sqlStringSplit) {
				try {
					if (sqlQuery != null) {
						preparedStatement = connection.prepareStatement(sqlQuery);
						if (!preparedStatement.execute()) isResultSet = false;
					} else if (sqlStringSplit.length <= 1) {
						isResultSet = false;
					}
				} catch (SQLException ex) {
					ex.printStackTrace();
					isResultSet = false;
				} finally {
					if (preparedStatement != null) {
						try {
							preparedStatement.close();
						} catch (SQLException ignored) {
						}
					}
				}
			}
			return isResultSet;
		}
		return false;
	}

	public StatementCreateTable setColumns(Column... columns) {
		this.columns = columns;
		return this;
	}

	public StatementCreateTable setEngine(String engine) {
		this.engine = engine;
		return this;
	}

	public StatementCreateTable setIfNotExists(boolean flag) {
		this.ifNotExists = flag;
		return this;
	}

	public StatementCreateTable setLike(String likeTable) {
		this.likeTable = this.stripSpecialCharacters(likeTable);
		return this;
	}

	public StatementCreateTable setPrimaryColumn(String primaryColumn) {
		this.primaryColumn = this.stripSpecialCharacters(primaryColumn);
		return this;
	}

	public StatementCreateTable setSelect(StatementSelectTable statementSelectTable) {
		this.selectTable = statementSelectTable;
		return this;
	}

	public StatementCreateTable setTable(String table) {
		this.tableName = this.stripSpecialCharacters(table);
		return this;
	}

	@Override
	public String toSQLString() {
		if (this.tableName == null) return null;
		String queryCreate = String.format(statementFormat, (this.ifNotExists ? "IF NOT EXISTS " : ""));
		queryCreate += this.getSpecialCharacter() + this.tableName + this.getSpecialCharacter();

		if (this.columns != null && this.columns.length > 0) {
			queryCreate += " (";
			for (int columnIndex = 0; columnIndex < this.columns.length; columnIndex++) {
				Column column = this.columns[columnIndex];
				queryCreate += column.toString();
				if (columnIndex != this.columns.length - 1) queryCreate += ", ";
			}
			if (this.primaryColumn != null)
				queryCreate += ", PRIMARY KEY (" + this.getSpecialCharacter() + this.primaryColumn + this.getSpecialCharacter() + ")";
			queryCreate += ")";
		}

		queryCreate += (this.likeTable != null ? " LIKE " + this.getSpecialCharacter() + this.likeTable + this.getSpecialCharacter() : (this.selectTable != null && this.selectTable.toSQLString() != null ? " " + this.selectTable.toSQLString() : "")) + ";";

		if (this.engine == null) return queryCreate;
		else return queryCreate + System.lineSeparator() + "ENGINE=" + this.engine + ";";
	}

	public static class Column {
		private String name = "";
		private String type = "";
		private Object defaultValue = null;

		private List<String> attributes = new ArrayList<>();
		private boolean notNull = false;

		public Column(String name, String type) {
			this.name = name;
			this.type = type;
		}

		public Column(String name, String type, boolean notNull) {
			this.name = name;
			this.type = type;
			this.notNull = notNull;
		}

		public Column(String name, String type, Object defaultValue) {
			this.name = name;
			this.type = type;
			this.defaultValue = defaultValue;
		}

		public Column addAttribute(String attribute) {
			this.attributes.add(attribute);
			return this;
		}

		public Column setDefaultValue(Object defaultValue) {
			this.defaultValue = defaultValue;
			return this;
		}

		public Column setNotNull(boolean notNull) {
			this.notNull = notNull;
			return this;
		}

		@Override
		public String toString() {
			if (this.name == null || this.type == null) return "";
			StringBuilder sbColumn = new StringBuilder();
			sbColumn.append(getTableAndColumnCharacter()).append(this.name).append(getTableAndColumnCharacter());
			sbColumn.append(" ");
			sbColumn.append(this.type);
			if (this.notNull) sbColumn.append(" ").append("NOT NULL");
			if (!this.attributes.isEmpty()) {
				for (String attribute : this.attributes) sbColumn.append(" ").append(attribute);
			}
			if (this.defaultValue != null)
				sbColumn.append(" ").append("DEFAULT ").append(this.defaultValue instanceof String ? "'" + this.defaultValue + "'" : this.defaultValue);
			return sbColumn.toString();
		}
	}

}
