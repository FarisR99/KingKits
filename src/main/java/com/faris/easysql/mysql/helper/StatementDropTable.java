package com.faris.easysql.mysql.helper;

import com.faris.easysql.mysql.MySQLHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StatementDropTable extends StatementBuilder {

	private static String statementFormat = "DROP %s IF EXISTS %s";

	private String[] tables = new String[0];

	public StatementDropTable(MySQLHandler handler) {
		super(handler);
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

	public StatementDropTable setTable(String table) {
		if (table != null) {
			this.tables = new String[1];
			this.tables[0] = table;
		} else {
			this.tables = new String[0];
		}
		return this;
	}

	public StatementDropTable setTables(String... tables) {
		if (tables != null && tables.length > 0) {
			this.tables = tables;
		} else {
			this.tables = new String[0];
		}
		return null;
	}

	@Override
	public String toSQLString() {
		if (this.tables.length == 0) return null;
		StringBuilder sbTables = new StringBuilder();
		for (int i = 0; i < this.tables.length; i++) {
			sbTables.append(this.getSpecialCharacter()).append(this.tables[i]).append(this.getSpecialCharacter());
			if (i < this.tables.length - 1) sbTables.append(", ");
		}
		return this.tables.length > 0 ? String.format(statementFormat, this.tables.length > 1 ? "TABLES" : "TABLE", sbTables.toString()) + ";" : null;
	}

}
